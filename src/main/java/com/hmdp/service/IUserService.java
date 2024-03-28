package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    /**
     * 发送用户验证码
     * @param phone
     */
    void send(String phone);

    /**
     * 用户登录
     * @param loginForm
     * @return
     */
    Result login(LoginFormDTO loginForm);
}
