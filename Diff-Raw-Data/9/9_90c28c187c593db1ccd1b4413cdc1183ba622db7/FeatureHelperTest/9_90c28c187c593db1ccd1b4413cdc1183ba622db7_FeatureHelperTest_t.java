 package eu.liveandgov.wp1.backend;
 
 import static org.junit.Assert.*;
 
 import org.apache.commons.math3.complex.Complex;
 import org.junit.Test;
 
 import eu.liveandgov.wp1.backend.sensorLoop.FeatureHelper;
 
 public class FeatureHelperTest {
 
 	@Test
 	public void test() {
 		float[] input = { 1,2,3,4,5 };
 		
 		assertEquals( 3, FeatureHelper.mean(input), 0.005 );
 		
 		
 		System.out.println(FeatureHelper.var(input));
 
 		input = new float[] { 1,2,3,4,5,6,7,8 };
 
 		for (Complex s : FeatureHelper.FFT(input)){
			System.out.println(s.abs());
 		}
 	}
 
 }
