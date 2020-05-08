 package ie.wombat.ha.nic.xbee;
 
 import ie.wombat.ha.DebugUtils;
 import ie.wombat.ha.ZigBeeNIC;
 import ie.wombat.ha.nic.APIFrameListener;
 import ie.wombat.ha.nic.UARTAdapter;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 
 /**
  * A stream IO interface to the XBee UART. Suitable for XBee on local USB bus or 
  * available via ser2net on network.
  * 
  * TODO: this could be a super class for a SIOAdapter and TCPAdapter.
  * 
  * @author joe
  *
  */
 public class XBeeStreamAdapter implements UARTAdapter, APIFrameListener {
 	
 	private static Logger log = Logger.getLogger(XBeeStreamAdapter.class);
 	
 	private XBeeDriver nic;
 	private InputStream in;
 	private OutputStream out;
 	
 	/**
 	 * A thread listens to the xbeeIn input stream, triggering callbacks when an API packet arrives.
 	 */
 	private XBeeReadThread readThread;
 	
 	private APIFrameListener nicListener;
 	
 	public XBeeStreamAdapter (XBeeDriver nic, InputStream in, OutputStream out) {
 		this.nic = nic;
 		this.in = in;
 		this.out = out;
 		
 		readThread = new XBeeReadThread(nic,in);
 		readThread.setName("XBeeRead");
 		
 		// We only require this thread to exist while the driver is in existence
 		readThread.setDaemon(true);
 		
 		readThread.addListener(this);
 		
 		readThread.start();
 		
 		
 	}
 
 	public void setName (String name) {
 		readThread.setName(name);
 	}
 	
 	public void txAPIFrame(byte[] apiPacketData, int packetLen) throws IOException {
		log.info("sendXBeeAPIPacket() Sending escaped API packet len=" + packetLen + " on the 'wire' using OutputStream " + out);
		log.debug ("sendXBeeAPIPacket(): " + DebugUtils.formatXBeeAPIFrame(apiPacketData, 0, packetLen));
 		out.write(apiPacketData,0,packetLen);
 		out.flush();
 	}
 	
 	// TODO: experimental
 	public void receiveXBeeAPIPacket(byte[] apiPacketData, int packetLen)
 	throws IOException {
 		nicListener.handleAPIFrame(apiPacketData, packetLen);
 	}
 	
 
 	public void setRxAPIFrameListener(APIFrameListener listener) {
 		//readThread.addListener(listener);
 		nicListener = listener;
 	}
 	
 
 
 	public Date getExpiryTime() {
 		return null;
 	}
 
 	public void setExpiryTime(Date expire) {
 		// ignore
 	}
 
 	public void handleAPIFrame(byte[] packet, int packetLen) {
 		try {
 			receiveXBeeAPIPacket(packet, packetLen);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 	}
 
 	
 	public void close () {
 		try {
 			in.close();
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 }
