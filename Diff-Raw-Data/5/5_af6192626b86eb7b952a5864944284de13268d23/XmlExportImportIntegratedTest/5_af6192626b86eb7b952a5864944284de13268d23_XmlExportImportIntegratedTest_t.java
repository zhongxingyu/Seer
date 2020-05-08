 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Child;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
 import edu.northwestern.bioinformatics.studycalendar.service.importer.TemplateImportService;
 import edu.nwu.bioinformatics.commons.DateUtils;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Simulates a round trip XML export and import.
  *
  * @author Rhett Sutphin
  */
 public class XmlExportImportIntegratedTest extends DaoTestCase {
     private StudyService studyService
         = (StudyService) getApplicationContext().getBean("studyService");
     private AmendmentService amendmentService
         = (AmendmentService) getApplicationContext().getBean("amendmentService");
     private DeltaService deltaService
         = (DeltaService) getApplicationContext().getBean("deltaService");
     private TemplateImportService templateImportService
         = (TemplateImportService) getApplicationContext().getBean("templateImportService");
 
     private StudyDao studyDao
         = (StudyDao) getApplicationContext().getBean("studyDao");
     private ActivityDao activityDao
         = (ActivityDao) getApplicationContext().getBean("activityDao");
     private EpochDao epochDao
         = (EpochDao) getApplicationContext().getBean("epochDao");
     private StudyXmlSerializer serializer
         = (StudyXmlSerializer) getApplicationContext().getBean("studyXmlSerializer");
 
     private int studyId;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         Study created = TemplateSkeletonCreator.BASIC.create("Exportable");
         Add add = (Add) created.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0);
         Period period = createPeriod(1, 7, 4);
         period.addPlannedActivity(createPlannedActivity(activityDao.getById(-1), 2));
         ((Epoch) add.getChild()).getStudySegments().get(0).addPeriod(period);
         studyService.save(created);
 
         interruptSession();
 
         studyId = created.getId();
     }
 
     private InputStream export() {
         return export(null);
     }
 
     private InputStream export(Study study) {
         String xml = serializer.createDocumentString(study == null ? reload() : study);
         ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
         interruptSession();
         return input;
     }
 
     private Study reimport() {
         return doImport(export());
     }
 
     private Study doImport(InputStream export) {
         templateImportService.readAndSaveTemplate(export);
         interruptSession();
         return reload();
     }
 
     private Study reload() {
         Study study = studyDao.getById(studyId);
         assertNotNull("Test setup failure: could not reload using expected ID", study);
         return study;
     }
 
     public void testExportImportWithDevAmendmentOnly() throws Exception {
         Study actual = reimport();
         assertNotNull("Dev amendment missing", actual.getDevelopmentAmendment());
         assertEquals("Dev amendment should be initial", Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME,
             actual.getDevelopmentAmendment().getName());
         assertNull("Should be no released amendments", actual.getAmendment());
 
         Add add = (Add) actual.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0);
         assertNotNull("Add not found", add);
         Child<?> child = deltaService.findChangeChild(add);
         assertTrue(child instanceof Epoch);
         Epoch actualEpoch = (Epoch) child;
         assertEquals("Wrong epoch", "Treatment", actualEpoch.getName());
         // assuming everything else
     }
 
     public void testExportImportWithReorder() throws Exception {
         Study study = studyDao.getById(studyId);
         Integer childToReorder = ((Add) study.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0)).getChildId();
         Reorder reorder = Reorder.create(epochDao.getById(childToReorder), 0, 1);
         reorder.setGridId("2c845525-46cb-4c05-a0d8-9de0e866b61c");
         study.getDevelopmentAmendment().getDeltas().get(0).addChange(reorder);
         Study actual = reimport();
         Integer childToReorder1 = ((Add) actual.getDevelopmentAmendment().getDeltas().get(0).getChanges().get(0)).getChildId();
         Reorder reorder1 = Reorder.create(epochDao.getById(childToReorder1),0,1);
         assertNotNull("Reorder not found",reorder1);
     }
 
     public void testExportImportWithSingleReleasedAmendment() throws Exception {
         amendmentService.amend(reload());
 
         Study actual = reimport();
         assertNull("Should have no dev amendment", actual.getDevelopmentAmendment());
         assertNotNull("Should have a released amendment", actual.getAmendment());
         assertEquals("Released amendment should be initial", Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME, actual.getAmendment().getName());
 
         Add add = (Add) actual.getAmendment().getDeltas().get(0).getChanges().get(0);
         assertNotNull("Add not found", add);
         Child<?> child = deltaService.findChangeChild(add);
         assertTrue(child instanceof Epoch);
         Epoch actualEpoch = (Epoch) child;
         assertEquals("Wrong epoch", "Treatment", actualEpoch.getName());
     }
 
     public void testExportImportWithReleasedAmendmentAndNewReleasedAmendment() throws Exception {
         amendmentService.amend(reload());
 
         Study expectedExport = reload().transientClone();
         Epoch e1 = expectedExport.getPlannedCalendar().getEpochs().get(1);
         assertEquals("Test setup failure -- expected 1 segment in epoch 1 to start", 1, e1.getStudySegments().size());
         Amendment dev = createAmendment("A0", DateUtils.createDate(2008, Calendar.JANUARY, 3));
        StudySegment s1 = setId(3, Fixtures.createNamedInstance("New Segment", StudySegment.class));
        s1.setGridId("SS");
        Add newSegment = setId(5, Add.create(s1));
        newSegment.setGridId("ADD");
         dev.addDelta(setGridId("D", Delta.createDeltaFor(e1, newSegment)));
         expectedExport.setDevelopmentAmendment(dev);
         Fixtures.amend(expectedExport);
 
         InputStream xml = export(expectedExport);
         Study actual = doImport(xml);
 
         assertNull("Should have no dev amendment", actual.getDevelopmentAmendment());
         assertNotNull("Should have a released amendment", actual.getAmendment());
         assertEquals("Released amendment should be A0", "A0", actual.getAmendment().getName());
         assertNotNull("Should have two released amendments, actually", actual.getAmendment().getPreviousAmendment());
         assertEquals("Prev amendment should be original", Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME,
             actual.getAmendment().getPreviousAmendment().getName());
 
         Epoch actualE1 = actual.getPlannedCalendar().getEpochs().get(1);
         assertEquals("Segment not added to live plan tree", 2, actualE1.getStudySegments().size());
         assertEquals("Wrong segment added to live plan tree", "New Segment", actualE1.getStudySegments().get(1).getName());
     }
 }
