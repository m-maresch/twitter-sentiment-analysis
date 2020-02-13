package com.twittersentimentanalysis.apigateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

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

        try {
            while (!tweetsQueue.isEmpty()) {
                var tweet = tweetsQueue.take();

                // Analyse tweet here

                // Send result to client (one by one)
                messagingTemplate.convertAndSend( "/analysed-sentiment", tweet);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        // Cleanup
        //client.stop();
        requestsMap.remove(key);

        return DONE_SIGNAL;
    }
}