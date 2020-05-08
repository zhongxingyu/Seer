 package org.xbrlapi.fragment.tests;
 
 import java.util.List;
 import java.util.Set;
 
 import org.xbrlapi.Context;
 import org.xbrlapi.DOMLoadingTestCase;
 import org.xbrlapi.Entity;
 import org.xbrlapi.EntityResource;
 import org.xbrlapi.LabelResource;
 
 /**
  * Tests the implementation of the org.xbrlapi.EntityResource interface.
  * Uses the DOM-based data store to ensure rapid testing.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class EntityResourceTestCase extends DOMLoadingTestCase {
 	private final String STARTING_POINT = "test.data.local.entities.simple";
 	private final String ENTITY_MAP = "real.data.sec.entity.map";
 	
 	protected void setUp() throws Exception {
 		super.setUp();
         loader.discover(this.getURI(STARTING_POINT));	
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}	
 	
 	public EntityResourceTestCase(String arg0) {
 		super(arg0);
 	}
 	
     public void testEntityResourceManipulations() {
         try {
             
             List<Context> contexts = store.<Context>getXMLResources("Context");
             assertTrue("There are two contexts.",contexts.size() == 2);
 
             for (Context context: contexts) {
                 if (context.getId().equals("c1")) {
                     Entity entity = context.getEntity();
                    assertEquals("http://xbrlapi.org/integer/entity/scheme/", entity.getIdentifierScheme().toString());
                     assertEquals("1", entity.getIdentifierValue());
                     List<EntityResource> resources = entity.getEntityResources();
                     assertEquals(1,resources.size());
                     EntityResource resource = resources.get(0);
                     Set<EntityResource> equivalents = resource.getEquivalents();
                     assertEquals(2,equivalents.size());
                     List<LabelResource> labels = resource.getLabels();
                     assertEquals(1,labels.size());
                     labels = entity.getAllEntityLabels();
                     assertEquals(2,labels.size());
                     assertEquals("http://xbrlapi.org/integer/entity/scheme/",resource.getIdentifierScheme());
                     assertEquals("1",resource.getIdentifierValue());
                 }
             }
             
         } catch (Exception e) {
             e.printStackTrace();
             fail(e.getMessage());
         }
     }
 }
