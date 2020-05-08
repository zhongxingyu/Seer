 package eu.socialsensor.framework.abstractions.flickr;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 
 import com.aetrion.flickr.people.User;
 import com.aetrion.flickr.photos.GeoData;
 import com.aetrion.flickr.photos.Photo;
 import com.aetrion.flickr.tags.Tag;
 
 import eu.socialsensor.framework.common.domain.Feed;
 import eu.socialsensor.framework.common.domain.Item;
 import eu.socialsensor.framework.common.domain.Location;
 import eu.socialsensor.framework.common.domain.MediaItem;
 import eu.socialsensor.framework.common.domain.SocialNetworkSource;
 import eu.socialsensor.framework.common.domain.Source;
 
 /**
  * Class that holds the information regarding the flickr photo
  * @author ailiakop
  * @email  ailiakop@iti.gr
  */
 public class FlickrItem extends Item {
 
 	public FlickrItem(String id, Operation operation) {
 		super(SocialNetworkSource.Flickr.toString(), operation);
 		setId(SocialNetworkSource.Flickr+"#"+id);
 	}
 	
 	public FlickrItem(Photo photo) {
 		super(SocialNetworkSource.Flickr.toString(), Operation.NEW);
 		if (photo == null || photo.getId() == null) return;
 		
 		//Id
 		id = SocialNetworkSource.Flickr + "#" + photo.getId();
 		//SocialNetwork Name
 		streamId = SocialNetworkSource.Flickr.toString();
 		//Timestamp of the creation of the photo
 		publicationTime = photo.getDatePosted().getTime();
 		//Title of the photo
 		if(photo.getTitle()!=null){
 			if(photo.getTitle().length()>100){
 				title = photo.getTitle().subSequence(0, 100)+"...";
 				text = photo.getTitle();
 			}
 			else{
 				title = photo.getTitle();
 				text = photo.getTitle();
 			}
 		}
 		//Description of the photo
 		description = photo.getDescription();
 		//Tags of the photo
 		@SuppressWarnings("unchecked")
 		Collection<Tag> photoTags = photo.getTags();
 		if (photoTags != null) {
 			tags = new String[photoTags.size()];
 			int i = 0;
 			for(Tag tag : photoTags) {
 				tags[i++] = tag.getValue();
 			}
 		}
 		//User that posted the photo
         User user = photo.getOwner();
         if(user != null) {
                 streamUser = new FlickrStreamUser(user);
                 uid = streamUser.getId();
         }
 		//Location
 		if(photo.hasGeoData()){
 			
 			GeoData geo = photo.getGeoData();
 			
 			double latitude = (double)geo.getLatitude();
 			double longitude = (double) geo.getLongitude();
 			
 			location = new Location(latitude, longitude);
 		}
 		//Popularity
 		
 		//Getting the photo
 	
 		String url = null;
 		try {
 			String thumbnail = photo.getMediumUrl();
 			if(thumbnail==null) {
 				thumbnail = photo.getThumbnailUrl();
 			}
 			URL mediaUrl = null;
 			if((url = photo.getLargeUrl()) != null) {
 				mediaUrl = new URL(url);
 			
 			}
 			else if ((url = photo.getMediumUrl()) != null) {
 				mediaUrl = new URL(url);
 			}
 			
 			if(mediaUrl!=null){
 				//url
 				MediaItem mediaItem = new MediaItem(mediaUrl);
 				
 				String mediaId = SocialNetworkSource.Flickr + "#"+photo.getId(); 
 				
 				//id
 				mediaItem.setId(mediaId);
 				//SocialNetwork Name
 				mediaItem.setStreamId(streamId);
 				//Reference
 				mediaItem.setRef(id);
 				//Type 
 				mediaItem.setType("image");
 				//Time of publication
 				mediaItem.setPublicationTime(publicationTime);
 				//PageUrl
 				mediaItem.setPageUrl(photo.getUrl());
 				//Thumbnail
 				mediaItem.setThumbnail(thumbnail);
 				//Title
 				mediaItem.setTitle(title);
 				//Description
 				mediaItem.setDescription(description);
 				//Tags
 				mediaItem.setTags(tags);
 				//Popularity
 				mediaItem.setComments(new Long(photo.getComments()));
 				//Location
 				mediaItem.setLocation(location);
 				
 				//Store mediaItems and their ids 
 				mediaItems.add(mediaItem);
 				mediaIds.add(mediaId);
 				
 			}
 			
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	public FlickrItem(Photo photo,Feed itemFeed) {
 		this(photo);
 
 		feed = itemFeed;
 		feedType = itemFeed.getFeedtype().toString();
 
 	}
 
 }
