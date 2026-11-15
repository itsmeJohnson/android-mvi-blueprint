package com.mvi.blueprint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mvi.blueprint.feature.orderdashboard.ui.OrderDashboardRoute
import com.mvi.blueprint.ui.theme.MviBlueprintTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MviBlueprintTheme {
                OrderDashboardRoute()
            }
        }
    }
}
