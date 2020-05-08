 package manager.dht;
 
 public class NodeID implements Comparable<NodeID> {
 	public static final int ADDRESS_SIZE = 1;
 
 	private static NodeID MIN_POSITION_CONSTANT = null;
 	private static NodeID MAX_POSITION_CONSTANT = null;
 	
 	private byte[] id = new byte[ADDRESS_SIZE];
 	
 	public NodeID(byte[] id) {
 		this.id = id;
 	}
 	
 	public byte[] getID() {
 		return id;
 	}
 	
 	public String toString() {
 		return SHA1Generator.convertToHex(id);
 	}
 
 	@Override
 	public int compareTo(NodeID comp) {
 		//Its us!
 		if(comp == this) return 0;
 		
 		for(int i = 0; i < ADDRESS_SIZE; i++) {
			if(comp.id[i] > id[i]) return 1;
			else if(comp.id[i] < id[i]) return -1;
 		}
 
 		//Equal
 		return 0;
 	}
 	
 	public final static NodeID MIN_POSITION() {
 		//Create address if necessary
 		if(MIN_POSITION_CONSTANT == null) {
 			byte[] address = new byte[ADDRESS_SIZE];
 			for(int i = 0; i < ADDRESS_SIZE; i++) address[i] = 0x00;
 			MIN_POSITION_CONSTANT = new NodeID(address);
 		}
 		return MIN_POSITION_CONSTANT;
 	}
 
 	public final static NodeID MAX_POSITION() {
 		//Create address if necessary
 		if(MAX_POSITION_CONSTANT == null) {
 			byte[] address = new byte[ADDRESS_SIZE];
 			for(int i = 0; i < ADDRESS_SIZE; i++) address[i] = (byte)0xff;
 			MAX_POSITION_CONSTANT = new NodeID(address);
 		}
 		return MAX_POSITION_CONSTANT;
 	}
 }
