 package simulation.beefs.event.filesystem;
 
 import static org.junit.Assert.assertEquals;
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
 
 /**
  * 
  * @author Patrick Maia
  *
  */
 public class ReadTest {
 	
 	private final Time TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);
 	private final Time TO_SLEEP_TIMEOUT = new Time(15*60, Unit.SECONDS);
 	private final Time ONE_MINUTE = new Time(60, Unit.SECONDS);
 	private final Time ONE_SECOND = new Time(1, Unit.SECONDS);
 	
 	private MetadataServer metadataServer;
 	private FileSystemClient client;
 	private Machine jurupoca;
 	
 	@Before
 	public void setup() {
 		jurupoca = new Machine("jurupoca", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
 		jurupoca.setIdle(Time.GENESIS, ONE_MINUTE);
 		
 		Set<DataServer> dataServers = new HashSet<DataServer>();
 		dataServers.add(new DataServer(jurupoca));
 		
 		Time timeToCoherence = new Time(5 * 60, Unit.SECONDS);
 		Time timeToDelete = new Time(5 * 60, Unit.SECONDS);
 		metadataServer = new MetadataServer(dataServers, "random", 0, timeToCoherence, timeToDelete);
 		
 		client = new FileSystemClient(jurupoca, metadataServer, true);
 	}
 	
 	@Test
 	public void testFileSizeIsTheSameAfterRead() {
 		String filePath = "/home/patrick/cruzeiro.txt";
 		ReplicatedFile file = client.createOrOpen(filePath);
 		file.setSize(1024);
 		
 		Read read = new Read(client, Time.GENESIS, new Time(5, Unit.MILLISECONDS), filePath, 10);
 		read.process();
 		
 		assertEquals(1024, file.getSize());
 	}
 	
 	@Test
 	public void testReplicasCoherenceAreTheSameAfterRead() {
 		String filePath = "/home/patrick/cruzeiro.txt";
 		ReplicatedFile file = client.createOrOpen(filePath);
 		file.setSize(1024);
 		file.setReplicasAreConsistent(true);
 		
 		Read read = new Read(client, Time.GENESIS, new Time(5, Unit.MILLISECONDS), filePath, 10);
 		read.process();
 		assertEquals(true, file.areReplicasConsistent());
 		
 		file.setReplicasAreConsistent(false);
 		read.process();
 		assertEquals(false, file.areReplicasConsistent());
 	}
 	
 	@Test
 	public void testReadIntervalsAfterOneRead() {
 		Time scheduledTime = Time.GENESIS;
 		Time duration = new Time(5, Unit.MILLISECONDS);
 		String filePath = "/home/patrick/teste.txt";
 		long length = 1024;
 		
 		Read read = new Read(client, scheduledTime, duration, filePath, length);
 		read.process();
 		
 		ReplicatedFile file = client.createOrOpen(filePath);
 		List<TimeInterval> readIntervals = file.getPrimary().getReadIntervals();
 		
 		assertEquals(1, readIntervals.size());
 		TimeInterval expectedReadInterval = new TimeInterval(scheduledTime, scheduledTime.plus(duration));
 		assertTrue(readIntervals.contains(expectedReadInterval));
 	}
 	
 	@Test
 	public void testReadIntervalsAfterNonOverlappedReads() {
 		String filePath = "/home/patrick/cruzeiro.txt";
 		Time read1Start = Time.GENESIS;
 		Time read2Start = new Time(6, Unit.MILLISECONDS);
 		Time eventsDuration = new Time(5, Unit.MILLISECONDS);
 		
 		Read read1 = new Read(client, read1Start, eventsDuration, filePath, 64);
 		read1.process();
 		Read read2 = new Read(client, read2Start, eventsDuration, filePath, 1024);
 		read2.process();
 		
 		ReplicatedFile file = client.createOrOpen(filePath);
 		List<TimeInterval> readIntervals = file.getPrimary().getReadIntervals();
 		
 		assertEquals(2, readIntervals.size());
 		TimeInterval expectedTimeInterval = new TimeInterval(read1Start, read1Start.plus(eventsDuration));
 		assertTrue(readIntervals.contains(expectedTimeInterval));
 		expectedTimeInterval = new TimeInterval(read2Start, read2Start.plus(eventsDuration));
 		assertTrue(readIntervals.contains(expectedTimeInterval));
 	}
 	
 	@Test
 	public void testReadIntervalsAfterOverlapperdReads() {
 		String filePath = "/home/patrick/cruzeiro.txt";
 		Time read1Start = Time.GENESIS;
 		Time read2Start = new Time(5, Unit.MILLISECONDS);
 		Time duration = new Time(10, Unit.MILLISECONDS);
 		
 		Read read1 = new Read(client, read1Start, duration, filePath, 1024);
 		read1.process();
 		
 		Read read2 = new Read(client, read2Start, duration, filePath, 1024);
 		read2.process();
 		
 		ReplicatedFile file = client.createOrOpen(filePath);
 		List<TimeInterval> readIntervals = file.getPrimary().getReadIntervals();
 		
 		assertEquals(1, readIntervals.size());
 		TimeInterval expectedTimeInterval = new TimeInterval(Time.GENESIS, new Time(15, Unit.MILLISECONDS));
 		assertTrue(readIntervals.contains(expectedTimeInterval));
 	}
 	
 	@Test
 	public void testReadIntervalsAfterOverlappedAndNonOverlappedReads() {
 		String filePath = "/home/patrick/cruzeiro.txt";
 		Time read1Start = Time.GENESIS;
 		Time read2Start = new Time(3, Unit.MILLISECONDS);
 		Time read3Start = new Time(10, Unit.MILLISECONDS);
 		Time duration = new Time(5, Unit.MILLISECONDS);
 		
 		Read read1 = new Read(client, read1Start, duration, filePath, 1024);
 		read1.process();
 		
 		Read read2 = new Read(client, read2Start, duration, filePath, 1024);
 		read2.process();
 		
 		Read read3 = new Read(client, read3Start, duration, filePath, 1024);
 		read3.process();
 		
 		ReplicatedFile file = client.createOrOpen(filePath);
 		List<TimeInterval> readIntervals = file.getPrimary().getReadIntervals();
 		
 		assertEquals(2, readIntervals.size());
 		TimeInterval expectedTimeInterval = new TimeInterval(Time.GENESIS, new Time(8, Unit.MILLISECONDS));
 		assertTrue(readIntervals.contains(expectedTimeInterval));
 		expectedTimeInterval = new TimeInterval(read3Start, read3Start.plus(duration));
 		assertTrue(readIntervals.contains(expectedTimeInterval));
 	}
 	
 	/*
 	 * If client and data server are in different machine, the target machine is sleeping and client is configured to 
 	 * use wakeOnLan...
 	 */
 	@Test
 	public void testReadIsProperlyReScheduled() { 
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
 		
 		// call read
 		Time readDuration = new Time(500, Unit.MILLISECONDS);
 		Time aTimeJurupocaIsSleeping = new Time(17*60 + 4, Unit.SECONDS); // 17 min:4 sec
 		Read read = new Read(otherClient, aTimeJurupocaIsSleeping, readDuration, fullpath, 1024);
 		read.process();
 		
 		// check if machine's status is waking up
 		assertEquals(State.WAKING_UP, jurupoca.getState());
 		
 		// check if a new Read with the same parameters but different time was scheduled
 		Time theTimeJurupocaMustWakeUp = aTimeJurupocaIsSleeping.plus(TRANSITION_DURATION);
 
 		UserIdleness userIdleness = new UserIdleness(jurupoca, theTimeJurupocaMustWakeUp, 
				new Time(18*60, Unit.SECONDS).minus(theTimeJurupocaMustWakeUp));
 		assertTrue(eventsMultiplexer.contains(userIdleness));
 		
 		read = new Read(otherClient, theTimeJurupocaMustWakeUp.plus(ONE_SECOND), readDuration, fullpath, 1024);
 		assertTrue(eventsMultiplexer.contains(read));
 		
 		EventScheduler.start(); // consumes the UserIdleness scheduled by the call to wakeOnLan
 		assertEquals(State.IDLE, jurupoca.getState());
 		TimeInterval expectedInterval = new TimeInterval(theTimeJurupocaMustWakeUp.plus(ONE_SECOND), 
 				theTimeJurupocaMustWakeUp.plus(ONE_SECOND).plus(readDuration));
 		ReplicatedFile file = otherClient.createOrOpen(fullpath);
 		assertEquals(expectedInterval, file.getPrimary().getReadIntervals().get(0));
 		
 		assertEquals(0, client.readsWhileClientSleeping());
 	}
 	
 	/*
 	 * If client and data server are in different machine, the target machine is sleeping and client is not configured 
 	 * to use wakeOnLan... 
 	 */
 	@Test
 	public void testReadIsProperlyRegisteredAndIgnored() {
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
 		
 		// call read
 		Time readDuration = new Time(500, Unit.MILLISECONDS);
 		Time aTimeJurupocaIsSleeping = new Time(17*60 + 4, Unit.SECONDS); // 17 min:4 sec
 		Read read = new Read(otherClient, aTimeJurupocaIsSleeping, readDuration, fullpath, 1024);
 		read.process();
 		
 		// the status must not change
 		assertEquals(State.SLEEPING, jurupoca.getState());
 		assertEquals(queueSizeBefore, eventsMultiplexer.queueSize());
 		
 		// zero read intervals must be added
 		ReplicatedFile file = otherClient.createOrOpen(fullpath);
 		assertEquals(0, file.getPrimary().getReadIntervals().size());
 		
 		assertEquals(1, otherClient.readsWhileDataServerSleeping());
 	}
 	
 	/*
 	 * If client machine is sleeping...
 	 */
 	@Test
 	public void testReadIsProperlyRegisteredAndIgnored1() {
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
 		
 		// call read
 		Time readDuration = new Time(500, Unit.MILLISECONDS);
 		Time aTimeJurupocaIsSleeping = new Time(17*60 + 4, Unit.SECONDS); // 17 min:4 sec
 		Read read = new Read(client, aTimeJurupocaIsSleeping, readDuration, fullpath, 1024);
 		read.process();
 
 		// the status must not change
 		assertEquals(State.SLEEPING, jurupoca.getState());
 		assertEquals(queueSizeBefore, eventsMultiplexer.queueSize());
 		
 		// zero read intervals must be added
 		ReplicatedFile file = client.createOrOpen(fullpath);
 		assertEquals(0, file.getPrimary().getReadIntervals().size());
 		
 		assertEquals(1, client.readsWhileClientSleeping());
 	}
 
 }
