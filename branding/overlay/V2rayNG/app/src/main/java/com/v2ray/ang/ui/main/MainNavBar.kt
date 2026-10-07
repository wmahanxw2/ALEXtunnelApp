package com.v2ray.ang.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R

/** Bottom navigation entries of the ALEX Tunnel main screen. */
internal enum class MainNavItem(@DrawableRes val iconRes: Int, @StringRes val labelRes: Int) {
    Home(R.drawable.ic_alex_home, R.string.alex_nav_home),
    Configs(R.drawable.ic_alex_cloud, R.string.alex_nav_configs),
    Subs(R.drawable.ic_alex_folder, R.string.alex_nav_subs),
    Logs(R.drawable.ic_alex_info, R.string.alex_nav_logs),
    More(R.drawable.ic_alex_more, R.string.alex_nav_more),
}

internal val AlexPurple = Color(0xFFA855F7)
internal val AlexPurpleDeep = Color(0xFF7C3AED)
internal val AlexLavender = Color(0xFFC4B5FD)

@Composable
internal fun MainNavBar(
    selected: MainNavItem,
    onSelect: (MainNavItem) -> Unit,
) {
    // The design is a fixed left-to-right layout (Home ... More), also for Persian.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        val shape = RoundedCornerShape(30.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1B0F38), Color(0xFF0F0723))),
                    shape
                )
                .border(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(AlexPurpleDeep, AlexPurple, AlexPurpleDeep)),
                    shape
                )
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainNavItem.entries.forEach { item ->
                    NavEntry(
                        item = item,
                        isSelected = item == selected,
                        onClick = { onSelect(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavEntry(
    item: MainNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemShape = RoundedCornerShape(22.dp)
    val tint = if (isSelected) Color.White else AlexLavender
    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .then(
                if (isSelected) {
                    Modifier
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF3B1A78), Color(0xFF241047))),
                            itemShape
                        )
                        .border(BorderStroke(1.5.dp, AlexPurple), itemShape)
                } else Modifier
            )
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(item.iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = stringResource(item.labelRes),
            color = tint,
            fontSize = 13.sp,
            maxLines = 1,
        )
    }
}

/** Start / stop button shown on the Configs tab (the Home tab has the big power button). */
@Composable
internal fun MainStartStopFab(isRunning: Boolean, onToggle: () -> Unit) {
    FloatingActionButton(
        onClick = onToggle,
        containerColor = if (isRunning) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
    ) {
        Icon(
            painter = painterResource(if (isRunning) R.drawable.ic_stop_24dp else R.drawable.ic_play_24dp),
            contentDescription = stringResource(if (isRunning) R.string.acc_stop else R.string.acc_start),
            modifier = Modifier.size(24.dp)
        )
    }
}
