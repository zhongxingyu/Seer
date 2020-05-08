 package cansocket;
 
 import java.io.*;
 
 public class GpsOriginMessage extends NetMessage
 {
 	public static final short fifo_tag = FMT_GPS_ORIGIN;
	public static final short MSG_SIZE = 
			 8 /*lat*/ + 8 /*lon*/ + 8 /* height */;
 	public final double latitude, longitude, height; 
	
	public GpsOriginMessage(double lat, double lon, double height)
	{
		this.latitude = lat;
		this.longitude = lon;
		this.height = height;
	}
 
 	public GpsOriginMessage(DataInputStream dis) throws IOException
 	{
		 this(
				dis.readDouble() /* latitude */,
				dis.readDouble() /* longitude */,
				dis.readDouble() /* height */
				);
	}
	
	public byte[] toByteArray()
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(HEADER_SIZE+MSG_SIZE);
		DataOutputStream dos = new DataOutputStream(bos);
		putMessage(dos); 
		return bos.toByteArray();
 	}
 
 	
 	public void putMessage(DataOutputStream dos)
 	{
 		try
 		{
				putHeader(dos, MSG_SIZE, fifo_tag);
				dos.writeDouble(latitude);
				dos.writeDouble(longitude);
				dos.writeDouble(height);
 		} catch(IOException e) {
 			// never happens
 		}
 	}
 
 	public String toString()
 	{
 		return "GPS orig lat:" + Double.toString(latitude) + " GPS orig long:" + Double.toString(longitude) + "GPS orig height" + Double.toString(height);
 	}
 }
 
