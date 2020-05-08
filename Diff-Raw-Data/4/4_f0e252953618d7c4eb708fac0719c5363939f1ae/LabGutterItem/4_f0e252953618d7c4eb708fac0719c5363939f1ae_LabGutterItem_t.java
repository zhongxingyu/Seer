 package org.openmrs.module.simplelabentry.extension.html;
 
 import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.LinkExt;
 
public class LabGutterItem extends LinkExt {
 
 	@Override
 	public MEDIA_TYPE getMediaType() {
 		return Extension.MEDIA_TYPE.html;
 	}
 
 	public String getLabel() {
 		return "simplelabentry.title";
 	}
 
 	public String getUrl() {
 		return "module/simplelabentry/index.htm";
 	}
 
 	/**
 	 * Returns the required privilege in order to see this section. Can be a
 	 * comma delimited list of privileges. If the default empty string is
 	 * returned, only an authenticated user is required
 	 * 
 	 * @return Privilege string
 	 */
 	public String getRequiredPrivilege() {
 		return "View Orders";
 	}
 
 }
