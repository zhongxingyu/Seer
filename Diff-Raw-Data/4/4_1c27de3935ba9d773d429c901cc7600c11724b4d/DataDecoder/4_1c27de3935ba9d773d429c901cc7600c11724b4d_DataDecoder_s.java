 package org.zju.car_monitor.codec;
 
 import org.apache.log4j.Logger;
 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
 import org.apache.mina.filter.codec.ProtocolDecoderOutput;
 import org.zju.car_monitor.model.Record;
 import org.zju.car_monitor.server.RecordParser;
 import org.zju.car_monitor.util.Config;
 import org.zju.car_monitor.util.EventProcessor;
 
 import java.util.ArrayList;
 
 /**
  * User: jiezhen
  * Date: 3/21/13
  * Time: 2:27 PM
  */
 public class DataDecoder extends CumulativeProtocolDecoder {
 
     private static Logger logger = Logger.getLogger(DataDecoder.class);
     private static int MINIMAL_MESSAGE_LENGTH = 20;
 
     private Byte[] getCompletePacket(IoBuffer ioBuffer) {
         int bufferSize = ioBuffer.remaining();
         ioBuffer.mark();
         byte first = ioBuffer.get();
         if ((int)first == 13) {
        	//if it is carriage return skip it
         	first = ioBuffer.get();
         }
         if ((char)first != '&') {
             logger.info("Message doesn't start with &. Skip it. First = " + (char)first);
             ioBuffer.reset();
             return null;
         }
         boolean findEnding = false;
         ArrayList<Byte> byteArrayList = new ArrayList<Byte>();
         byteArrayList.add(first);
         for (int i = 0; i < bufferSize - 1 ; i ++) {
             byte b = ioBuffer.get();
             byteArrayList.add(b);
             if ((char)b == '!') {
                 findEnding = true;
                 break;
             }
         }
         if (!findEnding) {
             logger.info("Haven't found the ending, reset the buffer");
             ioBuffer.reset();
             return null;
         }else{
             Byte[] bytes = new Byte[byteArrayList.size()];
             return byteArrayList.toArray(bytes);
         }
     }
 
     @Override
     protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
 
         logger.info("In doDecode");
         int bytesInBuffer = ioBuffer.remaining();
         logger.info("Receiving " + bytesInBuffer + " bytes");
         if (bytesInBuffer <= MINIMAL_MESSAGE_LENGTH) return false;
 
         Byte[] bytes = getCompletePacket(ioBuffer);
         if(bytes == null) {
             return false;
         } else {
             byte[] bts = new byte[bytes.length];
             for (int i = 0; i < bytes.length; i ++) {
                 bts[i] = bytes[i];
             }
             try {
             	EventProcessor.process(bts);
             } catch(Exception e) {
             	logger.error(e);
             }
             
             return true;
         }
 
     }
 
 }
