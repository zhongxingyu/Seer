 package net.skycraftmc.SkyQuest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import net.skycraftmc.SkyQuest.objective.ObjectiveType;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class QuestData
 {
 	private Quest q;
 	String stage;
 	ArrayList<String> unassigned = new ArrayList<String>();
 	HashMap<String, String> objprog = new HashMap<String, String>();
 	private String player;
 	private PlayerQuestLog log;
 	private boolean settingstage = false;
 	private String lateststage;
 	private CompletionState state = CompletionState.IN_PROGRESS;
 	protected QuestData(PlayerQuestLog log, Quest quest)
 	{
 		q = quest;
 		this.log = log;
 		player = log.getPlayer();
 		for(Objective o:q.getObjectives())unassigned.add(o.getID());
 	}
 	protected static QuestData createComplete(PlayerQuestLog log, Quest quest)
 	{
 		QuestData qd = new QuestData(log, quest);
 		qd.unassigned.clear();
 		return qd;
 	}
 	public void assign(String oid)
 	{
 		if(unassigned.contains(oid))
 		{
 			unassigned.remove(oid);
 			Objective o = q.getObjective(oid);
 			ObjectiveType type = o.getType();
 			objprog.put(oid, type.createProgress(type.getData(o.getTarget())));
 			if(SkyQuest.isOnServer() && o.isVisible())
 			{
 				Player p = Bukkit.getServer().getPlayerExact(player);
				if(p != null)p.sendMessage(o.getName() + (o.isOptional() ? " (Optional)" : ""));
 			}
 		}
 	}
 	public String getProgress(String oid)
 	{
 		Objective o = q.getObjective(oid);
 		if(o == null)
 			throw new IllegalArgumentException("Quest \"" + q.getName() + "\" has no such objective: " + oid);
 		return objprog.get(oid);
 	}
 	public void setProgress(String oid, String progress)
 	{
 		Objective o = q.getObjective(oid);
 		if(o == null)
 			throw new IllegalArgumentException("Quest \"" + q.getName() + "\" has no such objective: " + oid);
 		if(!o.getType().isValid(progress))
 			throw new IllegalArgumentException("progress is not valid for type " + o.getType().getName());
 		if(o.getType().isComplete(o.getTarget(), progress))
 		{
 			objprog.remove(oid);
 			if(SkyQuest.isOnServer())
 			{
 				Player p = Bukkit.getServer().getPlayerExact(player);
 				if(p != null && o.isVisible())p.sendMessage(ChatColor.GREEN + "Objective completed: " + o.getName());
 				for(QuestAction r:o.getRewards())
 				{
					if(!r.getType().requiresPlayer())r.apply(player);
 				}
 			}
 		}
 		else objprog.put(oid, progress);
 	}
 	public Quest getQuest()
 	{
 		return q;
 	}
 	public boolean isComplete(String oid)
 	{
 		Objective o = q.getObjective(oid);
 		if(o == null)
 			throw new IllegalArgumentException("Quest \"" + q.getName() + "\" has no such objective: " + oid);
 		return !objprog.containsKey(o.getID()) && !unassigned.contains(oid);
 	}
 	public boolean isComplete()
 	{
 		return state != CompletionState.IN_PROGRESS;
 	}
 	public void markComplete(CompletionState state)
 	{
 		if(state != CompletionState.IN_PROGRESS)
 		{
 			this.state = state;
 			checkCompletion();
 		}
 	}
 	private void checkCompletion()
 	{
 		if(state == CompletionState.IN_PROGRESS)return;
 		if(isComplete())
 		{
 			log.complete(q, state);
 			if(SkyQuest.isOnServer())
 			{
 				Player p = Bukkit.getServer().getPlayerExact(player);
 				if(p != null && q.isVisible())
 				{
 					if(state == CompletionState.COMPLETE)
 						p.sendMessage(ChatColor.GREEN + "Quest completed: " + q.getName());
 					else if(state == CompletionState.FAILED)
 						p.sendMessage(ChatColor.RED + "Quest failed: " + q.getName());
 				}
 			}
 		}
 	}
 	public boolean isAssigned(String oid)
 	{
 		return objprog.containsKey(oid);
 	}
 	public void setStage(String id)
 	{
 		Stage s = q.getStage(id);
 		if(s == null)
 			throw new IllegalArgumentException("No such stage: " + id);
 		if(settingstage)
 		{
 			lateststage = id;
 			return;
 		}
 		settingstage = true;
 		stage = s.getID();
 		for(QuestAction a:s.getActions())
 		{
 			a.apply(player);
 		}
 		settingstage = false;
 		if(lateststage != null)
 		{
 			final String lstage = lateststage;
 			lateststage = null;
 			setStage(lstage);
 		}
 	}
 	public String getStage()
 	{
 		return stage;
 	}
 	/**
 	 * Returns all unassigned objectives of the quest.
 	 * @return The unassigned {@link Objective}s of the quest.
 	 */
 	public Objective[] getUnassignedObjectives()
 	{
 		ArrayList<Objective>obs = new ArrayList<Objective>();
 		for(Objective o:q.getObjectives())
 		{
 			if(!objprog.containsKey(o.getID()))obs.add(o);
 		}
 		return obs.toArray(new Objective[obs.size()]);
 	}
 	/**
 	 * Returns all assigned objectives of the quest.  This should be used in
 	 * events that handle quest progress instead of looping through the objectives
 	 * of a quest.
 	 * @return The assigned {@link Objective}s of the quest.
 	 */
 	public Objective[] getAssignedObjectives()
 	{
 		ArrayList<Objective>obs = new ArrayList<Objective>();
 		for(Objective o:q.getObjectives())
 		{
 			if(objprog.containsKey(o.getID()))obs.add(o);
 		}
 		return obs.toArray(new Objective[obs.size()]);
 	}
 }
