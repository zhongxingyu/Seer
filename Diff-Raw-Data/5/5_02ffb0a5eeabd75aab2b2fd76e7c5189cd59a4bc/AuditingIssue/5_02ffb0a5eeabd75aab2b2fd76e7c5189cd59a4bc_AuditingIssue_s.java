 package com.isaacjg.darklight.issues;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.ijg.darklight.sdk.core.Issue;
 import com.ijg.darklight.sdk.utils.FileLoader;
 import com.ijg.darklight.sdk.utils.INIUtils;
 
 /*
  * AuditingIssue - An Issue for Darklight Nova Core
  * Copyright  2013 Isaac Grant
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * AuditingIssue is an Issue for Darklight Nova Core that checks if
  * auditing policy has been set in Windows
  * 
  * @author Isaac Grant
  */
 public class AuditingIssue extends Issue {
 
	private File seceditDump = new File(".", "secedit.txt");
 	private long lastModified = seceditDump.lastModified();
 	
 	public AuditingIssue() {
 		super("Auditing", "Enabled auditing of various events");
 	}
 
 	@Override
 	public boolean isFixed() {
 		try {
			Process p = Runtime.getRuntime().exec("cmd.exe /c secedit /export /cfg " + seceditDump.getAbsolutePath());
 			p.waitFor();
 			lastModified = seceditDump.lastModified();
 		} catch (InterruptedException | IOException e) {
 			e.printStackTrace();
 		}
 		if (seceditDump.lastModified() == lastModified) {
 			try {
 				ArrayList<byte[]> dump = FileLoader.tokenizedLoad(seceditDump);
 				int systemEvents = Integer.parseInt(INIUtils.search("AuditSystemEvents", dump));
 				int logonEvents = Integer.parseInt(INIUtils.search("AuditLogonEvents", dump));
 				int objectAccess = Integer.parseInt(INIUtils.search("AuditObjectAccess", dump));
 				int privilegeUse = Integer.parseInt(INIUtils.search("AuditPrivilegeUse", dump));
 				int policyChange = Integer.parseInt(INIUtils.search("AuditPolicyChange", dump));
 				int accountManage = Integer.parseInt(INIUtils.search("AuditAccountManage", dump));
 				int processTracking = Integer.parseInt(INIUtils.search("AuditProcessTracking", dump));
 				int dsAccess = Integer.parseInt(INIUtils.search("AuditDSAccess", dump));
 				int accountLogon = Integer.parseInt(INIUtils.search("AuditAccountLogon", dump));
 				
 				if (systemEvents > 0 && logonEvents > 0 && objectAccess > 0 && privilegeUse > 0 && 
 						policyChange > 0 && accountManage > 0 && processTracking > 0 && dsAccess > 0 && 
 						accountLogon > 0) {
 					return true;
 				}
 			} catch (FileNotFoundException e) {}
 		} else {
 			System.err.println("secedit dump has been modified");
 		}
 		return false;
 	}
 }
