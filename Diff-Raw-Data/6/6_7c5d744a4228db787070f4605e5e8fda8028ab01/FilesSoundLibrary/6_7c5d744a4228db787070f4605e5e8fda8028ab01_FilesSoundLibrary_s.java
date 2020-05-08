 /***
  **
  ** This library is free software; you can redistribute it and/or
  ** modify it under the terms of the GNU Lesser General Public
  ** License as published by the Free Software Foundation; either
  ** version 2.1 of the License, or (at your option) any later version.
  **
  ** This library is distributed in the hope that it will be useful,
  ** but WITHOUT ANY WARRANTY; without even the implied warranty of
  ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  ** Lesser General Public License for more details.
  **
  ** You should have received a copy of the GNU Lesser General Public
  ** License along with this library; if not, write to the Free Software
  ** Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  **
  **/
 package org.soundboard.library;
 
 import java.io.File;
 import java.io.InputStream;
import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import org.soundboard.server.LoggingService;
 import org.soundboard.util.ChunkedByteBuffer;
 
 public class FilesSoundLibrary extends SoundLibrary {
 
   private Map<String, File> files = new HashMap<String, File>();
 
    /**
     * add a file to the library
     * return if the file was added to the library
     */
    @Override public boolean addFile(File source) {
       boolean added = false;
       if (source != null && !source.isDirectory()) {
          try {
             String shortName = source.getName().substring(0, source.getName().lastIndexOf(".")).toLowerCase();
             if (shouldAttemptLoad(source) && files.get(shortName) == null) {
                markAttempt(source);
                files.put(shortName, source);
                added = true;
                LoggingService.getInstance().log("Added '" + shortName + "' into library" + (!getName().equals(SoundLibrarian.DEFAULT_LIBRARY) ? " " + getName() : ""));
             }
          } catch (Exception e) {
             LoggingService.getInstance().log("Unable to load '" + source.getAbsolutePath() + "' into library" + (!getName().equals(SoundLibrarian.DEFAULT_LIBRARY) ? " " + getName() : ""));
             LoggingService.getInstance().serverLog(e);
          }
       }
       return added;
    }
    
    /**
     * get the list of files in the library (lists the short name -- lookup key)
     */
    @Override
    public Set<String> list() {
       return files.keySet();
    }
 
    /**
     * get the data for the given short filename
     */
    @Override
    public ChunkedByteBuffer getData(String shortName) {
       return null;
    }
    
    /**
     * get the inputstream for the given short filename
     */
    @Override
    public InputStream getDataStream(String shortName) {
       return null;
    }
    
    @Override public File getFile(String name) {
       return files.get(name);
    }
    
 }
