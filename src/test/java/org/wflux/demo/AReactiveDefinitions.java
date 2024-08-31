package org.wflux.demo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.*;
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

    private Flux<Integer> skipWhileAndUntil(final int whileStart, final int untilStart,
                                            final int whileVal, final int untilVal) {
        var f1 = Flux.range(whileStart, 10*whileStart).skipWhile(n -> n < whileVal);
        // Note last argument of range is "count" and NOT "end position",
        // i.e. Starts at "100" having a Count of "1000" elements:
        // Result=100, 101, 102, ..., 1097, 1098, 1099 -1099 is the 1000th element in the sequence.
        var f2 = Flux.range(untilStart, 10*untilStart).skipUntil(n -> n >= untilVal);
        return Flux.concat(f1, f2);
    }

    private Flux<Integer> unionAll(final int start0, final int start1, final int count,
                                  final Duration delay, final InsertMode mode) {
        var f1 = Flux.range(start0, count).delayElements(delay);
        var f2 = Flux.range(start1, count).delayElements(delay.plusMillis(50));
        // enhanced "switch"
        return switch (mode) {
            case CONCAT -> Flux.concat(f1, f2);
            case MERGE -> Flux.merge(f1, f2);
        };
    }

    private Flux<Tuple2<Integer, Integer>> combine(final int start0, final int start1, final int count) {
        var f1 = Flux.range(start0, count);
        var f2 = Flux.range(start1, count);
        // default behaviour
        return Flux.zip(f1, f2);
    }

    private Flux<String> combineToSingle(final Integer[] data0, final String... data1) {
        var f1 = Flux.fromArray(data0);
        var f2 = Flux.just(data1);
        // using a combination function:
        return Flux.zip(f1, f2, (n, s) -> (n + "-" + s).toUpperCase());
    }

    private Mono<List<Integer>> collectAsSingle(final Integer[] data, final Duration delayElements) {
        // from Flux to Mono
        return Flux
                .fromArray(data)
                .delayElements(delayElements)
                // returns an Observable none blocking operation as a Mono "single" Stream
                .collectList()
                .log();
    }

    private Flux<List<Long>> buffer(final long take, final Duration interval, final Duration batch) {
        return Flux.interval(interval)
                .take(take)
                .buffer(batch);
    }

    private Mono<Map<String, String>> collectAsSingleByKey(final List<Person> people) {
        return Flux.fromIterable(people)
                .collectMap(p -> p.id, Person::fullName);
    }

    private Flux<String> logSignals(final String... data) {
        return Flux.fromArray(data).doOnEach(s -> {
            System.out.println("Signal: " + s);
            if (s.isOnNext()) {
                System.out.println("Received value: " + s.get());
            } else if (s.isOnError()) {
                System.err.println("Error: " + s.getThrowable());
            } else if (s.isOnComplete()) {
                System.out.println("Stream completed");
            }
        });
    }

    private Flux<String> getNamesWithFallbacks(final List<Person> people) {
        return Flux.fromIterable(people)
                .map(Person::fullName)
                .onErrorResume(e -> {
                    System.out.println("Error occurred, switching to Fallback stream, detail: " + e.getMessage());
                    return Flux.just("Mili", "Sol");
                });
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
        // Total duration of emission is 1.5 seconds but the elements emitted inside 1-second window will be ignored:
        app.getCertainDataWithDelay("cpp", "python", "javascript", "java", "go").subscribe(System.out::println);
        // Requirement!!!: Wait for 2 seconds to allow the emission of the elements according to de delay established above.
        // Otherwise, the elements won't be emitted as the main thread will exit immediately.
        TimeUnit.SECONDS.sleep(2);

        System.out.println("SkipWhile and SkipUntil tests ...");
        app.skipWhileAndUntil(1, 100, 7, 1097).subscribe(System.out::println);
        System.out.println("...");
        app.skipWhileAndUntil(1, 1, 7, 7).subscribe(System.out::println);

        System.out.println("Concat and Merge tests ...");
        app.unionAll(1, 101, 5, Duration.ofMillis(100), InsertMode.CONCAT).subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(2);
        System.out.println("...");
        app.unionAll(1, 101, 5, Duration.ofMillis(100), InsertMode.MERGE).subscribe(System.out::println);
        // Requirement!!!: Wait for 2 seconds to allow the emission of the elements according to de delay established above.
        // Otherwise, the elements won't be emitted as the main thread will exit immediately.
        TimeUnit.SECONDS.sleep(2);

        System.out.println("Zip tests ...");
        app.combine(1, 101, 5).subscribe(System.out::println);
        System.out.println("...");
        // it will only combine 1st 3 elements because when the shortest stream completes, the rest will be ignored:
        app.combineToSingle(new Integer[]{1, 2, 3, 4, 5}, "a", "b", "c").subscribe(System.out::println);

        System.out.println("Collect List and Block tests ...");
        // instead of subscribing to the Mono<List> none blocking operation
        // "block" operation is performed to convert to a single java.util.List "blocking current thread",
        // which will wait until the Mono<List> is completed so there is no need to set a Time-Out above 1 second:
        List<Integer> legacyList = app.collectAsSingle(new Integer[]{1, 2, 3, 4}, Duration.ofMillis(250)).block();
        System.out.println(legacyList);

        System.out.println("Buffer tests ...");
        // emit elements every 100 millisecond and buffer every 210 milliseconds, i.e. creating 3 batches:
        app.buffer(5, Duration.ofMillis(100), Duration.ofMillis(210)).subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(1);

        System.out.println("Collect Map tests ...");
        app.collectAsSingleByKey(List.of(new Person("Cami", 10),
                        new Person("Ema", 10),
                        new Person("Male", 22)))
                .subscribe(System.out::println);

        System.out.println("Do On tests ...");
        // no need to do .subscribe(System.out::println) as logging and metrics are performed inside "doOnEach"
        app.logSignals("java", "go", "cpp").subscribe();

        System.out.println("On Error tests ...");
        app.getNamesWithFallbacks(List.of(new Person("Cami", 10),
                // null value mapping error will occur in this position so the fallback stream process will be triggered
                // as WebFlux don't allow emitting null values:
                new Person(null, 0),
                new Person("Male", 22))).subscribe(System.out::println);
    }

    record Person(String id, String fullName, int age) {
        Person(final String fullName, final int age) {
            this(UUID.randomUUID().toString(), fullName, age);
        }
    }

    enum InsertMode { CONCAT, MERGE }
}
