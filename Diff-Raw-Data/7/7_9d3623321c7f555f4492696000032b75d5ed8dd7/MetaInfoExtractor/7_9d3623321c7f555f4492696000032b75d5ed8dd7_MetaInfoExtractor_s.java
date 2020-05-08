 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.photo.metadata;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.drew.imaging.jpeg.JpegMetadataReader;
 import com.drew.imaging.jpeg.JpegProcessingException;
 import com.drew.metadata.Directory;
 import com.drew.metadata.Metadata;
 import com.drew.metadata.MetadataException;
 import com.drew.metadata.Tag;
 
 /**
  * This class provides the metadata saved in a specified image. There are 
  * several methods to get certain parts of the data.
  */
 public class MetaInfoExtractor {
   private static Log LOGGER = LogFactory.getFactory().getInstance(MetaInfoExtractor.class
       );
   
   /**
    * Returns an array of Tag elements representing the metainformation,
    * contained in the specified directory of the given file.
    * 
    * @param imageFile The file to extract the data from.
    * @param directory The desired directory's Class.
    * @return An arry of Tags.
    * @throws MetadataException
    */
   public List<Tag> getDirectoryTagsAsTagList(InputStream imageFile, 
       Class<Directory> directory) throws MetadataException{
     Metadata metadata = getMetadata(imageFile);
     Directory dir = metadata.getDirectory(directory);
     List<Tag> data = new ArrayList<Tag>();
     for(Tag tag : dir.getTags()) {
       data.add(tag);
     }
     return data;
   }
     
   /**
    * To get all meta tags possibly contained in an image.
    * 
    * @param imageFile File to extract the Metadata from.
    * @return Hashtable containing the directorys data.
    * @throws MetadataException
    */
   public Map<String, String> getAllTags(InputStream imageFile) throws MetadataException{
     Metadata data = getMetadata(imageFile);
     Map<String, String> tags = new HashMap<String, String>();
    for(Directory dir : data.getDirectories()) {
      tags.putAll(getDirsTags(dir));
     }
     return tags;
   }
   
   /*
    * Extracts the metadata from the image file represented by an InputStream
    * 
    * @param imageFile InputStream of an image file.
    * @return Metadata containied in the specified image.
    */
   Metadata getMetadata(InputStream imageFile){
     Metadata metadata = null;
     try {
       metadata = JpegMetadataReader.readMetadata(imageFile);
     } catch (JpegProcessingException e) {
       LOGGER.error("Not able to load the meta data of " + imageFile, e);
     }
     return metadata;
   }
   
   /*
    * Saves all tags contained in the specified directory to a Hashtable and 
    * returnes them.
    * 
    * @param dir Directory to extract the tags from.
    * @return Hashtable containing th metatags from the Directory.
    * @throws MetadataException
    */
   Hashtable<String, String> getDirsTags(Directory dir) throws MetadataException{
     Hashtable<String, String> metadata = new Hashtable<String, String>();
     for (Tag tag : dir.getTags()) {
       metadata.put(tag.getTagName(), tag.toString());
     }
     return metadata;
   }
 }
