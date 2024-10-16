package ua.dymohlo.FootballPredictions.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.FootballPredictions.DTO.LoginInDto;
import ua.dymohlo.FootballPredictions.service.UserService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/login")
public class LoginInController {
    private final UserService userService;

    @PostMapping
    public String loginIn(@RequestBody LoginInDto loginInDto, HttpServletResponse response) {
        userService.loginIn(loginInDto);
        response.setHeader("Location", "/office-page");
        response.setStatus(HttpStatus.FOUND.value());
        return "Success";
    }
}