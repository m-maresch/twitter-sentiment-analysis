package com.twittersentimentanalysis.apigateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.twitter.clientlib.api.TwitterApi
import com.twitter.clientlib.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class TwitterStream(
    private val twitterApi: TwitterApi,
    private val mapper: ObjectMapper,
) {
    private var tweetResponses: SharedFlow<TweetResponse> = MutableSharedFlow()

    private val logger = LoggerFactory.getLogger(TwitterStream::class.java)

    fun stream(hashtags: Set<String>): Flow<Tweet> {
        hashtags.forEach { addHashtagRule(it) }
        return tweetResponses
            .filter { (it.tags union hashtags).isNotEmpty() }
            .map { Tweet(it.text) }
            .flowOn(Dispatchers.Default)
    }

    fun removeHashtagRules(hashtags: Set<String>) {
        val rulesByTag = allRules().associateBy { it.tag }
        val ruleIdsToRemove = hashtags.mapNotNull { rulesByTag[it]?.id }
        removeRules(ruleIdsToRemove)
    }

    private fun filteredStream() =
        flow {
            val stream = twitterApi.tweets()
                .searchStream()
                .tweetFields(setOf("text"))
                .execute()

            val reader = BufferedReader(InputStreamReader(stream))
            var line: String? = reader.readLine()
            while (line != null) {
                if (line.isEmpty()) {
                    line = reader.readLine()
                    continue
                }

                logger.info("Filtered stream response: $line")

                val tweet = lineToTweetResponse(line)
                tweet?.let {
                    emit(it)
                    delay(50)
                }

                line = reader.readLine()
            }
        }.flowOn(Dispatchers.IO)
            .onCompletion { logger.warn("Connection to the Twitter API was closed") }
            .shareIn(GlobalScope, SharingStarted.Eagerly)

    private fun lineToTweetResponse(line: String): TweetResponse? {
        val response = mapper.readValue<FilteredStreamingTweetResponse>(line)
        val tags = response.matchingRules
            ?.mapNotNull { it.tag }
            ?.toSet()
            ?: emptySet()
        return response.data?.let { TweetResponse(it.text, tags) }
    }

    private fun allRules(): List<Rule> {
        val rules = twitterApi.tweets().rules.execute()
        logger.info("All rules response: ${rules.toJson()}")
        return rules.data ?: emptyList()
    }

    private fun addHashtagRule(hashtag: String) {
        val tag = hashtag.replace("#", "")
        val rule = AddRulesRequest().add(
            listOf(RuleNoId().value("#$tag").tag(tag))
        )

        val addOrDeleteRulesRequest = AddOrDeleteRulesRequest(rule)
        val result = twitterApi.tweets()
            .addOrDeleteRules(addOrDeleteRulesRequest)
            .execute()

        logger.info("Add rule response: ${result.toJson()}")
    }

    private fun removeRules(ruleIds: List<String>) {
        val rule = DeleteRulesRequest()
            .delete(DeleteRulesRequestDelete().ids(ruleIds))
        val addOrDeleteRulesRequest = AddOrDeleteRulesRequest(rule)
        val result = twitterApi.tweets()
            .addOrDeleteRules(addOrDeleteRulesRequest)
            .execute()

        logger.info("Delete rule response: ${result.toJson()}")
    }

    @PostConstruct
    fun setupFilteredStream() {
        tweetResponses = filteredStream()
    }

    @PreDestroy
    fun removeAllRules() {
        val allRuleIds = allRules().mapNotNull { it.id }

        if (allRuleIds.isNotEmpty()) {
            removeRules(allRuleIds)
        }
    }
}