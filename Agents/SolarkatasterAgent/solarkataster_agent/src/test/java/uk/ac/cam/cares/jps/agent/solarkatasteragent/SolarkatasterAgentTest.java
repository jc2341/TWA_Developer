package uk.ac.cam.cares.jps.agent.solarkatasteragent;

import org.json.JSONArray;
import org.json.JSONObject;
import javax.servlet.http.HttpServletRequest;

import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;

import org.junit.jupiter.api.Test;
import java.sql.Connection;

public class SolarkatasterAgentTest {
    @Test
    public void testProcessReqeustParameters() throws Exception {
        // Test processRequestParameters(JSONObject requestParams, HttpServletRequest request)
        SolarkatasterAgent agentHTTP = spy(new SolarkatasterAgent());
        Method processRequestParametersHTTP = agentHTTP.getClass().getDeclaredMethod("processRequestParameters", JSONObject.class, HttpServletRequest.class);

        JSONObject mockJSONObject = mock(JSONObject.class);
        HttpServletRequest mockHTTP = mock(HttpServletRequest.class);

        doReturn(new JSONObject()).when(agentHTTP).processRequestParameters(any(JSONObject.class));

        processRequestParametersHTTP.invoke(agentHTTP, mockJSONObject, mockHTTP);

        verify(agentHTTP, times(1)).processRequestParameters(any(JSONObject.class));

        // Test processRequestParameters(JSONObject requestParams)
        SolarkatasterAgent agent = spy(new SolarkatasterAgent());
        Method processRequestParameters = agent.getClass().getDeclaredMethod("processRequestParameters", JSONObject.class);

        JSONArray mockJSONArray = mock(JSONArray.class);
        RemoteRDBStoreClient mockRDBClient = mock(RemoteRDBStoreClient.class);
        TimeSeriesClient mockTS = mock(TimeSeriesClient.class);

        Field tsClient = agent.getClass().getDeclaredField("tsClient");
        tsClient.setAccessible(true);
        tsClient.set(agent, mockTS);

        Field rdbStoreClient = agent.getClass().getDeclaredField("rdbStoreClient");
        rdbStoreClient.setAccessible(true);
        rdbStoreClient.set(agent, mockRDBClient);

        Field tsRDBStoreClient = agent.getClass().getDeclaredField("tsRDBStoreClient");
        tsRDBStoreClient.setAccessible(true);
        tsRDBStoreClient.set(agent, mockRDBClient);

        doReturn(true).when(agent).validateInput(any(JSONObject.class));

        doReturn("test").when(mockJSONObject).getString(anyString());
        doReturn(1).when(mockJSONObject).getInt(anyString());

        doReturn(mockJSONArray).when(mockRDBClient).executeQuery(anyString());
        doReturn(mock(Connection.class)).when(mockRDBClient).getConnection();

        doReturn(mockJSONObject).when(mockJSONArray).getJSONObject(anyInt());

        doNothing().when(mockTS).bulkInitTimeSeries(anyList(), anyList(), anyList(), any(Connection.class), anyList(), anyList(), anyList());
        doNothing().when(mockTS).bulkaddTimeSeriesData(anyList(), any(Connection.class));

        processRequestParameters.invoke(agent, mockJSONObject);

        verify(agent, times(1)).validateInput(any(JSONObject.class));
        verify(mockTS).bulkaddTimeSeriesData(anyList(), any(Connection.class));

    }

    @Test
    public void testValidateInput() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        SolarkatasterAgent agent = new SolarkatasterAgent();
        Method validateInput = agent.getClass().getDeclaredMethod("validateInput", JSONObject.class);

        Field KEY_TABLE = agent.getClass().getDeclaredField("KEY_TABLE");
        KEY_TABLE.setAccessible(true);
        String key_table = (String) KEY_TABLE.get(agent);

        JSONObject testJSONObject = new JSONObject();
        testJSONObject.put(key_table, "test");

        assertTrue((Boolean) validateInput.invoke(agent, testJSONObject));
    }

    @Test
    public void testGetDoubleList() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        SolarkatasterAgent agent = new SolarkatasterAgent();
        Method getDoubleList = agent.getClass().getDeclaredMethod("getDoubleList", JSONObject.class);

        assertNotNull(getDoubleList);
        getDoubleList.setAccessible(true);

        JSONObject testJSONObject = new JSONObject();

        Field TIME_SERIES = agent.getClass().getDeclaredField("TIME_SERIES");
        TIME_SERIES.setAccessible(true);

        List<String> time_series = (List <String>) TIME_SERIES.get(agent);

        for (int i = 0; i < time_series.size(); i++){
            testJSONObject.put(time_series.get(i), (double) i);
        }

        List<Double> results = (List<Double>) getDoubleList.invoke(agent, testJSONObject);

        assertEquals(time_series.size(), results.size());

        for (int i = 0; i < results.size(); i++){
            assertEquals(testJSONObject.getDouble(time_series.get(i)), results.get(i));
        }
    }

    @Test
    public void testParseDataToLists() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        SolarkatasterAgent agent = new SolarkatasterAgent();
        Method parseDataToLists = agent.getClass().getDeclaredMethod("parseDataToLists", JSONArray.class);

        assertNotNull(parseDataToLists);
        parseDataToLists.setAccessible(true);

        JSONArray testJSONArray = new JSONArray();
        JSONObject testJSON1 = new JSONObject();
        JSONObject testJSON2 = new JSONObject();

        Field KEY_OID = agent.getClass().getDeclaredField("KEY_OID");
        KEY_OID.setAccessible(true);
        String key_oid = (String) KEY_OID.get(agent);

        Field KEY_GEB = agent.getClass().getDeclaredField("KEY_GEB");
        KEY_GEB.setAccessible(true);
        String key_geb = (String) KEY_GEB.get(agent);

        Field TIME_SERIES = agent.getClass().getDeclaredField("TIME_SERIES");
        TIME_SERIES.setAccessible(true);
        List<String> time_series = (List <String>) TIME_SERIES.get(agent);

        testJSON1.put(key_oid, 1);
        testJSON2.put(key_oid, 2);

        testJSON1.put(key_geb, "test1");
        testJSON2.put(key_geb, "test2");

        for (int i = 0; i < time_series.size(); i++){
            testJSON1.put(time_series.get(i), i + 0.01);
            testJSON2.put(time_series.get(i), i + 0.02);
        }

        testJSONArray.put(testJSON1);
        testJSONArray.put(testJSON2);

        ArrayList<List> result = (ArrayList<List>) parseDataToLists.invoke(agent, testJSONArray);

        List<List<String>> dataIRI = (List<List<String>>) result.get(0);
        List<String> IRI1 = dataIRI.get(0);
        List<String> IRI2 = dataIRI.get(1);

        assertEquals(IRI1.size(), 1);
        assertEquals(IRI2.size(), 1);
        assertTrue(IRI1.get(0).contains(testJSON1.getString(key_geb) + String.valueOf(testJSON1.getInt(key_oid))));
        assertTrue(IRI2.get(0).contains(testJSON2.getString(key_geb) + String.valueOf(testJSON2.getInt(key_oid))));

        List<TimeSeries<Double>> tsList = (List<TimeSeries<Double>>) result.get(1);
        TimeSeries<Double> ts1 = tsList.get(0);
        TimeSeries<Double> ts2 = tsList.get(1);
        List<Double> tl1 = ts1.getValuesAsDouble(IRI1.get(0));
        List<Double> tl2 = ts2.getValuesAsDouble(IRI2.get(0));

        assertEquals(tl1.size(), time_series.size());
        assertEquals(tl2.size(), time_series.size());

        for(int i = 0; i < tl1.size(); i++){
            assertEquals(tl1.get(i), testJSON1.get(time_series.get(i)));
            assertEquals(tl2.get(i), testJSON2.get(time_series.get(i)));
        }
    }

    @Test
    public void testGetQueryString() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        SolarkatasterAgent agent = new SolarkatasterAgent();
        Method getQueryString = agent.getClass().getDeclaredMethod("getQueryString", String.class);

        assertNotNull(getQueryString);
        getQueryString.setAccessible(true);

        Field TIME_SERIES = agent.getClass().getDeclaredField("TIME_SERIES");
        TIME_SERIES.setAccessible(true);
        List<String> time_series = (List <String>) TIME_SERIES.get(agent);

        Field KEY_OID = agent.getClass().getDeclaredField("KEY_OID");
        KEY_OID.setAccessible(true);
        String key_oid = (String) KEY_OID.get(agent);

        Field KEY_GEB = agent.getClass().getDeclaredField("KEY_GEB");
        KEY_GEB.setAccessible(true);
        String key_geb = (String) KEY_GEB.get(agent);

        String result = (String) getQueryString.invoke(agent, "testTable");

        assertTrue(result.contains("SELECT"));
        assertTrue(result.contains("testTable"));
        assertTrue(result.contains(key_geb));
        assertTrue(result.contains(key_oid));

        for (int i = 0; i < time_series.size(); i++){
            assertTrue(result.contains(time_series.get(i)));
        }
    }

    @Test
    public void testCreateTimeSeries() throws Exception {
        SolarkatasterAgent agent = new SolarkatasterAgent();
        Method createTimeSeries = agent.getClass().getDeclaredMethod("createTimeSeries", List.class);

        assertNotNull(createTimeSeries);
        createTimeSeries.setAccessible(true);

        TimeSeriesClient mockTS = mock(TimeSeriesClient.class);

        Field tsClient = agent.getClass().getDeclaredField("tsClient");
        tsClient.setAccessible(true);
        tsClient.set(agent, mockTS);

        RemoteRDBStoreClient mockRDBClient = mock(RemoteRDBStoreClient.class);

        Field tsRDBStoreClient = agent.getClass().getDeclaredField("tsRDBStoreClient");
        tsRDBStoreClient.setAccessible(true);
        tsRDBStoreClient.set(agent, mockRDBClient);


        doNothing().when(mockTS).bulkInitTimeSeries(anyList(), anyList(), anyList(), any(Connection.class), anyList(), anyList(), anyList());
        doReturn(mock(Connection.class)).when(mockRDBClient).getConnection();

        List<List<String>> testIRI = Arrays.asList(Arrays.asList("test1"), Arrays.asList("test2"));

        createTimeSeries.invoke(agent, testIRI);

        verify(mockTS, times(1)).bulkInitTimeSeries(anyList(), anyList(), anyList(), any(Connection.class), anyList(), anyList(), anyList());
    }

    @Test
    public void testGetData() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        SolarkatasterAgent agent = new SolarkatasterAgent();
        Method getData = agent.getClass().getDeclaredMethod("getData", String.class);

        assertNotNull(getData);
        getData.setAccessible(true);

        RemoteRDBStoreClient mockRDBClient = mock(RemoteRDBStoreClient.class);

        Field rdbStoreClient = agent.getClass().getDeclaredField("rdbStoreClient");
        rdbStoreClient.setAccessible(true);
        rdbStoreClient.set(agent, mockRDBClient);

        doReturn(new JSONArray()).when(mockRDBClient).executeQuery(anyString());

        getData.invoke(agent, "test");

        verify(mockRDBClient, times(1)).executeQuery(anyString());
    }

}
