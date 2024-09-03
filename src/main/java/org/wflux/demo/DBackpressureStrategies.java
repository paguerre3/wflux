package org.wflux.demo;

import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class DBackpressureStrategies {

    /**
     * There is "No Overflow" here as the data comes only after a request is pérformed, emitting is in "sequence".
     * e.g. request(1) and then onNext(1) ...
     *
     * @return Flux<Long>
     */
    private Flux<Long> createNoOverflowFlux() {
        // create a range with a "large" count:
        // Build a Flux that will only emit a "sequence" of count incrementing integers, starting from start.
        // That is, emit integers between start (included) and start + count (excluded) then complete.
        return Flux.range(1, Integer.MAX_VALUE)
                .log()
                // simulate that processing takes some milliseconds:
                .concatMap(i -> Mono.delay(Duration.ofMillis(100)));
    }

    /**
     * "Overflow" will occur because evey "1" millisecond the data is being emitted
     * but the processing time is larger, i.e. "100" milliseconds.
     * Demand surpass will cause an "onError" Overflow.
     *
     * @return Flux<Long>
     */
    private Flux<Long> createOverflowFlux() {
        //  Emit every 1 millisecond:
        //  Create a Flux that emits long values starting with 0 and incrementing at specified time intervals on the global timer.
        //  The first element is emitted after an initial delay equal to the period.
        //  If demand is not produced in time, an "onError will be signalled with an overflow IllegalStateException detailing the tick that couldn't be emitted".
        //  In normal conditions, the Flux will never complete.
        //  Runs on the Schedulers. parallel() Scheduler
        return Flux.interval(Duration.ofMillis(1))
                .log()
                // Process time takes 100 milliseconds:
                // simulate that processing takes some milliseconds.
                .concatMap(i -> Mono.delay(Duration.ofMillis(100)));
    }

    private Flux<Long> createOverflowFluxWithDropOnBackpressure() {
        // Only in cases where elements emitted can be ignored when demand surpass occurs.
        //  Emit every 1 millisecond, in parallel:
        return Flux.interval(Duration.ofMillis(1))
                // Request an unbounded demand and push to the returned Flux,
                // or drop the observed elements if not enough demand is requested downstream.
                .onBackpressureDrop()
                // process time takes 100 milliseconds:
                .concatMap(i -> Mono.delay(Duration.ofMillis(100))
                        // Let this Mono complete successfully, then emit the provided value.
                        .thenReturn(i))
                .doOnNext(j -> System.out.printf("Element kept by consumer: %s%n", j));
    }

    private Flux<Long> createOverflowFluxWithBufferingOnBackpressure() {
        //  Stores in memory until demand is covered, i.e. save a large count of elements in memory
        //  so the demand isn't surpassed "Quickly" as storing a small count Overflow fast,
        //  Because with Buffering with Size "only" Overflow still occur then its suggested to
        //  establish a BufferingStrategy when the demand is not covered, e.g. DROP_LATEST. DROP_OLDEST, or ERROR
        //  to inform to reduce publishing.
        //  Emit every 1 millisecond, in parallel:
        return Flux.interval(Duration.ofMillis(1))
                // Request an unbounded demand and push to the returned Flux,
                // or "park up to maxSize" elements when not enough demand is requested downstream.
                // The first element past this buffer to arrive out of sync with the downstream subscriber's demand
                // (the "overflowing" element) immediately triggers an overflow error and cancels the source.
                // The Flux is going to terminate with an overflow error, but this error is delayed,
                // which lets the subscriber make more requests for the content of the buffer.
                .onBackpressureBuffer(1500,
                        // When the buffer is full, remove the oldest element from it and offer the new element at the end instead.
                        // Do not propagate an error
                        BufferOverflowStrategy.DROP_OLDEST)
                // process time takes 100 milliseconds:
                .concatMap(i -> Mono.delay(Duration.ofMillis(100))
                        // Let this Mono complete successfully, then emit the provided value.
                        .thenReturn(i))
                .doOnNext(j -> System.out.printf("Element kept by consumer: %s%n", j));
    }

    public static void main(String[] args) {
        var bs = new DBackpressureStrategies();
        //bs.createNoOverflowFlux()
        //bs.createOverflowFlux()
        //bs.createOverflowFluxWithDropOnBackpressure()
        bs.createOverflowFluxWithBufferingOnBackpressure()
                // blockLast(): it automatically subscribes to the Flux and starts processing elements
                // blocking indefinitely "until the upstream signals its last value or completes":
                .blockLast();
    }
}
