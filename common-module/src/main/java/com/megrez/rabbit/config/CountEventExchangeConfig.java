package com.megrez.rabbit.config;

import com.megrez.rabbit.exchange.CountEventExchange;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CountEventExchangeConfig {

    @Bean
    public DirectExchange countEventExchange() {
        return new DirectExchange(CountEventExchange.DIRECT_EXCHANGE_COUNT, true, false);
    }

    @Bean
    public Queue likeQueue() {
        return QueueBuilder.durable(CountEventExchange.LIKE).build();
    }

    @Bean
    public Queue likedQueue() {
        return QueueBuilder.durable(CountEventExchange.LIKED).build();
    }

    @Bean
    public Queue favoriteQueue() {
        return QueueBuilder.durable(CountEventExchange.FAVORITE).build();
    }

    @Bean
    public Queue workQueue() {
        return QueueBuilder.durable(CountEventExchange.WORK).build();
    }

    @Bean
    public Queue historyQueue() {
        return QueueBuilder.durable(CountEventExchange.HISTORY).build();
    }

    @Bean
    public Queue followQueue() {
        return QueueBuilder.durable(CountEventExchange.FOLLOW).build();
    }

    @Bean
    public Queue followedQueue() {
        return QueueBuilder.durable(CountEventExchange.FOLLOWED).build();
    }


    @Bean
    public Binding likeBinding() {
        return BindingBuilder.bind(likeQueue())
                .to(countEventExchange())
                .with(CountEventExchange.RK_LIKE);
    }

    @Bean
    public Binding likedBinding() {
        return BindingBuilder.bind(likedQueue())
                .to(countEventExchange())
                .with(CountEventExchange.RK_LIKED);
    }

    @Bean
    public Binding favoriteBinding() {
        return BindingBuilder.bind(favoriteQueue())
                .to(countEventExchange())
                .with(CountEventExchange.RK_FAVORITE);
    }

    @Bean
    public Binding workBinding() {
        return BindingBuilder.bind(workQueue())
                .to(countEventExchange())
                .with(CountEventExchange.RK_WORK);
    }

    @Bean
    public Binding historyBinding() {
        return BindingBuilder.bind(historyQueue())
                .to(countEventExchange())
                .with(CountEventExchange.RK_HISTORY);
    }

    @Bean
    public Binding followBinding() {
        return BindingBuilder.bind(followQueue())
                .to(countEventExchange())
                .with(CountEventExchange.RK_FOLLOW);
    }

    @Bean
    public Binding followedBinding() {
        return BindingBuilder.bind(followedQueue())
                .to(countEventExchange())
                .with(CountEventExchange.RK_FOLLOWED);
    }
}
