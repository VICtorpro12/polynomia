package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BossMonster
import com.example.model.GameLevel
import com.example.model.PlayerProfile
import com.example.model.WorldType
import com.example.ui.theme.ArcanePurple
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.RubyRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.Screen

data class WorldTabInfo(
    val type: WorldType,
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun WorldMapScreen(
    profile: PlayerProfile,
    levels: List<GameLevel>,
    bosses: List<BossMonster>,
    selectedTab: WorldType,
    onSelectTab: (WorldType) -> Unit,
    onNavigate: (Screen) -> Unit
) {
    val tabs = listOf(
        WorldTabInfo(WorldType.RUFFINI, "Torre Ruffini", Icons.Default.Castle, NeonCyan),
        WorldTabInfo(WorldType.RATIONAL_ROOTS, "Caza Divisores", Icons.Default.TrackChanges, EmeraldGreen),
        WorldTabInfo(WorldType.BIQUADRATIC, "Bicuadradas", Icons.Default.Science, ArcanePurple),
        WorldTabInfo(WorldType.GROUPING, "Factor Común", Icons.Default.Category, NeonGold),
        WorldTabInfo(WorldType.BOSS_ARENA, "Jefes RPG", Icons.Default.Gavel, RubyRed),
        WorldTabInfo(WorldType.SANDBOX, "Laboratorio", Icons.Default.Calculate, NeonCyan)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_game_banner),
                    contentDescription = "Polynomia Reino de Ecuaciones",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DarkBackground.copy(alpha = 0.5f),
                                    DarkBackground
                                )
                            )
                        )
                )

                // Title overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonGold)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = DarkBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "POLYNOMIA",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = NeonGold
                        )
                    }
                    Text(
                        text = "Conquista las ecuaciones de grado superior jugando",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Quick Stats Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Ecuaciones",
                    value = "${profile.solvedEquations}",
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Racha",
                    value = "${profile.streak} Días 🔥",
                    color = NeonGold,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Jefes Caídos",
                    value = "${profile.bossDefeated} ⚔️",
                    color = RubyRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // World Tabs Navigation
        item {
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOfFirst { it.type == selectedTab }.coerceAtLeast(0),
                containerColor = DarkBackground,
                contentColor = TextPrimary,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    val index = tabs.indexOfFirst { it.type == selectedTab }.coerceAtLeast(0)
                    if (index < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = tabs[index].color,
                            height = 3.dp
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEach { tab ->
                    val isSelected = tab.type == selectedTab
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectTab(tab.type) },
                        modifier = Modifier
                            .testTag("tab_${tab.type.name.lowercase()}")
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) tab.color else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) tab.color else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // World Content
        when (selectedTab) {
            WorldType.BOSS_ARENA -> {
                item {
                    Text(
                        text = "Coliseo de Titanes Matemáticos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RubyRed,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                items(bosses) { boss ->
                    BossCard(
                        boss = boss,
                        onFightClick = { onNavigate(Screen.BossBattle(boss)) }
                    )
                }
            }
            WorldType.SANDBOX -> {
                item {
                    SandboxPromoCard(
                        onOpenSandbox = { onNavigate(Screen.Sandbox) }
                    )
                }
            }
            else -> {
                val currentLevels = levels.filter { it.worldType == selectedTab }
                item {
                    Text(
                        text = "Niveles Disponibles (${currentLevels.count { it.isUnlocked }}/${currentLevels.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 8.dp)
                    )
                }
                items(currentLevels) { level ->
                    LevelCard(
                        level = level,
                        onClick = {
                            if (level.isUnlocked) {
                                when (level.worldType) {
                                    WorldType.RUFFINI -> onNavigate(Screen.RuffiniGame(level))
                                    WorldType.RATIONAL_ROOTS -> onNavigate(Screen.RationalRootsGame(level))
                                    WorldType.BIQUADRATIC -> onNavigate(Screen.BiquadraticGame(level))
                                    WorldType.GROUPING -> onNavigate(Screen.GroupingGame(level))
                                    else -> {}
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
fun LevelCard(
    level: GameLevel,
    onClick: () -> Unit
) {
    val borderColor = if (level.isUnlocked) NeonCyan.copy(alpha = 0.5f) else CardBorder
    val containerBg = if (level.isUnlocked) DarkSurface else DarkSurface.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("level_card_${level.id}")
            .clickable(enabled = level.isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level badge number
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (level.isUnlocked) Brush.linearGradient(listOf(NeonCyan, Color(0xFF3B82F6)))
                        else Brush.linearGradient(listOf(DarkSurfaceElevated, DarkSurfaceElevated))
                    )
            ) {
                if (level.isUnlocked) {
                    Text(
                        text = "${level.levelNumber}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bloqueado",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Level Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (level.isUnlocked) TextPrimary else TextMuted
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Equation formula chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = level.polynomial.toFormattedString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (level.isUnlocked) NeonCyan else TextMuted
                    )
                }
            }

            // Stars earned
            Column(horizontalAlignment = Alignment.End) {
                Row {
                    repeat(3) { starIndex ->
                        val earned = starIndex < level.stars
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (earned) NeonGold else Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+${level.xpReward} XP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGold
                )
            }
        }
    }
}

@Composable
fun BossCard(
    boss: BossMonster,
    onFightClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("boss_card_${boss.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, RubyRed.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = boss.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = RubyRed
                    )
                    Text(
                        text = boss.title,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RubyRed.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "HP: ${boss.currentHp}/${boss.maxHp}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RubyRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Equation of the boss
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(10.dp)
            ) {
                Text(
                    text = boss.polynomial.toFormattedString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ElevatedButton(
                onClick = onFightClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_fight_boss_${boss.id}"),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = RubyRed,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DESAFIAR AL JEFE",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun SandboxPromoCard(
    onOpenSandbox: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Laboratorio Libre de Polinomios",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Experimenta con cualquier ecuación de grado 3, 4 o 5. Visualiza su gráfica cartesiana en tiempo real y resuelve con la tabla animada de Ruffini.",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            ElevatedButton(
                onClick = onOpenSandbox,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_open_sandbox"),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = NeonCyan,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "ABRIR LABORATORIO",
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
