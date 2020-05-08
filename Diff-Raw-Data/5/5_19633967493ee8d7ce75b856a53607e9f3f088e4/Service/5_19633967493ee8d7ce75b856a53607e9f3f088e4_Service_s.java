 package data;
 
 import interfaces.NetworkService;
 import interfaces.WebService;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import states.StateManager;
 
 import config.GuiConfig;
 import data.UserProfile.UserRole;
 
 /**
  * If user field is null, then this offer is inactive, otherwise it's active. If
  * time is 0, this offer is expired. If price is 0, this offer is a demand,
  * otherwise it's supply.
  * 
  * A service that have one of the statuses : TRANSFER_* should have only one
  * user entry, he will be the user who accepted the transfer, the rest of the
  * users specified in <code>users</code> field will be ignored.
  * 
  * @author Ghennadi Procopciuc
  * @author Paul Vlase
  * @see Status
  */
public class Service implements Comparable<Service> {
 	private String name;
 	private long time;
 	private double price;
 	private ArrayList<UserEntry> users;
 	private Status status;
 
 	private StateManager stateMgr;
 
 	/**
 	 * This field will be used only with a TRANSFER_* status, otherwise it means
 	 * nothing.
 	 */
 	private int progress;
 
 	/**
 	 * Service status :
 	 * 
 	 * INACTIVE - if no one offers this service
 	 * 
 	 * ACTIVE - if someone offers this service
 	 * 
 	 * TRANSFER_* - Transaction statuses
 	 * 
 	 */
 	public enum Status {
 		ACTIVE, INACTIVE, TRANSFER_STARTED, TRANSFER_IN_PROGRESS, TRANSFER_COMPLETE, TRANSFER_FAILED, DROP
 	};
 
 	public Service() {
 	}
 
 	public Service(String name, ArrayList<UserEntry> users, Status status) {
 		this.name = name;
 		this.users = users;
 		this.time = 0;
 		this.price = 0;
 		this.status = status;
 		this.stateMgr = new StateManager();
 	}
 
 	public Service(Service service) {
 		this.name = service.getName();
 		this.users = service.getUsers();
 		this.time = service.getTime();
 		this.price = service.getPrice();
 		this.progress = service.getProgress();
 		this.status = service.getStatus();
 		this.stateMgr = service.getStateMgr();
 	}
 
 	public Service(String serviceName) {
 		this(serviceName, null, Status.INACTIVE);
 	}
 
 	public StateManager getStateMgr() {
 		return stateMgr;
 	}
 
 	/**
 	 * Note, if <code>status</code> is not <code>Status.INACTIVE</code>, after
 	 * instantiation you have to add at least one
 	 * <code>UserEntry<code>, otherwise you will get unpredictable results
 	 * 
 	 * @param serviceName
 	 *            Name of the service
 	 * @param status
 	 *            Service/Transaction status
 	 * @see Status
 	 */
 	public Service(String serviceName, Status status) {
 		this(serviceName, null, status);
 	}
 
 	public void executeNet(NetworkService net) {
 		stateMgr.executeNet(net);
 	}
 
 	public void executeWeb(WebService web) {
 		stateMgr.executeWeb(web);
 	}
 
 	public void executeGui() {
 	}
 
 	public boolean isInactiveState() {
 		return stateMgr.isInactiveState();
 	}
 
 	public void addUserEntry(UserEntry user) {
 		if (users == null) {
 			users = new ArrayList<UserEntry>();
 		}
 
 		users.add(user);
 	}
 
 	public ArrayList<UserEntry> getUsers() {
 		return users;
 	}
 	
 	public UserEntry getUser(String username){
 		for (UserEntry user : users) {
 			if(user.getUsername().equals(username)){
 				return user;
 			}
 		}
 		
 		return null;
 	}
 
 	public void setUsers(ArrayList<UserEntry> users) {
 		this.users = users;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String serviceName) {
 		this.name = serviceName;
 	}
 
 	public long getTime() {
 		return time;
 	}
 
 	public void setTime(long time) {
 		this.time = time;
 	}
 
 	public double getPrice() {
 		return price;
 	}
 
 	public Status getStatus() {
 		return status;
 	}
 
 	public void setStatus(Status status) {
 		this.status = status;
 	}
 
 	public void setPrice(double price) {
 		this.price = price;
 	}
 
 	public int getProgress() {
 		return progress;
 	}
 
 	public void setProgress(int progress) {
 		this.progress = progress;
 	}
 
 	public ArrayList<ArrayList<Object>> getAsTable() {
 		return getAsTable(UserRole.BUYER);
 	}
 
 	private ArrayList<Object> getActiveRow(UserRole role, UserEntry user) {
 		ArrayList<Object> row = new ArrayList<Object>();
 
 		switch (user.getOffer()) {
 		case TRANSFER_STARTED:
 			row = new ArrayList<Object>(Arrays.asList("", "", 0, "", "", ""));
 			break;
 		case TRANSFER_COMPLETE:
 		case TRANSFER_IN_PROGRESS:
 			row = new ArrayList<Object>(Arrays.asList("", "",
 					user.getProgress(), "", "", ""));
 			break;
 		default:
 			row = new ArrayList<Object>(Arrays.asList("", "", user.getName(),
 					user.getOffer(), user.getTime(), user.getPrice()));
 			break;
 		}
 
 		return row;
 	}
 
 	public ArrayList<ArrayList<Object>> getAsTable(UserRole role) {
 		ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();
 		Boolean first = true;
 
 		/**
 		 * Columns from row are : Service Name, Status, User, Offer made, Time,
 		 * Price
 		 */
 		ArrayList<Object> row;
 
 		switch (status) {
 		case INACTIVE:
 			row = new ArrayList<Object>(Arrays.asList(getName(),
 					GuiConfig.getValue(GuiConfig.INACTIVE), "", "", "", ""));
 			data.add(row);
 			break;
 		case ACTIVE:
 			if (users == null) {
 				row = new ArrayList<Object>(Arrays.asList(getName(),
 						GuiConfig.getValue(GuiConfig.ACTIVE), "", "", "", ""));
 				data.add(row);
 				break;
 			}
 
 			for (UserEntry user : users) {
 
 				row = getActiveRow(role, user);
 				if (first) {
 					first = false;
 
 					row.set(0, getName());
 					row.set(1, GuiConfig.getValue(GuiConfig.ACTIVE));
 				}
 
 				data.add(row);
 			}
 			break;
 		case TRANSFER_IN_PROGRESS:
 			if (role == UserRole.BUYER) {
 				System.out
 						.println("[Bag ceva in ea de viata]]]]]]]]] progress = "
 								+ progress);
 				row = new ArrayList<Object>(Arrays.asList(getName(),
 						GuiConfig.getValue(GuiConfig.TRANSFER_IN_PROGRESS),
 						progress, "", "", ""));
 				data.add(row);
 				break;
 			} else {
 				// TODO
 			}
 		case TRANSFER_STARTED:
 			if (role == UserRole.BUYER) {
 				row = new ArrayList<Object>(Arrays.asList(getName(),
 						GuiConfig.getValue(GuiConfig.TRANSFER_STARTED), 0, "",
 						"", ""));
 				data.add(row);
 				break;
 			} else {
 				// TODO
 			}
 		case TRANSFER_COMPLETE:
 			if (role == UserRole.BUYER) {
 				row = new ArrayList<Object>(Arrays.asList(getName(),
 						GuiConfig.getValue(GuiConfig.TRANSFER_COMPLETE),
 						progress, "", "", ""));
 				data.add(row);
 				break;
 			} else {
 				// TODO
 			}
 		case TRANSFER_FAILED:
 			// TODO
 			break;
 		default:
 			System.err.println("[Service, getAsTable] Unexpected Status :|");
 			break;
 		}
 
 		return data;
 	}
 
 	public Service clone() {
 		return new Service(this);
 	}
 
 	@Override
 	public boolean equals(Object arg0) {
 		if (!(arg0 instanceof Service)) {
 			return false;
 		}
 
 		return ((Service) arg0).getName().equals(name);
 	}
 
 	@Override
 	public String toString() {
 		return "" + name + " " + users;
 	}
 
 	@Override
 	public int compareTo(Service o) {
 		return getName().compareTo(o.getName());
 	}
 
 	public void setInactiveState() {
 		stateMgr.setInactiveState();
 	}
 
 	public void setAccceptOfferState(Integer userIndex) {
 		stateMgr.setAcceptOfferState(this, userIndex);
 	}
 
 	public void setDropAuctionState() {
 		stateMgr.setDropAuctionState(this);
 	}
 
 	public void setDropOfferState() {
 		stateMgr.setDropOfferState(this);
 	}
 
 	public void setLaunchOfferState() {
 		stateMgr.setLaunchOfferState(this);
 	}
 
 	public void setRefuseOfferState(Integer userIndex) {
 		stateMgr.setRefuseOfferState(this, userIndex);
 	}
 
 	public void setRemoveOfferState() {
 		stateMgr.setRemoveOfferState(this);
 	}
 
 	public void setMakeOfferState(Integer userIndex, Double price) {
 		stateMgr.setMakeOfferState(this, userIndex, price);
 	}
 }
