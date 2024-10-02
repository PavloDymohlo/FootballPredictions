package ua.dymohlo.FootballPredictions.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HostController {
    @GetMapping("/api/host-page")
    public String showHostPage() {
        log.info("Host page accessed.");
        return "pages/host_page";
    }
}