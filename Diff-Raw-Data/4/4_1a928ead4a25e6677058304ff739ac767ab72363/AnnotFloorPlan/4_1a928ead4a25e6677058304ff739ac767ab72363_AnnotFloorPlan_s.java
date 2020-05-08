 /**
  * Floor Plan Marker Project
  * Copyright (C) 2013  Vy Thuy Nguyen
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  * 
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
  * Boston, MA  02110-1301, USA.
  */
 
 package entity;
 
 import java.io.Serializable;
 import java.util.*;
 import javax.persistence.*;
 import org.jgrapht.alg.DijkstraShortestPath;
 import org.jgrapht.graph.SimpleWeightedGraph;
 import util.DatabaseService;
 
 /**
  * Bug: when open an old file, new cells are created instead, if a cell is marked dead, 
  * a this one will be persisted as new cell. Similarly, if a cell is enabled,
  * it may visually appear that the cell is not dead, but in fact, the dead cell
  * is not removed.
  * 
  * @author              Vy Thuy Nguyen
  * @version             1.0 Jan 16, 2013
  * Last modified:       
  */
 @Entity
 public class AnnotFloorPlan implements Serializable 
 {
     private static final long serialVersionUID = 1L;
     
     @Id
     @GeneratedValue (strategy = GenerationType.AUTO)
     private long id;
     
     @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
     private FloorPlan floorPlan;
     
     @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.PERSIST}, 
                           fetch = FetchType.EAGER, mappedBy = "annotFloorPlan")
     private Set<DeadPoint> deadPoints;
     
     
     /**
      * Each cell accounts for ratio% of the width and the height
      */
     int ratio;
     
     /**
      * Width of the floor plan in real life
      */
     int actualW;
     
     /**
      * Height of the floor plan in real life
      */
     int actualH;
     
     /**
      * Width of each cell
      */
     int unitW;
     
     /**
      * Height of each cell
      */
     int unitH;
     
     /**
      * Number of rows of the matrix
      */
     int rowCount;
     
     /**
      * Number columns of the matrix
      */
     int colCount;
     
     @Transient
     private boolean graphInitialized = false;
     
     @Transient
     private SimpleWeightedGraph<Cell, WeightedEdge> g;
     
     @Transient
     private Cell[][] cellContainer;
         
     @Transient
     private EntityManager em;
     
     @Transient
     private PriorityQueue<SimpleWeightedGraph<Cell, WeightedEdge>> subRegions;
     
     @SuppressWarnings({"unchecked", "unchecked"})
     public AnnotFloorPlan()
     {
         
         em = DatabaseService.getEntityManager();
         floorPlan = new FloorPlan();
         deadPoints = new HashSet<DeadPoint>();
         g = new SimpleWeightedGraph<Cell, WeightedEdge>(WeightedEdge.class);
     }
     
     public AnnotFloorPlan(FloorPlan fp, int ratio, int actualW, int actualH)
     {
         em = DatabaseService.getEntityManager();
         floorPlan = fp;        
         deadPoints = new HashSet<DeadPoint>();
         this.ratio = ratio;
         this.actualW = actualW;
         this.actualH = actualH;
         
         //Width and height of each cell
 //        unitW = fp.getWidth() * ratio / 100;
 //        unitH = fp.getHeight() * ratio / 100;
         
         //Calc the number of rows and columns needed
 //        rowCount = fp.getHeight() / unitH + 1;
 //        colCount = fp.getWidth() / unitW + 1;
 
         rowCount = 50;
         colCount = 75;
         unitW = (int)Math.ceil(fp.getWidth() / colCount);
         unitH = (int)Math.ceil(fp.getHeight() / rowCount);
         cellContainer = new Cell[rowCount][colCount];
         
         //init the graph
         g = new SimpleWeightedGraph<Cell, WeightedEdge>(WeightedEdge.class);
         
         initGraph();
     }
      
     public void setSubRegions(PriorityQueue<SimpleWeightedGraph<Cell, WeightedEdge>> subs)
     {
         subRegions = subs;
     }
     
     public FloorPlan getFloorPlan()
     {
         return this.floorPlan;
     }
     
     public int getUnitW()
     {
         return unitW;
     }
     
     public int getUnitH()
     {
         return unitH;
     }
     
     public int getRatio()
     {
         return ratio;
     }
     
     public SimpleWeightedGraph<Cell, WeightedEdge> getGraph()
     {
         return g;
     }
     
 //    public boolean needInitGraph()
 //    {
 //        return !graphInitialized;
 //    }
 //    
     public void initGraph()
     {
         generateVertices();
         generateEdges();
     }
     
     /**
      * 
      * @param x1 the actual "from" x coordinate (in respect to the "dense grid")
      * @param y1 the actual "from" y coordinate (ditto)
      * @param x2 the actual "to" x coordinate (ditto)
      * @param y2 the actual "to" y coordinate (ditto)
      * @return the list of edges representing the shortest path from P1 to P2
      */
     public List<WeightedEdge> getShortestPath(int x1, int y1, int x2, int y2)
     {
         //int x1 = 158, y1 = 109, x2 = 108, y2 = 489;
         int row1 = y1 / unitH, col1 = x1 / unitW, row2 = y2 / unitH, col2 = x2 / unitW;
         DijkstraShortestPath d = new DijkstraShortestPath(g, cellContainer[row1][col1], cellContainer[row2][col2]);
         return d.getPathEdgeList();
     }
     
     /**
      * Generate vertices for the graph representing the floor plan
      */
     private void generateVertices()
     {
         for (int row = 0; row < rowCount; ++row)
         {
             for (int col = 0; col < colCount; ++col)
             {                
                 cellContainer[row][col] = new Cell(row, col, this);
                 g.addVertex(cellContainer[row][col]);
             }
         }
     }
     
     /**
      * Generate edges for the graph representing the floor plan
      */
     private void generateEdges()
     {
         for (int row = 0; row < rowCount; ++row)
             for (int col = 0; col < colCount; ++col)
             {
                 addEdges(row, col);
             }
     }
      
     /**
      * Insert a vertex to the graph whose row and col coordinates are as given.
      * @param row
      * @param col 
      */
     private void addVertexAt(int row, int col)
     {
         g.addVertex(cellContainer[row][col]);
         addEdges(row, col);
     }
     
     /**
      * Add edges connecting this cell and its 8 (or less) neighbors
      * 
      * @param row
      * @param col 
      */
     private void addEdges(int row, int col)
     {
         System.out.println("Adding (" + row + ", " + col + ")");
         //Add weighted edge
         //North
         if (row > 0 && g.containsVertex(cellContainer[row][col]))
         {
             g.addEdge(cellContainer[row][col], cellContainer[row - 1][col]);
             g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                       cellContainer[row - 1][col]),
                             1);
             //NE
             if (col < colCount - 1 && g.containsVertex(cellContainer[row - 1][col + 1]))
             {
                 g.addEdge(cellContainer[row][col], cellContainer[row - 1][col + 1]);
                 g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                           cellContainer[row - 1][col + 1]),
                                 Math.sqrt(2.0));
             }
         }
 
         //East
         if (col < colCount - 1 && g.containsVertex(cellContainer[row][col + 1]))
         {
             g.addEdge(cellContainer[row][col], cellContainer[row][col + 1]);
             g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                       cellContainer[row][col + 1]),
                             1);
 
 
             //SE
             if (row < rowCount - 1 && g.containsVertex(cellContainer[row + 1][col + 1]))
             {
                 g.addEdge(cellContainer[row][col], cellContainer[row + 1][col + 1]);
                 g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                           cellContainer[row + 1][col + 1]),
                                 Math.sqrt(2.0));
 
             }
         }
 
         //South
         if (row < rowCount - 1 && g.containsVertex(cellContainer[row + 1][col]))
         {
             g.addEdge(cellContainer[row][col], cellContainer[row + 1][col]);
             g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                       cellContainer[row + 1][col]),
                             1);
 
             //SW
             if (col > 0 && g.containsVertex(cellContainer[row + 1][col - 1]))
             {
                 g.addEdge(cellContainer[row][col], cellContainer[row + 1][col - 1]);
                 g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                           cellContainer[row + 1][col - 1]),
                                 Math.sqrt(2.0));
             }
         }
 
         //West
         if (col > 0 && g.containsVertex(cellContainer[row][col - 1]))
         {
             g.addEdge(cellContainer[row][col], cellContainer[row][col - 1]);
             g.setEdgeWeight(g.getEdge(cellContainer[row][col], cellContainer[row][col - 1]),
                             1);
 
             //NW
             if (row > 0 && g.containsVertex(cellContainer[row - 1][col - 1]))
             {
                 g.addEdge(cellContainer[row][col], cellContainer[row - 1][col - 1]);
                 g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                           cellContainer[row - 1][col - 1]),
                                 Math.sqrt(2.0));
             }
         }
     }
     
     /**
      * Disable the cell containing the given point
      * @param x
      * @param y 
      */
     public void disableCell(int x, int y)
     {
         int col = getCorrespondingCol(x);
         int row = getCorrespondingRow(y);
         
         //If a cell is already dead, it wouldn't be disabled again; Do this to reduce the number of dead points created
         if (valid(row, col) && !cellContainer[row][col].isDead())
         {
             cellContainer[row][col].disableCell();
             deadPoints.add(new DeadPoint(x, y, this));
 
             //removing this cell from the graph and all of its touching edges
             g.removeVertex(cellContainer[row][col]);
         }
     }
     
     /**
      * Enable the cell containing the given point
      * @param x
      * @param y 
      */
     public void enabbleCell(int x, int y)
     {
         int col = getCorrespondingCol(x);
         int row = getCorrespondingRow(y);
         if (valid(row, col))
         {
             cellContainer[row][col].enabbleCell();
             
             //Remove the whole cell
             //Even if user doesn't click the exact position as that when the cell is disabled,
             //as long as it's in the same cell. The cell should be enabled again.
             ArrayList<DeadPoint> temps = new ArrayList<DeadPoint>();
             for (DeadPoint dp : deadPoints)
             {
                 if (isInCell(row, col, dp))
                     temps.add(dp);
             }
             
             for (DeadPoint dp : temps)
                 deadPoints.remove(dp);
             
             em.getTransaction().begin();
             em.remove(cellContainer[row][col]);
             em.getTransaction().commit();
             //adding this cell to the graph and connecting it with its neighbors
             addVertexAt(row, col);
         }
     }
 
     /**
      * 
      * @return an unmodifiable list of cells containing dead points (aka dead cells)
      */
     public List<Cell> getDeadCells()
     {
         ArrayList<Cell> deadCells = new ArrayList<Cell>();
         int row, col;
         for (DeadPoint dp : deadPoints)
         {
             row = getCorrespondingRow(dp.getY());
             col = getCorrespondingCol(dp.getX());
             if (valid(row, col))
                 deadCells.add(cellContainer[row][col]);
         }
         return Collections.unmodifiableList(deadCells); 
     }
     
     public Set<DeadPoint> getDeadPoints()
     {
         return this.deadPoints;
     }
     
     /**
      * Remove all edges connecting dead cells with others
      * Call this after loading this entity from db
      * OR after config is changed. (i.e., after ratio or actualW or actualH is changed)
      */
     public void updateGraph()
     {
         cellContainer = new Cell[rowCount][colCount];
         initGraph();
         int row, col;
         for (DeadPoint dp : deadPoints)
         {            
             col = getCorrespondingCol(dp.getX());
             row = getCorrespondingRow(dp.getY());
             if (valid(row, col))
                 g.removeVertex(cellContainer[row][col]);
         }
     }
     
     public int getCorrespondingCol(DeadPoint dp)
     {
         return dp.getX() / unitW;
     }
     
     public int getCorrespondingCol(int x)
     {
         return x / unitW;
     }
     
     public int getCorrespondingRow(DeadPoint dp)
     {
         return dp.getY() / unitH;
     }
     
     public int getCorrespondingRow(int y)
     {
         return y / unitH;
     }
         
     public int getRowCount()
     {
         return this.rowCount;
     }
     
     public int getColCount()
     {
         return this.colCount;
     }
     
     public Cell[][] getCellContainer()
     {
         return this.cellContainer;
     }
     
     /**
      * 
      * @return the largest connected component of the graph.
      */
     public ArrayList<Cell> getLargestConnectedComponent()
     {
         ArrayList<ArrayList<Cell>> components = new ArrayList<>();
         boolean visited[][] = new boolean[rowCount][colCount];
         for (int row = 0; row < rowCount; ++row)
             for (int col = 0; col < colCount; ++col)
                 visited[row][col] = false;
         
         Queue<Cell> q; 
         Cell t = null, u = null;
         for (Cell c : g.vertexSet())
         {
             if (!visited[c.getRow()][c.getCol()])
             {
                 q = new LinkedList<Cell>();
                 ArrayList<Cell> component = new ArrayList<>();
                 visited[c.getRow()][c.getCol()] = true;
                 
                 //Find all connected nodes
                 q.add(c);
                 component.add(c);
                 while (!q.isEmpty())
                 {
                     t = q.remove();
                     for (WeightedEdge e : g.edgesOf(t))
                     {
                         u = t.equals(g.getEdgeSource(e)) ?
                                 g.getEdgeTarget(e) :
                                 g.getEdgeSource(e);
                         if (!visited[u.getRow()][u.getCol()])
                         {
                             visited[u.getRow()][u.getCol()] = true;
                             q.add(u);
                             component.add(u);
                         }
                     }
                 }
                 
                 components.add(component);
             } 
         }
         
         int largestSize = 0, largestIndex = 0;
         for (int i = 0; i < components.size(); ++i)
         {
             if (components.get(i).size() > largestSize)
             {
                 largestSize = components.get(i).size();
                 largestIndex = i;
             }
         }
 
         filterGraph(components.get(largestIndex));
         return components.get(largestIndex);
     }
     
     /**
      * Remove all nodes which are NOT in cells from graph g
      * @param cells 
      */
     private void filterGraph(ArrayList<Cell> cells)
     {
         for (int row = 0; row < rowCount; ++row)
             for (int col = 0; col < colCount; ++col)
                 if (!cells.contains(cellContainer[row][col]))
                     g.removeVertex(cellContainer[row][col]);
     }
     
     public SimpleWeightedGraph<Cell, WeightedEdge> getLargestConnectedComponentAsGraph()
     {
         filterGraph(getLargestConnectedComponent());
         return g;
     }
 
     /**
      * 
      * @param row Row of the cell
      * @param col Column of the cell
      * @return true if row and col are within the range
      */
     private boolean valid(int row, int col)
     {
         return row >= 0 && row < rowCount && col >= 0 && col < colCount;
     }
     
     /**
      * The coordinate is the top left corner
      * @param xm x position in meters
      * @param ym y position in meters
      * @return [0] contains row; [1] contains col
      */
     public int[] getNodePosition(double xm, double ym)
     {
         int pxW = floorPlan.getWidth(), pxH = floorPlan.getHeight(),
             pxX, pxY;
         double widthRatio = pxW / actualW, heightRatio = pxH / actualH;
         pxX = (int) Math.round(xm * widthRatio);
         pxY = (int) Math.round(ym * heightRatio);
         
         int pos[] = new int[2];
         pos[0] = pxY / unitH;
         pos[1] = pxX / unitW;
         return pos;
     }
     
     public int getActualW()
     {
         return actualW;
     }
     
     public int getActualH()
     {
         return actualH;
     }
     
     /**
      * 
      * @param xm x coordinate in real life
      * @param ym
      * @return 
      */
     public Cell getNode(double xm, double ym)
     {
         int[] pos = getNodePosition(xm, ym);
         if (valid(pos[0], pos[1]))
             return this.cellContainer[pos[0]][pos[1]];
         else
         {
             System.out.printf("Invalid pos, row = %d, col = %d, xm = %f, ym = %f\n",
                               pos[0],
                               pos[1],
                               xm,
                               ym);
             return null;
         }
     }
 
     public void updateConfig(int ratio, int actualW, int actualH)
     {   
         this.ratio = ratio;
         this.actualH = actualH;
         this.actualW = actualW;
         
         //Re-generate graph
         //Width and height of each cell
         unitW = floorPlan.getWidth() * ratio / 100;
         unitH = floorPlan.getHeight() * ratio / 100;
         
         //Calc the number of rows and columns needed
         rowCount = floorPlan.getHeight() / unitH + 1;
         colCount = floorPlan.getWidth() / unitW + 1;
         
         //init the graph
         g = new SimpleWeightedGraph<Cell, WeightedEdge>(WeightedEdge.class);
         
         updateGraph();
     }
 
     /**
      * 
      * @param row
      * @param col
      * @param dp
      * @return true if the given dead point is in the cell at specified location
      */
     private boolean isInCell(int row, int col, DeadPoint dp)
     {
         return row == getCorrespondingRow(dp.getY()) 
                && col == getCorrespondingCol(dp.getX());
     }
     
     /**
      * 
      * @param row
      * @param col
      * @param x
      * @param y
      * @return true if the position (x, y) (in pixel) is in the cell at given location
      */
     public boolean isInCell(int row, int col, int x, int y)
     {
         return row == getCorrespondingRow(y) && col == getCorrespondingCol(x);
     }
     
     
     /**
      * 
      * @param rowCentroid the row coordinate of the centroid
      * @param colCentroid the col coordinate of the centroid
      * @return array containing [x-coor in meter][y-coor in meter]
      */
     public double[] getCentroiCoordinates(double rowCentroid, double colCentroid)
     {
         double[] coordinates = new double[2];
        coordinates[0] = colCentroid * (colCount - 1) / actualW;
        coordinates[1] = rowCentroid * (rowCount - 1) / actualH;
         return coordinates;
     }
 }
