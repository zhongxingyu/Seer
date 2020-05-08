 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 
 public class GameBoard {
 	StaticBoard board;
 	ArrayList<Point> boxes;
 	Point player;
 	
 	GameBoard startBoard;
 	GameBoard endBoard;
 	
 	public GameBoard(String text) {
 		// parse string and create board
 		// copy boxes and player into startBoard, mutate stuff and make endBoard
 		// let startBoard and endBoard point to the same StaticBoard as this instance.
 	}
 	
 	// checking if player can walk backwards i.e there is a empty square behind player in any direction
 	//return is an array of all possible direction one can walk
 	public ArrayList<MapNode> canWalk(){
 		ArrayList<MapNode> walkablepos= new ArrayList<MapNode>();
 		if(map[playerPos.x-1][playerPos.y].data==' '||map[playerPos.x-1][playerPos.y].data=='.'){
 			walkablepos.add(map[playerPos.x-1][playerPos.y]);
 		}
 		if(map[playerPos.x+1][playerPos.y].data==' '||map[playerPos.x+1][playerPos.y].data=='.'){
 			walkablepos.add(map[playerPos.x-1][playerPos.y]);
 
 		}
 		if(map[playerPos.x][playerPos.y-1].data==' '||map[playerPos.x][playerPos.y-1].data=='.'){
 			walkablepos.add(map[playerPos.x-1][playerPos.y]);
 
 		}
 		if(map[playerPos.x][playerPos.y+1].data==' '||map[playerPos.x][playerPos.y+1].data=='.'){
 			walkablepos.add(map[playerPos.x-1][playerPos.y]);
 
 		}
 		return walkablepos;
 	}
 	
 	
 	// walks "backwards" or rather walks, but if a box is opposite direction of the walk then it pulls the box
 	public void Walk(MapNode Movepos){
 
 		//Pull box if box is in the square in just infront of the player when the player moves opposite direction
 		if(playerPos.x-Movepos.x==1){
 			if(hasBox(map[playerPos.x+1][playerPos.y])){
 				pullBox(map[playerPos.x][playerPos.y],map[playerPos.x+1][playerPos.y]);
 			}else{
 				updateRoad(map[playerPos.x][playerPos.y]);
 			}
 
 		}
 		if(playerPos.x-Movepos.x==-1){
 			if(hasBox(map[playerPos.x-1][playerPos.y])){
 				pullBox(map[playerPos.x][playerPos.y],map[playerPos.x-1][playerPos.y]);
 			}else{
 				updateRoad(map[playerPos.x][playerPos.y]);
 			}
 
 		}
 		if(playerPos.y-Movepos.y==1){
 			if(hasBox(map[playerPos.x][playerPos.y+1])){
 				pullBox(map[playerPos.x][playerPos.y],map[playerPos.x][playerPos.y+1]);
 			}else{
 				updateRoad(map[playerPos.x][playerPos.y]);
 			}
 
 
 		}
 		
 	
 		if(playerPos.y-Movepos.y==-1){
 			if(hasBox(map[playerPos.x][playerPos.y-1])){
 				pullBox(map[playerPos.x][playerPos.y],map[playerPos.x][playerPos.y-1]);
 			}else{
 				updateRoad(map[playerPos.x][playerPos.y]);
 			}
 
 		}
 		// update player pos
 		setPlayerPos(new Point(Movepos.x,Movepos.y));
 
 
 
 
 	}
 	
 	// if box is in opposite direction of a player walk, then move the box to playerpos(pull)
 	public void pullBox(MapNode playerPos,MapNode BoxPos){
 		if(playerPos.data=='@'&&BoxPos.data=='$'){
 			playerPos.data=BoxPos.data;
 			BoxPos.data=' ';
 		}
 		if(playerPos.data=='@'&&BoxPos.data=='*'){
 			playerPos.data='$';
 			BoxPos.data='.';
 		}
 		if(playerPos.data=='+'&&BoxPos.data=='$'){
 			playerPos.data='*';
 			BoxPos.data=' ';
 		}
 		if(playerPos.data=='+'&&BoxPos.data=='*'){
 			playerPos.data=BoxPos.data;
 			BoxPos.data='.';
 		}
 
 	}
 	
 	
 	public void setPlayerPos(Point pPos){
 		this.playerPos=pPos;
 		if(map[pPos.x][pPos.y].data=='.'){
 			map[pPos.x][pPos.y].data='+';
 		}else{
 			map[pPos.x][pPos.y].data='@';
 		}
 
 	}
 	public boolean hasBox(MapNode m){
 		if(m.data=='$'||m.data=='*'){
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public int hashCode() {
 		/*
 		position of all the boxes
 		position of the player
 			position is not actual position, but rather
 			the position it would have if the player moved
 			to the topmost row reachable from its current
 			position, then the leftmost column from there.
 		*/
 
 		int boxes = boxHash();
 
 		// get the position the player would have if it moved top, then left
 		Point anchorPosition = getAnchorPosition(0, 0);
 
 		return boxes + anchorPosition.hashCode();
 	}
 
 	private int boxHash() {
 		return 0;
 	}
 
 	private Point getAnchorPosition(int fromX, int fromY) {
 		int anchorX = 0;
 		int anchorY = 0;
 		// find northernmost reachable position through bfs
 		// bfs()
 		// go left as far as possible
 		return new Point(anchorX, anchorY);
 	}
 	
 	private GameBoard update(Move m){
 		//use the makeMove-function in Move to change a move's direction
 		return null;
 	}
 	//returns true if we can go from start point to goal point
 	public boolean goToPoint(int fromX, int fromY, int toX, int toY){
 		int from = fromY * board.getSize().getX() + fromX;
 		int goal = toY*board.getSize().getX() + toX;
 		boolean found = false;
 		int currPos = from;
 		int nextPos;
 		ArrayList<ArrayList<Integer>> neighbour = new ArrayList<ArrayList<Integer>>();
 		Queue<Integer> queue = new LinkedList<Integer>();
         queue.add(from);
 		boolean[] visited = new boolean[board.getSize().getX()*board.getSize().getY()];
         ArrayList<Integer> adjacent;
         while(!queue.isEmpty() && !found){
         currPos = queue.poll();
            adjacent = neighbour.get(currPos);
             for(int i:adjacent){
              if(!visited[i] && !found){
               queue.add(i);                                       
                     visited[i]=true;
                     if(i == goal){
                      found=true;  
                     }
              }
             }
         }
         return false;
 	}
 }
