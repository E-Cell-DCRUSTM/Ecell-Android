package dcrustm.ecell.mobile.ui.gallery

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun GalleryScreen() {
    val context = LocalContext.current

    // Prepare a list of drawable resource IDs for images named "gal_1" to "gal_25"
    val galleryImages = remember {
        (1..25).map { number ->
            context.resources.getIdentifier("gal_$number", "drawable", context.packageName)
        }
    }

    val gridState = rememberLazyStaggeredGridState()

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2), // using 2 columns for a staggered layout
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Provide a unique key for each item
        items(items = galleryImages, key = { it }) { resId ->
            GalleryImageItem(resId = resId)
        }
    }
}

@Composable
fun GalleryImageItem(resId: Int) {
    val painter = painterResource(id = resId)
    // Use intrinsic aspect ratio if available; otherwise default to 1:1.
    val aspectRatio = if (painter.intrinsicSize.height > 0f)
        painter.intrinsicSize.width / painter.intrinsicSize.height
    else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()       // Each item fills the available width of its column.
            .aspectRatio(aspectRatio)  // Maintains each image's intrinsic aspect ratio.
            .clip(RoundedCornerShape(12.dp))  // Rounded corners for a modern look.
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop // Crop as needed while preserving aspect ratio.
        )
        // Overlay effect placeholder (customize later if desired)
        // Box(
        //     modifier = Modifier
        //         .matchParentSize()
        //         .background(Color.Black.copy(alpha = 0.15f))
        // )
    }
}

