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
 package ddg.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Random;
 
 import org.junit.Test;
 
 import ddg.emulator.EventParser;
 import ddg.emulator.FileSystemEventParser;
 import ddg.emulator.MultipleEventParser;
 import ddg.kernel.Event;
 import ddg.kernel.EventScheduler;
 import ddg.model.DDGClient;
 import ddg.model.Machine;
 
 /**
  * A suite of tests to the MultipleSourceEventParser class
  *
  * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
  */
 public class MultipleEventParserTest {
 	
 	
 	
 	@Test
 	public void eventOrderingTest() {
 		EventParser [] parsers = new EventParser[3];
 		
 		InputStream trace1 = new FakeTraceStream(0);
 		InputStream trace2 = new FakeTraceStream(60);
 		InputStream trace3 = new FakeTraceStream(30);
 		
 		EventScheduler scheduler = new EventScheduler();
 		
 		Machine machine1 = new Machine(scheduler, "cherne");
 		Machine machine2 = new Machine(scheduler, "palhaco");
 		Machine machine3 = new Machine(scheduler, "abelhinha");
 		
 		DDGClient client1 = new DDGClient(scheduler, 1, machine1, null);
 		DDGClient client2 = new DDGClient(scheduler, 2, machine2, null);
 		DDGClient client3 = new DDGClient(scheduler, 3, machine3, null);
 		
 		parsers[0] = new FileSystemEventParser(trace1, client1);
 		parsers[1] = new FileSystemEventParser(trace2, client2);
 		parsers[2] = new FileSystemEventParser(trace3, client3);
 		
 		EventParser multipleSourceParser = new MultipleEventParser(parsers);
 		
 		Event currentEvent = multipleSourceParser.getNextEvent();
 		Event nextEvent = null;
 		while((nextEvent = multipleSourceParser.getNextEvent()) != null) {
			assertTrue(currentEvent.getTheScheduledTime().isEarlierThan(nextEvent.getTheScheduledTime()));
 			currentEvent = nextEvent;
 		}
 	}
 	
 	@Test
 	public void eventsDeliveredCountTest() {
 		EventParser [] parsers = new EventParser[3];
 		
 		InputStream trace1 = new FakeTraceStream(50);
 		InputStream trace2 = new FakeTraceStream(1000);
 		InputStream trace3 = new FakeTraceStream(50);
 		
 		EventScheduler scheduler = new EventScheduler();
 		
 		Machine machine1 = new Machine(scheduler, "cherne");
 		Machine machine2 = new Machine(scheduler, "palhaco");
 		Machine machine3 = new Machine(scheduler, "abelhinha");
 		
 		DDGClient client1 = new DDGClient(scheduler, 1, machine1, null);
 		DDGClient client2 = new DDGClient(scheduler, 2, machine2, null);
 		DDGClient client3 = new DDGClient(scheduler, 3, machine3, null);
 		
 		parsers[0] = new FileSystemEventParser(trace1, client1);
 		parsers[1] = new FileSystemEventParser(trace2, client2);
 		parsers[2] = new FileSystemEventParser(trace3, client3);
 		
 		EventParser multipleSourceParser = new MultipleEventParser(parsers);
 		
 		int eventCount = 0;
 		while(multipleSourceParser.getNextEvent() != null) {
 			eventCount++;
 		}
 		
 		assertEquals(1100, eventCount);
 	}
 	
 	/**
 	 * 
 	 * An InputStream that simulates an InputStream over a trace file whose events are in the format expected by 
 	 * {@link FileSystemEventParser}. <code>numberOfEvents</code> events will be generated.  
 	 *
 	 * @author Patrick Maia - patrickjem@lsd.ufcg.edu.br
 	 */
 	private static class FakeTraceStream extends InputStream {
 		
 		private final String [] operations = {"read", "write", "open", "close"};
 		private final Random random = new Random();
 		
 		private int remainingEvents;
 		private long nextTimeStamp = System.currentTimeMillis();
 		private InputStream currentEventStream;
 		
 		public FakeTraceStream(int numberOfEvents) {
 			this.remainingEvents = numberOfEvents - 1;
 			this.currentEventStream = generateNextEventStream();
 		}
 
 		@Override
 		public int read() throws IOException {
 			
 			int nextByte = currentEventStream.read();
 
 			if(nextByte == -1) {
 				if(remainingEvents > 0) {
 					currentEventStream = generateNextEventStream();
 					remainingEvents--;	
 				} else {
 					return -1;
 				}
 			}
 			
 			return nextByte;
 		}
 		
 		//TODO I implemented this to generate lines similar to the ones found in the cleared SEER traces.
 		private InputStream generateNextEventStream() {
 			final String SEPARATOR = "\t";
 			final String LINE_SEPARATOR = "\n";
 			final int uniqueFileHandle = 33;
 			final int lengthReadOrWrite = 1024;
 			
 			StringBuilder strBuilder = new StringBuilder();
 			
 			String op = operations[random.nextInt(4)];
 			
 			strBuilder.append(op);
 			strBuilder.append(SEPARATOR);
 			
 			if(op.equals("open")) {
 				strBuilder.append("/home/unique/file");
 				strBuilder.append(SEPARATOR);
 			}
 			
 			strBuilder.append(nextTimeStamp);
 			strBuilder.append(SEPARATOR);
 			strBuilder.append(uniqueFileHandle);
 			strBuilder.append(SEPARATOR);
 			
 			if(op.equals("read") || op.equals("write")) {
 				strBuilder.append(lengthReadOrWrite);
 			}
 			
 			strBuilder.append(LINE_SEPARATOR);
 			
 			nextTimeStamp += random.nextInt(1000);
 			
 			return new ByteArrayInputStream(strBuilder.toString().getBytes());
 		}
 	
 	}
 	
 	public static void main(String[] args) throws IOException {
 		FakeTraceStream res = new FakeTraceStream(1000000);
 		BufferedReader br = new BufferedReader(new InputStreamReader(res));
 		String line;
 		while((line = br.readLine()) != null) {
 			System.out.println(line);
 		}
 	}
 	
 }
