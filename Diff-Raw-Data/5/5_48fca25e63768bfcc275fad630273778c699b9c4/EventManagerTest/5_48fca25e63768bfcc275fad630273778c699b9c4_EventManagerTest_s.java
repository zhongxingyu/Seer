 package com.theminequest.MQTest.Core.EventsAPI;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
 import com.theminequest.MineQuest.EventsAPI.QEvent;
 
 public class EventManagerTest {
 	
 
 
 	@Before
 	public void setUp() throws Exception {
 		
 	}
 
 	@Test
 	public void testRegisterEvent() {
 		//fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetNewEvent() {
 		//fail("Not yet implemented");
 	}
 	
 	private class TestEvent extends QEvent {
 
 		private long milliseconds;
 		private long initialmilliseconds;
 		
 		public TestEvent() {
 			super(0, 1, "");
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see com.theminequest.MineQuest.EventsAPI.QEvent#parseDetails(java.lang.String[])
 		 * Basic Quest Event:
 		 * [0]: delay in milliseconds
 		 * [1]: task to trigger
 		 */
 		@Override
 		public void parseDetails(String[] details) {
 			milliseconds = 1000;
 			initialmilliseconds = System.currentTimeMillis();
 		}
 
 		@Override
 		public boolean conditions() {
 			if ((System.currentTimeMillis()-initialmilliseconds)<milliseconds)
 				return false;
 			return true;
 		}
 
 		@Override
 		public CompleteStatus action() {
 			return CompleteStatus.SUCCESS;
 		}
 		
 	}
 
 }
