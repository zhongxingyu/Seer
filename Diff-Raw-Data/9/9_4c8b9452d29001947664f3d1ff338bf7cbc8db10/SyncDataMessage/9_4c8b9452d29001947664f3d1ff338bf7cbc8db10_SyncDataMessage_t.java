 
 
 public class SyncDataMessage extends SyncRequestMessage {
   private int owner;
   private boolean exists;
 
   public SyncDataMessage() {
     super(MessageWireType.SyncData);
   }
 
   /**
    * Construct a new SyncDataMessage.
    *
    * @param fileName name of the file being sent
    * @param version version number of the file being sent
    * @param flags Flags describing the file being sent
    * @param data Optional data in the file being sent. You may pass null here,
    *             and the receiving end will see data == null.
    */
   protected SyncDataMessage(String fileName, int version, Flags flags, int owner, boolean exists, String data) {
     super(MessageWireType.SyncData, fileName, version, flags, data);
     this.owner = owner;
     this.exists = exists;
   }
 
   @Override
   public byte[] pack() {
     byte[] packed = packSyncHeader(getEncodedData().length + 5);
     
    int syncDataStart = packed.length - getEncodedData().length - 5;
 
     BinaryUtils.uintToBEWord(owner, packed, syncDataStart);
     BinaryUtils.uintToByte(exists ? 1 : 0, packed, syncDataStart + 4);
     
     System.arraycopy(getEncodedData(), 
                      0, 
                      packed, 
                      syncDataStart + 5, 
                      getEncodedData().length);
 
     return packed;
   }
 
   @Override
   public boolean fromByteStream(byte[] msg) {
     int contentStart = deserializeSyncHeader(msg);
     if (contentStart == -1)
       return false;
 
     if (msg.length - contentStart < 5)
       return false;
 
     owner = BinaryUtils.beWordToUint(msg, contentStart);
     exists = (BinaryUtils.byteToUint(msg, contentStart + 4) > 0) ? true : false;
     
     return deserializeDataTrailerIfPresent(msg, contentStart + 5);
   }
 
   public int getOwner() {
     return owner;
   }
 
   public boolean exists() {
     return exists;
   }
 
   @Override
   public String toString() {
     return "SyncData file=" + getFileName() + ", version=" + getVersion() + ", flags=" + flags + ", owner=" + owner + ", exists=" + exists + ", data=" + (data != null ? "<" + data.length() + " bytes>" : "(null)");
   }
 }
