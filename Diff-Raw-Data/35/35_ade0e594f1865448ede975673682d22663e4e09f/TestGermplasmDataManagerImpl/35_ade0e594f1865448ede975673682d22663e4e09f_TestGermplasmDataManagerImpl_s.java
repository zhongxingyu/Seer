 package org.generationcp.middleware.manager.test;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.generationcp.middleware.manager.Database;
 import org.generationcp.middleware.manager.DatabaseConnectionParameters;
 import org.generationcp.middleware.manager.FindGermplasmByNameModes;
 import org.generationcp.middleware.manager.GermplasmNameType;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.generationcp.middleware.manager.Operation;
 import org.generationcp.middleware.manager.api.GermplasmDataManager;
 import org.generationcp.middleware.pojos.Attribute;
 import org.generationcp.middleware.pojos.Bibref;
 import org.generationcp.middleware.pojos.Country;
 import org.generationcp.middleware.pojos.Germplasm;
 import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
 import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
 import org.generationcp.middleware.pojos.Location;
 import org.generationcp.middleware.pojos.Method;
 import org.generationcp.middleware.pojos.Name;
 import org.generationcp.middleware.pojos.UserDefinedField;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TestGermplasmDataManagerImpl 
 {
 	private static ManagerFactory factory;
 	private static GermplasmDataManager manager;
 
 	@BeforeClass
 	public static void setUp() throws Exception 
 	{
 		DatabaseConnectionParameters local = new DatabaseConnectionParameters("testDatabaseConfig.properties", "local");
 		DatabaseConnectionParameters central = new DatabaseConnectionParameters("testDatabaseConfig.properties", "central");
 		factory = new ManagerFactory(local, central);
 		manager = factory.getGermplasmDataManager();
 	}
 	
 	@Test
 	public void testFindGermplasmByName() throws Exception
 	{
 		long start = System.currentTimeMillis();
		List<Germplasm> germplasmList = manager.findGermplasmByName("IR 64", 0, 5, FindGermplasmByNameModes.NORMAL, Operation.EQUAL, null, null, Database.CENTRAL);
 		Assert.assertTrue(germplasmList != null);
		Assert.assertTrue(germplasmList.size() >= 5);
 		
 		System.out.println("SEARCH RESULTS");
 		for(Germplasm g : germplasmList)
 		{
 			System.out.println(g);
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testCountGermplasmByName() throws Exception
 	{
 		long start = System.currentTimeMillis();
		int count = manager.countGermplasmByName("IR 64", FindGermplasmByNameModes.NORMAL, Operation.EQUAL, null, null, Database.CENTRAL);
 		System.out.println(count);
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testFindGermplasmByNameUsingLike() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		List<Germplasm> germplasmList = manager.findGermplasmByName("IR%", 0, 5, FindGermplasmByNameModes.NORMAL, Operation.LIKE, null, null, Database.CENTRAL);
 		Assert.assertTrue(germplasmList != null);
 		Assert.assertTrue(germplasmList.size() >= 5);
 		
 		System.out.println("SEARCH RESULTS");
 		for(Germplasm g : germplasmList)
 		{
 			System.out.println(g);
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testCountGermplasmByNameUsingLike() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		int count = manager.countGermplasmByName("IR%", FindGermplasmByNameModes.NORMAL, Operation.LIKE, null, null, Database.CENTRAL);
 		System.out.println(count);
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testFindGermplasmByNameWithStatus() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		List<Germplasm> germplasmList = manager.findGermplasmByName("IR 64", 0, 5, FindGermplasmByNameModes.NORMAL, Operation.EQUAL, new Integer(1), null, Database.CENTRAL);
 		Assert.assertTrue(germplasmList != null);
 		Assert.assertTrue(germplasmList.size() >= 5);
 		
 		System.out.println("SEARCH RESULTS");
 		for(Germplasm g : germplasmList)
 		{
 			System.out.println(g);
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testCountGermplasmByNameWithStatus() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		int count = manager.countGermplasmByName("IR 64", FindGermplasmByNameModes.NORMAL, Operation.EQUAL, new Integer(1), null, Database.CENTRAL);
 		System.out.println(count);
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testFindGermplasmByNameWithStatusAndType() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		List<Germplasm> germplasmList = manager.findGermplasmByName("IR 64", 0, 5, FindGermplasmByNameModes.NORMAL, Operation.EQUAL, new Integer(1), GermplasmNameType.RELEASE_NAME, Database.CENTRAL);
 		Assert.assertTrue(germplasmList != null);
 		Assert.assertTrue(germplasmList.size() >= 5);
 		
 		System.out.println("SEARCH RESULTS");
 		for(Germplasm g : germplasmList)
 		{
 			System.out.println(g);
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testCountGermplasmByNameWithStatusAndType() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		int count = manager.countGermplasmByName("IR 64", FindGermplasmByNameModes.NORMAL, Operation.EQUAL, new Integer(1), GermplasmNameType.RELEASE_NAME, Database.CENTRAL);
 		System.out.println(count);
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testFindGermplasmByNameWithStatusUsingLike() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		List<Germplasm> germplasmList = manager.findGermplasmByName("IR%", 0, 5, FindGermplasmByNameModes.NORMAL, Operation.LIKE, new Integer(1), null, Database.CENTRAL);
 		Assert.assertTrue(germplasmList != null);
 		Assert.assertTrue(germplasmList.size() >= 5);
 		
 		System.out.println("SEARCH RESULTS");
 		for(Germplasm g : germplasmList)
 		{
 			System.out.println(g);
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testFindGermplasmByNameWithStatusAndTypeUsingLike() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		List<Germplasm> germplasmList = manager.findGermplasmByName("IR%", 0, 5, FindGermplasmByNameModes.NORMAL, Operation.LIKE, new Integer(1), GermplasmNameType.RELEASE_NAME, Database.CENTRAL);
 		Assert.assertTrue(germplasmList != null);
 		Assert.assertTrue(germplasmList.size() >= 5);
 		
 		System.out.println("SEARCH RESULTS");
 		for(Germplasm g : germplasmList)
 		{
 			System.out.println(g);
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testFindGermplasmByLocationNameUsingEqual() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		List<Germplasm> germplasmList = manager.findGermplasmByLocationName("Philippines", 0, 5, Operation.EQUAL);
 		Assert.assertTrue(germplasmList != null);
 		Assert.assertTrue(germplasmList.size() >= 5);
 		
 		System.out.println("SEARCH RESULTS");
 		for(Germplasm g : germplasmList)
 		{
 			System.out.println(g);
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testCountGermplasmByLocationNameUsingEqual() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		int count = manager.countGermplasmByLocationName("Philippines", Operation.EQUAL);
 		System.out.println("COUNT = " + count);
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testFindGermplasmByLocationNameUsingLike() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		List<Germplasm> germplasmList = manager.findGermplasmByLocationName("International%", 0, 5, Operation.LIKE);
 		Assert.assertTrue(germplasmList != null);
 		Assert.assertTrue(germplasmList.size() >= 5);
 		
 		System.out.println("SEARCH RESULTS");
 		for(Germplasm g : germplasmList)
 		{
 			System.out.println(g);
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testCountGermplasmByLocationNameUsingLike() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		int count = manager.countGermplasmByLocationName("International%", Operation.LIKE);
 		System.out.println("COUNT = " + count);
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testFindGermplasmByMethodNameUsingEqual() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		List<Germplasm> germplasmList = manager.findGermplasmByMethodName("SINGLE CROSS", 0, 5, Operation.EQUAL);
 		Assert.assertTrue(germplasmList != null);
 		Assert.assertTrue(germplasmList.size() >= 5);
 		
 		System.out.println("SEARCH RESULTS");
 		for(Germplasm g : germplasmList)
 		{
 			System.out.println(g);
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testCountGermplasmByMethodNameUsingEqual() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		int count = manager.countGermplasmByMethodName("SINGLE CROSS", Operation.EQUAL);
 		System.out.println("COUNT = " + count);
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testFindGermplasmByMethodNameUsingLike() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		List<Germplasm> germplasmList = manager.findGermplasmByMethodName("%CROSS%", 0, 5, Operation.LIKE);
 		Assert.assertTrue(germplasmList != null);
 		Assert.assertTrue(germplasmList.size() >= 5);
 		
 		System.out.println("SEARCH RESULTS");
 		for(Germplasm g : germplasmList)
 		{
 			System.out.println(g);
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testCountGermplasmByMethodNameUsingLike() throws Exception
 	{
 		long start = System.currentTimeMillis();
 		int count = manager.countGermplasmByMethodName("%CROSS%", Operation.LIKE);
 		System.out.println("COUNT = " + count);
 		long end = System.currentTimeMillis();
 		System.out.println("QUERY TIME: " + (end - start) + " ms");
 	}
 	
 	@Test
 	public void testGetGermplasmByGID() throws Exception
 	{
 		Germplasm germplasm = manager.getGermplasmByGID(new Integer(50533));
 		Assert.assertTrue(germplasm != null);
 		Assert.assertTrue(germplasm.getGid() == 50533);
 		System.out.println(germplasm);
 	}
 	
 	@Test
 	public void testGetGermplasmWithPrefName() throws Exception
 	{
 		Germplasm germplasm = manager.getGermplasmWithPrefName(new Integer(50533));
 		Assert.assertTrue(germplasm != null);
 		Assert.assertTrue(germplasm.getGid() == 50533);
 		Assert.assertTrue(germplasm.getPreferredName() != null);
 		
 		System.out.println(germplasm);
 		System.out.println(germplasm.getPreferredName());
 	}
 	
 	@Test
 	public void testGetGermplasmWithPrefAbbrev() throws Exception
 	{
 		Germplasm germplasm = manager.getGermplasmWithPrefAbbrev(new Integer(151));
 		Assert.assertTrue(germplasm != null);
 		Assert.assertTrue(germplasm.getGid() == 151);
 		Assert.assertTrue(germplasm.getPreferredName() != null);
 		Assert.assertTrue(germplasm.getPreferredAbbreviation() != null);
 		
 		System.out.println(germplasm);
 		System.out.println(germplasm.getPreferredName());
 		System.out.println(germplasm.getPreferredAbbreviation());
 	}
 	
 	@Test
 	public void testGetGermplasmNameByID() throws Exception
 	{
 		Name name = manager.getGermplasmNameByID(new Integer(42268));
 		Assert.assertTrue(name != null);
 		Assert.assertTrue(name.getNid() == 42268);
 		System.out.println(name);
 	}
 	
 	@Test
 	public void testGetNamesByGID() throws Exception
 	{
 		List<Name> names = manager.getNamesByGID(new Integer(50533), null, null);
 		Assert.assertTrue(names != null);
 		Assert.assertTrue(!names.isEmpty());
 		
 		for(Name name: names)
 		{
 			System.out.println(name);
 		}
 	}
 	
 	@Test
 	public void testGetPreferredNameByGID() throws Exception
 	{
 		System.out.println(manager.getPreferredNameByGID(1));
 	}
 	
 	@Test
 	public void testGetPreferredAbbrevByGID() throws Exception
 	{
 		System.out.println(manager.getPreferredAbbrevByGID(1));
 	}
 	
 	@Test
 	public void testGetNameByGIDAndNval() throws Exception
 	{
 		System.out.println(manager.getNameByGIDAndNval(1, "GCP-TEST"));
 	}
 	
 	@Test
 	public void testGetNamesByGIDWithStatus() throws Exception
 	{
 		List<Name> names = manager.getNamesByGID(new Integer(50533), new Integer(1), null);
 		Assert.assertTrue(names != null);
 		Assert.assertTrue(!names.isEmpty());
 		Assert.assertTrue(names.size() == 1);
 		
 		for(Name name: names)
 		{
 			System.out.println(name);
 		}
 	}
 	
 	@Test
 	public void testGetNamesByGIDWithStatusAndType() throws Exception
 	{
 		List<Name> names = manager.getNamesByGID(new Integer(50533), new Integer(8), GermplasmNameType.INTERNATIONAL_TESTING_NUMBER);
 		Assert.assertTrue(names != null);
 		Assert.assertTrue(!names.isEmpty());
 		Assert.assertTrue(names.size() == 1);
 		
 		for(Name name: names)
 		{
 			System.out.println(name);
 		}
 	}
 	
 	@Test
 	public void testGetAttributesByGID() throws Exception
 	{
 		List<Attribute> attributes = manager.getAttributesByGID(new Integer(50533));
 		Assert.assertTrue(attributes != null);
 		Assert.assertTrue(!attributes.isEmpty());
 		//Assert.assertTrue(attributes.size() == 2);
 		
 		for(Attribute attribute : attributes)
 		{
 			System.out.println(attribute);
 		}
 	}
 	
 	@AfterClass
 	public static void tearDown() throws Exception 
 	{
 		factory.close();
 	}
 
 }
