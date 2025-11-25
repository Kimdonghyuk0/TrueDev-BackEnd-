package com.trueDev.trueDev.user.mapper;

import com.trueDev.trueDev.user.dto.response.AccountUpdateRes;
import com.trueDev.trueDev.user.entity.User;


public class UserMapper {
    public static AccountUpdateRes toUpdateUser(User user){
        return new AccountUpdateRes(
                user.getName(),
                user.getEmail(),
                user.getProfileImage()
        );
    }

}
