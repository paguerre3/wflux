# wflux
WebFlux compendium


---
## Concepts
<b>Reactive programming is a non-blocking technique, asynchronous programming paradigm</b>.

A <b>synchronous</b> programming paradigm is where the program is paused and waits for an event to occur, 
while an <b>asynchronous</b> programming paradigm is where the program is not paused and does not wait for an event to occur.

![sync-vs-async](./img/0-sync-vs-async-non-blocking.png)

E.g. having 5 chefs and 1 manager that handles requests, the manager won't be able to accept new requests above 
the maximum number that could be handled in parallel, "i.e. above 5 at the beginning", because using a blocking technique 
it won't be possible to accept 6 or more requests at the same time wih the need of Blocking them
until at least one chef becomes available (probably setting a blocking time period until discard) or Discarding all the new requests above the threshold.
<b>A none blocking technique allows the Manager to accept all requests</b>, handle the processing priority order and availability, 
<b>and then notify the users once every request is being completed</b> -without the need of blocking requests 
until Chefs get available.

![non-blocking](./img/0-non-blocking-technique.png)


---
### a) Why Reactive?
1. Asynchronous and non-blocking
2. Resource efficiency
3. Reduces Back Pressure
4. Better for Streaming Data


---
### b) Reactive Stream Workflow
Publisher/Subscriber reactive Steps:
1. When the <b>Subscriber</b> wants to receive some data, it subscribes to the Publisher, i.e. <code>Subscribe()</code> making the subscription a.k.a. request to the Publisher.
2. Once the <b>Publisher</b> receives the subscribe request from the Subscriber, it acknowledges it, and it sends the <code>subscription</code> a.k.a. response to the Subscriber.
3. After subscription is complete, when the <b>Subscriber</b> wants to receive some data for processing, it makes a <code>Request(n)</code> to the Publisher -being <b>"n"</b> how many data is being requested to the Publisher, e.g. 10 more entries of data.
4. Then <code>onNext()</code> is called on the <b>Subscriber</b> and the Publisher sends the data to the Subscriber -until <b>"n"</b> is reached, e.g. the Subscriber will make up to 10 <code>onNext()</code> calls.
5. Finally, when the <b>Publisher</b> finds that all data has been sent, e.g. the 10 entries where sent, the Publisher lets the Subscriber know that everything is done with <code>onComplete()</code>, i.e. the Request is completed. Also, if it were errors then <code>onError()</code> would be called and the Subscriber could do other things like Retrying.
![reactive-stream-workflow](./img/1-reactive-workflow.png)


---
## WebFlux
WebFlux dependency uses <b>Project Reactor</b> as the reactive library, based on Reactive Streams specification, 
for building non-blocking applications on the JVM.

<code>Mono</code> and <code>Flux</code> are the two main types of Reactive Programming:

#### 1. Mono
<code>Mono</code> is a <b>Single</b> value that can be either <b>empty</b> or <b>non-empty</b>. Used for a ***single entity***.
- ⚠️<code>Mono.just("")</code> doesn't allow null values while <code>Mono.justOrEmpty(null)</code> allows it without throwing error.
- <code>Mono.just("java").log()</code> will log the workflow life cycle after <code>subscribe()</code> is performed, otherwise it doesn't do anything, i.e. the log won't display "java" as no subscription exists.
- <code>Mono.empty()</code> simply returns empty even after subscription.
<pre><code>
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

// console output:
> Task :org.wflux.demo.AReactiveDefinitions.main()
20:07:55.708 [main] INFO reactor.Mono.Just.1 -- | onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
20:07:55.712 [main] INFO reactor.Mono.Just.1 -- | request(unbounded)
20:07:55.712 [main] INFO reactor.Mono.Just.1 -- | onNext(java)
java
20:07:55.712 [main] INFO reactor.Mono.Just.1 -- | onComplete()
20:07:55.712 [main] INFO reactor.Mono.Empty.2 -- onSubscribe([Fuseable] Operators.EmptySubscription)
20:07:55.712 [main] INFO reactor.Mono.Empty.2 -- request(unbounded)
20:07:55.712 [main] INFO reactor.Mono.Empty.2 -- onComplete()

BUILD SUCCESSFUL in 3s
</code></pre>

#### 2. Flux
<code>Flux</code> is a <b>Stream</b> of values that can be either <b>empty</b> or <b>non-empty</b>. Used for a ***collection of entities***.


---
### Requirements
1. ⚠️Docker must be running before executing Application.
2. <code>docker-compose -f mongo.yml up -d</code> before running tests. 

***Optional***: Running under WSL needs allowing traffic through the firewall, i.e. using PS <code>New-NetFirewallRule -DisplayName "Allow MongoDB" -Direction Inbound -LocalPort 27017 -Protocol TCP -Action Allow</code>.  


---
### Further samples
***Forked Repository***

[Reactive Programing, JWT, MSA and OAuth](https://github.com/paguerre3/Spring-Boot-Tutorials)







