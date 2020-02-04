package com.twittersentimentanalysis.apigateway;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;

@RequestMapping(path = "/api/sentiment")
@RestController
class SentimentResource {

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
    public Mono<Void> get(@RequestParam(value = "hashtags", required = true) List<String> hashtags) {
        final TweetsQueue tweetsQueue = new TweetsQueue(1000);

        return Mono.just(new HttpHosts(Constants.STREAM_HOST))
                .map(httpHosts -> {
                    StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

                    hosebirdEndpoint.trackTerms(hashtags);

                    return new ClientBuilder()
                        .hosts(httpHosts)
                        .authentication(new OAuth1(consumerKey, consumerSecret, token, tokenSecret))
                        .endpoint(hosebirdEndpoint)
                        .processor(new StringDelimitedProcessor(tweetsQueue));
                })
                .map(ClientBuilder::build)
                .doOnNext(BasicClient::connect)
                .doOnNext(hosebirdClient -> requestsMap.put(hashtags.stream()
                    .reduce((s1, s2) -> s1 + s2)
                    .orElseThrow(() -> new RuntimeException(new IllegalArgumentException())),
                    Tuples.of(hosebirdClient, tweetsQueue)))
                .then();
    }
}
