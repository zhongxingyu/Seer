 /**
  *
  */
 package org.iplantc.core.uidiskresource.client.models;
 
 import com.google.web.bindery.autobean.shared.AutoBean.PropertyName;
 
 /**
  * @author sriram
  *
  */
 public interface DiskResourceInfo {
 
     @PropertyName("dir-count")
     int getDirCount();
 
     @PropertyName("dir-count")
     void setDirCount(int count);
 
     @PropertyName("file-count")
     void setFileCount(int count);
 
     @PropertyName("file-count")
     int getFileCount();
 
     @PropertyName("share-count")
     int getShareCount();
 
     @PropertyName("share-count")
     void setShareCount(int count);
 
     @PropertyName("permissions")
     void setPermissions(Permissions p);
 
     @PropertyName("permissions")
     Permissions getPermissions();
 
     @PropertyName("type")
     String getType();
 
     @PropertyName("type")
     void setType(String type);
 
     @PropertyName("created")
     long getCreated();
 
     @PropertyName("created")
     void setCreated(long created);
 
     @PropertyName("modified")
     long getModified();
 
     @PropertyName("modified")
     void setModified(long modified);
 
     @PropertyName("size")
     void setSize(int size);
 
     @PropertyName("size")
     long getSize();
 
    @PropertyName("file-type")
     String getFileType();
 
    @PropertyName("file-type")
      void setFileType(String type);
 }
