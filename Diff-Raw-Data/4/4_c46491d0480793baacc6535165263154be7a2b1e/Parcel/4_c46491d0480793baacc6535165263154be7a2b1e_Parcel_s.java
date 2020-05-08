 package kimononet.net.parcel;
 
 import java.nio.ByteBuffer;
 
 public class Parcel implements Parcelable{
 
 	private ByteBuffer buffer;
 	
 	public Parcel(ByteBuffer buffer){
 		this.buffer = buffer;
 	}
 	
 	public Parcel(int length){
 		buffer = ByteBuffer.allocate(length);
 	}
 	
 	public Parcel(byte parcel){
 		this(new byte[]{parcel});
 	}
 	
 	public Parcel(byte[] parcel){
 		this(parcel.length);
 		add(parcel);
 	}
 	
 	public void add(byte data){
 		buffer.put(data);
 	}
 	
 	public void add(double data){
 		buffer.putDouble(data);
 	}
 	
 	public void add(char data){
 		buffer.putChar(data);
 	}
 	
 	public void add(int data){
 		buffer.putInt(data);
 	}
 	
 	public void add(float data){
 		buffer.putFloat(data);
 	}
 	
 	public void add(long data){
 		buffer.putLong(data);
 	}
 	
 	public void add(String data){
 		buffer.put(data.getBytes());
 	}
 	
 	public void add(byte[] data){
 		buffer.put(data);
 	}
 	
 	public void add(int index, long data){
 		buffer.putLong(index, data);
 	}
 	
 	public short getShort(){
 		return buffer.getShort();
 	}
 	
 	public long getLong(){
 		return buffer.getLong();
 	}
 	
 	public double getDouble(){
 		return buffer.getDouble();
 	}
 	
 	public int getInt(){
 		return buffer.getInt();
 	}
 	
 	public char getChar(){
 		return buffer.getChar();
 	}
 	
 	public float getFloat(){
 		return buffer.getFloat();
 	}
 	
 	public byte getByte(){
 		return buffer.get();
 	}
 	
 	public void getByteArray(byte[] data){
 		buffer.get(data);
 	}
 	
 	public byte[] toByteArray(){
 		return buffer.array();
 	}
 	
 	public void add(Parcelable parcelable){
 		add(parcelable.toParcel().toByteArray());
 	}
 	
 	@Override
 	public Parcel toParcel(){
 		return this;
 	}
 	
 	public void setParcel(Parcel parcel){
 		this.buffer = parcel.buffer;
 	}
 	
 	public void copy(byte[] data, int offset, int length){
 		this.buffer.put(data, offset, length);
 	}
 	
 	/**
 	 * Returns current size of the parcel. To get the total capacity of the 
 	 * parcel, use {@link #capacity()}.
 	 * 
 	 * @return The current size of the parcel.
 	 */
 	@Override
 	public int getParcelSize(){
 		return buffer.position();
 	}
 	
 
 	
 	/**
 	 * Returns the capacity of the current parcel. To get the current size of
 	 * the parcel, use {@link #getParcelSize()}.
 	 * 
 	 * @return The capacity of the current parcel.
 	 */
 	public int capacity(){
 		return buffer.capacity();
 	}
 	
 	public void rewind(){
 		this.buffer.rewind();
 	}
 	
 	public Parcel slice(){
 		return new Parcel(buffer.slice());
 	}
 	
 	public Parcel compact(){
 		return new Parcel(buffer.compact());
 	}
 	
 	/**
 	 * Combines multiple parcelable objects into a single parcel.
 	 * 
 	 * @param parcels Various parcels to be combined.
 	 * @return A byte array that represents all the specified parcels.
 	 */
 	public static Parcel combineParcelables(Parcelable... parcelables){
 		
 		
 		Parcel[] parcels = new Parcel[parcelables.length];
 		
 		int totalParcelLength = 0;
 		for(int i = 0; i < parcels.length; i++){
 			parcels[i] = parcelables[i].toParcel();
 			totalParcelLength += parcels[i].toByteArray().length;
 		}
 		
 		Parcel parcel = new Parcel(totalParcelLength);
 		
 		for(int i = 0; i < parcels.length; i++){
 			parcel.add(parcels[i]);
 		}
 		
 		return parcel; 
 		
 		
 	}
 	
 	@Override
 	public String toString(){
 		return "Current Offset: " + getParcelSize() + 
 				"\t Total Capacity: " + capacity();
 	}
 }
