package com.d4rk.netprobe.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.d4rk.netprobe.R
import com.d4rk.netprobe.data.datastore.DataStore
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun LanguageDialog(
    dataStore: DataStore,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf("") }
    val languageEntries = stringArrayResource(R.array.preference_language_entries)
    val languageValues = stringArrayResource(R.array.preference_language_values)// Fetch initial language preference when the dialog is first displayed
    LaunchedEffect(Unit) {
        selectedLanguage = dataStore.getLanguage().firstOrNull() ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            LanguageDialogContent(
                selectedLanguage,
                languageEntries,
                languageValues,
                onLanguageSelected = { newLanguage ->
                    selectedLanguage = newLanguage
                }
            )},
        icon = {
            Icon(Icons.Outlined.Language, contentDescription = "Language Icon") // Added content description
        },
        confirmButton = {
            TextButton(onClick = {
                onLanguageSelected(selectedLanguage)
                onDismiss()
            }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
fun LanguageDialogContent(
    selectedLanguage: String,
    languageEntries: Array<String>,
    languageValues: Array<String>,
    onLanguageSelected: (String) -> Unit
) {
    Column {
        Text(stringResource(id = R.string.dialog_language_subtitle))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn {
                itemsIndexed(languageEntries) { index, entry ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {onLanguageSelected(languageValues[index]) }, // Make the whole row clickable
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        RadioButton(
                            selected = selectedLanguage == languageValues[index],
                            onClick = null // Radio button selection is handled by row click
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = entry,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { // Align info icon and text
            Icon(imageVector = Icons.Outlined.Info, contentDescription = "Info Icon")
            Spacer(modifier = Modifier.width(8.dp)) // Add space between icon and text
            Text(stringResource(id = R.string.dialog_info_language))
        }
    }
}