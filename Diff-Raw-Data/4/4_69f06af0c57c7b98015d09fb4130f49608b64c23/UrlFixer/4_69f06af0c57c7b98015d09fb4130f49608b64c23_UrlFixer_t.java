/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

 package org.mconf;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.commons.codec.digest.DigestUtils;
 
 public class UrlFixer {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		
 		System.out.print("Server salt: ");
 		String salt = in.readLine();
 		
 		System.out.println("Paste BigBlueButton API calls to fix the checksum (CTRL+C or empty to quit)");
 		String s;
 		while ((s = in.readLine()) != null && s.length() != 0) {
 			try {
 				String prefix = s.substring(0, s.indexOf("/api/") + 5);
 				s = s.substring(prefix.length(), s.lastIndexOf("&checksum="));
 				String checksum = DigestUtils.sha1Hex(s.replaceFirst("[?]", "") + salt);
 				s = prefix + s + "&checksum=" + checksum;
 				System.out.println("=> " + s);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
