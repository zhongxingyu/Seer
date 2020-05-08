 package server.auction;
 
 import java.io.FileNotFoundException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.ConcurrentModificationException;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
import server.analytics.AnalyticsServerRMI;
 import server.analytics.AuctionEvent;
 import server.analytics.BidEvent;
 import server.billing.BillingServerRMI;
 import server.billing.BillingServerSecure;
 import tools.PropertiesParser;
 
 public class Cron implements Runnable
 {
 	private AuctionServer main = null;
 	private Timer timer = null;
 	private Task task = null;
 	private String billingBindingName = "";
 
 	public Cron(AuctionServer main, String billingBindingName) {
 		this.main = main;
 		this.billingBindingName = billingBindingName;
 	}
 
 	public void run() {
 		timer = new Timer();
 		task = new Task();
 		timer.schedule(task, 0, 500);
 	}
 
 	public void cancel() {
 		task.cancel();
 		timer.cancel();
 	}
 
 	public class Task extends TimerTask {
 		public void run() {
 			try {
 				for (Auction a : main.auctions) {
 					if (new Date().after(a.getDate())) {
 						BillingServerRMI bs = null;
 						BillingServerSecure bss = null;
						AnalyticsServerRMI as = null;
 						PropertiesParser ps = null;
 						Registry reg = null;
 						try {
 							ps = new PropertiesParser("registry.properties");
 							int portNr = Integer.parseInt(ps.getProperty("registry.port"));
 							String host = ps.getProperty("registry.host");
 							reg = LocateRegistry.getRegistry(host, portNr);
 							bs = (BillingServerRMI) reg.lookup(billingBindingName);
 							bss = (BillingServerSecure) bs.login("auctionServer", "auctionServer");
							as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
 						} catch (FileNotFoundException e) {
 							System.err.println("properties file not found!");
 						} catch (NumberFormatException e) {
 							System.err.println("Port non-numeric!");
 						} catch (RemoteException e) {
 							System.err.println("Registry couln't be found!");
 						} catch (NotBoundException e) {
 							System.err.println("Remote object couldn't be found!");
 							e.printStackTrace();
 						}
 						String username = "";
 						if (a.getHighestBidder() == null)
 							username = "noone";
 						else
 							username = a.getHighestBidder().getUsername();
 						try {
 							bss.billAuction(username, (long)a.getId(), a.getHighestBid());
 						} catch (RemoteException e) {
 							System.err.println("Error: Couldn't bill auction! BillingServer may be down!");
 						}
 
 						try {
 							BidEvent be = new BidEvent();
 							be.setType("BID_WON");
 							be.setUsername(a.getHighestBidder().getUsername());
 							be.setAuctionId(a.getId());
 							be.setPrice(a.getHighestBid());
 							as.processEvent(be);
 
 							AuctionEvent ae = new AuctionEvent();
 							ae.setType("AUCTION_ENDED");
 							ae.setAuctionID(a.getId());
 							if(a.getHighestBidder()!=null) {
 								ae.setSuccess(true);
 							}
 							as.processEvent(ae);
 						} catch (RemoteException e) {
 							System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
 							e.printStackTrace();
 						}
 
 						main.auctions.remove(a);
 						/*********************
 						 * no UDP in Lab 2
 						 *********************
 						if (a.getHighestBidder() != null) {
 							String message = "!auction-ended " + a.getHighestBidder().getUsername() + "  " + a.getHighestBid() + " " + a.getDescription();
 							main.sendNotification(a.getHighestBidder(), message);
 							main.sendNotification(a.getOwner(), message);
 						} else {
 							String message = "!auction-ended noone " + a.getHighestBid() + " " + a.getDescription();
 							main.sendNotification(a.getOwner(), message);
 						}
 						 */
 					}
 				} 
 			} catch (ConcurrentModificationException e) {
 				;
 			}
 		}
 	}
 }
