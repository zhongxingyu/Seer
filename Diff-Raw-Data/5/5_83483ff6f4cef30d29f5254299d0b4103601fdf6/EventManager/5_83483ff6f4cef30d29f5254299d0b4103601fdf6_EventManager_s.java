 package com.theminequest.MineQuest.EventsAPI;
 
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 
 import com.theminequest.MineQuest.Quest.Quest;
 import com.theminequest.MineQuest.Tasks.Task;
 
 /**
  * Because we don't know what classes will be available
  * on runtime, we need to keep track of all classes that
  * extend QEvent and record them here.
  * @author xu_robert <xu_robert@linux.com>
  *
  */
 public class EventManager {
 
 	private HashMap<String,Class<? extends QEvent>> classes;
 
 	public EventManager(){
 		classes = new HashMap<String,Class<? extends QEvent>>();
 	}
 	
 	/**
 	 * Register an event with MineQuest. It needs to have a name, such
 	 * as QuestFinishEvent, that the quest file can use.
 	 * <br>
 	 * <b>WARNING: QEvents and classes based off of it must NOT tamper
 	 * the constructor. Instead, use {@link QEvent#parseDetails(String)} to
 	 * set instance variables and conditions.
 	 * @param eventname Event name
 	 * @param event Class of the event (.class)
 	 */
 	public void registerEvent(String eventname, Class <? extends QEvent> event){
 		if (classes.containsKey(eventname) || classes.containsValue(event))
 			throw new IllegalArgumentException("We already have this class!");
 		try {
			event.getConstructor(com.theminequest.MineQuest.Quest.Quest.class,com.theminequest.MineQuest.Tasks.Task.class,java.lang.String.class);
 		}catch (Exception e){
 			throw new IllegalArgumentException("Constructor tampered with!");
 		}
 		classes.put(eventname, event);
 	}
 	
 	/**
 	 * Retrieve a new instance of an event for use with a quest and task.
 	 * @param eventname Event to use
 	 * @param q Quest to attribute to
 	 * @param t Task to attribute to
 	 * @param d Details for use with {@link QEvent#parseDetails(String)}
 	 * @return new instance of the event requested
 	 */
 	public QEvent getNewEvent(String eventname, Quest q, Task t, String d){
 		if (!classes.containsKey(eventname))
 			return null;
 		Class<? extends QEvent> cl = classes.get(eventname);
 		Constructor<? extends QEvent> ctor = null;
 		try {
			ctor = cl.getConstructor(com.theminequest.MineQuest.Quest.Quest.class,com.theminequest.MineQuest.Tasks.Task.class,java.lang.String.class);
 		} catch (NoSuchMethodException e) {
 			// we have no idea how to handle this method.
 			return null;
 		}
 		try {
 			return (QEvent)ctor.newInstance(q,t,d);
 		} catch (Exception e){
 			return null;
 		}
 	}
 	
 }
