 package ca.uqam.mgl7361.a2011.gamma.test;
 
import ca.uqam.mgl7361.a2011.gamma.*;
 
 public class TestMain
 {
     public static void main(String[] args)
     {
         TestCases t = new TestCases();
     
         t.addTestCase(6 + 4, 10);
         t.addTestCase("bleu","rouge");
         t.addTestCase("rouge","rouge");
         t.addTestCase(3.4,3.40);
         t.addTestCase(12.75,12.85);
         t.executeTestCase();
     }
 }
