 package layr.engine.expressions;
 
 import junit.framework.Assert;
 import layr.api.RequestContext;
 import layr.routing.impl.StubRequestContext;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class EvaluatorTest {
 	
 	static final int STRESS_LOOPS = 10000;
 
 	String name = "Poppins";
 	Pos pos = new Pos( 2, 45 );
 	RequestContext context;
 	
 	@Before
 	public void setup(){
 		context = createContext();
 	}
 
 	@Test
 	public void measureTimeForMultiExpressionStringEvaluation(){
 		long start = System.currentTimeMillis();
 		for ( int i=0; i<STRESS_LOOPS; i++ )
 			grantThatParseSingleExpression();
 		long elapsed = System.currentTimeMillis() - start;
 		Assert.assertTrue( elapsed < 900 );
 	}
 
 	@Test
 	public void grantThatParseSingleExpression(){
 		Evaluator evaluator = new Evaluator( context , "#{this.name}" );
 		Assert.assertEquals( "Poppins", evaluator.eval().toString() );
 	}
 
 	@Test
 	public void grantThatEvaluateMultiExpressionStartingWithComplexExpression() {
 		Evaluator evaluator = new Evaluator( context , "#{this.name} is between #{this.pos.start} and #{this.pos.end}." );
 		Assert.assertEquals( "Poppins is between 2 and 45.", evaluator.eval().toString() );
 	}
 
 	@Test
 	public void grantThatEvaluateMultiExpressionStartingWithStringExpression() {
 		Evaluator evaluator = new Evaluator( context , "Is #{this.name} between #{this.pos.start} and #{this.pos.end}?" );
 		Assert.assertEquals( "Is Poppins between 2 and 45?", evaluator.eval().toString() );
 	}
 	
	@Test
 	public void measureTimeForStringOnlyExpressionEvaluation(){
		long start = System.currentTimeMillis();
 		for ( int i=0; i<STRESS_LOOPS; i++ )
 			grantThatWhenEvaluateStringOnlyExpressionReturnsItSelf();
		long elapsed = System.currentTimeMillis() - start;
		Assert.assertTrue( elapsed < 100 );
 	}
 
 	@Test
 	public void grantThatWhenEvaluateStringOnlyExpressionReturnsItSelf() {
 		String stringOnlyExpression = "Grant that when parse String-Only Expression returns itself.";
 		Evaluator evaluator = new Evaluator( context , stringOnlyExpression );
 		Assert.assertEquals( stringOnlyExpression, evaluator.eval().toString() );
 	}
 
 	RequestContext createContext() {
 		RequestContext layrContext = new StubRequestContext();
 		layrContext.put( "this", this );
 		return layrContext;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	class Pos {
 		int start = 0;
 		int end = 0;
 		
 		Pos( int start, int end ) {
 			this.start = start;
 			this.end = end;
 		}
 		
 		public int getStart() {
 			return start;
 		}
 		
 		public int getEnd() {
 			return end;
 		}
 	}
 }
