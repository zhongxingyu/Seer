 /**
  * 
  */
 package org.dataportal;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.Map;
 
 import javax.persistence.RollbackException;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.dataportal.model.User;
 import org.dataportal.utils.DataPortalException;
 
 /**
  * Servlet to comunicate with Client to download process
  * 
  * @author Micho Garcia
  * 
  */
 public class DownloadServlet extends HttpServlet implements DataportalCodes {
 
 	private static final long serialVersionUID = 1L;
 
 	private static Logger logger = Logger.getLogger(DownloadServlet.class);
 
 	private static final String CONTENTDISPOSITION = "Content-disposition"; //$NON-NLS-1$
 	private static final String TYPEXML = "application/xml"; //$NON-NLS-1$
 	private static final String TYPEZIP = "application/zip"; //$NON-NLS-1$
 	private static final String UTF8 = "UTF-8"; //$NON-NLS-1$
 
 	private DataPortalError error;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
 	 */
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
 	 * , javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		HttpSession session = req.getSession();
 		String lang = (String) session.getAttribute(LANG);
 		Messages.setLang(lang);
 
 		User user = this.authenticate(req, resp);
 		if (user == null)
 			return;
 
 		@SuppressWarnings("unchecked")
 		Map<String, String[]> params = req.getParameterMap();
 
         if (params.containsKey("file")) { //$NON-NLS-1$ //$NON-NLS-2$
 
 			String fileName = params.get("file")[0]; //$NON-NLS-1$
 			String url = req.getRequestURL().toString();
 
 			DownloadController download = new DownloadController(lang);
             download.setUser(user);
             download.setUrl(url);
 			int fileSize = (int) download.getFileSize(fileName);
 
 			InputStream contents = download.getFileContents(fileName);
 			OutputStream out = resp.getOutputStream();
 
 			resp.setContentType(TYPEZIP);
 			resp.setHeader(CONTENTDISPOSITION, "attachment; filename=" //$NON-NLS-1$
 					+ fileName);
 			resp.setContentLength(fileSize);
 
 			IOUtils.copyLarge(contents, out);
 			out.flush();
 			out.close();
 			contents.close();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
 	 * , javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		HttpSession session = req.getSession();
 		String lang = (String) session.getAttribute(LANG);
 		Messages.setLang(lang);
 
 		User user = this.authenticate(req, resp);
 		if (user == null)
 			return;
 
 		resp.setContentType(TYPEXML);
 		resp.setCharacterEncoding(UTF8);
 		PrintWriter writer2Client = resp.getWriter();
 		InputStream isRequestXML = req.getInputStream();
 
 		try {
 			logger.debug("UserName to download: " + user.getId()); //$NON-NLS-1$
 			
 			String url = req.getRequestURL().toString();
 
 			DownloadController download = new DownloadController(lang);
 			download.setUser(user);
 			download.setUrl(url);
 			String fileName = download.askgn2download(isRequestXML);
 			String id = download.getId();
 			logger.debug("FILE to download: " + fileName); //$NON-NLS-1$
 			writer2Client.println("<download>"); //$NON-NLS-1$
 			writer2Client.println("  <id>" + id + "</id>"); //$NON-NLS-1$ //$NON-NLS-2$
 			writer2Client.println("  <filename>" + fileName + "</filename>"); //$NON-NLS-1$ //$NON-NLS-2$
 			writer2Client.println("</download>"); //$NON-NLS-1$
 		} catch (Exception e) {
 			Class<?> clase = e.getClass();
 			error = new DataPortalError();
 			if (clase.equals(DataPortalException.class)) {
 				DataPortalException dtException = (DataPortalException) e;
 				error.setCode(dtException.getCode());
 			} else if (clase.equals(RollbackException.class)) {
 				error.setCode(RDBMSERROR);
 			} else {
 				error.setCode(RUNTIMEERROR);				
 			}
 			error.setMessage(e.getMessage());
 			writer2Client.write(error.getErrorMessage());
 		} finally {
 			writer2Client.flush();
 			writer2Client.close();
 		}
 	}
 
 	/**
 	 * Test if user is logged
 	 * 
 	 * @param req
 	 * @param resp
 	 * @throws IOException
 	 */
 	private User authenticate(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		HttpSession session = req.getSession();
 		User user = (User) session.getAttribute(USERACCESS);
 		if (user == null)
 			try {
 				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED,
 						Messages.getString("downloadservlet.access_denied")); //$NON-NLS-1$
 				return null;
 			} catch (Exception e) {
 				DataPortalError error = new DataPortalError();
 				error.setCode(ERRORLOGIN);
 				error.setMessage(Messages.getString("downloadservlet.error_auth_user")); //$NON-NLS-1$
 				resp.getWriter().print(error.getErrorMessage());
 			}	
 		return user;
 	}
 }
