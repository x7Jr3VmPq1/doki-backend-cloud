package com.megrez.controller;


import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionModel;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/search")
public class XiaoMeng {
    private static final Logger log = LoggerFactory.getLogger(XiaoMeng.class);
    private final DeepSeekClient deepSeekClient;

    public XiaoMeng(DeepSeekClient deepSeekClient) {
        this.deepSeekClient = deepSeekClient;
    }


    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatCompletionResponse> chat(String prompt) {

        log.info("用户：{}", prompt);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(ChatCompletionModel.DEEPSEEK_CHAT)
                // 添加用户消息
                .addUserMessage(prompt)
                .addSystemMessage(XiaoMengPrompt.prompt)
                .build();

        // 定义一个 StringBuilder 来收集所有响应
        StringBuilder collector = new StringBuilder();

        List<Boolean> shouldStop = new java.util.ArrayList<>(List.of());

        return deepSeekClient.chatFluxCompletion(request)
                .doOnNext(resp -> {
                    log.info("{}", resp.id());
                    String content = resp.choices().get(0).delta().content();
                    collector.append(content);
                })
                .filter(resp -> {
                    if (resp.choices().get(0).delta().content().equals("[") || !shouldStop.isEmpty()) {
                        shouldStop.add(true);
                        return false;
                    }
                    return true;
                })
                .doOnError(err -> log.error("API调用错误: ", err))
                .doOnComplete(() -> {
                    // 在完成时分析结果
                    Pattern pattern = Pattern.compile("\\[[^]]+]");
                    Matcher matcher = pattern.matcher(collector.toString());
                    if (matcher.find()) {
                        String matched = matcher.group();
                        log.info("收集完成，提取结果: {}", matched);
                    }
                    log.info("AI回答：{}", collector);
                });
    }
}
