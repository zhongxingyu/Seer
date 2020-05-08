 package uk.ac.ox.oucs.oxam.logic;
 
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.TypeException;
 
 import uk.ac.ox.oucs.oxam.utils.Utils;
 
 /**
  * This is a Sakai implementation for locating uploaded paper files. 
  * @author buckett
  *
  */
 public class SakaiLocation implements Location {
 	
 	private ServerConfigurationService serverConfigurationService;
 	private ContentHostingService contentHostingService;
 	private String sitePath;
 	private String prefix;
 	
 	public void setServerConfigurationService(
 			ServerConfigurationService serverConfigurationService) {
 		this.serverConfigurationService = serverConfigurationService;
 	}
 
 
 	public void setContentHostingService(ContentHostingService contentHostingService) {
 		this.contentHostingService = contentHostingService;
 	}
 
 
 	public void init() {
 		String siteId = serverConfigurationService.getString(SakaiValueSource.OXAM_CONTENT_SITE_ID);
 		if (siteId == null) {
 			// Oh poo.
 		}
 		sitePath = contentHostingService.getSiteCollection(siteId);
 	}
 
 	public String getPrefix() {
 		if (prefix == null) {
 			try {
				// This is so that we can have a relative path, without the hostname.
				String siteUrl = contentHostingService.getCollection(sitePath).getUrl(true);
				String prefix = serverConfigurationService.getString("accessPath", "/access");
				this.prefix = Utils.joinPaths("/", prefix, siteUrl);
 				// Ignore all the exceptions.
 			} catch (PermissionException e) {
 				//
 			} catch (IdUnusedException e) {
 				//
 			} catch (TypeException e) {
 				//
 			}
 		}
 		return prefix;
 	}
 
 	public String getPath(String path) {
 		String fullPath = Utils.joinPaths("/", sitePath, path);
 		return fullPath;
 	}
 
 }
