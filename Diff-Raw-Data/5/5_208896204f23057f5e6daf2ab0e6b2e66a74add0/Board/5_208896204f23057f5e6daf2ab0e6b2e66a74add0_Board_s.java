 package model;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 /**
  * @author Andrew Wylie <andrew.dale.wylie@gmail.com>
  * @version 1.0
  * @since 2012-07-02
  */
 public class Board {
 
 	private Tile[][] gameBoard;
 
 	/*
 	 * This class acts as a mediator between the tiles and meeples to keep track
 	 * of which tiles have a meeple placed on them, and on which section.
 	 */
 	private class MeeplePosition {
 
 		private Tile tile = null;
 		private int xTile;
 		private int yTile;
 
 		public MeeplePosition(Tile tile, int xTile, int yTile) {
 			this.tile = tile;
 			this.xTile = xTile;
 			this.yTile = yTile;
 		}
 
 		/**
 		 * @return the tile
 		 */
 		public Tile getTile() {
 			return tile;
 		}
 
 		/**
 		 * @return the xTile
 		 */
 		public int getxTile() {
 			return xTile;
 		}
 
 		/**
 		 * @return the yTile
 		 */
 		public int getyTile() {
 			return yTile;
 		}
 
 	}
 
 	// The board will keep track of the relationship between meeples and tiles.
 	// Two hash tables for quick lookup would be somewhat ideal, though it
 	// creates somewhat of an overhead. Here, especially so since tiles can
 	// have multiple meeples on them. So we'll just have one table which will
 	// keep track for each meeple which tile it is on.
 	private HashMap<Meeple, MeeplePosition> meeplePlacement;
 
 	/**
 	 * Initialize the game board. All tile positions are set to null to start.
 	 * After this, the starting tile is placed in the center of the board
 	 */
 	public Board() {
 
 		meeplePlacement = new HashMap<Meeple, MeeplePosition>();
 
 		// We have 72 tiles to place, with the starting tile in the center
 		// of the board. This guarantees that the board can't extend outside
 		// of it's container. Use an odd number for an easier 'center' to
 		// place the beginning piece.
 		// The tile at 72, 72 is at the center of the board.
 
 		gameBoard = new Tile[145][145];
 
 		for (int i = 0; i < gameBoard.length; i++) {
 			for (int j = 0; j < gameBoard[i].length; j++) {
 				gameBoard[i][j] = null;
 			}
 		}
 	}
 
 	/**
 	 * Allow a player to place a tile on the game board.
 	 * 
 	 * @param aPlayer
 	 *            The player which is placing the tile.
 	 * @param xPos
 	 *            The x position on the game board to place the tile.
 	 * @param yPos
 	 *            The y position on the game board to place the tile.
 	 * @return a non-zero integer if the tile could not be placed, zero
 	 *         otherwise.
 	 */
 	public int placeTile(Player aPlayer, int xPos, int yPos) {
 		Tile tileToPlace = aPlayer.getCurrentTile();
 
 		if (tileToPlace != null && isTilePosValid(tileToPlace, xPos, yPos)) {
 			gameBoard[yPos][xPos] = tileToPlace;
 			aPlayer.setCurrentTile(null);
 			aPlayer.setLastTilePlacedPos(xPos, yPos);
 
 			return 0;
 		}
 
 		return 1;
 	}
 
 	/**
 	 * Verifies whether a tile can be placed at a specified board position.
 	 * There are three separate conditions which have to be met. The first is
 	 * that the position for tile placement must not already be occupied by a
 	 * tile. Secondly, there must be at least one adjacent tile on either the
 	 * top, bottom, left, or right of the placement position. Lastly, the sides
 	 * of the tile to be placed must match with any adjacent tile sides.
 	 * 
 	 * @param tileToPlace
 	 *            The tile which is to be placed.
 	 * @param xPos
 	 *            The x position the tile is to be placed on the board.
 	 * @param yPos
 	 *            The y position the tile is to be placed on the board.
 	 * @return True if the move is valid, false otherwise.
 	 */
 	private boolean isTilePosValid(Tile tileToPlace, int xPos, int yPos) {
 		// Check that there is no tile in the specified position.
 		boolean free = (gameBoard[yPos][xPos] == null);
 
 		// Check that there is an adjacent tile wrt/ the specified position.
 		Tile top = gameBoard[yPos - 1][xPos];
 		Tile bottom = gameBoard[yPos + 1][xPos];
 		Tile right = gameBoard[yPos][xPos + 1];
 		Tile left = gameBoard[yPos][xPos - 1];
 
 		boolean adjacent = (top != null) || (bottom != null) || (right != null)
 				|| (left != null);
 
 		// Check that the adjacent tiles sides' match.
 		boolean topMatches = (top == null);
 		boolean bottomMatches = (bottom == null);
 		boolean rightMatches = (right == null);
 		boolean leftMatches = (left == null);
 
 		if (top != null
 				&& Arrays.deepEquals(top.getBottom(), tileToPlace.getTop())) {
 			topMatches = true;
 		}
 
 		if (bottom != null
 				&& Arrays.deepEquals(bottom.getTop(), tileToPlace.getBottom())) {
 			bottomMatches = true;
 		}
 
 		if (right != null
 				&& Arrays.deepEquals(right.getLeft(), tileToPlace.getRight())) {
 			rightMatches = true;
 		}
 
 		if (left != null
 				&& Arrays.deepEquals(left.getRight(), tileToPlace.getLeft())) {
 			leftMatches = true;
 		}
 
 		boolean sidesMatch = (topMatches && bottomMatches && rightMatches && leftMatches);
 
 		// Return our answer.
 		return (free && adjacent && sidesMatch) || !hasGameStarted();
 	}
 
 	// Answer whether there has been a tile placed yet or not.
 	public boolean hasGameStarted() {
 
 		boolean gameStarted = false;
 
 		// Check if there has already been a tile placed on the board.
 		for (int i = 0; i < gameBoard.length; i++) {
 			for (int j = 0; j < gameBoard[i].length; j++) {
 				if (gameBoard[j][i] != null) {
 					gameStarted = true;
 				}
 			}
 		}
 
 		return gameStarted;
 	}
 
 	/**
 	 * Allow a player to place a meeple on the game board. The meeple must be
 	 * placed on the most recently played tile of the player. Also, the tile
 	 * type it is placed on (field, road, city, or cloister) must not be claimed
 	 * by another player's meeple.
 	 * 
 	 * @param aPlayer
 	 *            The player which is placing the meeple.
 	 * @param xBoard
 	 *            The x position the meeple is to be placed on the board.
 	 * @param yBoard
 	 *            The y position the meeple is to be placed on the board.
 	 * @param xTile
 	 *            The x position on the tile to place the meeple.
 	 * @param yTile
 	 *            The y position on the tile to place the meeple.
 	 * @return An integer, with non-zero representing an error. -1: Something
 	 *         went horribly wrong. 1: Player has no meeples to place. 2: Player
 	 *         tried to place meeple on tile they didn't just place. 3: Player
 	 *         didn't play on a new feature.
 	 */
 	public int placeMeeple(Player aPlayer, int xBoard, int yBoard, int xTile,
 			int yTile) {
 		// First we need to check the correctness of the input x & y.
 		boolean correctTile = (xBoard == aPlayer.getLastTilePlacedXPos() && yBoard == aPlayer
 				.getLastTilePlacedYPos());
 
 		// Next we check that the tile type is not already taken.
 		boolean newFeature = isNewFeature(xBoard, yBoard, xTile, yTile);
 
 		// If so, we place the meeple.
 		if (correctTile && newFeature) {
 			Tile theTile = gameBoard[yBoard][xBoard];
 			Meeple[] meeples = aPlayer.getMeeples();
 
 			// TODO: better selection of meeple to allow the correct one to
 			// be selected and draw?
 			MeeplePosition mp;
 
 			for (int i = 0; i < meeples.length; i++) {
 				if (meeplePlacement.get(meeples[i]) == null) {
 
 					mp = new MeeplePosition(theTile, xTile, yTile);
 					meeplePlacement.put(meeples[i], mp);
 
 					return 0;
 				}
 			}
 		}
 
 		return 1;
 	}
 
 	// return the number of meeples which are in gameplay
 	public int getNumMeeplesPlaced(Player player) {
 
 		int numMeeplesPlaced = 0;
 		Meeple[] meeples = player.getMeeples();
 
 		for (int i = 0; i < meeples.length; i++) {
 			if (meeplePlacement.get(meeples[i]) != null) {
 				numMeeplesPlaced++;
 			}
 		}
 
 		return numMeeplesPlaced;
 	}
 
 	/**
 	 * This function returns whether a meeple may be placed on the game board.
 	 * This function uses the method
 	 * {@link #isNewFeatureRecursive(HashSet, HashSet)} to search the game board
 	 * & contained tiles for any meeples which may have already claimed the
 	 * terrain/feature.
 	 * 
 	 * @param xBoard
 	 *            The x board position to start the search at.
 	 * @param yBoard
 	 *            The y board position to start the search at.
 	 * @param xTile
 	 *            The x tile position to start the search at.
 	 * @param yTile
 	 *            The y tile position to start the search at.
 	 * @return A boolean indicating whether the terrain is free to be claimed.
 	 */
 	private boolean isNewFeature(int xBoard, int yBoard, int xTile, int yTile) {
 		// Hold searched tile positions so we don't search any positions twice.
 		HashSet<String> searched = new HashSet<String>();
 		HashSet<String> toSearch = new HashSet<String>();
 
 		toSearch.add(xBoard + "," + yBoard + "," + xTile + "," + yTile);
 
 		// Do a recursive search on all adjacent cells for a meeple.
 		return isNewFeatureRecursive(searched, toSearch);
 	}
 
 	/**
 	 * This function returns whether a meeple may be placed on the game board.
 	 * 
 	 * @param searched
 	 *            A HashSet containing any searched tiles.
 	 * @param toSearch
 	 *            A HashSet containing any tiles to be searched.
 	 * @return A boolean indicating whether the terrain is free to be claimed.
 	 */
 	private boolean isNewFeatureRecursive(HashSet<String> searched,
 			HashSet<String> toSearch) {
 		// Take a position from the toSearch map.
 		String tilePosition = toSearch.toArray(new String[0])[0];
 		String[] tilePos = tilePosition.split(",");
 
 		int xBoard = Integer.parseInt(tilePos[0]);
 		int yBoard = Integer.parseInt(tilePos[1]);
 		int xTile = Integer.parseInt(tilePos[2]);
 		int yTile = Integer.parseInt(tilePos[3]);
 
 		Tile currentTile = gameBoard[yBoard][xBoard];
 
 		// Search the position & add it to searched map.
 		if (!hasMeeple(currentTile, xTile, yTile)) {
 			toSearch.remove(tilePosition);
 			searched.add(tilePosition);
 		} else {
 			return false;
 		}
 
 		// Add valid neighbors of position to toSearch map.
 		// A neighbor is valid if it is in neither map,
 		// and is of the same tile type.
 		TileType currentTileType = currentTile.getTileType(xTile, yTile);
 
 		// Add to an array. And run through adding each to toSearch.
 		int[][] neighborTiles = getTileNeighbors(xBoard, yBoard, xTile, yTile);
 
 		for (int i = 0; i < neighborTiles.length; i++) {
 			// Check the tile is not null.
 			Tile tile = gameBoard[neighborTiles[i][1]][neighborTiles[i][0]];
 
 			if (tile != null) {
 				String toAdd = "";
 
 				for (int j = 0; j < neighborTiles[i].length; j++) {
 					toAdd += neighborTiles[i][j];
 					toAdd += ",";
 				}
 
 				// Remove the last comma.
 				toAdd = toAdd.substring(0, toAdd.length() - 1);
 
 				// Check that the tile has the same tile type.
 				TileType tileType = tile.getTileType(neighborTiles[i][2],
 						neighborTiles[i][3]);
 
 				// Check the tile is not already in searched or toSearch.
 				if (tileType == currentTileType && !toSearch.contains(toAdd)
 						&& !searched.contains(toAdd)) {
 					toSearch.add(toAdd);
 				}
 			}
 		}
 
 		// If toSearch is empty then we are done. We have found that this is
 		// indeed a new feature.
 		if (toSearch.isEmpty()) {
 			return true;
 		}
 
 		return isNewFeatureRecursive(searched, toSearch);
 	}
 
 	/**
 	 * Return whether a tile has a meeple on it, with respect to the tile pos.
 	 * 
 	 * @param tile
 	 *            The tile to check for the meeple.
 	 * @param xTile
 	 *            The x position to check for the meeple.
 	 * @param yTile
 	 *            The y position to check for the meeple.
 	 * @return True if the tile has a meeple on it, false otherwise.
 	 */
 	// TODO remove this?
 	private boolean hasMeeple(Tile tile, int xTile, int yTile) {
 
 		if (getMeeple(tile, xTile, yTile) != null) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Return the meeple on a tile given the tile & its position.
 	 * 
 	 * @param tile
 	 *            The tile to check for the meeple.
 	 * @param xTile
 	 *            The x position to check for the meeple.
 	 * @param yTile
 	 *            The y position to check for the meeple.
 	 * @return The meeple if one has claimed the tile, null otherwise.
 	 */
 	private Meeple getMeeple(Tile tile, int xTile, int yTile) {
 
 		Iterator<Meeple> iter = meeplePlacement.keySet().iterator();
 
 		while (iter.hasNext()) {
 			Meeple meeple = iter.next();
 			MeeplePosition meeplePosition = meeplePlacement.get(meeple);
 
 			if (meeplePosition != null && meeplePosition.getTile() == tile
 					&& meeplePosition.getxTile() == xTile
 					&& meeplePosition.getyTile() == yTile) {
 				return meeple;
 			}
 		}
 
 		return null;
 	}
 
 	// Helper function allows the game to get a meeple so that it can be added
 	// to the ui canvas component.
 	public Meeple getMeeple(int xBoard, int yBoard, int xTile, int yTile) {
 
 		Tile theTile = gameBoard[yBoard][xBoard];
 		return getMeeple(theTile, xTile, yTile);
 	}
 
 	/**
 	 * Get the x position of a tile on the game board.
 	 * 
 	 * @param tile
 	 *            The tile to search for on the game board.
 	 * @return The x position of the tile, or -1 if it was not found.
 	 */
 	private int getxTile(Tile tile) {
 
 		for (int i = 0; i < gameBoard.length; i++) {
 			for (int j = 0; j < gameBoard[i].length; j++) {
 				if (gameBoard[i][j] != null && gameBoard[i][j] == tile) {
 					return j;
 				}
 			}
 		}
 
 		// The tile is not placed on the board.
 		return -1;
 	}
 
 	/**
 	 * Get the y position of a tile on the game board.
 	 * 
 	 * @param tile
 	 *            The tile to search for on the game board.
 	 * @return The y position of the tile, of -1 if it was not found.
 	 */
 	private int getyTile(Tile tile) {
 
 		for (int i = 0; i < gameBoard.length; i++) {
 			for (int j = 0; j < gameBoard[i].length; j++) {
 				if (gameBoard[i][j] != null && gameBoard[i][j] == tile) {
 					return i;
 				}
 			}
 		}
 
 		// The tile is not placed on the board.
 		return -1;
 	}
 
 	/**
 	 * Score each player for any cloisters they may have. This function can be
 	 * called either during the game to score completed cloisters, or after the
 	 * game to score incomplete cloisters. The passed in boolean parameter
 	 * determines if incomplete cloisters are scored.
 	 * 
 	 * @param players
 	 *            An array of the players. Used with the meeple to figure out
 	 *            which player has scored.
 	 * @param hasGameEnded
 	 *            True if scoring at the end of the game, false if scoring
 	 *            during the game.
 	 * @return an Arraylist of Meeple which have been removed from the board.
 	 */
 	public ArrayList<Meeple> scoreCloisters(Player[] players,
 			boolean hasGameEnded) {
 
 		ArrayList<Meeple> removedMeeples = new ArrayList<Meeple>();
 		Iterator<Meeple> iter = meeplePlacement.keySet().iterator();
 
 		// For each meeple which is placed we check to see if it is placed on
 		// a cloister. Then depending on this and the game state we score.
 		while (iter.hasNext()) {
 
 			Meeple meeple = iter.next();
 			MeeplePosition mp = meeplePlacement.get(meeple);
 
 			// Check to see if it is attached to a cloister.
 			Tile tile = mp.getTile();
 			TileType tileType = tile.getTileType(mp.getxTile(), mp.getyTile());
 
 			// If it is attached to a cloister.
 			if (tileType == TileType.CLOISTER) {
 				// If all 8 neighbor tiles are not null.
 				int xIile = getxTile(tile);
 				int yTile = getyTile(tile);
 
 				Tile nTile = gameBoard[xIile][yTile - 1];
 				Tile neTile = gameBoard[xIile + 1][yTile - 1];
 				Tile eTile = gameBoard[xIile + 1][yTile];
 				Tile seTile = gameBoard[xIile + 1][yTile + 1];
 				Tile sTile = gameBoard[xIile][yTile + 1];
 				Tile swTile = gameBoard[xIile - 1][yTile + 1];
 				Tile wTile = gameBoard[xIile - 1][yTile];
 				Tile nwTile = gameBoard[xIile - 1][yTile - 1];
 
 				Tile[] neighborTiles = { nTile, neTile, eTile, seTile, sTile,
 						swTile, wTile, nwTile };
 
 				int numNeighborTiles = 0;
 
 				// Count the number of placed (non-null) neighbor tiles.
 				for (int i = 0; i < neighborTiles.length; i++) {
 					if (neighborTiles[i] != null) {
 						numNeighborTiles++;
 					}
 				}
 
 				// Score if the game has ended, or score if the game
 				// is still being played and the cloister is complete.
 				if (hasGameEnded || (!hasGameEnded && numNeighborTiles == 8)) {
 
 					// Find out which player owns the meeple.
 					// Let's just test color for now. TODO
 					Player scorer = null;
 
 					for (int i = 0; i < players.length; i++) {
 						if (players[i].getColor() == meeple.getColor()) {
 							scorer = players[i];
 						}
 					}
 
 					// Then add to the player's score.
 					int playerScore = scorer.getScore();
 					scorer.setScore(playerScore + numNeighborTiles + 1);
 
 					// And remove the meeple from the tile.
 					meeplePlacement.remove(meeple);
 					removedMeeples.add(meeple);
 
 					// Since we are iterating through meeplePlacement
 					// we need to refresh it when it's altered.
 					iter = meeplePlacement.keySet().iterator();
 				}
 			}
 		}
 
 		return removedMeeples;
 	}
 
 	/**
 	 * Score the cities, using the helper function
 	 * {@link #genericScore(TileType, boolean)}.
 	 * 
 	 * @param hasGameEnded
 	 *            A boolean indicating whether the game has ended.
 	 * @return an ArrayList of Meeple which have been removed from the board.
 	 */
 	public ArrayList<Meeple> scoreCities(Player[] players, boolean hasGameEnded) {
 		return genericScore(players, TileType.CITY, hasGameEnded);
 	}
 
 	/**
 	 * Score the roads, using the helper function
 	 * {@link #genericScore(TileType, boolean)}.
 	 * 
 	 * @param hasGameEnded
 	 *            A boolean indicating whether the game has ended.
 	 * @return an ArrayList of Meeple which have been removed from the board.
 	 */
 	public ArrayList<Meeple> scoreRoads(Player[] players, boolean hasGameEnded) {
 		return genericScore(players, TileType.ROAD, hasGameEnded);
 	}
 
 	/**
 	 * Find out which player(s) have the majority of meeples on a given feature.
 	 * The player(s) with the most meeples on a feature receive the points for
 	 * the feature.
 	 * 
 	 * @param meeplesOnFeature
 	 *            An ArrayList of meeples which are on the feature.
 	 * @return An ArrayList of players that will receive the points for the
 	 *         feature.
 	 */
 	// TODO thing besides color
 	private ArrayList<Player> getFeatureScorers(Player[] players,
 			ArrayList<Meeple> meeplesOnFeature) {
 		HashMap<Color, Integer> nMeeples = new HashMap<Color, Integer>();
 
 		// Count the number of meeples for each color.
 		for (int i = 0; i < meeplesOnFeature.size(); i++) {
 			Color color = meeplesOnFeature.get(i).getColor();
 
 			if (nMeeples.get(color) == null) {
 				nMeeples.put(color, 1);
 			} else {
 				nMeeples.put(color, nMeeples.get(color) + 1);
 			}
 		}
 
 		// Find which player has the most meeples on the feature.
 		Iterator<Color> meepleColors = nMeeples.keySet().iterator();
 		int max = 0;
 
 		// One run to get the max number color.
 		while (meepleColors.hasNext()) {
 			Color color = meepleColors.next();
 			int num = nMeeples.get(color);
 
 			if (num >= max) {
 				max = num;
 			}
 		}
 
 		// One more run to get the colors with that max.
 		ArrayList<Color> scoringColors = new ArrayList<Color>();
 		ArrayList<Player> scoringPlayers = new ArrayList<Player>();
 
 		meepleColors = nMeeples.keySet().iterator();
 
 		while (meepleColors.hasNext()) {
 			Color color = meepleColors.next();
 			int num = nMeeples.get(color);
 
 			if (num == max) {
 				scoringColors.add(color);
 			}
 		}
 
 		for (int i = 0; i < players.length; i++) {
 			if (scoringColors.contains(players[i].getColor())) {
 				scoringPlayers.add(players[i]);
 			}
 		}
 
 		return scoringPlayers;
 	}
 
 	/**
 	 * Generic scoring function used to score roads & cities. The function runs
 	 * through each meeple, determining if each is on a tile of the specified
 	 * type. If so, another helper function
 	 * {@link #genericScoreRecursive(HashSet, HashSet, Object[])} is called to
 	 * assist by finding all the meeples on that feature, whether the feature is
 	 * complete, and the number of tiles taken by the feature. This information
 	 * is then used to complete scoring. Scoring is done by finding the
 	 * player(s) which has the maximum number of meeples on the feature and then
 	 * giving them the points. Meeples are also returned to their owners, and
 	 * different scoring types are accounted for through a score multiplier
 	 * (cities during the game).
 	 * 
 	 * @param players
 	 *            An array of the players. This is used for scoring purposes.
 	 * @param scoreTileType
 	 *            The tile type to score for.
 	 * @param hasGameEnded
 	 *            A boolean indicating whether the game has ended.
 	 * @return an ArrayList of Meeple which have been removed from the board.
 	 */
 	private ArrayList<Meeple> genericScore(Player[] players,
 			TileType scoreTileType, boolean hasGameEnded) {
 
 		// Initialize empty tracker variables & other variables.
 		ArrayList<Meeple> removedMeeples = new ArrayList<Meeple>();
 		ArrayList<Meeple> meeplesOnFeature = new ArrayList<Meeple>();
 
 		int nTiles = 0;
 		boolean nullTileFound = false;
 
 		HashSet<String> searched = new HashSet<String>();
 		HashSet<String> toSearch = new HashSet<String>();
 		Object[] featureProperties = { nTiles, nullTileFound };
 
 		// Run through all the placed meeples.
 		Iterator<Meeple> iter = meeplePlacement.keySet().iterator();
 
 		while (iter.hasNext()) {
 
 			MeeplePosition mp = meeplePlacement.get(iter.next());
 
 			// Check to see if it is attached to the correct tile type.
 			Tile tile = mp.getTile();
 
 			TileType tileType = tile.getTileType(mp.getxTile(), mp.getyTile());
 
 			// If it is attached to the correct tile type.
 			if (tileType == scoreTileType) {
 
 				// Init search.
 				toSearch.add(getxTile(tile) + "," + getyTile(tile) + ","
 						+ mp.getxTile() + "," + mp.getyTile());
 
 				// Call our search.
 				genericScoreRecursive(searched, toSearch, meeplesOnFeature,
 						featureProperties);
 
 				// Recover tracker variables from object array.
 				nTiles = (Integer) featureProperties[0];
 				nullTileFound = (Boolean) featureProperties[1];
 
 				// Score multiplier depends on the feature.
 				// The base multiplier is 1.
 				int multiplier = 1;
 
 				// Each city tile is worth 2 points during the game.
 				if (!hasGameEnded && scoreTileType == TileType.CITY) {
 					multiplier = 2;
 				}
 
 				// We are scoring if the feature is complete and
 				// the game is not over, or if the game is over.
 				if (hasGameEnded || (!nullTileFound && !hasGameEnded)) {
 
 					ArrayList<Player> scoringPlayers = getFeatureScorers(
 							players, meeplesOnFeature);
 
 					// Remove the meeples from the board.
 					for (int i = 0; i < meeplesOnFeature.size(); i++) {
 
 						Meeple meepleOnFeature = meeplesOnFeature.get(i);
 						meeplePlacement.remove(meepleOnFeature);
 						removedMeeples.add(meepleOnFeature);
 					}
 
 					// Since we are iterating through meeplePlacement
 					// we need to refresh it when it's altered.
 					iter = meeplePlacement.keySet().iterator();
 
 					// Recalculate scores.
 					for (int i = 0; i < scoringPlayers.size(); i++) {
 
 						int score = scoringPlayers.get(i).getScore();
 						int newScore = (nTiles * multiplier) + score;
 						scoringPlayers.get(i).setScore(newScore);
 					}
 				}
 			}
 		}
 		return removedMeeples;
 	}
 
 	/**
 	 * This method calculates the number of tiles held by a meeple. This method
 	 * is used for the score calculation for both roads and cities in game and
 	 * at the end of a game. It is to be called by one of the main score
 	 * calculation methods which will pass in the proper arguments. A similar
 	 * technique to what is used in
 	 * {@link #isNewFeatureRecursive(HashSet, HashSet)} is utilized for the main
 	 * tile search. While the search is occurring there are several separate
 	 * data items kept track of. These include: <li>A list of meeples which are
 	 * located on the same feature. <li>The total number of tiles which comprise
 	 * the feature. <li>Whether we have found any adjacent tiles which are null;
 	 * this determines if the feature is incomplete.
 	 * 
 	 * @param searched
 	 *            A HashSet of Strings of board & tile coordinates which have
 	 *            been used.
 	 * @param toSearch
 	 *            A HashSet of Strings of board & tile coordinates which are to
 	 *            be searched.
 	 * @param meeplesOnFeature
 	 *            An ArrayList of Meeple to be filled by the meeples which are
 	 *            found on the feature.
 	 * @param featureProperties
 	 *            An array of objects containing an ArrayList of meeples which
 	 *            have claimed the feature, the number of tiles which comprise
 	 *            the feature, and a boolean indicating the presence of a null
 	 *            tile.
 	 */
 	private void genericScoreRecursive(HashSet<String> searched,
 			HashSet<String> toSearch, ArrayList<Meeple> meeplesOnFeature,
 			Object[] featureProperties) {
 
 		// Take a position from the toSearch map to search.
 		String tilePosition = toSearch.toArray(new String[0])[0];
 		String[] tilePos = tilePosition.split(",");
 
 		// TODO make struct for these instead of passing by weird string thing
 		int xBoard = Integer.parseInt(tilePos[0]);
 		int yBoard = Integer.parseInt(tilePos[1]);
 		int xTile = Integer.parseInt(tilePos[2]);
 		int yTile = Integer.parseInt(tilePos[3]);
 
 		Tile currentTile = gameBoard[yBoard][xBoard];
 
 		// Search the position & add it to searched map.
 		if (hasMeeple(currentTile, xTile, yTile)) {
 			// Add the meeple on the tile to our list.
 			meeplesOnFeature.add(getMeeple(currentTile, xTile, yTile));
 		}
 
 		toSearch.remove(tilePosition);
 		searched.add(tilePosition);
 
 		// Add valid neighbors of position to toSearch map.
 		// A neighbor is valid if it is in neither map,
 		// and is of the same tile type.
 		TileType currentTileType = currentTile.getTileType(xTile, yTile);
 
 		// Add to an array. And run through adding each to toSearch.
 		int[][] neighborTiles = getTileNeighbors(xBoard, yBoard, xTile, yTile);
 
 		for (int i = 0; i < neighborTiles.length; i++) {
 			// Check the tile is not null.
 			Tile tile = gameBoard[neighborTiles[i][1]][neighborTiles[i][0]];
 
 			if (tile != null) {
 				String toAdd = "";
 
 				for (int j = 0; j < neighborTiles[i].length; j++) {
 					toAdd += neighborTiles[i][j];
 					toAdd += ",";
 				}
 
 				// Remove the last comma.
 				toAdd = toAdd.substring(0, toAdd.length() - 1);
 
 				// Check that the tile has the same tile type.
 				TileType tileType = tile.getTileType(neighborTiles[i][2],
 						neighborTiles[i][3]);
 
 				// Check the tile is not already in searched or toSearch.
 				if (tileType == currentTileType && !toSearch.contains(toAdd)
 						&& !searched.contains(toAdd)) {
 					toSearch.add(toAdd);
 				}
 
 			} else {
 				// If we have a neighboring tile which is null then the
 				// feature is not complete.
 				featureProperties[1] = true;
 			}
 
 		}
 
 		// If there are still tiles in toSearch then continue searching.
 		if (!toSearch.isEmpty()) {
 			genericScoreRecursive(searched, toSearch, meeplesOnFeature,
 					featureProperties);
 		} else {
 			// Get the number of tiles searched.
 			HashSet<String> searchedTiles = new HashSet<String>();
 			Iterator<String> searchedIterator = searched.iterator();
 
 			while (searchedIterator.hasNext()) {
 				String[] coordinates = searchedIterator.next().split(",");
 				searchedTiles.add(coordinates[0] + "," + coordinates[1]);
 			}
 
 			// Set the number of tiles searched.
 			featureProperties[0] = searchedTiles.size();
 		}
 
 	}
 
 	/**
 	 * This function scores the fields at the end of the game. It uses
 	 * {@link #fieldScoreRecursive(HashSet, HashSet, ArrayList, ArrayList, ArrayList, Object[])}
 	 * and {@link #getCompletedCities()}. This function relies heavily on the
 	 * aforementioned helper functions and does not do much else besides set up
 	 * the variables to be passed to the helpers, recalculate the player scores,
 	 * and meeple removal after scoring.
 	 */
 	// Basic overview:
 	//
 	// Check all meeples for claim to a field,
 	// Once one is found do a search of the whole field to record both
 	// any other meeples on it, and which completed cities are adjacent.
 	// Use the gathered info to recalculate scores & return meeples to players.
 	//
 	// To do this:
 	//
 	// 1. flooding alg. altered to keep track of completed cities.
 	// returns a list of 'cities'
 	// a 'city' is a set of strings of coordinates ie ['46,42,4,4', ..]
 	// which consists of xBoard, yBoard, xTile, yTile.
 	//
 	// 2. flooding alg. (genericScoreRecursive) altered to detect if there is
 	// an adjacent castle to the field being explored. if so, then check
 	// if it is in a completed castle (which is not the same as another
 	// already counted castle) and add points.. & the castle for future
 	// checking.
 	//
 	public ArrayList<Meeple> scoreFields(Player[] players) {
 
 		// Get the list of completed cities, init other vars too.
 		ArrayList<Meeple> removedMeeples = new ArrayList<Meeple>();
 		ArrayList<HashSet<String>> completedCities = getCompletedCities();
 		ArrayList<Meeple> meeplesOnFeature = new ArrayList<Meeple>();
 		int nCities = 0;
 
 		HashSet<String> searched = new HashSet<String>();
 		HashSet<String> toSearch = new HashSet<String>();
 		Object[] featureProperties = { nCities };
 
 		// Run through all the placed meeples.
 		Iterator<Meeple> iter = meeplePlacement.keySet().iterator();
 
 		while (iter.hasNext()) {
 			Meeple m = iter.next();
 			MeeplePosition mp = meeplePlacement.get(m);
 
 			Tile tile = mp.getTile();
 			TileType tileType = tile.getTileType(mp.getxTile(), mp.getyTile());
 
 			if (tileType == TileType.FIELD) {
 				// Init search.
 				toSearch.add(getxTile(tile) + "," + getyTile(tile) + ","
 						+ mp.getxTile() + "," + mp.getyTile());
 
 				// Call the search.
 				fieldScoreRecursive(searched, toSearch, meeplesOnFeature,
 						completedCities, new ArrayList<HashSet<String>>(),
 						featureProperties);
 
 				// Recover the tracker variables.
 				// Only primitives need to be recovered.
 				nCities = (Integer) featureProperties[0];
 
 				// Score multiplier.
 				int multiplier = 3;
 
 				ArrayList<Player> scoringPlayers = getFeatureScorers(players,
 						meeplesOnFeature);
 
 				// Remove the meeples from the board.
 				for (int i = 0; i < meeplesOnFeature.size(); i++) {
 
 					Meeple meepleOnFeature = meeplesOnFeature.get(i);
 					meeplePlacement.remove(meepleOnFeature);
 					removedMeeples.add(meepleOnFeature);
 				}
 
 				// Since we are iterating through meeplePlacement
 				// we need to refresh it when it's altered.
 				iter = meeplePlacement.keySet().iterator();
 
 				// Recalculate scores.
 				for (int i = 0; i < scoringPlayers.size(); i++) {
 
 					int score = scoringPlayers.get(i).getScore();
 					int newScore = (nCities * multiplier) + score;
 					scoringPlayers.get(i).setScore(newScore);
 				}
 			}
 		}
 
 		return removedMeeples;
 	}
 
 	/**
 	 * This function returns a list of the completed cities which exist. This is
 	 * the first of two helper functions which are used by
 	 * {@link #scoreFields()}. The other is the {@link #fieldScoreRecursive()}.
 	 * It searches through the game board position by position testing if each
 	 * is a city tile. If the position is a city tile and is not already part of
 	 * a completed city, then a new city is recorded using the
 	 * {@link #getCompletedCitiesRecursive(HashSet, HashSet, ArrayList, ArrayList)}
 	 * function. As a result of the linear method of board search, incomplete
 	 * cities must be recorded during the method and are removed at the end,
 	 * before the final list is returned.
 	 * 
 	 * @return A list of sets of board positions belonging to separate completed
 	 *         cities.
 	 */
 	private ArrayList<HashSet<String>> getCompletedCities() {
 
 		// Each HashSet in the ArrayList denotes a city; a collection of strings
 		// which indicate the tile locations which comprise the city.
 		ArrayList<HashSet<String>> cities = new ArrayList<HashSet<String>>();
 
 		// Each city which is found to be incomplete is recorded by having an
 		// entry put in incompleteCities specifying the index of the incomplete
 		// city in the cities array.
 		ArrayList<Integer> incompleteCities = new ArrayList<Integer>();
 
 		boolean newCity = true;
 		HashSet<String> searched;
 		HashSet<String> toSearch;
 
 		// Run through the whole map; all board tiles and all tile positions.
 		// When we find a city tile type we check if the city it belongs to is
 		// already recorded. If not then we call the recursive search to record
 		// the city.
 		for (int i = 0; i < gameBoard.length; i++) {
 			for (int j = 0; j < gameBoard[i].length; j++) {
 
 				Tile tile = gameBoard[i][j];
 
 				if (tile == null) {
 					continue;
 				}
 
 				for (int k = 0; k < tile.getLeft().length; k++) {
 					for (int l = 0; l < tile.getTop().length; l++) {
 						if (tile.getTileType(l, k) == TileType.CITY) {
 
 							// Reset flags for testing the next tile position.
 							newCity = true;
 
 							// Board x, board y, tile x, tile y.
 							String curTile = j + "," + i + "," + l + "," + k;
 
 							for (int m = 0; m < cities.size(); m++) {
 								if (cities.get(m).contains(curTile)) {
 									newCity = false;
 								}
 							}
 
 							if (newCity) {
 								searched = new HashSet<String>();
 								toSearch = new HashSet<String>();
 
 								toSearch.add(curTile);
 
 								getCompletedCitiesRecursive(searched, toSearch,
 										cities, incompleteCities);
 							}
 
 						}
 					}
 				}
 			}
 		}
 
 		// After the search is completed we will remove the incomplete cities
 		// from the list as they are not used in field scoring.
 		for (int i = incompleteCities.size() - 1; i >= 0; i--) {
 			int cityToRemove = incompleteCities.get(i);
 			cities.remove(cityToRemove);
 		}
 
 		return cities;
 	}
 
 	/**
 	 * This function uses recursion to find all board positions of a city. This
 	 * function is similar to
 	 * {@link #genericScoreRecursive(HashSet, HashSet, Object[])} and
 	 * {@link #isNewFeatureRecursive(HashSet, HashSet)} in the way that it
 	 * searches the game board feature. It recursively records a city by finding
 	 * the neighbor tile positions of an initial position (passed in in
 	 * toSearch). If a neighboring tile is found to be null during the search,
 	 * this is also recorded and used by the calling function (which indicates
 	 * an incomplete city). When the search has completed the set of coordinates
 	 * is added to the list of cities.
 	 * 
 	 * @param searched
 	 *            A set of previously searched game board coordinates.
 	 * @param toSearch
 	 *            A set of game board coordinates to be searched.
 	 * @param cities
 	 *            A list of sets of coordinates which comprise a city.
 	 * @param incompleteCities
 	 *            A list of integers corresponding to any cities in the 'cities'
 	 *            parameter which are incomplete.
 	 */
 	private void getCompletedCitiesRecursive(HashSet<String> searched,
 			HashSet<String> toSearch, ArrayList<HashSet<String>> cities,
 			ArrayList<Integer> incompleteCities) {
 
 		// Take a position from the toSearch map to search.
 		String tilePosition = toSearch.toArray(new String[0])[0];
 		String[] tilePos = tilePosition.split(",");
 
 		int xBoard = Integer.parseInt(tilePos[0]);
 		int yBoard = Integer.parseInt(tilePos[1]);
 		int xTile = Integer.parseInt(tilePos[2]);
 		int yTile = Integer.parseInt(tilePos[3]);
 
 		Tile currentTile = gameBoard[yBoard][xBoard];
 
 		toSearch.remove(tilePosition);
 		searched.add(tilePosition);
 
 		TileType currentTileType = currentTile.getTileType(xTile, yTile);
 
 		// Add neighbors to an array. And run through adding each to toSearch.
 		int[][] neighborTiles = getTileNeighbors(xBoard, yBoard, xTile, yTile);
 
 		// Add valid neighbors of position to toSearch map.
 		// A neighbor is valid if it is in neither map,
 		// and is of the same tile type.
 		for (int i = 0; i < neighborTiles.length; i++) {
 			// Check the tile is not null.
 			Tile tile = gameBoard[neighborTiles[i][1]][neighborTiles[i][0]];
 
 			if (tile != null) {
 
 				String toAdd = "";
 
 				for (int j = 0; j < neighborTiles[i].length; j++) {
 					toAdd += neighborTiles[i][j];
 					toAdd += ",";
 				}
 
 				// Remove the last comma.
 				toAdd = toAdd.substring(0, toAdd.length() - 1);
 
 				// Check that the tile has the same tile type.
 				TileType tileType = tile.getTileType(neighborTiles[i][2],
 						neighborTiles[i][3]);
 
 				// Check the tile is not already in searched or toSearch.
 				if (tileType == currentTileType && !toSearch.contains(toAdd)
 						&& !searched.contains(toAdd)) {
 					toSearch.add(toAdd);
 				}
 
 			} else {
 				// If we find a null tile, add the city's index to
 				// incompleteCities.
 				// Do not subtract 1, as the city has yet to be added;
 				// Once the city is added to cities, the index will be correct.
 				if (!incompleteCities.contains(cities.size())) {
 					incompleteCities.add(cities.size());
 				}
 			}
 		}
 
 		// If toSearch is empty, the add searched to cities. Otherwise recurse.
 		if (!toSearch.isEmpty()) {
 			getCompletedCitiesRecursive(searched, toSearch, cities,
 					incompleteCities);
 		} else {
 			cities.add(searched);
 		}
 
 	}
 
 	/**
 	 * This function searches through the fields to find all adjacent cities.
 	 * Given board coordinates to start with, this function will search out from
 	 * the position recording any adjacent cities along the way. It also tracks
 	 * any meeples which are on the same feature for scoring by the calling
 	 * function.
 	 * 
 	 * @param searched
 	 *            A set of previously searched game board coordinates.
 	 * @param toSearch
 	 *            A set of game board coordinates to be searched.
 	 * @param meeplesOnFeature
 	 *            A list of meeples which are found on the feature.
 	 * @param allCities
 	 *            A list of all completed cities in the game (sets of
 	 *            coordinates).
 	 * @param adjacentCities
 	 *            A list of the adjacent completed cities to the feature being
 	 *            searched (sets of coordinates).
 	 * @param featureProperties
 	 *            An object array used to hold the number of cities adjacent to
 	 *            the searched feature.
 	 */
 	private void fieldScoreRecursive(HashSet<String> searched,
 			HashSet<String> toSearch, ArrayList<Meeple> meeplesOnFeature,
 			ArrayList<HashSet<String>> allCities,
 			ArrayList<HashSet<String>> adjacentCities,
 			Object[] featureProperties) {
 
 		// Take a position from the toSearch map to search.
 		String tilePosition = toSearch.toArray(new String[0])[0];
 		String[] tilePos = tilePosition.split(",");
 
 		int xBoard = Integer.parseInt(tilePos[0]);
 		int yBoard = Integer.parseInt(tilePos[1]);
 		int xTile = Integer.parseInt(tilePos[2]);
 		int yTile = Integer.parseInt(tilePos[3]);
 
 		Tile currentTile = gameBoard[yBoard][xBoard];
 
 		// Search the position & add it to searched map.
 		if (hasMeeple(currentTile, xTile, yTile)) {
 			// Add the meeple on the tile to our list.
 			meeplesOnFeature.add(getMeeple(currentTile, xTile, yTile));
 		}
 
 		toSearch.remove(tilePosition);
 		searched.add(tilePosition);
 
 		// Get the neighbor tile positions.
 		int[][] neighborTiles = getTileNeighbors(xBoard, yBoard, xTile, yTile);
 
 		// Add valid neighbors of position to toSearch map.
 		// A neighbor is valid if it is in neither map, and is a field.
 		for (int i = 0; i < neighborTiles.length; i++) {
 			// Check the tile is not null.
 			Tile tile = gameBoard[neighborTiles[i][1]][neighborTiles[i][0]];
 
 			if (tile != null) {
 				// Add the tile to the toSearch set.
 				String toAdd = "";
 
 				for (int j = 0; j < neighborTiles[i].length; j++) {
 					toAdd += neighborTiles[i][j];
 					toAdd += ",";
 				}
 
 				// Remove the last comma.
 				toAdd = toAdd.substring(0, toAdd.length() - 1);
 
 				// Check that the tile has the same tile type.
 				TileType tileType = tile.getTileType(neighborTiles[i][2],
 						neighborTiles[i][3]);
 
 				// Add the tile to be searched if it is also a field.
 				if (tileType == TileType.FIELD) {
 					// Check the tile is not already in searched or toSearch.
 					if (!toSearch.contains(toAdd) && !searched.contains(toAdd)) {
 						toSearch.add(toAdd);
 					}
 				} else if (tileType == TileType.CITY) {
 					// If the neighboring tile is a city tile, find the city
 					// that it belongs to and add that city to our adjacent
 					// cities (only if not already in the adjacent cities).
 					boolean newCity = true;
 
 					for (int j = 0; j < adjacentCities.size(); j++) {
 						if (adjacentCities.get(j).contains(toAdd)) {
 							newCity = false;
 						}
 					}
 
 					if (newCity) {
 						for (int j = 0; j < allCities.size(); j++) {
 							if (allCities.get(j).contains(toAdd)) {
 								adjacentCities.add(allCities.get(j));
 							}
 						}
 					}
 
 				}
 			}
 		}
 
 		// If there are still tiles in toSearch then continue searching.
 		if (!toSearch.isEmpty()) {
 			fieldScoreRecursive(searched, toSearch, meeplesOnFeature,
 					allCities, adjacentCities, featureProperties);
 		} else {
 			// Get the number of cities searched.
 			featureProperties[0] = adjacentCities.size();
 		}
 
 	}
 
 	/**
 	 * This function gets neighbor tile positions from a given position. It
 	 * compensates for tile positions which are on the edge of a given tile by
 	 * testing for these cases and will overflow to positions on the neighboring
 	 * tiles if needed.
 	 * 
 	 * @param xBoard
 	 *            An x position on the board.
 	 * @param yBoard
 	 *            An y position on the board.
 	 * @param xTile
 	 *            An x tile position on the board.
 	 * @param yTile
 	 *            An y tile position on the board.
 	 * @return An integer array containing the coordinates of all neighboring
 	 *         tile positions (not tiles).
 	 */
 	private int[][] getTileNeighbors(int xBoard, int yBoard, int xTile,
 			int yTile) {
 
 		int[] nStr = { xBoard, yBoard, xTile, (yTile - 1) };
 		int[] eStr = { xBoard, yBoard, (xTile + 1), yTile };
 		int[] sStr = { xBoard, yBoard, xTile, (yTile + 1) };
 		int[] wStr = { xBoard, yBoard, (xTile - 1), yTile };
 
 		// Catch any across-tiles conditions.
 		// If yTile == 6 then we set it to 0 and increment yBoard.
 		// If yTile == 0 then we set it to 6 and decrement yBoard.
 		// If xTile == 6 then we set it to 0 and increment xBoard.
 		// If xTile == 0 then we set it to 6 and decrement xBoard.
 		// Only for those which we are adding or subtracting from.
 		if (yTile == 6) {
 			sStr[3] = 0;
 			sStr[1]++;
 		}
 
 		if (yTile == 0) {
 			nStr[3] = 6;
 			nStr[1]--;
 		}
 
 		if (xTile == 6) {
 			eStr[2] = 0;
 			eStr[0]++;
 		}
 
 		if (xTile == 0) {
 			wStr[2] = 6;
 			wStr[0]--;
 		}
 
 		int[][] neighborTiles = { nStr, eStr, sStr, wStr };
 
 		return neighborTiles;
 	}
 
 	// Used for the ui; return the size of the array.
 	public int getWidth() {
 		return gameBoard[0].length;
 	}
 
 	public int getHeight() {
 		return gameBoard.length;
 	}
 
 }
