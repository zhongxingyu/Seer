 /**
  * SOEN 490
  * Capstone 2011
  * Message domain object.
  * Team members: 	Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.sql.Timestamp;
 
 import org.msgpack.MessagePack;
 import org.msgpack.packer.Packer;
 import org.msgpack.unpacker.Unpacker;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.Logger;
 
 public class Message {	
 	private BigInteger mid;
 	private float speed;
 	private double latitude;
 	private double longitude;
 	private Timestamp createdAt;
 	private int userRating;
 	private File message;
	private String ownerEmail;
 	
	public Message(BigInteger mid, File message, float speed, double latitude, double longitude, Timestamp createdAt, int userRating, String ownerEmail)
 	{
 		this.mid = mid;
 		this.message = message;
 		this.speed = speed;
 		this.latitude = latitude;
 		this.longitude = longitude;
 		this.createdAt = createdAt;
 		this.userRating = userRating;
		this.ownerEmail = ownerEmail;
 	}
 		
 	// Getters and Setters
 	public BigInteger getMid() {
 		return mid;
 	}
 	
 	public File getMessage() {
 		return message;
 	}
 	
 	public float getSpeed() {
 		return speed;
 	}
 	
 	public double getLatitude() {
 		return latitude;
 	}
 	
 	public double getLongitude() {
 		return longitude;
 	}
 	
 	public Timestamp getCreatedAt() {
 		return createdAt;
 	}
 	
 	public int getUserRating() {
 		return userRating;
 	}
 
	public String getOwnerEmail() {
		return ownerEmail;
 	}
 	
 	public void setUserRating(int userRating) {
 		this.userRating = userRating;
 	}
 	
 	@Override
 	public String toString() {
 		return "MessageID: "+getMid()+
 				"\nSpeed: "+getSpeed()+
 				"\nLatitude: "+getLatitude()+
 				"\nLongitude: "+getLongitude()+
 				"\nUserRating: "+getUserRating()+
 				"\nCreatedAt: "+getCreatedAt() +
				"\nOwner: "+ getOwnerEmail();
 	}
 	
 	public static Message createMessage(InputStream in, String fileType, String folder) throws IOException {
 		Unpacker unpacker = (new MessagePack()).createUnpacker(in);
 		int size = unpacker.readInt();
 		BigInteger mid = new BigInteger(unpacker.readString());	
		String email = unpacker.readString();
 		byte[] byteMessage = unpacker.readByteArray();		
 		Float speed = unpacker.readFloat();
 		Timestamp createdAt = new Timestamp(unpacker.readLong());//unpacker.read(Timestamp.class);
 		double longitude = unpacker.readDouble();
 		double latitude = unpacker.readDouble();
 		int userRating = unpacker.readInt();
 		File message = File.createTempFile(mid.toString(), fileType, new File(folder));
 		FileOutputStream output = new FileOutputStream(message);
 		output.write(byteMessage);
 		output.close();
 		
		return new Message(mid, message, speed, latitude, longitude, createdAt, userRating, email);

 	}
 }
