package com.github.jetbrains.rssreader.core.datasource.network

import com.github.jetbrains.rssreader.core.entity.Feed
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpMethod

internal class FeedLoader(
    private val httpClient: HttpClient,
    private val parser: FeedParser
) {
    suspend fun getFeed(url: String, isDefault: Boolean, retries: Int = 3): Feed {
        var lastException: Exception? = null

        repeat(retries) {
            try {
                val xml = httpClient.request {
                    this.url(url)
                    method = HttpMethod.Get
                }
                return parser.parse(url, xml.bodyAsText(), isDefault)
            } catch (e: Exception) {
                lastException = e
            }
        }

        throw lastException ?: RuntimeException("Unknown error while fetching feed from $url")
    }
}