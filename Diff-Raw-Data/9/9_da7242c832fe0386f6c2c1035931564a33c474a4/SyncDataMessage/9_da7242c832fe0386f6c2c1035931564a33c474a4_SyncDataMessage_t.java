 
 
 public class SyncDataMessage extends SyncRequestMessage {
   private int owner;
 
   protected SyncDataMessage() {
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
   protected SyncDataMessage(String fileName, int version, Flags flags, int owner, String data) {
     super(MessageWireType.SyncData, fileName, version, flags, data);
     this.owner = owner;
   }
 
   @Override
   public byte[] pack() {
     byte[] packed = packSyncHeader(getEncodedData().length + 4);
     
     int syncDataStart = packed.length - getEncodedData().length;
 
     BinaryUtils.uintToBEWord(owner, packed, syncDataStart);
     
     System.arraycopy(getEncodedData(), 
                      0, 
                      packed, 
                      syncDataStart + 4, 
                      getEncodedData().length);
 
     return packed;
   }
 
   @Override
   public boolean fromByteStream(byte[] msg) {
     int contentStart = deserializeSyncHeader(msg);
     if (contentStart == -1)
       return false;
 
     owner = BinaryUtils.beWordToUint(msg, contentStart);
     
     return deserializeDataTrailerIfPresent(msg, contentStart + 4);
   }
 
   public int getOwner() {
     return owner;
   }
 }
