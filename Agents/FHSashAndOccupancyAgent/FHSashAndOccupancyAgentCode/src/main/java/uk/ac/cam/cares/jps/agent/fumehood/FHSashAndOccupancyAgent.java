package uk.ac.cam.cares.jps.agent.fumehood;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;

@WebServlet(urlPatterns = {"/retrieve"})
public class FHSashAndOccupancyAgent extends JPSAgent {
    private static final Logger LOGGER = LogManager.getLogger(FHSashAndOccupancyAgent.class);


    public static final String PARAMETERS_VALIDATION_ERROR_MSG = "Unable to validate request sent to the agent.";
    public static final String EMPTY_PARAMETER_ERROR_MSG = "Empty Request.";
    public static final String AGENT_CONSTRUCTION_ERROR_MSG = "The Agent could not be constructed.";
    public static final String LOADTSCLIENTCONFIG_ERROR_MSG = "Unable to load timeseries client configs!";
    public static final String QUERYSTORE_CONSTRUCTION_ERROR_MSG = "Unable to construct QueryStore!";
    public static final String GETLATESTDATA_ERROR_MSG = "Unable to get latest timeseries data for the following IRI: ";

    String dbUrlForOccupiedState;
    String dbUsernameForOccupiedState;
    String dbPasswordForOccupiedState;
    String dbUrlForSashOpening;
    String dbUsernameForSashOpening;
    String dbPasswordForSashOpening;
    String sparqlQueryEndpoint;
    String sparqlUpdateEndpoint;
    String bgUsername;
    String bgPassword;

    TimeSeriesClient<OffsetDateTime> tsClient;
    RemoteRDBStoreClient RDBClient;
    TimeSeries<OffsetDateTime> timeseries;

    /**
     * Servlet init.
     *
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        super.init();
        LOGGER.debug("This is a debug message.");
        LOGGER.info("This is an info message.");
        LOGGER.warn("This is a warn message.");
        LOGGER.error("This is an error message.");
        LOGGER.fatal("This is a fatal message.");
    }

    /**
     * Handle GET request and route to different functions based on the path.
     * @param requestParams Parameters sent with HTTP request
     * @param request HTTPServletRequest instance
     * @return result of the request
     */
    @Override
    public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
        String datetime = dateFormat.format(new Date());
        LOGGER.info("Request received at: {}", datetime);

        JSONObject msg = new JSONObject();

        String url = request.getRequestURI();

        if (url.contains("status")) {
            msg = getStatus();
        }

        if (url.contains("retrieve")) {
            msg = runAgent();
        }

        return msg;
        
    }

    /**
     * Initialise agent
     * @return successful initialisation message
     */
    private JSONObject runAgent() {
        QueryStore queryStore;
        Map<String, List<String>> map = new HashMap<>();

        try {
            loadTSClientConfigs(System.getenv("CLIENTPROPERTIES_01"),System.getenv("CLIENTPROPERTIES_02"));
        } catch (IOException e) {
            LOGGER.info("Unable to read timeseries client configs from the properties files.");
            LOGGER.info("Attempting to load configs via stack clients...");
        }

        try {
            queryStore = new QueryStore(sparqlUpdateEndpoint, sparqlQueryEndpoint, bgUsername, bgPassword);
        } catch (IOException e) {
            throw new JPSRuntimeException(QUERYSTORE_CONSTRUCTION_ERROR_MSG, e);
        }

        map = queryStore.queryForFHandWFHDevices();
        map.put("OccupancyIRIs", new ArrayList<>());
        map.put("SashOpeningIRIs", new ArrayList<>());

        for (int i = 0; i < map.get("FHandWFH").size(); i++){
            String IRI = queryStore.queryForOccupancyState(map.get("FHandWFH").get(i));
            map.get("OccupancyIRIs").add(IRI);
        }

        for (int i = 0; i < map.get("FHandWFH").size(); i++){
            String IRI = queryStore.queryForSashOpening(map.get("FHandWFH").get(i));
            map.get("SashOpeningIRIs").add(IRI);
        }
        
        setTsClientAndRDBClient(dbUsernameForOccupiedState, dbPasswordForOccupiedState, dbUrlForOccupiedState, bgUsername, bgPassword, sparqlUpdateEndpoint, sparqlQueryEndpoint);

        map = getOccupiedStateTsData(map);

        setTsClientAndRDBClient(dbUsernameForSashOpening, dbPasswordForSashOpening, dbUrlForSashOpening, bgUsername, bgPassword, sparqlUpdateEndpoint, sparqlQueryEndpoint);

        map = getSashOpeningTsData(map);

        if (checkSashAndOccupancy(map)) {
        EmailBuilder emailBuilder = new EmailBuilder();
        emailBuilder.parsesMapAndPostProcessing(map);
        }

        LOGGER.info( map.get("FHandWFH").toString());
        LOGGER.info( map.get("Label").toString());
        LOGGER.info( map.get("OccupancyIRIs").toString());
        LOGGER.info( map.get("SashOpeningIRIs").toString());
        LOGGER.info(map.get("OccupiedStateTsData").toString());
        LOGGER.info(map.get("SashOpeningTsData").toString());
        
        JSONObject msg = new JSONObject();
        msg.put("result", "Agent has successfully query and check through all fumehoods and walkin-fumehoods.");
        return msg;
    }

    /**
     * Handle GET /status route and return the status of the agent.
     * @return Status of the agent
     */
    private JSONObject getStatus() {
        LOGGER.info("Detected request to get agent status...");
        JSONObject result = new JSONObject();
        result.put("description", "FHSashAndOccupancyAgent is ready.");
        return result;
    }

    /**
     * Reads the parameters needed for the timeseries client
     * @param filepath Path to the properties file from which to read the parameters
     * @param filepath2 Path to second properties file from which to read the parameters
     */
    private void loadTSClientConfigs(String filepath , String filepath2) throws IOException {
        // Check whether properties file exists at specified location
        File file = new File(filepath);
        if (!file.exists()) {
            throw new FileNotFoundException("No properties file found at specified filepath: " + filepath);
        }

        // Try-with-resource to ensure closure of input stream
        try (InputStream input = new FileInputStream(file)) {

            // Load properties file from specified path
            Properties prop = new Properties();
            prop.load(input);

            // Get timeseries client parameters from properties file
            if (prop.containsKey("db.url")) {
                this.dbUrlForOccupiedState = prop.getProperty("db.url");
            } else {
                throw new IOException("Properties file is missing \"db.url=<db_url>\"");
            }
            if (prop.containsKey("db.user")) {
                this.dbUsernameForOccupiedState = prop.getProperty("db.user");
            } else {
                throw new IOException("Properties file is missing \"db.user=<db_user>\"");
            }
            if (prop.containsKey("db.password")) {
                this.dbPasswordForOccupiedState = prop.getProperty("db.password");
            } else {
                throw new IOException("Properties file is missing \"db.password=<db_password>\"");
            }
            if (prop.containsKey("sparql.query.endpoint")) {
                this.sparqlQueryEndpoint = prop.getProperty("sparql.query.endpoint");
            } else {
                throw new IOException("Properties file is missing \"sparql.query.endpoint=<sparql_query_endpoint>\"");
            }
            if (prop.containsKey("sparql.update.endpoint")) {
                this.sparqlUpdateEndpoint = prop.getProperty("sparql.update.endpoint");
            } else {
                throw new IOException("Properties file is missing \"sparql.update.endpoint=<sparql_update_endpoint>\"");
            }
            if (prop.containsKey("bg.username")) {
                this.bgUsername = prop.getProperty("bg.username");
            }
            if (prop.containsKey("bg.password")) {
                this.bgPassword = prop.getProperty("bg.password");
            }
        }

        // Check whether properties file exists at specified location
        file = new File(filepath2);
        if (!file.exists()) {
            throw new FileNotFoundException("No properties file found at specified filepath: " + filepath2);
        }

        // Try-with-resource to ensure closure of input stream
        try (InputStream input = new FileInputStream(file)) {

            // Load properties file from specified path
            Properties prop = new Properties();
            prop.load(input);

            // Get timeseries client parameters from properties file
            if (prop.containsKey("db.url")) {
                this.dbUrlForSashOpening = prop.getProperty("db.url");
            } else {
                throw new IOException("Properties file is missing \"db.url=<db_url>\"");
            }
            if (prop.containsKey("db.user")) {
                this.dbUsernameForSashOpening = prop.getProperty("db.user");
            } else {
                throw new IOException("Properties file is missing \"db.user=<db_user>\"");
            }
            if (prop.containsKey("db.password")) {
                this.dbPasswordForSashOpening = prop.getProperty("db.password");
            } else {
                throw new IOException("Properties file is missing \"db.password=<db_password>\"");
            }
        }
    }

    /**
     * set tsClient and RDBClient
     * @param dbUsername username for accessing the postgreSQL database
     * @param dbPassword password for accessing the postgreSQL database
     * @param dbUrl jdbc URL for accessing the postgreSQL database
     * @param bgUsername username for accessing an authentication enabled blazegraph
     * @param bgPassword password for accessing an authentication enabled blazegraph
     * @param sparqlUpdateEndpoint sparql endpoint for executing updates
     * @param sparqlQueryEndpoint sparql endpoint for executing queries
     */
    private void setTsClientAndRDBClient(String dbUsername, String dbPassword, String dbUrl, String bgUsername, String bgPassword, String sparqlUpdateEndpoint, String sparqlQueryEndpoint) {
        RemoteStoreClient kbClient = new RemoteStoreClient();
        kbClient.setQueryEndpoint(sparqlQueryEndpoint);
        kbClient.setUpdateEndpoint(sparqlUpdateEndpoint);
        kbClient.setUser(bgUsername);
        kbClient.setPassword(bgPassword);

        tsClient = new TimeSeriesClient<>(kbClient ,OffsetDateTime.class);
        RDBClient = new RemoteRDBStoreClient(dbUrl, dbUsername, dbPassword);
    }

    /**
     * Retrieve timeseries data for occupied state
     * @param map map that consists of several keys where each key has its own List of Strings
     */
    private Map<String, List<String>> getOccupiedStateTsData (Map<String, List<String>> map) {
        map.put("OccupiedStateTsData", new ArrayList<>());

        for (int i = 0; i < map.get("FHandWFH").size(); i++) {
            String occupiedStateIRI = map.get("OccupancyIRIs").get(i);

            if (!occupiedStateIRI.contains("This device does not have a occupied state.")) {
                try (Connection conn = RDBClient.getConnection()) {
                    timeseries = tsClient.getLatestData(occupiedStateIRI, conn);
                    map.get("OccupiedStateTsData").add(timeseries.getValuesAsString(occupiedStateIRI).get(timeseries.getValuesAsString(occupiedStateIRI).size() - 1));
                } catch (Exception e) {
                    throw new JPSRuntimeException(GETLATESTDATA_ERROR_MSG + occupiedStateIRI);
                }
            } else {
                map.get("OccupiedStateTsData").add("This device does not have an occupied state.");
            }
        }
        return map;
    }

    /**
     * Retrieve timeseries data for sash opening
     * @param map map that consists of several keys where each key has its own List of Strings
     */
    private Map<String, List<String>> getSashOpeningTsData (Map<String, List<String>> map) {
        map.put("SashOpeningTsData", new ArrayList<>());

        for (int i = 0; i < map.get("FHandWFH").size(); i++) {
            String sashOpeningIRI = map.get("SashOpeningIRIs").get(i);

            if (!sashOpeningIRI.contains("This device does not have a Sash Opening Percentage.")) {
                try (Connection conn = RDBClient.getConnection()) {
                    timeseries = tsClient.getLatestData(sashOpeningIRI, conn);
                    map.get("SashOpeningTsData").add(timeseries.getValuesAsString(sashOpeningIRI).get(timeseries.getValuesAsString(sashOpeningIRI).size() - 1));
                } catch (Exception e) {
                    throw new JPSRuntimeException(GETLATESTDATA_ERROR_MSG + sashOpeningIRI);
                }
            } else {
                map.get("SashOpeningTsData").add("This device does not have a sash opening.");
            }
        }
        return map;
    }

    /**
     * Check sash and occupancy values
     * @param map map that consists of several keys where each key has its own List of Strings
     * @return boolean of whether there exist a fumehood that is not occupied and have a sash opening value of more than 50
     */
    private Boolean checkSashAndOccupancy(Map<String, List<String>> map) {
        Boolean check = false;
        for (int i = 0; i < map.get("FHandWFH").size(); i++){
            String occupiedStateData = map.get("OccupiedStateTsData").get(i);
            String sashOpeningData = map.get("SashOpeningTsData").get(i);
            if (occupiedStateData.contains("This device does not have an occupied state.") | sashOpeningData.contains("This device does not have a sash opening.")) {
                check = false;
            } else {
                if (Double.parseDouble(occupiedStateData) == 0.0 && Double.parseDouble(sashOpeningData) > 50.0) {
                    check = true;
                    return check;
                }
            }
        }
        LOGGER.info("Check is " + check);
        return check;
    }

}
