 /*
  * Copyright (C) 2008
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Derived from Travian world" logo. If the display of the logo is not
  * reasonably feasible for technical reasons, the Appropriate Legal Notices must
  * display the words "Derived from Travian world".
  */
 package ste.travian.world;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.zip.GZIPInputStream;
 
 /**
  *
  * @author ste
  */
 public class MapDownloader {
     
     private URL mapURL;
     
     public MapDownloader(final String url) {
         if (url == null) {
             throw new IllegalArgumentException("url cannot be null");
         }
         
         try {
             mapURL = new URL(url);
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException("'" + url + "' is not a valid URL (" + e + ")");
         }
     }
     
     /**
      * Retrieves the content of the URL and returns it as a String
      * 
      * @return the content of the URL as a String
      * 
      * @throws java.io.IOException
      */
     public String downloadMapAsString() throws IOException {
         StringBuffer map = new StringBuffer();
         
         BufferedReader r = this.downloadMapAsReader();
         String line = null;
         while ((line = r.readLine()) != null) {
             map.append(line);
         }
         
         r.close();
         
         return map.toString();
         
     }
     
     /**
      * Retrieves the content of the URL as an InputStream
      * @return the input stream from which read the content of the URL
      * 
      * @throws java.io.IOException
      */
     public InputStream downloadMapAsStream() throws IOException {
        return new GZIPInputStream(mapURL.openStream());
     }
     
     public BufferedReader downloadMapAsReader() throws IOException {
         return  new BufferedReader(
                     new InputStreamReader(
                        downloadMapAsStream()
                     )
                 );
     }
 
 }
