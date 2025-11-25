package com.trueDev.trueDev.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@RequiredArgsConstructor
@Configuration
@EnableRedisRepositories(basePackages = "com.week4.lucas.redis.repository")
public class RedisConfig {

    private final RedisProperties redisProperties; //redis 설정 데이터를 가지고 있는 객체

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(); //단일 Redis 서버(standalone) 접속용 설정 객체
        config.setHostName(redisProperties.getHost()); //application의 spring.data.redis.host 값을 읽어서 설정
        config.setPort(redisProperties.getPort()); //spring.data.redis.port 값 설정
        return new LettuceConnectionFactory(config); // 실제로 Redis와 연결을 만들어주는 커넥션 팩토리 구현체 생성
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>(); // Redis에 데이터 넣고 빼는 걸 도와주는 헬퍼 클래스
        redisTemplate.setConnectionFactory(redisConnectionFactory()); // RedisConnectionFactory를 이 템플릿
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // Redis에 저장할 때 키를 어떤 방식으로 바이트로 바꿀지(직렬화) 결정, StringRedisSerializer는 문자열을 UTF-8 바이트 배열로 직렬화해 줌
        redisTemplate.setValueSerializer(new StringRedisSerializer()); //값도 마찬가지로 문자열로 저장/조회하게 설정
        return redisTemplate; //설정 완료된 템플릿을 빈으로 등록
    }
}
