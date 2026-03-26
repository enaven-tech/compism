package com.enaven.compism

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enaven.compism.compose.CompismSheet

@Composable
fun ScreenA(
    onOpen : () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Compism",
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onOpen) {
                Text("Open")
            }
        }
    }
}

@Composable
@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
fun PreviewScreenA() {
    ScreenA {}
}

// -------------------------------------------

@Composable
fun ScreenB(
    data: String?,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val displayText = data?.let {
                "Data Loaded: \"$it\""
            } ?: "Loading..."

            AnimatedContent(
                targetState = displayText,
            ) { text ->
                Text(text)
            }

            Button(onClick = onBack) {
                Text("Back")
            }
            Button(onClick = onNext) {
                Text("Next")
            }
        }
    }
}

@Composable
@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
fun PreviewScreenB() {
    ScreenB("Test Data", {}) {}
}

@Composable
fun ScreenC(
    sheetData : Int?,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Button(onClick = { AppObject.compism.send(AppEvent.Open) }) {
                Text("Open Sheet")
            }
            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }

    CompismSheet(
        sheetData,
        onDismissRequest = { AppObject.compism.send(AppEvent.Back) },
    ) { data ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text("Clicks: $data")

                Button(onClick = { AppObject.compism.send(AppEvent.Increment) }) {
                    Text("Click")
                }
            }
        }
    }
}