 package edu.teco.dnd.module.messages.joinStartApp;
 
 import java.util.UUID;
 
 import edu.teco.dnd.network.messages.Response;
 
 /**
  * send when a new Application is supposed to be started.
  * 
  * @author Marvin Marx
  * 
  */
 public class StartApplicationAck extends Response {
 
	public static String MESSAGE_TYPE = "join application ack";
 
 	public UUID appId;
 
 	public StartApplicationAck(UUID appId) {
 		this.appId = appId;
 	}
 
 	public StartApplicationAck(StartApplicationMessage msg) {
 		this.appId = msg.getApplicationID();
 	}
 
 	@SuppressWarnings("unused")
 	/* for gson */
 	private StartApplicationAck() {
 		appId = null;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((appId == null) ? 0 : appId.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!super.equals(obj)) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		StartApplicationAck other = (StartApplicationAck) obj;
 		if (appId == null) {
 			if (other.appId != null) {
 				return false;
 			}
 		} else if (!appId.equals(other.appId)) {
 			return false;
 		}
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "JoinApplicationAck [appId=" + appId + ", getSourceUUID()=" + getSourceUUID()
 				+ ", getUUID()=" + getUUID() + "]";
 	}
 	
 	
 }
