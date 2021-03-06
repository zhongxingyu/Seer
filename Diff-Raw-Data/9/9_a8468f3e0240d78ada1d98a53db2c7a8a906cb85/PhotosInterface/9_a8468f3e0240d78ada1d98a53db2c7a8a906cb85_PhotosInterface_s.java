 /*
  * Copyright (c) 2005 Aetrion LLC.
  */
 package com.aetrion.flickr.photos;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import com.aetrion.flickr.FlickrException;
 import com.aetrion.flickr.Parameter;
 import com.aetrion.flickr.RequestContext;
 import com.aetrion.flickr.Response;
 import com.aetrion.flickr.Transport;
 import com.aetrion.flickr.people.User;
 import com.aetrion.flickr.tags.Tag;
 import com.aetrion.flickr.util.StringUtilities;
 import com.aetrion.flickr.util.XMLUtilities;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.SAXException;
 
 /**
  * @author Anthony Eden
  */
 public class PhotosInterface {
 
     public static final String METHOD_ADD_TAGS = "flickr.photos.addTags";
     public static final String METHOD_GET_CONTACTS_PHOTOS = "flickr.photos.getContactsPhotos";
     public static final String METHOD_GET_CONTACTS_PUBLIC_PHOTOS = "flickr.photos.getContactsPublicPhotos";
     public static final String METHOD_GET_CONTEXT = "flickr.photos.getContext";
     public static final String METHOD_GET_COUNTS = "flickr.photos.getCounts";
     public static final String METHOD_GET_EXIF = "flickr.photos.getExif";
     public static final String METHOD_GET_INFO = "flickr.photos.getInfo";
     public static final String METHOD_GET_NOT_IN_SET = "flickr.photos.getNotInSet";
     public static final String METHOD_GET_PERMS = "flickr.photos.getPerms";
     public static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
     public static final String METHOD_GET_SIZES = "flickr.photos.getSizes";
     public static final String METHOD_GET_UNTAGGED = "flickr.photos.getUntagged";
     public static final String METHOD_REMOVE_TAG = "flickr.photos.removeTag";
     public static final String METHOD_SEARCH = "flickr.photos.search";
     public static final String METHOD_SET_DATES = "flickr.photos.setDates";
     public static final String METHOD_SET_META = "flickr.photos.setMeta";
     public static final String METHOD_SET_PERMS = "flickr.photos.setPerms";
     public static final String METHOD_SET_TAGS = "flickr.photos.setTags";
 
     private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
     private String apiKey;
     private Transport transport;
 
     public PhotosInterface(String apiKey, Transport transport) {
         this.apiKey = apiKey;
         this.transport = transport;
     }
 
     /**
      * Add tags to a photo.
      *
      * @param photoId The photo ID
      * @param tags The tags
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public void addTags(String photoId, String[] tags) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_ADD_TAGS));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photo_id", photoId));
         parameters.add(new Parameter("tags", StringUtilities.join(tags, " ")));
 
         Response response = transport.post(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     /**
      * Get photos from the user's contacts.
      *
      * @param count The number of photos to return
      * @param justFriends Set to true to only show friends photos
      * @param singlePhoto Set to true to get a single photo
      * @param includeSelf Set to true to include self
      * @return The Collection of photos
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Collection getContactsPhotos(int count, boolean justFriends, boolean singlePhoto, boolean includeSelf)
             throws IOException, SAXException, FlickrException {
         List photos = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_CONTACTS_PHOTOS));
         parameters.add(new Parameter("api_key", apiKey));
 
         if (count > 0) {
             parameters.add(new Parameter("count", new Integer(count)));
         }
         if (justFriends) {
             parameters.add(new Parameter("just_friends", "1"));
         }
         if (singlePhoto) {
             parameters.add(new Parameter("single_photo", "1"));
         }
         if (includeSelf) {
             parameters.add(new Parameter("include_self", "1"));
         }
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element photosElement = response.getPayload();
         NodeList photoNodes = photosElement.getElementsByTagName("photo");
         for (int i = 0; i < photoNodes.getLength(); i++) {
             Element photoElement = (Element) photoNodes.item(i);
             Photo photo = new Photo();
             photo.setId(photoElement.getAttribute("id"));
 
             User owner = new User();
             owner.setId(photoElement.getAttribute("owner"));
             owner.setUsername(photoElement.getAttribute("username"));
             photo.setOwner(owner);
 
             photo.setSecret(photoElement.getAttribute("secret"));
             photo.setServer(photoElement.getAttribute("server"));
             photo.setTitle(photoElement.getAttribute("title"));
             photo.setPublicFlag("1".equals(photoElement.getAttribute("ispublic")));
             photo.setFriendFlag("1".equals(photoElement.getAttribute("isfriend")));
             photo.setFamilyFlag("1".equals(photoElement.getAttribute("isfamily")));
 
             photos.add(photo);
         }
         return photos;
     }
 
     /**
      * Get public photos from the user's contacts.
      *
      * @param userId The user ID
      * @param count The number of photos to return
      * @param justFriends True to include friends
      * @param singlePhoto True to get a single photo
      * @param includeSelf True to include self
      * @return A collection of Photo objects
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Collection getContactsPublicPhotos(String userId, int count, boolean justFriends, boolean singlePhoto, boolean includeSelf)
             throws IOException, SAXException, FlickrException {
         List photos = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_CONTACTS_PUBLIC_PHOTOS));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("user_id", userId));
 
         if (count > 0) {
             parameters.add(new Parameter("count", new Integer(count)));
         }
         if (justFriends) {
             parameters.add(new Parameter("just_friends", "1"));
         }
         if (singlePhoto) {
             parameters.add(new Parameter("single_photo", "1"));
         }
         if (includeSelf) {
             parameters.add(new Parameter("include_self", "1"));
         }
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element photosElement = response.getPayload();
         NodeList photoNodes = photosElement.getElementsByTagName("photo");
         for (int i = 0; i < photoNodes.getLength(); i++) {
             Element photoElement = (Element) photoNodes.item(i);
             Photo photo = new Photo();
             photo.setId(photoElement.getAttribute("id"));
 
             User owner = new User();
             owner.setId(photoElement.getAttribute("owner"));
             owner.setUsername(photoElement.getAttribute("username"));
             photo.setOwner(owner);
 
             photo.setSecret(photoElement.getAttribute("secret"));
             photo.setServer(photoElement.getAttribute("server"));
             photo.setTitle(photoElement.getAttribute("name"));
 
             photos.add(photo);
         }
         return photos;
     }
 
     /**
      * Get the context for the specified photo.
      *
      * @param photoId The photo ID
      * @return The PhotoContext
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public PhotoContext getContext(String photoId) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_CONTEXT));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photo_id", photoId));
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         PhotoContext photoContext = new PhotoContext();
         Collection payload = response.getPayloadCollection();
         Iterator iter = payload.iterator();
         while (iter.hasNext()) {
             Element payloadElement = (Element) iter.next();
             String tagName = payloadElement.getTagName();
             if (tagName.equals("prevphoto")) {
                 Photo photo = new Photo();
                 photo.setId(payloadElement.getAttribute("id"));
                 photo.setSecret(payloadElement.getAttribute("secret"));
                 photo.setTitle(payloadElement.getAttribute("title"));
                 photo.setUrl(payloadElement.getAttribute("url"));
                 photoContext.setPreviousPhoto(photo);
             } else if (tagName.equals("nextphoto")) {
                 Photo photo = new Photo();
                 photo.setId(payloadElement.getAttribute("id"));
                 photo.setSecret(payloadElement.getAttribute("secret"));
                 photo.setTitle(payloadElement.getAttribute("title"));
                 photo.setUrl(payloadElement.getAttribute("url"));
                 photoContext.setNextPhoto(photo);
             }
         }
         return photoContext;
     }
 
     /**
      * Gets a collection of photo counts for the given date ranges for the calling user.
      *
      * @param dates An array of dates, denoting the periods to return counts for. They should be specified smallest
      * first.
      * @param takenDates An array of dates, denoting the periods to return counts for. They should be specified smallest
      * first.
      * @return A Collection of Photocount objects
      */
 
     public Collection getCounts(Date[] dates, Date[] takenDates) throws IOException, SAXException,
             FlickrException {
         List photocounts = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_COUNTS));
         parameters.add(new Parameter("api_key", apiKey));
 
         if (dates == null && takenDates == null) {
             throw new IllegalArgumentException("You must provide a value for either dates or takenDates");
         }
 
         if (dates != null) {
             List dateList = new ArrayList();
             for (int i = 0; i < dates.length; i++) {
                 dateList.add(dates[i]);
             }
             parameters.add(new Parameter("dates", StringUtilities.join(dateList, ",")));
         }
 
         if (takenDates != null) {
             List dateList = new ArrayList();
             for (int i = 0; i < dates.length; i++) {
                 dateList.add(dates[i]);
             }
             parameters.add(new Parameter("taken_dates", StringUtilities.join(dateList, ",")));
         }
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element photocountsElement = response.getPayload();
         NodeList photocountNodes = photocountsElement.getElementsByTagName("photocount");
         for (int i = 0; i < photocountNodes.getLength(); i++) {
             Element photocountElement = (Element) photocountNodes.item(i);
             Photocount photocount = new Photocount();
             photocount.setCount(photocountElement.getAttribute("count"));
             photocount.setFromDate(photocountElement.getAttribute("fromdate"));
             photocount.setToDate(photocountElement.getAttribute("todate"));
             photocounts.add(photocount);
         }
         return photocounts;
     }
 
     /**
      * Get the Exif data for the photo.
      *
      * @param photoId The photo ID
      * @param secret The secret
      * @return A collection of Exif objects
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Collection getExif(String photoId, String secret) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_EXIF));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photo_id", photoId));
         if (secret != null) {
             parameters.add(new Parameter("secret", secret));
         }
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         List exifs = new ArrayList();
         Element photoElement = response.getPayload();
         NodeList exifElements = photoElement.getElementsByTagName("exif");
         for (int i = 0; i < exifElements.getLength(); i++) {
             Element exifElement = (Element) exifElements.item(i);
             Exif exif = new Exif();
             exif.setTagspace(exifElement.getAttribute("tagspace"));
             exif.setTagspaceId(exifElement.getAttribute("tagspaceid"));
             exif.setTag(exifElement.getAttribute("tag"));
             exif.setLabel(exifElement.getAttribute("label"));
             exif.setRaw(XMLUtilities.getChildValue(exifElement, "raw"));
             exif.setClean(XMLUtilities.getChildValue(exifElement, "clean"));
             exifs.add(exif);
         }
         return exifs;
     }
 
     /**
      * Get all info for the specified photo.
      *
      * @param photoId The photo Id
      * @param secret The optional secret String
      * @return The Photo
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Photo getInfo(String photoId, String secret) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_INFO));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photo_id", photoId));
         if (secret != null) {
             parameters.add(new Parameter("secret", secret));
         }
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element photoElement = response.getPayload();
         Photo photo = new Photo();
         photo.setId(photoElement.getAttribute("id"));
         photo.setSecret(photoElement.getAttribute("secret"));
         photo.setServer(photoElement.getAttribute("server"));
         photo.setFavorite("1".equals(photoElement.getAttribute("isfavorite")));
         photo.setLicense(photoElement.getAttribute("license"));
 
         Element ownerElement = (Element) photoElement.getElementsByTagName("owner").item(0);
         User owner = new User();
         owner.setId(ownerElement.getAttribute("nsid"));
         owner.setUsername(ownerElement.getAttribute("username"));
         owner.setRealName(ownerElement.getAttribute("realname"));
         owner.setLocation(ownerElement.getAttribute("location"));
         photo.setOwner(owner);
 
         photo.setTitle(XMLUtilities.getChildValue(photoElement, "title"));
         photo.setDescription(XMLUtilities.getChildValue(photoElement, "description"));
 
         Element visibilityElement = (Element) photoElement.getElementsByTagName("visibility").item(0);
         photo.setPublicFlag("1".equals(visibilityElement.getAttribute("ispublic")));
         photo.setFriendFlag("1".equals(visibilityElement.getAttribute("isfriend")));
         photo.setFamilyFlag("1".equals(visibilityElement.getAttribute("isfamily")));
 
         Element datesElement = XMLUtilities.getChild(photoElement, "dates");
         photo.setDatePosted(datesElement.getAttribute("posted"));
         photo.setDateTaken(datesElement.getAttribute("taken"));
         photo.setTakenGranularity(datesElement.getAttribute("takengranularity"));
 
         NodeList permissionsNodes = photoElement.getElementsByTagName("permissions");
         if (permissionsNodes.getLength() > 0) {
             Element permissionsElement = (Element) permissionsNodes.item(0);
             Permissions permissions = new Permissions();
             permissions.setComment(permissionsElement.getAttribute("permcomment"));
             permissions.setAddmeta(permissionsElement.getAttribute("permaddmeta"));
         }
 
         Element editabilityElement = (Element) photoElement.getElementsByTagName("editability").item(0);
         Editability editability = new Editability();
         editability.setComment("1".equals(editabilityElement.getAttribute("cancomment")));
         editability.setAddmeta("1".equals(editabilityElement.getAttribute("canaddmeta")));
 
         Element commentsElement = (Element) photoElement.getElementsByTagName("comments").item(0);
         photo.setComments(((Text) commentsElement.getFirstChild()).getData());
 
         Element notesElement = (Element) photoElement.getElementsByTagName("notes").item(0);
         List notes = new ArrayList();
         NodeList noteNodes = notesElement.getElementsByTagName("note");
         for (int i = 0; i < noteNodes.getLength(); i++) {
             Element noteElement = (Element) noteNodes.item(i);
             Note note = new Note();
             note.setId(noteElement.getAttribute("id"));
             note.setAuthor(noteElement.getAttribute("author"));
             note.setAuthorName(noteElement.getAttribute("authorname"));
             note.setBounds(noteElement.getAttribute("x"), noteElement.getAttribute("y"),
                     noteElement.getAttribute("w"), noteElement.getAttribute("h"));
             notes.add(note);
         }
         photo.setNotes(notes);
 
         Element tagsElement = (Element) photoElement.getElementsByTagName("tags").item(0);
         List tags = new ArrayList();
         NodeList tagNodes = tagsElement.getElementsByTagName("tag");
         for (int i = 0; i < tagNodes.getLength(); i++) {
             Element tagElement = (Element) tagNodes.item(i);
             Tag tag = new Tag();
             tag.setId(tagElement.getAttribute("id"));
             tag.setAuthor(tagElement.getAttribute("author"));
             tag.setRaw(tagElement.getAttribute("raw"));
             tag.setValue(((Text) tagElement.getFirstChild()).getData());
             tags.add(tag);
         }
         photo.setTags(tags);
 
         Element urlsElement = (Element) photoElement.getElementsByTagName("urls").item(0);
         List urls = new ArrayList();
         NodeList urlNodes = urlsElement.getElementsByTagName("url");
         for (int i = 0; i < urlNodes.getLength(); i++) {
             Element urlElement = (Element) urlNodes.item(i);
             PhotoUrl photoUrl = new PhotoUrl();
             photoUrl.setType(urlElement.getAttribute("type"));
             photoUrl.setUrl(XMLUtilities.getValue(urlElement));
             if (photoUrl.getType().equals("photopage")) {
                 photo.setUrl(photoUrl.getUrl());
             }
         }
         photo.setUrls(urls);
 
         return photo;
     }
 
     /**
      * Return a collection of Photo objects not in part of any sets.
      *
      * @param perPage The per page
      * @param page The page
      * @return The collection of Photo objects
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Collection getNotInSet(int perPage, int page) throws IOException, SAXException, FlickrException {
         List photos = new ArrayList();
 
         List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_NOT_IN_SET));
         parameters.add(new Parameter("api_key", apiKey));
 
         RequestContext requestContext = RequestContext.getRequestContext();
 
         List extras = requestContext.getExtras();
         if (extras.size() > 0) {
             parameters.add(new Parameter("extras", StringUtilities.join(extras, ",")));
         }
 
         if (perPage > 0) {
             parameters.add(new Parameter("per_page", new Integer(perPage)));
         }
         if (page > 0) {
             parameters.add(new Parameter("page", new Integer(page)));
         }
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element photosElement = response.getPayload();
         NodeList photoElements = photosElement.getElementsByTagName("photo");
         for (int i = 0; i < photoElements.getLength(); i++) {
             Element photoElement = (Element) photoElements.item(i);
             Photo photo = new Photo();
             photo.setId(photoElement.getAttribute("id"));
             photo.setSecret(photoElement.getAttribute("secret"));
             photo.setServer(photoElement.getAttribute("server"));
             photo.setTitle(photoElement.getAttribute("title"));
             photo.setPublicFlag("1".equals(photoElement.getAttribute("ispublic")));
             photo.setFriendFlag("1".equals(photoElement.getAttribute("isfriend")));
             photo.setFamilyFlag("1".equals(photoElement.getAttribute("isfamily")));
 
             User user = new User();
             user.setId(photoElement.getAttribute("owner"));
             photo.setOwner(user);
 
             photos.add(photo);
         }
         return photos;
     }
 
     /**
      * Get the permission information for the specified photo.
      *
      * @param photoId The photo id
      * @return The Permissions object
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Permissions getPerms(String photoId) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_PERMS));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photo_id", photoId));
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element permissionsElement = response.getPayload();
         Permissions permissions = new Permissions();
         permissions.setId(permissionsElement.getAttribute("id"));
         permissions.setPublicFlag("1".equals(permissionsElement.getAttribute("ispublic")));
         permissions.setFamilyFlag("1".equals(permissionsElement.getAttribute("isfamily")));
         permissions.setComment(permissionsElement.getAttribute("permcomment"));
         permissions.setAddmeta(permissionsElement.getAttribute("permaddmeta"));
         return permissions;
     }
 
     /**
      * Get a collection of recent photos.
      *
      * @param perPage The number of photos per page
      * @param page The page offset
      * @return A collection of Photo objects
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Collection getRecent(int perPage, int page) throws IOException, SAXException, FlickrException {
         List photos = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_RECENT));
         parameters.add(new Parameter("api_key", apiKey));
 
         if (perPage > 0) {
             parameters.add(new Parameter("per_page", new Integer(perPage)));
         }
         if (page > 0) {
             parameters.add(new Parameter("page", new Integer(page)));
         }
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element photosElement = response.getPayload();
         NodeList photoNodes = photosElement.getElementsByTagName("photo");
         for (int i = 0; i < photoNodes.getLength(); i++) {
             Element photoElement = (Element) photoNodes.item(i);
             Photo photo = new Photo();
             photo.setId(photoElement.getAttribute("id"));
 
             User owner = new User();
             owner.setId(photoElement.getAttribute("owner"));
             photo.setOwner(owner);
 
             photo.setSecret(photoElement.getAttribute("secret"));
             photo.setServer(photoElement.getAttribute("server"));
             photo.setTitle(photoElement.getAttribute("name"));
             photo.setPublicFlag("1".equals(photoElement.getAttribute("ispublic")));
             photo.setFriendFlag("1".equals(photoElement.getAttribute("isfriend")));
             photo.setFamilyFlag("1".equals(photoElement.getAttribute("isfamily")));
 
             photos.add(photo);
         }
         return photos;
     }
 
     /**
      * Get the available sizes for sizes.
      *
      * @param photoId The photo ID
      * @return The size collection
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Collection getSizes(String photoId) throws IOException, SAXException, FlickrException {
         List sizes = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_SIZES));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photo_id", photoId));
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element sizesElement = response.getPayload();
         NodeList sizeNodes = sizesElement.getElementsByTagName("size");
         for (int i = 0; i < sizeNodes.getLength(); i++) {
             Element sizeElement = (Element) sizeNodes.item(i);
             Size size = new Size();
             size.setLabel(sizeElement.getAttribute("label"));
             size.setWidth(sizeElement.getAttribute("width"));
             size.setHeight(sizeElement.getAttribute("height"));
             size.setSource(sizeElement.getAttribute("source"));
             size.setUrl(sizeElement.getAttribute("url"));
             sizes.add(size);
         }
         return sizes;
     }
 
     /**
      * Get the collection of untagged photos.
      *
      * @param perPage
      * @param page
      * @return A Collection of Photos
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Collection getUntagged(int perPage, int page) throws IOException, SAXException,
             FlickrException {
         List photos = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_UNTAGGED));
         parameters.add(new Parameter("api_key", apiKey));
 
         if (perPage > 0) {
             parameters.add(new Parameter("per_page", new Integer(perPage)));
         }
         if (page > 0) {
             parameters.add(new Parameter("page", new Integer(page)));
         }
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element photosElement = response.getPayload();
         NodeList photoNodes = photosElement.getElementsByTagName("photo");
         for (int i = 0; i < photoNodes.getLength(); i++) {
             Element photoElement = (Element) photoNodes.item(i);
             Photo photo = new Photo();
             photo.setId(photoElement.getAttribute("id"));
 
             User owner = new User();
             owner.setId(photoElement.getAttribute("owner"));
             photo.setOwner(owner);
 
             photo.setSecret(photoElement.getAttribute("secret"));
             photo.setServer(photoElement.getAttribute("server"));
             photo.setTitle(photoElement.getAttribute("title"));
             photo.setPublicFlag("1".equals(photoElement.getAttribute("ispublic")));
             photo.setFriendFlag("1".equals(photoElement.getAttribute("isfriend")));
             photo.setFamilyFlag("1".equals(photoElement.getAttribute("isfamily")));
 
             photos.add(photo);
         }
         return photos;
     }
 
     /**
      * Remove a tag from a photo.
      *
      * @param tagId The tag ID
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public void removeTag(String tagId) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_REMOVE_TAG));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("tag_id", tagId));
 
         Response response = transport.post(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     /**
      * Search for photos which match the given search parameters.
      *
      * @param params The search parameters
      * @param perPage The number of photos to show per page
      * @param page The page offset
      * @return A PhotoList
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public PhotoList search(SearchParameters params, int perPage, int page) throws IOException, SAXException,
             FlickrException {
         PhotoList photos = new PhotoList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_SEARCH));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.addAll(params.getAsParameters());
 
         if (perPage > 0) {
             parameters.add(new Parameter("per_page", new Integer(perPage)));
         }
         if (page > 0) {
             parameters.add(new Parameter("page", new Integer(page)));
         }
 
         Response response = transport.get(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element photosElement = response.getPayload();
         photos.setPage(photosElement.getAttribute("page"));
         photos.setPages(photosElement.getAttribute("pages"));
         photos.setPerPage(photosElement.getAttribute("perpage"));
         photos.setTotal(photosElement.getAttribute("total"));
 
         NodeList photoNodes = photosElement.getElementsByTagName("photo");
         for (int i = 0; i < photoNodes.getLength(); i++) {
             Element photoElement = (Element) photoNodes.item(i);
             Photo photo = new Photo();
             photo.setId(photoElement.getAttribute("id"));
 
             User owner = new User();
             owner.setId(photoElement.getAttribute("owner"));
             photo.setOwner(owner);
 
             photo.setSecret(photoElement.getAttribute("secret"));
             photo.setServer(photoElement.getAttribute("server"));
             photo.setTitle(photoElement.getAttribute("name"));
             photo.setPublicFlag("1".equals(photoElement.getAttribute("ispublic")));
             photo.setFriendFlag("1".equals(photoElement.getAttribute("isfriend")));
             photo.setFamilyFlag("1".equals(photoElement.getAttribute("isfamily")));
 
             photos.add(photo);
         }
         return photos;
     }
 
     /**
      * Set the dates for the specified photo.
      *
      * @param photoId The photo ID
      * @param datePosted The date the photo was posted or null
      * @param dateTaken The date the photo was taken or null
      * @param dateTakenGranularity The granularity of the taken date or null
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public void setDates(String photoId, Date datePosted, Date dateTaken, String dateTakenGranularity)
             throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_SET_DATES));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photo_id", photoId));
 
         if (datePosted != null) {
             parameters.add(new Parameter("date_posted", new Long(datePosted.getTime())));
         }
 
         if (dateTaken != null) {
             parameters.add(new Parameter("date_taken", DF.format(dateTaken)));
         }
 
         if (dateTakenGranularity != null) {
             parameters.add(new Parameter("date_taken_granularity", dateTakenGranularity));
         }
 
         Response response = transport.post(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     /**
      * Set the meta data for the photo.
      *
      * @param photoId The photo ID
      * @param title The new title
      * @param description The new description
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public void setMeta(String photoId, String title, String description) throws IOException,
             SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_SET_META));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photo_id", photoId));
         parameters.add(new Parameter("title", title));
         parameters.add(new Parameter("description", description));
 
         Response response = transport.post(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     /**
      * Set the permissions for the photo.
      *
      * @param photoId The photo ID
      * @param permissions The permissions object
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public void setPerms(String photoId, Permissions permissions) throws IOException,
             SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_SET_META));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photo_id", photoId));
         parameters.add(new Parameter("is_public", permissions.isPublicFlag() ? "1" : "0"));
         parameters.add(new Parameter("is_friend", permissions.isFriendFlag() ? "1" : "0"));
         parameters.add(new Parameter("is_family", permissions.isFamilyFlag() ? "1" : "0"));
         parameters.add(new Parameter("perm_comment", new Integer(permissions.getComment())));
         parameters.add(new Parameter("perm_addmeta", new Integer(permissions.getAddmeta())));
 
         Response response = transport.post(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     /**
      * Set the tags for a photo.
      *
      * @param photoId The photo ID
      * @param tags The tag array
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public void setTags(String photoId, String[] tags) throws IOException, SAXException,
             FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_SET_TAGS));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("photo_id", photoId));
         parameters.add(new Parameter("tags", StringUtilities.join(tags, " ")));
 
         Response response = transport.post(transport.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     /**
      * Get the photo for the specified ID. Currently maps to the getInfo() method.
      *
      * @param id The ID
      * @return The Photo
      * @throws IOException
      * @throws FlickrException
      * @throws SAXException
      */
     public Photo getPhoto(String id) throws IOException, FlickrException, SAXException {
         return getPhoto(id, null);
     }
 
     /**
      * Get the photo for the specified ID with the given secret. Currently maps to the getInfo() method.
      *
      * @param id The ID
      * @param secret The secret
      * @return The Photo
      * @throws IOException
      * @throws FlickrException
      * @throws SAXException
      */
     public Photo getPhoto(String id, String secret) throws IOException, FlickrException, SAXException {
         return getInfo(id, secret);
     }
 
 
 }
