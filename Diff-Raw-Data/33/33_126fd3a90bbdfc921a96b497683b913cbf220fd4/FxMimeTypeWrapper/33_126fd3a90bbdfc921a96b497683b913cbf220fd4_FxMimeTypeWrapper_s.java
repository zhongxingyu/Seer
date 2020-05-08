 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.media;
 
 import com.flexive.shared.media.impl.FxMimeType;
 import java.io.Serializable;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A class to handle / wrap mime types incl. various tool methods
  *
  * @author Christopher Blasnik (c.blasnik@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  *
  * @since 3.1
  */
 public class FxMimeTypeWrapper implements Serializable {
     private static final long serialVersionUID = -3880163774501204024L;
     
     private List<FxMimeType> mimeTypes;
 
     /**
      * Def. constructor
      */
     public FxMimeTypeWrapper() {
         init();
     }
 
     /**
      * Construct using a (list) of mime type(s)
      *
      * @param mimeTypes a single mime type or a comma-separated list of mime types
      */
     public FxMimeTypeWrapper(String mimeTypes) {
         init();
         addMimeTypes(mimeTypes);
     }
 
     /**
      * Construct using a single MimeType
      *
      * @param mimeType a MimeType
      */
     public FxMimeTypeWrapper(FxMimeType mimeType) {
         init();
         addMimeType(mimeType);
     }
 
     /**
      * Init the mimeTypes ArrayList
      */
     private void init() {
         if (mimeTypes == null)
             mimeTypes = new ArrayList<FxMimeType>(3);
     }
 
     /**
      * Retrieve the List of all MimeTypes, returns an empty list if none were registered
      *
      * @return the List&lt;MimeType&gt;>
      */
     public List<FxMimeType> getMimeTypes() {
         return mimeTypes;
     }
 
     /**
      * Set a the internal List of MimeTypes FxMimeType by providing a List of MimeType
      *
      * @param mimeTypes the List of MimeTypes
      */
     public void setMimeTypes(List<FxMimeType> mimeTypes) {
         this.mimeTypes = mimeTypes;
     }
 
     /**
      * Add a mime type by providing an instance of the MimeType enum
      *
      * @param mimeType a MimeType
      */
     public void addMimeType(FxMimeType mimeType) {
         if(!mimeTypes.contains(mimeType))
             mimeTypes.add(mimeType);
     }
 
     /**
      * Add a mime type by providing a String parameter
      * The parameter can either be a single mime type or a list of comma-separated values
      *
      * @param mimeTypes the mime type as a String, e.g. "audio/wav", or several values such as "audio/wav,audio/mp3"
      */
     public void addMimeTypes(String mimeTypes) {
         String[] mtSplit = mimeTypes.split(",");
         for (String m : mtSplit) {
             m = StringUtils.trim(m);
             final FxMimeType mt = FxMimeType.getMimeTypeFromString(m);
             if (!this.mimeTypes.contains(mt))
                 this.mimeTypes.add(mt);
         }
     }
 
     /**
      * Remove a mime type by providing an instance of the MimeType enum
      *
      * @param mimeType the MimeType t.b. removed
      * @return true if the mimetype was removed
      */
     public boolean removeMimeType(FxMimeType mimeType) {
         return this.mimeTypes.remove(mimeType);
     }
 
     /**
      * Remove a mime type by providing a String parameter, e.g. "image/png"
      *
      * @param mimeType the mime type as a String
      * @return returns true if the removal was successful
      */
     public boolean removeMimeType(String mimeType) {
         boolean removed = false;
         FxMimeType mt = FxMimeType.getMimeTypeFromString(mimeType.toLowerCase());
 
         int idx = -1;
         for (int i = 0; i < mimeTypes.size(); i++) {
             final String currentType = mimeTypes.get(i).getType().toLowerCase();
             final String currentSubType = mimeTypes.get(i).getSubType();
 
             if (currentType.equals(mt.getType()) && currentSubType.equals(mt.getSubType())) {
                 idx = i;
                 break;
             }
         }
         if (idx != -1) {
             mimeTypes.remove(idx);
             removed = true;
         }
 
         return removed;
     }
 
     /**
      * Check if a given mimeType exists within the FxMimeType obj.
      *
      * @param mimeType a MimeType
      * @return returns true if found
      */
     public boolean contains(FxMimeType mimeType) {
         return mimeTypes.contains(mimeType);
     }
     
     /**
      * Check if a given mimeType exists within the FxMimeType obj.
      *
      * @param mimeType a MimeType
      * @return returns true if found
      */
     public boolean contains(String mimeType) {
         return contains(FxMimeType.getMimeTypeFromString(mimeType));
     }
 
 
     /**
      * Creates a comma separated list of mime types
      *
      * @return returns the list of mime types
      */
     @Override
     public String toString() {
        return mimeTypes == null ? "[]" : mimeTypes.toString();
     }
 }
