 import java.util.Scanner;
 import java.io.File;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.io.FileNotFoundException;
 
 public class Simulation {
     private Grid grid;
     private int stepNumber;
     private Map<String, String> symbolMap;
     
     public Simulation(int gridSize) {
         Debug.echo("Constructing a new Simulation object");
 
         parseSymbolMap();
         System.out.println(symbolMap.keySet()+"\n"+symbolMap.values());
         grid = new Grid(gridSize);
         stepNumber = 0;
     }
     public Simulation(File animals, File terrain, File foodweb){ 
         Debug.echo("Constructing a new Simulation object");
         
         parseSymbolMap();
         parseGrid(animals, terrain);
         parseFoodWeb(foodweb);
         stepNumber = 0;
     }
 
     public void step() {
         Debug.echo("Here is where the simulation would step through each organism in the grid.");
     }
     public void parseSymbolMap() {
         symbolMap = new HashMap<String, String>();
         try {
             Scanner scanner = new Scanner(new File("resources/symbols.dat"));
             String symbol, className, line;
             String[] words;
             while(scanner.hasNext()){
                 line = scanner.nextLine().trim();
                 words = line.split("\\s+");
                 symbol = words[0];
                 className = words[1];
                 symbolMap.put(symbol, className);
             }
         } catch (FileNotFoundException e) {
             Debug.echo("SymbolMap: File not found!");
         } catch (Exception e) {
             Debug.echo("SymbolMap: Invalid file format!");
         }
     }
     public void parseFoodWeb(File file) {
         Debug.echo("Here is where I would parse the food web file");
 
         try {
             Scanner scanner = new Scanner(file);
             while (scanner.hasNextLine()) {
                 String line = scanner.nextLine().trim();
                 String[] contents = line.split(":");
 
                 // get name of predator
                 String predator = contents[0].trim();
 
                 // get names of prey
                 ArrayList<String> prey = new ArrayList<String>();
                for (int i = 1; i < contents.length; i++) {
                    prey.add(contents[i].trim());
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
         
         }
     }
     public void parseGrid(File animals, File terrain) {
         //grid = new Grid(gridFilename);
         Debug.echo("Parsing grid from file");
     }
     public Grid getGrid() {
         return grid;
     }
 }
