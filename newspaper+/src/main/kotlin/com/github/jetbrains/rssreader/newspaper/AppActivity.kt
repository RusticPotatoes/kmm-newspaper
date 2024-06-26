package com.github.jetbrains.rssreader.newspaper

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.github.jetbrains.rssreader.newspaper.composeui.AppTheme
import com.github.jetbrains.rssreader.newspaper.composeui.MainScreen
import com.github.jetbrains.rssreader.app.FeedSideEffect
import com.github.jetbrains.rssreader.app.FeedStore
import kotlinx.coroutines.flow.*
import org.koin.android.ext.android.inject

class AppActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val store: FeedStore by inject()
                val scaffoldState = rememberScaffoldState()
                val error = store.observeSideEffect()
                    .filterIsInstance<FeedSideEffect.Error>()
                    .collectAsState(null)
                LaunchedEffect(error.value) {
                    error.value?.let {
                        scaffoldState.snackbarHostState.showSnackbar(
                            it.error.message.toString()
                        )
                    }
                }
                Box(
                    Modifier.padding(
                        WindowInsets.systemBars
                            .only(WindowInsetsSides.Start + WindowInsetsSides.End)
                            .asPaddingValues()
                    )
                ) {
                    Scaffold(
                        scaffoldState = scaffoldState,
                        snackbarHost = { hostState ->
                            SnackbarHost(
                                hostState = hostState,
                                modifier = Modifier.padding(
                                    WindowInsets.systemBars
                                        .only(WindowInsetsSides.Bottom)
                                        .asPaddingValues()
                                )
                            )
                        }
                    ) {
                        Navigator(MainScreen())
                    }
                }
            }
        }
    }
}
