 package ling572;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 public class CosineDistanceMetricTest {
 
 	static Instance testInstance;
 	static Instance test2Instance;
 	static Instance test3Instance;
 	static Instance test4Instance;
 	static Instance test5Instance;
 	static Instance test6Instance;
 	
 	{
 		testInstance = new Instance("test");
 		testInstance.addFeature("test1", 1);
 		testInstance.addFeature("test2", 2);
 		testInstance.addFeature("test3", 3);
 		testInstance.addFeature("test4", 5);		
 				
 		test2Instance = new Instance("test2");
 		test2Instance.addFeature("test1", 20);
 		test2Instance.addFeature("test2", 245);
 		test2Instance.addFeature("test3", 39);
 		test2Instance.addFeature("test4", 509);	
 		
 		test3Instance = new Instance("test3");
 		test3Instance.addFeature("test1", 20000);
 		test3Instance.addFeature("test2", 2450000);
 		test3Instance.addFeature("test3", 390000);
 		test3Instance.addFeature("test4", 5090000);
 		
 		test4Instance = new Instance("test4");
 		test4Instance.addFeature("test1", 19);
 		test4Instance.addFeature("test2", 243);
 		test4Instance.addFeature("test3", 36);
 		test4Instance.addFeature("test4", 504);
 		
 		test5Instance = new Instance("test5");
 		test5Instance.addFeature("test1", 12);
 		test5Instance.addFeature("test2", 16);
 		test5Instance.addFeature("test3", 17);
 		
 		test6Instance = new Instance("test6");
 		test6Instance.addFeature("test1",16);
 		test6Instance.addFeature("test2", 76);
 		test6Instance.addFeature("test3", 27);
 		test6Instance.addFeature("test4", 9);
 		test6Instance.addFeature("test5", 88);
 				
 	}
 		
 	@Test
 	public void testDistance() {
 		CosineDistanceMetric tester = new CosineDistanceMetric();
 		assertEquals("Result", 1, tester.distance(testInstance, testInstance), .00001);
 		assertEquals("Result", 0.896458305, tester.distance(testInstance, test2Instance), .0000000001);
 		assertNotSame("Result", 1, tester.distance(testInstance, test2Instance));
 		assertTrue(tester.distance(testInstance,test2Instance) < tester.distance(testInstance, test3Instance));
 		assertEquals("Result", tester.distance(testInstance, test2Instance), -tester.distance(testInstance, test4Instance), 0000001); 
 	}
 	
 	@Test
 	public void testNonIsolengthVectors() {
 		CosineDistanceMetric tester = new CosineDistanceMetric();
 		// |lhs| > |rhs|
		assertEquals("Result", 0.677254, tester.distance(test4Instance, test5Instance), .000001);
 		// |lhs| < |rhs|
		assertEquals("Result", 0.677254, tester.distance(test5Instance, test4Instance), .000001);
 	}
 	
 }
