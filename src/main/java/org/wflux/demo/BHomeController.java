package org.wflux.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class BHomeController {
    @GetMapping("/")
    public Mono<String> handleHomePage() {
        // home.html must exist under templates folder located under resources:
        return Mono.just("home");
    }
}
