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
		User testUser = new User("raphael2", false, "Raphael", "2345234523452345");
		Order order = new Order(6, testUser.getUserID(), "Shredder Hideout, New York", "Pepperoni", "Boat");
 		OrderUtils.addOrder(order);
		User adminTestUser = new User("mastersplinter10", true, "Master Splinter", "5678567856785678");
 		UserUtils.checkAdmin(adminTestUser.getUserID());
 		UserUtils.deleteOrders(testUser.getUserID(), order.getOrderID(), true);
 	}
 
 }
