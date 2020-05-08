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
 package com.flexive.shared.media.impl;
 
 import org.apache.commons.lang.StringUtils;
 
 import java.io.Serializable;
 
 /**
  * A general mime type / subtype implementation
  *
  * @author Christopher Blasnik (c.blasnik@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  *
  * @since 3.1
  */
 public class FxMimeType implements Serializable {
     /**
      * "Main/default" mime types
      */
     public static final String APPLICATION = "application";
     public static final String AUDIO = "audio";
     public static final String EXAMPLE = "example";
     public static final String IMAGE = "image";
     public static final String MESSAGE = "message";
     public static final String MODEL = "model";
     public static final String MULTIPART = "multipart";
     public static final String TEXT = "text";
     public static final String VIDEO = "video";
     public static final String UNKNOWN = "unknown";
    public static final String DEFAULT = UNKNOWN + "/" + UNKNOWN;
 
     private String type;
     private String subType;
     private static final long serialVersionUID = -5416305800516366421L;
 
     /**
      * Default constructor, sets a default of "application/octet-stream"
      */
     public FxMimeType() {
         this.type = "application";
         this.subType = "octet-stream";
     }
 
     /**
      * Constr. providing a main type only, no subtype ("unknown")
      *
      * @param type the main mime type
      */
     public FxMimeType(String type) {
         this.type = type;
        this.subType = UNKNOWN;
     }
 
     /**
      * Construct a mimeType from a given main- and sub type
      *
      * @param type the type (e.g. "application")
      * @param subType the sub type (e.g. "msword")
      */
     public FxMimeType(String type, String subType) {
         this.type = type;
         this.subType = subType;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public String getSubType() {
         return subType;
     }
 
     public void setSubType(String subType) {
         this.subType = subType;
     }
 
     @Override
     public String toString() {
         if(!StringUtils.isBlank(subType))
             return type + "/" + subType;
        else return type + "/" + UNKNOWN;
     }
 
     @Override
     public int hashCode() {
         int hashCode;
         hashCode = (type != null ? type.hashCode() : 0);
         hashCode = 31 * hashCode + (subType != null ? subType.hashCode() : 0);
         return hashCode;
     }
 
     @Override
     public boolean equals(Object o) {
         if(o == null || !(o instanceof FxMimeType))
             return false;
         if(o == this)
             return true;
         final FxMimeType comp = (FxMimeType)o;
         return !(!this.type.equals(comp.type) || !this.subType.equals(comp.subType));
     }
 
     /**
      * Construct a MimeType from a given String. If the parameterised mime type cannot be identified,
      * a DEFAULT mime type of "application/octet-stream" will be constructed
      *
      * @param mimeType the mimetype as a String, e.g. "application/pdf"
      * @return returns a MimeType
      */
     public static FxMimeType getMimeTypeFromString(String mimeType) {
         final String[] s = mimeType.split("/");
         final String type = StringUtils.trim(s[0].toLowerCase());
         final String subType;
 
        if(s.length > 1 && !StringUtils.isBlank(type))
           subType = StringUtils.trim(s[1].toLowerCase());
        else if(StringUtils.isBlank(type))
            return new FxMimeType(FxMimeType.UNKNOWN, FxMimeType.UNKNOWN);
         else
            subType = UNKNOWN;
 
         return new FxMimeType(type, subType);
     }
 }
