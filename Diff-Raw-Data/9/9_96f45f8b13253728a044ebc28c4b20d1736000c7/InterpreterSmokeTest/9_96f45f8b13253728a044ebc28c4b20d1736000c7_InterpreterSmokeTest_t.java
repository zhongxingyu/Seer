 package edu.osu.cse.meisam.interpreter;
 
 import junit.framework.TestCase;
 
 public class InterpreterSmokeTest extends TestCase {
 
     public void testNumericAtom() {
         System.out.println("InterpreterSmokeTest.testNumericAtom()");
         final StringInputProvider in = new StringInputProvider("5");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testSampleExpresion() {
         System.out.println("InterpreterSmokeTest.testSampleExpresion()");
         final StringInputProvider in = new StringInputProvider(
                 "(Plus (Minus 4 10) (TIMES 4 5)) 3");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testPlusExpresion() {
         System.out.println("InterpreterSmokeTest.testPlusExpresion()");
         final StringInputProvider in = new StringInputProvider("(Plus 3 4)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testMinusExpresion() {
         System.out.println("InterpreterSmokeTest.testMinusExpresion()");
         final StringInputProvider in = new StringInputProvider("(mInuS 3 4)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testRemindreExpresion() {
         System.out.println("InterpreterSmokeTest.testRemindreExpresion()");
         final StringInputProvider in = new StringInputProvider(
                 "(REMAINDER 3 4)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testQuotientExpresion() {
         System.out.println("InterpreterSmokeTest.testQuotientExpresion()");
         final StringInputProvider in = new StringInputProvider("(QUOTIENT 3 4)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testTimesExpresion() {
         System.out.println("InterpreterSmokeTest.testTimesExpresion()");
         final StringInputProvider in = new StringInputProvider("(Times 3 4)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testEqExpresion() {
         System.out.println("InterpreterSmokeTest.testEqExpresion()");
         final StringInputProvider in = new StringInputProvider("(EQ 3 4)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testEqExpresion2() {
         System.out.println("InterpreterSmokeTest2.testEqExpresion()");
         final StringInputProvider in = new StringInputProvider("(EQ a b)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testEqExpresion3() {
         System.out.println("InterpreterSmokeTest3.testEqExpresion()");
         final StringInputProvider in = new StringInputProvider("(EQ a a)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testEqExpresion4() {
         System.out.println("InterpreterSmokeTest4.testEqExpresion()");
         final StringInputProvider in = new StringInputProvider("(EQ T T)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testLessExpresion() {
         System.out.println("InterpreterSmokeTest.testTimesExpresion()");
         final StringInputProvider in = new StringInputProvider("(LESS 3 4)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testGreaterExpresion() {
         System.out.println("InterpreterSmokeTest.testTimesExpresion()");
         final StringInputProvider in = new StringInputProvider("(Greater 3 4)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testTExpresion() {
         System.out.println("InterpreterSmokeTest.testTExpresion()");
         final StringInputProvider in = new StringInputProvider("T");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testComplicatedExpresion() {
         System.out.println("InterpreterSmokeTest.testComplicatedExpresion()");
         final StringInputProvider in = new StringInputProvider(
                 "(GREATER (PLUS 4 5) (MINUS (TIMES 100 10) (REMAINDER 10 7)))");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testAtomExpresion() {
         System.out.println("InterpreterSmokeTest.testAtomExpresion()");
         final StringInputProvider in = new StringInputProvider("(ATOM 5)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testNullExpresion() {
         System.out.println("InterpreterSmokeTest.testNullExpresion()");
         final StringInputProvider in = new StringInputProvider(
                 "(NULL (GREATER 4 1))");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testIntExpresion() {
         System.out.println("InterpreterSmokeTest.testIntExpresion()");
         final StringInputProvider in = new StringInputProvider(
                 "(Int (PLUS 4 1))");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testNilExpresion() {
         System.out.println("InterpreterSmokeTest.testNilExpresion()");
         final StringInputProvider in = new StringInputProvider("NIL");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testCondExpresion() {
         System.out.println("InterpreterSmokeTest.testCondExpresion()");
         final StringInputProvider in = new StringInputProvider(
                 "(COND ((LESS 4 3) 2) ((LESS 5 7) 9))");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testQouteExpresion() {
         System.out.println("InterpreterSmokeTest.testQouteExpresion()");
         final StringInputProvider in = new StringInputProvider("(Quote (1 2)");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testConsExpresion() {
         System.out.println("InterpreterSmokeTest.testQouteExpresion()");
         final StringInputProvider in = new StringInputProvider(
                 "(Cons 3 (Cons 5 7))");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
     public void testDefunExpresion() {
         System.out.println("InterpreterSmokeTest.testDefunExpresion()");
         final StringInputProvider in = new StringInputProvider(
                 "(Defun x (a b) (PLUS 5 4))");
         final Interpreter interpreter = new Interpreter(in, System.out);
         interpreter.interpret();
         System.out.println();
     }
 
    public void testApplyFunExpresion() {
        System.out.println("InterpreterSmokeTest.testDefunExpresion()");
        final StringInputProvider in = new StringInputProvider(
                "(Defun x (a b) (PLUS a 4)) (x 6 7)");
        final Interpreter interpreter = new Interpreter(in, System.out);
        interpreter.interpret();
        System.out.println();
    }

     public void FIXMEtestInterpret() { // FIXME
         final Interpreter interpreter = new Interpreter(
                 new InputStreamProvider(System.in), System.out);
         interpreter.interpret();
         System.out.println();
     }
 
 }
