 /**
  * @author Christian Wellenbrock
  * @author Florian Simon
  * @author Jrgen Walter
  * @author Stefan Kober
  * Teams 09, 10
  *
  * This code has been developed during the winter term 2010-2011 at the
  * Karlsruhe Institute of Technology (KIT), Germany.
  * It is part of a project assignment in the course
  * "Multicore Programming in Practice: Tools, Models, and Languages".
  * Project director/instructor:
  * Dr. Victor Pankratius (pankratius@ipd.uka.de)
 **/
 package attributes;
 
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 
 import database.Scope;
 
 /**
  * This class stores all attribute names that can be used for mining.
  */
 public class Attribute {
 	private Scope scope;
 	private String name;
 	private boolean parallel;
 	
 	private static HashMap<String, Attribute> attributes = new HashMap<String, Attribute>();
 	
 	/**
 	 * combines all passed {@code Attribute}s to one {@code String} 
 	 * @param objects {@code Attributes}
 	 * @return a {@code String} with names for all objects
 	 */
 	public static String combine(Object ... objects) {
 		String result = "";
 		for (Object object: objects) {
 			String attributeName = object.toString();
 			
 			if (!result.equals("") && !attributeName.equals("")) {
 				result += ", ";
 			}
 			
 			result += attributeName;
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * {@link Constructor}
 	 * @param scope
 	 * @param name
 	 * @param parallel
 	 */
 	public Attribute(Scope scope, String name, boolean parallel) {
 		this.scope = scope;
 		this.name = name;
 		this.parallel = parallel;
 		
 		attributes.put(name, this);
 	}
 	
 	/**
 	 * gets {@code Attribute} by {@code String}
 	 * @param name name of the wanted {@code Attribute}
 	 * @return {@code Attribute}
 	 */
 	public static Attribute getAttribute(String name) {
		Attribute attribute = attributes.get(name);
		
		if (attribute == null) {
			System.err.println("attribute not found: " + name);
		}
		
		return attribute;
 	}
 
 	/**
 	 * gets {@code Scope} of {@code Attribute}
 	 * @return {@code Scope} of {@code Attribute}
 	 */
 	public Scope getScope() {
 		return scope;
 	}
 	
 	/**
 	 * gets name of {@code Attribute}
 	 * @return name of {@code Attribute}
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * returns {@code Attribute#name}
 	 * @return name of {@code Attribute}
 	 */
 	public String toString() {
 		return getName();
 	}
 	
 	/**
 	 * says if this is a parallel {@code Attribute}
 	 * @return is parallel
 	 */
 	public boolean isParallel() {
 		return parallel;
 	}
 
 
 	/** name of project */
 	public static final Attribute PROJECT_NAME = new Attribute(Scope.PROJECT, "PROJECT_NAME", false);
 	/** name of class */	
 	public static final Attribute CLASS_NAME = new Attribute(Scope.METHOD, "CLASS_NAME", false);
 	/** name of method */
 	public static final Attribute METHOD_NAME = new Attribute(Scope.CLASS, "METHOD_NAME", false);
 	
 	/** number of {@code public} methods */
 	public static final Attribute PUBLIC_METHODS = new Attribute(Scope.METHOD, "PUBLIC_METHODS", false);
 	/** number of {@code private} methods */
 	public static final Attribute PRIVATE_METHODS = new Attribute(Scope.METHOD, "PRIVATE_METHODS", false);
 	/** number of {@code synchronized} methods */
 	public static final Attribute SYNCHRONIZED_METHODS = new Attribute(Scope.METHOD, "SYNCHRONIZED_METHODS", true);
 	
 	/** number of method calls in a method*/
 	public static final Attribute METHOD_CALLS = new Attribute(Scope.METHOD, "METHOD_CALLS", false);
 	/** number of {@code synchronized} calls in a method*/
 	public static final Attribute SYNCHRONIZED_BLOCKS = new Attribute(Scope.METHOD, "SYNCHRONIZED_BLOCKS", true);
 	
 	/** number of while wait pattern */
 	public static final Attribute WHILE_WAIT = new Attribute(Scope.METHOD, "WHILE_WAIT", true);
 	/** number of conditional wait pattern */
 	public static final Attribute CONDITIONAL_WAIT = new Attribute(Scope.METHOD, "CONDITIONAL_WAIT", true);
 	/** number of double checked lock pattern */
 	public static final Attribute DOUBLE_CHECKED_LOCK = new Attribute(Scope.METHOD, "DOUBLE_CHECKED_LOCK", true);;
 
 	/** number of {@code Lock.lock()} calls */
 	public static final Attribute LOCK_CALLS = new Attribute(Scope.METHOD, "LOCK_CALLS", true);
 	/** number of {@code Lock.unlock()} calls */
 	public static final Attribute UNLOCK_CALLS= new Attribute(Scope.METHOD, "UNLOCK_CALLS", true);
 	/** number of {@code Thread.wait()} calls */
 	public static final Attribute WAIT_CALLS = new Attribute(Scope.METHOD, "WAIT_CALLS", true);
 	/** number of {@code Thread.notify()} calls */
 	public static final Attribute NOTIFY_CALLS = new Attribute(Scope.METHOD, "NOTIFY_CALLS", true);
 	/** number of {@code Thread.notifyAll()} calls */
 	public static final Attribute NOTIFYALL_CALLS = new Attribute(Scope.METHOD, "NOTIFYALL_CALLS", true);
 	/** number of {@code Thread.sleep(long)} calls */
 	public static final Attribute SLEEP_CALLS = new Attribute(Scope.METHOD, "SLEEP_CALLS", true);
 	/** number of {@code Thread.yield()} calls */
 	public static final Attribute YIELD_CALLS = new Attribute(Scope.METHOD, "YIELD_CALLS", true);
 	/** number of {@code Thread.join()} calls */
 	public static final Attribute JOIN_CALLS = new Attribute(Scope.METHOD, "JOIN_CALLS", true);
 	/** number of {@code Thread.start()} calls */
 	public static final Attribute START_CALLS = new Attribute(Scope.METHOD, "START_CALLS", true);
 	/** number of {@code Thread.run()} calls */
 	public static final Attribute RUN_CALLS = new Attribute(Scope.METHOD, "RUN_CALLS", true);
 	
 	/** number of ({@link java.util.concurrent.locks.ReentrantLock | ReadLock | WriteLock | Lock}) objects in method*/
 	public static final Attribute LOCK_OBJECTS = new Attribute(Scope.METHOD, "LOCK_OBJECTS", true);
 	/** number of {@link java.util.concurrent.locks CountDownLatch} objects in method */	
 	public static final Attribute COUNT_DOWN_OBJECTS = new Attribute(Scope.METHOD, "COUNTDOWNLATCH_OBJECTS", true);
 	/** number of {@link java.util.concurrent.locks.Condition} objects in method */		
 	public static final Attribute CONDITION_OBJECTS = new Attribute(Scope.METHOD, "CONDITION_OBJECTS", true);
 	/** number of {@link java.util.concurrent.locks.Semaphore} objects in method */		
 	public static final Attribute SEMAPHORE_OBJECTS = new Attribute(Scope.METHOD, "SEMAPHORE_OBJECTS", true);
 	/** number of {@link java.util.concurrent.locks.CyclicBarrier} objects in method */
 	public static final Attribute CYCLICBARRIER_OBJECTS = new Attribute(Scope.METHOD, "CYCLICBARRIER_OBJECTS", true);
 	
 	/** nestedness of {@code lock} */			
 	public static final Attribute NESTEDNESS_LOCKS = new Attribute(Scope.METHOD, "NESTEDNESS_LOCKS", true);
 	/** nestedness of {@code synchronized}*/
 	public static final Attribute NESTEDNESS_SYNCHRONIZED = new Attribute(Scope.METHOD, "NESTEDNESS_SYNCHRONIZED", true);
 	/** 
 	 * nestedness of {@code if}, example:
 	 * {@code 
 	 *   if (a) {
 	 *     if (b){
 	 *     }
 	 *   }
 	 * }
 	 * has nestedness of 2
 	 *  */			
 	public static final Attribute NESTEDNESS_CONDITIONALS = new Attribute(Scope.METHOD, "NESTEDNESS_CONDITIONALS", false);
 	/** nestedness of {@code for| while | do while} */			
 	public static final Attribute NESTEDNESS_LOOPS = new Attribute(Scope.METHOD, "NESTEDNESS_LOOPS", false);
 	
 	/** number of methods used in class */
 	public static final Attribute NUMBER_OF_METHODS = new Attribute(Scope.CLASS, "NUMBER_OF_METHODS", true);
 	/** number of {@code volatile} variables used in class */
 	public static final Attribute VOLATILE_VARIABLES = new Attribute(Scope.CLASS, "VOLATILE_VARIABLES", true);
 	/** number of {@code public} variables used in class */
 	public static final Attribute PUBLIC_VARIABLES = new Attribute(Scope.CLASS, "PUBLIC_VARIABLES", true);
 	/** number of {@code private} variables used in class */
 	public static final Attribute PRIVATE_VARIABLES = new Attribute(Scope.CLASS, "PRIVATE_VARIABLES", true);
 	
 	
 	/** number of {@code ReentrantLock, ReadLock, WriteLock or Lock} objects in class*/
 	public static final Attribute LOCK_OBJECT_FIELDS = new Attribute(Scope.CLASS, "LOCK_OBJECT_FIELDS", true);
 	/** number of {@code Condition} objects in class */
 	public static final Attribute CONDITION_OBJECT_FIELDS = new Attribute(Scope.CLASS, "CONDITION_OBJECT_FIELDS", true);
 	/** number of {@code Executor} objects in class */
 	public static final Attribute EXECUTOR_OBJECT_FIELDS = new Attribute(Scope.CLASS, "EXECUTOR_OBJECT_FIELDS", true);
 	/** number of {@code ConcurrentMap} objects in class */
 	public static final Attribute CONCURRENTMAP_OBJECT_FIELDS = new Attribute(Scope.CLASS, "CONCURRENTMAP_OBJECT_FIELDS", true);
 	/** number of {@code AtomicInteger} objects in class */
 	public static final Attribute ATOMICINTEGER_OBJECT_FIELDS = new Attribute(Scope.CLASS, "ATOMICINTEGER_OBJECT_FIELDS", true);
 	/** number of {@code BlockingQueue} objects in class */
 	public static final Attribute BLOCKINGQUEUE_OBJECT_FIELDS = new Attribute(Scope.CLASS, "BLOCKINGQUEUE_OBJECT_FIELDS", true);
 	/** number of {@code ConcurrentLinkedQueue} objects in class */
 	public static final Attribute CONCURRENTLINKEDQUEUE_OBJECT_FIELDS = new Attribute(Scope.CLASS, "CONCURRENTLINKEDQUEUE_OBJECT_FIELDS", true);
 	/** number of {@code CopyOnWriteArrayList} objects in class */
 	public static final Attribute COPYONWRITEARRAYLIST_OBJECT_FIELDS = new Attribute(Scope.CLASS, "COPYONWRITEARRAYLIST_OBJECT_FIELDS", true);
 	/** number of {@code ExecutorService} objects in class */
 	public static final Attribute EXECUTORSERVICE_OBJECT_FIELDS = new Attribute(Scope.CLASS, "EXECUTORSERVICE_OBJECT_FIELDS", true);
 	/** number of {@code Future} objects in class */
 	public static final Attribute FUTURE_OBJECT_FIELDS = new Attribute(Scope.CLASS, "FUTURE_OBJECT_FIELDS", true);
 	/** number of {@code ThreadPoolExecutor} objects in class */
 	public static final Attribute THREADPOOLEXECUTOR_OBJECT_FIELDS = new Attribute(Scope.CLASS, "THREADPOOLEXECUTOR_OBJECT_FIELDS", true);
 	/** number of {@code CountDownLatch} objects in class */
 	public static final Attribute COUNTDOWNLATCH_OBJECT_FIELDS = new Attribute(Scope.CLASS, "COUNTDOWNLATCH_OBJECT_FIELDS", true);
 	/** number of {@code CyclicBarrier} objects in class */		
 	public static final Attribute CYCLICBARRIER_OBJECT_FIELDS = new Attribute(Scope.CLASS, "CYCLICBARRIER_OBJECT_FIELDS", true);
 	/** number of {@code Exchanger} objects in class */		
 	public static final Attribute EXCHANGER_OBJECT_FIELDS = new Attribute(Scope.CLASS, "EXCHANGER_OBJECT_FIELDS", true);
 	/** number of {@code Semaphore} objects in class */		
 	public static final Attribute SEMAPHORE_OBJECT_FIELDS = new Attribute(Scope.CLASS, "SEMAPHORE_OBJECT_FIELDS", true);
 	/** number of {@code ThreadFactory} objects in class */		
 	public static final Attribute THREADFACTORY_OBJECT_FIELDS = new Attribute(Scope.CLASS, "THREADFACTORY_OBJECT_FIELDS", true);
 	
 	/** number of {@code Callable} interface use */
 	public static final Attribute CALLABLE_INTERFACE = new Attribute(Scope.CLASS, "CALLABLE_INTERFACE", true);
 	/** number of {@code Delayed} interface use */
 	public static final Attribute DELAYED_INTERFACE = new Attribute(Scope.CLASS, "DELAYED_INTERFACE", true);
 	/** number of {@code ThreadFactory} interface use */
 	public static final Attribute THREADFACTORY_INTERFACE = new Attribute(Scope.CLASS, "THREADFACTORY_INTERFACE", true);
 	/** number of {@code BlockingQueue} interface use */
 	public static final Attribute BLOCKINGQUEUE_INTERFACE = new Attribute(Scope.CLASS, "BLOCKINGQUEUE_INTERFACE", true);
 
 	/** class {@code extends ConcurrentMap}*/
 	public static final Attribute CONCURRENTMAP_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "CONCURRENTMAP_OBJECTS_EXTENDS", true);
 	/** class {@code extends ExecutorService}*/
 	public static final Attribute EXECUTORSERVICE_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "EXECUTORSERVICE_OBJECTS_EXTENDS", true);
 	/** class {@code extends ThreadPoolExecutor}*/
 	public static final Attribute THREADPOOLEXECUTOR_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "THREADPOOLEXECUTOR_OBJECTS_EXTENDS", true);
 	/** class {@code extends CyclicBarrier}*/
 	public static final Attribute CYCLICBARRIER_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "CYCLICBARRIER_OBJECTS_EXTENDS", true);
 	/** class {@code extends Exchanger}*/
 	public static final Attribute EXCHANGER_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "EXCHANGER_OBJECTS_EXTENDS", true);
 	/** class {@code extends Semaphore}*/
 	public static final Attribute SEMAPHORE_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "SEMAPHORE_OBJECTS_EXTENDS", true);
 	/** class {@code extends Lock}*/
 	public static final Attribute LOCK_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "LOCK_OBJECTS_EXTENDS", true);
 	/** class {@code extends Executor}*/
 	public static final Attribute EXECUTOR_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "EXECUTOR_OBJECTS_EXTENDS", true);
 	/** class {@code extends AtomicInteger}*/
 	public static final Attribute ATOMICINTEGER_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "ATOMICINTEGER_OBJECTS_EXTENDS", true);
 	/** class {@code extends ConcurrentLinkedQueue}*/
 	public static final Attribute CONCURRENTLINKEDQUEUE_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "CONCURRENTLINKEDQUEUE_OBJECTS_EXTENDS", true);
 	/** class {@code extends CopyOnWriteArrayList}*/
 	public static final Attribute COPYONWRITEARRAYLIST_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "COPYONWRITEARRAYLIST_OBJECTS_EXTENDS", true);
 	/** class {@code extends Future}*/
 	public static final Attribute FUTURE_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "FUTURE_OBJECTS_EXTENDS", true);
 	/** class {@code extends CountDownLatch}*/
 	public static final Attribute COUNTDOWNLATCH_OBJECTS_EXTENDS = new Attribute(Scope.CLASS, "COUNTDOWNLATCH_OBJECTS_EXTENDS", true);
 	
 	/**
 	 * returns all {@code synchronized Attribute}s
 	 * @return {@code SYNCHRONIZED_METHODS and SYNCHRONIZED_BLOCKS}
 	 */
 	public static String getAllSynchronizedAttributes() {
 		return Attribute.combine(SYNCHRONIZED_METHODS, 
 				SYNCHRONIZED_BLOCKS);
 	}
 	
 	/**
 	 * returns all code pattern {@code Attribute}s
 	 * @return {@code WHILE_WAIT, CONDITIONAL_WAIT, DOUBLE_CHECKED_LOCK }
 	 */
 	public static String getAllPatternAttributes() {
 		return Attribute.combine(
 				WHILE_WAIT, 
 				CONDITIONAL_WAIT,
 				DOUBLE_CHECKED_LOCK);
 	}
 	
 	/**
 	 * returns all method call {@code Attribute}s
 	 * @return {@code LOCK_CALLS, UNLOCK_CALLS, WAIT_CALLS, NOTIFY_CALLS,
 	 * 			NOTIFYALL_CALLS, SLEEP_CALLS, YIELD_CALLS, JOIN_CALLS,
 	 * 			START_CALLS, RUN_CALLS}
 	 */
 	public static String getAllMethodCallAttributes() {
 		return Attribute.combine(LOCK_CALLS, 
 				UNLOCK_CALLS,
 				WAIT_CALLS,
 				NOTIFY_CALLS,
 				NOTIFYALL_CALLS,
 				SLEEP_CALLS,
 				YIELD_CALLS,
 				JOIN_CALLS,
 				START_CALLS,
 				RUN_CALLS);
 	}
 	
 	/**
 	 * returns all object {@code Attribute}s
 	 * @return {@code LOCK_OBJECTS, COUNT_DOWN_OBJECTS, CONDITION_OBJECTS,
 	 * 			SEMAPHORE_OBJECTS, CYCLICBARRIER_OBJECTS}
 	 */
 	public static String getAllObjectAttributes() {
 		return Attribute.combine(LOCK_OBJECTS, 
 				COUNT_DOWN_OBJECTS,
 				CONDITION_OBJECTS,
 				SEMAPHORE_OBJECTS,
 				CYCLICBARRIER_OBJECTS);
 	}
 	
 	/**
 	 * returns all nestedness {@code Attribute}s
 	 * @return {@code ESTEDNESS_LOCKS,NESTEDNESS_SYNCHRONIZED,
 	 * 			NESTEDNESS_CONDITIONALS,NESTEDNESS_LOOPS}
 	 */
 	public static String getAllNestednessAttributes() {
 		return Attribute.combine(NESTEDNESS_LOCKS, 
 				NESTEDNESS_SYNCHRONIZED,
 				NESTEDNESS_CONDITIONALS,
 				NESTEDNESS_LOOPS);
 	}
 	
 	/**
 	 * returns all object field {@code Attribute}s
 	 * @return {@code LOCK_OBJECT_FIELDS, CONDITION_OBJECT_FIELDS,
 	 * 			EXECUTOR_OBJECT_FIELDS, CONCURRENTMAP_OBJECT_FIELDS,
 	 * 			ATOMICINTEGER_OBJECT_FIELDS, BLOCKINGQUEUE_OBJECT_FIELDS,
 	 * 			CONCURRENTLINKEDQUEUE_OBJECT_FIELDS, COPYONWRITEARRAYLIST_OBJECT_FIELDS,
 	 * 			EXECUTORSERVICE_OBJECT_FIELDS, FUTURE_OBJECT_FIELDS,
 	 * 			THREADPOOLEXECUTOR_OBJECT_FIELDS, COUNTDOWNLATCH_OBJECT_FIELDS,
 	 * 			CYCLICBARRIER_OBJECT_FIELDS, EXCHANGER_OBJECT_FIELDS,
 	 * 			SEMAPHORE_OBJECT_FIELDS, THREADFACTORY_OBJECT_FIELDS}
 	 */
 	public static String getAllObjectFieldsAttributes() {
 		return Attribute.combine(LOCK_OBJECT_FIELDS, 
 				CONDITION_OBJECT_FIELDS,
 				EXECUTOR_OBJECT_FIELDS,
 				CONCURRENTMAP_OBJECT_FIELDS,
 				ATOMICINTEGER_OBJECT_FIELDS,
 				BLOCKINGQUEUE_OBJECT_FIELDS,
 				CONCURRENTLINKEDQUEUE_OBJECT_FIELDS,
 				COPYONWRITEARRAYLIST_OBJECT_FIELDS,
 				EXECUTORSERVICE_OBJECT_FIELDS,
 				FUTURE_OBJECT_FIELDS,
 				THREADPOOLEXECUTOR_OBJECT_FIELDS,
 				COUNTDOWNLATCH_OBJECT_FIELDS,
 				CYCLICBARRIER_OBJECT_FIELDS,
 				EXCHANGER_OBJECT_FIELDS,
 				SEMAPHORE_OBJECT_FIELDS,
 				THREADFACTORY_OBJECT_FIELDS);
 	}
 	
 	/**
 	 * returns all {@code Interface Attribute}s
 	 * @return {@code CALLABLE_INTERFACE, DELAYED_INTERFACE,THREADFACTORY_INTERFACE,
 	 * 			BLOCKINGQUEUE_INTERFACE}
 	 */
 	public static String getAllInterfaceAttributes() {
 		return Attribute.combine(CALLABLE_INTERFACE, 
 				DELAYED_INTERFACE,
 				THREADFACTORY_INTERFACE,
 				BLOCKINGQUEUE_INTERFACE);
 	}
 	
 	/**
 	 * returns all {@code extends Attribute}s
 	 * @return {@code CONCURRENTMAP_OBJECTS_EXTENDS, EXECUTORSERVICE_OBJECTS_EXTENDS,
 	 * 			THREADPOOLEXECUTOR_OBJECTS_EXTENDS, CYCLICBARRIER_OBJECTS_EXTENDS,
 	 * 			EXCHANGER_OBJECTS_EXTENDS, SEMAPHORE_OBJECTS_EXTENDS,
 	 * 			LOCK_OBJECTS_EXTENDS, EXECUTOR_OBJECTS_EXTENDS,
 	 * 			ATOMICINTEGER_OBJECTS_EXTENDS, CONCURRENTLINKEDQUEUE_OBJECTS_EXTENDS,
 	 * 			COPYONWRITEARRAYLIST_OBJECTS_EXTENDS, FUTURE_OBJECTS_EXTENDS,
 	 * 			COUNTDOWNLATCH_OBJECTS_EXTENDS}
 	 */
 	public static String getAllObjectExtendsAttributes() {
 		return Attribute.combine(CONCURRENTMAP_OBJECTS_EXTENDS, 
 				EXECUTORSERVICE_OBJECTS_EXTENDS,
 				THREADPOOLEXECUTOR_OBJECTS_EXTENDS,
 				CYCLICBARRIER_OBJECTS_EXTENDS,
 				EXCHANGER_OBJECTS_EXTENDS,
 				SEMAPHORE_OBJECTS_EXTENDS,
 				LOCK_OBJECTS_EXTENDS,
 				EXECUTOR_OBJECTS_EXTENDS,
 				ATOMICINTEGER_OBJECTS_EXTENDS,
 				CONCURRENTLINKEDQUEUE_OBJECTS_EXTENDS,
 				COPYONWRITEARRAYLIST_OBJECTS_EXTENDS,
 				FUTURE_OBJECTS_EXTENDS,
 				COUNTDOWNLATCH_OBJECTS_EXTENDS);
 	}
 
 }
