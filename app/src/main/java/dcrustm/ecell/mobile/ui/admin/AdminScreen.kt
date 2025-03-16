package dcrustm.ecell.mobile.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.NotificationAdd
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Timer3Select
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dcrustm.ecell.mobile.R
import dcrustm.ecell.mobile.ui.theme.AppTheme

@Composable
fun AdminScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(R.drawable.profile),
                    modifier = Modifier.size(40.dp),
                    contentDescription = null
                )
                Icon(
                    imageVector = Icons.Outlined.Info,
                    modifier = Modifier.size(24.dp),
                    contentDescription = "Info icon button"
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                AdminFeatureCard(
                    modifier = Modifier.weight(1f)
                )
                Spacer(
                    Modifier.width(20.dp)
                )
                AdminFeatureCard(
                    title = "Host",
                    description = "Meeting",
                    leadingIcon = Icons.Outlined.MeetingRoom,
                    modifier = Modifier.weight(1f)
                )

            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                AdminFeatureCard(
                    title = "Create",
                    description = "Quiz",
                    leadingIcon = Icons.Outlined.Quiz,
                    modifier = Modifier.weight(1f)
                )
                Spacer(
                    Modifier.width(20.dp)
                )
                AdminFeatureCard(
                    title = "Create",
                    description = "Buzzer",
                    leadingIcon = Icons.Outlined.Timer3Select,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                AdminFeatureCard(
                    title = "Create",
                    description = "Post",
                    leadingIcon = Icons.Outlined.MeetingRoom,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AdminFeatureCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xfff5f5f4),
    contentPadding: Dp = 10.dp,
    title: String = "Push",
    description: String = "Notification",
    leadingIcon: ImageVector = Icons.Outlined.NotificationAdd,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "Push Notification",
                modifier = Modifier.size(30.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowRight,
                        contentDescription = "Navigate",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }

}
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdminScreenPreview() {
    AppTheme {
        AdminScreen()
    }
}