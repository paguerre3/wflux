package org.wflux.demo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

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
    }

    record Person(String fullName, int age) {}

    enum InsertMode { CONCAT, MERGE }
}
