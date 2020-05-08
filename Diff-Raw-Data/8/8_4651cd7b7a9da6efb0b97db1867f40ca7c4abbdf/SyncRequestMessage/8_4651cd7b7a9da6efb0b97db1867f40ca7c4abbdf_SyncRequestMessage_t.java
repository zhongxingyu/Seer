 
 /**
  * This is a HACK HACK HACK! We don't actually have any data in this
  * message, and getData() will always return null. But, the format of
  * this message is so similar to SyncDataMessage, and Java doesn't have
  * multiple inheritance or mixins, so we're stuck with this :-/.
  */
 public class SyncRequestMessage extends FileMessage {
   protected SyncRequestMessage() {
     super(MessageWireType.SyncRequest);
   }
 
   protected SyncRequestMessage(MessageWireType wireId) {
     super(wireId);
   }
 
   public SyncRequestMessage(String fileName, int version, Flags flags) {
     this(MessageWireType.SyncRequest, fileName, version, flags, null);
   }
 
   protected int version;
   protected Flags flags;
 
   /**
    * HACK HACK HACK constructor.
    * Makes SyncDataMessage work.
    */
   protected SyncRequestMessage(MessageWireType wireId,
                                String fileName,
                                int version,
                                Flags flags,
                                String data) {
     super(wireId, fileName, data);
 
     if (version < 0)
       throw new IllegalArgumentException("Version should be nonnegative!");
 
     this.version = version;
 
     if (flags.pack().length != Flags.kDefaultEncodedLength)
       throw new IllegalArgumentException("Encoded flags length (" + flags.pack().length +
                                          ") is not equal to default encoded length!");
 
    this.flags = new Flags(flags);
   }
 
   protected byte[] packSyncHeader(int numAdditionalBytes) {
     int trailerSizeBytes = Flags.kDefaultEncodedLength + 4 + numAdditionalBytes;
 
     byte[] trailer = new byte[trailerSizeBytes];
     byte[] packed = createPackedHeader(trailerSizeBytes);
 
     int trailerStart = packed.length - trailerSizeBytes;
 
     BinaryUtils.uintToBEWord(version, packed, trailerStart);
 
     byte[] encodedFlags = flags.pack();
     System.arraycopy(encodedFlags, 0, packed, trailerStart + 4, encodedFlags.length);
 
     return packed;
   }
 
   public Flags getFlags() {
     return new Flags(flags);
   }
 
   public int getVersion() {
     return version;
   }
 
   @Override
   public byte[] pack() {
     return packSyncHeader(0);
   }
 
   protected int deserializeSyncHeader(byte[] packed) {
     int contentStart = unpackFileNameHeader(packed);
     if (contentStart < 0)
       return -1;
 
     // Unpack flags field
     if (contentStart + 4 + Flags.kDefaultEncodedLength >= packed.length)
       return -1;
 
     version = BinaryUtils.beWordToUint(packed, contentStart);
 
     flags = Flags.unpack(packed,
                          contentStart + 4,
                          Flags.kDefaultEncodedLength);
     if (flags == null)
       return -1;

     return contentStart + 4 + Flags.kDefaultEncodedLength;
   }
 
   @Override
   protected boolean fromByteStream(byte[] packed) {
     int trailerStart = deserializeSyncHeader(packed);
     if (trailerStart == -1)
       return false;

     return deserializeDataTrailerIfPresent(packed, trailerStart);
   }
 }
