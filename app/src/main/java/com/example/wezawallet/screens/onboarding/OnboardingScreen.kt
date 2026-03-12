package com.example.wezawallet.screens.onboarding


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wezawallet.R

@Composable
fun OnboardingScreen(onGetStartedClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title from reference image
        Text(
            text = "Welcome to\nWeza Wallet",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E), // Deep navy blue
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Illustration Placeholder
        // Replace R.drawable.onboarding_ill with your actual image resource
        Box(
            modifier = Modifier.size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            // If you don't have the image yet, use a simple colored box for now
            Surface(
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Illustration", color = Color(0xFF2F80ED))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Subtitle from reference image
        Text(
            text = "Smart Saving & Spending\nfor Your Future",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // The Primary "Get Started" Button
        Button(
            onClick = { onGetStartedClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2F80ED) // Signature Blue
            )
        ) {
            Text(
                text = "Get Started",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary "Learn More" Button
        TextButton(
            onClick = { /* Optional: Navigate to Info */ },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                text = "Learn More",
                fontSize = 16.sp,
                color = Color(0xFF2F80ED)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    MaterialTheme {
        OnboardingScreen()
    }
}