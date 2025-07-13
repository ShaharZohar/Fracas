package com.demo.fracasgame.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.fracasgame.game.engine.GameEngine
import com.demo.fracasgame.game.engine.GameState
import com.demo.fracasgame.game.models.Cell
import com.demo.fracasgame.game.models.GameSettings
import com.demo.fracasgame.game.models.Player
import com.demo.fracasgame.ui.theme.PlayerColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Fracas game that connects the UI with the game engine
 */
class GameViewModel : ViewModel() {

    private val _settings = MutableStateFlow(GameSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()
    
    // Create a default grid to ensure something is always displayed
    private val defaultGrid = createDefaultGrid(10, 10)
    
    private var gameEngine = GameEngine(_settings.value)
    
    // Initialize with default values to ensure something always displays
    private val _gameState = MutableStateFlow(GameState.Running)
    private val _grid = MutableStateFlow(defaultGrid)
    private val _players = MutableStateFlow(createDefaultPlayers())
    private val _currentPlayer = MutableStateFlow(0)
    private val _selectedCell = MutableStateFlow<Cell?>(null)
    private val _movesAvailable = MutableStateFlow(3)
    private val _message = MutableStateFlow("Welcome to Fracas Game!")
    
    // Expose game state for the UI
    val gameState: StateFlow<GameState> = _gameState
    val grid: StateFlow<List<List<Cell>>> = _grid
    val players: StateFlow<List<Player>> = _players
    val currentPlayer: StateFlow<Int> = _currentPlayer
    val selectedCell: StateFlow<Cell?> = _selectedCell
    val movesAvailable: StateFlow<Int> = _movesAvailable
    val message: StateFlow<String> = _message
    
    init {
        // Initialize with default grid immediately
        println("GameViewModel: Initializing with default grid")
    }
    
    // Helper method to create a default grid
    private fun createDefaultGrid(width: Int, height: Int): List<List<Cell>> {
        val grid = List(height) { y ->
            List(width) { x ->
                Cell(x, y)
            }
        }
        
        // Create a mutable copy to add some default cells
        val mutableGrid = grid.toMutableList().map { it.toMutableList() }
        
        // Add a few owned cells
        for (playerId in 0..3) {
            val x = (playerId * 2) + 1
            val y = (playerId * 2) + 1
            if (x < width && y < height) {
                mutableGrid[y][x] = Cell(
                    x = x,
                    y = y,
                    owner = playerId,
                    troops = 5,
                    isCapital = playerId < 2
                )
            }
        }
        
        return mutableGrid.map { it.toList() }
    }
    
    // Helper method to create default players
    private fun createDefaultPlayers(): List<Player> {
        return listOf(
            Player(id = 0, name = "Player 1", color = PlayerColors[0], isAI = false, money = 10),
            Player(id = 1, name = "AI 1", color = PlayerColors[1], isAI = true, money = 10),
            Player(id = 2, name = "AI 2", color = PlayerColors[2], isAI = true, money = 10),
            Player(id = 3, name = "AI 3", color = PlayerColors[3], isAI = true, money = 10)
        )
    }
    
    /**
     * Initialize the game with current settings
     */
    fun initializeGame() {
        // Always use hardcoded default settings to ensure the game starts properly
        val defaultSettings = GameSettings(
            gridWidth = 10,
            gridHeight = 10,
            humanPlayers = 1,
            aiPlayers = 3,
            initialMoney = 10,
            initialTroopsPerCapital = 5,
            troopsPerTurn = 3,
            moneyPerCapital = 2,
            capitalCost = 15,
            troopCost = 1
        )
        
        _settings.value = defaultSettings
        
        try {
            println("GameViewModel: Creating new game engine")
            // Create a completely new game engine
            gameEngine = GameEngine(defaultSettings)
            
            // Initialize on the main thread to ensure it completes immediately
            println("GameViewModel: Initializing game engine")
            gameEngine.initializeGame()
            
            // Update our local state with the game engine's state
            println("GameViewModel: Updating local state from engine")
            _gameState.value = gameEngine.gameState.value
            _grid.value = gameEngine.grid.value.ifEmpty { defaultGrid }
            _players.value = gameEngine.players.value.ifEmpty { createDefaultPlayers() }
            _currentPlayer.value = gameEngine.currentPlayer.value
            _movesAvailable.value = gameEngine.movesAvailable.value
            _message.value = "Game started! " + gameEngine.message.value
            
            println("GameViewModel: Initialization complete")
        } catch (e: Exception) {
            println("GameViewModel: Error initializing game: ${e.message}")
            // Ensure we have something to display even if initialization fails
            _gameState.value = GameState.Running
            _grid.value = defaultGrid
            _players.value = createDefaultPlayers()
            _currentPlayer.value = 0
            _movesAvailable.value = 3
            _message.value = "Error initializing game. Using default board."
        }
    }
    
    /**
     * Reload the game board while preserving current settings
     */
    fun reloadBoard() {
        try {
            println("GameViewModel: Reloading board")
            
            // Create a new game engine with current settings
            gameEngine = GameEngine(_settings.value)
            gameEngine.initializeGame()
            
            // Update our local state with the game engine's state
            _gameState.value = gameEngine.gameState.value
            _grid.value = gameEngine.grid.value.ifEmpty { defaultGrid }
            _players.value = gameEngine.players.value.ifEmpty { createDefaultPlayers() }
            _currentPlayer.value = gameEngine.currentPlayer.value
            _movesAvailable.value = gameEngine.movesAvailable.value
            _message.value = "Board reloaded successfully!"
        } catch (e: Exception) {
            println("GameViewModel: Error reloading board: ${e.message}")
            _message.value = "Error reloading board. Try again."
        }
    }

    /**
     * Handle cell click event
     */
    fun onCellClick(cell: Cell) {
        try {
            gameEngine.onCellClick(cell)
            
            // Update our local state with the game engine's state
            _gameState.value = gameEngine.gameState.value
            _grid.value = gameEngine.grid.value
            _selectedCell.value = gameEngine.selectedCell.value
            _movesAvailable.value = gameEngine.movesAvailable.value
            _message.value = gameEngine.message.value
        } catch (e: Exception) {
            println("GameViewModel: Error handling cell click: ${e.message}")
            _message.value = "Error processing move. Try reloading the board."
        }
    }
    
    /**
     * End the current player's turn
     */
    fun endTurn() {
        try {
            gameEngine.endTurn()
            
            // Update our local state with the game engine's state
            _gameState.value = gameEngine.gameState.value
            _grid.value = gameEngine.grid.value
            _players.value = gameEngine.players.value
            _currentPlayer.value = gameEngine.currentPlayer.value
            _selectedCell.value = gameEngine.selectedCell.value
            _movesAvailable.value = gameEngine.movesAvailable.value
            _message.value = gameEngine.message.value
        } catch (e: Exception) {
            println("GameViewModel: Error ending turn: ${e.message}")
            _message.value = "Error ending turn. Try reloading the board."
        }
    }
    
    /**
     * Purchase troops for the selected cell
     */
    fun purchaseTroops(cell: Cell, count: Int) {
        try {
            if (count > 0) {
                gameEngine.purchaseTroops(cell, count)
                
                // Update our local state with the game engine's state
                _gameState.value = gameEngine.gameState.value
                _grid.value = gameEngine.grid.value
                _players.value = gameEngine.players.value
                _message.value = gameEngine.message.value
            }
        } catch (e: Exception) {
            println("GameViewModel: Error purchasing troops: ${e.message}")
            _message.value = "Error purchasing troops. Try reloading the board."
        }
    }
    
    /**
     * Upgrade a cell to a capital
     */
    fun upgradeToCapital(cell: Cell) {
        try {
            gameEngine.upgradeToCapital(cell)
            
            // Update our local state with the game engine's state
            _gameState.value = gameEngine.gameState.value
            _grid.value = gameEngine.grid.value
            _players.value = gameEngine.players.value
            _message.value = gameEngine.message.value
        } catch (e: Exception) {
            println("GameViewModel: Error upgrading capital: ${e.message}")
            _message.value = "Error upgrading capital. Try reloading the board."
        }
    }
    
    /**
     * Update game settings
     */
    fun updateSettings(settings: GameSettings) {
        _settings.value = settings
    }
}
