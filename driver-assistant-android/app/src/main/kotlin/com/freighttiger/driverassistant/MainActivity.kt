package com.freighttiger.driverassistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.freighttiger.driverassistant.runtime.PromptPresenter
import com.freighttiger.driverassistant.ui.navigation.DriverAssistantNavHost
import com.freighttiger.driverassistant.ui.navigation.PromptOpen
import com.freighttiger.driverassistant.ui.theme.FtTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var presenter: PromptPresenter

    private val intentPrompt = MutableStateFlow<PromptOpen?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        consume(intent)
        setContent {
            FtTheme {
                DriverAssistantNavHost(
                    openRequests = presenter.openRequests,
                    intentPrompt = intentPrompt,
                    onIntentConsumed = { intentPrompt.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consume(intent)
    }

    private fun consume(intent: Intent?) {
        val promptId = intent?.getStringExtra(EXTRA_PROMPT_ID) ?: return
        intentPrompt.value = PromptOpen(promptId, intent.getBooleanExtra(EXTRA_USER_INITIATED, false))
        intent.removeExtra(EXTRA_PROMPT_ID)
        presenter.dismissNotification(promptId)
    }

    companion object {
        private const val EXTRA_PROMPT_ID = "ftda.prompt_id"
        private const val EXTRA_USER_INITIATED = "ftda.user_initiated"

        fun promptIntent(context: Context, promptId: String, userInitiated: Boolean): Intent =
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_PROMPT_ID, promptId)
                .putExtra(EXTRA_USER_INITIATED, userInitiated)
    }
}
