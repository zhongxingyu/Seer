 package factory;
 
 import java.util.ArrayList;
 import agent.Agent;
 import java.util.*;
 
 
 import factory.interfaces.*;
 import factory.masterControl.MasterControl;
 
 public class GantryAgent extends Agent implements Gantry {
 	public GantryAgent(MasterControl mc) {
 		super(mc); // needed for the server 
 
 	}
 	// *** DATA ***
 
 	//This is an list of myBins object that keeps track of the bins that the feeders need
	public List<MyBin> myBins = Collections.synchronizedList(new ArrayList<MyBin>());   
 
 	//enum to keep track of the myBin object state.
 	enum MyBinState { NEEDED, DELIVERED}
 
 	//boolean to make unit testing work without trying to use animation
 	public boolean unitTesting = false;
 	
 	/**
 	 * Private class that keeps track of a requested bin, and which part is needed, and which feeder it goes to
 	 */
 	public class MyBin {
 
 		public MyBinState state;
 		public Part pt;
 		public Feeder fdr;
 
 		/**
 		 * Constructor for MyBin object.
 		 * @param part Type of part needed.
 		 * @param feeder The feeder the parts are sent to.
 		 */
 		public MyBin(Part part, Feeder feeder){
 			this.state = MyBinState.NEEDED;
 			this.fdr = feeder;
 			this.pt = part;
 		}
 	}
 
 	// *** MESSAGES ***
 
 	/**
 	 * This is a message that comes from a feeder that requests parts
 	 */
 	public void msgFeederNeedsPart(Part part, Feeder feeder) {
 		myBins.add(new MyBin(part, feeder));
 		stateChanged();
 	}
 
 
 
 	// *** SCHEDULER ***
 
 	public boolean pickAndExecuteAnAction() {
 		System.out.println("gantry scheduler called");
		synchronized(myBins)
		{
			for(MyBin b: myBins){
				if(b.state == MyBinState.NEEDED){
					goFetchTheRequestedBin(b);
					return true;
				}
 			}
 		}
 		return false;
 	}
 
 
 	// *** ACTIONS ***
 
 	/**
 	 * This method tells the gantry to go fetch a bin.
 	 * @param b This is the bin that the gantry is going to go fetch.
 	 */
 	private void goFetchTheRequestedBin(MyBin b) {
		//debug("goFetchTheRequestedBin");
 		if (b.fdr.getFeederHasABinUnderneath() == true)
 			DoPickupPurgeBin(b); //animation message
 
 		DoGoGetNewBin(b);
 		DoBringNewBin(b);
 
 		b.fdr.msgHereAreParts(b.pt);
 		myBins.remove(b);
 		stateChanged();
 	}
 
 	// *** ANIMATION METHODS ***
 
 	/**
 	 * This is an animation method that tells the gantry to go pick up a bin and place it under a feeder that current
 	 * doesn't have a bin.
 	 * @param b The bin that is requested.
 	 */
 	private void DoPickupPurgeBin(MyBin b) {
 		print("Picking up Purge Bin");
 		if(!unitTesting){
 			server.command("ga fpm cmd pickuppurgebin " + b.fdr.getFeederNumber());
 			try{
 				animation.acquire();
 			}
 			catch (InterruptedException e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * This is a method that tells the gantry to get the bin if there is already a bin underneath the feeder
 	 * @param b The bin that is requested.
 	 */
 	private void DoGoGetNewBin(MyBin b) {
 		print("Going to get new Bin");
 		if(!unitTesting){
 			server.command("ga fpm cmd getnewbin " + b.pt.name);
 			try{
 				animation.acquire();
 			}
 			catch (InterruptedException e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * This is a method that tells the gantry to come back and drop off the bin if there is already a bin underneath the feeder
 	 * @param b The bin that is requested.
 	 */
 	private void DoBringNewBin(MyBin b) {
 		print("Bringing new Bin to the feeder");
 		if(!unitTesting){
 			server.command("ga fpm cmd bringbin " + b.fdr.getFeederNumber());
 			try{
 				animation.acquire();
 			}
 			catch (InterruptedException e){
 				e.printStackTrace();
 			}
 		}
 	}
 }
 
 
 
 
