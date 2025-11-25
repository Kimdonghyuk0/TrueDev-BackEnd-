package com.trueDev.trueDev.redis.repository;

import com.trueDev.trueDev.redis.entity.RedisRefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RedisRefreshTokenRepository extends CrudRepository<RedisRefreshToken, Long> {
}
