package com.github.jetbrains.rssreader.core

import android.util.Xml
import com.github.jetbrains.rssreader.core.datasource.network.FeedParser
import com.github.jetbrains.rssreader.core.entity.Feed
import com.github.jetbrains.rssreader.core.entity.Post
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

internal class AndroidFeedParser : FeedParser {
    private val dateFormat = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)

    override suspend fun parse(sourceUrl: String, xml: String, isDefault: Boolean): Feed = withContext(Dispatchers.IO) {
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        }

        var feed: Feed

        xml.reader().use { reader ->
            parser.setInput(reader)

            var tag = parser.nextTag()
            while (tag != XmlPullParser.START_TAG) {
                skip(parser)
                tag = parser.next()
            }

            feed = when (parser.name) {
                "rss" -> {
                    parser.nextTag() // Move to the next tag
                    readFeed(sourceUrl, parser, isDefault)
                }
                "rdf:RDF" -> readRdfFeed(sourceUrl, parser, isDefault)
                else -> throw IllegalArgumentException("Unsupported feed type: ${parser.name}")
            }
        }

        return@withContext feed
    }

    private fun readFeed(sourceUrl: String, parser: XmlPullParser, isDefault: Boolean): Feed {
        parser.require(XmlPullParser.START_TAG, null, "channel")

        var title: String? = null
        var link: String? = null
        var description: String? = null
        var imageUrl: String? = null
        val posts = mutableListOf<Post>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "title" -> title = readTagText("title", parser)
                "link" -> link = readTagText("link", parser)
                "description" -> description = readTagText("description", parser)
                "image" -> imageUrl = readImageUrl(parser)
                "item" -> posts.add(readPost(title!!, parser))
                else -> skip(parser)
            }
        }

        return Feed(title!!, link!!, description!!, imageUrl, posts, sourceUrl, isDefault)
    }

    private fun readImageUrl(parser: XmlPullParser): String? {
        parser.require(XmlPullParser.START_TAG, null, "image")

        var url: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "url" -> url = readTagText("url", parser)
                else -> skip(parser)
            }
        }

        return url
    }

    private fun readRdfPost(feedTitle: String, parser: XmlPullParser): Post {
        parser.require(XmlPullParser.START_TAG, null, "item")

        var title: String? = null
        var link: String? = null
        var description: String? = null
        var date: String? = null
        var creator: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "title" -> title = readTagText("title", parser)
                "link" -> link = readTagText("link", parser)
                "description" -> description = readTagText("description", parser)
                "dc:date" -> date = readTagText("dc:date", parser)
                "dc:creator" -> creator = readTagText("dc:creator", parser)
                else -> skip(parser)
            }
        }

        val dateLong: Long = date?.let {
//            val its = it.replace(" +0000", " GMT") // this format is terrible
//            val itst = its.replace("+00:00", " GMT")
            val dateFormats = listOf(
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss xx", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH), // for terrible format
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH),
            )

            var parsedDate: ZonedDateTime? = null
            var parseException: Throwable? = null

            for (dateFormat in dateFormats) {
                try {
                    parsedDate = ZonedDateTime.parse(it, dateFormat)
                    break
                } catch (e: Throwable) {
                    parseException = e
                }
            }

            if (parsedDate != null) {
                parsedDate.toEpochSecond() * 1000
            } else {
                Napier.e("Parse date error: ${parseException?.message}")
                null
            }
        } ?: System.currentTimeMillis()


        return Post(
            title ?: feedTitle,
            link,
            FeedParser.cleanText(description), // dont use cleanTextCompact
            null, // for rdf dont pull image, likely a stock twitter one
            dateLong,
            creator,
            feedTitle = feedTitle
        )
    }

    private fun readRdfFeed(sourceUrl: String, parser: XmlPullParser, isDefault: Boolean): Feed {
        parser.require(XmlPullParser.START_TAG, null, "rdf:RDF")

        var title: String? = null
        var link: String? = null
        var description: String? = null
        var imageUrl: String? = null
        val posts = mutableListOf<Post>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "channel" -> {
                    val channelProperties = readChannelProperties(parser)
                    title = channelProperties["title"]
                    link = channelProperties["link"]
                    description = channelProperties["description"]
                }
                "image" -> imageUrl = readImageUrl(parser)
                "item" -> posts.add(readRdfPost(title!!, parser))
                else -> skip(parser)
            }
        }

        return Feed(title!!, link!!, description!!, imageUrl, posts, sourceUrl, isDefault)
    }

    private fun readChannelProperties(parser: XmlPullParser): Map<String, String?> {
        val properties = mutableMapOf<String, String?>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "title", "link", "description" -> properties[parser.name] = readTagText(parser.name, parser)
                else -> skip(parser)
            }
        }

        return properties
    }

    private fun readPost(feedTitle: String, parser: XmlPullParser): Post {
        parser.require(XmlPullParser.START_TAG, null, "item")

        var title: String? = null
        var link: String? = null
        var description: String? = null
        var date: String? = null
        var creator: String? = null

        var content: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "title" -> title = readTagText("title", parser)
                "link" -> link = readTagText("link", parser)
                "description" -> description = readTagText("description", parser)
                "content:encoded" -> content = readTagText("content:encoded", parser)
                "pubDate" -> date = readTagText("pubDate", parser)
                "dc:creator" -> creator = readTagText("dc:creator", parser)
                else -> skip(parser)
            }
        }

        val dateLong: Long = date?.let {
//            val its = it.replace(" +0000", " GMT") // this format is terrible
//            val itst = its.replace("+00:00", " GMT")
            val dateFormats = listOf(
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss xx", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH), // for terrible format
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH),
            )

            var parsedDate: ZonedDateTime? = null
            var parseException: Throwable? = null

            for (dateFormat in dateFormats) {
                try {
                    parsedDate = ZonedDateTime.parse(it, dateFormat)
                    break
                } catch (e: Throwable) {
                    parseException = e
                }
            }

            if (parsedDate != null) {
                parsedDate.toEpochSecond() * 1000
            } else {
                Napier.e("Parse date error: ${parseException?.message}")
                null
            }
        } ?: System.currentTimeMillis()

        return Post(
            title ?: feedTitle,
            link,
            FeedParser.cleanText(description), // dont use cleanTextCompact
            FeedParser.pullPostImageUrl(link, description, content),
            dateLong,
            creator,
            feedTitle = feedTitle
        )
    }

    private fun readTagText(tagName: String, parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, tagName)
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, tagName)
        return title
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun skip(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, null, null)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

}