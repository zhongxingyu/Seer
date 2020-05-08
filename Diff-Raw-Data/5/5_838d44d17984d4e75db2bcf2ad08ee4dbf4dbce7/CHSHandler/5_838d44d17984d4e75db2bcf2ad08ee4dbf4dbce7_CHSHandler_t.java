 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008 Timefields Ltd
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sdata.tool;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.sdata.FileItemIterator;
 import org.apache.commons.fileupload.sdata.FileItemStream;
 import org.apache.commons.fileupload.sdata.servlet.ServletFileUpload;
 import org.apache.commons.fileupload.sdata.util.Streams;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.Kernel;
 import org.sakaiproject.content.api.ContentCollection;
 import org.sakaiproject.content.api.ContentCollectionEdit;
 import org.sakaiproject.content.api.ContentEntity;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentResourceEdit;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.exception.IdInvalidException;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.InconsistentException;
 import org.sakaiproject.exception.OverQuotaException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.ServerOverloadException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.sdata.tool.api.Handler;
 import org.sakaiproject.sdata.tool.api.ResourceDefinition;
 import org.sakaiproject.sdata.tool.api.ResourceDefinitionFactory;
 import org.sakaiproject.sdata.tool.api.ResourceFunctionFactory;
 import org.sakaiproject.sdata.tool.api.SDataException;
 import org.sakaiproject.sdata.tool.api.SDataFunction;
 import org.sakaiproject.sdata.tool.model.CHSNodeMap;
 import org.sakaiproject.sdata.tool.util.ResourceDefinitionFactoryImpl;
 import org.sakaiproject.tool.api.Tool;
 import org.sakaiproject.util.Validator;
 
 /**
  * <p>
  * CHSServlet is a servlet that gives access to the CHS returning the content of
  * files within the chs or a map response (directories). The resource is pointed
  * to using the URI/URL requested (the path info part), and the standard Http
  * methods do what they are expected to in the http standard. GET gets the
  * content of the file, PUT put puts a new file, the content coming from the
  * stream of the PUT. DELETE deleted the file. HEAD gets the headers that would
  * come from a full GET.
  * </p>
  * <p>
  * The content type and content encoding headers are honored for GET,HEAD and
  * PUT, but other headers are not honored completely at the moment (range-*)
  * etc,
  * </p>
  * <p>
  * POST takes multipart uploads of content, the URL pointing to a folder and
  * each upload being the name of the file being uploaded to that folder. The
  * upload uses a streaming api, and expects that form fields are ordered, such
  * that a field starting with mimetype before the upload stream will specify the
  * mimetype associated with the stream.
  * </p>
  * 
  * @author ieb
  */
 public abstract class CHSHandler implements Handler
 {
 	private static final Log log = LogFactory.getLog(CHSHandler.class);
 
 	/**
 	 * Required for serialization... also to stop eclipse from giving me a
 	 * warning!
 	 */
 	private static final long serialVersionUID = 676743152200357708L;
 
 	private static final String BASE_PATH_INIT = "basepath";
 
 	private static final String DEFAULT_BASE_PATH = "/private/sdata";
 
 	private static final String LAST_MODIFIED = "Last-Modified";
 
 	private static final String BASE_URL_INIT = "baseurl";
 
 	private static final String DEFAULT_BASE_URL = "c";
 
 	private static final Object REAL_UPLOAD_NAME = "realname";
 
 	private String basePath;
 
 	private ContentHostingService contentHostingService;
 
 	private ResourceDefinitionFactory resourceDefinitionFactory;
 
 	private ResourceFunctionFactory resourceFunctionFactory;
 
 	private String baseUrl;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
 	 */
 	public void init(Map<String, String> config)
 	{
 
 		basePath = config.get(BASE_PATH_INIT);
 		if (basePath == null)
 		{
 			this.basePath = DEFAULT_BASE_PATH;
 		}
 
 		baseUrl = config.get(BASE_URL_INIT);
 		if (baseUrl == null)
 		{
 			this.baseUrl = DEFAULT_BASE_URL;
 		}
 
 		contentHostingService = Kernel.contentHostingService();
 		resourceDefinitionFactory = getResourceDefinitionFactory(config);
 		resourceFunctionFactory = getResourceFunctionFactory(config);
 
 	}
 	
 	public void destroy() 
 	{
 		resourceDefinitionFactory.destroy();
 		resourceFunctionFactory.destroy();
 	}
 
 	private ResourceFunctionFactory getResourceFunctionFactory(Map<String, String> config)
 	{
 		return new ResourceFunctionFactoryImpl(config);
 	}
 
 	/**
 	 * Creates a resource definition factory suitable for controlling the
 	 * storage of items
 	 * 
 	 * @param config
 	 * @return
 	 */
 	protected ResourceDefinitionFactory getResourceDefinitionFactory(
 			Map<String, String> config)
 	{
 		return new ResourceDefinitionFactoryImpl(config, baseUrl, basePath);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#doDelete(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public void doDelete(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		try
 		{
 
 			snoopRequest(request);
 
 			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
 			String name = rp.getRepositoryPath();
 			ContentEntity e = getEntity(name);
 			if (e == null)
 			{
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				return;
 			}
 
 			if (e instanceof ContentCollection)
 			{
 				if (log.isDebugEnabled())
 				{
 					log.debug("Deleting Collection " + name);
 				}
 				contentHostingService.removeCollection(e.getId());
 			}
 			else if (e instanceof ContentResource)
 			{
 				if (log.isDebugEnabled())
 				{
 					log.debug("Deleting File " + name);
 				}
 
 				long lastModifiedTime = -10;
 				ContentResource cr = (ContentResource) e;
 
 				Date lastModified = getLastModified(cr);
 				lastModifiedTime = lastModified.getTime();
 
 				if (!checkPreconditions(request, response, lastModifiedTime, String
 						.valueOf(lastModifiedTime)))
 				{
 					return;
 				}
 				contentHostingService.removeResource(e.getId());
 			}
 			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
 		}
 		catch (Exception e)
 		{
 			sendError(request, response, e);
 
 			snoopRequest(request);
 			log.error("Failed  TO service Request ", e);
 		}
 		finally
 		{
 			request.removeAttribute(Tool.NATIVE_URL);
 		}
 	}
 
 	/**
 	 * Gets an ContentEntity based on a repository path.
 	 * 
 	 * @param repositoryPath
 	 * @return
 	 * @throws PermissionException
 	 * @throws SDataAccessException
 	 */
 	public ContentEntity getEntity(String repositoryPath) throws PermissionException,
 			SDataAccessException
 	{
 		ContentEntity ce = null;
 		try
 		{
 			ce = contentHostingService.getResource(repositoryPath);
 		}
 		catch (IdUnusedException e)
 		{
 			if (log.isDebugEnabled())
 			{
 				log.debug("Resource Not Found " + repositoryPath + " " + e);
 			}
 		}
 		catch (TypeException e)
 		{
 			if (log.isDebugEnabled())
 			{
 				log.debug("Resource Not Found " + repositoryPath + " " + e);
 			}
 		}
 		if (ce == null)
 		{
 			if (!repositoryPath.endsWith("/"))
 			{
 				repositoryPath = repositoryPath + "/";
 			}
 			try
 			{
 				ce = contentHostingService.getCollection(repositoryPath);
 			}
 			catch (IdUnusedException e)
 			{
 				if (log.isDebugEnabled())
 				{
 					log.debug("Collection Not Found " + repositoryPath + " " + e);
 				}
 			}
 			catch (TypeException e)
 			{
 				if (log.isDebugEnabled())
 				{
 					log.debug("Collection Not Found " + repositoryPath + " " + e);
 				}
 			}
 		}
 		if (ce != null)
 		{
 			String lock = ContentHostingService.AUTH_RESOURCE_HIDDEN;
 			boolean canSeeHidden = Kernel.securityService().unlock(lock,
 					ce.getReference());
 			if (!canSeeHidden && !ce.isAvailable())
 			{
 				throw new SDataAccessException(403, "Permission denied on item");
 			}
 
 		}
 
 		return ce;
 	}
 
 	/**
 	 * Extract the last modified date from the ContentResource, if none is present then 
 	 * assume that the last modified date is now.
 	 * 
 	 * @param cr
 	 * @return
 	 */
 	private Date getLastModified(ContentResource cr)
 	{
 		String lastModified = cr.getProperties().getProperty(
 				ResourceProperties.PROP_MODIFIED_DATE);
 		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
 		df.setTimeZone(TimeZone.getTimeZone("GMT"));
 		try
 		{
 			return df.parse(lastModified);
 		}
 		catch (ParseException e)
 		{
 			return new Date();
 		}
 	}
 
 	/**
 	 * Set the last modified date in the COntentResourceEdit to the supplied lastModified
 	 * date.
 	 * 
 	 * @param cre
 	 * @param lastModified
 	 */
 	private void setLastModified(ContentResourceEdit cre, Date lastModified)
 	{
 		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
 		df.setTimeZone(TimeZone.getTimeZone("GMT"));
 		String lastModifiedString = df.format(lastModified);
 		cre.getPropertiesEdit().addProperty(ResourceProperties.PROP_MODIFIED_DATE,
 				lastModifiedString);
 	}
 
 	/**
 	 * If the request parameter "snoop" = 1 then dump the request to the logs
 	 * @param request
 	 */
 	private void snoopRequest(HttpServletRequest request)
 	{
 		boolean snoop = "1".equals(request.getParameter("snoop"));
 		if (snoop)
 		{
 			StringBuilder sb = new StringBuilder("SData Request :");
 			sb.append("\n\tRequest Path :").append(request.getPathInfo());
 			sb.append("\n\tMethod :").append(request.getMethod());
 			for (Enumeration<?> hnames = request.getHeaderNames(); hnames
 					.hasMoreElements();)
 			{
 				String name = (String) hnames.nextElement();
 				sb.append("\n\tHeader :").append(name).append("=[").append(
 						request.getHeader(name)).append("]");
 			}
 			if (request.getCookies() != null)
 			{
 				for (Cookie c : request.getCookies())
 				{
 					sb.append("\n\tCookie:");
 					sb.append("name[").append(c.getName());
 					sb.append("]path[").append(c.getPath());
 					sb.append("]value[").append(c.getValue());
 				}
 			}
 			sb.append("]");
 			log.info(sb.toString());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#doHead(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public void doHead(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		try
 		{
 			snoopRequest(request);
 
 			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
 			ContentEntity e = getEntity(rp.getRepositoryPath());
 			if (e == null)
 			{
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				return;
 			}
 			if (e instanceof ContentResource)
 			{
 				ContentResource cr = (ContentResource) e;
 				response.setContentType(cr.getContentType());
 				response.setDateHeader(LAST_MODIFIED, getLastModified(cr).getTime());
 				// we need to do something about huge files
 				response.setContentLength((int) cr.getContentLength());
 				response.setStatus(HttpServletResponse.SC_OK);
 			}
 			else
 			{
 				response.setDateHeader(LAST_MODIFIED, new Date().getTime());
 				response.setStatus(HttpServletResponse.SC_OK);
 			}
 
 		}
 		catch (Exception e)
 		{
 			sendError(request, response, e);
 
 			snoopRequest(request);
 			log.error("Failed  TO service Request ", e);
 		}
 		finally
 		{
 			request.removeAttribute(Tool.NATIVE_URL);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#doPut(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public void doPut(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		OutputStream out = null;
 		try
 		{
 			snoopRequest(request);
 
 			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
 			ContentEntity e = getEntity(rp.getRepositoryPath());
 			ContentResourceEdit cre = null;
 			boolean created = false;
 			if (e == null)
 			{
 				String s = rp.getRepositoryPath();
 				s = s.substring(0, s.lastIndexOf("/"));
 				getFolder(s);
 				cre = addFile(rp.getRepositoryPath());
 
 				created = true;
 				if (cre == null)
 				{
 					throw new RuntimeException("Failed to create node at "
 							+ rp.getRepositoryPath() + " type ContentResource ");
 				}
 			}
 			else if (e instanceof ContentResource)
 			{
 				cre = contentHostingService.editResource(rp.getRepositoryPath());
 				if (cre == null)
 				{
 					throw new RuntimeException("Failed to create node at "
 							+ rp.getRepositoryPath() + " type ContentResource ");
 				}
 			}
 			else
 			{
 				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 						"Content Can only be put to a file ");
 				return;
 			}
 
 			GregorianCalendar gc = new GregorianCalendar();
 			long lastMod = request.getDateHeader(LAST_MODIFIED);
 			if (lastMod > 0)
 			{
 				gc.setTimeInMillis(lastMod);
 			}
 			else
 			{
 				gc.setTime(new Date());
 			}
 			String name = getName(cre);
 			String mimeType = ContentTypes.getContentType(name, request.getContentType());
 			String charEncoding = null;
 			if (mimeType.startsWith("text"))
 			{
 				charEncoding = request.getCharacterEncoding();
 			}
 
 			InputStream in = request.getInputStream();
 
 			saveStream(cre, in, mimeType, charEncoding, gc);
 
 			in.close();
 			if (created)
 			{
 				response.setStatus(HttpServletResponse.SC_CREATED);
 			}
 			else
 			{
 				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
 			}
 			if (log.isDebugEnabled())
 			{
 				log.debug("PUT Saved " + request.getContentLength() + " bytes to "
 						+ rp.getRepositoryPath());
 			}
 		}
 		catch (SDataException e)
 		{
 			sendError(request, response, e);
 			log.error("Failed  To service Request " + e.getMessage());
 		}
 		catch (Exception e)
 		{
 			sendError(request, response, e);
 			snoopRequest(request);
 			log.error("Failed  TO service Request ", e);
 		}
 		finally
 		{
 			request.removeAttribute(Tool.NATIVE_URL);
 
 			try
 			{
 				out.close();
 			}
 			catch (Exception ex)
 			{
 			}
 		}
 	}
 
 	/**
 	 * @param cre
 	 * @return
 	 */
 	protected String getName(ContentResourceEdit cre)
 	{
 		String id = cre.getId();
 		if (id == null) return null;
 		if (id.length() == 0) return null;
 
 		// take after the last resource path separator, not counting one at the
 		// very end if there
 		boolean lastIsSeparator = id.charAt(id.length() - 1) == '/';
 		return id.substring(id.lastIndexOf('/', id.length() - 2) + 1,
 				(lastIsSeparator ? id.length() - 1 : id.length()));
 	}
 
 	/**
 	 * Add a file to CHS, based on the supplied repository path, returning the uncommitted 
 	 * edit object
 	 * 
 	 * @param repositoryPath the path into the repository
 	 * @return an uncommitted edit object
 	 * @throws ServerOverloadException if the server has too many request
 	 * @throws InconsistentException 
 	 * @throws IdInvalidException If the id is invalid (repository path)
 	 * @throws IdUsedException If the file already exists
 	 * @throws PermissionException if permission was denied.
 	 */
 	private ContentResourceEdit addFile(String repositoryPath)
 			throws PermissionException, IdUsedException, IdInvalidException,
 			InconsistentException, ServerOverloadException
 	{
 		String name = repositoryPath.substring(repositoryPath.lastIndexOf("/") + 1);
 		ContentResourceEdit cre = contentHostingService.addResource(repositoryPath);
 		cre.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME,
 				Validator.escapeResourceName(name));
 		return cre;
 	}
 
 	/**
 	 * Get a folder
 	 * 
 	 * @param s the repository path, with or without trailing / (added if no present)
 	 */
 	protected ContentCollection getFolder(String path)
 	{
 		if (path != null && !path.endsWith("/"))
 		{
 			path = path + "/";
 		}
 		ContentCollection cc = null;
 		try
 		{
 			cc = contentHostingService.getCollection(path);
 		}
 		catch (IdUnusedException ex)
 		{
 		}
 		catch (PermissionException inc)
 		{
 			log
 					.warn(
 							"Failed Creating folder " + path + " cause:"
 									+ inc.getMessage(), inc);
 
 			return null;
 		}
 		catch (TypeException e)
 		{
 			log.warn("Failed Creating folder " + path + " cause:" + e.getMessage(), e);
 
 			return null;
 		}
 		if (cc == null)
 		{
 			try
 			{
 				if (log.isDebugEnabled())
 				{
 					log.debug("Failed to get " + path);
 				}
 				char[] p = path.toCharArray();
 				for (int i = p.length - 1; i > 0; i--)
 				{
 					if (p[i] == '/')
 					{
 
 						String folderName = new String(p, 0, i + 1);
 						if (log.isDebugEnabled())
 						{
 							log.debug("Getting Folder " + folderName);
 						}
 						try
 						{
 							cc = contentHostingService.getCollection(folderName);
 						}
 						catch (IdUnusedException idex)
 						{
 
 						}
 						if (cc != null)
 						{
 							int m = i + 1;
 							for (int j = m; j < p.length; j++)
 							{
 								if (p[j] == '/')
 								{
 									String collectionPath = new String(p, 0, j + 1);
 									String collectionName = new String(p, m, j - m);
 									ContentCollectionEdit cce = contentHostingService
 											.addCollection(collectionPath);
 									cce.getPropertiesEdit().addProperty(
 											ResourceProperties.PROP_DISPLAY_NAME,
 											Validator.escapeResourceName(collectionName));
 									contentHostingService.commitCollection(cce);
 									cc = contentHostingService
 											.getCollection(collectionPath);
 									if (cc != null)
 									{
 										if (log.isDebugEnabled())
 										{
 											log.debug("Created " + collectionPath
 													+ " with name " + collectionName);
 										}
 									}
 									else
 									{
 										log.warn("Failed " + collectionPath
 												+ " with name " + collectionName);
 										return null;
 									}
 
 								}
 							}
 							return cc;
 						}
 					}
 				}
 				return cc;
 			}
 			catch (Exception inc)
 			{
 				log.warn("Failed Creating folder " + path + " cause:" + inc.getMessage(),
 						inc);
 
 				return null;
 			}
 		}
 		return cc;
 	}
 
 	/**
 	 * Save a stream of data to the underlying store
 	 * 
 	 * @param cre The content resource edit object
 	 * @param in the input stream
 	 * @param mimeType the mime type of the stream
 	 * @param charEncoding the expected modification date
 	 * @param lastModified the date on which the stream was last modified.
 	 * @throws ServerOverloadException If the server was overloaded
 	 * @throws OverQuotaException If the user has no more file storage quota
 	 */
 	private long saveStream(ContentResourceEdit cre, InputStream in, String mimeType,
 			String charEncoding, Calendar lastModified) throws OverQuotaException,
 			ServerOverloadException
 	{
 
 		cre.setContentType(mimeType);
 		setLastModified(cre, lastModified.getTime());
 		cre.setContent(in);
 		contentHostingService.commitResource(cre);
 
 		return cre.getContentLength();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#doGet(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public void doGet(final HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		OutputStream out = null;
 		InputStream in = null;
 		try
 		{
 			snoopRequest(request);
 
 
 			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
 			ContentEntity e = getEntity(rp.getRepositoryPath());
 			if (e == null)
 			{
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				return;
 			}
 			SDataFunction m = resourceFunctionFactory.getFunction(rp
 					.getFunctionDefinition());
 			if (m != null)
 			{
 				m.call(this, request, response, e, rp);
 			}
 			else
 			{
 
 				if (e instanceof ContentResource)
 				{
 					ContentResource cr = (ContentResource) e;
 					Date lastModified = getLastModified(cr);
 
 					response.setContentType(cr.getContentType());
 					response.setDateHeader(LAST_MODIFIED, lastModified.getTime());
 					setGetCacheControl(response, rp.isPrivate());
 
 					String currentEtag = String.valueOf(lastModified.getTime());
 					response.setHeader("ETag", currentEtag);
 
 					long lastModifiedTime = lastModified.getTime();
 
 					if (!checkPreconditions(request, response, lastModifiedTime,
 							currentEtag))
 					{
 						return;
 					}
 					long totallength = cr.getContentLength();
 					long[] ranges = new long[2];
 					ranges[0] = 0;
 					ranges[1] = totallength;
 					if (!checkRanges(request, response, lastModifiedTime, currentEtag,
 							ranges))
 					{
 						return;
 					}
 
 					long length = ranges[1] - ranges[0];
 
 					if (totallength != length)
 					{
 						response.setHeader("Accept-Ranges", "bytes");
 						response.setDateHeader("Last-Modified", lastModifiedTime);
 						response.setHeader("Content-Range", "bytes " + ranges[0] + "-"
 								+ (ranges[1] - 1) + "/" + totallength);
 						response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
 
 						log.info("Partial Content Sent "
 								+ HttpServletResponse.SC_PARTIAL_CONTENT);
 					}
 					else
 					{
 						response.setStatus(HttpServletResponse.SC_OK);
 					}
 
 					response.setContentLength((int) length);
 					if (length > 0)
 					{
 
 						out = response.getOutputStream();
 
 						in = cr.streamContent();
 						if (in == null)
 						{
 							log.warn("Failed to get Input Stream from content Resource ["
 									+ in + "] ContentResource[" + cr + "] ID["
 									+ cr.getId() + "]");
 							response.sendError(
 									HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 									"Failed to get Input Stream from content Resource ["
 											+ in + "] ContentResource[" + cr + "] ID["
 											+ cr.getId() + "]");
 							return;
 						}
 						in.skip(ranges[0]);
 						byte[] b = new byte[10240];
 						int nbytes = 0;
 						while ((nbytes = in.read(b)) > 0 && length > 0)
 						{
 							if (nbytes < length)
 							{
 								out.write(b, 0, nbytes);
 								length = length - nbytes;
 							}
 							else
 							{
 								out.write(b, 0, (int) length);
 								length = 0;
 							}
 						}
 					}
 				}
 				else if (e instanceof ContentCollection)
 				{
 					setGetCacheControl(response, rp.isPrivate());
 
 					CHSNodeMap outputMap = new CHSNodeMap(e, rp.getDepth(), rp);
 
 					sendMap(request, response, outputMap);
 				} else {
 					throw new SDataException(HttpServletResponse.SC_NOT_FOUND,"Resource Not found ");
 
 				}
 			}
 
 		}
 		catch (SDataException e)
 		{
 			log.error("Failed  To service Request " + e.getMessage());
 			sendError(request, response, e);
 		}
 		catch (Exception e)
 		{
 			log.error("Failed  TO service Request ", e);
 			sendError(request, response, e);
 			snoopRequest(request);
 		}
 		finally
 		{
 			request.removeAttribute(Tool.NATIVE_URL);
 
 			try
 			{
 				out.close();
 			}
 			catch (Exception ex)
 			{
 			}
 			try
 			{
 				in.close();
 			}
 			catch (Exception ex)
 			{
 			}
 		}
 	}
 
 	/**
 	 * Check the HTTP headers to see if they valid relative to the spec, if valid fill the 
 	 * ranges array with start and end byte offsets
 	 * 
 	 * @param request The http request object
 	 * @param response the http response object
 	 * @param lastModifiedTime the time the target entity was last modified (in the server) 
 	 * @param currentEtag the etag for the target entity. (in the server)
 	 * @param ranges an array of range offsets into the stream, filled with the current start and end, modified to contain the requested start and end
 	 * @return true if the response is ok
 	 * @throws IOException
 	 */
 	private boolean checkRanges(HttpServletRequest request, HttpServletResponse response,
 			long lastModifiedTime, String currentEtag, long[] ranges) throws IOException
 	{
 
 		long[] finalRanges = new long[2];
 		finalRanges[0] = ranges[0];
 		finalRanges[1] = ranges[1];
 		String range = request.getHeader("range");
 		long ifRangeDate = request.getDateHeader("if-range");
 		String ifRangeEtag = request.getHeader("if-range");
 
 		if (ifRangeDate != -1 && lastModifiedTime > ifRangeDate)
 		{
 			// the entity has been modified, ignore and send the whole lot
 			return true;
 		}
 		if (ifRangeEtag != null && !currentEtag.equals(ifRangeEtag))
 		{
 			// the entity has been modified, ignore and send the whole lot
 			return true;
 		}
 
 		if (range != null)
 		{
 			String[] s = range.split("=");
 			if (!"bytes".equals(s[0]))
 			{
 				response
 						.sendError(416,
 								"System only supports single range responses, specified in bytes");
 				return false;
 			}
 			range = s[1];
 			String[] r = range.split(",");
 			if (r.length > 1)
 			{
 				response.sendError(416, "System only supports single range responses");
 				return false;
 			}
 			range = range.trim();
 			if (range.startsWith("-"))
 			{
 				ranges[1] = Long.parseLong(range.substring(1));
 			}
 			else if (range.endsWith("-"))
 			{
 				ranges[0] = Long.parseLong(range.substring(0, range.length() - 1));
 			}
 			else
 			{
 				r = range.split("-");
 				ranges[0] = Long.parseLong(r[0]);
 				ranges[1] = Long.parseLong(r[1]);
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Evaluate pre-conditions, based on the request, as per the http rfc.
 	 * 
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws IOException
 	 */
 	private boolean checkPreconditions(HttpServletRequest request,
 			HttpServletResponse response, long lastModifiedTime, String currentEtag)
 			throws IOException
 	{
 		lastModifiedTime = lastModifiedTime - (lastModifiedTime % 1000);
 		long ifUnmodifiedSince = request.getDateHeader("if-unmodified-since");
 		if (ifUnmodifiedSince > 0 && (lastModifiedTime >= ifUnmodifiedSince))
 		{
 			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
 			return false;
 		}
 
 		String ifMatch = request.getHeader("if-match");
 		if (ifMatch != null && ifMatch.indexOf(currentEtag) < 0)
 		{
 			// ifMatch was present, but the currentEtag didnt match
 			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
 			return false;
 		}
 		String ifNoneMatch = request.getHeader("if-none-match");
 		if (ifNoneMatch != null && ifNoneMatch.indexOf(currentEtag) >= 0)
 		{
 			if ("GET|HEAD".indexOf(request.getMethod()) >= 0)
 			{
 				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
 
 			}
 			else
 			{
 				// ifMatch was present, but the currentEtag didnt match
 				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
 			}
 			return false;
 		}
 		long ifModifiedSince = request.getDateHeader("if-modified-since");
 		if ((ifModifiedSince > 0) && (lastModifiedTime <= ifModifiedSince))
 		{
 			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Set the cache control headers in the response to values that will work for all versions
 	 * of HTTP
 	 * 
 	 * @param response
 	 */
 	private void setGetCacheControl(HttpServletResponse response, boolean isprivate)
 	{
 		if (isprivate)
 		{
 			response.addHeader("Cache-Control", "must-revalidate");
 			response.addHeader("Cache-Control", "private");
 			response.addHeader("Cache-Control", "no-store");
 		}
 		else
 		{
 			// response.addHeader("Cache-Control", "must-revalidate");
 			response.addHeader("Cache-Control", "public");
 		}
 		response.addHeader("Cache-Control", "max-age=600");
 		response.addHeader("Cache-Control", "s-maxage=600");
 		response.setDateHeader("Date", System.currentTimeMillis());
 		response.setDateHeader("Expires", System.currentTimeMillis() + 600000);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		snoopRequest(request);
 		try
 		{
 			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
 			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 
 			// multiparts are always streamed uploads
 			if (isMultipart)
 			{
 				doMumtipartUpload(request, response, rp);
 			}
 			else
 			{
 
 				ContentEntity ce = getEntity(rp.getRepositoryPath());
 				SDataFunction m = resourceFunctionFactory.getFunction(rp
 						.getFunctionDefinition());
 				if (m != null)
 				{
 					m.call(this, request, response, ce, rp);
 				}
 				else
 				{
 					log.info("NOP Post performed");
 					throw new SDataException(HttpServletResponse.SC_NOT_FOUND,"Method not found ");
 				}
 
 			}
 		}
 		catch (SDataException sde)
 		{
 			sendError(request, response, sde);
 
 		}
 		catch (Exception ex)
 		{
 			sendError(request, response, ex);
 		}
 
 	}
 
 	/**
 	 * Process a multipart upload, that may contain one or more parts each tagetted at a different file
 	 * 
 	 * @param request THe request object
 	 * @param response the response object
 	 * @param rp THe resource definition from which the location in the repository will be derived
 	 * @throws ServletException 
 	 * @throws IOException
 	 */
 	private void doMumtipartUpload(HttpServletRequest request,
 			HttpServletResponse response, ResourceDefinition rp) throws ServletException,
 			IOException
 	{
 		try
 		{
 			try
 			{
 
 				ContentCollection e = getFolder(rp.getRepositoryPath());
 				if (e == null)
 				{
 					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 							"Unable to uplaod to location " + rp.getRepositoryPath());
 					return;
 				}
 			}
 			catch (Exception ex)
 			{
 				sendError(request, response, ex);
 				snoopRequest(request);
 				log.error("Failed  TO service Request ", ex);
 				return;
 			}
 
 			// Check that we have a file upload request
 
 			// Create a new file upload handler
 			ServletFileUpload upload = new ServletFileUpload();
 			List<String> errors = new ArrayList<String>();
 
 
 			// Parse the request
 			FileItemIterator iter = upload.getItemIterator(request);
 			Map<String, Object> responseMap = new HashMap<String, Object>();
 			Map<String, Object> uploads = new HashMap<String, Object>();
 			Map<String, List<String>> values = new HashMap<String, List<String>>();
 			int uploadNumber = 0;
 			while (iter.hasNext())
 			{
 				FileItemStream item = iter.next();
 				log.debug("Got Upload through Uploads");
 				String name = item.getName();
 				String fieldName = item.getFieldName();
 				if (log.isDebugEnabled())
 				{
 					log.debug("    Name is " + name + " field Name " + fieldName);
 					for (String headerName : item.getHeaderNames())
 					{
 						log.debug("Header " + headerName + " is "
 								+ item.getHeader(headerName));
 					}
 				}
 				InputStream stream = item.openStream();
 				if (!item.isFormField())
 				{
 					try
 					{
 						if (name != null && name.trim().length() > 0)
 						{
 							
 							List<String> realNames = values.get(REAL_UPLOAD_NAME);
 							String finalName = name;
 							if ( realNames != null && realNames.size() > uploadNumber ) {
 								finalName = realNames.get(uploadNumber);
 							}
 							
 							String mimeType = ContentTypes.getContentType(finalName, item
 									.getContentType());
 							String resourceName = rp.getRepositoryPath() + "/" + finalName;
 							ContentEntity ce = getEntity(resourceName);
 							ContentResourceEdit target = null;
 							if (ce != null)
 							{
 								target = contentHostingService.editResource(resourceName);
 							}
 							if (target == null)
 							{
 								target = addFile(resourceName);
 
 							}
 							GregorianCalendar lastModified = new GregorianCalendar();
 							lastModified.setTime(new Date());
 							long size = saveStream(target, stream, mimeType, "UTF-8",
 									lastModified);
 							Map<String, Object> uploadMap = new HashMap<String, Object>();
 							if (size > Integer.MAX_VALUE)
 							{
 								uploadMap.put("contentLength", String.valueOf(size));
 							}
 							else
 							{
 								uploadMap.put("contentLength", (int) size);
 							}
 							uploadMap.put("name", finalName);
 							uploadMap.put("url", rp.getExternalPath(rp
 									.getRepositoryPath(finalName)));
 							uploadMap.put("mimeType", mimeType);
 							uploadMap.put("lastModified", lastModified.getTime());
 							uploadMap.put("status", "ok");
 
 							uploads.put(fieldName, uploadMap);
 							uploadMap = new HashMap<String, Object>();
 							uploadNumber++;
 						}
 					}
 					catch (Exception ex)
 					{
 						log.error("Failed to Upload Content", ex);
 						Map<String, Object> uploadMap = new HashMap<String, Object>();
 						uploadMap.put("mimeType", "text/plain");
 						uploadMap.put("encoding", "UTF-8");
 						uploadMap.put("contentLength", -1);
 						uploadMap.put("lastModified", 0);
 						uploadMap.put("status", "Failed");
 						uploadMap.put("cause", ex.getMessage());
 						List<String> stackTrace = new ArrayList<String>();
 						for (StackTraceElement ste : ex.getStackTrace())
 						{
 							stackTrace.add(ste.toString());
 						}
 						uploadMap.put("stacktrace", stackTrace);
 						uploads.put(fieldName, uploadMap);
 						uploadMap = new HashMap<String, Object>();
 
 					}
 
 				} else {
 					String value = Streams.asString(stream);
					List<String> valueList = values.get(fieldName);
 					if ( valueList == null ) {
 						valueList = new ArrayList<String>();
						values.put(fieldName,valueList);
 						
 					}
 					valueList.add(value);
 				}
 			}
 			responseMap.put("success", true);
 			responseMap.put("errors", errors.toArray(new String[1]));
 			responseMap.put("uploads", uploads);
 			sendMap(request, response, responseMap);
 			log.info("Response Complete Saved to " + rp.getRepositoryPath());
 		}
 		catch (Throwable ex)
 		{
 			log.error("Failed  TO service Request ", ex);
 			sendError(request, response, ex);
 			return;
 		}
 	}
 
 	/**
 	 * The base path of this handler
 	 * 
 	 * @return the basePath
 	 */
 	public String getBasePath()
 	{
 		return basePath;
 	}
 
 	/**
 	 * The base path of this handler
 	 * 
 	 * @param basePath
 	 *        the basePath to set
 	 */
 	public void setBasePath(String basePath)
 	{
 		this.basePath = basePath;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.Handler#setHandlerHeaders(javax.servlet.http.HttpServletResponse)
 	 */
 	public void setHandlerHeaders(HttpServletRequest request, HttpServletResponse response)
 	{
 		response.setHeader("x-sdata-handler", this.getClass().getName());
 		response.setHeader("x-sdata-url", request.getPathInfo());
 	}
 }
