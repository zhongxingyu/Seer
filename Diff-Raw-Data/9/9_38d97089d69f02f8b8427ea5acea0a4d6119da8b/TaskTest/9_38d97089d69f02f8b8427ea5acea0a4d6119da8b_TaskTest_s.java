 package pl.kaczanowski.model;
 
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 public class TaskTest {
 
 	@DataProvider
 	private Object[][] getInvalidTicks() {
 		return new Object[][]{{-1}, {0}};
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class, dataProvider = "getInvalidTicks")
 	public void testTask(final int ticks) {
		new Task(ticks);
 
 	}
 
 }
