 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import static edu.nwu.bioinformatics.commons.testing.CoreTestCase.*;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditEvent;
 import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataReference;
 import edu.northwestern.bioinformatics.studycalendar.domain.auditing.Operation;
 import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
 
 import java.util.List;
 import java.util.Collection;
 import java.util.ArrayList;
 
 /**
  * @author Rhett Sutphin
  */
 public class StudyDaoTest extends ContextDaoTestCase<StudyDao> {
     private StudyDao dao = (StudyDao) getApplicationContext().getBean("studyDao");
 
     public void testGetById() throws Exception {
         Study study = dao.getById(-100);
         assertIsTestStudy100(study);
     }
     
     public void testGetByGridId() throws Exception {
         Study actual = dao.getByGridId("long-GUID-string");
         assertIsTestStudy100(actual);
     }
 
     public void testGetByGridIdByTemplate() throws Exception {
         Study actual = dao.getByGridId(Fixtures.setGridId("long-GUID-string", new Study()));
         assertIsTestStudy100(actual);
     }
 
     public void testGetAll() throws Exception {
         List<Study> actual = dao.getAll();
         assertEquals(2, actual.size());
         Collection<Integer> ids = DomainObjectTools.collectIds(actual);
         assertContains("Wrong study found", ids, -100);
         assertContains("Wrong study found", ids, -101);
     }
 
     public void testSaveWithAmended() throws Exception {
         Study study = dao.getById(-100);
         Boolean amended = study.getAmended();
         study.setAmended(true);
         dao.save(study);
         Study studyAfterAmended = dao.getById(-100);
         assertEquals("Not same ", false, (boolean) amended);
         assertEquals("Not amended ", true, (boolean) studyAfterAmended.getAmended());
     }
 
     public void testSaveNewStudy() throws Exception {
         Integer savedId;
         {
             Study study = new Study();
             study.setName("New study");
             dao.save(study);
             savedId = study.getId();
             assertNotNull("The saved study didn't get an id", savedId);
         }
 
         interruptSession();
 
         {
             Study loaded = dao.getById(savedId);
             assertNotNull("Could not reload study with id " + savedId, loaded);
             assertEquals("Wrong name", "New study", loaded.getName());
             assertNotNull("Grid ID not automatically added", loaded.getGridId());
         }
     }
 
     public void testSaveNewStudyIsAudited() throws Exception {
         Integer savedId;
         {
             Study study = new Study();
             study.setName("New study");
             dao.save(study);
             savedId = study.getId();
             assertNotNull("The saved study didn't get an id", savedId);
         }
 
         interruptSession();
 
         List<DataAuditEvent> trail = getAuditDao().getAuditTrail(new DataReference(Study.class, savedId));
         assertEquals("Wrong number of events in trail", 1, trail.size());
         DataAuditEvent event = trail.get(0);
         assertEquals("Wrong operation", Operation.CREATE, event.getOperation());
     }
     
     public void testGetStudyParticipantAssigments() throws Exception {
         List<StudyParticipantAssignment> actual = dao.getAssignmentsForStudy(-100);
         assertEquals("Wrong number of assigments", 2, actual.size());
         List<Integer> ids = new ArrayList<Integer>(DomainObjectTools.collectIds(actual));
 
         assertContains("Missing expected assignment", ids, -10);
         assertContains("Missing expected assignment", ids, -11);
         assertEquals("Assignments in wrong order", -10, (int) ids.get(0));
         assertEquals("Assignments in wrong order", -11, (int) ids.get(1));
     }
 
     private void assertIsTestStudy100(Study actual) {
         assertNotNull("Could not locate", actual);
         assertEquals("Wrong id", -100, (int) actual.getId());
         assertEquals("Wrong name", "First Study", actual.getName());
         assertEquals("Wrong grid ID", "long-GUID-string", actual.getGridId());
         assertEquals("Wrong protocol auth id", "NCI-IS-WATCHING", actual.getProtocolAuthorityId());
     }
 
 }
