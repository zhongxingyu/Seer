 package pizzawatch.sql.functions.test;
 
 import org.junit.Test;
 import pizzawatch.datamodels.Order;
 import pizzawatch.datamodels.User;
 import pizzawatch.utils.OrderUtils;
 import pizzawatch.utils.UserUtils;
 
 public class FunctionsTest {
 
 	@Test
 	public void test()
 	{
		User testUser = new User("2", false, "Raphael", "TMNT002");
		Order order = new Order(UserUtils.getUserIDFromName(testUser.getName()), "Shredder Hideout, New York", "Pepperoni", "boat");
 		OrderUtils.addOrder(order);
		User adminTestUser = new User("10", true, "Master Splinter", "MASTERCARD0010");
 		UserUtils.checkAdmin(adminTestUser.getUserID());
 		UserUtils.deleteOrders(testUser.getUserID(), order.getOrderID(), true);
 	}
 
 }
