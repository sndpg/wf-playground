package io.sykq.wfplayground.controller;

import io.sykq.wfplayground.model.MiscResponse;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

@Slf4j
@Controller
public class DataController {

    private final FluxProcessor<MiscResponse, MiscResponse> miscResponsesFluxProcessor;

    private final Flux<ScoreCounter> sumFlux;
    private final Flux<ScoreCounter> scoreStatus;
    private final ScoreCounter scoreCounter = new ScoreCounter();

    public DataController(FluxProcessor<MiscResponse, MiscResponse> miscResponsesFluxProcessor) {
        this.miscResponsesFluxProcessor = miscResponsesFluxProcessor;
        sumFlux = miscResponsesFluxProcessor
                .map(miscResponse -> {
                    if (miscResponse.getScore().compareTo(50) >= 0) {
                        scoreCounter.incrementHighScoreCounter();
                    } else {
                        scoreCounter.incrementLowScoreCounter();
                    }
                    return scoreCounter;
                })
                .doOnEach(s -> log.info("highScores: {}, lowScores: {}",
                        Objects.requireNonNull(s.get()).getHighScores(),
                        Objects.requireNonNull(s.get()).getLowScores()));

        scoreStatus =
                miscResponsesFluxProcessor
                        .publish(1)
                        .autoConnect()
                        .map(MiscResponse::getScore)
                        .log("scores", Level.INFO)
                        .scanWith(ScoreCounter::new, (accumulator, score) -> {
                            if (score.compareTo(50) < 0) {
                                accumulator.incrementLowScoreCounter();
                            } else {
                                accumulator.incrementHighScoreCounter();
                            }
                            return accumulator;
                        })
//                        .scan(new ScoreCounter(), (accumulator, score) -> {
//                            if (score.compareTo(50) < 0) {
//                                accumulator.incrementLowScoreCounter();
//                            } else {
//                                accumulator.incrementHighScoreCounter();
//                            }
//                            return accumulator;
//                        })
                        .doOnNext(next -> log.info("reduced to -> high: {}, low: {}", next.getHighScores(),
                                next.getLowScores()));
    }

    @GetMapping("/data")
    public String greeting(@RequestParam(name = "text", defaultValue = "abc") String text, Model model) {
        model.addAttribute("text", text);
        return "index";
    }

    @GetMapping("/stream")
    public String streamedFlux(Model model) {
        model.addAttribute("scoreCounter",
                new ReactiveDataDriverContextVariable(sumFlux, 1));

        //        model.addAttribute("lowScoreCount",
        //                new ReactiveDataDriverContextVariable(miscResponsesReplayProcessor, 1));
        //        model.addAttribute("data",
        //                new ReactiveDataDriverContextVariable(miscResponsesReplayProcessor, 1));
        //        model.addAttribute("test", "meh");
        return "misc-log";
    }

    @GetMapping(value = "/simple-status")
    public String simpleStatus(Model model) {
        model.addAttribute("scoreCounters", scoreStatus);
        return "simple-status";
    }

    @GetMapping(value = "/status")
    public String status(Model model) {
        model.addAttribute("scoreCounters", new ReactiveDataDriverContextVariable(scoreStatus, 1));
        return "status";
    }

    @GetMapping(value = "/status-counter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public String statusCounter(Model model) {
        model.addAttribute("scoreCounters", new ReactiveDataDriverContextVariable(scoreStatus, 1));
        return "status :: #score";
    }

    @EqualsAndHashCode
    @NoArgsConstructor
    public static class ScoreCounter {
        private AtomicInteger lowScoreCounter = new AtomicInteger(0);
        private AtomicInteger highScoreCounter = new AtomicInteger(0);

        public void incrementLowScoreCounter() {
            lowScoreCounter.incrementAndGet();
        }

        public void incrementHighScoreCounter() {
            highScoreCounter.incrementAndGet();
        }

        public int getLowScores() {
            return lowScoreCounter.get();
        }

        public int getHighScores() {
            return highScoreCounter.get();
        }
    }
}
