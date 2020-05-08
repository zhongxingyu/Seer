 package commons.sim.jeevent;
 
 import static org.junit.Assert.assertEquals;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 
 import util.CleanConfigurationTest;
 
 public class JEEventTest extends CleanConfigurationTest {
 
 	@Test
 	public void testCompareToWithDifferentTimes(){
 		JEEventHandler handler = EasyMock.createStrictMock(JEAbstractEventHandler.class);
 		EasyMock.expect(handler.getHandlerId()).andReturn(1).times(2);
 		EasyMock.replay(handler);
 		JEEvent eventA = new JEEvent(JEEventType.READWORKLOAD, handler, 1000);
 		JEEvent eventB = new JEEvent(JEEventType.READWORKLOAD, handler, 2000);
 		assertEquals(-1, eventA.compareTo(eventB));
 		assertEquals(1, eventB.compareTo(eventA));
 		EasyMock.verify(handler);
 	}
 	
 	@Test
 	public void testCompareToWithSameTimeDifferentType(){
 		JEEventHandler handler = EasyMock.createStrictMock(JEAbstractEventHandler.class);
 		EasyMock.expect(handler.getHandlerId()).andReturn(1).times(2);
 		EasyMock.replay(handler);
 		JEEvent eventA = new JEEvent(JEEventType.READWORKLOAD, handler, 1000);
 		JEEvent eventB = new JEEvent(JEEventType.NEWREQUEST, handler, 1000);
		assertEquals(-6, eventA.compareTo(eventB));
		assertEquals(6, eventB.compareTo(eventA));
 		EasyMock.verify(handler);
 	}
 	
 	@Test
 	public void testCompareToWithDifferentTimeAndDifferentTypes(){
 		JEEventHandler handler = EasyMock.createStrictMock(JEAbstractEventHandler.class);
 		EasyMock.expect(handler.getHandlerId()).andReturn(1).times(2);
 		EasyMock.replay(handler);
 		JEEvent eventA = new JEEvent(JEEventType.READWORKLOAD, handler, 1000);
 		JEEvent eventB = new JEEvent(JEEventType.NEWREQUEST, handler, 2000);
 		assertEquals(-1, eventA.compareTo(eventB));
 		assertEquals(1, eventB.compareTo(eventA));
 		EasyMock.verify(handler);
 	}
 }
