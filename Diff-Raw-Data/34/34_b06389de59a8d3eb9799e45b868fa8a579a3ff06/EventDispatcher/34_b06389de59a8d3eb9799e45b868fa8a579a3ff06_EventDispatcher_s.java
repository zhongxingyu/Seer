 package cbccore.events;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 public class EventDispatcher {
 	private HashMap<EventEmitter, HashMap<Class<Event>, ArrayList<IEventListener>>> events = new HashMap<EventEmitter, HashMap<Class<Event>, ArrayList<IEventListener>>>();
 	private static EventDispatcher instance = null;
 	
 	public static EventDispatcher getInstance() {
 		if(instance == null) instance = new EventDispatcher();
 		return instance;
 	}
 	
 	public void addEventListener(EventEmitter emitter, Event type, IEventListener listener) {
 		HashMap<Class<Event>, ArrayList<IEventListener>> emit = getEmitter(emitter);
 		ArrayList<IEventListener> listeners = getListeners(emit, type);
 		
 		listeners.add(listener);
 	}
 	
 	/**
 	 * Removes an event listener from all types in an emitter
 	 */
 	public void removeEventListener(EventEmitter emitter, IEventListener listener) {
 		HashMap<Class<Event>, ArrayList<IEventListener>> types = events.get(emitter);
 		Iterator<ArrayList<IEventListener>> it = types.values().iterator();
 		while(it.hasNext()) {
 			ArrayList<IEventListener> listeners = it.next();
 			for(int i = 0; i < listeners.size(); ++i) {
 				IEventListener working = listeners.get(i);
 				if(working.equals(listener)) {
 					listeners.remove(i);
 				}
 			}
 		}
 	}
 	
 	public void emit(EventEmitter emitter, Event type) {
 		HashMap<Class<Event>, ArrayList<IEventListener>> emit = getEmitter(emitter);
 		ArrayList<IEventListener> listeners = getListeners(emit, type);
 		for(IEventListener i : listeners) {
 			i.eventDispatched(emitter, type);
 		}
 	}
 	
 	private HashMap<Class<Event>, ArrayList<IEventListener>> getEmitter(EventEmitter e) {
 		HashMap<Class<Event>, ArrayList<IEventListener>> emit = events.get(e);
 		if(emit == null) {
 			emit = new HashMap<Class<Event>, ArrayList<IEventListener>>();
 			events.put(e, emit);
 		}
 		return emit;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private ArrayList<IEventListener> getListeners(HashMap<Class<Event>, ArrayList<IEventListener>> e, Event type) {
 		ArrayList<IEventListener> listeners = e.get(type.getClass());
 		if(listeners == null) {
 			listeners = new ArrayList<IEventListener>();
 			e.put((Class<Event>) type.getClass(), listeners);
 		}
 		return listeners;
 	}
}
