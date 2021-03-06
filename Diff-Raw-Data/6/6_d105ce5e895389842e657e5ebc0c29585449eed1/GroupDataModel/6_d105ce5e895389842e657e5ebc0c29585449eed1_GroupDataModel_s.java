 package ise.gameoflife.participants;
 
 import ise.gameoflife.environment.PublicEnvironmentConnection;
 import ise.gameoflife.inputs.Proposition;
 import ise.gameoflife.models.History;
 import ise.gameoflife.models.UnmodifiableHistory;
 import ise.gameoflife.models.GroupDataInitialiser;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.ElementList;
 import presage.abstractparticipant.APlayerDataModel;
 
 /**
  * Stub for groups
  * @author Olly
  */
 class GroupDataModel extends APlayerDataModel
 {
 	private static final long serialVersionUID = 1L;
 	
 	@Element
 	private String name;
 
 	/**
 	 * Array list of GroupDataModel members
 	 */
 	@ElementList
 	private ArrayList<String> memberList;
 
 	@Element
 	private History<Double> economicPosition;
 
 	@Element
 	private History<HashMap<Proposition,Integer>> propositionHistory;
 
 	@Deprecated
 	GroupDataModel()
 	{
 		super();
 	}
 
 	/**
 	 * Create a new instance of the GroupDataModel, automatically generating a new
 	 * UUID
 	 * @param randomseed The random number seed to use with this class
 	 * @return The new GroupDataModel
 	 */
 	@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
 	public static GroupDataModel createNew(GroupDataInitialiser init)
 	{
 		GroupDataModel ret = new GroupDataModel();
 		ret.myId = UUID.randomUUID().toString();
 		ret.memberList = new ArrayList<String>();
 		ret.myrolesString = "<group>";
 		ret.randomseed = init.getRandomSeed();
 		ret.name = init.getName();
 		ret.economicPosition = new History<Double>(50);
 		ret.propositionHistory = new History<HashMap<Proposition, Integer>>(50);
 		ret.economicPosition.newEntry(init.getInitialEconomicBelief());
 		return ret;
 	}
 
 	/**
 	 * Get the group id that this group has
 	 * @return The UUID of this group
 	 */
 	@Override
 	public String getId()
 	{
 		return myId;
 	}
 
 	List<String> getMemberList()
 	{
 		return Collections.unmodifiableList(memberList);
 	}
 
 	double getCurrentEconomicPoisition()
 	{
 		return economicPosition.getValue();
 	}
 
 	void setEconomicPosition(double pos)
 	{
 		economicPosition.setValue(pos);
 	}
 
 	UnmodifiableHistory<Double> getEconomicPoisition()
 	{
 		return economicPosition.getUnmodifableHistory();
 	}
 
 	void clearRoundData()
 	{
 		economicPosition.newEntry(true);
 		propositionHistory.newEntry(null);
 	}
 
 	void addMember(String a)
 	{
 		memberList.add(a);
 	}
 
 	void removeMember(String a)
 	{
 		memberList.remove(a);
 	}
 
 	@Override
 	public void onInitialise()
 	{
 		// Nothing to see here. Move along, citizen!
 	}
         
 	/**
 	 * Get a re-distribution safe copy of this object. The returned object is
 	 * backed by this one, so their is no need to keep calling this to receive
 	 * updated data.
 	 * @return
 	 */
 	public PublicGroupDataModel getPublicVersion()
 	{
 		return new PublicGroupDataModel(this);
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public Map<Proposition, Integer> getTurnsProposals()
 	{
 		if (propositionHistory.isEmpty()) return null;
 		HashMap<Proposition, Integer> d = propositionHistory.getValue();
 
 		if (d == null) return null;
 		return Collections.unmodifiableMap(d);
 	}
 
 	void setProposals(HashMap<Proposition, Integer> p)
 	{
 		propositionHistory.setValue(p);
 	}
 
 	double getEstimatedSocialLocation()
 	{
 		/*
 		 * Algorithm:
 		 *  - The leaders are those people trust most
 		 *  - The social location is ratio(ish) of leaders to group members
 		 *  - This can be modelled as the the deviation of the trust values
 		 *   - If one agent is trusted more than others, high deviation
 		 *   - This represents an autocracy
 		 *  - The values used to find standard deviation will be the average of each
 		 *    agent's opinion of an agent
 		 */
 		List<Double> values = new ArrayList<Double>(memberList.size());
 		PublicEnvironmentConnection ec = PublicEnvironmentConnection.getInstance();
 		if (ec == null) return 0.5;
 
 		double sigma_x = 0;
 		int n = 0;
 
 		for (String candidate : memberList)
 		{
 			n = 0;
 			sigma_x = 0;
 			for (String truster : memberList)
 			{
 				PublicAgentDataModel dm = ec.getAgentById(truster);
 				Double t = (dm == null ? null : dm.getTrust(candidate));
 				if (t != null && !candidate.equals(truster))
 				{
 					sigma_x += t;
 					++n;
 				}
 			}
 			if (n > 0) values.add(sigma_x / n);
 		}
 
 		n = values.size();
 
 		if (n == 0) return 0.5;
 
 		sigma_x = 0;
 		for (Double v : values)	sigma_x += v;
 
 		double mu = sigma_x / n;
 
 		sigma_x = 0;
 		for (Double v : values)	sigma_x += (v - mu)*(v - mu);
 
		sigma_x = 2 * Math.sqrt(sigma_x);
                
                return sigma_x;
 	}
 
 	int size()
 	{
 		return memberList.size();
 	}
 }
