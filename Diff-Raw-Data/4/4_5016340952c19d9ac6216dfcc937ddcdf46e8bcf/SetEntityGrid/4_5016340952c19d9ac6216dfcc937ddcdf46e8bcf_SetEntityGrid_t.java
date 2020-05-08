 import java.awt.*;
 
 import org.apache.commons.codec.binary.Base64;
 
 import org.json.*;
 
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 import javax.swing.JApplet;
 
 public class SetEntityGrid {
     
     private static Color background;
     private static Grid grid;
     
     private static String FILENAME;
     private static String INFO_FILE;
     private static String ACTION;
     private static Rectangle2D.Double SUB_GRID;
     
     private static int GRID_HEIGHT;
     private static int GRID_WIDTH;
     private final static int ROW_BORDER = 0;
     private final static int COLUMN_BORDER = 0;
     
     private static double CELL_WIDTH;
     private static double CELL_HEIGHT;
     
     private ArrayList<Set> sets = null;
     private ArrayList<Entity> entities = null;
     
     public SetEntityGrid(ArrayList<Set> newsets) {
         
         this.sets = new ArrayList<Set>();
         this.entities = new ArrayList<Entity>();
         
         Iterator<Set> setsIter = newsets.iterator();
         while (setsIter.hasNext()) {
             
             Set set = setsIter.next();
             // add to the sets list
             sets.add(set);
             
             // add the entities for this set that aren't already here
             ArrayList<Entity> ents = set.getEntities();
             Iterator<Entity> iter = ents.iterator();
             while (iter.hasNext()) {
                 Entity newEnt = iter.next();
                 if (!this.entities.contains((Entity)newEnt)) {
                     this.entities.add((Entity)newEnt);
                 } else {
                     // increment the number of occurances of this entity
                     int index = this.entities.indexOf((Entity)newEnt);
                     this.entities.get(index).incrementNumOccurances();
                 }
             }           
         }
         
         // sort entities by the number of occurances (reverse to get top ents on top)
         EntityComparator ec = new EntityComparator();
         Collections.sort(this.entities, ec);
         Collections.reverse(this.entities);
     }
     
     public void setBackground(Color c) {
         background = c;
     }
 
     public void makeGif() {
         
         BufferedImage img = null;
         try {
             
             File file = new File(FILENAME);
             file.createNewFile();
   
             BufferedImage image =
                 new BufferedImage(GRID_WIDTH, GRID_HEIGHT, BufferedImage.TYPE_INT_RGB);
             
             Graphics2D g2 = (Graphics2D)image.createGraphics();
             this.grid.paintGrid(g2, Color.RED, Color.BLACK, Color.GREEN);
             
             ImageIO.write(image, "gif", file);
             g2.dispose();
             
             createInfoFile();
             
         } catch (Exception e) {
             
             System.out.print(e.getStackTrace());
         }
   
     }
     
     public void createInfoFile() throws Exception {
         
         JSONObject jObj = new JSONObject();
         jObj.put("column_width", CELL_WIDTH);
         jObj.put("row_height", CELL_HEIGHT);
         
         String columnNames[] = new String[this.sets.size()];
         Iterator iter = this.sets.iterator();
         int i = 0;
         while (iter.hasNext()) {            
             columnNames[i++] = ((Set)iter.next()).getName();
         }
         
         jObj.put("columns", columnNames);
         
         try {
             File infoFile = new File(INFO_FILE);
             infoFile.createNewFile();
             FileWriter fw = new FileWriter(infoFile);
             fw.write(jObj.toString());
             fw.close();
         } catch (Exception e) {
             System.out.print(e.getStackTrace());
         }       
     }
                          
     
     public void makeGifEncodeToString() {
         BufferedImage img = null;
         try {          
             File file = new File(FILENAME);
             file.createNewFile();
             
             BufferedImage image =
                 new BufferedImage(GRID_WIDTH, GRID_HEIGHT, BufferedImage.TYPE_INT_RGB);
             
             Graphics2D g2 = (Graphics2D)image.createGraphics();
             this.grid.paintGrid(g2, Color.RED, Color.BLACK, Color.GREEN);
             
             String encodedString = imageToBase64String(image);
             
             FileWriter fw = new FileWriter(file);
             fw.write(encodedString);
             fw.flush();
             fw.close();
             
             g2.dispose();
 
             createInfoFile();
             
         } catch (Exception e) {
             
             System.out.print("error!");
         }        
     }
     
     public static String imageToBase64String(BufferedImage image) throws IOException {
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ImageIO.write( image, "gif", baos );
         baos.flush();
         byte[] byteArray = baos.toByteArray();
         baos.close();
         
         Base64 encoder = new Base64();
         return encoder.encodeToString(byteArray);
     }
     
 
     /*
      * Builds the 1x1 cell size grid of columns, each with a group of cells. 
      * 
      *
      */
     public void buildGrid() {
         
         // math!
         int numColumns = this.sets.size();
         int numRows = this.entities.size();
         CELL_WIDTH = (double)(GRID_WIDTH - ROW_BORDER) / (double)numColumns;
         CELL_HEIGHT = (double)(GRID_HEIGHT - COLUMN_BORDER)/ (double)numRows;        
         
         Iterator<Set> setsIter = this.sets.iterator();
                
         Grid grid = new Grid();
         
         double columnIndex = 0;
         while (setsIter.hasNext()) {
             
             // each set corresponds to a column
             Set set = (Set)setsIter.next();
             Column column = new Column(columnIndex, set.getName());
             
             double rowIndex = 0;
             Iterator<Entity> entityIter = this.entities.iterator();
             while (entityIter.hasNext()) {
                 
                 // entity/set pairs correspond to a cell
                 Entity ent = entityIter.next();
                 Cell cell = new Cell(columnIndex, rowIndex, CELL_WIDTH, CELL_HEIGHT, set, ent);
                 
                 column.addCell(cell);
                 
                 rowIndex = rowIndex + CELL_HEIGHT;
             }         
             grid.addColumn(column);
             
             columnIndex = columnIndex + CELL_WIDTH;             
         }
         
         this.grid = grid;
     }
     
     public String getZoomJSON() {
         
         String json = new String();
         
         HashMap<String, ArrayList<String>> map = this.grid.getSetsEntitiesForDimension(SUB_GRID);
         
         ArrayList<String> sets = map.get("sets");
         ArrayList<String> entities = map.get("entities");
         
         JSONArray jsonArray = new JSONArray();
         JSONObject setsObj = new JSONObject();
         JSONObject entObj = new JSONObject();
         try {
             setsObj.put("sets", sets);
             entObj.put("entities", entities);
             jsonArray.put(setsObj);
             jsonArray.put(entObj);
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return jsonArray.toString();
     }
     
     /**
      * @param args
      */
     public static void main(String[] args) throws Exception{
         // TODO Auto-generated method stub
         
         /*
         Set newset1 = new Set((new JSONArray("[{'_metadata':{'name':'viral reproduction','id':123012,'type':'set'},'_name':'mod6','_delim':'^','_active':1,'_elements':{'mod8':'','Oas1a':'','Banf1':'','mod7':'','mod3':'','Fv4':'','mod81':''}}]")).getJSONObject(0));
         Set newset2 = new Set((new JSONArray("[{'_metadata':{'name':'viral reproduction','id':123012,'type':'set'},'_name':'mod6','_delim':'^','_active':1,'_elements':{'mod1':'','Oas1a':'','Banf1':'','mod12':'','mod3':'','Fv4':'','mod81':''}}]")).getJSONObject(0));
 
         ArrayList<Set> sets = new ArrayList<Set>();
         sets.add(newset1);
         sets.add(newset2);
         
         SetEntityGrid heatmap = new SetEntityGrid(sets);
         heatmap.init();
         heatmap.buildGrid();
         heatmap.makeGif();
         
         */
         // read from standard inputs 
         
         
         ArrayList<Set> sets = new ArrayList<Set>();
         
         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
         String line;
        
         JSONClassifier classifier = new JSONClassifier();
         
         boolean foundInfo = false;
         while ((line = in.readLine()) != null && line.length() != 0) {
             try { 
                 JSONArray arr1 = new JSONArray(line);
                 JSONObject jsonObj = arr1.getJSONObject(0);
                 int jsonType = classifier.classify(jsonObj);
                 
                 switch (jsonType) {
             
                     case 1: 
                             sets.add(new Set(jsonObj));
                             break;
                     case 2: 
                             JSONObject data = jsonObj.getJSONObject("_metadata");
                             ACTION = data.getString("action");
                             if (ACTION.compareTo("gif") == 0 || ACTION.compareTo("base64gif") == 0) {
                                 FILENAME = data.getString("filename");
                                 INFO_FILE = FILENAME + ".json";
                                GRID_HEIGHT = data.getInt("height");
                                GRID_WIDTH = data.getInt("width");
                             }
                             if (ACTION.compareTo("zoom") == 0) {
                                 SUB_GRID = new Rectangle2D.Double(
                                         data.getDouble("xcoordinate"),
                                         data.getDouble("ycoordinate"),
                                         data.getDouble("width"),
                                         data.getDouble("height")
                                 );
                             }
                             foundInfo = true;
                             break;
                         
                 }
                 
             } catch (Exception e) {
                 System.out.println("Can't parse line:" + line);
                 e.printStackTrace();
             }
         }
             
         if (foundInfo == false) {
             throw new Exception("No metadata/info found for set of JSON strings!");        
         }
         
         SetEntityGrid heatmap = new SetEntityGrid(sets);
         heatmap.buildGrid();
         
         if (ACTION.compareTo("gif") == 0) {
             heatmap.makeGif();
         } else if (ACTION.compareTo("zoom") == 0) {
             String json = heatmap.getZoomJSON();
             System.out.print(json);
         } else if (ACTION.compareTo("base64gif") == 0) {
             heatmap.makeGifEncodeToString();
         }
         
     }
 }
