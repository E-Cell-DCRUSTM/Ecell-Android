package dcrustm.ecell.mobile.ui.onboarding.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dcrustm.ecell.mobile.R
import dcrustm.ecell.mobile.domain.model.EmailProviderUser
import dcrustm.ecell.mobile.domain.model.User
import dcrustm.ecell.mobile.domain.model.toUser
import dcrustm.ecell.mobile.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    onGoogleSignupClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    onEmailSignupClick: (user: User) -> Unit,
    onEmailLoginClick: (email: String, password: String) -> Unit,

) {

    // State to control the visibility of the email bottom sheet
    var showEmailBottomSheet by remember { mutableStateOf(false) }

    var showLoginBottomSheet by remember { mutableStateOf(false) }

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
            modifier = Modifier
                .align(Alignment.Start)
                .padding(end = 20.dp)
                .clip(RoundedCornerShape(bottomEnd = 60.dp))
        )


        Image(
            painter = painterResource(R.drawable.logo_color),
            contentDescription = "Official ECell Logo",
            modifier = Modifier
                .size(160.dp)
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
            onClick = onGoogleSignupClick,
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
            onClick = {
                showEmailBottomSheet = true
            },
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
                    showLoginBottomSheet = true
                }
        )

    }

    // ModalBottomSheet that shows when the email sign-up button is clicked.
    if (showEmailBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEmailBottomSheet = false },
            containerColor = Color(0xFFF4F4F1),
            modifier = Modifier.fillMaxWidth()
        ) {
            EmailBottomSheetContent { email, password, fullName ->

                val userData = EmailProviderUser(
                    email = email,
                    password = password,
                    fullName = fullName
                ).toUser()

                onEmailSignupClick(userData)
                showEmailBottomSheet = false
            }
        }
    }

    // LoginBottomSheet when "I already have an account" is clicked.
    if (showLoginBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLoginBottomSheet = false },
            containerColor = Color(0xFFF4F4F1),
            modifier = Modifier.fillMaxWidth()
        ) {
            LoginBottomSheetContent(
                onLogin = { email, password ->
                    onEmailLoginClick(email, password)
                    showLoginBottomSheet = false
                },
                onGoogleClick = {
                    onGoogleLoginClick()
                    showLoginBottomSheet = false
                }
            )
        }
    }

}

val dummyOnEmailContinue: (String, String, String) -> Unit = { email, password, fullName ->
    println("Email: $email")
    println("Password: $password")
    println("Full Name: $fullName")
}

val dummyOnLogin: (String, String) -> Unit = { email, password ->
    println("Login Email: $email")
    println("Login Password: $password")
}

val dummyOnGoogleLogin: () -> Unit = {
    println("Login with Google clicked")
}

/**

A new composable for the login bottom sheet with the following UI:

Header "Welcome back"

Inputs for email & password (grouped in a container)

A Login button (triggering dummyOnLogin) which dismisses the sheet

A horizontal divider with "OR" text in the middle.

A slightly smaller "Continue with Google" button (triggering dummyOnGoogleLogin)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBottomSheetContent(
    onLogin: (email: String, password: String) -> Unit,
    onGoogleClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Grouped inputs for email and password (custom for login)
        LoginGroupedTextFields(
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
            passwordVisible = passwordVisible,
            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login button
        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Login", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal divider with "OR" text in-between
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.Gray)
            Text(text = "OR", modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Continue with Google" button (smaller width and height)
        Button(
            onClick = { onGoogleClick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.9f)
                .height(48.dp)
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
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

    }
}

/**

A custom grouped input component for the login bottom sheet,

displaying email and password in a container with a divider.
 */
@Composable
fun LoginGroupedTextFields(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))
    ) {
        // Email input row
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 40.dp)
            ) {
                Text(
                    text = "Email Address",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black
                )
                BasicTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Divider between email and password rows.
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
            color = Color.Black
        )

        // Password input row
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 40.dp)
            ) {
                Text(
                    text = "Password",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black
                )
                BasicTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(
                onClick = onPasswordVisibilityToggle,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailBottomSheetContent(
    onEmailContinue: (email: String, password: String, fullName: String) -> Unit
) {

    // Local state for full name, email, password and password visibility.
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Standard email regex for validation.
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    val emailValid = email.isEmpty() || email.matches(emailRegex)

    // The form is valid only when all fields are non-empty and email passes regex.
    val isFormValid =
        fullName.isNotEmpty() && email.isNotEmpty() && email.matches(emailRegex) && password.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Create your account",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Grouped inputs for email & password.
        CustomGroupedTextFields(
            email = email,
            onEmailChange = { email = it },
            emailInvalid = email.isNotEmpty() && !emailValid,
            password = password,
            onPasswordChange = { password = it },
            passwordVisible = passwordVisible,
            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // New grouped input for "Full Name"
        CustomSingleInputField(
            label = "Full Name",
            value = fullName,
            onValueChange = { fullName = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Continue button enabled only when all fields are valid.
        Button(
            onClick = { onEmailContinue(email, password, fullName) },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Continue", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(80.dp))

        HorizontalDivider(thickness = 2.dp, color = DividerDefaults.color)

    }

}

/**

A composable that groups the email and password fields together

in one container with a black border and a thin divider between them.
 */
@Composable
fun CustomGroupedTextFields(
    email: String,
    onEmailChange: (String) -> Unit,
    emailInvalid: Boolean,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))
    ) {
        // Email row
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 40.dp)
            ) {
                Text(
                    text = "Email Address",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black
                )
                BasicTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (emailInvalid) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Invalid Email",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                )
            }
        }

        // Divider between email and password.
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
            color = Color.Black
        )

        // Password row
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 40.dp)
            ) {
                Text(
                    text = "Password",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black
                )
                BasicTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(
                onClick = onPasswordVisibilityToggle,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        }

    }
}

/**

A composable for a single input field inside a black-bordered container.
 */
@Composable
fun CustomSingleInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Black
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

