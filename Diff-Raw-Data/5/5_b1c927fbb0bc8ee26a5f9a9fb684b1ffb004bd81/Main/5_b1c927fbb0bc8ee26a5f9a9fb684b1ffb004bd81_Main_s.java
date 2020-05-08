 package edu.columbia.stat.wood.pdia;
 
 import edu.columbia.stat.wood.hpyp.Util;
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.zip.GZIPOutputStream;
 
 public class Main {
 
     public static void main(String[] args)
             throws FileNotFoundException, IOException, ClassNotFoundException {
         File train = new File("/Users/davidpfau/Documents/Wood Group/aiw/aiw.train");
         File test = new File("/Users/davidpfau/Documents/Wood Group/aiw/aiw.test");
         File objs = new File("/Users/davidpfau/Documents/Wood Group/PDIA/results/objectsFromPDIA_hpy.txt.gz");
         File output = new File("/Users/davidpfau/Documents/Wood Group/PDIA/results/predictiveValuesAliceInWonderland_hpy.txt");
 
         int samples = 10000;
 
         BufferedInputStream bis = null;
         ObjectOutputStream oos = null;
         PrintStream ps = null;
         int[] symbols;
         int[][] symbolLines;
         HashMap<Integer, Integer> alphabet = new HashMap<Integer, Integer>(28);
         alphabet.put(10, -1);
         try {
             bis = new BufferedInputStream(new FileInputStream(train));
 
             symbols = new int[(int) train.length()];
 
             int ind = 0;
             int b;
             while ((b = bis.read()) > -1) {
                 Integer c = alphabet.get(b);
                 if (c != null) {
                     symbols[(ind++)] = c;
                 } else {
                     symbols[(ind++)] = alphabet.size() - 1;
                     alphabet.put(b, alphabet.size() - 1);
                 }
             }
 
             symbolLines = new int[99][];
             int i = 0;
             int line = 0;
             for (int j = 0; j < symbols.length; j++) {
                 if (symbols[j] == -1) {
                     symbolLines[line] = new int[j - i];
                     System.arraycopy(symbols, i, symbolLines[line], 0, j - i);
                     i = j + 1;
                     line++;
                 }
             }
         } finally {
             if (bis != null) {
                 bis.close();
             }
         }
         int[] testSymbols;
         int[][] testSymbolLines;
         try {
             bis = new BufferedInputStream(new FileInputStream(test));
             testSymbols = new int[(int) test.length()];
 
             int ind = 0;
             int b;
             while ((b = bis.read()) > -1) {
                 testSymbols[(ind++)] = alphabet.get(b);
             }
             testSymbolLines = new int[49][];
             int i = 0;
             int line = 0;
             for (int j = 0; j < testSymbols.length; j++) {
                 if (testSymbols[j] == -1) {
                     testSymbolLines[line] = new int[j - i];
                     System.arraycopy(testSymbols, i, testSymbolLines[line], 0, j - i);
                     i = j + 1;
                     line++;
                 }
             }
         } finally {
             if (bis != null) {
                 bis.close();
             }
 
         }
 
         PDIA pdia = new PDIA(27, symbolLines);
         try {
             oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(objs)));
             ps = new PrintStream(new FileOutputStream(output));
 
             for (int s = 0; s < samples; s++) {
                 for (int j = 0; j < 10; j++) {
                     pdia.sample();
                 }
 
                 int testLen = 0;
                 for (int i = 0; i < testSymbolLines.length; i++) {
                     if (testSymbolLines[i] != null) {
                         testLen += testSymbolLines[i].length;
                     }
                 }
                 double[] score = new double[testLen];
                 for (int j = 0; j < 10; j++) {
                     PDIA pdiaTest = Util.copy(pdia);
                    addArrays(score, pdiaTest.score(testSymbolLines, pdiaTest.endState()));
                 }
 
                 for (int j = 0; j < score.length; j++) {
                    score[j] /= 10.0D;
                 }
 
                 oos.writeObject(pdia.beta);
                 oos.writeObject(pdia.dMatrix);
                 oos.writeObject(pdia.rf);
 
                 ps.print(pdia.beta.doubleVal() + ", " + pdia.states() + ", " + pdia.jointScore());
                 ps.print(score[0]);
                 for (int j = 1; j < score.length; j++) {
                     ps.print(",");
                     ps.print(score[j]);
                 }
                 ps.println();
 
                 System.out.println("Iteration = " + s + " : SingleMachinePrediction = " + summarizeScore(score));
             }
         } finally {
             if (oos != null) {
                 oos.close();
             }
             if (ps != null) {
                 ps.close();
             }
         }
     }
 
     public static double summarizeScore(double[] score) {
         double logLik = 0.0D;
         for (double p : score) {
             logLik -= Math.log(p) / Math.log(2.0D);
         }
         logLik /= score.length;
 
         return logLik;
     }
 
     public static void addArrays(double[] base, double[] other) {
         assert (base.length == other.length);
         for (int i = 0; i < base.length; i++) {
             base[i] += other[i];
         }
     }
 }
