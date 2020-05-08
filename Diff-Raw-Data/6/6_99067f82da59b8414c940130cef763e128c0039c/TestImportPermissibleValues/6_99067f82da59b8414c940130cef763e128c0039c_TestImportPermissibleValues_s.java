 package edu.common.dynamicextensions.entitymanager;
 
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
			ImportPermissibleValues.main(new String[] {"./pvs/PermissibleValues.csv"});
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 }
