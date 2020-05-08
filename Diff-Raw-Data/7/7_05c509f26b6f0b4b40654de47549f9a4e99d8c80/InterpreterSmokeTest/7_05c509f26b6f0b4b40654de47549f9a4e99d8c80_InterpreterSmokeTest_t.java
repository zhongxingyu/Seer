 package edu.osu.cse.meisam.interpreter;
 
 import junit.framework.TestCase;
 
 public class InterpreterSmokeTest extends TestCase {
 
     public void testNumericAtom() {
         final StringInputProvider in = new StringInputProvider("5");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
     }
 
    public void testSampleExpresion() {
        final StringInputProvider in = new StringInputProvider(
                "(Plus (Minus 4 10) (TIMES 4 5)) 3");
        final Interpreter interpreter = new Interpreter(in, System.out);
        interpreter.interpret();
    }

     public void testInterpret() {
         final Interpreter interpreter = new Interpreter(
                 new InputStreamProvider(System.in), System.out);
         interpreter.interpret();
     }
 
 }
