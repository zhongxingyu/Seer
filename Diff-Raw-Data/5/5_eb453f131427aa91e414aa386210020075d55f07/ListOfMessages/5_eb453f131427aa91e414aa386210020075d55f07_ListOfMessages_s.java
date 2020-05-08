 package genlab.core.usermachineinteraction;
 
 import genlab.core.commons.ProgramException;
 import genlab.core.commons.WrongParametersException;
 import genlab.core.model.exec.ComputationState;
 
 import java.io.PrintStream;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.TreeSet;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.management.RuntimeErrorException;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.Priority;
 
 /**
  * Stores a set of messages. Orders them by increasing timestamp.
  * Avoids to add many times a message, by just increasing the amount of occurences (for the X last messages).
  * 
  * TODO thread safety, please !
  * TODO observer pattern
  * 
  * @author Samuel Thiriot
  *
  */
 @SuppressWarnings("rawtypes")
 public class ListOfMessages implements Iterable<ITextMessage>, Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 
 	/**
 	 * After this limit, will attempt to clean the messages
 	 */
 	public static final int DEFAULT_LIMIT_START_CLEANUP = 5000;
 	public static final int DEFAULT_CLEANUP_TARGET_SIZE = 3000;
 	
 	/**
 	 * If true, relays every message to a log4j logger.
 	 */
 	public static boolean DEFAULT_RELAY_TO_LOG4J = true;
 
 	public int limitStartCleanup = DEFAULT_CLEANUP_TARGET_SIZE;
 	public int cleanupTarget = DEFAULT_CLEANUP_TARGET_SIZE;
 	
 	
 	/**
 	 * The maximum size for the "pending in queue" messages.
 	 */
 	public static final int QUEUE_SIZE = 2000;
 
 	public static final MessageLevel DEFAULT_LEVEL = MessageLevel.TRACE;
 
 	protected MessageLevel filterIgnoreBelow = MessageLevel.TRACE;
 	
 	public MessageLevel getFilterIgnoreBelow() {
 		return filterIgnoreBelow;
 	}
 
 	public void setFilterIgnoreBelow(MessageLevel filterIgnoreBelow) {
 		this.filterIgnoreBelow = filterIgnoreBelow;
 	}
 
 	/**
 	 * Stores the messages as soon as received. Then they will be added to the sorted space;
 	 */
 	protected transient BlockingQueue<ITextMessage> receivedMessages = new LinkedBlockingQueue<ITextMessage>(QUEUE_SIZE);
 
 	
 	/**
 	 * All the messages in natural order (by timestamp)
 	 */
 	private TreeSet<ITextMessage> sortedMessages = new TreeSet<ITextMessage>();
 
 	private transient LinkedList<IListOfMessagesListener> listeners = new LinkedList<IListOfMessagesListener>();
 	
 	private transient long countMessagesCanBeDeleted = 0;
 	
 	protected transient final ReceiveMessagesThread queueConsumerThread;
 
 	/**
 	 * true if received one message (even if it was cleaned) or level error.
 	 */
 	protected transient boolean containedAnError = false;
 	
 	protected static final Map<MessageLevel,Priority> messageLevel2log4jPriority = new HashMap<MessageLevel, Priority>(){{
 		put(MessageLevel.TRACE, Priority.DEBUG);
 		put(MessageLevel.DEBUG, Priority.DEBUG);
 		put(MessageLevel.INFO, Priority.INFO);
 		put(MessageLevel.TIP, Priority.INFO);
 		put(MessageLevel.WARNING, Priority.WARN);
 		put(MessageLevel.ERROR, Priority.ERROR);
 	}};
 	
 	static {
 		// init of LOG4J
 		if (DEFAULT_RELAY_TO_LOG4J) {
 		    BasicConfigurator.configure();
 			Logger.getRootLogger().setLevel(Level.DEBUG);
 			//Logger.getRootLogger().addAppender(new ConsoleAppender());
 		}
 	}
 	
 	public ListOfMessages() {
 		
 		this(DEFAULT_LEVEL, DEFAULT_LIMIT_START_CLEANUP, DEFAULT_CLEANUP_TARGET_SIZE);
 	}
 	
 	public ListOfMessages(MessageLevel level, int cleanupSize) {
 		
 		this(level, cleanupSize, cleanupSize/2);
 	}
 	
 	public ListOfMessages(MessageLevel level, int cleanupSize, int cleanupTarget) {
 		
 		if (cleanupTarget >= cleanupSize)
 			throw new ProgramException("cleanup target size should be lower than cleanup size");
 		
 		this.filterIgnoreBelow = level;
 		this.limitStartCleanup = cleanupSize;
 		this.cleanupTarget = cleanupTarget;
 		
 		queueConsumerThread = new ReceiveMessagesThread();
 		queueConsumerThread.start();
 	}
 	
 	public boolean isEmpty() {
 		return sortedMessages.isEmpty();
 	}
 	
 	public ITextMessage last() {
 		return sortedMessages.last();
 	}
 
 	/**
 	 * Returns true if the list of messages contained an error. Takes into account all the messages (no flush time).
 	 * Takes into account deleted messages.
 	 * @return
 	 */
 	public final boolean containedAnError() {
 		return containedAnError;
 	}
 
 	protected void clearOld() {
 			
 		synchronized (sortedMessages) {
 			
 			//System.err.println("can be cleaned: "+countMessagesCanBeDeleted);
 			
 			long timeBegin = System.currentTimeMillis();
 			Iterator<ITextMessage> it = sortedMessages.iterator();
 			ITextMessage current = null;
 			while (it.hasNext() && countMessagesCanBeDeleted > cleanupTarget) {
 				
 				current = it.next();
 				if (current.getLevel().compareTo(MessageLevel.DEBUG) <= 0) {
 					it.remove();
 					countMessagesCanBeDeleted --;
 				}
 				
 			}
 			//System.err.println("can be cleaned: "+countMessagesCanBeDeleted);
 			//System.err.println("cleanup took "+(System.currentTimeMillis()-timeBegin)+" ms");
 			
 		}
 		
 		
 	}
 	
 	
 	
 	/**
 	 * Adds without raising event
 	 * @param e
 	 * @return
 	 */
 	private void _add(ITextMessage e) {
 		
 		synchronized (sortedMessages) {
 			
 			Iterator<ITextMessage> it = sortedMessages.descendingIterator();
 			
 			// attempt to find a similar message in time (in the previous 3 messages ?)
 			ITextMessage messageIdentical = null;
 			{
 				ITextMessage current = null;
 				int i=3;
 				while (it.hasNext() && (i-- > 0) ) {
 					
 					current = it.next();
 					
 					if (current.equals(e)) {
 						messageIdentical = current;
 						break;
 					}
 						
 				}
 			}
 								
 			if (messageIdentical != null) {
 				// if the message was already stored just a short time before, just add it.
 				messageIdentical.addIncrementCount();
 	
 			} else {
 				sortedMessages.add(e);	
 				
 				if (DEFAULT_RELAY_TO_LOG4J) {
 					
 					final StringBuffer msg = new StringBuffer();
 					
 					// add the basic message
 					msg.append(e.getDate().toString())
 					.append(" - ")
 					.append(e.getMessage());
 					
 					// send it using Logger.
 					Logger.getLogger((e.getClass()==null?Object.class:e.getEmitter())).log(
 							messageLevel2log4jPriority.get(e.getLevel()),
 							msg.toString(),
 							e.getException()
 							);
 				}
 				
 
 				if (e.getLevel().compareTo(MessageLevel.DEBUG) <= 0)
 					countMessagesCanBeDeleted++;
 				
 				if (countMessagesCanBeDeleted > limitStartCleanup)
 					clearOld();
 
 				
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * This thread consumes the queue of messages.
 	 * 
 	 * @author Samuel Thiriot
 	 *
 	 */
 	protected class ReceiveMessagesThread extends Thread {
 		
 		/**
 		 * Once received in the queue, the thread will notify the end of the thread.
 		 */
 		protected final ITextMessage MESSAGE_PILL_NOTIFY_EMPTY = new TextMessage(null, null, this.getClass(), null);
 		
 		/**
 		 * If true, the thread will attempt to stop (when consuming a message)
 		 */
 		protected boolean cancel = false;
 		
 		/**
 		 * Is notified when the queue becomes empty.
 		 */
 		private Object notifierReceivedMessages = new Object();
 		
 		protected boolean notifyWhenEmpty = false;
 		
 		public ReceiveMessagesThread() {
 			super();
 			
 			setName("glConsumeMessages");
 			setPriority(NORM_PRIORITY);
 			setDaemon(true);
 		}
 		
 		public void cancel() {
 			
 			// cancel in case the thread in consuming messages
 			this.cancel = true; 
 		
 			// and interrupt
 			interrupt();
 		}
 		
 		public void waitUntilEndOfQueue() {
 			
 			// submit a magic pill at the end of the queue.
 			// so even if many, many things are still trying to push data into the queue, we are 
 			// in some way accepting a large part of their data before.
 			try {
 				receivedMessages.put(MESSAGE_PILL_NOTIFY_EMPTY);
 			} catch (InterruptedException e1) {
 				throw new RuntimeException("unable to enqueue another message");
 			}
 			
 			// and wait for it to work
 			synchronized (notifierReceivedMessages) {
 				this.interrupt();
 				try {
 					notifierReceivedMessages.wait();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			notifyWhenEmpty = false;
 		}
 		
 		protected void signalQueueEmpty() {
 			synchronized (notifierReceivedMessages) {
 				notifierReceivedMessages.notifyAll();
 			}
 			notifyWhenEmpty = false;
 		}
 		
 		public void run() {
 			
 			long total = 0;
 			long made = 0;
 			
 			while (!cancel) {
 				
 				if (notifyWhenEmpty && receivedMessages.isEmpty()) {
 					signalQueueEmpty();
 				}
 				
 				try {
 					ITextMessage message = receivedMessages.take();
 					if (message == MESSAGE_PILL_NOTIFY_EMPTY) {
 						if (receivedMessages.isEmpty()) {
 							signalQueueEmpty();	
 						} else {
 							notifyWhenEmpty = true;
 						}
 						continue;
 					}
 					long start = System.currentTimeMillis();
 					addDelayed(message);
 					total += (System.currentTimeMillis()-start);
 					made++;
 					if (made > 2 && made % 500 == 1) {
 						System.err.println("adding and sending a message costs ~"+(total/made)+"ms (on "+made+" messages)");
 						total = 0;
 						made = 0;
 					}
 				} catch (InterruptedException e) {
 					
 				}
 				
 				// for debug
 				//if (receivedMessages.remainingCapacity() < 1000)
 				//	System.err.println("****************************** REACHING CAPACITY :!!!!! =========================**");
 				
 			}
 		}
 	}
 	
 	public boolean add(ITextMessage e) {
 				
 		if (!e.getLevel().shouldDisplay(filterIgnoreBelow))
 			return false;
 		
 		
 		try {
 			// add the message to the list of messages to be processed.
 			receivedMessages.put(e); // may wait for the queue to be small enough
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 		
 		if (e.getLevel() == MessageLevel.ERROR) {
 			containedAnError = true; 
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Actual add of the message. Inserts the message in the right place, and calls listeners.
 	 * @param e
 	 */
 	private void addDelayed(ITextMessage e) {
 		
 		_add(e);
 		
 		for (IListOfMessagesListener l : new LinkedList<IListOfMessagesListener>(getListeners())) {
 			l.messageAdded(this, e);
 		}
 		
 		for (IListOfMessagesListener l : new LinkedList<IListOfMessagesListener>(getListeners())) {
 			l.contentChanged(this);
 		}
 		
 	}
 	
 	/**
 	 * Waits until all the messages are processed and stored.
 	 * Remember that once a message was added, it takes time before being added.
 	 */
 	protected void waitUntilMessagesQueueConsumed() {
 		if (queueConsumerThread != null)
 			queueConsumerThread.waitUntilEndOfQueue();
 	}
 	
 	/**
 	 * Returns an iterator on the natural order (that is, sorted by timestamp)
 	 * @return
 	 */
 	public Iterator<ITextMessage> iterator() {
 		// TODO ! wait for the end of dispatch messages queues before analysis !!!
 		
 		waitUntilMessagesQueueConsumed();
 
 		return sortedMessages.iterator();
 	}
 
 	/**
 	 * Adds all the messages from the other
 	 * @param others
 	 * @return
 	 */
 	public boolean addAll(Iterable<ITextMessage> others) {
 
 		for (ITextMessage m : others) {
 			
 			try {
				receivedMessages.put(m);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		/* OLD implementation 
 		 * 
 		 
 		// add messages
 		synchronized (sortedMessages) {
 
 			Iterator<ITextMessage> itOther = others.iterator();
 			
 			while (itOther.hasNext()) {
 				ITextMessage m = itOther.next();
 				
 				if (m.getLevel().shouldDisplay(filterIgnoreBelow))
 					this._add(m);
 			}
 			
 
 		}
 		
 		// raise messages
 		synchronized (listeners) {
 			for (IListOfMessagesListener l : getListeners()) {
 				l.contentChanged(this);
 			}
 		}
 		
 		*/
 		return true;
 		
 	}
 	
 	public void clear() {
 		
 		synchronized (sortedMessages) {
 
 			sortedMessages.clear();
 			
 		}
 		synchronized (listeners) {
 			for (IListOfMessagesListener l : getListeners()) {
 				l.contentChanged(this);
 			}
 		}
 		
 		containedAnError = false;
 		
 	}
 
 	public ITextMessage[] asArray() {
 		
 		synchronized (sortedMessages) {
 
 			return sortedMessages.toArray(new ITextMessage[sortedMessages.size()]);
 		
 		}
 	}
 	
 	private final Collection<IListOfMessagesListener> getOrCreateListeners() {
 		
 		return listeners;
 	}
 	
 	public Collection<IListOfMessagesListener> getListeners() {
 		return listeners;
 	}
 	
 	public void addListener(IListOfMessagesListener l) {
 		if (l == null) 
 			throw new WrongParametersException("listeners should never be null");
 			
 		synchronized (listeners) {
 			getOrCreateListeners().add(l);
 		}
 		
 	}
 	
 	public void removeListener(IListOfMessagesListener l) {
 		synchronized (listeners) {
 			getOrCreateListeners().remove(l);
 		}
 	}
 	
 	public int getSize() {
 	
 		synchronized (sortedMessages) {
 
 			return sortedMessages.size();
 		}
 	}
 		
 	
 	public void debugUser(String message, String fromShort, Class fromClass) {
 		if (!MessageLevel.DEBUG.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.DEBUG, 
 						MessageAudience.USER, 
 						fromShort,
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void debugUser(String message, Class fromClass) {
 		if (!MessageLevel.DEBUG.shouldDisplay(filterIgnoreBelow))
 			return;
 		
 		add(
 				new TextMessage(
 						MessageLevel.DEBUG, 
 						MessageAudience.USER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void warnUser(String message, String fromShort, Class fromClass) {
 		if (!MessageLevel.WARNING.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.WARNING, 
 						MessageAudience.USER, 
 						fromShort,
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void warnUser(String message, Class fromClass) {
 		if (!MessageLevel.WARNING.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.WARNING, 
 						MessageAudience.USER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void infoUser(String message, String fromShort, Class fromClass) {
 		if (!MessageLevel.INFO.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.INFO, 
 						MessageAudience.USER, 
 						fromShort,
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void infoUser(String message, Class fromClass) {
 		if (!MessageLevel.INFO.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.INFO, 
 						MessageAudience.USER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void tipUser(String message, String fromShort, Class fromClass) {
 		if (!MessageLevel.TIP.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.TIP, 
 						MessageAudience.USER, 
 						fromShort,
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void tipUser(String message, Class fromClass) {
 		if (!MessageLevel.TIP.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.TIP, 
 						MessageAudience.USER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message
 						)
 			);
 	}
 	
 
 	public void errorUser(String message, String fromShort, Class fromClass) {
 		if (!MessageLevel.ERROR.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.ERROR, 
 						MessageAudience.USER, 
 						fromShort,
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void errorUser(String message, Class fromClass, Throwable e) {
 		if (!MessageLevel.ERROR.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.ERROR, 
 						MessageAudience.USER, 
 						fromClass,
 						message, 
 						e
 						)
 			);
 	}
 	
 	public void errorUser(String message, Class fromClass) {
 		if (!MessageLevel.ERROR.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.ERROR, 
 						MessageAudience.USER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message
 						)
 			);
 	}
 	public void traceTech(String message, String fromShort, Class fromClass) {
 		if (!MessageLevel.TRACE.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.TRACE, 
 						MessageAudience.DEVELOPER, 
 						fromShort,
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void traceTech(String message, Class fromClass) {
 		if (!MessageLevel.TRACE.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.TRACE, 
 						MessageAudience.DEVELOPER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void traceTech(String message, String fromShort, Class fromClass, Throwable e) {
 		if (!MessageLevel.TRACE.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.TRACE, 
 						MessageAudience.DEVELOPER, 
 						fromShort,
 						fromClass,
 						message,
 						e
 						)
 			);
 	}
 	
 	public void traceTech(String message, Class fromClass, Throwable e) {
 		if (!MessageLevel.TRACE.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.TRACE, 
 						MessageAudience.DEVELOPER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message,
 						e
 						)
 			);
 	}
 	
 	public void debugTech(String message, String fromShort, Class fromClass) {
 		if (!MessageLevel.DEBUG.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.DEBUG, 
 						MessageAudience.DEVELOPER, 
 						fromShort,
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void debugTech(String message, Class fromClass) {
 		
 		if (!MessageLevel.DEBUG.shouldDisplay(filterIgnoreBelow))
 			return;
 		
 		add(
 				new TextMessage(
 						MessageLevel.DEBUG, 
 						MessageAudience.DEVELOPER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void debugTech(String message, String fromShort, Class fromClass, Throwable e) {
 		
 		if (!MessageLevel.DEBUG.shouldDisplay(filterIgnoreBelow))
 			return;
 		
 		add(
 				new TextMessage(
 						MessageLevel.DEBUG, 
 						MessageAudience.DEVELOPER, 
 						fromShort,
 						fromClass,
 						message,
 						e
 						)
 			);
 	}
 	
 	public void debugTech(String message, Class fromClass, Throwable e) {
 		if (!MessageLevel.DEBUG.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.DEBUG, 
 						MessageAudience.DEVELOPER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message,
 						e
 						)
 			);
 	}
 	
 	public void warnTech(String message, String fromShort, Class fromClass) {
 		if (!MessageLevel.WARNING.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.WARNING, 
 						MessageAudience.DEVELOPER, 
 						fromShort,
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void warnTech(String message, Class fromClass) {
 		if (!MessageLevel.WARNING.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.WARNING, 
 						MessageAudience.DEVELOPER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void warnTech(String message, String fromShort, Class fromClass, Throwable e) {
 		if (!MessageLevel.WARNING.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.WARNING, 
 						MessageAudience.DEVELOPER, 
 						fromShort,
 						fromClass,
 						message,
 						e
 						)
 			);
 	}
 	
 	public void warnTech(String message, Class fromClass, Throwable e) {
 		if (!MessageLevel.WARNING.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.WARNING, 
 						MessageAudience.DEVELOPER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message,
 						e
 						)
 			);
 	}
 	
 	public void infoTech(String message, String fromShort, Class fromClass) {
 		if (!MessageLevel.INFO.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.INFO, 
 						MessageAudience.DEVELOPER, 
 						fromShort,
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void infoTech(String message, Class fromClass) {
 		if (!MessageLevel.INFO.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.INFO, 
 						MessageAudience.DEVELOPER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void tipTech(String message, String fromShort, Class fromClass) {
 		if (!MessageLevel.TIP.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.TIP, 
 						MessageAudience.DEVELOPER, 
 						fromShort,
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void tipTech(String message, Class fromClass) {
 		if (!MessageLevel.TIP.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.TIP, 
 						MessageAudience.DEVELOPER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void errorTech(String message, String fromShort, Class fromClass) {
 		if (!MessageLevel.ERROR.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.ERROR, 
 						MessageAudience.DEVELOPER, 
 						fromShort,
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void errorTech(String message, Class fromClass) {
 		if (!MessageLevel.ERROR.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.ERROR, 
 						MessageAudience.DEVELOPER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message
 						)
 			);
 	}
 	
 	public void errorTech(String message, String fromShort, Class fromClass, Throwable e) {
 		if (!MessageLevel.ERROR.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.ERROR, 
 						MessageAudience.DEVELOPER, 
 						fromShort,
 						fromClass,
 						message,
 						e
 						)
 			);
 	}
 	
 	public void errorTech(String message, Class fromClass, Throwable e) {
 		if (!MessageLevel.ERROR.shouldDisplay(filterIgnoreBelow))
 			return;
 		add(
 				new TextMessage(
 						MessageLevel.ERROR, 
 						MessageAudience.DEVELOPER, 
 						fromClass.getSimpleName(),
 						fromClass,
 						message,
 						e
 						)
 			);
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 	
 		if (queueConsumerThread != null) {
 			queueConsumerThread.cancel();
 		}
 		
 		super.finalize();
 	}
 	
 	/**
 	 * Returns true if it contains a messages of this level. O(n).
 	 * @param searchedLevel
 	 * @return
 	 */
 	public boolean containsMessageLevel(MessageLevel searchedLevel) {
 		 
 		synchronized (sortedMessages) {
 	
 			for (ITextMessage message: sortedMessages) {
 				if (message.getLevel() == searchedLevel)
 					return true;
 			}
 
 		}
 		return false;
 	}
 	
 	/**
 	 * Dumps the messages to stream
 	 * @param ps
 	 */
 	public void dumpToStream(PrintStream ps) {
 		
 		synchronized (sortedMessages) {
 
 			for (ITextMessage message: sortedMessages) {
 				ps.println(message.toString());
 			}
 		}
 	}
 	
 }
 
