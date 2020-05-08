 import java.io.UnsupportedEncodingException;
 
 /**
  * Implements messages that contain a file name in the first portion of
  * the message.
  *
  * Wire format: (<offset> <size in bytes> <field name>)
  * - 0x00       1       Message type.
  * - 0x01       2       Size, in bytes, of file name field.
  * - 0x03       ?       File name, encoded in UTF-8
  */
 public class FileNameMessage extends DFSMessage {
   protected String fileName;
 
   protected FileNameMessage(DFSMessage.MessageWireType messageType) {
     super(messageType);
   }
 
   protected FileNameMessage(MessageWireType messageType, String fileName) {
     this(messageType);
     setFileName(fileName);
   }
 
   /**
    * Create an array to hold packed data. Fill the first bytes as per
    * createPackedHeader(int), and allocate trailingBytes.length bytes at
    * the end. Copy the data in trailing bytes into the end of the array,
    * and return it.
    *
    * @param trailingBytes trailer of the packed representation.
    * @return packed representation of the packet, whose last bytes are
    *         equal to the contents of trailingBytes.
    */
   protected byte[] createPackedHeader(byte[] trailingBytes) {
     byte[] packedMsg = createPackedHeader(trailingBytes.length);
     System.arraycopy(trailingBytes,
                      0,
                      packedMsg,
                      packedMsg.length - trailingBytes.length,
                      trailingBytes.length);
     return packedMsg;
   }
 
   /**
    * Create an array to hold packed data and fill the first bytes
    * with the packet type identifier and file name. There are
    * numAdditionalBytes bytes allocated past the end of the file name.
    *
    * The bytes starting at index @code length - numAdditionalBytes can be freely
    * changed by the caller without corrupting the file name field and packet
    * type id.
    *
    * @param numAdditionalBytes
    *         Number of bytes to allocate at the end of the array.
    * @return a byte array whose first bytes are filled with the packet type id
    *         and file name header.
    */
   protected byte[] createPackedHeader(int numAdditionalBytes) {
     byte[] fileNameBytes;
     try {
       fileNameBytes = fileName.getBytes(DFSMessage.kStringCharset);
     } catch (UnsupportedEncodingException e) {
       assert false : "should not get here";
       throw new RuntimeException("Unexpectedly could not serialize to UTF-8!");
     }
     byte[] packedMsg = new byte[3 + fileNameBytes.length + numAdditionalBytes];
 
     writeMessageType(packedMsg);
     BinaryUtils.uintToBEShort(fileNameBytes.length, packedMsg, 1);
     System.arraycopy(fileNameBytes, 0, packedMsg, 3, fileNameBytes.length);
     return packedMsg;
   }
 
   /** Unpack the file name header, validate it, and store the file name in
    * fileName.
    *
    * @param msg byte array containing the encoded file name header.
    * @return index of the byte in the array immediately following the file
    *         name header, or -1 if the header was invalid. NOTE: a nonnegative
    *         value is not necessarily within the array bounds; the last byte
    *         of the array may also be the last byte of the file name.
    */
   protected int unpackFileNameHeader(byte[] msg) {
     int fileNameSize = BinaryUtils.beShortToUint(msg, 1);
     if (fileNameSize < 0 || fileNameSize > DFSMessage.kMaxFileNameSizeBytes)
       return -1;
 
     try {
       this.fileName = new String(msg, 3, fileNameSize, DFSMessage.kStringCharset);
     } catch (UnsupportedEncodingException e) {
       assert false : "should not get here";
       throw new RuntimeException("Unexpectedly could not serialize to UTF-8!");
     }
 
     return fileNameSize + 3;
   }
 
 
   @Override
   protected boolean fromByteStream(byte[] msg) {
     return unpackFileNameHeader(msg) > -1;
   }
 
   @Override
   public byte[] pack() {
     return createPackedHeader(0);
   }
 
   public String getFileName() {
     return fileName;
   }
 
  public DFSFilename getDFSFileName(String fileName) {
     return new DFSFilename(fileName);
   }
 
   protected void setFileName(String fileName) {
     try {
       if (fileName.getBytes(DFSMessage.kStringCharset).length > kMaxFileNameSizeBytes)
         throw new IllegalArgumentException("Encoded representation of the file name is too long!");
     } catch (UnsupportedEncodingException e) {
        assert false : "should not get here";
       throw new RuntimeException("Unexpectedly could not serialize to UTF-8!");
     }
 
     this.fileName = fileName;
   }
 
   @Override
   public String toString() {
     return messageType.toString() + " message, fileName = \"" + fileName + "\"";
   }
 }
