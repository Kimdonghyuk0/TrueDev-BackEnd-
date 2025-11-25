package com.trueDev.trueDev.springSecurity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class JwtUserDetails extends User {

    private final Long userId;

    public JwtUserDetails(Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
        super(username, "", authorities);
        this.userId = userId;
    }

}
