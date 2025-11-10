package com.megrez.rabbit.config;

import com.megrez.rabbit.exchange.UserAddExchange;
import com.megrez.rabbit.exchange.UserUpdateExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserAddExchangeConfig {
    // 定义新增用户交换机 (FANOUT类型)
    @Bean(UserAddExchange.FANOUT_EXCHANGE_USER_ADD)
    public FanoutExchange userUpdateExchange() {
        return new FanoutExchange(UserAddExchange.FANOUT_EXCHANGE_USER_ADD);
    }

    // 定义更新搜索引擎队列
    @Bean(UserAddExchange.QUEUE_USER_ADD_SEARCH)
    public Queue userUpdateSearchQueue() {
        return new Queue(UserAddExchange.QUEUE_USER_ADD_SEARCH, true);
    }

    // 绑定队列到交换机
    @Bean
    public Binding bindingUserAddSearchQueueToExchange(
            @Qualifier(UserAddExchange.QUEUE_USER_ADD_SEARCH) Queue queue,
            @Qualifier(UserAddExchange.FANOUT_EXCHANGE_USER_ADD) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }
}
