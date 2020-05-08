 package org.otherobjects.cms.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.dao.PagedList;
 
 /**
  * Filesystem backed implentation of DataFileDao.
  * 
  * <p>TODO Should this support versioning?
  * <p>TODO Should this support renaming?
  * <p>FIXME Add assertions to prevent file overwriting and check files exist
  * 
  * @author rich
  */
 public class DataFileDaoFileSystem implements DataFileDao
 {
    // TODO Need to customise this
    // TODO Need to allow static data servers
    private final String urlBase = "/data";
    private final String dataPath = "webapp/data/";
 
     public boolean exists(String id)
     {
         File file = new File(this.dataPath + id);
         return file.exists();
     }
 
     public DataFile get(String id)
     {
         if (!exists(id))
             return null;
 
         File file = new File(this.dataPath + id);
         DataFile dataFile = new DataFile(file);
         dataFile.setId(id);
         setFileProperties(dataFile);
         return dataFile;
 
     }
 
     private void setFileProperties(DataFile dataFile)
     {
         dataFile.setExternalUrl(this.urlBase + dataFile.getId());
     }
 
     public void remove(String id)
     {
     }
 
     public DataFile save(DataFile dataFile)
     {
         try
         {
             // TODO Check for existing file?
             File destination = new File(this.dataPath + dataFile.getId());
             if (!destination.getParentFile().exists())
                 destination.getParentFile().mkdirs();
 
             FileUtils.copyFile(dataFile.getFile(), destination, false);
             dataFile.setFile(destination);
             setFileProperties(dataFile);
             return dataFile;
         }
         catch (IOException e)
         {
             throw new OtherObjectsException("Error copying file data:" + dataFile.getId(), e);
         }
     }
 
     public DataFile save(DataFile object, boolean validate)
     {
         return null;
     }
 
     public List<DataFile> getAll()
     {
         return null;
     }
 
     public PagedList<DataFile> getAllPaged(int pageSize, int pageNo, String filterQuery, String sortField, boolean asc)
     {
         return null;
     }
 
     public PagedList<DataFile> getPagedByQuery(String query, int pageSize, int pageNo, String filterQuery, String sortField, boolean asc)
     {
         return null;
     }
 }
