package ua.dymohlo.FootballPredictions.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
@Slf4j
public class RulesPageController {

    @GetMapping("/api/rules")
    public String showHostPage() {
        log.info("Rules page accessed.");
        return "pages/rules";
    }
}
