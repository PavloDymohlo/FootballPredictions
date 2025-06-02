package ua.dymohlo.FootballPredictions.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class NodeScriptService {

    public String runNodeScript(String action) {
        StringBuilder result = new StringBuilder();

        try {
            Path resourcesPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "scraper");
            String scriptPath = resourcesPath.resolve("scraper.js").toString();

            if (!Files.exists(resourcesPath)) {
                throw new RuntimeException("Scraper directory not found: " + resourcesPath);
            }

            ProcessBuilder processBuilder = new ProcessBuilder("node", scriptPath, action);
            processBuilder.directory(resourcesPath.toFile());

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("The script finished with an error, exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while running the Node.js script", e);
        }

        return result.toString();
    }
}