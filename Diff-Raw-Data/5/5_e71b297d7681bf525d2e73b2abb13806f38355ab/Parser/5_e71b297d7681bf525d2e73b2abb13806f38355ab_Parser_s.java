 package dk.itu.grp11.data;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.EnumMap;
 import java.util.HashMap;
 
 import dk.itu.grp11.enums.MapBound;
 import dk.itu.grp11.enums.RoadType;
 import dk.itu.grp11.exceptions.DataNotInitializedException;
 
 /**
  * Parses information about a road network.
  * 
  * @author Group 11
  * 
  */
 public class Parser {
   private static boolean pointsInit = false;
   private File nodes;
   private File connections;
   private static EnumMap<MapBound, Double> mapBounds;
   
   /**
    * 
    * @param nodeFile A java.File object referencing the file containing nodes.
    * @param connectionFile A java.File object referencing the file containing connections.
    */
   public Parser(File nodeFile, File connectionFile) {
     nodes = nodeFile;
     connections = connectionFile;
     mapBounds = new EnumMap<MapBound, Double>(MapBound.class);
     mapBounds.put(MapBound.MINX, 1000000.0);
     mapBounds.put(MapBound.MAXX, 0.0);
     mapBounds.put(MapBound.MINY, 100000000.0);
     mapBounds.put(MapBound.MAXY, 0.0);
   }
 
   /**
    * Parses all points in the network and puts it in a HashMap with point-ID as key,
    * and the point as value.
    * 
    * @return HashMap containing all nodes
    */
   public HashMap<Integer, Point> parsePoints() {
     HashMap<Integer, Point> tmp = new HashMap<Integer, Point>();
     try {
       BufferedReader input = new BufferedReader(new FileReader(nodes));
       try {
         String line = null;
         /*
          * readLine is a bit quirky : it returns the content of a line MINUS the
          * newline. it returns null only for the END of the stream. it returns
          * an empty String if two newlines appear in a row.
          */
         input.readLine();
         // Finding maximum and minimum x and y coordinates
         while ((line = input.readLine()) != null) {
           Point p = createPoint(line);
           if (p.getX() > mapBounds.get(MapBound.MAXX)){
             mapBounds.put(MapBound.MAXX, p.getX());
           }
           if (p.getX() < mapBounds.get(MapBound.MINX)){
             mapBounds.put(MapBound.MINX, p.getX());
           }
           if (p.getY() > mapBounds.get(MapBound.MAXY)){
             mapBounds.put(MapBound.MAXY, p.getY());
           }
           if (p.getY() < mapBounds.get(MapBound.MINY)){
             mapBounds.put(MapBound.MINY, p.getY());
           }
           tmp.put(p.getID(), p);
         }
       } finally {
         input.close();
       }
     } catch (IOException ex) {
       ex.printStackTrace();
     }
     pointsInit = true; //Points have been initialized (and getMinMaxValues() can be called)
     return tmp;
   }
 
   /**
    * Parses all roads in the network and puts it in a DimensionalTree sorted by
    * the x and y coordinate of the road and road type.
    * 
    * @return DimensionalTree containing all roads
    */
   public DimensionalTree<Double, RoadType, Road> parseRoads(HashMap<Integer, Point> points) {
    DimensionalTree<Double, RoadType, Road> tmp = new DimensionalTree<Double, RoadType, Road>();    
       try(BufferedReader input = new BufferedReader(new FileReader(connections))) {
         String line = null;
         /*
          * readLine is a bit quirky : it returns the content of a line MINUS the
          * newline. it returns null only for the END of the stream. it returns
          * an empty String if two newlines appear in a row.
          */
         input.readLine();
         while ((line = input.readLine()) != null) {
           String[] split = splitRoadInput(line);
          if (split.length == 4) {
             Double xS = points.get(Integer.parseInt(split[0])).getX();
             Double yS = points.get(Integer.parseInt(split[0])).getY();
             Double xE = points.get(Integer.parseInt(split[1])).getX();
             Double yE = points.get(Integer.parseInt(split[1])).getY();
             Integer type = Integer.parseInt(split[3]);
             Road value = new Road(Integer.parseInt(split[0]), Integer.parseInt(split[1]), split[2], RoadType.getById(type), Double.parseDouble(split[4]), Double.parseDouble(split[5]));
             tmp.insert(xS, yS, RoadType.getById(type), value);
             tmp.insert(xE, yE, RoadType.getById(type), value);
           }
         } 
     } catch (IOException ex) {
       ex.printStackTrace();
     }
     return tmp;
   }
 
   // Creates a point, to be put in the array and parsed.
   /**
    * createPoint splits a line from the kdv_node_unload.txt document and then
    * creates a Point object from the information in the string.
    * 
    * @param input
    *          A line from the kdv_node_unload.txt document.
    * @return A Point object containing the information from the line.
    */
   private static Point createPoint(String input) {
     Point tmp;
     String[] inputSplit = input.split(",");
     tmp = new Point(Integer.parseInt(inputSplit[2]),
         Double.parseDouble(inputSplit[3]), Double.parseDouble(inputSplit[4]));
 
     return tmp;
   }
 
   /**
    * createPoint splits a line from the kdv_unload.txt document and then creates
    * a Road object from the information in the string.
    * 
    * @param input
    *          A line from the kdv_unload.txt document.
    * @return A Road object containing the information from the line.
    */
   private static String[] splitRoadInput(String input) {
     String[] tmp = new String[6];
 
     String[] inputSplit = input.split(",");
     if (Integer.parseInt(inputSplit[0]) != Integer.parseInt(inputSplit[1])) {
 
       tmp[0] = inputSplit[0];
       tmp[1] = inputSplit[1];
       tmp[2] = inputSplit[6];
       tmp[3] = inputSplit[5];
       tmp[4] = inputSplit[2]; //Length
       tmp[5] = inputSplit[26]; //Time
 
       return tmp;
     } else return new String[0];
     
   }
   
   /**
    * Returns an array containing 4 values, representing the smallest and largest x and y coordinates of all Points.
    * [0]=minX.
    * [1]=maxX.
    * [2]=minY.
    * [3]=maxY.
    * 
    * Values can be referenced using the MinMax enum.
    * 
    * CAN ONLY BE CALLED IF THE PARSEPOINTS() FUNCTION HAS ALREADY BEEN CALLED!
    * @return Maximum/minimum x- and y-coordinates.
    * @throws DataNotInitializedException if points has not been initialized.
    */
   public static double getMapBound(MapBound mb) throws DataNotInitializedException {
     if(!pointsInit) throw new DataNotInitializedException(); // Checks if data has been initialized (parsed)
     return mapBounds.get(mb);
   }
 
 }
