 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package magicitemgen;
 
 import java.util.*;
 import java.io.*;
 
 /**
  *
  * @author chris
  */
 public class MagicItemGen {
 
     private static Map<String, Table> map;
     
     public static void main(String args[]) {
         System.out.println("Welcome to Chris' Magic Item Generator!");
         
         map = new HashMap<String, Table>();
         
         readDatabase(new File("magic.txt"));
         
         generateItems();
         
         System.out.println("Thank you for using Chris' Magic Item Generator!");
         System.out.println("When you are done generating items, type 'exit' to quit");
         System.out.println("Type 'help' for more information\n");
     }
     
     public static Map getMap() {
         return map;
     }
     
    private static String readLine(Scanner scan) {
        System.out.print("$ ");
        return scan.nextLine();
    }
    
     private static void generateItems() {
         Scanner scan = new Scanner(System.in);
         String line="";
         
        commandReading: while(!(line = readLine(scan)).equals("exit")) {
         	
         	System.out.println("--------------------\n");
         	
             int size=0;
             boolean autoRand = true;
   		    boolean runTable = true;
             String table = "t1";
             int count = 1;
             
             if(!line.equals(""))
             for(String input:line.split(" ")) {
                 if(input.equals("0") || input.equals("minor") || input.equals("min")) {
                     size=0;
                 } else if(input.equals("1") || input.equals("medium") || input.equals("med")) {
                     size=1;
                 } else if(input.equals("2") || input.equals("major") || input.equals("maj")) {
                     size=2;
                 } else if(input.equals("manual") || input.equals("man")) {
                     autoRand = false;
                 } else if(input.equals("auto")) {
                     autoRand = true;
                 } else if(input.equals("help")) {
                     System.out.println("Command listing:");
                     System.out.println("exit - quit the program");
                     System.out.println("help - get command listing");
                     System.out.println("list - list available tables");
                     System.out.println("show @TABLE_NAME - list the contents of a table");
                     System.out.println("count=X - generate X items with the same parameters");
                     System.out.println();
                     System.out.println("Usage parameters:");
                     System.out.println("@TABLE_NAME        - the table to run");
                     System.out.println("auto               - sets the number generation to automatic; this is the default");
                     System.out.println("manual             - sets the number generation to manual");
                     System.out.println("minor    (min)     - sets the item to be a minor; this is the default");
                     System.out.println("medium    (med)    - sets the item to be a medium");
                     System.out.println("major    (maj)     - sets the tiem to be a major");
                     System.out.println();
                     
                     continue commandReading;
                 } else if(input.equals("list")) {
                     Iterator it = map.entrySet().iterator();
                     
                     while(it.hasNext()) {
                         Map.Entry pairs = (Map.Entry)it.next();
                         System.out.println(pairs.getValue());
                     }
                     
                     System.out.println();
                     
                     continue commandReading;
                 } else if(input.equals("show")) {
                         runTable = false;
                 } else if(input.charAt(0) == '@') {
                     table = input.substring(1);
                 } else if(input.split("=")[0].equals("count")) {
                     count = Integer.parseInt(input.split("=")[1]);
                 }
             }
             
             if(runTable)
                 for(int i=0;i<count;i++) {
                 	if(count > 1) System.out.print((i+1)+". ");
                     if(i>0) System.out.println("--------------------\n");
                     map.get(table).runTable(size,autoRand);
                 }
             else {
                     map.get(table).showTable();
                     System.out.println();
             }
         }
     }
     
     private static void readDatabase(File file) {
         try{
 			InputStream is = MagicItemGen.class.getResourceAsStream("/magic.txt");
 			InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);
             String line;
             Table table = new Table(null,null);
             int state=0;
             String entry;
             while ((line = br.readLine()) != null) {
                 
                 //System.out.println(line);
                 
                 switch(state) {
                     case 0:    // No current table
                         if(line.indexOf(":")>=0) {
                             table = new Table(line.split(":")[0],line.split(":")[1].trim());
                             state = 1;
                         }
                         break;
                     case 1:    // First line of new table
                         table.setHeader(line);
                         state = 2;
                         break;
                     case 2:    // Table entries
                         if(line.equals("")) {
                             state = 3;
                             break;
                         }
                         String split[] = line.split(" ");
                         int probs[] = new int[3];
                         
                         int probCount = 1;
                         if(isProb(split[1])) {
                             probCount++;
                             if(isProb(split[2])) {
                                 probCount++;
                             }
                         }
                         
                         entry = line;
                         
                         if(probCount == 1) {
                             probs[0] = parseProb(split[0]);
                             probs[1] = parseProb(split[0]);
                             probs[2] = parseProb(split[0]);
                             
                             entry = entry.substring(entry.indexOf(' ')+1);
                         } else if(probCount == 2) {
                             probs[0] = -1;
                             probs[1] = parseProb(split[0]);
                             probs[2] = parseProb(split[1]);
                             
                             entry = entry.substring(entry.indexOf(' ')+1);
                             entry = entry.substring(entry.indexOf(' ')+1);
                         } else if(probCount == 3) {
                             probs[0] = parseProb(split[0]);
                             probs[1] = parseProb(split[1]);
                             probs[2] = parseProb(split[2]);
                             
                             entry = entry.substring(entry.indexOf(' ')+1);
                             entry = entry.substring(entry.indexOf(' ')+1);
                             entry = entry.substring(entry.indexOf(' ')+1);
                         }
                         
                         //System.out.println(probs[0]+" "+probs[1]+" "+probs[2]);
                         table.addEntry(probs,entry);
                         break;
                     case 3:    // Table is complete
                         map.put(table.getName(),table);
                         state = 0;
                         if(line.indexOf(":")>=0) {
                             table = new Table(line.split(":")[0],line.split(":")[1].trim());
                             state = 1;
                         }
                         break;
                 }
             }
             br.close();
         } catch(IOException e) {
             System.err.println(e.getMessage());
         }
     }
     
     private static boolean isProb(String s) {
         try {
             if(s.indexOf('-') < 0) {
                 Integer.parseInt(s);
                 return true;
             }
             if(s.equals("-")) return true;
             
             Integer.parseInt(s.split("-")[0]);
             Integer.parseInt(s.split("-")[1]);
             
             return true;
         } catch(NumberFormatException nfe) {
             return false;
         }
     }
     
     private static int parseProb(String s) {
         //System.out.println(s);
         if(s.equals("-")) return -1;
         if(s.indexOf('-') >= 0) {
             s = s.substring(s.indexOf('-')+1);
         }
         return Integer.parseInt(s);
     }
 }
