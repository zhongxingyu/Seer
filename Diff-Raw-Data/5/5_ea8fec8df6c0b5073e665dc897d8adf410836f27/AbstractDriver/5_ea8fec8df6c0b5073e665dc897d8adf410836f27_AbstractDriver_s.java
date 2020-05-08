 package drivers;
 
 import interiores.business.models.FurnitureModel;
 import interiores.utils.Dimension;
 import interiores.business.models.FurnitureType;
 import interiores.business.models.Orientation;
 import interiores.business.models.OrientedRectangle;
 import interiores.business.models.RoomType;
 import interiores.business.models.backtracking.FurnitureValue;
 import interiores.core.presentation.terminal.IOStream;
 import interiores.utils.Range;
 import java.awt.Color;
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  *
  * @author alvaro
  */
 public abstract class AbstractDriver {
     
     private static IOStream iostream = new IOStream(System.in, System.out);
     
     public static Dimension readDimension() {
         int w = iostream.readInt("Enter the width: ");
         int h = iostream.readInt("Enter the depth: ");
         
         return new Dimension(w, h);
     }
     
     public static Range readRange() {
         int min = iostream.readInt("Enter the minimum value: ");
         int max = iostream.readInt("Enter the maximum value: ");
         
         return new Range(min,max);
     }
     
     public static Color readColor() {
         
         int r = iostream.readInt("R:");
         int g = iostream.readInt("G: ");
         int b = iostream.readInt("B: ");
         
         return new Color(r, g, b);
     }
     
     public static FurnitureType readFurnitureType() {
         
         String name = iostream.readString("Enter the name of the furniture type: ");
         
         iostream.println("Enter the width range: ");
         Range widthRange = readRange();
         
         iostream.println("Enter the maximum dimensions of the furniture model: ");
         Range depthRange = readRange();
                 
         return new FurnitureType(name, widthRange, depthRange);
     }
     
     public static FurnitureModel readFurnitureModel() {
         
        FurnitureType ftype = readFurnitureType();
        
         String name = iostream.readString("Enter the name of the furniture model: ");
         
         iostream.println("Enter the dimensions of the model: ");
         Dimension dim = readDimension();
         
         float price = iostream.readFloat("Enter the price of the furniture model: ");
         
         iostream.println("Introduce un color");
         Color color = readColor();
         
         String material = iostream.readString("Enter the material of the furniture model");
         
         
        return new FurnitureModel(ftype, name, dim, price, color, material);
         
     }
     
     public static Collection<FurnitureType> readFurnitureTypeCollection() {
         
         int n = iostream.readInt("How many?: ");
         Collection<FurnitureType> furnitureTypes = new ArrayList<FurnitureType>(n);
                 
         for (FurnitureType ft : furnitureTypes) {
             ft = readFurnitureType();
         }
         
         return furnitureTypes;
     }
     
     public static RoomType readRoomType() {
         
         String name = iostream.readString("Enter the name of the room type: ");
         
         iostream.println("Enter the furnitures that have to be in this type of Room: ");
         Collection<FurnitureType> accepted = readFurnitureTypeCollection();
         
         iostream.println("Enter the furnitures that can't to be in this type of Room: ");
         Collection<FurnitureType> forbidden = readFurnitureTypeCollection();
         
         return new RoomType(name, accepted, forbidden);
     }
     
     public static Orientation readOrientation() {
         String orientation = iostream.readString("Enter the orientation (N,S,W or E): ");
         
         switch(orientation.charAt(0)) {
             case 'N':
                 return Orientation.N;
             case 'E':
                 return Orientation.E;
             case 'W':
                 return Orientation.W;
             default:
                 return Orientation.S;
         }
                 
     }
     
     public static OrientedRectangle readOrientedRectangle() {
         int x = iostream.readInt("Enter the x position of the rectangle: ");
         int y = iostream.readInt("Enter the y position of the rectangle: ");
         
         iostream.println("Enter the size of the rectangle: ");
         Dimension dim = readDimension();
         
         Orientation o = readOrientation();
         
         return new OrientedRectangle(new Point(x,y), dim, o);
     }
     
     public static FurnitureValue readFurnitureValue() {
         OrientedRectangle or = readOrientedRectangle();
         FurnitureModel fm = readFurnitureModel();
         
         return new FurnitureValue(or, fm);
     }
     
     public abstract boolean test();
 }
