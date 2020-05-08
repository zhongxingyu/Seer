 package clue;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 public class Board {
 	private int numRows, numCols;
 	private String[] config;
 	private HashMap<Character, String> rooms;
 	private HashMap<Integer, BoardCell> cellCache;
 
 	public Board() {
 		rooms = new HashMap<Character, String>();
 		cellCache = new HashMap<Integer, BoardCell>();
 	}
 
 	public HashMap<Character, String> getRooms() {
 		return rooms;
 	}
 
 	public int getNumRows() {
 		return numRows;
 	}
 
 	public int getNumCols() {
 		return numCols;
 	}
 
 	public void loadConfigFiles(String board, String legend) throws IOException, BadConfigFormatException {
 		// Import Legend
 		FileReader legendReader = new FileReader(legend);
 		Scanner legendIn = new Scanner(legendReader);
 		String line, parts[];
 		while(legendIn.hasNextLine()) {
 			line = legendIn.nextLine();
 			parts = line.split(", ");
 			if(parts.length != 2 || parts[0] == "" || parts[1] == "") {
 				legendIn.close();
 				legendReader.close();
 				throw new BadConfigFormatException("Legend is malformed.");
 			}
 			rooms.put(parts[0].charAt(0), parts[1]);
 		}
 		legendIn.close();
 		legendReader.close();
 
 		// Import Clue Board
 		FileReader boardReader = new FileReader(board);
 		Scanner boardIn = new Scanner(boardReader);
 		String configString = "";
 		int col_count = -1;
 		int row_count = 0;
 
 		while(boardIn.hasNextLine()) {
 			line = boardIn.nextLine();
 			configString += line + ",";
 			parts = line.split(",");
 			if(col_count == -1) {
 				col_count = parts.length;
 			} else {
 				if(col_count != parts.length) {
 					boardIn.close();
 					boardReader.close();
 					throw new BadConfigFormatException("Line length mismatch in board config.");
 				}
 			}
 			row_count += 1;
 		}
 		boardIn.close();
 		boardReader.close();
 
 		String[] tempConfig = configString.split(",");
 		String value;
 		for(String i : tempConfig) {
 			value = rooms.get(i.charAt(0));
 			if(value == null) throw new BadConfigFormatException("Invalid room character in board config.");
 		}
		numCols = col_count;
		numRows = row_count;
 		config = tempConfig;
 	}
 
 	public int calcIndex(int row, int col) {
 		// Does not check for bad row/col values...
 		return (row * numCols) + col;
 	}
 
 	public RoomCell getRoomCellAt(int row, int col) {
 		// No error checking? What happens if this is not a room cell?
 		// Note that this does not use the cellCache, because we shouldn't
 		// risk filling our cache with arbitrary RoomCells instead of cells
 		// based on the config.
 		int index = calcIndex(row, col);
 		return new RoomCell(index, numRows, numCols, config[index]);
 	}
 
 	public BoardCell getCellAt(int i) {
 		if(cellCache.get(i) == null) {
 			// Then create the cell.
 			if(config[i].equals("W")) {
 				cellCache.put(i, new WalkwayCell(i, numRows, numCols));
 			} else {
 				cellCache.put(i, new RoomCell(i, numRows, numCols, config[i]));
 			}
 		}
 		return cellCache.get(i);
 	}
 
 	public LinkedList<Integer> getAdjList(int index) {
 		LinkedList<Integer> adjList = new LinkedList<Integer>();
 		LinkedList<Integer> adjCells = new LinkedList<Integer>();
 		BoardCell cell = getCellAt(index);
 		if(cell.isDoorway()) {
 			switch(((RoomCell) cell).getDoorDirection()) {
 				case UP: adjCells.add(cell.top); break;
 				case RIGHT: adjCells.add(cell.right); break;
 				case DOWN: adjCells.add(cell.bottom); break;
 				case LEFT: adjCells.add(cell.left); break;
 				default: break;
 				// isDoorway already checks that direction is not NONE,
 				// default case should never happen.
 			}
 			// This is the only adjacency for doors.
 			return adjCells;
 		} else if(cell.isRoom()) {
 			// Room cells (that are not doors) don't have any adjacencies.
 			return adjCells;
 		}
 
 		// Here, we use the fact that LinkedLists and arrays are both ordered
 		// to associate each link with its proper door direction. If we are
 		// moving up, we may only enter doors with a direction of DOWN, etc.
 		adjList.add(cell.top);
 		adjList.add(cell.right);
 		adjList.add(cell.bottom);
 		adjList.add(cell.left);
 		RoomCell.DoorDirection[] cardinals = {RoomCell.DoorDirection.DOWN, RoomCell.DoorDirection.LEFT, RoomCell.DoorDirection.UP, RoomCell.DoorDirection.RIGHT};
 		for(int i = 0; i<cardinals.length; ++i) {
 			if(adjList.get(i) == null) {
 				continue;
 			}
 			BoardCell cellTemp = getCellAt(adjList.get(i));
 			if(cellTemp.isWalkway() || (cellTemp.isDoorway() && ((RoomCell) cellTemp).getDoorDirection() == cardinals[i])) {
 				// The cell is either a walkway, or a door with the correct direction.
 				// Just trust us, don't try to think about it.
 				adjCells.add(cellTemp.index);
 			}
 		}
 		return adjCells;
 	}
 
 	public HashSet<BoardCell> getTargets(int index, int steps) {
 		// This is the initial setup function that calls our recursive calcTargets().
 		HashSet<Integer> targetList = new HashSet<Integer>();
 		HashSet<Integer> visitedList = new HashSet<Integer>();
 		steps = steps + 1;
 		if(getCellAt(index).isDoorway()) {
 			targetList = calcTargets(getAdjList(index).get(0), steps - 1, targetList, visitedList);
 			targetList.remove(index);
 		} else {
 			targetList = calcTargets(index, steps, targetList, visitedList);
 		}
 		// Ssh, this was a Set of cells all along.
 		HashSet<BoardCell> targetCells = new HashSet<BoardCell>();
 		for(int target : targetList) {
 			targetCells.add(getCellAt(target));
 		}
 		return targetCells;
 	}
 
 	private HashSet<Integer> calcTargets(int start, int steps, HashSet<Integer> list, HashSet<Integer> visited) {
 		// This is our recursive function that creates the targets list.
 		steps = steps - 1;
 		visited.add(start);
 		if(getCellAt(start).isDoorway()) {
 			list.add(start);
 		} else if(steps == 0) {
 			list.add(start);
 		} else {
 			for(Integer adjCell : getAdjList(start)) {
 				HashSet<Integer> visitedTemp = new HashSet<Integer>(visited);
 				if(!visited.contains(adjCell)) {
 					list = calcTargets(adjCell, steps, list, visitedTemp);
 				}
 			}
 		}
 		return list;
 	}
 }
