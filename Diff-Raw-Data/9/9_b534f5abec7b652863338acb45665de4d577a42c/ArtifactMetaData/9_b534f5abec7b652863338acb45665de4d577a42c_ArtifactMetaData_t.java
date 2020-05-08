 /*
  * Copyright LGPL3
  * YES Technology Association
  * http://yestech.org
  *
  * http://www.opensource.org/licenses/lgpl-3.0.html
  */
 
 /*
  *
  * Author:  Artie Copeland
  * Last Modified Date: $DateTime: $
  */
 package org.yestech.publish.objectmodel;
 
 import org.joda.time.DateTime;
 
 /**
  * @author Artie Copeland
  * @version $Revision: $
  */
public class ArtifactMetaData<O extends IArtifactOwner, I> implements IArtifactMetaData<O, I> {
     private String mimeType;
     private String fileName;
     private long size;
     private ArtifactType type;
     private String location;
     private O owner;
     private I identifier;
     private DateTime created;
     private DateTime modified;
 
     public DateTime getCreated() {
         return created;
     }
 
     public void setCreated(DateTime created) {
         this.created = created;
     }
 
     public DateTime getModified() {
         return modified;
     }
 
     public void setModified(DateTime modified) {
         this.modified = modified;
     }
 
     public I getIdentifier() {
         return identifier;
     }
 
     public void setIdentifier(I identifier) {
         this.identifier = identifier;
     }
 
     public String getMimeType() {
         return mimeType;
     }
 
     public void setMimeType(String mimeType) {
         this.mimeType = mimeType;
     }
 
     public String getFileName() {
         return fileName;
     }
 
     public void setFileName(String fileName) {
         this.fileName = fileName;
     }
 
     public long getSize() {
         return size;
     }
 
     public void setSize(long size) {
         this.size = size;
     }
 
     public ArtifactType getType() {
         return type;
     }
 
     public void setType(ArtifactType type) {
         this.type = type;
     }
 
     public String getLocation() {
         return location;
     }
 
     public void setLocation(String location) {
         this.location = location;
     }
 
     public O getOwner() {
         return owner;
     }
 
     public void setOwner(O owner) {
         this.owner = owner;
     }
 }
