 package edu.common.dynamicextensions.entitymanager;
 
import java.io.File;

 import edu.common.dynamicextensions.util.DynamicExtensionsBaseTestCase;
 import edu.common.dynamicextensions.util.parser.ImportPermissibleValues;
 
 /**
  * 
  * @author mandar_shidhore
  *
  */
 public class TestImportPermissibleValues extends DynamicExtensionsBaseTestCase
 {
 	/**
 	 * 
 	 */
 	public void testAddPermissibleValues()
 	{
 		try
 		{
			File file = new File("./pvs/PermissibleValues.csv");
			ImportPermissibleValues.main(new String[] {file.getCanonicalPath()});
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 }
