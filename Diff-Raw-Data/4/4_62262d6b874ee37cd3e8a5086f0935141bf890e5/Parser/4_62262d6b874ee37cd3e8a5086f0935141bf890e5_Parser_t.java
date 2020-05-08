 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.Buffer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Scanner;
 
 public class Parser {
     static ArrayList<Terminals> charClasses;
     static ArrayList<Terminals> tokenDefs;
     static ArrayList<Terminals> charsAndTokens;
     static NFA bigNFA;
     static DFA bigDFA;
     private static boolean debug = false;
     private static boolean dfa = false;
     private static boolean output = false;
     private static String dfaOutputFilename;
     private static String outputFilename;
 
     public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException{
         PrintStream out = new PrintStream(System.out, true, "UTF-8");
         System.setOut(out);
         if(args.length > 1 && args.length < 9 && args.length%2 == 0){
             System.out.println("HERE!");
             if(args[0].equals("-s") && args.length >= 2){
                 System.out.println("Found -s");
                 fileParser(args[1]);
             }
             if(argsContains(args, "-d") != -1 && args.length >= 4){
                 System.out.println("Found -d");
                 dfa = true;
                 int ind = argsContains(args, "-d");
                 dfaOutputFilename = args[ind++];
             }
             if(argsContains(args, "-o") != -1 && argsContains(args, "-i") != -1
                     && args.length >= 6){
                 System.out.println("Found -o && -i");
                 output = true;
                 outputFilename = args[argsContains(args, "-o")];
                 int ind = argsContains(args, "-i");
                 scannerDFA(args[ind++]);
             }
             else if(argsContains(args, "-i") != -1 && args.length >= 4){
                 System.out.println("Found -i");
                 int ind = argsContains(args, "-i");
                 scannerDFA(args[ind++]);
             }
             if(argsContains(args, "--debug") != -1 && args.length >= 3){
                 System.out.println("Found --debug");
                 debug = true;
             }
         }
         else{
             throw new RuntimeException("\nIncorrect arguments. Arguments must be: \n" +
             "-s <input_spec.txt> [-i <input.txt>] [-d <dfa.txt>] [-o <output.txt>] [--debug]");
         }
         //        PrintStream out = new PrintStream(System.out, true, "UTF-8");
         //        System.setOut(out);
         //                      fileParser("specs/ansic.txt");
         //                      scannerDFA("inputs/ansic.txt");
         //        System.out.println(bigDFA.toTableString(true));
     }
     
     public static int argsContains(String[] args, String str){
         for(int i=0; i<args.length; i++){
             if(args[i].equals(str)){
                 return i;
             }
         }
         return -1;
     }
 
     public static void scanner(String filename) throws FileNotFoundException {
         System.out.println("Scanning input file...");
         Scanner scan = new Scanner(new File(filename));
         while(scan.hasNextLine()){
             String line = scan.nextLine();
             while(!line.isEmpty()) {
                 line = line.trim();
 
                 ArrayList<ScanResult> results = new ArrayList<ScanResult>();
 
                 // Run all the DFAs on the current string
                 // See which one matches the farthest
                 // First defined token takes precedence
                 int maxPointer = 0;
                 Terminals maxKlass = null;
                 for (Terminals klass : tokenDefs) {
                     ScanResult result = klass.getDFA().walk(line);
                     results.add(result);
                     if (result.lastPointer > maxPointer) {
                         maxPointer = result.lastPointer;
                         maxKlass = klass;
                     }
                 }
 
                 // If nothing matched, invalid input!
                 if (maxPointer == 0) {
                     if(debug)
                         System.out.println("INVALID INPUT!");
                     return;
                 }
 
                 // Something matched!
                 String token = line.substring(0, maxPointer);
 
                 if(!output){
                     System.out.print(maxKlass.getName());
                     System.out.println(" "+token);
                 }
                 else{
                     File dfaFile = new File(outputFilename);
                     try {
                         FileWriter fw = new FileWriter(dfaFile);
                         BufferedWriter bw = new BufferedWriter(fw);
                         bw.write(bigDFA.toTableString(true));
                         bw.close();
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                 }
 
 
                 // Consume and start over!
                 line = line.substring(maxPointer);
             }
         }
     }
     public static void scannerDFA(String filename) throws FileNotFoundException {
         if(debug)
             System.out.println("Scanning input file wih big dfa...");
         Scanner scan = new Scanner(new File(filename));
         while(scan.hasNextLine()){
             String line = scan.nextLine();
             while(!line.isEmpty()) {
                 line = line.trim();
 
                 ArrayList<ScanResult> results = new ArrayList<ScanResult>();
 
                 ScanResult result = bigDFA.walk(line);
                 results.add(result);
 
                 // If nothing matched, invalid input!
                 if (result.lastPointer == 0) {
                     if(debug)
                         System.out.println("INVALID INPUT!");
                     return;
                 }
 
                 // Something matched!
                 String token = line.substring(0, result.lastPointer);
 
                 // Note: lastAccept.klass should NEVER be null!
                 if(output){
                     System.out.print(result.lastAccept.klass.getName());
                     System.out.println(" "+token);
                 }
 
                 // Consume and start over!
                 line = line.substring(result.lastPointer);
             }
         }
     }
 
     public static void fileParser(String filename) throws FileNotFoundException{
         if(debug)
             System.out.println("Parsing input spec...");
         Scanner scan = new Scanner(new File(filename));
         Terminals currClass = null;
         charClasses = new ArrayList<Terminals>();
         tokenDefs = new ArrayList<Terminals>();
         charsAndTokens = new ArrayList<Terminals>();
 
         // Which array are we filling?
         ArrayList<Terminals> classes = charClasses;
         while(scan.hasNextLine()){
             String line = scan.nextLine();
 
             // EOF or switch arrays?
             if (line.trim().isEmpty()) {
                 // Time for a switch?
                 if (classes == charClasses)
                     classes = tokenDefs;
                 continue;
             }
 
             Scanner lineScan = new Scanner(line);
 
             String token = lineScan.next();
 
             Terminals newClass = new Terminals();
             currClass = newClass;
             classes.add(currClass);
             charsAndTokens.add(currClass);
             currClass.setName(token.substring(1,token.length()));
             if(debug)
                 System.out.println(currClass.getName());
 
             token = line.substring(token.length());
 
             //token = token.replaceAll("\\s","");
             // Strip out leading space only
             token = token.replaceAll("^\\s*", "");
             if(debug)
                 System.out.println(token);
             RegExpFunc func = new RegExpFunc(token);
             NFA nfa = func.origRegExp(currClass.getName());
             currClass.setNFA(nfa);
             currClass.setDFA(nfa.toDFA());
             if(debug)
                 System.out.println("Parsing class "+currClass);
             nfa.setKlass(currClass);
 
             //System.out.println(currClass.getNFA());
             //System.out.println(currClass.getDFA());
         }
         State newStart = new State();
         //newStart.setLabel("Start");
         NFA newNfa = new NFA(newStart);
         //newNfa.setAccepts(newStart, false);
         for(Terminals each : tokenDefs){
             newNfa.addEpsilonTransition(newNfa.getStart(), each.getNFA().getStart());
         }
         bigNFA = newNfa;
         bigDFA = newNfa.toDFA();
         
         if(!dfa)
             System.out.println(bigDFA.toTableString(true));
         else{
             File dfaFile = new File(dfaOutputFilename);
             try {
                 FileWriter fw = new FileWriter(dfaFile);
                 BufferedWriter bw = new BufferedWriter(fw);
                 bw.write(bigDFA.toTableString(true));
                 bw.close();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }                        
         }
     }
     //  }
     /**
      * @return the classes
      */
     public static ArrayList<Terminals> getClasses() {
         return charsAndTokens;
     }
 
     public static ArrayList<Character> getIntervalOfChars(String inside){
         ArrayList<Character> list = new ArrayList<Character>();
         int index=0;
         char previousChar = inside.charAt(0);
         while(index<inside.length()){
             char c = inside.charAt(index);
             if(c == '-'){
                 previousChar++;
                 char currentChar = previousChar;
                 char endChar = inside.charAt(index+1);
                 while (currentChar < endChar){
                     list.add(currentChar);
                     currentChar++;
                 }
             }
             else{
                 list.add(c);
             }
             previousChar = c;
             index++;
         }
         return list;
     }
 
     public static HashSet<Character> getClass(String className){
         for(int i=0; i<charsAndTokens.size(); i++){
             if(charsAndTokens.get(i).getName().equals(className))
                 return charsAndTokens.get(i).getChars();
         }
         return null;
     }
 
     public static void setClass(String className, HashSet<Character> exclude) {
         for(int i=0; i<charsAndTokens.size(); i++){
             if(charsAndTokens.get(i).getName().equals(className))
                 charsAndTokens.get(i).setChars(exclude);
         }
     }
 }
