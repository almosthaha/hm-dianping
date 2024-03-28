package com.hmdp.mapper;

import com.hmdp.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据手机号查询用户信息
     * @param phone
     * @return
     */
    User getUser(String phone);

    /**
     * 注册用户
     * @param user
     */
    void save(User user);
}
