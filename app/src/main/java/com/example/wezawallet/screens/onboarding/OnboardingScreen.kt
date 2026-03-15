package com.example.wezawallet.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
        // Title
        Text(
            text = "Welcome to\nWeza Wallet",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E),
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Updated Illustration Section
        Surface(
            color = Color(0xFFE3F2FD), // Light blue background behind the image
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.size(280.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding),
                contentDescription = "Weza Wallet Onboarding Illustration",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // Space inside the card
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Subtitle
        Text(
            text = "Smart Saving & Spending\nfor Your Future",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // "Get Started" Button
        Button(
            onClick = onGetStartedClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2F80ED)
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

        // "Learn More" Button
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