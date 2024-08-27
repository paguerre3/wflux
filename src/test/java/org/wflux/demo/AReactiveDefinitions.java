package org.wflux.demo;

import reactor.core.publisher.Mono;

public class AReactiveDefinitions {

    private Mono<String> verifySingleValue(final String value) {
        return Mono.
                justOrEmpty(value).
                log();
    }

    public static void main(String[] args) {
        var app = new AReactiveDefinitions();
        app.verifySingleValue("java").subscribe(System.out::println);
        app.verifySingleValue(null).subscribe(System.out::println);
    }
}
