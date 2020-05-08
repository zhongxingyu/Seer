 package org.fuelsyourcyb.exathunk;
 
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.ExecutionException;
 
 import java.util.List;
 
 public class ExecutorTest {
 
     private class TimeDelayingArithmeticThunkFactory extends ArithmeticThunkFactory {
 	private final long timeout;
 	private final TimeUnit timeUnit;
 
 	public TimeDelayingArithmeticThunkFactory(long timeout, TimeUnit timeUnit) {
 	    this.timeout = timeout;
 	    this.timeUnit = timeUnit;
 	}
 
	public Thunk<Object> makeThunk(String funcId, List<Object> params) throws UnknownFuncException, ExecutionException {
 	    return new TimeDelayingThunk<>(timeout, timeUnit, super.makeThunk(funcId, params));
 	}
     }
 
 
     public Interpreter<String, Class, String, String, Object> makeInterpreter(long timeout, TimeUnit timeUnit) {
 	ThunkFactory<Class, String, Object> factory = new TimeDelayingArithmeticThunkFactory(timeout, timeUnit);
 	NTreeParser<String, String, String> parser = new SexpParser();
 	TypeChecker<Class, String, Object> checker = new IntTypeChecker();
 	Executor executor = Executors.newSingleThreadExecutor();
 	NTree.Visitor<Class, String, Thunk<Object>> visitor = new ExecutorVisitor<Class, String, Object>(factory, executor);
 	NTreeEvaluator<Class, String, Object> evaluator = new DefaultEvaluator<>(visitor);
 	return new Interpreter<>(parser, checker, factory, evaluator);
     }
 
     @Test
     public void testParse1() throws Exception {
 	assertEquals(6, makeInterpreter(50, TimeUnit.MILLISECONDS).interpret("(* 3 2)"));
     }
 
     @Test
     public void testParse2() throws Exception {
 	assertEquals(11, makeInterpreter(50, TimeUnit.MILLISECONDS).interpret("(+ (- 1 2) (* 3 (/ 16 4)))"));
     }
 
 }
