 /**
  * 
  */
 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.models;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 import edu.wpi.cs.wpisuitetng.modules.core.models.User;
 
 /**
  * @author Dylan
  *
  */
 public class TransactionTest {
 
 	@Test
 	public void testCreateTransaction() {
		String u = "joe";
 		Transaction t = new Transaction(u, 1234567, "changed priority from LOW to HIGH");
 		assertNotNull(t);
 		assertSame(t.getUser(), u);
 		assertSame(t.getTS(), 1234567);
 		assertSame(t.getMessage(), "changed priority from LOW to HIGH");
 	}
 
 }
