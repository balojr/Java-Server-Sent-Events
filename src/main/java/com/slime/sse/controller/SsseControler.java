package com.slime.sse.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/sse")
public class SsseControler {
    private static final String MEDIA_TYPE_TEXT_EVENT_STREAM = "text/event-stream";
    private static final String MEDIA_TYPE_APPLICATION_STREAM_JSON = "application/stream+json";

    /**
     * This method returns a Flux stream of strings. Each string is prefixed with "Flux_Example - " and
     * appended with the media type for text event stream. The Flux stream emits a new string every 3 seconds.
     * The method follows the W3C specifications for SSE streaming endpoint and designates its MIME type as text/event-stream.
     * The interval method creates a Flux that emits long values incrementally, which are then mapped to the desired output.
     *
     * @return a Flux stream of strings
     */
    @GetMapping(path = "/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFlux() {
        return Flux.interval(Duration.ofSeconds(3))
                .map(sequence -> "Flux_Example - " + MEDIA_TYPE_TEXT_EVENT_STREAM);
    }

    /**
     * This method returns a Flux stream of ServerSentEvent objects. Each ServerSentEvent object
     * contains an id, event type, and data. The id is the sequence number of the event, the event
     * type is "periodic-event", and the data is a string prefixed with "SSE - " and appended with the media type for application stream JSON and the current time.
     * The Flux stream emits a new ServerSentEvent every 3 seconds.
     * The output String is wrapped into a ServerSentEvent object, allowing for handling of event metadata and ignoring the "text/event-stream" media type declaration.
     *
     * @return a Flux stream of ServerSentEvent objects
     */
    @GetMapping("/stream-sse")
    public Flux<ServerSentEvent<String>> streamEvents() {
        return Flux.interval(Duration.ofSeconds(3))
                .map(sequence -> ServerSentEvent.<String> builder()
                        .id(String.valueOf(sequence))
                        .event("periodic-event")
                        .data("SSE - " + MEDIA_TYPE_APPLICATION_STREAM_JSON + LocalTime.now().toString())
                        .build());
    }

    /**
     * This method returns an SseEmitter that emits Server-Sent Events (SSE) for MVC.
     * It creates a new single-threaded ExecutorService to handle the emission of events.
     * In an infinite loop, it creates a new SseEvent with the current time, an incrementing id, and a name.
     * The event is then sent to the client every 3 seconds.
     * If an exception occurs during the execution, it completes the SseEmitter with the occurred error.
     *
     * @return an SseEmitter that emits Server-Sent Events (SSE)
     */
    @GetMapping("/stream-sse-mvc")
    public SseEmitter streamSseMvc() {
        SseEmitter emitter = new SseEmitter();
        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
        sseMvcExecutor.execute(() -> {
            try {
                for (int i = 0; true; i++) {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data("SSE MVC - " + LocalTime.now().toString())
                            .id(String.valueOf(i))
                            .name("sse event - mvc");
                    emitter.send(event);
                    Thread.sleep(3000);
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }
}