package uk.ac.cam.cares.jps.agent.dashboard.json.panel;

import uk.ac.cam.cares.jps.agent.dashboard.utils.StringHelper;

import java.util.List;

/**
 * A Java representation of a JSON-like model that encapsulates and enforces information
 * about gauge chart syntax specific to Grafana dashboard.
 *
 * @author qhouyee
 */
class Gauge extends TemplatePanel {
    private boolean SHOW_THRESHOLD_MARKERS = false;
    private String COLOR_MODE = "palette-classic";
    private String COLOR_STEPS = "{\"color\":\"red\",\"value\":80}";
    private String MIN_MAX_VALS = "";

    /**
     * An alternate constructor that will generate a gauge chart for all items.
     *
     * @param measure            The measure name for this variable.
     * @param itemGroup          The item group for this measure - asset type or rooms.
     * @param unit               Optional measure unit symbol. Can be null.
     * @param databaseId         The database connection ID generated by Grafana.
     * @param timeSeriesMetadata A list of items and their metadata for the specified measure.
     */
    public Gauge(String measure, String itemGroup, String unit, String databaseId, List<String[]> timeSeriesMetadata, String[] thresholds) {
        this(measure, itemGroup, unit, databaseId, timeSeriesMetadata, thresholds, false);
    }


    /**
     * Standard Constructor.
     *
     * @param measure            The measure name for this variable.
     * @param itemGroup          The item group for this measure - asset type or rooms.
     * @param unit               Optional measure unit symbol. Can be null.
     * @param databaseId         The database connection ID generated by Grafana.
     * @param timeSeriesMetadata A list of items and their metadata for the specified measure.
     * @param thresholds         The min and max threshold for this gauge if available.
     * @param calcAggregate      A boolean indicator that will generate a gauge chart displaying the current aggregate value within the group.
     */
    public Gauge(String measure, String itemGroup, String unit, String databaseId, List<String[]> timeSeriesMetadata, String[] thresholds, boolean calcAggregate) {
        super(measure, itemGroup, databaseId, timeSeriesMetadata);
        // Sets the unit for retrieval
        super.setUnit(unit);
        // Title and description depends on requirements
        if (calcAggregate) {
            // Set Title for panel - Systems will generate the latest total while rooms generate the latest average
            String titleContent = itemGroup.equals("systems") ? "Latest Cumulative Total" : "Latest Average";
            super.setTitle(titleContent);
            // Set Description for panel
            String description = itemGroup.equals("systems") ? "A gauge chart displaying the latest cumulative total value of " + measure.toLowerCase() + " for all " + itemGroup.toLowerCase()
                    : "A gauge chart displaying the latest average value of " + measure.toLowerCase() + " for all " + itemGroup.toLowerCase();
            description += "; Do note that this value is constant at the dashboard level and unaffected by any filters for individual elements";
            super.setDescription(description);
            // Generate and set the related query if required
            int totalItems = 0;
            StringBuilder aggregateQuery = new StringBuilder();
            StringBuilder summation = new StringBuilder();
            for (String[] metadata : timeSeriesMetadata) {
                // Only append a plus sign at the start if it is not the first column
                if (summation.length() != 0) summation.append("+");
                summation.append("\\\"").append(metadata[1]).append("\\\"");
                totalItems++;
            }
            // Systems require a simple summation to compute the total. Rooms require an average computed from the summation
            String aggregateVar = itemGroup.equals("systems") ? summation.toString() : "(" + summation + ")/" + totalItems;
            aggregateQuery.append("SELECT time AS \\\"time\\\",").append(aggregateVar).append(" FROM \\\"")
                    .append(timeSeriesMetadata.get(0)[2]) // Retrieve table name and assumes they are all the same for the same measure
                    .append("\\\" WHERE $__timeFilter(time)");
            // Reset the query in the inherited class
            super.setQuery(aggregateQuery);
        } else {
            // Default options
            // Set Title for panel
            // Title is: Current Measure [Unit]
            String titleContent = "Latest " + StringHelper.addSpaceBetweenCapitalWords(measure);
            titleContent = unit.equals("null") ? titleContent : titleContent + " [" + unit + "]"; // Unit is optional
            super.setTitle(titleContent);
            // Set Description for panel
            // Description should follow the measure name and item group
            String description = "A gauge chart displaying the latest value of all individuals' " + measure.toLowerCase() + " for " + itemGroup.toLowerCase();
            super.setDescription(description);
        }
        // If there are thresholds, override the following items
        if (thresholds.length != 0) {
            this.SHOW_THRESHOLD_MARKERS = true;
            this.COLOR_MODE = "thresholds";
            // Color steps should be from min (green) to max (red) threshold
            this.COLOR_STEPS = "{\"color\":\"green\",\"value\":" + thresholds[0] + "},{\"color\":\"red\",\"value\":" + thresholds[1] + "}";
            // Ensure that the gauge can see past the min and max threshold to allow user to recognise the limits
            double minValue = Float.parseFloat(thresholds[0]) - 1.0;
            double maxValue = Float.parseFloat(thresholds[1]) + 1.0;
            this.MIN_MAX_VALS = "\"min\":" + minValue + ",\"max\":" + maxValue + ",";
        }
    }

    /**
     * Construct the Gauge Chart syntax as a String.
     *
     * @param height    Height of the panel.
     * @param width     Width of the panel.
     * @param xPosition X position within the dashboard.
     * @param yPosition Y position within the dashboard.
     * @return The Gauge Chart syntax as a String.
     */
    @Override
    protected String construct(int height, int width, int xPosition, int yPosition) {
        StringBuilder builder = new StringBuilder();
        builder.append("{").append(super.genCommonJson(height, width, xPosition, yPosition))
                // Chart type must be set to gauge
                .append(",\"type\": \"gauge\",")
                // Field Configuration
                .append("\"fieldConfig\":{")
                // Default field configuration
                .append("\"defaults\":{\"color\":{\"mode\": \"").append(this.COLOR_MODE).append("\"},")
                .append("\"thresholds\":{\"mode\": \"absolute\",")
                .append("\"steps\": [{\"color\":\"red\",\"value\":null},").append(this.COLOR_STEPS).append("]},")
                .append(this.MIN_MAX_VALS)
                .append("\"mappings\": []")
                .append("},") // End of defaults
                .append("\"overrides\": []")
                .append("},") // End of field configuration
                // Options
                .append("\"options\":{")
                // Legend options
                .append("\"reduceOptions\": {\"values\": false,\"calcs\": [\"lastNotNull\"],\"fields\": \"\"},")
                // Tooltip options
                .append("\"orientation\": \"auto\",\"showThresholdLabels\":false,\"showThresholdMarkers\":").append(this.SHOW_THRESHOLD_MARKERS).append(",\"text\": {}")
                .append("}") // end of options
                .append("}");
        return builder.toString();
    }
}
