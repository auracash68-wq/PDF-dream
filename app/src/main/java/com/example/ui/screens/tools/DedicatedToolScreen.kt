package com.example.ui.screens.tools

import androidx.compose.runtime.Composable
import com.example.data.model.PdfTool
import com.example.data.model.ToolCategory
import com.example.ui.SweetPdfViewModel

@Composable
fun DedicatedToolScreen(
    tool: PdfTool,
    viewModel: SweetPdfViewModel,
    onDismiss: () -> Unit
) {
    when (tool.category) {
        ToolCategory.ORGANIZE -> {
            Category1OrganizeToolScreen(
                tool = tool,
                viewModel = viewModel,
                onBack = onDismiss
            )
        }
        ToolCategory.VIEWING -> {
            Category2ViewingToolScreen(
                tool = tool,
                viewModel = viewModel,
                onBack = onDismiss
            )
        }
        ToolCategory.MARKUP -> {
            Category3MarkupToolScreen(
                tool = tool,
                viewModel = viewModel,
                onBack = onDismiss
            )
        }
        ToolCategory.SCAN_CV -> {
            Category4ScanCvToolScreen(
                tool = tool,
                viewModel = viewModel,
                onBack = onDismiss
            )
        }
        else -> {
            Category567ToolScreen(
                tool = tool,
                viewModel = viewModel,
                onBack = onDismiss
            )
        }
    }
}
