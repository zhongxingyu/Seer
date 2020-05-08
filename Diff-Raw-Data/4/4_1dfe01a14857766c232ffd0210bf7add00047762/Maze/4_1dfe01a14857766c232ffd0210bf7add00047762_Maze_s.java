 /**
  * @author      Jacob Leach
  * @version     1.0            
  */
 package org.sythe.suf.maze;
 
 import java.util.Random;
 import java.util.Stack;
 
 public class Maze
 {
     private final int WALL_THICKNESS = 1;
     private final int MIN_WIDTH = (WALL_THICKNESS * 2) + 1;
     private final int MIN_LENGTH = (WALL_THICKNESS * 2) + 1;
     private final int MAX_WIDTH = 100;
     private final int MAX_LENGTH = 100;
     private long seed;
     private Random random;
 
     public Maze()
     {
         this(new Random().nextLong());
     }
 
     public Maze(long seed)
     {
         this.seed = seed;
         random = new Random(seed);
     }
 
     /**
      * 
      * 
      * @param width
      *            The width of the maze including walls (must be an odd number)
      * @param length
      *            The length of the maze including walls (must be an odd number)
      * @param endX
      * @param endY
      * @return
      */
     public int[][] getMaze(int width, int length, int endX, int endY)
     {
         // Make it odd if its not
         if (width % 2 == 0)
         {
             width--;
         }
         if (length % 2 == 0)
         {
             length--;
         }
 
         // Check to see if it is a valid size
         if (width >= (MIN_WIDTH + 1) / 2 && width <= (MAX_WIDTH + 1) / 2 && length >= (MIN_LENGTH + 1) / 2 && length <= (MAX_LENGTH + 1) / 2)
         {
             // Calculate the number of nodes needed for this particular size maze
             int nodeWidth = (width - 1) / 2;
             int nodeLength = (length - 1) / 2;
 
             /*
              * Testing outputs
              * 
              * System.out.println("Width: " + width);
              * System.out.println("Length: " + length);
              * 
              * System.out.println("Node Width: " + nodeWidth);
              * System.out.println("Node Length: " + nodeLength);
              */
 
             // Make the maze
             MazeNode[][] nodes = editNodes(fillNodes(nodeWidth, nodeLength));
 
             return createWalls(editPaths(createPaths(nodes, nodes[endX][endY])));
         }
         else
         {
             return null;
         }
     }
 
     /**
      * Returns a 2 dimensional array of completely neighbored maze nodes.
      * 
      * @param width
      *            The number of actual maze nodes wide the maze should be (the maze itself will be
      *            ((2 * width) + 1) units wide with walls)
      * @param length
      *            The number of actual maze nodes long the maze should be (the maze itself will be
      *            ((2 * length) + 1) units long with walls)
      * @return Returns a 2 dimensional array of completely neighbored maze nodes. array[i][0] and
      *         array[0][i] are set to visited to allow createWalls() to create a wall around the
      *         maze.
      */
     private final MazeNode[][] fillNodes(int width, int length)
     {
         MazeNode[][] mazeNodes = new MazeNode[width][length];
 
         for (int lengthCount = 0; lengthCount < mazeNodes.length; lengthCount++)
         {
             for (int widthCount = 0; widthCount < mazeNodes[lengthCount].length; widthCount++)
             {
                 MazeNode temp = new MazeNode(widthCount, lengthCount);
                 if (widthCount != 0)
                 {
                     temp.addNeighbor(mazeNodes[widthCount - 1][lengthCount], true);
                 }
                 if (lengthCount != 0)
                 {
                     temp.addNeighbor(mazeNodes[widthCount][lengthCount - 1], true);
                 }
                 mazeNodes[widthCount][lengthCount] = temp;
             }
         }
         return mazeNodes;
     }
 
     /**
      * A method for overriding. Allows for custom seeds for mazes.
      * 
      * @param nodes
      *            The result of the fillNodes() method, passed automatically
      * @return Returns nodes after any edits made to them
      */
     private MazeNode[][] editNodes(MazeNode[][] nodes)
     {
         return nodes;
     }
 
     /**
      * A recursive method that creates the paths through the maze
      * 
      * @param nodes
      *            The result of the editNodes() method, passed automatically
      * 
      * @param start
      *            The MazeNode to start the paths at
      * @return Returns nodes after a path has been made through it (by removing neighbors)
      */
     private final MazeNode[][] createPaths(MazeNode[][] nodes, MazeNode start)
     {
         Stack<MazeNode> path = new Stack<MazeNode>();
 
         MazeNode current = start;
         current.setVisited();
 
         while (current.hasUnvisitedNeighbors())
         {
             MazeNode temp = current.getRandomUnvisitedNeighbor(random);
             path.push(temp);
 
             temp.removeNeighbor(current.getX(), current.getY());
             temp.setVisited();
 
             current = temp;
         }
         while (path.size() > 0)
         {
             MazeNode temp = path.pop();
             if (temp.hasUnvisitedNeighbors())
             {
                 nodes = createPaths(nodes, temp);
             }
         }
 
         return nodes;
     }
 
     /**
      * A method for overriding. Allows paths to be edited once they are created
      * 
      * @param nodes
      *            The result of the createPaths() method, passed automatically
      * @return Returns nodes after any edits made to them
      */
     private MazeNode[][] editPaths(MazeNode[][] nodes)
     {
         return nodes;
     }
 
     /**
      * Takes the MazeNode 2d array and converts it to a 2d int array with walls included.
      * 
      * @param nodes
      *            The result of the editPaths() method, passed automatically
      * @return Returns an 2d int array of the finished maze
      */
     private final int[][] createWalls(MazeNode[][] nodes)
     {
         int[][] walls = fillWalls(nodes.length, nodes[0].length);
         MazeNode current = null;
 
         for (int countY = 0; countY < nodes.length; countY++)
         {
             for (int countX = 0; countX < nodes[countY].length; countX++)
             {
                 int wallX = (2 * countX) + 1;
                 int wallY = (2 * countY) + 1;
 
                 current = nodes[countX][countY];
                 MazeNode[] neighbors = current.getAllNeighbors();
 
                 for (MazeNode temp : neighbors)
                 {
                     if (temp != null)
                     {
                        if (current.getX() == 0 && current.getY() == 3)
                        {
                            System.out.println("HEY");
                        }
                         if (current.getX() == temp.getX() - 1 && current.getY() == temp.getY())
                         {
                             walls[wallX + 1][wallY] = 1;
                         }
                         else if (current.getX() == temp.getX() + 1 && current.getY() == temp.getY())
                         {
                             walls[wallX - 1][wallY] = 1;
                         }
                         else if (current.getY() == temp.getY() - 1 && current.getX() == temp.getX())
                         {
                             walls[wallX][wallY + 1] = 1;
                         }
                         else if (current.getY() == temp.getY() + 1 && current.getX() == temp.getX())
                         {
                             walls[wallX][wallY - 1] = 1;
                         }
                     }
                 }
             }
         }
         return walls;
     }
 
     /**
      * Fills in the border wall as well as the spaces that are always walls
      * 
      * @param nodeWidth The number of nodes wide the maze is
      * @param nodeLength The number of nodes long the maze is
      * 
      * @return Returns an 2d int array of the walls
      */
     private final int[][] fillWalls(int nodeWidth, int nodeLength)
     {
         // Convert from number of nodes to size of maze
         int[][] walls = new int[(2 * nodeWidth) + 1][(2 * nodeLength) + 1];
 
         for (int countY = 0; countY < walls.length; countY++)
         {
             for (int countX = 0; countX < walls[countY].length; countX++)
             {
                 if (((countX % 2) == 0 && (countY % 2) == 0) || countX == 0 || countX == walls.length - 1 || countY == 0 || countY == walls[countX].length - 1)
                 {
                     walls[countX][countY] = 1;
                 }
             }
         }
         return walls;
     }
 
     public int[] createMaze(int width, int height, int endX, int endY)
     {
         MazeNode[] mazeNodes = createNodes(width, height);
 
         createPaths(mazeNodes, mazeNodes[(width * endY) + endX]);
 
         int[] walls = new int[((2 * width) - 1) * ((2 * height) - 1)];
 
         for (int i = 2 * width; i < walls.length; i += (4 * width) - 2)
         {
             for (int j = 0; j < (2 * width - 2); j += 2)
             {
                 walls[i + j] = 1;
             }
         }
 
         int row = -2;// It will incriment first thing in the first loop to 0
         int pos = 0;
         int real = 0;
         MazeNode current = null;
         for (int i = 0; i < mazeNodes.length; i++)
         {
             current = mazeNodes[i];
             if ((mazeNodes[i].getX() % width) == 0)
             {
                 row += 2;
                 pos = 0;
             }
             real = (row * ((2 * width) - 1)) + pos;// The real position on the walls array of the
             // current cell
             MazeNode[] neighbors = mazeNodes[i].getAllNeighbors();
             for (int j = 0; j < neighbors.length; j++)
             {
                 if (neighbors[j] != null)
                 {
                     if (neighbors[j].getX() == (current.getX() + 1))
                     {
                         walls[real + 1] = 1;
                     }
                     else if (neighbors[j].getX() == (current.getX() - width))
                     {
                         walls[real - (2 * width) + 1] = 1;
                     }
                 }
             }
             pos += 2;
         }
         return walls;
     }
 
     private MazeNode[] createNodes(int width, int height)
     {
         MazeNode[] mazeNodes = new MazeNode[width * height];
         mazeNodes[0] = new MazeNode(0, 0);
         for (int i = 1; i < mazeNodes.length; i++)
         {
             mazeNodes[i] = new MazeNode(i, 0);
             if ((i % width) != 0)
             {
                 mazeNodes[i].addNeighbor(mazeNodes[i - 1]);
                 mazeNodes[i - 1].addNeighbor(mazeNodes[i]);
             }
             if ((i - width) >= 0)
             {
                 mazeNodes[i].addNeighbor(mazeNodes[i - width]);
                 mazeNodes[i - width].addNeighbor(mazeNodes[i]);
             }
         }
 
         return mazeNodes;
     }
 
     private MazeNode[] createPaths(MazeNode[] mazeNodes, MazeNode start)
     {
         MazeNode current = start;
         Stack<MazeNode> path = new Stack<MazeNode>();
         current.setVisited();
         while (current.hasUnvisitedNeighbors())
         {
             MazeNode temp = current.getRandomUnvisitedNeighbor(random);
             temp.removeNeighbor(current.getX(), 0);
             path.push(temp);
             current = temp;
             current.setVisited();
         }
         while (path.size() > 0)
         {
             MazeNode temp = path.pop();
             if (temp.hasUnvisitedNeighbors())
             {
                 mazeNodes = createPaths(mazeNodes, temp);
             }
         }
 
         return mazeNodes;
     }
 
     public static void main(String[] args)
     {
         int size = 11;
 
         Maze maze = new Maze(123);
         // Test fillNodes
         MazeNode[][] test1 = maze.fillNodes(size / 2, size / 2);
         for (int y = 0; y < test1.length; y++)
         {
             for (int x = 0; x < test1[y].length; x++)
             {
                 System.out.print(test1[y][x]);
                 System.out.println(" : Visited: " + test1[y][x].isVisisted());
             }
         }
 
         System.out.println("\n\n\n*********************TEST SEPERATOR************************\n\n");
 
         // Test createPaths
         MazeNode[][] test2 = maze.createPaths(test1, test1[0][0]);
         for (int y = 0; y < test2.length; y++)
         {
             for (int x = 0; x < test2[y].length; x++)
             {
                 System.out.print(test2[y][x]);
                 System.out.println(" : Visited: " + test2[y][x].isVisisted());
             }
         }
 
         System.out.println("\n\n\n*********************TEST SEPERATOR************************\n\n");
 
         // Test createWalls
 
         int[][] test3 = maze.createWalls(test2);
         for (int y = 0; y < test3.length; y++)
         {
             for (int x = 0; x < test3[y].length; x++)
             {
                 System.out.print("(" + x + "," + y + ") = " + test3[x][y] + " - ");
             }
             System.out.println();
         }
     }
 }
