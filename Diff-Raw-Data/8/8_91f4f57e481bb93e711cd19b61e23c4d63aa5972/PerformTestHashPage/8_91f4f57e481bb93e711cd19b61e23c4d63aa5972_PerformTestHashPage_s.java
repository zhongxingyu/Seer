 package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.MessageDigest;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 
 import javax.servlet.ServletException;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 
 import uk.ac.warwick.dcs.boss.frontend.Page;
 import uk.ac.warwick.dcs.boss.frontend.PageContext;
 import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
 import uk.ac.warwick.dcs.boss.model.FactoryException;
 import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
 import uk.ac.warwick.dcs.boss.model.dao.DAOException;
 import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
 import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
 import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
 import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
 import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
 
 public class PerformTestHashPage extends Page {
 
 	public PerformTestHashPage() throws PageLoadException {
 		super("staff_hashed", AccessLevel.USER);
 	}
 
 	public void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException,
 			IOException {
 		throw new ServletException("Unexpected GET");
 	}
 
 	protected void handlePost(PageContext pageContext, Template template,
 			VelocityContext templateContext) throws ServletException,
 			IOException {
 		IDAOSession f;
 		String submissionSalt = null;
 		try {
 			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
 			f = df.getInstance();
 			submissionSalt = df.getSubmissionHashSalt();
 		} catch (FactoryException e) {
 			throw new ServletException("dao init error", e);
 		}
 
 		Assignment assignment = null;
 		
 		// Get the assignment
 		String assignmentString = pageContext.getParameter("assignment");
 		if (assignmentString == null) {
 			throw new ServletException("No assignment parameter given");
 		}
 		Long assignmentId = Long.valueOf(pageContext.getParameter("assignment"));
 		
 		Collection<String> fileNames = null;
 		
 		// Render page
 		try {
 			f.beginTransaction();
 			
 			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
 			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
 			assignment = assignmentDao.retrievePersistentEntity(assignmentId);
 			
 			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
 				f.abortTransaction();
 				throw new DAOException("permission denied (not on module)");
 			}
 			
 			templateContext.put("assignment", assignment);
 			fileNames = assignmentDao.fetchRequiredFilenames(assignmentId);
 			
 			f.endTransaction();
 		} catch (DAOException e) {
 			f.abortTransaction();
 			throw new ServletException("dao exception", e);
 		}
 		
 		// Handle digest form
 		String securityCode = null;
 		MessageDigest digest = null;
 		HashSet<String> remainingFiles = new HashSet<String>(fileNames);
 		
 		// Digest the upload
 		try {
 			digest = MessageDigest.getInstance("MD5");
 			
 			FileItemIterator fileIterator = pageContext.getUploadedFiles();
 			
 			while (fileIterator.hasNext()) {
 				FileItemStream currentUpload = fileIterator.next();
 
 				if (fileNames.contains(currentUpload.getFieldName())) { 
 					InputStream is = currentUpload.openStream();
 					
 					try {
 						byte buffer[] = new byte[1024];
 						int nread = -1;
 						long total = 0;
 						while ((nread = is.read(buffer)) != -1) {
 							total += nread;
 							digest.update(buffer, 0, nread);
 						}
 						if (total > 0) {
 							remainingFiles.remove(currentUpload.getFieldName());
 						}
 					} catch (IOException e) {
 						throw new DAOException("IO error returning file stream", e);
 					}
 					
 				}			
 			}
 			
 			if (remainingFiles.equals(fileNames)) {
 				pageContext.performRedirect(pageContext.getPageUrl("staff", "test_hash") + "?assignment=" + assignmentId + "&missing=true");
 				return;
 			}
 			
 			// Finalize the resource.
 			securityCode = byteArrayToHexString(digest.digest());
 			
 		} catch (Exception e) {
 			throw new ServletException("error hashing upload", e);
 		}
 
 		// Salt security code, SHA256 it.
 		securityCode = securityCode + submissionSalt;
 		
 		try {
 			digest = MessageDigest.getInstance("SHA-256");
 			securityCode = byteArrayToHexString(digest.digest(securityCode.getBytes("UTF-8")));
 		} catch (Exception e) {
 			throw new ServletException("error salt-hashing upload", e);
 		}
 
 		
 		// Well, that seemed to be successful(!)
 		templateContext.put("hash", securityCode);
 		templateContext.put("now", new Date());		
 		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
 		pageContext.renderTemplate(template, templateContext);
 	}
 	
 	/**
 	 * Convert a byte[] array to readable string format. This makes the "hex" readable!
 	 * OBTAINED FROM: http://www.devx.com/tips/Tip/13540
 	 * TODO: Consolidate in some sort of library.
 	 * @return result String buffer in String format 
 	 * @param in byte[] buffer to convert to string format
 	 */
 	static String byteArrayToHexString(byte in[]) {
 		byte ch = 0x00;
 		int i = 0; 
 
 		if (in == null || in.length <= 0) {
 			return null;
 		}
 
 		String pseudo[] = {
 				"0", "1", "2",
 				"3", "4", "5", "6", "7", "8",
 				"9", "A", "B", "C", "D", "E",
 				"F"
 		};
 
 		StringBuffer out = new StringBuffer(in.length * 2);
 
 		while (i < in.length) {
 			ch = (byte) (in[i] & 0xF0);		// Strip off high nibble
 			ch = (byte) (ch >>> 4);			// shift the bits down
 			ch = (byte) (ch & 0x0F);		// must do this if high order bit is on!
 			out.append(pseudo[(int) ch]);	// convert the nibble to a String Character
 
 			ch = (byte) (in[i] & 0x0F);		// Strip off low nibble 
 			out.append(pseudo[ (int) ch]);	// convert the nibble to a String Character
 
 			i++;
 		}
 
 		String rslt = new String(out);
 		return rslt;
 	}	
 		
 }
