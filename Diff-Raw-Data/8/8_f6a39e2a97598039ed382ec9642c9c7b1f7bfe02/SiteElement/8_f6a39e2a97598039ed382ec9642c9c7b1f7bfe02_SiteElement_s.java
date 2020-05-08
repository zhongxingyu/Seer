 import java.util.ArrayList;
 import java.awt.*;
 /**
   * class for SiteElement
   * @author Casey Harford
   * @version 2.0
   * @param buildSpace   defines array for grid of objects
   * @param size_x       defines the size of x axis for buildSpace
   * @param size_y       defines the size of y axis for buildSpace
  */
 public class SiteElement {
 
   /** store objects already built */
   private ArrayList<alreadyBuilt> builtAlready;
 
   /** buildSpace starts with 0s, and is used to track which objects are already on map */
   private int buildSpace[][];
 
   /** store the widths of each object */
   private static int buildSizes_x[];
 
   /** store the height of each object */
   private static int buildSizes_y[];
 
   /** store the names of each object */
   private static String buildObjects[];
 
   /** width of object */
   private int size_x;
 
   /** height of object */
   private int size_y;
 
   /**
    *  constructor for SiteElement
    *  @param width    width of build space to create
    *  @param height   height of build space to create
   */
   public SiteElement (int width,int height) {
 
     /** set width and height based on given parameters */
     size_x = width;
     size_y = height;
 
     /** instantiate list for alreadyBuilt objects */
     builtAlready = new ArrayList<alreadyBuilt>();
 
     /** initialize build space based on size x and y */
     buildSpace = new int[size_x][size_y];
     
     /** storing names of each object */
     buildObjects = new String[7];
 
       buildObjects[0] = "Empty";
       buildObjects[1] = "Tree";
       buildObjects[2] = "Grass";
       buildObjects[3] = "Road";
       buildObjects[4] = "House";
       buildObjects[5] = "Building";
       buildObjects[6] = "Water";
       
     /** initialize array of sizes for objects, both width and height */
     buildSizes_x = new int[7];
     buildSizes_y = new int[7];
     
       buildSizes_x[0] = 0;  //nothing
       buildSizes_y[0] = 0;  //nothing
 
       buildSizes_x[1] = 1;  //tree
       buildSizes_y[1] = 1;  //tree
 
       buildSizes_x[2] = 1;  //grass
       buildSizes_y[2] = 1;  //grass
    
       buildSizes_x[3] = 1;  //road
       buildSizes_y[3] = 1;  //road
     
       buildSizes_x[4] = 2;  //house
       buildSizes_y[4] = 2;  //house
 
       buildSizes_x[5] = 3;  //building
       buildSizes_y[5] = 2;  //building
 
       buildSizes_x[6] = 1;  //water
       buildSizes_y[6] = 1;  //water
   }
   
   /**
    *  method for getList
    *  @return an ArrayList of current items on map
   */ 
   public ArrayList<alreadyBuilt> getList() {
     return builtAlready;
   }
   
   /**
    *  method for createObject
    *  @param type   gets the requested type of object to create
    *  @param loc_x  gets the x coordinate to place object
    *  @param loc_y  gets the y coordinate to place object
    *  @param color  gets the color to be used for the object 
    *  @return       true if object is created, and false if it fails
   */
   public boolean createObject(String type,int loc_x,int loc_y,Color color,int size) {
 
     /** based on the type being created, check if it exists and can be built, if built return true */
     if(type.equalsIgnoreCase("tree")) {
       if(trackObjects(1,loc_x,loc_y,color,size)) {
         builtAlready.add(new alreadyBuilt(1,type,loc_x,loc_y,color,size));
         return true;
       }
     }
     else if(type.equalsIgnoreCase("grass")) {
       if(trackObjects(2,loc_x,loc_y,color,size)) {
         builtAlready.add(new alreadyBuilt(2,type,loc_x,loc_y,color,size));
         return true;
       }
     }
     else if(type.equalsIgnoreCase("road")) {
       if(trackObjects(3,loc_x,loc_y,color,1)) {
         builtAlready.add(new alreadyBuilt(3,type,loc_x,loc_y,color,1));
         return true;
       }
     }
     else if(type.equalsIgnoreCase("house")) {
       if(trackObjects(4,loc_x,loc_y,color,size)) {
         builtAlready.add(new alreadyBuilt(4,type,loc_x,loc_y,color,size));
         return true;
       }
     }
     else if(type.equalsIgnoreCase("building")) {
       if(trackObjects(5,loc_x,loc_y,color,size)) {
         builtAlready.add(new alreadyBuilt(5,type,loc_x,loc_y,color,size));
         return true;
       }
     }
     else if(type.equalsIgnoreCase("water")) {
       if(trackObjects(6,loc_x,loc_y,color,size)) {
         builtAlready.add(new alreadyBuilt(6,type,loc_x,loc_y,color,size));
         return true;
       }
     }
     /** display error if invalid object was specified */
     else {
       System.out.println("Invalid object specified");
       return false;
     }
     return false;
   }
   
   /**
    *  method for trackObjects, used to track objects in the buildSpace
    *  @param type     the type of object being created
    *  @param loc_x    x coordinate to create object
    *  @param loc_y    y coordinate to create object
    *  @param color    the color to make object
    *  @param size     the size to make object
    *  @return         true if item can be inserted, or false if something already exists
   */
   private boolean trackObjects(int type,int loc_x,int loc_y,Color color,int size) {
 
     /** set width */
     int create_size_x = buildSizes_x[type]*size;
 
     /** set height */
     int create_size_y = buildSizes_y[type]*size;
   
     /** default to space available, if space is not available, set to false */
     boolean spaceAvailable = true;
     if( ( (loc_x+create_size_x)>size_x ) | ( (loc_y+create_size_y)>size_y ) ) {
       System.out.println("Error, outside of buildSpace " + loc_x + ", " + loc_y);
       return false;
     }
 
     /** checking if space is available in buildSpace */
     for(int cord_y=loc_y; cord_y<create_size_y+loc_y; cord_y++) {
       for(int cord_x=loc_x; cord_x<create_size_x+loc_x; cord_x++) {
         if(buildSpace[cord_x][cord_y]==0) {
           //System.out.println("Setting buildSpace at " + cord_x + ", " + cord_y + " to " + "type: " + type);
         }
         else {
           spaceAvailable=false;
           System.out.println("Error, object already at location " + cord_x + ", " + cord_y);
         }
       }
     }
     /** if spaceAvailable then go ahead and build it */
     if(spaceAvailable) {
       for(int cord_y=loc_y; cord_y<create_size_y+loc_y; cord_y++) {
         for(int cord_x=loc_x; cord_x<create_size_x+loc_x; cord_x++) {
           buildSpace[cord_x][cord_y]=type;
           //System.out.println("buildSpace recorded for type " + type + " at location " + cord_x + ", " + cord_y);
         }
       }
     }  
     return spaceAvailable;
   }
   
   /**
    *  method for clear
    *  clears the build space, allowing user to start over
   */
   public void clear() {
     builtAlready = new ArrayList<alreadyBuilt>();
     buildSpace = new int[size_x][size_y];
     System.out.println("cleared build space");
   }
   
   /**
    *  method for undo, deletes the last object built
   */
   public void undo() {
     
     /** the y coordinate of object */
     int y = builtAlready.get(builtAlready.size()-1).getY();
 
     /** the x coordinate of object */
     int x = builtAlready.get(builtAlready.size()-1).getX();
 
     /** the size of object */
     int size = builtAlready.get(builtAlready.size()-1).getSize();
 
     /** get the intType of object*/
     int intType = builtAlready.get(builtAlready.size()-1).getIntType();
 
     /** width of object */
     int width = buildSizes_x[intType]*size;
 
     /** height of object */
     int height = buildSizes_y[intType]*size;
     
     /** reset buildSpace coordinates to 0 where object once existed */
     for(int i=y; i<(y+height); i++) {
       for(int j=x; j<(x+width); j++) {
         buildSpace[j][i]=0;
       }
     }

     /** remove last item from list of objects built */
    builtAlready.remove(builtAlready.size()-1);
   }
 
   /**
    *  class for alreadyBuilt
    *  used to store info about an object that has already been built
   */
   static class alreadyBuilt {
     
     /** */
     private int intType;
     /** type of object to create */
     private String create;
 
     /** x coordinate to place object */
     private int x;
 
     /** y coordinate to place object */
     private int y;
 
     /** size of object */
     private int size;
 
     /** color of object */
     private Color color;
 
 
     /**
      *  constructor for alreadyBuilt
      *  @param createThis   object to create
      *  @param x_cord       x coordinate of object
      *  @param y_cord       y coordinate of object
      *  @param colorThis    color of object 
      *  @param sizeThis     size of object
     */
     alreadyBuilt(int type,String createThis,int x_cord,int y_cord, Color colorThis, int sizeThis) {
       intType = type;
       create = createThis;
       color = colorThis;
       size = sizeThis;
       x = x_cord;
       y = y_cord;
     }
 
     /**
      *  method for getX
      *  @return  the x coordinate of object
     */
     public int getX() {
       return x;
     }
 
     /**
      *  method for getY
      *  @return  the y coordinate of object
     */
     public int getY() {
       return y;
     }
   
     /**
      *  method for getType
      *  @return  the type of object
     */
     public String getType() {
       return create;
     }
     
     /**
      *  method for getColor
      *  @return  the color of the object
     */
     public Color getColor() {
       return color;
     }
 
     /** 
      *  method for getSize 
      *  @return  the size of the object 
     */
     public int getSize() {
       return size;
     }
 
     public int getIntType() {
       return intType;
     }
 
     public int getWidth() {
       return buildSizes_x[intType];
     }
     
     public int getHeight() {
       return buildSizes_y[intType];
     }
   };
 };
