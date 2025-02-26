package dcrustm.ecell.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dcrustm.ecell.mobile.ui.theme.AppTheme

@Composable
fun SloganPager(headerText: String, bodyText: String, modifier: Modifier = Modifier) {

    Surface(
        color = Color(0xfcfcf9ff),
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column {
            Text(
                text = headerText,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 14.sp
            )
        }
    }
}

@Preview
@Composable
private fun SlognPagerPreview() {
    AppTheme {
        SloganPager(
            headerText = "Ideate",
            bodyText = "Unleash the creativity and transform ideas into reality."
        )
    }
}