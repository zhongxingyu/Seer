 /*
  * Copyright 2007 Sun Microsystems, Inc.
  *
  * This file is part of jVoiceBridge.
  *
  * jVoiceBridge is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License version 2 as 
  * published by the Free Software Foundation and distributed hereunder 
  * to you.
  *
  * jVoiceBridge is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied this 
  * code. 
  */
 
 package com.sun.mc.softphone.media;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * Load native libraries stored in the jar file
  * @author jkaplan
  */
 public class NativeLibUtil {
     
     
     /**
      * Load a native library by reading it from a jar file and storing
      * the data in a temporary file.  This assumes the native library is
      * in the "native" package of the calling class (e.g. if the class
      * is com.sun.softphone.media.foo.FooAudio, the native libraries
      * should be in the jar file in /com/sun/softphone/media/foo/native/
      *
      * @param clazz the class that is loading the library
      * @param name the name of the library to load
      *
      * @throws IOException if there is an error reading the file
      * 
      */
     public static void loadLibrary(Class clazz, String name) 
         throws IOException 
     {
         // load the data from the jar file
         InputStream in = clazz.getResourceAsStream("native/" + name);
         
	System.err.println("native name is native/" + name);

         // create a temporary file
         String suffix = null;
         int dotIdx = name.lastIndexOf('.');
         if (dotIdx != -1) {
             suffix = name.substring(dotIdx - 1);
         }
         File f = File.createTempFile("lib", suffix);
         f.deleteOnExit();
         
         // write the data to the temporary file
         FileOutputStream fos = new FileOutputStream(f);
         byte[] buffer = new byte[1024];
         int count;
         while ((count = in.read(buffer)) != -1) {
             fos.write(buffer, 0, count);
         }
         fos.close();
         
         // load the temporary file as a library
 	String path = f.getAbsolutePath();
 
 	//System.out.println("Trying to load " + path + " writable " + f.canWrite()
 	//    + " length " + f.length() + " exists " + f.exists());
 
 	//System.out.println("class path " + System.getProperty("java.class.path"));
 	//System.out.println("library path " + System.getProperty("java.library.path"));
 
         System.load(path);
     }
     
 }
