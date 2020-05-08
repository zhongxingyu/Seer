 package com.universogtp.fillthejar;
 
 import java.io.Serializable;
 import java.util.Calendar;
 
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.media.SoundPool.OnLoadCompleteListener;
 import android.widget.Toast;
 
public class Jar  implements Serializable, OnLoadCompleteListener {
 	private static final long serialVersionUID = 1L;
 	private long iD;
 	private String name;
 	private int created;
 	private int value;
 	private int frequency;
 	private int weekends;
 	private int fillsPerCycle;
 	private int fillsThisCycle;
 	private int lastFill;
 	private int currentCycleStart;
 	private int streak;
 
 	public Jar(long iD, String name) {
 		this.iD = iD;
 		this.name = name;
 		this.value=0;
 		this.frequency = 1;
 		this.weekends=0;
 		this.fillsPerCycle=1;
 		this.fillsThisCycle=0;
 		this.lastFill=0;
 		this.streak = 0;
 	}
 	
 	public long getID() {
 		return iD;
 	}
 	
 	public void setID(long iD){
 		this.iD=iD;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public int getCreated() {
 		return created;
 	}
 
 	public void setCreated(int created) {
 		this.created = created;
 	}	
 	
 	public void setName(String name){
 		this.name=name;
 	}	
 	
 	public int getValue(){
 		return value;
 	}
 	
 	public void setValue(int value){
 		this.value=value;
 	}
 	
 	public int getFrequency() {
 		return frequency;
 	}
 
 	public void setFrequency(int frequency) {
 		this.frequency = frequency;
 	}
 	
 	public int isWeekends() {
 		return weekends;
 	}
 	
 	public int getWeekends(){
 		return weekends;
 	}
 	
 	public void setWeekends(int weekends) {
 		this.weekends = weekends;
 	}
 	
 	public int getFillsPerCycle() {
 		return fillsPerCycle;
 	}
 
 	public void setFillsPerCycle(int fillsPerCycle) {
 		this.fillsPerCycle = fillsPerCycle;
 	}
 	
 	public int getFillsThisCycle() {
 		return fillsThisCycle;
 	}
 
 	public void setFillsThisCycle(int fillsThisCycle) {
 		this.fillsThisCycle = fillsThisCycle;
 	}
 	
 	public int getLastFill() {
 		return lastFill;
 	}
 
 	public void setLastFill(int lastFill) {
 		this.lastFill = lastFill;
 	}
 
 	public int getCurrentCycleStart() {
 		return currentCycleStart;
 	}
 
 	public void setCurrentCycleStart(int currentCycleStart) {
 		this.currentCycleStart = currentCycleStart;
 	}	
 	
 	public int getStreak() {
 		return streak;
 	}
 
 	public void setStreak(int streak) {
 		this.streak = streak;
 	}
 
	private void playSound(Context context) {
		SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
 		soundPool.load(context, R.raw.coin_to_empty, 1);
 		
 		soundPool.setOnLoadCompleteListener(this);
 	}
 	
 	@Override
 	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
 		soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
	}	
	
 	public void fill(Context context) {
 		if (getFrequency() == 0) {
 			setValue(getValue()+1);
			playSound(context);
 			return;
 		}
 		if ((getFillsThisCycle() < getFillsPerCycle()) || getFillsPerCycle() == 0) {
 			setValue(getValue()+1);
 			setLastFill((int)(System.currentTimeMillis()/1000));
 			setFillsThisCycle(getFillsThisCycle()+1);
 			setStreak(getStreak()+1);
			playSound(context);
 		} else {
 			Toast.makeText(context, context.getString(R.string.it_can_not_be_filled_today), Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	public boolean refresh() {
 		boolean updated = false;
 		
 		if (getFrequency() == 0) return false;
 		
 		long currentCycleStartMillis = (long) getCurrentCycleStart() * 1000;
 		Calendar currentCycleStartCalendar = Calendar.getInstance();
 		currentCycleStartCalendar.setTimeInMillis(currentCycleStartMillis);
 		currentCycleStartCalendar.set(Calendar.HOUR_OF_DAY, 0);
 		currentCycleStartCalendar.set(Calendar.MINUTE, 0);
 		currentCycleStartCalendar.set(Calendar.SECOND, 0);
 		currentCycleStartCalendar.set(Calendar.MILLISECOND, 0);
 		
 		long todayMillis = System.currentTimeMillis();
 		Calendar todayCalendar = Calendar.getInstance();
 		todayCalendar.setTimeInMillis(todayMillis);
 		todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
 		todayCalendar.set(Calendar.MINUTE, 0);
 		todayCalendar.set(Calendar.SECOND, 0);
 		todayCalendar.set(Calendar.MILLISECOND, 0);		
 		
 		int daysSinceCurrentCycleStart =  (int) ((todayCalendar.getTimeInMillis() - currentCycleStartCalendar.getTimeInMillis()) / (1000 * 60 * 60 * 24));		
 		
 		if (daysSinceCurrentCycleStart >= getFrequency()) {
 			int ellapsesCycles = daysSinceCurrentCycleStart / getFrequency();
 			if (getFillsThisCycle() == 0 &&
 					(getFrequency() != 1 || 
 						(isWeekends() == 0 &&  
 							(todayCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||  
 							 todayCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY )))){
 				int newValue = getValue()-ellapsesCycles;
 				if (newValue < 0) newValue = 0;
 				setValue(newValue);
 				setStreak(0);
 			}
 			setFillsThisCycle(0);
 			
 			setCurrentCycleStart(getCurrentCycleStart()+
 					(getFrequency()*ellapsesCycles*(60*24*24)));
 			updated = true;
 		}
 		
 		return updated;
 	}
 }
