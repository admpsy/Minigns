package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Rarity
import com.example.ui.theme.*
import com.example.viewmodel.GameUiState

@Composable
fun VictoryRewardScreen(
    state: GameUiState,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(2.dp, GoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(GoldSecondary.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(36.dp))
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "VITÓRIA TÁTICA!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldLight,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Os horrores da masmorra foram subjugados.",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                Spacer(Modifier.height(16.dp))

                // Rewards breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceElevated
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Experiência", fontSize = 11.sp, color = ArcaneCyan)
                            Text("+${state.gainedXp} XP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceElevated
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Tesouro", fontSize = 11.sp, color = GoldPrimary)
                            Text("+${state.gainedGold} Ouro", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                        }
                    }
                }

                if (state.gainedLoot.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Recompensas Encontradas:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GoldLight)
                    Spacer(Modifier.height(4.dp))
                    state.gainedLoot.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurfaceElevated,
                            border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                    Text(item.description, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("victory_continue_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text("Continuar Exploração", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun DefeatScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(2.dp, BloodCrimson)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(BloodCrimson.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Dangerous, contentDescription = null, tint = BloodCrimson, modifier = Modifier.size(36.dp))
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "O GRUPO FOI DERROTADO",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = BloodCrimson,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "A escuridão das catacumbas consumiu seus passos.",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("defeat_retry_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson, contentColor = Color.White)
                ) {
                    Text("Reagrupar no Acampamento", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
