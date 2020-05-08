 package com.utopia.lijiang.alarm;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.util.Log;
 
 import com.utopia.lijiang.global.Status;
 
 public class AlarmManager {
 	
 	private static AlarmManager instance = null;
 	public static AlarmManager getInstance(){
 		if(instance == null){
 			instance = new AlarmManager();
 		}
 		return instance;
 	}
 	
 	private List<Alarm> alarms = null;
 	private List<AlarmListener> alListeners = null;
 	
 	public AlarmManager(){
 		alarms = new ArrayList<Alarm>();
 		alListeners = new ArrayList<AlarmListener>();
 	}
 	
 	public void addAlarm(Alarm alarm){
 		alarms.add(alarm);
 	}
 	
 	public Alarm removeAlarm(int location){
 		 return alarms.remove(location);
 	}
 	
 	public boolean removeAlarm(Object object){
 		return alarms.remove(object);
 	}
 	
 	public int addAlarmListener(AlarmListener al){
 		boolean isAdded = alListeners.add(al);
 		
 		if(isAdded){
 			int lastIndex = alListeners.size() -1; 
 			return lastIndex; 
 		}else{
 			return -1;
 		}
 	}
 	
 	public AlarmListener removeAlarmListener(int location){
 		return alListeners.remove(location);
 	}
 	
 	public boolean removeAlarmListener(Object object){
 		return alListeners.remove(object);
 	}
 	
 	public int alarmAllPossible(){
 		int count = 0;
 		Iterator<Alarm> it = alarms.iterator();
 		while(it.hasNext()){
 			Alarm alarm = it.next();
 			if(alarm.shouldAlarm(Status.getCurrentStatus())){
 				try{
 					alarm(alarm);
 					count++;
 				}catch(Exception ex){
					Log.d("lijiang","Create LocationService");
 				}
 			}
 		}
 		return count;
 	}
 	
 	public void alarm(Alarm alarm){
 		Iterator<AlarmListener> it = alListeners.iterator();
 		while(it.hasNext()){
 			AlarmListener al = it.next();
 			al.onAlarm(alarm);
 		}
 	}
 	
 }
