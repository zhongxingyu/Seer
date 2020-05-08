 package com.isaacjg.darklight.issues;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.ijg.darklight.sdk.core.Issue;
 import com.ijg.darklight.sdk.utils.FileLoader;
 import com.ijg.darklight.sdk.utils.INIUtils;
 
 /*
  * PwdPolicyIssue - An Issue for Darklight Nova Core
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
  * PwdPolicyIssue is an Issue for Darklight Nova Core that checks if
  * password policies have been set in Windows
  * 
  * @author Isaac Grant
  */
 public class PwdPolicyIssue extends Issue {
 
 	public PwdPolicyIssue() {
 		super("Password Policy", "Password policy has been enforced");
 	}
 	
 	@Override
 	public boolean isFixed() {
		File seceditDump = new File("secedit.txt");
 		try {
			Process p = Runtime.getRuntime().exec("cmd.exe /c secedit /export /cfg \"" + seceditDump.getAbsolutePath() + "\"");
 			p.waitFor();
 		} catch (InterruptedException | IOException e) {
 			e.printStackTrace();
 		}
 		try {
 			ArrayList<byte[]> dump = FileLoader.tokenizedLoad(seceditDump);
 			int minAge = Integer.parseInt(INIUtils.search("MinimumPasswordAge", dump));
 			int maxAge = Integer.parseInt(INIUtils.search("MaximumPasswordAge", dump));
 			int minLength = Integer.parseInt(INIUtils.search("MinimumPasswordLength", dump));
 			int complexity = Integer.parseInt(INIUtils.search("PasswordComplexity", dump));
 			if (minAge > 4 && minAge < maxAge) {
 				if (maxAge >= 25) {
 					if (minLength >= 8) {
 						if (complexity == 1) {
 							return true;
 						}
 					}
 				}
 			}
 		} catch (FileNotFoundException e) {}
 		
 		return false;
 	}
 }
