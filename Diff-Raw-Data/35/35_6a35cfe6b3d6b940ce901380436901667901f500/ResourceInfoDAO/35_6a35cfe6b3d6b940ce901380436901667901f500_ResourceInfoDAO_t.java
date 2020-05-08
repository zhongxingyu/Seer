 /**
  * 
  */
 package pl.psnc.dl.wf4ever.db.dao;
 
 import org.joda.time.DateTime;
 
 import pl.psnc.dl.wf4ever.db.ResourceInfo;
 
 /**
  * RODL user model.
  * 
  * @author piotrhol
  * 
  */
 public final class ResourceInfoDAO extends AbstractDAO<ResourceInfo> {
 
     /** id. */
     private static final long serialVersionUID = -4468344863067565271L;
 
 
     /**
      * Load an instance or create a new one.
      * 
      * @param path
      *            file path
      * @param name
      *            file name
      * @param checksum
      *            checksum
      * @param sizeInBytes
      *            size in bytes
      * @param digestMethod
      *            i.e. MD5, SHA1
      * @param lastModified
      *            date of last modification
      * @param mimeType
      *            MIME type
      * @return an instance
      */
     public ResourceInfo create(String path, String name, String checksum, long sizeInBytes, String digestMethod,
             DateTime lastModified, String mimeType) {
         ResourceInfo res = findByPath(path);
         if (res == null) {
             return new ResourceInfo(path, name, checksum, sizeInBytes, digestMethod, lastModified, mimeType);
         } else {
            res.setName(name);
            res.setChecksum(checksum);
            res.setSizeInBytes(sizeInBytes);
            res.setDigestMethod(digestMethod);
            res.setLastModifiedInMilis(lastModified.getMillis());
            res.setMimeType(mimeType);
             return res;
         }
     }
 
 
     /**
      * Find by file path.
      * 
      * @param path
      *            file path
      * @return resource info or null
      */
     public ResourceInfo findByPath(String path) {
         return findByPrimaryKey(ResourceInfo.class, path);
     }
 }
