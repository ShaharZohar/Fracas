package com.demo.fracasgame.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.demo.fracasgame.game.engine.GameState
import com.demo.fracasgame.game.models.Cell
import com.demo.fracasgame.game.models.GameSettings
import com.demo.fracasgame.game.models.Player
import com.demo.fracasgame.ui.theme.GameBackground
import com.demo.fracasgame.ui.theme.GridColor
import com.demo.fracasgame.ui.theme.PlayerColors
import com.demo.fracasgame.game.MapBoard // Add MapBoard import

/**
 * Main composable for the Fracas game
 */
@Composable
fun FracasGame(
    gameViewModel: GameViewModel = viewModel()
) {
    val gameState by gameViewModel.gameState.collectAsState()
    val grid by gameViewModel.grid.collectAsState()
    val players by gameViewModel.players.collectAsState()
    val currentPlayerId by gameViewModel.currentPlayer.collectAsState()
    val selectedCell by gameViewModel.selectedCell.collectAsState()
    val movesRemaining by gameViewModel.movesAvailable.collectAsState()
    val gameMessage by gameViewModel.message.collectAsState()
    
    var showNewGameDialog by remember { mutableStateOf(false) }
    var showHelpScreen by remember { mutableStateOf(false) }
    var troopsToPurchase by remember { mutableStateOf("1") }

    LaunchedEffect(Unit) {
        if (gameState == GameState.Loading) {
            gameViewModel.initializeGame()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
            .padding(8.dp)
    ) {
        // Game header
        GameHeader(
            currentPlayer = if (players.isNotEmpty() && currentPlayerId < players.size) 
                            players[currentPlayerId] else null,
            movesRemaining = movesRemaining,
            onNewGame = { showNewGameDialog = true },
            onEndTurn = { gameViewModel.endTurn() },
            onHelp = { showHelpScreen = true }
        )
        
        // Game message area
        GameMessageArea(message = gameMessage)
        
        // Game board
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (grid.isEmpty()) {
                // Show loading indicator when grid is empty
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading game board...",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            } else {
                // Use the new MapBoard for realistic terrain rendering
                MapBoard(
                    grid = grid,
                    selectedCell = selectedCell,
                    players = players,
                    onCellClick = { cell ->
                        gameViewModel.onCellClick(cell)
                    }
                )
            }
        }
        
        // Player controls
        PlayerControls(
            currentPlayer = if (players.isNotEmpty() && currentPlayerId < players.size)
                            players[currentPlayerId] else null,
            selectedCell = selectedCell,
            troopsToPurchase = troopsToPurchase,
            onTroopsToPurchaseChange = { troopsToPurchase = it },
            onBuyTroops = { 
                selectedCell?.let { cell -> 
                    gameViewModel.purchaseTroops(cell, troopsToPurchase.toIntOrNull() ?: 1)
                }
            },
            onUpgradeCapital = {
                selectedCell?.let { cell ->
                    gameViewModel.upgradeToCapital(cell)
                }
            }
        )
    }
    
    // New game dialog
    if (showNewGameDialog) {
        NewGameDialog(
            onDismiss = { showNewGameDialog = false },
            onStartGame = { settings ->
                gameViewModel.updateSettings(settings)
                gameViewModel.initializeGame()
                showNewGameDialog = false
            }
        )
    }
    
    // Help screen dialog
    if (showHelpScreen) {
        HelpScreen(
            onDismiss = { showHelpScreen = false }
        )
    }
    
    // Game over dialog
    if (gameState == GameState.GameOver) {
        GameOverDialog(
            message = gameMessage,
            onNewGame = {
                showNewGameDialog = true
            }
        )
    }
}

/**
 * Game board UI that displays the grid of cells
 */
@Composable
fun GameBoard(
    grid: List<List<Cell>>,
    selectedCell: Cell?,
    players: List<Player>,
    onCellClick: (Cell) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .background(Color.DarkGray) // Add background color to make the grid visible
    ) {
        if (grid.isEmpty()) {
            // Show loading indicator when grid is empty
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading game board...",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        } else {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val cellWidth = size.width / (grid.firstOrNull()?.size?.toFloat()?.coerceAtLeast(1f) ?: 1f)
                val cellHeight = size.height / grid.size.coerceAtLeast(1).toFloat()
                
                // Draw grid lines
                for (i in 0..grid.size) {
                    val y = i * cellHeight
                    drawLine(
                        color = Color.White, // Change grid color to white for better visibility
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2.dp.toPx() // Increase line thickness
                    )
                }
                
                for (i in 0..(grid.firstOrNull()?.size ?: 0)) {
                    val x = i * cellWidth
                    drawLine(
                        color = Color.White, // Change grid color to white for better visibility
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 2.dp.toPx() // Increase line thickness
                    )
                }
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(grid.first().size),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp)
            ) {
                grid.flatten().forEach { cell ->
                    item {
                        CellItem(
                            cell = cell,
                            isSelected = cell == selectedCell,
                            playerColor = if (cell.owner >= 0 && cell.owner < players.size) 
                                        players[cell.owner].color else Color.Gray,
                            onClick = { onCellClick(cell) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual cell UI
 */
@Composable
fun CellItem(
    cell: Cell,
    isSelected: Boolean,
    playerColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(if (cell.owner >= 0) playerColor.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.3f)) 
            .border(
                width = if (isSelected) 2.dp else 1.dp, 
                color = if (isSelected) Color.Yellow else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(2.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (cell.owner >= 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (cell.isCapital) {
                    Box(
                        modifier = Modifier
                            .size(10.dp) 
                            .background(Color.Yellow, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                }
                
                Text(
                    text = cell.troops.toString(),
                    color = Color.White,
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Game header UI with player info and controls
 */
@Composable
fun GameHeader(
    currentPlayer: Player?,
    movesRemaining: Int,
    onNewGame: () -> Unit,
    onEndTurn: () -> Unit,
    onHelp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
    ) {
        // Top row with player info and moves
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player info
            if (currentPlayer != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(currentPlayer.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = currentPlayer.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Money: ${currentPlayer.money} | Troops: ${currentPlayer.totalTroops}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            // Moves remaining
            Text(
                text = "Moves: $movesRemaining",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp),
                lineHeight = 36.sp
            )
        }
        
        // Bottom row with all buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onHelp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Help")
            }
            
            Button(
                onClick = onEndTurn,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text("End Turn")
            }
            
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text("New Game")
            }
        }
    }
}

/**
 * Game message area UI
 */
@Composable
fun GameMessageArea(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(Color(0xFF333333), RoundedCornerShape(4.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Player controls UI for purchasing troops and upgrading capitals
 */
@Composable
fun PlayerControls(
    currentPlayer: Player?,
    selectedCell: Cell?,
    troopsToPurchase: String,
    onTroopsToPurchaseChange: (String) -> Unit,
    onBuyTroops: () -> Unit,
    onUpgradeCapital: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF333333)
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Troop purchase controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = troopsToPurchase,
                    onValueChange = { value ->
                        // Only allow numbers
                        if (value.isEmpty() || value.all { it.isDigit() }) {
                            onTroopsToPurchaseChange(value)
                        }
                    },
                    label = { Text("Troops", fontSize = 12.sp) },
                    modifier = Modifier.width(80.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF444444),
                        focusedContainerColor = Color(0xFF444444)
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onBuyTroops,
                    enabled = currentPlayer != null && 
                              selectedCell != null && 
                              selectedCell.owner == currentPlayer.id &&
                              troopsToPurchase.isNotEmpty() &&
                              (troopsToPurchase.toIntOrNull() ?: 0) > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Buy Troops")
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Capital upgrade button
            Button(
                onClick = onUpgradeCapital,
                enabled = currentPlayer != null && 
                          selectedCell != null && 
                          selectedCell.owner == currentPlayer.id &&
                          !selectedCell.isCapital,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Text("Upgrade to Capital")
            }
        }
    }
}

/**
 * New game dialog UI
 */
@Composable
fun NewGameDialog(
    onDismiss: () -> Unit,
    onStartGame: (GameSettings) -> Unit
) {
    var gridSize by remember { mutableStateOf("10") }
    var humanPlayers by remember { mutableStateOf("1") }
    var aiPlayers by remember { mutableStateOf("3") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("New Game")
        },
        text = {
            Column {
                TextField(
                    value = gridSize,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) gridSize = it },
                    label = { Text("Grid Size") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )
                
                TextField(
                    value = humanPlayers,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) humanPlayers = it },
                    label = { Text("Human Players") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )
                
                TextField(
                    value = aiPlayers,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) aiPlayers = it },
                    label = { Text("AI Players") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val size = gridSize.toIntOrNull() ?: 10
                    val humans = humanPlayers.toIntOrNull() ?: 1
                    val ais = aiPlayers.toIntOrNull() ?: 3
                    
                    onStartGame(
                        GameSettings(
                            gridWidth = size,
                            gridHeight = size,
                            humanPlayers = humans,
                            aiPlayers = ais
                        )
                    )
                }
            ) {
                Text("Start Game")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Game over dialog UI
 */
@Composable
fun GameOverDialog(
    message: String,
    onNewGame: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text("Game Over")
        },
        text = {
            Text(message)
        },
        confirmButton = {
            Button(
                onClick = onNewGame
            ) {
                Text("New Game")
            }
        },
        dismissButton = {}
    )
}
