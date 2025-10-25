package com.megrez.client;

import com.megrez.entity.User;
import com.megrez.result.Result;
import com.megrez.vo.user_service.UsersVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户服务客户端
 * <p>
 * 提供用户相关的远程调用接口
 *
 * @author Doki Team
 * @since 1.0.0
 */
@FeignClient(name = "user-service", path = "/user")
public interface UserServiceClient {

    /**
     * 根据用户ID列表批量获取用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息结果(不包含关注状态)
     */
    @PostMapping("/userinfo")
    Result<List<User>> getUserinfoById(@RequestBody List<Integer> userIds);

    /**
     * 根据用户ID列表批量获取用户信息
     *
     * @param targetIds 查询目标用户ID列表
     * @param userId    请求者ID
     * @return 用户信息结果(包含关注状态)
     */
    @PostMapping("/userinfo")
    Result<List<UsersVO>> getUserinfoByIdWithIfFollowed(
            @RequestParam("userId") Integer userId,
            @RequestBody List<Integer> targetIds);
}
