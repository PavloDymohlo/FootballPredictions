package ua.dymohlo.FootballPredictions.api;

import ua.dymohlo.FootballPredictions.DTO.LoginInDto;
import ua.dymohlo.FootballPredictions.DTO.RegisterDto;
import ua.dymohlo.FootballPredictions.Entity.User;

public interface AuthService {
    User register(RegisterDto registerDto);
    String loginIn(LoginInDto loginInDto);
}