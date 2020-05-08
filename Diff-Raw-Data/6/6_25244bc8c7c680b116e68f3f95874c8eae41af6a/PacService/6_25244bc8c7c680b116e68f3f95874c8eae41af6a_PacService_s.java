 package org.pentaho.pac.client;
 
 
 import org.pentaho.pac.client.datasources.IDataSource;
 import org.pentaho.pac.client.roles.ProxyPentahoRole;
 import org.pentaho.pac.client.users.DuplicateUserException;
 import org.pentaho.pac.client.users.ProxyPentahoUser;
 
 import com.google.gwt.user.client.rpc.RemoteService;
 
 public interface PacService extends RemoteService {
   public boolean createRole(ProxyPentahoRole role) throws PacServiceException;
   public boolean deleteRoles(ProxyPentahoRole[] roles) throws PacServiceException;
   public boolean updateRole(ProxyPentahoRole role) throws PacServiceException;
   public ProxyPentahoRole[] getRoles() throws PacServiceException;
   
   public boolean createUser(ProxyPentahoUser user) throws DuplicateUserException, PentahoSecurityException, PacServiceException;
  public boolean deleteUsers(ProxyPentahoUser[] users) throws PacServiceException;
  public boolean updateUser(ProxyPentahoUser user) throws PacServiceException;
   public ProxyPentahoUser[] getUsers() throws PacServiceException;
   
   public boolean createDataSource(IDataSource dataSource) throws PacServiceException;
   public boolean deleteDataSources(IDataSource[] dataSources) throws PacServiceException;
   public boolean updateDataSource(IDataSource dataSource) throws PacServiceException;
   public IDataSource[] getDataSources() throws PacServiceException;
   
   public String refreshSolutionRepository() throws PacServiceException;
   public String cleanRepository() throws PacServiceException;
   public String clearMondrianDataCache() throws PacServiceException;
   public String clearMondrianSchemaCache() throws PacServiceException;
   public String scheduleRepositoryCleaning() throws PacServiceException;
   public String resetRepository() throws PacServiceException;
   public String refreshSystemSettings() throws PacServiceException;
   public String executeGlobalActions() throws PacServiceException;
   public String refreshReportingMetadata() throws PacServiceException;
   public String getHomePage(String url) throws PacServiceException;
 }
