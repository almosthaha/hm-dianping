package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexPatterns;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;
    /**
     * 发送用户验证码
     * @param phone
     */
    @Override
    public void send(String phone) {
        //这个验证方式要注意一下
        if (RegexPatterns.PHONE_REGEX.matches(phone)){
            log.info("手机号格式错误,请重新输入");
            return;
        }
        Random random = new Random();
        String str="";
        for (int i = 0; i < 6; i++) {
            int num = random.nextInt(9);
            str+=num;
        }
        stringRedisTemplate.opsForValue().set(phone,str);
        log.info("手机验证码为:"+str);
    }

    /**
     * 用户登录
     * @param loginForm
     * @return
     */
    @Override
    public Result login(LoginFormDTO loginForm) {
        if (RegexPatterns.PHONE_REGEX.matches(loginForm.getPhone())) {
            log.info("手机号格式错误");
            return Result.ok("手机号格式错误");
        }
        String code = stringRedisTemplate.opsForValue().get(loginForm.getPhone());
        if (!code.equals(code)){
            log.info("验证码输入错误");
            return Result.ok("验证码输入错误");
        }
        User user= userMapper.getUser(loginForm.getPhone());
        if (user==null){
            //注册用户:
            //这里这种写法可以简化代码?不用在注册逻辑中再写以此缓存?
            user = save(loginForm);
        }

        //将用户信息保存到redis,注意,这里要保存哪些信息,以及为什么要使用hashMap
        String token = String.valueOf(UUID.randomUUID(true));
        // 7.2.将User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        System.out.println(userDTO);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 7.3.存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        return Result.ok(token);
    }

    /**
     * 注册用户
     * @param loginForm
     */
    public User save(LoginFormDTO loginForm){
        User user = new User();
        user.setPhone(loginForm.getPhone());
        user.setIcon(SystemConstants.USER_NICK_NAME_PREFIX+loginForm.getPhone());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.save(user);
        return user;
    }
}
