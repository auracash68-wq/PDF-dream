package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.SweetPdfViewModel
import com.example.ui.components.SweetPdfBottomBar
import com.example.ui.components.SweetPdfHeader
import com.example.ui.dialogs.PdfViewerDialog
import com.example.ui.dialogs.ToolExecutionDialog
import com.example.ui.screens.AllToolsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.SweetPdfTheme

class MainActivity : ComponentActivity() {
  private val viewModel: SweetPdfViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SweetPdfTheme {
        SweetPdfApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun SweetPdfApp(viewModel: SweetPdfViewModel = viewModel()) {
  val isWelcomeDismissed by viewModel.isWelcomeDismissed.collectAsState()
  val currentTab by viewModel.currentTab.collectAsState()
  val activeTool by viewModel.activeTool.collectAsState()
  val activeViewerDoc by viewModel.activeViewerDoc.collectAsState()
  val userMessage by viewModel.userMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(userMessage) {
    userMessage?.let { msg ->
      snackbarHostState.showSnackbar(
        message = msg,
        duration = SnackbarDuration.Short
      )
      viewModel.clearMessage()
    }
  }

  if (!isWelcomeDismissed) {
    WelcomeScreen(
      onGetStarted = { viewModel.dismissWelcome() }
    )
  } else {
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .testTag("sweet_pdf_main_scaffold"),
      topBar = {
        SweetPdfHeader(
          onSearchClick = { viewModel.selectTab(AppTab.ALL_TOOLS) },
          onQuickActionsClick = { viewModel.selectTab(AppTab.ALL_TOOLS) },
          onProfileClick = { viewModel.selectTab(AppTab.SETTINGS) }
        )
      },
      bottomBar = {
        SweetPdfBottomBar(
          currentTab = currentTab,
          onTabSelected = { viewModel.selectTab(it) }
        )
      },
      snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
      ) {
        Crossfade(targetState = currentTab, label = "tab_transition") { tab ->
          when (tab) {
            AppTab.HOME -> HomeScreen(viewModel = viewModel)
            AppTab.ALL_TOOLS -> AllToolsScreen(viewModel = viewModel)
            AppTab.HISTORY -> HistoryScreen(viewModel = viewModel)
            AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
          }
        }
      }
    }

    // Interactive Tool Dialog
    activeTool?.let { tool ->
      ToolExecutionDialog(
        tool = tool,
        viewModel = viewModel,
        onDismiss = { viewModel.closeTool() }
      )
    }

    // In-App PDF Viewer
    activeViewerDoc?.let { doc ->
      PdfViewerDialog(
        doc = doc,
        viewModel = viewModel,
        onClose = { viewModel.closeViewer() }
      )
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  SweetPdfTheme { Greeting("Android") }
}
