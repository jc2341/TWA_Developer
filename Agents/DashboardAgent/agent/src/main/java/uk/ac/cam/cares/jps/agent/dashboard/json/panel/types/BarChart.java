package uk.ac.cam.cares.jps.agent.dashboard.json.panel.types;

import uk.ac.cam.cares.jps.agent.dashboard.utils.StringHelper;

import java.util.List;

/**
 * A Java representation of a JSON-like model that encapsulates and enforces information
 * about bar chart syntax specific to Grafana dashboard.
 *
 * @author qhouyee
 */
public class BarChart extends TemplatePanel {
    /**
     * Standard Constructor.
     *
     * @param measure            The measure name for this variable.
     * @param itemGroup          The item group for this measure - asset type or rooms.
     * @param unit               Optional measure unit symbol. Can be null.
     * @param databaseId         The database connection ID generated by Grafana.
     * @param timeSeriesMetadata A list of items and their metadata for the specified measure.
     */
    public BarChart(String measure, String itemGroup, String unit, String databaseId, List<String[]> timeSeriesMetadata) {
        super(measure, databaseId);
        // Sets the unit for retrieval
        super.setUnit(unit);
        // Set Title for panel
        // Title is: Measure trends [Unit]
        String titleContent = StringHelper.addSpaceBetweenCapitalWords(measure) + " trends";
        titleContent = unit.equals("null") ? titleContent : titleContent + " [" + unit + "]"; // Unit is optional
        super.setTitle(titleContent);
        // Set Description for panel
        // Description should follow the measure name and item group
        String description = "A bar chart displaying the trends for " + measure.toLowerCase() + " over a specific period";
        super.setDescription(description);
        String tableName = timeSeriesMetadata.get(0)[2];
        // Set a query for daily measure amount for the past seven days
        StringBuilder query = new StringBuilder().append("SELECT to_char(time, 'DD-Mon-YY') as \\\"Day\\\", ${")
                // Custom csv parameter must be lower case with no spacing ie: measurenameitemgroup
                .append(StringHelper.formatVariableName(measure)).append(StringHelper.formatVariableName(itemGroup)).append(":csv} ")
                .append("FROM \\\"").append(tableName).append("\\\" ")
                // Time period from last 6 days to the specified time in Grafana
                .append("WHERE time BETWEEN TO_TIMESTAMP(${__to}/1000) - interval '6 day' AND TO_TIMESTAMP(${__to}/1000) ")
                // Arrange the results from the latest day
                .append("ORDER BY (EXTRACT(DOW FROM time)- EXTRACT(DOW FROM TO_TIMESTAMP(${__to}/1000)) + 6) % 7;");
        super.setQuery(query);
        // Apply an aggregate transformation before renaming the fields
        super.TRANSFORMATIONS.addGroupByTransformation("range", timeSeriesMetadata);
        // Add a white space as the group by transformation in Grafana appends the following
        super.TRANSFORMATIONS.addOrganizeTransformation(" (range)", timeSeriesMetadata);
    }

    /**
     * Construct the Bar Chart syntax as a String.
     *
     * @param height    Height of the panel.
     * @param width     Width of the panel.
     * @param xPosition X position within the dashboard.
     * @param yPosition Y position within the dashboard.
     * @return The Bar Chart syntax as a String.
     */
    @Override
    public String construct(int height, int width, int xPosition, int yPosition) {
        StringBuilder builder = new StringBuilder();
        builder.append("{").append(super.genCommonJson(height, width, xPosition, yPosition))
                // Chart type must be set to time series
                .append(",\"type\": \"barchart\",")
                // Plugin version
                .append("\"pluginVersion\": \"10.0.3\",")
                // Field Configuration
                .append("\"fieldConfig\": { ")
                // Default field configuration
                .append("\"defaults\": {\"color\": {\"mode\": \"palette-classic\"},")
                // Custom parts of field configurations
                .append("\"custom\":{").append("\"axisCenteredZero\":false,\"axisColorMode\":\"text\",")
                .append("\"axisLabel\":\"\",\"axisPlacement\":\"auto\", \"barAlignment\":0, \"drawStyle\":\"line\",")
                .append("\"fillOpacity\":80,\"gradientMode\":\"none\",\"lineWidth\":1,")
                .append("\"hideFrom\":{\"legend\":false, \"tooltip\":false, \"viz\":false},")
                .append("\"scaleDistribution\":{\"type\":\"linear\"}, \"showPoints\":\"auto\", \"spanNulls\":false,")
                .append("\"stacking\":{\"group\":\"A\", \"mode\":\"none\"}, \"thresholdsStyle\":{\"mode\":\"off\"}")
                .append("},") // End of custom parts
                // Thresholds
                .append("\"thresholds\":{\"mode\": \"absolute\", \"steps\": [" +
                        "{\"color\":\"green\",\"value\":null},{\"color\":\"red\",\"value\":80},}]},)")
                .append("\"mappings\": []")
                .append("},") // End of defaults
                .append("\"overrides\": []")
                .append("},") // End of field configuration
                // Options
                .append("\"options\":{")
                // Legend options
                .append("\"legend\":{\"calcs\": [], \"displayMode\":\"list\",\"placement\":\"bottom\",\"showLegend\":true},")
                // Tooltip options
                .append("\"tooltip\":{\"mode\":\"single\",\"sort\":\"none\"},")
                // Bar chart options
                .append("\"barRadius\":0,\"barWidth\":0.97,\"fullHighlight\":false,\"groupWidth\":0.7,")
                .append("\"orientation\":\"auto\",\"showValue\":\"never\",\"stacking\":\"normal\",")
                .append("\"xTickLabelRotation\":0,\"xTickLabelSpacing\":0")
                .append("}") // end of options
                .append("}");
        return builder.toString();
    }
}
