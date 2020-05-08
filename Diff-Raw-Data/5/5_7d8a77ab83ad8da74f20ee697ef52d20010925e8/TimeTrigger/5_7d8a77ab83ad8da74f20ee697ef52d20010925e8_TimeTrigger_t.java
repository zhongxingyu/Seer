 package com.wolvencraft.prison.mines.triggers;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.configuration.serialization.SerializableAs;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.AutomaticResetRoutine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.ResetTrigger;
 import com.wolvencraft.prison.mines.util.Util;
 
 @SerializableAs("TimeTrigger")
 public class TimeTrigger implements BaseTrigger {
 	
 	private long period;
 	private long next;
 	private String mine;
 	
 	private boolean canceled;
 	
 	/**
 	 * Default constructor for the TimeTrigger
 	 * @param mine Mine object associated with the trigger
 	 * @param period Reset period, in seconds
 	 */
 	public TimeTrigger(Mine mineObj, int period) {
 		this.mine = mineObj.getId();
 		this.period = this.next = period * 20L;
 		
 		canceled = false;
 		
 		PrisonSuite.addTask(this);
 	}
 	
 	/**
 	 * Deserializing constructor for TimeTrigger
 	 * @param map Map of trigger data
 	 */
 	public TimeTrigger(Map<String, Object> map) {
 		mine = (String) map.get("mine");
 		period = Long.parseLong((String)map.get("period"));
 		next = Long.parseLong((String)map.get("next"));
 		
 		canceled = false;
 		
 		PrisonSuite.addTask(this);
 	}
 	
 	/**
 	 * Serialization method for the trigger
 	 */
 	public Map<String, Object> serialize() {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("mine", mine);
 		map.put("period", Long.toString(period));
 		map.put("next", Long.toString(next));
 		return map;
 	}
 	
 	/**
 	 * Main trigger method. Run every TICKRATE, defined in the config
 	 */
 	public void run() {
 		Mine mineObj = Mine.get(mine);
 		if(mineObj == null) {
 			Message.log(Level.SEVERE, "mineObj " + mineObj + " was not found, but its TimeTrigger still exists");
 			return;
 		}
 		
 		if(mineObj.getCooldown() && mineObj.getCooldownEndsIn() > 0) {
 			mineObj.updateCooldown(PrisonMine.getSettings().TICKRATE);
 		}
 		
 		if(mineObj.hasParent()) return;
 
 		next -= PrisonMine.getSettings().TICKRATE;
 		
 		if(next <= 0L) {
 			Message.debug("+---------------------------------------------");
			Message.debug("| mine " + mine + " is resetting. Reset report:");
 			Message.debug("| Reset cause: timer has expired (" + next +" / " + period + ")");
 			AutomaticResetRoutine.run(mineObj);
 			Message.debug("| Updated the timer (" + next +" / " + period + ")");
			Message.debug("| Reached the end of the report for " + mine);
 			Message.debug("+---------------------------------------------");
 		}
 		
 		List<Integer> warnTimes = mineObj.getWarningTimes();
 		if(!mineObj.getSilent() && mineObj.getWarned() && warnTimes.indexOf((int)(next / 20)) != -1)
 			Message.broadcast(Util.parseVars(PrisonMine.getLanguage().RESET_WARNING, mineObj));
 	}
 	
 	/**
 	 * Tags the task to expire during the next run
 	 */
 	public void cancel() { canceled = true; }
 	
 	public String getName() 	{ return "PrisonMine:TimeTrigger:" + mine; }
 	public ResetTrigger getId() { return ResetTrigger.TIME; }
 	public boolean getExpired() { return canceled; }
 	
 	public int getPeriod() 		{ return (int)(period / 20L); }
 	public int getNext() 		{ return (int)(next / 20L); }
 	public void resetTimer()	{ next = period; }
 	
 	public void setPeriod(int period) {
 		this.period = period * 20L;
 		if(this.next > period) this.next = period * 20L;
 	}
 }
