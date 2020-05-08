 package com.assaydepot;
 
 import junit.framework.TestCase;
 
 import com.assaydepot.conf.Configuration;
 import com.assaydepot.result.Provider;
 import com.assaydepot.result.Results;
 
 public class AssayDepotTest extends TestCase {
 
 	public void testProviderQuery() throws Exception {
 		Configuration conf = new Configuration();
 		conf.setApiToken("5ae0a040967efe332d237277afb6beca");
 		AssayDepot assDeep = AssayDepotFactory.getAssayDepot( conf );
 		Results  results = assDeep.getProviderRefs( "antibody" );
 		assertEquals( results.getFacets().size() > 0, true );
 		assertEquals( results.getProviderRefs().size() > 0, true );
 	}
 	//1c929d31b856a4009453186a95927cd6
 
 	public void testGetProvider() throws Exception {
 		Configuration conf = new Configuration();
 		conf.setApiToken("5ae0a040967efe332d237277afb6beca");
 		AssayDepot assDeep = AssayDepotFactory.getAssayDepot( conf );
		Provider  provider = assDeep.getProvider( "1c929d31b856a4009453186a95927cd6" );
		assertEquals( provider.getId(), "1c929d31b856a4009453186a95927cd6" );
 		assertEquals( provider.getKeywords().size() > 0, true );
 		assertEquals( provider.getCertifications().size() > 0, true );
 	}
 
 	public void testGetWareRefs() throws Exception {
 		Configuration conf = new Configuration();
 		conf.setApiToken("5ae0a040967efe332d237277afb6beca");
 		AssayDepot assDeep = AssayDepotFactory.getAssayDepot( conf );
 		Results  results = assDeep.getWareRefs( "antibody" );
 		assertEquals( results.getWareRefs().size() > 0, true );
 		assertEquals( results.getFacets().size() > 0, true );
 	}
 	
 }
