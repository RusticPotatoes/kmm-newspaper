package com.github.jetbrains.rssreader.androidApp.composeui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.github.jetbrains.rssreader.core.entity.Post
import com.github.jetbrains.rssreader.androidApp.utils.DateUtils
//import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostList(
    modifier: Modifier,
    posts: List<Post>,
    listState: LazyListState,
    onClick: (Post) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(10.dp),
        state = listState
    ) {
        itemsIndexed(posts) { i, post ->
            if (i == 0) Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            PostItem(post) { onClick(post) }
            if (i != posts.size - 1) Spacer(modifier = Modifier.size(10.dp))
        }
    }
}

//private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

@Composable
fun PostItem(
    item: Post,
    onClick: () -> Unit
) {
    val padding = 10.dp
    Box {
        Card(
            modifier = Modifier.fillMaxHeight(),
            elevation = 10.dp,
            shape = RoundedCornerShape(padding)
        ) {
            Column(
                modifier = Modifier.clickable(onClick = onClick)
            ) {
                item.imageUrl?.let { url ->
                    Spacer(modifier = Modifier.size(padding))
                    Image(
                        painter = rememberAsyncImagePainter(url),
                        modifier = Modifier.height(180.dp).fillMaxWidth(),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.size(padding))
                Text(
                    modifier = Modifier.padding(start = padding, end = padding),
                    style = MaterialTheme.typography.h6,
                    text = item.title
                )
//                Spacer(modifier = Modifier.size(padding))
                Text(
                    modifier = Modifier.padding(start = padding, end = padding),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    text = DateUtils.timeAgo(Date(item.date))
                )
//                Spacer(modifier = Modifier.size(padding))
                item.desc?.let { desc ->
                    Spacer(modifier = Modifier.size(padding))
                    Text(
                        modifier = Modifier.padding(start = padding, end = padding).fillMaxHeight(),
                        style = MaterialTheme.typography.body1,
//                        maxLines = 5,
//                        overflow = TextOverflow.Ellipsis,
                        text = desc
                    )
                }
            }
        }
    }
}