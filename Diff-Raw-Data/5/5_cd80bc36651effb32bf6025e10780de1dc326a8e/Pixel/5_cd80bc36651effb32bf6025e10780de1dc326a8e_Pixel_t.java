 package de.ifcore.metis.client.pixel;
 
 public class Pixel
 {
 	private final String publicId;
 	private final String privateId;
 	private final String host;
 
 	public Pixel(String publicId, String privateId, String host)
 	{
 		this.publicId = publicId;
 		this.privateId = privateId;
 		this.host = host;
 	}
 
	public String getPublicId()
 	{
 		return publicId;
 	}
 
	public String getPrivateId()
 	{
 		return privateId;
 	}
 
 	public String getHost()
 	{
 		return host;
 	}
 
 	@Override
 	public String toString()
 	{
 		return "Pixel [publicId=" + publicId + ", privateId=" + privateId + ", host=" + host + "]";
 	}
 }
