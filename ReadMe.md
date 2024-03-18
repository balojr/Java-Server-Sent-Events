# SSE with Spring 6 Webflux

To achieve this, we can make use of implementations such as the Flux class provided by the Reactor library, or potentially the ServerSentEvent entity, which gives us control over the events metadata.

###  Stream Events Using Flux

Flux is a reactive representation of a stream of events – it’s handled differently based on the specified request or response media type.

To create an SSE streaming endpoint, we’ll have to follow the W3C specifications and designate its MIME type as text/event-stream:

```java

@GetMapping(path = "/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> streamFlux() {
    return Flux.interval(Duration.ofSeconds(1))
      .map(sequence -> "Flux - " + LocalTime.now().toString());
}
```

The interval method creates a Flux that emits long values incrementally. Then we map those values to our desired output.

Let’s start our application and try it out by browsing the endpoint then.

We’ll see how the browser reacts to the events being pushed second by second by the server. For more information about Flux and the Reactor Core, we can check out this post.

### Making Use of the ServerSentEvent Element

We’ll now wrap our output String into a ServerSentSevent object, and examine the benefits of doing this:

```java
@GetMapping("/stream-sse")
public Flux<ServerSentEvent<String>> streamEvents() {
    return Flux.interval(Duration.ofSeconds(1))
      .map(sequence -> ServerSentEvent.<String> builder()
        .id(String.valueOf(sequence))
          .event("periodic-event")
          .data("SSE - " + LocalTime.now().toString())
          .build());
}
```
As we can appreciate, there’re a couple of benefits of using the ServerSentEvent entity:

* we can handle the events metadata, which we’d need in a real case scenario
* we can ignore “text/event-stream” media type declaration
In this case, we specified an id, an event name, and, most importantly, the actual data of the event.

Also, we could’ve added a comments attribute, and a retry value, which will specify the reconnection time to be used when trying to send the event.

# SSE Streaming in Spring MVC

As we said, the SSE specification was supported since Spring 4.2, when the SseEmitter class was introduced.

In simple terms, we’ll define an ExecutorService, a thread where the SseEmitter will do its work pushing data, and return the emitter instance, keeping the connection open in this manner:

```java
@GetMapping("/stream-sse-mvc")
public SseEmitter streamSseMvc() {
    SseEmitter emitter = new SseEmitter();
    ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
    sseMvcExecutor.execute(() -> {
        try {
            for (int i = 0; true; i++) {
                SseEventBuilder event = SseEmitter.event()
                  .data("SSE MVC - " + LocalTime.now().toString())
                  .id(String.valueOf(i))
                  .name("sse event - mvc");
                emitter.send(event);
                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            emitter.completeWithError(ex);
        }
    });
    return emitter;
}
```

Always make sure to pick the right ExecutorService for your use-case scenario.

# Understanding Server-Sent Events

Now that we know how to implement SSE endpoints, let’s try to go a little bit deeper by understanding some underlying concepts.

An SSE is a specification adopted by most browsers to allow streaming events unidirectionally at any time.

The ‘events’ are just a stream of UTF-8 encoded text data that follow the format defined by the specification.

This format consists of a series of key-value elements (id, retry, data and event, which indicates the name) separated by line breaks.

Comments are supported as well.

The spec doesn’t restrict the data payload format in any way; we can use a simple String or a more complex JSON or XML structure.

One last point we have to take into consideration is the difference between using SSE streaming and WebSockets.

While WebSockets offer full-duplex (bi-directional) communication between the server and the client, while SSE uses uni-directional communication.

Also, WebSockets isn’t an HTTP protocol and, opposite to SSE, it doesn’t offer error-handling standards.

[credits: Baeldung](https://www.baeldung.com/spring-server-sent-events)