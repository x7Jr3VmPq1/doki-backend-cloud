package com.megrez.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.megrez.exception.CodeNotConsumedException;
import com.megrez.exception.InvalidCodeException;
import com.megrez.service.SmsService;
import com.megrez.utils.RedisUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
  短信验证码服务
 */
@Service
public class SmsServiceImpl implements SmsService {
    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    public SmsServiceImpl(RedisUtil redisUtil, ObjectMapper objectMapper) {
        this.redisUtil = redisUtil;
        this.objectMapper = objectMapper;
    }

    /**
     * 发送验证码
     *
     * @param phone
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public String sendCode(String phone) throws JsonProcessingException {
        // 1. 判断是否60秒发送过，如果是，失败则抛出异常
        String code = redisUtil.get(phone + ":lock");
        if (code != null) {
            throw new CodeNotConsumedException();
        }
        // 2. 生成六位数字随机验证码
        String newCode = String.format("%06d", new Random().nextInt(1000000));
        // 3. 删除旧验证码
        redisUtil.del(phone);
        // 4. 写入Redis并设置300秒失效时间
        String data = objectMapper.writeValueAsString(new CodeData(newCode, 0));
        redisUtil.set(phone, data, 300L, TimeUnit.SECONDS);
        // 5. 加锁限流
        redisUtil.set(phone + ":lock", "1", 60L, TimeUnit.SECONDS);
        // 6. 返回成功提示
        log.info("{} has received code: {}", phone, newCode);
        return "发送验证码成功";
    }

    /**
     * 核对验证码
     *
     * @param phone
     * @param code
     * @return
     * @throws JsonProcessingException
     */
    @Override
    public boolean verifyCode(String phone, String code) throws JsonProcessingException {
        // 1. 从Redis中根据手机号查询验证码
        String data = redisUtil.get(phone);
        // 没有查询到验证码，返回错误
        if (data == null) {
            throw new InvalidCodeException();
        }
        CodeData codeData = objectMapper.readValue(data, CodeData.class);
        // 2. 比对是否正确
        if (codeData.getCode().equals(code)) {
            // 3. 如果成功，立刻使验证码失效，并返回成功
            redisUtil.del(phone);
            return true;
        } else {
            // 失败次数+1
            codeData.setFailCount(codeData.getFailCount() + 1);

            // 如果失败次数达到3次，删除验证码
            if (codeData.getFailCount() >= 3) {
                redisUtil.del(phone);
            } else {
                // 否则，更新Redis中的数据
                redisUtil.set(phone, objectMapper.writeValueAsString(codeData));
            }
            throw new InvalidCodeException();
        }
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CodeData {

    private String code; // 验证码

    private int failCount; // 失败次数

}
