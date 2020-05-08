 package net.holyc.dispatcher;
 
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 
 import org.openflow.protocol.OFMessage;
 import org.openflow.protocol.OFError;
 
 import net.holyc.dispatcher.OFEvent;
 
 /** Class for OpenFlow error event
  *
  * @author ykk
  */
 public class OFErrorEvent
     extends OFEvent
 {
     /** Constructor
      *
      * @param ofevent OpenFlow message event to clone
      */
     public OFErrorEvent(OFEvent ofevent)
     {
 	this.scn = ofevent.getSocketChannelNumber();
 	byteArray = ofevent.getByteArray().clone();
 	ofm = new OFError();
 	((OFError) ofm).readFrom(getByteBuffer(byteArray));
     }
 
     /** Return reference to OpenFlow message
      */
     public OFError getOFError()
     {
 	return (OFError) ofm;
     }
 }
