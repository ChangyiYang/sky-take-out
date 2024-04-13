package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.utils.HttpClientUtil;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Service
public class UserServiceImpl implements com.sky.service.UserService {


    public static final String wxLoginURL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    public User login(UserLoginDTO userLoginDTO) {
        String openID = getOpenID(userLoginDTO);


        if (openID == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.getByOpenID(openID);

        if (user == null) {
            user = User.builder()
                    .openid(openID)
                    .createTime(LocalDateTime.now())
                    .build();

            userMapper.insert(user);
        }

        return user;
    }

    private String getOpenID(UserLoginDTO userLoginDTO) {
        Map<String, String> map = new HashMap<>();

        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");
        String res = HttpClientUtil.doGet(wxLoginURL, map);

        JSONObject jsonObject = JSON.parseObject(res);

        String openID = jsonObject.getString("openid");
        return openID;
    }
}





























