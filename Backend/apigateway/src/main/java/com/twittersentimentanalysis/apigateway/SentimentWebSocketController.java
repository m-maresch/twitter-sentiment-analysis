package com.twittersentimentanalysis.apigateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
public class SentimentWebSocketController {

    // Used to signal the client that all tweets have been analysed
    private final String DONE_SIGNAL = "done";

    private RequestsMap requestsMap;
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    public SentimentWebSocketController(RequestsMap requestsMap, SimpMessageSendingOperations messagingTemplate) {
        this.requestsMap = requestsMap;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/send/hashtags")
    @SendTo("/analysed-sentiment")
    public String onReceivedKey(String key) {
        var request = requestsMap.get(key); // Use the concatenated hashtags to get the Client and response queue

        // Destruct data
        var client = request.getTwitterClient();
        var tweetsQueue = request.getTweetsQueue();

        // Time out after 15 seconds
        var timeout = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).thenRunAsync(() -> {
            client.stop(); // Stop receiving new tweets

            // Cleanup
            requestsMap.remove(key);
        });

        while (!timeout.isDone()) {
            var tweet = Optional.ofNullable(tweetsQueue.poll());

            // Analyse tweet here

            // Send result to client (one by one)
            tweet.ifPresent(t -> messagingTemplate.convertAndSend( "/analysed-sentiment", t));
        }

        return DONE_SIGNAL;
    }
}