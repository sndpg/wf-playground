package io.sykq.wfplayground.controller;

import io.sykq.wfplayground.model.MiscResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.FluxProcessor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@RestController
@RequestMapping("api")
public class MiscRestController {

    private final FluxProcessor<MiscResponse, MiscResponse> miscResponsesFluxProcessor;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "misc")
    public MiscResponse getMisc(@RequestParam MultiValueMap<String, String> requestParams) {
        Map<String, Object> responseValues = new ConcurrentHashMap<>(requestParams.size() + 1);

        responseValues.put("timestamp", LocalDateTime.now());
        responseValues.putAll(requestParams);

        MiscResponse miscResponse = new MiscResponse().withId(UUID.randomUUID().toString())
                .withScore(Integer.parseInt(requestParams.get("score").get(0)))
                .withValues(responseValues);

        miscResponsesFluxProcessor.onNext(miscResponse);

        return miscResponse;
    }

}
