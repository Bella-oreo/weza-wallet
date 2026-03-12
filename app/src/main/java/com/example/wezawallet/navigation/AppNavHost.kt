package com.example.wezawallet.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wezawallet.screens.splash.SplashScreen
import com.example.wezawallet.screens.onboarding.OnboardingScreen
import com.example.wezawallet.screens.login.LoginScreen
import com.example.wezawallet.screens.home.HomeScreen
import com.example.wezawallet.screens.addmoney.AddMoneyScreen
import com.example.wezawallet.screens.sendmoney.SendMoneyScreen
import com.example.wezawallet.screens.expense.ExpenseScreen
import com.example.wezawallet.screens.goals.GoalScreen
import com.example.wezawallet.screens.profile.ProfileScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onTimeout = {
                navController.navigate(Routes.ONBOARDING) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(onGetStartedClick = {
                navController.navigate(Routes.LOGIN)
            })
        }

        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            HomeScreen(
                onExpenseClick = { navController.navigate(Routes.EXPENSES) },
                onGoalClick = { navController.navigate(Routes.GOALS) },
                onAddMoneyClick = { navController.navigate(Routes.ADD_MONEY) },
                onSendMoneyClick = { navController.navigate(Routes.SEND_MONEY) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.ADD_MONEY) {
            AddMoneyScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SEND_MONEY) {
            // FIXED: Changed onBackClick to onBack
            SendMoneyScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.EXPENSES) {
            ExpenseScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.GOALS) {
            GoalScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onHistoryClick = {
                    // This will take them to your transaction history/expenses
                    navController.navigate(Routes.EXPENSES)
                },
                onLogoutClick = {
                    navController.navigate(Routes.LOGIN) {
                        // This clears the 'Home' and 'Profile' from the backstack
                        // so they can't 'Back' button their way back in after logging out
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

    }
}