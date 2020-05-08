 package gov.nih.nci.gss.api;
 
 import gov.nih.nci.gss.domain.HostingCenter;
 import gov.nih.nci.gss.util.GSSProperties;
 import gov.nih.nci.system.dao.orm.ORMDAOImpl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hibernate.classic.Session;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 /**
  * Servlet which serves images from the file system.
  * 
  * @author <a href="mailto:rokickik@mail.nih.gov">Konrad Rokicki</a>
  */
 public class ImageService extends HttpServlet {
 
 	private static final String fileExtension = ".png";
 	
 	private static final String fileSeparator = System.getProperty("file.separator");
 	
     private static Logger log = Logger.getLogger(ImageService.class);
     
     /** Hibernate session factory */
     private SessionFactory sessionFactory;
     
     @Override
     public void init() throws ServletException {
         
         try {
             WebApplicationContext ctx =  
                 WebApplicationContextUtils.getWebApplicationContext(getServletContext());
             this.sessionFactory = ((ORMDAOImpl)ctx.getBean("ORMDAO")).getHibernateTemplate().getSessionFactory();
         }
         catch (Exception e) {
             throw new ServletException(e);
         }
     }
     
 	@Override
     public void doGet(HttpServletRequest request, HttpServletResponse response) 
             throws ServletException, IOException {
 
     	String noun = null;
     	String objId = null;
     	
     	try {
     		String[] pathList = request.getPathInfo().split("/");
     		noun = pathList[1];
     		objId = pathList[2];
     	}
     	catch (Exception e) {
     		log.error(e);
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return;
     	}
 		
 		if (noun.equals("host")) {
 			
 			String imageName = null;
 			
 			if (objId.matches("^\\d+$")) {
 				Session s = null;
 				try {
 					s = sessionFactory.openSession();
 
 		            // Create the Hibernate Query
 		            StringBuffer hql = new StringBuffer(JSONDataService.GET_HOST_HQL_SELECT);
		            hql.append("and host.id = ?");
 		            Query q = s.createQuery(hql.toString());
 		            q.setString(0, objId);
 		            
 		            // Execute the query
 		            List<HostingCenter> hosts = q.list();
 		            
 		            if (hosts.isEmpty()) {
 			            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 		            	return;
 		            }
 					imageName = getHostImageName(hosts.get(0));
 				}
 				finally {
 					if (s != null) s.close();
 				}
 			}
 			else if (objId.matches("^[\\w]+$")) {
 				imageName = objId;
 			}
 			else {
 	    		log.error("Invalid object id: "+objId);
 	            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 				return;
 			}
 
 			writeImage(getHostImageFilePath(imageName), response);
 		}
 		else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 		}
     }
 
 	/**
 	 * Get the prefix of the image file name for the given host.
 	 * @param host
 	 * @return
 	 */
     public static String getHostImageName(HostingCenter host) {
 		return host.getLongName().replaceAll("\\W", " ").
 				replaceAll("\\s+", "_");
     }
     
 
 	/**
 	 * Get the full file path of an image.
 	 * @param host
 	 * @return
 	 */
     public static String getHostImageFilePath(String imageName) {
 		return GSSProperties.getHostImageDir()+fileSeparator+imageName+".png";
     }
     
     /**
      * Read from the given path and write to output. 
      * @param filename
      * @param response
      */
     private void writeImage(String path, HttpServletResponse response) 
     		throws FileNotFoundException {
     	
         // Get the absolute path of the image
         ServletContext sc = getServletContext();
     
         // Get the MIME type of the image
         String mimeType = sc.getMimeType(path);
         if (mimeType == null) {
             sc.log("Could not get MIME type of "+path);
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             return;
         }
     
         // Set content type
         response.setContentType(mimeType);
     
         // Open the file and output streams
         try {
             FileInputStream in = new FileInputStream(path);
             OutputStream out = response.getOutputStream();
 
             // Set content size
             File file = new File(path);
             response.setContentLength((int)file.length());
             
             // Copy the contents of the file to the output stream
             byte[] buf = new byte[1024];
             int count = 0;
             while ((count = in.read(buf)) >= 0) {
                 out.write(buf, 0, count);
             }
             in.close();
             out.close();
         }
         catch (FileNotFoundException e) {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         }
         catch (IOException e) {
         	log.error("Error writing image file",e);
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
     }
     
     @Override
     public void destroy() {
         super.destroy();
     }
 }
