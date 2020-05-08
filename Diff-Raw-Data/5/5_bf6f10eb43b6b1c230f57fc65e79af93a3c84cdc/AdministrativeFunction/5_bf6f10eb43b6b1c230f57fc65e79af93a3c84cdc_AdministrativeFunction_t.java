 package unitTests;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import vsp.VspServiceProvider;
 
 @RunWith(OrderedRunner.class)
 public class AdministrativeFunction
 {
 	private final VspServiceProvider vsp = new VspServiceProvider();
 	
 	@Test
 	@Order(order=1)
 	public void getUserList()
 	{
 		try
 		{
 			// the default traders in the database include 'test' and 'test1'
 			List<String> traders = vsp.getTraders();
 			Assert.assertTrue(traders.size() >= 2);
 			Assert.assertTrue(traders.contains("test"));
 			Assert.assertTrue(traders.contains("test1"));
 		}
 		catch (Exception e)
 		{
 			Assert.fail();
 		}
 	}
 	
 	@Test
 	@Order(order=2)
	public void changePasswordInvalid()
 	{
 		fail("Not yet implemented");		
 	}
 	
 	@Test
 	@Order(order=3)
	public void changePasswordValid()
 	{
 		fail("Not yet implemented");		
 	}
 }
