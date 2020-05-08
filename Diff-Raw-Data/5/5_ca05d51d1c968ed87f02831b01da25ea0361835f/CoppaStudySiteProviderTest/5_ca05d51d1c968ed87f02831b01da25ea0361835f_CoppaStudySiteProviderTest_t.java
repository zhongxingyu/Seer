 package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;
 
 import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper;
 import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import gov.nih.nci.cabig.ctms.testing.MockRegistry;
 import gov.nih.nci.coppa.po.Organization;
 import gov.nih.nci.coppa.po.ResearchOrganization;
 import gov.nih.nci.coppa.services.pa.Id;
 import junit.framework.TestCase;
 import static org.easymock.EasyMock.*;
 import org.iso._21090.DSETII;
 import org.iso._21090.ENON;
 import org.iso._21090.ENXP;
 import org.iso._21090.EntityNamePartType;
 import org.iso._21090.II;
 import org.osgi.framework.BundleContext;
 import org.springframework.osgi.mock.MockServiceReference;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import static java.util.Arrays.asList;
 import java.util.List;
 
 /**
  * @author John Dzak
  */
 public class CoppaStudySiteProviderTest extends TestCase {
     private CoppaStudySiteProvider provider;
     private MockRegistry mocks = new MockRegistry();
     private CoppaAccessor coppaAccessor;
     private BundleContext bundleContext;
 
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         bundleContext = mocks.registerMockFor(BundleContext.class);
         coppaAccessor = mocks.registerMockFor(CoppaAccessor.class);
 
         MockServiceReference ref = new MockServiceReference();
         expect(bundleContext.getServiceReference(CoppaProviderHelper.ACCESSOR_SERVICE)).
             andStubReturn(ref);
         expect(bundleContext.getService(ref)).andStubReturn(coppaAccessor);
 
 
         provider = new CoppaStudySiteProvider(bundleContext);
     }
 
     public void testGetAssociatedSitesWithEmptyStudyList() throws Exception {
         expect(coppaAccessor.searchStudySitesByStudyProtocolId((Id) notNull())).andReturn(
             new gov.nih.nci.coppa.services.pa.StudySite[0]
         );
 
         mocks.replayMocks();
 
         List<List<StudySite>> actual = provider.getAssociatedSites(new ArrayList<Study>());
 
         assertEquals("Wrong results size", 0, actual.size());
     }
 
     public void testGetAssociatedSitesWithNoResults() throws Exception {
         expect(coppaAccessor.searchStudySitesByStudyProtocolId((Id) notNull())).andReturn(
             new gov.nih.nci.coppa.services.pa.StudySite[0]
         );
 
         mocks.replayMocks();
 
         List<List<StudySite>> actual = provider.getAssociatedSites(asList(
             pscStudy("Ext A")
         ));
 
         assertEquals("Wrong results size", 1, actual.size());
         assertNull("Wrong element", actual.get(0));
     }
 
     public void testGetAssociatedSitesWithResults() throws Exception {
         expect(coppaAccessor.searchStudySitesByStudyProtocolId((Id) notNull())).andReturn( new gov.nih.nci.coppa.services.pa.StudySite[] {
             coppaStudySite("Ext SS", coppaResearchOrg("Ext RO", "Player Ext RO"))
         });
 
         expect(coppaAccessor.getResearchOrganizations((gov.nih.nci.coppa.po.Id[]) notNull())).andReturn( new ResearchOrganization[] {
             coppaResearchOrg("Ext RO A", "Player Ext RO A"),
             coppaResearchOrg("Ext RO B", "Player Ext RO B")
         });
 
         expect(coppaAccessor.getOrganization((gov.nih.nci.coppa.po.Id) notNull())).andReturn(
             coppaOrganization("Name A", "Ext O A")
         );            
 
         expect(coppaAccessor.getOrganization((gov.nih.nci.coppa.po.Id) notNull())).andReturn(
             coppaOrganization("Name B", "Ext O B")
         );
 
         mocks.replayMocks();
 
         List<List<StudySite>> actual = provider.getAssociatedSites(
            asList(pscStudy("Ext SS"))
         );
 
         assertEquals("Wrong size", 1, actual.size());
         assertEquals("Wrong size", 2, actual.get(0).size());
         assertEquals("Wrong name", "Name A", actual.get(0).get(0).getSite().getName());
         assertEquals("Wrong name", "Name B", actual.get(0).get(1).getSite().getName());
     }
 
     // since COPPA doesn't support this
     public void testGetAssociatedStudiesReturnsListOfEmptyLists() throws Exception {
         List<List<StudySite>> actual = provider.getAssociatedStudies(Arrays.asList(
             Fixtures.createSite("A"),
             Fixtures.createSite("B"),
             Fixtures.createSite("C")
         ));
 
         assertEquals("Wrong number of results", 3, actual.size());
         assertEquals("Matches for A not empty", 0, actual.get(0).size());
         assertEquals("Matches for B not empty", 0, actual.get(1).size());
         assertEquals("Matches for C not empty", 0, actual.get(2).size());
     }
 
     /////////////// Helper Methods
     private gov.nih.nci.coppa.services.pa.StudySite coppaStudySite(String extension, ResearchOrganization organizations) {
         gov.nih.nci.coppa.services.pa.StudySite studySite =
             new gov.nih.nci.coppa.services.pa.StudySite();
 
         II ii = new II();
         ii.setExtension(extension);
         studySite.setIdentifier(ii);
 
         studySite.setResearchOrganization(organizations.getPlayerIdentifier());
         return studySite;
     }
 
     private ResearchOrganization coppaResearchOrg(String extension, String playerExtension) {
         ResearchOrganization ro = new ResearchOrganization();
 
         DSETII dsetti = new DSETII();
         dsetti.setControlActExtension(extension);
 
         II ii = new II();
         ii.setExtension(playerExtension);
 
         ro.setPlayerIdentifier(ii);
 
         return ro;
     }
 
     private Study pscStudy(String extensionSecondaryIdentifier) {
         Study study = new Study();
 
         StudySecondaryIdentifier i = new StudySecondaryIdentifier ();
        i.setType(CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE);
         i.setValue(extensionSecondaryIdentifier);
         study.addSecondaryIdentifier(i);
 
         return study;
     }
 
      private Organization coppaOrganization(String name, String iiValue) {
         Organization org = new Organization();
 
         ENON n = new ENON();
         ENXP namePart = new ENXP();
         namePart.setType(EntityNamePartType.DEL);
         namePart.setValue(name);
         n.getPart().add(namePart);
         org.setName(n);
 
         gov.nih.nci.coppa.po.Id id = new gov.nih.nci.coppa.po.Id();
         id.setRoot("ROOT");
         id.setExtension(iiValue);
         org.setIdentifier(id);
 
         return org;
     }
 }
