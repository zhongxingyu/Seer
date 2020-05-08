package assembler.TOASTY;

 import java.util.Scanner;
 import java.util.Iterator;
 import java.util.List;
 import java.util.LinkedList;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.BufferedWriter;
 import java.util.ArrayList;
 import java.util.Arrays; 
import wp.*;
 
 /**
  * Write a description of class ASMParser here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Assembler
 {
     // The delimiter used to separate instructions in the final code
     public static String SEP = "\n";
 
     private WPParser parser;
     
     // Assembled binary code(output)
     private List<String> binary;
 
     // Unassembled assembly code(input)
     private List<String> lines;
 
     private boolean assembled;
     
     /**
      * Create an assembly parser given a path to an assembly file
      * @param path The path to the file
      */
     public Assembler(String path)
     {
         this.lines = new LinkedList();
         
         try
         {
             Scanner scanner = new Scanner(new File(path));
 
             while (scanner.hasNextLine())
             {
                 lines.add(scanner.nextLine());
             }
         }
         catch (Throwable e)
         {
             System.out.println("404 File Not Found: " + path);
         }
     }
 
     /**
      * Do the assembling
      * The assembled program will be printed, and stored in the internal storage of this object
      * Any preprocessor directives will be kept track of past assembling this file
      */
     public boolean assemble()
     {   
         this.parser = new WPParser();
 
         // Format the file
         this.format(lines);
 
         // Handle preprocessor directives
         List<List<String>> parsed = new ArrayList();
         parsed = this.preprocess(lines);
 
         // Do the assembling
         List<Binary> insts = new ArrayList();
         
         try
         {       
             insts = this.generateInstructions(parsed);
         }
         catch (Throwable e)
         {
             return false;
         }
         
         // Format the final output
         this.binary = new ArrayList();
         this.binary = finalize(insts);
         
         for (String s : this.binary)
         {
             System.out.print(s);
         }
       
         System.out.println();
         System.out.println("ASSEMBLY COMPLETE");
         
         this.assembled = true;
         return true;
     }
     
     public void write(String path) throws IOException
     {
         assert this.assembled;
         
         FileWriter fstream = new FileWriter(path);
         BufferedWriter out = new BufferedWriter(fstream);
         
         for (String line : this.binary)
         {
             out.write(line);
         }
         
         out.close();
     }
         
     /**
      * Remove all newlines
      */
     private void format(List<String> lines)
     {
         Iterator<String> i = lines.iterator();
         String line;
         
         while (i.hasNext())
         {
             line = i.next().trim();
             if (line.length() == 0)
             {
                 i.remove();
             }
         }
         
     }
     
     private List<List<String>> preprocess(List<String> lines)
     {
         List<List<String>> processed = new ArrayList();
         
         for (String l : lines)
         {
             List<String> p = this.parseLine(l);
             for (int i = 1; i < p.size(); i++)
             {
                 p.set(i, p.get(i).replace("R", "0d"));
             }
             processed.add(p);
         }
         
         return processed;  
     }
     
     private List<Binary> generateInstructions(List<List<String>> lines) throws InvalidInputException
     {
         ArrayList<Binary> instructions = new ArrayList();
         
         for (List<String> parsed : lines)
         {
             WPChunk op = this.parser.getChunk(parsed.get(0));
             
             List<Binary> operands = this.makeBinariesFromOperands(parsed.subList(1, parsed.size()));
             
             instructions.add(op.generateInstruction(operands));
         }
         
         return instructions;    
     }
     
     /**
      * Add the preceding colons to every line of Intel Hex and convert the binary objects to Strings
      * @param 
      */
     private List<String> finalize(List<Binary> insts)
     {
         List<String> l = new ArrayList();
         
         // Split into 4 bit chunks for prettyness
         // Convert to strings for writing to a file
         
         for (Binary b : insts)
         {
             // Split into chunks of 4 with spaces
             // These are removed by the loader, but are put here for easy reading by humans
             String line = b.toBinaryString();
             for(int i = 4; i < line.length(); i += 5)
             {
                 line = new StringBuffer(line).insert(i, " ").toString();
             }
             l.add(line + Assembler.SEP);
         }
         
         return l;
     }
  
     public static List<String> parseLine(String line)
     {
         List<String> parsed = new ArrayList();
         
         for (String s : line.replace(",", " ").split(" "))
         {
             if (s.length() != 0)
             {
                 parsed.add(s);
             }
         }
        
         return parsed;
 
     }
     
     public List<Binary> makeBinariesFromOperands(List<String> ops)
     {
         List<Binary> bins = new ArrayList();
         
         for (String s : ops)
         {
             bins.add(new Binary(s));
         }
         
         return bins;
     }
     
     public static void test() throws IOException
     {
         Assembler test = new Assembler("./test.asm");
         
         test.assemble();
         test.write("./test.bin");
     }
 }
