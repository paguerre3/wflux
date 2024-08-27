# wflux
WebFlux compendium

---
<b>Reactive programming is a non-blocking technique</b>, asynchronous programming paradigm. 
E.g. having 5 chefs and 1 manager that handles requests, the manager won't be able to accept new requests above 
the maximum number that could be handled in parallel, "i.e. above 5 at the beginning", because using a blocking technique 
it won't be possible to accept 6 or more requests at the same time wih the need of Blocking them
until at least one chef becomes available (probably setting a blocking time period) or Discarding all the new requests above the threshold.
<b>A none blocking technique allows the Manager to accept all requests</b>, handle the processing priority order and availability, 
<b>and then notify the users once every request is being completed</b> -without the need of blocking requests 
until Chefs get available.

![non-blocking](./img/0-non-blocking-technique.png)

<b>WebFlux</b> is an implementation of Reactive Programming that can be considered as a Pipe.

<code>Mono</code> and <code>Flux</code> are the two main types of Reactive Programming:
- <code>Mono</code> is a <b>Single</b> value that can be either <b>empty</b> or <b>non-empty</b>. Used for a ***single entity***.
- <code>Flux</code> is a <b>Stream</b> of values that can be either <b>empty</b> or <b>non-empty</b>. Used for a ***collection of entities***.






