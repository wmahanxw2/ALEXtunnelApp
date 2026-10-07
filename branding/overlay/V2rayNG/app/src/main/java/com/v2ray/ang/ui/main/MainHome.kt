package com.v2ray.ang.ui.main

import android.net.TrafficStats
import android.os.Process
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R
import com.v2ray.ang.dto.GroupMapItem
import com.v2ray.ang.extension.nullIfBlank
import com.v2ray.ang.extension.toTrafficString
import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val CardBorder = Brush.linearGradient(listOf(AlexPurpleDeep, AlexPurple, AlexPurpleDeep))
private val TileColor = Color(0xFF160B30)
private val TileBorder = Color(0xFF3A2766)
private val PingGreen = Color(0xFF22C55E)
private const val SUPPORT_URL = "https://t.me/alexsupportsell"

private data class ActiveConfigInfo(
    val remarks: String,
    val description: String,
    val type: String,
    val delayMillis: Long,
)

private data class SessionTraffic(val rx: Long = 0L, val tx: Long = 0L)

/**
 * ALEX Tunnel home dashboard: logo, power button, status, active configuration with
 * ping / downlink / uplink. All actions go through [MainAction] via the callbacks.
 */
@Composable
internal fun MainHomePage(
    isRunning: Boolean,
    status: MainStatus,
    statusText: String,
    selectedGuid: String?,
    groups: List<GroupMapItem>,
    onToggle: () -> Unit,
    onTest: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenConfigs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The design is a fixed left-to-right layout, also when the app language is Persian.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier = modifier.fillMaxSize()) {
            HomeWaves()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeHeader(onOpenMenu = onOpenMenu)
                Spacer(Modifier.height(20.dp))
                PowerButton(isRunning = isRunning, onToggle = onToggle)
                Spacer(Modifier.height(14.dp))
                StatusPill(isRunning = isRunning, onTest = onTest)
                if (isRunning) {
                    Text(
                        text = statusText,
                        color = AlexLavender,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
                    )
                }
                Spacer(Modifier.height(22.dp))
                ActiveConfigCard(
                    isRunning = isRunning,
                    status = status,
                    selectedGuid = selectedGuid,
                    groups = groups,
                    onTest = onTest,
                    onOpenConfigs = onOpenConfigs
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 26.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(AlexPurpleDeep.copy(alpha = 0.6f))
                    )
                    Text(
                        text = stringResource(R.string.alex_stay_connected),
                        color = AlexPurple,
                        fontSize = 14.sp,
                        letterSpacing = 4.sp
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(AlexPurpleDeep.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(onOpenMenu: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        SquareIconButton(
            iconRes = R.drawable.ic_alex_menu,
            description = stringResource(R.string.alex_acc_menu),
            onClick = onOpenMenu
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_alex_crown),
                contentDescription = null,
                tint = AlexPurple,
                modifier = Modifier.size(34.dp)
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stringResource(R.string.alex_brand_alex),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        shadow = Shadow(AlexPurple, Offset.Zero, 28f)
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.alex_brand_tunnel),
                    style = TextStyle(
                        color = Color(0xFFE9D5FF),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Light,
                        shadow = Shadow(AlexPurple, Offset.Zero, 20f)
                    )
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TagText(R.string.alex_tag_fast)
                Text("•", color = AlexPurple, fontSize = 12.sp)
                TagText(R.string.alex_tag_secure)
                Text("•", color = AlexPurple, fontSize = 12.sp)
                TagText(R.string.alex_tag_free)
            }
        }
        SquareIconButton(
            iconRes = R.drawable.ic_alex_crown,
            description = stringResource(R.string.alex_acc_support),
            onClick = { runCatching { uriHandler.openUri(SUPPORT_URL) } }
        )
    }
}

@Composable
private fun TagText(resId: Int) {
    Text(text = stringResource(resId), color = Color(0xFFBFA9E8), fontSize = 15.sp)
}

@Composable
private fun SquareIconButton(@DrawableRes iconRes: Int, description: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF2A1558), Color(0xFF170B33))))
            .border(1.dp, AlexPurpleDeep, shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            tint = AlexLavender,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun HomeWaves() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        fun wave(mirror: Boolean, yOffset: Float, alpha: Float) {
            fun x(v: Float) = if (mirror) w - v else v
            val path = Path()
            path.moveTo(x(0f), h * 0.15f + yOffset)
            path.cubicTo(
                x(w * 0.22f), h * 0.19f + yOffset,
                x(w * 0.20f), h * 0.33f + yOffset,
                x(w * 0.46f), h * 0.32f + yOffset
            )
            drawPath(path, color = AlexPurple.copy(alpha = alpha), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        }
        for (i in 0..5) {
            val offset = i * 9.dp.toPx()
            val alpha = 0.32f - i * 0.045f
            wave(false, offset, alpha)
            wave(true, offset, alpha)
        }
    }
}

@Composable
private fun PowerButton(isRunning: Boolean, onToggle: () -> Unit) {
    val accent by animateColorAsState(
        targetValue = if (isRunning) Color(0xFF34D399) else AlexPurple,
        label = "powerAccent"
    )
    val pulse by rememberInfiniteTransition(label = "powerPulse").animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "powerPulseValue"
    )
    val description = stringResource(
        if (isRunning) R.string.alex_acc_power_off else R.string.alex_acc_power_on
    )
    Box(modifier = Modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = center
            val r = size.minDimension / 2f
            val glowAlpha = if (isRunning) pulse else 0.3f
            drawCircle(
                brush = Brush.radialGradient(listOf(accent.copy(alpha = glowAlpha), Color.Transparent), center = c, radius = r),
                radius = r,
                center = c
            )
            drawCircle(color = accent.copy(alpha = 0.5f), radius = r * 0.88f, center = c, style = Stroke(width = 2.dp.toPx()))
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFF1C0E3A), Color(0xFF0A0418)), center = c, radius = r * 0.80f),
                radius = r * 0.80f,
                center = c
            )
            drawCircle(
                brush = Brush.sweepGradient(listOf(accent, Color(0xFFF5F3FF), accent), center = c),
                radius = r * 0.74f,
                center = c,
                style = Stroke(width = 5.dp.toPx())
            )
            drawCircle(color = Color(0xFF150A2E), radius = r * 0.58f, center = c)

            // power symbol: open ring + vertical bar
            val iconColor = if (isRunning) Color(0xFFA7F3D0) else Color(0xFFD8B4FE)
            val ir = r * 0.27f
            val strokeWidth = 6.dp.toPx()
            drawArc(
                color = iconColor,
                startAngle = -55f,
                sweepAngle = 290f,
                useCenter = false,
                topLeft = Offset(c.x - ir, c.y - ir),
                size = Size(ir * 2f, ir * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawLine(
                color = iconColor,
                start = Offset(c.x, c.y - ir * 1.15f),
                end = Offset(c.x, c.y - ir * 0.1f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .semantics { contentDescription = description }
                .clickable(role = Role.Button, onClick = onToggle)
        )
    }
}

@Composable
private fun StatusPill(isRunning: Boolean, onTest: () -> Unit) {
    val color = if (isRunning) Color(0xFF4ADE80) else Color(0xFFC084FC)
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(Color(0xFF160B30))
            .border(1.dp, AlexPurpleDeep.copy(alpha = 0.7f), shape)
            .clickable(role = Role.Button, onClick = onTest)
            .padding(horizontal = 28.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(12.dp).background(color, CircleShape))
        Text(
            text = stringResource(
                if (isRunning) R.string.alex_status_connected else R.string.alex_status_disconnected
            ),
            color = color,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ActiveConfigCard(
    isRunning: Boolean,
    status: MainStatus,
    selectedGuid: String?,
    groups: List<GroupMapItem>,
    onTest: () -> Unit,
    onOpenConfigs: () -> Unit,
) {
    // Re-read the selected profile when the selection, a test result or the server list changes.
    val info by produceState<ActiveConfigInfo?>(null, selectedGuid, status, groups) {
        value = withContext(Dispatchers.IO) { loadActiveConfig(selectedGuid) }
    }
    val traffic by rememberSessionTraffic(isRunning)
    val liveDelay = (status as? MainStatus.ConnectionTest)?.result?.delayMillis
        ?.takeIf { isRunning && it > 0L }
    val delay = liveDelay ?: info?.delayMillis ?: 0L

    val cardShape = RoundedCornerShape(28.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A0E36), Color(0xFF0E0722))), cardShape)
            .border(1.5.dp, CardBorder, cardShape)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_alex_bolt),
                contentDescription = null,
                tint = AlexPurple,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = stringResource(R.string.alex_active_config),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        val rowShape = RoundedCornerShape(22.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(rowShape)
                .background(TileColor)
                .border(1.dp, TileBorder, rowShape)
                .clickable(role = Role.Button, onClick = onOpenConfigs)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.linearGradient(listOf(AlexPurple, AlexPurpleDeep)),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_alex_doc),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val current = info
                if (current == null) {
                    Text(
                        text = stringResource(R.string.alex_no_config),
                        color = AlexLavender,
                        fontSize = 15.sp
                    )
                } else {
                    Text(
                        text = current.remarks,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = current.description,
                            color = Color(0xFFB7A9D6),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = current.type, color = AlexLavender, fontSize = 14.sp, maxLines = 1)
                    }
                    if (delay > 0L) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_alex_bolt),
                                contentDescription = null,
                                tint = PingGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.server_test_delay_value, delay),
                                color = PingGreen,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_alex_chevron),
                contentDescription = null,
                tint = AlexLavender,
                modifier = Modifier.size(28.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(TileBorder)
            )
            val pillShape = RoundedCornerShape(50)
            Text(
                text = stringResource(R.string.alex_ping),
                color = Color(0xFFD8B4FE),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .clip(pillShape)
                    .background(Color(0xFF241047))
                    .border(1.dp, AlexPurpleDeep, pillShape)
                    .clickable(role = Role.Button, onClick = onTest)
                    .padding(horizontal = 26.dp, vertical = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrafficTile(
                iconRes = R.drawable.ic_alex_download,
                label = stringResource(R.string.alex_downlink),
                value = formatBytes(traffic.rx),
                modifier = Modifier.weight(1f)
            )
            TrafficTile(
                iconRes = R.drawable.ic_alex_upload,
                label = stringResource(R.string.alex_uplink),
                value = formatBytes(traffic.tx),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TrafficTile(
    @DrawableRes iconRes: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(TileColor)
            .border(1.dp, TileBorder, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Color(0xFF2A1558), CircleShape)
                .border(1.dp, AlexPurpleDeep, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = AlexPurple,
                modifier = Modifier.size(26.dp)
            )
        }
        Column {
            Text(text = label, color = AlexLavender, fontSize = 14.sp, maxLines = 1)
            Text(
                text = value,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

private fun loadActiveConfig(guid: String?): ActiveConfigInfo? {
    if (guid.isNullOrEmpty()) return null
    val profile = MmkvManager.decodeServerConfig(guid) ?: return null
    val delay = MmkvManager.decodeServerAffiliationInfo(guid)?.testDelayMillis ?: 0L
    return ActiveConfigInfo(
        remarks = profile.remarks,
        description = profile.description.nullIfBlank() ?: AngConfigManager.generateDescription(profile),
        type = profile.configType.name,
        delayMillis = delay,
    )
}

/**
 * Bytes sent / received by this app's UID since the connection started. The core and the tunnel run
 * inside this UID, so this is the encrypted traffic exchanged with the server.
 * TrafficStats returns UNSUPPORTED (-1) on some devices; that is shown as 0.
 */
@Composable
private fun rememberSessionTraffic(isRunning: Boolean): State<SessionTraffic> =
    produceState(SessionTraffic(), isRunning) {
        if (!isRunning) {
            value = SessionTraffic()
            return@produceState
        }
        val uid = Process.myUid()
        val base = withContext(Dispatchers.IO) { readUidTraffic(uid) }
        while (true) {
            delay(1000)
            val now = withContext(Dispatchers.IO) { readUidTraffic(uid) }
            value = SessionTraffic(
                rx = (now.rx - base.rx).coerceAtLeast(0L),
                tx = (now.tx - base.tx).coerceAtLeast(0L)
            )
        }
    }

private fun readUidTraffic(uid: Int): SessionTraffic {
    val rx = TrafficStats.getUidRxBytes(uid)
    val tx = TrafficStats.getUidTxBytes(uid)
    return SessionTraffic(rx = rx.coerceAtLeast(0L), tx = tx.coerceAtLeast(0L))
}

private fun formatBytes(bytes: Long): String =
    if (bytes < 1024L) "$bytes B" else bytes.toTrafficString()
