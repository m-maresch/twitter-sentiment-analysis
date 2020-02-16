package com.twittersentimentanalysis.apigateway;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Basically the Web app sends a POST request to this endpoint and then connects to the WebSocket endpoint
@CrossOrigin({"http://localhost:3000"})
@RequestMapping(path = "/api/sentiment")
@RestController
class SentimentResource {

    // Twitter API Credentials are configurable
    @Value("${twitter.consumerKey}")
    private String consumerKey;

    @Value("${twitter.consumerSecret}")
    private String consumerSecret;

    @Value("${twitter.token}")
    private String token;

    @Value("${twitter.tokenSecret}")
    private String tokenSecret;

    @Autowired
    RequestsMap requestsMap;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void post(@RequestParam(value = "hashtags") List<String> hashtags) {
        // Contains the responses from the Twitter Streaming API
        final var tweetsQueue = new TweetsQueue(1000);

        // Set up Twitter API Client
        final var httpHosts = new HttpHosts(Constants.STREAM_HOST);

        final var hosebirdEndpoint = new StatusesFilterEndpoint();

        // Look for tweets with any of the submitted hashtags
        hosebirdEndpoint.trackTerms(hashtags);

        // For testing
        tweetsQueue.addAll(hashtags);

        final var client = new ClientBuilder()
                .hosts(httpHosts)
                .authentication(new OAuth1(consumerKey, consumerSecret, token, tokenSecret))
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(tweetsQueue))
                .build();

        //client.connect();

        // Concatenate all hashtags to one string used as the key
        final var key = hashtags.stream()
            .reduce((s1, s2) -> s1 + s2)
            .orElseThrow(() -> new RuntimeException(new IllegalArgumentException()));

        // Used in the WebSocket handler
        final var value = Request.builder()
            .twitterClient(client)
            .tweetsQueue(tweetsQueue)
            .build();

        requestsMap.put(key, value);
    }
}
