 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created by Silvestrov Ilya
  * Date: Oct 9, 2010
  * Time: 12:30:09 AM
  */
 public class PlanSelectionWithFindValuesScorerTest {
     private List<Plan> plans = null;
     private Timer timer = new Timer() {
         @Override
         public boolean shouldStop() {
             return false;
         }
     };
 
     @Before
     public void setup() {
         plans = new LinkedList<Plan>();
         for (int i = 0; i < 51; ++i) {
             SquareMatrix tr = new SquareMatrix(2);
             tr.set(0, 1, i);
             Plan plan = new Plan();
             plan.addTransitions(tr);
             plans.add(plan);
         }
 
         //Added repeated value for 20 to include equals comparison in population in PlanSelection
         SquareMatrix tr = new SquareMatrix(2);
         tr.set(0, 1, 20);
         Plan plan = new Plan();
         plan.addTransitions(tr);
         plans.add(plan);
     }
 
     @Test
     public void test() {
         //Warm up run
         PlanSelection selection;
         selection = new PlanSelection(timer);
         selection.setScorer(new FindValuesScorer(20, 50, 70, 100));
         selection.doPlanSelection(null, new LinkedList<Plan>(plans));
 
         //Run to measure time and assure that max value is 100 as there exists
         //intermediate value greater or equal than 50
         LinkedList<Plan> plans = new LinkedList<Plan>(this.plans);
         selection = new PlanSelection(timer);
         selection.setScorer(new FindValuesScorer(20, 50, 70, 100));
 
         long time = System.currentTimeMillis();
         selection.doPlanSelection(null, plans);
         time = System.currentTimeMillis() - time;
         System.out.println(time);
 
         Assert.assertEquals(100, selection.getBestScore());
         Assert.assertEquals(100, selection.getBestPlan().transitions().iterator().next().get(0, 1));
 
         //This run should have max value 20 as 20 < 50
         plans = new LinkedList<Plan>(this.plans);
         selection = new PlanSelection();
         selection.setScorer(new FindValuesScorer(20, 100));
         time = System.currentTimeMillis();
         MyBot.start = time;
         selection.doPlanSelection(null, plans);
 
         Assert.assertEquals(20, selection.getBestScore());
         Assert.assertEquals(20, selection.getBestPlan().transitions().iterator().next().get(0, 1));
     }
 
     @Test
     public void testSameScoredPlansAreOrdered() {
         PlanSelection selection;
         selection = new PlanSelection(timer);
         selection.setScorer(new Scorer() {
             @Override
             public long score(PlanetWarsState state, Plan plan) {
                 return 10;
             }
         });
         plans = new LinkedList<Plan>(this.plans);
         selection.doPlanSelection(null, plans);
 
         Assert.assertEquals(10, selection.getBestScore());
         Assert.assertEquals(0, selection.getBestPlan().transitions().iterator().next().get(0, 1));
     }
 
     @Test
     public void testTimeout() {
         PlanSelection selection;
         selection = new PlanSelection(new Timer() {
             @Override
             public boolean shouldStop() {
                 return true;
             }
         });
         plans = new LinkedList<Plan>(this.plans);
         selection.doPlanSelection(null, plans);
         assertEmptyPlanSelection(selection);
     }
 
     @Test
     public void testEmptyQueue() {
         PlanSelection selection;
         selection = new PlanSelection(timer);
         selection.setScorer(new Scorer() {
             @Override
             public long score(PlanetWarsState state, Plan plan) {
                 return 10;
             }
         });
 
         selection.doPlanSelection(null, Collections.<Plan>emptyList());
         assertEmptyPlanSelection(selection);
     }
 
     @Test
     public void testAllPlansBad() {
         PlanSelection selection;
         selection = new PlanSelection(timer);
         selection.setScorer(new Scorer() {
             @Override
             public long score(PlanetWarsState state, Plan plan) {
                return Integer.MIN_VALUE;
             }
         });
 
         selection.doPlanSelection(null, new LinkedList<Plan>(this.plans));
         assertEmptyPlanSelection(selection);
     }
 
     private void assertEmptyPlanSelection(PlanSelection selection) {
         Plan plan = selection.getBestPlan();
         Iterator<SquareMatrix> iter = plan.transitions().iterator();
         iter.next();
         Assert.assertFalse(iter.hasNext());
 
        Assert.assertEquals(Integer.MIN_VALUE, selection.getBestScore());
     }
 }
