 package edu.macalester.cs124.oopexperiments;
 
 import acm.program.ConsoleProgram;
 
 public class ExperimentRunner extends ConsoleProgram {
     public void run() {
     }
 
     public void runLocalVarExperiment() {
          HappyCounter c = new HappyCounter();
          println(c.incrementVariables());
          println(c.incrementVariables());
          println(c.incrementVariables());
     }
 
     public void runStaticVarExperiment() {
         FunkyCounter eenie = new FunkyCounter();
         FunkyCounter meenie = new FunkyCounter();
         
         println("Before incrementing:");
         println("  eenie.x is " + eenie.getX());
         println("  eenie.y is " + eenie.getY());
         println("  meenie.x is " + meenie.getX());
         println("  meenie.y is " + meenie.getY());
         
         eenie.incrementVariables();
         meenie.incrementVariables();
         eenie.incrementVariables();
         eenie.incrementVariables();
         meenie.incrementVariables();
         
         println("After incrementing:");
         println("  eenie.x is " + eenie.getX());
         println("  eenie.y is " + eenie.getY());
         println("  meenie.x is " + meenie.getX());
         println("  meenie.y is " + meenie.getY());
     }
     
     public void runNullExperiment1() {
         //FunkyCounter a = null; 
         //int b = null;
         //String c = null;
         //CardSuit d = null;
     }
     
     public void runNullExperiment2() {
//        FunkyCounter funkyLocalVar = null; 
//        funkyLocalVar.incrementVariables();
     }
     
     private FunkyCounter funkyInstanceVar;
     public void runNullExperiment3() {
         funkyInstanceVar.incrementVariables();
     }
     
     public void runEnumExperiment() {
         CardSuit s = CardSuit.SPADES;
         println(s.name());
        // Type "s." right here, and see what Eclipse suggests
     }
 }
