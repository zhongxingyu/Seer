 /**
  * 
  */
 package com.github.yuyang226.j500px.photos;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.json.JSONException;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.github.yuyang226.j500px.J500pxException;
 import com.github.yuyang226.j500px.oauth.AbstractJ500pxTest;
 import com.github.yuyang226.j500px.oauth.RequestContext;
 import com.github.yuyang226.j500px.users.UserList;
 
 /**
  * @author yayu
  *
  */
 public class PhotosInterfaceTest extends AbstractJ500pxTest {
 
 	/**
 	 * Test method for {@link com.github.yuyang226.j500px.photos.PhotosInterface#getPhotos(com.github.yuyang226.j500px.photos.GlobalFeatures, com.github.yuyang226.j500px.photos.SearchSort, int, int)}.
 	 * @throws JSONException 
 	 * @throws IOException 
 	 * @throws J500pxException 
 	 */
 	@Test
 	public void testGetPhotos() throws J500pxException, IOException, JSONException {
 		PhotoList photoList = p.getPhotosInterface().getPhotos(GlobalFeatures.EDITORS, null, PhotoCategory.BlackAndWhite, 
 				null, new ImageSize[]{ImageSize.THUMNAIL}, 
 				null, false, false, false, -1, -1);
 		assertNotNull(photoList);
 		assertFalse(photoList.isEmpty());
 	}
 	
 	@Test
 	public void testGetPhotosNoAuth() throws J500pxException, IOException, JSONException {
 		RequestContext.getRequestContext().setOAuth(null);
 		List<Photo> photoList = p.getPhotosInterface().getPhotos(GlobalFeatures.POPULAR, 
 				null, null, null, new ImageSize[]{ImageSize.THUMNAIL, ImageSize.LARGEST}, null, 
 				true, true, true, 1, 20);
 		assertNotNull(photoList);
 		assertFalse(photoList.isEmpty());
 	}
 	
 	@Test
 	public void testGetUserPhotos() throws J500pxException, IOException, JSONException {
		PhotoList photoList = p.getPhotosInterface().getUserPhotos(null, "753809", null, null, null, null, new ImageSize[] {ImageSize.LARGEST}, -1, -1);
 		assertNotNull(photoList);
 		assertFalse(photoList.isEmpty());
 	}
 	
 	@Test
 	public void testGetUserFavouritesPhotos() throws J500pxException, IOException, JSONException {
		PhotoList photoList = p.getPhotosInterface().getUserPhotos(GlobalFeatures.USER_FAVORITES, "753809", null, null, null, null, new ImageSize[] {ImageSize.LARGEST}, -1, -1);
 		assertNotNull(photoList);
 		assertFalse(photoList.isEmpty());
 	}
 	
 	@Test
 	public void testGetUserFriendsPhotos() throws J500pxException, IOException, JSONException {
 		PhotoList photoList = p.getPhotosInterface().getUserPhotos(GlobalFeatures.USER_FRIENDS, "753809", null, 
				SearchSort.RATING, null, null, new ImageSize[] {ImageSize.LARGEST}, -1, -1);
 		assertNotNull(photoList);
 		assertFalse(photoList.isEmpty());
 	}
 	
 	@Test
 	public void testGetPhotoDetail() throws J500pxException, IOException, JSONException {
 		Photo photo = p.getPhotosInterface().getPhotoDetail(22161277, null, true, -1);
 		assertNotNull(photo);
 	}
 	
 	@Test
 	public void testGetPhotoComments() throws IOException, JSONException, J500pxException {
 		CommentList comments = p.getPhotosInterface().getPhotoComments(22151841, -1);
 		assertFalse(comments.isEmpty());
 	}
 	
 	@Test
 	public void testGetPhotoVotedUsers() throws IOException, JSONException, J500pxException {
 		UserList userList = p.getPhotosInterface().getPhotoVotedUsers(22151841, -1, -1);
 		assertFalse(userList.isEmpty());
 	}
 	
 	@Test
 	public void testGetPhotoFavedUsers() throws IOException, JSONException, J500pxException {
 		UserList userList = p.getPhotosInterface().getPhotoFavedUsers(22151841, -1, -1);
 		assertFalse(userList.isEmpty());
 	}
 	
 	@Test
 	public void testSearchPhotos() throws J500pxException, IOException, JSONException {
 		PhotoList photoList = p.getPhotosInterface().searchPhotos("Nikon", "Nikon", true, null, -1, -1);
 		assertNotNull(photoList);
 		assertFalse(photoList.isEmpty());
 	}
 	
 	@Test
 	public void testSearchPhotosOneArgs() throws J500pxException, IOException, JSONException {
 		PhotoList photoList = p.getPhotosInterface().searchPhotos(null, "nikon", false, null, -1, -1);
 		assertNotNull(photoList);
 		assertFalse(photoList.isEmpty());
 	}
 	
 	@Test
 	public void testCreateNewPhoto() throws J500pxException, IOException, JSONException {
 		final String name = "test";
 		PhotoUpload photoUpload = p.getPhotosInterface()
 				.createNewPhoto(name, null, PhotoCategory.Uncategorized, null, null, null, null);
 		assertNotNull(photoUpload);
 		assertNotNull(photoUpload.getUploadKey());
 		assertEquals(name, photoUpload.getPhoto().getName());
 	}
 	
 	@Test
 	@Ignore("one image can only be voted once and once only!")
 	public void testVotePhoto() throws J500pxException, IOException, JSONException {
 		int photoId = 22768699;
 		Photo photoVoted = p.getPhotosInterface().votePhoto(photoId, true);
 		assertNotNull(photoVoted);
 		assertEquals(photoVoted.getId(), photoId);
 	}
 	
 	@Test
 	public void testLikePhoto() throws J500pxException, IOException, JSONException {
 		int photoId = 22151841;
 		p.getPhotosInterface().likePhoto(photoId, false);
 		p.getPhotosInterface().likePhoto(photoId, true);
 	}
 	
 	@Test
 	public void testCommentPhoto() throws J500pxException, IOException, JSONException {
 		int photoId = 22151841;
 		p.getPhotosInterface().commentPhoto(photoId, "Test");
 	}
 	
 }
