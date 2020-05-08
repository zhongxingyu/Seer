     package com.mycompany.testproject.iterativeTests;
 
 import fi.lolcatz.profiler.ClassBlacklist;
 import fi.lolcatz.profiler.ComplexityAnalysis;
 import fi.lolcatz.profiler.Output;
 import fi.lolcatz.profiler.Util;
 import java.util.Arrays;
 import java.util.List;
 import org.apache.log4j.Logger;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class ComplexityExponentialsTest {
 
     public ComplexityExponentialsTest() {
     }
     
     IntegerImpl impl;
     ComplexityAnalysis framework;
     
     @BeforeClass
     public static void setUpClass() {
         ClassBlacklist.add(ComplexityExponentialsTest.class);
         Util.loadAgent();
         Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
     }
 
     @Before
     public void testSetup() {
         impl = new IntegerImpl();
         impl.setClassName("com.mycompany.testproject.iteratives.IterativeComplexityExample");
     }
 
     
     @Test
     public void squaredTest() throws Exception {
         
         impl.setMethodName("squaredFunction");
         
         long[] totalCost = new long[5];
         int syote = 10;
         
         totalCost[0] = impl.runStatic(impl.getInput(syote));
         
         for (int i = 1; i < totalCost.length; i++) {
             syote = 2*syote;
             totalCost[i] = impl.runStatic(impl.getInput(syote));    
         }
         
         printResults("--- testSquared ---", totalCost);
 
         assertTrue(totalCost[0] * 4 >= totalCost[1]);
         assertTrue(totalCost[1] * 4 >= totalCost[2]);
         assertTrue(totalCost[2] * 4 >= totalCost[3]);
         assertTrue(totalCost[3] * 4 >= totalCost[4]);
         System.out.println("- - - - -");
 
     }
 
     @Test
     public void testApproximatedSquared() throws Exception {
         
         impl.setMethodName("approximatedSquaredFunction");
         
         long[] totalCost = new long[5];
 
         int syote = 10;
         
         totalCost[0] = impl.runStatic(impl.getInput(syote));
         
         for (int i = 1; i < totalCost.length; i++) {
             syote = 2*syote;
             totalCost[i] = impl.runStatic(impl.getInput(syote));    
         }
         
         printResults("--- testApproximatedSquared ---", totalCost);
         assertTrue(totalCost[0] * 4 >= totalCost[1]);
         assertTrue(totalCost[1] * 4 >= totalCost[2]);
         assertTrue(totalCost[2] * 4 >= totalCost[3]);
         assertTrue(totalCost[3] * 4 >= totalCost[4]);
 
         
 
     }
 
     @Test
     public void testCoinFlipExponential() throws Exception {     
         impl.setMethodName("squaredCoinFlipFunction");
         
         long[] totalCost = new long[5];
         int syote = 10;
         
         totalCost[0] = impl.runStatic(impl.getInput(syote));
         
         for (int i = 1; i < totalCost.length; i++) {
             syote = 2*syote;
             totalCost[i] = impl.runStatic(impl.getInput(syote));    
         }
 
 
         printResults("--- testCoinFlipSquared ---", totalCost);
         
         assertTrue(totalCost[0] * 4 >= totalCost[1]);
         assertTrue(totalCost[1] * 4 >= totalCost[2]);
         assertTrue(totalCost[2] * 4 >= totalCost[3]);
         assertTrue(totalCost[3] * 4 >= totalCost[4]);
 
         System.out.println("- - - - -");
     }
     
     @Test
     public void testGenerateOutput() throws Exception {     
         impl.setMethodName("approximatedSquaredFunction");
         List<Integer> list = Arrays.asList(2,4,8,16,32,64,512,1024,2048,4096,8192/*,16384, 32768, 65536, 131072, 262144*/);
         Output<Integer> o = impl.runMethod(list);
         int i = 0;
         for (Long l : o.getTime()){
 //            System.out.println("Size: " + o.getSize().get(i) + " Time: " + l);
 //            i++;
             assertTrue(l > 0);
         }
         framework.drawGraph(o, o);
        assertTrue(framework.isSquared(o));     
     }
     
 //    @Test
 //    public void testGenerateOutputNlogN() throws Exception {     
 //        impl.setMethodName("approximatedNlogNFunction");
 //        List<Integer> list = Arrays.asList(2,4,8,16,32,64,512/*,1024,2048,4096,8192,16384*/);
 //        Output<Integer> o = impl.generateOutput(list);
 //        int i = 0;
 //        for (Long l : o.getTime()){
 //            System.out.println("Size: " + o.getSize().get(i) + " Time: " + l);
 //            i++;
 //            assertTrue(l > 0);
 //        }
 //        impl.drawGraph(o, o);
 //        assertTrue(framework.isNlogN(o));     
 //    }
     
     public void printResults(String testname, long[] results) {
         System.out.println("---" + testname + "---");
         int i = 0;
         for (long l : results) {
             i++;
             System.out.println(i + ": " + l);
         }
     }
 }
