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
             String dcqlXML = PortalTestUtils.readFileASString("test/data/sampleDCQL1.xml");
             String cqlXML = PortalTestUtils.readFileASString("test/data/sampleCQL1.xml");
 
            facade.setQuery(dcqlXML);
            assertNull("Should be a valid query", facade.validate());
             facade.setQuery(cqlXML);
             assertNull("Should be a valid query", facade.validate());
 
             facade.setQuery("<samples/>");
             assertNotNull("Is not a valid query", facade.validate());
 
 
         } catch (IOException e) {
             fail(e.getMessage());
         }
 
     }
 }
