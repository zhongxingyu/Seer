 package kimononet.net;
 
 import kimononet.geo.GeoLocation;
 import kimononet.geo.GeoVelocity;
 import kimononet.net.parcel.Parcel;
 import kimononet.net.parcel.Parcelable;
 import kimononet.peer.Peer;
 import kimononet.peer.PeerAddress;
 
 /**
  * Each packet has to a header section and a content section. Header section 
  * consists of the following fields:
  * 
  * HDR-MAGIC (2)
  * HDR-VERSION (1)
  * HDR-TYPE (1)
  * HDR-SRC-ID (8)
  * HDR-SRC-LOC (24)
  * HDR-SRC-VEC (8)
  * 
  * Total Length: 44.
  * 
  * The content field has variable length: 
  * -content (*)
  * 
  * @author Zorayr Khalapyan
  *
  */
 public class Packet implements Parcelable {
 
 	/**
 	 * The length of the header section. 
 	 */
 	protected static final int HEADER_LENGTH = 4 + PeerAddress.PARCEL_SIZE 
 											   + GeoLocation.PARCEL_SIZE  
 											   + GeoVelocity.PARCEL_SIZE;
 	
 	/**
 	 * Default magic sequence.
 	 */
 	private byte[] magic = new byte[] {(byte)0xBE, (byte)0xC0};
 	
 	/**
 	 * Version of the packet. 
 	 */
 	private byte version;
 	
 	/**
 	 * Packet type.
 	 */
 	private PacketType type;
 	
 	/**
 	 * Source peer of the packet - information includes peer address, 
 	 * location, and velocity.
 	 */
 	private Peer peer;
 	
 	/**
 	 * A variable length parcel of the packet.
 	 */
 	private Parcel contents;
 	
 	/** 
 	 * Creates a new empty packet. All values will be initilaized to their 
 	 * defaults.
 	 * 
 	 */
 	public Packet(){
 		
 	}
 	
 	/**
 	 * Constructs a new packet with the provided version, packet type, 
 	 * and source peer agent.
 	 * 
 	 * @param version The version of the packet structure. 
 	 * @param type The type of the packet.
 	 * @param agent The source peer agent of the packet.
 	 */
 	public Packet(byte version, PacketType type, Peer peer ){
 		
 		this.version = version;
 		this.type    = type;
 		this.peer    = peer;
 	}
 	
 	/**
 	 * Parses a packet provided the byte representation.
 	 * 
 	 * @param packet A byte representation of a packet.
 	 */
 	public Packet(byte[] packet){
 		this(new Parcel(packet));
 	}
 	
 	/**
 	 * Parses a packet from the specified parcel. 
 	 * @param parcel A parcel representation of the packet.
 	 * @see #parse(Parcel)
 	 */
 	public Packet(Parcel parcel){
 		parse(parcel);
 	}
 	
 	/**
 	 * Set's current packet's contents. The new contents will be included in the
 	 * parcel returned by {@link #toParcel()}.
 	 * 
 	 * @param contents Contents to set.
 	 */
 	public void setContents(Parcel contents){
 		this.contents = contents;
 	}
 	
 	/**
 	 * Return's current packet's contents.
 	 * @return Current packet's contents.
 	 */
 	public Parcel getContents(){
 		return this.contents;
 	}
 	
 	/**
 	 * Parses a parcel representation of a packet to an actual packet object.
 	 * 
 	 * @param packet Parcel representation of a packet.
 	 */
 	public void parse(Parcel parcel){
 	
 		//The packet must have at least enough bytes to contain 
 		//the header section.
 		if(parcel.getParcelSize() < HEADER_LENGTH){
 			throw new PacketException("Malformed or missing packet header.");
 		}
 		
 		//This should only be done at the top of the parsing hierarchy. 
 		parcel.rewind();
 		
 		parcel.getByteArray(magic);
 		version = parcel.getByte();
 		type = PacketType.parse(parcel.getByte());
 		peer = new Peer(parcel);
 		
 		if(parcel.getParcelSize() > 0){
 			contents = parcel.slice();
 		}
 		
 	}
 	
 	/**
 	 * Returns the parcel size of the current packet. Size is calculated as 
 	 * header length + length of packet contents.
 	 * 
 	 * @return The length of the current packet.
 	 */
 	@Override
 	public int getParcelSize(){
 		return HEADER_LENGTH + getContentsLength();
 	}
 	
 	/**
 	 * Returns a parcel representation of the current packet. To parse the 
 	 * returned parcel back into a packet, use either the constructor
 	 * {@link #Packet(byte[])} or the method {@link #parse(byte[])}.
 	 *  
 	 * @return A parcel representation of the current packet.
 	 */
 	@Override
 	public Parcel toParcel(){
 		
		if(peer == null){
			throw new PacketException("Cannot parcel a packet with a null source peer.");
		}
		
 		Parcel parcel = new Parcel(getParcelSize());
 	
 		parcel.add(magic);
 		parcel.add(version);
 		parcel.add(type);
 		parcel.add(peer);
 		
 		if(contents != null)
 			parcel.add(contents);
 		
 		return parcel;
 	}
 	
 	/**
 	 * Returns the length of the contents attached to this packet. If the packet
 	 * doesn't have any contents, then 0 will be returned. To get the entire
 	 * length of the packet, use {@link #getParcelSize()}. Note that the method
 	 * returns content's capacity as opposed to the current size. 
 	 * 
 	 * @return The length of the contents attached to this packet.
 	 */
 	public int getContentsLength(){
 		return (contents == null)? 0 : contents.capacity();
 	}
 	
 	/**
 	 * Returns the source peer associated with the current packet. The returned 
 	 * peer may be a null reference. 
 	 * 
 	 * @return Source peer associated with the current packet.
 	 */
 	public Peer getPeer(){
 		return peer;
 	}
 
 	/**
 	 * Returns the type of the packet.
 	 * @return The type of the current packet.
 	 */
 	public PacketType getType(){
 		return type;
 	}
 	
 	/**
 	 * Sets the type of the current packet.
 	 * @param type Thew new type of the packet.
 	 */
 	public void setType(PacketType type){
 		this.type = type;
 	}
 	
 	/**
 	 * Returns a string representation of the current packet.
 	 * @return String representation of the current packet.
 	 */
 	@Override
 	public String toString(){
 		
 		String peerString = "";
 		
 		if(peer != null){
 			peerString = "Source Address:  \t" + peer.getAddress()   + "\n" + 
 			   		     "Source Location: \t" + peer.getLocation()  + "\n" +
 			   		     "Source Velocity: \t" + peer.getVelocity()  + "\n"; 
 		}else{
 			peerString = "Source Peer:     \t Is null or invalid.       \n";
 		}
 		
 		return "--------------------------------------------- \n" +
 			   "Packet Type:     \t" + type.toString()     + "\n" +  
 			   						   peerString                 + 
 			   "Header Length:   \t" + HEADER_LENGTH       + "\n" + 
 			   "Contents Length: \t" + getContentsLength() + "\n" + 
 			   "Total Length:    \t" + getParcelSize()     + "\n" + 
 			   "---------------------------------------------";
 			   
 	}
 
 }
