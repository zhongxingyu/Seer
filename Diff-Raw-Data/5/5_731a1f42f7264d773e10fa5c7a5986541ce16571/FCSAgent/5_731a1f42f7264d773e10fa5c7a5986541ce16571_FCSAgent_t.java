 package agent;
 
 import java.util.ArrayList;
 
 import DeviceGraphics.DeviceGraphics;
 import Utils.Constants;
 import agent.data.Bin;
 import agent.interfaces.Conveyor;
 import agent.interfaces.FCS;
 import agent.interfaces.Gantry;
 import agent.interfaces.Nest;
 import agent.interfaces.PartsRobot;
 import agent.interfaces.Stand;
 import factory.Order;
 import factory.PartType;
 
 /**
  * Unused in V0
  * @author Daniel Paje, Michael Gendotti
  */
 public class FCSAgent extends Agent implements FCS { 
 	
 	private Stand stand;    
 	private PartsRobot partsRobot;    
 	private Gantry gantry;    
 	private ArrayList<Nest> nests;    
 	private Conveyor conveyor;
 	private myState state;    
 	private ArrayList<Order> orders;  
 	private int numOrdersFinished=0;
 	
 	private factory.FCS fcs;
 	
 	private final String name;
 	
 	private boolean binsSet;
 	private ArrayList<PartType> binsToAdd;
 
 	public enum myState {PENDING, STARTED, LOADED};    
 	
 	public FCSAgent(String name){
 		super();
 		this.name=name;
 		this.nests=new ArrayList<Nest>();
 		this.orders=new ArrayList<Order>();
 		binsSet=false;
 		binsToAdd= new ArrayList<PartType>();
 		state=myState.STARTED;
 	}
 	
 	public FCSAgent(){
 		super();
 		this.name="FCS Agent";
 		binsSet=false;
 		binsToAdd= new ArrayList<PartType>();
 		state=myState.STARTED;
 	}
 
 	@Override
 	public void msgAddKitsToQueue(Order o){   
 		print("Received new order");
 	    orders.add(o); 
 	    if(fcs!=null){
 	    	fcs.updateQueue();
 	    }
 	    stateChanged();
 	}    
 	
 	@Override
 	public void msgStopMakingKit(Order o){    
 	    for(Order order: orders){    
 	        if(order.equals(o)){
 	        	o.cancel=true;
 	        	if(fcs!=null){
 	        		fcs.updateQueue();
 	        	}
 	        }
 	    }    
 	    stateChanged();
 	}
 	
 	@Override
 	public void msgStartProduction(){    
 	    state=myState.STARTED;    
 	    stateChanged();
 	}    
 	
 	@Override
 	public void msgAddNewPartType(PartType part) {
 		binsToAdd.add(part);
 		stateChanged();
 	}
 
 	@Override
 	public void msgOrderFinished(){  
 		numOrdersFinished++;
 		print("Order " + numOrdersFinished + " Done!!!!");
 		for(Order o:orders){
 			if(o.state == Order.orderState.ORDERED){
 				orders.remove(o);
 				if(fcs!=null){
 					fcs.updateQueue();
 				}
 				break;
 			}
 		}
 	    state=myState.STARTED;    
 	    stateChanged();
 	}    
 
 	@Override
 	public boolean pickAndExecuteAnAction(){
 		print("I'm scheduling stuff");
 		if(state==myState.STARTED){
			if(!binsSet && gantry!=null){
 				initializeBins();
 				return true;
 			}
			if(binsToAdd.size()>0 && gantry!=null){
 				addBin();
 				return true;
 			}
 		    if(!orders.isEmpty()){   
 		    	for(Order o:orders){
 					if(o.cancel){
 						cancelOrder(o);
 						return true;
 					}
 				}
 		        for(Order o:orders){    
 		            if(o.state==Order.orderState.PENDING){    
 		                placeOrder(o);  
 		                return true;
 		            }    
 		        }    
 		    }    
 		}
 		return false;
 	}
 	
 	public void placeOrder(Order o){  
 		print("Placing Order");
 	    o.state=Order.orderState.ORDERED;    
 	    state=myState.LOADED;   
 	    if(fcs!=null){
 	    	fcs.updateQueue();
 	    }
 	    
 	    conveyor.msgHereIsKitConfiguration(o.kitConfig);
 	    stand.msgMakeKits(o.numKits);    
 	    
 	    partsRobot.msgHereIsKitConfiguration(o.kitConfig);   
 	    
 	    /*for(PartType type:o.kitConfig.getConfig().keySet())    
 	    {    
 	    	gantry.msgHereIsBinConfig(new Bin(o.parts.get(i),i+1));  
 	    }  */
 	    int k=0;
 	    for(PartType type:o.kitConfig.getConfig().keySet()){ 
 	    	for(int i=0;i<o.kitConfig.getConfig().get(type);i++){
 	    		nests.get(k).msgHereIsPartType(type);  
 	    		k++;
 	    	}
 	    }     
 	    stateChanged();
 	}    
 	
 	public void cancelOrder(Order o){
 		if(o.state==Order.orderState.ORDERED){
 			//stand.msgStopMakingTheseKits(o.parts);
 			orders.remove(o);
 		} else {
 			orders.remove(o);
 		}
 		if(fcs!=null){
 			fcs.updateQueue();
 		}
 		stateChanged();
 	}
 	
 	public void initializeBins(){
 		print("Messaging gantry about default bins");
 		for(int i=0;i<Constants.DEFAULT_PARTTYPES.size();i++){
 			gantry.msgHereIsBin(new Bin(Constants.DEFAULT_PARTTYPES.get(i),i));
 		}
 		binsSet=true;
 		stateChanged();
 	}
 	
 	public void addBin(){
 		for(int i=binsToAdd.size()-1;i>=0;i--){
 			gantry.msgHereIsBin(new Bin(binsToAdd.get(i),Constants.DEFAULT_PARTTYPES.size()-i));
 			binsToAdd.remove(i);
 		}
 		stateChanged();
 	}
 
 	public void setStand(Stand stand){
 		this.stand=stand;
 	}
 	
 	public void setPartsRobot(PartsRobot partsRobot){
 		this.partsRobot=partsRobot;
 	}
 	
 	public void setGantry(Gantry gantry){
 		this.gantry=gantry;
 	}
 	
 	public void setConveyor(Conveyor conveyor){
 		this.conveyor=conveyor;
 	}
 	
 	public void setNest(Nest nest){
 		this.nests.add(nest);
 	}
 	
 	public void setNests(ArrayList<Nest> nests){
 		this.nests=nests;
 	}
 	
 	public Stand getStand(){
 		return stand;
 	}
 	
 	public PartsRobot getPartsRobot(){
 		return partsRobot;
 	}
 	
 	public Gantry getGantry(){
 		return gantry;
 	}
 	
 	public Conveyor getConveyor(){
 		return conveyor;
 	}
 	
 	public ArrayList<Nest> getNests(){
 		return nests;
 	}
 	
 	public String getName(){
 		return name;
 	}
 
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics dg) {
 		//fcsGraphics=(fcsGraphics) dg;
 	}
 	
 	public DeviceGraphics getGraphicalRepresentation() {
 		//return fcsGraphics;
 		return null;
 	}
 	
 	public ArrayList<Order> getOrders(){
 		return orders;
 	}
 	
 	public void setFCS(factory.FCS fcs){
 		this.fcs=fcs;
 	}
 
 }
