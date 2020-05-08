 package rmit.ai.clima.jackagt;
 import rmit.ai.clima.gui.grid.*;
 
 public class PathGrid
 {
    //handle to the PathGrid Singleton
    private static PathGrid instance = null;
 
    public int width;
    public int height;
    public boolean firstRequest;
    PathNode [] grid;
 
 
    public static PathGrid initInstance(int width, int height)
    {
       if(instance == null)
       {
          instance = new PathGrid( width, height );
          instance.firstRequest = true;
          System.out.println("Instantiating the grid");
       }
       return instance;
    }
    public static PathGrid getInstance()
    {
       if(instance == null)
          instance = new PathGrid( 50 , 50);
       return instance;
    }
 
    public static void Release()
    {
       instance = null;
    }
 
    public void Reset()
    {
       this.firstRequest = true;
       for (int x=0; x<this.width; ++x) {
          for (int y=0; y<this.height; ++y) {
 
             PathNode node =  grid[y * width + x]; 
             node.obstacle = false;
          }
       }
 
    }
 
    public void Clean()
    {
       for (int x=0; x<this.width; ++x) {
          for (int y=0; y<this.height; ++y) {
 
             PathNode node =  grid[y * width + x]; 
             node.visited = false;
             node.f = 0;
             node.g = 0;
             node.h = 0;
          }
       }
    }
 
    public PathGrid (int width, int height)
    {
       this.width = width;
       this.height = height;
       grid = new PathNode[ width * height ];
 
       for (int x=0; x<width; ++x) {
          for (int y=0; y<height; ++y) {
 
             PathNode node = new PathNode(); 
             node.pos.x = x;
             node.pos.y = y;
             grid[y * width + x] = node;
          }
       }
    }
 
    public PathNode getNode (int x, int y)
    {
       return grid[ y * width + x ];
    }
 
    public PathNode getNode (GridPoint pt)
    {
       return getNode( pt.x, pt.y );
    }
 }
 
