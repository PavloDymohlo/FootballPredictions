package ua.dymohlo.FootballPredictions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.dymohlo.FootballPredictions.DTO.LoginInDto;
import ua.dymohlo.FootballPredictions.DTO.RegisterDto;
import ua.dymohlo.FootballPredictions.Entity.User;
import ua.dymohlo.FootballPredictions.api.AuthService;
import ua.dymohlo.FootballPredictions.configuration.PasswordEncoderConfig;
import ua.dymohlo.FootballPredictions.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public User register(RegisterDto registerDto) {
        long startCount = 0;
        long userRankingPosition = calculateUserRankingPositionDuringRegistration();
        String userName = registerDto.getUserName();

        checkUserNameInLatin(userName);
        String passwordEncoded = PasswordEncoderConfig.encoderPassword(registerDto.getPassword());

        if (userRepository.findByUserName(userName).isPresent()) {
            log.error("user with userName " + userName + " already exists!");
            throw new IllegalArgumentException("Цей псевдонім вже використовується!");
        }

        User user = User.builder()
                .userName(userName)
                .password(passwordEncoded)
                .rankingPosition(userRankingPosition)
                .trophyCount(startCount)
                .monthlyScore(startCount)
                .totalScore(startCount)
                .predictionCount(startCount)
                .percentGuessedMatches((int) startCount)
                .lastPredictions(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Override
    public String loginIn(LoginInDto userLoginInDto) {
        Optional<User> user = userRepository.findByUserName(userLoginInDto.getUserName());

        if (user.isEmpty()) {
            throw new NoSuchElementException("Невірний логін");
        }

        if (!PasswordEncoderConfig.checkPassword(userLoginInDto.getPassword(), user.get().getPassword())) {
            throw new IllegalArgumentException("Невірний пароль");
        }

        return "Success";
    }

    private void checkUserNameInLatin(String userName) {
        if (userName.chars().anyMatch(ch -> Character.UnicodeBlock.of(ch).equals(Character.UnicodeBlock.CYRILLIC))) {
            throw new IllegalArgumentException("Введіть псевдонім латинськими символами!");
        }
    }

    private long calculateUserRankingPositionDuringRegistration() {
        return userRepository.count() + 1;
    }
}