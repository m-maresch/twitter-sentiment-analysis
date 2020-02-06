package com.twittersentimentanalysis.apigateway;

import com.twitter.hbc.core.Client;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;

import java.util.HashMap;

// Used to match WebSocket connection to HTTP POST request via concatenated hashtags (key)
@Component
class RequestsMap extends HashMap<String, Tuple2<Client, TweetsQueue>> {
}
