package com.twittersentimentanalysis.apigateway

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.sender.KafkaSender

@Component
class KafkaStream(
    @Value("\${kafka.tweets.topic}") val tweetsTopic: String,
    val tweetSender: KafkaSender<String, String>,
    val analyzedTweetReceiver: KafkaReceiver<String, String>,
    val mapper: ObjectMapper,
    val scope: CoroutineScope
) {

    private val logger = LoggerFactory.getLogger(SentimentController::class.java)

    fun send(tweets: Flow<Tweet>) =
        tweets.onEach { logger.info("Sending $it") }
            .toProducerRecords(tweetsTopic)
            .produce()
            .launchIn(scope)

    fun receive() =
        analyzedTweetReceiver.receive()
            .asFlow()
            .map { it.value() }

    private fun Flow<Tweet>.toProducerRecords(topic: String) =
        map { mapper.writeValueAsString(it) }
            .map { ProducerRecord<String, String>(topic, it) }

    private fun Flow<ProducerRecord<String, String>>.produce() =
        tweetSender.createOutbound()
            .send(asPublisher())
            .asFlow()
}