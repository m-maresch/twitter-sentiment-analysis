package com.twittersentimentanalysis.apigateway;

import java.util.concurrent.LinkedBlockingQueue;

class TweetsQueue extends LinkedBlockingQueue<String> {
    TweetsQueue(int size) {
        super(size);
    }
}
