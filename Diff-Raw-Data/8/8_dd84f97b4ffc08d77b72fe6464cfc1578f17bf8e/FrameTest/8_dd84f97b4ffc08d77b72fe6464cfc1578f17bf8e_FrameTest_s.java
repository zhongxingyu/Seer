 import static org.junit.Assert.*;
 
 import java.awt.Frame;
 import java.awt.geom.Point2D;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class FrameTest {
 
 	@Test
 	public void testThatJUnitLoads(){
 		assertTrue("JUnit did not load properly...", true);
 	}
 	
 	@Test
 	public void testThatFrameCanBeInstantiated(){
 		Frame f = new Frame(0);
 		assertNotNull(f);
 	}
 	
 	@Test
	public void testThatFrameHoldsStableAround60FPS{
 		Frame f = new Frame(60);
 		try{Thread.sleep(10);}
 		catch(InterruptedException ie){
 			// Sleep Interrupted
 		}
 		assertThat(50, lessThan(f.getFPS()));
 		assertThat(70, greatherThan(f.getFPS()));
 		assertThat(50, lessThan(f.getFPS()));
 		assertThat(70, greatherThan(f.getFPS()));
 		assertThat(50, lessThan(f.getFPS()));
 		assertThat(70, greatherThan(f.getFPS()));
 		assertThat(50, lessThan(f.getFPS()));
 		assertThat(70, greatherThan(f.getFPS()));
 	}
 	
 	@Test
	public void testThatSleepSkipsIsMinimallized{
 		Frame f = new Frame(1000000);
 		try{Thread.sleep(10);}
 		catch(InterruptedException ie){
 			// Sleep Interrupted
 		}
 		assertThat(10, greaterThan(f.getSleepSkips()));
 	}
 	
 	@Test
	public void testThatFramesAreSkipedToDecreaseLagIsMinimalized{
 		Frame f = new Frame(1000000);
 		try{Thread.sleep(10);}
 		catch(InterruptedException ie){
 			// Sleep Interrupted
 		}
 		assertThat(0, lessThan(f.getFrameSkips()));
 	}
 }
