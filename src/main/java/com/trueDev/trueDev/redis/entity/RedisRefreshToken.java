package com.trueDev.trueDev.redis.entity;

import org.springframework.data.annotation.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@RedisHash(value = "MemberToken", timeToLive = 3600 * 24)
public class RedisRefreshToken {
    @Id
    private Long userId;
    private String refreshToken;
}
