package dcrustm.ecell.mobile.ui.about

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dcrustm.ecell.mobile.R
import dcrustm.ecell.mobile.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val sections = listOf("About DCRUST", "Our Vision", "Our Mission")
    val pagerState = rememberPagerState(pageCount = { sections.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.background(Color(0xFFF8F8F8)),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F8F8)
                ),
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    // Place the scrollable tab row inside the top app bar title slot.
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 16.dp,
                        indicator = { /* No indicator */ },
                        divider = { /* No divider */ },
                        containerColor = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        sections.forEachIndexed { index, title ->
                            val isSelected = pagerState.currentPage == index
                            Tab(
                                selected = isSelected,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) Color(0xFF2D2D2D) else Color(0xFFE8E8E8)
                                    )
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color.White else Color.DarkGray,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            pageSpacing = 16.dp,
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (page) {
                    0 -> AboutDcrustCard(
                        pagerState = pagerState,
                        currentPage = page
                    )
                    1 -> VisionCard(
                        pagerState = pagerState,
                        currentPage = page
                    )
                    2 -> MissionCard(
                        pagerState = pagerState,
                        currentPage = page
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutDcrustCard(pagerState: PagerState, currentPage: Int) {
    val alpha = if (pagerState.currentPage == currentPage) 1f else 0.7f

    Surface(
        color = Color(0xfcfcf9ff),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .alpha(alpha)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "About DCRUST",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Image(
                painter = painterResource(R.drawable.university),
                contentDescription = "Best university in existence",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Text(
                text = "Deenbandhu Chhotu Ram University of Science & Technology, Murthal came into being on " +
                        "6th November 2006 by upgrading erstwhile Chhotu Ram State College of Engineering, Murthal " +
                        "through an Act 29 of 2006 of the Legislature of the state of Haryana with the vision to " +
                        "facilitate and promote studies and research in emerging areas of higher education with focus on " +
                        "new frontiers of science, engineering, technology, architecture and management studies, humanities, " +
                        "and also to achieve excellence in these and connected fields.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VisionCard(pagerState: PagerState, currentPage: Int) {
    val alpha = if (pagerState.currentPage == currentPage) 1f else 0.7f

    Surface(
        color = Color(0xfcfcf9ff),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .alpha(alpha)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Our Vision",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Image(
                painter = painterResource(R.drawable.vision),
                contentDescription = "Best version description",
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Text(
                text = "We envision DCRUST E-Cell as a beacon of innovation " +
                        "and entrepreneurship, where students are inspired to dream " +
                        "big and are equipped with the tools to bring those dreams to life. " +
                        "By creating a supportive and dynamic environment, we aim to become a " +
                        "leading hub for entrepreneurial excellence, bridging the gap between academia and " +
                        "industry, and nurturing the next generation of entrepreneurs who will shape the future.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Text(
                text = "Our vision is to leave a lasting impact on both our campus and the broader startup ecosystem, " +
                        "driving positive change through entrepreneurial ventures.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MissionCard(pagerState: PagerState, currentPage: Int) {
    val alpha = if (pagerState.currentPage == currentPage) 1f else 0.7f

    Surface(
        color = Color(0xfcfcf9ff),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .alpha(alpha)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Our Mission",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Image(
                painter = painterResource(R.drawable.mission),
                contentDescription = "Our mission illustration",
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Text(
                text = "Our mission is to foster a vibrant entrepreneurial ecosystem at " +
                        "DCRUST by empowering students with the skills, resources, and " +
                        "platform they need to innovate and create impactful solutions. Through " +
                        "brainstorming sessions, networking opportunities, and hands-on workshops, " +
                        "we aim to ignite the entrepreneurial spirit in every student, enabling them to " +
                        "turn their ideas into reality.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Text(
                text = "We strive to build a community where collaboration, " +
                        "creativity, and leadership thrive, helping students not only grow " +
                        "as entrepreneurs but also develop skills that prepare them for a " +
                        "successful future.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.weight(1f))
            
            Image(
                painter = painterResource(R.drawable.logo_black),
                contentDescription = "Ecell watermark logo",
                modifier = Modifier.size(60.dp)
                    .offset(x=-6.dp)
            )
            Text(
                text = "v25.02.28",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AboutUsScreenPreview() {
    AppTheme {
        AboutUsScreen(
            onDismiss = {}
        )
    }

}