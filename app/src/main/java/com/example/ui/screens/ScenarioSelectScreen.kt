package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.model.EnvironmentTheme
import com.example.ui.theme.*
import com.example.viewmodel.GameScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioSelectScreen(
    onSelectScenario: (EnvironmentTheme) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cenários & Ambientes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                        Text("Selecione uma masmorra para explorar", fontSize = 11.sp, color = Color.LightGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("scenario_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = GoldLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurfaceElevated)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(EnvironmentTheme.values()) { theme ->
                ScenarioCard(
                    theme = theme,
                    onSelect = { onSelectScenario(theme) }
                )
            }
        }
    }
}

@Composable
private fun ScenarioCard(
    theme: EnvironmentTheme,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColor = Color(theme.primaryColorHex)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(themeColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = theme.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = themeColor.copy(alpha = 0.2f),
                    border = BorderStroke(0.5.dp, themeColor)
                ) {
                    Text(
                        text = theme.dangerLevel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = theme.subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoldSecondary
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = theme.description,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = Color(0xFFCBD5E1)
            )

            Spacer(Modifier.height(8.dp))

            // Ambient Quote
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceElevated
            ) {
                Text(
                    text = "✦ ${theme.ambientDescription}",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("enter_${theme.name.lowercase()}"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColor,
                    contentColor = if (theme == EnvironmentTheme.ROYAL_CRYPT || theme == EnvironmentTheme.ANCIENT_RUINS) Color.Black else Color.White
                )
            ) {
                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Adentrar Masmorra", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
