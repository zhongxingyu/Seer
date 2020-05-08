 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.verifyZeroInteractions;
 
 @RunWith(MockitoJUnitRunner.class)
 public class PlanningLoaderTest {
   PlanningLoader loader = new PlanningLoader();
 
   @Mock
   Planning planning;
 
   @Test
   public void should_create_talks() {
     String json = "{days:[{day:DAY,slots:[{slot:SLOT,talks:[{id:TALK}]}]}]}";
 
     loader.createTalks(planning, json);
 
    verify(planning).createTalk("TALK", "DAY-SLOT");
   }
 
   @Test
   public void should_ignore_empty_json() {
     String emptyJson = "";
 
     loader.createTalks(planning, emptyJson);
 
     verifyZeroInteractions(planning);
   }
 }
