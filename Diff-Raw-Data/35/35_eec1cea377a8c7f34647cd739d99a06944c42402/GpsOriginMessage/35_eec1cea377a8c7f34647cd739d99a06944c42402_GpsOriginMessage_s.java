 package cansocket;
 
 import java.io.*;
 
 public class GpsOriginMessage extends NetMessage
 {
 	public static final short fifo_tag = FMT_GPS_ORIGIN;
 	public final double latitude, longitude, height; 
 
 	public GpsOriginMessage(DataInputStream dis) throws IOException
 	{
		latitude = dis.readDouble();
		longitude = dis.readDouble();
		height = dis.readDouble();
 	}
 
 	
 	public void putMessage(DataOutputStream dos)
 	{
 		try
 		{
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
 
