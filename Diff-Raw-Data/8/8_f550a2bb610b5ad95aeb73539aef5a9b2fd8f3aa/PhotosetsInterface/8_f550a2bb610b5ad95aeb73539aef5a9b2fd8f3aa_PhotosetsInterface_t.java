 /*
  * Copyright (c) 2005 Aetrion LLC.
  */
 package com.aetrion.flickr.photosets;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.aetrion.flickr.Flickr;
 import com.aetrion.flickr.FlickrException;
 import com.aetrion.flickr.Parameter;
 import com.aetrion.flickr.Response;
 import com.aetrion.flickr.Transport;
 import com.aetrion.flickr.people.User;
 import com.aetrion.flickr.photos.Photo;
 import com.aetrion.flickr.photos.PhotoContext;
 import com.aetrion.flickr.photos.PhotoList;
 import com.aetrion.flickr.photos.PhotoUtils;
 import com.aetrion.flickr.util.StringUtilities;
 import com.aetrion.flickr.util.XMLUtilities;
 
 /**
  * Interface for working with photosets.
  *
  * @author Anthony Eden
 * @version $Id: PhotosetsInterface.java,v 1.17 2007/03/13 22:59:05 x-mago Exp $
  */
 public class PhotosetsInterface {
 
     public static final String METHOD_ADD_PHOTO = "flickr.photosets.addPhoto";
     public static final String METHOD_CREATE = "flickr.photosets.create";
     public static final String METHOD_DELETE = "flickr.photosets.delete";
     public static final String METHOD_EDIT_META = "flickr.photosets.editMeta";
     public static final String METHOD_EDIT_PHOTOS = "flickr.photosets.editPhotos";
     public static final String METHOD_GET_CONTEXT = "flickr.photosets.getContext";
     public static final String METHOD_GET_INFO = "flickr.photosets.getInfo";
     public static final String METHOD_GET_LIST = "flickr.photosets.getList";
     public static final String METHOD_GET_PHOTOS = "flickr.photosets.getPhotos";
     public static final String METHOD_ORDER_SETS = "flickr.photosets.orderSets";
     public static final String METHOD_REMOVE_PHOTO = "flickr.photosets.removePhoto";
 
     public static final int PRIVACY_FILTER_NO_FILTER = 0;
     public static final int PRIVACY_FILTER_PUBLIC = 1;
     public static final int PRIVACY_FILTER_FRIENDS = 2;
     public static final int PRIVACY_FILTER_FAMILY = 3;
     public static final int PRIVACY_FILTER_FRIENDS_FAMILY = 4;
     public static final int PRIVACY_FILTER_PRIVATE = 5;
 
     private String apiKey;
     private Transport transportAPI;
 
     public PhotosetsInterface(String apiKey, Transport transport) {
         this.apiKey = apiKey;
         this.transportAPI = transport;
     }
 
     /**
      * Add a photo to the end of the photoset.
      * <p/>
      * Note: requires authentication with the new authentication API with 'write' permission.
      *
      * @param photosetId The photoset ID
      * @param photoId The photo ID
      */
     public void addPhoto(String photosetId, String photoId) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_ADD_PHOTO));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photoset_id", photosetId));
         parameters.add(new Parameter("photo_id", photoId));
 
         Response response = transportAPI.post(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     /**
      * Create a new photoset.
      *
      * @param title The photoset title
      * @param description The photoset description
      * @param primaryPhotoId The primary photo id
      * @return The new Photset
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Photoset create(String title, String description, String primaryPhotoId)
             throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_CREATE));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("title", title));
         parameters.add(new Parameter("description", description));
         parameters.add(new Parameter("primary_photo_id", primaryPhotoId));
 
         Response response = transportAPI.post(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
        Element photosetElement = (Element) response.getPayload();
         Photoset photoset = new Photoset();
         photoset.setId(photosetElement.getAttribute("id"));
         photoset.setUrl(photosetElement.getAttribute("url"));
         return photoset;
     }
 
     /**
      * Delete the specified photoset.
      *
      * @param photosetId The photoset ID
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public void delete(String photosetId) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_DELETE));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photoset_id", photosetId));
 
         Response response = transportAPI.post(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     /**
      * Delete the specified photoset.
      *
      * @param photosetId The photoset ID
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public void editMeta(String photosetId, String title, String description)
             throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_EDIT_META));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photoset_id", photosetId));
         parameters.add(new Parameter("title", title));
         if (description != null) {
             parameters.add(new Parameter("description", description));
         }
 
         Response response = transportAPI.post(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     /**
      * Edit which photos are in the photoset.
      *
      * @param photosetId The photoset ID
      * @param primaryPhotoId The primary photo Id
      * @param photoIds The photo IDs for the photos in the set
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public void editPhotos(String photosetId, String primaryPhotoId, String[] photoIds)
             throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_EDIT_PHOTOS));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photoset_id", photosetId));
         parameters.add(new Parameter("primary_photo_id", primaryPhotoId));
         parameters.add(new Parameter("photo_ids", StringUtilities.join(photoIds, ",")));
 
         Response response = transportAPI.post(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     /**
      * Get a photo's context in the specified photo set.
      *
      * @param photoId The photo ID
      * @param photosetId The photoset ID
      * @return The PhotoContext
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public PhotoContext getContext(String photoId, String photosetId)
             throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_CONTEXT));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photo_id", photoId));
         parameters.add(new Parameter("photoset_id", photosetId));
 
         Response response = transportAPI.get(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Collection payload = response.getPayloadCollection();
         Iterator iter = payload.iterator();
         PhotoContext photoContext = new PhotoContext();
         while (iter.hasNext()) {
             Element element = (Element) iter.next();
             String elementName = element.getTagName();
             if (elementName.equals("prevphoto")) {
                 Photo photo = new Photo();
                 photo.setId(element.getAttribute("id"));
                 photoContext.setPreviousPhoto(photo);
             } else if (elementName.equals("nextphoto")) {
                 Photo photo = new Photo();
                 photo.setId(element.getAttribute("id"));
                 photoContext.setNextPhoto(photo);
            } else if (elementName.equals("count")) {
            	// TODO: process this information
             } else {
                 System.err.println("unsupported element name: " + elementName);
             }
         }
         return photoContext;
     }
 
     /**
      * Get the information for a specified photoset.
      *
      * @param photosetId The photoset ID
      * @return The Photoset
      * @throws FlickrException
      * @throws IOException
      * @throws SAXException
      */
     public Photoset getInfo(String photosetId) throws FlickrException, IOException, SAXException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_INFO));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photoset_id", photosetId));
 
         Response response = transportAPI.post(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element photosetElement = (Element)response.getPayload();
         Photoset photoset = new Photoset();
         photoset.setId(photosetElement.getAttribute("id"));
 
         User owner = new User();
         owner.setId(photosetElement.getAttribute("owner"));
         photoset.setOwner(owner);
 
         Photo primaryPhoto = new Photo();
         primaryPhoto.setId(photosetElement.getAttribute("primary"));
         primaryPhoto.setSecret(photosetElement.getAttribute("secret")); // TODO verify that this is the secret for the photo
         primaryPhoto.setServer(photosetElement.getAttribute("server")); // TODO verify that this is the server for the photo
         primaryPhoto.setFarm(photosetElement.getAttribute("farm"));
         photoset.setPrimaryPhoto(primaryPhoto);
 
         // TODO remove secret/server/farm from photoset?
         // It's rather related to the primaryPhoto, then to the photoset itself.
         photoset.setSecret(photosetElement.getAttribute("secret"));
         photoset.setServer(photosetElement.getAttribute("server"));
         photoset.setFarm(photosetElement.getAttribute("farm"));
         photoset.setPhotoCount(photosetElement.getAttribute("photos"));
 
         photoset.setTitle(XMLUtilities.getChildValue(photosetElement, "title"));
         photoset.setDescription(XMLUtilities.getChildValue(photosetElement, "description"));
         photoset.setPrimaryPhoto(primaryPhoto);
 
         return photoset;
     }
 
     /**
      * Get a list of all photosets for the specified user.
      *
      * @param userId The User id
      * @return The Photosets object
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Photosets getList(String userId) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_LIST));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("user_id", userId));
 
         Response response = transportAPI.get(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Photosets photosetsObject = new Photosets();
         Element photosetsElement = response.getPayload();
         List photosets = new ArrayList();
         NodeList photosetElements = photosetsElement.getElementsByTagName("photoset");
         for (int i = 0; i < photosetElements.getLength(); i++) {
             Element photosetElement = (Element) photosetElements.item(i);
             Photoset photoset = new Photoset();
             photoset.setId(photosetElement.getAttribute("id"));
 
             User owner = new User();
             owner.setId(photosetElement.getAttribute("owner"));
             photoset.setOwner(owner);
 
             Photo primaryPhoto = new Photo();
             primaryPhoto.setId(photosetElement.getAttribute("primary"));
             primaryPhoto.setSecret(photosetElement.getAttribute("secret")); // TODO verify that this is the secret for the photo
             primaryPhoto.setServer(photosetElement.getAttribute("server")); // TODO verify that this is the server for the photo
             primaryPhoto.setFarm(photosetElement.getAttribute("farm"));
             photoset.setPrimaryPhoto(primaryPhoto);
 
             photoset.setSecret(photosetElement.getAttribute("secret"));
             photoset.setServer(photosetElement.getAttribute("server"));
             photoset.setFarm(photosetElement.getAttribute("farm"));
             photoset.setPhotoCount(photosetElement.getAttribute("photos"));
 
             photoset.setTitle(XMLUtilities.getChildValue(photosetElement, "title"));
             photoset.setDescription(XMLUtilities.getChildValue(photosetElement, "description"));
 
             photosets.add(photoset);
         }
 
         photosetsObject.setPhotosets(photosets);
         return photosetsObject;
     }
 
     /**
      * Get a collection of Photo objects for the specified Photoset.
      *
      * @param photosetId The photoset ID
      * @param extras Hash of extra-fields
      * @param privacy_filter filter value for authenticated calls
      * @param perPage The number of photos per page
      * @param page The page offset
      * @return PhotoList The Collection of Photo objects
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public PhotoList getPhotos(String photosetId, Set extras,
       int privacy_filter, int perPage, int page)
       throws IOException, SAXException, FlickrException {
         PhotoList photos = new PhotoList();
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_PHOTOS));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photoset_id", photosetId));
 
         if (perPage > 0) {
             parameters.add(new Parameter("per_page", new Integer(perPage)));
         }
 
         if (page > 0) {
             parameters.add(new Parameter("page", new Integer(page)));
         }
 
         if (privacy_filter > 0) {
             parameters.add(new Parameter("privacy_filter", "" + privacy_filter));
         }
 
         if (extras != null) {
             StringBuffer sb = new StringBuffer();
             Iterator it = extras.iterator();
             for (int i = 0; it.hasNext(); i++) {
                 if (i > 0) {
                     sb.append(",");
                 }
                 sb.append(it.next());
             }
             parameters.add(new Parameter(Flickr.KEY_EXTRAS, sb.toString()));
         }
 
         Response response = transportAPI.get(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
 
         Element photoset = response.getPayload();
         NodeList photoElements = photoset.getElementsByTagName("photo");
         photos.setPage(photoset.getAttribute("page"));
         photos.setPages(photoset.getAttribute("pages"));
         photos.setPerPage(photoset.getAttribute("per_page"));
         photos.setTotal(photoset.getAttribute("total"));
 
         for (int i = 0; i < photoElements.getLength(); i++) {
             Element photoElement = (Element) photoElements.item(i);
             photos.add(PhotoUtils.createPhoto(photoElement));
         }
 
         return photos;
     }
 
     /**
      * Convenience method.
      *
      * @param photosetId The photoset ID
      * @param perPage The number of photos per page
      * @param page The page offset
      * @return PhotoList The Collection of Photo objects
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public PhotoList getPhotos(String photosetId, int perPage, int page) 
       throws IOException, SAXException, FlickrException {
         return getPhotos(photosetId, Flickr.MIN_EXTRAS, PRIVACY_FILTER_NO_FILTER, perPage, page);
     }
 
     /**
      * Set the order in which sets are returned for the user.
      *
      * @param photosetIds
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public void orderSets(String[] photosetIds) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_ORDER_SETS));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photoset_ids", StringUtilities.join(photosetIds, ",")));
 
         Response response = transportAPI.post(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     /**
      * Remove a photo from the set.
      *
      * @param photosetId The photoset ID
      * @param photoId The photo ID
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public void removePhoto(String photosetId, String photoId) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_REMOVE_PHOTO));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photoset_id", photosetId));
         parameters.add(new Parameter("photo_id", photoId));
 
         Response response = transportAPI.post(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
 }
