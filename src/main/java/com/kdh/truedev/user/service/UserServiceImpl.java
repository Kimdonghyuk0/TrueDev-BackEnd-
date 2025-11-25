package com.kdh.truedev.user.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.kdh.truedev.springSecurity.TokenProvider;
import com.kdh.truedev.springSecurity.dto.TokenDto;
import com.kdh.truedev.redis.util.RedisUtil;
import com.kdh.truedev.user.dto.response.AccountUpdateRes;
import com.kdh.truedev.user.dto.response.LoginSuccess;
import com.kdh.truedev.user.dto.response.LoginUser;
import com.kdh.truedev.user.entity.User;
import com.kdh.truedev.user.mapper.UserMapper;
import com.kdh.truedev.user.repository.UserRepository;
import com.kdh.truedev.user.dto.request.UserReq;
import com.kdh.truedev.user.dto.request.AccountUpdateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {


    private final UserRepository repo;

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final Cloudinary cloudinary;

    @Transactional
    @Override
    public void signup(UserReq dto,MultipartFile profileImage) {
        String img_url = null;
        // 이메일 중복 체크 → 400
        repo.findByEmail(dto.email()).ifPresent(u -> {
            throw new IllegalArgumentException("email duplicated");
        });
        if(profileImage!=null&&!profileImage.isEmpty()) {
            img_url = uploadImage(profileImage);
        }
        User saved = repo.save(
                User.builder()
                        .email(dto.email())
                        .password(passwordEncoder.encode(dto.password()))
                        .name(dto.name())
                        .profileImage(img_url)
                        .build()
        );
    }

    // 유저 정보 가져오기
    @Transactional(readOnly = true)
    @Override
    public LoginUser get(Long id) {

        User user =  repo.findById(id).orElseThrow(() ->
                new IllegalArgumentException("not found"));
        return new LoginUser(user.getEmail(), user.getName(), user.getProfileImage());
    }

    // 로그인
    @Transactional(readOnly = true)
    @Override
    public LoginSuccess login(String email, String password) {
        User u = repo.findByEmail(email).orElseThrow(UnauthorizedException::new);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication, u.getId());

        redisUtil.save(u.getId(), tokenDto.refreshToken());

        LoginUser loginUser = new LoginUser(u.getEmail(), u.getName(), u.getProfileImage());
        return new LoginSuccess(tokenDto,loginUser);
    }

    @Override
    public void logout(Long userId) {
        redisUtil.delete(userId);
    }

    @Transactional
    @Override
    public boolean deleteAccount(Long userId) {
        User user = repo.findById(userId).orElse(null);
        if(user==null)return false;
        user.softDelete();
        return true;
    }

    @Transactional
    @Override
    public AccountUpdateRes updateAccount(Long userId, AccountUpdateReq req) {
        User user = repo.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        if (req.name() != null && !req.name().isBlank()) {
            user.setName(req.name());
        }
        if (req.profileImage() != null) {
            user.setProfileImage(req.profileImage());
        }
        if (req.email()!=null && !req.email().isBlank()){
            user.setEmail(req.email());
        }
        return UserMapper.toUpdateUser(user);
    }

    @Transactional
    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = repo.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("user_not_found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("currentPassword_unauthorized");
        }
        String newHashed = passwordEncoder.encode(newPassword);
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("password_duplicated"); // "이전 비밀번호와 동일"
        }

        user.setPassword(newHashed);
    }

    @Transactional(readOnly = true)
    @Override
    public TokenDto reissue(String refreshToken) {
        return tokenProvider.reissueAccessToken(refreshToken);
    }

    @Override
    public String uploadImage(MultipartFile image) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    image.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "truedev",       // Cloudinary 안에서 폴더명
                            "resource_type", "image"
                    )
            );

            // 업로드 후 HTTPS 이미지 주소
            return result.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }
}