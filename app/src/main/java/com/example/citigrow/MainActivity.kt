package com.example.citigrow
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.citigrow.ui.CitiGrowApp
import com.example.citigrow.ui.theme.CitiGrowTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CitiGrowTheme { CitiGrowApp() } }
    }
}
