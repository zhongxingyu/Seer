 package states;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 import data.Message;
 import data.Service;
 import interfaces.MediatorNetwork;
 import interfaces.NetworkMediator;
 import interfaces.NetworkService;
 import interfaces.WebService;
 
 public class StateManager implements Serializable {
 	private static final long	serialVersionUID	= 1L;
 	static Logger				logger				= Logger.getLogger(RefuseOfferState.class);
 
 	private EnabledState		enabledState;
 	private MakeOfferState		makeOfferState;
 	private RemoveOfferState	removeOfferState;
 	private DropAuctionState	dropAuctionState;
 	private RefuseOfferState	refuseOfferState;
 	private AcceptOfferState	acceptOfferState;
 
 	private LaunchOfferState	launchOfferState;
 	private DropOfferState		dropOfferState;
 
 	protected State				currentState;
 
 	public StateManager(Service service) {
 		enabledState = new EnabledState(service);
 		makeOfferState = new MakeOfferState(service);
 		removeOfferState = new RemoveOfferState(service);
 		dropAuctionState = new DropAuctionState(service);
 		refuseOfferState = new RefuseOfferState(service);
 		acceptOfferState = new AcceptOfferState(service);
 		launchOfferState = new LaunchOfferState(service);
 		dropOfferState = new DropOfferState(service);
 
 		currentState = enabledState;
 	}
 
 	public StateManager(StateManager mgr) {
 		enabledState = mgr.enabledState.clone();
 		makeOfferState = mgr.makeOfferState.clone();
 		removeOfferState = mgr.removeOfferState.clone();
 		dropAuctionState = mgr.dropAuctionState.clone();
 		refuseOfferState = mgr.refuseOfferState.clone();
 		acceptOfferState = mgr.acceptOfferState.clone();
 		launchOfferState = mgr.launchOfferState.clone();
 		dropOfferState = mgr.dropOfferState.clone();
 
 		if (mgr.currentState == mgr.enabledState) {
 			currentState = enabledState;
 		} else if (mgr.currentState == mgr.makeOfferState) {
 			currentState = makeOfferState;	
 		} else if (mgr.currentState == mgr.removeOfferState) {
 			currentState = removeOfferState;
 		} else if (mgr.currentState == mgr.dropAuctionState) {
 			currentState = dropAuctionState;
 		} else if (mgr.currentState == mgr.refuseOfferState) {
 			currentState = refuseOfferState;
 		} else if (mgr.currentState == mgr.acceptOfferState) {
 			currentState = acceptOfferState;
 		} else if (mgr.currentState == mgr.launchOfferState) {
 			currentState = launchOfferState;
 		} else if (mgr.currentState == mgr.dropOfferState) {
 			currentState = dropOfferState;
 		}
 	}
 
 	public void setEnabledState() {
 		enabledState.updateState();
 		currentState = enabledState;
 	}
 
 	public void setMakeOfferState(Integer userIndex, Double price) {
 		makeOfferState.updateState(userIndex, price);
 		currentState = makeOfferState;
 	}
 
 	public void setRemoveOfferState() {
 		removeOfferState.updateState();
 		currentState = removeOfferState;
 	}
 
 	public void setDropAuctionState() {
 		dropAuctionState.updateState();
 		currentState = dropAuctionState;
 	}
 
 	public void setRefuseOfferState(Integer userIndex) {
 		refuseOfferState.updateState(userIndex);
 		currentState = refuseOfferState;
 	}
 
 	public void setAcceptOfferState(Integer userIndex) {
 		acceptOfferState.updateState(userIndex);
 		currentState = acceptOfferState;
 
 		System.out.println("[StateManager: setAcceptOfferState]");
 	}
 
 	public void setLaunchOfferState() {
 		launchOfferState.updateState();
 		currentState = launchOfferState;
 		System.out.println("[StateManager: setLaunchOfferState]");
 	}
 
 	public void setDropOfferState() {
 		dropOfferState.updateState();
 		currentState = dropOfferState;
 		System.out.println("[StateManager:setDropOfferState()]");
 	}
 
 	public String getName() {
 		return currentState.getName();
 	}
 
 	public boolean isEnabledState() {
 		return currentState == enabledState;
 	}
 
 	public void executeNet(NetworkService net) {
 		currentState.executeNet(net);
 	}
 
 	public void executeWeb(WebService web) {
 		System.out.println("[StateManager: executeWeb] State : " + currentState.getClass());
 		currentState.executeWeb(web);
 	}
 
 	public ArrayList<Message> asMessages(NetworkService net) {
 		return currentState.asMessages(net);
 	}
 
 	public String getStateName() {
 		return currentState.getName();
 	}
	
	public void setService(Service service) {
		enabledState.setService(service);
		makeOfferState.setService(service);
		removeOfferState.setService(service);
		dropAuctionState.setService(service);
		refuseOfferState.setService(service);
		acceptOfferState.setService(service);
		launchOfferState.setService(service);
		dropOfferState.setService(service);
	}
 
 	public StateManager clone() {
 		return new StateManager(this);
 	}
 }
