 package com.marakana.filez.web;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipOutputStream;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NameClassPair;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.catalina.Globals;
 import org.apache.catalina.servlets.WebdavServlet;
 import org.apache.catalina.util.RequestUtil;
 import org.apache.naming.resources.CacheEntry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.marakana.filez.service.Params;
 
 public class FilezServlet extends WebdavServlet {
 	private static final Logger logger = LoggerFactory
 			.getLogger(FilezServlet.class);
 	private static final long serialVersionUID = 1L;
 	private static final Pattern ZIP_FILE_NAME_PATTERN = Pattern
 			.compile("^(.+/)([^/]+)/?$");
 
 	private Pattern baseUriPattern;
 
 	@Override
 	public void init() throws ServletException {
 		super.init();
 		try {
 			Context ctx = new InitialContext();
 			try {
 				Params params = WebUtil.asParams((Context) ctx
 						.lookup("java:comp/env/"));
 				String n = FilezServlet.class.getName();
 				this.baseUriPattern = Pattern.compile(params.getString(n
 						+ ".baseUriPattern"));
 				logger.info("Initialized with baseDirPattern=["
 						+ baseUriPattern.pattern() + "]");
 			} finally {
 				ctx.close();
 			}
 		} catch (Exception e) {
 			throw new ServletException("Failed to init", e);
 		}
 	}
 
 	protected void service(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String path = getRelativePath(req);
 		Matcher baseUriMatcher = this.baseUriPattern.matcher(path);
 		if (baseUriMatcher.matches()) {
 			String remainder = baseUriMatcher.group(2);
 			if (remainder == null || remainder.isEmpty()) {
 				File file = new File(super.getServletContext()
 						.getRealPath(path));
 				if (file.exists()) {
 					if (logger.isTraceEnabled()) {
 						logger.trace("Directory root already exists: "
 								+ file.getAbsolutePath());
 					}
 				} else {
 					if (file.mkdirs()) {
 						if (logger.isDebugEnabled()) {
 							logger.debug("Created directory root: "
 									+ file.getAbsolutePath());
 						}
 					} else {
 						if (logger.isErrorEnabled()) {
 							logger.error("Failed to create directory root: "
 									+ file.getAbsolutePath());
 						}
 						resp.sendError(
 								HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 								"Failed to initialize the requested path");
 						return;
 					}
 				}
 			} else {
 				// not a base-uri request; ignoring
 			}
 			if ("GET".equals(req.getMethod())
 					&& "zip".equals(req.getParameter("as"))) {
 				this.doZipDownload(req, resp);
 			} else {
 				super.service(req, resp);
 			}
 		} else {
 			// TODO: this should really go to a filter
 			if (logger.isErrorEnabled()) {
 				logger.error("Path [" + path
 						+ "] does not match baseUriPattern=["
 						+ this.baseUriPattern.pattern() + "]");
 			}
 			resp.sendError(HttpServletResponse.SC_FORBIDDEN,
 					"Unauthorized path: " + path);
 			return;
 		}
 	}
 
 	private CacheEntry getCacheEntry(String path, HttpServletRequest req,
 			HttpServletResponse resp) throws IOException {
 		CacheEntry cacheEntry = resources.lookupCache(path);
 
 		if (!cacheEntry.exists) {
 			// Check if we're included so we can return the appropriate
 			// missing resource name in the error
 			String requestUri = (String) req
 					.getAttribute(Globals.INCLUDE_REQUEST_URI_ATTR);
 			if (requestUri == null) {
 				requestUri = req.getRequestURI();
 			} else {
 				// We're included, and the response.sendError() below is going
 				// to be ignored by the resource that is including us.
 				// Therefore, the only way we can let the including resource
 				// know is by including warning message in response
 				resp.getWriter().write(
 						RequestUtil.filter(sm.getString(
 								"defaultServlet.missingResource", requestUri)));
 			}
 
 			resp.sendError(HttpServletResponse.SC_NOT_FOUND, requestUri);
 			return null;
 		}
 
 		// If the resource is not a collection, and the resource path
 		// ends with "/" or "\", return NOT FOUND
 		if (cacheEntry.context == null) {
 			if (path.endsWith("/") || (path.endsWith("\\"))) {
 				// Check if we're included so we can return the appropriate
 				// missing resource name in the error
 				String requestUri = (String) req
 						.getAttribute(Globals.INCLUDE_REQUEST_URI_ATTR);
 				if (requestUri == null) {
 					requestUri = req.getRequestURI();
 				}
 				resp.sendError(HttpServletResponse.SC_NOT_FOUND, requestUri);
 				return null;
 			}
 		}
 
 		return cacheEntry;
 	}
 
 	private void doZipDownload(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException, ServletException {
 		final String path = getRelativePath(req);
 		final CacheEntry cacheEntry = this.getCacheEntry(path, req, resp);
 		if (cacheEntry == null) {
 			return;
 		} else {
 			if (logger.isDebugEnabled()) {
 				logger.debug("Handling zip download of " + cacheEntry.name);
 			}
 			Matcher matcher = ZIP_FILE_NAME_PATTERN.matcher(path);
 			if (matcher.matches()) {
 				final int nameOffset = matcher.group(1).length();
 				final String filename = matcher.group(2) + ".zip";
 				resp.setContentType("application/zip");
 				resp.setHeader("Content-Disposition",
 						String.format("attachment; filename=\"%s\"", filename));
 				final ZipOutputStream zos = new ZipOutputStream(
 						resp.getOutputStream());
 				try {
 					int count = zip(cacheEntry, nameOffset, zos);
 					if (count > 0) {
 						if (logger.isDebugEnabled()) {
 							logger.debug("Zipped up " + count + " file(s) to "
 									+ filename);
 						}
 					} else {
 						ZipEntry entry = new ZipEntry("README.txt");
 						entry.setTime(System.currentTimeMillis());
 						zos.putNextEntry(entry);
 						zos.write("There are no other files in this ZIP archive\n"
 								.getBytes());
 					}
 				} finally {
 					zos.close();
 				}
 			} else {
 				if (logger.isWarnEnabled()) {
 					logger.warn("Cannot zip invalid path: [" + path + "]");
 				}
 				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
 			}
 		}
 	}
 
 	private int zip(CacheEntry cacheEntry, int nameOffset, ZipOutputStream zos)
 			throws ZipException, IOException, ServletException {
 		if (!cacheEntry.exists) {
 			if (logger.isWarnEnabled()) {
 				logger.warn("Not zipping non-existant entry: "
 						+ cacheEntry.name);
 			}
 			return 0;
 		} else if ("WEB-INF".equals(cacheEntry.name)
 				|| "META-INF".equals(cacheEntry.name)) {
 			if (logger.isWarnEnabled()) {
 				logger.warn("Not zipping protected entry: " + cacheEntry.name);
 			}
 			return 0;
 		} else if (cacheEntry.context == null) {
 			if (logger.isTraceEnabled()) {
 				logger.trace("Zipping " + cacheEntry.name);
 			}
 			ZipEntry entry = new ZipEntry(cacheEntry.name.substring(nameOffset));
 			entry.setTime(cacheEntry.timestamp);
 			zos.putNextEntry(entry);
 			InputStream in = cacheEntry.resource.streamContent();
 			try {
 				copy(in, zos);
 			} finally {
 				in.close();
 			}
 			return 1;
 		} else {
 			try {
 				int count = 0;
 				for (@SuppressWarnings("unchecked")
 				NamingEnumeration<NameClassPair> enumeration = resources
 						.list(cacheEntry.name); enumeration.hasMoreElements();) {
					count += zip(
							resources.lookupCache(cacheEntry.name + '/'
									+ enumeration.nextElement().getName()),
							nameOffset, zos);
 				}
 				return count;
 			} catch (NamingException e) {
 				// Something went wrong
 				throw new ServletException("Error zipping [" + cacheEntry.name
 						+ "]", e);
 			}
 		}
 	}
 
 	private void copy(InputStream in, OutputStream out) throws IOException {
 		byte[] b = new byte[2048];
 		for (int len = 0; (len = in.read(b)) > 0;) {
 			out.write(b, 0, len);
 		}
 	}
 }
