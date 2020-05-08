 package btwmods.events;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import btwmods.ModLoader;
 
 public class EventDispatcherFactory implements InvocationHandler, EventDispatcher {
 	
 	private static long invocationCount = 0;
 	
 	public static long getInvocationCount() {
 		return invocationCount;
 	}
 
 	private enum QueueAction { ADD, REMOVE };
 	private static class QueuedListener {
 		public final QueueAction action;
 		public final IAPIListener listener;
 		public final Class listenerClass;
 		
 		public QueuedListener(QueueAction action, IAPIListener listener, Class listenerClass) {
 			this.action = action;
 			this.listener = listener;
 			this.listenerClass = listenerClass;
 		}
 	}
 	
 	public static class Invocation {
 		public final IAPIListener listener;
 		public final Method method;
 		public final Object[] args;
 		
 		public Invocation(IAPIListener listener, Method method, Object[] args) {
 			this.listener = listener;
 			this.method = method;
 			this.args = args;
 		}
 		
 		public void invoke() throws InvocationTargetException, IllegalAccessException {
 			method.invoke(listener, args);
 		}
 	}
 	
 	/**
 	 * Create a new event dispatcher that implements the methods defined in an array of interfaces.
 	 * 
 	 * @param  interfaces An array of interfaces that will implemented on the returned proxy.
 	 * @return A proxy that implements all the supplied interfaces.
 	 */
 	public static EventDispatcher create(Class<IAPIListener>[] interfaces) {
 		return create(interfaces, null);
 	}
 	
 	/**
 	 * Create a new event dispatcher that implements the methods defined in an array of interfaces.
 	 * EventObject parameters in those methods will be first processed by an event preprocessor.
 	 * 
 	 * @param interfaces An array of interfaces that will implemented on the returned proxy.
 	 * @param invocationWrapper EventObject parameters in the interfaces' methods will be first processed by this.
 	 * @return A proxy that implements all the supplied interfaces.
 	 */
 	public static EventDispatcher create(Class<IAPIListener>[] interfaces, IInvocationWrapper invocationWrapper) {
 		if (interfaces == null || interfaces.length == 0)
 			throw new IllegalArgumentException("listenerClasses cannot be null or an empty array");
 		
 		// Add the EventDispatcher interface to the end of the list.
 		Class[] listenerClassesExtended = new Class[interfaces.length + 1];
 		System.arraycopy(interfaces, 0, listenerClassesExtended, 0, interfaces.length);
 		listenerClassesExtended[interfaces.length] = EventDispatcher.class;
 		
 		// Create a new factory as the handler and create the proxy.
 		EventDispatcherFactory dispatcher = new EventDispatcherFactory(interfaces, invocationWrapper);
 		dispatcher.proxy = Proxy.newProxyInstance(EventDispatcherFactory.class.getClassLoader(), listenerClassesExtended, dispatcher);
 		return (EventDispatcher)dispatcher.proxy;
 	}
 	
 	private Map<Class, IAPIListener[]> lookup = new HashMap<Class, IAPIListener[]>();
 	private ConcurrentLinkedQueue<QueuedListener> listenerQueue = new ConcurrentLinkedQueue<QueuedListener>();
 	private Class[] listenerClasses;
 	private Object proxy;
 	private IInvocationWrapper invocationWrapper;
 	
 	/**
 	 * Initializes this dispatcher's lookup.
 	 * @param listenerClasses
 	 */
 	private EventDispatcherFactory(Class[] listenerClasses, IInvocationWrapper invocationWrapper) {
 		this.listenerClasses = listenerClasses;
 		this.invocationWrapper = invocationWrapper;
 		init(listenerClasses);
 	}
 	
 	/**
 	 * Creates the lookup entries for the listener classes.
 	 */
 	private void init(Class[] listenerClasses) {
 		for (int i = 0; i < listenerClasses.length; i++)
 			lookup.put(listenerClasses[i], null);
 	}
 	
 	public boolean isEmpty(Class listenerClass) {
 		return getListeners(listenerClass) == null;
 	}
 	
 	// Note: If any other methods access 'lookup' then they should call processQueue() first.
 	private IAPIListener[] getListeners(Class listenerClass) {
 		processQueue();
 		return listenerClass == null ? null : lookup.get(listenerClass);
 	}
 	
 	/**
 	 * @see EventDispatcher#addListener(IAPIListener)
 	 */
 	public void addListener(IAPIListener listener) {
 		isValidArgument(listener);
 		Class listenerClass = listener.getClass();
 		for (int i = 0; i < listenerClasses.length; i++) {
 			if (listenerClasses[i].isAssignableFrom(listenerClass)) {
 				addListenerInternal(listener, listenerClasses[i]);
 			}
 		}
 	}
 
 	/**
 	 * @see EventDispatcher#addListener(IAPIListener, Class)
 	 */
 	public boolean addListener(IAPIListener listener, Class listenerClass) throws IllegalArgumentException {
 		return checkIsSupportedListener(listener, listenerClass) && listenerClass.isAssignableFrom(proxy.getClass())
 				&& addListenerInternal(listener, listenerClass);
 	}
 
 	private boolean addListenerInternal(IAPIListener listener, Class listenerClass) {
 		// Create a set so an IAPIListener is not added twice.
 		Set<IAPIListener> listenerSet = new HashSet<IAPIListener>();
 		
 		// Add existing listeners.
 		IAPIListener[] listenerArray = getListeners(listenerClass);
 		if (listenerArray != null)
 			listenerSet.addAll(Arrays.asList(listenerArray));
 		
 		// Attempt to add the listener.
 		boolean added = listenerSet.add(listener);
 		
 		// Recreate the listener array, if changed.
 		if (added)
 			lookup.put(listenerClass, listenerSet.toArray(new IAPIListener[listenerSet.size()]));
 		
 		// Return if the listener was added.
 		return added;
 	}
 	
 	/**
 	 * @see EventDispatcher#queuedAddListener(IAPIListener)
 	 */
 	public void queuedAddListener(IAPIListener listener) {
 		isValidArgument(listener);
 		listenerQueue.add(new QueuedListener(QueueAction.ADD, listener, null));
 	}
 	
 	/**
 	 * @see EventDispatcher#queuedAddListener(IAPIListener, Class)
 	 */
 	public void queuedAddListener(IAPIListener listener, Class listenerClass) {
 		if (checkIsSupportedListener(listener, listenerClass)) {
 			listenerQueue.add(new QueuedListener(QueueAction.ADD, listener, listenerClass));
 		}
 	}
 
 	/**
 	 * @see EventDispatcher#removeListener(IAPIListener)
 	 */
 	public void removeListener(IAPIListener listener) {
 		isValidArgument(listener);
 		Class listenerClass = listener.getClass();
 		for (int i = 0; i < listenerClasses.length; i++) {
 			if (listenerClasses[i].isAssignableFrom(listenerClass)) {
 				removeListenerInternal(listener, listenerClasses[i]);
 			}
 		}
 	}
 
 	/**
 	 * @see EventDispatcher#removeListener(IAPIListener, Class)
 	 */
 	public boolean removeListener(IAPIListener listener, Class listenerClass) throws IllegalArgumentException {
 		return checkIsSupportedListener(listener, listenerClass) && listenerClass.isAssignableFrom(proxy.getClass())
 				&& removeListenerInternal(listener, listenerClass);
 	}
 
 	private boolean removeListenerInternal(IAPIListener listener, Class listenerClass) {
 		// Create a set of IAPIListener.
 		Set<IAPIListener> listenerSet = new HashSet<IAPIListener>();
 		
 		IAPIListener[] listenerArray = getListeners(listenerClass);
 		
 		// Cannot remove from an empty list.
 		if (listenerArray == null)
 			return false;
 
 		// Add existing listeners.
 		listenerSet.addAll(Arrays.asList(listenerArray));
 		
 		// Attempt to remove the listener.
 		boolean removed = listenerSet.remove(listener);
 		
 		// Recreate the listener array, if changed.
 		if (removed)
 			lookup.put(listenerClass, listenerSet.size() == 0 ? null : listenerSet.toArray(new IAPIListener[listenerSet.size()]));
 		
 		// Return if the listener was removed.
 		return removed;
 	}
 
 	/**
 	 * @see EventDispatcher#queuedRemoveListener(IAPIListener)
 	 */
 	public void queuedRemoveListener(IAPIListener listener) {
 		isValidArgument(listener);
 		listenerQueue.add(new QueuedListener(QueueAction.REMOVE, listener, null));
 	}
 
 	/**
 	 * @see EventDispatcher#queuedRemoveListener(IAPIListener, Class)
 	 */
 	public void queuedRemoveListener(IAPIListener listener, Class listenerClass) {
 		if (checkIsSupportedListener(listener, listenerClass)) {
 			listenerQueue.add(new QueuedListener(QueueAction.REMOVE, listener, listenerClass));
 		}
 	}
 	
 	@SuppressWarnings("static-method")
 	private boolean isValidArgument(IAPIListener listener) throws IllegalArgumentException {
 		if (listener == null)
 			throw new IllegalArgumentException("listener cannot be null");
 		
 		return true;
 	}
 	
 	@SuppressWarnings("static-method")
 	private boolean areValidArguments(IAPIListener listener, Class listenerClass) throws IllegalArgumentException {
 		if (listener == null)
 			throw new IllegalArgumentException("listener cannot be null");
 		
 		if (listenerClass == null)
 			throw new IllegalArgumentException("listenerClass cannot be null");
 		
 		if (!IAPIListener.class.isAssignableFrom(listenerClass))
 			throw new IllegalArgumentException("listenerClass is not an instance of IAPIListener");
 
 		if (!listenerClass.isAssignableFrom(listener.getClass()))
 			throw new IllegalArgumentException("listener is not an instance of listenerClass");
 		
 		return true;
 	}
 	
 	private boolean checkIsSupportedListener(IAPIListener listener, Class listenerClass) throws IllegalArgumentException {
 		return areValidArguments(listener, listenerClass) && listenerClass.isAssignableFrom(proxy.getClass());
 	}
 	
 	private void processQueue() {
 		QueuedListener entry;
 		while ((entry = listenerQueue.poll()) != null) {
 			if (entry.action == QueueAction.ADD) {
 				if (entry.listenerClass == null)
 					addListener(entry.listener);
 				else
 					addListener(entry.listener, entry.listenerClass);
 			}
 			else if (entry.action == QueueAction.REMOVE)
 				if (entry.listenerClass == null)
 					removeListener(entry.listener);
 				else
 					removeListener(entry.listener, entry.listenerClass);
 		}
 	}
 	
 	public boolean isSupportedListener(IAPIListener listener) {
 		isValidArgument(listener);
 		Class listenerClass = listener.getClass();
 		for (int i = 0; i < listenerClasses.length; i++) {
 			if (listenerClasses[i].isAssignableFrom(listenerClass)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		invocationCount++;
		
 		Class declaringClass = method.getDeclaringClass();
 		
 		if (declaringClass == EventDispatcher.class) {
 			return method.invoke(this, args);
 		}
 		else {
 			// Process any queued failures before invoking a method on a mod.
 			ModLoader.processFailureQueue();
 			
 			// Get the listeners for method's declaring class.
 			IAPIListener[] listeners = getListeners(method.getDeclaringClass());
 			
 			// Execute all the listeners.
 			if (listeners != null) {
 				for (int i = 0; i < listeners.length; i++) {
 					
 					try {
 						if (invocationWrapper != null)
 							invocationWrapper.handleInvocation(new Invocation(listeners[i], method, args));
 						else
 							method.invoke(listeners[i], args);
 						
 						// Stop processing events if there is an event argument that has flagged for the event handling to stop.
 						if (args.length > 0 && args[0] instanceof IEventInterrupter && ((IEventInterrupter)args[0]).isInterrupted())
 							break;
 					}
 					catch (InvocationTargetException e) {
 						//listeners.remove();
 						removeListener(listeners[i]);
 						handleListenerFailure(e.getCause(), listeners[i]);
 					} catch (Throwable e) {
 						//listeners.remove();
 						removeListener(listeners[i]);
 						handleListenerFailure(e.getCause(), listeners[i]);
 					}
 				}
 			}
 			
 			// Listener methods return void.
 			return null;
 		}
 	}
 	
 	@SuppressWarnings("static-method")
 	protected void handleListenerFailure(Throwable t, IAPIListener listener) {
 		ModLoader.reportListenerFailure(t, listener);
 	}
 }
