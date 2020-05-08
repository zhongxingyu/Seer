 /**
  * JAFER Toolkit Project. Copyright (C) 2002, JAFER Toolkit Project, Oxford
  * University. This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of the License,
  * or (at your option) any later version. This library is distributed in the
  * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
  * the GNU Lesser General Public License for more details. You should have
  * received a copy of the GNU Lesser General Public License along with this
  * library; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package org.jafer.srwserver;
 
 import gov.loc.www.zing.cql.xcql.OperandType;
 import gov.loc.www.zing.srw.DiagnosticsType;
 import gov.loc.www.zing.srw.EchoedSearchRetrieveRequestType;
 import gov.loc.www.zing.srw.RecordType;
 import gov.loc.www.zing.srw.RecordsType;
 import gov.loc.www.zing.srw.SearchRetrieveRequestType;
 import gov.loc.www.zing.srw.SearchRetrieveResponseType;
 import gov.loc.www.zing.srw.StringOrXmlFragment;
 import gov.loc.www.zing.srw.diagnostic.DiagnosticType;
 
 import java.io.StringWriter;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import org.apache.axis.message.MessageElement;
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.axis.types.PositiveInteger;
 import org.apache.axis.types.URI;
 import org.apache.axis.types.URI.MalformedURIException;
 import org.apache.xerces.dom.TextImpl;
 import org.jafer.databeans.DatabeanManager;
 import org.jafer.databeans.DatabeanManagerFactory;
 import org.jafer.databeans.DatabeanManagerFactoryConfig;
 import org.jafer.exception.JaferException;
 import org.jafer.query.CQLQuery;
 import org.jafer.query.JaferQuery;
 import org.jafer.query.QueryException;
 import org.jafer.record.Field;
 import org.jafer.util.xml.XMLSerializer;
 import org.w3c.dom.Element;
 
 /**
  * This class services SRW requests
  */
 public class SRWServer implements gov.loc.www.zing.srw.interfaces.SRWPort
 {
 
     /**
      * Stores a reference to the logger
      */
     protected static Logger logger = Logger.getLogger("org.jafer.srwserver");
 
     /**
      * Stores a reference to the data bean manager factory to use
      */
     private DatabeanManagerFactory databeanManagerFactory = null;
 
     /**
      * Stores a reference to server configuration information
      */
     private SRWServerConfig serverConfig = null;
 
     /**
      * The SRWServer is constructed with the databean manager factory that
      * creates a databean manager to process all the search requests. The
      * databean manager will be configured using the config file
      * 
      * @param srwserverConfigLocation The location of the resource in the
      *        distribution that will be retrieved as a stream using
      *        class.getResourceAsStream() to load the srwserver config details
      * @param databeanManagerConfigLocation The location of the resource in the
      *        distribution that will be retrieved as a stream using
      *        class.getResourceAsStream() to load the databeanmanager config
      *        details
      * @throws JaferException
      */
     public SRWServer(String srwserverConfigLocation, String databeanManagerConfigLocation) throws JaferException
     {
         try
         {
             serverConfig = new SRWServerConfig();
             serverConfig.initialiseFromResourceStream(srwserverConfigLocation);
             DatabeanManagerFactoryConfig config = new DatabeanManagerFactoryConfig();
             config.initialiseFromResourceStream(databeanManagerConfigLocation);
             databeanManagerFactory = config.getDatabeanManagerFactory();
         }
         catch (JaferException exc)
         {
             String msg = "Unable to configure SRWServer: ";
             logger.severe(msg + exc);
             throw new JaferException(msg, exc);
         }
     }
 
     /**
      * The SRWServer is constructed with the databean manager factory that
      * creates a databean manager to process all the search requests. The
      * databean manager will be configured using the supplied factory rather
      * than the config file
      * 
      * @param srwserverConfigLocation The location of the resource in the
      *        distribution that will be retrieved as a stream using
      *        class.getResourceAsStream()
      * @param factory The databean manager factory to use
      * @throws JaferException
      */
     public SRWServer(String srwserverConfigLocation, DatabeanManagerFactory factory) throws JaferException
     {
         try
         {
             serverConfig = new SRWServerConfig();
             serverConfig.initialiseFromResourceStream(srwserverConfigLocation);
             // make sure we have a factory to use
             if (factory == null)
             {
                 throw new JaferException("You must supply a DatabeanManagerFactory to configure the SRWSever with");
             }
             databeanManagerFactory = factory;
         }
         catch (JaferException exc)
         {
             String msg = "Unable to configure SRWServer: ";
             logger.severe(msg + exc);
             throw new JaferException(msg, exc);
         }
     }
 
     /**
      * The SRWServer is constructed with the databean manager factory that
      * creates a databean manager to process all the search requests. The
      * databean manager will be configured using the supplied factory rather
      * than the config file
      * 
      * @param srwserverConfig The srw server config class
      * @param factoryConfig The databean factory config class
      * @throws JaferException
      */
     public SRWServer(SRWServerConfig srwserverConfig, DatabeanManagerFactoryConfig factoryConfig) throws JaferException
     {
         try
         {
             serverConfig = srwserverConfig;
             databeanManagerFactory = factoryConfig.getDatabeanManagerFactory();
             // make sure we have a factory to use
             if (databeanManagerFactory == null)
             {
                 throw new JaferException("You must supply a valid DatabeanManagerFactoryConfig to configure the SRWSever with");
            }
         }
         catch (JaferException exc)
         {
             String msg = "Unable to configure SRWServer: ";
             logger.severe(msg + exc);
             throw new JaferException(msg, exc);
         }
     }
 
     /**
      * This method creates a DiagnosticType for the information provided
      * 
      * @param code the diagnostic lookup code
      * @param details The diagnotic details
      * @return The created DiagnosticType
      * @throws MalformedURIException
      */
     private DiagnosticType createDiagnostic(String code, String details) throws MalformedURIException
     {
         // create the diagnostic URI using the code
         URI uri = new URI("info:srw/diagnostic/1/" + code);
         String message;
         try
         {
             message = serverConfig.getDiagnosticMessaage(code);
         }
         catch (JaferException exc)
         {
             logger.warning("Unable to find diagnostic message for code: " + code);
             message = "UNABLE TO FIND MESSAGE";
         }
         return new DiagnosticType(details, message, uri);
     }
 
     /**
      * This method makes sure that the request contains all the required
      * parameters. If it fails the response will be populated with the
      * appropriate diagnostic
      * 
      * @param request The SearchRetrieveRequestType request message
      * @param response The SearchRetrieveResponseType response message
      * @return true if the request is valid otherwise false and diagnostics
      *         added to response
      * @throws MalformedURIException
      * @throws JaferException
      */
     private boolean validateSearchRetrieveRequest(SearchRetrieveRequestType request, SearchRetrieveResponseType response)
             throws MalformedURIException, JaferException
     {
         logger.fine("Validating request");
         ArrayList diagnostics = new ArrayList();
 
         // check mandatory parameters and that version is valid
         if (request.getVersion() == null || request.getVersion().length() == 0
                 || Double.parseDouble(request.getVersion()) > serverConfig.getHighestSupportedSearchVersion())
         {
             diagnostics.add(createDiagnostic("5", Double.toString(serverConfig.getHighestSupportedSearchVersion())));
             // set version to empty string so response will serialise only if
             // not set in the request
             if (request.getVersion() == null)
             {
                 response.setVersion("");
                 response.getEchoedSearchRetrieveRequest().setVersion("");
             }
         }
         if (request.getQuery() == null || request.getQuery().length() == 0)
         {
             diagnostics.add(createDiagnostic("7", "Query"));
             // set query to empty string so response will serialise
             response.getEchoedSearchRetrieveRequest().setQuery("");
         }
 
         // add the diagnostics to the response
         response.setDiagnostics(new DiagnosticsType(
                 (DiagnosticType[]) diagnostics.toArray(new DiagnosticType[diagnostics.size()])));
 
         logger.fine("Assigning any default values to request when not set");
 
         if (request.getStartRecord() == null)
         {
             // standards state this defaults to 1 when not set
             request.setStartRecord(new PositiveInteger("1"));
         }
         if (request.getMaximumRecords() == null)
         {
             // standards state this defaults to server config when not set
             request.setMaximumRecords(new NonNegativeInteger(serverConfig.getDefaultMaxRecords()));
         }
 
         logger.fine("Request Valid: " + diagnostics.isEmpty());
         // validated succesfully if diagnostics array is empty
         return diagnostics.isEmpty();
     }
 
     /**
      * This method creates a basic response object from the request to be
      * populated with the results of the search and retrieve operaration
      * 
      * @param request The request object that must be replecated in the response
      * @return A basic SearchRetrieveResponseType object that contains the
      *         replecated request information
      */
     private gov.loc.www.zing.srw.SearchRetrieveResponseType createBasicSearchRetrieveResponse(
             gov.loc.www.zing.srw.SearchRetrieveRequestType request)
     {
         logger.fine("Creating basic search and retrieve response from request");
 
         SearchRetrieveResponseType response = new SearchRetrieveResponseType();
 
         // create the echoed request object
         EchoedSearchRetrieveRequestType echoedRequest = new EchoedSearchRetrieveRequestType();
 
         // copy the basic information over
         echoedRequest.setVersion(request.getVersion());
         echoedRequest.setStylesheet(request.getStylesheet());
         echoedRequest.setStartRecord(request.getStartRecord());
         echoedRequest.setSortKeys(request.getSortKeys());
         echoedRequest.setResultSetTTL(request.getResultSetTTL());
         echoedRequest.setRecordXPath(request.getRecordXPath());
         echoedRequest.setRecordSchema(request.getRecordSchema());
         echoedRequest.setRecordPacking(request.getRecordPacking());
         echoedRequest.setQuery(request.getQuery());
         echoedRequest.setMaximumRecords(request.getMaximumRecords());
         echoedRequest.setExtraRequestData(request.getExtraRequestData());
         echoedRequest.setXQuery(new OperandType());
 
         // For now the setting of the XQuery (XCQL) information will not be set
         response.setEchoedSearchRetrieveRequest(echoedRequest);
         response.setVersion(request.getVersion());
         response.setNumberOfRecords(new NonNegativeInteger("0"));
 
         logger.fine("Built search and retrieve response");
         return response;
     }
 
     /**
      * This method adds the requested results to the response according to the
      * request information for startrecord, maxrecords, recordpacking values
      * 
      * @param request The SearchRetrieveRequestType request message
      * @param response The SearchRetrieveResponseType response message
      * @param beanManager The bean manager to retrieve results from
      * @throws JaferException
      */
     private void addReplyRecordsToResponse(SearchRetrieveRequestType request, SearchRetrieveResponseType response,
             DatabeanManager beanManager) throws JaferException
     {
         logger.fine("Processing search and retrieve results into response ");
         try
         {
             // extract condition values required for loop
             int startRecord = request.getStartRecord().intValue();
             int maxRecords = request.getMaximumRecords().intValue();
             int numberOfResults = beanManager.getNumberOfResults();
 
             // holds all the records to be added
             RecordType[] records = new RecordType[maxRecords];
 
             // loop round retrieving and adding all the records from the start
             // record through to either the last result or until the maximum
             // number
             // of requested records has been reached
             for (int index = startRecord; index <= numberOfResults && index < startRecord + maxRecords; index++)
             {
                 logger.fine("Processing record " + index);
 
                 // create basic record Type
                 RecordType record = new RecordType();
                 record.setRecordSchema(request.getRecordSchema());
                 record.setRecordPacking(request.getRecordPacking());
                 record.setRecordPosition(new PositiveInteger(Integer.toString(index)));
 
                 beanManager.setRecordCursor(index);
                 Field field = beanManager.getCurrentRecord();
                 // make sure the record was retrieved
                 if (field == null)
                 {
                     throw new JaferException("Unable to read record " + (startRecord + index));
                 }
 
                 MessageElement element = null;
 
                 // are we adding the record as a string representation or as XML
                 if (request.getRecordPacking().equalsIgnoreCase("string"))
                 {
                     // convert the xml to a string ommiting XML header
                     StringWriter writer = new StringWriter();
                     XMLSerializer.out(field.getXML(), true, writer);
                     writer.flush();
 
                     // MAY NEED TO ENCODE THE XML STRING HERE BEFORE ADDING
                     // TO MESSAGE ELEMENT. TESTING SO FAR HAS NOT PROVED THIS TO
                     // BE REQUIRED
 
                     // add XML as the string node value for the message element
                     TextImpl data = new TextImpl();
                     data.replaceData(writer.toString());
                     element = new MessageElement(data);
                 }
                 else
                 {
                     element = new MessageElement((Element) field.getXML());
                     // add the XML in as a NODE to the message element
                     // element.appendChild(field.getXML());
                 }
 
                 // add the message Element into response
                 record.setRecordData(new StringOrXmlFragment(new MessageElement[] { element }));
 
                 // add the record to the array
                 records[index - startRecord] = record;
             }
 
             // add the records to the response
             response.setRecords(new RecordsType(records));
         }
         finally
         {
             logger.fine("Search and retrieve results added to response");
         }
     }
 
     /**
      * This method processes a search and retrieve operation
      * 
      * @param request The search and retrieve request message
      * @return The search and retrieve response message object
      * @throws RemoteException
      */
     public gov.loc.www.zing.srw.SearchRetrieveResponseType searchRetrieveOperation(
             gov.loc.www.zing.srw.SearchRetrieveRequestType request) throws java.rmi.RemoteException
     {
         // QUESTION - WILL THIS BE CALLED SYCHRONOUSLY BY AXIS OR DO WE NEED TO
         // SYNCHRONISE THE CALL AS WE REFERENCE A PRIVATE MEMEBER
 
         logger.fine("Processing search and retrieve request");
         DatabeanManager beanManager = null;
 
         SearchRetrieveResponseType response = createBasicSearchRetrieveResponse(request);
         try
         {
             try
             {
                 // make sure the request is valid
                 if (validateSearchRetrieveRequest(request, response))
                 {
                     // make sure we have a record schema set otherwise use the
                     // default schema of this SRWServer instance
                     if (request.getRecordSchema() == null || request.getRecordSchema().length() == 0)
                     {
                         request.setRecordSchema(serverConfig.getDefaultSchema());
                     }
 
                     // create the databean to perform the search
                     beanManager = (DatabeanManager) databeanManagerFactory.getDatabean();
                     beanManager.setRecordSchema(request.getRecordSchema());
 
                     logger.fine("Converting CQL query to JaferQuery");
                     // take the request query and convert it to a JAFERQuery
                     JaferQuery query = new CQLQuery(request.getQuery()).toJaferQuery();
 
                     logger.fine("Submitting jafer query to databeanManager");
                     int numberOfResults = beanManager.submitQuery(query.getQuery());
                     logger.fine("Found " + numberOfResults + " of results");
 
                     // populate the response message from results
                     response.setNumberOfRecords(new NonNegativeInteger(Integer.toString(numberOfResults)));
 
                     // did we get any search exceptions for the databases
                     String[] databases = beanManager.getAllDatabases();
                     JaferException[] exceptions = beanManager.getSearchException(databases);
 
                     logger.fine("Adding any search diagnostics");
                     // add each exception as a diagnostic message
                     ArrayList diagnostics = new ArrayList();
                     for (int index = 0; index < exceptions.length; index++)
                     {
                         // only add the diagnotic if the search exception is not
                         // null
                         if (exceptions[index] != null)
                         {
                             diagnostics.add(createDiagnostic("1", databases[index] + ":" + exceptions[index].getMessage()));
                         }
                     }
 
                     // add the diagnostics to the response
                     response.setDiagnostics(new DiagnosticsType(
                             (DiagnosticType[]) diagnostics.toArray(new DiagnosticType[diagnostics.size()])));
 
                     // calculate the next position
                     int nextPosition = request.getStartRecord().intValue() + request.getMaximumRecords().intValue();
                     // only set the next postion if it's going to be in the
                     // result set
                     if (nextPosition <= numberOfResults)
                     {
                         response.setNextRecordPosition(new PositiveInteger(Integer.toString(nextPosition)));
                     }
 
                     if (numberOfResults > 0)
                     {
                         // add the reply records to the response
                         addReplyRecordsToResponse(request, response, beanManager);
                     }
                 }
             }
             catch (QueryException exc)
             {
                // make sure we return no records now
                response.setNumberOfRecords(new NonNegativeInteger("0"));
                 logger.severe("QueryException performing search: " + exc);
                 DiagnosticType diagnostic = createDiagnostic("1", exc.getMessage());
                 // add the diagnostics to the response
                 response.setDiagnostics(new DiagnosticsType(new DiagnosticType[] { diagnostic }));
             }
             catch (JaferException exc)
             {
                // make sure we return no records now
                response.setNumberOfRecords(new NonNegativeInteger("0"));
                 logger.severe("JaferException performing search: " + exc);
                 DiagnosticType diagnostic = createDiagnostic("1", exc.getMessage());
                 // add the diagnostics to the response
                 response.setDiagnostics(new DiagnosticsType(new DiagnosticType[] { diagnostic }));
             }
         }
         catch (Exception exc)
         {
             // this should not ever occur so if it does throw a remote exception
             logger.severe("Exception performing search: " + exc);
             throw new RemoteException(exc.getMessage());
         }
         finally
         {
             if (beanManager != null)
             {
                 // stop any auto populating of cache as we have completed and
                 // will no longer need any more records from the cache
                 beanManager.stopAutoPopulateCache();
             }
             logger.fine("Processed search and retrieve request");
         }
         return response;
     }
 
     /**
      * This method processes a scan operation. Currently not supported by this
      * Server class
      * 
      * @param body The scan request message
      * @return The scan response message object
      * @throws RemoteException
      */
     public gov.loc.www.zing.srw.ScanResponseType scanOperation(gov.loc.www.zing.srw.ScanRequestType body)
             throws java.rmi.RemoteException
     {
         throw new UnsupportedOperationException();
     }
 }
