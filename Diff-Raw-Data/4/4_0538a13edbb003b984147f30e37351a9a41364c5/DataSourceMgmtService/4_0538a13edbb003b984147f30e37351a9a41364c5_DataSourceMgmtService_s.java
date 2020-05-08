 package org.pentaho.pac.server.datasources;
 
 import java.util.List;
 
 import org.pentaho.pac.common.PentahoSecurityException;
 import org.pentaho.pac.common.datasources.DuplicateDataSourceException;
 import org.pentaho.pac.common.datasources.IPentahoDataSource;
 import org.pentaho.pac.common.datasources.NonExistingDataSourceException;
 import org.pentaho.pac.server.common.DAOException;
 import org.pentaho.pac.server.common.DAOFactory;
 
 public class DataSourceMgmtService implements IDataSourceMgmtService {
   IDataSourceDAO dataSourceDAO = null;
 
   public DataSourceMgmtService() {
     dataSourceDAO = DAOFactory.getDataSourceDAO();
   }
 
   public void createDataSource(IPentahoDataSource newDataSource) throws DuplicateDataSourceException, DAOException, PentahoSecurityException {
     if (hasCreateDataSourcePerm(newDataSource)) {
       dataSourceDAO.createDataSource(newDataSource);
     } else {
       throw new PentahoSecurityException();
     }
   }
 
   public void deleteDataSource(String jndiName) throws NonExistingDataSourceException, DAOException, PentahoSecurityException {
     IPentahoDataSource dataSource = dataSourceDAO.getDataSource(jndiName);
    if (jndiName != null) {
      deleteDataSource(jndiName);
     } else {
       throw new NonExistingDataSourceException(jndiName);
     }
   }
 
   public void deleteDataSource(IPentahoDataSource dataSource) throws NonExistingDataSourceException, DAOException, PentahoSecurityException {
     if (hasDeleteDataSourcePerm(dataSource)) {
       dataSourceDAO.deleteDataSource(dataSource);
     } else {
       throw new PentahoSecurityException();
     }
   }
 
   public IPentahoDataSource getDataSource(String jndiName) throws DAOException {
     return dataSourceDAO.getDataSource(jndiName);
   }
 
   public List<IPentahoDataSource> getDataSources() throws DAOException {
     return dataSourceDAO.getDataSources();
   }
 
   public void updateDataSource(IPentahoDataSource dataSource) throws DAOException, PentahoSecurityException, NonExistingDataSourceException {
     if (hasUpdateDataSourcePerm(dataSource)) {
       dataSourceDAO.updateDataSource(dataSource);
     } else {
       throw new PentahoSecurityException();
     }
   }
   public void beginTransaction()  throws DAOException {
     dataSourceDAO.beginTransaction();
   }
 
   public void commitTransaction()  throws DAOException {
     dataSourceDAO.commitTransaction();
   }
 
   public void rollbackTransaction()  throws DAOException {
     dataSourceDAO.rollbackTransaction();
   }
 
   public void closeSession() {
     dataSourceDAO.closeSession();
   }
 
   protected boolean hasCreateDataSourcePerm(IPentahoDataSource dataSource) {
     return true;
   }
 
   protected boolean hasUpdateDataSourcePerm(IPentahoDataSource dataSource) {
     return true;
   }
   protected boolean hasDeleteDataSourcePerm(IPentahoDataSource dataSource) {
     return true;
   }
 }
