 /**
  * This file, EventManager.java, is part of MineQuest:
  * A full featured and customizable quest/mission system.
  * Copyright (C) 2012 The MineQuest Team
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  **/
 package com.theminequest.MineQuest.EventsAPI;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 import com.theminequest.MineQuest.MineQuest;
 
 /**
  * Because we don't know what classes will be available on runtime, we need to
  * keep track of all classes that extend QEvent and record them here.
  * 
  * @author xu_robert <xu_robert@linux.com>
  * 
  */
 public class EventManager implements Listener {
 
 	private LinkedHashMap<String, Class<? extends QEvent>> classes;
	private List<QEvent> activeevents;
 	private Object activelock;
 	private Runnable activechecker;
 	private Object classlistlock;
 	private volatile boolean stop;
 
 	public EventManager() {
 		MineQuest.log("[Event] Starting Manager...");
 		classes = new LinkedHashMap<String, Class<? extends QEvent>>();
		activeevents = new ArrayList<QEvent>();
 		activelock = new Object();
 		classlistlock = new Object();
 		stop = false;
 		activechecker = new Runnable(){
 
 			@Override
 			public void run() {
 				while(!stop){
 					checkAllEvents();
 				}
 			}
 
 		};
 		new Thread(activechecker).start();
 	}
 
 	public void dismantleRunnable(){
 		stop = true;
 	}
 
 	/**
 	 * Register an event with MineQuest. It needs to have a name, such as
 	 * QuestFinishEvent, that the quest file can use. <br>
 	 * <b>WARNING: QEvents and classes based off of it must NOT tamper the
 	 * constructor. Instead, use {@link QEvent#parseDetails(String)} to set
 	 * instance variables and conditions.</b> This method explicitly
 	 * requests the original constructor and if the event does not have
 	 * this constructor, classes will fail to be hooked in entirely.
 	 * 
 	 * @param eventname
 	 *            Event name
 	 * @param event
 	 *            Class of the event (.class)
 	 */
 	public void registerEvent(String eventname, Class<? extends QEvent> event) {
 		synchronized(classlistlock){
 			if (classes.containsKey(eventname) || classes.containsValue(event))
 				throw new IllegalArgumentException("We already have this class!");
 			try {
 				event.getConstructor(long.class, int.class, java.lang.String.class);
 			} catch (Exception e) {
 				throw new IllegalArgumentException("Constructor tampered with!");
 			}
 			classes.put(eventname, event);
 		}
 	}
 
 	/**
 	 * Retrieve a new instance of an event for use with a quest and task.
 	 * 
 	 * @param eventname
 	 *            Event to use
 	 * @param q
 	 *            Quest ID to attribute
 	 * @param eventnum
 	 *            Event Number for Quest
 	 * @param d
 	 *            Details for use with {@link QEvent#parseDetails(String)}
 	 * @return new instance of the event requested
 	 */
 	public QEvent getNewEvent(String eventname, long q, int eventnum, String d) {
 		synchronized(classlistlock){
 			System.out.println("28 REPEAT");
 			if (!classes.containsKey(eventname))
 				return null;
 			System.out.println("29 REPEAT");
 			Class<? extends QEvent> cl = classes.get(eventname);
 			System.out.println("30 REPEAT");
 			Constructor<? extends QEvent> ctor = null;
 			System.out.println("31 REPEAT");
 			try {
 				ctor = cl.getConstructor(long.class, int.class,
 						java.lang.String.class);
 			} catch (NoSuchMethodException e) {
 				// we have no idea how to handle this method.
 				return null;
 			}
 			System.out.println("32 REPEAT");
 			try {
 				return (QEvent) ctor.newInstance(q, eventnum, d);
 			} catch (Exception e) {
 				return null;
 			}
 		}
 	}
 
 	/**
 	 * Check if the event implements an interface.
 	 * 
 	 * @param eventname
 	 *            Event to check
 	 * @param interfaze
 	 *            Interface to check
 	 * @return true if this event does implement the specified interface
 	 */
 	public boolean hasInterface(String eventname, String interfaze) {
 		synchronized(classlistlock){
 			if (!classes.containsKey(eventname))
 				return false;
 			Class<? extends QEvent> cl = classes.get(eventname);
 			Class<?>[] interfazes = cl.getInterfaces();
 			for (Class<?> c : interfazes) {
 				if (c.getSimpleName().equals(interfaze))
 					return true;
 			}
 			return false;
 		}
 	}
 
	public void addEventListener(QEvent e){
 		System.out.println("36 REPEAT");
 		synchronized(activelock){
			activeevents.add(e);
 		}
 	}
 
 	public void rmEventListener(QEvent e){
 		synchronized(activelock){
 			activeevents.remove(e);
 		}
 	}
 
 	public void checkAllEvents(){
 		synchronized(activelock){
			for (final QEvent e : activeevents){
				new Thread(new Runnable(){
					@Override
					public void run() {
						e.check();
					}
				}).start();
				
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBlockBreak(final BlockBreakEvent e){
 		synchronized(activelock){
			for (final QEvent a : activeevents){
 				new Thread(new Runnable(){
 					@Override
 					public void run() {
 						a.onBlockBreak(e);
 					}
 				}).start();
 			}
 		}
 	}
 
 	@EventHandler
 	public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent e){
 		synchronized(activelock){
			for (final QEvent a : activeevents){
 				new Thread(new Runnable(){
 					@Override
 					public void run() {
 						a.onEntityDamageByEntityEvent(e);
 					}
 				}).start();
 			}
 		}
 	}
 
 }
