package uk.ac.cam.cares.jps.agent.dashboard.json.templating;

import java.util.List;

/**
 * A Java representation of a JSON-like model that encapsulates and enforces information
 * about PostgreSQL template variable syntax specific to Grafana dashboard. At the moment,
 * the PostgreSQL variables are used to link each asset to their corresponding time series column to perform correct SQL queries.
 *
 * @author qhouyee
 */
class PostgresVariable extends TemplateVariable {
    private final String DESCRIPTION;
    private final StringBuilder QUERY_SYNTAX = new StringBuilder();

    /**
     * Standard Constructor.
     *
     * @param measure       The measure name for this variable.
     * @param assetType     The asset type for this measure.
     * @param assetMeasures A list of time series column and asset names for different assets to be included into the query component.
     */
    public PostgresVariable(String measure, String assetType, List<String[]> assetMeasures) {
        // Variable name will be a combination of measure name and asset type to make it unique
        super(measure + assetType, 2);
        // Change the variable name to
        String assetTypeVariable = super.formatVariableName(assetType);
        // Description should follow the measure name and asset type
        this.DESCRIPTION = "A hidden template variable that displays the corresponding time series of " + measure.toLowerCase() + " for " + assetType.toLowerCase();
        // Append each value in the list in the required format
        this.QUERY_SYNTAX.append("SELECT k AS \\\"__text\\\", v AS \\\"__value\\\" FROM (values ");
        StringBuilder temp = new StringBuilder();
        for (String[] assetMeasure : assetMeasures) {
            // Only append a comma at the start if it is not the first value
            if (temp.length() != 0) temp.append(", ");
            // Append the name and the corresponding column name
            temp.append("('").append(assetMeasure[0]).append("', '")
                    .append(assetMeasure[1]).append("')");
        }
        this.QUERY_SYNTAX.append(temp).append(") AS v(k,v)  WHERE k IN (${").append(assetTypeVariable).append("});");
    }

    /**
     * Construct the Postgres variable as a String.
     *
     * @return The Postgres variable syntax as a String.
     */
    @Override
    protected String construct() {
        // Construct the common elements
        StringBuilder builder = super.genCommonJson()
                // Description for this variable
                .append("\"description\": \"").append(this.DESCRIPTION).append("\",")
                // Query values of this variable
                .append("\"definition\": \"").append(this.QUERY_SYNTAX).append("\",")
                .append("\"query\": \"").append(this.QUERY_SYNTAX).append("\",")
                // Default settings but unsure what they are for
                .append("\"regex\": \"\",")
                .append("\"sort\" : 0,")
                // Variable type must be set as query to work
                .append("\"type\": \"query\"")
                .append("}");
        return builder.toString();
    }
}
