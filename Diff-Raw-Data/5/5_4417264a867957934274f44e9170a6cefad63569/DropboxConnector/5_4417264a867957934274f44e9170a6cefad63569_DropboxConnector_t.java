 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package com.mulesoft.module.dropbox;
 
 import com.mulesoft.module.dropbox.exception.DropboxException;
 import com.mulesoft.module.dropbox.exception.DropboxTokenExpiredException;
 import com.mulesoft.module.dropbox.jersey.AuthBuilderBehaviour;
 import com.mulesoft.module.dropbox.jersey.DropboxResponseHandler;
 import com.mulesoft.module.dropbox.jersey.MediaTypesBuilderBehaviour;
 import com.mulesoft.module.dropbox.jersey.json.GsonFactory;
 import com.mulesoft.module.dropbox.model.AccountInformation;
 import com.mulesoft.module.dropbox.model.Chunk;
 import com.mulesoft.module.dropbox.model.Item;
 import com.mulesoft.module.dropbox.model.Link;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.json.JSONConfiguration;
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.core.impl.provider.entity.FormMultivaluedMapProvider;
 import com.sun.jersey.core.impl.provider.entity.FormProvider;
 import com.sun.jersey.core.impl.provider.entity.InputStreamProvider;
 import com.sun.jersey.core.impl.provider.entity.MimeMultipartProvider;
 import com.sun.jersey.multipart.FormDataBodyPart;
 import com.sun.jersey.multipart.FormDataMultiPart;
 import com.sun.jersey.multipart.MultiPart;
 import com.sun.jersey.multipart.impl.MultiPartWriter;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.mule.api.MuleException;
 import org.mule.api.annotations.Configurable;
 import org.mule.api.annotations.Connector;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.lifecycle.Start;
 import org.mule.api.annotations.oauth.*;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 import org.mule.api.annotations.param.Payload;
 import org.mule.commons.jersey.JerseyUtil;
 import org.mule.commons.jersey.provider.GsonProvider;
 
 import javax.ws.rs.core.MediaType;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Date;
 
 /**
  * Dropbox Cloud Connector.
  * The Dropbox Connector will allow to use the Dropbox REST API. Almost every operation that can be done via the API can be done thru this connector.
  * 
  * @author MuleSoft, Inc.
  */
 @Connector(name = "dropbox", schemaVersion = "3.3.0", friendlyName = "Dropbox", minMuleVersion = "3.3")
 @OAuth2(authorizationUrl = "https://www.dropbox.com/1/oauth2/authorize",
 		accessTokenUrl = "https://api.dropbox.com/1/oauth2/token",
         accessTokenRegex = "\"access_token\"[ ]*:[ ]*\"([^\\\"]*)\"",
         expirationRegex = "\"expires_in\"[ ]*:[ ]*([\\d]*)",
         refreshTokenRegex = "\"refresh_token\"[ ]*:[ ]*\"([^\\\"]*)\"")
 public class DropboxConnector {
     private static final String ROOT_PARAM = "dropbox";
 
     private static final int MAX_UPLOAD_BUFFER_LEN = 4194304;
 
     private String accessTokenIdentifier;
 
 	/**
 	 * URL of the Dropbox server API
 	 */
 	@Configurable
 	@Optional
 	@Default("https://api.dropbox.com/1/")
 	private String server;
 
 	/**
 	 * URL of the Dropbox server content API
 	 */
 	@Configurable
 	@Optional
 	@Default("https://api-content.dropbox.com/1/")
 	private String contentServer;
 
 	/**
 	 * Application key
 	 */
 	@Configurable
 	@OAuthConsumerKey
 	private String appKey;
 
 	/**
 	 * Application secret
 	 */
 	@Configurable
 	@OAuthConsumerSecret
 	private String appSecret;
 
 	@OAuthAccessToken
 	private String accessToken;
 
     private JerseyUtil jerseyUtil;
 
     private JerseyUtil jerseyUtilUpload;
 
     private WebResource apiResource;
 
     private WebResource contentResource;
 
     /**
      * This method initiates the dropbox client and the auth callback.
      * @throws MuleException
      */
     @Start
     public void init() throws MuleException {
         ClientConfig clientConfig = new DefaultClientConfig();
         clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
         clientConfig.getClasses().add(MultiPartWriter.class);
         clientConfig.getClasses().add(MimeMultipartProvider.class);
         clientConfig.getClasses().add(InputStreamProvider.class);
         clientConfig.getClasses().add(FormProvider.class);
         clientConfig.getClasses().add(FormMultivaluedMapProvider.class);
         clientConfig.getSingletons().add(new GsonProvider(GsonFactory.get()));
 
         Client client = Client.create(clientConfig);
         Client contentClient = Client.create(clientConfig);
         contentClient.setChunkedEncodingSize(null);
 
         this.initJerseyUtil();
 
         this.apiResource = client.resource(this.server);
         this.contentResource = contentClient.resource(this.contentServer);
     }
 
     private void initJerseyUtil() {
         JerseyUtil.Builder builder = JerseyUtil.builder().addRequestBehaviour(MediaTypesBuilderBehaviour.INSTANCE)
                 .addRequestBehaviour(new AuthBuilderBehaviour(this)).setResponseHandler(DropboxResponseHandler.INSTANCE);
 
         JerseyUtil.Builder builderContent = JerseyUtil.builder()
                 .addRequestBehaviour(new AuthBuilderBehaviour(this)).setResponseHandler(DropboxResponseHandler.INSTANCE);
 
         this.jerseyUtil = builder.build();
         this.jerseyUtilUpload = builderContent.build();
     }
 
     @OAuthAccessTokenIdentifier
     public String getOAuthTokenAccessIdentifier() throws Exception {
         if (this.accessTokenIdentifier == null) {
             this.accessTokenIdentifier = this.getAccount().getUid();
         }
 
         return this.accessTokenIdentifier;
     }
 
 	/**
 	 * Upload file to Dropbox. The payload is an InputStream containing bytes of
 	 * the data to be uploaded.
      *
      * You can upload files of up to 150MB with this method. Use upload-long-stream for larger files
 	 * 
 	 * {@sample.xml ../../../doc/Dropbox-connector.xml.sample dropbox:upload-stream}
 	 * 
 	 * @param fileData
 	 *            file to be uploaded
 	 * @param overwrite
 	 * 				overwrite file in case it already exists           
 	 * @param path
 	 *            The destination path
 	 * @param filename
 	 *            The destination file name
 	 * 
 	 * @return Item with the metadata of the uploaded object
 	 * @throws Exception
 	 *             exception
 	 */
 	@SuppressWarnings("resource")
     @Processor
 	@OAuthProtected
     @OAuthInvalidateAccessTokenOn(exception = DropboxTokenExpiredException.class)
 	public Item uploadStream(@Payload InputStream fileData,
 							@Optional @Default("true") Boolean overwrite,
 							String path,
 							String filename) throws Exception {
 
         final FormDataBodyPart formDataBodyPart = new FormDataBodyPart(fileData, MediaType.APPLICATION_OCTET_STREAM_TYPE);
         MultiPart parts = new FormDataMultiPart().bodyPart(formDataBodyPart);
 
         formDataBodyPart.setContentDisposition(FormDataContentDisposition
                 .name("file")
                 .fileName(filename)
                 .size(fileData.available())
                 .modificationDate(new Date()).build());
 
         WebResource.Builder r = this
                             .contentResource
                             .path("files")
                             .path(ROOT_PARAM)
                             .path(adaptPath(path))
                             .queryParam("overwrite", overwrite.toString())
                             .entity(parts)
                             .accept(MediaType.APPLICATION_JSON)
                             .type(MediaType.MULTIPART_FORM_DATA_TYPE);
 
         return this.jerseyUtilUpload.post(r, Item.class, 200);
 	}
 
 	/**
 	 * Create new folder on Dropbox
 	 * 
 	 * {@sample.xml ../../../doc/Dropbox-connector.xml.sample dropbox:create-folder}
 	 * 
 	 * @param path
 	 *            full path of the folder to be created
 	 * 
 	 * @return Item with the metadata of the created folder
 	 * @throws Exception
 	 *             exception
 	 */
 	@Processor
 	@OAuthProtected
     @OAuthInvalidateAccessTokenOn(exception = DropboxTokenExpiredException.class)
 	public Item createFolder(String path) throws Exception {
 
         Item folder = this.jerseyUtil.post(
                         this.apiResource.path("fileops").path("create_folder").queryParam("root", ROOT_PARAM).queryParam("path", path), Item.class, 200, 403);
 
         // A 403 response means that the folder already exists
         if (folder.getPath() == null)
             return this.list(path);
 
         return folder;
 	}
 
 	/**
 	 * Deletes a file or folder.
 	 * 
 	 * {@sample.xml ../../../doc/Dropbox-connector.xml.sample dropbox:delete}
 	 * 
 	 * @param path
 	 *            full path to the file to be deleted
 	 * 
 	 * @return Item with the metadata of the deleted object
 	 * @throws Exception
 	 *             exception
 	 */
 	@Processor
 	@OAuthProtected
     @OAuthInvalidateAccessTokenOn(exception = DropboxTokenExpiredException.class)
 	public Item delete(String path) throws Exception {
         return this.jerseyUtil.post(
                 this.apiResource.path("fileops").path("delete").queryParam("root", ROOT_PARAM).queryParam("path", path), Item.class, 200);
 	}
 
 	/**
 	 * Downloads a file from Dropbox
 	 * 
 	 * {@sample.xml ../../../doc/Dropbox-connector.xml.sample dropbox:download-file}
 	 * 
 	 * @param path
 	 *            path to the file
 	 * @param delete
 	 *            delete the file on the Dropbox after download (ignored if
 	 *            moveTo is set)
 	 *
 	 * @return Stream containing the downloaded file data
 	 * @throws Exception
 	 *             exception
 	 */
 	@Processor
 	@OAuthProtected
     @OAuthInvalidateAccessTokenOn(exception = DropboxTokenExpiredException.class)
     public InputStream downloadFile(String path,
 			@Optional @Default("false") boolean delete) throws Exception {
 
        InputStream response = this.jerseyUtil.get(this.contentResource
                                                            .path("files")
                                                            .path(ROOT_PARAM)
                                                            .path(adaptPath(path)), InputStream.class, 200);
 
 		if (delete)
 			this.delete(path);
 
 		return response;
 	}
 
 	/**
 	 * Lists the content of the remote directory
 	 * 
 	 * {@sample.xml ../../../doc/Dropbox-connector.xml.sample dropbox:list}
 	 * 
 	 * @param path
 	 *            path to the remote directory
 	 * 
 	 * @return List of files and/or folders
 	 * @throws Exception
 	 *             exception
 	 */
 	@Processor
 	@OAuthProtected
     @OAuthInvalidateAccessTokenOn(exception = DropboxTokenExpiredException.class)
 	public Item list(String path) throws Exception {
 		final String apiPath = adaptPath(path);
 
         return this.jerseyUtil.get(
                 this.apiResource.path("metadata").path("dropbox").path(apiPath), Item.class, 200);
 	}
 
 	/**
 	 * Moves a file or folder to a new location.
 	 * 
 	 * {@sample.xml ../../../doc/Dropbox-connector.xml.sample dropbox:move}
 	 * 
 	 * @param from
 	 *            Specifies the file or folder to be moved from, relative to
 	 *            root.
 	 * @param to
 	 *            Specifies the destination path, including the new name for the
 	 *            file or folder, relative to root.
 	 *            
 	 * @return Item with the metadata of the moved object
 	 * @throws Exception
 	 *             exception
 	 */
 	@Processor
 	@OAuthProtected
     @OAuthInvalidateAccessTokenOn(exception = DropboxTokenExpiredException.class)
 	public Item move(String from, String to) throws Exception {
         return this.jerseyUtil.post(
                 this.apiResource.path("fileops")
                                 .path("move")
                                 .queryParam("root", ROOT_PARAM)
                                 .queryParam("from_path", adaptPath(from))
                                 .queryParam("to_path", adaptPath(to)), Item.class, 200);
 	}
 	
 	/**
      * Copies a file or folder to a new location.
      * 
      * {@sample.xml ../../../doc/Dropbox-connector.xml.sample dropbox:copy}
      * 
      * @param from
      *            Specifies the file or folder to be copied from, relative to
      *            root.
      * @param to
      *            Specifies the destination path, including the new name for the
      *            file or folder, relative to root.
      *            
      * @return Item with the metadata of the copied object
      * @throws Exception
      *             exception
      */
     @Processor
     @OAuthProtected
     @OAuthInvalidateAccessTokenOn(exception = DropboxTokenExpiredException.class)
     public Item copy(String from, String to) throws Exception {
         return this.jerseyUtil.post(
                 this.apiResource.path("fileops")
                                 .path("copy")
                                 .queryParam("root", ROOT_PARAM)
                                 .queryParam("from_path", adaptPath(from))
                                 .queryParam("to_path", adaptPath(to)), Item.class, 200);
     }
 
 	/**
 	 * Creates and returns a Dropbox link to files or folders users can use to view a preview of the file in a web browser.
 	 * 
 	 * {@sample.xml ../../../doc/Dropbox-connector.xml.sample dropbox:get-link}
 	 * 
 	 * @param path The path to the file or folder you want to link to.
 	 * @param shortUrl Boolean indicating if the url returned will be shortened using the Dropbox url shortener (when true) or will link directly to the file's preview page (when false).
 	 * @return Link. A Dropbox link to the given path.
 	 * 
 	 * @throws Exception exception
 	 */
 	@Processor
 	@OAuthProtected
     @OAuthInvalidateAccessTokenOn(exception = DropboxTokenExpiredException.class)
 	public Link getLink(String path, @Optional @Default("true") Boolean shortUrl) throws Exception {
 		path = adaptPath(path);
 
         return this.jerseyUtil.get(
                 this.apiResource.path("shares")
                                 .path("dropbox")
                                 .path(path)
                                 .queryParam("short_url", shortUrl.toString()), Link.class, 200);
 	}
 
     /**
      * Requests the account's information.
      *
      * {@sample.xml ../../../doc/Dropbox-connector.xml.sample dropbox:get-account}
      *
      * @return AccountInformation. A Dropbox account's information.
      *
      * @throws Exception exception
      */
     @Processor
     @OAuthProtected
     @OAuthInvalidateAccessTokenOn(exception = DropboxTokenExpiredException.class)
     public AccountInformation getAccount() throws Exception {
         return this.jerseyUtil.get(
                 this.apiResource.path("account").path("info"), AccountInformation.class, 200);
     }
 
 
     /**
      * Upload file to Dropbox. The payload is an InputStream containing bytes of
      * the data to be uploaded.
      *
      * This version of the method supports streams of arbitrary length
      *
      * {@sample.xml ../../../doc/Dropbox-connector.xml.sample dropbox:upload-long-stream}
      *
      * @param fileData
      *            file to be uploaded
      * @param overwrite
      * 				overwrite file in case it already exists
      * @param path
      *            The destination path
      * @param filename
      *            The destination file name
      *
      * @return Item with the metadata of the uploaded object
      * @throws Exception
      *             exception
      */
     @SuppressWarnings("resource")
     @Processor
     @OAuthProtected
     @OAuthInvalidateAccessTokenOn(exception = DropboxTokenExpiredException.class)
     public Item uploadLongStream(@Payload InputStream fileData,
                                  @Optional @Default("true") Boolean overwrite,
                                  String path,
                                  String filename) throws Exception {
 
         byte[] buffer = new byte[MAX_UPLOAD_BUFFER_LEN];
         Long readBytesAccum = 0L;
         int readBytes = 0;
         String uploadId = null;
 
         while(readBytes >= 0) {
             readBytes = fileData.read(buffer);
 
             ByteArrayInputStream chunk = new ByteArrayInputStream(ArrayUtils.subarray(buffer,0, readBytes));
 
             if (readBytes > 0) {
                 WebResource r = this
                         .contentResource
                         .path("chunked_upload");
 
                 if (uploadId != null)
                     r = r.queryParam("upload_id", uploadId)
                          .queryParam("offset", readBytesAccum.toString());
 
                 WebResource.Builder request = r
                         .entity(chunk)
                         .accept(MediaType.APPLICATION_JSON)
                         .type(MediaType.APPLICATION_OCTET_STREAM);
 
                 Chunk uploadedChunk = this.jerseyUtilUpload.put(request, Chunk.class, 200);
 
                 // Set the uploadId after the first successful upload
                 if (uploadId == null && uploadedChunk != null)
                     uploadId = uploadedChunk.getUploadId();
 
                 readBytesAccum += readBytes;
 
                 if (!uploadedChunk.getOffset().equals(readBytesAccum)) {
                     throw new DropboxException("Error while uploading file. Offsets do not match");
                 }
             }
         }
 
         WebResource r = this.contentResource
                                     .path("commit_chunked_upload")
                                     .path(ROOT_PARAM)
                                     .path(path)
                                     .path(filename);
 
         Item file = this.list(StringUtils.join( new String[] {path, filename } , "/"));
         if (file != null) {
             r = r.queryParam("parent_rev", file.getRev());
         }
 
         return jerseyUtil.post(r.queryParam("overwrite", overwrite.toString())
                 .queryParam("upload_id", uploadId)
                 .accept(MediaType.APPLICATION_JSON)
                 .type(MediaType.APPLICATION_JSON), Item.class, 200);
     }
 
 	// --------------------------------------
 	public String getServer() {
 		return server;
 	}
 
 	public void setServer(String server) {
 		this.server = server;
 	}
 
 	public String getContentServer() {
 		return contentServer;
 	}
 
 	public void setContentServer(String contentServer) {
 		this.contentServer = contentServer;
 	}
 
 	public String getAppKey() {
 		return appKey;
 	}
 
 	public void setAppKey(String appKey) {
 		this.appKey = appKey;
 	}
 
 	public String getAppSecret() {
 		return appSecret;
 	}
 
 	public void setAppSecret(String appSecret) {
 		this.appSecret = appSecret;
 	}
 
 	public String getAccessToken() {
 		return accessToken;
 	}
 
 	public void setAccessToken(String accessToken) {
 		this.accessToken = accessToken;
 	}
 
 	private String adaptPath(String path) {
 		if (path.startsWith("/")) {
 			path = path.substring(1);
 		}
 		return path;
 	}
 }
