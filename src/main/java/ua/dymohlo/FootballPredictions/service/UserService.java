package ua.dymohlo.FootballPredictions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import ua.dymohlo.FootballPredictions.DTO.RegisterDto;
import ua.dymohlo.FootballPredictions.DTO.LoginInDto;
import ua.dymohlo.FootballPredictions.Entity.User;
import ua.dymohlo.FootballPredictions.configuration.CacheConfig;
import ua.dymohlo.FootballPredictions.configuration.PasswordEncoderConfig;
import ua.dymohlo.FootballPredictions.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    public User register(RegisterDto registerDto) {
        long startCount = 0;
        long userRankingPosition = calculateUserRankingPosition();
        String passwordEncoded = PasswordEncoderConfig.encoderPassword(registerDto.getPassword());
        if (userRepository.findByUserName(registerDto.getUserName()).isPresent()) {
            throw new IllegalArgumentException("This name is already taken");
        }
        User user = User.builder()
                .userName(registerDto.getUserName())
                .password(passwordEncoded)
                .rankingPosition(userRankingPosition)
                .trophyCount(startCount)
                .monthlyScore(startCount)
                .totalScore(startCount)
                .build();
        return userRepository.save(user);
    }

    private long calculateUserRankingPosition() {
        return userRepository.count() + 1;
    }

    public String loginIn(LoginInDto userLoginInDto) {
        Optional<User> user = userRepository.findByUserName(userLoginInDto.getUserName());
        if(user.isEmpty()){
            throw new NoSuchElementException("Invalid login");
        }
        if(!PasswordEncoderConfig.checkPassword(userLoginInDto.getPassword(), user.get().getPassword())){
            throw new IllegalArgumentException("Invalid password");
        }
        return "Success";
    }
}
