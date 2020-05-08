 package se.kth.ssvl.tslab.wsn.general.bpf;
 
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.Bundle;
 
 public interface BPFActionReceiver {
 
 	/**
 	 * This method is called when the framework has a notification. This is usually used for telling the user about events that are occuring.
 	 * @param header A string describing the header of the notificaiton
 	 * @param description A string for describing the event further
 	 */
 	public abstract void notify(String header, String description);
 	
 	/**
 	 * This method is called from the framework when there was a bundle received for the local device. 
 	 * @param bundle The bundle which was received
 	 */
	public abstract void bundleReceived(Bundle bundle);
 	
 }
