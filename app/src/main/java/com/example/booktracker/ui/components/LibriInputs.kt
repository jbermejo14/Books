package com.example.booktracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.booktracker.data.ReadingStatus
import com.example.booktracker.ui.theme.Libri
import com.example.booktracker.ui.theme.LibriType

/**
 * DESIGN.md, Input Fields: "Minimalist style with only a bottom border of 1px in a
 * neutral tone, which thickens and changes to Ink Blue on focus."
 */
@Composable
fun LibriUnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = LibriType.bodyLg) },
            textStyle = LibriType.bodyLg,
            singleLine = true,
            isError = isError,
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Libri.SurfaceContainerLowest,
                unfocusedContainerColor = Libri.SurfaceContainerLowest,
                errorContainerColor = Libri.SurfaceContainerLowest,
                focusedIndicatorColor = Libri.Primary,
                unfocusedIndicatorColor = Libri.OnSurfaceVariant,
                errorIndicatorColor = Libri.Error,
                focusedTextColor = Libri.OnSurface,
                unfocusedTextColor = Libri.OnSurface,
                cursorColor = Libri.Primary,
                focusedPlaceholderColor = Libri.OutlineVariant,
                unfocusedPlaceholderColor = Libri.OutlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = LibriType.labelSm,
                color = if (isError) Libri.Error else Libri.OnSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/** Boxed variant used inside the "Update Progress" card, per that design. */
@Composable
fun LibriBoxedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = LibriType.labelSm,
            color = Libri.OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LibriType.bodyMd,
            singleLine = true,
            isError = isError,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Libri.SurfaceContainerLow,
                unfocusedContainerColor = Libri.SurfaceContainerLow,
                errorContainerColor = Libri.SurfaceContainerLow,
                focusedBorderColor = Libri.Primary,
                unfocusedBorderColor = Libri.SurfaceVariant,
                errorBorderColor = Libri.Error,
                focusedTextColor = Libri.Primary,
                unfocusedTextColor = Libri.Primary,
                cursorColor = Libri.Primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * The "Reading Status" segmented control: a 2x2 grid on phones, with the selected
 * cell filled Ink Blue.
 */
@Composable
fun LibriSegmentedStatus(
    selected: ReadingStatus,
    onSelect: (ReadingStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Libri.SurfaceContainer)
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReadingStatus.entries.chunked(2).forEach { rowStatuses ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowStatuses.forEach { status ->
                    SegmentCell(
                        label = statusLabel(status),
                        selected = status == selected,
                        onClick = { onSelect(status) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentCell(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Libri.Primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = LibriType.labelMd,
            color = if (selected) Libri.OnPrimary else Libri.OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** "Select Genre" dropdown, styled to match the underline fields beside it. */
@Composable
fun LibriGenreDropdown(
    selected: String?,
    options: List<String>,
    placeholder: String,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selected ?: placeholder,
                    style = LibriType.bodyLg,
                    color = if (selected == null) Libri.OutlineVariant else Libri.OnSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = Libri.OnSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Libri.OnSurfaceVariant)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = LibriType.bodyMd) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
            if (selected != null) {
                DropdownMenuItem(
                    text = { Text(placeholder, style = LibriType.bodyMd, color = Libri.OnSurfaceVariant) },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Multi-line review field, boxed like the progress inputs it sits beside. */
@Composable
fun LibriNotesField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = LibriType.labelSm,
            color = Libri.OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = LibriType.bodyMd, color = Libri.OutlineVariant) },
            textStyle = LibriType.bodyMd,
            minLines = 3,
            maxLines = 8,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Libri.SurfaceContainerLow,
                unfocusedContainerColor = Libri.SurfaceContainerLow,
                focusedBorderColor = Libri.Primary,
                unfocusedBorderColor = Libri.SurfaceVariant,
                focusedTextColor = Libri.OnSurface,
                unfocusedTextColor = Libri.OnSurface,
                cursorColor = Libri.Primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Ink Blue pill — the primary call to action across the designs. */
@Composable
fun LibriPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(percent = 50),
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Libri.Primary,
            contentColor = Libri.OnPrimary,
            disabledContainerColor = Libri.SurfaceContainerHighest,
            disabledContentColor = Libri.Outline
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(text = text, style = LibriType.labelMd)
    }
}
