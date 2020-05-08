 /**
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.google.contact;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.apache.commons.io.IOUtils;
 import org.mule.api.MuleMessage;
 import org.mule.api.NestedProcessor;
 import org.mule.api.annotations.Configurable;
 import org.mule.api.annotations.Connector;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.oauth.OAuth2;
 import org.mule.api.annotations.oauth.OAuthAccessToken;
 import org.mule.api.annotations.oauth.OAuthAccessTokenIdentifier;
 import org.mule.api.annotations.oauth.OAuthAuthorizationParameter;
 import org.mule.api.annotations.oauth.OAuthConsumerKey;
 import org.mule.api.annotations.oauth.OAuthConsumerSecret;
 import org.mule.api.annotations.oauth.OAuthInvalidateAccessTokenOn;
 import org.mule.api.annotations.oauth.OAuthPostAuthorization;
 import org.mule.api.annotations.oauth.OAuthProtected;
 import org.mule.api.annotations.oauth.OAuthScope;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 import org.mule.modules.google.AbstractGoogleOAuthConnector;
 import org.mule.modules.google.AccessType;
 import org.mule.modules.google.ForcePrompt;
 import org.mule.modules.google.IdentifierPolicy;
 import org.mule.modules.google.api.domain.BatchResult;
 import org.mule.modules.google.api.util.DateTimeUtils;
 import org.mule.modules.google.oauth.invalidation.InvalidationAwareCredential;
 import org.mule.modules.google.oauth.invalidation.OAuthTokenExpiredException;
 
 import com.google.api.client.auth.oauth2.BearerToken;
 import com.google.api.client.auth.oauth2.Credential;
 import com.google.gdata.client.Service.GDataRequest;
 import com.google.gdata.client.batch.BatchInterruptedException;
 import com.google.gdata.client.contacts.ContactQuery;
 import com.google.gdata.client.contacts.ContactQuery.OrderBy;
 import com.google.gdata.client.contacts.ContactQuery.SortOrder;
 import com.google.gdata.client.contacts.ContactsService;
 import com.google.gdata.data.BaseEntry;
 import com.google.gdata.data.BaseFeed;
 import com.google.gdata.data.Link;
 import com.google.gdata.data.batch.BatchOperationType;
 import com.google.gdata.data.batch.BatchUtils;
 import com.google.gdata.data.contacts.ContactEntry;
 import com.google.gdata.data.contacts.ContactFeed;
 import com.google.gdata.data.contacts.ContactGroupEntry;
 import com.google.gdata.data.contacts.ContactGroupFeed;
 import com.google.gdata.data.contacts.GroupMembershipInfo;
 import com.google.gdata.util.ContentType;
 import com.google.gdata.util.ServiceException;
 
 /**
  * Cloud connector for the Google Contacts API v3 using OAuth2 for initialization.
  * Uses OAuth2 for authentication 
  *
  * @author mariano.gonzalez@mulesoft.com
  */
@Connector(name="google-contacts", schemaVersion="1.0", friendlyName="Google Contacts Connector with OAuth2 authentication", minMuleVersion="3.3", configElementName="config-with-oauth")
 @OAuth2(
 		authorizationUrl = "https://accounts.google.com/o/oauth2/auth",
 		accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
 				accessTokenRegex="\"access_token\"[ ]*:[ ]*\"([^\\\"]*)\"",
 				expirationRegex="\"expires_in\"[ ]*:[ ]*([\\d]*)",
 				refreshTokenRegex="\"refresh_token\"[ ]*:[ ]*\"([^\\\"]*)\"",
 				authorizationParameters={
 						@OAuthAuthorizationParameter(name="access_type", defaultValue="online", type=AccessType.class, description="Indicates if your application needs to access a Google API when the user is not present at the browser. " + 
 													" Use offline to get a refresh token and use that when the user is not at the browser. Default is online", optional=true),
 						@OAuthAuthorizationParameter(name="force_prompt", defaultValue="auto", type=ForcePrompt.class, description="Indicates if google should remember that an app has been authorized or if each should ask authorization every time. " + 
 													" Use force to request authorization every time or auto to only do it the first time. Default is auto", optional=true)
 				}
 )
 
 public class GoogleContactsConnector extends AbstractGoogleOAuthConnector {
 
 	private static final String CONTACT_FEED_URL = "https://www.google.com/m8/feeds/contacts/default/full";
 	private static final String GROUP_FEED_URL = "https://www.google.com/m8/feeds/groups/default/full";
 	
 	private static final String CONTACT_BATCH_FEED_URL = "https://www.google.com/m8/feeds/contacts/default/full/batch";
 	private static final String GROUP_BATCH_FEED_URL = "https://www.google.com/m8/feeds/groups/default/full/batch";
 	private static final String BATCH_REQUEST = "BATCH_REQUEST";
 	private static final int MAX_BATCH_OPERATIONS = 100;
 	
     /**
      * Application name registered on Google API console
      */
     @Configurable
     @Optional
     @Default("Mule-GoogleContactsConnector/1.0")
     private String applicationName;
     
 	/**
      * The OAuth2 consumer key 
      */
     @Configurable
     @OAuthConsumerKey
     private String consumerKey;
 
     /**
      * The OAuth2 consumer secret 
      */
     @Configurable
     @OAuthConsumerSecret
     private String consumerSecret;
     
     /**
      * The OAuth2 scopes you want to request
      */
     @OAuthScope
     @Configurable
     @Optional
     @Default(USER_PROFILE_SCOPE + " https://www.google.com/m8/feeds")
     private String scope;
     
     /**
      * This policy represents which id we want to use to represent each google account.
      * 
      * PROFILE means that we want the google profile id. That means, the user's primary key in google's DB.
      * This is a long number represented as a string.
      * 
      * EMAIL means you want to use the account's email address
      */
     @Configurable
     @Optional
     @Default("EMAIL")
     private IdentifierPolicy identifierPolicy = IdentifierPolicy.EMAIL;
     
 	@OAuthAccessToken
 	private String accessToken;
     
     /**
      * The actual instance of the {@link com.google.gdata.client.contacts.ContactsService}
      */
 	private ContactsService contactsService;
 	
 	/**
 	 * Contacts atom feed url
 	 */
 	private URL contactFeedURL = this.newURL(CONTACT_FEED_URL);
 	
 	/**
 	 * Groups atom feed url
 	 */
 	private URL groupFeedURL = this.newURL(GROUP_FEED_URL);
 	
 	/**
 	 * Contacts batch atom feed url
 	 */
 	private URL contactBatchUrl = this.newURL(CONTACT_BATCH_FEED_URL);
 	
 	/**
 	 * Groups batch atom feed url
 	 */
 	private URL grouptBatchUrl = this.newURL(GROUP_BATCH_FEED_URL);
 	
 	@OAuthAccessTokenIdentifier
    	public String getAccessTokenId() {
    		return this.identifierPolicy.getId(this);
    	}
    	
    	@OAuthPostAuthorization
    	public void postAuth() {
    		Credential credential = new InvalidationAwareCredential(BearerToken.authorizationHeaderAccessMethod());
    		credential.setAccessToken(this.getAccessToken());
    		
    		ContactsService service = new ContactsService(this.getApplicationName());
 		service.setOAuth2Credentials(credential);
 		service.setHeader("If-Match", "*");
 
 		this.setService(service);
    	}
 	
 	/**
 	 * Retrieves all the contacts matching the given criterias. If a criteria is not provided
 	 * then it's not used in the filtering. Thus, providing no criteria equals getting all contacts
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:get-contacts}
 	 * 
 	 * @param updatedMin Sets the minimum updated timestamp used for the query.  Only entries with
 	 * 					an updated timestamp equal to or later than the specified timestamp will be returned.
 	 * @param updatedMax Sets the maximum updated timestamp used for the query.  Only entries with
      * 					 an updated timestamp earlier than the specified timestamp will be returned.
 	 * @param datetimeFormat the pattern to be used for parsing updatedMin and updatedMax
 	 * @param fullTextQuery Sets the full text query string that will be used for the query.
 	 * @param maxResults  Sets the maximum number of results to return for the query.  Note: a GData server may choose to provide
 	 * 					fewer results, but will never provide more than the requested maximum.
 	 * @param firstResult Sets the start index for query results.  This is a 1-based index.
 	 * @param sortOrder valid values are NONE, ASCENDING and DESCENDING
 	 * @param showDeleted wether to show deleted entries or not
 	 * @param orderBy the field to be used when sorting. Valid values are NONE, LAST_MODIFIED and EDITED 
 	 * @param groupId only show contacts from a given group
 	 * @return a list with instances of com.google.gdata.data.contacts.ContactEntry
 	 * @throws IOException if there's a communication error with google's servers 
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public List<ContactEntry> getContacts(
 									@Optional String updatedMin,
 									@Optional String updatedMax,
 									@Optional @Default(DateTimeUtils.RFC3339) String datetimeFormat,
 									@Optional String fullTextQuery,
 									@Optional @Default("1000") int maxResults,
 									@Optional @Default("1") int firstResult,
 									@Optional @Default("NONE") SortOrder sortOrder,
 									@Optional @Default("false") Boolean showDeleted,
 									@Optional @Default("NONE") OrderBy orderBy,
 									@Optional String groupId) throws IOException, ServiceException {
 		
 		ContactQuery query = new ContactQuery(this.contactFeedURL);
 		
 		if (updatedMax != null) {
 			query.setUpdatedMax(DateTimeUtils.parseDateTime(updatedMax, datetimeFormat, null));
 		}
 		
 		if (updatedMin != null) {
 			query.setUpdatedMin(DateTimeUtils.parseDateTime(updatedMin, datetimeFormat, null));
 		}
 		
 		query.setFullTextQuery(fullTextQuery);
 		query.setMaxResults(maxResults);
 		query.setStartIndex(firstResult);
 		
 		query.setShowDeleted(showDeleted);
 		query.setOrderBy(orderBy);
 		query.setGroup(groupId);
 		query.setSortOrder(sortOrder);
 		
 		return this.getService().getFeed(query, ContactFeed.class).getEntries();
 	}
 	
 	/**
 	 * Retrieves a contact by id
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:get-contact-by-id}
 	 * 
 	 * @param id the id of the contact
 	 * @return an instance of {@link com.google.gdata.data.contacts.ContactEntry}
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public ContactEntry getContactById(String id) throws IOException, ServiceException {
 		
 		URL url = null;
 		try {
 			url = new URL(String.format("%s/%s", CONTACT_FEED_URL, id));
 		} catch (MalformedURLException e) {
 			throw new IllegalArgumentException(String.format("%s is not a valid contact id", id), e);
 		}
 		
 		return this.getService().getEntry(url, ContactEntry.class);
 	}
 	
 	/**
 	 * Inserts a new contact
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:insert-contact}
 	 * 
 	 * @param contact an instance of {@link com.google.gdata.data.contacts.ContactEntry} representing the contact to be inserted
 	 * @return an instance of {@link com.google.gdata.data.contacts.ContactEntry} representing the newly inserted contact
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public ContactEntry insertContact(@Optional @Default("#[payload:]") ContactEntry contact) throws IOException, ServiceException {
 		return this.getService().insert(this.contactFeedURL, contact);
 	}
 	
 	/**
 	 * Updates a contact entry
 	 *
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:update-contact}
 	 * 
 	 * @param contact an instance of {@link com.google.gdata.data.contacts.ContactEntry} representing the contact to be updated
 	 * @return an instance of {@link com.google.gdata.data.contacts.ContactEntry} representing the contact's updated state
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public ContactEntry updateContact(@Optional @Default("#[payload:]") ContactEntry contact) throws IOException, ServiceException {
 		return this.getService().update(this.contactFeedURL, contact);
 	}
 	
 	/**
 	 * Adds a group to a given contact
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:add-group}
 	 * 
 	 * @param contact an instance of {@link com.google.gdata.data.contacts.ContactEntry} representing the contact to be updated
 	 * @param groupId the id of the group
 	 * @return an instance of {@link com.google.gdata.data.contacts.ContactEntry} representing the contact's updated state
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public ContactEntry addGroup(@Optional @Default("#[payload:]") ContactEntry contact, String groupId) throws IOException, ServiceException {
 		contact.addGroupMembershipInfo(new GroupMembershipInfo(false, groupId));
 		URL editUrl = null;
 		
 		try {
 			editUrl = new URL(contact.getEditLink().getHref());
 		} catch (MalformedURLException e) {
 			throw new RuntimeException("The contact's edit link is not a valid URL");
 		}
 		
 		return this.getService().update(editUrl, contact);
 	}
 	
 	
 	/**
 	 * Deletes a contact signaled by its id
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:delete-contact-by-id}
 	 * 
 	 * @param contactId the id of the contact to be deleted
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public void deleteContactById(String contactId) throws IOException, ServiceException {
 		ContactEntry contact = this.getContactById(contactId);
 		this.deleteContact(contact);
 	}
 	
 	/**
 	 * Deletes a given contact
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:delete-contact}
 	 * 
 	 * @param entry the contact to be deleted
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public void deleteContact(@Optional @Default("#[payload:]") ContactEntry entry) throws IOException, ServiceException {
 		entry.delete();
 	}
 	
 	/**
 	 * Downloads the photo of a contact signaled by its id contact and returns it as an input stream
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:download-photo-by-id}
 	 * 
 	 * @param id the id of the contact whose photo we want
 	 * @return an instance of {@link java.io.InputStream}
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public InputStream downloadPhotoById(String id) throws IOException, ServiceException {
 		ContactEntry contact = this.getContactById(id);
 		return this.downloadPhoto(contact);
 	}
 	
 	/**
 	 * Downloads the photo of a given contact
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:download-photo}
 	 * 
 	 * @param contact the contact whose photo we want
 	 * @return an instance of {@link java.io.InputStream}
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public InputStream downloadPhoto(@Optional @Default("#[payload:]") ContactEntry contact) throws IOException, ServiceException {
 		Link photoLink = contact.getContactEditPhotoLink();
 
 		if (photoLink != null) {
 		
 			GDataRequest request = this.getService().createLinkQueryRequest(photoLink);
 		    request.execute();
 		    return request.getResponseStream();
 		
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Updates the photo of a contact signaled by its id taken a {@link java.io.InputStream} as an input
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:update-contact-photo}
 	 * 
 	 * @param contactId the id of the contact whose photo we want to update
 	 * @param in a {@link java.io.InputStream} with the photo's binary content
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public void updateContactPhoto(String contactId, @Optional @Default("#[payload:]") InputStream in) throws IOException, ServiceException {
 		ContactEntry contact = this.getContactById(contactId);
 		Link photoLink = contact.getContactPhotoLink();
 		URL photoUrl = new URL(photoLink.getHref());
 
 		GDataRequest request = this.getService().createRequest(GDataRequest.RequestType.UPDATE, photoUrl, new ContentType("image/jpeg"));
 		request.setEtag(photoLink.getEtag());
 		OutputStream requestStream = request.getRequestStream();
 		requestStream.write(IOUtils.toByteArray(in));
 
 	    try {
 	    	request.execute();
 	    } finally {
 	    	IOUtils.closeQuietly(in);
 	    }
 	}
 	
 	/**
 	 * Deletes the photo of a contact signaled by its id
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:delete-contact-photo-by-id}
 	 * 
 	 * @param contactId the id of the contact whose photo we want to delete
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public void deleteContactPhotoById(String contactId) throws IOException, ServiceException {
 		this.deleteContactPhoto(this.getContactById(contactId));
 	}
 	
 	/**
 	 * Deletes the photo associated to a given contact
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:delete-contact-photo}
 	 * 
 	 * @param contact an instance of {@link com.google.gdata.data.contacts.ContactEntry} representing the contact whose photo we want deleted
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public void deleteContactPhoto(@Optional @Default("#[payload:]") ContactEntry contact) throws IOException, ServiceException {
 		Link photoLink = contact.getContactPhotoLink();
 		URL photoUrl = new URL(photoLink.getHref());
 		this.getService().delete(photoUrl, photoLink.getEtag());
 	}
 	
 	/**
 	 * Returns all the groups the authenticated user has access to
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:delete-contact-photo}
 	 * 
 	 * @param accessToken the OAuth2 access token
 	 * @param updatedMin Sets the minimum updated timestamp used for the query.  Only entries with
 	 * 					an updated timestamp equal to or later than the specified timestamp will be returned. 
 	 * @param updatedMax Sets the maximum updated timestamp used for the query.  Only entries with
 	 * 					  an updated timestamp earlier than the specified timestamp will be returned.
 	 * @param datetimeFormat the date pattern used to parse updatedMin and updatedMax
 	 * @return a list with instances of {@link com.google.gdata.data.contacts.ContactGroupEntry}
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public List<ContactGroupEntry> getGroups(
 											@Optional String updatedMin,
 											@Optional String updatedMax,
 											@Optional @Default(DateTimeUtils.RFC3339) String datetimeFormat ) throws IOException, ServiceException {
 		
 		ContactQuery query = new ContactQuery(this.groupFeedURL);
 		
 		if (updatedMax != null) {
 			query.setUpdatedMax(DateTimeUtils.parseDateTime(updatedMax, datetimeFormat, null));
 		}
 		
 		if (updatedMin != null) {
 			query.setUpdatedMin(DateTimeUtils.parseDateTime(updatedMin, datetimeFormat, null));
 		}
 		
 		return this.getService().getFeed(query, ContactGroupFeed.class).getEntries();
 	}
 	
 	/**
 	 * Retrieves a group with the given name
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:get-group-by-name}
 	 * 
 	 * @param groupName the name of the group you want
 	 * @return an instance of {@link com.google.gdata.data.contacts.ContactGroupEntry} or <code>null</code> if the group doesn't exist
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public ContactGroupEntry getGroupByName(String groupName) throws IOException, ServiceException {
 		List<ContactGroupEntry> groups = this.getGroups(null, null, null);
 		
 		if (groups != null)	{
 			for (ContactGroupEntry group : groups) {
 				if (group.getPlainTextContent().equals(groupName)) {
 					return group;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Retrieves a group by id
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:get-group-by-id}
 	 * 
 	 * @param id the id of the group to be returned
 	 * @return an instance of {@link com.google.gdata.data.contacts.ContactGroupEntry}
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public ContactGroupEntry getGroupById(String id) throws IOException, ServiceException {
 		URL url = null;
 		try {
 			url = new URL(String.format("%s/%s", GROUP_FEED_URL, id));
 		} catch (MalformedURLException e) {
 			throw new IllegalArgumentException(String.format("%s is not a valid group id", id), e);
 		}
 		
 		return this.getService().getEntry(url, ContactGroupEntry.class);
 	}
 	
 	/**
 	 * Inserts a new group
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:create-group}
 	 * 
 	 * @param group an instance of {@link com.google.gdata.data.contacts.ContactGroupEntry} representing the group to be inserted
 	 * @return an instance of {@link com.google.gdata.data.contacts.ContactGroupEntry} representing the newly created group
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public ContactGroupEntry createGroup(@Optional @Default("#[payload:]") ContactGroupEntry group) throws IOException, ServiceException {
 		return this.getService().insert(this.groupFeedURL, group);
 	}
 	
 	/**
 	 * Updates the state of a group
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:update-group}
 	 * 
 	 * @param group an instance of {@link com.google.gdata.data.contacts.ContactGroupEntry} with the group's new state
 	 * @return an instance of {@link com.google.gdata.data.contacts.ContactGroupEntry} reflecting the group's updated state
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public ContactGroupEntry updateGroup(@Optional @Default("#[payload:]") ContactGroupEntry group)  throws IOException, ServiceException {
 		return this.getService().update(this.groupFeedURL, group);
 	}
 	
 	/**
 	 * Deletes a group signaled by its id
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:delete-group-by-id}
 	 * 
 	 * @param groupId the id of the group to be deleted
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public void deleteGroupById(String groupId) throws IOException, ServiceException {
 		this.deleteGroup(this.getGroupById(groupId));
 	}
 	
 	/**
 	 * Deletes the given group
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:delete-group}
 	 * 
 	 * @param group an instance of {@link com.google.gdata.data.contacts.ContactGroupEntry} representing the group to be deleted
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public void deleteGroup(@Optional @Default("#[payload:]") ContactGroupEntry group) throws IOException, ServiceException {
 		group.delete();
 	}
 	
 	/**
 	 * This tag encloses a series of nested processors that perform operations on contacts entities 
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:batch-contacts}
 	 * 
 	 * @param batchId an id to identify the batch
 	 * @param operations a list with instances of {@link org.mule.api.NestedProcessor} representing the operations to be performed in the batch
 	 * @return a list with instances of {@link org.mule.modules.google.api.domain.BatchResult}
 	 * @throws Exception if an error is encountered
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public List<BatchResult> batchContacts(@Optional String batchId, List<NestedProcessor> operations) throws Exception {
 		return this.batch(operations, this.contactBatchUrl, new ContactFeed());
 	}
 	
 	/**
 	 * This tag encloses a series of nested processors that perform operations on group entities.
 	 * According to the API's specification the maximum number of operations allowed in one batch is 100. Thus,
 	 * this processor will automatically group the operations in batches of 100. 
 	 * 
 	 * According to the API's specification the maximum number of operations allowed in one batch is 100. Thus,
 	 * this processor will automatically group the operations in batches of 100.
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:batch-groups}
 	 * 
 	 * @param batchId an id to identify the batch
 	 * @param operations a list with instances of {@link org.mule.api.NestedProcessor} representing the operations to be performed in the batch
 	 * @return a list with instances of {@link org.mule.modules.google.api.domain.BatchResult}
 	 * @throws Exception if an error is encountered
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	public List<BatchResult> batchGroups(@Optional String batchId, List<NestedProcessor> operations) throws Exception {
 		return this.batch(operations, this.grouptBatchUrl, new ContactGroupFeed());
 	}
 	
 	/**
 	 * The function of this sub processor is to add insert operations into the current batch for the given entries
 	 * 
 	 * This processor is intended to be used nested inside batch-contacts or batch-groups, although there's no syntactic
 	 * validation inside the connector's XSD enforcing that. However, if you don't use it that way, then it's most
 	 * likely to throw a {@link java.lang.IllegalStateException}
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:batch-insert}
 	 * 
 	 * @param message the current mule message
 	 * @param operationId id to identify this particular operation inside the batch
 	 * @param entries a collection with instances of {@link com.google.gdata.data.BaseEntry} to be batch inserted
 	 * @throws IllegalStateException if not nested in batch-contact or batch-group 
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	@Inject
 	public void batchInsert(MuleMessage message, String operationId, @Optional @Default("#[payload:]") Collection<BaseEntry<?>> entries) {
 		
 		for (BaseEntry<?> entry : entries) {
 			this.addBatchOperation(entry, operationId, BatchOperationType.INSERT, message);
 		}
 	}
 	
 	/**
 	 * The function of this sub processor is to add updated operations into the current batch for the given entries
 	 * 
 	 * This processor is intended to be used nested inside batch-contacts or batch-groups, although there's no syntactic
 	 * validation inside the connector's XSD enforcing that. However, if you don't use it that way, then it's most
 	 * likely to throw a {@link java.lang.IllegalStateException}
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:batch-update}
 	 * 
 	 * @param message the current mule message
 	 * @param operationId id to identify this particular operation inside the batch
 	 * @param entries a collection with instances of {@link com.google.gdata.data.BaseEntry} to be batch updated
 	 * @throws IllegalStateException if not nested in batch-contact or batch-group
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	@Inject
 	public void batchUpdate(MuleMessage message, String operationId, @Optional @Default("#[payload:]") Collection<BaseEntry<?>> entries) {
 		
 		for (BaseEntry<?> entry : entries) {
 			this.addBatchOperation(entry, operationId, BatchOperationType.UPDATE, message);
 		}
 	}
 	
 	/**
 	 * The function of this sub processor is to add updated operations into the current batch for the given entries
 	 * 
 	 * This processor is intended to be used nested inside batch-contacts or batch-groups, although there's no syntactic
 	 * validation inside the connector's XSD enforcing that. However, if you don't use it that way, then it's most
 	 * likely to throw a {@link java.lang.IllegalStateException}
 	 * 
 	 * {@sample.xml ../../../doc/GoogleContactsConnector.xml.sample google-contacts:batch-delete}
 	 * 
 	 * @param message the current mule message
 	 * @param operationId id to identify this particular operation inside the batch
 	 * @param entries a collection with instances of {@link com.google.gdata.data.BaseEntry} to be batch updated
 	 * @throws IllegalStateException if not nested in batch-contact or batch-group
 	 */
 	@Processor
 	@OAuthProtected
 	@OAuthInvalidateAccessTokenOn(exception=OAuthTokenExpiredException.class)
 	@Inject
 	public void batchDelete(MuleMessage message, String operationId, @Optional @Default("#[payload:]") Collection<BaseEntry<?>> entries) {
 		
 		for (BaseEntry<?> entry : entries) {
 			this.addBatchOperation(entry, operationId, BatchOperationType.DELETE, message);
 		}
 	}
 	
 	/**
 	 * Basic skeleton for a batch operation. This private method creates and groups the batch operations
 	 * in groups of 100 
 	 * 
 	 * @param operations the nested processors describing the operations
 	 * @param batchUrl the url of the batch feed
 	 * @param batchRequest the feed grouping the requests
 	 * @return a list with instances of {@link org.mule.modules.google.api.domain.BatchResult}
 	 * @throws Exception if an error is encountered
 	 */
 	private List<BatchResult> batch(List<NestedProcessor> operations, URL batchUrl, BaseFeed<?, ?> batchRequest) throws Exception {
 
 		Map<String, Object> properties = new HashMap<String, Object>();
 		properties.put(BATCH_REQUEST, batchRequest);
 		
 		int operationsCount = 0;
 		List<BatchResult> results = new ArrayList<BatchResult>();
 		
 		for (NestedProcessor operation : operations) {
 		
 			if (operationsCount > MAX_BATCH_OPERATIONS) {
 				results.addAll(this.executeBatch(batchUrl, batchRequest));
 				operationsCount = 0;
 			}
 		
 			operation.processWithExtraProperties(properties);
 			operationsCount++;
 		}
 		
 		if (operationsCount > 0) {
 			results.addAll(this.executeBatch(batchUrl, batchRequest));
 		}
 		
 		return results;
 	}
 	
 	/**
 	 * Adds an operation to the batch
 	 * 
 	 * @param entry the entry to be processed in the batch
 	 * @param operationId the id of the operation
 	 * @param operationType the type of the operation
 	 * @param message current mule message
 	 */
 	private void addBatchOperation(BaseEntry<?> entry, String operationId, BatchOperationType operationType, MuleMessage message) {
 		BaseFeed<?, BaseEntry<?>> batchRequest = message.getInvocationProperty(BATCH_REQUEST);
 		
 		if (batchRequest == null) {
 			throw new IllegalStateException("Can only be invoked as a nested processor of batchContact or batchGroup");
 		}
 		
 		BatchUtils.setBatchId(entry, operationId);
 		BatchUtils.setBatchOperationType(entry, operationType);
 		batchRequest.getEntries().add(entry);
 	}
 	
 	/**
 	 * Executes the batch
 	 * 
 	 * @param batchUrl the URL of the batch feed
 	 * @param batchRequest the feed containing the batch operations 
 	 * @return a list with instances of {@link org.mule.modules.google.api.domain.BatchResult}
 	 * @throws IOException if there's a communication error with google's servers
 	 * @throws ServiceException if the operation raises an error on google's end
 	 */
 	private List<BatchResult> executeBatch(URL batchUrl, BaseFeed<?, ?> batchRequest) throws IOException, ServiceException {
 		BaseFeed<?, ?> response = null;
 		
 		try {
 			response = this.getService().batch(batchUrl, batchRequest);
 		} catch (BatchInterruptedException e) {
 			throw new RuntimeException("The batch has been interrupted", e);
 		}
 		
 		@SuppressWarnings("unchecked")
 		List<BaseEntry<?>> entries = (List<BaseEntry<?>>) response.getEntries();
 		
 		if (entries.isEmpty()) {
 			return new ArrayList<BatchResult>();
 		}
 		
 		List<BatchResult> results = new ArrayList<BatchResult>(entries.size());
 		
 		for (BaseEntry<?> entry : entries) {
 			results.add(new BatchResult(entry));
 		}
 		
 		return results;
 	}
 	
 	protected final ContactsService getService() {
 		return this.contactsService;
 	}
 	
 	/**
 	 * 
 	 * @param href
 	 * @return
 	 */
 	private URL newURL(String href) {
 		try {
 			return new URL(href); 
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(String.format("%s is not a valid url", href), e);
 		}
 	}
 
 	public void setService(ContactsService contactsService) {
 		this.contactsService = contactsService;
 	}
 
 	public String getApplicationName() {
 		return applicationName;
 	}
 
 	public void setApplicationName(String applicationName) {
 		this.applicationName = applicationName;
 	}
 	
 	public String getConsumerKey() {
 		return consumerKey;
 	}
 
 	public void setConsumerKey(String consumerKey) {
 		this.consumerKey = consumerKey;
 	}
 
 	public String getConsumerSecret() {
 		return consumerSecret;
 	}
 
 	public void setConsumerSecret(String consumerSecret) {
 		this.consumerSecret = consumerSecret;
 	}
 
 	public String getScope() {
 		return scope;
 	}
 
 	public void setScope(String scope) {
 		this.scope = scope;
 	}
 
 	@Override
 	public String getAccessToken() {
 		return accessToken;
 	}
 
 	public void setAccessToken(String accessToken) {
 		this.accessToken = accessToken;
 	}
 
 	public IdentifierPolicy getIdentifierPolicy() {
 		return identifierPolicy;
 	}
 
 	public void setIdentifierPolicy(IdentifierPolicy identifierPolicy) {
 		this.identifierPolicy = identifierPolicy;
 	}
 }
