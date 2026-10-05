package com.example.presentation.login

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.models.FirebaseUserEntity
import com.example.data.models.UserRole
import com.example.presentation.components.PulsingLiveDot
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RiskLow

@Composable
fun SplashScreen(
    onSkipSplash: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyberBackground,
                        Color(0xFF060C1E),
                        CyberSurface
                    )
                )
            )
            .clickable { onSkipSplash() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 420.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(116.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(2.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = stringResource(id = R.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "BrandShield AI",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.splash_subtitle),
                style = MaterialTheme.typography.titleMedium,
                color = ElectricCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            LinearProgressIndicator(
                color = ElectricCyan,
                trackColor = ElectricCyan.copy(alpha = 0.18f),
                modifier = Modifier
                    .width(180.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Connecting Firebase Auth, Firestore & Multimodal Threat Engine...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    initialEmail: String,
    initialRole: UserRole,
    loginError: String?,
    firebaseUsers: List<FirebaseUserEntity> = emptyList(),
    onLogin: (String, String, UserRole) -> Unit,
    onFirebaseEmailAuth: (Context, String, String, String, String, String, UserRole, Boolean) -> Unit = { _, e, p, _, _, _, r, _ ->
        onLogin(e, p, r)
    },
    onGoogleFirebaseAuth: (Context, String, String, UserRole, String, Boolean) -> Unit = { _, _, _, r, _, _ ->
        onLogin("varigondabhavya@gmail.com", "GoogleOAuth", r)
    },
    onFacebookFirebaseAuth: (Context, String, String, UserRole, String, Boolean) -> Unit = { _, _, _, r, _, _ ->
        onLogin("bhavya.soc.fb@brandshield.in", "FacebookOAuth", r)
    },
    onQuickAuth: (String, UserRole) -> Unit
) {
    val context = LocalContext.current
    var isSignUpMode by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("Bhavya Varigonda") }
    var organization by remember { mutableStateOf("State Bank of India — YONO SOC Desk") }
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("SOC-Shield#2026") }
    var confirmPassword by remember { mutableStateOf("SOC-Shield#2026") }
    var selectedRole by remember { mutableStateOf(initialRole) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var showGoogleAuthDialog by remember { mutableStateOf(false) }
    var showFacebookAuthDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hero Banner Card with Firebase Connection Status
            Card(
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = stringResource(id = R.string.hero_banner_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        CyberBackground.copy(alpha = 0.25f),
                                        CyberBackground.copy(alpha = 0.88f),
                                        CyberBackground
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "BRANDSHIELD AI • SOC PORTAL",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                color = RiskLow.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.border(1.dp, RiskLow.copy(alpha = 0.55f), RoundedCornerShape(50))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    PulsingLiveDot(color = RiskLow)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "FIREBASE DB READY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RiskLow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "AI-Powered Digital Risk Protection",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Firebase Authentication (Google, Facebook & Email) + Users Database Sync",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Role Selector Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SELECT SOC ANALYST ROLE",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.entries.forEach { role ->
                            val isSelected = selectedRole == role
                            val roleColor = Color(role.badgeColorHex)
                            Surface(
                                color = if (isSelected) roleColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) roleColor else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedRole = role }
                                    .testTag("role_chip_${role.name.lowercase()}")
                            ) {
                                Text(
                                    text = role.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) roleColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedRole.permissionsDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Main Login / Sign-Up + Google & Facebook Firebase Auth Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Login vs Sign Up Mode Toggle Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Surface(
                                color = if (!isSignUpMode) ElectricCyan else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable { isSignUpMode = false }
                                    .testTag("login_mode_tab")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Security,
                                        contentDescription = null,
                                        tint = if (!isSignUpMode) Color(0xFF002026) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LOGIN (SIGN IN)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (!isSignUpMode) Color(0xFF002026) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                color = if (isSignUpMode) ElectricCyan else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable { isSignUpMode = true }
                                    .testTag("signup_mode_tab")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PersonAdd,
                                        contentDescription = null,
                                        tint = if (isSignUpMode) Color(0xFF002026) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SIGN UP (NEW)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSignUpMode) Color(0xFF002026) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // PROMINENT GOOGLE & FACEBOOK FIREBASE LOGIN / SIGN-UP SECTION
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSignUpMode) "QUICK SIGN UP WITH FIREBASE OAUTH" else "INSTANT LOGIN WITH FIREBASE OAUTH",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = RiskLow,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Firestore Sync",
                                style = MaterialTheme.typography.labelSmall,
                                color = RiskLow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Google & Facebook Icon Buttons Side-by-Side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // GOOGLE LOGIN / SIGN UP BUTTON WITH OFFICIAL 4-COLOR GOOGLE ICON
                        Surface(
                            color = Color(0xFF131F42),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .border(1.5.dp, Color(0xFF4285F4).copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                                .clickable { showGoogleAuthDialog = true }
                                .testTag("google_login_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp)
                            ) {
                                Surface(
                                    color = Color.White,
                                    shape = CircleShape,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_google_logo),
                                            contentDescription = "Google Login Icon",
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isSignUpMode) "Sign Up with" else "Login with",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.72f)
                                    )
                                    Text(
                                        text = "Google",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // FACEBOOK LOGIN / SIGN UP BUTTON WITH OFFICIAL FACEBOOK ICON
                        Surface(
                            color = Color(0xFF12234E),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .border(1.5.dp, Color(0xFF1877F2).copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                                .clickable { showFacebookAuthDialog = true }
                                .testTag("facebook_login_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp)
                            ) {
                                Surface(
                                    color = Color.White,
                                    shape = CircleShape,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_facebook_logo),
                                            contentDescription = "Facebook Login Icon",
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isSignUpMode) "Sign Up with" else "Login with",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.72f)
                                    )
                                    Text(
                                        text = "Facebook",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        )
                        Text(
                            text = if (isSignUpMode) "  OR REGISTER EMAIL IN FIREBASE DB  " else "  OR SIGN IN WITH EMAIL & PASSWORD  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sign-Up Extra Fields (Full Name & Bank/SOC Organization)
                    AnimatedVisibility(visible = isSignUpMode) {
                        Column {
                            TextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Analyst Full Name") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Full Name",
                                        tint = ElectricCyan
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_name_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            TextField(
                                value = organization,
                                onValueChange = { organization = it },
                                label = { Text("Bank / SOC Organization") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Business,
                                        contentDescription = "Organization",
                                        tint = ElectricCyan
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_org_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(id = R.string.login_email_label)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.MailOutline,
                                contentDescription = "Email",
                                tint = ElectricCyan
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(id = R.string.login_password_label)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Password",
                                tint = ElectricCyan
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    AnimatedVisibility(visible = isSignUpMode) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Security Password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Confirm Password",
                                        tint = ElectricCyan
                                    )
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_password_input")
                            )
                        }
                    }

                    if (loginError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loginError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { isSignUpMode = !isSignUpMode }
                        ) {
                            Text(
                                text = if (isSignUpMode) "Already registered? Login" else "New analyst? Sign Up",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                        }
                        TextButton(
                            onClick = { showForgotDialog = true },
                            modifier = Modifier.testTag("forgot_password_button")
                        ) {
                            Text(
                                text = stringResource(id = R.string.login_forgot_password),
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onFirebaseEmailAuth(
                                context,
                                email,
                                password,
                                confirmPassword,
                                fullName,
                                organization,
                                selectedRole,
                                isSignUpMode
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF001F26)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_button")
                    ) {
                        Icon(
                            imageVector = if (isSignUpMode) Icons.Filled.PersonAdd else Icons.Filled.Security,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSignUpMode) "Sign Up & Sync to Firebase Database" else stringResource(id = R.string.login_button),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SSO & Biometric Login buttons (Section 8)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onQuickAuth("Enterprise SSO (SAML 2.0)", selectedRole) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("sso_login_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Key,
                                contentDescription = "SSO",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SSO",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedButton(
                            onClick = { onQuickAuth("Hardware Biometric Key", selectedRole) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("biometric_login_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Fingerprint,
                                contentDescription = "Biometric Login",
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Biometric",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Live Synced Firebase Database Users Registry Card (`users` collection)
            if (firebaseUsers.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .testTag("firebase_users_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Storage,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "FIREBASE DATABASE • SYNCED ACCOUNTS (${firebaseUsers.size})",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ElectricCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Collection: firestore/users • Tap any synced profile for instant login",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        firebaseUsers.take(4).forEach { fbUser ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        when (fbUser.authProvider) {
                                            "google.com" -> onGoogleFirebaseAuth(
                                                context,
                                                fbUser.email,
                                                fbUser.displayName,
                                                selectedRole,
                                                fbUser.organization,
                                                false
                                            )
                                            "facebook.com" -> onFacebookFirebaseAuth(
                                                context,
                                                fbUser.email,
                                                fbUser.displayName,
                                                selectedRole,
                                                fbUser.organization,
                                                false
                                            )
                                            else -> onFirebaseEmailAuth(
                                                context,
                                                fbUser.email,
                                                "SOC-Shield#2026",
                                                "SOC-Shield#2026",
                                                fbUser.displayName,
                                                fbUser.organization,
                                                selectedRole,
                                                false
                                            )
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            color = Color.White,
                                            shape = CircleShape,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                when (fbUser.authProvider) {
                                                    "google.com" -> Image(
                                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                                        contentDescription = "Google Provider",
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    "facebook.com" -> Image(
                                                        painter = painterResource(id = R.drawable.ic_facebook_logo),
                                                        contentDescription = "Facebook Provider",
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    else -> Icon(
                                                        imageVector = Icons.Filled.Security,
                                                        contentDescription = "Password Provider",
                                                        tint = Color(0xFF0A1128),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = fbUser.displayName,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${fbUser.email} • UID: ${fbUser.uid}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                                color = ElectricCyan,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Synced",
                                            tint = RiskLow,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = fbUser.authProvider,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RiskLow
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // GOOGLE SIGN-IN / SIGN-UP FIREBASE DIALOG
    if (showGoogleAuthDialog) {
        var googleEmail by remember { mutableStateOf("varigondabhavya@gmail.com") }
        var googleName by remember { mutableStateOf("Bhavya Varigonda") }
        var googleOrg by remember { mutableStateOf("Google Workspace • BrandShield SOC") }

        AlertDialog(
            onDismissRequest = { showGoogleAuthDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Icon",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isSignUpMode) "Google Sign-Up -> Firebase" else "Google Sign-In -> Firebase",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Provider: google.com • Syncs to firestore/users",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select a Google Workspace account or enter your Google email to authenticate and sync with the Firebase Database:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Quick Preset Google Account Pill
                    Surface(
                        color = Color(0xFF4285F4).copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF4285F4).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable {
                                googleEmail = "varigondabhavya@gmail.com"
                                googleName = "Bhavya Varigonda"
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Bhavya Varigonda",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "varigondabhavya@gmail.com",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }

                    TextField(
                        value = googleName,
                        onValueChange = { googleName = it },
                        label = { Text("Google Account Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = googleEmail,
                        onValueChange = { googleEmail = it },
                        label = { Text("Google Email Address") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGoogleAuthDialog = false
                        onGoogleFirebaseAuth(
                            context,
                            googleEmail,
                            googleName,
                            selectedRole,
                            googleOrg,
                            isSignUpMode
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("confirm_google_auth_button")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect Google to Firebase")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleAuthDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // FACEBOOK LOGIN / SIGN-UP FIREBASE DIALOG
    if (showFacebookAuthDialog) {
        var fbEmail by remember { mutableStateOf("priya.nair.fb@brandshield.in") }
        var fbName by remember { mutableStateOf("Priya Nair (Facebook Auth)") }
        var fbOrg by remember { mutableStateOf("Meta Threat Intel • BrandShield SOC") }

        AlertDialog(
            onDismissRequest = { showFacebookAuthDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_facebook_logo),
                                contentDescription = "Facebook Icon",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isSignUpMode) "Facebook Sign-Up -> Firebase" else "Facebook Login -> Firebase",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Provider: facebook.com • Syncs to firestore/users",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Authenticate with Facebook OAuth and store your analyst credentials in the Firebase Database:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextField(
                        value = fbName,
                        onValueChange = { fbName = it },
                        label = { Text("Facebook Profile Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = fbEmail,
                        onValueChange = { fbEmail = it },
                        label = { Text("Facebook Email Address") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("facebook_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFacebookAuthDialog = false
                        onFacebookFirebaseAuth(
                            context,
                            fbEmail,
                            fbName,
                            selectedRole,
                            fbOrg,
                            isSignUpMode
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1877F2),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("confirm_facebook_auth_button")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_facebook_logo),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect Facebook to Firebase")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFacebookAuthDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Firebase Auth Password Reset") },
            text = {
                Text(
                    "A Firebase Authentication password reset link and MFA verification token have been dispatched to $email."
                )
            },
            confirmButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Acknowledge")
                }
            }
        )
    }
}
