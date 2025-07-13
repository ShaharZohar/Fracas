package com.demo.fracasgame.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.demo.fracasgame.ui.theme.GameBackground

/**
 * Help screen that explains how to play the Fracas game
 */
@Composable
fun HelpScreen(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = GameBackground
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "How to Play Fracas",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
                
                HelpSection(
                    title = "Game Objective",
                    content = "Conquer the entire map by capturing all territories and eliminating your opponents. A player is eliminated when they lose all their capitals."
                )
                
                HelpSection(
                    title = "Game Basics",
                    content = "- Each player starts with one capital and some troops\n" +
                            "- Capitals generate money each turn\n" +
                            "- Use money to buy troops or upgrade territories to capitals\n" +
                            "- Attack adjacent territories to expand your empire"
                )
                
                HelpSection(
                    title = "Taking Your Turn",
                    content = "1. Select one of your territories that has at least 2 troops\n" +
                            "2. Click on an adjacent territory to attack\n" +
                            "3. If successful, you'll capture the territory\n" +
                            "4. You can make multiple attacks per turn\n" +
                            "5. Click 'End Turn' when done"
                )
                
                HelpSection(
                    title = "Combat",
                    content = "- Attacking requires at least 2 troops (1 stays behind)\n" +
                            "- Combat outcome is determined by troop numbers and luck\n" +
                            "- Capturing a capital is a significant advantage\n" +
                            "- If all your capitals are captured, you're eliminated"
                )
                
                HelpSection(
                    title = "Economy",
                    content = "- Each capital generates money every turn\n" +
                            "- Use money to purchase new troops in your territories\n" +
                            "- Upgrade strategic territories to capitals\n" +
                            "- More capitals = more income"
                )
                
                HelpSection(
                    title = "Strategy Tips",
                    content = "- Protect your capitals at all costs\n" +
                            "- Balance between expansion and defense\n" +
                            "- Target opponent capitals when possible\n" +
                            "- Create a defensive line of high-troop territories"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Got It!", fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * Helper composable for displaying a section of the help screen
 */
@Composable
fun HelpSection(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2A2A)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = content,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
