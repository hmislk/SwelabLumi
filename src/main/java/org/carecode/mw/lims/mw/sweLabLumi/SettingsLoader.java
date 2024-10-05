package org.carecode.mw.lims.mw.sweLabLumi;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.carecode.lims.libraries.MiddlewareSettings;

public class SettingsLoader {

    private static final Logger logger = LogManager.getLogger(SettingsLoader.class);
    private static MiddlewareSettings middlewareSettings;

    public static void loadSettings() {
        Gson gson = new Gson();
        try {
            // Read and print the contents of the config.json file
            String filePath = "config.json";
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println("Contents of config.json:");
            System.out.println(jsonContent);

            // Now parse the JSON content
            try (FileReader reader = new FileReader(filePath)) {
                middlewareSettings = gson.fromJson(reader, MiddlewareSettings.class);
                logger.info("Settings loaded from config.json");

                // Debugging output
                System.out.println("MiddlewareSettings loaded:");
                System.out.println(middlewareSettings);

                if (middlewareSettings.getAnalyzerDetails() != null) {
                    System.out.println("Analyzer Name: " + middlewareSettings.getAnalyzerDetails().getAnalyzerName());
                } else {
                    System.out.println("AnalyzerDetails is null");
                }

                if (middlewareSettings.getLimsSettings() != null) {
                    System.out.println("LIMS Server Base URL: " + middlewareSettings.getLimsSettings().getLimsServerBaseUrl());
                } else {
                    System.out.println("LimsSettings is null");
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load settings from config.json", e);
            System.out.println("Failed to load settings: " + e.getMessage());
        }
    }

    public static MiddlewareSettings getSettings() {
        if (middlewareSettings == null) {
            loadSettings();
        }
        return middlewareSettings;
    }
}
