 package bmc.game.level;
     
 import java.util.ArrayList;
 import java.util.List;
 
 import bmc.game.Panel;
 import bmc.game.gameobjects.GameObject;
 import bmc.game.gameobjects.LaserGun;
 import bmc.game.gameobjects.Player;
 import bmc.game.gameobjects.Sprite;
 
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.graphics.RectF;
 
 public class Level {
 	protected ArrayList<Path>         paths;
     protected ArrayList<Entrance>     entrances;
     protected ArrayList<Block>		  blocksOnScreen;
     protected ArrayList<GameObject>   objectsOnScreen;
     protected Sprite[] 				  mSprites;
     protected RectF 				  mRect = new RectF();
     protected Rect					  mDestination = new Rect();
 	protected int 				      mWidth;
 	protected int                     mHeight;
     protected boolean				  initialized=false;
     protected Path                    pathWithStartPoint;
     protected int                     startX = 0;
     protected int                     startY = 0;
     
     public enum CollisionStates
     {
         NONE,
         LEFT,
         RIGHT,
         TOP,
         BOTTOM,
         TOPANDLEFT,
         TOPANDRIGHT,
         BOTTOMANDLEFT,
         BOTTOMANDRIGHT
     }
     public Level()
     {
         paths = new ArrayList<Path>();
         entrances = new ArrayList<Entrance>();
         blocksOnScreen = new ArrayList<Block>();
         
     }
     
     public void UpdateStartPoint()
     {
         // Search through the paths to find the start point.
         // This will be called every time a path is added.
         // If no start point exists, the start point will remain (0, 0).
         // This code also assumes one start point, as it will check
         // each path. The last path in the array to have a start point
         // will give its start point data.
         for (Path p : paths)
         {
             if (p.isHasStartPoint())
             {
                 pathWithStartPoint = p;
                 startX = p.getStartX();
                 startY = p.getStartY();
             }
         }
     }
     
     public void AddPath (Path path)
     {
         paths.add(path);
         UpdateStartPoint();
     }
     
     public void AddEntrance (Entrance entrance)
     {
         entrances.add(entrance);
     }
     
 
 	public void animate(long elapsedTime,float X, float Y) {
 		// TODO Auto-generated method stub
 		mWidth = (int) Panel.mWidth;
 		mHeight = (int) Panel.mHeight;
 		this.addX(X);
 		this.addY(Y);
 		if(X+Y != 0 || !initialized)
 		{
 			synchronized (blocksOnScreen) 
 			{
 				blocksOnScreen.clear();
 				//if we change the screen look through blocks to see which ones we need to draw
 				synchronized (paths) {
 			        for (Path path : paths) {
 			        	for (Block block : path.getBlocks())
 			        	{
 			        		if(block.shouldDraw(mDestination))
 			        		{
 			        			block.animate(elapsedTime);
 			        			block.setX(block.getMapPostion().left-mDestination.left);
 			        			block.setY(block.getMapPostion().top-mDestination.top);
 			        			blocksOnScreen.add(block);
 			        		}
 			        	}
 			        	
 			        }
 			    }
 				initialized = true;
 			}
 			synchronized (objectsOnScreen)
 			{
 			    objectsOnScreen.clear();
 			    
 			    synchronized (paths) 
 			    {
                     for (Path path : paths) 
                     {
         			    for (GameObject obj : path.getObjects())
                         {
                             if(obj.shouldDraw(mDestination))
                             {
                                 obj.animate(elapsedTime);
                                 obj.setX(obj.getMapPostion().left-mDestination.left);
                                 obj.setY(obj.getMapPostion().top-mDestination.top);
                                 objectsOnScreen.add(obj);
                             }
                         }
                     }
 			    }
			}
 		}
 		int i = 0;
 	}
 
 	public void doDraw(Canvas canvas) {
 		// TODO Auto-generated method stub
 		
 		synchronized (blocksOnScreen) 
 		{
         	for (Block block : blocksOnScreen)
         	{
         		block.doDraw(canvas);
         	}
 		}
 	}
     
     public CollisionStates IsCollidingWithLevel(RectF rect1)
     {
         // We have the bounding rectangle for the object in question. Now
         // we should check all of the blocks in our paths to see if they intersect in any direction.
         CollisionStates state = CollisionStates.NONE;
         RectF rect = new RectF(rect1.left + mDestination.left,rect1.top + mDestination.top,rect1.right + mDestination.left,rect1.bottom + mDestination.top);
 
         boolean top = false, bottom = false, left = false, right = false;
         for (Path p : paths)
         {
             ArrayList<Block> blocks = p.getBlocks();
             
             for (Block b : blocks)
             {
             	
             	
                 // Bounds for top and bottom
                 if ((rect.left >= b.getXpos() && rect.left <= b.getXpos()+b.getWidth()) || ((rect.right <= b.getXpos() + b.getWidth())&& (rect.right >= b.getXpos())))
                 {
                     // Top check
                     if (rect.top <= b.getYpos() + b.getHeight() && rect.top >= b.getYpos())
                     {
                         top = true;
                     }
                     
                     if (rect.bottom >= b.getYpos() && rect.bottom <= b. getYpos() + b.getHeight())
                     {
                         bottom = true;
                     }
                 }
                 
                 // Bounds for left and right
                 if ((rect.top >= b.getYpos()&& rect.top <= b.getYpos()+b.getHeight()) || (rect.bottom <= b.getYpos() + b.getHeight() && rect.bottom >= b.getYpos()))
                 {
                     // Left check
                     if (rect.left >= b.getXpos() && rect.left <= b.getXpos()+b.getWidth())
                     {
                         left = true;
                     }
                     
                     // Right check
                     if (rect.right <= b.getXpos()+ b.getWidth() && rect.right >= b.getXpos())
                     {
                         right = true;
                     }
                 }
             }
         }
         
         // Now, merge the booleans
         if (top)
         {
             if (left) { state = CollisionStates.TOPANDLEFT; }
             else if (right) { state = CollisionStates.TOPANDRIGHT; }
             else { state = CollisionStates.TOP; }
         }
         
         else if (bottom)
         {
             if (left) { state = CollisionStates.BOTTOMANDLEFT; }
             else if (right) { state = CollisionStates.BOTTOMANDRIGHT; }
             else { state = CollisionStates.BOTTOM; }
         }
         
         // We already checked for the combined ones, so we can safely use else if's to check for left and right
         else if (left) { state = CollisionStates.LEFT; }
         else if (right) { state = CollisionStates.RIGHT; }
         
         // If it didn't hit any of those, it's already set to NONE.
         return state;
     }
 
     public void addX(float X)
     {
     	setX(mRect.left+X);
     }
     public void addY(float Y)
     {
     	setY(mRect.top+Y);
     }
 
 	public float getX() {
 		return mRect.left;
 	}
 
 	public void setX(float mX) {
 		this.mRect.left = mX;
 		mRect.right = mRect.left+mWidth;
 		
 		this.mDestination.left = (int)mX;
 		mDestination.right = mDestination.left+mWidth;
 	}
 
 	public float getY() {
 		return mRect.top;
 	}
 
 	public void setY(float mY) {
 		this.mRect.top = mY;
 		mRect.bottom = mRect.top+mHeight;
 		
 		this.mDestination.top = (int)mY;
 		mDestination.bottom = mDestination.top+mHeight;
 	}
     public Rect getDestination() {
 		return mDestination;
 	}
 	public void setDestination(Rect mDestination) {
 		this.mDestination = mDestination;
 	}
 	public RectF getRect() {
 		return mRect;
 	}
 	public void setRect(RectF mRect) {
 		this.mRect = mRect;
 	}
 
 	public int getmWidth() {
 		return mWidth;
 	}
 
 	public void setmWidth(int mWidth) {
 		this.mWidth = mWidth;
 	}
 
 	public int getmHeight() {
 		return mHeight;
 	}
 
 	public void setmHeight(int mHeight) {
 		this.mHeight = mHeight;
 	}
 
 	public Sprite[] getmSprites() {
 		return mSprites;
 	}
 
 	public void setmSprites(Sprite[] mSprites) {
 		this.mSprites = mSprites;
 	}
 
     public Path getPathWithStartPoint()
     {
         return pathWithStartPoint;
     }
 
     public void setPathWithStartPoint(Path pathWithStartPoint)
     {
         this.pathWithStartPoint = pathWithStartPoint;
     }
 
     public int getStartX()
     {
         return startX;
     }
 
     public void setStartX(int startX)
     {
         this.startX = startX;
     }
 
     public int getStartY()
     {
         return startY;
     }
 
     public void setStartY(int startY)
     {
         this.startY = startY;
     }
     
     public void setPlayerInObjects(Player player)
     {
         // This function is meant to set the player object in all
         // of the objects we have stored in each path. A temp player
         // was created in LevelManager to feed into the objects to
         // create them, but this temp player obviously isn't the one
         // we want. Physics calls this, as it holds on to the player 
         // object. 
         for (Path p : paths)
         {
             ArrayList<GameObject> objs = p.getObjects();
             for (GameObject obj : objs)
             {
                 if (obj.getClass().equals(LaserGun.class))
                 {
                     LaserGun gun = (LaserGun) obj;
                     gun.setPlayer(player);
                 }
             }
         }
     }
     
     public void setListInObjects(List<GameObject> objects)
     {
         // This function is meant to set the object list in all
         // of the objects we have stored in each path. A temp list
         // was created in LevelManager to feed into the objects to
         // create them, but this temp list obviously isn't the one
         // we want. Physics calls this, as it holds on to the object 
         // list. 
         for (Path p : paths)
         {
             ArrayList<GameObject> objs = p.getObjects();
             for (GameObject obj : objs)
             {
                 if (obj.getClass().equals(LaserGun.class))
                 {
                     LaserGun gun = (LaserGun) obj;
                     gun.setList(objects);
                 }
             }
         }
     }
 }
