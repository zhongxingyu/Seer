 package it.unipd.fast.broadcast.range_estimation;
 
 import java.util.List;
 
 import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
 
 /**
  * Range Estimator interface. 
  * 
  * @author Moreno Ambrosin
  *
  */
 public interface IRangeEstimator {
 
 	/**
 	 * Turn duration in milliseconds
 	 */
 	public static final int TURN_DURATION = 1000;
 	
 	/**
 	 * Method used to tell whether a hello message already arrived
 	 * 
 	 * @param arrived
 	 */
 	public void setHelloMessageArrived(boolean arrived);
 	
 	/**
 	 * Method used to pass an Hello Message to the estimation object
 	 * 
 	 * @param message
 	 */
 	public void helloMessageReceived(IMessage message);
 
 	/**
 	 * Stops the estimator execution
 	 */
 	public void stopExecuting();
 	
 	/**
 	 * Devices list setter
 	 * 
 	 * @param devices
 	 */
 	public void setDevicesList(List<String> devices);
	
 }
