 package linkjvm.events;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
public class EventManager extends Thread{
 	private ArrayList<Event> eventList;
 	private boolean stop;	
 	
 	public EventManager(){
 		eventList = new ArrayList<Event>();
 		stop = false;
 	}
 
 	public void run(){
 		while(!stop){
 			Iterator<Event> it = eventList.iterator();
 			while(it.hasNext()){
 				Event nextEvent = it.next();
 				if(nextEvent.proof()){
 					nextEvent.trigger();
 				}
 			}
 		}
 	}
 	
	@Override
 	public void stop(){
 		stop = true;
 	}
 
 	public boolean registerEvent(Event e){
 		return eventList.add(e);
 	}
 
 	public boolean unregisterEvent(Event e){
 		return eventList.remove(e);
 	}
 }
