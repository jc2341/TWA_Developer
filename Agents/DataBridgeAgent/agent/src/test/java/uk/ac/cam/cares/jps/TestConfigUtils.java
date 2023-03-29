package uk.ac.cam.cares.jps;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class TestConfigUtils {
    private static final String originSparqlKey = "sparql.origin.endpoint";
    private static final String destinationSparqlKey = "sparql.destination.endpoint";
    private static final String srcDBUrl = "src.db.url";
    private static final String srcDBUser = "src.db.user";
    private static final String srcDBPass = "src.db.password";
    private static final String targetDBUrl  = "target.db.url";
    private static final String targetDBUser = "target.db.user";
    private static final String targetDBPass = "target.db.password";


    public static File genSampleSPARQLConfigFile(boolean isComplete, String originSparql, String destinationSparql) throws IOException {
        File file = new File(System.getProperty("user.dir") + "/config/endpoint.properties");
        // Check if the directory exists, create it if it doesn't
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        PrintWriter writer = new PrintWriter(file);
        writer.println(originSparqlKey + "=" + originSparql);
        if (isComplete) {
            writer.println(destinationSparqlKey + "=" + destinationSparql);
        }
        writer.close();
        return file;
    }

    public static File genSampleSQLConfigFile(boolean isComplete, String srcDb, String srcDbUser, String srcDbPass,
                                              String tgtDb, String tgtDbUser, String tgtDbPass) throws IOException {
        File file = new File(System.getProperty("user.dir") + "/config/endpoint.properties");
        // Check if the directory exists, create it if it doesn't
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        PrintWriter writer = new PrintWriter(file);
        writer.println(srcDBUrl + "=" + srcDb);
        writer.println(targetDBUrl + "=" + tgtDb);
        writer.println(targetDBUser + "=" + tgtDbUser);
        writer.println(targetDBPass + "=" + tgtDbPass);
        if (isComplete) {
            writer.println(srcDBUser + "=" + srcDbUser);
            writer.println(srcDBPass + "=" + srcDbPass);
        }
        writer.close();
        return file;
    }
}
