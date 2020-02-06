package com.twittersentimentanalysis.apigateway;

import java.util.concurrent.LinkedBlockingQueue;

// Contains the tweets from the Twitter Streaming API
class TweetsQueue extends LinkedBlockingQueue<String> {
    TweetsQueue(int size) {
        super(size);
    }
}
