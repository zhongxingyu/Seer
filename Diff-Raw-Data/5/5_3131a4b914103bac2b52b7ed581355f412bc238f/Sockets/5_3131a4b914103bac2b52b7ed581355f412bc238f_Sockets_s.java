 package petrglad.javarpc.util;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import org.apache.log4j.Logger;
 
 public final class Sockets {
     static final Logger LOG = Logger.getLogger(Sockets.class);
 
     private Sockets() {
     }
 
     public static Socket openClientSocket(final String host, final int port) {
         try {
             return new Socket(host, port);
         } catch (UnknownHostException e) {
            throw new RuntimeException("Host is not found " + host + ":" + port);
         } catch (IOException e) {
            throw new RuntimeException("Could not open connection to " + host);
         }
     }
 
     /**
      * @return Stop condition for spoolers.
      */
     public static Flag getIsSocketOpen(final Socket socket) {
         return new Flag() {
             @Override
             public Boolean get() {
                 return !socket.isClosed();
             }
         };
     }
 
     public static void closeSocket(final ServerSocket socket) {
         try {
             socket.close();
         } catch (IOException e) {
             LOG.error("Error closing server socket " + socket, e);
         }
     }
 
     public static void closeSocket(final Socket socket) {
         try {
             socket.close();
         } catch (IOException e) {
             LOG.error("Error closing socket " + socket, e);
         }
     }
 }
