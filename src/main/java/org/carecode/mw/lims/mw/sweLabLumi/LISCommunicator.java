package org.carecode.mw.lims.mw.sweLabLumi;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.carecode.lims.libraries.AnalyzerDetails;
import org.carecode.lims.libraries.DataBundle;
import org.carecode.lims.libraries.LimsSettings;
import org.carecode.lims.libraries.MiddlewareSettings;
import org.carecode.lims.libraries.QueryRecord;
import org.carecode.lims.libraries.ResultsRecord;

public class LISCommunicator {
    
    public static final Logger logger = LogManager.getLogger(SweLabLumiServer.class.getName());

//    static boolean testing = true;
    private static final Gson gson = new Gson();

    public static DataBundle pullTestOrdersForSampleRequests(QueryRecord queryRecord) {
        System.out.println("pullTestOrdersForSampleRequests");
//        if (testing) {
//            PatientDataBundle pdb = new PatientDataBundle();
//            List<String> testNames = Arrays.asList("HDL", "RF2");
//            OrderRecord or = new OrderRecord(0, queryRecord.getSampleId(), testNames, "S", new Date(), "testInformation");
//            pdb.getOrderRecords().add(or);
//            PatientRecord pr = new PatientRecord(0, "1010101", "111111", "Buddhika Ariyaratne", "M H B", "Male", "Sinhalese", null, "Galle", "0715812399", "Dr Niluka");
//            pdb.setPatientRecord(pr);
//            return pdb;
//        }

        try {
            String postSampleDataEndpoint = SettingsLoader.getSettings().getLimsSettings().getLimsServerBaseUrl();
            System.out.println("postSampleDataEndpoint = " + postSampleDataEndpoint);
            URL url = new URL(postSampleDataEndpoint + "/test_orders_for_sample_requests");
            System.out.println("url = " + url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            System.out.println("queryRecord = " + queryRecord);
            // Convert QueryRecord to JSON

            DataBundle databundle = new DataBundle();
            databundle.setMiddlewareSettings(SettingsLoader.getSettings());
            databundle.getQueryRecords().add(queryRecord);
            String jsonInputString = gson.toJson(databundle);
            System.out.println("jsonInputString = " + jsonInputString);
            // Send the request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode = " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("OK responseCode = " + responseCode);
                // Process response
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("response.toString() = " + response.toString());
                // Convert the response to a PatientDataBundle object
                DataBundle patientDataBundle = gson.fromJson(response.toString(), DataBundle.class);
                System.out.println("patientDataBundle = " + patientDataBundle);
                return patientDataBundle;
            } else {
                System.out.println("POST request failed. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public static void pushResults(DataBundle db) {
        System.out.println("pushResults = ");
        Gson gson = new Gson(); // Ensure Gson is available here for serializing and deserializing
        try {
           
            String pushResultsEndpoint =  SettingsLoader.getSettings().getLimsSettings().getLimsServerBaseUrl() + "/test_results";
            System.out.println("pushResultsEndpoint = " + pushResultsEndpoint);
            URL url = new URL(pushResultsEndpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            MiddlewareSettings ms = new MiddlewareSettings();
            AnalyzerDetails ad = new AnalyzerDetails();
            LimsSettings ls = new LimsSettings();
            ls.setUsername("buddhika"); 
            ls.setPassword("Buddhika123@"); 
            ms.setAnalyzerDetails(ad);
            ms.setLimsSettings(ls);
            ms.getAnalyzerDetails().setAnalyzerName("SwelabLumi");
            // Set middleware settings and serialize DataBundle to JSON
            db.setMiddlewareSettings(ms );
            String jsonInputString = gson.toJson(db);
            System.out.println("jsonInputString = " + jsonInputString);

            // Send the JSON in the request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode = " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("ok");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("response.toString() = " + response.toString());

                // Optionally process the server response (if needed)
                JsonObject responseObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                logger.info("Response from server: " + responseObject.toString());

                // Extract status
                String status = responseObject.get("status").getAsString();
                logger.info("Status: " + status);

                // Extract the list of ResultsRecord objects
                JsonArray detailsArray = responseObject.getAsJsonArray("details");

                // Deserialize the JSON array into a list of ResultsRecord objects
                List<ResultsRecord> resultsRecords = new ArrayList<>();
                for (JsonElement element : detailsArray) {
                    ResultsRecord record = gson.fromJson(element, ResultsRecord.class);
                    resultsRecords.add(record);
                }

                // Log and process the ResultsRecord objects as needed
                for (ResultsRecord record : resultsRecords) {
                    logger.info("Sample ID: " + record.getSampleId()
                            + ", Test: " + record.getTestCode()
                            + ", Status: " + record.getStatus());
                }

            } else {
                System.out.println("POST request failed. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void pushResultsOld(DataBundle patientDataBundle) {
        System.out.println("pushResults = ");
        try {
            System.out.println("SettingsLoader.getSettings() = " + SettingsLoader.getSettings());
            System.out.println("SettingsLoader.getSettings().getLimsSettings() = " + SettingsLoader.getSettings().getLimsSettings());
            System.out.println("SettingsLoader.getSettings().getLimsSettings().getLimsServerBaseUrl() = " + SettingsLoader.getSettings().getLimsSettings().getLimsServerBaseUrl());
            String pushResultsEndpoint = SettingsLoader.getSettings().getLimsSettings().getLimsServerBaseUrl() + "/test_results";

            
            for(ResultsRecord rr:patientDataBundle.getResultsRecords()){
                System.out.println("rr value  = " + rr.getResultValue() + "");
                System.out.println("rr value string = " + rr.getResultValueString());
            }
            
            
            URL url = new URL(pushResultsEndpoint);
            System.out.println("url = " + url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            // Serialize PatientDataBundle to JSON
            patientDataBundle.setMiddlewareSettings(SettingsLoader.getSettings());
            String jsonInputString = gson.toJson(patientDataBundle);
            System.out.println("jsonInputString = " + jsonInputString);
            // Send the JSON in the request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode = " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("ok");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println("response.toString() = " + response.toString());

                // Optionally process the server response (if needed)
                JsonObject responseObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                SweLabLumi.logger.info("Response from server: " + responseObject.toString());

// Extract status
                String status = responseObject.get("status").getAsString();
                SweLabLumi.logger.info("Status: " + status);

// Extract the list of ResultsRecord objects
                Gson gson = new Gson();
                JsonArray detailsArray = responseObject.getAsJsonArray("details");

// Deserialize the JSON array into a list of ResultsRecord objects
                List<ResultsRecord> resultsRecords = new ArrayList<>();
                for (JsonElement element : detailsArray) {
                    ResultsRecord record = gson.fromJson(element, ResultsRecord.class);
                    resultsRecords.add(record);
                }

// Log and process the ResultsRecord objects as needed
                for (ResultsRecord record : resultsRecords) {
                    SweLabLumi.logger.info("Sample ID: " + record.getSampleId()
                            + ", Test: " + record.getTestCode()
                            + ", Status: " + record.getStatus());
                }

            } else {
                System.out.println("POST request failed. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
