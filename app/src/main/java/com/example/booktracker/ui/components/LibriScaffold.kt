package com.example.booktracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.booktracker.ui.theme.Libri
import com.example.booktracker.ui.theme.LibriType

/** One entry of the bottom navigation bar. */
data class LibriNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * The bottom bar from the designs: 80dp tall, rounded top, Paper White, and an
 * active tab rendered as an amber pill rather than the Material indicator.
 */
@Composable
fun LibriBottomBar(
    items: List<LibriNavItem>,
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                ambientColor = Libri.Primary,
                spotColor = Libri.Primary
            )
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(Libri.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(80.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                LibriNavTab(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = { onSelect(item.route) }
                )
            }
        }
    }
}

@Composable
private fun LibriNavTab(
    item: LibriNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) Libri.OnSecondaryContainer else Libri.OnSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) Libri.SecondaryContainer else Libri.Surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = item.label,
            style = LibriType.labelSm,
            color = contentColor,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Top bar from the designs: a centred Libre Caslon title flanked by optional
 * outlined icon buttons, on Paper White with a whisper of ambient shadow.
 */
@Composable
fun LibriTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    navigationContentDescription: String? = null,
    actionIcon: ImageVector? = null,
    onActionClick: () -> Unit = {},
    actionContentDescription: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Libri.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopBarSlot(
                icon = navigationIcon,
                contentDescription = navigationContentDescription,
                onClick = onNavigationClick
            )
            Text(
                text = title,
                style = LibriType.headlineMd,
                color = Libri.Primary,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                maxLines = 1
            )
            TopBarSlot(
                icon = actionIcon,
                contentDescription = actionContentDescription,
                onClick = onActionClick
            )
        }
    }
}

/** Keeps the title optically centred whether or not a slot holds an icon. */
@Composable
private fun TopBarSlot(
    icon: ImageVector?,
    contentDescription: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Libri.OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/** Serif section heading — "In Progress", "To Read", "Recently Added". */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = LibriType.headlineLgMobile,
            color = Libri.Primary
        )
        trailing?.invoke()
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(com.example.booktracker.R.string.app_name),
            style = LibriType.headlineMd,
            color = Libri.OutlineVariant
        )
        Text(
            text = message,
            style = LibriType.bodyMd,
            color = Libri.OnSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
