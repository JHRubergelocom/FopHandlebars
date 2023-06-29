package test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class JiraRESTAPITest {

    private String loginToJira(String baseURL, String loginURL, String loginUserName, String loginPassword) throws IOException {
        // Create URL object
        URL url = new URL(baseURL + loginURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set properties
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        // Create JSON post data
        String input = "{\"username\":\"" + loginUserName + "\", \"password\":\"" + loginPassword + "\"}";

        // Send our request
        OutputStream os = conn.getOutputStream();
        os.write(input.getBytes());
        os.flush();

        // Handle our response
        String output = null;
        String loginResponse = "";
        if(conn.getResponseCode() == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((output = br.readLine()) != null) {
                loginResponse += output;
            }
            conn.disconnect();
        }
        System.out.println("logonResponse: " + loginResponse);
        return  loginResponse;
    }

    private String parseJSessionID(String jsonInput) {
        JsonElement jsonElement = JsonParser.parseString(jsonInput);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement session = jsonObject.get("session");
        JsonObject jsonObjectSession = session.getAsJsonObject();
        String jSessionID = jsonObjectSession.get("value").getAsString();

        System.out.println("jSessionID: " + jSessionID);
        return  jSessionID;

    }

    private String getJsonData(String baseURL, String biExportURL, String jSessionID, String analysisStartDate, String analysisEndDate) throws IOException {
        // URL url = new URL(baseURL + biExportURL + "?startDate=" + analysisStartDate + "&endDate=" + analysisEndDate);
        URL url = new URL(baseURL + "api/2/search" + "?jql%3assignee%3DJan-Hendrik%20Ruberg");
        String cookie = "JSESSIONID" + jSessionID;
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Cookie", cookie);

        String output = null;
        String jsonData = "";
        if(conn.getResponseCode() == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((output = br.readLine()) != null) {
                jsonData += output;
            }
            conn.disconnect();
        }
        System.out.println("jsonData: " + jsonData);
        return  jsonData;
    }


    private String formatAsCSV(String jsonData) {
        return "";
    }

    @Test
    public void TestLoginToJira() throws IOException {
        final String baseURL = "https://eloticksy.elo.com/rest/";
        final String loginURL = "auth/1/session";
        final String loginUserName = "J-H.Ruberg@elo.com";
        final String loginPassword = "XXX";

        final String jsonInput = loginToJira(baseURL, loginURL, loginUserName, loginPassword);
        final String jSessionID = parseJSessionID(jsonInput);

        final String biExportURL = "getbusinessintelligenceexport/1.0/message";
        final String analysisStartDate = "01-JAN-23";
        final String analysisEndDate = "31-JAN-23";

        String jsonData = getJsonData(baseURL, biExportURL, jSessionID, analysisStartDate, analysisEndDate);
/*
        jsonData = jsonData.replaceAll("\\\\", "");
        jsonData = jsonData.substring(jsonData.indexOf("["), jsonData.indexOf("]") + 1);
        jsonData = "{\"records\":" + jsonData + "}";

        final String csvData = formatAsCSV(jsonData);
 */
    }
}
