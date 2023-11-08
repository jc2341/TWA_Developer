package uk.ac.cam.cares.jps.agent.dashboard.stack;

import com.cmclinnovations.stack.clients.blazegraph.BlazegraphEndpointConfig;
import com.cmclinnovations.stack.clients.docker.ContainerClient;
import com.cmclinnovations.stack.clients.postgis.PostGISEndpointConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import uk.ac.cam.cares.jps.agent.dashboard.IntegrationTestUtils;
import uk.ac.cam.cares.jps.agent.dashboard.utils.StringHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StackClientTest {

    @BeforeAll
    static void setup() {
        IntegrationTestUtils.createNamespace(IntegrationTestUtils.SPATIAL_ZONE_NAMESPACE);
        IntegrationTestUtils.createNamespace(IntegrationTestUtils.GENERAL_NAMESPACE);
        PostGisClientTest.genSampleDatabases();
    }

    @AfterEach
    void clearNamespace() {
        IntegrationTestUtils.updateEndpoint(IntegrationTestUtils.GENERAL_SPARQL_ENDPOINT, IntegrationTestUtils.SPARQL_DELETE);
        IntegrationTestUtils.updateEndpoint(IntegrationTestUtils.SPATIAL_ZONE_SPARQL_ENDPOINT, IntegrationTestUtils.SPARQL_DELETE);
        IntegrationTestUtils.deletePostGisPasswordFile();
    }

    @AfterAll
    static void cleanUp() {
        PostGisClientTest.removeSampleDatabases();
    }

    @Test
    void testGetDashboardUrl() {
        IntegrationTestUtils.createPostGisPasswordFile();
        try (MockedConstruction<ContainerClient> mockClient = Mockito.mockConstruction(ContainerClient.class, (mock, context) -> {
            // Ensure all mocks return the test config class for the method to continue
            Mockito.when(mock.readEndpointConfig("blazegraph", BlazegraphEndpointConfig.class)).thenReturn(IntegrationTestUtils.SPARQL_ENDPOINT_CONFIG);
            Mockito.when(mock.readEndpointConfig("postgis", PostGISEndpointConfig.class)).thenReturn(IntegrationTestUtils.POSTGIS_ENDPOINT_CONFIG);
        })) {
            StackClient mockStackClient = new StackClient();
            // Verify there is no organisation if no relevant triples are found
            assertEquals("http://" + IntegrationTestUtils.DOCKER_HOST_NAME + "-grafana:3000", mockStackClient.getDashboardUrl());
        }
    }

    @Test
    void testSetDashboardUrl() {
        IntegrationTestUtils.createPostGisPasswordFile();
        try (MockedConstruction<ContainerClient> mockClient = Mockito.mockConstruction(ContainerClient.class, (mock, context) -> {
            // Ensure all mocks return the test config class for the method to continue
            Mockito.when(mock.readEndpointConfig("blazegraph", BlazegraphEndpointConfig.class)).thenReturn(IntegrationTestUtils.SPARQL_ENDPOINT_CONFIG);
            Mockito.when(mock.readEndpointConfig("postgis", PostGISEndpointConfig.class)).thenReturn(IntegrationTestUtils.POSTGIS_ENDPOINT_CONFIG);
        })) {
            StackClient mockStackClient = new StackClient();
            String sampleUrl = "Test";
            mockStackClient.setDashboardUrl(sampleUrl);
            // Verify there is no organisation if no relevant triples are found
            assertEquals(sampleUrl, mockStackClient.getDashboardUrl());
        }
    }

    @Test
    void testGetAllOrganisations_NoData() {
        // Create a postgis password file
        IntegrationTestUtils.createPostGisPasswordFile();
        try (MockedConstruction<ContainerClient> mockClient = Mockito.mockConstruction(ContainerClient.class, (mock, context) -> {
            // Ensure all mocks return the test config class for the method to continue
            Mockito.when(mock.readEndpointConfig("blazegraph", BlazegraphEndpointConfig.class)).thenReturn(IntegrationTestUtils.SPARQL_ENDPOINT_CONFIG);
            Mockito.when(mock.readEndpointConfig("postgis", PostGISEndpointConfig.class)).thenReturn(IntegrationTestUtils.POSTGIS_ENDPOINT_CONFIG);
        })) {
            StackClient mockStackClient = new StackClient();
            String[] organisations = mockStackClient.getAllOrganisations();
            // Verify there is no organisation if no relevant triples are found
            assertEquals(0, organisations.length);
        }
    }

    @Test
    void testGetAllOrganisations_WithData() {
        // Insert these triples into the blazegraph
        SparqlClientTest.insertFacilityTriples(IntegrationTestUtils.SPATIAL_ZONE_SPARQL_ENDPOINT);
        SparqlClientTest.insertAssetTriples(IntegrationTestUtils.GENERAL_SPARQL_ENDPOINT, true);
        // Create a postgis password file
        IntegrationTestUtils.createPostGisPasswordFile();
        try (MockedConstruction<ContainerClient> mockClient = Mockito.mockConstruction(ContainerClient.class, (mock, context) -> {
            // Ensure all mocks return the test config class for the method to continue
            Mockito.when(mock.readEndpointConfig("blazegraph", BlazegraphEndpointConfig.class)).thenReturn(IntegrationTestUtils.SPARQL_ENDPOINT_CONFIG);
            Mockito.when(mock.readEndpointConfig("postgis", PostGISEndpointConfig.class)).thenReturn(IntegrationTestUtils.POSTGIS_ENDPOINT_CONFIG);
        })) {
            StackClient mockStackClient = new StackClient();
            String[] organisations = mockStackClient.getAllOrganisations();
            // Verify there is an organisation if there are relevant triples
            assertEquals(1, organisations.length);
        }
    }

    @Test
    void testGetDatabaseNames() {
        // Insert these triples into the blazegraph
        SparqlClientTest.insertFacilityTriples(IntegrationTestUtils.SPATIAL_ZONE_SPARQL_ENDPOINT);
        SparqlClientTest.insertAssetTriples(IntegrationTestUtils.GENERAL_SPARQL_ENDPOINT, true);
        // Create a postgis password file
        IntegrationTestUtils.createPostGisPasswordFile();
        try (MockedConstruction<ContainerClient> mockClient = Mockito.mockConstruction(ContainerClient.class, (mock, context) -> {
            // Ensure all mocks return the test config class for the method to continue
            Mockito.when(mock.readEndpointConfig("blazegraph", BlazegraphEndpointConfig.class)).thenReturn(IntegrationTestUtils.SPARQL_ENDPOINT_CONFIG);
            Mockito.when(mock.readEndpointConfig("postgis", PostGISEndpointConfig.class)).thenReturn(IntegrationTestUtils.POSTGIS_ENDPOINT_CONFIG);
        })) {
            StackClient mockStackClient = new StackClient();
            List<String> databases = mockStackClient.getDatabaseNames();
            // Verify that there are two databases
            assertEquals(2, databases.size());
            databases.forEach((database) ->
                    assertTrue(database.equals(PostGisClientTest.TEMPERATURE_SQL_DATABASE) || database.equals(PostGisClientTest.ROOM_SQL_DATABASE))
            );
        }
    }

    @Test
    void testGetPostGisCredentials() {
        // Insert these triples into the blazegraph
        SparqlClientTest.insertFacilityTriples(IntegrationTestUtils.SPATIAL_ZONE_SPARQL_ENDPOINT);
        SparqlClientTest.insertAssetTriples(IntegrationTestUtils.GENERAL_SPARQL_ENDPOINT, true);
        // Create a postgis password file
        IntegrationTestUtils.createPostGisPasswordFile();
        try (MockedConstruction<ContainerClient> mockClient = Mockito.mockConstruction(ContainerClient.class, (mock, context) -> {
            // Ensure all mocks return the test config class for the method to continue
            Mockito.when(mock.readEndpointConfig("blazegraph", BlazegraphEndpointConfig.class)).thenReturn(IntegrationTestUtils.SPARQL_ENDPOINT_CONFIG);
            Mockito.when(mock.readEndpointConfig("postgis", PostGISEndpointConfig.class)).thenReturn(IntegrationTestUtils.POSTGIS_ENDPOINT_CONFIG);
        })) {
            StackClient mockStackClient = new StackClient();
            String[] credentials = mockStackClient.getPostGisCredentials();
            // Verify that credentials are as follows
            assertEquals(IntegrationTestUtils.DOCKER_HOST_NAME + ":5431", credentials[0]);
            assertEquals(IntegrationTestUtils.TEST_POSTGIS_USER, credentials[1]);
            assertEquals(IntegrationTestUtils.TEST_POSTGIS_PASSWORD, credentials[2]);

        }
    }

    @Test
    void testGetAllTimeSeries() {
        // Insert these triples into the blazegraph
        SparqlClientTest.insertFacilityTriples(IntegrationTestUtils.SPATIAL_ZONE_SPARQL_ENDPOINT);
        SparqlClientTest.insertAssetTriples(IntegrationTestUtils.GENERAL_SPARQL_ENDPOINT, true);
        // Create a postgis password file
        IntegrationTestUtils.createPostGisPasswordFile();
        try (MockedConstruction<ContainerClient> mockClient = Mockito.mockConstruction(ContainerClient.class, (mock, context) -> {
            // Ensure all mocks return the test config class for the method to continue
            Mockito.when(mock.readEndpointConfig("blazegraph", BlazegraphEndpointConfig.class)).thenReturn(IntegrationTestUtils.SPARQL_ENDPOINT_CONFIG);
            Mockito.when(mock.readEndpointConfig("postgis", PostGISEndpointConfig.class)).thenReturn(IntegrationTestUtils.POSTGIS_ENDPOINT_CONFIG);
        })) {
            StackClient mockStackClient = new StackClient();
            mockStackClient.setDashboardUrl(IntegrationTestUtils.TEST_DASHBOARD_URL);
            String[] organisations = mockStackClient.getAllOrganisations();
            assertEquals(1, organisations.length); // verifies if there is only one organisation
            for (String organisation : organisations) {
                // Assumes that there is only one organisation and can hardcode their mappings
                Map<String, Map<String, List<String[]>>> timeSeries = mockStackClient.getAllTimeSeries(organisation);
                // Verify smart sensor
                verifyItemData(timeSeries, SparqlClientTest.SAMPLE_LAB_NAME, SparqlClientTest.SAMPLE_LAB_SMART_SENSOR_TYPE, SparqlClientTest.SAMPLE_LAB_SMART_SENSOR_NAME,
                        SparqlClientTest.TEMPERATURE, PostGisClientTest.SAMPLE_TEMPERATURE_COLUMN, PostGisClientTest.SAMPLE_TEMPERATURE_TABLE,
                        PostGisClientTest.TEMPERATURE_SQL_DATABASE, SparqlClientTest.TEMPERATURE_UNIT);
                // Verify rooms
                verifyItemData(timeSeries, SparqlClientTest.SAMPLE_OFFICE_NAME, StringHelper.ROOM_KEY, SparqlClientTest.SAMPLE_OFFICE_STAFF_ROOM_NAME,
                        SparqlClientTest.RELATIVE_HUMIDITY, PostGisClientTest.SAMPLE_ROOM_HUMIDITY_COLUMN, PostGisClientTest.SAMPLE_ROOM_HUMIDITY_TABLE,
                        PostGisClientTest.ROOM_SQL_DATABASE, "null");
                // Verify humidity exists
                List<String[]> thresholds = timeSeries.get(StringHelper.ROOM_KEY).get(StringHelper.THRESHOLD_KEY);
                thresholds.forEach((threshold) -> {
                    if (threshold[0].equals(SparqlClientTest.RELATIVE_HUMIDITY)){
                        assertEquals(threshold[1], String.valueOf(SparqlClientTest.RELATIVE_HUMIDITY_MIN_THRESHOLD));
                        assertEquals(threshold[2], String.valueOf(SparqlClientTest.RELATIVE_HUMIDITY_MAX_THRESHOLD));
                    }
                });

            }
        }
    }

    public static void verifyItemData(Map<String, Map<String, List<String[]>>> timeSeries, String facility, String itemType, String itemName, String measure, String column, String table, String database, String unit) {
        // Verifies if item exists in the facility mapping
        Map<String, List<String[]>> facilities = timeSeries.get(StringHelper.FACILITY_KEY);
        String[] items = facilities.get(facility).get(0);
        assertTrue(Arrays.asList(items).contains(itemName));
        // The key name will vary depending on if it is a room or asset
        String nestedKey = itemType.equals(StringHelper.ROOM_KEY) ? StringHelper.ROOM_KEY : StringHelper.ASSET_KEY;
        assertEquals(timeSeries.get(itemType).get(nestedKey).size(), 1);
        assertEquals(timeSeries.get(itemType).get(nestedKey).get(0)[0], itemName);
        // Extract the measure metadata
        List<String[]> measureData = timeSeries.get(itemType).get(measure);
        assertEquals(1, measureData.size()); // verifies if there is only one metadata for temperature
        assertEquals(measureData.get(0)[0], itemName);
        assertEquals(measureData.get(0)[1], column);
        assertEquals(measureData.get(0)[2], table);
        assertEquals(measureData.get(0)[3], database);
        assertEquals(measureData.get(0)[4], unit);
    }
}