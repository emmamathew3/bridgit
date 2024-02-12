# bridgit

The BridgIt Game is a two-player board game where the goal is to create a continuous bridge from one side of the board to the opposite side. Players take turns placing their bridges on the board, and the first player to create a connected path from their starting side to the opposite side wins.

## Game Board

- The game board is represented by an n by n grid, where each cell can be empty (0), belong to player 1 (1), or belong to player 2 (2).
- Each cell has connections to its left, right, above, and below neighbors.
- Tiles are 40 by 40 pixels in size.

## Mouse Clicking

- Players make moves by clicking on an empty cell to place their bridge.
- The game alternates between Player 1 and Player 2 turns.
- Bridges cannot be placed on the edges of the board.
