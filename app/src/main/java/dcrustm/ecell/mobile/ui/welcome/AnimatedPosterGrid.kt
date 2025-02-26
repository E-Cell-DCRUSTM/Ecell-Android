package dcrustm.ecell.mobile.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dcrustm.ecell.mobile.R


@Composable
fun PosterGrid(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.pos_1),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Image(
                painter = painterResource(R.drawable.pos_2),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
                    .clip(RoundedCornerShape(10.dp))

            )
            Image(
                painter = painterResource(R.drawable.pos_3),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
                    .clip(RoundedCornerShape(10.dp))

            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.pos_4),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
                    .clip(RoundedCornerShape(10.dp))

            )
            Image(
                painter = painterResource(R.drawable.pos_5),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
                    .clip(RoundedCornerShape(10.dp))

            )
            Image(
                painter = painterResource(R.drawable.pos_6),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
                    .clip(RoundedCornerShape(10.dp))

            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AnimatedPosterGridPreview() {
    PosterGrid()
}