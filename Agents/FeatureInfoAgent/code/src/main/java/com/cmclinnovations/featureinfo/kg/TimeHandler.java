package com.cmclinnovations.featureinfo.kg;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cmclinnovations.featureinfo.FeatureInfoAgent;
import com.cmclinnovations.featureinfo.Utils;
import com.cmclinnovations.featureinfo.config.ConfigEndpoint;

import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

/**
 * This class handles querying a Blazegraph endpoint to determine what measurements
 * are available, then retrieving these from the PostGreSQL endpoint.
 */
public class TimeHandler {

    /**
     * Logger for reporting info/errors.
     */
    private static final Logger LOGGER = LogManager.getLogger(TimeHandler.class);

    /**
     * IRI of the asset.
     */
    private final String iri;

    /**
     * Name of matching class.
     */
    private final String classMatch;

    /**
     * Endpoint(s) for the KG.
     */
    private final List<ConfigEndpoint> endpoints = new ArrayList<>();

    /**
     * Connection to KG.
     */
    private RemoteStoreClient rsClient;

    /**
     * Connection to timeseries RDB
     */
    private TimeSeriesClient<Instant> tsClient;

    /**
     * Last n hours to get timeseries data for.
     */
    private int hours = 24;

    /**
     * Response to write details back to.
     */
    private HttpServletResponse response;

    /**
     * Initialise a new TimeHandler.
     * 
     * @param iri IRI of the asset.
     * @param classMatch name of class for asset.
     * @param endpoint Blazegraph endpoint for the KG.
     */
    public TimeHandler(String iri, String classMatch, ConfigEndpoint endpoint) {
        this(iri, classMatch, Arrays.asList(endpoint));
    }

    /**
     * Initialise a new TimeHandler with multiple endpoints.
     * 
     * @param iri IRI of the asset.
     * @param classMatch name of class for asset
     * @param endpoints Blazegraph endpoints for the KG.
     */
    public TimeHandler(String iri, String classMatch, List<ConfigEndpoint> endpoints) {
        String fixedIRI = iri;
        if(!fixedIRI.startsWith("<")) fixedIRI = "<" + fixedIRI;
        if(!fixedIRI.endsWith(">")) fixedIRI = fixedIRI + ">";

        this.iri = fixedIRI;
        this.classMatch = classMatch;
        this.endpoints.addAll(endpoints);
    }

    /**
     * Sets the remote store client used to connect to the KG.
     * 
     * @param rsClient KG connection client.
     * @param tsClient Timeseries client.
     */
    public void setClients(RemoteStoreClient rsClient, TimeSeriesClient<Instant> tsClient) {
        this.rsClient = rsClient;
        this.tsClient = tsClient;
    }

    /**
     * Sets the most recent N hours to get data for.
     * 
     * @param hours most recent N hours
     */
    public void setHours(int hours) {
        this.hours = hours;
    }

    /**
     * Queries the KG to determine measurement details using the provided timeQuery, then passes
     * these measurement IRIs to the TimeSeriesClient to get real timeseries data.
     * 
     * @param response HttpServletResponse object.
     * 
     * @return JSONArray of query result.
     * 
     * @throws Exception if anything goes wrong.
     */
    public JSONArray getData(HttpServletResponse response) throws Exception {
        this.response = response;

        if(this.endpoints.isEmpty()) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.getWriter().write("{\"description\":\"Could not determine any Blazegraph endpoints.\"}");
            return null;
        }

        // Lookup queries attached to classes
        String queryTemplate = FeatureInfoAgent.CONFIG.getTimeQuery(this.classMatch);
        if(queryTemplate == null) {
            LOGGER.info("Could not find any timeseries queries for this class, skipping this stage.");
            return null;
        }

        // Inject parameters into query
        String query = queryTemplate.replaceAll(Pattern.quote("[IRI]"), this.iri);

        // Inject ontop endpoint
        if (query.contains("[ONTOP]")) {
            String ontopEndpoint = getOntopURL();
            if(ontopEndpoint == null) {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                response.getWriter().write("{\"description\":\"Could not determine the Ontop endpoint.\"}");
                return null;
            }
            query = query.replaceAll(Pattern.quote("[ONTOP]"), "<" + ontopEndpoint + ">");
        }

        try {
            // Run matching query
            JSONArray jsonResult = this.rsClient.executeFederatedQuery(getEndpointURLs(), query);
            if(jsonResult == null || jsonResult.length() == 0) {
                LOGGER.warn("No results regarding measurements, maybe this IRI has none?");
                return null;
            }

            // Filter the results
            jsonResult = this.filterJSON(jsonResult);

            // Transform into expected JSON objects
            return this.populateTimeseries(jsonResult);
        } catch(Exception exception) {
            LOGGER.warn("Query to get measurement details has caused an exception!");

            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.getWriter().write("{\"description\":\"Could not determine a valid Blazegraph endpoint.\"}");
            return null;
        }
    }

    /**
     * Filter the raw JSON array returned from the KG, trimming any URLS
     *  
     * @param array raw KG results.
     */
    @SuppressWarnings("java:S3776")
    private JSONArray filterJSON(JSONArray array) {
        if(array == null || array.length() == 0) return array;

        for(int i = 0; i < array.length(); i++) {
            Map<String, String> replacements = new LinkedHashMap<>();
            JSONObject object = array.getJSONObject(i);

            Iterator<String> keyIter = object.keys();
            while(keyIter.hasNext()) {
                String key = keyIter.next();
                String value = object.get(key).toString();
                boolean toRemove = false;

                // Don't touch the measurement property
                if(key.equalsIgnoreCase("Measurement") || key.equalsIgnoreCase("Forecast")) continue;

                // Replace underscores with spaces
                if(key.contains("_") || value.contains("_")) {
                    key = key.replaceAll(Pattern.quote("_"), " ");
                    value = value.replaceAll(Pattern.quote("_"), " ");
                    replacements.put(key, value);
                    toRemove = true;
                }
                
                if(toRemove) keyIter.remove();
            }

            // Add replacements back in
            replacements.keySet().forEach(key -> object.put(key, replacements.get(key)));
        }
        return array;
    }

    /**
     * Given the JSON array with details on measurements, this method
     * queries the Postgres endpoint to get actual timeseries data then
     * returns the final format JSONArray for return to the caller.
     * 
     * @param measurements measurement definitions from KG
     * 
     * @return timeseries details
     */
    @SuppressWarnings("java:S3776")
    private JSONArray populateTimeseries(JSONArray measurements) {
        Map<String, TimeSeries<Instant>> tsObjectList = new LinkedHashMap<>();

        // Build timeseries objects for all measurements
        for(int i = 0; i < measurements.length(); i++) {
            JSONObject entry = measurements.getJSONObject(i);

            String measureIRI = entry.getString("Measurement");
            if(measureIRI == null) measureIRI = entry.getString("Forecast");

            // Get timeseries object
            TimeSeries<Instant> tsObject = this.buildTimeseriesObject(measureIRI);

            // Skip empty objects
            if(tsObject == null || !tsObject.getTimes().isEmpty()) tsObjectList.put(measureIRI, tsObject);
        }

        // Skip if all empty
        if(tsObjectList.isEmpty()) return null;

        // Extract measurement names and units
        Map<String, String> names = new LinkedHashMap<>();
        Map<String, String> units = new LinkedHashMap<>();
        
        for(int i = 0; i < measurements.length(); i++) {
            JSONObject entry = measurements.getJSONObject(i);

            if(entry.has("Measurement")) {
                String measurementIRI = entry.getString("Measurement");
                names.put(measurementIRI, entry.optString("Name"));
                units.put(measurementIRI, entry.optString("Unit"));

            } else if(entry.has("Forecast")) {
                String forecastIRI = entry.getString("Forecast");
                names.put(forecastIRI, entry.optString("Name"));
                units.put(forecastIRI, entry.optString("Unit"));

            } else {
                LOGGER.warn("No 'Measurement' or 'Forecast' property found, skipping this entry.");
            }
        }

        try {
            // Combine into a single instance (required by client before conversion to JSON can happen)
            TimeSeries<Instant> combinedObject = Utils.getCombinedTimeSeries(tsClient, tsObjectList.values());

            // Convert to JSON
            JSONArray timeseriesJSON = tsClient.convertToJSON(
                    new ArrayList<>(Arrays.asList(combinedObject)),
                    Collections.nCopies(tsObjectList.size(), 1),
                    new ArrayList<>(Arrays.asList(units)),
                    new ArrayList<>(Arrays.asList(names))
            );

            // Add additionally reported parameters
            JSONArray properties = new JSONArray();

            for(int i = 0; i < measurements.length(); i++) {
                JSONObject oldEntry = measurements.getJSONObject(i);
                JSONObject newEntry = new JSONObject();

                oldEntry.keySet().forEach(key ->  {
                    if(!key.equals("Measurement") && !key.equals("Forecase") && !key.equals("Name") && !key.equals("Unit")) {
                        newEntry.put(key, oldEntry.get(key));
                    }
                });

                properties.put(newEntry);
            }

            // Add the properties
            timeseriesJSON.getJSONObject(0).put("properties", properties);

            // Return JSON
            return timeseriesJSON;

        } catch (Exception exception) {
            LOGGER.error("Could not produce JSON objects from TimeSeries instances!", exception);

            try {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                response.getWriter().write("{\"description\":\"Could not produce JSON objects from TimeSeries instances!\"}");
            } catch(IOException ioException) {
                LOGGER.error("Could not write to HTTP response!", ioException);
            }
        }

        return null;
    }

    /**
     * Builds a timeseries object for the input measurement IRI.
     * 
     * @param measureIRI IRI of measurement
     * 
     * @return timeseries object
     */
    protected TimeSeries<Instant> buildTimeseriesObject(String measureIRI) {

        // Remove the brackets from the IRI as the timeseries client is shit and can't handle them
        String fixedIRI = measureIRI;
        if (fixedIRI.startsWith("<") && fixedIRI.endsWith(">")) {
            fixedIRI = fixedIRI.substring(1, fixedIRI.length() - 1);
        }

        // Build then return the object
        TimeSeries<Instant> result = null;
        if(this.hours < 0) {
            // Get all data
            result = this.tsClient.getTimeSeries(new ArrayList<>(Arrays.asList(fixedIRI)));
        } else {
            // Determine bounds
            Instant lowerBound = LocalDateTime.now().minusHours(this.hours).toInstant(ZoneOffset.UTC);
            Instant upperBound = LocalDateTime.now().toInstant(ZoneOffset.UTC);
            result = this.tsClient.getTimeSeriesWithinBounds(
                    new ArrayList<>(Arrays.asList(fixedIRI)),
                    lowerBound,
                    upperBound
            );
        }
        return result;
    }

    /**
     * Get a list of the URLs from the input endpoints.
     * 
     * @return list of endpoint urls.
     */
    private List<String> getEndpointURLs() {
        List<String> urls = new ArrayList<>();
        this.endpoints.forEach(endpoint -> urls.add(endpoint.url()));
        return urls;
    }

    /**
     * Returns the URL for the ONTOP endpoint.
     *
     * @return ONTOP url.
     */
    private String getOntopURL() {
        Optional<ConfigEndpoint> result = FeatureInfoAgent.CONFIG.getOntopEndpoint();
        if(result.isPresent()) {
            return result.get().url();
        }
        return null;
    }

}
// End of class.