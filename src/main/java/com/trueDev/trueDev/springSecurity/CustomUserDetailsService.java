package com.trueDev.trueDev.springSecurity;

import com.trueDev.trueDev.user.entity.User;
import com.trueDev.trueDev.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(email + " -> 데이터베이스에서 찾을 수 없습니다."));
    }

    // DB에 User 값이 존재한다면 UserDetails 객체로 만들어서 리턴
    private UserDetails createUserDetails(User user) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getAuthority().name());

        //  여기서 만드는 User는 "스프링 시큐리티 User" 여야 한다
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())        // username
                .password(user.getPassword())          // 암호화된 비밀번호(BCrypt 결과)
                .authorities(grantedAuthority)   // 권한 리스트 (스프링은 기본적으로 권한을 여러개 가질 수 있어서 리스트로 처리됨)
                .build();
    }

}