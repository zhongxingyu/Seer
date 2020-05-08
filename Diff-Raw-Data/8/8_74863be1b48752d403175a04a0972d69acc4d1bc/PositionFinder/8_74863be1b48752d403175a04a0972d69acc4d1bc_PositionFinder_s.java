 package model;
 
 import java.util.ArrayList;
 
 import static model.Cell.ECell.*;
 import model.Cell.ECell;
 
 import exception.PathNotFoundException;
 import java.io.IOException;
 
 public class PositionFinder {
 
 	PositionFinder() {
 	}
 
 	public ArrayList<Position> findEmptySpacesAround(Position position, Map map, Cell.ECell what) throws CloneNotSupportedException, IOException {
 		ArrayList<Position> spaces = new ArrayList<Position>(4);
 		char[] dirs = {'U', 'D', 'L', 'R'};
 		for (int i=0; i<4; i++) {
 			if (isValidMove(map, position, what, dirs[i]))
 				spaces.add(position.unboundMove(dirs[i]));
 		}
 		return spaces;
 	}
 
 	private boolean isPlayerAccessible(Map map, Position position) {
 		ECell cellType = map.getCellFromPosition(position).getType();	
		if (cellType == VISITED || cellType == EMPTY_FLOOR || cellType == GOAL_SQUARE)
		return true;
		else return false;
 	}
 
 	private ECell getCellType(Map map, Position pos) {
 		return map.getCellFromPosition(pos).getType();
 	}
 	
 	//[up down left right]
 	private ECell[] getAdjacentCellTypes(Map map, Position position) throws CloneNotSupportedException {
 		ECell[] cells = new ECell[4];
 		cells[0] = getCellType(map, position.unboundMove('U'));
 		cells[1] = getCellType(map, position.unboundMove('D'));
 		cells[2] = getCellType(map, position.unboundMove('L'));
 		cells[3] = getCellType(map, position.unboundMove('R'));
 		return cells;
 	}
 
 	private ECell[] getSurroundingCellTypes(Map map, Position position) throws CloneNotSupportedException {
 		char[] dirs = {'U', 'R', 'D', 'D', 'L', 'L', 'U', 'U'};
 		ECell[] surr = new ECell[8];
 		for (int i=0; i<8; i++) {
 			surr[i] = getCellType(map, position.unboundMove(dirs[i]));
 		}
 		return surr;
 	}
 
 	private boolean isCorner(Map map, Position position) throws CloneNotSupportedException {
 		ECell[] cells = getAdjacentCellTypes(map, position); //[up down left right]
 		return ((cells[0] == WALL || cells[1] == WALL) && (cells[2] == WALL || cells[3] == WALL));
 	}
 
 	private boolean boxWillDeadlock(Map map, Position pos) throws CloneNotSupportedException {
 		int numAdjBoxes = 0;
 		for (ECell c : getSurroundingCellTypes(map, pos)) {//[up down left right]
 			if (c == BOX || c == BOX_ON_GOAL)
 				numAdjBoxes++;
 		}
		if (numAdjBoxes > 2) return true;
		else return false;
 	}
 
 	char[] getOrthogonals(char dir) {
 		char[] orthos = new char[2];
 		if (dir == 'U' || dir == 'D') {
 			char[] lr = {'L', 'R'};
 			return lr;
 			}
 		else {
 			char[] ud = {'U', 'D'};
 			return ud;
 			}
 	}
 
 	private boolean isGoal(Map map, Position pos) {
 		return (getCellType(map,pos) == GOAL_SQUARE);
 	}
 
 	private boolean isValidBoxSquare(Map map, Position pos) {
 		ECell type = getCellType(map, pos);
 		return (type == EMPTY_FLOOR || type == VISITED || type == GOAL_SQUARE);
 	}
 
 	private boolean boxWillStickOnWall(Map map, Position pos, char dir) throws CloneNotSupportedException {
 		Position target = pos.unboundMove(dir);
 		Position twoAway = target.unboundMove(dir);
 		if (map.isPositionOnTheMap(twoAway)) {
 			ECell twoAwayType = getCellType(map, pos);
 			if (twoAwayType == WALL) {
 				char[] orthos = getOrthogonals(dir);
 				Position spreader1 = target.unboundMove(orthos[0]);
 				Position spreader2 = target.unboundMove(orthos[1]);
 				while (isValidBoxSquare(map, spreader1)) {
 					if (isValidBoxSquare(map, spreader1.unboundMove(dir)) || isGoal(map, spreader1.unboundMove(dir)))
 						return false;
 					spreader1.unboundIncrement(orthos[0]);
 				}
 				while (isValidBoxSquare(map, spreader2)) {
 					if (isValidBoxSquare(map, spreader2.unboundMove(dir)) || isGoal(map, spreader2.unboundMove(dir)))
 						return false;
 					spreader1.unboundIncrement(orthos[1]);
 				}
 			}
 		}
 		return true;
 	}
 
 	private boolean isValidMove(Map map, Position position, Cell.ECell what, char dir) throws CloneNotSupportedException {
 		if (!map.isPositionOnTheMap(position.unboundMove(dir)))
 			return false;
 		boolean isPlayer = (what == PLAYER || what == PLAYER_ON_GOAL_SQUARE);
 
 		if (isPlayer && isPlayerAccessible(map, position))
 			return true;
 		else {
 			if (isValidBoxSquare(map, position.unboundMove(dir))) {
 				if (getCellType(map, position.unboundMove(dir)) == GOAL_SQUARE) return true;
 				if (isCorner(map, position.unboundMove(dir))) return false;
 				if (boxWillDeadlock(map, position.unboundMove(dir))) return false;
 				if (boxWillStickOnWall(map, position, dir)) return false;
 			}
 		}
 		return true;
 
 	}
 
 }
