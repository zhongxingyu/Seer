 package my.triviagame.xmcd;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import java.util.Collections;
 import java.util.List;
 import my.triviagame.dal.*;
 
 /**
  * A fake DAL that captures imported discs.
  */
 public class FakeDal implements IDAL {
 
     @Override
     public void openConnection(String hostName, int port, String dbName, String userName, String password) throws DALException {
         Preconditions.checkState(!opened, "Connection already open");
         opened = true;
     }
 
     @Override
     public void closeConnection() throws DALException {
         Preconditions.checkState(opened, "Connection not open");
         opened = false;
     }
 
     @Override
     public int getEstimatedTrackCount() throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public int getEstimatedAlbumCount() throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public int getEstimatedArtistCount() throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public ITableStatistics getTrackTableStatistics() throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public ITableStatistics getAlbumTableStatistics() throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public ITableStatistics getArtistTableStatistics() throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
    public List<ITrackDescriptor> getTrackDescriptors(String... keywords) throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public List<ITrackDescriptor> getTrackDescriptors(List<Integer> ids) throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public List<ITrackDescriptor> getAlbumTrackDescriptors(IAlbumDescriptor album) throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public List<ITrackDescriptor> getArtistTrackDescriptors(IArtistDescriptor artist) throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public List<IAlbumDescriptor> getAlbumDescriptors(List<Integer> ids) throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public List<IAlbumDescriptor> getArtistAlbumDescriptors(IArtistDescriptor artist) throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public List<IArtistDescriptor> getArtistDescriptors(List<Integer> ids) throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void prepareConstraintsForImport() throws DALException {
         // Do nothing
     }
 
     @Override
     public void createConstraintsAfterImport() throws DALException {
         // Do nothing
     }
 
     @Override
     public void importXmcdBatch(List<XmcdDisc> xmcdDiscList) throws DALException {
         imported.addAll(xmcdDiscList);
     }
 
     @Override
     public void CreateAlbum(IAlbumDescriptor album) throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void DeleteAlbum(IAlbumDescriptor album) throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void UpdateAlbum(IAlbumDescriptor album) throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     public List<XmcdDisc> getImported() {
         return Collections.unmodifiableList(imported);
     }
 
     private boolean opened = false;
     private List<XmcdDisc> imported = Lists.newArrayList();
 
     @Override
     public void CreateAlbum(IAlbumDescriptor album, List<ITrackDescriptor> tracks) throws DALException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 }
