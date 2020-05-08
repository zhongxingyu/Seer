 package sfs2x.extensions.projectsasha.game.entities.gateways;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 import java.util.Vector;
 
 import sfs2x.extensions.projectsasha.game.GameConsts;
 import sfs2x.extensions.projectsasha.game.entities.Player;
 import sfs2x.extensions.projectsasha.game.ia.Trace;
 import sfs2x.extensions.projectsasha.game.entities.software.Software;
 import sfs2x.extensions.projectsasha.game.entities.software.Proxy;
 import sfs2x.extensions.projectsasha.game.entities.software.SoftwareFactory;
 
 public abstract class Gateway 
 {	
 	
 	private static int lastGatewayID = 1000;
 	
 	private List<Trace> traces;
 	private Software[] installedSoftware;
 	private Gateway[] neighboors;
 	private Map<String,Integer> costsSoFar;
 	private Map<String, Gateway> parentForPath;
 	private Player owner;
 	private String name, state;
 	private int id, x, y;
 	private double lat, lon;
 	private Set<Integer> startedAttacks = Collections.synchronizedSet(new HashSet<Integer>());
 
 	
 	public Gateway(Player owner, String name, String state, int x, int y, float lat, float lon)
 	{
 		this.owner = owner;
 		this.name = name;
 		this.state = state;
 		this.id = getNewID();
 		this.traces = new Vector<Trace>();
 		this.installedSoftware = new Software[GameConsts.MAX_SOFTWARE_INSTALLED];
 		this.x = x;
 		this.y = y;
 		this.lat = lat;
 		this.lon = lon;
 		this.costsSoFar = new HashMap<String,Integer>();
 		this.parentForPath = new HashMap<String, Gateway>();
 	}	
 	
 	
 	//GETTERS
 	
 	public int getID()
 	{
 		return id;
 	}
 
 	public Player getOwner()
 	{
 		return owner;
 	}
 	
 	public Gateway[] getNeighboors()
 	{
 		return neighboors;
 	}
 	
 	public List<Trace> getTraces()
 	{
 		return traces;
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public String getState()
 	{
 		return state;
 	}
 	
 	public int getWeightByRelevance(int attackRelevance)
 	{
 		int traceCount=0;
 		
 		for(Trace t : traces)
 			if(t.relevance == attackRelevance)
 				traceCount++;
 		return (int)((1/(1+traceCount))*100);
 	}
 	
 	public int getWeightByDistance(Gateway destination)
 	{
 		return this.distanceFrom(destination);
 	}
 	
 	public int distanceFrom(Gateway dest)
 	{
 		double dLat = Math.toRadians(dest.lat - this.lat);
 		double dLon =  Math.toRadians(dest.lon - this.lon);
 		double lat1 =  Math.toRadians(this.lat);
 		double lat2 =  Math.toRadians(dest.lat);
 
 		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
 		
 		return (int)(GameConsts.EARTH_RADIUS * c * 100 / GameConsts.EARTH_MAX_DISTANCE);
 	}
 	
 	public Map<String, Integer> getCostsSoFar()
 	{
 		return this.costsSoFar;
 	}
 	
 	public void setParentForPath(String user, Gateway gw)
 	{
 		this.parentForPath.put(user, gw);
 	}
 	
 	public Map<String, Gateway> getParentForPath()
 	{
 		return this.parentForPath;
 	}
 	
 	public void setCostsSoFar(String user, Integer cost)
 	{
 		this.costsSoFar.put(user, cost);
 	}
 	
 	public Set<Integer> getStartedAttacks() 
 	{
 		return startedAttacks;
 	}
 
 	public void addStartedAttack(int startedAttack) 
 	{
 		this.startedAttacks.add(startedAttack);
 	}
 
 	public void removeStartedAttack(int startedAttack) 
 	{
 		this.startedAttacks.remove(startedAttack);
 	}
 
 	public boolean hasStartedAttack(int startedAttack)
 	{
 		return this.startedAttacks.contains(startedAttack);
 	}
 	
 	synchronized public void confiscate()
 	{
 		this.traces.clear();
 		this.installedSoftware = new Software[GameConsts.MAX_SOFTWARE_INSTALLED];
 		this.owner = null;
 		this.startedAttacks.clear();	
 	}	
 	
 	synchronized public int getDefenceLevel()
 	{
 		int dl = getBaseDefenceLevel();
 		
 		for(Software s : installedSoftware)
 		{
 			if(s != null)
 				dl += s.getDefenceLevel();
 		}
 		return dl;
 	}
 	
 	synchronized public int getAttackLevel()
 	{
 		int dl = getBaseAttackLevel();
 		
 		for(Software s : installedSoftware)
 		{
 			if(s != null)
 				dl += s.getAttackLevel();
 		}
 		return dl;
 	}
 	
 	
 	synchronized public Software[] getInstalledSoftwares()
 	{
 		return this.installedSoftware;
 	}
 	
 	synchronized public Software getInstalledSoftware(String type)
 	{
 		for(Software s : installedSoftware)
 		{
 			if (s.getType() == type)
 				return s;
 		}
 		return null;
 	}
 	
 	synchronized public Software getInstalledSoftware(int slot)
 	{
 		return installedSoftware[slot];
 	}
 	
 	synchronized public int getX(){
 		return this.x;
 	}
 	
 	synchronized public int getY(){
 		return this.y;
 	}
 	
 	//SETTERS
 	
 	public void setNeighborhoods(Gateway[] neighboors)
 	{
 		this.neighboors = neighboors;
 	}
 	
 	public void setOwner(Player p)
 	{
 		this.owner = p;
 	}
 	
 	//ABSTRACT METHODS
 
 	protected abstract int getBaseAttackLevel();
 	protected abstract int getBaseDefenceLevel();
 	public abstract int getPaymentAmount();
 	public abstract void onGatewayConquered(Player newOwner, Player oldOwner);
 		
 	//STATIC METHODS
 	
 	private static synchronized int getNewID()
 	{
 		return lastGatewayID++;
 	}
 	
 	public static int difference(Gateway from, Gateway to)
 	{
 		int diff = to.getDefenceLevel()-from.getAttackLevel();
 		return diff;
 	}
 	
 	//NON STATIC METHODS
 	synchronized public void installSoftware(String type, Player hacker)
 	{
 		Software newSoftware = SoftwareFactory.makeSoftware(type);
 		if(this.owner!=hacker)
 			return;
 
 		
 		if(this.hasSoftware(newSoftware.getType()) && !newSoftware.isCumulative())
 			return;
 		
 		int available_slot = this.getNextSoftwareSlotAvailable();
 		newSoftware.setVersion(1);
 		
 		if(available_slot < GameConsts.MAX_SOFTWARE_INSTALLED)
 		{
 			newSoftware.setSlot(available_slot);
 			installedSoftware[available_slot] = newSoftware;
 			
 		}else
 			return;		
 	}
 	
 	synchronized public void uninstallSoftware(String type, Player p)
 	{
 		this.uninstallSoftware(type, p, 0);
 	}
 	
 	synchronized public void uninstallSoftware(String type, Player hacker, int slot)
 	{
 		
 		if(this.owner!=hacker)
 			return;
 		
 		
 		if(this.hasSoftware(type)) //is installed
 		{
 			Software theSoftware = this.getInstalledSoftware(type); 
 			
 			if(!this.getInstalledSoftware(type).isCumulative()) //not cumulative
 			{
 				theSoftware.setVersion(0);
 				installedSoftware[theSoftware.getSlot()] = null;
 			}
 			else //cumulative
 			{
 				this.getInstalledSoftware(slot).setVersion(0);
 				installedSoftware[slot] = null;
 			}
 		}
 	}
 	
 	synchronized public boolean hasSoftware(String type){
 		for(Software s : installedSoftware)
 			if(s != null && type == s.getType())
 				return true;
 		return false;
 	}
 	
 	synchronized public int getNextSoftwareSlotAvailable(){
 		int slot = 0;
 		
 		for(Software s : installedSoftware)
 			if(s != null)
 				slot++;
 		
 		return slot;
 	}
 	
 	synchronized public void upgradeSoftware(int slot, Player hacker){
 		Software sw = null;
 		try
 		{
 			sw = this.getInstalledSoftware(slot);
 			this.upgradeSoftware(sw.getType(), hacker, slot);
 		}
 		catch(NullPointerException e)
 		{/*SLOT EMPTY*/}
 		
 	}
 	
 	synchronized public void upgradeSoftware(String type, Player hacker){
 	
 		if(this.owner!=hacker)
 			return;
 		
 		if(this.hasSoftware(type))
 			this.getInstalledSoftware(type).upgrade();
 		else
 			return; 
 	}
 	
 	synchronized public void upgradeSoftware(String type, Player hacker,  int slot){
 		
 		if(this.owner!=hacker)
 			return;
 		
 		if(this.hasSoftware(type))
 			this.getInstalledSoftware(slot).upgrade();
 		else
 			return;
 	}
 	
 	synchronized public void downgradeSoftware(String type, Player hacker){
 		
 		if(this.owner!=hacker)
 			return;
 		
 		if(this.hasSoftware(type))
 			this.getInstalledSoftware(type).downgrade();
 		else
 			return;
 		}
 	
 	synchronized public void downgradeSoftware(String type, Player hacker, int slot){
 		
 		if(this.owner!=hacker)
 			return;
 		
 		if(this.hasSoftware(type))
 			this.getInstalledSoftware(slot).downgrade();
 		else
 			return; 
 	}
 	
 	@Override
 	synchronized public String toString(){
 		String ownerName = this.owner == null?"NEUTRAL":this.owner.getName();
 		String returnString = "/*****************************************\\\n";
 		returnString += " * Owner:\t "+ownerName+"\n";
 		returnString += " * Name:\t "+this.name+"\n";
 		returnString += " * State:\t "+this.state+"\n";
 		returnString += " * Id:\t\t "+this.id+"\n";
 		returnString += " * Attak:\t "+this.getAttackLevel()+"\n";
 		returnString += " * Defence:\t "+this.getDefenceLevel()+"\n";
 		returnString += " * Software:\t "+Arrays.toString(this.getInstalledSoftwares())+"\n";
 		returnString += "\\*****************************************/";
 		return returnString;
 	}
 	
 	public boolean isNeighboor(Gateway s){
 		for(Gateway g : neighboors)
 			if(g != null && g.getName() == s.getName())
 				return true;
 		return false;
 	}
 	
 	synchronized public void setTrace(List<Gateway> list, int level)
 	{
 		if(this.hasSoftware(GameConsts.LOGCLEANER))
 		{
 			Software s = this.getInstalledSoftware(GameConsts.LOGCLEANER); 
 			level-=s.getVersion();
 		}
 		for(Gateway g : list)
 		{
			Trace tr = new Trace(this,this.getID(),level,this.getOwner().getName());
 			g.traces.add(tr);

 		}
 	}
 	
 	
 	
 	public void startTimedEvent()
 	{
 		//timed event here
 	}
 	
 	synchronized public List<Gateway> tracePath(final Gateway destination, final int relevance )
 	{
 		Queue<Gateway> openSet = null;
 		int numSearchSteps = 0;
 		final String sourceOwner = this.getOwner().getUserName();
 		int maxSteps = ((Proxy) this.getInstalledSoftware(GameConsts.PROXY)).getRange();
 		List<Gateway> path = new ArrayList<Gateway>();
 		int proxyLevel = this.getInstalledSoftware(GameConsts.PROXY).getVersion();
 		int tentativeG = 0 ;
 		Set<Gateway> bucket = new HashSet<Gateway>();
 		
 		openSet = new PriorityQueue<Gateway>(4, new Comparator<Gateway>()
 		{
 			@Override
 			public int compare(Gateway o1, Gateway o2)
 			{
 				return (o1.costsSoFar.get(sourceOwner) + o1.distanceFrom(destination)) - (o2.costsSoFar.get(sourceOwner) + o2.distanceFrom(destination));
 			}
 		 });	
 		
 		Set<Gateway> closedSet = new HashSet<Gateway>();
 		
 		openSet.add(this);
 		bucket.add(this);
 		//DEBUG ONLY!
 		maxSteps = 99;
 		while(!openSet.isEmpty()  && (numSearchSteps <= maxSteps))
 		{
 			Gateway currentGateway = openSet.poll();
 			bucket.add(currentGateway);
 			if(currentGateway.getID() == destination.getID())
 			{
 				path.add(currentGateway);
 				Gateway parent = currentGateway.parentForPath.get(sourceOwner);
 				while(parent != null)
 				{
 					path.add(0, parent);
 					parent = parent.parentForPath.get(sourceOwner);
 				}
 				
 				for(Gateway g: bucket)
 				{
 					g.setParentForPath(sourceOwner, null);
 					g.setCostsSoFar(sourceOwner, 0);
 				}
 				return path; 
 			}
 			
 			Gateway[] neighboors = currentGateway.getNeighboors();
 			
 			for(int i=0; i<neighboors.length; i++ )
 			{
 				
 				Gateway currentNeighboor = neighboors[i];
 				bucket.add(currentNeighboor);
 				boolean inOpenSet;
 				
 				if(closedSet.contains(currentNeighboor))
 					continue;
 				
 				Gateway discSuccessorNode = null;
 				for (Gateway gw : openSet)
 				{
 					if(gw.equals(currentNeighboor)) 
 						discSuccessorNode = gw;
 				}
 				
 				if(discSuccessorNode != null)
 				{
 					currentNeighboor = discSuccessorNode;
 					inOpenSet = true;
 				}
 				else
 					inOpenSet = false;
 				
 				if(proxyLevel == 1)
 					tentativeG = currentGateway.costsSoFar.get(sourceOwner) + currentGateway.getWeightByDistance(currentNeighboor);
 				else if (proxyLevel >= 2)
 					tentativeG = currentGateway.costsSoFar.get(sourceOwner) + currentGateway.getWeightByRelevance(relevance);
 				
 				if(inOpenSet && tentativeG >= currentNeighboor.costsSoFar.get(sourceOwner))
 					continue;
 				
 				currentNeighboor.parentForPath.put(sourceOwner, currentGateway);
 				
 				if(inOpenSet)
 				{
 					openSet.remove(currentNeighboor);
 					currentNeighboor.costsSoFar.put(sourceOwner, tentativeG);
 					openSet.add(currentNeighboor);
 				}
 				else
 				{
 					currentNeighboor.costsSoFar.put(sourceOwner, tentativeG);
 					openSet.add(currentNeighboor);
 				}
 				
 				closedSet.add(currentGateway);
 				numSearchSteps++;
 			}
 		}
 
 		
 		for(Gateway g: bucket)
 		{
 			g.setParentForPath(sourceOwner, null);
 			g.setCostsSoFar(sourceOwner, 0);
 		}
 		
 		return null;
 	}
 }
