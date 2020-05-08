 package model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import model.Cell.ECell;
 import static model.Cell.ECell.*;
 
 /**
  * Represents a map
  *
  */
 
 public class Map implements Cloneable
 {
 	protected ArrayList<ArrayList<Cell>> map = null;
 	protected ArrayList<Position> goals = null;
 	protected ArrayList<Box> boxes;
 	
 	// Todo : model with a Player, not only a position
 	protected Position playerPosition = null;
 	protected int height = 0;
 	protected int width = 0;
 	
 	public Map()
 	{
 		map = new ArrayList<ArrayList<Cell>>();
 		goals = new ArrayList<Position>();
 		boxes = new ArrayList<Box>();
 		playerPosition = new Position();
 		height = 0;
 		width = 0;
 	}
 	
 	public Map(BufferedReader bf)
 	{
 		this();
 		map = extractBuffer(bf);
 		height = map.size();
 		fillTheMapWithEmptyFloor();
 	}
 	
 	public Map(ArrayList<String> sList)
 	{
 		this();
 		map = extractString(sList);
 		height = map.size();
 		fillTheMapWithEmptyFloor();
 	}
 	
 	@Override
 	public Map clone() throws CloneNotSupportedException {
 		
 		
 		Map copie = (Map)super.clone();
 		
 		// Clone the goals
 		copie.goals = new ArrayList<Position>();
 		copie.boxes = new ArrayList<Box>();
 
 		for(Position position : goals)
 		{
 			copie.goals.add(position.clone());
 		}
 
 		for(Box box: boxes) {
 			copie.boxes.add(box.clone());
 		}
 		
 		// Clone the map
 		copie.map = new ArrayList<ArrayList<Cell>>();
 		copie.map.clear();
 		
 		
 		for(ArrayList<Cell> rowCells : map)
 		{
 			ArrayList<Cell> cloneRow = new ArrayList<Cell>();
 			for(Cell cell : rowCells)
 			{
 				switch(cell.getType())
 				{
 					case WALL:
 						cloneRow.add(new Cell(Cell.ECell.WALL));
 						break;
 					case PLAYER:
 						cloneRow.add(new Cell(Cell.ECell.PLAYER));
 						break;
 					case BOX:
 						cloneRow.add(new Cell(Cell.ECell.BOX));
 						break;
 					case BOX_ON_GOAL:
 						cloneRow.add(new Cell(Cell.ECell.BOX_ON_GOAL));
 						break;
 					case GOAL_SQUARE:
 						cloneRow.add(new Cell(Cell.ECell.GOAL_SQUARE));
 						break;
 					case EMPTY_FLOOR:
 						cloneRow.add(new Cell(Cell.ECell.EMPTY_FLOOR));
 						break;
 					case VISITED:
 						cloneRow.add(new Cell(Cell.ECell.VISITED));
 						break;
 				}
 				
 			}
 			copie.map.add(cloneRow);
 		}
 		
 		// Clone the player position
 		copie.playerPosition = playerPosition.clone();
 		
 		return copie;
 	}
 	
 	public void movePlayer(Position position) throws CloneNotSupportedException
 	{
 		
 		if(getCellFromPosition(position).getType().equals(Cell.ECell.GOAL_SQUARE))
 		{
 			set(Cell.ECell.PLAYER_ON_GOAL_SQUARE, position);
 			set(Cell.ECell.VISITED, playerPosition);
 			playerPosition = position.clone();
 		}
 		else if(getCellFromPosition(position).getType().equals(Cell.ECell.EMPTY_FLOOR))
 		{
 			set(Cell.ECell.PLAYER, position);
 			set(Cell.ECell.VISITED, playerPosition);
 			playerPosition = position.clone();
 		}
 	}
 	
 	public String toString()
 	{
 		String result = "";
 		
 		for(ArrayList<Cell> rowCell : map)
 		{
 			for(Cell cell : rowCell)
 			{
 				result += Cell.cellToChar(cell.getType());
 			}
 			result += "\n";
 		}
 		return result;
 	}
 	
 	public void set(Cell.ECell cell, Position position)
 	{
 		if(isPositionOnTheMap(position))
 		{
 			ArrayList<Cell> row = new ArrayList<Cell>();
 			row = map.get(position.getI());
 			row.set(position.getJ(), new Cell(cell));
 			
 			map.set(position.getI(), row);
 		}
 	}
 	
 	protected ArrayList<ArrayList<Cell>> extractString(ArrayList<String> sList)
 	{
 		
 		int i = 0;
 		for(String s : sList)
 		{
 			
 			ArrayList<Cell> mapRow = new ArrayList<Cell>();
 			
 			if(s.length() > width)
 			{
 				width = s.length();
 			}
 			
 			for(int j = 0; j < s.length(); j++)
 			{
 				if(Cell.charToCell(s.charAt(j)) != null)
 				{
 					Cell cell = new Cell(Cell.charToCell(s.charAt(j)));
 									
 					switch(cell.getType())
 					{
 						case GOAL_SQUARE:
 							addGoal(new Position(i,j));
 							break;
 						case PLAYER:
 							setPlayerPosition(new Position(i,j));
 							break;
 						case PLAYER_ON_GOAL_SQUARE:
 							setPlayerPosition(new Position(i,j));
 							break;
 						case BOX:
 							addBox(new Position(i,j), false);
 							break;
 						case BOX_ON_GOAL:
 							addBox(new Position(i,j), true);
 							break;
 					}
 				
 					mapRow.add(cell);
 				}
 			}
 			
 			map.add(mapRow);
 			i++;
 		}
 		
 		return map;
 	}
 	
 	protected ArrayList<ArrayList<Cell>> extractBuffer(BufferedReader bf)
 	{
 		try
 		{
 			map = new ArrayList<ArrayList<Cell>>();
 			
 			int i = 0;
 			String currentLine;
 			
 			while ((currentLine = bf.readLine()) != null) {
 				
 				ArrayList<Cell> mapRow = new ArrayList<Cell>();
 				
 				if(currentLine.length() > width)
 				{
 					width = currentLine.length();
 				}
 				
 				for(int j = 0; j < currentLine.length(); j++)
 				{
 					if(Cell.charToCell(currentLine.charAt(j)) != null)
 					{
 						Cell cell = new Cell(Cell.charToCell(currentLine.charAt(j)));
 										
 						switch(cell.getType())
 						{
 							case GOAL_SQUARE:
 								addGoal(new Position(i,j));
 								break;
 							case PLAYER:
 								setPlayerPosition(new Position(i,j));
 								break;
 							case PLAYER_ON_GOAL_SQUARE:
 								setPlayerPosition(new Position(i,j));
 								break;
 							case BOX:
 								System.out.println(i + " " + j);
 								addBox(new Position(i, j), false);
 								System.out.println(boxes);
 								break;
 							case BOX_ON_GOAL:
 								addBox(new Position(i, j), true);
 								break;
 						}
 					
 						mapRow.add(cell);
 					}
 				}
 				
 				map.add(mapRow);
 				i++;
 			}
 			
 			return map;
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			try {
 				if (bf != null)bf.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 		
 		return null;
 	}
 	
 	protected void fillTheMapWithEmptyFloor()
 	{
 		ArrayList<ArrayList<Cell>> filledMap = new ArrayList<ArrayList<Cell>>();			
 		
 		for(ArrayList<Cell> rowCellsInit : map)
 		{
 			ArrayList<Cell> row = new ArrayList<Cell>();
 			for(int j = 0; j < width; j++)
 			{
 				row.add(new Cell(Cell.ECell.EMPTY_FLOOR));
 			}
 			int k = 0;
 			for(Cell cell : rowCellsInit)
 			{
 				switch(cell.getType())
 				{
 				case WALL:
 					row.set(k, new Cell(Cell.ECell.WALL));
 					break;
 				case PLAYER:
 					row.set(k, new Cell(Cell.ECell.PLAYER));
 					break;
 				case PLAYER_ON_GOAL_SQUARE:
 					row.set(k, new Cell(Cell.ECell.PLAYER_ON_GOAL_SQUARE));
 					break;
 				case BOX:
 					row.set(k, new Cell(Cell.ECell.BOX));
 					break;
 				case BOX_ON_GOAL:
 					row.set(k, new Cell(Cell.ECell.BOX_ON_GOAL));
 					break;
 				case GOAL_SQUARE:
 					row.set(k, new Cell(Cell.ECell.GOAL_SQUARE));
 					break;
 				case EMPTY_FLOOR:
 					row.set(k, new Cell(Cell.ECell.EMPTY_FLOOR));
 					break;
 				case VISITED:
 					row.set(k, new Cell(Cell.ECell.VISITED));
 					break;
 				}
 				k++;
 			}
 			
 			filledMap.add(row);
 		}
 		
 		map = filledMap;
 	}
 	
 	public boolean isPositionOnTheMap(Position position)
 	{
 		return (position.getI() >= 0 && position.getJ() >= 0 
 				&& map != null 
 				&& position.getI() < map.size() 
 				&& position.getJ() < width);
 	}
 
 	public void putBoxOnGoal(Box box, Position goal, String boxPath) throws CloneNotSupportedException {
 		Position boxPos = box.getPosition();
 		ECell boxCellType = getCellFromPosition(boxPos).getType();
 		Position playerPos = goal.clone();
 		ECell playerPosType = getCellFromPosition(playerPos).getType();
 
 		switch (boxPath.charAt(boxPath.length())) {
 
 		case 'U':
 			playerPos.down(this);
 		case 'D':
 			playerPos.up(this);
 		case 'L':
 			playerPos.right(this);
 		case 'R':
 			playerPos.left(this);
 		}
 
 		set(BOX_ON_GOAL, goal);
 
 		if (boxCellType == BOX_ON_GOAL)
 			set(GOAL_SQUARE, boxPos);
 		else if (boxCellType == BOX)
 			set(EMPTY_FLOOR, boxPos);
 
 		if (playerPosType == GOAL_SQUARE)
 			set(PLAYER_ON_GOAL_SQUARE, playerPos);
 		else if (playerPosType == EMPTY_FLOOR || playerPosType == VISITED)
 			set(PLAYER, playerPos);
 
 			
 		box.setPosition(goal.clone());
 	}
 	
 	public Cell getCellFromPosition(Position position)
 	{
 		if(isPositionOnTheMap(position))
 		{
 			return map.get(position.getI()).get(position.getJ());
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 
 	public ArrayList<Position> getGoals() {
 		return goals;
 	}
 
 	public ArrayList<Box> getBoxes() {
 		return boxes;
 	}
 
 	public int getNumberOfBoxes() {
 		return boxes.size();
 	}
 
 	public void setGoals(ArrayList<Position> goals) {
 		this.goals = goals;
 	}
 
 	public int getNumberOfGoals() {
 		return goals.size();
 	}
 
 	protected void addBox(Position position, boolean onGoal) {
 		boxes.add(new Box(position, onGoal));
 	}
 	
 	protected void addGoal(Position position)
 	{
 		goals.add(position);
 	}
 
 	public ArrayList<ArrayList<Cell>> getMap() {
 		return map;
 	}
 
 	public void setMap(ArrayList<ArrayList<Cell>> map) {
 		this.map = map;
 	}
 
 	public Position getPlayerPosition() {
 		return playerPosition;
 	}
 
 	public void setPlayerPosition(Position playerPosition) {
 		this.playerPosition = playerPosition;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 	
 	public void toStringAccessible()
 	{
 		for(ArrayList<Cell> lCells : map)
 		{
 			for(Cell c : lCells)
 			{
 				if(c.isAccessible())
 				{
 					System.out.print(" ");
 				}
 				else
 				{
 					System.out.print("#");
 				}
 			}
 			System.out.print('\n');
 		}
 	}

	public void setBoxes(ArrayList<Box> boxes) {
		this.boxes = boxes;
	}
	
	
 	
 }
