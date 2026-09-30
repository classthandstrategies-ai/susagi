package com.guardian.app.ui.guardians

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guardian.app.protect.advanced.TrustedContactManager
import com.guardian.app.ui.components.PrimarySafetyAction
import com.guardian.app.ui.components.SecondarySafetyAction
import com.guardian.app.ui.design.SuSagiColors
import com.guardian.app.ui.design.SuSagiShape
import com.guardian.app.ui.design.SuSagiSpacing
import com.guardian.app.ui.design.SuSagiTheme

/**
 * Banking-grade dialog to add, edit, or remove the user's trusted emergency contact.
 * Persists directly using [TrustedContactManager].
 */
@Composable
fun ManageTrustedContactModal(
    existingContact: GuardianUiModel?,
    onDismiss: () -> Unit,
    onContactUpdated: () -> Unit,
    modifier: Modifier = Modifier,
    isHindi: Boolean = false
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existingContact?.name ?: "") }
    var phone by remember { mutableStateOf(existingContact?.phone ?: "") }
    var autoAlertEnabled by remember { mutableStateOf(existingContact?.isEnabled ?: true) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val isEditing = existingContact != null

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = SuSagiColors.Surface,
            titleContentColor = SuSagiColors.TextPrimary,
            textContentColor = SuSagiColors.TextSecondary,
            shape = SuSagiShape.card,
            title = {
                Text(
                    text = if (isHindi) "विश्वसनीय संपर्क हटाएं?" else "Remove Trusted Contact?",
                    style = SuSagiTheme.typography.title,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isHindi)
                        "क्या आप वाकई इस संपर्क को हटाना चाहते हैं? उच्च जोखिम वाले घोटालों के दौरान आपातकालीन SMS नहीं भेजे जा सकेंगे।"
                    else
                        "Are you sure you want to remove this contact? SuSagi will no longer be able to send emergency SMS alerts during high-risk scam calls.",
                    style = SuSagiTheme.typography.bodyMedium,
                    color = SuSagiColors.TextSecondary
                )
            },
            confirmButton = {
                PrimarySafetyAction(
                    text = if (isHindi) "हाँ, हटाएं" else "Yes, Remove",
                    onClick = {
                        TrustedContactManager.clear(context)
                        showDeleteConfirm = false
                        onContactUpdated()
                        onDismiss()
                    },
                    isCritical = true,
                    height = 44.dp
                )
            },
            dismissButton = {
                SecondarySafetyAction(
                    text = if (isHindi) "रद्द करें" else "Cancel",
                    onClick = { showDeleteConfirm = false },
                    height = 44.dp
                )
            }
        )
        return
    }

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
                    text = if (isEditing) {
                        if (isHindi) "विश्वसनीय संपर्क संपादित करें" else "Edit Trusted Contact"
                    } else {
                        if (isHindi) "विश्वसनीय संपर्क जोड़ें" else "Add Trusted Contact"
                    },
                    style = SuSagiTheme.typography.title,
                    color = SuSagiColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isHindi)
                        "आपका विश्वसनीय व्यक्ति सुरक्षा अलर्ट प्राप्त कर सकता है।"
                    else
                        "Your trusted person can receive safety alerts.",
                    style = SuSagiTheme.typography.caption,
                    color = SuSagiColors.TextSecondary
                )

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = { Text(if (isHindi) "संपर्क का नाम" else "Contact Name") },
                    placeholder = { Text(if (isHindi) "उदा. माँ, जीवनसाथी" else "e.g. Mom, Partner") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SuSagiColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SuSagiColors.TextPrimary,
                        unfocusedTextColor = SuSagiColors.TextPrimary,
                        focusedBorderColor = SuSagiColors.Brand,
                        unfocusedBorderColor = SuSagiColors.Border,
                        focusedLabelColor = SuSagiColors.Brand,
                        unfocusedLabelColor = SuSagiColors.TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        validationError = null
                    },
                    label = { Text(if (isHindi) "फ़ोन नंबर" else "Phone Number") },
                    placeholder = { Text("+91 98765 43210") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = SuSagiColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    isError = validationError != null,
                    supportingText = if (validationError != null) {
                        { Text(validationError!!, color = SuSagiColors.RiskCritical) }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SuSagiColors.TextPrimary,
                        unfocusedTextColor = SuSagiColors.TextPrimary,
                        focusedBorderColor = SuSagiColors.Brand,
                        unfocusedBorderColor = SuSagiColors.Border,
                        focusedLabelColor = SuSagiColors.Brand,
                        unfocusedLabelColor = SuSagiColors.TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Auto-Alert Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "ऑटो-अलर्ट सक्षम करें" else "Enable Auto-Alert",
                            style = SuSagiTheme.typography.bodyMedium,
                            color = SuSagiColors.TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isHindi)
                                "सुरक्षा अलर्ट प्राप्त करने की अनुमति दें"
                            else
                                "Allow safety alerts to be sent",
                            style = SuSagiTheme.typography.caption,
                            color = SuSagiColors.TextMuted
                        )
                    }

                    Switch(
                        checked = autoAlertEnabled,
                        onCheckedChange = { autoAlertEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SuSagiColors.TextOnBrand,
                            checkedTrackColor = SuSagiColors.Brand,
                            uncheckedThumbColor = SuSagiColors.TextMuted,
                            uncheckedTrackColor = SuSagiColors.SurfaceElevated
                        )
                    )
                }

                // Delete option if editing
                if (isEditing) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = SuSagiColors.RiskCritical,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = if (isHindi) "संपर्क हटाएं" else "Remove Contact",
                            color = SuSagiColors.RiskCritical,
                            style = SuSagiTheme.typography.caption,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            PrimarySafetyAction(
                text = if (isHindi) "सुरक्षित करें" else "Save Contact",
                onClick = {
                    val cleanPhone = phone.trim()
                    if (cleanPhone.length < 5) {
                        validationError = if (isHindi)
                            "कृपया मान्य फ़ोन नंबर दर्ज करें"
                        else
                            "Please enter a valid phone number"
                        return@PrimarySafetyAction
                    }

                    TrustedContactManager.save(
                        context,
                        TrustedContactManager.TrustedContact(
                            name = name.trim().ifBlank { "Trusted Contact" },
                            phone = cleanPhone,
                            enabled = autoAlertEnabled
                        )
                    )
                    onContactUpdated()
                    onDismiss()
                },
                height = 44.dp
            )
        },
        dismissButton = {
            SecondarySafetyAction(
                text = if (isHindi) "रद्द करें" else "Cancel",
                onClick = onDismiss,
                height = 44.dp
            )
        }
    )
}
