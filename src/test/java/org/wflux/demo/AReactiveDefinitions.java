package org.wflux.demo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AReactiveDefinitions {

    private Mono<String> verifySingleData(final String value) {
        return Mono
                .justOrEmpty(value)
                .log();
    }

    private Flux<String> verifyCollectionOfData(final String... data) {
        final var filtered = Arrays.stream(data).map(Optional::ofNullable).map(Mono::justOrEmpty).toList();
        return Flux.defer(() -> Flux.concat(filtered)).log();
    }

    private Flux<String> getNames(final List<Optional<Person>> people) {
        return Flux.fromIterable(people)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(p -> Mono.justOrEmpty(p.fullName()))
                .log();
    }

    private Flux<String> getCertainDataWithDelay(final String... data) {
        return Flux.just(data)
                // add Delay of 300 milliseconds among the emission of each element:
                .delayElements(Duration.ofMillis(300))
                // only start emitting after 1 second has elapsed, i.e. the 1st 3 elements emitted will always be ignored:
                .skip(Duration.ofSeconds(1))
                .log();
    }

    public static void main(String[] args) throws InterruptedException {
        var app = new AReactiveDefinitions();
        System.out.println("Mono tests ...");
        app.verifySingleData("java").subscribe(System.out::println);
        app.verifySingleData(null).subscribe(System.out::println);

        System.out.println("Flux tests ...");
        app.verifyCollectionOfData("java", null, "go").subscribe(System.out::println);

        System.out.println("Map and FlatMap tests ...");
        app.getNames(List.of(Optional.of(new Person("Cami", 10)),
                Optional.empty(),
                Optional.of(new Person("Male", 22)))).subscribe(System.out::println);

        System.out.println("Delay and Skip tests ...");
        // Total duration of emission is 1.5 seconds but the elements emitted inside 1 second window will be ignored:
        app.getCertainDataWithDelay("cpp", "python", "javascript", "java", "go").subscribe(System.out::println);
        // Requirement!!!: Wait for 2 seconds to allow the emission of the elements according to de delay established above.
        // Otherwise, the elements won't be emitted as the main thread will exit immediately.
        TimeUnit.SECONDS.sleep(2);
    }

    record Person(String fullName, int age) {}
}
