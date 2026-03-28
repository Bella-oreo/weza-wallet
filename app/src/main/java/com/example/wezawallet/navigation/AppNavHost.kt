package com.example.wezawallet.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Import the Screens - If these stay red, it means the SCREEN files have internal errors
import com.example.wezawallet.screens.splash.SplashScreen
import com.example.wezawallet.screens.onboarding.OnboardingScreen
import com.example.wezawallet.screens.login.LoginScreen
import com.example.wezawallet.screens.home.HomeScreen
import com.example.wezawallet.screens.addmoney.AddMoneyScreen
import com.example.wezawallet.screens.sendmoney.SendMoneyScreen
import com.example.wezawallet.screens.expense.ExpenseScreen
import com.example.wezawallet.screens.goals.GoalScreen
import com.example.wezawallet.screens.profile.ProfileScreen
import com.example.wezawallet.screens.history.TransactionHistoryScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        // 1. Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(onTimeout = {
                navController.navigate(Routes.ONBOARDING) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        // 2. Onboarding
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onGetStartedClick = {
                navController.navigate(Routes.LOGIN)
            })
        }

        // 3. Login
        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }

        // 4. Dashboard (Home)
        composable(Routes.HOME) {
            HomeScreen(
                onExpenseClick = { navController.navigate(Routes.EXPENSES) },
                onGoalClick = { navController.navigate(Routes.GOALS) },
                onAddMoneyClick = { navController.navigate(Routes.ADD_MONEY) },
                onSendMoneyClick = { navController.navigate(Routes.SEND_MONEY) },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onViewAllClick = { navController.navigate(Routes.HISTORY) }
            )
        }

        // 5. Functional Screens
        composable(Routes.ADD_MONEY) {
            AddMoneyScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SEND_MONEY) {
            SendMoneyScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.EXPENSES) {
            ExpenseScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.GOALS) {
            GoalScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.HISTORY) {
            TransactionHistoryScreen(onBack = { navController.popBackStack() })
        }

        // 6. Profile & Logout
        composable(Routes.PROFILE) {
            ProfileScreen(
                onHistoryClick = {
                    navController.navigate(Routes.HISTORY)
                },
                onLogoutClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}