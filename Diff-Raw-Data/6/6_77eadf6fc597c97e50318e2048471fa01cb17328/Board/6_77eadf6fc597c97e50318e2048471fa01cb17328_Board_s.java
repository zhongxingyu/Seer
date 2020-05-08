 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Board {
 	private ArrayList<BoardCell> cells = new ArrayList<BoardCell>();
 	private Map<Character,String> rooms= new HashMap<Character, String>();
 	private int numRows;
 	private int numColumns;
 	
 	public void loadConfigFiles(){
 
 	}
 	public int calcIndex(int row, int column){
 		return 0;
 	}
 	
 	
 	
 	public RoomCell GetRoomCellAt(int row, int column){
		return null;
		//return cells.contains(BoardCell(row,column)); was unable to return a RoomCell
	}
 	
 	
 	
 	public ArrayList<BoardCell> getCells() {
 		return cells;
 	}
 	public void setCells(ArrayList<BoardCell> cells) {
 		this.cells = cells;
 	}
 	public Map<Character, String> getRooms() {
 		return rooms;
 	}
 	public void setRooms(Map<Character, String> rooms) {
 		this.rooms = rooms;
 	}
 	public int getNumRows() {
 		return numRows;
 	}
 	public void setNumRows(int numRows) {
 		this.numRows = numRows;
 	}
 	public int getNumColumns() {
 		return numColumns;
 	}
 	public void setNumColumns(int numColumns) {
 		this.numColumns = numColumns;
 	}
 
 }
 
 
