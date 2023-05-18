package com.cmclinnovations.jurongisland;

public class EnvConfig {
    public static final String DATABASE = System.getenv("DATABASE");
    public static final String DATA_DIR = System.getenv("DATA_DIR");
    public static final String LAST_READ_FILE = System.getenv("LAST_READ_FILE");
    public static final String TIME_OFFSET_FILE = System.getenv("TIME_OFFSET_FILE");
    public static final String GEOSERVER_WORKSPACE = System.getenv("GEOSERVER_WORKSPACE");
    public static final String EMISSIONS_AGENT_URL = System.getenv("EMISSIONS_AGENT_URL");
    public static final String EMISSIONS_AGENT_IRI = System.getenv("EMISSIONS_AGENT_IRI");
    public static final String PYTHON_SERVICE_URL = System.getenv("PYTHON_SERVICE_URL");
    public static final String PARALLELISE_CALCULATIONS = System.getenv("PARALLELISE_CALCULATIONS");
    public static final String QUERY_ENDPOINT = System.getenv("QUERY_ENDPOINT");
    public static final String NUMBER_SOURCES = System.getenv("NUMBER_SOURCES");

    private EnvConfig() {
        throw new IllegalStateException();
    }
}
