package com.twittersentimentanalysis.apigateway;

import org.springframework.stereotype.Component;

import java.util.HashMap;

// Used to match WebSocket connection to HTTP POST request via concatenated hashtags (key)
@Component
class RequestsMap extends HashMap<String, Request> {
}
