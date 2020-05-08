 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class Grid {
     private GridSquare[][]  gridSquares;
     private GridSummary[][] gridSummaries;
     private Location[] locations;
 
     public Grid(int gridSize) {
         gridSquares = new GridSquare[gridSize][gridSize];
         locations = new Location[gridSize*gridSize];
         for (int i = 0; i < gridSize; i++) {
             for (int j = 0; j < gridSize; j++) {
                 gridSquares[i][j] = new GridSquare(new Location(i, j));
                 locations[i*gridSize+j] = new Location(i, j);
             }
         }
     }
 
     public GridSquare get(int row, int col) {
         return gridSquares[row][col];
     }
 
     public GridSquare get(Location loc) {
         return gridSquares[loc.row][loc.col];
     }
 
     public void addAnimal(Animal animal, GridSquare gridSquare){
         if (animal == null) return;
         gridSquare.setAnimal(animal);
     }
     public void addAnimal(Animal animal, int row, int col) {
         addAnimal(animal, gridSquares[row][col]);
     }
     public void addAnimal(Animal animal, Location loc) {
         addAnimal(animal, loc.row, loc.col);
     }
 
     public void addPlant(Plant plant, GridSquare gridSquare){
         if (plant == null) return;
         gridSquare.setPlant(plant);
     }
     public void addPlant(Plant plant, int row, int col) {
         addPlant(plant, gridSquares[row][col]);
     }
     public void addPlant(Plant plant, Location loc) {
         addPlant(plant, loc.row, loc.col);
     }
 
     public void removeAnimal(GridSquare gridSquare) {
         Animal currentAnimal = gridSquare.getAnimal();
         if(currentAnimal != null) currentAnimal.setLocation(null);
         gridSquare.setAnimal(null);
     }
     public void removeAnimal(int row, int col) {
         removeAnimal(gridSquares[row][col]);
     }
     public void removeAnimal(Location loc) {
         removeAnimal(loc.row, loc.col);
     }
 
     private boolean inBounds(int row, int col) {
         return row >= 0 && row < getGridSize()
             && col >= 0 && col < getGridSize();
     }
 
     private int distance(int x, int y, int i, int j) {
         return Math.abs(x - i) + Math.abs(y - j);
     }
 
     private int distance(Location loc1, Location loc2) {
         return distance(loc1.row, loc1.col, loc2.row, loc2.col);
     }
 
     @SuppressWarnings("unchecked")
     public List<DistanceSquarePair> getAdjacentSquares(Location loc, int dist) {
         ArrayList<DistanceSquarePair> ret = new ArrayList<DistanceSquarePair>();
 
         int row = loc.row;
         int col = loc.col;
 
         for (int i = row - dist; i < row + dist; i++) {
             for (int j = col - dist; j < col + dist; j++) {
                 int d = distance(row, col, i, j);
                 if (inBounds(i, j) && d <= dist && d != 0) {
                     ret.add(new DistanceSquarePair(d, get(i, j)));
                 }
             }
         }
         Collections.sort(ret);
         return ret;
     }
 
     public List<DistanceSquarePair> getEmptySquares(Location loc, int distance) {
         List<DistanceSquarePair> squares = getAdjacentSquares(loc, distance);
         return getEmptySquares(squares);
     }
     public List<DistanceSquarePair> getEmptySquares(int row, int col, int distance) {
         List<DistanceSquarePair> squares = getAdjacentSquares(new Location(row, col), distance);
         return getEmptySquares(squares);
     }
     public List<DistanceSquarePair> getEmptySquares(List<DistanceSquarePair> squares) {
         ArrayList<DistanceSquarePair> ret  = new ArrayList<DistanceSquarePair>();
 
         for (DistanceSquarePair s : squares) {
             if (s.gridSquare.getAnimal() == null) ret.add(s);
         }
 
         return ret;
     }
 
     public List<DistanceSquarePair> getOccupiedSquares(int row, int col, int distance) {
         List<DistanceSquarePair> squares = getAdjacentSquares(new Location(row, col), distance);
         return getOccupiedSquares(squares);
     }
     public List<DistanceSquarePair> getOccupiedSquares(List<DistanceSquarePair> squares) {
         ArrayList<DistanceSquarePair> ret  = new ArrayList<DistanceSquarePair>();
 
         for (DistanceSquarePair s : squares) {
             if (s.gridSquare.getAnimal() != null) ret.add(s);
         }
 
         return ret;
     }
 
     public List<DistanceSquarePair> getOrganismSquares(List<DistanceSquarePair> squares, List<String> organismNames) {
         ArrayList<DistanceSquarePair> ret = new ArrayList<DistanceSquarePair>();
         boolean correctSquare;
         for (DistanceSquarePair s : squares) {
             correctSquare = false;
             if (s.gridSquare.getAnimal() != null) {
                 String name = s.gridSquare.getAnimal().getClass().getName();
                 if (organismNames.contains(name)) {
                     correctSquare = true;
                 }
             }
             if (!correctSquare && s.gridSquare.getPlant() != null && s.gridSquare.getPlant().isAlive()) {
                 String name = s.gridSquare.getPlant().getClass().getName();
                 if (organismNames.contains(name)) {
                     correctSquare = true;
                 }
             }
             
             if(correctSquare){
                 ret.add(s);
             }
         }
 
         return ret;
     }
 
     public GridSquare getOptimalMoveSquare(Location start, Location target, int distance, boolean closest) {
         int bestDist;
         GridSquare bestSquare = null;
 
         if (closest) bestDist = Integer.MAX_VALUE;
         else bestDist = -1;
 
         List<DistanceSquarePair> emptySquares = getEmptySquares(start, distance);
         for (DistanceSquarePair pair : emptySquares) {
             int dist = distance(pair.gridSquare.getLocation(), target);
             if (closest) {
                if (dist < bestDist && pair.gridSquare.getAnimal() == null) {
                     bestDist = dist;
                     bestSquare = pair.gridSquare;
                 }
             } else {
                if (dist > bestDist && pair.gridSquare.getAnimal() == null) {
                     bestDist = dist;
                     bestSquare = pair.gridSquare;
                 }
             }
         }
 
         return bestSquare;
     }
 
     public GridSquare[][]  getGridSquares()   { return gridSquares;   }
     public GridSummary[][] getGridSummaries() { return gridSummaries; }
     public int getGridSize() { return gridSquares.length; }
     public Location[] getLocations() { return locations; }
 
     public String toString() {
         String terrain = "";
         String animals = "";
 
         for (int i = 0; i < getGridSize(); i++) {
             for (GridSquare s : gridSquares[i]) {
                 if (s.getPlant() != null) {
                     terrain += Simulation.classnameToSymbol(s.getPlant().getClass().getName());
                 } else {
                     terrain += "_";
                 }
 
                 if (s.getAnimal() != null) {
                     animals += Simulation.classnameToSymbol(s.getAnimal().getClass().getName());
                 } else {
                     animals += "_";
                 }
             }
             terrain += "\n";
             animals += "\n";
         }
 
         return terrain + "\n" + animals;
     }
 }
