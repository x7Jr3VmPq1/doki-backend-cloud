package com.megrez.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    public SmsService(RedisUtils redisUtils, ObjectMapper objectMapper) {
        this.redisUtils = redisUtils;
        this.objectMapper = objectMapper;
    }

    RedisUtils redisUtils;
    ObjectMapper objectMapper;

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return 成功提示
     */
    public Result sendCode(String phone) {
        // 1. 判断是否60秒发送过，如果是，返回错误。
        if (!redisUtils.setLock(phone + ":lock", "1", 60L, TimeUnit.SECONDS)) {
            return Result.error(Response.TOO_MANY_REQUEST);
        }
        // 2. 生成六位数字随机验证码
        String newCode = String.format("%06d", new Random().nextInt(1000000));
        // 3. 删除旧验证码
        redisUtils.del(phone);
        // 4. 写入Redis并设置300秒失效时间
        String data;
        try {
            data = objectMapper.writeValueAsString(new CodeData(newCode, 0));
            redisUtils.set(phone, data, 300L, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            return Result.error(Response.FAILED);
        }
        // 5. 加锁限流
        redisUtils.set(phone + ":lock", "1", 60L, TimeUnit.SECONDS);
        // 6. 返回成功提示
        log.info("Phone:{} has send code:{}", phone, newCode);

        return Result.success(null);
    }

    /**
     * 校验验证码是否正确
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 成功或失败
     */
    public boolean verifyCode(String phone, String code) {
        try {
            // 1. 从Redis中根据手机号查询验证码
            String data = redisUtils.get(phone);
            // 没有查询到验证码，返回错误
            if (data == null) {
                return false;
            }
            CodeData codeData = null;

            codeData = objectMapper.readValue(data, CodeData.class);

            // 2. 比对是否正确
            if (codeData.getCode().equals(code)) {
                // 3. 如果成功，立刻使验证码失效，并返回成功
                redisUtils.del(phone);
                return true;
            } else {
                // 失败次数+1
                codeData.setFailCount(codeData.getFailCount() + 1);

                // 如果失败次数达到3次，删除验证码
                if (codeData.getFailCount() >= 3) {
                    redisUtils.del(phone);
                } else {
                    // 否则，更新Redis中的数据
                    redisUtils.set(phone, objectMapper.writeValueAsString(codeData));
                }
                return false;
            }
        } catch (Exception e) {
            log.error("JSON序列化异常");
            return false;
        }
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
// 数据传输DTO
class CodeData {

    private String code; // 验证码

    private int failCount; // 失败次数

}
