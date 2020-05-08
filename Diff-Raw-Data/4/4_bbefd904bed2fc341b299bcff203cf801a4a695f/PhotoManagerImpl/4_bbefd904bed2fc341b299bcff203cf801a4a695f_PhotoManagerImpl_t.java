 package org.mdissjava.mdisscore.controller.bll.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.Date;
 import java.util.List;
 
 import org.mdissjava.mdisscore.controller.bll.PhotoManager;
 import org.mdissjava.mdisscore.model.dao.PhotoDao;
 import org.mdissjava.mdisscore.model.dao.factory.MorphiaDatastoreFactory;
 import org.mdissjava.mdisscore.model.dao.impl.PhotoDaoImpl;
 import org.mdissjava.mdisscore.model.pojo.Album;
 import org.mdissjava.mdisscore.model.pojo.Metadata;
 import org.mdissjava.mdisscore.model.pojo.Photo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.morphia.Datastore;
 
 /**
  * Manager for photo 
  * 
  * 
  * @author slok
  *
  */
 public class PhotoManagerImpl implements PhotoManager{
 	
 	//TODO: Load from properties
 	private final String DATABASE = "mdissphoto"; 
 	private PhotoDao photoDao;
 	private Datastore datastore;
 	private final Logger logger = LoggerFactory.getLogger(this.getClass());
 	
 	
 	public PhotoManagerImpl(Datastore datastore) {
 		this.datastore = datastore;
 		this.photoDao = new PhotoDaoImpl(this.datastore);
 	}
 	
 	public PhotoManagerImpl() {
 		this.datastore = MorphiaDatastoreFactory.getDatastore(DATABASE);
 		this.photoDao = new PhotoDaoImpl(this.datastore);
 	}
 	
 	/**
 	 * Splits the tags from a regular expression, if the regex is null then by default will split comma separated 
 	 * 
 	 * @param tags
 	 * @return
 	 */
 	private ArrayList<String> splitTags(String tags, String regex)
 	{
 		this.logger.debug("Splitting tags");
 		if (regex == null)
 			regex = "\\,";
 		
 		String[] splittedTags = tags.split(regex);
 		
 		ArrayList<String> tagList = new ArrayList<String>();
 		for (String i:splittedTags)
 			tagList.add(i);
 		
 		return tagList;
 	}
 	
 	/**
 	 * Inserts a photo with the data given the necessary arguments, some are necessary other no
 	 * 
 	 * @param imageId
 	 * @param userNickname
 	 * @param title
 	 * @param albumTitle
 	 * @param publicPhoto
 	 * @param plus18
 	 * @param license
 	 * @param tags
 	 * @throws IllegalArgumentException
 	 * @throws IOException 
 	 */
 	@Override
 	public void insertPhoto(String imageId, String userNickname, String title, 
 							String albumTitle, boolean publicPhoto, boolean plus18, 
 							String license, String tags) throws IllegalArgumentException, IOException{
 		
 		this.logger.debug("Inserting new photo {} in {}", title, albumTitle);
 		
 		//the other are not necessary, only title, user, imageId and the album
 		if (imageId.isEmpty() || userNickname.isEmpty() || 
 			title.isEmpty() || albumTitle.isEmpty())
 		{
 			this.logger.error("Some arguments is/are null, can't continue with the action");
 			throw new IllegalArgumentException("Some arguments are null, can't continue with the action");
 		}
 		
 		//create the photo
 		Photo p = new Photo();
 		p.setDataId(imageId);
 		p.setPhotoId(imageId);
 		//p.setLicense(license);
 		p.setTitle(title);
 		p.setPublicPhoto(publicPhoto);
 		p.setPlus18(plus18);
		//set date to now!
		p.setUploadDate(new Date());
 		
 		if (tags.isEmpty())
 			p.setTags(null);
 		else
 			p.setTags(this.splitTags(tags, "\\,"));
 		
 		//sometimes the metadata is inserted before all this data so we retrieve metadata, 
 		//delete the photo an insert the new photo with all the data, the reason of deleting
 		//and not updating the photo is because there is many login in the insertion and update, 
 		//and for this the used database is mongo, so itś amazingly fast :)
 		Metadata metadata = null;
 		try
 		{
 			Photo pAux = this.searchPhotoUniqueUtil(imageId);
 			metadata = pAux.getMetadata();
 			
 			//if the saved photo is metadata only (and not title) means that is a metadata saved and not a previous photo
 			//so we delete it
 			if (metadata != null && pAux.getTitle() == null)
 			{
 				this.photoDao.deletePhoto(pAux);
 				//asign the metadata to the new photo
 				p.setMetadata(metadata);
 			}
 			
 		}catch(IOException e)
 		{
 			this.logger.debug("No metadata for this photo in the database available");
 		}finally{
 			//call to the save with or without metadata
 			new AlbumManagerImpl(datastore).addNewPhotoToAlbum(userNickname, albumTitle, p);
 			
 		}
 		
 		
 		
 	}
 	
 	public void insertMetadata(String photoId, Metadata metadata) {
 		
 		//the other are not necessary, only title, user, imageId and the album
 		if (photoId.isEmpty() || metadata == null)
 		{
 			this.logger.error("Some arguments is/are null, can't continue with the action");
 			throw new IllegalArgumentException("Some arguments are null, can't continue with the action");
 		}
 		Photo p = new Photo();
 		p.setPhotoId(photoId);
 		p.setMetadata(metadata);
 		
 		this.photoDao.insertPhoto(p);
 		
 	}
 
 
 	/**
 	 * Finds photo(s) from a given pojo with some arguments 
 	 * 
 	 * @param photo
 	 * @return a list of photos
 	 * @throws IllegalArgumentException
 	 */
 	@Override
 	public List<Photo> findPhoto(Photo photo) throws IllegalArgumentException{
 		
 		this.logger.debug("Searching for a photos");
 		
 		if (photo == null)
 		{
 			this.logger.error("Photo argument is null, can't continue with the action");
 			throw new IllegalArgumentException("Photo argument is null, can't continue with the action");
 		}
 		return this.photoDao.findPhoto(photo);
 		
 			
 	}
 
 	/**
 	 * Updates a photo
 	 * 
 	 * @param photo
 	 * @throws IllegalArgumentException
 	 */
 	@Override
 	public void updatePhoto(Photo photo) throws IllegalArgumentException{
 		
 		this.logger.debug("Updating photo");
 		
 		if (photo == null)
 		{
 			this.logger.error("Photo argument is null, can't continue with the action");
 			throw new IllegalArgumentException("Photo argument is null, can't continue with the action");
 		}
 		this.photoDao.updatePhoto(photo);
 		
 	}
 
 	/**
 	 * Deletes a photo from a pojo and deletes the photo reference from the album's list
 	 * 
 	 * @param photo
 	 * @throws IllegalArgumentException
 	 */
 	@Override
 	public void deletePhoto(Photo photo) throws IllegalArgumentException{
 		
 		this.logger.debug("Deleting photo");
 		
 		if (photo == null)
 		{
 			this.logger.error("Photo argument is null, can't continue with the action");
 			throw new IllegalArgumentException("Photo argument is null, can't continue with the action");
 		}
 		
 		//Delete photo from the album too
 		Album a = photo.getAlbum();
 		if (a != null)
 		{	
 			a.getPhotos().remove(photo);
 			new AlbumManagerImpl(datastore).updateAlbum(a);
 		}
 		
 		//Delete the photo
 		this.photoDao.deletePhoto(photo);
 		
 	}
 
 	/**
 	 * deletes a photo from a given photoId
 	 * 
 	 * @param photoId
 	 * @throws IOException
 	 */
 	@Override
 	public void deletePhoto(String photoId) throws IOException {
 		Photo photo = this.searchPhotoUniqueUtil(photoId);
 		this.deletePhoto(photo);
 		
 	}
 	
 	
 	/**
 	 * Searchs a photo from a given id
 	 * 
 	 * @param photoId
 	 * @return the photo pojo itself
 	 * @throws IOException
 	 */
 	public Photo searchPhotoUniqueUtil(String photoId) throws IOException
 	{
 		
 		this.logger.debug("Searching for the photo with {} name", photoId);
 		
 		List<Photo> pList = new ArrayList<Photo>();
 		Photo p = new Photo();
 		p.setPhotoId(photoId);
 		
 		pList = this.photoDao.findPhoto(p);
 		
 		if (pList.isEmpty())
 		{
 			this.logger.error("No {} photo named is stored in database", photoId);
 			throw new IOException("No " + photoId + " photo named is stored in database");
 		}
 		int size = pList.size();
 		if (size > 1)
 		{
 			
 			this.logger.error("Too many photos found ({}), expected only one", size);
 			throw new IllegalStateException("Too many photos found(" + size + "), expected only one");
 		}
 		return pList.get(0);
 		
 	}
 
 	
 	
 }
