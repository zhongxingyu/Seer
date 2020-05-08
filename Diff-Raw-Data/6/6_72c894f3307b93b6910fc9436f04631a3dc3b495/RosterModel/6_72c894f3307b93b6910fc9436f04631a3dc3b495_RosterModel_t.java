 package core.abstraction;
 
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterGroup;
 
 import core.im.Buddy;
 import core.im.ChatboardRoster;
 
 public class RosterModel implements ListDataListener{
 	
 	private Vector<RosterGroup> groups;
 	private Vector<ListDataListener>listeners;
 	private ChatboardRoster roster;
 	
 	public HashMap<String, String> aliasMap;
 	public HashMap<String, Vector<Buddy>> onlineMap;
 	
 	
 	
 	public RosterModel()
 	{
 		groups = new Vector<RosterGroup>();
 		onlineMap = new HashMap<String, Vector<Buddy>>();
 		aliasMap = new HashMap<String, String>();
 		listeners = new Vector<ListDataListener>();
 		roster = null;
 	}
 	
 	public RosterModel(ChatboardRoster theRoster)
 	{
 		this();
 		roster = theRoster;
 		//setup();
 		roster.addListener(this);
 	}
 	
 	public void addListener(ListDataListener listener)
 	{
 		listeners.add(listener);
 	}
 	
 	public void setup()
 	{
 		groups = new Vector<RosterGroup>(roster.roster.getGroups());
 		updateOnline(roster.online);
 	}
 	
 	public String toString()
 	{
 		return onlineMap.toString();
 	}
 	
 	public void updateOnline(Vector<Buddy> online)
 	{
 		for(RosterGroup g : groups)
 		{
 			Vector<Buddy>groupBuddies = new Vector<Buddy>();
 			for(int i = 0; i < online.size(); i++)
 			{
 				Buddy b = online.get(i);
 				//System.out.println(g.contains(b.userID));
 				if(g.contains(b.userID))
 				{
 					//System.out.println("In here");
 					b.groupName = g.getName();
 					groupBuddies.add(b);
 					online.remove(i);
 					i--;
					
 				}		
				if(aliasMap.get(b.alias) == null)
					aliasMap.put(b.alias, b.userID);
 			}
 			onlineMap.put(g.getName(), groupBuddies);
 		}
 		if(!online.isEmpty())
 			onlineMap.put("Unorganized", online);
 	}
 	
 	public ChatboardRoster getRoster() {
 		return roster;
 	}
 	public void setRoster(ChatboardRoster roster) {
 		this.roster = roster;
 	}
 	public Set<String> getGroups()
 	{
 		return onlineMap.keySet();
 	}
 	@Override
 	public void contentsChanged(ListDataEvent e) {
 		setup();
 		
 	}
 	@Override
 	public void intervalAdded(ListDataEvent e) {
 		setup();
 		
 	}
 	@Override
 	public void intervalRemoved(ListDataEvent e) {
 		setup();
 		
 	}
 }
