 package com.aetrion.flickr.photos;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import com.aetrion.flickr.Authentication;
 import com.aetrion.flickr.FlickrException;
 import com.aetrion.flickr.Parameter;
 import com.aetrion.flickr.REST;
 import com.aetrion.flickr.RESTResponse;
 import com.aetrion.flickr.RequestContext;
 import com.aetrion.flickr.Response;
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
 
     private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
     public static final String METHOD_ADD_TAGS = "flickr.photos.addTags";
     public static final String METHOD_GET_CONTACTS_PHOTOS = "flickr.photos.getContactsPhotos";
     public static final String METHOD_GET_CONTACTS_PUBLIC_PHOTOS = "flickr.photos.getContactsPublicPhotos";
     public static final String METHOD_GET_CONTEXT = "flickr.photos.getContext"; // NYI
     public static final String METHOD_GET_COUNTS = "flickr.photos.getCounts";
     public static final String METHOD_GET_EXIF = "flickr.photos.getExif"; // NYI
     public static final String METHOD_GET_INFO = "flickr.photos.getInfo";
     public static final String METHOD_GET_NOT_IN_SET = "flickr.photos.getNotInSet"; // NYI
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
 
     private String apiKey;
     private REST restInterface;
 
     public PhotosInterface(String apiKey, REST restInterface) {
         this.apiKey = apiKey;
         this.restInterface = restInterface;
     }
 
     public void addTags(String photoId, String[] tags) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_ADD_TAGS));
         parameters.add(new Parameter("api_key", apiKey));
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
         parameters.add(new Parameter("photo_id", photoId));
         parameters.add(new Parameter("tags", StringUtilities.join(tags, " ")));
 
         Response response = restInterface.post("/services/rest/", parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
     public Collection getContactsPhotos(int count, boolean justFriends, boolean singlePhoto, boolean includeSelf)
             throws IOException, SAXException, FlickrException {
         List photos = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_CONTACTS_PHOTOS));
         parameters.add(new Parameter("api_key", apiKey));
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
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
 
         RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         } else {
             Element photosElement = (Element) response.getPayload();
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
                 photo.setPublicFlag("1".equals(photoElement.getAttribute("ispublic")));
                 photo.setFriendFlag("1".equals(photoElement.getAttribute("isfriend")));
                 photo.setFamilyFlag("1".equals(photoElement.getAttribute("isfamily")));
 
                 photos.add(photo);
             }
             return photos;
         }
     }
 
     public Collection getContactsPublicPhotos(String userId, int count, boolean justFriends, boolean singlePhoto, boolean includeSelf)
             throws IOException, SAXException, FlickrException {
         List photos = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_CONTACTS_PUBLIC_PHOTOS));
         parameters.add(new Parameter("api_key", apiKey));
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
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
 
         RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         } else {
             Element photosElement = (Element) response.getPayload();
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
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
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
 
         RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         } else {
             Element photocountsElement = (Element) response.getPayload();
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
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
         parameters.add(new Parameter("photo_id", photoId));
         if (secret != null) {
             parameters.add(new Parameter("secret", secret));
         }
 
         RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         } else {
             Element photoElement = (Element) response.getPayload();
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
             owner.setRealname(ownerElement.getAttribute("realname"));
             owner.setLocation(ownerElement.getAttribute("location"));
             photo.setOwner(owner);
 
             Element titleElement = (Element) photoElement.getElementsByTagName("title").item(0);
             photo.setTitle(XMLUtilities.getValue(titleElement));
 
             Element descriptionElement = (Element) photoElement.getElementsByTagName("description").item(0);
             photo.setDescription(XMLUtilities.getValue(descriptionElement));
 
             Element visibilityElement = (Element) photoElement.getElementsByTagName("visibility").item(0);
             photo.setPublicFlag("1".equals(visibilityElement.getAttribute("ispublic")));
             photo.setFriendFlag("1".equals(visibilityElement.getAttribute("isfriend")));
             photo.setFamilyFlag("1".equals(visibilityElement.getAttribute("isfamily")));
 
             Element datesElement = (Element) photoElement.getElementsByTagName("dates").item(0);
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
 
             return photo;
         }
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
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
         parameters.add(new Parameter("photo_id", photoId));
 
         RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         } else {
             Element permissionsElement = (Element) response.getPayload();
             Permissions permissions = new Permissions();
             permissions.setId(permissionsElement.getAttribute("id"));
             permissions.setPublicFlag("1".equals(permissionsElement.getAttribute("ispublic")));
             permissions.setFamilyFlag("1".equals(permissionsElement.getAttribute("isfamily")));
             permissions.setComment(permissionsElement.getAttribute("permcomment"));
             permissions.setAddmeta(permissionsElement.getAttribute("permaddmeta"));
             return permissions;
         }
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
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
         if (perPage > 0) {
             parameters.add(new Parameter("per_page", new Integer(perPage)));
         }
         if (page > 0) {
             parameters.add(new Parameter("page", new Integer(page)));
         }
 
         RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         } else {
             Element photosElement = (Element) response.getPayload();
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
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
         parameters.add(new Parameter("photo_id", photoId));
 
         RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         } else {
             Element sizesElement = (Element) response.getPayload();
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
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
         if (perPage > 0) {
             parameters.add(new Parameter("per_page", new Integer(perPage)));
         }
         if (page > 0) {
             parameters.add(new Parameter("page", new Integer(page)));
         }
 
         RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         } else {
             Element photosElement = (Element) response.getPayload();
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
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
         parameters.add(new Parameter("tag_id", tagId));
 
         Response response = restInterface.post("/services/rest/", parameters);
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
      * @return A Collection of Photo objects
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Collection search(SearchParameters params, int perPage, int page) throws IOException, SAXException,
             FlickrException {
         List photos = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_SEARCH));
         parameters.add(new Parameter("api_key", apiKey));
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
         parameters.addAll(params.getAsParameters());
 
         if (perPage > 0) {
             parameters.add(new Parameter("per_page", new Integer(perPage)));
         }
         if (page > 0) {
             parameters.add(new Parameter("page", new Integer(page)));
         }
 
         RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         } else {
             Element photosElement = (Element) response.getPayload();
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
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
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
 
         Response response = restInterface.post("/services/rest/", parameters);
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
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
         parameters.add(new Parameter("photo_id", photoId));
         parameters.add(new Parameter("title", title));
         parameters.add(new Parameter("description", description));
 
         Response response = restInterface.post("/services/rest/", parameters);
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
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
         parameters.add(new Parameter("photo_id", photoId));
         parameters.add(new Parameter("is_public", permissions.isPublicFlag() ? "1" : "0"));
         parameters.add(new Parameter("is_friend", permissions.isFriendFlag() ? "1" : "0"));
         parameters.add(new Parameter("is_family", permissions.isFamilyFlag() ? "1" : "0"));
         parameters.add(new Parameter("perm_comment", new Integer(permissions.getComment())));
         parameters.add(new Parameter("perm_addmeta", new Integer(permissions.getAddmeta())));
 
         Response response = restInterface.post("/services/rest/", parameters);
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
 
         RequestContext requestContext = RequestContext.getRequestContext();
         Authentication auth = requestContext.getAuthentication();
         if (auth != null) {
             parameters.addAll(auth.getAsParameters());
         }
 
         parameters.add(new Parameter("photo_id", photoId));
         parameters.add(new Parameter("tags", StringUtilities.join(tags, " ")));
 
         Response response = restInterface.post("/services/rest/", parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
     }
 
 }
