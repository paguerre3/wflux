package org.wflux.demo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AReactiveDefinitions {

    private Mono<String> verifySingleData(final String value) {
        return Mono.
                justOrEmpty(value).
                log();
    }

    private Flux<String> verifyCollectionOfData(final String... data) {
        return Flux.just(data);
    }

    public static void main(String[] args) {
        var app = new AReactiveDefinitions();
        System.out.println("Mono tests ...");
        app.verifySingleData("java").subscribe(System.out::println);
        app.verifySingleData(null).subscribe(System.out::println);

        System.out.println("Flux tests ...");

    }
}
