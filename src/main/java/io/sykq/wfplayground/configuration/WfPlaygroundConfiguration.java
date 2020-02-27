package io.sykq.wfplayground.configuration;

import io.sykq.wfplayground.model.MiscResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.publisher.ReplayProcessor;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class WfPlaygroundConfiguration implements WebFluxConfigurer {

    @Bean
    public ReplayProcessor<MiscResponse> miscResponsesReplayProcessor() {
        return ReplayProcessor.create(200);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        miscResponsesReplayProcessor().subscribe(next -> log.info("{}", next.getId()));
    }
}

