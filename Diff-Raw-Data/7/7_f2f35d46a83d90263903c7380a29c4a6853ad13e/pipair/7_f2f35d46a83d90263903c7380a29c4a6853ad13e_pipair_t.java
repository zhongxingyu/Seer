 package fauna.testing;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Collections;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.Map;
 import java.io.InputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.text.NumberFormat;
 import java.math.RoundingMode;
 
 class Pipair {
 
     int tSupport = 3;
     float tConfidence = 0.65f;
     NumberFormat numf = NumberFormat.getNumberInstance();
 
     public static String getPairName(String a, String b) {
         if (a.compareTo(b) > 0) {
             String temp = a;
             a = b;
             b = temp;
         }
         return a + ":" + b;
     }
 
     class SupportGraph {
         Hashtable<String,Integer> supports = new Hashtable<String,Integer>();
         HashSet<String> allNames = new HashSet<String>();
 
         private void parseFromCallGraph(Hashtable<String,ArrayList<String>> cg) {
             Enumeration funcs = cg.elements();
             while (funcs.hasMoreElements()) {
                 ArrayList<String> calls = (ArrayList<String>)funcs.nextElement();
                 calls = removeDuplicateCalls(calls);
 
                 for (int i = 0; i < calls.size(); i++) {
                     allNames.add(calls.get(i));
                     for (int j = i + 1; j < calls.size(); j++) {
                         String name = Pipair.getPairName(calls.get(i),
                                                          calls.get(j));
                         createOrIncrementSupport(name);
                     }
                     createOrIncrementSupport(calls.get(i));
                 }
             }
         }
 
         private ArrayList<String> removeDuplicateCalls(ArrayList<String> calls) {
             HashSet<String> callSet = new HashSet<String>(calls);
             calls = new ArrayList<String>(callSet);
             return calls;
         }
 
         private void createOrIncrementSupport(String name) {
             Integer curSing = supports.get(name);
             if (curSing == null) {
                 supports.put(name, 1);
             } else {
                 supports.put(name, new Integer(curSing + 1));
             }
         }
     }
 
     public void findAndPrintViolations(Hashtable<String,ArrayList<String>> cg,
                                        SupportGraph sg) {
         Enumeration<String> cgKeySet = cg.keys();
         while (cgKeySet.hasMoreElements()) {
             String caller = (String)cgKeySet.nextElement();
             ArrayList<String> callsL = (ArrayList<String>)cg.get(caller);
             HashSet<String> calls = new HashSet<String>(callsL);
 
             Iterator i = calls.iterator();
             while (i.hasNext()) {
                 String f = (String)i.next();
                 printInvariantsForFunction(caller, f, sg, calls);
             }
         }
     }
 
     private void printInvariantsForFunction(String caller,
                                             String f1,
                                             SupportGraph sg,
                                             HashSet<String> calls) {
         Iterator<String> i = sg.allNames.iterator();
         while (i.hasNext()) {
             String f2 = i.next();
             String key = Pipair.getPairName(f1, f2);
             
            if (!sg.supports.containsKey(key) ||
                !sg.supports.containsKey(f1)) {
                 continue;
             }
 
             int pairSupport = sg.supports.get(key).intValue();
             int singleSupport = sg.supports.get(f1).intValue();
             float confidence = (float)pairSupport/singleSupport;
 
            if (confidence >= tConfidence && pairSupport >= tSupport) {
                 if (!calls.contains(f2)) {
                     printViolation(caller, f1, f2, pairSupport,
                                    confidence);
                 }
             }
         }
     }
     
     public void printViolation(String caller, String f1, String f2,
                                int support, float confidence) {
         System.out.println("bug: " + f1 + " in " + caller + ", " +
                            "pair: (" +
                            f1 + " " + f2 + "), support: " +
                            support + ", confidence: " +
                            numf.format(confidence * 100.0) + "%");
     }
 
     public void run(String cgFile) {
         numf.setMaximumFractionDigits(2);
         numf.setMinimumFractionDigits(2);
         numf.setRoundingMode(RoundingMode.HALF_EVEN);
 
         Hashtable<String,ArrayList<String>> cg = Parser.parseFile(cgFile);
         SupportGraph sg = new SupportGraph();
         sg.parseFromCallGraph(cg);
         findAndPrintViolations(cg, sg);
     }
 
     public static void main(String[] args) {
         if (args.length < 1) {
             System.out.println("Usage: ./pipair <bitcode file> <T SUPPORT> <T CONFIDENCE>,");
             System.exit(0);
         }
 
         Pipair prog = new Pipair();
         if (args.length >= 2) {
             prog.tSupport = Integer.parseInt(args[1]);
         }
         if (args.length >= 3) {
             prog.tConfidence = Float.parseFloat(args[2]);
         }
         if (args.length >= 4) {
             Parser.levels = Integer.parseInt(args[3]);
         }
         prog.run(args[0]);
     }
 }
