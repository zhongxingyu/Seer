 package riskyspace.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import riskyspace.logic.Builder;
 
 public class World {
 	private int rows = 0;
 	private int cols = 0;
 	private Map<Position, Territory> territories = null;
 	private Map<Player, PlayerStats> playerstats = null;
 
 	public World(int rows, int cols) {
 		this.rows = rows;
 		this.cols = cols;
 		initPlayers();
 		territories = Builder.generateMap(rows, cols);
 	}
 
 	public World() {
 		this(20, 20);
 	}
 
 	private void initPlayers() {
 		playerstats = new HashMap<Player, PlayerStats>();
 		playerstats.put(Player.BLUE, new PlayerStats());
 		playerstats.put(Player.RED, new PlayerStats());
 	}
 
 	@Override
 	public boolean equals(Object other) {
 		if (this == other) {
 			return true;
 		} else if (other == null || this.getClass() != other.getClass()) {
 			return false;
 		} else {
 			World otherWorld = (World) other;
 			return (rows == otherWorld.rows && cols == otherWorld.cols);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "[" + "Rows: " + rows + ", " + "Columns: " + cols + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		return rows * 17 + cols * 23;
 	}
 
 	public Map<Position, Territory> getTerritories() {
 		return territories;
 	}
 
 	public int getRows() {
 		return rows;
 	}
 	
 	public int getCols() {
 		return cols;
 	}
 }
