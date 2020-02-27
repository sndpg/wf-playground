package io.sykq.wfplayground.controller;

import io.sykq.wfplayground.model.MiscResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.ReplayProcessor;

@RequiredArgsConstructor
@Controller
public class DataController {

    private final ReplayProcessor<MiscResponse> miscResponsesReplayProcessor;

    @GetMapping("/data")
    public String greeting(@RequestParam(name = "text", defaultValue = "abc") String text, Model model) {
        model.addAttribute("text", text);
        return "index";
    }

    @GetMapping("/stream")
    public String doSomething(Model model) {

        model.addAttribute("data",
                new ReactiveDataDriverContextVariable(miscResponsesReplayProcessor, 1));
        model.addAttribute("test", "meh");
        return "misc-log";
    }
}
