package uk.ac.cam.cares.jps.agent.historicalhouse45utilitiesagent;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DateTSClientDecoratorTest {
    private DateTSClientDecorator testDecorator;
    @Mock
    private TimeSeriesClient<LocalDate> mockTSClient;
    private static Map<String, String> testMappings;
    private static Map<String, List<?>> testReadings;
    private static final List<String> testIRIs = Arrays.asList("iri1", "iri2", "iri3");
    private static final String testDateKey = "dates";
    private static final int[] testDateArrays = {0, 1, 2};
    private static final String rdbUrl = "jdbc:postgresql://host.docker.internal:5432/test";
    private static final String rdbUser = "pass";
    private static final String rdbPassword = "user";

    @BeforeAll
    static void genTestData() {
        testMappings = new HashMap<>();
        testMappings.put("testKey", "testIRI");
        testReadings = new HashMap<>();
        testReadings.put("testKey", testIRIs);
    }

    @BeforeEach
    void initTSClient() {
        testDecorator = new DateTSClientDecorator(testDateKey);
        testDecorator.setTsClient(mockTSClient);
    }

    @Test
    void testDateTSClientWrapperConstructor() {
        assertAll(
                () -> assertNotNull(new DateTSClientDecorator(testDateKey)),
                () -> assertNotNull(new DateTSClientDecorator(testDateKey, testDateArrays))
        );
    }

    @Test
    void testSetRDBClient() {
        assertNull(testDecorator.getRDBClient());
        testDecorator.setRDBClient(rdbUrl, rdbUser, rdbPassword);
        RemoteRDBStoreClient testRdbClient = testDecorator.getRDBClient();
        assertAll(
                () -> assertEquals(rdbUrl, testRdbClient.getRdbURL()),
                () -> assertEquals(rdbUser, testRdbClient.getUser()),
                () -> assertEquals(rdbPassword, testRdbClient.getPassword())
        );
    }
}
