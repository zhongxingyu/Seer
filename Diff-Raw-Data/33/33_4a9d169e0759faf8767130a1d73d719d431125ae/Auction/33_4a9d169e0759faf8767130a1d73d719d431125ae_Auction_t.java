 package server.logic;
 
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import analyticsServer.event.EventFactory;
 import server.ServerStatus;
 
 public class Auction implements Comparable<Auction>{
 	private long ID;
 
 	private String description;
 	private double price=0;	
 
 	private long duration;
 	private User owner;
 	private User highestBidder;
 
 	private Timestamp creation;
 	private Timestamp expiration;
 
 	private boolean expired=false;
 	private Timer timer;
 
 	private boolean isBilled=false;
 	private EventFactory ef;
 
 
 	public Auction(User owner,String description, long duration,int auctionID){
 		creation = new Timestamp(System.currentTimeMillis());
 		this.description= description;
 		this.owner= owner;
 		this.duration=duration;
 		this.ID=auctionID;
 		expiration= new Timestamp(creation.getTime()+duration);
 
 		timer= new Timer();
 
 		timer.schedule(new ExpireTask(), duration, 10000); //10s check interval
 
 
 
 		ServerStatus.getInstance().notifyAnalyticsServer(ef.createAuctionEvent(ID,0));
 
 	}
 
 	private class ExpireTask extends TimerTask{
 
 		@Override
 		public void run() {
 			timer.cancel();
 			timer.purge();
 			
 			expired=true;
 
 			owner.addNotification(NotificationFactory.createNotification(getOwnerNote()));
 
 			if(highestBidder!=null){
 				highestBidder.addNotification(NotificationFactory.createNotification(getBidderNote()));
 				
 				ServerStatus.getInstance().notifyAnalyticsServer(ef.createBidEvent(highestBidder.getName(), ID, price, 2)); //bidder won
 
 			}
 
 			//notify analytics-server
 			
 			ServerStatus.getInstance().notifyAnalyticsServer(ef.createAuctionEvent(ID,1)); //auction ended
 
 
 
 			//notify billing-server
 			
 			ServerStatus.getInstance().billAuction(owner.getName(), ID, price);
 			
 
 
 		}
 	}
 
 	private String getOwnerNote(){
 
 		if(highestBidder!=null)
 			return "!auction-ended "+ highestBidder.getName()+" "+owner.getName()+" "+ price+" "+description+"";
		else  return "!auction-ended "+ "none "+owner.getName()+" "+" "+ "0.0 "+" "+description;
 
 		//return "!auction-ended "+ highestBidder.getName()+" "+ price+" "+description;
 	}
 
 	private String getBidderNote(){
 		return "!auction-ended "+ highestBidder.getName()+" "+owner.getName()+" "+ price+" "+description;//owner.getName()+"> "+"The auction '"+description.trim()+"' has ended. You won with "+price;
 	}
 
 	public String toString(){
 		//DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
 		SimpleDateFormat df= new SimpleDateFormat("dd.MM.yyyy kk:mm z");
 
 		String s= ID+". '"+description+"' "+owner.getName()+" "+ df.format(expiration);
 		if(highestBidder!=null){
 			s+=" "+price+ " "+highestBidder.getName();  
 		}
 		else s+=" 0.0 none";
 		return  s; 
 	}
 
 	public String toDetailString(){
 		//DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
 		SimpleDateFormat df= new SimpleDateFormat("dd.MM.yyyy kk:mm:ss z");
 
 		String s= ID+". '"+description+"' "+owner.getName()+" "+ df.format(expiration);
 		if(highestBidder!=null){
 			s+=" "+price+ " "+highestBidder.getName();  
 		}
 		else s+=" 0.0 none";
 		return  s; 
 	}
 
 	public void stop(){
 		//System.out.println("cancel auction");
 		timer.cancel();
 		timer.purge();
 		
 	}
 
 
 
 	/*
 	 * GETTER//SETTER
 	 * 
 	 */
 
 	public boolean isExpired(){
 		return expired;
 	}
 
 	public long getID() {
 		return ID;
 	}
 
 	public void setID(long iD) {
 		ID = iD;
 	}
 
 	public User getHighestBidder() {
 		return highestBidder;
 	}
 
 	public void setHighestBidder(User highestBidder) {
 		if(this.highestBidder!=null){
 			this.highestBidder.addNotification(NotificationFactory.createNotification("!new-bid "+description.trim()));
 		}
 		this.highestBidder = highestBidder;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public double getPrice() {
 		return price;
 	}
 
 	public void setPrice(double price) {
 		this.price= price;
 	}
 
 	public long getDuration() {
 		return duration;
 	}
 
 	public User getOwner() {
 		return owner;
 	}
 
 	public Timestamp getCreation() {
 		return creation;
 	}
 
 	public Timestamp getExpiration() {
 		return expiration;
 	}
 
 	public boolean isBilled() {
 		return isBilled;
 	}
 
 	public void setBilled(boolean isBilled) {
 		this.isBilled = isBilled;
 	}
 
 	@Override
 	public int compareTo(Auction o) {
 		if(this.ID == o.getID())
 			return 0;
 		else if(this.ID < o.getID())
 			return -1;
 		else return 1;
 	}
 
 
 
 
 
 }
