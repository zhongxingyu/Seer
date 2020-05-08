 package simulation.beefs.event.filesystem;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import simulation.beefs.event.machine.UserIdleness;
 import simulation.beefs.model.DataServer;
 import simulation.beefs.model.FileSystemClient;
 import simulation.beefs.model.Machine;
 import simulation.beefs.model.Machine.State;
 import simulation.beefs.model.MetadataServer;
 import simulation.beefs.model.ReplicatedFile;
 import simulation.beefs.util.ObservableEventSourceMultiplexer;
 import core.EventScheduler;
 import core.EventSource;
 import core.Time;
 import core.Time.Unit;
 import core.TimeInterval;
 
 public class WriteTest {
 	
 	private static final Time TO_SLEEP_TIMEOUT = new Time(15*60, Unit.SECONDS);
 	private static final Time TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);
 	private static final Time ONE_MINUTE = new Time(60, Unit.SECONDS);
 	private static final Time ONE_SECOND = new Time(1, Unit.SECONDS);
 	
 	private FileSystemClient client;
 	private MetadataServer metadataServer;
 	private Machine jurupoca;
 	
 	@Before
 	public void setup() {
 		jurupoca = new Machine("jurupoca", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
 		jurupoca.setIdle(Time.GENESIS, ONE_MINUTE);
 		
 		Time timeToCoherence = new Time(5 * 60, Unit.SECONDS);
 		Time timeToDelete = new Time(5 * 60, Unit.SECONDS);
 		Set<DataServer> dataServers = new HashSet<DataServer>();
 		dataServers.add(new DataServer(jurupoca));
 		metadataServer = new MetadataServer(dataServers, "random", 0, timeToCoherence, timeToDelete);
 		client = new FileSystemClient(jurupoca, metadataServer, true);		
 	}
 	
 	@Test
 	public void testWriteIntervalsAfterOneWrite() {
 		Time eventStart = Time.GENESIS;
 		Time duration = new Time(3, Unit.MILLISECONDS);
 		Write write = 
 				new Write(client, eventStart, duration, "/home/josebiades/texto.txt", 15 , 1024);
 		
 		write.process();
 		
 		ReplicatedFile file = client.createOrOpen("/home/josebiades/texto.txt");
 		DataServer ds = file.getPrimary();
 		List<TimeInterval> writeIntervals = ds.getWriteIntervals();
 		
 		assertEquals(1024L, file.getSize());
 		assertFalse(file.areReplicasConsistent());
 		assertEquals(1, writeIntervals.size());
 		assertEquals(new TimeInterval(eventStart, eventStart.plus(duration)), writeIntervals.get(0));
 	}
 	
 	@Test
 	public void testWriteIntervalsAfterNonOverlappedWrites() {
 		Time event1Start = Time.GENESIS;
 		Time event1Duration = new Time(5, Unit.MILLISECONDS);
 		Write write = 
 				new Write(client, event1Start, event1Duration, "/home/josebiades/texto.txt", 15 , 1024);
 		write.process();
 		
 		Time event2Start = new Time(6, Unit.MILLISECONDS);
 		Time event2Duration = new Time(5, Unit.MILLISECONDS);
 		write =	new Write(client, event2Start, event2Duration, "/home/josebiades/texto.txt", 15 , 1024);
 		write.process();
 		
 		ReplicatedFile file = client.createOrOpen("/home/josebiades/texto.txt");
 		DataServer ds = file.getPrimary();
 		List<TimeInterval> writeIntervals = ds.getWriteIntervals();
 		
 		assertEquals(1024L, file.getSize());
 		assertFalse(file.areReplicasConsistent());
 		assertEquals(2, writeIntervals.size());
 		TimeInterval expectedTimeInterval = new TimeInterval(event1Start, event1Start.plus(event1Duration));
 		assertTrue(writeIntervals.contains(expectedTimeInterval));
 		expectedTimeInterval = new TimeInterval(event2Start, event2Start.plus(event2Duration));
 		assertTrue(writeIntervals.contains(expectedTimeInterval));
 	}
 	
 	@Test
 	public void testWriteIntervalsAfterOverlappedWrites() {
 		Time event1Start = Time.GENESIS;
 		Time event1Duration = new Time(5, Unit.MILLISECONDS);
 		Write write = 
 				new Write(client, event1Start, event1Duration, "/home/josebiades/texto.txt", 15 , 1024);
 		write.process();
 		
 		Time event2Start = new Time(2, Unit.MILLISECONDS);
 		Time event2Duration = new Time(5, Unit.MILLISECONDS);
 		write =	new Write(client, event2Start, event2Duration, "/home/josebiades/texto.txt", 15 , 1024);
 		write.process();
 		
 		ReplicatedFile file = client.createOrOpen("/home/josebiades/texto.txt");
 		DataServer ds = file.getPrimary();
 		List<TimeInterval> writeIntervals = ds.getWriteIntervals();
 		
 		assertEquals(1024L, file.getSize());
 		assertFalse(file.areReplicasConsistent());
 		assertEquals(1, writeIntervals.size());
 		TimeInterval expectedTimeInterval = new TimeInterval(Time.GENESIS, event2Start.plus(event2Duration));
 		assertTrue(writeIntervals.contains(expectedTimeInterval));
 	}
 	
 	@Test
 	public void testWriteIntervalsAfterOverlappedAndNonOverlappedWrites() {
 		String filePath = "/home/patrick/teste.txt";
 		Time zero = Time.GENESIS;
 		Time five = new Time(5, Unit.MILLISECONDS);
 		Time ten = new Time(10, Unit.MILLISECONDS);
 		Time fifteen = new Time(15, Unit.MILLISECONDS);
 		
 		Write write = new Write(client, zero, five, filePath, 15, 1024);
 		write.process();
 		
 		write = new Write(client, ten, five, filePath, 15, 1024);
 		write.process();
 		
 		write = new Write(client, zero, five, filePath, 15, 1024);
 		write.process();
 		
 		ReplicatedFile file = client.createOrOpen(filePath);
 		List<TimeInterval> writeIntervals = file.getPrimary().getWriteIntervals();
 		
 		assertEquals(1024L, file.getSize());
 		assertFalse(file.areReplicasConsistent());
 		assertEquals(2, writeIntervals.size());
 		TimeInterval expectedTimeInterval = new TimeInterval(Time.GENESIS, five);
 		assertTrue(writeIntervals.contains(expectedTimeInterval));
 		expectedTimeInterval = new TimeInterval(ten, fifteen);
 		assertTrue(writeIntervals.contains(expectedTimeInterval));
 	}
 	
 	@Test
 	public void testWritesChangingFileSize() {
 		String filePath = "/home/patrick/teste.txt";
 		Time zero = Time.GENESIS;
 		Time five = new Time(5, Unit.MILLISECONDS);
 		Time ten = new Time(10, Unit.MILLISECONDS);
 
 		Write write = new Write(client, zero, five, filePath, 15, 1024);
 		write.process();
 		
 		write = new Write(client, five, five, filePath, 1024, 2048);
 		write.process();
 		
 		ReplicatedFile file = client.createOrOpen(filePath);
 		List<TimeInterval> writeIntervals = file.getPrimary().getWriteIntervals();
 		
 		assertEquals(2048L, file.getSize());
 		assertFalse(file.areReplicasConsistent());
 		assertEquals(1, writeIntervals.size());
 		TimeInterval expectedTimeInterval = new TimeInterval(Time.GENESIS, ten);
 		assertTrue(writeIntervals.contains(expectedTimeInterval));
 	}
 	
 	/*
 	 * If client and data server are in different machine, the target machine is sleeping and client is configured to 
 	 * use wakeOnLan...
 	 */
 	@Test
 	public void testWriteIsProperlyReScheduled() { 
 		String fullpath = "/home/beefs/arquivo.txt";
 		
 		// setup the scenario
 		ObservableEventSourceMultiplexer eventsMultiplexer = new ObservableEventSourceMultiplexer(new EventSource[0]);
 		EventScheduler.setup(Time.GENESIS, Time.THE_FINAL_JUDGMENT, eventsMultiplexer);
 
 		// making jurupoca sleep
 		jurupoca.setActive(ONE_MINUTE, ONE_MINUTE);
 		jurupoca.setIdle(ONE_MINUTE.times(2), TO_SLEEP_TIMEOUT.plus(ONE_MINUTE));
 		EventScheduler.start();
 		assertEquals(State.SLEEPING, jurupoca.getState());
 		
 		Machine awakeMachine = new Machine("cherne", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
 		awakeMachine.setIdle(Time.GENESIS, ONE_MINUTE);
 		awakeMachine.setActive(ONE_MINUTE, ONE_MINUTE.times(60));
 		FileSystemClient otherClient = new FileSystemClient(awakeMachine, metadataServer, true);
 		
 		// call write
 		Time writeDuration = new Time(500, Unit.MILLISECONDS);
 		Time aTimeJurupocaIsSleeping = new Time(17*60 + 4, Unit.SECONDS); // 17 min:4 sec
 		Write write = new Write(otherClient, aTimeJurupocaIsSleeping, writeDuration, fullpath, 1024, 1024*1024);
 		write.process();
 		
 		// check if machine's status is waking up
 		assertEquals(State.WAKING_UP, jurupoca.getState());
 		
 		// check if a new Write with the same parameters but different time was scheduled
 		Time theTimeJurupocaMustWakeUp = aTimeJurupocaIsSleeping.plus(TRANSITION_DURATION);
 
 		UserIdleness userIdleness = new UserIdleness(jurupoca, theTimeJurupocaMustWakeUp, 
				new Time(18*60, Unit.SECONDS).minus(theTimeJurupocaMustWakeUp));
 		assertTrue(eventsMultiplexer.contains(userIdleness));
 		
 		write = new Write(otherClient, theTimeJurupocaMustWakeUp.plus(ONE_SECOND), writeDuration, 
 				fullpath, 1024, 1024*1024);
 		assertTrue(eventsMultiplexer.contains(write));
 		
 		EventScheduler.start(); // consumes the UserIdleness scheduled by the call to wakeOnLan
 		assertEquals(State.IDLE, jurupoca.getState());
 		TimeInterval expectedInterval = new TimeInterval(theTimeJurupocaMustWakeUp.plus(ONE_SECOND), 
 				theTimeJurupocaMustWakeUp.plus(ONE_SECOND).plus(writeDuration));
 		ReplicatedFile file = otherClient.createOrOpen(fullpath);
 		assertEquals(expectedInterval, file.getPrimary().getWriteIntervals().get(0));
 		
 		assertEquals(0, client.writesWhileClientSleeping());
 	}
 	
 	/*
 	 * If client and data server are in different machine, the target machine is sleeping and client is not configured 
 	 * to use wakeOnLan... 
 	 */
 	@Test
 	public void testWriteIsProperlyRegisteredAndIgnored() {
 		String fullpath = "/home/beefs/arquivo.txt";
 		
 		// setup the scenario
 		ObservableEventSourceMultiplexer eventsMultiplexer = new ObservableEventSourceMultiplexer(new EventSource[0]);
 		EventScheduler.setup(Time.GENESIS, Time.THE_FINAL_JUDGMENT, eventsMultiplexer);
 
 		// making jurupoca sleep
 		jurupoca.setActive(ONE_MINUTE, ONE_MINUTE);
 		jurupoca.setIdle(ONE_MINUTE.times(2), TO_SLEEP_TIMEOUT.plus(ONE_MINUTE));
 		EventScheduler.start();
 		assertEquals(State.SLEEPING, jurupoca.getState());
 		
 		Machine awakeMachine = new Machine("cherne", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
 		awakeMachine.setIdle(Time.GENESIS, ONE_MINUTE);
 		awakeMachine.setActive(ONE_MINUTE, ONE_MINUTE.times(60));
 		FileSystemClient otherClient = new FileSystemClient(awakeMachine, metadataServer, false);
 		
 		int queueSizeBefore = eventsMultiplexer.queueSize();
 		
 		// call write
 		Time writeDuration = new Time(500, Unit.MILLISECONDS);
 		Time aTimeJurupocaIsSleeping = new Time(17*60 + 4, Unit.SECONDS); // 17 min:4 sec
 		Write write = new Write(otherClient, aTimeJurupocaIsSleeping, writeDuration, fullpath, 1024, 1024*1024);
 		write.process();
 		
 		// the status must not change
 		assertEquals(State.SLEEPING, jurupoca.getState());
 		assertEquals(queueSizeBefore, eventsMultiplexer.queueSize());
 		
 		// zero write intervals must be added
 		ReplicatedFile file = otherClient.createOrOpen(fullpath);
 		assertEquals(0, file.getPrimary().getWriteIntervals().size());
 		
 		assertEquals(1, otherClient.writesWhileDataServerSleeping());
 	}
 	
 	/*
 	 * If client machine is sleeping...
 	 */
 	@Test
 	public void testWriteIsProperlyRegisteredAndIgnored1() {
 		String fullpath = "/home/beefs/arquivo.txt";
 		
 		// setup the scenario
 		ObservableEventSourceMultiplexer eventsMultiplexer = new ObservableEventSourceMultiplexer(new EventSource[0]);
 		EventScheduler.setup(Time.GENESIS, Time.THE_FINAL_JUDGMENT, eventsMultiplexer);
 		
 		// making jurupoca sleep
 		jurupoca.setActive(ONE_MINUTE, ONE_MINUTE);
 		jurupoca.setIdle(ONE_MINUTE.times(2), TO_SLEEP_TIMEOUT.plus(ONE_MINUTE));
 		EventScheduler.start();
 		assertEquals(State.SLEEPING, jurupoca.getState());
 		
 		int queueSizeBefore = eventsMultiplexer.queueSize();
 		
 		// call write
 		Time writeDuration = new Time(500, Unit.MILLISECONDS);
 		Time aTimeJurupocaIsSleeping = new Time(17*60 + 4, Unit.SECONDS); // 17 min:4 sec
 		Write write = new Write(client, aTimeJurupocaIsSleeping, writeDuration, fullpath, 1024, 1024*1024);
 		write.process();
 
 		// the status must not change
 		assertEquals(State.SLEEPING, jurupoca.getState());
 		assertEquals(queueSizeBefore, eventsMultiplexer.queueSize());
 		
 		// zero write intervals must be added
 		ReplicatedFile file = client.createOrOpen(fullpath);
 		assertEquals(0, file.getPrimary().getWriteIntervals().size());
 		
 		assertEquals(1, client.writesWhileClientSleeping());
 	}
 
 }
