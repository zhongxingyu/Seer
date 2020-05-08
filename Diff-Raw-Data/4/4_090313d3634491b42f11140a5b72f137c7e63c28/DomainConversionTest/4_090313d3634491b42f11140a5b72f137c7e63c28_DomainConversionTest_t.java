 package net.straininfo2.grs.idloader.bioproject.xmlparsing;
 
 import net.straininfo2.grs.idloader.bioproject.bindings.Project;
 import net.straininfo2.grs.idloader.bioproject.domain.BioProject;
 import net.straininfo2.grs.idloader.bioproject.domain.OrganismEnvironment;
 import net.straininfo2.grs.idloader.bioproject.domain.ProjectRelevance;
 import net.straininfo2.grs.idloader.bioproject.domain.Publication;
 import org.junit.Before;
 import org.junit.Test;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 import static net.straininfo2.grs.idloader.bioproject.domain.Archive.NCBI;
 
 /**
  * Tests for various aspects of the conversion to domain objects.
  */
 public class DomainConversionTest {
 
     private static Project retrieveBorreliaProject() throws Exception {
         // SAX parses in document order, so we know which one this is
         return DocumentChunkerTest.parseBioProjectFile().get(0).getProject().getProject();
     }
 
     private static Project retrieveBordetellaProject() throws Exception {
         return DocumentChunkerTest.parseBioProjectFile().get(1).getProject().getProject();
     }
 
     private static Project retrieveAtribeusProject() throws Exception {
         return DocumentChunkerTest.parseBioProjectFile().get(2).getProject().getProject();
     }
 
     private static Project retrieveHmpProject() throws Exception {
         return DocumentChunkerTest.parseBioProjectFile().get(3).getProject().getProject();
     }
 
     private BioProject project;
 
     private DomainConverter converter;
 
     @Before
     public void setUp() {
         this.project = new BioProject();
         this.converter = new DomainConverter();
     }
 
     @Test
     public void testIdentifierConversion() throws Exception {
         converter.addIdentifiers(project, retrieveBorreliaProject().getProjectID());
         assertEquals("PRJNA3", project.getAccession());
         assertEquals(3, project.getProjectId());
         assertEquals(NCBI, project.getArchive());
     }
 
     @Test
     public void testDescriptionConversion() throws Exception {
         converter.addDescription(project, retrieveBorreliaProject().getProjectDescr());
         assertEquals("Borrelia burgdorferi B31", project.getName());
         assertEquals("Causes Lyme disease", project.getTitle());
         assertTrue(project.getDescription().contains("ATCC 35210"));
         // external links, locus tag prefix and release date not parsed
         // "RefSeq" tag not used here
     }
 
     @Test
     public void testRelevanceConversion() throws Exception {
         converter.addRelevanceFields(project, retrieveBordetellaProject().getProjectDescr().getRelevance());
         assertTrue(project.getProjectRelevance() != null);
         assertEquals(1, project.getProjectRelevance().size());
         assertEquals(ProjectRelevance.RelevantField.MEDICAL, project.getProjectRelevance().iterator().next().getRelevantField());
     }
 
     @Test
     public void testLocusTags() throws Exception {
         converter.addLocusTags(project, retrieveBorreliaProject().getProjectDescr().getLocusTagPrefixes());
         assertEquals("BB", project.getLocusTagPrefixes().iterator().next());
     }
 
     @Test
     public void testPublications() throws Exception {
         converter.addPublications(project, retrieveAtribeusProject().getProjectDescr().getPublications());
         Publication publication = project.getPublications().iterator().next();
         assertEquals("PLoS One.", publication.getJournalTitle());
         assertEquals("Schountz" ,publication.getAuthors().get(0).getLastName());
     }
 
     @Test
     public void testExternalLinks() throws Exception {
         converter.addLinks(project, retrieveHmpProject().getProjectDescr().getExternalLinks());
         assertEquals(3, project.getExternalLinks().size());
         assertEquals(1, project.getCrossReferences().size());
         assertEquals("phs000228", project.getCrossReferences().iterator().next().getDbId());
     }
 
     @Test
     public void testOrganismConversion() throws Exception {
         converter.addTypeSpecificInformation(project, retrieveBorreliaProject().getProjectType());
         assertEquals(Boolean.FALSE, project.retrieveOrganism().getMorphology().getMotility());
         assertTrue(project.retrieveOrganism().getEnvironment().getOxygenReq() == OrganismEnvironment.OxygenReq.eMicroaerophilic);
     }
 
 }
