package com.guardian.app.ui.guardians

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guardian.app.ui.components.SecondarySafetyAction
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiTheme
import com.guardian.app.verification.TrustedContactRelationship

/**
 * Focused Product Guardian selection modal when multiple canonical Guardians are available.
 *
 * Rules:
 * - Displays human-facing identity (displayName, relationship)
 * - Never exposes UUIDs as primary UI
 * - Requires explicit user selection; no silent fallback to contacts[0]
 */
@Composable
fun GuardianSelectionDialog(
    guardians: List<TrustedContactRelationship>,
    onSelectGuardian: (TrustedContactRelationship) -> Unit,
    onDismiss: () -> Unit,
    isHindi: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SuSagiColors.Surface,
        titleContentColor = SuSagiColors.TextPrimary,
        textContentColor = SuSagiColors.TextSecondary,
        shape = SuSagiShape.card,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = SuSagiColors.Brand,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isHindi) "अभिभावक चुनें" else "Select Guardian",
                    style = SuSagiTheme.typography.title,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isHindi)
                        "इस कॉल की पहचान की पुष्टि करने के लिए एक विश्वसनीय अभिभावक चुनें:"
                    else
                        "Choose a trusted Guardian to verify this caller's identity:",
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextSecondary
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(guardians) { guardian ->
                        Surface(
                            shape = SuSagiShape.sm,
                            color = SuSagiColors.SurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectGuardian(guardian) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = SuSagiColors.Brand,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = guardian.displayName,
                                        style = SuSagiTheme.typography.bodyLarge,
                                        color = SuSagiColors.TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (guardian.relationship.isNotBlank()) {
                                        Text(
                                            text = guardian.relationship,
                                            style = SuSagiTheme.typography.caption,
                                            color = SuSagiColors.TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            SecondarySafetyAction(
                text = if (isHindi) "रद्द करें" else "Cancel",
                onClick = onDismiss,
                height = 40.dp
            )
        }
    )
}
