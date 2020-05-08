 package org.fuelsyourcyb.exathunk;
 
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeoutException;
 
 public class ArithmeticThunkFactoryTest {
 
     public Interpreter<String, Class, String, String, Object> makeInterpreter(ThunkFactory<Class, String, Object> factory) {
 	NTreeParser<String, String, String> parser = new SexpParser();
 	TypeChecker<Class, String, Object> checker = new IntTypeChecker();
 	NTree.Visitor<Class, String, Thunk<Object>> visitor = new ThunkUtils.StepVisitor<Class, String, Object>(factory);
 	NTreeEvaluator<Class, String, Object> evaluator = new DefaultEvaluator<>(visitor);
 	return new Interpreter<>(parser, checker, factory, evaluator);
     }
 
     @Test
     public void testParse1() throws Exception {
 	ThunkFactory<Class, String, Object> factory = new ArithmeticThunkFactory();
 	Interpreter<String, Class, String, String, Object> interpreter = makeInterpreter(factory);
 	assertEquals(6, interpreter.interpret("(* 3 2)"));
     }
 
     @Test
     public void testParse2() throws Exception {
 	ThunkFactory<Class, String, Object> factory = new ArithmeticThunkFactory();
 	Interpreter<String, Class, String, String, Object> interpreter = makeInterpreter(factory);
 	assertEquals(11, interpreter.interpret("(+ (- 1 2) (* 3 (/ 16 4)))"));
     }
 
     private class DelayingArithmeticThunkFactory extends ArithmeticThunkFactory {
 	private final Integer numSteps;
 
 	public DelayingArithmeticThunkFactory(Integer numSteps) {
 	    this.numSteps = numSteps;
 	}
 
	public Thunk<Object> makeThunk(String funcId, List<Object> params) throws UnknownFuncException {
 	    return new StepDelayingThunk<>(numSteps, super.makeThunk(funcId, params));
 	}
     }
 
     @Test
     public void testDelaying1() throws Exception {
 	ThunkFactory<Class, String, Object> factory = new DelayingArithmeticThunkFactory(3);
 	Interpreter<String, Class, String, String, Object> interpreter = makeInterpreter(factory);
 	assertEquals(6, interpreter.interpret("(* 3 2)"));
     }
 
     @Test
     public void testDelaying2() throws Exception {
 	ThunkFactory<Class, String, Object> factory = new DelayingArithmeticThunkFactory(3);
 	Interpreter<String, Class, String, String, Object> interpreter = makeInterpreter(factory);
 	assertEquals(11, interpreter.interpret("(+ (- 1 2) (* 3 (/ 16 4)))"));
     }
 
     @Test
     public void testTyping() throws Exception {
 	ThunkFactory<Class, String, Object> factory = new ArithmeticThunkFactory();
 	Interpreter<String, Class, String, String, Object> interpreter = makeInterpreter(factory);
 	assertEquals(2, interpreter.interpret("(- (len foo) 1)"));
     }
 }
