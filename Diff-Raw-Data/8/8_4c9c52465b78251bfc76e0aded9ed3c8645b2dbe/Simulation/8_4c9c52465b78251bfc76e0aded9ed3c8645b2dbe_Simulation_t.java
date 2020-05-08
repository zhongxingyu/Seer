 import java.util.Scanner;
 import java.io.File;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.io.FileNotFoundException;
 
 public class Simulation {
     private Grid grid;
     private int stepNumber;
     private static Map<Character, String> symbolToClassnameMap;
     private static Map<String, Character> classnameToSymbolMap;
     
     public Simulation(int gridSize) {
         Debug.echo("Constructing a new Simulation object");
 
         parseSymbolMap();
         parseFoodWeb();
         parseGrid(new File("resources/maps/testMap/animals.dat"), new File("resources/maps/testMap/terrain.dat"));
         System.out.println(toString());
         //grid = new Grid(gridSize);
         stepNumber = 0;
     }
     public Simulation(File animals, File terrain){ 
         Debug.echo("Constructing a new Simulation object");
         
         parseSymbolMap();
         parseGrid(animals, terrain);
         parseFoodWeb();
         stepNumber = 0;
     }
 
     public void step() {
         Debug.echo("Here is where the simulation would step through each organism in the grid.");
     }
     public void parseSymbolMap() {
         symbolToClassnameMap = new HashMap<Character, String>();
         classnameToSymbolMap = new HashMap<String, Character>();
         try {
             Scanner scanner = new Scanner(new File("resources/symbols.dat"));
             Character symbol;
             String className, line;
             String[] words;
             while(scanner.hasNext()){
                 line = scanner.nextLine().trim();
                 words = line.split("\\s+");
                 symbol = (words[0].toCharArray())[0];
                 className = words[1];
                 symbolToClassnameMap.put(symbol, className);
                 classnameToSymbolMap.put(className, symbol);
             }
         } catch (FileNotFoundException e) {
             Debug.echo("SymbolMap: File not found!");
         } catch (Exception e) {
             Debug.echo("SymbolMap: Invalid file format! "+e);
         }
     }
     public void parseFoodWeb() {
         Debug.echo("Here is where I would parse the food web file");
 
         try {
             Scanner scanner = new Scanner(new File("resources/foodWeb.dat"));
             while (scanner.hasNextLine()) {
                 String line = scanner.nextLine().trim();
                 String[] contents = line.split(":");
 
                 // get name of predator
                 String predator = contents[0].trim();
                 // get names of prey
                 ArrayList<String> prey = new ArrayList<String>();
                String[] preys = contents[1].trim().split("\\s+");
                 for (int i = 0; i < preys.length; i++) {
                     prey.add(preys[i]);
                 }
                 for (String p : prey) {
                     if      (predator.equals("Rabbit")) Rabbit.addPrey(p);
                     else if (predator.equals("Fox"))    Fox.addPrey(p);
                     // Note: Plants don't need to know their predators
                     if      (p.equals("Rabbit")) Rabbit.addPredator(p);
                     else if (p.equals("Fox"))    Fox.addPredator(p);
                 }
             }
         } catch (FileNotFoundException e) {
             Debug.echo("FoodWeb: File not found!");
         } catch (Exception e) {
             Debug.echo("FoodWeb: Invalid file format! "+e);
         }
     }
     public void parseGrid(File animals, File terrain) {
         parseTerrain(terrain);
         parseAnimals(animals);
         Debug.echo("Parsing grid from file");
     }
     private void parseAnimals(File animals){
         try {
             Scanner scanner = new Scanner(animals);
             int gridSize = new Integer(scanner.nextLine());
             if(gridSize != grid.getGridSize()){
                 throw(new Exception("Grid sizes not equal."));
             }
             String line, className;
             int row = 0;
             int col = 0;
             Animal current;
             while (scanner.hasNext()) {
                 col = 0;
                 line = scanner.nextLine();
                 line = line.replaceAll("[^A-Za-z_]", "");
                 for (char symbol : line.toCharArray()){
                     className = symbolToClassnameMap.get(symbol);
                     if (className != null) {
                         // IF MORE ANIMALS ARE ADDED, ADD THEM HERE (IN AN ELSE IF)
                         if (className.equals("Rabbit")) {
                             current = new Rabbit();
                         } else if (className.equals("Fox")) {
                             current = new Fox();
                         } else {
                             current = null;
                         }
                         grid.addAnimal(current, row, col);
                     }
                     col++;
                 }
                 row++;
             }
         } catch (FileNotFoundException e) {
             Debug.echo("Animals: File not found!");
         } catch (Exception e) {
             Debug.echo("Animals: Invalid file format! "+e);
         }
     }
     private void parseTerrain(File terrain){
         try {
             Scanner scanner = new Scanner(terrain);
             int gridSize = new Integer(scanner.nextLine());
             grid = new Grid(gridSize);
             String line, className;
             int row = 0;
             int col = 0;
             Plant current;
             while (scanner.hasNext()) {
                 col = 0;
                 line = scanner.nextLine();
                 line = line.replaceAll("[^A-Za-z_]", "");
                 for (char symbol : line.toCharArray()){
                     className = symbolToClassnameMap.get(symbol);
                     if (className != null) {
                         // IF MORE PLANTS ARE ADDED, ADD THEM HERE (IN AN ELSE IF)
                         if (className.equals("Grass")) {
                             current = new Grass();
                         } else if (className.equals("Carrot")) {
                             current = new Carrot();
                         } else {
                             current = null;
                         }
                         grid.addPlant(current, row, col);
                     }
                     col++;
                 }
                 row++;
             }
         } catch (FileNotFoundException e) {
             Debug.echo("Terrain: File not found!");
         } catch (Exception e) {
             Debug.echo("Terrain: Invalid file format! "+e);
         }
     }
     public Grid getGrid() {
         return grid;
     }
     public static String symbolToClassname(Character symbol){
         return symbolToClassnameMap.get(symbol);
     }
     public static Character classnameToSymbol(String classname){
         return classnameToSymbolMap.get(classname);
     }
     public String toString() {
         String output = "";
         output+="SymbolMap="+symbolToClassnameMap.keySet().toString()+"\n"+symbolToClassnameMap.values().toString()+"\n";
         output+="Grid=\n"+grid.toString();
         return output;
     }
 }
