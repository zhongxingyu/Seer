 /**
  * Copyright (C) 2009 Universidade Federal de Campina Grande
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package manelsim;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import manelsim.Time.Unit;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Patrick Maia
  */
 public class EventSourceMultiplexerTest {
 	EventSource [] eventSources;
 	private EventSourceMultiplexer eventsMultiplexer;
 	
 	@Before
 	public void setup() {
 		eventSources = new EventSource[0];
 		eventsMultiplexer = new EventSourceMultiplexer(eventSources);
 	}
 
 	@Test
 	public void testAddGetEvent() {
 		TestEvent event = new TestEvent(new Time(0, Unit.SECONDS));
 		
 		eventsMultiplexer.addNewEvent(event);
 		assertEquals(event, eventsMultiplexer.getNextEvent());
 		assertNull(eventsMultiplexer.getNextEvent());
 	}
 	
 	@Test
 	public void testEventsAreDeliveredInOrder() {
 		TestEvent event0 = new TestEvent(new Time(0, Unit.SECONDS));
 		TestEvent event1 = new TestEvent(new Time(1, Unit.SECONDS));
 		
 		eventsMultiplexer.addNewEvent(event1);
 		eventsMultiplexer.addNewEvent(event0);
 		
 		assertEquals(event0, eventsMultiplexer.getNextEvent());
 		assertEquals(event1, eventsMultiplexer.getNextEvent());
 		assertNull(eventsMultiplexer.getNextEvent());
 	}
 	
 	@Test
 	public void testEventsAreDeliveredInOrder1() { //with some events coming from an EventSource
 		TestEvent event0 = new TestEvent(new Time(0, Unit.SECONDS));
 		TestEvent event1 = new TestEvent(new Time(1, Unit.SECONDS));
 		TestEvent event2 = new TestEvent(new Time(2, Unit.SECONDS));
 		TestEvent event3 = new TestEvent(new Time(3, Unit.SECONDS));
 		
 		Event [] events = {event1, event3};
 		eventSources = new EventSource[1];
 		eventSources[0] = new TestEventSource(events);
 		eventsMultiplexer = new EventSourceMultiplexer(eventSources);
 
 		eventsMultiplexer.addNewEvent(event0);
 		eventsMultiplexer.addNewEvent(event2);
 		
 		assertEquals(event0, eventsMultiplexer.getNextEvent());
 		assertEquals(event1, eventsMultiplexer.getNextEvent());
 		assertEquals(event2, eventsMultiplexer.getNextEvent());
 		assertEquals(event3, eventsMultiplexer.getNextEvent());
 		assertNull(eventsMultiplexer.getNextEvent());
 	}
 	
 	@Test
 	public void testSameTimeEventsObeyFIFO() {
 		TestEvent event0 = new TestEvent(new Time(1, Unit.SECONDS));
 		TestEvent event1 = new TestEvent(new Time(1, Unit.SECONDS));
 		
 		eventsMultiplexer.addNewEvent(event0);
 		eventsMultiplexer.addNewEvent(event1);
 		
 		assertEquals(event0, eventsMultiplexer.getNextEvent());
 		assertEquals(event1, eventsMultiplexer.getNextEvent());
 		assertNull(eventsMultiplexer.getNextEvent());
 	}
 	
 	@Test
 	public void testSameTimeEventsObeyFIFO1() {
 		TestEvent event0 = new TestEvent(new Time(1, Unit.SECONDS));
 		TestEvent event1 = new TestEvent(new Time(1, Unit.SECONDS));
 		TestEvent event2 = new TestEvent(new Time(1, Unit.SECONDS));
 		TestEvent event3 = new TestEvent(new Time(1, Unit.SECONDS));
 		
 		Event [] events = {event0, event1};
 		eventSources = new EventSource[1];
 		eventSources[0] = new TestEventSource(events);
 		eventsMultiplexer = new EventSourceMultiplexer(eventSources);
 		
 		eventsMultiplexer.addNewEvent(event2);
 		eventsMultiplexer.addNewEvent(event3);
 
 		assertEquals(event0, eventsMultiplexer.getNextEvent());
 		assertEquals(event1, eventsMultiplexer.getNextEvent());
 		assertEquals(event2, eventsMultiplexer.getNextEvent());
 		assertEquals(event3, eventsMultiplexer.getNextEvent());
 		assertNull(eventsMultiplexer.getNextEvent());
 	}
 	
 	
 	@Test
 	public void testEarlyEventsScheduledLaterAreDeliveredFirst() {
 		TestEvent event0 = new TestEvent(new Time(0, Unit.SECONDS));
 		TestEvent event1 = new TestEvent(new Time(1, Unit.SECONDS));
 		TestEvent event2 = new TestEvent(new Time(2, Unit.SECONDS));
 		
 		Event [] events = {event0, event2};
 		eventSources = new EventSource[1];
 		eventSources[0] = new TestEventSource(events);
 		eventsMultiplexer = new EventSourceMultiplexer(eventSources);
 		
 		eventsMultiplexer.addNewEvent(event1);
 
 		assertEquals(event0, eventsMultiplexer.getNextEvent());
 		assertEquals(event1, eventsMultiplexer.getNextEvent());
 		assertEquals(event2, eventsMultiplexer.getNextEvent());
 		assertNull(eventsMultiplexer.getNextEvent());
 	}
 	
 	private static class TestEvent extends Event {
 		public TestEvent(Time scheduledTime) {
 			super(scheduledTime);
 		}
 		@Override
 		public void process() { /* does nothing */ }
		@Override
		public boolean equals(Object obj) {
			return obj == this; // without this all same time TestEvents would be considered the same
		}
 	}
 	
 	private static class TestEventSource implements EventSource {
 		private Event [] events;
 		private int count = 0;
 		public TestEventSource(Event ... events) {
 			this.events = events;
 		}
 		@Override
 		public Event getNextEvent() {
 			return count < events.length ? events[count++] : null;
 		}
 	}
 }
