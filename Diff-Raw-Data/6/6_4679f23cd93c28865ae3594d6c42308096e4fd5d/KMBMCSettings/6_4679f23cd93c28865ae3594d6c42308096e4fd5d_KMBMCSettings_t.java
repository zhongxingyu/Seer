 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package serverManagementConsol;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.net.Socket;
 import kittyMiningBuddyLibrary.enums.ConnectStatus;
 
 /**
  * This class provides the settings and central data store for the Kitty Mining
  * Buddy Server Management Consol
  * @author Roy
  */
 public class KMBMCSettings {
     
     private ConnectStatus ConnStatus = ConnectStatus.DISCONNECTED;
     public static final String PROP_CONNSTATUS = "ConnStatus";
     
     private Socket connSocket = null;
     public static final String PROP_CONNSOCKET = "connSocket";
 
     /**
      * Get the value of connSocket
      *
      * @return the value of connSocket
      */
     public Socket getConnSocket() {
         return connSocket;
     }
 
     /**
      * Set the value of connSocket
      *
      * @param connSocket new value of connSocket
      */
     public void setConnSocket(Socket connSocket) {
         Socket oldConnSocket = this.connSocket;
         this.connSocket = connSocket;
         propertyChangeSupport.firePropertyChange(PROP_CONNSOCKET, oldConnSocket, connSocket);
     }
 
 
     /**
      * Get the value of ConnStatus
      *
      * @return the value of ConnStatus
      */
     public ConnectStatus getConnStatus() {
         return ConnStatus;
     }
 
     /**
      * Set the value of ConnStatus
      *
      * @param ConnStatus new value of ConnStatus
      */
     public void setConnStatus(ConnectStatus ConnStatus) {
         ConnectStatus oldConnStatus = this.ConnStatus;
         this.ConnStatus = ConnStatus;
         propertyChangeSupport.firePropertyChange(PROP_CONNSTATUS, oldConnStatus, ConnStatus);
     }
    private transient PropertyChangeSupport propertyChangeSupport;
 
     /**
      * Add PropertyChangeListener.
      *
      * @param listener
      */
     public void addPropertyChangeListener(PropertyChangeListener listener) {
        if(null == propertyChangeSupport){
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
         propertyChangeSupport.addPropertyChangeListener(listener);
     }
 
     /**
      * Remove PropertyChangeListener.
      *
      * @param listener
      */
     public void removePropertyChangeListener(PropertyChangeListener listener) {
         propertyChangeSupport.removePropertyChangeListener(listener);
     }
 }
