 package org.generationcp.middleware.manager;
 
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.generationcp.commons.hibernate.HttpRequestAware;
 import org.generationcp.middleware.exceptions.ConfigException;
 import org.generationcp.middleware.hibernate.HibernateSessionPerRequestProvider;
 import org.generationcp.middleware.hibernate.HibernateSessionProvider;
 import org.generationcp.middleware.manager.api.ManagerFactoryProvider;
 import org.generationcp.middleware.pojos.workbench.CropType;
 import org.generationcp.middleware.pojos.workbench.Project;
 import org.generationcp.middleware.util.SessionFactoryUtil;
 import org.hibernate.SessionFactory;
 
 /**
  * The {@link DefaultManagerFactoryProvider} is an implementation of
  * {@link ManagerFactoryProvider} that expects central databases are named using
  * the format: <br>
  * <code>ibdb_&lt;crop type&gt;_central</code><br>
  * and local databases are named using the format: <br>
  * <code>&lt;crop type&gt;_&lt;project id&gt;_local</code><br>
  * 
  * @author Glenn Marintes
  */
 public class DefaultManagerFactoryProvider implements ManagerFactoryProvider, HttpRequestAware {
     private Map<Long, SessionFactory> localSessionFactories = new HashMap<Long, SessionFactory>();
     private Map<CropType, SessionFactory> centralSessionFactories = new HashMap<CropType, SessionFactory>();
     
     private final static ThreadLocal<HttpServletRequest> CURRENT_REQUEST = new ThreadLocal<HttpServletRequest>();
     
     private Map<HttpServletRequest, HibernateSessionProvider> localSessionProviders = new HashMap<HttpServletRequest, HibernateSessionProvider>();
     private Map<HttpServletRequest, HibernateSessionProvider> centralSessionProviders = new HashMap<HttpServletRequest, HibernateSessionProvider>();
     
     private String localHost = "localhost";
 
     private Integer localPort = 13306;
 
     private String localUsername = "local";
 
     private String localPassword = "local";
 
     private String centralHost = "localhost";
 
     private Integer centralPort = 13306;
 
     private String centralUsername = "central";
 
     private String centralPassword = "central";
 
     public void setLocalHost(String localHost) {
         this.localHost = localHost;
     }
 
     public void setLocalPort(Integer localPort) {
         this.localPort = localPort;
     }
 
     public void setLocalUsername(String localUsername) {
         this.localUsername = localUsername;
     }
 
     public void setLocalPassword(String localPassword) {
         this.localPassword = localPassword;
     }
 
     public void setCentralHost(String centralHost) {
         this.centralHost = centralHost;
     }
 
     public void setCentralPort(Integer centralPort) {
         this.centralPort = centralPort;
     }
 
     public void setCentralUsername(String centralUsername) {
         this.centralUsername = centralUsername;
     }
 
     public void setCentralPassword(String centralPassword) {
         this.centralPassword = centralPassword;
     }
 
     @Override
     public synchronized ManagerFactory getManagerFactoryForProject(Project project) {
         SessionFactory localSessionFactory = localSessionFactories.get(project.getProjectId());
         if (localSessionFactory == null) {
             String localDbName = String.format("%s_%d_local", project.getCropType().getCropName().toLowerCase(), project.getProjectId().longValue());
             
             DatabaseConnectionParameters params = new DatabaseConnectionParameters(localHost, String.valueOf(localPort), localDbName, localUsername, localPassword);
             try {
                 localSessionFactory = SessionFactoryUtil.openSessionFactory(params);
                 localSessionFactories.put(project.getProjectId(), localSessionFactory);
             }
             catch (FileNotFoundException e) {
                 throw new ConfigException("Cannot create a SessionFactory for " + project, e);
             }
         }
         
         SessionFactory centralSessionFactory = centralSessionFactories.get(project.getCropType());
        if (centralSessionFactory == null && project.getCropType().getCentralDatabaseName() != null) {
            String centralDbName = project.getCropType().getCentralDatabaseName();
             
             DatabaseConnectionParameters params = new DatabaseConnectionParameters(centralHost, String.valueOf(centralPort), centralDbName, centralUsername, centralPassword);
             
             try {
                 centralSessionFactory = SessionFactoryUtil.openSessionFactory(params);
                 centralSessionFactories.put(project.getCropType(), centralSessionFactory);
             }
             catch (FileNotFoundException e) {
                 throw new ConfigException("Cannot create a SessionFactory for " + project, e);
             }
         }
         
         // get or create the HibernateSessionProvider for the current request
         HttpServletRequest request = CURRENT_REQUEST.get();
         HibernateSessionProvider localSessionProvider = localSessionProviders.get(request);
         if (localSessionProvider == null && localSessionFactory != null) {
             localSessionProvider = new HibernateSessionPerRequestProvider(localSessionFactory);
             localSessionProviders.put(request, localSessionProvider);
         }
         
         HibernateSessionProvider centralSessionProvider = centralSessionProviders.get(request);
         if (centralSessionProvider == null && centralSessionFactory != null) {
             centralSessionProvider = new HibernateSessionPerRequestProvider(centralSessionFactory);
             centralSessionProviders.put(request, centralSessionProvider);
         }
         
         // create a ManagerFactory and set the HibernateSessionProviders
         // we don't need to set the SessionFactories here
         // since we want to a Session Per Request 
         ManagerFactory factory = new ManagerFactory();
         factory.setSessionProviderForLocal(localSessionProvider);
         factory.setSessionProviderForCentral(centralSessionProvider);
         
         return factory;
     }
     
     @Override
     public synchronized ManagerFactory getManagerFactoryForCropType(CropType cropType) {
        String centralDbName = cropType.getCentralDatabaseName();
         if (centralDbName == null) {
             return null;
         }
         
         SessionFactory centralSessionFactory = centralSessionFactories.get(cropType);
         if (centralSessionFactory == null) {
             DatabaseConnectionParameters params = new DatabaseConnectionParameters(centralHost, String.valueOf(centralPort), centralDbName, centralUsername, centralPassword);
             
             try {
                 centralSessionFactory = SessionFactoryUtil.openSessionFactory(params);
                 centralSessionFactories.put(cropType, centralSessionFactory);
             }
             catch (FileNotFoundException e) {
                 throw new ConfigException("Cannot create a SessionFactory for " + cropType, e);
             }
         }
         
         // get or create the HibernateSessionProvider for the current request
         HttpServletRequest request = CURRENT_REQUEST.get();
         
         HibernateSessionProvider centralSessionProvider = centralSessionProviders.get(request);
         if (centralSessionProvider == null && centralSessionFactory != null) {
             centralSessionProvider = new HibernateSessionPerRequestProvider(centralSessionFactory);
             centralSessionProviders.put(request, centralSessionProvider);
         }
         
         // create a ManagerFactory and set the HibernateSessionProviders
         // we don't need to set the SessionFactories here
         // since we want to a Session Per Request 
         ManagerFactory factory = new ManagerFactory();
         factory.setSessionProviderForCentral(centralSessionProvider);
         
         return factory;
     }
     
     @Override
     public void onRequestStarted(HttpServletRequest request, HttpServletResponse response) {
         // remember the HttpServletRequest for this thread
         CURRENT_REQUEST.set(request);
     }
     
     @Override
     public void onRequestEnded(HttpServletRequest request, HttpServletResponse response) {
         HibernateSessionProvider localSessionProvider = localSessionProviders.get(request);
         if (localSessionProvider != null) {
             localSessionProvider.close();
             localSessionProviders.remove(request);
         }
         
         HibernateSessionProvider centralSessionProvider = centralSessionProviders.get(request);
         if (centralSessionProvider != null) {
             centralSessionProvider.close();
             centralSessionProviders.remove(request);
         }
         
         CURRENT_REQUEST.remove();
     }
 
     @Override
     public synchronized void close() {
         // close the HibernateSessionProviders
         for (HttpServletRequest request : localSessionProviders.keySet()) {
             HibernateSessionProvider provider = localSessionProviders.get(request);
             if (provider != null) {
                 provider.close();
             }
         }
         
         for (HttpServletRequest request : centralSessionProviders.keySet()) {
             HibernateSessionProvider provider = centralSessionProviders.get(request);
             if (provider != null) {
                 provider.close();
             }
         }
         
         // close the SessionFactories
         for (Long projectId : localSessionFactories.keySet()) {
             SessionFactory factory = localSessionFactories.get(projectId);
             if (factory != null) {
                 factory.close();
             }
         }
         
         for (CropType cropType : centralSessionFactories.keySet()) {
             SessionFactory factory = centralSessionFactories.get(cropType);
             if (factory != null) {
                 factory.close();
             }
         }
 
         // clear the cache
         localSessionFactories.clear();
         centralSessionFactories.clear();
         localSessionProviders.clear();
         centralSessionProviders.clear();
     }
 }
