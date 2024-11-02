package ua.dymohlo.FootballPredictions.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class NodeScriptService {

    public String runNodeScript(String action) {
        StringBuilder result = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder("node",
                "C:\\Users\\DELL\\flashscore-scraper\\scraper.js",
                action);

        try {
            processBuilder.directory(new File("C:\\Users\\DELL\\flashscore-scraper"));
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Скрипт завершився з помилкою, код виходу: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Помилка при виконанні Node.js-скрипта", e);
        }
        return result.toString();
    }
}