 package types;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import enums.Currency;
 import enums.Type;
 import enums.User;
 
 public class CalculationTest {
 
 	@Test
 	public void testCalculation() {
 		List<Transaction> transactions = new ArrayList<Transaction>();
 		for (int i = 0; i < 10; i++)
 			transactions.add(new Transaction(new Amount(10, Currency.NOK), Type.Supermarket, new Date(System.currentTimeMillis()), User.Akira));
 		
 		Calculations impl = new Calculations();
 		double actualsuper = impl.getTotal(transactions, Type.Supermarket);
		int expectedsuper = 10 * 10;
 		
 		for (int i = 10; i < 20; i++)
 			transactions.add(new Transaction(new Amount(25, Currency.NOK), Type.Food, new Date(System.currentTimeMillis()), User.Akira));
 		
 		Calculations impl2 = new Calculations();
 		double actualfood = impl.getTotal(transactions, Type.Food);
 		int expectedfood = 10 * 25;
 
 		
 		Assert.assertEquals(expectedsuper, actualsuper, 1e-10);
 		Assert.assertEquals(expectedfood, actualfood, 1e-10);
 	}
 }
