package com.github.jetbrains.rssreader.androidApp.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

class DateUtils {
    companion object {
        fun timeAgo(date: Date): String {
            val now = LocalDateTime.now()
            val past = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            val components = listOf(
                Pair("year", ChronoUnit.YEARS.between(past, now)),
                Pair("month", ChronoUnit.MONTHS.between(past, now)),
                Pair("day", ChronoUnit.DAYS.between(past, now)),
                Pair("hour", ChronoUnit.HOURS.between(past, now)),
                Pair("minute", ChronoUnit.MINUTES.between(past, now)),
                Pair("second", ChronoUnit.SECONDS.between(past, now))
            )

            var timeAgo = ""
            for ((unit, value) in components) {
                if (value > 0) {
                    timeAgo += format(value, unit)
                    break
                }
            }

            return if (timeAgo.isEmpty()) "just now" else "$timeAgo ago"
        }

        private fun format(value: Long, unit: String): String {
            return "$value $unit${if (value == 1L) "" else "s"}"
        }
    }
}