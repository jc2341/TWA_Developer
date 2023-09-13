package uk.ac.cam.cares.jps.agent.dashboard;

import com.google.gson.*;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;
import java.util.*;

public class IntegrationTestUtils {
    public static final String TEST_DASHBOARD_URL = "http://172.27.0.3:3000";
    public static final String TEST_POSTGIS_JDBC = "jdbc:postgresql://172.27.0.2:5432/";
    public static final String TEST_POSTGIS_URL = "http://172.27.0.2:5432/";
    public static final String TEST_POSTGIS_USER = "user";
    public static final String TEST_POSTGIS_PASSWORD = "pg123";
    public static final String NAME_KEY = "name";
    public static final String SERVICE_ACCOUNT_NAME = "grafana";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String SERVICE_ACCOUNT_ROUTE = "/api/serviceaccounts";
    private static final String SERVICE_ACCOUNT_SEARCH_SUB_ROUTE = "/search";
    private static final String DATA_SOURCE_ROUTE = "/api/datasources";
    private static final String ID_KEY = "id";
    public static final String DASHBOARD_ACCOUNT_USER = "admin";
    public static final String DASHBOARD_ACCOUNT_PASS = "admin";

    public static Object retrieveServiceAccounts(String user, String password) {
        String route = TEST_DASHBOARD_URL + SERVICE_ACCOUNT_ROUTE;
        // Send a get request to target API for a response
        HttpResponse response = sendGetRequest(route + SERVICE_ACCOUNT_SEARCH_SUB_ROUTE, user, password);
        // Will always be a hash map
        Map<String, Object> responseMap = (Map<String, Object>) retrieveResponseBody(response);
        // Response will always contain service accounts key, even if there is no account
        return responseMap.get("serviceAccounts");
    }

    public static JsonArray retrieveDataSources(String user, String password) {
        String route = TEST_DASHBOARD_URL + DATA_SOURCE_ROUTE;
        // Send a get request to target API for a response
        HttpResponse response = sendGetRequest(route, user, password);
        JsonArray responseMap = (JsonArray) retrieveResponseBody(response);
        return responseMap;
    }

    public static void deleteServiceAccounts() {
        String route = TEST_DASHBOARD_URL + SERVICE_ACCOUNT_ROUTE;
        List<Map<String, Object>> accountInfo = (List<Map<String, Object>>) retrieveServiceAccounts(DASHBOARD_ACCOUNT_USER, DASHBOARD_ACCOUNT_PASS);
        int accountId = -1;
        if (accountInfo.size() > 0 && accountInfo.get(0).get(NAME_KEY).equals(SERVICE_ACCOUNT_NAME)) {
            Double idDoubleFormat = (Double) accountInfo.get(0).get(ID_KEY);
            accountId = idDoubleFormat.intValue();
        }
        if (accountId != -1) {
            sendDeleteRequest(route + "/" + accountId, DASHBOARD_ACCOUNT_USER, DASHBOARD_ACCOUNT_PASS);
        }
    }

    public static void deleteDataSources() {
        List<Map<String, Object>> accountInfo = (List<Map<String, Object>>) retrieveServiceAccounts(DASHBOARD_ACCOUNT_USER, DASHBOARD_ACCOUNT_PASS);
        int accountId = -1;
        if (accountInfo.size() > 0 && accountInfo.get(0).get(NAME_KEY).equals(SERVICE_ACCOUNT_NAME)) {
            Double idDoubleFormat = (Double) accountInfo.get(0).get(ID_KEY);
            accountId = idDoubleFormat.intValue();
        }
        if (accountId != -1) {
            String route = TEST_DASHBOARD_URL + DATA_SOURCE_ROUTE;
            HttpResponse response = sendGetRequest(route, DASHBOARD_ACCOUNT_USER, DASHBOARD_ACCOUNT_PASS);
            JsonArray dataSources = (JsonArray) retrieveResponseBody(response);
            for (JsonElement dataSource : dataSources) {
                if (dataSource.isJsonObject()) {
                    JsonObject jsonObject = dataSource.getAsJsonObject();
                    String id = jsonObject.get("uid").getAsString();
                    sendDeleteRequest(route + "/uid/" + id, DASHBOARD_ACCOUNT_USER, DASHBOARD_ACCOUNT_PASS);
                }
            }
        }
    }
    
    public static Connection connectDatabase(String jdbc) {
        try {
            return DriverManager.getConnection(jdbc, TEST_POSTGIS_USER, TEST_POSTGIS_PASSWORD);
        } catch (Exception e) {
            throw new JPSRuntimeException("Unable to connect to database: " + e.getMessage());
        }
    }

    public static void updateDatabase(Connection connection, String query) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (Exception e) {
            throw new JPSRuntimeException("Unable to execute updates: " + e.getMessage());
        }
    }

    public static Object retrieveResponseBody(HttpResponse response) {
        JsonElement jsonResponse = JsonParser.parseString(response.body().toString());
        // When the response is a JSON Object
        if (jsonResponse.isJsonObject()) {
            Gson gson = new Gson();
            // Return a hashmap parsed from it
            return gson.fromJson(response.body().toString(), HashMap.class);
        } else {
            // When the response is a JSON array, return a JSON array
            return jsonResponse.getAsJsonArray();
        }
    }

    public static HttpResponse sendGetRequest(String url, String userName, String password) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(3600));
            // If username and password are provided, add the authentication
            if (!userName.isEmpty() && !password.isEmpty())
                requestBuilder.header("Authorization", getBasicAuthenticationHeader(userName, password));
            // Await response before continue executing the rest of the code
            return HTTP_CLIENT.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new JPSRuntimeException("Unable to connect or send request. Please ensure the url is valid. If valid, check the message for more details: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new JPSRuntimeException("Thread has been interrupted! " + e.getMessage());
        }
    }

    private static HttpResponse sendDeleteRequest(String url, String userName, String password) {
        try {
            HttpRequest requestBuilder = HttpRequest.newBuilder()
                    .header("Authorization", getBasicAuthenticationHeader(userName, password))
                    .uri(URI.create(url))
                    .DELETE()
                    .timeout(Duration.ofSeconds(3600))
                    .build();
            // Await response before continue executing the rest of the code
            return HTTP_CLIENT.send(requestBuilder, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new JPSRuntimeException("Unable to connect or send request. Please ensure the url is valid. If valid, check the message for more details: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new JPSRuntimeException("Thread has been interrupted! " + e.getMessage());
        }
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}