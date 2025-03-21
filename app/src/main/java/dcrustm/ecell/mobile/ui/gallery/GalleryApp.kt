package dcrustm.ecell.mobile.ui.gallery

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

//@Composable
//fun GalleryScreen() {
//    val context = LocalContext.current
//
//
//    // Prepare a list of drawable resource IDs for images named "gal_1" to "gal_25"
//    val galleryImages = remember {
//        (1..25).map { number ->
//            context.resources.getIdentifier("gal_$number", "drawable", context.packageName)
//        }
//    }
//
//    val gridState = rememberLazyStaggeredGridState()
//
//    LazyVerticalStaggeredGrid(
//        columns = StaggeredGridCells.Fixed(2), // using 2 columns for a staggered layout
//        state = gridState,
//        contentPadding = PaddingValues(8.dp),
//        verticalItemSpacing = 8.dp,
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        // Provide a unique key for each item
//        items(items = galleryImages, key = { it }) { resId ->
//            GalleryImageItem(resId = resId)
//        }
//    }
//}
//
//@Composable
//fun GalleryImageItem(resId: Int) {
//    val painter = painterResource(id = resId)
//    // Use intrinsic aspect ratio if available; otherwise default to 1:1.
//    val aspectRatio = if (painter.intrinsicSize.height > 0f)
//        painter.intrinsicSize.width / painter.intrinsicSize.height
//    else 1f
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()       // Each item fills the available width of its column.
//            .aspectRatio(aspectRatio)  // Maintains each image's intrinsic aspect ratio.
//            .clip(RoundedCornerShape(12.dp))  // Rounded corners for a modern look.
//    ) {
//        Image(
//            painter = painter,
//            contentDescription = null,
//            modifier = Modifier.fillMaxWidth(),
//            contentScale = ContentScale.Crop // Crop as needed while preserving aspect ratio.
//        )
//    }
//}

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = hiltViewModel()) {
// Observe images and refresh state from the ViewModel.
    val images by viewModel.imagesFlow.collectAsState()
    val refreshState by viewModel.refreshState.collectAsState()

// Trigger image fetch on launch.
    LaunchedEffect(Unit) {
        viewModel.refreshFull()
    }

// Create swipe-refresh state using Accompanist.
    val isRefreshing = refreshState is RefreshState.Loading
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refreshFull() },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (refreshState) {
                is RefreshState.Error -> {
                    Text(
                        text = (refreshState as RefreshState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    // Use LazyVerticalStaggeredGrid to display images with mixed aspect ratios.
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(images, key = { it.id }) { image ->
                            GalleryImageItem(imageUrl = image.imageUrl)
                        }
                    }
                    // In case the grid is empty (first load), show a loading indicator.
                    if (images.isEmpty() && isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }

}
@Composable
fun GalleryImageItem(imageUrl: String) {
// Use Coil's SubcomposeAsyncImage to load the image.
// When loaded successfully, the item's intrinsic aspect ratio is used.
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
    ) {
        when (val state = painter.state) {
            is AsyncImagePainter.State.Loading -> {
// Display a loading placeholder with a default aspect ratio.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is AsyncImagePainter.State.Error -> {
// Display an error placeholder.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error", color = Color.Red)
                }
            }
            is AsyncImagePainter.State.Success -> {
// Compute and apply the intrinsic aspect ratio.
                val intrinsicSize = painter.intrinsicSize
                val aspectRatio = if (intrinsicSize.height > 0f) {
                    intrinsicSize.width / intrinsicSize.height
                } else 1f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio)
                ) {
                    this@SubcomposeAsyncImage.SubcomposeAsyncImageContent()
                }
            }
            else -> {
// Fallback placeholder.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
        }
    }
}
