package dcrustm.ecell.mobile.ui.onboarding.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dcrustm.ecell.mobile.R
import dcrustm.ecell.mobile.ui.theme.AppTheme

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    onGoogleButtonClick: () -> Unit,
    onEmailButtonClick: () -> Unit,
    onAccountAlreadyClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()

    ) {
        Image(
            painter = painterResource(R.drawable.team),
            contentDescription = "The ecell team with the VC",
            modifier = Modifier.align(Alignment.Start)
                .padding(end = 20.dp)
                .clip(RoundedCornerShape(bottomEnd = 60.dp))
        )


        Image(
            painter = painterResource(R.drawable.logo_color),
            contentDescription = "Official ECell Logo",
            modifier = Modifier.size(160.dp)
                .padding(horizontal = 20.dp)
        )

        Text(
            text = "Start your journey today",
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .width(240.dp)
                .padding(bottom = 20.dp)
                .padding(horizontal = 20.dp)
        )


        Text(
            text = "Choose your favorite option to create an account to get started",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(
            modifier.height(32.dp)
        )

        // TODO: Create a common button shape

        // Google sign up/ login button
        Button(
            onClick = onGoogleButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.9f)
                .height(54.dp)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_google),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(
            modifier.height(16.dp)
        )

        Button(
            onClick = onEmailButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.9f)
                .height(54.dp)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Email,
                    contentDescription = "Email icon",
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Continue with Email",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(
            modifier.height(16.dp)
        )

        Text(
            text = "I already have an account",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable {
                    onAccountAlreadyClick()
                }
        )

    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun SignInScreenPreview() {
    AppTheme {
        SignInScreen(
            onGoogleButtonClick = {},
            onEmailButtonClick = {},
            onAccountAlreadyClick = {}
        )
    }
}