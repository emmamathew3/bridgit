//n by n arraylist to represent all parts of the game and the colors
//0 for blank. 1 for player1. 2 for player2.
//each cell has a left bottom right and top
//each tile is 40 by 40
//player1 is left to right
//player2 is top to bottom

//mouse clicking
//maybe the game has a boolean field to tell which player it is
//i need to convert my mouse position to coords

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Utils class to convert from coordinates to row and column number
//supports other row and column number operations
class CoordUtils {

  // method to convert row or column number to x or y coordinate
  // takes in the total number of rows and columns
  int convertIndexToCoord(int index, int dim) {
    if (index >= 0 && index < dim) {
      return index * 40 + 20;
    }
    else {
      throw new IllegalArgumentException("Index should be between 0 and " + dim);
    }
  }

  // Converts a coordinate to an index
  int convertCoordToIndex(int coord) {
    return coord / 40;
  }

}

//Utils class to perform a breadth-first-search 
//on the tiles
class ArrayListTileUtils {
  ArrayList<ArrayList<Tile>> tiles;

  ArrayListTileUtils(ArrayList<ArrayList<Tile>> tiles) {
    this.tiles = tiles;
  }

  // returns whether a path exists for the specified player in the board
  boolean hasPathBFS(int player) {
    ArrayDeque<Tile> worklist = new ArrayDeque<Tile>();
    ArrayList<Tile> seen = new ArrayList<Tile>();

    // gets the starting tiles for the specified player and stores them in a list
    ArrayList<Tile> startTiles = new ArrayList<Tile>();

    // go down the left side of the board and collect every odd index
    if (player == 1) {
      for (int i = 1; i < this.tiles.size(); i += 2) {
        startTiles.add(this.tiles.get(i).get(0));
      }
    }
    // go down the top row of the board and collect every odd index
    else {
      for (int i = 1; i < this.tiles.size(); i += 2) {
        startTiles.add(this.tiles.get(0).get(i));
      }
    }

    // performs the search on every tile
    while (startTiles.size() > 0) {

      worklist.add(startTiles.get(0));
      startTiles.remove(0);

      while (worklist.size() > 0) {

        Tile next = worklist.removeFirst();

        if (next.hasPathBFS(worklist, seen, player, this.tiles.size())) {
          return true;
        }
      }
    }

    return false;
  }
}

//class that represents a BridgIt Game
class BridgIt extends World {

  ArrayList<ArrayList<Tile>> tiles;
  boolean player1;

  // Constructor for a BridgIt game that takes in a board dimension
  BridgIt(int n) {
    if (n % 2 == 0 || n < 3) {
      throw new IllegalArgumentException("Board length is not a valid number.");
    }

    this.tiles = new ArrayList<ArrayList<Tile>>();

    for (int i = 0; i < n; i++) {
      ArrayList<Tile> rowTiles = new ArrayList<Tile>();

      for (int j = 0; j < n; j++) {
        // if i is even, if j is odd, make it player 2
        // if i is odd, if j is even, make it player1
        // otherwise make it 0
        int num;

        if (i % 2 == 0 && j % 2 == 1) {
          num = 2;
        }
        else if (i % 2 == 1 && j % 2 == 0) {
          num = 1;
        }
        else {
          num = 0;
        }
        rowTiles.add(new Tile(num, i, j));
      }
      this.tiles.add(rowTiles);
    }

    // setting the tile links
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {

        // if its in the top row
        if (i != 0) {
          this.tiles.get(i).get(j).updateAbove(this.tiles.get(i - 1).get(j));
        }

        // if its in the bottom row
        if (i != n - 1) {
          this.tiles.get(i).get(j).updateBelow(this.tiles.get(i + 1).get(j));
        }

        // left row
        if (j != 0) {
          this.tiles.get(i).get(j).updateLeft(this.tiles.get(i).get(j - 1));
        }

        // right row
        if (j != n - 1) {
          this.tiles.get(i).get(j).updateRight(this.tiles.get(i).get(j + 1));
        }
      }
    }

    this.player1 = true;
  }

  // Constructor that takes in an arraylist of tiles
  BridgIt(ArrayList<ArrayList<Tile>> n) {
    this.tiles = n;
    this.player1 = true;

  }

  // makes a scene of the background
  public WorldScene makeScene() {
    // generate a background of n by n times 40

    WorldScene w = new WorldScene(this.tiles.size() * 40, this.tiles.size() * 40);

    for (int r = 0; r < this.tiles.size(); r++) {
      for (int c = 0; c < this.tiles.size(); c++) {
        w = this.tiles.get(r).get(c).drawAt(c, r, this.tiles.size(), w);
      }
    }

    return w;
  }

  // EFFECT: handles mouse clicking
  public void onMouseReleased(Posn pos) {
    CoordUtils u = new CoordUtils();
    int row = u.convertCoordToIndex(pos.y);
    int col = u.convertCoordToIndex(pos.x);

    boolean changed = tiles.get(row).get(col).onMouseReleased(this.player1, this.tiles.size());

    // only change it when a tile has been changed
    // only check for path when the tile has been changed
    if (changed) {
      this.player1 = !(this.player1);

      ArrayListTileUtils tileU = new ArrayListTileUtils(this.tiles);
      if (tileU.hasPathBFS(1)) {
        this.endOfWorld("One");
      }
      else if (tileU.hasPathBFS(2)) {
        this.endOfWorld("Two");
      }
    }
  }

  // Ends the game
  public WorldScene lastScene(String msg) {
    WorldScene w = this.makeScene();
    if (msg.equals("One")) {
      w.placeImageXY(new TextImage("Player 1 won!", 30, Color.black), this.tiles.size() * 20,
          this.tiles.size() * 20);
      return w;
    }
    else {
      w.placeImageXY(new TextImage("Player 2 won!", 30, Color.black), this.tiles.size() * 20,
          this.tiles.size() * 20);
      return w;
    }
  }
}

//represents a tile in the game
//A tile will have a player: either 0, representing a blank tile, 1, or 2
class Tile {
  int player;
  Tile above;
  Tile below;
  Tile left;
  Tile right;
  int row;
  int col;

  static final Color RED = new Color(229, 89, 52);
  static final Color GREEN = new Color(140, 179, 105);

  // Constructor that just takes in a player
  Tile(int player, int row, int col) {
    this.player = player;
    this.above = this;
    this.below = this;
    this.left = this;
    this.right = this;
    this.row = row;
    this.col = col;
  }

  // Constructor that takes in all four other locations
  // EFFECT: updates the given tiles with their neighboring locations
  Tile(int player, Tile above, Tile below, Tile left, Tile right, int row, int col) {
    this.player = player;
    this.above = above;
    this.below = below;
    this.left = left;
    this.right = right;
    this.above.updateBelow(this);
    this.below.updateAbove(this);
    this.left.updateRight(this);
    this.right.updateLeft(this);
    this.row = row;
    this.col = col;
  }

  // EFFECT: updates the left field of the tile to the given tile
  void updateLeft(Tile that) {
    this.left = that;
  }

  // EFFECT: updates the right field of the tile to the given tile
  void updateRight(Tile that) {
    this.right = that;
  }

  // EFFECT: updates the above field of the tile to the given tile
  void updateAbove(Tile that) {
    this.above = that;
  }

  // EFFECT: updates the below field of the tile to the given tile
  void updateBelow(Tile that) {
    this.below = that;
  }

  // draws the image of the tile
  WorldImage drawTile() {
    if (this.player == 0) {
      return new RectangleImage(40, 40, OutlineMode.SOLID, Color.white);
    }
    else if (this.player == 1) {
      return new RectangleImage(40, 40, OutlineMode.SOLID, RED);
    }
    else {
      return new RectangleImage(40, 40, OutlineMode.SOLID, GREEN);
    }
  }

  // Draws this tile onto the background at the specified logical coordinates
  WorldScene drawAt(int col, int row, int dim, WorldScene background) {
    background.placeImageXY(this.drawTile(), new CoordUtils().convertIndexToCoord(col, dim),
        new CoordUtils().convertIndexToCoord(row, dim));
    return background;
  }

  // returns whether a tile has been changed when a player has clicked on it
  // EFFECT: changes the tile to the appropriate player
  boolean onMouseReleased(boolean player1, int dim) {
    if (this.row == 0 || this.col == 0 || this.row == dim - 1 || this.col == dim - 1) {
      return false;
    }

    if (this.player != 0) {
      return false;
    }

    if (player1) {
      this.player = 1;
    }
    else {
      this.player = 2;
    }
    return true;
  }

  // Returns whether a path has been found to the end
  // EFFECT: changes the worklist and the seen tiles according to the connections
  // of this tile
  // adds valid tiles to the worklist
  // adds tiles to seen
  boolean hasPathBFS(ArrayDeque<Tile> worklist, ArrayList<Tile> seen, int player, int dim) {

    // is the player of this tile the same as the player
    // we are performing the BFS search for?
    if (this.player == player) {
      // if player is 1, are we at the rightmost column?
      if (player == 1 && this.col == dim - 1) {
        return true;
      }

      // if player is 2, are we at the bottom-most row?
      else if (player == 2 && this.row == dim - 1) {
        return true;
      }

      // if we've seen this tile before in our search, we return false
      else if (!seen.contains(this)) {
        // gets all the neighbors of the tile regardless of player
        ArrayList<Tile> neighbors = new ArrayList<Tile>();
        neighbors.add(this.left);
        neighbors.add(this.right);
        neighbors.add(this.above);
        neighbors.add(this.below);

        // goes through each neighbor and adds them to the worklist
        // only if the player is the same
        for (int i = 0; i < 4; i++) {
          if (neighbors.get(i).player == player) {
            worklist.addLast(neighbors.get(i));
          }
        }
        // adds this tile to the list of tiles we've checked
        seen.add(this);
      }
    }
    // one of the last three statements ran
    return false;
  }

  // equals method for Tiles
  public boolean equals(Object other) {
    if (other instanceof Tile) {
      Tile t2 = (Tile) other;
      return t2.player == this.player && t2.row == this.row && t2.col == this.col;
    }
    else {
      return false;
    }
  }

  // override hashCode
  public int hashCode() {
    return (this.player * 29 + this.row * 31 + this.col * 97) * 101;
  }
}

//to run the game
class ExampleBridgIt {
  void testGame(Tester t) {
    int n = 11;
    BridgIt g = new BridgIt(n);
    g.bigBang(n * 40, n * 40);
  }
}

//Examples for the game
class Examples {
  BridgIt game1;
  ArrayList<ArrayList<Tile>> tiles1;
  WorldScene initWorld;
  BridgIt game2;
  BridgIt game3;
  BridgIt game4;

  ArrayList<ArrayList<Tile>> tileOnMouseReleased;

  // initializes data
  void initData() {
    this.game1 = new BridgIt(5);
    this.tiles1 = this.game1.tiles;
    this.initWorld = new WorldScene(400, 400);
    this.game2 = new BridgIt(19);
    this.game3 = new BridgIt(7);
    this.game4 = new BridgIt(21);
  }

  // tests convertIndexToCoord
  void testConvertIndexToCoord(Tester t) {
    CoordUtils u = new CoordUtils();
    t.checkExpect(u.convertIndexToCoord(0, 1), 20);
    t.checkExpect(u.convertIndexToCoord(0, 4), 20);
    t.checkExpect(u.convertIndexToCoord(0, 5), 20);
    t.checkExpect(u.convertIndexToCoord(1, 2), 60);
    t.checkExpect(u.convertIndexToCoord(1, 5), 60);
    t.checkExpect(u.convertIndexToCoord(2, 3), 100);
    t.checkExpect(u.convertIndexToCoord(2, 5), 100);
    t.checkExpect(u.convertIndexToCoord(3, 4), 140);
    t.checkExpect(u.convertIndexToCoord(3, 5), 140);
    t.checkExpect(u.convertIndexToCoord(4, 5), 180);
    t.checkException(new IllegalArgumentException("Index should be between 0 and 5"), u,
        "convertIndexToCoord", -1, 5);
    t.checkException(new IllegalArgumentException("Index should be between 0 and 3"), u,
        "convertIndexToCoord", 5, 3);
    t.checkException(new IllegalArgumentException("Index should be between 0 and 5"), u,
        "convertIndexToCoord", 5, 5);

  }

  // tests constructor for BridgIt
  // makes sure exception is tested
  // makes sure all tiles are linked up correctly
  void testConstructorBridgIt(Tester t) {
    this.initData();
    t.checkConstructorException(new IllegalArgumentException("Board length is not a valid number."),
        "BridgIt", -2);
    t.checkConstructorException(new IllegalArgumentException("Board length is not a valid number."),
        "BridgIt", -1);
    t.checkConstructorException(new IllegalArgumentException("Board length is not a valid number."),
        "BridgIt", 0);
    t.checkConstructorException(new IllegalArgumentException("Board length is not a valid number."),
        "BridgIt", 1);
    t.checkConstructorException(new IllegalArgumentException("Board length is not a valid number."),
        "BridgIt", 2);

    t.checkConstructorException(new IllegalArgumentException("Board length is not a valid number."),
        "BridgIt", 4);
    t.checkConstructorException(new IllegalArgumentException("Board length is not a valid number."),
        "BridgIt", 6);
    t.checkConstructorException(new IllegalArgumentException("Board length is not a valid number."),
        "BridgIt", 784);
    t.checkConstructorException(new IllegalArgumentException("Board length is not a valid number."),
        "BridgIt", 1024);
    t.checkConstructorException(new IllegalArgumentException("Board length is not a valid number."),
        "BridgIt", 2048);

    // checking whether the board size is correct
    t.checkExpect(this.game1.tiles.size(), 5);
    t.checkExpect(this.game1.tiles.get(0).size(), 5);

    t.checkExpect(this.game2.tiles.size(), 19);
    t.checkExpect(this.game2.tiles.get(0).size(), 19);

    // check that it alternates players
    t.checkExpect(this.game1.tiles.get(0).get(0).player, 0);
    t.checkExpect(this.game1.tiles.get(0).get(1).player, 2);
    t.checkExpect(this.game1.tiles.get(0).get(2).player, 0);
    t.checkExpect(this.game1.tiles.get(0).get(3).player, 2);
    t.checkExpect(this.game1.tiles.get(0).get(4).player, 0);

    t.checkExpect(this.game1.tiles.get(1).get(0).player, 1);
    t.checkExpect(this.game1.tiles.get(1).get(1).player, 0);
    t.checkExpect(this.game1.tiles.get(1).get(2).player, 1);
    t.checkExpect(this.game1.tiles.get(1).get(3).player, 0);
    t.checkExpect(this.game1.tiles.get(1).get(4).player, 1);

    t.checkExpect(this.game2.tiles.get(0).get(0).player, 0);
    t.checkExpect(this.game2.tiles.get(0).get(1).player, 2);
    t.checkExpect(this.game2.tiles.get(0).get(2).player, 0);
    t.checkExpect(this.game2.tiles.get(0).get(3).player, 2);
    t.checkExpect(this.game2.tiles.get(0).get(4).player, 0);

    t.checkExpect(this.game2.tiles.get(1).get(0).player, 1);
    t.checkExpect(this.game2.tiles.get(1).get(1).player, 0);
    t.checkExpect(this.game2.tiles.get(1).get(2).player, 1);
    t.checkExpect(this.game2.tiles.get(1).get(3).player, 0);
    t.checkExpect(this.game2.tiles.get(1).get(4).player, 1);
  }

  // tests the links for the tiles in BridgIt
  void testLinks(Tester t) {
    this.initData();

    t.checkExpect(this.tiles1.get(0).get(0).above, this.tiles1.get(0).get(0));
    t.checkExpect(this.tiles1.get(0).get(1).above, this.tiles1.get(0).get(1));
    t.checkExpect(this.tiles1.get(0).get(4).above, this.tiles1.get(0).get(4));
    t.checkExpect(this.tiles1.get(1).get(0).above, this.tiles1.get(0).get(0));
    t.checkExpect(this.tiles1.get(2).get(0).above, this.tiles1.get(1).get(0));
    t.checkExpect(this.tiles1.get(4).get(0).above, this.tiles1.get(3).get(0));
    t.checkExpect(this.tiles1.get(4).get(4).above, this.tiles1.get(3).get(4));

    t.checkExpect(this.tiles1.get(4).get(0).below, this.tiles1.get(4).get(0));
    t.checkExpect(this.tiles1.get(4).get(1).below, this.tiles1.get(4).get(1));
    t.checkExpect(this.tiles1.get(4).get(4).below, this.tiles1.get(4).get(4));
    t.checkExpect(this.tiles1.get(3).get(0).below, this.tiles1.get(4).get(0));
    t.checkExpect(this.tiles1.get(2).get(2).below, this.tiles1.get(3).get(2));
    t.checkExpect(this.tiles1.get(0).get(0).below, this.tiles1.get(1).get(0));
    t.checkExpect(this.tiles1.get(0).get(4).below, this.tiles1.get(1).get(4));

    t.checkExpect(this.tiles1.get(0).get(0).left, this.tiles1.get(0).get(0));
    t.checkExpect(this.tiles1.get(1).get(0).left, this.tiles1.get(1).get(0));
    t.checkExpect(this.tiles1.get(4).get(0).left, this.tiles1.get(4).get(0));
    t.checkExpect(this.tiles1.get(0).get(1).left, this.tiles1.get(0).get(0));
    t.checkExpect(this.tiles1.get(4).get(4).left, this.tiles1.get(4).get(3));
    t.checkExpect(this.tiles1.get(0).get(4).left, this.tiles1.get(0).get(3));

    t.checkExpect(this.tiles1.get(0).get(4).right, this.tiles1.get(0).get(4));
    t.checkExpect(this.tiles1.get(1).get(4).right, this.tiles1.get(1).get(4));
    t.checkExpect(this.tiles1.get(4).get(4).right, this.tiles1.get(4).get(4));
    t.checkExpect(this.tiles1.get(0).get(1).right, this.tiles1.get(0).get(2));
    t.checkExpect(this.tiles1.get(3).get(3).right, this.tiles1.get(3).get(4));
    t.checkExpect(this.tiles1.get(0).get(0).right, this.tiles1.get(0).get(1));
    t.checkExpect(this.tiles1.get(4).get(0).right, this.tiles1.get(4).get(1));
  }

  // tests makeScene
  void testMakeScene(Tester t) {
    this.initData();
    WorldScene w1 = new WorldScene(5 * 40, 5 * 40);

    for (int r = 0; r < 5; r++) {
      for (int c = 0; c < 5; c++) {
        w1 = this.game1.tiles.get(r).get(c).drawAt(c, r, 5, w1);
      }
    }
    t.checkExpect(this.game1.makeScene(), w1);

    w1 = new WorldScene(19 * 40, 19 * 40);

    for (int r = 0; r < 19; r++) {
      for (int c = 0; c < 19; c++) {
        w1 = this.game2.tiles.get(r).get(c).drawAt(c, r, 19, w1);
      }
    }
    t.checkExpect(this.game2.makeScene(), w1);

    w1 = new WorldScene(7 * 40, 7 * 40);

    for (int r = 0; r < 7; r++) {
      for (int c = 0; c < 7; c++) {
        w1 = this.game3.tiles.get(r).get(c).drawAt(c, r, 7, w1);
      }
    }
    t.checkExpect(this.game3.makeScene(), w1);

    w1 = new WorldScene(21 * 40, 21 * 40);

    for (int r = 0; r < 21; r++) {
      for (int c = 0; c < 21; c++) {
        w1 = this.game4.tiles.get(r).get(c).drawAt(c, r, 21, w1);
      }
    }
    t.checkExpect(this.game4.makeScene(), w1);
  }

  // tests updateLeft, updateRight, updateAbove, updateBelow
  void testUpdate(Tester t) {
    Tile t1 = new Tile(0, 4, 4);
    t1.updateAbove(t1);
    t1.updateBelow(t1);
    t1.updateLeft(t1);
    t1.updateRight(t1);
    t.checkExpect(t1.above, t1);
    t.checkExpect(t1.below, t1);
    t.checkExpect(t1.left, t1);
    t.checkExpect(t1.right, t1);

    Tile t2 = new Tile(0, 3, 4);
    Tile t3 = new Tile(0, 5, 4);
    Tile t4 = new Tile(0, 4, 3);
    Tile t5 = new Tile(0, 4, 5);
    t1.updateAbove(t2);
    t1.updateBelow(t3);
    t1.updateLeft(t4);
    t1.updateRight(t5);
    t.checkExpect(t1.above, t2);
    t.checkExpect(t1.below, t3);
    t.checkExpect(t1.left, t4);
    t.checkExpect(t1.right, t5);

    Tile t6 = new Tile(1, 3, 4);
    Tile t7 = new Tile(2, 5, 4);
    Tile t8 = new Tile(1, 4, 3);
    Tile t9 = new Tile(2, 4, 5);
    t1.updateAbove(t6);
    t1.updateBelow(t7);
    t1.updateLeft(t8);
    t1.updateRight(t9);
    t.checkExpect(t1.above, t6);
    t.checkExpect(t1.below, t7);
    t.checkExpect(t1.left, t8);
    t.checkExpect(t1.right, t9);
  }

  // tests drawTile
  void testDrawTile(Tester t) {
    Color RED = new Color(229, 89, 52);
    Color GREEN = new Color(140, 179, 105);
    t.checkExpect(new Tile(0, 0, 0).drawTile(),
        new RectangleImage(40, 40, OutlineMode.SOLID, Color.white));
    t.checkExpect(new Tile(1, 9, 10).drawTile(),
        new RectangleImage(40, 40, OutlineMode.SOLID, RED));
    t.checkExpect(new Tile(2, 67, 89).drawTile(),
        new RectangleImage(40, 40, OutlineMode.SOLID, GREEN));
  }

  // tests drawAt
  void testDrawAt(Tester t) {
    this.initData();
    this.initWorld.placeImageXY(new Tile(0, 2, 3).drawTile(),
        new CoordUtils().convertIndexToCoord(2, 5), new CoordUtils().convertIndexToCoord(3, 5));
    t.checkExpect(new Tile(0, 2, 3).drawAt(2, 3, 5, new WorldScene(400, 400)), this.initWorld);

    this.initWorld.placeImageXY(new Tile(1, 1, 4).drawTile(),
        new CoordUtils().convertIndexToCoord(1, 5), new CoordUtils().convertIndexToCoord(4, 5));
    t.checkExpect(new Tile(0, 2, 3).drawAt(2, 3, 5,
        new Tile(1, 1, 4).drawAt(1, 4, 5, new WorldScene(400, 400))), this.initWorld);

    this.initWorld.placeImageXY(new Tile(2, 3, 4).drawTile(),
        new CoordUtils().convertIndexToCoord(3, 5), new CoordUtils().convertIndexToCoord(4, 5));
    t.checkExpect(new Tile(0, 2, 3).drawAt(2, 3, 5, new Tile(1, 4, 5).drawAt(1, 4, 5,
        new Tile(2, 3, 4).drawAt(3, 4, 5, new WorldScene(400, 400)))), this.initWorld);

    this.initWorld = new WorldScene(400, 400);
    this.initWorld.placeImageXY(new Tile(1, 1, 2).drawTile(),
        new CoordUtils().convertIndexToCoord(1, 5), new CoordUtils().convertIndexToCoord(2, 5));
    t.checkExpect(new Tile(1, 1, 2).drawAt(1, 2, 5, new WorldScene(400, 400)), this.initWorld);
  }

  // tests onMouseReleased for Tiles
  void testOnMouseReleasedTile(Tester t) {
    this.initData();
    ArrayList<ArrayList<Tile>> tiles = this.game1.tiles;

    // test 6 tiles that on edges and check that their stuff was not updated
    Tile before = tiles.get(0).get(0);
    t.checkExpect(tiles.get(0).get(0).onMouseReleased(false, 5), false);
    t.checkExpect(tiles.get(0).get(0), before);

    before = tiles.get(0).get(1);
    t.checkExpect(tiles.get(0).get(1).onMouseReleased(true, 5), false);
    t.checkExpect(tiles.get(0).get(1), before);

    before = tiles.get(0).get(4);
    t.checkExpect(tiles.get(0).get(4).onMouseReleased(true, 5), false);
    t.checkExpect(tiles.get(0).get(4), before);

    before = tiles.get(2).get(4);
    t.checkExpect(tiles.get(2).get(4).onMouseReleased(true, 5), false);
    t.checkExpect(tiles.get(2).get(4), before);

    before = tiles.get(1).get(0);
    t.checkExpect(tiles.get(1).get(0).onMouseReleased(false, 5), false);
    t.checkExpect(tiles.get(1).get(0), before);

    before = tiles.get(4).get(0);
    t.checkExpect(tiles.get(4).get(0).onMouseReleased(false, 5), false);
    t.checkExpect(tiles.get(4).get(0), before);

    before = tiles.get(4).get(1);
    t.checkExpect(tiles.get(4).get(1).onMouseReleased(true, 5), false);
    t.checkExpect(tiles.get(4).get(1), before);

    before = tiles.get(4).get(2);
    t.checkExpect(tiles.get(4).get(2).onMouseReleased(true, 5), false);
    t.checkExpect(tiles.get(4).get(2), before);

    // test 2 more tiles that are of a color so they can't be changed
    before = tiles.get(1).get(2);
    t.checkExpect(tiles.get(1).get(2).onMouseReleased(true, 5), false);
    t.checkExpect(tiles.get(1).get(2), before);

    before = tiles.get(3).get(2);
    t.checkExpect(tiles.get(3).get(2).onMouseReleased(true, 5), false);
    t.checkExpect(tiles.get(3).get(2), before);

    // test 3 tiles that change
    before = tiles.get(1).get(1);
    t.checkExpect(tiles.get(1).get(1).onMouseReleased(true, 5), true);
    t.checkExpect(tiles.get(1).get(1).player, 1);

    before = tiles.get(2).get(2);
    t.checkExpect(tiles.get(2).get(2).onMouseReleased(true, 5), true);
    t.checkExpect(tiles.get(2).get(2).player, 1);

    before = tiles.get(1).get(3);
    t.checkExpect(tiles.get(1).get(3).onMouseReleased(false, 5), true);
    t.checkExpect(tiles.get(1).get(3).player, 2);
  }

  // tests convert coordToIndex
  // a 5 by 5 grid was used and 2 points in each top square were tested
  // since convertCoordToIndex only takes in one number
  void testConvertCoordToIndex(Tester t) {
    this.initData();
    CoordUtils c = new CoordUtils();
    t.checkExpect(c.convertCoordToIndex(0), 0);
    t.checkExpect(c.convertCoordToIndex(25), 0);
    t.checkExpect(c.convertCoordToIndex(40), 1);
    t.checkExpect(c.convertCoordToIndex(50), 1);
    t.checkExpect(c.convertCoordToIndex(95), 2);
    t.checkExpect(c.convertCoordToIndex(110), 2);
    t.checkExpect(c.convertCoordToIndex(125), 3);
    t.checkExpect(c.convertCoordToIndex(135), 3);
    t.checkExpect(c.convertCoordToIndex(170), 4);
    t.checkExpect(c.convertCoordToIndex(180), 4);
  }

  // tests hasPathBFS in ArrayUtils
  void testHasPathBFS1(Tester t) {
    this.initData();

    // game w/ player1 winning, straight path
    ArrayList<Tile> row1 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 0, 0), new Tile(2, 0, 1),
        new Tile(0, 0, 2), new Tile(2, 0, 3), new Tile(0, 0, 4)));
    ArrayList<Tile> row2 = new ArrayList<Tile>(Arrays.asList(new Tile(1, 1, 0), new Tile(2, 1, 1),
        new Tile(1, 1, 2), new Tile(0, 1, 3), new Tile(1, 1, 4)));
    ArrayList<Tile> row3 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 2, 0), new Tile(2, 2, 1),
        new Tile(1, 2, 2), new Tile(2, 2, 3), new Tile(0, 2, 4)));
    ArrayList<Tile> row4 = new ArrayList<Tile>(Arrays.asList(new Tile(1, 3, 0), new Tile(2, 3, 1),
        new Tile(1, 3, 2), new Tile(1, 3, 3), new Tile(1, 3, 4)));
    ArrayList<Tile> row5 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 4, 0), new Tile(2, 4, 1),
        new Tile(0, 4, 2), new Tile(2, 4, 3), new Tile(0, 4, 4)));
    ArrayList<ArrayList<Tile>> winningBoard1 = new ArrayList<ArrayList<Tile>>(
        Arrays.asList(row1, row2, row3, row4, row5));

    this.linkTiles(winningBoard1);
    ArrayListTileUtils u = new ArrayListTileUtils(winningBoard1);

    t.checkExpect(u.hasPathBFS(1), false);
    t.checkExpect(u.hasPathBFS(2), true);

    // game w/ player1 winning, curvy path
    ArrayList<Tile> row6 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 1, 1), new Tile(2, 1, 2),
        new Tile(0, 1, 3), new Tile(2, 1, 4), new Tile(0, 1, 5)));
    ArrayList<Tile> row7 = new ArrayList<Tile>(Arrays.asList(new Tile(1, 2, 1), new Tile(2, 2, 2),
        new Tile(1, 2, 3), new Tile(1, 2, 4), new Tile(1, 2, 5)));
    ArrayList<Tile> row8 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 3, 1), new Tile(2, 3, 2),
        new Tile(1, 3, 3), new Tile(2, 3, 4), new Tile(0, 3, 5)));
    ArrayList<Tile> row9 = new ArrayList<Tile>(Arrays.asList(new Tile(1, 4, 1), new Tile(1, 4, 2),
        new Tile(1, 4, 3), new Tile(2, 4, 4), new Tile(1, 4, 5)));
    ArrayList<Tile> row10 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 5, 1), new Tile(2, 5, 2),
        new Tile(0, 5, 3), new Tile(2, 5, 4), new Tile(0, 5, 5)));
    ArrayList<ArrayList<Tile>> winningBoard2 = new ArrayList<ArrayList<Tile>>(
        Arrays.asList(row6, row7, row8, row9, row10));

    this.linkTiles(winningBoard2);
    ArrayListTileUtils u2 = new ArrayListTileUtils(winningBoard2);
    t.checkExpect(u2.hasPathBFS(1), true);
    t.checkExpect(u2.hasPathBFS(2), false);

    // game w/ player2 winning
    ArrayList<Tile> row16 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 1, 1), new Tile(2, 1, 2),
        new Tile(0, 1, 3), new Tile(2, 1, 4), new Tile(0, 1, 5)));
    ArrayList<Tile> row17 = new ArrayList<Tile>(Arrays.asList(new Tile(1, 2, 1), new Tile(1, 2, 2),
        new Tile(1, 2, 3), new Tile(2, 2, 4), new Tile(1, 2, 5)));
    ArrayList<Tile> row18 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 3, 1), new Tile(2, 3, 2),
        new Tile(1, 3, 3), new Tile(2, 3, 4), new Tile(0, 3, 5)));
    ArrayList<Tile> row19 = new ArrayList<Tile>(Arrays.asList(new Tile(1, 4, 1), new Tile(0, 4, 2),
        new Tile(1, 4, 3), new Tile(2, 4, 4), new Tile(1, 4, 5)));
    ArrayList<Tile> row20 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 5, 1), new Tile(2, 5, 2),
        new Tile(0, 5, 3), new Tile(2, 5, 4), new Tile(0, 5, 5)));
    ArrayList<ArrayList<Tile>> player2Wins = new ArrayList<ArrayList<Tile>>(
        Arrays.asList(row16, row17, row18, row19, row20));

    this.linkTiles(player2Wins);
    ArrayListTileUtils u3 = new ArrayListTileUtils(player2Wins);

    t.checkExpect(u3.hasPathBFS(2), true);
    t.checkExpect(u3.hasPathBFS(1), false);
  }

  // helper method for linking tiles in the test above
  void linkTiles(ArrayList<ArrayList<Tile>> arr) {
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {

        // if its in the top row
        if (i != 0) {
          arr.get(i).get(j).updateAbove(arr.get(i - 1).get(j));
        }

        // if its in the bottom row
        if (i != 5 - 1) {
          arr.get(i).get(j).updateBelow(arr.get(i + 1).get(j));
        }

        // left row
        if (j != 0) {
          arr.get(i).get(j).updateLeft(arr.get(i).get(j - 1));
        }

        // right row

        if (j != 5 - 1) {
          arr.get(i).get(j).updateRight(arr.get(i).get(j + 1));
        }
      }
    }
  }

  // Tests for onMouseReleased in World
  void testOnMouseReleasedBridgIt(Tester t) {
    this.initData();
    ArrayList<ArrayList<Tile>> tiles = this.game1.tiles;

    // white tile that was changed to player1
    Posn clickPos1 = new Posn(50, 50);
    this.game1.onMouseReleased(clickPos1);
    t.checkExpect(this.game1.player1, false);
    t.checkExpect(tiles.get(1).get(1).player, 1);

    // white tile that was changed to player 2
    Posn clickPos2 = new Posn(90, 90);
    this.game1.onMouseReleased(clickPos2);
    t.checkExpect(this.game1.player1, true);
    t.checkExpect(tiles.get(2).get(2).player, 2);

    // tile that was not changed at all
    Posn clickPos3 = new Posn(70, 70);
    this.game1.onMouseReleased(clickPos3);
    t.checkExpect(this.game1.player1, true);
    t.checkExpect(tiles.get(1).get(1).player, 1);

    // tile that was not changed at all
    Posn clickPos4 = new Posn(110, 110);
    this.game1.onMouseReleased(clickPos4);
    t.checkExpect(this.game1.player1, true);
    t.checkExpect(tiles.get(2).get(2).player, 2);

    // tile that was not changed at all
    Posn clickPos5 = new Posn(110, 90);
    this.game1.onMouseReleased(clickPos5);
    t.checkExpect(this.game1.player1, true);
    t.checkExpect(tiles.get(2).get(2).player, 2);

    // tile that does not change
    Posn clickPos6 = new Posn(70, 90);
    this.game1.onMouseReleased(clickPos6);
    t.checkExpect(this.game1.player1, true);
    t.checkExpect(tiles.get(2).get(1).player, 2);

    // white tile on edge that does not change
    Posn clickPos7 = new Posn(170, 170);
    this.game1.onMouseReleased(clickPos7);
    t.checkExpect(this.game1.player1, true);
    t.checkExpect(tiles.get(4).get(4).player, 0);

    // winning click for player1
    Posn clickPos8 = new Posn(150, 54);
    this.game1.onMouseReleased(clickPos8);
    t.checkExpect(this.game1.player1, false);
    t.checkExpect(tiles.get(1).get(3).player, 1);

    // checking that player1 won
    ArrayListTileUtils u = new ArrayListTileUtils(tiles);
    t.checkExpect(u.hasPathBFS(1), true);
    t.checkExpect(u.hasPathBFS(2), false);
  }

  // tests lastScene
  void testLastScene(Tester t) {
    this.initData();

    WorldScene w1 = this.game1.makeScene();
    w1.placeImageXY(new TextImage("Player 1 won!", 30, Color.black), 100, 100);
    t.checkExpect(this.game1.lastScene("One"), w1);

    WorldScene w2 = this.game1.makeScene();
    w2.placeImageXY(new TextImage("Player 2 won!", 30, Color.black), 100, 100);
    t.checkExpect(this.game1.lastScene("Two"), w2);

    WorldScene w3 = this.game2.makeScene();
    w3.placeImageXY(new TextImage("Player 1 won!", 30, Color.black), 380, 380);
    t.checkExpect(this.game2.lastScene("One"), w3);

    WorldScene w4 = this.game2.makeScene();
    w4.placeImageXY(new TextImage("Player 2 won!", 30, Color.black), 380, 380);
    t.checkExpect(this.game2.lastScene("Two"), w4);
    
   }

  // tests for hasPathBFS for tile
  void testHasPathBFS2(Tester t) {
    this.initData();

    // game w/ player2 winning
    ArrayList<Tile> row16 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 1, 1), new Tile(2, 1, 2),
        new Tile(0, 1, 3), new Tile(2, 1, 4), new Tile(0, 1, 5)));
    ArrayList<Tile> row17 = new ArrayList<Tile>(Arrays.asList(new Tile(1, 2, 1), new Tile(1, 2, 2),
        new Tile(1, 2, 3), new Tile(2, 2, 4), new Tile(1, 2, 5)));
    ArrayList<Tile> row18 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 3, 1), new Tile(2, 3, 2),
        new Tile(1, 3, 3), new Tile(2, 3, 4), new Tile(0, 3, 5)));
    ArrayList<Tile> row19 = new ArrayList<Tile>(Arrays.asList(new Tile(1, 4, 1), new Tile(0, 4, 2),
        new Tile(1, 4, 3), new Tile(2, 4, 4), new Tile(1, 4, 5)));
    ArrayList<Tile> row20 = new ArrayList<Tile>(Arrays.asList(new Tile(0, 5, 1), new Tile(2, 5, 2),
        new Tile(0, 5, 3), new Tile(2, 5, 4), new Tile(0, 5, 5)));
    ArrayList<ArrayList<Tile>> player2Wins = new ArrayList<ArrayList<Tile>>(
        Arrays.asList(row16, row17, row18, row19, row20));

    this.linkTiles(player2Wins);

    // 3 cases where the player that this tile is isn't equal to the player we're
    // searching for
    t.checkExpect(new Tile(1, 1, 2).hasPathBFS(new ArrayDeque<Tile>(), row16, 2, 5), false);
    t.checkExpect(new Tile(2, 3, 1).hasPathBFS(new ArrayDeque<Tile>(), row20, 1, 5), false);
    t.checkExpect(new Tile(2, 5, 2).hasPathBFS(new ArrayDeque<Tile>(), row20, 1, 5), false);

    // 3 cases for second case
    t.checkExpect(new Tile(1, 2, 4).hasPathBFS(new ArrayDeque<Tile>(), row16, 1, 5), true);
    t.checkExpect(new Tile(2, 4, 4).hasPathBFS(new ArrayDeque<Tile>(), row19, 2, 5), true);
    t.checkExpect(new Tile(1, 2, 4).hasPathBFS(new ArrayDeque<Tile>(), row18, 1, 5), true);

    // adding neighbors of tiles and checking if worklist has changed
    ArrayList<Tile> seen1 = new ArrayList<Tile>();
    ArrayDeque<Tile> worklist1 = new ArrayDeque<Tile>();

    worklist1.addLast(player2Wins.get(1).get(2));
    worklist1.addLast(player2Wins.get(3).get(2));
    seen1.add(player2Wins.get(2).get(2));

    ArrayDeque<Tile> worklist2 = new ArrayDeque<Tile>();
    ArrayList<Tile> seen2 = new ArrayList<Tile>();

    t.checkExpect(player2Wins.get(2).get(2).hasPathBFS(worklist2, seen2, 1, 5), false);
    t.checkExpect(worklist1, worklist2);
    t.checkExpect(seen1, seen2);

    
    //another example of the above
    seen1 = new ArrayList<Tile>();
    worklist1 = new ArrayDeque<Tile>();

    worklist1.addLast(player2Wins.get(1).get(3));
    worklist1.addLast(player2Wins.get(3).get(3));
    seen1.add(player2Wins.get(2).get(3));

    worklist2 = new ArrayDeque<Tile>();
    seen2 = new ArrayList<Tile>();

    t.checkExpect(player2Wins.get(2).get(3).hasPathBFS(worklist2, seen2, 2, 5), false);
    t.checkExpect(worklist1, worklist2);
    t.checkExpect(seen1, seen2); 
    
    // goes through tiles that have been seen before

    ArrayList<Tile> seen = new ArrayList<Tile>();
    seen.add(new Tile(2, 3, 3));
    ArrayDeque<Tile> worklist = new ArrayDeque<Tile>();

    // Tile is in the seen list, therefore false should be returned since this
    // tile was encountered before
    t.checkExpect(seen.contains(new Tile(2, 3, 3)), true);
    t.checkExpect(new Tile(2, 3, 3).hasPathBFS(worklist, seen, 2, 5), false);
    // Check that the worklist and seen are unchanged
    t.checkExpect(seen, new ArrayList<Tile>(Arrays.asList(new Tile(2, 3, 3))));
    t.checkExpect(worklist, new ArrayDeque<Tile>());

    // Another example of the tile being in the seen list
    seen.add(new Tile(1, 1, 2));
    t.checkExpect(new Tile(1, 1, 2).hasPathBFS(worklist, seen, 1, 5), false);
    t.checkExpect(worklist, new ArrayDeque<Tile>());
    t.checkExpect(seen, new ArrayList<Tile>(Arrays.asList(new Tile(2, 3, 3), new Tile(1, 1, 2))));
  }

  // tests for equals
  void testEquals(Tester t) {
    t.checkExpect(new Tile(2, 3, 3).equals(new Tile(2, 3, 3)), true);
    t.checkExpect(new Tile(2, 3, 3).equals(new Tile(4, 3, 3)), false);
    t.checkExpect(new Tile(2, 3, 4).equals(new Tile(2, 4, 3)), false);
    t.checkExpect(new Tile(2, 2, 3).equals(new Tile(2, 3, 3)), false);

    Tile tile = new Tile(4, 5, 6);
    tile.above = new Tile(1, 2, 3);
    t.checkExpect(tile.equals(new Tile(4, 5, 6)), true);
  }

  // tests for hashCode
  void testHashcode(Tester t) {
    t.checkExpect(new Tile(2, 3, 3).hashCode(), new Tile(2, 3, 3).hashCode());
    t.checkExpect(new Tile(2, 3, 3).hashCode() == new Tile(4, 3, 3).hashCode(), false);
    t.checkExpect(new Tile(2, 3, 4).hashCode() == new Tile(2, 4, 3).hashCode(), false);
    t.checkExpect(new Tile(2, 2, 3).hashCode() == new Tile(2, 3, 3).hashCode(), false);

    Tile tile = new Tile(4, 5, 6);
    tile.above = new Tile(1, 2, 3);
    t.checkExpect(tile.hashCode() == new Tile(4, 5, 6).hashCode(), true);
  }
}
