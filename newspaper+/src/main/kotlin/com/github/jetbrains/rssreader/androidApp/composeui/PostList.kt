package com.github.jetbrains.rssreader.androidApp.composeui

import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
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
//            modifier = Modifier.fillMaxHeight(),
            elevation = 10.dp,
            shape = RoundedCornerShape(padding)
        ) {
            Column(
                modifier = Modifier.clickable(onClick = onClick)
            ) {
                item.imageUrl?.let { url ->
                    val painter = rememberAsyncImagePainter(url)
                    Box(
                        modifier = Modifier.fillMaxWidth() //.padding(start = padding, end = padding)
                    ) {
                        Image(
                            painter = painter,
                            modifier = Modifier.align(Alignment.TopCenter).aspectRatio(16/9f), //.clip(RoundedCornerShape(percent = 5))
                            contentDescription = null
                        )
                        when (painter.state) {
                            is AsyncImagePainter.State.Empty -> {
                                // Handle the empty state
//                                Text("No image")
                            }
                            is AsyncImagePainter.State.Loading -> {
                                // Display a placeholder while the image is loading
                                Text("Loading...")
                            }
                            is AsyncImagePainter.State.Success -> {
                                // Handle the success state
                                // You can leave this empty if you don't need to do anything specific when the image loads successfully
//                                Text("success?")
                            }
                            is AsyncImagePainter.State.Error -> {
                                // Handle the error state
                                Text("Failed to load image")
                            }
                        }
                    }
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
                    text = if (!item.creator.isNullOrEmpty()) "${item.creator} | ${DateUtils.timeAgo(Date(item.date))}" else DateUtils.timeAgo(Date(item.date))
                )
//                Spacer(modifier = Modifier.size(padding))
                item.desc?.let { desc ->
                    Spacer(modifier = Modifier.size(padding))
                    Text(
                        modifier = Modifier.padding(start = padding, end = padding),
                        style = MaterialTheme.typography.body1,
//                        maxLines = 5,
//                        overflow = TextOverflow.Ellipsis,
                        text = desc
                    )
                    Log.d("PostItem", "Article description: $desc")
                }
//                Spacer(modifier = Modifier.size(padding))
            }
        }
    }
}