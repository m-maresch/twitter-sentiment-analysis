package com.twittersentimentanalysis.apigateway

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class StreamConfiguration {

    @Bean
    fun coroutineScope() = CoroutineScope(Dispatchers.IO)

    @Bean
    fun senderOptions(
        @Value("\${kafka.server}") server: String,
    ) = SenderOptions.create<String, String>(
        mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to server,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )
    ).maxInFlight(1024)

    @Bean
    fun sender(senderOptions: SenderOptions<String, String>): KafkaSender<String, String> =
        KafkaSender.create(senderOptions)

    @Bean
    fun receiverOptions(
        @Value("\${kafka.server}") server: String,
        @Value("\${kafka.analyzedTweets.group}") analyzedTweetsGroup: String,
        @Value("\${kafka.analyzedTweets.topic}") analyzedTweetsTopic: String
    ) = ReceiverOptions.create<String, String>(
        mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to server,
            ConsumerConfig.GROUP_ID_CONFIG to analyzedTweetsGroup,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
    ).subscription(listOf(analyzedTweetsTopic))

    @Bean
    fun receiver(receiverOptions: ReceiverOptions<String, String>): KafkaReceiver<String, String> =
        KafkaReceiver.create(receiverOptions)

}