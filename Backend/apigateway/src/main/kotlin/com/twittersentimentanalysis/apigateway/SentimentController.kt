package com.twittersentimentanalysis.apigateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["http://localhost:3000"])
@RequestMapping(path = ["/sentiment"])
@RestController
class SentimentController(
    val kafkaStream: KafkaStream,
    val twitterStream: TwitterStream,
    val mapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(SentimentController::class.java)

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getSentiment(@RequestParam(name = "hashtags") hashtags: List<String>): Flow<AnalyzedTweet> {
        logger.info("Requested hashtags from client: $hashtags")

        val tweets = twitterStream.stream(hashtags.toSet())
        val senderJob = kafkaStream.send(tweets)

        return kafkaStream.receive()
            .onEach { logger.info("Received $it") }
            .onCompletion {
                logger.info("Client connection was closed - Cleaning up")
                twitterStream.removeHashtagRules(hashtags.toSet())
                senderJob.cancel()
            }
            .jsonToObject()
    }

    private inline fun <reified T> Flow<String>.jsonToObject() =
        map { mapper.readValue<T>(it) }
}