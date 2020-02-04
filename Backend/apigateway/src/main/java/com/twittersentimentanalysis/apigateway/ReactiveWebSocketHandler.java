package com.twittersentimentanalysis.apigateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.ArrayList;

@Component
class ReactiveWebSocketHandler implements WebSocketHandler {

    @Autowired
    RequestsMap requestsMap;

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        return webSocketSession.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(key -> Tuples.of(key, requestsMap.get(key)))
                .flatMap(values -> {
                    var key = values.getT1();
                    var client = values.getT2().getT1();
                    var tweetsQueue = values.getT2().getT2();

                    var list = new ArrayList<String>();

                    while (!client.isDone()) {
                        try {
                            list.add(tweetsQueue.take());
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    return webSocketSession.send(Flux.fromIterable(list)
                        .map(webSocketSession::textMessage)
                        .doOnComplete(() -> {
                            client.stop();
                            requestsMap.remove(key);
                        }));
                })
                .then();
    }
}