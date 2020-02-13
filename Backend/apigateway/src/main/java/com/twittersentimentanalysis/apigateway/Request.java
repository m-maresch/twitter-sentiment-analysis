package com.twittersentimentanalysis.apigateway;

import com.twitter.hbc.core.Client;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
class Request {
    private Client twitterClient;
    private TweetsQueue tweetsQueue;
}
