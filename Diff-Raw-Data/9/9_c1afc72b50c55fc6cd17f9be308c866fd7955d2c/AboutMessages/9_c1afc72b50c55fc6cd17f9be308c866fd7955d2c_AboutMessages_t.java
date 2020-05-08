 /*
 Copyright (C) 2001 Concord Consortium
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package 	org.concord.CCProbe;
 
 public class AboutMessages
 {
 	public static String [] getMessage()
 	{
 		messages[messages.length-1] = "v"+Version.VERSION+"."+
 			Version.MVERSION1+"."+Version.MVERSION2+" build "+Version.BUILD;
 		return messages;
 	}
 
 	// Leave a blank line at the end.  This had to be changed
 	// because wabajump doesn't do static initializers quite right
 	// so this message string was getting set before the Version class
 	// was initialized.
 	public static String [] messages = {"CCProbe Copyright (c) 2001",
 										"by Concord Consortium",
 										"All Rights Reserved",
 										"http://concord.org/ccprobeware",
 										"/ccprobe/ccprobe.html",
 										"Licensed under the GNU GPL",
 										"http://concord.org/ccprobeware",
 										"/ccprobe/ccprobe-license.html",
 										""};
 }
