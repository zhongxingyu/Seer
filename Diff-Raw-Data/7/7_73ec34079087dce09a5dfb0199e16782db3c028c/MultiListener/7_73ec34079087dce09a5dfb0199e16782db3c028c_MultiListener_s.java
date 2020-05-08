 package gt.plugin.listener;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventException;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.EventExecutor;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 
 /**
  * Wraps the bukkit listener system to achieve the ability to unregister
  * listeners. This is believed to be compatible to bukkit for most basic cases
  * (dont try to use priorities or so)
  * 
  * @author Sebastian Fahnenschreiber
  * 
  */
 public final class MultiListener implements Listener, EventExecutor {
 
 	private static MultiListener multiListenerInstance;
 	
 	/**
 	 * Identifies an atomic event listener
 	 */
 	private class AtomicListener {
 		private final Method method;
 		private final Listener listener;
 		private final Class<? extends Event> event;
 
 		/**
 		 * @param method
 		 *            The method to be executed
 		 * @param listner
 		 *            The class which contains method
 		 * @param event
 		 *            The event type to be listened
 		 */
 		public AtomicListener(final Method method, final Listener listner,
 				final Class<? extends Event> event) {
 			this.method = method;
 			this.listener = listner;
 			this.event = event;
 		}
 	}
 
 	private final Multimap<Class<? extends Event>, AtomicListener> events;
 	private final Multimap<Listener, AtomicListener> listeners;
 
 	private final Set<Class<? extends Event>> registeredListeners;
 
 	private final JavaPlugin plugin;
 
 	/**
 	 * Generates a new Multilistener
 	 * 
 	 * @param plugin
 	 *            Whose Events should be handled.
 	 */
 	private MultiListener(final JavaPlugin plugin) {
 		events = HashMultimap.create();
 		listeners = HashMultimap.create();
 		registeredListeners = new HashSet<Class<? extends Event>>();
 
 		this.plugin = plugin;
 	}
 	
 	/**
 	 * @param listener
 	 *            the listener to be registered
 	 */
 	private void doRegisterListener(final Listener listener) {
 		Class<? extends Listener> cls = listener.getClass();
		for (Method method : cls.getDeclaredMethods()) {
 			if (method.isAnnotationPresent(EventHandler.class)) {
 				Class<? extends Event> event = getEventType(method);

 				AtomicListener al = new AtomicListener(method, listener, event);
 
 				events.put(event, al);
 				listeners.put(listener, al);
 
 				registerEvent(event);
 			}
 		}
 
 	}
 
 	/**
 	 * parses the type of the event method of the given method is
 	 * 
 	 * @param method
 	 *            the method to be parsed
 	 * @return the type of the first argument
 	 */
 	@SuppressWarnings("unchecked")
 	private Class<? extends Event> getEventType(final Method method) {
 		Class<?> param[] = method.getParameterTypes();
 		if (param.length == 1 && Event.class.isAssignableFrom(param[0])) {
 			return (Class<? extends Event>) param[0];
 		}
 		throw new RuntimeException("invalid listener method deteckted");
 	}
 
 	/**
 	 * @param event
 	 *            the event to be registered with bukkit
 	 */
 	private void registerEvent(final Class<? extends Event> event) {
 		if (!registeredListeners.contains(event)) {
 			Bukkit.getPluginManager().registerEvent(event, this,
 					EventPriority.NORMAL, this, plugin);
 		}
 	}
 
 	/**
 	 * @param listener
 	 *            the listener to be removed
 	 */
 	private void doUnregisterListener(final Listener listener) {
 
 		for (AtomicListener al : listeners.removeAll(listener)) {
 			events.remove(al.event, al);
 		}
 
 	}
 
 	@Override
 	public void execute(final Listener listener, final Event event)
 			throws EventException {
 		try {
 
 			for (AtomicListener al : events.get(event.getClass())) {
 				al.method.invoke(al.listener, event);
 			}
 
 		} catch (InvocationTargetException ex) {
 			throw new EventException(ex.getCause());
 		} catch (Throwable t) {
 			throw new EventException(t);
 		}
 	}
 
 	/**
 	 * initializes the MultiListener
 	 * 
 	 * @param plugin Whose Events should be handled.
 	 */
 	public static void initialize(final JavaPlugin plugin) {
 		if(multiListenerInstance == null) {
 			multiListenerInstance = new MultiListener(plugin);
 		}
 	}
 	
 	
 	/**
 	 * Shortcut function to register listeners
 	 * 
 	 * @param listener the Listener to be registered
 	 */
 	public static void registerListener(final Listener listener) {
 		ensureInitialized();
 		multiListenerInstance.doRegisterListener(listener);
 	}
 
 	/**
 	 * @param listeners
 	 *            the listeners to be registered
 	 */
 	public static void registerListeners(final Listener... listeners) {
 		ensureInitialized();
 		for (Listener l : listeners) {
 			multiListenerInstance.doRegisterListener(l);
 		}
 	}
 	
 	/**
 	 * Shortcut function to register listeners
 	 * 
 	 * @param listener the Listener to be registered
 	 */
 	public static void unregisterListener(final Listener listener) {
 		ensureInitialized();
 		multiListenerInstance.doUnregisterListener(listener);
 	}
 	
 	/**
 	 * throws an exception if this is not ready for use
 	 */
 	private static void ensureInitialized() {
 		if(multiListenerInstance == null) {
 			throw new RuntimeException("The MultiListener must be initialized first.");
 		}
 	}
 	
 }
