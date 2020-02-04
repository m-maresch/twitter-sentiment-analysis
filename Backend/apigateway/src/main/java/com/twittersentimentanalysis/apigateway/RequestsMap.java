package com.twittersentimentanalysis.apigateway;

import com.twitter.hbc.core.Client;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;

import java.util.HashMap;

@Component
class RequestsMap extends HashMap<String, Tuple2<Client, TweetsQueue>> {
}
