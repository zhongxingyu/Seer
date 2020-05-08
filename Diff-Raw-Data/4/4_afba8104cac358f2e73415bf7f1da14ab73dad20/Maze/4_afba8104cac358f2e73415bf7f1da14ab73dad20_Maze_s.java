 // name: Torin Rudeen
 // dependencies: StdDraw, StdIn, StdOut
 
 public class Maze
 {
     //input maze from file; 0 = passageway, 1 = wall, 2 = entrance, 3 = exit
     // file begins with height followed by width.
     public static int[][] load()
     { 
         int height = StdIn.readInt();
         int width = StdIn.readInt();
         int[][] maze = new int[height][width];
         
         for (int i = 0; i < height; i++)
         {
             for (int j = 0; j < width; j++)
             {
                 maze[i][j] = StdIn.readInt();
             }
         }
         return maze;
     }
     
     public static int[][] generate(int height, int width)
     {
         height = 2*height + 1;
         width = 2*width + 1;
         int[][] maze = new int[height][width];
         boolean[][] visited = newBoolean(height, width, false);
         
         // fill maze with walls.
         for (int i = 0; i < height; i++)
         {
             {
                 for (int j = 0; j < width; j++)
                 {
                     if (i % 2 == 0 || j % 2 == 0) maze[i][j] = 1;
                 }
             }
         }
         
         // place exit and entrance.
         int endI = (((int) ((height - 1)*Math.random()))/2)*2 + 1;
         int startI = (((int) ((height - 1)*Math.random()))/2)*2 + 1;  
         maze[endI][0] = 2;
         maze[startI][width - 1] = 3;
         
         // call carving function to carve maze itself.
         carve(endI, 1, maze, visited);
         
         return maze;
     }
     
     // recursive function, uses depth first search to carve out a perfect maze.
     // (perfect = every square part of maze, one and only one path between any
     // two spaces in the maze).
     public static void carve(int currentI, int currentJ, int[][] maze,
                              boolean[][] visited)
     {
         // mark current cell as visited.
         visited[currentI][currentJ] = true;
         
         // fetch a random ordering of the cardinal directions.
         int[][] directions = randomDirections();
         
         // call itself recursively in the four compass directions, in the order
         // determined above.
         for (int i = 0; i < 4; i++)
         { 
             int newI = currentI + 2*directions[i][0];
             int newJ = currentJ + 2*directions[i][1];
             // ensure target square is in maze and unvisited.
             if (newI < 1 || newI >= maze.length - 1 || newJ < 1
                     || newJ >= maze[0].length - 1) continue;
             if (visited[newI][newJ] == true) continue;
             
             // remove the wall between current square and target square, and
             // then move to target square.
             maze[currentI + directions[i][0]][currentJ + directions[i][1]] = 0;
             carve(newI, newJ, maze, visited);
         }      
         
         return;
     }
     // generates a random ordering of the cardinal directions, in array form.
     public static int[][] randomDirections()
     {
         int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
         int a[] = new int[2];
         for (int i = 0; i < 3; i++)
         {
             a = directions[i];
             int random = (int) (Math.random()*(3-i));
             int target = i + random + 1;
             directions[i] = directions[target];
             directions[target] = a;
         }
         return directions;
     }
     
     // recursive algorithm to solve maze by depth first search. Note that
     // this algorithm is designed to start at the exit and find the entrance.
     // after the entrance is found, it will color the path leading back to the
     // exit, meaning that the solution path will animate the way the user
     // expects (from entrance to exit).
     public static boolean solve(int[][] maze, int currentI, int currentJ,
                                 boolean[][] checked)
     {
         // return if current square is out of the maze, a wall, the entrance,
         // or has been checked. If current square is entrance, it returns true,
         // signaling the start of the solution path.
         if (currentI < 0 || currentI >= maze.length || currentJ < 0
                 || currentJ >= maze[0].length) return false;
         if (maze[currentI][currentJ] == 2) return true;
         if (maze[currentI][currentJ] == 1) return false;
         if (checked[currentI][currentJ] == true) return false;
         
         // mark current cell as checked.
         checked[currentI][currentJ] = true;
         
         // fetch random ordering directions, as in carve(), and then call itself
         // recursively in each of the four directions.
         int[][] directions = randomDirections();
         for (int i = 0; i < 4; i++)
         {
             // call self recursively. If solve() returns true, than called
             // square is either the entrance or on the path to the entrance,
             // so this square is on the path to the entrance. So, we color
             // current square blue (unless we're already at the exit), and
             // return true.
             if (solve(maze, currentI + directions[i][0],
                       currentJ + directions[i][1], checked))
             {
                 if (maze[currentI][currentJ] != 3) 
                     StdDraw.filledSquare(currentJ, 
                                          maze.length - 0.5 - currentI, 0.51);
                 return true;
             }
         }      
         return false;
     }
     
     // prints out the generated or loaded maze.
     public static void print(int[][] maze)
     {
         int height = maze.length;
         int width = maze[0].length;
         double yMax = height - 0.5;
         double xMax = width - 0.5;
         StdDraw.setXscale(-0.5, xMax);
         StdDraw.setYscale(-0.5, yMax);
 
 	// enter animation mode
 	StdDraw.show(0);
 
         // clear canvas
         StdDraw.setPenColor(StdDraw.WHITE);
         StdDraw.filledSquare(0, 0, Math.max(xMax, yMax));
         
         StdDraw.setPenColor(StdDraw.BLACK);
         for (int i = 0; i < height; i++)
         {
             for (int j = 0; j < width; j++)
             {
                 if (maze[i][j] == 1) StdDraw.filledSquare(j, yMax - i, 0.51);
                 else if (maze[i][j] == 2)
                 {
                     StdDraw.setPenColor(StdDraw.GREEN);
                     StdDraw.filledSquare(j, yMax - i, 0.51);
                     StdDraw.setPenColor();
                 }
                 else if (maze[i][j] == 3)
                 {
                     StdDraw.setPenColor(StdDraw.RED);
                     StdDraw.filledSquare(j, yMax - i, 0.51);
                     StdDraw.setPenColor();
                 }
             }
         }
 	StdDraw.show();
     }
     
     
     // utility; generates a boolean array of the requested size, initialized
     // to the requested value.
     public static boolean[][] newBoolean(int a, int b, Boolean value)
     {
         boolean[][] array = new boolean[a][b];
         for (int i = 0; i < a; i++)
         {
             for (int j = 0; j < b; j++)
             {
                 array[i][j] = value;
             }
         }
         return array;
     }
     
     public static void explore(int[][] maze, int currentI, int currentJ)
     {
         while (true)
         {
             if (maze[currentI][currentJ] == 3)
             {
                 StdOut.println("Congratulations!");
                 break;
             }
             
             int[] direction = {0, 0};
         }
         return;
     }
     
     public static void main(String[] args)
     {   
         while (true)
         {
             // generate a maze of the size user requests.
             StdOut.println("What size of maze do you want?");
 	    StdOut.println("     Values from 10 to 100 recommended.");
             StdOut.println("     (type 0 to quit)");
             int size = StdIn.readInt();
            if (size == 0) break;
            int[][] maze = generate(size, size);           
             int height = maze.length;
             int width = maze[0].length;
             
             // locates exit of maze, so we can begin solution finding from
             // that point.
             int endI = 0;
             int endJ = 0;
             int startI = 0;
             int startJ = 0;
             for (int i = 0; i < height; i++)
             {
                 for (int j = 0; j < width; j++)
                 {
                     if (maze[i][j] == 3)
                     {
                         endI = i;
                         endJ = j;
                     }
                     if (maze[i][j] == 2)
                     {
                         startI = i;
                         startJ = j;
                     }
                 }
             }
             print(maze);  
             
             StdOut.println("What do you want to do now?");
             while (true)
             {
                 StdOut.println("     (1 to show solution, 2 to explore,"
                                    + " 0 to go to previous menu)");
                 int choice = StdIn.readInt();
                 if (choice == 1)
                 {
 		    System.out.println("Animate solution? (1 = yes, 0 = no)");
 		    int animate = StdIn.readInt();
 		    if (animate !=  1) { StdDraw.show(0); }
                     boolean[][] checked = newBoolean(height, width, false);
                     StdDraw.setPenColor(StdDraw.BLUE);
                     solve(maze, endI, endJ, checked);
 		    if (animate != 1) { StdDraw.show(); }
                 }
                 else if (choice == 2)
                 {
                     StdOut.println("Sorry, this feature is not yet implemented.");
                     // explore(maze, startI, startJ);
                 }
                 else if (choice == 0) break;
                 else StdOut.println("Pardon me, I'm not all that bright." 
                                         + " Could you please enter 1, 2, or 0?");
             }
         }
     }
     
 }
 
