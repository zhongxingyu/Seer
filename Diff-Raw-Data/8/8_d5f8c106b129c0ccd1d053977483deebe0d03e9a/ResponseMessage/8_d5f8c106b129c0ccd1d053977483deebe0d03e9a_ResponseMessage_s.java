 import java.io.UnsupportedEncodingException;
 
 /** Response message; represents a response to an RPC.
  *
  * Wire format: (<offset> <size in bytes> <field name>)
  * - 0x00       1       Message type.
  * - 0x01       1       Response code (unsigned int)
  * - 0x02       ?       Additional data, specific to RPC or response code
  */
 public class ResponseMessage extends DFSMessage{
   private int errorCode;
   private String data;
 
   public ResponseMessage() {
     super(MessageWireType.Response);
   }
 
   public ResponseMessage(int errorCode) {
     this(errorCode, null);
   }
 
   public ResponseMessage(int errorCode, String data) {
     this();
     this.errorCode = errorCode;
     this.data = data;
   }
 
   @Override
   public boolean fromByteStream(byte[] msg) {
     if (msg.length < 2)
       return false;
 
     errorCode = BinaryUtils.byteToUint(msg, 1);
 
     if (msg.length == 2)
       return true;
 
     try {
       data = new String(msg, 2, msg.length - 2, DFSMessage.kStringCharset);
     } catch (UnsupportedEncodingException e) {
       assert false : "should not get here";
       throw new RuntimeException("Unexpectedly could not serialize to UTF-8!");
     }
 
     return true;
   }
 
   @Override
   public byte[] pack() {
     int packedSizeBytes = 2;
     byte[] encodedData;
     byte[] packed;
 
     if (data != null) {
       try {
         encodedData = data.getBytes(DFSMessage.kStringCharset);
       } catch (UnsupportedEncodingException e) {
         assert false : "should not get here";
         throw new RuntimeException("Unexpectedly could not serialize to UTF-8!");
       }
 
       packedSizeBytes += encodedData.length;
 
       packed = new byte[packedSizeBytes];
       writeMessageType(packed);
       BinaryUtils.uintToByte(errorCode, packed, 1);
 
      System.arraycopy(encodedData, 0, packed, 0, encodedData.length);
     } else {
       packed = new byte[packedSizeBytes];
       writeMessageType(packed);
       BinaryUtils.uintToByte(errorCode, packed, 1);
     }
 
 
     return packed;
   }
 
   public int getCode() {
     return errorCode;
   }
 
   public String getData() {
     return data;
   }
 }
