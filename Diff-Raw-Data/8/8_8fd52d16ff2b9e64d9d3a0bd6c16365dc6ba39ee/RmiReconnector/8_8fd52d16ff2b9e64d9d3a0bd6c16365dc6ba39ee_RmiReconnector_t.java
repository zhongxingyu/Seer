 package de.cismet.reconnector.rmi;
 
 import de.cismet.reconnector.Reconnector;
 import de.cismet.reconnector.ReconnectorException;
 import java.net.MalformedURLException;
 import java.rmi.ConnectException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.rmi.ServerException;
 
 /**
  *
  * @author jruiz
  */
 public class RmiReconnector <R extends Remote> extends Reconnector<R> {
 
     private final static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RmiReconnector.class);
 
     public static final String WRONG_VERSION = java.util.ResourceBundle.getBundle("de/cismet/reconnector/rmi/Bundle").getString("wrong_version");
     public static final String CONNECTION_LOST = java.util.ResourceBundle.getBundle("de/cismet/reconnector/rmi/Bundle").getString("connection_lost");
     public static final String LOOKUP_FAILED = java.util.ResourceBundle.getBundle("de/cismet/reconnector/rmi/Bundle").getString("lookup_failed");
     public static final String UNKNOWN = java.util.ResourceBundle.getBundle("de/cismet/reconnector/rmi/Bundle").getString("unknown_error");
 
     private String serviceUrl;
 
     public RmiReconnector(final Class serviceClass, final String serviceUrl) {
         super(serviceClass);        
         this.serviceUrl = serviceUrl;
     }
 
     @Override
     protected ReconnectorException getReconnectorException(final Throwable exception) throws Throwable {
         if (exception instanceof ConnectException) {
             return new ReconnectorException(CONNECTION_LOST);
         } else if (exception instanceof ServerException) {
             if (exception.getCause().getClass().equals(RemoteException.class)) {
                 return new ReconnectorException(CONNECTION_LOST);
             }
         }
         if (LOG.isDebugEnabled()) {
             LOG.debug(exception, exception);
         }
         throw exception;
     }
 
     @Override
     protected R connectService() throws ReconnectorException {
         try {
 //            //TODO ab hier entfernen !!! nur zum Testen !
 //            try {
 //
 //                Thread.sleep(500);
 //            } catch (InterruptedException ex) {
 //                if (LOG.isDebugEnabled()) {
 //                  log.debug("Sleep interrupted", ex);
 //                }
 //            }
 //            //TODO bis hier entfernen !!! nur zum Testen !
             return (R) Naming.lookup(serviceUrl);
         } catch (NotBoundException nbe) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("[NetworkError] could not connect to '" + serviceUrl + "'", nbe);
             }
             throw new ReconnectorException(LOOKUP_FAILED);
         } catch (MalformedURLException mue) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("'" + serviceUrl + "' is not a valid URL", mue);
             }
             throw new ReconnectorException(LOOKUP_FAILED);
         } catch (RemoteException re) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("[ServerError] could not connect to '" + serviceUrl + "'", re);
             }
             throw new ReconnectorException(LOOKUP_FAILED);
         }
     }
 }
