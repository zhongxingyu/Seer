 package edu.northwestern.bioinformatics.studycalendar.domain.delta;
 
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
 
 import java.util.Arrays;
 
 /**
  * @author Rhett Sutphin
  */
 public class DeltaTest extends StudyCalendarTestCase {
     public void testDeltaForPlannedCalendar() throws Exception {
         assertDeltaFor(new PlannedCalendar(), PlannedCalendarDelta.class);
     }
 
     public void testDeltaForEpoch() throws Exception {
         assertDeltaFor(new Epoch(), EpochDelta.class);
     }
 
     public void testDeltaForArm() throws Exception {
         assertDeltaFor(new Arm(), ArmDelta.class);
     }
 
     public void testDeltaForPeriod() throws Exception {
         assertDeltaFor(new Period(), PeriodDelta.class);
     }
 
     public void testDeltaForPlannedEvent() throws Exception {
         assertDeltaFor(new PlannedEvent(), PlannedEventDelta.class);
     }
 
     public void testAddChange() throws Exception {
         Delta<?> delta = new EpochDelta();
         assertEquals("Test setup failure", 0, delta.getChanges().size());
         delta.addChange(PropertyChange.create("nil", "null", "{}"));
         assertEquals(1, delta.getChanges().size());
     }
 
     public void testRemoveChangeNotifiesSiblings() throws Exception {
         Change sib0 = registerMockFor(Change.class);
         Change sib1 = registerMockFor(Change.class);
         Change sib2 = registerMockFor(Change.class);
 
         Delta<?> delta = new EpochDelta();
         delta.getChangesInternal().addAll(Arrays.asList(sib0, sib1, sib2));
 
         sib0.siblingDeleted(delta, sib1, 1, 0);
         sib2.siblingDeleted(delta, sib1, 1, 2);
         sib1.setDelta(null);
 
         replayMocks();
         delta.removeChange(sib1);
         verifyMocks();
     }
 
     public void testRemoveChangeDoesNotNotifyIfTheRemovedChangeWasNotInTheDelta() throws Exception {
         Change sib0 = registerMockFor(Change.class);
         Change sib1 = registerMockFor(Change.class);
         Change sib2 = registerMockFor(Change.class);
 
         Delta<?> delta = new EpochDelta();
         delta.getChangesInternal().addAll(Arrays.asList(sib0, sib2));
 
         replayMocks();
         delta.removeChange(sib1);
         verifyMocks();
     }
 
     public void testGetChangesReturnsReadOnlyList() throws Exception {
         Delta<?> delta = new EpochDelta();
         try {
             delta.getChanges().add(PropertyChange.create("aleph", "i", "I"));
             fail("Exception not thrown");
         } catch (UnsupportedOperationException e) {
             // good
         }
     }
 
    private static <T extends PlanTreeNode<?>> void assertDeltaFor(T node, Class<?> expectedClass) {
        Delta<T> actual = Delta.createDeltaFor(node);
         assertNotNull(actual);
         assertEquals("Wrong class", expectedClass, actual.getClass());
     }
 }
