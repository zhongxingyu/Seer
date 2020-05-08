 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Stack;
 import java.util.EmptyStackException;
 import java.lang.Math;
 
 
 public class Driver {
 	private double globalAvg = 0;
 	private double globalTotal = 0;
 	private double lastAvg = 0;
 	public int migrations = 0;
 	public int opinion_changes = 0;
 	private ArrayList<Agent> agents;
 	private ArrayList<Group> groups;
 	private ArrayList<Group> deletes = new ArrayList<Group>();
 	Random gen = new Random();
 	Constants Constants;
 	boolean verbose;
 	Graph graph;
 	double threshold;
 	int runcount;
 	boolean run;
 	double tickavg;
 	int ticks;
 	int groupnum = 0;
 	
 	public void init(){
 		run = true;
 		runcount = 0;
 		globalAvg = 0;
 		lastAvg = 0;
 		globalTotal = 0;
 		migrations = 0;
 		opinion_changes = 0;
 		threshold = Constants._epsilon * Constants._mu / 10;
 		agents = new ArrayList<Agent>();
 		groups = new ArrayList<Group>();
                 agents.clear();
                 groups.clear();
 		for(int i = 0; i < Constants._groups; i++){
 			Group group = new Group("G-"+i);
 			groups.add(group);
 			groupnum++;
 		}
 		for(int i = 0; i < Constants._numnodes; i++){
 			if(Constants._murand){
 				Agent agent = new Agent(i, "A-" +i, gen.nextDouble(), randMu());
 				agents.add(agent);
 			}
 			else if(Constants.muCheck)
 			{
 				Agent agent = new Agent(i, "A-" +i, gen.nextDouble(), Constants.muIncS);
 				agents.add(agent);
 			}
 			else{
 				Agent agent = new Agent(i, "A-" +i, gen.nextDouble(), Constants._mu);
 				agents.add(agent);
 			}
 		}
 		for(int i = 0; i < agents.size(); i++){
 			int g = gen.nextInt(groups.size());
 			agents.get(i).setGroup(groups.get(g));
 			groups.get(g).getAgents().add(agents.get(i));
 		}
 		for(Group g : groups){
 			g.calcavg();
 		}
 		graph = new Graph(agents, groups, Constants);
 		graph.Constants = this.Constants;
 	}
 	
 	private double randMu() {
		return (gen.nextDouble() * (Constants.randMuEnd - Constants.randMuStart)) + Constants.randMuStart;
 	}
 
 	public void reset(){
 		agents.clear();
 		groups.clear();
 		this.init();
 	}
 	
 	public ArrayList<Agent> returnAgents(){
 		return agents;
 	}
 	
 	public ArrayList<Group> returnGroups(){
 		return groups;
 	}
 	
 	public void run(){
 		int tMigrations; //number of migrations made each time interal
 		int tOpinionChange; //opinion changes made in one time interval
 		double tOpinionDifference; //total amount that opinions have been changed
 		ticks = 0;
 		
 		 do{
 			tMigrations = 0;
 			tOpinionChange = 0;
 			tOpinionDifference = 0;
 			ticks++;
 			for(Agent a : agents) {
 				Agent hold = a;
 				if(hold.getGroup().getAgents().size() + hold.getNumExternalNeighbors() > 1){
 					Agent neighbor = hold.nSelection();
 					if(Math.abs(hold.getOpinion() - neighbor.getOpinion()) < Constants._epsilon) {
 						Double dub = hold.getOpinion();
 						hold.setOpinion(neighbor.getOpinion());
 						tOpinionDifference += Math.abs(dub - hold.getOpinion());
 						neighbor.setOpinion(dub);
 						
 						tOpinionChange++;
 						globalTotal += hold.getOpinion();
 					}
 					else if(Constants.Repulsive){
 						if(Math.abs(hold.getOpinion() - neighbor.getOpinion()) > (Constants._epsilon + (Constants._epsilon * Constants.repuslivePer / 100))){
 							Double dub = hold.getOpinion();
 							hold.redOpinion(neighbor.getOpinion());
 							tOpinionDifference += Math.abs(dub - hold.getOpinion());
 							neighbor.redOpinion(dub);
 							tOpinionChange++;
 							globalTotal+= hold.getOpinion();
 						}
 					}
 				}
 			}
 			opinion_changes += tOpinionChange;
 			
 			tOpinionDifference /= tOpinionChange;
 			
 			if(Constants.migrateSwitch){
 				tMigrations = migrate();
 			}
 			/*if(verbose && ticks % 100 == 0){
 				System.out.print(
 									"Tick " + ticks + "\n"
 									+ groups.get(1).getAgents().size() + "\n"
 								    + "Number of Migrations: " + tMigrations + "\n"
 								    + "Average Opinion Change: " + tOpinionDifference + "\n"
 								    + "Total Migrations: " + migrations + "\n"
 				//				    + "Runcount: " + runcount + "\n"
 				);
 			}//*/
 			
 	        lastAvg = globalAvg;
 	        globalAvg = globalTotal / agents.size();
 	        globalTotal = 0;
 	        for(Group g : groups){
 	        	g.calcavg();
 	        }
 	        if(Constants.DynamicGroups){
 	        	deletes.clear();
 	        	for(Group g : groups){
 	        		if(g.getAgents().size() < 1){
 	        			deletes.add(g);
 	        		}
 	        	}
 	        	for(Group g : deletes){
 	        		groups.remove(g);
 	        	}
 	        }
 		} while((tOpinionDifference) > threshold && runcount < 10 && !Constants.debug);
 	}
 	
     public int migrate() {
     	int numMigrations = 0;
     	for(Agent a : agents) {
     		if(Math.abs(a.getOpinion() - a.getGroup().getavg()) > this.Constants._epsilon || a.getGroup().getAgents().size() < Constants.minAgents) {
     			boolean changed = false;
     			Group holdG = null;
     			double probRoll = gen.nextDouble();
                 a.calcEx();
     			for(Group b : a.exGroups) {
     				if(!changed){
     					double prob1 = 1 - Math.abs(a.getOpinion() - b.getavg()); // holder for calculating probability
     					double prob2 = prob1 / a.prob(); // actual probability
     					if(prob2 >= probRoll){
     						holdG = b;
     						changed = true;
     						migrations++;
     						break;
     					}
     				}
     			}
     			
     			if(changed) {
     				graph.migrateAgentToGroup(a, holdG);
     				numMigrations++;
     			}
     			else{
     				if(Constants.DynamicGroups){
 	    				if(probRoll <= Math.abs(a.getOpinion() - a.getGroup().getavg())){
 		    				Group group = new Group("G-"+groupnum);
 		    				groups.add(group);
 		    				groupnum++;
 		    				graph.migrateAgentToGroup(a, group);
 	    				}
     				}
     			}
     		}
     	}
     	return numMigrations;
     }
     		
     
     public double returnAvg(){
         return globalAvg;
     }
     
     public Graph returnGraph() {
         return graph;
     }
     public int getnumGroups(){
     	return groups.size();
     }
 }
 
