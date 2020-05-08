 package cmput301.f13t01.createyourownadventure.test;
 
 import java.util.ArrayList;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import cmput301.f13t01.createyourownadventure.Choice;
 import cmput301.f13t01.createyourownadventure.ChoiceMap;
 
 public class testChoiceMap extends TestCase {
 
         ChoiceMap choicemap;
         
         @Before
         public void setUp() throws Exception {
                 
                 super.setUp();
                 this.choicemap = new ChoiceMap();
         }
 
         @Test
         public void testAddChoice() {
                 choicemap.addChoice(1, new Choice(1, 2, "Hello"));
                 ArrayList<Choice> testList = choicemap.getChoices(1);
                 Choice choice = testList.get(0);
                 assertTrue(choice.getSourceId() == 1);
                 assertTrue(choice.getDestinationId() == 2);
                 
                 choicemap.addChoice(1, new Choice(1, 3, "Hello Again"));
                 testList = choicemap.getChoices(1);
                 choice = testList.get(1);
                 assertTrue(choice.getSourceId() == 1);
                 assertTrue(choice.getDestinationId() == 3);
                 choice = testList.get(0);
                 assertTrue(choice.getSourceId() == 1);
                 assertTrue(choice.getDestinationId() == 2);
                 
                 testList = choicemap.getChoices(2);
                assertTrue(testList.size() == 0);
         }
         
         @Test
         public void testDeleteChoice() {
                 choicemap.addChoice(1, new Choice(1, 2, "Hello"));
                 choicemap.addChoice(1, new Choice(1, 3, "Hello again"));
                 choicemap.addChoice(1, new Choice(1, 4, "Hello goodbye"));
                 assertTrue(choicemap.deleteChoice(1, 1));
                 ArrayList<Choice> testList = choicemap.getChoices(1);
                 Choice choice = testList.get(1);
                 assertTrue(choice.getSourceId() == 1);
                 assertTrue(choice.getDestinationId() == 4);
         }
         
         @Test
         public void testUpdateChoice() {
                 choicemap.addChoice(1, new Choice(1, 2, "Hello"));
                 choicemap.addChoice(1, new Choice(1, 3, "Hello again"));
                 choicemap.addChoice(1, new Choice(1, 4, "Hello goodbye"));
                 assertTrue(choicemap.updateChoice(1, 1, new Choice(1, 5, "Who am I?")));
                 ArrayList<Choice> testList = choicemap.getChoices(1);
                 Choice choice = testList.get(1);
                 assertTrue(choice.getSourceId() == 1);
                 assertTrue(choice.getDestinationId() == 5);
         }
         
         @Test
         public void testCleanFragmentReferences() {
                 choicemap.addChoice(1, new Choice(1, 2, "Hello"));
                 choicemap.addChoice(2, new Choice(2, 3, "What?"));
                 choicemap.addChoice(2, new Choice(2, 4, "Hey!"));
                 choicemap.addChoice(2, new Choice(2, 4, "Hey2!"));
                 choicemap.addChoice(4, new Choice(4, 1, "Haha!"));
                 //Should remove all choices referencing fragment 4
                 choicemap.cleanFragmentReferences(4);
                 ArrayList<Choice> testList = choicemap.getChoices(4);
                assertTrue(testList.size() == 0);
                 testList = choicemap.getChoices(2);
                 assertTrue(testList.size() == 1);
         }
 
 }
