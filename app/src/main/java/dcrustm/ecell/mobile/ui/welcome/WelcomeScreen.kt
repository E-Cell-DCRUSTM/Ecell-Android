package dcrustm.ecell.mobile.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import dcrustm.ecell.mobile.R
import dcrustm.ecell.mobile.data.local.slogans
import dcrustm.ecell.mobile.ui.components.DummyContainerGrid
import dcrustm.ecell.mobile.ui.components.SloganPager
import dcrustm.ecell.mobile.ui.theme.AppTheme

// TODO: Checkout the colors from an expert 

@OptIn(ExperimentalPagerApi::class)
@Composable
fun WelcomeScreen(onGetStartedClick: () -> Unit, modifier: Modifier = Modifier) {

    val pagerState = rememberPagerState()

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color.Gray)
    ) {
        Image(
            painter = painterResource(R.drawable.logo_color),
            contentDescription = "Official Ecell logo",
            modifier = Modifier
                .size(100.dp)
                .offset(x = -6.dp)
        )
        Spacer(
            modifier = Modifier.height(80.dp)
        )

        DummyContainerGrid(modifier.fillMaxWidth())

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Surface(
            color = Color(0xFCFCF9FF),
            modifier = Modifier
                .align(Alignment.End)
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {

                HorizontalPager(
                    count = slogans.size,
                    state = pagerState,
                    modifier = Modifier.height(140.dp)
                ) { page ->
                    val (title, description) = slogans[page]
                    SloganPager(headerText = title, bodyText = description)
                }

                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    activeColor = Color.Black, // Blue active dot
                    inactiveColor = Color.Gray.copy(alpha = 0.5f), // Faded inactive dots
                    indicatorWidth = 8.dp,
                    spacing = 8.dp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(
                    modifier = Modifier.height(30.dp)
                )

                Button(
                    onClick = onGetStartedClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WelcomeScreenPreview() {

    AppTheme {
        WelcomeScreen(
            onGetStartedClick = {}
        )
    }

}
