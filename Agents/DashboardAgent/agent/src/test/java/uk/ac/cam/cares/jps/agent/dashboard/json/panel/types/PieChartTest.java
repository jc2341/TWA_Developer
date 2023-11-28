package uk.ac.cam.cares.jps.agent.dashboard.json.panel.types;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.cares.jps.agent.dashboard.TestUtils;
import uk.ac.cam.cares.jps.agent.dashboard.utils.StringHelper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PieChartTest {
    private static final List<String[]> SAMPLE_METADATA = new ArrayList<>();
    private static final String SAMPLE_MEASURE = "ElectricalConsumption";
    private static final String SAMPLE_UNIT = "kwh";
    private static final String SAMPLE_DATABASE_ID = "3831j";
    private static final String SAMPLE_FIRST_SYSTEM_NAME = "Emergency systems";
    private static final String SAMPLE_FIRST_SYSTEM_COL_NAME = "column4";
    private static final String SAMPLE_SEC_SYSTEM_NAME = "Kitchen system";
    private static final String SAMPLE_SEC_SYSTEM_COL_NAME = "column27";
    private static final String SAMPLE_SYSTEM_TABLE_NAME = "table4";
    private static final int SAMPLE_PANEL_HEIGHT = 8;
    private static final int SAMPLE_PANEL_WIDTH = 12;
    private static final int SAMPLE_PANEL_X_POSITION = 1;
    private static final int SAMPLE_PANEL_Y_POSITION = 0;

    @BeforeAll
    static void setup() {
        SAMPLE_METADATA.add(new String[]{SAMPLE_FIRST_SYSTEM_NAME, SAMPLE_FIRST_SYSTEM_COL_NAME, SAMPLE_SYSTEM_TABLE_NAME});
        SAMPLE_METADATA.add(new String[]{SAMPLE_SEC_SYSTEM_NAME, SAMPLE_SEC_SYSTEM_COL_NAME, SAMPLE_SYSTEM_TABLE_NAME});
    }

    @Test
    void testConstructor() {
        // Generate expected inputs
        String[] expectedConfigItems = new String[]{SAMPLE_MEASURE, StringHelper.SYSTEM_KEY, SAMPLE_SYSTEM_TABLE_NAME, SAMPLE_DATABASE_ID, "null"};
        int[] expectedGeometryPosition = new int[]{SAMPLE_PANEL_HEIGHT, SAMPLE_PANEL_WIDTH, SAMPLE_PANEL_X_POSITION, SAMPLE_PANEL_Y_POSITION};
        // Execute the method
        PieChart chart = new PieChart(SAMPLE_MEASURE, StringHelper.SYSTEM_KEY, "null", SAMPLE_DATABASE_ID, SAMPLE_METADATA);
        // Verify results
        String result = chart.construct(SAMPLE_PANEL_HEIGHT, SAMPLE_PANEL_WIDTH, SAMPLE_PANEL_X_POSITION, SAMPLE_PANEL_Y_POSITION);
        assertEquals(genExpectedResults(expectedConfigItems, expectedGeometryPosition, SAMPLE_METADATA), result);
    }

    @Test
    void testConstruct_NoUnit() {
        // Generate expected inputs
        String[] expectedConfigItems = new String[]{SAMPLE_MEASURE, StringHelper.SYSTEM_KEY, SAMPLE_SYSTEM_TABLE_NAME, SAMPLE_DATABASE_ID, "null"};
        int[] expectedGeometryPosition = new int[]{SAMPLE_PANEL_HEIGHT, SAMPLE_PANEL_WIDTH, SAMPLE_PANEL_X_POSITION, SAMPLE_PANEL_Y_POSITION};
        // Construct the object
        PieChart chart = new PieChart(SAMPLE_MEASURE, StringHelper.SYSTEM_KEY, "null", SAMPLE_DATABASE_ID, SAMPLE_METADATA);
        // Execute the method
        String result = chart.construct(SAMPLE_PANEL_HEIGHT, SAMPLE_PANEL_WIDTH, SAMPLE_PANEL_X_POSITION, SAMPLE_PANEL_Y_POSITION);
        // Verify results
        assertEquals(genExpectedResults(expectedConfigItems, expectedGeometryPosition, SAMPLE_METADATA), result);
        assertEquals("null", chart.getUnit());
    }

    @Test
    void testConstruct_WithUnit() {
        // Generate expected inputs
        String[] expectedConfigItems = new String[]{SAMPLE_MEASURE, StringHelper.SYSTEM_KEY, SAMPLE_SYSTEM_TABLE_NAME, SAMPLE_DATABASE_ID, SAMPLE_UNIT};
        int[] expectedGeometryPosition = new int[]{SAMPLE_PANEL_HEIGHT, SAMPLE_PANEL_WIDTH, SAMPLE_PANEL_X_POSITION, SAMPLE_PANEL_Y_POSITION};
        // Construct the object
        PieChart chart = new PieChart(SAMPLE_MEASURE, StringHelper.SYSTEM_KEY, SAMPLE_UNIT, SAMPLE_DATABASE_ID, SAMPLE_METADATA);
        // Execute the method
        String result = chart.construct(SAMPLE_PANEL_HEIGHT, SAMPLE_PANEL_WIDTH, SAMPLE_PANEL_X_POSITION, SAMPLE_PANEL_Y_POSITION);
        // Verify results
        assertEquals(genExpectedResults(expectedConfigItems, expectedGeometryPosition, SAMPLE_METADATA), result);
        assertEquals(SAMPLE_UNIT, chart.getUnit());
    }

    @Test
    void testGetMeasure() {
        // Construct the object
        PieChart chart = new PieChart(SAMPLE_MEASURE, StringHelper.SYSTEM_KEY, SAMPLE_UNIT, SAMPLE_DATABASE_ID, SAMPLE_METADATA);
        // Execute the method and verify result
        assertEquals(SAMPLE_MEASURE, chart.getMeasure());
    }

    public static String genExpectedResults(String[] metadata, int[] geometryPositions, List<String[]> itemDetails) {
        String titleContent = "Latest " + StringHelper.addSpaceBetweenCapitalWords(metadata[0]) + " distribution";
        titleContent = metadata[4].equals("null") ? titleContent : titleContent + " [" + metadata[4] + "]";
        // Similar to above
        String description = "A pie chart displaying the latest distribution for " + metadata[0].toLowerCase() + " of " + metadata[1].toLowerCase();
        String expectedTransformations = "[" + TransformationOptionsTest.genExpectedOrganizeTransformation(itemDetails, "") + "]";
        // For generating the query
        StringBuilder queryBuilder = new StringBuilder();
        StringBuilder dataCols = new StringBuilder();
        String tableName = itemDetails.get(0)[2];
        for (String[] data : itemDetails) {
            if (dataCols.length() != 0) dataCols.append(",");
            dataCols.append("\\\"").append(data[1]).append("\\\"");
        }
        queryBuilder.append("SELECT time AS \\\"time\\\",").append(dataCols).append(" ")
                .append("FROM \\\"").append(tableName).append("\\\" WHERE $__timeFilter(time)");
        // Construct the expected syntax
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(TestUtils.genExpectedCommonTemplatePanelJson(titleContent, description, expectedTransformations, metadata, geometryPositions, itemDetails, queryBuilder.toString()))
                .append(",\"type\":\"piechart\",")
                .append("\"pluginVersion\":\"10.0.3\",")
                .append("\"fieldConfig\": {")
                .append("\"defaults\": {\"color\": {\"mode\": \"palette-classic\"},")
                .append("\"custom\":{").append("\"hideFrom\":{\"legend\":false,\"tooltip\":false,\"viz\":false}").append("},")
                .append("\"mappings\": []")
                .append("},")
                .append("\"overrides\": []")
                .append("},")
                // Options
                .append("\"options\":{")
                .append("\"legend\":{\"displayMode\":\"list\",\"placement\":\"right\",\"showLegend\":true},")
                .append("\"tooltip\":{\"mode\":\"single\",\"sort\":\"none\"},")
                .append("\"displayLabels\":[\"percent\"],")
                .append("\"pieType\":\"donut\",")
                .append("\"reduceOptions\": {\"calcs\":[\"lastNotNull\"],\"fields\":\"\",\"values\":false}")
                .append("}") // end of options
                .append("}");
        return sb.toString();
    }
}