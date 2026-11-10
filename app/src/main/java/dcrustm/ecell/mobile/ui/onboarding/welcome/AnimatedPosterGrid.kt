package dcrustm.ecell.mobile.ui.onboarding.welcome

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dcrustm.ecell.mobile.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun OptimizedResourceImage(
    @DrawableRes resourceId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    maxDimensionPx: Int = 640
) {
    val context = LocalContext.current
    var bitmap by remember(resourceId, maxDimensionPx) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(resourceId, maxDimensionPx) {
        bitmap = withContext(Dispatchers.IO) {
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                inScaled = false
            }
            BitmapFactory.decodeResource(context.resources, resourceId, bounds)

            var sampleSize = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > maxDimensionPx) {
                sampleSize *= 2
            }
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inScaled = false
            }
            BitmapFactory.decodeResource(context.resources, resourceId, options)?.asImageBitmap()
        }
    }

    if (bitmap == null) {
        Box(
            modifier = modifier.background(androidx.compose.ui.graphics.Color(0xFFE9E9E5)),
            contentAlignment = Alignment.Center
        ) {
        }
    } else {
        Image(
            bitmap = bitmap!!,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun PosterGrid(modifier: Modifier = Modifier) {
    val posters = listOf(
        R.drawable.pos_1, R.drawable.pos_2, R.drawable.pos_3,
        R.drawable.pos_4, R.drawable.pos_5, R.drawable.pos_6
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        posters.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { resourceId ->
                    OptimizedResourceImage(
                        resourceId = resourceId,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp).clip(RoundedCornerShape(10.dp)),
                        maxDimensionPx = 480
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PosterGridPreview() {
    PosterGrid()
}
