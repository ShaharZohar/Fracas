# Fracas Game

A strategic conquest game inspired by the classic game Fracas, implemented using Kotlin and Jetpack Compose UI.

## Game Overview

Fracas is a turn-based strategy game where players compete to conquer territory on a grid. Each player starts with a single capital cell and must expand their territory by attacking neighboring cells. The game combines elements of risk management, resource allocation, and tactical positioning.

## Features

- **Territory Conquest**: Attack and capture territory from neutral cells or opponent players
- **Resource Management**: Earn money each turn based on the number of capitals you control
- **Strategic Depth**: Choose between expanding quickly or fortifying your position
- **Multiple Players**: Support for both human and AI players
- **Interactive UI**: Modern Material 3 design with Jetpack Compose

## Game Rules

1. **Setup**: Each player starts with a single capital that produces money each turn
2. **Turns**: Players take turns attacking neighboring cells and purchasing upgrades
3. **Combat**: The outcome of attacks depends on the number of troops and some randomness
4. **Victory**: The last player with remaining capitals wins the game

## Controls

- **Select Your Cell**: Click on one of your cells with more than one troop to select it
- **Attack**: Click on an adjacent cell to attack it
- **Buy Troops**: Select one of your cells and use the "Buy Troops" button to purchase reinforcements
- **Upgrade Capital**: Select one of your cells and use the "Upgrade to Capital" button to create a new money-generating capital
- **End Turn**: Click the "End Turn" button when you're done with your moves

## Technical Implementation

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **State Management**: Kotlin Flow and StateFlow

## Getting Started

1. Clone the repository
2. Open the project in Android Studio
3. Build and run the app on an emulator or physical device

## Game Settings

You can customize the following settings when starting a new game:

- Grid size
- Number of human players
- Number of AI players

Enjoy playing Fracas!
