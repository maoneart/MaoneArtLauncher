package id.maoneart.flow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import id.maoneart.flow.ui.home.HomeScreen
import id.maoneart.flow.ui.home.HomeViewModel
import id.maoneart.flow.ui.theme.MaoneArtFlowTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            // Custom back press handler: if search query is active, back key clears search
            BackHandler(enabled = uiState.isSearching) {
                viewModel.onSearchQueryChange("")
            }

            MaoneArtFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
