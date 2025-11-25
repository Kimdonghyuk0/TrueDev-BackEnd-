package com.trueDev.trueDev.redis.util;

import com.trueDev.trueDev.redis.entity.RedisRefreshToken;
import com.trueDev.trueDev.redis.repository.RedisRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisRefreshTokenRepository redisRefreshTokenRepository;

    public void save(Long userId, String refreshToken) {
        redisRefreshTokenRepository.save(new RedisRefreshToken(userId, refreshToken));
    }

    public Optional<String> find(Long userId) {
        return redisRefreshTokenRepository.findById(userId).map(RedisRefreshToken::getRefreshToken);
    }

    public void delete(Long userId) {
        redisRefreshTokenRepository.deleteById(userId);
    }
}
