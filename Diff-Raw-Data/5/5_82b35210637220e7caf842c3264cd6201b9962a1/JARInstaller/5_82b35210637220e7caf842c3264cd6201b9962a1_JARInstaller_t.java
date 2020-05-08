 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.support.io;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 
 /**
  * This class provides basic mechanics to install
  * the contents of a JAR file into a particular location
  * for access by components
  *
  * @author Xavier Llor&agrave;
  * @author Boris Capitanu
  *
  */
 public class JARInstaller {
 
     public enum InstallStatus {
         SUCCESS, FAILED, SKIPPED
     }
 
 	/** Chunk size */
 	private static final int READ_WRITE_CHUNK_SIZE = 65536;
 
 	/** Install the contents of the jar at the given location. If location
 	 * exists no installation is performed, unless forced.
 	 *
 	 * @param sDestDir The location of the root directory where to install the stuff
 	 * @param sJarName The name of the jar to expand
 	 * @param bForce Force the installation by deleting the folder
 	 * @return True is the process finished correctly, false otherwise.
 	 */
 	public static synchronized InstallStatus installFromStream(InputStream jarStream, String sDestDir, boolean bForce ) {
 		File fRootDir = new File(sDestDir);
 		// Basic checking
 		if ( fRootDir.exists() ) {
 			if ( bForce ) {
 				boolean bOK = deleteDir(fRootDir);
 				if ( !bOK ) return InstallStatus.FAILED;
 			}
 			else {
 				return InstallStatus.SKIPPED;
 			}
 		}
 		else
 			fRootDir.mkdirs();
 
 		// Unjar the contents
 		try {
 			JarInputStream jar = new JarInputStream(jarStream);
 			JarEntry je = null;
 			while ( (je=jar.getNextJarEntry())!=null ) {
			    String target = sDestDir+File.separator+je.getName();
	            File fileTarget = new File(new File(target).toURI());
 				if ( je.isDirectory() ) {
 					fileTarget.mkdirs();
 				} else {
 					FileOutputStream fos = new FileOutputStream(fileTarget);
 	                byte [] baBuf = new byte[READ_WRITE_CHUNK_SIZE];
 	                int len;
 	                while ((len = jar.read(baBuf)) > 0) {
 	                    fos.write(baBuf, 0, len);
 	                }
 	                fos.close();
 	            }
 	        }
 		} catch (Throwable t) {
		    t.printStackTrace();
 			deleteDir(new File(sDestDir));
 			return InstallStatus.FAILED;
 		}
 
 		return InstallStatus.SUCCESS;
 	}
 
 
 	/**  Deletes all files and subdirectories under dir.
 	 *   Returns true if all deletions were successful.
 	 *   If a deletion fails, the method stops attempting to delete and returns false.
 	 *
 	 * @param dir The directory to delete
 	 * @return True if it was properly cleaned, false otherwise
 	 */
     protected static boolean deleteDir(File dir) {
     	if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i=0; i<children.length; i++) {
                 boolean success = deleteDir(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
             }
         }
 
         // The directory is now empty so delete it
     	if (dir.exists())
             return dir.delete();
     	else
     		return true;
     }
 }
