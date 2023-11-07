package uk.ac.cam.cares.jps.agent.dashboard.json.templating;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.cam.cares.jps.agent.dashboard.utils.StringHelper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PostgresVariableTest {
    private static final String EXPECTED_ASSET_TYPE = "Fridge";
    private static final String EXPECTED_MEASURE = "Energy Consumption";
    private static final String DATABASE_ID = "nhsaf781rh";
    private static final Map<String, List<String>> FACILITY_ITEM_MAPPING = new HashMap<>();
    private static final String FACILITY_ONE = "Home";
    private static final String FACILITY_ONE_ASSET = "Bed";
    private static final String FACILITY_TWO = "Bakery";
    private static final String FACILITY_TWO_ASSET = "Oven";
    private static final String FACILITY_TWO_ROOM = "Kitchen";
    private static final List<String[]> ASSET_TS_COL_LIST = new ArrayList<>();
    private static final String[] TEST_SET1 = new String[]{"F1", "column1"};
    private static final String[] TEST_SET2 = new String[]{"F2", "column2"};
    private static final String[] TEST_SET3 = new String[]{"F3", "column3"};

    @BeforeAll
    static void genTestAssetMeasureList() {
        FACILITY_ITEM_MAPPING.put(FACILITY_ONE, List.of(FACILITY_ONE_ASSET));
        FACILITY_ITEM_MAPPING.put(FACILITY_TWO, Arrays.asList(FACILITY_TWO_ASSET, FACILITY_TWO_ROOM));
        ASSET_TS_COL_LIST.add(TEST_SET1);
        ASSET_TS_COL_LIST.add(TEST_SET2);
        ASSET_TS_COL_LIST.add(TEST_SET3);
    }

    @Test
    void testConstruct_ItemFilter() {
        // Construct the object through the alternate constructor
        PostgresVariable variable = new PostgresVariable(EXPECTED_ASSET_TYPE, FACILITY_ITEM_MAPPING, DATABASE_ID);
        // Execute the method
        String result = variable.construct();
        // Test outputs
        assertEquals(genExpectedPostgresVarSyntaxForItemFilter(EXPECTED_ASSET_TYPE, DATABASE_ID, FACILITY_ITEM_MAPPING), result);
    }

    @Test
    void testConstruct_MeasureFilter() {
        // Construct the object through the alternate constructor
        PostgresVariable variable = new PostgresVariable(EXPECTED_MEASURE, EXPECTED_ASSET_TYPE, DATABASE_ID, ASSET_TS_COL_LIST);
        // Execute the method
        String result = variable.construct();
        // Test outputs
        assertEquals(genExpectedPostgresVarSyntaxForMeasureFilter(EXPECTED_MEASURE, EXPECTED_ASSET_TYPE, DATABASE_ID, ASSET_TS_COL_LIST), result);
    }

    public static String genExpectedPostgresVarSyntaxForItemFilter(String itemType, String databaseID, Map<String, List<String>> facilityItemMapping) {
        String formattedItemType = itemType.toLowerCase().replaceAll("\\s", "");
        String label = itemType.equals(StringHelper.ROOM_KEY) ? "Rooms" : StringHelper.addSpaceBetweenCapitalWords(itemType);
        String description = "A template variable that filters the items of " + itemType.toLowerCase() + " type.";
        List<String[]> parsedMappings = new ArrayList<>();
        // Parse the input map to give a list containing arrays in the form of [Facility, Item]
        for (String facility : facilityItemMapping.keySet()) {
            List<String> containingItems = facilityItemMapping.get(facility);
            containingItems.forEach((item) -> {
                String[] mapping = new String[2];
                mapping[0] = facility;
                mapping[1] = item;
                parsedMappings.add(mapping);
            });
        }
        String query = "SELECT v AS \\\"__value\\\" FROM (values " +
                genValueQueryForListOfArrays(parsedMappings) + ") AS v(k,v)  WHERE k IN (${" + StringHelper.FACILITY_KEY + "});";
        return genExpectedPostgresVarSyntax(formattedItemType, label, description, databaseID, query, 0);
    }

    public static String genExpectedPostgresVarSyntaxForMeasureFilter(String measure, String assetType, String databaseID, List<String[]> assetMeasureMap) {
        String formattedMeasure = measure.toLowerCase().replaceAll("\\s", "");
        String formattedAssetType = assetType.toLowerCase().replaceAll("\\s", "");
        String description = "A hidden template variable that displays the corresponding time series of " + measure.toLowerCase() + " for " + assetType.toLowerCase();
        String query = "SELECT k AS \\\"__text\\\", v AS \\\"__value\\\" FROM (values " +
                genValueQueryForListOfArrays(assetMeasureMap) + ") AS v(k,v)  WHERE k IN (${" + formattedAssetType + "});";
        return genExpectedPostgresVarSyntax(formattedMeasure + formattedAssetType, "", description, databaseID, query, 2);
    }

    private static String genExpectedPostgresVarSyntax(String title, String label, String description, String databaseID, String query, int displayOption) {
        StringBuilder results = new StringBuilder();
        results.append(TemplateVariableTest.genExpectedCommonJsonBase(title, displayOption))
                .append("\"label\": \"").append(label).append("\",")
                .append("\"datasource\": {\"type\": \"postgres\", \"uid\": \"").append(databaseID).append("\"},")
                .append("\"description\": \"").append(description).append("\",")
                .append("\"definition\": \"").append(query).append("\",")
                .append("\"query\": \"").append(query).append("\"")
                .append(",\"regex\": \"\",\"sort\" : 0,\"type\": \"query\"}");
        return results.toString();
    }

    private static StringBuilder genValueQueryForListOfArrays(List<String[]> assetMeasureMap) {
        StringBuilder results = new StringBuilder();
        for (String[] asset : assetMeasureMap) {
            // Append comma before if it is not the only value
            if (results.length() != 0) results.append(", ");
            results.append("('").append(asset[0]).append("', '")
                    .append(asset[1]).append("')");
        }
        return results;
    }
}