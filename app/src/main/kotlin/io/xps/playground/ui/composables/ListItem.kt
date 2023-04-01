package io.xps.playground.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ListItem(
    tittle: String,
    hint: String = "",
    @DrawableRes drawable: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .background(Color.Black.copy(if (isSelected) 0.2f else 0f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(end = 16.dp),
            painter = painterResource(id = drawable),
            contentDescription = null
        )
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = tittle,
                style = MaterialTheme.typography.bodyMedium
            )
            if (hint.isNotBlank()) {
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = hint,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
