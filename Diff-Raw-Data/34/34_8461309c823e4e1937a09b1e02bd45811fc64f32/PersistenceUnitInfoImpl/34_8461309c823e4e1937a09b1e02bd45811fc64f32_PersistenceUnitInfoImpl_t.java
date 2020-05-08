 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2006, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.jpa.impl.deployment;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.persistence.Caching;
 import javax.persistence.ValidationMode;
 import javax.persistence.spi.ClassTransformer;
 import javax.persistence.spi.PersistenceUnitInfo;
 import javax.persistence.spi.PersistenceUnitTransactionType;
 import javax.sql.DataSource;
 
 import org.hibernate.ejb.HibernatePersistence;
 import org.jboss.logging.Logger;
 import org.jboss.metadata.jpa.spec.PersistenceUnitMetaData;
 import org.jboss.metadata.jpa.spec.SharedCacheMode;
 import org.jboss.metadata.jpa.spec.TransactionType;
 
 /**
  * Comment
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author Emmanuel Bernard
  * @version $Revision$
  */
 public class PersistenceUnitInfoImpl implements PersistenceUnitInfo
 {
    private static final Logger log = Logger.getLogger(PersistenceUnitInfoImpl.class);
    
    private String entityManagerName;
    private DataSource jtaDataSource;
    private DataSource nonJtaDataSource;
    private List<String> mappingFileNames;
    private List<URL> jarFiles;
    private List<String> entityclassNames;
    private Properties properties;
    private ClassLoader classLoader;
    private String persistenceProviderClassName;
    private PersistenceUnitTransactionType transactionType;
    private URL persistenceUnitRootUrl;
    private boolean excludeClasses;
    private ValidationMode validationMode;
    private Caching caching;
    private String persistenceXMLSchemaVersion;
 
    public PersistenceUnitInfoImpl()
    {
    }
 
    /**
     * Note that the jarFiles in metaData are ignore and should be
     * specified in the jarFiles argument.
     * 
     * @param metaData the persistence unit meta data
     * @param props properties for the persistence provider
     * @param classLoader the class loader used for entity class loading
     * @param persistenceUnitRootUrl a jar or JarInputStream where the entities are packaged
     * @param jarFiles a list of URLs pointing to jar or JarInputStreams where entities are packaged
     * @param ctx naming context for looking up data sources
     * @throws NamingException when a data source can't be located
     */
    public PersistenceUnitInfoImpl(PersistenceUnitMetaData metaData, Properties props, ClassLoader classLoader, URL persistenceUnitRootUrl, List<URL> jarFiles, Context ctx) throws NamingException
    {
       log.debug("Using class loader " + classLoader);
       this.setClassLoader(classLoader);
 
       this.setJarFiles(jarFiles);
       this.setPersistenceProviderClassName(HibernatePersistence.class.getName());
       log.debug("Found persistence.xml file in EJB3 jar");
       this.setManagedClassnames(safeList(metaData.getClasses()));
       this.setPersistenceUnitName(metaData.getName());
       this.setMappingFileNames(safeList(metaData.getMappingFiles()));
       this.setExcludeUnlistedClasses(metaData.isExcludeUnlistedClasses());
       this.setPersistenceUnitRootUrl(persistenceUnitRootUrl);
       PersistenceUnitTransactionType transactionType = getJPATransactionType(metaData);
       this.setTransactionType(transactionType);
       this.setCaching( convertToCaching( metaData.getSharedCacheMode() ) );
       this.setValidationMode( convertToValidationMode( metaData.getValidationMode() ) );
       //FIXME set appropriate version when accessible from metadata
       this.setPersistenceXMLSchemaVersion( null);
 
       if (metaData.getProvider() != null) this.setPersistenceProviderClassName(metaData.getProvider());
       /*
       if (explicitEntityClasses.size() > 0)
       {
          List<String> classes = this.getManagedClassNames();
          if (classes == null) classes = explicitEntityClasses;
          else classes.addAll(explicitEntityClasses);
          this.setManagedClassnames(classes);
       }
       */
       if (metaData.getJtaDataSource() != null)
       {
          this.setJtaDataSource((javax.sql.DataSource) ctx.lookup(metaData.getJtaDataSource()));
       }
       else if (transactionType == PersistenceUnitTransactionType.JTA)
       {
          throw new RuntimeException("Specification violation [EJB3 JPA 6.2.1.2] - "
                + "You have not defined a jta-data-source for a JTA enabled persistence context named: " + metaData.getName());
       }
       if (metaData.getNonJtaDataSource() != null)
       {
          this.setNonJtaDataSource((javax.sql.DataSource) ctx.lookup(metaData.getNonJtaDataSource()));
       }
       else if (transactionType == PersistenceUnitTransactionType.RESOURCE_LOCAL)
       {
          throw new RuntimeException("Specification violation [EJB3 JPA 6.2.1.2] - "
                + "You have not defined a non-jta-data-source for a RESOURCE_LOCAL enabled persistence context named: "
                + metaData.getName());
       }
       props.putAll(getProperties(metaData));
       this.setProperties(props);
 
       if (this.getPersistenceUnitName() == null)
       {
          throw new RuntimeException("you must specify a name in persistence.xml");
       }
 
       // EJBTHREE-893
       /* TODO: can this work remotely?
       if(!this.getProperties().containsKey("hibernate.session_factory_name"))
       {
          this.getProperties().put("hibernate.session_factory_name", kernelName);
       }
       */
    }
 
    private ValidationMode convertToValidationMode(org.jboss.metadata.jpa.spec.ValidationMode validationMode)
    {
      // JBJPA-15 / JBAS-7417: if no validation mode is specified return null
      if(validationMode == null) return null;
      
       switch (validationMode) {
          case AUTO:
             return ValidationMode.AUTO;
          case CALLBACK:
             return ValidationMode.CALLBACK;
          case NONE:
             return ValidationMode.NONE;
          default:
             return null;
       }
    }
 
    private Caching convertToCaching(SharedCacheMode sharedCacheMode)
    {
      // JBJPA-15: if no caching mode is specified return null
       if(sharedCacheMode == null) return null;
       
       switch (sharedCacheMode) {
          case ALL:
             return Caching.ALL;
          case DISABLE_SELECTIVE:
             return Caching.DISABLE_SELECTIVE;
          case ENABLE_SELECTIVE:
             return Caching.ENABLE_SELECTIVE;
          case NONE:
             return Caching.NONE;
          default:
             return null;
       }
    }
 
    public void addTransformer(ClassTransformer transformer)
    {
       //throw new RuntimeException("NOT IMPLEMENTED");
    }
 
    private static List<String> safeList(Set<String> set)
    {
       return (set == null || set.isEmpty()) ? Collections.<String>emptyList() : new ArrayList<String>(set);
    }
 
    public ClassLoader getNewTempClassLoader()
    {
       return null;
    }
 
    public String getPersistenceProviderClassName()
    {
       return persistenceProviderClassName;
    }
 
    public void setPersistenceProviderClassName(String persistenceProviderClassName)
    {
       this.persistenceProviderClassName = persistenceProviderClassName;
    }
 
    public String getPersistenceUnitName()
    {
       return entityManagerName;
    }
 
    public void setPersistenceUnitName(String entityManagerName)
    {
       this.entityManagerName = entityManagerName;
    }
 
    public DataSource getJtaDataSource()
    {
       return jtaDataSource;
    }
 
    public void setJtaDataSource(DataSource jtaDataSource)
    {
       this.jtaDataSource = jtaDataSource;
    }
 
    protected static PersistenceUnitTransactionType getJPATransactionType(PersistenceUnitMetaData metaData)
    {
       TransactionType type = metaData.getTransactionType();
       if (type == TransactionType.RESOURCE_LOCAL)
          return PersistenceUnitTransactionType.RESOURCE_LOCAL;
       else // default or actually being JTA
          return PersistenceUnitTransactionType.JTA;
    }
 
    public DataSource getNonJtaDataSource()
    {
       return nonJtaDataSource;
    }
 
    public void setNonJtaDataSource(DataSource nonJtaDataSource)
    {
       this.nonJtaDataSource = nonJtaDataSource;
    }
 
    public List<String> getMappingFileNames()
    {
       return mappingFileNames;
    }
 
    public void setMappingFileNames(List<String> mappingFileNames)
    {
       this.mappingFileNames = mappingFileNames;
    }
 
    public List<URL> getJarFileUrls()
    {
       return jarFiles;
    }
 
    public void setJarFiles(List<URL> jarFiles)
    {
       // Hibernate EM 3.3.2.GA LogHelper@49
       assert jarFiles != null : "jarFiles is null";
       
       this.jarFiles = jarFiles;
    }
 
    public List<String> getManagedClassNames()
    {
       return entityclassNames;
    }
 
    public void setManagedClassnames(List<String> entityclassNames)
    {
       this.entityclassNames = entityclassNames;
    }
 
    public Properties getProperties()
    {
       return properties;
    }
 
    protected static Map<String, String> getProperties(PersistenceUnitMetaData metaData)
    {
       Map<String, String> properties = metaData.getProperties();
       return (properties != null) ? properties : Collections.<String, String>emptyMap();
    }
 
    public void setProperties(Properties properties)
    {
       this.properties = properties;
    }
 
    public ClassLoader getClassLoader()
    {
       return classLoader;
    }
 
    public void setClassLoader(ClassLoader classLoader)
    {
       this.classLoader = classLoader;
    }
 
    public PersistenceUnitTransactionType getTransactionType()
    {
       return transactionType;
    }
 
    public void setTransactionType(PersistenceUnitTransactionType transactionType)
    {
       this.transactionType = transactionType;
    }
 
    public URL getPersistenceUnitRootUrl()
    {
       return persistenceUnitRootUrl;
    }
 
    public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl)
    {
       this.persistenceUnitRootUrl = persistenceUnitRootUrl;
    }
 
    public boolean excludeUnlistedClasses()
    {
       return excludeClasses;
    }
 
    public void setExcludeUnlistedClasses(boolean excludeClasses)
    {
       this.excludeClasses = excludeClasses;
    }
 
    public ValidationMode getValidationMode()
    {
       return validationMode;
    }
 
    public void setValidationMode(ValidationMode validationMode)
    {
       this.validationMode = validationMode;
    }
 
    public Caching getCaching()
    {
       return caching;
    }
 
    public void setCaching(Caching caching)
    {
       this.caching = caching;
    }
 
    public String getPersistenceXMLSchemaVersion()
    {
       return persistenceXMLSchemaVersion;
    }
 
    //TODO remove once typo is fixed in JPA API
    public String PersistenceXMLSchemaVersion()
    {
       return persistenceXMLSchemaVersion;
    }
 
    public void setPersistenceXMLSchemaVersion(String persistenceXMLSchemaVersion)
    {
       this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
    }
 }
