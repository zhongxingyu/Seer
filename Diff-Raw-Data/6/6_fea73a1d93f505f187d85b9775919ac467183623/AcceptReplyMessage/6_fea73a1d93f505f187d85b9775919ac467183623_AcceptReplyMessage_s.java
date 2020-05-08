 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 public class AcceptReplyMessage extends PaxosMessage {
   int proposalId;
   int lastAcceptedProposalId;
 
   public AcceptReplyMessage() {
     super(MessageWireType.AcceptReply);
   }
   
   public AcceptReplyMessage(int proposalId, int lastAcceptedProposalId) {
     this();
     this.proposalId = proposalId;
     this.lastAcceptedProposalId = lastAcceptedProposalId;
   }
   
   public int getProposalId() {
     return proposalId;
   }
 
   public int getLastAcceptedProposalId() {
     return lastAcceptedProposalId;
   }
   
   @Override
   protected boolean fromByteStream(byte[] msg) {
    System.err.println("Unpacking msg of length " + msg.length);
     if (msg.length < 1)
       return false;
 
     ByteArrayInputStream bytes = new ByteArrayInputStream(msg, 1, msg.length-1);
     
     try {
       ObjectInputStream ois = new ObjectInputStream(bytes);
       lastAcceptedProposalId = ois.readInt();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
       System.err.println("Error while unpacking Paxos Accept Reply");
       return false;
     }
     return true;
   }
 
   @Override
   public byte[] pack() {
     ByteArrayOutputStream bytes = new ByteArrayOutputStream();
     ObjectOutputStream output;
   
     try {
       output = new ObjectOutputStream(bytes);
       output.writeInt(lastAcceptedProposalId);
       output.close();
       bytes.close();
     } catch (IOException e) {
       assert false : "Should not get here.";
       return null;
     }
     
     byte[] state = bytes.toByteArray();
     byte[] packed = new byte[state.length + 1];
     writeMessageType(packed);
 
     System.arraycopy(state, 0, packed, 1, state.length);
     return packed;
   }
 
 }
