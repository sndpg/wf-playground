package io.sykq.wfplayground.configuration;

import io.sykq.wfplayground.model.MiscResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.ReplayProcessor;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class WfPlaygroundConfiguration implements WebFluxConfigurer {

    @Bean
    public FluxProcessor<MiscResponse, MiscResponse> miscResponsesFluxProcessor() {
        return ReplayProcessor.cacheLast();
    }

    @EventListener(value = ApplicationReadyEvent.class)
    public void applicationReady() {
        miscResponsesFluxProcessor().subscribe(next -> log.info("{}", next.getId()));

        // count all miscResponse emissions with a score of >= 50 and emit the sum of all responses of this filtered
        // flux
        miscResponsesFluxProcessor().filter(miscResponse -> miscResponse.getScore().compareTo(50) >= 0)
                .map(MiscResponse::getScore)
                .scan(0, (a, b) -> a + 1)
                .doOnNext(next -> log.info("reduced to: {}", next))
                .subscribe();
    }
}

