 
 public class TransactionMessage extends DFSMessage{
 
   public static final int START   = 0x01;
   public static final int COMMIT  = 0x02;
   public static final int ABORT   = 0x04;
   public static final int CONFIRM = 0x08;
  
 
   private int wireID;
   private int clientID;
   private int txFlags;
  //txID is now in DFSMessage 
  private HashMap<FileNameMessage, FileVersion>() changedFiles;
 
   public TransactionMessage(int client, int txID) {
     super(MessageWireType.TransactionMessage);
     wireID = MessageWireType.TransactionMessage.wireId;
     clientID = client;
     this.txID = txID;
     txFlags = 0;
   }
  
   public void set(int flag) {
     if (flag <= CONFIRM)
       txFlags |= flag;
   }
  
   public boolean isSet(int flag) {
     return (txFlags & flag) != 0;
   }
  
   public void clearFlag(int flag) {
     txFlags &= ~flag;
   }
  
   public void clearAllFlags() {
     txFlags = 0;
   }
 
   public void setClientID(int client) {
     clientID = client;
   }
 
   public int getClientID() {
     return clientID;
   }
 
   public byte[] pack() {
     byte [] packed = new byte[4];
     BinaryUtils.uintToByte(wireID, packed, 0);
     BinaryUtils.uintToByte(clientID, packed, 1);
     BinaryUtils.uintToByte(txID, packed, 2);
     BinaryUtils.uintToByte(txFlags, packed, 3);
    
     return packed;
   }
 
   @Override
   protected boolean fromByteStream(byte[] msg) {
     if (msg.length < 4)
       return false;
     this.wireID = BinaryUtils.byteToUint(msg, 0);
     this.clientID = BinaryUtils.byteToUint(msg, 1);
     this.txID = BinaryUtils.byteToUint(msg, 2);
     this.txFlags = BinaryUtils.byteToUint(msg, 3);
     return true;
   }
 
  public void putChangedFile(FileNameMessage file, FileVersion version) {  
    if (changedFiles == null) 
       changedFiles = new HashMap<FileNameMessage, FileVersion>();
     changedFiles.put(file, version);
   }
  
   public void setChangedFiles(HashMap<FileNameMessage,FileVersion> changed) {
     changedFiles = changed;
   }
 
   public HashMap<FileNameMessage, FileVersion> getChangedFiles() {
     return changedFiles;
   }
  
 }
