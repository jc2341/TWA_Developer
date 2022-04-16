package uk.ac.cam.cares.derivation.example;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;

import org.eclipse.rdf4j.sparqlbuilder.constraint.SparqlAggregate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.cam.cares.jps.base.derivation.DerivationClient;
import uk.ac.cam.cares.jps.base.derivation.DerivationOutputs;
import uk.ac.cam.cares.jps.base.derivation.DerivationSparql;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.interfaces.StoreClientInterface;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

/**
 * These tests require the docker stack to be up and running
 * please refer to TheWorldAvatar/Agents/DerivationExample/README.md for more details
 * @author Kok Foong Lee
 *
 */
public class IntegrationTest {
	JSONObject initResponse;
	
	// note that the URLs in the properties file are the URLs when they are accessed from within the docker 
	String kgurl = "http://localhost:8889/blazegraph/namespace/kb/sparql";
	String rdburl = "jdbc:postgresql://localhost:7432/postgres";
	
	@Before
	public void initialise() {
		// the response is a JSON object containing the IRIs of the initialise instances, refer to InitialiseInstances for the keys
	    initResponse = new JSONObject(AgentCaller.executeGet("http://localhost:8081/DerivationExample/InitialiseInstances"));
	}
	
	@Test
	public void testInputAgent() {
		// obtain IRI of the input that is initialised
		String input = initResponse.getString("input");
		Config.initProperties();
		RemoteStoreClient storeClient = new RemoteStoreClient(kgurl, kgurl, Config.kguser, Config.kgpassword);
		TimeSeriesClient<Instant> tsClient = new TimeSeriesClient<Instant>(storeClient, Instant.class, rdburl, Config.dbuser, Config.dbpassword);

		TimeSeries<Instant> ts1 = tsClient.getTimeSeries(Arrays.asList(input));
		
		// the input agent will add a row to the time series table
		AgentCaller.executeGet("http://localhost:8081/DerivationExample/InputAgent");
		
		TimeSeries<Instant> ts2 = tsClient.getTimeSeries(Arrays.asList(input));
		
		Assert.assertTrue(ts2.getTimes().size() > ts1.getTimes().size());
	}
	
	/**
	 * assumes that if the derivation time stamps are updated, it is functioning as intended
	 * derivations are initialised with timestamp = 0
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	@Test
	public void testUpdateDerivations() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Config.initProperties();
		RemoteStoreClient storeClient = new RemoteStoreClient(kgurl, kgurl, Config.kguser, Config.kgpassword);
		
		// get IRIs of initialise instances, the keys are located in the servlet InitialiseInstances
		String derived_diff = initResponse.getString("derivation of difference");
		String derived_average = initResponse.getString("derivation of average");
		
		DerivationSparql devClient = new DerivationSparql(storeClient, InitialiseInstances.derivationInstanceBaseURL);
		
		Method getTimestamp = devClient.getClass().getDeclaredMethod("getTimestamp", String.class);
		getTimestamp.setAccessible(true);
		
		long oldtimestamp_diff = (long) getTimestamp.invoke(devClient, derived_diff);
		long oldtimestamp_average = (long) getTimestamp.invoke(devClient, derived_average);
		
		AgentCaller.executeGet("http://localhost:8081/DerivationExample/UpdateDerivations");
		
		long newtimestamp_diff = (long) getTimestamp.invoke(devClient, derived_diff);
		long newtimestamp_average = (long) getTimestamp.invoke(devClient, derived_average);
		
		Assert.assertTrue(newtimestamp_diff > oldtimestamp_diff);
		Assert.assertTrue(newtimestamp_average > oldtimestamp_average);
	}
	
	@Test
	public void testMinValueAgent() {
		String input = initResponse.getString("input");
		JSONObject request = new JSONObject().put(
			DerivationClient.AGENT_INPUT_KEY, new JSONObject()
				.put(SparqlClient.getRdfTypeString(SparqlClient.InputData), input));
		
		String response = AgentCaller.executeGetWithURLAndJSON("http://localhost:8081/DerivationExample/MinValueAgent", request.toString());
		JSONObject responseJson = new JSONObject(response);
		Assert.assertTrue(responseJson.has(DerivationClient.AGENT_OUTPUT_KEY));
		Assert.assertTrue(responseJson.has(DerivationOutputs.RETRIEVED_INPUTS_TIMESTAMP_KEY));
	}
	
	@Test
	public void testMaxValueAgent() {
		String input = initResponse.getString("input");
		JSONObject request = new JSONObject().put(
			DerivationClient.AGENT_INPUT_KEY, new JSONObject()
				.put(SparqlClient.getRdfTypeString(SparqlClient.InputData), input));
		
		String response = AgentCaller.executeGetWithURLAndJSON("http://localhost:8081/DerivationExample/MaxValueAgent", request.toString());
		JSONObject responseJson = new JSONObject(response);
		Assert.assertTrue(responseJson.getJSONObject(DerivationClient.AGENT_OUTPUT_KEY)
			.has(SparqlClient.getRdfTypeString(SparqlClient.MaxValue)));
		Assert.assertTrue(responseJson.getLong(DerivationOutputs.RETRIEVED_INPUTS_TIMESTAMP_KEY) > 0);
	}
	
	@Test
	public void testAverageAgent() {
		String input = initResponse.getString("input");
		String average = initResponse.getString("average");
		
		Config.initProperties();
		RemoteStoreClient storeClient = new RemoteStoreClient(kgurl, kgurl, Config.kguser, Config.kgpassword);
		TimeSeriesClient<Instant> tsClient = new TimeSeriesClient<Instant>(storeClient, Instant.class, rdburl, Config.dbuser, Config.dbpassword);
		
		TimeSeries<Instant> ts1 = tsClient.getTimeSeries(Arrays.asList(average));
		
		// now call agent to update the table containing averages
		JSONObject request = new JSONObject().put(
			DerivationClient.AGENT_INPUT_KEY, new JSONObject()
				.put(SparqlClient.getRdfTypeString(SparqlClient.InputData), input));
		String response = AgentCaller.executeGetWithURLAndJSON("http://localhost:8081/DerivationExample/AverageAgent", request.toString());
		
		TimeSeries<Instant> ts2 = tsClient.getTimeSeries(Arrays.asList(average));
		
		Assert.assertTrue(ts2.getTimes().size() > ts1.getTimes().size());
		JSONObject responseJson = new JSONObject(response);
		Assert.assertTrue(responseJson.getJSONObject(DerivationClient.AGENT_OUTPUT_KEY)
			.has(SparqlClient.getRdfTypeString(SparqlClient.Average)));
		Assert.assertTrue(responseJson.getLong(DerivationOutputs.RETRIEVED_INPUTS_TIMESTAMP_KEY) > 0);
	}
	
	@Test 
	public void testDifferenceAgent() {
		String min = initResponse.getString("min value");
		String max = initResponse.getString("max value");
		
		JSONObject request = new JSONObject().put(
			DerivationClient.AGENT_INPUT_KEY, new JSONObject()
				.put(SparqlClient.getRdfTypeString(SparqlClient.MinValue), min)
				.put(SparqlClient.getRdfTypeString(SparqlClient.MaxValue), max));
		String response = AgentCaller.executeGetWithURLAndJSON("http://localhost:8081/DerivationExample/DifferenceAgent", request.toString());
		JSONObject responseJson = new JSONObject(response);
		Assert.assertTrue(responseJson.getJSONObject(DerivationClient.AGENT_OUTPUT_KEY)
			.has(SparqlClient.getRdfTypeString(SparqlClient.Difference)));
		Assert.assertTrue(responseJson.getLong(DerivationOutputs.RETRIEVED_INPUTS_TIMESTAMP_KEY) > 0);
	}
}
