 package de.pellepelster.myadmin.demo.client;
 
 import de.pellepelster.myadmin.client.base.modules.hierarchical.BaseHierarchicalConfiguration;
 
 public class TestClientHierarchicalConfiguration extends BaseHierarchicalConfiguration
 {
 	public static final String ID = "test";
 
 	public TestClientHierarchicalConfiguration()
 	{
 		super(ID);
 
		// addHierarchy(DemoDictionaryIDs.COMPANY);
		// addHierarchy(DemoDictionaryIDs.MANAGER, DemoDictionaryIDs.COMPANY);
		// addHierarchy(DemoDictionaryIDs.EMPLOYEE, DemoDictionaryIDs.COMPANY,
		// DemoDictionaryIDs.MANAGER);
 	}
 }
