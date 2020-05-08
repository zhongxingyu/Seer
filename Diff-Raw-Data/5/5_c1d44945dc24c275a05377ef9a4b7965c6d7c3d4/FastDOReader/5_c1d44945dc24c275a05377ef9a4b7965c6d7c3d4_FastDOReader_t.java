 package fedora.server.storage;
 
 import java.io.File;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Vector;
 
 import fedora.server.Context;
 import fedora.server.ReadOnlyContext;
 import fedora.server.Server;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StorageException;
 import fedora.server.errors.StorageDeviceException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.storage.types.DSBindingMapAugmented;
 import fedora.server.storage.types.ObjectMethodsDef;
 import fedora.server.storage.types.MethodDef;
 import fedora.server.storage.types.MethodParmDef;
 
 /**
  * <p>Title: FastDOReader.java</p>
  * <p>Description: Digital Object Reader that accesses objects located in the
  * "Fast" storage area. To enhance performance of disseminations, there are
  * two distinct storage areas for digital objects:
  * <ol>
  * <li>
  * "Fast" storage area - The storage area containing a subset of digital
  * objects that is optimized for performance. Both the composition of the
  * subset of objects and storage area are implementation specific. For Phase 1,
  * this object subset consists of a partial replication of the most current
  * version of each object and is used as the primary source for resolving
  * dissemination requests. The replication is partial since only information
  * required to disseminate the object is replicated in the Fast storage area.
  * For Phase 1, the Fast storage area is implemented as a relational database
  * that is accessed via JDBC. <i>Note that an appropriate definitve reader
  * should always be used to obtain the most complete information about a
  * specific object. A fast reader is used primarily for dissemination
  * requests.</i>.
  * </li>
  * <li>
  * Definitive storage area - The storage area containing complete information on
  * all digital objects in the repository. This storage area is used as the
  * authoritative source for reading complete information about a digital object.
  * This storage area is used as a secondary source for resolving dissemination
  * requests when the specified object does not exist in the Fast storage area.
  * </li>
  * </ol>
  * <p>This reader is designed to read objects from the "Fast" storage area that
  * is implemented as a relational database. If the object cannot be found in
  * the relational database, this reader will attempt to read the object
  * from the Definitive storage area using the appropriate definitive reader.
  * When the object exists in both storage areas, preference is given to the
  * Fast storage area since this reader is designed to read primarily from the
  * Fast Storage area. <code>SimpleDODOReader</code>
  * should always be used to read the authoritative version of an object.</p>
  * <i><b>Note that versioning is not implemented in Phase 1. Methods in
  * <code>FastDOReader</code> that contain arguments related to versioning date
  * such as <code>versDateTime</code> or <code>asOfDate</code> will be ignored
  * in Phase 1.</p>
  * <p></p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 public class FastDOReader implements DOReader
 {
 
   /** Database ConnectionPool instance */
   protected static ConnectionPool connectionPool = null;
 
   /** Fedora server instance */
   protected static Server s_server = null;
 
   /** Current Fedora server DOManager instance */
   protected static DOManager m_manager = null;
 
   /** Signals object found in fast storage area */
   protected boolean isFoundInFastStore = false;
 
   /** signals object found in definitive storage area */
   protected boolean isFoundInDefinitiveStore = false;
 
   /** Label of the digital object. */
   protected String doLabel = null;
 
   /** Persistent identifier of the digital object. */
   protected String PID = null;
 
   /** Instance of DOReader... used to get definitive readers. */
   protected DOReader doReader = null;
 
   /** Context for uncached objects. */
   protected static Context m_context = null;
 
   /** Make sure we have a server instance. */
   static
   {
     try
     {
       s_server =
           Server.getInstance(new File(System.getProperty("fedora.home")));
       m_manager = (DOManager) s_server.getModule(
           "fedora.server.storage.DOManager");
       HashMap h = new HashMap();
       h.put("application", "apia");
       h.put("useCachedObject", "false");
       h.put("userId", "fedoraAdmin");
       m_context = new ReadOnlyContext(h);
     } catch (InitializationException ie)
     {
       System.err.println(ie.getMessage());
     }
   }
 
   /**
    * <p>Constructs a new <code>FastDOReader</code> for the specified digital
    * object. It initializes the database connection for JDBC access to the
    * relational database and verifies existence of the specified object. If
    * the object is found, this constructor initializes the class variables for
    * <code>PID</code> and <code>doLabel</code>.
    *
    * @param context The context of this request.
    * @param objectPID The persistent identifier of the digital object.
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    */
   public FastDOReader(Context context, String objectPID) throws ServerException
   {
     try
     {
       // Get database connection pool
       ConnectionPoolManager poolManager = (ConnectionPoolManager)
           s_server.getModule("fedora.server.storage.ConnectionPoolManager");
       connectionPool = poolManager.getPool();
 
       // Attempt to find object in either Fast or Definitive store
       this.doLabel = locatePID(objectPID);
       this.PID = objectPID;
     } catch (ServerException se)
     {
       throw se;
     } catch (Throwable th)
     {
       s_server.logWarning("[FastDOReader] Unable to construct FastDOReader");
       throw new GeneralException("[FastDOReader] An error has occurred. The "
           + "error was a  \"" + th.getClass().getName() + "\"  . Reason:  \""
           + th.getMessage() + "\"  .");
     }
   }
 
   /**
    * <p>Exports the object. Since the XML representation of an object is
    * not stored in the Fast storage area, this method always queries the
    * Definitive storage area using <code>DefinitiveDOReader</code>.</p>
    *
    * @return A stream of bytes consisting of the XML-encoded representation
    *         of the digital object.
    * @throws StreamIOException If there is a problem in getting the XML input
    *         stream.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public InputStream ExportObject() throws StreamIOException, GeneralException
   {
     try
     {
       if (doReader == null)
       {
         doReader =  m_manager.getReader(m_context, PID);
       }
       return(doReader.ExportObject());
     } catch (Throwable th)
     {
       throw new GeneralException("[FastDOReader] Definitive doReader returned "
           + "error. The underlying error was a  \"" + th.getClass().getName()
           + "\"  . The message was  \"" + th.getMessage() + "\"  .");
     }
 
   }
 
   public String getContentModelId() throws ServerException {
       if (doReader==null) {
           doReader=m_manager.getReader(m_context, PID);
       }
       return doReader.getContentModelId();
   }
 
   public String getFedoraObjectType() throws ServerException {
       if (doReader==null) {
           doReader=m_manager.getReader(m_context, PID);
       }
       return doReader.getFedoraObjectType();
   }
 
   public Date getCreateDate() throws ServerException {
       if (doReader==null) {
           doReader=m_manager.getReader(m_context, PID);
       }
       return doReader.getCreateDate();
   }
 
   public Date getLastModDate() throws ServerException {
       if (doReader==null) {
           doReader=m_manager.getReader(m_context, PID);
       }
       return doReader.getLastModDate();
   }
 
   /**
    * <p>Gets a list of Behavior Definition object PIDs associated with the
    * specified digital object.</p>
    *
    * @param versDateTime The versioning datetime stamp.
    * @return An array containing a list of Behavior Definition object PIDs.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String[] GetBehaviorDefs(Date versDateTime)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     String[] behaviorDefs = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "bDef.bDefPID "
           + "FROM "
           + "bDef,"
           + "diss,"
           + "do,"
           + "doDissAssoc "
           + "WHERE "
           + "do.doDbID = doDissAssoc.doDbID AND "
           + "doDissAssoc.dissDbID = diss.dissDbID AND "
           + "bDef.bDefDbID = diss.bDefDbID AND "
           + "do.doPID=\'" + PID + "\';";
 
       s_server.logFinest("[FastDOReader] GetBehaviorDefsQuery: " + query);
       String results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           for (int i=1; i<=cols; i++)
           {
             results = new String(rs.getString(i));
           }
           queryResults.add(results);
         }
         behaviorDefs = new String[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           behaviorDefs[rowCount] = (String)e.nextElement();
           rowCount++;
         }
 
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         behaviorDefs = doReader.GetBehaviorDefs(versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] Definitive doReader returned "
             + "error. The underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     }
     return behaviorDefs;
   }
 
   /**
    * <p>Gets method parameters associated with the specified method name.</p>
    *
    * @param bDefPID The persistent identifer of Behavior Definition object.
    * @param methodName The name of the method.
    * @param versDateTime The versioning datetime stamp.
    * @return An array of method parameter definitions.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public MethodParmDef[] getObjectMethodParms(String bDefPID, String methodName,
       Date versDateTime) throws GeneralException
   {
     MethodParmDef[] methodParms = null;
     MethodParmDef methodParm = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
 
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String query =
           "SELECT DISTINCT "
           + "parmName,"
           + "parmDefaultValue,"
           + "parmDomainValues,"
           + "parmRequiredFlag,"
           + "parmLabel,"
           + "parmType "
           + " FROM "
           + "bDef,"
           + "bMech,"
           + "mechImpl,"
           + "method,"
           + "parm "
           + " WHERE "
           + "bMech.bDefDbID=parm.bDefDbID AND "
           + "method.bDefDbID=parm.bDefDbID AND "
           + "method.methodDbID=parm.methodDbID AND "
           + "bMech.bDefDbID=method.bDefDbID AND "
           + "mechImpl.methodDbID=method.methodDbID AND "
           + "bMech.bDefDbID=bDef.bDefDbID AND "
           + "bDef.bDefPID='" + bDefPID + "' AND "
           + "method.methodName='"  + methodName + "' ";
 
       s_server.logFinest("GetBMechMethodParmQuery=" + query);
       try
       {
         connection = connectionPool.getConnection();
         s_server.logFinest("connectionPool = " + connectionPool);
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
 
         // Note: a row is returned for each method parameter
         while (rs.next())
         {
           methodParm = new MethodParmDef();
           String[] results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           methodParm.parmName = results[0];
           methodParm.parmDefaultValue = results[1];
           methodParm.parmDomainValues = results[2].split(",");
          if (methodParm.parmDomainValues.length == 1 &&
              methodParm.parmDomainValues[0].equalsIgnoreCase("null"))
          {
            methodParm.parmDomainValues = new String[0];
          }
           Boolean B = new Boolean(results[3]);
           methodParm.parmRequired = B.booleanValue();
           methodParm.parmLabel = results[4];
           methodParm.parmType = results[5];
             s_server.logFinest("methodParms: " + methodParm.parmName
                 + "label: " + methodParm.parmLabel
                 + "default: " + methodParm.parmDefaultValue
                 + "required: " + methodParm.parmRequired
                 + "type: " + methodParm.parmType);
             for (int j=0; j<methodParm.parmDomainValues.length; j++)
             {
               s_server.logFinest("domain: " + methodParm.parmDomainValues[j]);
             }
           queryResults.addElement(methodParm);
         }
         methodParms = new MethodParmDef[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           methodParms[rowCount] = (MethodParmDef)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         methodParms = doReader.getObjectMethodParms(bDefPID, methodName,
             versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] Definitive doReader returned "
             + "error. The underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     }
     return methodParms;
   }
 
   /**
    * <p>Gets default method parameters associated with the specified
    * method name. Default method parameters are defined by the Behavior
    * Mechanism object as mechanism default parameters and cannot be altered
    * by the user.</p>
    *
    * @param bDefPID The persistent identifer of Behavior Definition object.
    * @param methodName The name of the method.
    * @param versDateTime The versioning datetime stamp.
    * @return An array of method parameter definitions.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
 /*
   public MethodParmDef[] GetBMechDefaultMethodParms(String bDefPID,
       String methodName, Date versDateTime) throws GeneralException
   {
     MethodParmDef[] methodParms = null;
     MethodParmDef methodParm = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
 
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String query =
           "SELECT DISTINCT "
           + "defParmName,"
           + "defParmDefaultValue,"
           + "defParmDomainValues,"
           + "defParmRequiredFlag,"
           + "defParmLabel,"
           + "defParmType "
           + " FROM "
           + "do,"
           + "bDef,"
           + "bMech,"
           + "mechImpl,"
           + "method,"
           + "mechDefParm "
           + " WHERE "
           + "bMech.bMechDbID=mechDefParm.bMechDbID AND "
           //+ "method.bDefDbID=mechDefParm.bDefDbID AND "
           + "method.methodDbID=mechDefParm.methodDbID AND "
           + "bMech.bDefDbID=method.bDefDbID AND "
           + "mechImpl.methodDbID=method.methodDbID AND "
           + "bMech.bDefDbID=bDef.bDefDbID AND "
           + "do.doPID=\'" + PID + "\' AND "
           + "bDef.bDefPID='" + bDefPID + "' AND "
           + "method.methodName='"  + methodName + "' ";
 
       s_server.logFinest("GetBMechDefaultMethodParmQuery=" + query);
       try
       {
         connection = connectionPool.getConnection();
         s_server.logFinest("connectionPool = " + connectionPool);
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
 
         // Note: a row is returned for each method parameter
         while (rs.next())
         {
           methodParm = new MethodParmDef();
           String[] results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           methodParm.parmName = results[0];
           methodParm.parmDefaultValue = results[1];
           methodParm.parmDomainValues = results[2].split(",");
           Boolean B = new Boolean(results[3]);
           methodParm.parmRequired = B.booleanValue();
           methodParm.parmLabel = results[4];
           methodParm.parmType = results[5];
             s_server.logFinest("FastDOReader:methodParms: " + methodParm.parmName
                 + "\nlabel: " + methodParm.parmLabel
                 + "\ndefault: " + methodParm.parmDefaultValue
                 + "\nrequired: " + methodParm.parmRequired
                 + "\ntype: " + methodParm.parmType);
             for (int j=0; j<methodParm.parmDomainValues.length; j++)
             {
               s_server.logFinest("FastDOReader:domainValues: "
                   + methodParm.parmDomainValues[j]);
             }
           queryResults.addElement(methodParm);
         }
         methodParms = new MethodParmDef[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           methodParms[rowCount] = (MethodParmDef)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         methodParms = doReader.GetBMechDefaultMethodParms(bDefPID, methodName,
             versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     }
     return methodParms;
   }
 */
   /**
    * <p>Gets all method defintiions associated with the specified Behavior
    * Mechanism. Note the PID of the associated Behavior Mechanism object is
    * determined via reflection based on the specified PID of the digital object
    * and the PID of its Behavior Definition object. This method retrieves the
    * list of available methods based on the assocaited Behavior Mechanism
    * object and NOT the Behavior Definition object. This is done to insure
    * that only methods that have been implemented in the mechanism are returned.
    * This distinction is only important when versioning is enabled
    * in a later release. When versioning is enabled, it is possible
    * that a versioned Behavior Definition may have methods that have not
    * yet been implemented by all of its associated Behavior Mechanisms.
    * In such a case, only those methods implemented in the mechanism
    * will be returned.</p>
    *
    * @param bDefPID The persistent identifier of Behavior Definition object.
    * @param versDateTime The versioning datetime stamp.
    * @return An array of method definitions.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public MethodDef[] getObjectMethods(String bDefPID, Date versDateTime)
       throws GeneralException
   {
     MethodDef[] methodDefs = null;
     MethodDef methodDef = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "method.methodName,"
           + "method.methodLabel,"
           + "mechImpl.addressLocation,"
           + "mechImpl.operationLocation "
           + "FROM "
           + "bDef,"
           + "diss,"
           + "method,"
           + "do,"
           + "doDissAssoc,"
           + "bMech,"
           + "mechImpl "
           + "WHERE "
           + "do.doDbID = doDissAssoc.doDbID AND "
           + "doDissAssoc.dissDbID = diss.dissDbID AND "
           + "bDef.bDefDbID = diss.bDefDbID AND "
           + "bMech.bMechDbID = diss.bMechDbID AND "
           + "bMech.bMechDbID = mechImpl.bMechDbID AND "
           + "bDef.bDefDbID = mechImpl.bDefDbID AND "
           + "method.methodDbID = mechImpl.methodDbID AND "
           + "method.bDefDbID = bDef.bDefDbID AND "
           + "bDef.bDefPID = \'" + bDefPID + "\' AND "
           + "do.doPID=\'" + PID + "\';";
 
       s_server.logFinest("getObjectMethodsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           methodDef = new MethodDef();
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           methodDef.methodName = results[0];
           methodDef.methodLabel = results[1];
           try
           {
             methodDef.methodParms = this.getObjectMethodParms(bDefPID,
                 methodDef.methodName, versDateTime);
           } catch (Throwable th)
           {
             // Failed to get method paramters
             throw new GeneralException("[FastDOReader] An error has occured. The "
                 + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
           }
           queryResults.add(methodDef);
         }
         methodDefs = new MethodDef[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           methodDefs[rowCount] = (MethodDef)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         methodDefs = doReader.getObjectMethods(bDefPID, versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] Definitive doReader returned "
             + "error. The underlying error was a  \"" + th.getClass().getName()
           + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     }
     return methodDefs;
   }
 
   /**
    * <p>Gets XML containing method definitions. Since the XML representation
    * of digital objects is not stored in the Fast storage area, this method
    * uses a <code>DOReader</code> to query the Definitive
    * storage area.</p>
    *
    * @param bDefPID The persistent identifier of Behavior Definition object.
    * @param versDateTime The versioning datetime stamp.
    * @return A stream of bytes containing XML-encoded representation of
    *         method definitions from XML in assocaited Behavior Mechanism
    *         object.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    */
   public InputStream getObjectMethodsXML(String bDefPID, Date versDateTime)
       throws GeneralException, ServerException
   {
     try
     {
       if (doReader == null)
       {
         doReader = m_manager.getReader(m_context, PID);
       }
       return doReader.getObjectMethodsXML(bDefPID, versDateTime);
     } catch (ServerException se)
     {
       throw se;
 
     } catch (Throwable th)
     {
       throw new GeneralException("[FastDOReader] Definitive doReader returned "
           + "error. The underlying error was a  \"" + th.getClass().getName()
           + "\"  . The message was  \"" + th.getMessage() + "\"  .");
     }
   }
 
   /**
    * <p>Gets a datastream specified by the datastream ID.</p>
    *
    * @param datastreamID The identifier of the requested datastream.
    * @param versDateTime The versioning datetime stamp.
    * @return The specified datastream.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public Datastream GetDatastream(String datastreamID, Date versDateTime)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     Datastream[] datastreams = null;
     Datastream datastream = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "dsBind.dsLabel,"
           + "dsBind.dsMIME,"
           + "dsBind.dsLocation "
           + "FROM "
           + "do,"
           + "dsBind "
           + "WHERE "
           + "do.doDbID = dsBind.doDbID AND "
           + "dsBind.dsID=\'" + datastreamID +"\' AND "
           + "do.doPID=\'" + PID + "\';";
 
       s_server.logFinest("GetDatastreamQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           datastream = new Datastream();
           datastream.DSLabel = results[0];
           datastream.DSMIME = results[1];
           datastream.DSLocation = results[2];
           queryResults.addElement(datastream);
         }
 
         datastreams = new Datastream[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           datastream = (Datastream)e.nextElement();
         }
       } catch (Throwable th)
       {
         // Problem with the relational database or query
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         datastream = doReader.GetDatastream(datastreamID, versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] Definitive doReader returned "
             + "error. The underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     }
     return datastream;
   }
 
   /**
    * <p>Gets all the datastreams of a digital object.</p>
    *
    * @param versDateTime The versioning datetime stamp.
    * @return An array of datastreams.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public Datastream[] GetDatastreams(Date versDateTime)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     Datastream[] datastreamArray = null;
     Datastream datastream = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "dsBind.dsLabel,"
           + "dsBind.dsMIME,"
           + "dsBind.dsLocation "
           + "FROM "
           + "do,"
           + "dsBind "
           + "WHERE "
           + "do.doDbID = dsBind.doDbID AND "
           + "do.doPID=\'" + PID + "\';";
 
       s_server.logFinest("GetDatastreamsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           datastream = new Datastream();
           datastream.DSLabel = results[0];
           datastream.DSMIME = results[1];
           datastream.DSLocation = results[2];
           queryResults.addElement(datastream);
         }
         datastreamArray = new Datastream[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           datastreamArray[rowCount] = (Datastream)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in the Definitve storage area; query
       // Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         datastreamArray = doReader.GetDatastreams(versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] Definitive doReader returned "
             + "error. The underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     }
     return datastreamArray;
   }
 
   /**
    * Gets the dissemination binding info necessary to perform a particular
    * dissemination.
    */
   public DisseminationBindingInfo[] getDisseminationBindingInfo(String bDefPID,
           String methodName, Date versDateTime)
           throws GeneralException {
     DisseminationBindingInfo dissBindInfo = null;
     DisseminationBindingInfo[] dissBindInfoArray = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String query =
           "SELECT DISTINCT "
           + "do.doPID,"
           + "bDef.bDefPID,"
           + "method.methodName,"
           + "mechImpl.addressLocation,"
           + "mechImpl.operationLocation,"
           + "mechImpl.protocolType,"
           + "dsBind.dsLocation, "
           + "dsBind.dsControlGroupType, "
           + "dsBind.dsID, "
           + "dsBind.dsCurrentVersionID, "
           + "dsBindSpec.dsBindSpecName "
           + " FROM "
           + "do,"
           + "bDef,"
           + "bMech,"
           + "dsBind,"
           + "diss,"
           + "doDissAssoc,"
           + "mechImpl,"
           + "method,"
           + "dsBindSpec "
           + " WHERE "
           + "do.doDbID=doDissAssoc.doDbID AND "
           + "doDissAssoc.dissDbID=diss.dissDbID AND "
           + "diss.bDefDbID = bDef.bDefDbID AND "
           + "diss.bMechDbID = bMech.bMechDbID AND "
           + "dsBind.doDbID = do.doDbID AND "
           + "bMech.bMechDbID = mechImpl.bMechDbID AND "
           + "mechImpl.dsBindKeyDbID = "
           + "dsBind.dsBindKeyDbID AND "
           + "dsBindSpec.dsBindKeyDbID = "
           + "mechImpl.dsBindKeyDbID AND "
           + "mechImpl.methodDbID = method.methodDbID AND "
           + "do.doPID='" + GetObjectPID() + "' AND "
           + " bDef.bDefPID=\'" + bDefPID + "\' AND "
           + " method.methodName=\'"  + methodName + "\' "
           + " ORDER BY dsBindSpec.dsBindSpecName";
 
       s_server.logFinest("GetDisseminationQuery=" + query);
 
       try
       {
         // execute database query and retrieve results
         connection = connectionPool.getConnection();
         s_server.logFinest("DisseminationConnectionPool: "+
                                      connectionPool);
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         String[] results = null;
         int cols = rsMeta.getColumnCount();
         // Note: When more than one datastream matches the DSBindingKey
         // or there are multiple DSBindingKeys associated with the method
         // in the dissemination query, multiple rows are returned.
         while (rs.next())
         {
           results = new String[cols];
           dissBindInfo = new DisseminationBindingInfo();
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           dissBindInfo.AddressLocation = results[3];
           dissBindInfo.OperationLocation = results[4];
           dissBindInfo.ProtocolType = results[5];
           dissBindInfo.dsLocation = results[6];
           dissBindInfo.dsControlGroupType = results[7];
           dissBindInfo.dsID = results[8];
           dissBindInfo.dsVersionID = results[9];
           dissBindInfo.DSBindKey = results[10];
           try
           {
             dissBindInfo.methodParms = this.getObjectMethodParms(results[1],
                 results[2], versDateTime);
           } catch (GeneralException ge)
           {
             dissBindInfo.methodParms = null;
           }
           // Add each row of returned data
           queryResults.addElement(dissBindInfo);
         }
         dissBindInfoArray = new DisseminationBindingInfo[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           dissBindInfoArray[rowCount] = (DisseminationBindingInfo)
                              e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, GetObjectPID());
         }
 
         // FIXME!! - code to perform disseminations directly from the
         // XML objects NOT implemented in Phase 1.
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] Definitive doReader returned "
             + "error. The underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     }
     return dissBindInfoArray;
   }
 
   /**
    * <p>Gets a disseminator with the specified ID.</p>
    *
    * @param disseminatorID the identifier of the requested disseminator
    * @param versDateTime versioning datetime stamp
    * @return Disseminator
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public Disseminator GetDisseminator(String disseminatorID, Date versDateTime)
       throws GeneralException
   {
     Disseminator disseminator = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "diss.dissID,"
           + "bDef.bDefPID,"
           + "bMech.bMechPID,"
           + "dsBindMap.dsBindMapID "
           + "FROM "
           + "bDef,"
           + "diss,"
           + "dsBindMap,"
           + "do,"
           + "doDissAssoc,"
           + "bMech "
           + "WHERE "
           + "do.doDbID = doDissAssoc.doDbID AND "
           + "doDissAssoc.dissDbID = diss.dissDbID AND "
           + "bDef.bDefDbID = diss.bDefDbID AND "
           + "bMech.bMechDbID = diss.bMechDbID AND "
           + "dsBindMap.bMechDbID=bMech.bMechDbID AND "
           + "diss.dissID=\'" + disseminatorID + "\' AND "
           + "do.doPID=\'" + PID + "\';";
 
       s_server.logFinest("GetDisseminatorQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           disseminator = new Disseminator();
           disseminator.dissID = results[0];
           disseminator.bDefID = results[1];
           disseminator.bMechID = results[2];
           disseminator.dsBindMapID = results[3];
         }
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         disseminator = doReader.GetDisseminator(disseminatorID, versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] Definitive doReader returned "
             + "error. The underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     }
     return disseminator;
   }
 
   /**
    * <p>Gets all disseminators of the specified object.</p>
    *
    * @param versDateTime versioning datetime stamp
    * @return Disseminator[] array of disseminators
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public Disseminator[] GetDisseminators(Date versDateTime)
       throws GeneralException
   {
     Disseminator[] disseminatorArray = null;
     Disseminator disseminator = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in the Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "diss.dissID,"
           + "bDef.bDefPID,"
           + "bMech.bMechPID,"
           + "dsBindMap.dsBindMapID "
           + "FROM "
           + "bDef,"
           + "diss,"
           + "dsBindMap,"
           + "do,"
           + "doDissAssoc,"
           + "bMech "
           + "WHERE "
           + "do.doDbID = doDissAssoc.doDbID AND "
           + "doDissAssoc.dissDbID = diss.dissDbID AND "
           + "bDef.bDefDbID = diss.bDefDbID AND "
           + "bMech.bMechDbID = diss.bMechDbID AND "
           + "dsBindMap.bMechDbID=bMech.bMechDbID AND "
           + "do.doPID=\'" + PID + "\';";
 
       s_server.logFinest("GetDisseminatorsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           disseminator = new Disseminator();
           disseminator.dissID = results[0];
           disseminator.bDefID = results[1];
           disseminator.bMechID = results[2];
           disseminator.dsBindMapID = results[3];
           queryResults.addElement(disseminator);
         }
         disseminatorArray = new Disseminator[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           disseminatorArray[rowCount] = (Disseminator)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         disseminatorArray = doReader.GetDisseminators(versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] Definitive doReader returned "
             + "error. The underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     }
     return disseminatorArray;
   }
 
   /**
    * <p>Gets datastream binding map.</p>
    *
    * @param versDateTime versioning datetime stamp
    * @return DSBindingMapAugmented[] array of datastream binding maps
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public DSBindingMapAugmented[] GetDSBindingMaps(Date versDateTime)
       throws GeneralException
   {
     try
     {
       if (doReader == null)
       {
         doReader =  m_manager.getReader(m_context, PID);
       }
       return doReader.GetDSBindingMaps(versDateTime);
     } catch (Throwable th)
     {
       throw new GeneralException("[FastDOReader] Definitive doReader returned "
           + "error. The underlying error was a  \"" + th.getClass().getName()
           + "\"  . The message was  \"" + th.getMessage() + "\"  .");
     }
   }
 
   /**
    * <p>Gets the label of the requested object.</p>
    *
    * @return String contining the object label
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String GetObjectLabel() throws GeneralException
   {
     s_server.logFinest("GetObjectLabel = " + doLabel);
     return doLabel;
   }
 
   /**
    * <p>Gets all methods associated with the specified digital object. If the
    * object is found, an array of <code>ObjectMethodsDef</code> is returned.
    * If the object cannot be found in the relational database, the method
    * attempts to find the object in the Definitive storage area. If the object
    * cannot be found, <code>ObjectNotFoundException</code> is thrown.</p>
    *
    * @param PID persistent identifier for the digital object
    * @param versDateTime The versioning datetime stamp.
    * @return ObjectMethodsDef containing all object methods
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public ObjectMethodsDef[] getObjectMethods(Date versDateTime)
       throws GeneralException
   {
     ObjectMethodsDef[] objectMethodsDefArray = null;
     ObjectMethodsDef objectMethodsDef = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
 
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "do.doPID,"
           + "bDef.bDefPID,"
           + "method.methodName "
           + "FROM "
           + "bDef,"
           + "diss,"
           + "method,"
           + "do,"
           + "doDissAssoc,"
           + "bMech,"
           + "mechImpl "
           + "WHERE "
           + "do.doDbID = doDissAssoc.doDbID AND "
           + "doDissAssoc.dissDbID = diss.dissDbID AND "
           + "bDef.bDefDbID = diss.bDefDbID AND "
           + "bMech.bMechDbID = diss.bMechDbID AND "
           + "bMech.bMechDbID = mechImpl.bMechDbID AND "
           + "bDef.bDefDbID = mechImpl.bDefDbID AND "
           + "method.methodDbID = mechImpl.methodDbID AND "
           + "do.doPID=\'" + GetObjectPID() + "\' "
           + "ORDER BY bDef.bDefPID, method.methodName;";
 
       s_server.logFinest("getObjectMethodsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           objectMethodsDef = new ObjectMethodsDef();
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           objectMethodsDef.PID = results[0];
           objectMethodsDef.bDefPID = results[1];
           objectMethodsDef.methodName = results[2];
           MethodParmDef[] methodParms = getObjectMethodParms(results[1],
               results[2], versDateTime);
           objectMethodsDef.methodParmDefs = methodParms;
           queryResults.add(objectMethodsDef);
         }
         objectMethodsDefArray = new ObjectMethodsDef[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           objectMethodsDefArray[rowCount] = (ObjectMethodsDef)e.nextElement();
           rowCount++;
         }
      } catch (Throwable th)
      {
        throw new GeneralException("[FastDOReader] An error has occured. The "
            + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
      } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitve storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, GetObjectPID());
         }
         String[] behaviorDefs = doReader.GetBehaviorDefs(versDateTime);
         Vector results = new Vector();
         for (int i=0; i<behaviorDefs.length; i++)
         {
           MethodDef[] methodDefs = doReader.getObjectMethods(behaviorDefs[i],
                                    versDateTime);
           // FIXME!! Behavior Mechanism and Behavior Definition
           // objects cannot currently be disseminated because the code
           // to implement this in the definitive readers has not been
           // implemented. Method getObjectMethods returns null for Behavior
           // Mechanism and Behavior Definition objects which gets trapped
           // here.
           if(methodDefs == null)
           {
             throw new GeneralException("[FastDOReader] The object: "
                 + GetObjectPID() + " is not a "
                 + "data object. Behavior Definition and Behavior Mechanism "
                 + "objects cannot be disseminated in the current release.");
           }
           for (int j=0; j<methodDefs.length; j++)
           {
             objectMethodsDef = new ObjectMethodsDef();
             objectMethodsDef.PID = GetObjectPID();
             objectMethodsDef.bDefPID = behaviorDefs[i];
             objectMethodsDef.methodName = methodDefs[j].methodName;
             System.out.println("methodName: "+methodDefs[j].methodName);
             System.out.println("CALL: bdef: "+behaviorDefs[i]+"methodefs: "+methodDefs[i].methodName);
             objectMethodsDef.methodParmDefs = doReader.getObjectMethodParms(behaviorDefs[i], methodDefs[i].methodName, versDateTime);
             objectMethodsDef.asOfDate = versDateTime;
             results.addElement(objectMethodsDef);
           }
         }
         int rowCount = 0;
         objectMethodsDefArray = new ObjectMethodsDef[results.size()];
         for (Enumeration e = results.elements(); e.hasMoreElements();)
         {
           objectMethodsDefArray[rowCount] = (ObjectMethodsDef)e.nextElement();
           rowCount++;
         }
         return objectMethodsDefArray;
       } catch (Throwable th)
       {
         th.printStackTrace();
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     }
     return objectMethodsDefArray;
   }
 
   /**
    * <p>Gets the persistent identifier or PID of the digital object.</p>
    *
    * @return String containing the persistent identifier
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String GetObjectPID() throws GeneralException
   {
     s_server.logFinest("GetObjectPID = " + PID);
     return this.PID;
   }
 
   /**
    * <p>Gets the state on a digital object</p>
    *
    * @return String state of the object
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String GetObjectState() throws GeneralException
   {
     try
     {
       if (doReader == null)
       {
         doReader =  m_manager.getReader(m_context, PID);
       }
       return doReader.GetObjectState();
     } catch (Throwable th)
     {
       throw new GeneralException("[FastDOReader] Definitive doReader returned "
           + "error. The underlying error was a  \"" + th.getClass().getName()
           + "\"  . The message was  \"" + th.getMessage() + "\"  .");
     }
   }
 
   public String getLockingUser()
           throws ServerException, StorageDeviceException, ObjectNotFoundException {
       if (doReader==null) {
           doReader=m_manager.getReader(m_context, PID);
       }
       return doReader.getLockingUser();
   }
 
   /**
    * <p>Gets the XML representation of the object. Since the XML representation
    * of an object is not stored in the Fast storage area, this method always
    * queries the Definitive storage area using <code>DefinitveDOReader</code>.
    * </p>
    *
    * @return String containing the XML representation of the object.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    * @throws StreamIOException If there was a failure in accessing the object
    *         for any IO reason during retrieval of the object from low-level
    *         storage. Extends ServerException.
    */
   public InputStream GetObjectXML()
       throws StreamIOException, GeneralException
   {
     try
     {
       if (doReader == null)
       {
         doReader = m_manager.getReader(m_context, PID);
       }
       return(doReader.GetObjectXML());
     } catch (Throwable th)
     {
       throw new GeneralException("[FastDOReader] Definitive doReader returned "
           + "error. The underlying error was a  \"" + th.getClass().getName()
           + "\"  . The message was  \"" + th.getMessage() + "\"  .");
     }
   }
 
   /**
    * <p>Lists the datastream IDs of the requested object having the
    * specified <code>state</code>. Note that the Fast storage area does NOT
    * contain state information so state is ignored when querying the Fast
    * storage area. <code>DefinitiveDOReader</code> should be used instead
    * to list datastream IDs with a given state.</p>
    *
    * @param state State of the datastreams.
    * @return An array containing the datastream IDs.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String[] ListDatastreamIDs(String state)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     String[] datastreamIDs = null;
     Datastream datastream = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database.
       String  query =
           "SELECT DISTINCT "
           + "dsBind.dsID "
           + "FROM "
           + "do,"
           + "dsBind "
           + "WHERE "
           + "do.doDbID = dsBind.doDbID AND "
           + "do.doPID=\'" + PID + "\';";
 
       s_server.logFinest("ListDatastreamIDsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           datastream = new Datastream();
           datastream.DatastreamID = results[0];
           queryResults.addElement(datastream);
         }
         datastreamIDs = new String[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           datastreamIDs[rowCount] = (String)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         datastreamIDs = doReader.ListDatastreamIDs("");
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] Definitive doReader returned "
             + "error. The underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     }
     return datastreamIDs;
   }
 
   /**
    * Gets a list of disseminator IDs. Note that the Fast storage area does
    * not contain state information and state is ignored.
    * <code>DefinitiveDOReader</code> should be used to list disseminator IDs
    * when state is specified.
    *
    * @param state State of the disseminators.
    * @return An array listing disseminator IDs.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String[] ListDisseminatorIDs(String state)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     Disseminator disseminator = null;
     String[] disseminatorIDs = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "diss.dissID "
           + "FROM "
           + "diss,"
           + "do,"
           + "doDissAssoc "
           + "WHERE "
           + "do.doDbID = doDissAssoc.doDbID AND "
           + "doDissAssoc.dissDbID = diss.dissDbID AND "
           + "do.doPID=\'" + PID + "\';";
 
       s_server.logFinest("ListDisseminatorIDsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           disseminator = new Disseminator();
           disseminator.dissID = results[0];
           queryResults.addElement(disseminator);
         }
         disseminatorIDs = new String[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           disseminator = (Disseminator)e.nextElement();
           disseminatorIDs[rowCount] = disseminator.dissID;
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] An error has occured. The "
             + "underlying error was a  \"" + th.getClass().getName()
             + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         disseminatorIDs = doReader.ListDisseminatorIDs("A");
       } catch (Throwable th)
       {
         throw new GeneralException("[FastDOReader] Definitive doReader returned "
             + "error. The underlying error was a  \"" + th.getClass().getName()
           + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     }
     return disseminatorIDs;
   }
 
   /**
    * <p>Locates the specified digital object using its persitent identifier.
    * This method will first attempt to locate the object in the Fast storage
    * area. If the the object cannot be located there, it will then try to find
    * it in the Definitive strorage area. If the object is found, the object's
    * label is returned. Otherwise, it throws
    * <code>ObjectNotFoundException</code>.</p>
    *
    * @param PID persistent identifier of the digital object.
    * @return String containing label of the specified digital object.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    */
   public String locatePID(String PID) throws GeneralException, ServerException
   {
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     String  query =
         "SELECT "
         + "do.doLabel "
         + "FROM "
         + "do "
         + "WHERE "
         + "do.doPID=\'" + PID + "\';";
     s_server.logFinest("LocatPIDQuery: " + query);
 
     try
     {
       connection = connectionPool.getConnection();
       s_server.logFinest("LocatePIDConnectionPool: "
                                    + connectionPool);
       statement = connection.createStatement();
       rs = statement.executeQuery(query);
       while (rs.next())
       {
         doLabel = rs.getString(1);
       }
     } catch (Throwable th)
     {
       throw new GeneralException("[FastDOReader] An error has occured. The "
           + "underlying error was a  \"" + th.getClass().getName()
           + "\"  . The message was  \"" + th.getMessage() + "\"  .");
     } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("[FastDOReader] Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     if (doLabel == null || doLabel.equalsIgnoreCase(""))
     {
       // Empty result means that the digital object could not be found in the
       // relational database. This could be due to incorrectly specified
       // parameter for PID OR the object is not in the relational database.
       // If not in the relational database, attempt to find the object in the
       // Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         doLabel = doReader.GetObjectLabel();
         isFoundInDefinitiveStore = true;
         s_server.logFinest("OBJECT FOUND IN DEFINITIVE STORE: " + PID);
       } catch (ServerException se)
       {
         throw se;
       } catch (Throwable th)
       {
         s_server.logWarning("OBJECT NOT FOUND IN DEFINITIVE STORE: " + PID);
         throw new GeneralException("[FastDOReader] Definitive doReader returned "
             + "error. The underlying error was a  \"" + th.getClass().getName()
           + "\"  . The message was  \"" + th.getMessage() + "\"  .");
       }
     } else
     {
       isFoundInFastStore = true;
       s_server.logFinest("OBJECT FOUND IN FAST STORE: " + PID);
     }
     return doLabel;
   }
 }
