 package ca.uqam.mgl7361.a2011.gamma;
 
 import java.util.ArrayList;
 import java.io.*;
 
 public class TestCases
 {
     private int testBad;
     private ArrayList<TestCase> listTestCase;
     private String traceFilename;
     
    TestCases()
     {
         testBad = 0;
         listTestCase = new ArrayList<TestCase>();
         traceFilename = "c://traceGamma.txt";
     }
     
     TestCases(String filename)
     {
         testBad = 0;
         listTestCase = new ArrayList<TestCase>();
         traceFilename = filename;
     }
     
     public void addTestCase(Object a, Object b)
     {
         listTestCase.add(new TestCase(a, b));
     }
 
     public void executeTestCase()
     {
         String text;
         
         try
         {
             Trace trace = new Trace(traceFilename);
             trace.addLine("Début des cas de tests :");
         
             for (TestCase test : listTestCase)
             {
                 text = " - Comparaison entre " + test.getObjectA() + " et " + test.getObjectB();
                 if (test.assertEquals() == false)
                 {
                     testBad++;
                     trace.addLine(text + " ... Erreur");
                 }
                 else
                 {
                     trace.addLine(text + " ... OK");
                 }
             }
             
             trace.addLine(listTestCase.size() + " test(s) effectué(s), " + testBad + " test(s) échoué(s)");
             trace.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 }
