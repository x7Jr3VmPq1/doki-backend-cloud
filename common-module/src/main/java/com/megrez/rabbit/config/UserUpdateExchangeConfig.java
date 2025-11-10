package com.megrez.rabbit.config;

import com.megrez.rabbit.exchange.CommentLikeExchange;
import com.megrez.rabbit.exchange.UserUpdateExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserUpdateExchangeConfig {
    // 定义用户更新交换机 (FANOUT类型)
    @Bean(UserUpdateExchange.FANOUT_EXCHANGE_USER_UPDATE)
    public FanoutExchange userUpdateExchange() {
        return new FanoutExchange(UserUpdateExchange.FANOUT_EXCHANGE_USER_UPDATE);
    }

    // 定义更新搜索引擎队列
    @Bean(UserUpdateExchange.QUEUE_USER_UPDATE_SEARCH)
    public Queue userUpdateSearchQueue() {
        return new Queue(UserUpdateExchange.QUEUE_USER_UPDATE_SEARCH, true);
    }

    // 绑定队列到交换机
    @Bean
    public Binding bindingUserUpdateSearchQueueToExchange(
            @Qualifier(UserUpdateExchange.QUEUE_USER_UPDATE_SEARCH) Queue queue,
            @Qualifier(UserUpdateExchange.FANOUT_EXCHANGE_USER_UPDATE) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }
}
