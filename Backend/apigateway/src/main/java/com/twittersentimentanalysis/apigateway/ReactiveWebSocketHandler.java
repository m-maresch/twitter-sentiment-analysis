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

    // Used to access the respective Client and response queue
    @Autowired
    RequestsMap requestsMap;

    // The Client sends the concatenated hashtags and receives the analysed tweets as well as a sentiment score
    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        return webSocketSession.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(text -> text.substring(1, text.length()-1)) // Remove double "
                .map(key -> Tuples.of(key, requestsMap.get(key))) // Use the concatenated hashtags to get the Client and response queue
                .flatMap(values -> {
                    // Destruct data
                    var key = values.getT1();
                    var client = values.getT2().getT1();
                    var tweetsQueue = values.getT2().getT2();

                    var list = new ArrayList<String>();

                    // Aggregate the tweets
                    while (!tweetsQueue.isEmpty()) {
                        try {
                            list.add(tweetsQueue.take());
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    // Analyse tweets here

                    // Returns one big result for now, will be changed to streaming
                    return webSocketSession.send(Flux.fromIterable(list)
                        .map(webSocketSession::textMessage)
                        .doOnComplete(() -> {
                            // Cleanup
                            //client.stop();
                            requestsMap.remove(key);
                        }));
                })
                .then();
    }
}
