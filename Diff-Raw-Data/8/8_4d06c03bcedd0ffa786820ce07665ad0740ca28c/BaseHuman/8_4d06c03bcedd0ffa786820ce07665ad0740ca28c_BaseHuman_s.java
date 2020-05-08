 package agents;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import repast.simphony.context.Context;
 import repast.simphony.engine.environment.RunEnvironment;
 import repast.simphony.engine.schedule.ScheduledMethod;
 import repast.simphony.parameter.Parameters;
 import repast.simphony.space.graph.Network;
 import repast.simphony.space.graph.RepastEdge;
 import repast.simphony.util.collections.IndexedIterable;
 import simconsts.Operations;
 import simconsts.SimConstants;
 
 import org.apache.commons.math.distribution.NormalDistributionImpl;
 
 import probtools.Distributions;
 import probtools.NDParams;
 
 import graphtools.generators.*;
 import graphtools.io.graphML.XMLAttribute;
 import graphtools.io.graphML.XMLNodeKey;
 import graphtools.samplers.GeneralTools;
 
 public class BaseHuman implements Comparable{
 	
 	static int rtA1 = 0, rtA2 = 0;
 	//--------------------------------
 	static int rtA1a = 0, rtA1b = 0, rtA1c = 0;	
 	//--------------------------------
 	static int rtA1a1 = 0, rtA1a2 = 0;
 	static int rtA1b1 = 0, rtA1b2 = 0;
 	static int rtA1c1 = 0, rtA1c2 = 0;
 	//---------------------------------
 	static int rtA1a1a = 0, rtA1a1b = 0;
 	static int rtA1a2a = 0, rtA1a2b = 0;
 	
 	static int rtA1b1a = 0, rtA1b1b = 0;
 
 	static int rtA1c1a = 0, rtA1c1b = 0;
 	static int rtA1c2a = 0, rtA1c2b = 0;
 	//---------------------------------
 	 
 	static int rtA2a = 0, rtA2b = 0;
 	static int rtA2a1 = 0, rtA2a2 = 0, rtA2a3 = 0;
 	static int rtA2a1A = 0, rtA2a1B = 0;
 	static int rtA2a1A1 = 0, rtA2a1A2 = 0, rtA2a1B1 = 0, rtA2a1B2 = 0;
 	static int rtA2a2A = 0, rtA2a2B = 0;
 	static int rtA2a2A1 = 0, rtA2a2A2 = 0, rtA2a2B1 = 0, rtA2a2B2 = 0;
 	static int rtA2a3A = 0, rtA2a3B = 0;
 	static int rtA2a3A1 = 0, rtA2a3A2 = 0, rtA2a3B1 = 0, rtA2a3B2 = 0;
 	
 	static int rtA2b1 = 0, rtA2b2 = 0;
 	static int rtA2b1A = 0, rtA2b1B = 0;
 	static int rtA2b2A = 0, rtA2b2B = 0;
 	
 	String id;
 	private Context<?> context;
 	private Network<BaseHuman> network;
 	
 	private boolean isSmoker;
 	private double willpower;
 	private double health;
 	private int smokedPerDay;
 	
 	private double stress;
 	
 	private boolean givingUp;
 	private int stepsSinceGiveUp;
 	private int giveUpAttempts;
 	
 	private double influenceability;
 	
 	private double sociable;
 	
 	//new attrs - leadership?
 	
 	/**
 	 * Constructor for scale free networks
 	 * @param id
 	 * @param context
 	 * @param network
 	 * @param generateOnAddition
 	 * @param reciprocate
 	 * @param reciprocateChance
 	 */
 	public BaseHuman(String id, Context<?> context, Network<BaseHuman> network, boolean generateOnAddition, boolean reciprocate, double reciprocateChance, NDParams nd)
 	{
 		this.id = id;
 		this.context = context;
 		this.network = network;
 
 		Parameters parameters = RunEnvironment.getInstance().getParameters();
 		
 		if(Math.random() > (Double)parameters.getValue("pSmokes"))
 		{
 			this.isSmoker = false;
 			//Some people are giving up
 			if(Math.random() < (Double)parameters.getValue("pGiveUp"))
 				this.givingUp = false;
 			else
 			{
 				this.givingUp = true;
 				this.stepsSinceGiveUp = (int) Math.random() * 50;
 			}
 		}
 		else
 		{
 			this.isSmoker = true;
 			this.givingUp = false;
 			this.stepsSinceGiveUp = 0;
 			
 		}
 		
 		this.giveUpAttempts = 0;
 		
 		this.willpower = Distributions.getNDWithLimits(SimConstants.WillpowerMean, SimConstants.WillpowerSD, 0, 1);
 		this.health = Distributions.getNDWithLimits(SimConstants.HealthMean, SimConstants.HealthSD, 0, 1);
 		this.sociable = Distributions.getNDWithLimits(SimConstants.SociableMean, SimConstants.SociableSD, 0, 1);
		this.influenceability = Distributions.getNDWithLimits(0.5, 0.4, 0, 1);
 		
 		this.stress = Distributions.getNDWithLimits(0.5, 0.4, 0, 1);
 		
 		//We need some heavy and light smokers, so add some in
 		double rand = Math.random();
 		if(rand < 0.2)
 		{
 			if(isSmoker)
 				this.smokedPerDay = Distributions.getIntNDWithLimits(SimConstants.SmokedPerDayLowerMean, SimConstants.SmokedPerDaySD, 0, 40);
 			else
 				this.smokedPerDay = 0;
 		}
 		else if (rand > 0.8)
 		{
 			if(isSmoker)
 				this.smokedPerDay = Distributions.getIntNDWithLimits(SimConstants.SmokedPerDayMidMean, SimConstants.SmokedPerDaySD, 0, 40);
 			else
 				this.smokedPerDay = 0;
 		}
 		else
 		{
 			if(isSmoker)
 				this.smokedPerDay = Distributions.getIntNDWithLimits(SimConstants.SmokedPerDayUpperMean, SimConstants.SmokedPerDaySD, 0, 40);
 			else
 				this.smokedPerDay = 0;
 		}
 		//Insert code for adding to network
 		if(generateOnAddition)
 			ScaleFree.addToRSF(this, context, network, reciprocate, reciprocateChance, nd);
 	}
 	
 	public boolean isGivingUp() {
 		return givingUp;
 	}
 
 	public int getStepsSinceGiveUp() {
 		return stepsSinceGiveUp;
 	}
 
 	/**
 	 * Constructor for small world networks
 	 */
 	public BaseHuman(String id, Context<?> context)
 	{
 		this.id = id;
 		this.context = context;
 		this.isSmoker = false;
 	}
 	
 	/**
 	 * Constructor for graphML importer
 	 */
 	public BaseHuman(Context context, String id, HashMap<String, String> attrList, HashMap<String, String> keyMap, boolean attrLoadOnly)
 	{
 		this.id = id;
 		this.context = context;
 		String keyID = "";
 		
 		Parameters parameters = RunEnvironment.getInstance().getParameters();
 		
 		if(!attrLoadOnly)
 		{
 			keyID = keyMap.get("isSmoker");
 			this.isSmoker = Boolean.parseBoolean(attrList.get(keyID));
 			
 			keyID = keyMap.get("smokedPerDay");
 			this.smokedPerDay = Integer.parseInt(attrList.get(keyID));
 			
 			keyID = keyMap.get("givingUp");
 			this.givingUp = Boolean.parseBoolean(attrList.get(keyID));
 			
 			keyID = keyMap.get("stepsSinceGiveUp");
 			this.stepsSinceGiveUp = Integer.parseInt(attrList.get(keyID));
 		}
 		else
 		{
 			if(Math.random() > (Double)parameters.getValue("pSmokes"))
 			{
 				this.isSmoker = false;
 				//Some people are giving up
 				if(Math.random() < (Double)parameters.getValue("pGiveUp"))
 					this.givingUp = false;
 				else
 				{
 					this.givingUp = true;
 					this.stepsSinceGiveUp = (int) Math.random() * 50;
 				}
 			}
 			else
 			{
 				this.isSmoker = true;
 				this.givingUp = false;
 				this.stepsSinceGiveUp = 0;
 				
 			}
 		}
 		
 		if(this.isSmoker && this.smokedPerDay <= 0)
 		{
 			//We need some heavy and light smokers, so add some in
 			double rand = Math.random();
 			if(rand < 0.2)
 			{
 				if(isSmoker)
 					this.smokedPerDay = Distributions.getIntNDWithLimits(SimConstants.SmokedPerDayLowerMean, SimConstants.SmokedPerDaySD, 0, 40);
 				else
 					this.smokedPerDay = 0;
 			}
 			else if (rand > 0.8)
 			{
 				if(isSmoker)
 					this.smokedPerDay = Distributions.getIntNDWithLimits(SimConstants.SmokedPerDayMidMean, SimConstants.SmokedPerDaySD, 0, 40);
 				else
 					this.smokedPerDay = 0;
 			}
 			else
 			{
 				if(isSmoker)
 					this.smokedPerDay = Distributions.getIntNDWithLimits(SimConstants.SmokedPerDayUpperMean, SimConstants.SmokedPerDaySD, 0, 40);
 				else
 					this.smokedPerDay = 0;
 			}
 		}
 		else if(!this.isSmoker && this.smokedPerDay > 0)
 		{
 			this.smokedPerDay = 0;
 		}
 		
 		keyID = keyMap.get("willpower");
 		this.willpower = Double.parseDouble(attrList.get(keyID));
 		
 		keyID = keyMap.get("health");
 		this.health = Double.parseDouble(attrList.get(keyID));
 		
 		
 		keyID = keyMap.get("influenceability");
 		this.influenceability = Double.parseDouble(attrList.get(keyID));
 		
 		keyID = keyMap.get("sociable");
 		this.sociable = Double.parseDouble(attrList.get(keyID));
 		
 	}
 	
 	
 	/**
 	 * Sets the network of the node. Should only be used after a small world generation/GraphML
 	 * importing
 	 * @param network
 	 */
 	public void setNetwork(Network<BaseHuman> network)
 	{
 		this.network = network;
 	}
 	
 	
 	@ScheduledMethod(start = 1, interval = 1)
 	public void step() {
 		//insert decisions, etc
 		//decide actions
 		
 		
 		//reconfigure networks
 		//we probabably want to get nodes within a local network (2 or 3 hops)
 		//to see if anyone random is worth picking.
 		
 		HashSet<NeighborStore> localNeighborhood = GeneralTools.getUniqueWithinHops(context, network, this, SimConstants.LocalNeighborhoodHops, true);
 		
 		//print("Calculating Decision Metrics");
 		NeighborMetrics nm = new NeighborMetrics(localNeighborhood);
 		
 		//statusCall();
 		
 		decisionTree(nm);
 		connectionAdjust(localNeighborhood, nm, network);
 		if(this.isGivingUp())
 			this.stepsSinceGiveUp++;
 
 		
 	}
 	
 	
 	private boolean decisionTree(NeighborMetrics nm)
 	{
 		//Add bypass probability for decisions
 		boolean changeMade = false;		
 		
 		//print("Influence: H:" + nm.getInfHealth() + " W:" + nm.getInfWillpower() + " S:" + nm.getInfIsSmokerVal());
 		
 		if(this.isSmoker)
 		{
 			rtA1++;
 			this.health = changeWithinBounds(this.health, 0, 1, (this.smokedPerDay/SimConstants.cigLimit)/10, Operations.SUBTRACT);
 			if(this.smokedPerDay <= 5)
 			{
 				rtA1a++;
 				if(this.giveUpAttempts > 1)
 				{
 					rtA1a1++;
 					if(this.health < nm.getInfHealth())
 					{
 						rtA1a1a++;
 						endDecision(true, 0.6, nm);
 					}	
 					else
 					{
 						rtA1a1b++;
 						endDecision(true, 0.8, nm);
 					}
 				}
 				else
 				{
 					rtA1a2++;
 					if(this.health < nm.getInfHealth())
 					{
 						rtA1a2a++;
 						endDecision(true, 0.5, nm);
 					}	
 					else
 					{
 						rtA1a2b++;
 						endDecision(true, 0.7, nm);
 					}
 				}
 				
 			}
 			else if(this.smokedPerDay > 5 && this.smokedPerDay < 15)
 			{
 				rtA1b++;
 				if(this.health < nm.getInfHealth())
 				{
 					rtA1b1++;
 					if(nm.getAvgCigPerDay() > this.smokedPerDay * SimConstants.SmokerPerDayUpperPct || irrationalChoice())
 					{
 						rtA1b1a++;
 						this.smokedPerDay = (int)Math.round(changeWithinBounds(this.smokedPerDay, 0, SimConstants.cigLimit, (nm.getInfCigPerDay()* 0.5), Operations.ADD));
 					}
 					else if(nm.getAvgCigPerDay() < this.smokedPerDay * SimConstants.SmokerPerDayLowerPct || irrationalChoice())
 					{
 						rtA1b1a++;
 						this.smokedPerDay = (int)Math.round(changeWithinBounds(this.smokedPerDay, 0, SimConstants.cigLimit, (nm.getInfCigPerDay()* 0.5), Operations.SUBTRACT));
 					}
 				}
 				else if(irrationalChoice())
 				{
 					rtA1b2++;
 					giveUpSmoking();
 				}
 			}
 			else 
 			{
 				rtA1c++;
 				if(this.willpower < 0.7)
 				{
 					rtA1c1++;
 					if(this.health < nm.getInfHealth())
 					{
 						rtA1c1a++;
 						endDecision(true, 0.4, nm);
 					}
 					else
 					{
 						rtA1c1b++;
 						endDecision(true, 0.6, nm);
 					}
 				}
 				else
 				{
 					rtA1c2++;
 					if(this.health < nm.getInfHealth())
 					{
 						rtA1c2a++;
 						endDecision(true, 0.6, nm);
 					}
 					else
 					{
 						rtA1c2b++;
 						endDecision(true, 0.8, nm);
 					}
 				}
 			}
 		}
 		else
 		{
 			rtA2++;
 			if(this.givingUp)
 			{
 				rtA2a++;
 				this.health = changeWithinBounds(this.health, 0, 1, (this.stepsSinceGiveUp/SimConstants.giveUpStepLimit)/10, Operations.ADD);
 				if(this.giveUpAttempts == 0)
 				{
 					rtA2a1++;
 					//perhaps incorporate the number of people giving up, as sort of a group mentality thing
 					if(this.willpower < nm.getInfWillpower())
 					{
 						rtA2a1A++;
 						if(this.health > nm.getInfHealth())
 						{ 
 							rtA2a1A1++;
 							endDecision(false, 0.5, nm);
 						}
 						else
 						{
 							rtA2a1A2++;
 							endDecision(false, 0.7, nm);
 						}
 					}
 					else
 					{
 						rtA2a1B++;
 						//Stronger people so far, hinge on own behaviour
 						if(this.health > nm.getInfHealth())
 						{ 
 							rtA2a1B1++;
 							endDecision(false, 0.6, nm);
 						}
 						else
 						{
 							rtA2a1B2++;
 							endDecision(false, 0.8, nm);
 						}
 					}
 				}
 				else if(this.giveUpAttempts >= 1 && this.giveUpAttempts < 5)
 				{
 					rtA2a2++;
 					if(nm.getPcGivingUp() > 0.5)
 					{
 						rtA2a2A++;
 						if(this.willpower < nm.getInfWillpower())
 						{
 							rtA2a2A1++;
 							endDecision(false, 0.5, nm);
 						}
 						else 
 						{
 							rtA2a2A2++;
 							endDecision(false, 0.8, nm);
 						}
 					}
 					else
 					{
 						rtA2a2B++;
 						if(this.willpower < nm.getInfWillpower())
 						{
 							rtA2a2B1++;
 							endDecision(false, 0.4, nm);
 						}
 						else 
 						{
 							rtA2a2B1++;
 							endDecision(false, 0.6, nm);
 						}
 					}
 				}
 				else
 				{
 					rtA2a3++;
 					if(nm.getPcGivingUp() > 0.8)
 					{
 						rtA2a3A++;
 						if(this.health < nm.getInfHealth())
 						{
 							rtA2a3A1++;
 							endDecision(false, 0.5, nm);
 						}
 						else 
 						{
 							rtA2a3A2++;
 							endDecision(false, 0.8, nm);
 						}
 					}
 					else
 					{
 						rtA2a3B++;
 						if(this.health < nm.getInfHealth())
 						{
 							rtA2a3B1++;
 							endDecision(false, 0.3, nm);
 						}
 						else 
 						{
 							rtA2a3B1++;
 							endDecision(false, 0.5, nm);
 						}
 					}
 				}
 				//completely arbitrary
 				if(this.stepsSinceGiveUp >= SimConstants.giveUpStepLimit)
 				{
 					this.givingUp = false;
 				}
 			}
 			else
 			{
 				rtA2b++;
 				//String path = "";
 				//path += "BRANCH 1";
 				if(this.health < nm.getInfHealth())
 				{
 					rtA2b1++;
 					//path += "\n\tBRANCH 2a";
 					if(this.willpower < 0.3)
 					{
 						rtA2b1A++;
 						//path += "\n\t\tBRANCH 3a";
 						endDecision(false, 0.6, nm);
 					}
 					else
 					{
 						rtA2b1B++;
 						//path += "\n\t\tBRANCH 3b";
 						endDecision(false, 0.8, nm);
 					}
 				}
 				else
 				{
 					rtA2b2++;
 					//path += "\n\tBRANCH 2b";
 					if(this.willpower < 0.4)
 					{
 						rtA2b2A++;
 						//path += "\n\t\tBRANCH 3c";
 						endDecision(false, 0.7, nm);
 					}
 					else
 					{
 						rtA2b2A++;
 						//path += "\n\t\tBRANCH 3d";
 						endDecision(false, 0.9, nm);
 					}
 				}
 				//if(this.isSmoker)
 					//print(path);
 			}
 			
 //			if(this.isSmoker)
 //				this.willpower = changeWithinBounds(this.willpower, 0, 1, (this.willpower * 0.01), Operations.SUBTRACT);
 		}
 		
 		return changeMade;
 	}
 	
 	
 	// HEY YOU
 	//*********************************************
 	// CHANGE THE PCT STUFF TO INFLUENCE PCT
 	private void endDecision(boolean giveUp, double compPct, NeighborMetrics nm)
 	{
 		//Also look at non smokers
 		if(giveUp)
 		{
			if((nm.getInfPcGivingUp() > compPct && Math.random()  > 0.75) || irrationalChoice())
 			{
 				///print((compPct) + " against " + nm.getInfPcGivingUp());
 				giveUpSmoking();
 				this.willpower = changeWithinBounds(this.willpower, 0, 1, (this.willpower * 0.01), Operations.ADD);
 			}
 			else
 			{
 				if(nm.getAvgCigPerDay() > this.smokedPerDay * SimConstants.SmokerPerDayUpperPct || irrationalChoice())
 				{
 					this.smokedPerDay = (int)Math.round(changeWithinBounds(this.smokedPerDay, 0, SimConstants.cigLimit, (nm.getInfCigPerDay()* 0.5), Operations.ADD));
 					this.willpower = changeWithinBounds(this.willpower, 0, 1, (this.willpower * 0.01), Operations.SUBTRACT);
 				}
 				else if(nm.getAvgCigPerDay() < this.smokedPerDay * SimConstants.SmokerPerDayUpperPct || irrationalChoice())
 				{
 					this.smokedPerDay = (int)Math.round(changeWithinBounds(this.smokedPerDay, 0, SimConstants.cigLimit, (nm.getInfCigPerDay()* 0.5), Operations.SUBTRACT));
 					this.willpower = changeWithinBounds(this.willpower, 0, 1, (this.willpower * 0.01), Operations.SUBTRACT);
 				}
 				else
 				{
 					this.willpower = changeWithinBounds(this.willpower, 0, 1, (this.willpower * 0.01), Operations.ADD);
 				}
 				
 			}
 		}
 		else
 		{
			if((nm.getInfPcSmokes() > compPct && Math.random()  > 0.75) || irrationalChoice())
 			{
 				statusCall();
 				print(nm.getInfPcSmokes() + " against " + compPct);
 				relapseSmoking((int)nm.getInfCigPerDay());
 				this.willpower = changeWithinBounds(this.willpower, 0, 1, (this.willpower * 0.01), Operations.SUBTRACT);
 			}
 			else
 			{
 				//Do something.
 				this.willpower = changeWithinBounds(this.willpower, 0, 1, (this.willpower * 0.01), Operations.ADD);
 			}
 		}
 		
 	}
 	private void statusCall()
 	{
 		print("Smoker: " + this.isSmoker + " Cigs: " + this.smokedPerDay + " Giving Up?: " + this.givingUp + " Steps: " + this.stepsSinceGiveUp); 
 	}
 	
 	private boolean irrationalChoice()
 	{
 		if(Math.random() < SimConstants.IrrationalChance)
 		{
 			//print("I made an irrational choice.");
 			return true;
 		}
 		return false;
 	}
 	private void relapseSmoking(int numCigarettes)
 	{
 		print("I started smoking!");
 		this.isSmoker = true;
 		this.givingUp = false;
 		this.giveUpAttempts++;
 		this.smokedPerDay = numCigarettes;
 		//statusCall();
 	}
 	
 	private void giveUpSmoking()
 	{
 		print("I gave up smoking!");
 		this.isSmoker = false;
 		this.givingUp = true;
 		this.giveUpAttempts++;
 		this.smokedPerDay = 0;
 		//statusCall();
 	}
 	
 	private double changeWithinBounds(double base, double lower, double upper, double change, Operations op)
 	{
 		double result = 0;
 		double temp = 0;
 		switch(op)
 		{
 		case ADD:
 			temp = base + change;
 			break;
 		case SUBTRACT:
 			temp = base - change;
 			break;
 		case MULTIPLY:
 			temp = base * change;
 			break;
 		case DIVIDE:
 			temp = base / change;
 			break;
 		default:
 			temp = -1;
 		}
 		
 		boolean invalid = true;
 		if(temp != -1)
 		{
 			int bound = 0;
 			
 			do
 			{
 				if(temp < lower)
 				{
 					switch(op)
 					{
 					case ADD:
 						temp = base + (change * 0.5);
 						break;
 					case SUBTRACT:
 						temp = base - (change * 0.5);
 						break;
 					case MULTIPLY:
 						temp = base * (change * 0.5);
 						break;
 					case DIVIDE:
 						temp = base / (change * 0.5);
 						break;
 					default:
 						temp = -1;
 					}
 				}
 				else if(temp > upper)
 				{
 					switch(op)
 					{
 					case ADD:
 						temp = base + (change * 0.5);
 						break;
 					case SUBTRACT:
 						temp = base - (change * 0.5);
 						break;
 					case MULTIPLY:
 						temp = base * (change * 0.5);
 						break;
 					case DIVIDE:
 						temp = base / (change * 0.5);
 						break;
 					default:
 						temp = -1;
 					}
 				}
 				else
 				{
 					invalid = false;
 				}
 				bound++;
 			} while(bound < 3 && invalid == true);
 		}
 		if(!invalid)
 			return temp;
 		else 
 			return base;
 	}
 	
 	
 	
 	private void connectionAdjust(HashSet<NeighborStore> neighbors, NeighborMetrics nm, Network network)
 	{
 		//First see if we want to add anyone
 		Iterator<NeighborStore> iter = neighbors.iterator();
 		boolean canAdd = true;
 		//We want to look for similar people or role models
 		if(network.getDegree(this) > 0)
 		{
 			while(iter.hasNext())
 			{
 				NeighborStore other = iter.next();
 				double score = scoreAgainst(other.getNeighbor());
 				//System.out.println(id + ": My score against " + other.getNeighbor().getID() + " is " + score);
 				if(score > SimConstants.ConnectionScoreAddBound)
 				{
 					if(network.getInDegree(this) >= SimConstants.maxInDegree )
 					{
 						RepastEdge<BaseHuman> removalCandidate = getMinInfluence(network.getInEdges(this));
 						//lower influence nodes more likely to be binned
 						if(Math.random() < (1 - removalCandidate.getWeight()) && removalCandidate != null)
 						{
 							RepastEdge<BaseHuman> ed1 = network.getEdge(other.getNeighbor(), this);
 							if(ed1 == null /*&& Math.random() < sociable*/)
 							{
 								double nd = Distributions.getND(new NDParams((score - 0.5)*2, 0.3, 0, 1));
 								network.addEdge(other.getNeighbor(), this, nd);
 								//System.out.println(id + ": I attached to " + other.getNeighbor().getID() + " with influence " + nd /*+ " and prob " + prob + " and points " + points*/);
 								network.removeEdge(removalCandidate);
 								if(!canAdd)
 									canAdd = true;
 							}
 						}
 					}
 					else if(canAdd)
 					{
 						//also check if the influencer has reached their maxOutDegree
 						RepastEdge<BaseHuman> ed1 = network.getEdge(other.getNeighbor(), this);
 						if(ed1 == null && !other.getNeighbor().equals(this) && Math.random() < sociable && network.getOutDegree(other.getNeighbor()) < SimConstants.maxOutDegree)
 						{
 							double nd = Distributions.getND(new NDParams((score - 0.5)*2, 0.3, 0, 1));
 							network.addEdge(other.getNeighbor(), this, nd);
 							canAdd = false;
 							System.out.println(id + ": I attached to " + other.getNeighbor().getID() + " with influence " + nd /*+ " and prob " + prob + " and points " + points*/);
 						}
 					}
 					
 				}
 				else if(score < SimConstants.ConnectionScoreRemoveBound)
 				{
 					RepastEdge<BaseHuman> ed1 = network.getEdge(other.getNeighbor(), this);
 					if(ed1 != null && Math.random() < sociable)
 					{
 						network.removeEdge(ed1);
 					}
 				}
 			}
 			
 			//Also have the chance of adding a random edge with a random influence
 			//print("soc"+(sociable * 0.0001));
 			if(Math.random() < (sociable * 0.000001)  && network.getInDegree(this) < SimConstants.maxInDegree )
 			{
 				//System.out.println("Adding a random edge");
 				IndexedIterable<BaseHuman> allNodes = (IndexedIterable<BaseHuman>) context.getObjects(BaseHuman.class);
 				BaseHuman random = allNodes.get((int)Math.round(Math.random() * (allNodes.size()-1)));
 				RepastEdge<BaseHuman> ed1 = network.getEdge(random, this);
 				if(ed1 == null && random != this && random.checkCanEdgeOut())
 				{
 					double nd = Distributions.getND(new NDParams(Math.random(), 0.3, 0, 1));
 					network.addEdge(random, this, nd);
 					print("I randomly attached to " + random.getID());
 					//System.out.println(id + ": I attached to " + other.getNeighbor().getID() + " with influence " + nd /*+ " and prob " + prob + " and points " + points*/);
 				}
 			}
 		}
 		//if a node has become disconnected, reconnect it with a random chance
 		else
 		{
 			//print("I have " + network.getDegree(this) + ". Adding a random edge");
 			if(Math.random() < 0.3)
 			{
 				IndexedIterable<BaseHuman> allNodes = (IndexedIterable<BaseHuman>) context.getObjects(BaseHuman.class);
 				BaseHuman random = allNodes.get((int)Math.round(Math.random() * (allNodes.size()-1)));
 				RepastEdge<BaseHuman> ed1 = network.getEdge(random, this);
 				if(ed1 == null && random != this && random.checkCanEdgeOut())
 				{
 					double nd = Distributions.getND(new NDParams(Math.random(), 0.3, 0, 1));
 					network.addEdge(random, this, nd);
 					System.out.println(id + ": I attached to " + random.getID() + " with influence " + nd /*+ " and prob " + prob + " and points " + points*/);
 				}
 			}
 		}
 	}
 	
 	public double scoreAgainst(BaseHuman other)
 	{
 		double score = 0;
 		
 		if(this.isSmoker() == other.isSmoker())
 		{
 			score++;
 			if(this.isGivingUp() == true && this.isGivingUp() == other.isGivingUp())
 			{
 				score =+ 2;
 				if(this.getStepsSinceGiveUp() * 1.5 < other.getStepsSinceGiveUp())
 					score += 5;
 				else
 					score += 2;
 			}
 			
 						
 			if(this.isSmoker())
 			{
 				double pctSPD = pctDiff(other.getSmokedPerDay(), this.getSmokedPerDay());
 				if(pctSPD > 1)
 					score = 0;
 				else
 					score += 5 - pctSPD * 5;
 			}
 		}
 
 		double pctHealth = pctDiff(other.getHealth(), this.getHealth());
 		if(pctHealth > 1)
 			score += 0;
 		else
 			score += 5 - pctHealth * 5;
 
 		//maybe change to make influenceable people stick with those who aren't influenced?
 		score += this.influenceability * 3;
 		
 		double pctWill = pctDiff(other.willpower, this.willpower);
 		if(pctWill > 1)
 			score += 0;
 		else
 			score += 5 - pctWill * 5;
 		
 		double pctSoc = pctDiff(other.sociable, this.sociable);
 		if(pctSoc > 1)
 			score += 0;
 		else
 			score += 2 - pctSoc * 2;
 		return score/28;
 	}
 	
 	private void print(String msg)
 	{
 		System.out.println("[NODE " + this.id + "]: "+msg);
 	}
 	
 	public boolean checkCanEdgeOut()
 	{
 		if(network.getOutDegree(this) > SimConstants.maxOutDegree)
 			return false;
 		else 
 			return true;
 	}
 	
 	private RepastEdge<BaseHuman> getMinInfluence(Iterable<RepastEdge<BaseHuman>> edges)
 	{
 		
 		Iterator<RepastEdge<BaseHuman>> edgesIterator = edges.iterator();
 		RepastEdge<BaseHuman> min = null;
 		
 		while(edgesIterator.hasNext())
 		{
 			if(min == null)
 				min = edgesIterator.next();
 			else
 			{
 				RepastEdge<BaseHuman> current = edgesIterator.next();
 				if(current.getWeight() < min.getWeight())
 					min = current;
 			}
 		}
 		return min;
 	}
 	
 	
 	
 	private double pctDiff(double num1, double num2)
 	{
 		return Math.abs(num1 - num2)/num2;
 	}
 		
 
 	//Used to modify the scale free algorithm's connection probability
 	public double compatProb(BaseHuman oth)
 	{
 
 		double prob = scoreAgainst(oth);
 		//print(prob + " against " + oth.getID());
 		return prob;
 	}
 
 	public double getSociable() {
 		return sociable;
 	}
 
 	public boolean isSmoker() {
 		return isSmoker;
 	}
 
 	public void setSmoker(boolean isSmoker) {
 		this.isSmoker = isSmoker;
 	}
 
 	public double getWillpower() {
 		return willpower;
 	}
 
 	public void setWillpower(double willpower) {
 		this.willpower = willpower;
 	}
 
 	public double getHealth() {
 		return health;
 	}
 
 	public void setHealth(double health) {
 		this.health = health;
 	}
 
 	public int getSmokedPerDay() {
 		return smokedPerDay;
 	}
 
 	public void setSmokedPerDay(int smokedPerDay) {
 		this.smokedPerDay = smokedPerDay;
 	}
 
 	public String getID() {
 		return id;
 	}
 	
 	public HashMap<String, Object> generateAttrList(HashMap<String, String> keyMap)
 	{
 		HashMap<String, Object> rtnList = new HashMap<String, Object>();
 		rtnList.put(keyMap.get("isSmoker"), this.isSmoker);
 		rtnList.put(keyMap.get("willpower"), this.willpower);
 		rtnList.put(keyMap.get("health"), this.health);
 		rtnList.put(keyMap.get("smokedPerDay"), this.smokedPerDay);
 		rtnList.put(keyMap.get("givingUp"), this.givingUp);
 		rtnList.put(keyMap.get("stepsSinceGiveUp"), this.stepsSinceGiveUp);
 		rtnList.put(keyMap.get("influenceability"), this.influenceability);
 		rtnList.put(keyMap.get("sociable"), this.sociable);
 
 		
 		return rtnList;
 	}
 
 
 	@Override
 	public int compareTo(Object arg0) {
 		// TODO Auto-generated method stub
 		BaseHuman b0 = (BaseHuman) arg0;
 		
 		return this.getID().compareTo(b0.getID());
 	}
 
 }
