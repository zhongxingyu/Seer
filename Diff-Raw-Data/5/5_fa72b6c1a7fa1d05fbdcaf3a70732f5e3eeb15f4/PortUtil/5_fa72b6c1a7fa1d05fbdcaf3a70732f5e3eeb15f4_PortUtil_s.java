 package de.scrum_master.util;
 
 import java.io.IOException;
 import java.net.DatagramSocket;
 import java.net.ServerSocket;
 
 /**
  * Provides utility methods for checking availability of TCP/UDP ports
  */
 public final class PortUtil {
 
 	public static final int MIN_PORT_NUMBER = 1024;
 	public static final int MAX_PORT_NUMBER = 65535;
	public static final int MAX_PORT_OFFSET = MAX_PORT_NUMBER - MIN_PORT_NUMBER - 1;
 
 	/**
 	 * Checks if a specific TCP/UDP port is available
 	 *
 	 * @param port the port to be checked for availability
 	 * @return true if port is available; false if port is unavailable or outside legal range of
 	 * MIN_PORT_NUMBER..MAX_PORT_NUMBER
 	 */
 	public static boolean isAvailable(int port) {
 		if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
 			return false;
 		}
 		ServerSocket serverSocket = null;
 		DatagramSocket datagramSocket = null;
 		try {
 			serverSocket = new ServerSocket(port);
 			serverSocket.setReuseAddress(true);
 			datagramSocket = new DatagramSocket(port);
 			datagramSocket.setReuseAddress(true);
 			return true;
 		} catch (IOException e) {
 			// todo... nothing to do it is not free
 		} finally {
 			if (datagramSocket != null) {
 				datagramSocket.close();
 			}
 
 			if (serverSocket != null) {
 				try {
 					serverSocket.close();
 				} catch (IOException e) {}
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Checks all non-privileged TCP/UDP ports in range MIN_PORT_NUMBER..MAX_PORT_NUMBER for availability
 	 * @return the first free port within the search range
 	 * @throws IOException if no port is available at all
 	 */
 	public static int getAvailablePort() throws IOException {
 		return getAvailablePort(0);
 	}
 
 	/**
 	 * Checks all non-privileged TCP/UDP ports in range MIN_PORT_NUMBER + offset..MAX_PORT_NUMBER for availability
 	 * @param offset number of ports above MIN_PORT_NUMBER to be skipped during availability check
 	 * @return the first free port within the search range
 	 * @throws IOException if no port is available at all
 	 * @throws IllegalArgumentException if offset is not in range 0..MAX_PORT_OFFSET
 	 */
 	public static int getAvailablePort(int offset) throws IOException {
 		// TODO: Is this method really needed?
 		if (offset < 0 || offset > MAX_PORT_OFFSET)
 			throw new IllegalArgumentException("offset too big (must be in range 0.." + MAX_PORT_OFFSET + ")");
		for (int i = MIN_PORT_NUMBER; i <=MAX_PORT_NUMBER; i++) {
 			if (isAvailable(i)) {
 				return i;
 			}
 		}
 		throw new IOException("no port available");
 	}
 }
