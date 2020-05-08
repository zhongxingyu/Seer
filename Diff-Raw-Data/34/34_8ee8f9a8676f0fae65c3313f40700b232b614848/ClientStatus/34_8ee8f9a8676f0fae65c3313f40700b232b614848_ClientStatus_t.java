 package client;
 
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import network.tcp.client.TCPOutputConnection;
 
 import java.util.ArrayList;
 
 
 
 public class ClientStatus {
 	private static ClientStatus instance=null;
 
 
 	private  boolean ack= false;
 	private  String user="";
 	private  boolean kill= false;
 	private boolean blocked=false;
 	//private Socket socket=null;
 	private String lastCommand;
 	private int resendTry;
 	
 	private TCPOutputConnection connection;
 
 	
 	private Timer timer;
 	private long timeout=90000; //TODO set to 20000 after testing
 
 	public ClientStatus(){
 		timer=new Timer();
 	}
 
 	public synchronized static ClientStatus getInstance(){
 		if(instance==null) {
 			instance= new ClientStatus();
 			
 		}
 		return instance;
 	}
 
 
 	public synchronized  boolean isAck(){
 		return ack;
 	}
 
 	public  synchronized String getUser(){
 		return this.user;
 	}
 
 	public synchronized boolean isKill(){
 		return this.kill;
 	}
 
 	public synchronized void setAck(boolean ack){
 		//System.out.println("ack changed");
 		this.ack=ack;
 		//System.out.println(this.ack+" | "+ack);
 	}
 
 	public synchronized void setUser(String user){
 		this.user=user;
 	}
 
 
 	public void killClient(){
 		kill= true;
 		//ClientMain.kill();
 	}
 
 
 	public void setKill(boolean b) {
 		kill=b;
 
 	}
 
 
 	public boolean isBlocked() {
 		return blocked;
 	}
 
 
 	public void setBlocked(boolean blocked) {
 		//System.out.println("new value: "+blocked);
 		if(blocked==true && this.blocked==false){
 			timer.schedule(new UnblockTask(), timeout); 
 		}else{
 			//System.out.println("unblock, cancel unblock-task");
 			try{
 				timer.cancel();
 				timer.purge();
 			}catch (IllegalStateException e){
 				
 			}
 		}
 
 		this.blocked = blocked;
 	}
 	
 
 	
 
 
 	public TCPOutputConnection getConnection() {
 		return connection;
 	}
 
 	public void setConnection(TCPOutputConnection connection) {
 		this.connection = connection;
 	}
 
 	public String getLastCommand() {
 		return lastCommand;
 	}
 
 	public void setLastCommand(String lastCommand) {
 		resendTry=0;
 		//System.out.println("last command: "+lastCommand);
 		this.lastCommand = lastCommand;
 	}
 
 
 	public int getResendTry() {
 		return resendTry;
 	}
 
 	public void setResendTry(int resendTry) {
 		this.resendTry = resendTry;
 	}
 
 
 
 
 	private class UnblockTask extends TimerTask{
 
 		@Override
 		public void run() {
 			blocked= false;
 			System.out.println("unblockTask: finished");
 		}
 
 	}
 
 


	public void end() {
		timer.cancel();
		
	}


 }
