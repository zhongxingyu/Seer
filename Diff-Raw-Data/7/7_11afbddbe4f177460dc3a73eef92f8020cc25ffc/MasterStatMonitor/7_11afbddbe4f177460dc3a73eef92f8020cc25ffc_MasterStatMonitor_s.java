 package kimononet.stat;
 
 
 /**
  * Represents a centralized statistical information gathering node. 
  * 
  * In a production mode, several nodes will act as slave monitors which will 
  * sent gathered data to a single master node. This object represents the master
  * node that has access to the aggregate statistical information.
  * 
  * Note that in simulation mode, if each node has a reference to a master 
  * monitor there is no need to utilize slave monitors. 
  * 
  * @author Zorayr Khalapyan
  * @since 3/12/2012
  * @version 3/12/2012
  *
  */
 public class MasterStatMonitor implements StatMonitor {
 
 	/**
 	 * Represents gathered statistical data.
 	 * 
 	 * @see #getStats()
 	 */
 	private StatData data;
 	
 	/**
 	 * Indicates the current state of the monitor. If true, collected data will
 	 * be processed; if false, all received and sent packets will be ignored.
 	 * To modify the state of the current monitor use either 
 	 * {@link #startService()} or {@link #shutdownService()}. By default this 
 	 * value is set to true.
 	 */
 	private boolean running = true;
 	
 	/**
 	 * Accounts for a sent data packet. Specified packet will be ignored if the 
 	 * current monitor has been shutdown using {@link #shutdownService()}.
 	 */
 	@Override
 	public void packetSent(StatPacket packet) {
 		if(running){
 			data.addSentPacket(packet);
 		}	
 	}
 
 	/**
 	 * Accounts for a received data packet. Specified packet will be ignored if 
 	 * the current monitor has been shutdown using {@link #shutdownService()}.
 	 */	
 	@Override
 	public void packetReceived(StatPacket packet) {
 		if(running){
 			data.addReceivedPacket(packet);
 		}		
 	}
 
 	/**
 	 * Returns the gathered statistical data.
 	 */
 	@Override
 	public StatData getStats() {
 		return data;
 	}
 
 	/**
 	 * All packets, sent or received, will be processed. 
 	 */
 	@Override
 	public void startService() {
 		this.running = true;
 	}
 
 	/**
 	 * All packets, sent or received, will be ignored. Stopping the monitoring 
 	 * service will remove almost all the overhead related to analyzing data
 	 * and allow for a much more efficient production level packet processing.
 	 */
 	@Override
 	public void shutdownService() {
 		this.running = false;
 	}
 
 }
