package io.xps.playground.ui.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScreenTittle(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier.padding(
            top = 136.dp,
            start = 16.dp,
            end = 16.dp
        ),
        text = text,
        style = MaterialTheme.typography.bodyLarge
    )
}
