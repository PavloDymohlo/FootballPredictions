package ua.dymohlo.FootballPredictions.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.FootballPredictions.DTO.RegisterDto;
import ua.dymohlo.FootballPredictions.Entity.User;
import ua.dymohlo.FootballPredictions.api.AuthService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/register")
public class RegisterController {
    private final AuthService authService;

    @PostMapping
    public User register(@RequestBody RegisterDto registerDto, HttpServletResponse response) {
        User user = authService.register(registerDto);
        response.setHeader("Location", "/office-page");
        response.setStatus(HttpStatus.FOUND.value());
        return user;
    }
}