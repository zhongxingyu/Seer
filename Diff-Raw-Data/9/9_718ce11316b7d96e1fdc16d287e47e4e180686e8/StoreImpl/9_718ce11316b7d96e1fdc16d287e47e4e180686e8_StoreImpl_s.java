 package org.xbrlapi.data.bdbxml;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.FragmentList;
 import org.xbrlapi.data.XBRLStore;
 import org.xbrlapi.data.XBRLStoreImpl;
 import org.xbrlapi.impl.FragmentFactory;
 import org.xbrlapi.impl.FragmentListImpl;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 import org.xbrlapi.utilities.XMLDOMBuilder;
 
 import com.sleepycat.db.CheckpointConfig;
 import com.sleepycat.db.DatabaseException;
 import com.sleepycat.db.Environment;
 import com.sleepycat.db.EnvironmentConfig;
 import com.sleepycat.dbxml.XmlContainer;
 import com.sleepycat.dbxml.XmlDocument;
 import com.sleepycat.dbxml.XmlDocumentConfig;
 import com.sleepycat.dbxml.XmlException;
 import com.sleepycat.dbxml.XmlIndexSpecification;
 import com.sleepycat.dbxml.XmlManager;
 import com.sleepycat.dbxml.XmlManagerConfig;
 import com.sleepycat.dbxml.XmlQueryContext;
 import com.sleepycat.dbxml.XmlQueryExpression;
 import com.sleepycat.dbxml.XmlResults;
 import com.sleepycat.dbxml.XmlUpdateContext;
 import com.sleepycat.dbxml.XmlValue;
 
 /**
  * Implementation of the data store using the Oracle 
  * Berkeley Database.  Note that this store implementation
  * does not use the XML:DB API and so does not require
  * a DBConnection implementation.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class StoreImpl extends XBRLStoreImpl implements XBRLStore {
 
     private int callCount = 0;
     private final int RESET_CALL_COUNT = 100000;
     private final int CHECKPOINT_KILOBYTES = 1000;
     private String locationName = null;
     private String containerName = null;
 	private Environment environment = null;
	private XmlManager dataManager = null;
	private XmlContainer dataContainer = null;
 
     /**
      * Initialise the BDB XML database data store.
      * @param location The location of the database (The path to where the database exists)
      * @param container The name of the container to hold the data in the store.
      * @throws XBRLException
      */
     public StoreImpl(String location, String container) throws XBRLException {
 
         if (location != null) this.locationName = location;
         else throw new XBRLException("The Berkeley DB XML database location must be specified.");
         
         if (container != null)  this.containerName = container;
         else throw new XBRLException("The Berkeley DB XML database container must be specified.");
         
         try {
             XmlManager.setLogLevel(XmlManager.LEVEL_ALL, false);
             XmlManager.setLogCategory(XmlManager.CATEGORY_ALL, false);          
         } catch (XmlException e) {
             throw new XBRLException("The BDB XML log levels could not be initialised.", e);
         }
         
         initContainer();
 
     }
 
 
     
 	private void resetConnection() throws XBRLException {
         logger.info("Doing a connection reset.");
 		close();
 	    initContainer();
         callCount = 0;
 	}
 	
 	private void incrementCallCount() throws XBRLException {
 
 	    //logger.info("Call count = " + callCount);
 	    callCount++;
         
         try {
             CheckpointConfig checkpointConfig = new CheckpointConfig();
             checkpointConfig.setKBytes(CHECKPOINT_KILOBYTES);
             environment.checkpoint(checkpointConfig);
         } catch (DatabaseException e) {
             throw new XBRLException("The checkpoint operation failed.", e);
         }
 	    
         if (callCount > this.RESET_CALL_COUNT) resetConnection();
 	}
 
 	/**
 	 * Initialises the database environment.
 	 * @throws XBRLException if the environment files are not found or 
 	 * there is a database problem.
 	 */
 	private void initEnvironment() throws XBRLException {
         try {
             EnvironmentConfig environmentConfiguration = new EnvironmentConfig();
             environmentConfiguration.setAllowCreate(true);         // If the environment does not exist, create it.
             environmentConfiguration.setInitializeLocking(true);   // Turn on the locking subsystem.
             environmentConfiguration.setErrorStream(System.err);   // Capture error information in more detail.
             environmentConfiguration.setInitializeCache(true);
             environmentConfiguration.setCacheSize(1024 * 1024 * 25);
             environmentConfiguration.setInitializeLogging(true);   // Turn off the logging subsystem.
             environmentConfiguration.setTransactional(true);       // Turn on the transactional subsystem.
             environment = new Environment(new File(locationName), environmentConfiguration);
             logger.info("Initialised the environment.");
         } catch (FileNotFoundException e) {
             throw new XBRLException("The physical location of the BDB XML database could not be found.", e);
         } catch (DatabaseException e) {
             throw new XBRLException("The BDB XML database environment could not be set up.", e);
         }	    
 	}
 	
     /**
      * Initialises the database manager
      * @throws XBRLException
      */
 	private void initManager() throws XBRLException {
 	    if (environment == null) initEnvironment();
         try {
             XmlManagerConfig managerConfiguration = new XmlManagerConfig();
             managerConfiguration.setAdoptEnvironment(true);
             managerConfiguration.setAllowExternalAccess(true);
             dataManager = new XmlManager(environment, managerConfiguration);
             logger.info("Initialised the data manager.");
         } catch (XmlException e) {
             throw new XBRLException("The Berkeley XML database manager could not be set up.", e);
         }	    
 	}
 	
     /**
      * Initialises the database container.
      * @throws XBRLException
      */
     private void initContainer() throws XBRLException {
         if (dataManager == null) initManager();
         try {
             if (dataManager.existsContainer(containerName) != 0) {
                 dataContainer = dataManager.openContainer(containerName);
             } else {
                 createContainer();
             }
             logger.info("Initialised the data container.");
         } catch (XmlException e) {
             throw new XBRLException("The database container, " + containerName + ", could not be opened.");
         }
     }
     
     private void createContainer() throws XBRLException {
         if (dataManager == null) initManager();
         try {
             dataContainer = dataManager.createContainer(containerName);
         } catch (XmlException e) {
             throw new XBRLException("The data container could not be created.", e);
         } 
         
         XmlIndexSpecification xmlIndexSpecification = null;
         XmlUpdateContext xmlUpdateContext = null;
 
         try {
 
             xmlIndexSpecification = dataContainer.getIndexSpecification();
 
            xmlIndexSpecification.addDefaultIndex("node-element-presence");
 
             xmlIndexSpecification.addIndex(Constants.XBRLAPIPrefix,"fragment","node-element-presence");
             xmlIndexSpecification.addIndex(Constants.XBRLAPIPrefix,"data","node-element-presence");
             xmlIndexSpecification.addIndex(Constants.XBRLAPIPrefix,"xptr","node-element-presence");

             
             xmlIndexSpecification.addIndex("","stub","node-attribute-presence");
 
             xmlIndexSpecification.addIndex("","index", "unique-node-attribute-equality-string");
 
             xmlIndexSpecification.addIndex("","parentIndex", "node-attribute-equality-string");
             xmlIndexSpecification.addIndex("","url", "node-attribute-equality-string");
             xmlIndexSpecification.addIndex("","type", "node-attribute-equality-string");
             xmlIndexSpecification.addIndex("","targetDocumentURL", "node-attribute-equality-string");
             xmlIndexSpecification.addIndex("","targetPointerValue", "node-attribute-equality-string");
             xmlIndexSpecification.addIndex("","absoluteHref", "node-attribute-equality-string");
             xmlIndexSpecification.addIndex("","id","node-attribute-equality-string");
             xmlIndexSpecification.addIndex("","value", "node-attribute-equality-string");
             xmlIndexSpecification.addIndex("","arcroleURI", "node-attribute-equality-string");
             xmlIndexSpecification.addIndex("","roleURI", "node-attribute-equality-string");
             xmlIndexSpecification.addIndex("","name", "node-attribute-equality-string");
             xmlIndexSpecification.addIndex("","targetNamespace", "node-attribute-equality-string");
 
             xmlIndexSpecification.addIndex(Constants.XMLNamespace,"lang","node-attribute-equality-string");
 
             xmlIndexSpecification.addIndex(Constants.XLinkNamespace,"label","node-attribute-equality-string");
             xmlIndexSpecification.addIndex(Constants.XLinkNamespace,"from","node-attribute-equality-string");
             xmlIndexSpecification.addIndex(Constants.XLinkNamespace,"to","node-attribute-equality-string");
             xmlIndexSpecification.addIndex(Constants.XLinkNamespace,"type","node-attribute-equality-string");
             xmlIndexSpecification.addIndex(Constants.XLinkNamespace,"arcrole","node-attribute-equality-string");
             xmlIndexSpecification.addIndex(Constants.XLinkNamespace,"role","node-attribute-equality-string");
             xmlIndexSpecification.addIndex(Constants.XLinkNamespace,"label","node-attribute-equality-string");
 
             xmlIndexSpecification.addIndex(Constants.XBRLAPILanguagesNamespace,"language","node-element-presence");
             xmlIndexSpecification.addIndex(Constants.XBRLAPILanguagesNamespace,"code","node-element-equality-string");
             xmlIndexSpecification.addIndex(Constants.XBRLAPILanguagesNamespace,"value","node-element-equality-string");
             xmlIndexSpecification.addIndex(Constants.XBRLAPILanguagesNamespace,"encoding","node-element-equality-string");
 
             xmlUpdateContext = dataManager.createUpdateContext();
             dataContainer.setIndexSpecification(xmlIndexSpecification,xmlUpdateContext);
             
         } catch (XmlException e) {
             throw new XBRLException("The indexes could not be configured.", e);
         } finally {
             if (xmlUpdateContext != null) xmlUpdateContext.delete();
             if (xmlIndexSpecification != null) xmlIndexSpecification.delete();
         }
         
     }        
 
 	
 
 	/**
 	 * The closure operation entails closing the XML container and the XML data manager
 	 * that are used by the store.
 	 * @see org.xbrlapi.data.Store#close()
 	 */
     synchronized public void close() throws XBRLException {
 	    super.close();
 	    closeContainer();
 	    closeManager();
 	}
 	
 
 	
 	private void closeContainer() throws XBRLException {
         if (dataContainer == null) return;
 	    if (dataManager == null) initManager();
         try {
             dataContainer.sync();
             dataContainer.close();
             dataContainer.delete();
             dataContainer = null;
             logger.info("Closed the data container.");
         } catch (XmlException e) {
             throw new XBRLException("The BDB XML database container could not be closed cleanly");
         }
 	}
 	
     private void closeManager() throws XBRLException {
         if (dataManager == null) return;
         try {
             dataManager.close();
             dataManager.delete();
             dataManager = null;
             environment = null;
             logger.info("Closed the data manager.");
         } catch (XmlException e) {
             throw new XBRLException("The BDB XML database manager could not be be closed cleanly.");
         }
     }	
 	
     private void deleteContainer() throws XBRLException {
         if (dataManager == null) initManager();
         closeContainer();
         try {
             if (dataManager.existsContainer(containerName) != 0) {
                 dataManager.removeContainer(containerName);
             }
         } catch (XmlException e) {
             throw new XBRLException("The BDB XML database container could not be deleted.");
         }
     }	
 	
 	/**
 	 * @see org.xbrlapi.data.Store#delete()
 	 */
     synchronized public void delete() throws XBRLException {
 	    deleteContainer();
 	    closeManager();
 	}
 	
 	/**
 	 * @see org.xbrlapi.data.Store#storeFragment(Fragment)
 	 */
     synchronized public void storeFragment(Fragment fragment) throws XBRLException {
 
 	    incrementCallCount();
         XmlUpdateContext xmlUpdateContext = null;
 	    try {
 	        
 	        if (fragment.getStore() != null) return;
 
 	        String index = fragment.getFragmentIndex();
 	        if (hasFragment(index)) removeFragment(index);
 
             String xml = serializeToString(fragment.getMetadataRootElement());
             xmlUpdateContext = dataManager.createUpdateContext();
             XmlDocumentConfig documentConfiguration = new XmlDocumentConfig();
             documentConfiguration.setWellFormedOnly(true);
             dataContainer.putDocument(index, xml, xmlUpdateContext, null);
 
             fragment.setResource(fragment.getBuilder().getMetadata());
             fragment.setStore(this);
 	        
         } catch (XmlException e) {
             throw new XBRLException("The fragment could not be added to the BDB XML data store.", e);
 	    } finally {
             if (xmlUpdateContext != null) xmlUpdateContext.delete();
 	    }
 	}
 
 	/**
 	 * @see org.xbrlapi.data.Store#hasFragment(String)
 	 */
 	synchronized public boolean hasFragment(String index) throws XBRLException {
 	    incrementCallCount();
         XmlDocument xmlDocument = null;
 	    try {
 			xmlDocument = dataContainer.getDocument(index);
 			return true;
         } catch (XmlException e) {
             return false;
 	    } finally {
             if (xmlDocument != null) xmlDocument.delete();
         }
 	}
 	
     /**
      * Overrides the base implementation to avoid the overhead of creating the
      * various fragment objects.
      * @see org.xbrlapi.data.Store#hasDocument(String)
      */
 	synchronized public boolean hasDocument(String url) throws XBRLException {
         XmlResults results = null;
         try {
             results = performQuery("/"+ Constants.XBRLAPIPrefix+ ":" + "fragment[@url='" + url + "' and @parentIndex='none']");
             return (results.size() == 1); 
         } catch (XmlException e) {
             throw new XBRLException("The size of the result set for the query could not be determined.", e);
         } finally {
             if (results != null) results.delete();
         }
     }    	
 
 	/**
 	 * TODO Make this a generic function
 	 * @see org.xbrlapi.data.Store#getFragment(String)
 	 */
     synchronized public Fragment getFragment(String index) throws XBRLException {
 	    this.incrementCallCount();
         XmlDocument xmlDocument = null;
         Document document = null;
 	    try {
 
 	        try {
 	            xmlDocument = dataContainer.getDocument(index);
 	        } catch (XmlException e) { // Thrown if the document is not found
 	            throw new XBRLException("The fragment " + index + " could not be retrieved from the store.",e);
 	        }
 
 	        try {
 	            document = XMLDOMBuilder.newDocument(xmlDocument.getContentAsInputStream());
 	        } catch (XmlException e) {
 	            throw new XBRLException("The fragment content is not available as an input stream.",e);
 	        }
 
     		return FragmentFactory.newFragment(this, document.getDocumentElement());
 
 	    } finally {
             if (xmlDocument != null) xmlDocument.delete();
 	    }
 	}
 
 	/**
 	 * @see org.xbrlapi.data.Store#removeFragment(String)
 	 */
     synchronized public void removeFragment(Fragment fragment) throws XBRLException {
 		removeFragment(fragment.getFragmentIndex());
 	}
 
 	/**
 	 * @see org.xbrlapi.data.Store@removeFragment(String)
 	 */
     synchronized public void removeFragment(String index) throws XBRLException {
 	    incrementCallCount();
         XmlUpdateContext xmlUpdateContext = null;
         try {
             xmlUpdateContext = dataManager.createUpdateContext();
             dataContainer.deleteDocument(index,xmlUpdateContext);
         } catch (XmlException e) {
             throw new XBRLException("The fragment removal failed.", e);
         } finally {
             if (xmlUpdateContext != null) xmlUpdateContext.delete();
         }
 	}
 
 	/**
      * TODO Make sure that queries finding a node within a fragment return the fragment itself.         
 	 * @see org.xbrlapi.data.Store#query(String)
 	 */
     @SuppressWarnings(value = "unchecked")
 	synchronized public <F extends Fragment> FragmentList<F> query(String myQuery) throws XBRLException {
         
         this.incrementCallCount();
         
         XmlResults xmlResults = null;
         XmlValue xmlValue = null;
         try {
     
             try {
     			double startTime = System.currentTimeMillis();
     
     			xmlResults = performQuery(myQuery);
     
     			Double time = new Double((System.currentTimeMillis()-startTime));
     			logger.info(time + " milliseconds for: " + myQuery);
     
                 xmlValue = xmlResults.next();
     			FragmentList<F> fragments = new FragmentListImpl<F>();
     		    while (xmlValue != null) {
     		        Document document = XMLDOMBuilder.newDocument(xmlValue.asString());
     		        xmlValue.delete();
     		        Element root = document.getDocumentElement();
     				fragments.addFragment((F) FragmentFactory.newFragment(this, root));
     		        xmlValue = xmlResults.next();
     		    }
     	    	
     			return fragments;
     
     		} catch (XmlException e) {
     			throw new XBRLException("Failed query: " + myQuery,e);
     		}
     		
         } finally {
             if (xmlValue != null) xmlValue.delete();
             if (xmlResults != null) xmlResults.delete();
         }
 	}
     
     /**
      * Provides direct access to the query mechanism so that we can use
      * queries without constructing fragments for each query result.
      * @param myQuery The query to be performed.
      * @return the results of the query.
      * @throws XBRLException if the query fails to execute.
      */
 	private XmlResults performQuery(String myQuery) throws XBRLException {
         // TODO provide a means of adding additional namespaces for querying.
         // TODO provide a means of investigating namespace bindings for the query configuration.
 	    
 	    XmlQueryContext xmlQueryContext = null;
 	    XmlQueryExpression xmlExpression = null;
 	    try {
             String query = "collection('" + dataContainer.getName() + "')" + myQuery;
             xmlQueryContext = createQueryContext();
 
 /*            xmlExpression = dataManager.prepare(myQuery,xmlQueryContext);
             logger.info(xmlExpression.getQueryPlan());
 */
             XmlResults xmlResults = dataManager.query(query,xmlQueryContext);
 			return xmlResults;
 
 		} catch (XmlException e) {
 			throw new XBRLException("Failed query: " + myQuery,e);
 		} finally {
             if (xmlQueryContext != null) xmlQueryContext.delete();
             if (xmlExpression != null) xmlExpression.delete();
 		}
     		
 	}
 	
 	/**
 	 * @return a XQuery context, prepared with namespace declarations etc.
 	 * @throws XBRLException
 	 */
 	private XmlQueryContext createQueryContext() throws XBRLException {
         XmlQueryContext xmlQueryContext = null;
         try {
             xmlQueryContext = dataManager.createQueryContext();
             xmlQueryContext.setReturnType(XmlQueryContext.DeadValues);
             xmlQueryContext.setNamespace(Constants.XLinkPrefix, Constants.XLinkNamespace);
             xmlQueryContext.setNamespace(Constants.XMLSchemaPrefix, Constants.XMLSchemaNamespace);
             xmlQueryContext.setNamespace(Constants.XBRL21Prefix, Constants.XBRL21Namespace);
             xmlQueryContext.setNamespace(Constants.XBRL21LinkPrefix, Constants.XBRL21LinkNamespace);
             xmlQueryContext.setNamespace(Constants.XBRLAPIPrefix, Constants.XBRLAPINamespace);
             xmlQueryContext.setNamespace(Constants.XBRLAPILanguagesPrefix, Constants.XBRLAPILanguagesNamespace);
             return xmlQueryContext;
         } catch (XmlException e) {
             throw new XBRLException("Failed to create query context.",e);
         }
 	}
 
 }
