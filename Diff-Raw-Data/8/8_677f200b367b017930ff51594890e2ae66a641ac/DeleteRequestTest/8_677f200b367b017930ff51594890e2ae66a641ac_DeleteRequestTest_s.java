 package com.dobby.tests.request;
 
import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 import com.dobby.core.Request;
 import com.dobby.core.StateVector;
 import com.dobby.core.requests.DeleteRequest;
import com.dobby.core.Request;
 
 public class DeleteRequestTest {
 
 	@Test
 	public void testInsertTransform() {
 		String testString = "0123456789abcdef";
 		Request d1 = new DeleteRequest("Test", new StateVector(), 0, 
 				2, '2');
 		Request d2 = new DeleteRequest ("Exam", new StateVector(), 1,
 				7, '7');
 	}
 
 	@Test
 	public void testDeleteTransform() {
 		DeleteRequest delete = new DeleteRequest("Test", new StateVector(), 0, 
 				11, 'b');
 		String testString = "0123456789abcdef";
 		Request d1 = new DeleteRequest("Test", new StateVector(), 0, 
 				2, '2');
 		Request d2 = new DeleteRequest ("Exam", new StateVector(), 1,
 				7, '7');
 		Request transform = d2.transform(d1);
 		String product = transform.apply(d1.apply(testString));
 		System.out.println(testString + " Double delete produced, First" +
 				" request positioned before second " + product);
 		assertTrue(product.equals("01345689abcdef"));
 		
 		String testString2 = "0123456789abcdef";
 		Request d3 = new DeleteRequest("Test", new StateVector(), 0, 
 				2, '2');
 		Request d4 = new DeleteRequest ("Exam", new StateVector(), 1,
 				1, '1');
		Request transform2 = d2.transform(d1);
 		String product2 = transform2.apply(d3.apply(testString2));
 		System.out.println(testString2 + " Double delete produced, " +
 				"request positioned after second" + product2);
 		assertTrue(product2.equals("03456789abcdef"));
 		
 		
 		String testString3 = "0123456789abcdef";
 		Request d5 = new DeleteRequest("Test", new StateVector(), 0, 
 				2, '2');
 		Request d6 = new DeleteRequest ("Exam", new StateVector(), 1,
 				7, '7');
 		Request transform3 = d6.transform(d5);
 		String product3 = transform3.apply(d5.apply(testString3));
 		System.out.println(testString3 + "Double delete produced" +
 				product3);
 		assertTrue(product3.equals("01345689abcdef"));
 	}
 
 	@Test
 	public void testApply() {
 		String testString = "0123456789abcdef";
 		DeleteRequest delete = new DeleteRequest("Test", new StateVector(), 0,
 				3, '3');
 		assertTrue("012456789abcdef".equals(delete.apply(testString)));
 	}
 
 	@Test
 	public void testBeginningApply() {
 		String testString = "0123456789abcdef";
 		DeleteRequest delete = new DeleteRequest("Test", new StateVector(), 0,
 				0, '0');
 		String product = delete.apply(testString);
 		System.out.println(testString + " Delete at beginning produced "
 				+ product);
 		assertTrue("123456789abcdef".equals(product));
 	}
 
 	@Test
 	public void testEndApply() {
 		String testString = "0123456789abcdef";
 		DeleteRequest delete = new DeleteRequest("Test", new StateVector(), 0,
 				15, 'f');
 		String product = delete.apply(testString);
 		System.out.println(testString + " Delete at end produced " + product);
 		assertTrue("0123456789abcde".equals(product));
 	}
 
 	@Test
 	public void testOutOfBoundsCase() {
 		String testString = "0123456789abcdef";
 		DeleteRequest delete = new DeleteRequest("Test", new StateVector(), 0,
 				16, 'x');
 		assertTrue("0123456789abcdef".equals(delete.apply(testString)));
 	}
 }
