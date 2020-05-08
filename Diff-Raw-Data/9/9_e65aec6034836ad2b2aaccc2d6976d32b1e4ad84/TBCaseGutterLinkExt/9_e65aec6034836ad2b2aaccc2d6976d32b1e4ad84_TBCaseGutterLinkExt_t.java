 package org.openmrs.module.tbcase.api.extension.html;
 
 import org.openmrs.module.web.extension.LinkExt;
 
 public class TBCaseGutterLinkExt extends LinkExt {
 
 	@Override
 	public String getLabel() {
 		return "TB Case Reports";
 		
 	}
 
 	@Override
 	public String getRequiredPrivilege() {
 		return "View Reports";
 	}
 
 	@Override
 	public String getUrl() {
		return "module/tbcase/admin/reportSelection.list";
 	}
 
 }
