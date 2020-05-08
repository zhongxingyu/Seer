 /**
  * Mule Facebook Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.facebook;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.mule.api.annotations.Configurable;
 import org.mule.api.annotations.Module;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.oauth.OAuth2;
 import org.mule.api.annotations.oauth.OAuthAccessToken;
 import org.mule.api.annotations.oauth.OAuthConsumerKey;
 import org.mule.api.annotations.oauth.OAuthConsumerSecret;
 import org.mule.api.annotations.oauth.OAuthScope;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 import org.mule.module.facebook.util.JSONMapper;
 import org.mule.modules.utils.MuleSoftException;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.LoggingFilter;
 import com.sun.jersey.api.representation.Form;
 import com.sun.jersey.multipart.BodyPart;
 import com.sun.jersey.multipart.FormDataMultiPart;
 
 /**
  * Facebook is a social networking service and website launched in February 2004.
  * 
  * @author MuleSoft, inc.
  */
 @Module(name = "facebook", schemaVersion = "2.0")
 @OAuth2(accessTokenUrl = "https://graph.facebook.com/oauth/access_token", authorizationUrl = "https://graph.facebook.com/oauth/authorize",
         accessTokenRegex = "access_token=([^&]+?)&", expirationRegex = "expires_in=([^&]+?)$")
 public class FacebookConnector
 {
 
     private static String FACEBOOK_URI = "https://graph.facebook.com";
     private static String ACCESS_TOKEN_QUERY_PARAM_NAME = "access_token";
 
     /**
      * The application identifier as registered with Facebook
      */
     @Configurable
     @OAuthConsumerKey
     private String appId;
 
     /**
      * The application secret
      */
     @Configurable
     @OAuthConsumerSecret
     private String appSecret;
 
     /**
      * Facebook permissions
      */
     @Configurable
     @Optional
     @Default(value = "email,read_stream,publish_stream")
     @OAuthScope
     private String scope;
 
     /**
      * Jersey client
      */
     private Client client;
 
     /**
      * Constructor
      */
     public FacebookConnector()
     {
         client = new Client();
         client.addFilter(new LoggingFilter());
     }
     
     /**
      * Gets the user logged details.
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:logged-user-details}
      * 
      * @param accessToken the access token to use to authenticate the request
      * @return response from Facebook the actual user.
      */
     @Processor
     public Map<String, Object> loggedUserDetails(@OAuthAccessToken String accessToken)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("me").build();
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("access_token", accessToken).type(MediaType.APPLICATION_FORM_URLENCODED).get(String.class));
     }
     
     /**
      * Search over all public objects in the social graph
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:search}
      * 
      * @param q The search string
      * @param obj Supports these types of objects: All public posts (post), people
      *            (user), pages (page), events (event), groups (group), check-ins
      *            (checkin)
      * @return response from Facebook the search resutl
      */
     @Processor
     public Map<String, Object> search(String q, @Optional @Default("post") String obj)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("search").build();
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap(resource.queryParam("q", q).queryParam("object", obj).get(String.class));
     }
 
     /**
      * A photo album
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getAlbum}
      * 
      * @param album Represents the ID of the album object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getAlbum(String album, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{album}").build(album);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).get(String.class));
     }
 
     /**
      * The photos contained in this album
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getAlbumPhotos}
      * 
      * @param album Represents the ID of the album object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getAlbumPhotos(String album,
                                  @Optional @Default("last week") String since,
                                  @Optional @Default("yesterday") String until,
                                  @Optional @Default("3") String limit,
                                  @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{album}/photos").build(album);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class));
     }
 
     /**
      * The comments made on this album
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getAlbumComments}
      * 
      * @param album Represents the ID of the album object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getAlbumComments(String album,
                                    @Optional @Default("last week") String since,
                                    @Optional @Default("yesterday") String until,
                                    @Optional @Default("3") String limit,
                                    @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{album}/comments").build(album);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class));
     }
 
     /**
      * Specifies information about an event, including the location, event name, and
      * which invitees plan to attend.
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getEvent}
      * 
      * @param eventId Represents the ID of the event object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getEvent(String eventId, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}").build(eventId);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).
 
         get(String.class));
     }
 
     /**
      * This event's wall
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventWall}
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getEventWall(String eventId,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/feed").build(eventId);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * All of the users who have been not yet responded to their invitation to this
      * event
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventNoReply}
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getEventNoReply(String eventId,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("3") String limit,
                                   @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/noreply").build(eventId);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * All of the users who have been responded "Maybe" to their invitation to this
      * event
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventMaybe}
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getEventMaybe(String eventId,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/maybe").build(eventId);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class));
     }
 
     /**
      * All of the users who have been invited to this event
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventInvited}
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getEventInvited(String eventId,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("3") String limit,
                                   @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/invited").build(eventId);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * All of the users who are attending this event
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventAttending}
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getEventAttending(String eventId,
                                     @Optional @Default("last week") String since,
                                     @Optional @Default("yesterday") String until,
                                     @Optional @Default("3") String limit,
                                     @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/attending").build(eventId);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * All of the users who declined their invitation to this event
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventDeclined}
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getEventDeclined(String eventId,
                                    @Optional @Default("last week") String since,
                                    @Optional @Default("yesterday") String until,
                                    @Optional @Default("3") String limit,
                                    @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/declined").build(eventId);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The event's profile picture
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventPicture}
      * 
      * @param eventId Represents the ID of the event object.
      * @param type One of square (50x50), small (50 pixels wide, variable height),
      *            and large (about 200 pixels wide, variable height)
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getEventPicture(String eventId, @Optional @Default("small") String type)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/picture").build(eventId);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("type", type).
 
         get(String.class));
     }
 
     /**
      * A Facebook group
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getGroup}
      * 
      * @param group Represents the ID of the group object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getGroup(String group, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{group}").build(group);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).
 
         get(String.class));
     }
 
     /**
      * This group's wall
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getGroupWall}
      * 
      * @param group Represents the ID of the group object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getGroupWall(String group,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{group}/feed").build(group);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * All of the users who are members of this group
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getGroupMembers}
      * 
      * @param group Represents the ID of the group object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getGroupMembers(String group,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("3") String limit,
                                   @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{group}/members").build(group);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The profile picture of this group
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getGroupPicture}
      * 
      * @param group Represents the ID of the group object.
      * @param type One of square (50x50), small (50 pixels wide, variable height),
      *            and large (about 200 pixels wide, variable height)
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getGroupPicture(String group, @Optional @Default("small") String type)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{group}/picture").build(group);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("type", type).
 
         get(String.class));
     }
 
     /**
      * A link shared on a user's wall 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getLink}
      * 
      * @param link Represents the ID of the link object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getLink(String link, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{link}").build(link);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).
 
         get(String.class));
     }
 
     /**
      * All of the comments on this link 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getLinkComments}
      * 
      * @param link Represents the ID of the link object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getLinkComments(String link,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("3") String limit,
                                   @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{link}/comments").build(link);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * A Facebook note
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getNote}
      * 
      * @param accessToken the access token to use to authenticate the request
      * @param note Represents the ID of the note object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
    public Map<String, Object> getNote(@OAuthAccessToken String access_token, String note, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{note}").build(note);
        WebResource resource = client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, access_token);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).
 
         get(String.class));
     }
 
     /**
      * All of the comments on this note 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getNoteComments}
      * 
      * @param note Represents the ID of the note object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getNoteComments(String note,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("3") String limit,
                                   @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{note}/comments").build(note);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * People who like the note 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getNoteLikes}
      * 
      * @param note Represents the ID of the note object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getNoteLikes(String note,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{note}/likes").build(note);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * A
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPage}
      * 
      * @param page Represents the ID of the page object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPage(String page, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).
 
         get(String.class));
     }
 
     /**
      * The page's wall 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageWall}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPageWall(String page,
                               @Optional @Default("last week") String since,
                               @Optional @Default("yesterday") String until,
                               @Optional @Default("3") String limit,
                               @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/feed").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The page's profile picture 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPagePicture}
      * 
      * @param page Represents the ID of the page object.
      * @param type One of square (50x50), small (50 pixels wide, variable height),
      *            and large (about 200 pixels wide, variable height)
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPagePicture(String page, @Optional @Default("small") String type)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/picture").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("type", type).
 
         get(String.class));
     }
 
     /**
      * The photos, videos, and posts in which this page has been tagged 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageTagged}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPageTagged(String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/tagged").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The page's posted links 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageLinks}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPageLinks(String page,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/links").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The photos this page has uploaded 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPagePhotos}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPagePhotos(String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/photos").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The groups this page is a member of 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageGroups}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPageGroups(String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/groups").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The photo albums this page has created 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageAlbums}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPageAlbums(String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/albums").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The page's status updates 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageStatuses}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPageStatuses(String page,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("3") String limit,
                                   @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/statuses").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The videos this page has created 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageVideos}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPageVideos(String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/videos").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The page's notes 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageNotes}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPageNotes(String page,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/notes").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The page's own posts 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPagePosts}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPagePosts(String page,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/posts").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The events this page is attending 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageEvents}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPageEvents(String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/events").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * Checkins made by the friends of the current session user 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageCheckins}
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPageCheckins(String page,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("3") String limit,
                                   @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/checkins").build(page);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * An individual photo 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPhoto}
      * 
      * @param photo Represents the ID of the photo object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPhoto(String photo, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{photo}").build(photo);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).get(String.class));
     }
 
     /**
      * All of the comments on this photo 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPhotoComments}
      * 
      * @param photo Represents the ID of the photo object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPhotoComments(String photo,
                                    @Optional @Default("last week") String since,
                                    @Optional @Default("yesterday") String until,
                                    @Optional @Default("3") String limit,
                                    @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{photo}/comments").build(photo);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * People who like the photo 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPhotoLikes}
      * 
      * @param photo Represents the ID of the photo object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPhotoLikes(String photo,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{photo}/likes").build(photo);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * An individual entry in a profile's feed 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPost}
      * 
      * @param post Represents the ID of the post object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPost(String post, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{post}").build(post);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).
 
         get(String.class));
     }
 
     /**
      * All of the comments on this post 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPostComments}
      * 
      * @param post Represents the ID of the post object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getPostComments(String post,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("3") String limit,
                                   @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{post}/comments").build(post);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * A status message on a user's wall 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getStatus}
      * 
      * @param accessToken the access token to use to authenticate the request
      * @param status Represents the ID of the status object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getStatus(@OAuthAccessToken String accessToken, String status, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{status}").build(status);
         WebResource resource = client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, accessToken);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).get(String.class));
     }
 
     /**
      * All of the comments on this message 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getStatusComments}
      * 
      * @param status Represents the ID of the status object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getStatusComments(String status,
                                     @Optional @Default("last week") String since,
                                     @Optional @Default("yesterday") String until,
                                     @Optional @Default("3") String limit,
                                     @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{status}/comments").build(status);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * A user profile. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUser}
      * 
      * @param user Represents the ID of the user object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUser(String user, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).
 
         get(String.class));
     }
 
     /**
      * Search an individual user's News Feed, restricted to that user's friends
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserSearch}
      * 
      * @param user Represents the ID of the user object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @param q The text for which to search.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserSearch(String user,
                                 @Optional @Default("0") String metadata,
                                 @Optional @Default("facebook") String q)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/home").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).queryParam("q", q).
 
         get(String.class));
     }
 
     /**
      * The user's News Feed. Requires the read_stream permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserHome}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserHome(String user,
                               @Optional @Default("last week") String since,
                               @Optional @Default("yesterday") String until,
                               @Optional @Default("3") String limit,
                               @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/home").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The user's wall. Requires the read_stream permission to see non-public posts.
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserWall}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserWall(String user,
                               @Optional @Default("last week") String since,
                               @Optional @Default("yesterday") String until,
                               @Optional @Default("3") String limit,
                               @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/feed").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The photos, videos, and posts in which this user has been tagged. Requires the
      * user_photo_tags, user_video_tags, friend_photo_tags, or friend_video_tags
      * permissions 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserTagged}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserTagged(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/tagged").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The user's own posts. Requires the read_stream permission to see non-public
      * posts. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserPosts}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserPosts(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/posts").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The user's profile picture 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserPicture}
      * 
      * @param user Represents the ID of the user object.
      * @param type One of square (50x50), small (50 pixels wide, variable height),
      *            and large (about 200 pixels wide, variable height)
      * @return Byte[] with the jpg image
      */
     @Processor
     public Byte[] getUserPicture(String user, @Optional @Default("small") String type)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/picture").build(user);
         WebResource resource = client.resource(uri);
         BufferedImage image = resource.queryParam("type", type).get(BufferedImage.class);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try
         {
             ImageIO.write(image, "jpg", baos);
         }
         catch (IOException e)
         {
             throw MuleSoftException.soften(e);
         }
         return ArrayUtils.toObject(baos.toByteArray());
     }
 
     /**
      * The user's friends 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserFriends}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserFriends(String user,
                                  @Optional @Default("last week") String since,
                                  @Optional @Default("yesterday") String until,
                                  @Optional @Default("3") String limit,
                                  @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/friends").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The activities listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserActivities}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserActivities(String user,
                                     @Optional @Default("last week") String since,
                                     @Optional @Default("yesterday") String until,
                                     @Optional @Default("3") String limit,
                                     @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/activities").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The music listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserCheckins}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserCheckins(String user,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("3") String limit,
                                   @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/checkins").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The interests listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserInterests}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserInterests(String user,
                                    @Optional @Default("last week") String since,
                                    @Optional @Default("yesterday") String until,
                                    @Optional @Default("3") String limit,
                                    @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/interests").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The music listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserMusic}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserMusic(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/music").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The books listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserBooks}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserBooks(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/books").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The movies listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserMovies}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserMovies(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/movies").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The television listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserTelevision}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserTelevision(String user,
                                     @Optional @Default("last week") String since,
                                     @Optional @Default("yesterday") String until,
                                     @Optional @Default("3") String limit,
                                     @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/television").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * All the pages this user has liked. Requires the user_likes or friend_likes
      * permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserLikes}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserLikes(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/likes").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The photos this user is tagged in. Requires the user_photos or friend_photos
      * permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserPhotos}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserPhotos(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/photos").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The photo albums this user has created. Requires the user_photos or
      * friend_photos permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserAlbums}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserAlbums(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/albums").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The videos this user has been tagged in. Requires the user_videos or
      * friend_videos permission. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserVideos}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserVideos(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/videos").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The groups this user is a member of. Requires the user_groups or friend_groups
      * permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserGroups}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserGroups(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/groups").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The user's status updates. Requires the read_stream permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserStatuses}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserStatuses(String user,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("3") String limit,
                                   @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/statuses").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The user's posted links. Requires the read_stream permission 
      * {@sample.xml../../../doc/mule-module-facebook.xml.sample facebook:getUserLinks}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserLinks(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/links").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The user's notes. Requires the read_stream permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserNotes}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserNotes(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/notes").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The events this user is attending. Requires the user_events or friend_events
      * permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserEvents}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserEvents(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/events").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The threads in this user's inbox. Requires the read_mailbox permission
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserInbox}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserInbox(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("3") String limit,
                                @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/inbox").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The messages in this user's outbox. Requires the read_mailbox permission
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserOutbox}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserOutbox(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("3") String limit,
                                 @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/outbox").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The updates in this user's inbox. Requires the read_mailbox permission
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserUpdates}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserUpdates(String user,
                                  @Optional @Default("last week") String since,
                                  @Optional @Default("yesterday") String until,
                                  @Optional @Default("3") String limit,
                                  @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/updates").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The Facebook pages owned by the current user 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserAccounts}
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getUserAccounts(String user,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("3") String limit,
                                   @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/accounts").build(user);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * An individual video 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getVideo}
      * 
      * @param accessToken the access token to use to authenticate the request
      * @param video Represents the ID of the video object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getVideo(@OAuthAccessToken String accessToken, String video, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{video}").build(video);
         WebResource resource = client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, accessToken);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).get(String.class));
     }
 
     /**
      * All of the comments on this video 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getVideoComments}
      * 
      * @param video Represents the ID of the video object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getVideoComments(String video,
                                    @Optional @Default("last week") String since,
                                    @Optional @Default("yesterday") String until,
                                    @Optional @Default("3") String limit,
                                    @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{video}/comments").build(video);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * Write to the given profile's feed/wall. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishMessage}
      * 
      * @param accessToken the access token to use to authenticate the request
      * @param profile_id the profile where to publish the message
      * @param msg The message
      * @param picture If available, a link to the picture included with this post
      * @param link The link attached to this post
      * @param caption The caption of the link (appears beneath the link name)
      * @param name The name of the link
      * @param description A description of the link (appears beneath the link
      *            caption)
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> publishMessage(@OAuthAccessToken String accessToken,
                                  String profile_id,
                                  String msg,
                                  @Optional String picture,
                                  @Optional String link,
                                  @Optional String caption,
                                  @Optional String name,
                                  @Optional String description)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{profile_id}/feed").build(profile_id);
         WebResource resource = client.resource(uri);
         Form form = new Form();
         form.add("access_token", accessToken);
         form.add("message", msg);
 
         if (picture != null) form.add("picture", picture);
         if (link != null) form.add("link", link);
         if (caption != null) form.add("caption", caption);
         if (name != null) form.add("name", name);
         if (description != null) form.add("description", description);
 
         return JSONMapper.toMap( resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(String.class, form));
     }
 
     /**
      * Comment on the given post 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishComment}
      * 
      * @param accessToken the access token to use to authentica the request to
      *            Facebook
      * @param postId Represents the ID of the post object.
      * @param msg comment on the given post
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> publishComment(@OAuthAccessToken String accessToken, String postId, String msg)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{postId}/comments").build(postId);
         WebResource resource = client.resource(uri);
         Form form = new Form();
         form.add("access_token", accessToken);
         form.add("message", msg);
 
         WebResource.Builder type = resource.type(MediaType.APPLICATION_FORM_URLENCODED);
         return JSONMapper.toMap( type.accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE).post(
             String.class, form));
     }
 
     /**
      * Write to the given profile's feed/wall. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:like}
      * 
      * @param postId Represents the ID of the post object.
      */
     @Processor
     public void like(String postId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{postId}/likes").build(postId);
         WebResource resource = client.resource(uri);
         resource.type(MediaType.APPLICATION_FORM_URLENCODED).post();
     }
 
     /**
      * Write a note on the given profile. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishNote}
      * 
      * @param profile_id the profile where to publish the note
      * @param msg The message
      * @param subject the subject of the note
      */
     @Processor
     public void publishNote(String profile_id, String msg, String subject)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{profile_id}/notes").build(profile_id);
         WebResource resource = client.resource(uri);
         Form form = new Form();
         form.add("message", msg);
         form.add("subject", subject);
 
         resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(form);
     }
 
     /**
      * Write a note on the given profile. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishLink}
      * 
      * @param profile_id the profile where to publish the link
      * @param msg The message
      * @param link the link
      */
     @Processor
     public void publishLink(String profile_id, String msg, String link)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{profile_id}/links").build(profile_id);
         WebResource resource = client.resource(uri);
         Form form = new Form();
         form.add("message", msg);
         form.add("link", link);
 
         resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(form);
     }
 
     /**
      * Post an event in the given profile. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishEvent}
      * 
      * @param profile_id the profile where to publish the event
      */
     @Processor
     public void publishEvent(String profile_id)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{profile_id}/events").build(profile_id);
         WebResource resource = client.resource(uri);
         resource.type(MediaType.APPLICATION_FORM_URLENCODED).post();
     }
 
     /**
      * Attend the given event. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:attendEvent}
      * 
      * @param eventId the id of the event to attend
      */
     @Processor
     public void attendEvent(String eventId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{eventId}/attending").build(eventId);
         WebResource resource = client.resource(uri);
         resource.type(MediaType.APPLICATION_FORM_URLENCODED).post();
     }
 
     /**
      * Maybe attend the given event. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:tentativeEvent}
      * 
      * @param eventId Represents the id of the event object
      */
     @Processor
     public void tentativeEvent(String eventId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{eventId}/maybe").build(eventId);
         WebResource resource = client.resource(uri);
         resource.type(MediaType.APPLICATION_FORM_URLENCODED).post();
     }
 
     /**
      * Decline the given event. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:declineEvent}
      * 
      * @param eventId Represents the id of the event object
      */
     @Processor
     public void declineEvent(String eventId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{eventId}/declined").build(eventId);
         WebResource resource = client.resource(uri);
         resource.type(MediaType.APPLICATION_FORM_URLENCODED).post();
     }
 
     /**
      * Create an album. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:publishAlbum}
      * 
      * @param profile_id the id of the profile object
      * @param msg The message
      * @param name the name of the album
      */
     @Processor
     public void publishAlbum(String profile_id, String msg, String name)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{profile_id}/albums").build(profile_id);
         WebResource resource = client.resource(uri);
         Form form = new Form();
         form.add("message", msg);
         form.add("name", name);
 
         resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(form);
     }
 
     /**
      * Upload a photo to an album. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishPhoto}
      * 
      * @param albumId the id of the album object
      * @param caption Caption of the photo
      * @param photo File containing the photo
      */
     @Processor
     public void publishPhoto(String albumId, String caption, File photo)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{albumId}/photos").build(albumId);
         WebResource resource = client.resource(uri);
         FormDataMultiPart multiPart = new FormDataMultiPart();
         multiPart.bodyPart(new BodyPart(photo, MediaType.APPLICATION_OCTET_STREAM_TYPE));
         multiPart.field("message", caption);
 
         resource.type(MediaType.MULTIPART_FORM_DATA).post(multiPart);
     }
 
     /**
      * Delete an object in the graph. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:deleteObject}
      * 
      * @param objectId The ID of the object to be deleted
      */
     @Processor
     public void deleteObject(String objectId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{object_id}").build(objectId);
         WebResource resource = client.resource(uri);
         resource.type(MediaType.APPLICATION_FORM_URLENCODED).post();
 
     }
 
     /**
      * Remove a 'like' from a post. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:dislike}
      * 
      * @param postId The ID of the post to be disliked
      */
     @Processor
     public void dislike(String postId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{postId}/likes").build(postId);
         WebResource resource = client.resource(uri);
         resource.type(MediaType.APPLICATION_FORM_URLENCODED).post();
     }
 
     /**
      * A check-in that was made through Facebook Places. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getCheckin}
      * 
      * @param checkin Represents the ID of the checkin object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getCheckin(String checkin, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{checkin}").build(checkin);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("metadata", metadata).
 
         get(String.class));
     }
 
     /**
      * An application's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplication}
      * 
      * @param application Represents the ID of the application object.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplication(String application)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.
 
         get(String.class));
     }
 
     /**
      * The application's wall. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationWall}
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationWall(String application,
                                      @Optional @Default("last week") String since,
                                      @Optional @Default("yesterday") String until,
                                      @Optional @Default("3") String limit,
                                      @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/feed").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The application's own posts. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationPosts}
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationPosts(String application,
                                       @Optional @Default("last week") String since,
                                       @Optional @Default("yesterday") String until,
                                       @Optional @Default("3") String limit,
                                       @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/posts").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The application's logo 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationPicture}
      * 
      * @param application Represents the ID of the application object.
      * @param type One of square (50x50), small (50 pixels wide, variable height),
      *            and large (about 200 pixels wide, variable height)
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationPicture(String application, @Optional @Default("small") String type)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/picture").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("type", type).
 
         get(String.class));
     }
 
     /**
      * The photos, videos, and posts in which this application has been tagged.
      * 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getApplicationTagged}
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationTagged(String application,
                                        @Optional @Default("last week") String since,
                                        @Optional @Default("yesterday") String until,
                                        @Optional @Default("3") String limit,
                                        @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/tagged").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The application's posted links. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationLinks}
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationLinks(String application,
                                       @Optional @Default("last week") String since,
                                       @Optional @Default("yesterday") String until,
                                       @Optional @Default("3") String limit,
                                       @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/links").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The photos this application is tagged in. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationPhotos}
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationPhotos(String application,
                                        @Optional @Default("last week") String since,
                                        @Optional @Default("yesterday") String until,
                                        @Optional @Default("3") String limit,
                                        @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/photos").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The photo albums this application has created. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationAlbums}
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationAlbums(String application,
                                        @Optional @Default("last week") String since,
                                        @Optional @Default("yesterday") String until,
                                        @Optional @Default("3") String limit,
                                        @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/albums").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The application's status updates. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationStatuses}
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationStatuses(String application,
                                          @Optional @Default("last week") String since,
                                          @Optional @Default("yesterday") String until,
                                          @Optional @Default("3") String limit,
                                          @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/statuses").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The videos this application has created 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationVideos}
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationVideos(String application,
                                        @Optional @Default("last week") String since,
                                        @Optional @Default("yesterday") String until,
                                        @Optional @Default("3") String limit,
                                        @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/videos").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * The application's notes. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationNotes}
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationNotes(String application,
                                       @Optional @Default("last week") String since,
                                       @Optional @Default("yesterday") String until,
                                       @Optional @Default("3") String limit,
                                       @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/notes").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class));
     }
 
     /**
      * The events this page is managing 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationEvents}
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationEvents(String application,
                                        @Optional @Default("last week") String since,
                                        @Optional @Default("yesterday") String until,
                                        @Optional @Default("3") String limit,
                                        @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/events").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .
 
             get(String.class));
     }
 
     /**
      * Usage metrics for this application 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationInsights}
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
     public Map<String, Object> getApplicationInsights(String application,
                                          @Optional @Default("last week") String since,
                                          @Optional @Default("yesterday") String until,
                                          @Optional @Default("3") String limit,
                                          @Optional @Default("2") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/insights").build(application);
         WebResource resource = client.resource(uri);
         return JSONMapper.toMap( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class));
     }
 
     public String getAppId()
     {
         return appId;
     }
 
     public void setAppId(String appId)
     {
         this.appId = appId;
     }
 
     public String getAppSecret()
     {
         return appSecret;
     }
 
     public void setAppSecret(String appSecret)
     {
         this.appSecret = appSecret;
     }
 
     public String getScope()
     {
         return scope;
     }
 
     public void setScope(String scope)
     {
         this.scope = scope;
     }
     
     public Client getClient()
     {
         return client;
     }
     
     public void setClient(Client client)
     {
         this.client = client;
     }
 }
