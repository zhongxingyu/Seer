 package edu.puc.concurrentavl;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.*;
 public class Menu {
 
 
 	public static void main(String[] args) throws InterruptedException {
 		// TODO Auto-generated method stub
 		
 		int[] values = generateValues();
 		
 		
 		PrintStream ps;
 		try {
 			RedBlackTree tree = runRedBlack(values);
 			JavaRedBlack<Integer, Integer> jrb = runTreeMap(values);
 
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	
 	}
 	
 	public static RedBlackTree runRedBlack(int [] values) throws FileNotFoundException {
 
 
 
 
         RedBlackTree tree = new RedBlackTree();
 		for(int i = 0; i < values.length; ++i) {
 
             File f = new File("output");
             if(f.exists()) {
                 f.delete();
             }
 
             PrintStream ps = new PrintStream(new File("output"));
             String op = "";
             if(values[i] < 0)     {
                 op = "DELETE " + -values[i];
                 tree.delete(-values[i]);
             }
             else {
                 op = "INSERT " + values[i];
 			    tree.insert(values[i]);
             }
 			tree.print(ps, 5);
 
             String[] cmd = { "/usr/bin/python", "transposer.py", op };
 
             try {
                 Process p = Runtime.getRuntime().exec(cmd);
                 try {
                     p.waitFor();
                 } catch (InterruptedException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
 
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             ps.close();
 		}
 
 		return tree;
 	}
 	
 	public static JavaRedBlack<Integer, Integer> runTreeMap(int [] values) {
 		JavaRedBlack<Integer, Integer> map = new JavaRedBlack<Integer, Integer>();
 		for(int i = 0; i < values.length; ++i) {
             if(values[i] < 0)
                 map.remove(-values[i]);
             else
                 map.put(values[i], values[i]);
 
 		}
 		return map;
 		
 	}
 	
 	public static int[] generateValues() {
		int [] values = {1, 2, 3, 4, 5, 6, 7, 800, 799, 801, 802, 740, 10, 8};
 		return values;
 	}
 
 }
 
