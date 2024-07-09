package com.sixeco.nettydemo.service.impl;

import com.sixeco.nettydemo.dto.Student;
import com.sixeco.nettydemo.service.UserService;
import com.sixeco.nettydemo.utils.RedisOperationUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserServiceImpl implements UserService {


    @Override
    public void bindUserGroup(long userId, long groupId) {
        RedisOperationUtil.addSetValue(String.valueOf(groupId), String.valueOf(userId));
        Set<String> setValue = RedisOperationUtil.getSetValue(String.valueOf(groupId));
        for (String s : setValue) {
            System.out.println(s);
        }
    }



}
