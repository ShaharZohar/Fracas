package com.demo.fracasgame.game

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.demo.fracasgame.game.models.Cell
import com.demo.fracasgame.game.models.Player
import com.demo.fracasgame.game.models.TerrainType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Renders a realistic map-like game board using Canvas
 */
@Composable
fun MapBoard(
    grid: List<List<Cell>>,
    selectedCell: Cell?,
    players: List<Player>,
    onCellClick: (Cell) -> Unit
) {
    // If grid is empty, don't render anything
    if (grid.isEmpty()) return
    
    val density = LocalDensity.current
    
    // Generate terrain data if not already present in the cell
    val terrainGrid = remember {
        generateTerrainData(grid)
    }
    
    // Remember terrain paths to avoid recalculating them
    val terrainPaths = remember {
        generateTerrainPaths(terrainGrid)
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(grid) {
                detectTapGestures { offset ->
                    // Convert tap coordinates to grid position
                    val cellWidth = size.width / grid[0].size
                    val cellHeight = size.height / grid.size
                    
                    val x = (offset.x / cellWidth).toInt().coerceIn(0, grid[0].size - 1)
                    val y = (offset.y / cellHeight).toInt().coerceIn(0, grid.size - 1)
                    
                    // Trigger click event with the cell at these coordinates
                    onCellClick(grid[y][x])
                }
            }
    ) {
        // Draw the base terrain
        drawTerrain(terrainGrid, terrainPaths)
        
        // Draw territory borders
        drawTerritoryBorders(grid, players)
        
        // Draw cell highlights and troops
        drawCellsAndTroops(grid, selectedCell, players)
    }
}

/**
 * Generate terrain data for each cell
 */
private fun generateTerrainData(grid: List<List<Cell>>): List<List<TerrainData>> {
    val height = grid.size
    val width = if (grid.isNotEmpty()) grid[0].size else 0
    val noise = SimplexNoise(Random.nextLong())
    
    // Generate base height map with simplex noise
    val heightMap = List(height) { y ->
        List(width) { x ->
            // Scale coordinates for smoother terrain
            val nx = x.toFloat() / width
            val ny = y.toFloat() / height
            val noiseValue = (noise.noise(nx * 5f, ny * 5f) + 1) / 2 // Range 0-1
            noiseValue
        }
    }
    
    // Convert height map to terrain types
    return heightMap.mapIndexed { y, row ->
        row.mapIndexed { x, height ->
            val terrainType = when {
                height < 0.3f -> TerrainType.WATER
                height < 0.4f -> TerrainType.COAST
                height < 0.6f -> TerrainType.PLAINS
                height < 0.75f -> TerrainType.HILLS
                else -> TerrainType.MOUNTAINS
            }
            
            // Add some random vegetation
            val hasForest = terrainType == TerrainType.PLAINS && Random.nextFloat() < 0.3f
            
            TerrainData(
                type = terrainType,
                height = height,
                hasForest = hasForest,
                x = x,
                y = y
            )
        }
    }
}

/**
 * Generates paths for terrain features to avoid recalculating them
 */
private fun generateTerrainPaths(terrain: List<List<TerrainData>>): TerrainPaths {
    val coastPath = androidx.compose.ui.graphics.Path()
    val mountainsPath = androidx.compose.ui.graphics.Path()
    val hillsPath = androidx.compose.ui.graphics.Path()
    
    // Process cells to create continuous paths
    terrain.forEachIndexed { y, row ->
        row.forEachIndexed { x, cell ->
            // Add path points based on terrain type
            when (cell.type) {
                TerrainType.COAST -> {
                    // Add coastline points
                }
                TerrainType.HILLS -> {
                    // Add hill points
                }
                TerrainType.MOUNTAINS -> {
                    // Add mountain points
                }
                else -> {} // Other terrain types don't need special paths
            }
        }
    }
    
    return TerrainPaths(coastPath, mountainsPath, hillsPath)
}

/**
 * Draws the terrain on the canvas
 */
private fun DrawScope.drawTerrain(
    terrain: List<List<TerrainData>>,
    paths: TerrainPaths
) {
    val height = terrain.size
    val width = if (terrain.isNotEmpty()) terrain[0].size else 0
    
    val cellWidth = size.width / width
    val cellHeight = size.height / height
    
    // Draw base water
    drawRect(
        color = Color(0xFF1E88E5), // Blue for water
        size = size
    )
    
    // Draw each cell based on terrain type
    terrain.forEachIndexed { y, row ->
        row.forEachIndexed { x, cell ->
            val left = x * cellWidth
            val top = y * cellHeight
            
            val cellColor = when (cell.type) {
                TerrainType.WATER -> Color(0xFF1E88E5) // Darker blue
                TerrainType.COAST -> Color(0xFF90CAF9) // Light blue
                TerrainType.PLAINS -> if (cell.hasForest) Color(0xFF66BB6A) else Color(0xFF81C784) // Green
                TerrainType.HILLS -> Color(0xFFBDB76B) // Khaki
                TerrainType.MOUNTAINS -> Color(0xFF9E9E9E) // Gray
            }
            
            // Draw the cell
            drawRect(
                color = cellColor,
                topLeft = Offset(left, top),
                size = Size(cellWidth, cellHeight)
            )
            
            // Add terrain details
            when (cell.type) {
                TerrainType.MOUNTAINS -> {
                    // Draw mountain peak
                    val centerX = left + cellWidth / 2
                    val centerY = top + cellHeight / 2
                    
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(centerX - cellWidth * 0.3f, centerY + cellHeight * 0.2f)
                            lineTo(centerX, centerY - cellHeight * 0.3f)
                            lineTo(centerX + cellWidth * 0.3f, centerY + cellHeight * 0.2f)
                            close()
                        },
                        color = Color(0xFF757575) // Darker gray
                    )
                }
                TerrainType.HILLS -> {
                    // Draw hill shape
                    val centerX = left + cellWidth / 2
                    val centerY = top + cellHeight / 2
                    
                    drawArc(
                        color = Color(0xFFBDB76B).copy(alpha = 0.8f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(centerX - cellWidth * 0.3f, centerY - cellHeight * 0.1f),
                        size = Size(cellWidth * 0.6f, cellHeight * 0.3f)
                    )
                }
                TerrainType.PLAINS -> {
                    if (cell.hasForest) {
                        // Draw simple tree
                        val centerX = left + cellWidth / 2
                        val centerY = top + cellHeight / 2
                        
                        drawCircle(
                            color = Color(0xFF2E7D32), // Darker green
                            radius = min(cellWidth, cellHeight) * 0.2f,
                            center = Offset(centerX, centerY - cellHeight * 0.1f)
                        )
                        
                        drawRect(
                            color = Color(0xFF795548), // Brown
                            topLeft = Offset(centerX - cellWidth * 0.05f, centerY),
                            size = Size(cellWidth * 0.1f, cellHeight * 0.15f)
                        )
                    }
                }
                else -> {} // Other terrain types don't need additional details
            }
        }
    }
}

/**
 * Draws borders between territories of different players
 */
private fun DrawScope.drawTerritoryBorders(
    grid: List<List<Cell>>,
    players: List<Player>
) {
    val height = grid.size
    val width = if (grid.isNotEmpty()) grid[0].size else 0
    
    val cellWidth = size.width / width
    val cellHeight = size.height / height
    
    // Draw borders between cells of different owners
    grid.forEachIndexed { y, row ->
        row.forEachIndexed { x, cell ->
            if (cell.owner >= 0) {
                val left = x * cellWidth
                val top = y * cellHeight
                
                // Check neighboring cells and draw borders if they belong to different players
                // Right border
                if (x < width - 1 && grid[y][x + 1].owner != cell.owner) {
                    drawLine(
                        color = Color.White,
                        start = Offset(left + cellWidth, top),
                        end = Offset(left + cellWidth, top + cellHeight),
                        strokeWidth = 3.dp.toPx()
                    )
                }
                
                // Bottom border
                if (y < height - 1 && grid[y + 1][x].owner != cell.owner) {
                    drawLine(
                        color = Color.White,
                        start = Offset(left, top + cellHeight),
                        end = Offset(left + cellWidth, top + cellHeight),
                        strokeWidth = 3.dp.toPx()
                    )
                }
                
                // Left border
                if (x > 0 && grid[y][x - 1].owner != cell.owner) {
                    drawLine(
                        color = Color.White,
                        start = Offset(left, top),
                        end = Offset(left, top + cellHeight),
                        strokeWidth = 3.dp.toPx()
                    )
                }
                
                // Top border
                if (y > 0 && grid[y - 1][x].owner != cell.owner) {
                    drawLine(
                        color = Color.White,
                        start = Offset(left, top),
                        end = Offset(left + cellWidth, top),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }
    }
}

/**
 * Draw cell highlighting and troop indicators
 */
private fun DrawScope.drawCellsAndTroops(
    grid: List<List<Cell>>,
    selectedCell: Cell?,
    players: List<Player>
) {
    val height = grid.size
    val width = if (grid.isNotEmpty()) grid[0].size else 0
    
    val cellWidth = size.width / width
    val cellHeight = size.height / height
    
    grid.forEachIndexed { y, row ->
        row.forEachIndexed { x, cell ->
            val left = x * cellWidth
            val top = y * cellHeight
            val centerX = left + cellWidth / 2
            val centerY = top + cellHeight / 2
            
            // Draw player territory color overlay
            if (cell.owner >= 0 && cell.owner < players.size) {
                val playerColor = players[cell.owner].color
                
                // Draw semi-transparent overlay for player territory
                drawRect(
                    color = playerColor.copy(alpha = 0.3f),
                    topLeft = Offset(left, top),
                    size = Size(cellWidth, cellHeight)
                )
                
                // Draw troop count with background
                if (cell.troops > 0) {
                    // Background circle for troops
                    drawCircle(
                        color = playerColor,
                        radius = min(cellWidth, cellHeight) * 0.3f,
                        center = Offset(centerX, centerY)
                    )
                    
                    // Troop number
                    drawContext.canvas.nativeCanvas.apply {
                        val textSize = min(cellWidth, cellHeight) * 0.4f
                        
                        drawText(
                            cell.troops.toString(),
                            centerX,
                            centerY + textSize / 3,  // adjust for baseline
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textAlign = Paint.Align.CENTER
                                this.textSize = textSize
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                            }
                        )
                    }
                }
                
                // Draw capital indicator
                if (cell.isCapital) {
                    drawCircle(
                        color = Color.Yellow,
                        radius = min(cellWidth, cellHeight) * 0.15f,
                        center = Offset(centerX, centerY - cellHeight * 0.25f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
            
            // Highlight selected cell
            if (cell == selectedCell) {
                drawRect(
                    color = Color.Yellow,
                    topLeft = Offset(left, top),
                    size = Size(cellWidth, cellHeight),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

/**
 * Data class to store terrain information
 */
data class TerrainData(
    val type: TerrainType,
    val height: Float,
    val hasForest: Boolean,
    val x: Int,
    val y: Int
)

/**
 * Class to hold pre-computed terrain paths
 */
data class TerrainPaths(
    val coastPath: androidx.compose.ui.graphics.Path,
    val mountainsPath: androidx.compose.ui.graphics.Path,
    val hillsPath: androidx.compose.ui.graphics.Path
)

/**
 * Simple implementation of Simplex Noise for terrain generation
 */
class SimplexNoise(seed: Long) {
    private val random = Random(seed)
    private val permutation = IntArray(512)
    
    init {
        // Initialize permutation
        val p = IntArray(256)
        for (i in 0 until 256) {
            p[i] = i
        }
        
        // Shuffle permutation
        for (i in 255 downTo 0) {
            val j = random.nextInt(i + 1)
            val temp = p[i]
            p[i] = p[j]
            p[j] = temp
        }
        
        // Extend permutation to avoid wrapping
        for (i in 0 until 512) {
            permutation[i] = p[i and 255]
        }
    }
    
    fun noise(x: Float, y: Float): Float {
        // Simple noise function for demonstration
        // In a real implementation, use a proper simplex noise algorithm
        
        val corners = 4
        var n = 0f
        
        // Sample multiple octaves
        n += sin(x * 10f + y * 5f) * 0.5f
        n += sin(x * 20f + y * 10f) * 0.25f
        n += sin(x * 40f + y * 20f) * 0.125f
        n += sin(x * 80f + y * 40f) * 0.0625f
        
        return n
    }
    
    private fun sin(x: Float): Float = kotlin.math.sin(x).toFloat()
}
