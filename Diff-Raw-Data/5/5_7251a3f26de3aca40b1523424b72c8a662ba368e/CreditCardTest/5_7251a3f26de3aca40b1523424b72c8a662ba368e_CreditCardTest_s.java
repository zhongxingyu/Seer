 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 
 public class CreditCardTest {
 
 	@Test
 	public void testPurchase() {
 		CreditCard cc = new CreditCard(1234567890,"XYZ",12000);  
 		  assertTrue(cc.purchase(12000));  
 		  //assertFalse(cc.purchase(6000));
 	}
 
 	@Test
 	public void testMakePayment() {
 		CreditCard cc = new CreditCard(1234567890,"XYZ",12000);  
 		  //make purchase  
 		  cc.purchase(6000);  
 		  //makePayment 2000  
		  cc.makePayment(3000);  
 		  //checkBalance  
		  assertTrue(cc.getAvailableBal()==9000);  
 	}
 
 	@Test
 	public void testGetAvailableBal() {
 		CreditCard cc = new CreditCard(1234567890,"XYZ",12000);  
 		  //make purchase  
 		  cc.purchase(5000);  
 		  //check balance  
 		  assertTrue(cc.getAvailableBal()==7000);  
 	}
 
 }
