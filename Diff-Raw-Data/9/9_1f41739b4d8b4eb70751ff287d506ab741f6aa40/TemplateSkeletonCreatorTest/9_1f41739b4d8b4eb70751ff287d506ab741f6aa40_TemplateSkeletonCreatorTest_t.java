 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
 
 import java.util.List;
 
 /**
  * @author Rhett Sutphin
  */
 public class TemplateSkeletonCreatorTest extends StudyCalendarTestCase {
 
     public void testBlank() throws Exception {
         assertBlankStudy(TemplateSkeletonCreator.BLANK.create());
     }
 
     public void testBasic() throws Exception {
         assertBasicStudy(TemplateSkeletonCreator.BASIC.create());
     }
     
     public static void assertBlankStudy(Study actual) {
         assertEquals("Wrong study name", "[Unnamed blank study]", actual.getName());
         assertNotNull(actual.getPlannedCalendar());
         Delta<?> actualDelta = assertHasSkeletonDevAmendment(actual);
 
         assertEquals("Wrong number of changes", 1, actualDelta.getChanges().size());
         assertIsAddEpochChange(actualDelta.getChanges().get(0), 0, "[Unnamed epoch]");
 
         assertEquals("Development amendment already applied", 0, actual.getPlannedCalendar().getEpochs().size());
     }
 
     public static void assertBasicStudy(Study actual) {
        assertEquals("Wrong study name for new study", "[ABC 1234]", actual.getName());
 
         Delta<?> actualDelta = assertHasSkeletonDevAmendment(actual);
 
         assertEquals("Wrong number of changes", 3, actualDelta.getChanges().size());
         assertIsAddEpochChange(actualDelta.getChanges().get(0), 0, "Screening");
         Epoch actualTreatment = assertIsAddEpochChange(actualDelta.getChanges().get(1), 1, "Treatment");
         assertIsAddEpochChange(actualDelta.getChanges().get(2), 2, "Follow up");
 
         PlannedCalendar calendar = actual.getPlannedCalendar();
 
         List<StudySegment> treatmentStudySegments = actualTreatment.getStudySegments();
         assertEquals("Wrong name for treatment study segment 0", "A", treatmentStudySegments.get(0).getName());
         assertEquals("Wrong name for treatment study segment 1", "B", treatmentStudySegments.get(1).getName());
         assertEquals("Wrong name for treatment study segment 2", "C", treatmentStudySegments.get(2).getName());
 
         assertEquals("Development amendment already applied", 0, calendar.getEpochs().size());
     }
 
     private static Delta<?> assertHasSkeletonDevAmendment(Study actual) {
         assertNotNull("No dev amendment", actual.getDevelopmentAmendment());
         assertEquals("Wrong number of deltas in dev amendment", 1, actual.getDevelopmentAmendment().getDeltas().size());
         Delta<?> actualDelta = actual.getDevelopmentAmendment().getDeltas().get(0);
         assertTrue("Wrong type of delta", PlannedCalendarDelta.class.isAssignableFrom(actualDelta.getClass()));
         assertNotNull("Delta has no node", actualDelta.getNode());
         assertSame("Delta has wrong node", actual.getPlannedCalendar(), actualDelta.getNode());
         return actualDelta;
     }
 
     private static Epoch assertIsAddEpochChange(Change change, Integer expectedIndex, String expectedName) {
         assertEquals("Wrong change action", ChangeAction.ADD, change.getAction());
 
         // This is two lines to work around a javac bug
         PlanTreeNode child = ((Add) change).getChild();
         Epoch added = (Epoch) child;
 
         assertNotNull("No added epoch", added);
         assertEquals("Wrong epoch name", expectedName, added.getName());
         assertEquals("Epoch added at wrong index", expectedIndex, ((Add) change).getIndex());
         return added;
     }
 }
