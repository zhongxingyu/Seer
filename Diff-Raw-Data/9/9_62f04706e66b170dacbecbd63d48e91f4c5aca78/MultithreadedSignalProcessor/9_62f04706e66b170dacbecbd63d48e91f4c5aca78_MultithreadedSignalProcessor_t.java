 package ar.edu.itba.pod.legajo51048.impl;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.log4j.Logger;
 import org.jgroups.Address;
 
 import ar.edu.itba.pod.api.NodeStats;
 import ar.edu.itba.pod.api.Result;
 import ar.edu.itba.pod.api.Result.Item;
 import ar.edu.itba.pod.api.SPNode;
 import ar.edu.itba.pod.api.Signal;
 import ar.edu.itba.pod.api.SignalProcessor;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multimaps;
 
 /**
  * Implementation of a node that finds similar signals using multiple threads
  * and distributing the request.
  * 
  * @author Esteban G. Pintos
  * 
  */
 public class MultithreadedSignalProcessor implements SPNode, SignalProcessor {
 
 	// Node's signals
 	private final BlockingQueue<Signal> signals;
 
 	// Members in the cluster known by this node
 	private BlockingQueue<Address> members;
 
 	// Back ups of other node's signals
 	private final Multimap<Address, Signal> backups;
 
 	// Signals that have been distributed and node still waiting for ACK
 	private final BlockingQueue<Signal> sendSignals;
 
 	// Backups that have been distributed and node still waiting for ACK
 	private final BlockingQueue<Backup> sendBackups;
 
 	// Queue of notifications to analyze
 	private final BlockingQueue<SignalMessage> notifications;
 
 	// Queue of acknowledges or answers to analyze
 	private final BlockingQueue<SignalMessage> acknowledges;
 
 	// List containing the find similar requests send to other nodes
 	private final ConcurrentMap<Integer, FindRequest> requests;
 
 	// Processing threads
 	private ExecutorService executor;
 	private NotificationsAnalyzer notificationsAnalyzer;
 	private AcknowledgesAnalyzer acknowledgesAnalyzer;
 	private final int threadsQty;
 
 	// Quantity of received find similar requests. Used for request id.
 	private AtomicInteger receivedSignals = new AtomicInteger(0);
 
 	// Connection implementation to a cluster
 	private Connection connection = null;
 
 	// Chunk size of signals/backups sent
 	private static final int CHUNK_SIZE = 512;
 
 	// Boolean indicating whether the node is degraded or not.
 	private AtomicBoolean degradedMode = new AtomicBoolean(false);
 
 	private Logger logger;
 
 	public MultithreadedSignalProcessor(int threadsQty) {
 		ArrayListMultimap<Address, Signal> list = ArrayListMultimap.create();
 		this.backups = Multimaps.synchronizedListMultimap(list);
 		this.sendBackups = new LinkedBlockingQueue<Backup>();
 		this.sendSignals = new LinkedBlockingQueue<Signal>();
 		this.notifications = new LinkedBlockingQueue<SignalMessage>();
 		this.acknowledges = new LinkedBlockingQueue<SignalMessage>();
 		this.requests = new ConcurrentHashMap<Integer, FindRequest>();
 		this.signals = new LinkedBlockingQueue<Signal>();
 		this.executor = Executors.newFixedThreadPool(threadsQty);
 		this.threadsQty = threadsQty;
 		this.logger = Logger.getLogger("MultithreadedSignalProcessor");
 	}
 
 	/******************** SPNODE IMPLEMENTATION ********************/
 	@Override
 	public void join(String clusterName) throws RemoteException {
 		if (connection != null && connection.getClusterName() != null) {
 			throw new IllegalStateException("Already in cluster "
 					+ connection.getClusterName());
 		}
 		if (!signals.isEmpty()) {
 			throw new IllegalStateException(
 					"Can't join a cluster because there are signals already stored");
 		}
 		logger.debug("Joining cluster " + clusterName + " ...");
 		this.connection = new Connection(clusterName, this);
 		this.members = new LinkedBlockingQueue<Address>(connection.getMembers());
 		this.initializeAnalyzers();
 
 	}
 
 	@Override
 	public void exit() throws RemoteException {
 		logger.debug("Starting exit...");
 		if (connected()) {
 			connection.disconnect();
 			if (getMembersQty() == 1) {
 				connection.close();
 			}
 			this.members = null;
 			connection = null;
 		}
 		signals.clear();
 		backups.clear();
 		notifications.clear();
 		acknowledges.clear();
 		sendBackups.clear();
 		sendSignals.clear();
 		requests.clear();
 		receivedSignals = new AtomicInteger(0);
 
 		try {
 			if (notificationsAnalyzer != null) {
 				notificationsAnalyzer.finish();
 				notificationsAnalyzer.interrupt();
 				notificationsAnalyzer.join();
 			}
 			if (acknowledgesAnalyzer != null) {
 				acknowledgesAnalyzer.finish();
 				acknowledgesAnalyzer.interrupt();
 				acknowledgesAnalyzer.join();
 			}
 			executor.shutdown();
 			while (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS))
 				;
 			this.executor = Executors.newFixedThreadPool(threadsQty);
 		} catch (InterruptedException e) {
 		} catch (Exception e) {
 		}
 	}
 
 	@Override
 	public NodeStats getStats() throws RemoteException {
		return new NodeStats(connected() ? "cluster "
				+ connection.getClusterName() : "standalone",
 				receivedSignals.longValue(), signals.size(), backups.size(),
 				degradedMode.get());
 	}
 
 	/**
 	 * Create and start notifications and acknowledge analyzer thread.
 	 */
 	private void initializeAnalyzers() {
 		Semaphore semaphore1 = new Semaphore(0);
 		Semaphore semaphore2 = new Semaphore(0);
 		Semaphore semaphore3 = new Semaphore(0);
 		this.notificationsAnalyzer = new NotificationsAnalyzer(signals,
 				notifications, this, requests, backups, connection, members,
 				semaphore1, semaphore2, semaphore3);
 		this.acknowledgesAnalyzer = new AcknowledgesAnalyzer(acknowledges,
 				sendSignals, sendBackups, requests, connection, semaphore1,
 				semaphore2, semaphore3);
 		this.notificationsAnalyzer.start();
 		this.acknowledgesAnalyzer.start();
 	}
 
 	/******************** SIGNALPROCESSOR IMPLEMENTATION ********************/
 
 	@Override
 	public void add(Signal signal) throws RemoteException {
 		distributeNewSignal(signal);
 	}
 
 	@Override
 	public Result findSimilarTo(Signal signal) throws RemoteException {
 
 		if (signal == null) {
 			throw new IllegalArgumentException("Signal cannot be null");
 		}
 
 		while (connected() && getMembersQty() != 1 && degradedMode.get())
 			;
 
 		int requestId = receivedSignals.incrementAndGet();
 		Result result = null;
 		if (connected() && getMembersQty() > 1) {
 
 			Semaphore semaphore = new Semaphore(0);
 			long timestamp = System.currentTimeMillis();
 			FindRequest request = new FindRequest(requestId, signal,
 					Lists.newArrayList(members), semaphore, timestamp);
 			this.requests.put(requestId, request);
 			connection.broadcastMessage(new SignalMessage(signal,
 					getMyAddress(), requestId, timestamp,
 					SignalMessageType.FIND_SIMILAR));
 			result = findSimilarToAux(signal);
 
 			// This while will wait for Results and will not finish until ALL
 			// the results arrived (1-tolerance)
 			try {
 				while (!semaphore.tryAcquire(request.getQty(), 1000,
 						TimeUnit.MILLISECONDS)) {
 					logger.debug("Waiting for results of "
 							+ semaphore.availablePermits() + " nodes");
 				}
 			} catch (InterruptedException e) {
 			}
 			if (request.retry()) {
 				result = findSimilarToAux(signal);
 			}
 			List<Result> results = request.getResults();
 			if (results != null) {
 				for (Result otherResult : results) {
 					for (Item item : otherResult.items()) {
 						result = result.include(item);
 					}
 				}
 			}
 		} else {
 			result = findSimilarToAux(signal);
 		}
 		return result;
 	}
 
 	/******************** FIND SIMILAR AUXILIARY METHODS ********************/
 
 	/**
 	 * Find similars signals to signal and returns the Result to the node that
 	 * send the order.
 	 * 
 	 * @param from
 	 *            Node that send the find similar request
 	 * @param signal
 	 *            Signal to analyze
 	 * @param id
 	 *            Id of the request
 	 * @param timestamp
 	 *            Timestamp of the resquest sent by from
 	 * @return Result of similar signals
 	 */
 	protected void findMySimilars(Address from, Signal signal, int id,
 			long timestamp) {
 		logger.debug(getMyAddress() + " findingSimilars...");
 		Result result = findSimilarToAux(signal);
 		connection.sendMessageTo(from, new SignalMessage(getMyAddress(),
 				result, id, SignalMessageType.FIND_SIMILAR_RESULT, timestamp));
 		logger.debug(getMyAddress() + " finishedFindingSimilars....");
 	}
 
 	/**
 	 * Finds similar signals of signal
 	 * 
 	 * @param signal
 	 * @return Result of similar signals
 	 */
 	private Result findSimilarToAux(Signal signal) {
 		List<Callable<Result>> tasks = new ArrayList<Callable<Result>>();
 		try {
 			BlockingQueue<Signal> copy = new LinkedBlockingQueue<Signal>(
 					this.signals);
 			for (int i = 0; i < threadsQty; i++) {
 				// Generate a copy of the signals so they are not lost
 				tasks.add(new FindSimilarWorker(signal, copy));
 			}
 			List<Future<Result>> futures = executor.invokeAll(tasks);
 			Result result = new Result(signal);
 			for (Future<Result> future : futures) {
 				Result r = future.get();
 				for (Item item : r.items()) {
 					result = result.include(item);
 				}
 			}
 			return result;
 		} catch (InterruptedException e) {
 		} catch (ExecutionException e) {
 		}
 		return null;
 	}
 
 	/******************** DISTRIBUTION METHODS ********************/
 
 	/**
 	 * Distributes a signal to a random node and the backup to another. If there
 	 * is only 1 node (this one), the signal is added to this node and the
 	 * backup also (if it is connected to a cluster). This method is blocking.
 	 * 
 	 * @param Signal
 	 *            signal added
 	 **/
 	private void distributeNewSignal(Signal signal) {
 		if (connected() && getMembersQty() != 1) {
 			List<Address> users = Lists.newArrayList(members);
 			int membersQty = getMembersQty();
 			Address futureOwner = users.get(random(membersQty));
 			if (getMyAddress().equals(futureOwner)) {
 				this.signals.add(signal);
 			} else {
 				connection.sendMessageTo(futureOwner, new SignalMessage(
 						getMyAddress(), signal, SignalMessageType.ADD_SIGNAL));
 				try {
 					// Waits for ACK
 					sendSignals.take();
 				} catch (InterruptedException e) {
 				}
 				if (!sendSignals.isEmpty()) {
 					System.out.println("no deberia pasar newSignal");
 				}
 			}
 			distributeBackup(futureOwner, signal);
 		} else {
 			this.signals.add(signal);
 			if (connected()) {
 				this.backups.put(getMyAddress(), signal);
 			}
 
 		}
 	}
 
 	/**
 	 * Distributes randomly a backup of a signal.
 	 * 
 	 * @param signalOwner
 	 *            Owner of the signal
 	 * 
 	 * @param signal
 	 *            Signal being back up
 	 * 
 	 */
 	protected void distributeBackup(Address signalOwner, Signal signal) {
 		List<Address> users = Lists.newArrayList(members);
 		int membersQty = getMembersQty();
 		int random = 0;
 		while (users.get(random = random(membersQty)).equals(signalOwner))
 			;
 		Address futureBackupOwner = users.get(random);
 		if (getMyAddress().equals(futureBackupOwner)) {
 			this.backups.put(signalOwner, signal);
 		} else {
 			Backup backup = new Backup(signalOwner, signal);
 			connection.sendMessageTo(futureBackupOwner, new SignalMessage(
 					getMyAddress(), backup, SignalMessageType.ADD_BACK_UP));
 			try {
 				// Wait for ACK
 				this.sendBackups.take();
 			} catch (InterruptedException e) {
 			}
 			if (!sendBackups.isEmpty()) {
 				System.out.println("no deberia pasar, distributeBackups");
 			}
 		}
 	}
 
 	/**
 	 * Distribute signals to a node. The quantity of signals distributed is
 	 * calculated according to the quantity of members in the cluster and the
	 * message is divided in chunks of size CHUNK_SIZE. Backups are distributed
	 * only if the quantity of members is 2. If the quantity of members is
	 * greater than 2, then no backups will be distributed, owners will be
	 * changed only.
 	 * 
 	 * @param to
 	 *            Destination node
 	 */
 	protected void distributeSignals(Address to) {
 		int sizeToDistribute = 0;
 		BlockingQueue<Signal> copy = null;
 		int membersQty = getMembersQty();
 		synchronized (this.signals) {
 			if (this.signals.isEmpty()) {
 				return;
 			}
 			sizeToDistribute = this.signals.size() / membersQty;
 			copy = new LinkedBlockingQueue<Signal>();
 			this.signals.drainTo(copy, sizeToDistribute);
 		}
 		int chunks = copy.size() / CHUNK_SIZE + 1;
 		int sent = 0;
 		for (int i = 0; i < chunks; i++) {
 			List<Signal> distSignals = new ArrayList<Signal>();
 			if (copy.drainTo(distSignals, CHUNK_SIZE) == 0) {
 				break;
 			}
 			sent += distSignals.size();
 			if (!distSignals.isEmpty()) {
 				connection.sendMessageTo(to, new SignalMessage(getMyAddress(),
 						distSignals, SignalMessageType.ADD_SIGNALS));
 			}
 		}
 
 		try {
 			// Waiting for ACKS
 			for (int i = 0; i < sent; i++) {
 				this.sendSignals.take();
 			}
 			if (sendSignals.size() != 0) {
 				System.out.println("3-no deberia pasar, size!=0");
 			}
 		} catch (InterruptedException e) {
 		}
 
 		if (membersQty == 2) {
 			distributeBackups(getMyAddress(), new LinkedBlockingQueue<Signal>(
 					this.signals));
 			this.backups.get(getMyAddress()).removeAll(this.signals);
 		}
 	}
 
 	/**
 	 * Distributes signals and backups. Sends a portion of them to every node in
 	 * the cluster, calculated randomly, dividing the messages into chunks of
 	 * CHUNK_SIZE. If this node is the only one in the cluster, the signals will
 	 * be added to it, and the backups also (If it is in the cluster).
 	 * 
 	 * @param newSignals
 	 *            Signals/backups being send
 	 */
 	protected void distributeSignals(BlockingQueue<Signal> newSignals) {
 		int sizeToDistribute = 0;
 		if (newSignals.isEmpty()) {
 			return;
 		}
 		if (connected() && getMembersQty() != 1) {
 			int membersQty = getMembersQty();
 			List<Address> users = Lists.newArrayList(members);
 			sizeToDistribute = newSignals.size() / membersQty;
 			List<Address> chosen = new ArrayList<Address>();
 			Address myAddress = getMyAddress();
 			for (int j = 0; j < membersQty; j++) {
 				int random = 0;
 				while (chosen.contains(users.get(random = random(membersQty))))
 					;
 				Address futureOwner = users.get(random);
 				chosen.add(futureOwner);
 				BlockingQueue<Signal> copy = new LinkedBlockingQueue<Signal>();
 				if (j == membersQty - 1) {
 					sizeToDistribute = newSignals.size();
 				}
 				newSignals.drainTo(copy, sizeToDistribute);
 				BlockingQueue<Signal> toBackup = new LinkedBlockingQueue<Signal>();
 				if (!futureOwner.equals(myAddress)) {
 					int chunks = copy.size() / CHUNK_SIZE + 1;
 					for (int i = 0; i < chunks; i++) {
 						List<Signal> distSignals = new ArrayList<Signal>();
 						if (copy.drainTo(distSignals, CHUNK_SIZE) == 0) {
 							break;
 						}
 						toBackup.addAll(distSignals);
 						connection.sendMessageTo(futureOwner,
 								new SignalMessage(getMyAddress(), distSignals,
 										SignalMessageType.ADD_SIGNALS));
 
 					}
 					try {
 						for (int k = 0; k < toBackup.size(); k++) {
 							this.sendSignals.take();
 						}
 						if (sendSignals.size() != 0) {
 							System.out.println("2-no deberia pasar, size!=0");
 						}
 					} catch (InterruptedException e) {
 					}
 					distributeBackups(futureOwner,
 							new LinkedBlockingQueue<Signal>(toBackup));
 
 				} else {
 					BlockingQueue<Signal> auxCopy = new LinkedBlockingQueue<Signal>(
 							copy);
 					this.signals.addAll(auxCopy);
 					distributeBackups(myAddress, auxCopy);
 				}
 			}
 		} else {
 			this.signals.addAll(newSignals);
 			if (connected()) {
 				this.backups.putAll(getMyAddress(), newSignals);
 			}
 		}
 
 	}
 
 	/**
 	 * Distributes signals backup to a ramdom node different from signalOwner.
 	 * 
 	 * @param signalOwner
 	 *            Owner of the signals being backup
 	 * @param signalsToBackup
 	 *            Signals to backup
 	 */
 	protected void distributeBackups(Address signalOwner,
 			BlockingQueue<Signal> signalsToBackup) {
 		if (signalsToBackup.isEmpty()) {
 			return;
 		}
 		int membersQty = getMembersQty();
 		List<Address> users = Lists.newArrayList(members);
 		int sizeToDistribute = 0;
 		sizeToDistribute = signalsToBackup.size();
 		Address myAddress = getMyAddress();
 		int random = 0;
 		while (users.get(random = random(membersQty)).equals(signalOwner))
 			;
 		Address futureOwner = users.get(random);
 		if (!futureOwner.equals(myAddress)) {
 			int chunks = sizeToDistribute / CHUNK_SIZE + 1;
 			int sent = 0;
 			for (int j = 0; j < chunks; j++) {
 				List<Signal> auxList = new ArrayList<Signal>();
 				signalsToBackup.drainTo(auxList, CHUNK_SIZE);
 
 				List<Backup> backupList = new ArrayList<Backup>();
 				for (Signal s : auxList) {
 					backupList.add(new Backup(signalOwner, s));
 				}
 
 				sent += backupList.size();
 				if (!backupList.isEmpty()) {
 					connection.sendMessageTo(futureOwner, new SignalMessage(
 							getMyAddress(), SignalMessageType.ADD_BACK_UPS,
 							backupList));
 				}
 			}
 			try {
 				// Waiting for ACKS
 				for (int i = 0; i < sent; i++) {
 					this.sendBackups.take();
 				}
 				if (sendBackups.size() != 0) {
 					System.out.println("1-no deberia pasar, size!=0");
 				}
 			} catch (InterruptedException e) {
 			}
 		} else {
 			this.backups.putAll(signalOwner, signalsToBackup);
 		}
 	}
 
 	/******************** CHANGE BACKUPS METHODS ********************/
 
 	/**
 	 * * If this node had a back up of signals, it will be changed by the new
 	 * owner.
 	 * 
 	 * @param newOwner
 	 *            New owner of the backup
 	 * @param oldOwner
 	 *            Old owner of the signals
 	 * @param signals
 	 *            Signals which are know backed up by a new owner.
 	 */
 	protected void changeSignalsOwner(Address oldOwner, Address newOwner,
 			List<Signal> signals) {
 		synchronized (backups) {
 			for (Signal signal : signals) {
 				if (this.backups.get(oldOwner).remove(signal)) {
 					this.backups.put(newOwner, signal);
 				}
 			}
 		}
 	}
 
 	/******************** ADD SIGNAL/S METHODS ********************/
 
 	/**
 	 * 
 	 * Adds a signal to this node and returns ADD_SIGNAL_ACK to "from"
 	 * 
 	 * @param from
 	 *            Node that send me the order to add the signal
 	 * @param signal
 	 *            Signal to add
 	 */
 	protected void addSignal(Address from, Signal signal) {
 		this.signals.add(signal);
 		connection.sendMessageTo(from, new SignalMessage(getMyAddress(),
 				signal, SignalMessageType.ADD_SIGNAL_ACK));
 	}
 
 	/**
 	 * Adds a list of signals to this node and returns ADD_SIGNALS_ACK.
 	 * 
 	 * @param from
 	 *            Node that send me the order
 	 * @param newSignals
 	 *            New signals to add
 	 */
 	protected void addSignals(Address from, List<Signal> newSignals) {
 		this.signals.addAll(newSignals);
 		connection.sendMessageTo(from, new SignalMessage(getMyAddress(),
 				newSignals, SignalMessageType.ADD_SIGNALS_ACK));
 
 	}
 
 	/******************** ADD BACKUP/S METHODS ********************/
 
 	/**
 	 * Adds a backup to this node and returns ADD_BACKUP_ACK
 	 * 
 	 * @param from
 	 *            Node that send me the order to add the backup
 	 * @param backup
 	 *            New backup
 	 */
 	protected void addBackup(Address from, Backup backup) {
 		this.backups.put(backup.getAddress(), backup.getSignal());
 		connection.sendMessageTo(from, new SignalMessage(getMyAddress(),
 				backup, SignalMessageType.ADD_BACKUP_ACK));
 	}
 
 	/**
 	 * Adds a list of backups to this node and returns ADD_BACKUPS_ACK
 	 * 
 	 * @param from
 	 *            Node that send me the order to add the backups
 	 * @param backupList
 	 *            New backup list
 	 */
 	protected void addBackups(Address from, List<Backup> backupList) {
 		for (Backup backup : backupList) {
 			this.backups.put(backup.getAddress(), backup.getSignal());
 		}
 		connection.sendMessageTo(from, new SignalMessage(getMyAddress(),
 				SignalMessageType.ADD_BACKUPS_ACK, backupList));
 	}
 
 	/**
 	 * Adds a new notification.
 	 * 
 	 * @param notification
 	 *            New notification
 	 */
 	protected void addNotification(SignalMessage notification) {
 		notifications.add(notification);
 	}
 
 	/**
 	 * Adds a new acknowledge notification.
 	 * 
 	 * @param acknowledge
 	 *            New acknowledge notification
 	 */
 	protected void addAcknowledge(SignalMessage acknowledge) {
 		acknowledges.add(acknowledge);
 	}
 
 	/**
 	 * Calculates random value between 0 and limit.
 	 * 
 	 * @param limit
 	 * @return Random number
 	 */
 	private int random(int limit) {
 		Random random = new Random();
 		return random.nextInt(limit);
 	}
 
 	/**
 	 * Sets the degraded mode to a state.
 	 * 
 	 * @param state
 	 *            New state
 	 */
 	public void setDegradedMode(boolean state) {
 		degradedMode.set(state);
 	}
 
 	/**
 	 * @return Quantity of members I know
 	 */
 	private int getMembersQty() {
 		return this.members.size();
 	}
 
 	/**
 	 * 
 	 * @return True if this node is connected to a cluster
 	 */
 	private boolean connected() {
 		return this.connection != null;
 	}
 
 	/**
 	 * Get this node address in the cluster
 	 * 
 	 * @return Address
 	 */
 	private Address getMyAddress() {
 		return connection.getMyAddress();
 	}
 }
