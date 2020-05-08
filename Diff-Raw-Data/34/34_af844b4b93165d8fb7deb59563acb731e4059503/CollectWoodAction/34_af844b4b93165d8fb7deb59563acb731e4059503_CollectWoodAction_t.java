 package edu.cwru.sepia.agent.action;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.cwru.sepia.action.Action;
 import edu.cwru.sepia.action.ActionType;
 import edu.cwru.sepia.action.DirectedAction;
 import edu.cwru.sepia.action.TargetedAction;
 import edu.cwru.sepia.agent.Condition;
 import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
 import edu.cwru.sepia.environment.model.state.State.StateView;
 import edu.cwru.sepia.environment.model.state.Unit.UnitView;
 import edu.cwru.sepia.util.Direction;
 
 public class CollectWoodAction implements BaseAction, CollectAction {
 
 	private int startTime;
 	private int endTime;
 	
 	private final static Condition pre = new Condition(0,0,1,0,0);
 	private final static Condition post = new Condition(0,100,1,0,0);
 	
 	// Keeps track of duration per mine
 	private static Map<Integer, Integer> durations = new HashMap<Integer, Integer>();
 	
 	private static Integer shortestDuration = null;
 	private static Integer shortestID = null;
 	
 	private static Map<Integer, Integer> tempGather = new HashMap<Integer, Integer>();
 	
 	private int unitid;
 
 	@Override
 	public Condition getPreConditions() {
 		return pre;
 	}
 
 	@Override
 	public Condition getPostConditions() {
 		return post;
 	}
 
 	@Override
 	public int getDuration() {
 		return shortestDuration;
 	}
 	
 	@Override
 	public Action getAction(int playernum, StateView state) {
 		int shortID = shortestID;
 		while(true)
 		{
 			int peasants = numUnitsCollecting(playernum, state, shortID);
 			if (peasants < 5)
 			{
 				break;
 			}
 			else
 			{
 				int ID = getNextShortest(durations.get(shortID));
 				if (ID == -1)
 				{
					return Action.createCompoundMove(unitid, (int)Math.random()*state.getXExtent(), (int)Math.random()*state.getYExtent());
 				}
 				shortID = ID;
 			}
 		}
 		
 		Integer num = tempGather.get(shortID);
 		int count = 1;
 		if (num != null)
 		{
 			count = num+1;
 		}
 		tempGather.put(shortID, count);
 		return Action.createCompoundGather(unitid, shortID);
 	}
 	
 	public static void clearTempGather()
 	{
 		tempGather.clear();
 	}
 	
 	private int numUnitsCollecting(int playerNum, StateView state, int resourceID)
 	{
 		int peasants = 0;
 		List<UnitView> units = state.getUnits(playerNum);
 		for (UnitView unit: units)
 		{
 			if (unit.getTemplateView().getName().equalsIgnoreCase("peasant"))
 			{
 				Action act = unit.getCurrentDurativeAction();
 				if (act != null)
 				{
 					if (act.getType() == ActionType.COMPOUNDGATHER)
 					{
 						TargetedAction targetAct = (TargetedAction) act;
 						if (targetAct.getTargetId() == resourceID)
 						{
 							peasants++;
 						}
 					}
 					else if (act.getType() == ActionType.PRIMITIVEGATHER)
 					{
 						DirectedAction dAct = (DirectedAction) act;
 						if (resourceMatchAt(unit, dAct.getDirection(), resourceID, state))
 						{
 							peasants++;
 						}
 					}
 				}
 			}
 		}
 		
 		Integer num = tempGather.get(resourceID);
 		int count = 0;
 		if (num != null)
 		{
 			count = num;
 		}
 		return peasants + count;
 	}
 	
 	private boolean resourceMatchAt(UnitView unit, Direction dir, int resourceID, StateView state)
 	{
 		int targetX = unit.getXPosition() + dir.xComponent();
 		int targetY = unit.getYPosition() + dir.yComponent();
 		
 		ResourceView res = state.getResourceNode(resourceID);
 		if (res.getXPosition() == targetX && res.getYPosition() == targetY)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	private int getNextShortest(int time)
 	{
 		int shortest = 999999;
 		int shortestID = -1;
 		Set<Integer> keys = durations.keySet();
 		for (Integer key:keys)
 		{
 			if (durations.get(key) < shortest && durations.get(key) > time)
 			{
 				shortest = durations.get(key);
 				shortestID = key;
 			}
 		}
 		
 		return shortestID;
 	}
 
 	@Override
 	public void updateDuration(int duration) throws Exception{
 		// if the duration is valid
 		if(duration > 0)
 		{
 			// update the action's resource ID with a new duration
 			durations.put(shortestID, duration);
 		}
 		else
 			throw new Exception("Duration out of bounds!!");
 	}
 
 	@Override
 	public String getUnitType() {
 		return "Peasant";
 	}
 	
 	@Override
 	public int getStartTime()
 	{
 		return startTime;
 	}
 	
 	@Override
 	public int getEndTime()
 	{
 		return endTime;
 	}
 	
 	@Override
 	public void setStartTime(int time)
 	{
 		startTime = time;
 	}
 	
 	@Override
 	public void setEndTime(int time)
 	{
 		endTime = time;
 	}
 
 	@Override
 	public void addResource(int resourceId, int distance) {
 		durations.put(resourceId, 2*distance*16 + 1000);
 		if(shortestDuration == null || 2*distance*16 + 1000 < shortestDuration)
 		{
 			shortestDuration = 2*distance*16 + 1000;
 			shortestID = resourceId;
 		}
 	}
 
 	@Override
 	public void deleteResource(int resourceId) {
 		durations.remove(resourceId);
 		// if the one to be removed is the shortest then a new shortest must be found
 		if(resourceId == shortestID)
 		{
 			shortestDuration = Integer.MAX_VALUE;
 			for(Integer key : durations.keySet())
 			{
 				if(durations.get(key) < shortestDuration)
 				{
 					shortestDuration = durations.get(key);
 					shortestID = key;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void setUnitId(int unitid) {
 		this.unitid = unitid;
 	}
 
 	@Override
 	public int getUnitId() {
 		return unitid;
 	}
 	
 	@Override
 	public String toString(){
 		return "Collect Wood with " + unitid;
 	}
 	
 	@Override
 	public int compareTo(BaseAction act) 
 	{
 		return this.getStartTime() - act.getStartTime();
 	}
 }
