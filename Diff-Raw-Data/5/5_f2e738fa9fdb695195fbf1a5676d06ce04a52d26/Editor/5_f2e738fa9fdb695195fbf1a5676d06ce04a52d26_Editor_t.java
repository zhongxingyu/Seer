 package Final;
 
 import java.util.Scanner;
 
 public class Editor {
 
   //TODO: main loop
   //TODO: Implement RUN! Can't believe you forgot to do this...
   //      As far as implementing RUN goes, have a private method
   //      in here that handles the logic around IFs and LETs and
   //      whatever else. This means that this Class is the one that handles
   //      passing stuff to InfixConverter, which merely returns a double 
   //      representing the output of the line. I think that's all we need.
     public static void main(String[] args) {
     LinkedList lines = new LinkedList();
     Scanner in = new Scanner(System.in);
     Dictionary vars = new Dictionary();
 
     System.out.println("Welcome to the testing release of this BASIC interpreter!");
 
     while(true) {
       System.out.print("> ");
       String input = in.nextLine();
       String keyword = input.split(" ")[0].toUpperCase();
       
       if (keyword.equals("RESEQUENCE")) {
         lines.resequence();
       } else if (keyword.equals("LIST")) {
         System.out.println(lines);
       } else if (keyword.equals("ACCEPT")) {
           String var = input.split(" ")[1];
           System.out.print(var + " = ");
           String num = in.nextLine();
           vars.put(var, Double.parseDouble(num));
       } else if (keyword.equals("PRINT")) {
           String var = input.split(" ")[1];
           System.out.println(var + " = " + vars.get(var));
       } else if (keyword.equals("RUN")) {
           //TODO: make it do things
 
       } else {
         //System.out.println("default case");
         String[] tokens = input.split(" ", 2);
         try {
           Integer.parseInt(tokens[0]);
           lines.insert(input);                  //to insert a line
         } catch (NumberFormatException e) {
           if (tokens[0].equals("LET")) {
             String[] letTokens = tokens[1].split("=");
             System.out.println(java.util.Arrays.toString(letTokens));
             try {
              System.out.println(letTokens[1].replaceAll("\\s", ""));
               Double num = Double.parseDouble(letTokens[1].replaceAll("\\s", ""));
              System.out.println(num);
              vars.put(letTokens[0].replaceAll("\\s", ""), num);
             } catch (NumberFormatException e1) {
               System.out.println("Invalid input, try again.");
             }    
           } else {
             InfixConverter converter = new InfixConverter(input, vars);
             System.out.println(converter.convert());
           }
         }
       }
     }
   }
 }
