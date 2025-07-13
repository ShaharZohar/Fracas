package com.demo.fracasgame.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demo.fracasgame.game.models.Cell
import com.demo.fracasgame.ui.theme.GameBackground
import com.demo.fracasgame.ui.theme.PlayerColors

/**
 * Test game screen that doesn't rely on the complex GameEngine
 * This is a simplified version to help debug rendering issues
 */
@Composable
fun TestGameScreen() {
    var grid by remember { mutableStateOf(generateSimpleGrid(10, 10)) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameBackground)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Test Game Grid",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(onClick = {
                // Regenerate grid on button press
                grid = generateSimpleGrid(10, 10)
            }) {
                Text("Regenerate Grid")
            }
        }
        
        // Game board
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.DarkGray)
        ) {
            TestGameBoard(
                grid = grid,
                onCellClick = { x, y ->
                    // Toggle cell owner on click
                    val newGrid = grid.toMutableList().map { it.toMutableList() }
                    val currentCell = newGrid[y][x]
                    val newOwner = if (currentCell.owner < 0) 0 else (currentCell.owner + 1) % 4
                    newGrid[y][x] = currentCell.copy(
                        owner = newOwner,
                        troops = if (newOwner >= 0) 5 else 0,
                        isCapital = newOwner >= 0 && newOwner % 2 == 0
                    )
                    grid = newGrid.map { it.toList() }
                }
            )
        }
    }
}

@Composable
fun TestGameBoard(
    grid: List<List<Cell>>,
    onCellClick: (Int, Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(grid.first().size),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp)
    ) {
        grid.forEachIndexed { y, row ->
            row.forEachIndexed { x, cell ->
                item {
                    TestCellItem(
                        cell = cell,
                        playerColor = if (cell.owner >= 0) PlayerColors[cell.owner % PlayerColors.size] else Color.Gray,
                        onClick = { onCellClick(x, y) }
                    )
                }
            }
        }
    }
}

@Composable
fun TestCellItem(
    cell: Cell,
    playerColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (cell.owner >= 0) playerColor.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (cell.owner >= 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (cell.isCapital) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Yellow, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                
                Text(
                    text = cell.troops.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Generate a simple grid with some random cells filled
 */
private fun generateSimpleGrid(width: Int, height: Int): List<List<Cell>> {
    val grid = List(height) { y ->
        List(width) { x ->
            Cell(x, y)
        }
    }
    
    // Create a mutable copy to make modifications
    val mutableGrid = grid.toMutableList().map { it.toMutableList() }
    
    // Place some random owned cells
    repeat(5) { playerId ->
        repeat(3) {
            val x = (0 until width).random()
            val y = (0 until height).random()
            mutableGrid[y][x] = mutableGrid[y][x].copy(
                owner = playerId % 4,
                troops = (1..10).random(),
                isCapital = (playerId % 2 == 0)
            )
        }
    }
    
    return mutableGrid.map { it.toList() }
}
