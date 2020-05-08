 package model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import model.Cell.ECell;
 
 import exception.PathNotFoundException;
 
 
 /**
  * 
  * Represents the Agent which is able to solve a given problem
  * The agent is the AI
  *
  */
 public class Agent {
 	
 	protected Map map;
 	protected Moves moves;
 	protected LinkedList<Node> nodes = new LinkedList<Node>();
 	protected AStarSearch astar = new AStarSearch();
 		
 	
 	public Agent()
 	{
 		map = new Map();
 		moves = new Moves();
 		nodes = new LinkedList<Node>();
 	}
 	
 	public Agent(ArrayList<String> stringMap)
 	{
 		this();
 		map = new Map(stringMap);
 	}
 	
 	/**
 	 * Computes the distance between two positions
 	 * @author arthur
 	 * @param position1
 	 * @param position2
 	 * @return int : distance
 	 */
 	public int distance(Position position1, Position position2)
 	{
 		return (position1.getI() - position2.getI())*(position1.getI() - position2.getI()) + (position1.getJ() - position2.getJ())*(position1.getJ() - position2.getJ());
 	}
 	
 	/**
 	 * Computes the minimum distance between on position and a list of positions
 	 * @author arthur
 	 * @param position
 	 * @param positions
 	 * @return the minimum distance
 	 *          if the list is empty, return -1
 	 */
 	public int distance(Position position, ArrayList<Position> positions)
 	{
 		if(positions != null && positions.size() > 0)
 		{
 			int result = distance(position, positions.get(0));
 			
 			for(Position pos : positions)
 			{
 				int dist = distance(position, pos);
 				if(result > dist)
 				{
 					result = dist;
 				}
 			}
 			
 			return result;
 		}
 		else
 		{
 			return -1;
 		}
 	}
 	
 	
 	/**
 	 * 
 	 * Set the cells to accessible when the player can reach them
 	 * 
 	 * @author arthur
 	 * @param map
 	 * @return
 	 */
 	public Map setCellAccessible(Map map)
 	{
 		
 		List<Node> lNodes = new LinkedList<Node>();
 		List<Node> lNodesVisited = new LinkedList<Node>();
 		
 		if(map.getPlayerPosition().equals(null))
 		{
 			// TODO : throw an exception instead
 			return null;
 		}
 		lNodes.add(new Node(map.getPlayerPosition()));
 		map.getCellFromPosition(map.getPlayerPosition()).setAccessible(true);
 		//System.out.println("Nodes : "+ lNodes);
 		
 		Position current;
 		
 		while(!lNodes.isEmpty())
 		{
 			current = ((LinkedList<Node>)lNodes).getFirst().getPosition();
 			//System.out.println(current);
 			
 			
 			for(Position p : astar.findEmptySpacesAround(current, map))
 			{
 				Node n = new Node(p);
 				
 				//System.out.println("visited : "+ lNodesVisited);
 				if(!lNodesVisited.contains(n))
 				{
 					//System.out.println("test" + p);
 					map.getCellFromPosition(p).setAccessible(true);
 					
 					lNodes.add(n);
 				}	
 			}
 			
 			lNodesVisited.add(new Node(current));
 			lNodes.remove(new Node(current));
 		}
 		
 		//map.toStringAccessible();
 		return map;
 	}
 	
 
 	
 	
 	/**
 	* Finds a box-to-goal path for each box.
 	*
 	* Result is only guaranteed to be accurate if the strings are accessed in the 
 	* order in which they are stored in the String array.
 	*
 	* @author Alden Coots <ialden.coots@gmail.com>
 	* @throws CloneNotSupportedException 
 	*/
 	public String[] getBoxToGoalPaths(Map map) throws CloneNotSupportedException, PathNotFoundException, IOException {
 		String[] paths = new String[map.getNumberOfBoxes()];
 		ArrayList<Box> orderedBoxes = new ArrayList<Box>();
 		findBoxToGoalPaths(orderedBoxes, map, paths);
 		
 		return paths;
 	}
 
 	private boolean findBoxToGoalPaths(ArrayList<Box> orderedBoxes, Map map, String[] paths) throws CloneNotSupportedException, PathNotFoundException, IOException {
 		if (map.getNumberOfBoxes() == 0) {
 			map.setBoxes(orderedBoxes);
 			if (findSequentialBoxToGoalPaths(map, paths, 0)) return true;
 			}
 		else {
 			boolean isSolved = false;
 			for (int b=0; b<map.getNumberOfBoxes(); b++) {
 				orderedBoxes.add(map.getBoxes().get(b));
 				map.getBoxes().remove(b);
 				isSolved = isSolved || findBoxToGoalPaths(orderedBoxes, map, paths); 
 				if (isSolved) break;
 			}
 		return isSolved;
 		}
 		
 		return false;
 	}
 
 	/**
 	* Finds a box-to-goal path for each box.
 	*
 	* Populates a String array with box-to-goal paths and returns true if a valid
 	* solution is found for all boxes.
 	*
 	* Result is only guaranteed to be accurate if the strings are accessed in the 
 	* order in which they are stored in the String array.
 	*
 	* @author Alden Coots <ialden.coots@gmail.com>
 	* @param map Should be a clone, as it is altered
 	* @param paths String array where box-to-goal paths are stored
 	* @param boxIndx index of initial box in map's box array (should be 0 initially)
 	* @throws CloneNotSupportedException 
 	*/
 	private boolean findSequentialBoxToGoalPaths(Map map, String[] paths, int boxIndx) throws CloneNotSupportedException, PathNotFoundException, IOException {
 		
 		if (map.getBoxes().isEmpty()) return true;
 		else {
 			boolean isSolved = false;
 				for (int g = 0; g<map.getNumberOfGoals(); g++) {
 					//System.out.println("G : " + g);
 					if (map.getCellFromPosition(map.getGoals().get(g)).getType() != Cell.ECell.BOX_ON_GOAL) {
 						if (pathExists(map, paths, boxIndx, g, Cell.ECell.BOX)) {
 							Map newMap = map.clone();
                                                          
 							//updateMapWithBoxOnGoal(newMap, g);
 
                                                         
                                                         // -------------------------
 
                                                                                                                 // -------------------------
 
                                                         // These three lines are making problems
                                                         // I Rolled back the code and it seems to work.
                                                         // -------------------------
                                                         
                                                         
                                                         newMap.putBoxOnGoal(newMap.getBoxes().get(0), newMap.getGoals().get(g), paths[boxIndx]);
                                                         newMap.getGoals().remove(g);
                                                         newMap.getBoxes().remove(0);
 
 
 
 
 							isSolved = isSolved || findSequentialBoxToGoalPaths(newMap, paths, ++boxIndx);
 							if (isSolved) break;
 						}
 					}
 				}
 				return isSolved;
 			}
 	
 		}
         
         private void updateMapWithBoxOnGoal(Map map, int goalIndx) {
 			map.set(Cell.ECell.WALL, map.getGoals().get(goalIndx));
 			map.set(Cell.ECell.EMPTY_FLOOR, map.getBoxes().get(0).getPosition());
 			map.getGoals().remove(goalIndx);
 			map.getBoxes().remove(0);
 	}
 
 	/**
 	* Finds a player path for the given box path.
 	*
 	*
 	* @author Joakim Andrén <joaandr@kth.se>
 	* @param Startmap map Should be a clone, as it is altered
 	* @param Boxpath String with the given box path
 	* @param BoxPos Position, initial position of the box
 	* @throws CloneNotSupportedException 
 	 * @throws IOException 
 	*/
 
 	//Converts a box path to the required player path.
 	public String findPlayerPathFromBoxPath(String BoxPath, Map StartMap, Position PlayerPos, Position BoxPos) throws CloneNotSupportedException, IOException{
 		String PlayerPath=new String();
 		char lastdir=' ';
 		Position newPlayerPos=new Position();
 		Position initialPositionPlayer = PlayerPos;
 
 
 		for(int i=0;i<BoxPath.length();i++){                    
 			//System.in.read();
 			//System.out.println(StartMap);
 			char newdir=BoxPath.charAt(i);
 			if(lastdir==newdir){ //If the box path follows the same direction, just move the player one additional step in that direction.
 				PlayerPath=PlayerPath+newdir;
 
 				if(newdir=='U'){PlayerPos.up(StartMap);}
 				if(newdir=='D'){PlayerPos.down(StartMap);}
 				if(newdir=='L'){PlayerPos.left(StartMap);}
 				if(newdir=='R'){PlayerPos.right(StartMap);}
 			}	
 			else{   //Else find a path for the player to the correct side of the box.
 				newPlayerPos=BoxPos.clone();
 				if(newdir=='U'){newPlayerPos.down(StartMap);}
 				if(newdir=='D'){newPlayerPos.up(StartMap);}
 				if(newdir=='L'){newPlayerPos.right(StartMap);}
 				if(newdir=='R'){newPlayerPos.left(StartMap);}
 				//System.out.println(StartMap);
 				try {
 					PlayerPath=PlayerPath+astar.findPath(StartMap,PlayerPos,newPlayerPos, ECell.PLAYER).toLowerCase(); 
 				} catch (PathNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				PlayerPos=newPlayerPos.clone();
 				PlayerPath=PlayerPath+newdir;
 				if(newdir=='U'){PlayerPos.up(StartMap);}
 				if(newdir=='D'){PlayerPos.down(StartMap);}
 				if(newdir=='L'){PlayerPos.left(StartMap);}
 				if(newdir=='R'){PlayerPos.right(StartMap);}
 				//System.out.println(StartMap);
 			}
 
 			StartMap.set(Cell.ECell.EMPTY_FLOOR,BoxPos);
 			if(newdir=='U'){BoxPos.up(StartMap);}
 			if(newdir=='D'){BoxPos.down(StartMap);}
 			if(newdir=='L'){BoxPos.left(StartMap);}
 			if(newdir=='R'){BoxPos.right(StartMap);}
 			StartMap.set(Cell.ECell.BOX,BoxPos);
 			//System.out.println(StartMap);
 			lastdir=newdir;
 		}
 		
 		StartMap.setPlayerPosition(PlayerPos);
 		StartMap.set(ECell.EMPTY_FLOOR, initialPositionPlayer);
 		StartMap.set(Cell.ECell.BOX_ON_GOAL,BoxPos);
 		StartMap.set(ECell.PLAYER, PlayerPos);
 
 
 		//System.out.println(StartMap);
 
 
 		return PlayerPath;	
 	}
 
 
 
 
 	/**
 	* Encapsulates findPath() in a boolean function and stores its result in paths[boxIndx].
 	* @throws CloneNotSupportedException 
 	*
 	*
 	*/
 	private boolean pathExists(Map m, String[] paths, int boxIndx, int g, Cell.ECell cellType) throws CloneNotSupportedException, PathNotFoundException, IOException {
 		try {
 			
 			//System.out.println(m.getBoxes());
 
 			//System.out.println("path:" + paths[boxIndx]);
 
 			//System.out.println(" Box Index : " +boxIndx);
 			paths[boxIndx] = astar.findPath(m, m.getBoxes().get(0).getPosition(), m.getGoals().get(g),cellType);
 
 			
 			//if (cellType == ECell.BOX)
                         //    paths[boxIndx] = paths[boxIndx].substring(0, paths[boxIndx].length()-1);
                         //System.out.println("path:" + paths[boxIndx]);
 			return true;
 		} catch (PathNotFoundException e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * Clean the agent variables
 	 * 
 	 * @author arthur
 	 */
 	protected void clean()
 	{
 		map = null;
 		moves = new Moves();
 		nodes = new LinkedList<Node>();
 		astar = new AStarSearch();
 	}
 
 	public Map getMap() {
 		return map;
 	}
 
 	public void setMap(Map map) {
 		this.map = map;
 	}
 	
 	public void setMap(ArrayList<String> sList) {
 		this.map = new Map(sList);
 	}
 	
 	public void setMap(BufferedReader br) {
 		this.map = new Map(br);
 	}
 
 	public Moves getMoves() {
 		return moves;
 	}
 
 	public void setMoves(Moves moves) {
 		this.moves = moves;
 	}
         /*
          * @author: Luis
          * Prints the moves we get for an answer.
          */
         public void SolveBoardMoves(String moves, Map map) throws IOException, CloneNotSupportedException
         {            
             Position start_position = map.getPlayerPosition();
             
             System.out.println(map);
             System.in.read();
             
             // Separate the box and player moves.
             for(char a: moves.toCharArray())
             {
                 System.out.print(a);
                 // Only Player moves in Upper case
                     switch (a)
                     {
                         case 'u':   map.set(ECell.EMPTY_FLOOR, start_position);
                                     map.setPlayerPosition(start_position.up(map));
                                     map.set(ECell.PLAYER, start_position);
                                     break;
                         case 'd':   map.set(ECell.EMPTY_FLOOR, start_position);
                                     map.setPlayerPosition(start_position.down(map));
                                     map.set(ECell.PLAYER, start_position);
                                     break;
                         case 'l':   map.set(ECell.EMPTY_FLOOR, start_position);
                                     map.setPlayerPosition(start_position.left(map));
                                     map.set(ECell.PLAYER, start_position);
                                     break;
                         case 'r':   map.set(ECell.EMPTY_FLOOR, start_position);
                                     map.setPlayerPosition(start_position.right(map));
                                     map.set(ECell.PLAYER, start_position);
                                     break;
                         case 'U':   map.set(ECell.EMPTY_FLOOR, start_position);                                    
                                     map.setPlayerPosition(start_position.up(map));
                                     map.set(Cell.ECell.BOX,start_position.clone().up(map));
                                     map.set(ECell.PLAYER, start_position);
                                     break;
                         case 'D':   map.set(ECell.EMPTY_FLOOR, start_position);                                    
                                     map.setPlayerPosition(start_position.down(map));
                                     map.set(Cell.ECell.BOX,start_position.clone().down(map));
                                     map.set(ECell.PLAYER, start_position);
                                     break;
                         case 'L':   map.set(ECell.EMPTY_FLOOR, start_position);                                    
                                     map.setPlayerPosition(start_position.left(map));
                                     map.set(Cell.ECell.BOX,start_position.clone().left(map));
                                     map.set(ECell.PLAYER, start_position);
                                     break;
                         case 'R':   map.set(ECell.EMPTY_FLOOR, start_position);                                    
                                     map.setPlayerPosition(start_position.right(map));
                                     map.set(Cell.ECell.BOX,start_position.clone().right(map));
                                     map.set(ECell.PLAYER, start_position);
                                     break;
                     
                     }                    
                     System.out.println(map);
                     System.in.read();
             }
         
         
         }
         
         /**
          * 
          * Solve a given Map by returning the right string sequence
          * 
          * @author arthur
          * @param map
          * @return
          * @throws CloneNotSupportedException
          * @throws IOException
          */
         public String solve(Map map) throws CloneNotSupportedException, IOException, PathNotFoundException
         {
 		int i = 0;
 		String result = "";
                 Map init = map.clone();
 		for(String s : getBoxToGoalPaths(map))
 		{
 			//System.out.println(map);
                         String r =findPlayerPathFromBoxPath(s, map, map.getPlayerPosition(), map.getBoxes().get(i).getPosition());
                         System.out.println (r);
 			result +=r; 
 			i++;
 		}
 
                 SolveBoardMoves(result,init);
 		//System.out.println(result.toUpperCase());
 		return result.toUpperCase();
 	}
 
 		public AStarSearch getAstar() {
 			return astar;
 		}
 
 		public void setAstar(AStarSearch astar) {
 			this.astar = astar;
 		}
 
 	
 	
 	
 }
