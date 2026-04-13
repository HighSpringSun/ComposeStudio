package composestudio.ui.codegen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composestudio.model.DesignState
import composestudio.ui.theme.StudioColors
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/**
 * Panel that displays generated Compose code from the current design.
 */
@Composable
fun CodeGenerationPanel(
    designState: DesignState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val generatedCode = remember(designState.components.size, designState.components.values.toList()) {
        CodeGenerator.generate(designState)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioColors.Surface)
            .padding(12.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Generated Code",
                color = StudioColors.OnSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(StringSelection(generatedCode), null)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioColors.Primary
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.height(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Copy", fontSize = 11.sp)
            }

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = StudioColors.OnSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Code display
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(StudioColors.Background)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = generatedCode,
                color = StudioColors.OnSurface,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}
