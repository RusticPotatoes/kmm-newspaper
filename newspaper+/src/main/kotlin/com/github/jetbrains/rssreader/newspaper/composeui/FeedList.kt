package com.github.jetbrains.rssreader.newspaper.composeui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.github.jetbrains.rssreader.newspaper.R
import com.github.jetbrains.rssreader.app.FeedAction
import com.github.jetbrains.rssreader.app.FeedStore
import com.github.jetbrains.rssreader.core.entity.Feed
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextField
//
@Composable
fun FeedList(store: FeedStore) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val state = store.observeState().collectAsState()
        val showAddDialog = remember { mutableStateOf(false) }
        val showEditDialog = remember { mutableStateOf(false) }
        val selectedFeed = remember { mutableStateOf<Feed?>(null) }

        FeedItemList(feeds = state.value.feeds, onClick = { feed ->
            selectedFeed.value = feed
            showEditDialog.value = true
        })
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .navigationBarsPadding()
                .imePadding(),
            onClick = { showAddDialog.value = true }
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                contentDescription = null
            )
        }
        if (showAddDialog.value) {
            AddFeedDialog(
                onAdd = {
                    store.dispatch(FeedAction.Add(it))
                    showAddDialog.value = false
                },
                onDismiss = {
                    showAddDialog.value = false
                }
            )
        }
        if (showEditDialog.value) {
            selectedFeed.value?.let { feed ->
                EditFeedDialog(
                    feed = feed,
                    onEdit = { newUrl ->
                        store.dispatch(FeedAction.Edit(feed.sourceUrl, newUrl))
                        selectedFeed.value = null
                        showEditDialog.value = false
                    },
                    onDelete = {
                        store.dispatch(FeedAction.Delete(feed.sourceUrl))
                        selectedFeed.value = null
                        showEditDialog.value = false
                    },
                    onDismiss = {
                        selectedFeed.value = null
                        showEditDialog.value = false
                    }
                )
            }
        }
    }
}

@Composable
fun FeedItemList(
    feeds: List<Feed>,
    onClick: (Feed) -> Unit
) {
    LazyColumn {
        itemsIndexed(feeds) { i, feed ->
            if (i == 0) Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            FeedItem(feed) { onClick(feed) }
        }
    }
}

@Composable
fun FeedItem(
    feed: Feed,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        FeedIcon(feed = feed)
        Spacer(modifier = Modifier.size(10.dp))
        Column {
            Text(
                style = MaterialTheme.typography.body1,
                text = feed.title
            )
            Text(
                style = MaterialTheme.typography.body2,
                text = feed.desc
            )
        }
    }
}

@Composable
fun EditFeedDialog(
    feed: Feed,
    onEdit: (newUrl: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var newUrl by remember { mutableStateOf(feed.sourceUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.edit)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.rss_feed_url),
                    style = MaterialTheme.typography.body1
                )
                TextField(
                    value = newUrl,
                    onValueChange = { newUrl = it }
                )
            }
        },
        confirmButton = {
            Row {
                Button(
                    onClick = {
                        onEdit(newUrl)
                    }
                ) {
                    Text(text = stringResource(R.string.save))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onDelete
                ) {
                    Text(text = stringResource(R.string.remove))
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}