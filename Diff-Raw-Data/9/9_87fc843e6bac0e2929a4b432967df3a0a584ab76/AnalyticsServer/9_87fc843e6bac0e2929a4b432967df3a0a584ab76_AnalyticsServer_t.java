 package server.analytics;
 
 import java.io.FileNotFoundException;
 import java.rmi.AccessException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import client.mgmt.ManagementClientInterface;
 
 import tools.PropertiesParser;
 
 public class AnalyticsServer implements AnalyticsServerRMI{
 	
 	private long startTime;
 
 	private ArrayList<Client> clients = new ArrayList<Client>();
 	private int highestSubscriptionId = 1;
 	private ArrayList<Double> sessionTime = new ArrayList<Double>();
 	private double minSessionTime = 0;
 	private double maxSessionTime = 0;
 	private ArrayList<UserEvent> userEvents = new ArrayList<UserEvent>();
 	private double maxBidPrice = 0;
 	private ArrayList<Double> auctionTime = new ArrayList<Double>();
 	private ArrayList<Boolean> auctionSucess = new ArrayList<Boolean>();
 	private int bids = 0;
 
 	public AnalyticsServer() {
 		this.startTime = System.currentTimeMillis()/1000;
 	}
 
 	@Override
 	public String subscribe(ManagementClientInterface mClient, String filter) throws RemoteException {
 		Client client = null;
 
 		for(Client c:clients) {
 			if(c.getmClient().getId()==mClient.getId()) {
 				client = c;
 			}
 		}
 		if(client == null) {
 			client = new Client(mClient);
 			System.out.println("new Client");
 			clients.add(client);
 		}	
 
 		Subscription sub = new Subscription(highestSubscriptionId, filter, client);
 
 		if(!sub.filter.isEmpty()) {
 
 			client.getSubscriptions().put(highestSubscriptionId, sub);
 		}
 
 		int id = highestSubscriptionId;
 		System.out.println(this.clients.toString());
 
 		if(sub.filter.isEmpty()) {
 			return "Creating subscription failed!";
 		}
 
 		highestSubscriptionId++;
 		return "Created subscription with ID " + id + " for events using filter " + filter;
 	}
 
 	@Override
 	public void processEvent(Event e) throws RemoteException {
 		if(e instanceof UserEvent) {
 			createSessionTime(e);
 		}
 
 		if(e instanceof BidEvent) {
 			if(e.type.equals("BID_PLACED")) {
 				if(((BidEvent) e).getPrice()>maxBidPrice) {
 					maxBidPrice = ((BidEvent) e).getPrice();
 					StatisticsEvent se = new StatisticsEvent();
 					se.setType("BID_PRICE_MAX");
					se.setTimestamp(System.currentTimeMillis()/1000);
 					se.setValue(maxBidPrice);
 					processEvent(se);					
 				}
 				
 				bids=bids+1;
 				long now = System.currentTimeMillis()/1000;
 				long minutes = (now - startTime)/60;
 				
 				StatisticsEvent se = new StatisticsEvent();
 				se.setType("BID_COUNT_PER_MINUTE");
				se.setTimestamp(System.currentTimeMillis()/1000);
 				se.setValue(bids/minutes);
 				processEvent(se);	
 			}
 		}
 
 		if(e instanceof AuctionEvent) {
 			if(e.type.equals("AUCTION_STARTED")) {
 				auctionTime.add(((AuctionEvent) e).getDuration());
 
 				double sum = 0;
 				for(double s:auctionTime) {
 					sum = sum + s;
 				}
 				double avg = sum / auctionTime.size();
 
 				StatisticsEvent se = new StatisticsEvent();
 				se.setTimestamp(System.currentTimeMillis()/1000);
 				se.setType("AUCTION_TIME_AVG");
 				se.setValue(avg);
 				processEvent(se);
 			}
 
 			if(e.type.equals("AUCTION_ENDED")) {
 				auctionSucess.add(((AuctionEvent) e).isSuccess());
 
 				double suc = 0;
 				for(boolean s:auctionSucess) {
 					if(s==true) {
 						suc = suc +1;
 					}
 				}
 
 				StatisticsEvent se = new StatisticsEvent();
 				se.setType("AUCTION_SUCCESS_RATIO");
 				se.setTimestamp(System.currentTimeMillis()/1000);
 				se.setValue(suc/auctionSucess.size());
 				processEvent(se);
 			}
 		}
 
 		//process Event to subscribed clients
 		for(Client c:clients) {
 			Collection<Subscription> sub = c.getSubscriptions().values();
 			for(Subscription s:sub) {
 				if(s.getFilter().contains(e.type)) {
 					c.getmClient().processEvent(e);
 				}
 			}
 		}
 	}
 
 	private void createSessionTime(Event e) {
 		//create SessionTimeEvents
 
 		if(e.type.equals("USER_LOGIN")) {
 			userEvents.add((UserEvent) e);
 		}
 		if(e.type.equals("USER_LOGOUT")) {
 			for(int i = 0;i<userEvents.size();i++){					
 				if(((UserEvent) e).getUsername().equals(userEvents.get(i).getUsername())) {
 					double session = e.getTimestamp() - userEvents.get(i).getTimestamp();
 
 					if(session < minSessionTime || minSessionTime == 0) {
 						minSessionTime = session;
 						try{
 							StatisticsEvent se = new StatisticsEvent();
 							se.setType("USER_SESSIONTIME_MIN");
							se.setTimestamp(System.currentTimeMillis()/1000);
 							se.setValue(session);
 							processEvent(se);
 						} catch (RemoteException ex) {
 							System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
 							ex.printStackTrace();
 						}
 					}
 
 					if(session > maxSessionTime) {
 						maxSessionTime = session;
 						try{
 							StatisticsEvent se = new StatisticsEvent();
 							se.setType("USER_SESSIONTIME_MAX");
 							se.setValue(session);
							se.setTimestamp(System.currentTimeMillis()/1000);
 							processEvent(se);
 						} catch (RemoteException ex) {
 							System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
 							ex.printStackTrace();
 						}
 					}
 
 					sessionTime.add(session);
 					userEvents.remove(i);
 					double sum = 0;
 					for(double s:sessionTime) {
 						sum = sum + s;
 					}
 
 					double avg = sum / sessionTime.size();
 					try{
 						StatisticsEvent se = new StatisticsEvent();
 						se.setType("USER_SESSIONTIME_AVG");
 						se.setValue(avg);
						se.setTimestamp(System.currentTimeMillis()/1000);
 						processEvent(se);
 					} catch (RemoteException ex) {
 						System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
 						ex.printStackTrace();
 					}
 					return;
 				}
 			}
 		}
 	}
 
 	@Override
 	public String unsubscribe(ManagementClientInterface mClient, int id) throws RemoteException {
 		for(Client c:clients) {
 			System.out.println(c.getmClient().getId());
 			if(c.getmClient().getId()==mClient.getId()) {
 				if(c.getSubscriptions().containsKey(id)) {
 					c.getSubscriptions().remove(id);
 					System.out.println("Remove");
 					return "subscription " + id + " terminated";
 				}
 			}
 		}
 
 		return "unsubscribe failed";
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		if (args.length != 1) {
 			System.err.println("Invalid arguments!");
 			System.err.println("USAGE: java AnalyticsServer <RMIBindingName>");
 		} else {
 			String bindingName = args[0];
 			PropertiesParser ps;
 			Registry registry = null;
 			try {
 				ps = new PropertiesParser("registry.properties");
 				int registryPort = Integer.parseInt(ps.getProperty("registry.port"));
 				registry = LocateRegistry.createRegistry(registryPort);
 			} catch (FileNotFoundException e) {
 				System.err.println("Properties file couldn't be found!");
 				e.printStackTrace();
 			} catch (RemoteException e) {
 				System.err.println("Couldn't create Registry.");
 				e.printStackTrace();
 			}
 			AnalyticsServerRMI as = new AnalyticsServer();
 
 			AnalyticsServerRMI ras = null;
 			try {
 				ras = (AnalyticsServerRMI) UnicastRemoteObject.exportObject(as, 0);
 			} catch (RemoteException e) {
 				System.err.println("Error exporting the remote object!");
 				e.printStackTrace();
 			}
 			try {
 				registry.rebind(bindingName, ras);
 			} catch (AccessException e) {
 				System.err.println("Access Error binding the remote object to registry!");
 				e.printStackTrace();
 			} catch (RemoteException e) {
 				System.err.println("Error binding the remote object to registry!");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public double getMin() throws RemoteException {
 		return minSessionTime;
 	}
 }
