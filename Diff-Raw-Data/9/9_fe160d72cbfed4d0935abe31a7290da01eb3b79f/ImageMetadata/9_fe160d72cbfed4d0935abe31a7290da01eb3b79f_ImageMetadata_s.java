 package domain;
 
public final class ImageMetadata {
 	
 	private String siteUrl, imageUrl;
 	
 	public ImageMetadata(String siteUrl, String imageUrl)
 	{
 		this.siteUrl = siteUrl;
 		this.imageUrl = imageUrl;
 	}
 	
 	public static ImageMetadata valueOf(String siteUrl, String imageUrl){
 		return new ImageMetadata(siteUrl, imageUrl);
 	}
 
 	public String getImageUrl()
 	{
 		return imageUrl;
 	}
 	
 	public String getSiteUrl()
 	{
 		return siteUrl;
 	}
 
 	@Override
 	public int hashCode()
 	{
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((imageUrl == null) ? 0 : imageUrl.hashCode());
 		result = prime * result + ((siteUrl == null) ? 0 : siteUrl.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj)
 	{
 		if ( this == obj )
 			return true;
 		if ( obj == null )
 			return false;
 		if ( getClass() != obj.getClass() )
 			return false;
 		ImageMetadata other = (ImageMetadata) obj;
 		if ( imageUrl == null ) {
 			if ( other.imageUrl != null )
 				return false;
 		} else if ( !imageUrl.equals(other.imageUrl) )
 			return false;
 		if ( siteUrl == null ) {
 			if ( other.siteUrl != null )
 				return false;
 		} else if ( !siteUrl.equals(other.siteUrl) )
 			return false;
 		return true;
 	}
 	
 }
