package com.twittersentimentanalysis.apigateway

data class Tweet(
    val text: String,
)

data class AnalyzedTweet(
    val tweet: String,
    val polarity: Double,
    val subjectivity: Double
)

data class TweetResponse(
    val text: String,
    val tags: Set<String>,
)