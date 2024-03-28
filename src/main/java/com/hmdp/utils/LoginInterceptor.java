package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author almost
 * @date 2024/3/27 14:46
 */

public class LoginInterceptor implements HandlerInterceptor {


    private StringRedisTemplate stringRedisTemplate;
    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //通过redis获取用户的信息.对信息进行判断,设置token,并进行封装
        request.getHeader("");
        //从前端浏览器中取出token的请求头
        //判断token是否为空
        //为空的话设置请求状态码为禁用
        String token = request.getHeader("authorization");
        //2. 如果token是空表示未登录需要拦截
        if (StrUtil.isBlank(token)) {
            response.setStatus(401);
            return false;
        }
        //3. 基于token作为key获取Redis的Hash结构中保存的用户数据,返回的是一个Map集合
        String key = RedisConstants.LOGIN_USER_KEY + token; //登录逻辑中token是怎么保存的,这个就怎么组数据去获取
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        //4. 判断Map集合中有没有元素，没有则拦截
        if (userMap.isEmpty()) {
            response.setStatus(401);
            return false;
        }
        //5. 将查询到的Hash结构的数据转化为UserDto对象,fasle表示不忽略转化时的错误
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //6. 将用户信息保存到UserHolder类的ThreadLocal中
        UserHolder.saveUser(userDTO);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
