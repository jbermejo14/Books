package com.bolskapps.libri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bolskapps.libri.R
import com.bolskapps.libri.data.GOAL_PRESETS
import com.bolskapps.libri.data.MAX_GOAL_BOOKS
import com.bolskapps.libri.ui.theme.Libri
import com.bolskapps.libri.ui.theme.LibriType

/** Sets or clears the yearly reading goal. Presets for speed, a field for anything else. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSheet(
    year: Int,
    currentTarget: Int?,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
    onClear: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var target by rememberSaveable(currentTarget) {
        mutableStateOf(currentTarget?.toString() ?: DEFAULT_TARGET.toString())
    }

    val targetValue = target.toIntOrNull() ?: 0
    val canSave = targetValue in 1..MAX_GOAL_BOOKS

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Libri.SurfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.goal_sheet_title, year),
                style = LibriType.headlineMd,
                color = Libri.Primary
            )
            Text(
                text = stringResource(R.string.goal_sheet_subtitle),
                style = LibriType.bodyMd,
                color = Libri.OnSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GOAL_PRESETS.forEach { preset ->
                    PresetChip(
                        value = preset,
                        selected = targetValue == preset,
                        onClick = { target = preset.toString() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            LibriBoxedField(
                value = target,
                onValueChange = { target = it.filter(Char::isDigit).take(3) },
                label = stringResource(R.string.goal_books_label),
                isError = target.isNotBlank() && !canSave,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )

            LibriPrimaryButton(
                text = stringResource(R.string.goal_save),
                enabled = canSave,
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    onSave(targetValue)
                    onDismiss()
                }
            )

            if (currentTarget != null) {
                TextButton(
                    onClick = {
                        onClear()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.goal_remove),
                        style = LibriType.labelMd,
                        color = Libri.Error
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    value: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) Libri.SecondaryContainer else Libri.Surface)
            .then(if (selected) Modifier else Modifier.border(1.dp, Libri.OutlineVariant, shape))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            style = LibriType.labelMd,
            color = if (selected) Libri.OnSecondaryContainer else Libri.OnSurface
        )
    }
}

private const val DEFAULT_TARGET = 12
