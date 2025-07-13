package com.demo.fracasgame.game.models

import androidx.compose.ui.graphics.Color

/**
 * Represents a cell in the game grid
 */
data class Cell(
    val x: Int,
    val y: Int,
    var owner: Int = -1,  // -1 means no owner, otherwise player index
    var troops: Int = 0,
    var isCapital: Boolean = false
) {
    fun isEmpty() = owner == -1
    fun isOwnedBy(playerId: Int) = owner == playerId
}

/**
 * Represents a player in the game
 */
data class Player(
    val id: Int,
    val name: String,
    val color: Color,
    var capitals: Int = 1,
    var totalCells: Int = 0,
    var totalTroops: Int = 0,
    var isAI: Boolean = false,
    var isActive: Boolean = true,
    var money: Int = 0
)

/**
 * Types of moves a player can make
 */
sealed class Move {
    data class Attack(val from: Cell, val to: Cell) : Move()
    data class PlaceTroops(val cell: Cell, val count: Int) : Move()
    data class UpgradeCapital(val cell: Cell) : Move()
    object EndTurn : Move()
}

/**
 * Game settings to configure different aspects of gameplay
 */
data class GameSettings(
    val gridWidth: Int = 10,
    val gridHeight: Int = 10,
    val humanPlayers: Int = 1,
    val aiPlayers: Int = 3,
    val initialMoney: Int = 10,
    val initialTroopsPerCapital: Int = 5,
    val troopsPerTurn: Int = 3,
    val moneyPerCapital: Int = 2,
    val capitalCost: Int = 15,
    val troopCost: Int = 1
)
