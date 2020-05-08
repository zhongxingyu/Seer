 package gov.nih.nci.cagrid.portal.portlet.browse.sharedQuery;
 
 import gov.nih.nci.cagrid.portal.PortalTestUtils;
 import gov.nih.nci.cagrid.portal.portlet.PortalPortletIntegrationTestBase;
 
 import java.io.IOException;
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 public class SharedQueryCatalogEntryManagerFacadeTest extends PortalPortletIntegrationTestBase {
 
 
     public void testValidate() {
         SharedQueryCatalogEntryManagerFacade facade = (SharedQueryCatalogEntryManagerFacade) getBean("sharedQueryCatalogEntryManagerFacade");
         try {
             facade.setQuery(PortalTestUtils.readFileASString("test/data/sampleCQL1.xml"));
             assertNull("Should be a valid query", facade.validate());
 
             facade.setQuery(PortalTestUtils.readFileASString("test/data/sampleDCQL1.xml"));
             assertNull("Should be a valid query", facade.validate());
 
             facade.setQuery("<samples/>");
             assertNotNull("Is not a valid query", facade.validate());
 
             facade.setQuery(PortalTestUtils.readFileASString("test/data/microArrayLargeDataDCQL.xml"));
             assertNull("Should be a valid query", facade.validate());
 
             facade.setQuery(PortalTestUtils.readFileASString("test/data/sampleCQLNotNull.xml"));
             assertNull("Should be a valid query", facade.validate());
 
            facade.setQuery(PortalTestUtils.readFileASString("test/data/sampleCQLPredicate.xml"));
            assertNull("Should be a valid query", facade.validate());
 
             facade.setQuery(PortalTestUtils.readFileASString("test/data/selected_attribute_query.xml"));
             assertNull("Should be a valid query", facade.validate());
 
 
         } catch (IOException e) {
             fail(e.getMessage());
         }
 
     }
 }
