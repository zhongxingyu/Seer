 package org.concordia.kingdoms.strategies;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.concordia.kingdoms.Player;
 import org.concordia.kingdoms.board.Board;
 import org.concordia.kingdoms.board.Entry;
 import org.concordia.kingdoms.board.TDCoordinate;
 import org.concordia.kingdoms.domain.Castle;
 import org.concordia.kingdoms.domain.Color;
 import org.concordia.kingdoms.domain.Tile;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class MinimizeStrategy implements IStrategy<TDCoordinate> {
 
 	private VirtualTDMatrix tdMatrix;
 
 	public MinimizeStrategy() {
 	}
 
 	public Entry<TDCoordinate> getEntry(Player<TDCoordinate> player,
 			List<Tile> tiles, List<Castle> castles,
 			List<TDCoordinate> emptyCoordinates) {
 
 		List<List<Integer>> rowsScores = Lists.newArrayList();
 
 		for (int i = 0; i < Board.MAX_ROWS; i++) {
 			List<Integer> rowScores = Lists.newArrayList();
 			tdMatrix.getTilesScore(0, Board.MAX_COLUMNS, i, true, rowScores);
 			rowsScores.add(rowScores);
 		}
 
 		List<List<Integer>> columnsScores = Lists.newArrayList();
 
 		for (int i = 0; i < Board.MAX_COLUMNS; i++) {
 			List<Integer> columnScores = Lists.newArrayList();
 			tdMatrix.getTilesScore(0, Board.MAX_ROWS, i, false, columnScores);
 			columnsScores.add(columnScores);
 		}
 
 		List<List<Map<Color, Integer>>> rowsCastlesRanks = Lists.newArrayList();
 
 		for (int i = 0; i < Board.MAX_ROWS; i++) {
 			List<Map<Color, Integer>> rowCastleRanks = Lists.newArrayList();
 			tdMatrix.getCastleRankScore(0, Board.MAX_COLUMNS, i, true,
 					rowCastleRanks);
 			rowsCastlesRanks.add(rowCastleRanks);
 		}
 
 		List<List<Map<Color, Integer>>> columnsCastlesRanks = Lists
 				.newArrayList();
 
 		for (int i = 0; i < Board.MAX_COLUMNS; i++) {
 			List<Map<Color, Integer>> columnCastlesRanks = Lists.newArrayList();
 			tdMatrix.getCastleRankScore(0, Board.MAX_ROWS, i, false,
 					columnCastlesRanks);
 			columnsCastlesRanks.add(columnCastlesRanks);
 		}
 
 		Map<TDCoordinate, Integer> coordinateScores = Maps.newHashMap();
 
 		for (TDCoordinate coordinate : emptyCoordinates) {
 
 			int row = coordinate.getRow();
 			int column = coordinate.getColumn();
 
 			fillCastleScoreCoordinates(player, rowsScores, rowsCastlesRanks,
 					coordinateScores, coordinate, row);
 			fillCastleScoreCoordinates(player, columnsScores,
 					columnsCastlesRanks, coordinateScores, coordinate, column);
 		}
 
 		TDCoordinate maxCoordinate = getMaxCoordinate(coordinateScores);
 
 		if (isStartingTileNegative(player)) {
 			return new Entry<TDCoordinate>(maxCoordinate,
 					player.getStartingTile());
 		}
 
 		int drawTileOrCastle = new Random().nextInt(2);
 
 		if (drawTileOrCastle == 0) {
			return drawTile(player, coordinateScores, maxCoordinate);
		} else {

			if (castles.size() > 0) {
				Castle castle = castles.remove(castles.size() - 1);
				return new Entry<TDCoordinate>(maxCoordinate, castle);

 			} else {
				return drawTile(player, coordinateScores, maxCoordinate);
 			}
 		}
 
 	}
 
	private Entry<TDCoordinate> drawTile(Player<TDCoordinate> player,
			Map<TDCoordinate, Integer> coordinateScores,
			TDCoordinate maxCoordinate) {
		Tile tile = player.drawTile();
		if (tile.getValue() == null || tile.getValue() < 0) {
			return new Entry<TDCoordinate>(maxCoordinate, tile);
		} else {
			TDCoordinate coordinate = getMinCoordinate(coordinateScores);
			return new Entry<TDCoordinate>(coordinate, tile);
		}
	}

 	private boolean isStartingTileNegative(Player<TDCoordinate> player) {
 		final Tile startTile = player.getStartingTile();
 		if (!player.isStartingTileUsed()) {
 			if (startTile.getValue() == null || startTile.getValue() < 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void fillCastleScoreCoordinates(Player<TDCoordinate> player,
 			List<List<Integer>> rowsScores,
 			List<List<Map<Color, Integer>>> rowsCastlesRanks,
 			Map<TDCoordinate, Integer> coordinateScores,
 			TDCoordinate coordinate, int row) {
 		List<Map<Color, Integer>> rowCastleRanks = rowsCastlesRanks.get(row);
 
 		if (rowCastleRanks != null && !rowCastleRanks.isEmpty()) {
 			int i = 0;
 			for (Map<Color, Integer> castleRank : rowCastleRanks) {
 				Iterator<Color> eachCastleColorItr = castleRank.keySet()
 						.iterator();
 				while (eachCastleColorItr.hasNext()) {
 					Color castleColor = eachCastleColorItr.next();
 
 					// checking for other's castles
 					if (!castleColor.equals(player.getChosenColor())) {
 						Integer rank = castleRank.get(castleColor);
 						int rowScore = rowsScores.get(row).get(i);
 						int castleScore = rank * rowScore;
 						coordinateScores.put(coordinate, castleScore);
 					}
 				}
 				i++;
 			}
 		}
 	}
 
 	private TDCoordinate getMaxCoordinate(
 			Map<TDCoordinate, Integer> coordinateScores) {
 		int min = Collections.min(coordinateScores.values());
 		Iterator<TDCoordinate> itr = coordinateScores.keySet().iterator();
 		TDCoordinate maxCoordinate = null;
 		int maxValue = min;
 		while (itr.hasNext()) {
 			TDCoordinate tdCoordinate = itr.next();
 			int value = coordinateScores.get(tdCoordinate);
 			if (maxValue <= value) {
 				maxValue = value;
 				maxCoordinate = tdCoordinate;
 			}
 		}
 		return maxCoordinate;
 	}
 
 	private TDCoordinate getMinCoordinate(
 			Map<TDCoordinate, Integer> coordinateScores) {
 		int max = Collections.max(coordinateScores.values());
 		Iterator<TDCoordinate> itr = coordinateScores.keySet().iterator();
 		TDCoordinate minCoordinate = null;
 		int minValue = max;
 		while (itr.hasNext()) {
 			TDCoordinate tdCoordinate = itr.next();
 			int value = coordinateScores.get(tdCoordinate);
 			if (minValue > value) {
 				minValue = value;
 				minCoordinate = tdCoordinate;
 			}
 		}
 		return minCoordinate;
 	}
 
 	public void setEntries(Iterator<Entry<TDCoordinate>> entries) {
 		this.tdMatrix = new VirtualTDMatrix(entries, 5, 6);
 	}
 
 }
