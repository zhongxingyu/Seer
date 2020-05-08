 /**
  * Purpose
  * @author aavisv
  * @created 4 Feb, 2010
  * $Id$
  */
 package thebutton.swing;
 
 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.fixture.JButtonFixture;
 import org.fest.swing.fixture.JTableFixture;
 import org.joda.time.Duration;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 import thebutton.track.TestingClock;
 import thebutton.track.TimeTracker;
 
 import java.util.ResourceBundle;
 
 public class ButtonBehaviour {
     private FrameFixture window;
     private ResourceBundle resourceBundle;
     private TestingClock clock;
     private TimeTracker timeTracker;
 
     @BeforeMethod
     public void setUp() {
         clock = new TestingClock();
         timeTracker = new TimeTracker(clock);
         resourceBundle = ButtonResources.lookupResources();
         ButtonFrame frame = GuiActionRunner.execute(new GuiQuery<ButtonFrame>() {
            @Override
             protected ButtonFrame executeInEDT() {
                 return new ButtonFrame(resourceBundle, timeTracker);
             }
         });
         window = new FrameFixture(frame);
         window.show(); // shows the frame to test
 
     }
 
     @Test
     public void findTheButton() {
         theButton().requireEnabled();
         theButton().requireFocused();
     }
 
     @Test
     public void findTheTracks() {
         theTracks().requireRowCount(0);
         theTracks().requireColumnCount(2);
     }
 
     private JTableFixture theTracks() {
         return window.table("the.track");
     }
 
     @Test
     public void oneClickOnTheButton() {
         theButton().click();
         theTracks().requireRowCount(0);
     }

     @Test
     public void twoClicksOnTheButton() {
         theButton().click();
         clock.advance(Duration.standardHours(1));
         theButton().click();
         theTracks().requireRowCount(1);
     }
 
     private JButtonFixture theButton() {
         return window.button("the.button");
     }
 
     @AfterMethod
     public void tearDown() {
         window.cleanUp();
     }
 }
