 package utilities;
 
 import static org.junit.Assert.*;
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class AggregateResultTest {
 
 	@Test
 	public void test() {
 		//test a single aggregate
 		int UPPER_BOUND = 2;
 		for(int i = 0; i < UPPER_BOUND; i++){
 			HypothesisTest.AggregateResults ar = new HypothesisTest.AggregateResults();
 			ar.addMove(i, UPPER_BOUND);
 			assertEquals(1.0- (double)i/UPPER_BOUND, ar.getAvgEvaluation(), 0.0000000001);
			assertSame(ar.getNumMoves(), 1);
 			
 		}
 		
 		//test a sum aggregate
 		for(int i = 0; i < UPPER_BOUND; i++){
 			for(int j = 0; j < UPPER_BOUND; j++){
 				HypothesisTest.AggregateResults ar = new HypothesisTest.AggregateResults();
 				ar.addMove(i, UPPER_BOUND);
 				ar.addMove(j, UPPER_BOUND);
 				
 				assertEquals(ar.getAvgEvaluation(), 1.0- ( (double)i/UPPER_BOUND +(double)j/UPPER_BOUND ) /2, 0.0000000001);
				assertSame(ar.getNumMoves(), 2);
 			}
 		}
 		
 		
 		
 	}
 
 }
