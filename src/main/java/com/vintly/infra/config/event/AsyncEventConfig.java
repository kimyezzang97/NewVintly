package com.vintly.infra.config.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class AsyncEventConfig {

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 기본 설정값 (적절히 조정 가능)
        executor.setCorePoolSize(4);       // 기본 쓰레드 수
        executor.setMaxPoolSize(10);       // 최대 쓰레드 수
        executor.setQueueCapacity(100);    // 큐 용량
        executor.setThreadNamePrefix("async-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 큐가 꽉 차면 현재 호출한 쓰레드(main 등)가 직접 실행
        executor.initialize();
        return executor;
    }
}
