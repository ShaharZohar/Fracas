package com.demo.fracasgame.game.engine

import com.demo.fracasgame.game.models.*
import com.demo.fracasgame.ui.theme.PlayerColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.random.Random

/**
 * Main game engine for Fracas that handles game state and logic
 */
class GameEngine(private var settings: GameSettings = GameSettings()) {

    private val _gameState = MutableStateFlow<GameState>(GameState.Loading)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private val _grid = MutableStateFlow<List<List<Cell>>>(emptyList())
    val grid: StateFlow<List<List<Cell>>> = _grid.asStateFlow()
    
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()
    
    private val _currentPlayer = MutableStateFlow<Int>(0)
    val currentPlayer: StateFlow<Int> = _currentPlayer.asStateFlow()
    
    private val _selectedCell = MutableStateFlow<Cell?>(null)
    val selectedCell: StateFlow<Cell?> = _selectedCell.asStateFlow()
    
    private val _movesAvailable = MutableStateFlow<Int>(0)
    val movesAvailable: StateFlow<Int> = _movesAvailable.asStateFlow()
    
    private val _message = MutableStateFlow<String>("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    /**
     * Initialize the game with settings
     */
    fun initializeGame() {
        _gameState.value = GameState.Loading
        initializePlayers()
        initializeGrid()
        _gameState.value = GameState.Running
        _movesAvailable.value = settings.troopsPerTurn
        _message.value = "Game started! ${getCurrentPlayer().name}'s turn"
    }
    
    /**
     * Initialize players based on game settings
     */
    private fun initializePlayers() {
        val totalPlayers = settings.humanPlayers + settings.aiPlayers
        val newPlayers = mutableListOf<Player>()
        
        for (i in 0 until settings.humanPlayers) {
            newPlayers.add(
                Player(
                    id = i,
                    name = "Player ${i + 1}",
                    color = PlayerColors[i % PlayerColors.size],
                    isAI = false,
                    money = settings.initialMoney
                )
            )
        }
        
        for (i in settings.humanPlayers until totalPlayers) {
            newPlayers.add(
                Player(
                    id = i,
                    name = "AI ${i - settings.humanPlayers + 1}",
                    color = PlayerColors[i % PlayerColors.size],
                    isAI = true,
                    money = settings.initialMoney
                )
            )
        }
        
        _players.value = newPlayers
        _currentPlayer.value = 0
    }
    
    /**
     * Initialize the game grid and place initial capitals
     */
    private fun initializeGrid() {
        // Ensure minimum grid dimensions to avoid empty grids
        val gridHeight = settings.gridHeight.coerceAtLeast(5)
        val gridWidth = settings.gridWidth.coerceAtLeast(5)
        
        val newGrid = List(gridHeight) { y ->
            List(gridWidth) { x ->
                Cell(x, y)
            }
        }
        
        _grid.value = newGrid
        
        // Ensure we have players before attempting to place capitals
        if (players.value.isEmpty()) {
            initializePlayers()
        }
        
        // Place initial capitals for each player
        players.value.forEach { player ->
            placeInitialCapital(player)
        }
        
        updatePlayerStats()
    }
    
    /**
     * Place initial capital for a player at a random position
     */
    private fun placeInitialCapital(player: Player) {
        // Safety check to ensure grid is initialized
        if (grid.value.isEmpty() || grid.value.first().isEmpty()) {
            return
        }
        
        val availableCells = grid.value.flatten().filter { it.isEmpty() }
        if (availableCells.isNotEmpty()) {
            val cell = availableCells.random()
            val mutGrid = grid.value.toMutableList().map { it.toMutableList() }
            
            mutGrid[cell.y][cell.x] = cell.copy(
                owner = player.id,
                troops = settings.initialTroopsPerCapital,
                isCapital = true
            )
            
            _grid.value = mutGrid.map { it.toList() }
        }
    }
    
    /**
     * Handle a cell click event
     */
    fun onCellClick(cell: Cell) {
        if (gameState.value != GameState.Running) return
        if (getCurrentPlayer().isAI) return

        val currentPlayerId = currentPlayer.value
        val selected = selectedCell.value
        
        if (selected == null) {
            // If no cell is selected, only allow selecting own cells
            if (cell.isOwnedBy(currentPlayerId) && cell.troops > 1) {
                _selectedCell.value = cell
                _message.value = "Selected ${cell.troops} troops. Click destination cell."
            } else if (cell.isOwnedBy(currentPlayerId)) {
                _message.value = "Not enough troops to move."
            }
        } else {
            // A cell is already selected
            if (cell.isOwnedBy(currentPlayerId) && cell != selected) {
                // Changed selection to another owned cell
                _selectedCell.value = cell
                _message.value = "Selected ${cell.troops} troops. Click destination cell."
            } else if (!cell.isOwnedBy(currentPlayerId) && canAttack(selected, cell)) {
                // Attack another player's cell or empty cell
                executeAttack(selected, cell)
                _selectedCell.value = null
            } else if (cell == selected) {
                // Deselected the cell
                _selectedCell.value = null
                _message.value = "Selection canceled."
            } else {
                _message.value = "Invalid move! Target too far."
            }
        }
    }
    
    /**
     * Check if an attack from one cell to another is valid
     */
    private fun canAttack(from: Cell, to: Cell): Boolean {
        // Check if cells are adjacent (horizontally or vertically)
        val xDistance = abs(from.x - to.x)
        val yDistance = abs(from.y - to.y)
        
        return (xDistance <= 1 && yDistance <= 1) && // Adjacent or diagonal
               (xDistance + yDistance <= 2) && // Not too far
               from.troops > 1 && // Has troops to move
               movesAvailable.value > 0 && // Has moves left
               from.owner != to.owner // Different owners
    }
    
    /**
     * Execute an attack from one cell to another
     */
    private fun executeAttack(from: Cell, to: Cell) {
        val attackingTroops = from.troops - 1  // Leave 1 troop behind
        val defendingTroops = to.troops
        
        val capturedTerr = if (to.owner == -1) true else {
            // Battle algorithm (simplified)
            val attackPower = attackingTroops * (0.8 + Random.nextDouble() * 0.4)
            val defensePower = defendingTroops * (1.0 + Random.nextDouble() * 0.5)
            
            attackPower > defensePower
        }
        
        val mutGrid = grid.value.toMutableList().map { it.toMutableList() }
        
        if (capturedTerr) {
            val wasCapital = mutGrid[to.y][to.x].isCapital
            val prevOwner = mutGrid[to.y][to.x].owner
            
            // Update source cell
            mutGrid[from.y][from.x] = mutGrid[from.y][from.x].copy(
                troops = 1
            )
            
            // Update destination cell
            mutGrid[to.y][to.x] = mutGrid[to.y][to.x].copy(
                owner = from.owner,
                troops = attackingTroops,
                isCapital = false  // Captured territory loses capital status
            )
            
            _message.value = "Attack successful! Captured territory."
            
            // If capital was captured, check if player is defeated
            if (wasCapital && prevOwner >= 0) {
                checkPlayerDefeat(prevOwner)
            }
        } else {
            // Attack failed, attacker loses some troops
            val troopsLost = (attackingTroops * 0.7).toInt().coerceAtLeast(1)
            val remainingTroops = from.troops - troopsLost
            
            // Update source cell
            mutGrid[from.y][from.x] = mutGrid[from.y][from.x].copy(
                troops = remainingTroops.coerceAtLeast(1)
            )
            
            // Defender may lose some troops too
            val defenderLosses = (defendingTroops * 0.3).toInt()
            if (defenderLosses > 0 && to.owner >= 0) {
                mutGrid[to.y][to.x] = mutGrid[to.y][to.x].copy(
                    troops = (to.troops - defenderLosses).coerceAtLeast(1)
                )
            }
            
            _message.value = "Attack failed! Lost $troopsLost troops."
        }
        
        _grid.value = mutGrid.map { it.toList() }
        _movesAvailable.value -= 1
        
        updatePlayerStats()
        
        // Check if current player has moves left, otherwise end turn
        if (_movesAvailable.value <= 0) {
            endTurn()
        }
        
        // Check if game is over
        checkGameOver()
    }
    
    /**
     * Get current player object
     */
    fun getCurrentPlayer(): Player {
        return players.value[currentPlayer.value]
    }
    
    /**
     * End current player's turn and switch to next player
     */
    fun endTurn() {
        var nextPlayer = (currentPlayer.value + 1) % players.value.size
        
        // Skip inactive players
        while (!players.value[nextPlayer].isActive && nextPlayer != currentPlayer.value) {
            nextPlayer = (nextPlayer + 1) % players.value.size
        }
        
        // Reset selection
        _selectedCell.value = null
        
        // Process turn end effects
        processTurnEnd()
        
        // Set next player
        _currentPlayer.value = nextPlayer
        _movesAvailable.value = settings.troopsPerTurn
        
        _message.value = "${getCurrentPlayer().name}'s turn"
        
        // If AI player, process AI turn
        if (getCurrentPlayer().isAI) {
            processAITurn()
        }
    }
    
    /**
     * Process turn end effects (income, etc.)
     */
    private fun processTurnEnd() {
        val player = getCurrentPlayer()
        
        // Calculate income based on capitals
        val income = player.capitals * settings.moneyPerCapital
        
        // Update player money
        val updatedPlayers = players.value.toMutableList()
        updatedPlayers[player.id] = player.copy(money = player.money + income)
        _players.value = updatedPlayers
    }
    
    /**
     * Process AI player turn
     */
    private fun processAITurn() {
        // Simple AI implementation
        val aiPlayer = getCurrentPlayer()
        val aiCells = grid.value.flatten().filter { it.owner == aiPlayer.id }
        
        // Try to attack from cells with more than 1 troop
        val attackingCells = aiCells.filter { it.troops > 1 }
        
        var attacksMade = 0
        
        for (fromCell in attackingCells) {
            if (attacksMade >= movesAvailable.value) break
            
            // Find adjacent cells that can be attacked
            val adjacentCells = getAdjacentCells(fromCell).filter { it.owner != aiPlayer.id }
            
            if (adjacentCells.isNotEmpty()) {
                // Attack weakest adjacent cell
                val targetCell = adjacentCells.minByOrNull { it.troops } ?: adjacentCells.random()
                
                if (canAttack(fromCell, targetCell)) {
                    executeAttack(fromCell, targetCell)
                    attacksMade++
                }
            }
        }
        
        // End turn after AI moves
        if (getCurrentPlayer().isAI) {
            endTurn()
        }
    }
    
    /**
     * Get adjacent cells to a given cell
     */
    private fun getAdjacentCells(cell: Cell): List<Cell> {
        val adjacent = mutableListOf<Cell>()
        val grid = grid.value
        
        // Check horizontally and vertically adjacent cells
        val directions = listOf(
            Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1),
            Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)
        )
        
        for ((dx, dy) in directions) {
            val newX = cell.x + dx
            val newY = cell.y + dy
            
            if (newX in 0 until settings.gridWidth && 
                newY in 0 until settings.gridHeight) {
                adjacent.add(grid[newY][newX])
            }
        }
        
        return adjacent
    }
    
    /**
     * Check if a player is defeated (no capitals left)
     */
    private fun checkPlayerDefeat(playerId: Int) {
        val playerCells = grid.value.flatten().filter { it.owner == playerId }
        val hasCapitals = playerCells.any { it.isCapital }
        
        if (!hasCapitals) {
            val updatedPlayers = players.value.toMutableList()
            updatedPlayers[playerId] = updatedPlayers[playerId].copy(isActive = false)
            _players.value = updatedPlayers
            
            _message.value = "${players.value[playerId].name} was defeated!"
        }
    }
    
    /**
     * Check if the game is over (only one player left)
     */
    private fun checkGameOver() {
        val activePlayers = players.value.count { it.isActive }
        
        if (activePlayers <= 1) {
            _gameState.value = GameState.GameOver
            
            val winner = players.value.find { it.isActive }
            _message.value = "Game Over! ${winner?.name ?: "Nobody"} wins!"
        }
    }
    
    /**
     * Update player statistics (cells owned, total troops, etc.)
     */
    private fun updatePlayerStats() {
        val updatedPlayers = players.value.toMutableList()
        
        players.value.forEachIndexed { index, player ->
            val ownedCells = grid.value.flatten().filter { it.owner == index }
            val capitals = ownedCells.count { it.isCapital }
            val totalTroops = ownedCells.sumOf { it.troops }
            
            updatedPlayers[index] = player.copy(
                totalCells = ownedCells.size,
                totalTroops = totalTroops,
                capitals = capitals
            )
        }
        
        _players.value = updatedPlayers
    }
    
    /**
     * Purchase troops for a specific cell
     */
    fun purchaseTroops(cell: Cell, count: Int) {
        if (gameState.value != GameState.Running) return
        
        val player = getCurrentPlayer()
        val cost = count * settings.troopCost
        
        if (player.money >= cost && cell.owner == player.id) {
            // Update player money
            val updatedPlayers = players.value.toMutableList()
            updatedPlayers[player.id] = player.copy(money = player.money - cost)
            _players.value = updatedPlayers
            
            // Update cell troops
            val mutGrid = grid.value.toMutableList().map { it.toMutableList() }
            mutGrid[cell.y][cell.x] = mutGrid[cell.y][cell.x].copy(
                troops = cell.troops + count
            )
            _grid.value = mutGrid.map { it.toList() }
            
            updatePlayerStats()
            _message.value = "Purchased $count troops for ${cost} money."
        } else {
            _message.value = "Not enough money or invalid cell selection."
        }
    }
    
    /**
     * Upgrade a cell to a capital
     */
    fun upgradeToCapital(cell: Cell) {
        if (gameState.value != GameState.Running) return
        
        val player = getCurrentPlayer()
        
        if (player.money >= settings.capitalCost && 
            cell.owner == player.id && 
            !cell.isCapital) {
            
            // Update player money
            val updatedPlayers = players.value.toMutableList()
            updatedPlayers[player.id] = player.copy(money = player.money - settings.capitalCost)
            _players.value = updatedPlayers
            
            // Update cell to capital
            val mutGrid = grid.value.toMutableList().map { it.toMutableList() }
            mutGrid[cell.y][cell.x] = mutGrid[cell.y][cell.x].copy(
                isCapital = true
            )
            _grid.value = mutGrid.map { it.toList() }
            
            updatePlayerStats()
            _message.value = "Upgraded cell to a capital for ${settings.capitalCost} money."
        } else {
            _message.value = "Not enough money or invalid cell selection."
        }
    }
}

/**
 * Enum representing possible game states
 */
enum class GameState {
    Loading,
    Running,
    Paused,
    GameOver
}
