 package fedora.server.storage;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.Vector;
 
 import fedora.server.Server;
 import fedora.server.errors.ConnectionPoolNotFoundException;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.errors.MethodNotFoundException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.storage.ConnectionPoolManagerImpl;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.storage.types.DSBindingMapAugmented;
 import fedora.server.storage.types.ObjectMethodsDef;
 import fedora.server.storage.types.MethodDef;
 import fedora.server.storage.types.MethodParmDef;
 import fedora.server.utilities.DateUtility;
 
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
  * Fast Storage area. <code>DefinitiveDOReader</code>,
  * <code>DefinitveBMechReader</code>, or <code>DefinitiveBDefReader</code>
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
 public class FastDOReader implements DisseminatingDOReader
 {
   private static boolean debug;
   private static ConnectionPool connectionPool = null;
   private static Server s_server = null;
   private boolean isFoundInFastStore = false;
   private boolean isFoundInDefinitiveStore = false;
   private String doLabel = null;
   private String PID = null;
   private DefinitiveDOReader doReader = null;
   private DefinitiveBMechReader bMechReader = null;
 
   static
   {
     try
     {
       s_server=Server.getInstance(new File(System.getProperty("fedora.home")));
       Boolean B1 = new Boolean(s_server.getParameter("debug"));
       debug = B1.booleanValue();
       System.out.println("Debug: "+debug);
     } catch (InitializationException ie) {
       System.err.println(ie.getMessage());
     }
   }
 
   /**
    * <p>Constructs a new <code>FastDOReader</code> for the specified digital
    * object. It initializes the database connection for JDBC access to the
    * relational database and verifies existence of the specified object. If
    * the object is found, this constructor initializes the class variables for
    * <code>PID</code> and <code>doLabel</code>. If the specified object cannot
    * be found, <code>ObjectNotFoundException</code> is thrown.</p>
    *
    * @param objectPID The persistent identifier of the digital object.
    * @throws ObjectNotFoundException If the digital object cannot be found.
    */
   public FastDOReader(String objectPID) throws ServerException
   {
     try
     {
       // Get database connection pool
       ConnectionPoolManager poolManager =
           (ConnectionPoolManager)s_server.
           getModule("fedora.server.storage.ConnectionPoolManager");
       connectionPool = poolManager.getPool();
 
       // Attempt to find object in either Fast or Definitive store
       this.doLabel = locatePID(objectPID);
       this.PID = objectPID;
 
     } catch (ConnectionPoolNotFoundException cpnfe)
     {
       throw new ObjectNotFoundException("Object not found: " +
                                         objectPID + "\n" +
                                         cpnfe.getMessage() + "\n");
     } catch (Exception e)
     {
       // FIXME!! - Decide on Exception handling
       throw new ObjectNotFoundException("Object not found:" +
                                         objectPID + "\n" +
                                         e.getMessage() + "\n");
     }
   }
 
   /**
    * <p>Exports the object. Since the XML representation of an object is
    * not stored in the Fast storage area, this method always queries the
    * Definitive storage area using <code>DefinitiveDOReader</code>.</p>
    *
    * @return A stream of bytes consisting of the XML-encoded representation
    * of the digital object.
    */
   public InputStream ExportObject() throws StreamIOException, GeneralException
   {
     try
     {
       if (doReader == null) doReader = new DefinitiveDOReader(PID);
     } catch (ServerException se)
     {
       System.err.println("ExportObject: Can't create DefinitiveDOReader");
     }
     return(doReader.ExportObject());
   }
 
   /**
    * <p>Gets a list of Behavior Definition object PIDs associated with the
    * specified digital object.</p>
    *
    * @param versDateTime The versioning datetime stamp.
    * @return An array containing a list of Behavior Definition object PIDs.
    */
   public String[] GetBehaviorDefs(Date versDateTime)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     String[] behaviorDefs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "+
           "BehaviorDefinition.BDEF_PID "+
           "FROM "+
           "BehaviorDefinition,"+
           "Disseminator,"+
           "DigitalObject,"+
           "DigitalObjectDissAssoc "+
           "WHERE "+
           "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "+
           "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "+
           "BehaviorDefinition.BDEF_DBID = Disseminator.BDEF_DBID AND "+
           "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) System.out.println("GetbDefsObjectQuery: "+query);
       ResultSet rs = null;
       String results = null;
       try
       {
         Connection connection = connectionPool.getConnection();
         Statement statement = connection.createStatement();
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
         connectionPool.free(connection);
         connection.close();
         statement.close();
       } catch (SQLException sqle)
       {
         // FIXME!! - Decide on Exception handling
         // Problem with the relational database or query
         System.out.println("Object not found: " +
             sqle.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " +
         //    sqle.getMessage() + "\n");
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         // FIXME!! - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the
         // sample objects in DefinitiveBMechReader
         if (doReader == null) doReader = new DefinitiveDOReader(PID);
         behaviorDefs = doReader.GetBehaviorDefs(versDateTime);
 
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: \n PID: " +
                          PID + "\n asOfDate: " +
                          DateUtility.convertDateToString(versDateTime) + "\n");
         //throw new ObjectNotFoundException("Object not found: \n PID: " +
         //    PID + "\n asOfDate: " +
         //    DateUtility.convertDateToString(versDateTime) + "\n");
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
    * @throws MethodNotFoundException If the specified method name cannot
    * be found.
    */
   public MethodParmDef[] GetBMechMethodParm(String bDefPID, String methodName,
       Date versDateTime) throws GeneralException
   //throws MethodNotFoundException
   {
     MethodParmDef[] methodParms = null;
     MethodParmDef methodParm = null;
     Vector queryResults = new Vector();
 
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String query =
           "SELECT DISTINCT "+
           "PARM_Name,"+
           "PARM_Default_Value,"+
           "PARM_Required_Flag,"+
           "PARM_Label "+
           " FROM "+
           "DigitalObject,"+
           "BehaviorDefinition,"+
           "BehaviorMechanism,"+
           "MechanismImpl,"+
           "Method,"+
           "Parameter "+
           " WHERE "+
           "BehaviorMechanism.BDEF_DBID=Parameter.BDEF_DBID AND "+
           "Method.BDEF_DBID=Parameter.BDEF_DBID AND "+
           "Method.METH_DBID=Parameter.METH_DBID AND "+
           "BehaviorMechanism.BDEF_DBID=Method.BDEF_DBID AND "+
           "MechanismImpl.METH_DBID=Method.METH_DBID AND " +
           "BehaviorMechanism.BDEF_DBID=BehaviorDefinition.BDEF_DBID AND "+
           "DigitalObject.DO_PID=\'" + PID + "\' AND "+
           "BehaviorDefinition.BDEF_PID='" + bDefPID + "' AND "+
           "Method.METH_Name='"  + methodName + "' ";
 
       if(debug) System.out.println("MethodParmQuery="+query+"\n");
       try
       {
         Connection connection = connectionPool.getConnection();
         if(debug) System.out.println("connectionPool = "+connectionPool);
         Statement statement = connection.createStatement();
         ResultSet rs = statement.executeQuery(query);
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
           Boolean B = new Boolean(results[2]);
           methodParm.parmRequired = B.booleanValue();
           methodParm.parmLabel = results[3];
           queryResults.addElement(methodParm);
         }
         methodParms = new MethodParmDef[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           methodParms[rowCount] = (MethodParmDef)e.nextElement();
           rowCount++;
         }
         connectionPool.free(connection);
         connection.close();
         statement.close();
       } catch (SQLException sqle)
       {
         // Problem with the relational database or query
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " +
             PID + "\n" + sqle.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " +
         //    PID + "\n" + sqle.getMessage() + "\n");
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         // FIXME!! - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the
         // sample objects in DefinitiveBMechReader
         DefinitiveBMechReader bMechReader = new DefinitiveBMechReader(bDefPID);
         // FIXME!! - code to get method parameters directly from the
         // XML objects NOT implemented yet.
         return methodParms;
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // FIXME!! - Decide on Exception handling
         System.out.println("Method parameter not found: " +
            "\n bDefPID: " + bDefPID +
            "\n methodName: "+methodName +
            "\n asOfDate: " + DateUtility.convertDateToString(versDateTime) +
            "\n");
         //throw new MethodNotFoundException("Method parameter not found: " +
         throw new GeneralException("Method parameter not found: " +
            "\n bDefPID: " + bDefPID +
            "\n methodName: "+methodName +
            "\n asOfDate: " + DateUtility.convertDateToString(versDateTime) +
            "\n" + e.getMessage() + "\n");
       }
     }
     return methodParms;
   }
 
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
    */
   public MethodDef[] GetBMechMethods(String bDefPID, Date versDateTime)
       throws GeneralException
   {
     System.out.println("MethodPIDMech: "+PID);
     MethodDef[] methodDefs = null;
     MethodDef methodDef = null;
     Vector queryResults = new Vector();
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT " +
           "Method.METH_Name," +
           "Method.METH_Label," +
           "MechanismImpl.MECHImpl_Address_Location," +
           "MechanismImpl.MECHImpl_Operation_Location " +
           "FROM " +
           "BehaviorDefinition," +
           "Disseminator," +
           "Method," +
           "DigitalObject," +
           "DigitalObjectDissAssoc," +
           "BehaviorMechanism," +
           "MechanismImpl " +
           "WHERE " +
           "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND " +
           "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND " +
           "BehaviorDefinition.BDEF_DBID = Disseminator.BDEF_DBID AND " +
           "BehaviorMechanism.BMECH_DBID = Disseminator.BMECH_DBID AND " +
           "BehaviorMechanism.BMECH_DBID = MechanismImpl.BMECH_DBID AND " +
           "BehaviorDefinition.BDEF_DBID = MechanismImpl.BDEF_DBID AND " +
           "Method.METH_DBID = MechanismImpl.METH_DBID AND " +
           "Method.BDEF_DBID = BehaviorDefinition.BDEF_DBID AND " +
           "BehaviorDefinition.BDEF_PID = \'" + bDefPID + "\' AND " +
           "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) System.out.println("ObjectQuery: "+query);
       ResultSet rs = null;
       String[] results = null;
       try
       {
         Connection connection = connectionPool.getConnection();
         Statement statement = connection.createStatement();
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
             System.out.println("method[+"+i+"] = "+results[i-1]);
           }
           methodDef.methodName = results[0];
           methodDef.methodLabel = results[1];
           try
           {
             methodDef.methodParms = this.GetBMechMethodParm(bDefPID,
                 methodDef.methodName, versDateTime);
           //} catch (MethodNotFoundException mpnfe)
           } catch (GeneralException ge)
           {
             // FIXME!! - Decide on Exception handling
             System.out.println("Method not found: " + methodDef.methodName);
             //throw new MethodNotFoundException("Method not found: " +
             //    methodDef.methodName +"\n");
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
         connectionPool.free(connection);
         connection.close();
         statement.close();
       } catch (SQLException sqle)
       {
         // Problem with the relational database or query
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
                            "\n" + sqle.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    "\n" + sqle.getMessage() + "\n");
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         // FIXME!! - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the
         // sample objects in DefinitiveBMechReader
         if (doReader == null) doReader = new DefinitiveDOReader(PID);
         methodDefs = doReader.GetBMechMethods(bDefPID, versDateTime);
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
                            "\n bDefPID: " + bDefPID +
                            "\n asOfDate: "+
                            DateUtility.convertDateToString(versDateTime) +
                            "\n" + e.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //                   "\n bDefPID: " + bDefPID +
         //                   "\n asOfDate: "+
         //                   DateUtility.convertDateToString(versDateTime) +
         //                   "\n" + e.getMessage() + "\n");
       }
     }
     return methodDefs;
   }
 
   /**
    * <p>Gets WSDL containing method definitions. Since the XML representation
    * of digital objects is not stored in the Fast storage area, this method
    * uses <code>DefinitiveDOReader</code> to query the Definitive
    * storage area.</p>
    *
    * @param bDefPID The persistent identifier of Behavior Definition object.
    * @param versDateTime The versioning datetime stamp.
    * @return A stream of bytes containing XML-encoded representation of
    * method definitions from WSDL in assocaited Behavior Mechanism object.
    */
   public InputStream GetBMechMethodsWSDL(String bDefPID, Date versDateTime)
       throws GeneralException
   {
     try
     {
     if (doReader == null) doReader = new DefinitiveDOReader(PID);
     } catch (ServerException se)
     {
       System.err.println("GetBMechMethodsWSDL: Can't create DefinitiveDOReader");
     }
     return doReader.GetBMechMethodsWSDL(bDefPID, versDateTime);
   }
 
   /**
    * <p>Gets a datastream specified by the datastream ID.</p>
    *
    * @param datastreamID The identifier of the requested datastream.
    * @param versDateTime The versioning datetime stamp.
    * @return The specified datastream.
    */
   public Datastream GetDatastream(String datastreamID, Date versDateTime)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     Datastream[] datastreams = null;
     Datastream datastream = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "+
           "DataStreamBinding.DSBinding_DS_Label,"+
           "DataStreamBinding.DSBinding_DS_MIME,"+
           "DataStreamBinding.DSBinding_DS_Location "+
           "FROM "+
           "DigitalObject,"+
           "DataStreamBinding "+
           "WHERE "+
           "DigitalObject.DO_DBID = DataStreamBinding.DO_DBID AND "+
           "DataStreamBinding.DSBinding_DS_ID=\'" + datastreamID +"\' AND "+
           "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) System.out.println("ObjectQuery: "+query);
       ResultSet rs = null;
       String[] results = null;
       try
       {
         Connection connection = connectionPool.getConnection();
         Statement statement = connection.createStatement();
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
         connectionPool.free(connection);
         connection.close();
         statement.close();
       } catch (SQLException sqle)
       {
         // Problem with the relational database or query
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             sqle.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    sqle.getMessage() + "\n");
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         // FIXME!! - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the
         // sample objects in DefinitiveBMechReader
         if (doReader == null) doReader = new DefinitiveDOReader(PID);
         datastream = doReader.GetDatastream(datastreamID, versDateTime);
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             "\n asOfDate: " +
             DateUtility.convertDateToString(versDateTime) +
             "\n" + e.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    "\n asOfDate: " +
         //    DateUtility.convertDateToString(versDateTime) +
         //    "\n" + e.getmessage() + "\n");
       }
     }
     return datastream;
   }
 
   /**
    * <p>Gets all the datastreams of a digital object.</p>
    *
    * @param versDateTime The versioning datetime stamp.
    * @return An array of datastreams.
    */
   public Datastream[] GetDatastreams(Date versDateTime)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     Datastream[] datastreams = null;
     Datastream datastream = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "+
           "DataStreamBinding.DSBinding_DS_Label,"+
           "DataStreamBinding.DSBinding_DS_MIME,"+
           "DataStreamBinding.DSBinding_DS_Location "+
           "FROM "+
           "DigitalObject,"+
           "DataStreamBinding "+
           "WHERE "+
           "DigitalObject.DO_DBID = DataStreamBinding.DO_DBID AND "+
           "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) System.out.println("ObjectQuery: "+query);
       ResultSet rs = null;
       String[] results = null;
       try
       {
         Connection connection = connectionPool.getConnection();
         Statement statement = connection.createStatement();
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
           datastreams[rowCount] = (Datastream)e.nextElement();
           rowCount++;
         }
         connectionPool.free(connection);
         connection.close();
         statement.close();
       } catch (SQLException sqle)
       {
         // Problem with the relational database or query
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             sqle.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    sqle.getMessage() + "\n");
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in the Definitve storage area; query
       // Definitive storage area.
       try
       {
         // FIXME!! - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the
         // sample objects in DefinitiveBMechReader
         if (doReader == null) doReader = new DefinitiveDOReader(PID);
         datastreams = doReader.GetDatastreams(versDateTime);
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             "\n asOfDate: " +
             DateUtility.convertDateToString(versDateTime) +
             "\n" + e.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    "\n asOfDate: " +
         //    DateUtility.convertDateToString(versDateTime) +
         //    "\n" + e.getmessage() + "\n");
       }
     }
     return datastreams;
   }
 
   /**
    * <p>Gets a dissemination result.</p>
    *
    * @param PID The persistent identifier for the digital object.
    * @param bDefPID The persistent identifier for the Behavior Definition
    * object.
    * @param methodName The name of the method to be executed.
    * @param versDateTime The versioning datetime stamp.
    * @return A MIME-typed stream containing the dissemination result.
    * @throws ObjectNotFoundException If object cannot be found.
    */
   public DisseminationBindingInfo[] getDissemination(String PID,
       String bDefPID, String methodName, Date versDateTime)
       throws ObjectNotFoundException
   {
     DisseminationBindingInfo dissBindInfo = null;
     DisseminationBindingInfo[] dissBindInfoArray = null;
     Vector queryResults = new Vector();
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String query =
           "SELECT DISTINCT "+
           "DigitalObject.DO_PID,"+
           "BehaviorDefinition.BDEF_PID,"+
           "Method.METH_Name,"+
           "MechanismImpl.MECHImpl_Address_Location,"+
           "MechanismImpl.MECHImpl_Operation_Location,"+
           "MechanismImpl.MECHImpl_Protocol_Type,"+
           "DataStreamBinding.DSBinding_DS_Location, "+
           "DataStreamBindingSpec.DSBindingSpec_Name "+
           " FROM "+
           "DigitalObject,"+
           "BehaviorDefinition,"+
           "BehaviorMechanism,"+
           "DataStreamBinding,"+
           "Disseminator,"+
           "DigitalObjectDissAssoc,"+
           "MechanismImpl,"+
           "Method,"+
           "DataStreamBindingSpec "+
            " WHERE "+
           "DigitalObject.DO_DBID=DigitalObjectDissAssoc.DO_DBID AND "+
           "DigitalObjectDissAssoc.DISS_DBID=Disseminator.DISS_DBID AND " +
           "Disseminator.BDEF_DBID = BehaviorDefinition.BDEF_DBID AND " +
           "Disseminator.BMECH_DBID = BehaviorMechanism.BMECH_DBID AND " +
           "DataStreamBinding.DO_DBID = DigitalObject.DO_DBID AND " +
           "BehaviorMechanism.BMECH_DBID = MechanismImpl.BMECH_DBID AND " +
           "MechanismImpl.DSBindingKey_DBID = " +
                      "DataStreamBinding.DSBindingKey_DBID AND " +
           "DataStreamBindingSpec.DSBindingKey_DBID = " +
                      "MechanismImpl.DSBindingKey_DBID AND "+
           "MechanismImpl.METH_DBID = Method.METH_DBID AND " +
           "DigitalObject.DO_PID='" + PID + "' AND " +
           " BehaviorDefinition.BDEF_PID=\'" + bDefPID + "\' AND " +
           " Method.METH_Name=\'"  + methodName + "\' "+
           " ORDER BY DataStreamBindingSpec.DSBindingSpec_Name";
       if(debug) System.out.println("DisseminationQuery="+query+"\n");
 
       try
       {
         // execute database query and retrieve results
         Connection connection = connectionPool.getConnection();
         if(debug) System.out.println("DisseminationConnectionPool: "+
                                      connectionPool);
         Statement statement = connection.createStatement();
         ResultSet rs = statement.executeQuery(query);
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
           dissBindInfo.DSLocation = results[6];
           dissBindInfo.DSBindKey = results[7];
           try
           {
             dissBindInfo.methodParms = this.GetBMechMethodParm(results[1],
                 results[2], versDateTime);
           //} catch (MethodNotFoundException mpnfe)
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
         connectionPool.free(connection);
         connection.close();
         statement.close();
       } catch (SQLException sqle)
       {
         // Problem with the relational database or query
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             sqle.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    sqle.getMessage() + "\n");
     }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null) doReader = new DefinitiveDOReader(PID);
         // FIXME!! - code to perform disseminations directly from the
         // XML objects NOT implemented in this release.
         return dissBindInfoArray;
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any Exception.
       } catch (Exception e)
       {
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
                            "\n bDefPID: " + bDefPID +
                            "\n methodName: " + methodName +
                            "\n asOfDate: "+
                            DateUtility.convertDateToString(versDateTime) +
                            "\n" + e.getMessage() + "\n");
         throw new ObjectNotFoundException("Object not found: " + PID +
                            "\n bDefPID: " + bDefPID +
                            "\n methodName: " + methodName +
                            "\n asOfDate: "+
                            DateUtility.convertDateToString(versDateTime) +
                            "\n" + e.getMessage() + "\n");
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
    */
   public Disseminator GetDisseminator(String disseminatorID, Date versDateTime)
       throws GeneralException
   {
     Disseminator disseminator = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "+
           "Disseminator.DISS_ID,"+
           "BehaviorDefinition.BDEF_PID,"+
           "BehaviorMechanism.BMECH_PID,"+
           "DataStreamBindingMap.DSBindingMap_ID "+
           "FROM "+
           "BehaviorDefinition,"+
           "Disseminator,"+
           "DataStreamBindingMap,"+
           "DigitalObject,"+
           "DigitalObjectDissAssoc,"+
           "BehaviorMechanism "+
           "WHERE "+
           "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "+
           "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "+
           "BehaviorDefinition.BDEF_DBID = Disseminator.BDEF_DBID AND "+
           "BehaviorMechanism.BMECH_DBID = Disseminator.BMECH_DBID AND "+
           "DataStreamBindingMap.BMECH_DBID=BehaviorMechanism.BMECH_DBID AND "+
           "Disseminator.DISS_ID=\'" + disseminatorID +"\' AND "+
           "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) System.out.println("ObjectQuery: "+query);
       ResultSet rs = null;
       String[] results = null;
       try
       {
         Connection connection = connectionPool.getConnection();
         Statement statement = connection.createStatement();
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
         connectionPool.free(connection);
         connection.close();
         statement.close();
       } catch (SQLException sqle)
       {
         // Problem with the relational database or query
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             sqle.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    sqle.getMessage() + "\n");
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         // FIXME!! - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the
         // sample objects in DefinitiveBMechReader
         if (doReader == null) doReader = new DefinitiveDOReader(PID);
         disseminator = doReader.GetDisseminator(disseminatorID, versDateTime);
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             "\n asOfDate: " +
             DateUtility.convertDateToString(versDateTime) +
             "\n" + e.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    "\n asOfDate: " +
         //    DateUtility.convertDateToString(versDateTime) +
         //    "\n" + e.getmessage() + "\n");
       }
     }
     return disseminator;
   }
 
   /**
    * <p>Gets all disseminators of the specified object.</p>
    *
    * @param versDateTime versioning datetime stamp
    * @return Disseminator[] array of disseminators
    */
   public Disseminator[] GetDisseminators(Date versDateTime)
       throws GeneralException
   {
     Disseminator[] disseminators = null;
     Disseminator disseminator = null;
     Vector queryResults = new Vector();
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in the Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "+
           "Disseminator.DISS_ID,"+
           "BehaviorDefinition.BDEF_PID,"+
           "BehaviorMechanism.BMECH_PID,"+
           "DataStreamBindingMap.DSBindingMap_ID "+
           "FROM "+
           "BehaviorDefinition,"+
           "Disseminator,"+
           "DataStreamBindingMap,"+
           "DigitalObject,"+
           "DigitalObjectDissAssoc,"+
           "BehaviorMechanism "+
           "WHERE "+
           "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "+
           "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "+
           "BehaviorDefinition.BDEF_DBID = Disseminator.BDEF_DBID AND "+
           "BehaviorMechanism.BMECH_DBID = Disseminator.BMECH_DBID AND "+
           "DataStreamBindingMap.BMECH_DBID=BehaviorMechanism.BMECH_DBID AND "+
           "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) System.out.println("ObjectQuery: "+query);
       ResultSet rs = null;
       String[] results = null;
       try
       {
         Connection connection = connectionPool.getConnection();
         Statement statement = connection.createStatement();
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
         disseminators = new Disseminator[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           disseminators[rowCount] = (Disseminator)e.nextElement();
           rowCount++;
         }
         connectionPool.free(connection);
         connection.close();
         statement.close();
       } catch (SQLException sqle)
       {
         // Problem with the relational database or query
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             sqle.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    sqle.getMessage() + "\n");
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         // FIXME!! - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the
         // sample objects in DefinitiveBMechReader
         if (doReader == null) doReader = new DefinitiveDOReader(PID);
         disseminators = doReader.GetDisseminators(versDateTime);
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             "\n asOfDate: " +
             DateUtility.convertDateToString(versDateTime) +
             "\n" + e.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    "\n asOfDate: " +
         //    DateUtility.convertDateToString(versDateTime) +
         //    "\n" + e.getmessage() + "\n");
       }
     }
     return disseminators;
   }
 
   /**
    * <p>Gets datastream binding map.</p>
    *
    * @param versDateTime versioning datetime stamp
    * @return DSBindingMapAugmented[] array of datastream binding maps
    */
   public DSBindingMapAugmented[] GetDSBindingMaps(Date versDateTime)
       throws GeneralException
   {
    if (bMechReader == null) bMechReader = new DefinitiveBMechReader(PID);
     return bMechReader.GetDSBindingMaps(versDateTime);
   }
 
   /**
    * <p>Gets the label of the requested object.</p>
    *
    * @return String contining the object label
    */
   public String GetObjectLabel() throws GeneralException
   {
     if (debug) System.out.println("GetObjectLabel = " + doLabel);
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
    * @return ObjectMethodsDef containing all object methods
    * @throws ObjectNotFoundException if object cannot be found
    */
   public ObjectMethodsDef[] getObjectMethods(String PID, Date versDateTime)
       throws ObjectNotFoundException
   {
     ObjectMethodsDef[] objectMethodsDefArray = null;
     ObjectMethodsDef objectMethodsDef = null;
     Vector queryResults = new Vector();
 
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "+
           "DigitalObject.DO_PID,"+
           "BehaviorDefinition.BDEF_PID,"+
           "Method.METH_Name "+
           "FROM "+
           "BehaviorDefinition,"+
           "Disseminator,"+
           "Method,"+
           "DigitalObject,"+
           "DigitalObjectDissAssoc,"+
           "BehaviorMechanism,"+
           "MechanismImpl "+
           "WHERE "+
           "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "+
           "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "+
           "BehaviorDefinition.BDEF_DBID = Disseminator.BDEF_DBID AND "+
           "BehaviorMechanism.BMECH_DBID = Disseminator.BMECH_DBID AND "+
           "BehaviorMechanism.BMECH_DBID = MechanismImpl.BMECH_DBID AND "+
           "BehaviorDefinition.BDEF_DBID = MechanismImpl.BDEF_DBID AND "+
           "Method.METH_DBID = MechanismImpl.METH_DBID AND "+
           "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) System.out.println("getObjectMethodsQuery: "+query);
       ResultSet rs = null;
       String[] results = null;
       try
       {
         Connection connection = connectionPool.getConnection();
         Statement statement = connection.createStatement();
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
           queryResults.add(objectMethodsDef);
         }
         objectMethodsDefArray = new ObjectMethodsDef[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           objectMethodsDefArray[rowCount] = (ObjectMethodsDef)e.nextElement();
           rowCount++;
         }
         connectionPool.free(connection);
         connection.close();
         statement.close();
       } catch (SQLException sqle)
       {
         // Problem with the relational database or query
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             sqle.getMessage() + "\n");
         throw new ObjectNotFoundException("Object not found: " + PID +
             sqle.getMessage() + "\n");
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitve storage area.
       try
       {
         // FIXME!! - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the
         // sample objects in DefinitiveBMechReader
         if (doReader == null) doReader = new DefinitiveDOReader(PID);
         String[] behaviorDefs = doReader.GetBehaviorDefs(versDateTime);
         Vector results = new Vector();
         for (int i=0; i<behaviorDefs.length; i++)
         {
           MethodDef[] methodDefs = doReader.GetBMechMethods(behaviorDefs[i],
                                    versDateTime);
           for (int j=0; j<methodDefs.length; j++)
           {
             objectMethodsDef = new ObjectMethodsDef();
             objectMethodsDef.PID = PID;
             objectMethodsDef.bDefPID = behaviorDefs[i];
             objectMethodsDef.methodName = methodDefs[j].methodName;
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
         // FIXME!! - need to add code to get object info from XML objects
         return objectMethodsDefArray;
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             "\n asOfDate: " +
             DateUtility.convertDateToString(versDateTime) +
             "\n" + e.getMessage() + "\n");
         throw new ObjectNotFoundException("Object not found: " + PID +
             "\n asOfDate: " +
             DateUtility.convertDateToString(versDateTime) +
             "\n" + e.getMessage() + "\n");
         }
     }
     return objectMethodsDefArray;
   }
 
   /**
    * <p>Gets the persistent identifier or PID of the digital object.</p>
    *
    * @return String containing the persistent identifier
    */
   public String GetObjectPID() throws GeneralException
   {
     if (debug) System.out.println("GetObjectPID = " + PID);
     return this.PID;
   }
 
   /**
    * <p>Gets the state on a digital object</p>
    *
    * @return String state of the object
    */
   public String GetObjectState() throws GeneralException
   {
     try
     {
       if (doReader == null) doReader = new DefinitiveDOReader(PID);
     } catch (ServerException se)
     {
       System.err.println("GetObjectState: Can't create DefinitiveDOReader");
     }
     return doReader.GetObjectState();
     }
 
   /**
    * <p>Gets the XML representation of the object. Since the XML representation
    * of an object is not stored in the Fast storage area, this method always
    * queries the Definitive storage area using <code>DefinitveDOReader</code>.
    * </p>
    *
    * @return String containing the XML representation of the object.
    */
   public InputStream GetObjectXML()
       throws StreamIOException, GeneralException
   {
     try
     {
       if (doReader == null) doReader = new DefinitiveDOReader(PID);
     } catch (ServerException se)
     {
       System.err.println("GetObjectXML: Can't create DefinitiveDOReader");
     }
     return(doReader.GetObjectXML());
   }
 
   /**
    * <p>Lists the datastream IDs of the requested object having the
    * specified <code>state</code>. Note that the Fast storage area does NOT
    * contain state information so state is ignored when querying the Fast
    * storage area. <code>DefinitiveDOReader</code> should be used instead
    * to list datastream IDs with a given state.</p>
    *
    * @param state State of the datastreams
    * @return String[] containing the datastream IDs
    */
   public String[] ListDatastreamIDs(String state)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     String[] datastreamIDs = null;
     Datastream datastream = null;
     if (isFoundInFastStore)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database.
       String  query =
           "SELECT DISTINCT "+
           "DataStreamBinding.DSBinding_DS_ID "+
           "FROM "+
           "DigitalObject,"+
           "DataStreamBinding "+
           "WHERE "+
           "DigitalObject.DO_DBID = DataStreamBinding.DO_DBID AND "+
           "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) System.out.println("ObjectQuery: "+query);
       ResultSet rs = null;
       String[] results = null;
       try
       {
         Connection connection = connectionPool.getConnection();
         Statement statement = connection.createStatement();
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
         connectionPool.free(connection);
         connection.close();
         statement.close();
       } catch (SQLException sqle)
       {
         // Problem with the relational database or query
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             sqle.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    sqle.getMessage() + "\n");
       }
     } else if (isFoundInDefinitiveStore)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         // FIXME!! - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the
         // sample objects in DefinitiveBMechReader
         if (doReader == null) doReader = new DefinitiveDOReader(PID);
         datastreamIDs = doReader.ListDatastreamIDs("");
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             "\n" + e.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    "\n" + e.getmessage() + "\n");
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
    * @param state State of the disseminators
    * @return String[] listing disseminator IDs
    */
   public String[] ListDisseminatorIDs(String state)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     Disseminator disseminator = null;
     String[] disseminatorIDs = null;
     if (isFoundInFastStore)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "+
           "Disseminator.DISS_ID "+
           "FROM "+
           "Disseminator,"+
           "DigitalObject,"+
           "DigitalObjectDissAssoc "+
           "WHERE "+
           "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "+
           "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "+
           "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) System.out.println("ObjectQuery: "+query);
       ResultSet rs = null;
       String[] results = null;
       try
       {
         Connection connection = connectionPool.getConnection();
         Statement statement = connection.createStatement();
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
         connectionPool.free(connection);
         connection.close();
         statement.close();
       } catch (SQLException sqle)
       {
         // Problem with the relational database or query
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             sqle.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    sqle.getMessage() + "\n");
       }
     } else if (isFoundInDefinitiveStore)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         // FIXME!! - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the
         // sample objects in DefinitiveBMechReader
         if (doReader == null) doReader = new DefinitiveDOReader(PID);
         disseminatorIDs = doReader.ListDisseminatorIDs("A");
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             "\n" + e.getMessage() + "\n");
         //throw new ObjectNotFoundException("Object not found: " + PID +
         //    "\n" + e.getmessage() + "\n");
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
    * @param PID persistent identifier of the digital object
    * @return String containing label of the specified digital object
    * @throws ObjectNotFoundException if object cannot be found
    */
   public String locatePID(String PID) throws ObjectNotFoundException
   {
     ResultSet rs = null;
     String  query =
         "SELECT "+
         "DigitalObject.DO_Label "+
         "FROM "+
         "DigitalObject "+
         "WHERE "+
         "DigitalObject.DO_PID=\'" + PID + "\';";
     if (debug) System.out.println("LocatObjectQuery: "+query);
 
     try
     {
       Connection connection = connectionPool.getConnection();
       if(debug) System.out.println("LocatePIDConnectionPool: "+
                                    connectionPool);
       Statement statement = connection.createStatement();
       rs = statement.executeQuery(query);
       while (rs.next())
       {
         doLabel = rs.getString(1);
       }
       connectionPool.free(connection);
       connection.close();
       statement.close();
     } catch (SQLException sqle)
     {
       // Problem with the relational database or query
       // FIXME!! Decide on Exception handling
       System.out.println("Object not found: " + PID +
           sqle.getMessage() + "\n");
       throw new ObjectNotFoundException("Object not found: " + PID +
                                         "\n" + sqle.getMessage() + "\n");
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
         // FIXME!! - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the
         // sample objects in DefinitiveDOReader
         if (doReader == null) doReader = new DefinitiveDOReader(PID);
         doLabel = doReader.GetObjectLabel();
         isFoundInDefinitiveStore = true;
         if (debug) System.out.println("OBJECT FOUND IN DEFINITIVE STORE");
       } catch (Exception e)
       {
         // FIXME!! - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
 
         // If object cannot be found in the Definitive storage area,
         // then the object request contains errors or the object does NOT
         // exist in the repository. In either case, this is a nonfatal
         // error that is passed back up the line.
         // FIXME!! - Decide on Exception handling
         System.out.println("Object not found: " + PID +
             "\n" + e.getMessage() + "\n");
         throw new ObjectNotFoundException("Object not found: " + PID +
             "\n" + e.getMessage() + "\n");
       }
     } else
     {
       isFoundInFastStore = true;
         if (debug) System.out.println("OBJECT FOUND IN FAST STORE");
     }
     return doLabel;
   }
 
   /**
    * <p>Tests the methods of <code>FastDOReader</code>.</p>
    *
    * @param args Command line arguments
    */
   public static void main(String[] args)
   {
     // Test dissemination query against relational database
     System.out.println("\nBEGIN ----- TEST RESULTS FOR DISSEMINATION:");
     String PID = "1007.lib.dl.test/text_ead/viu00003";
     String bDefPID = "web_ead";
     String methodName = "get_web_default";
     Date versDateTime = null;
     FastDOReader fdor = null;
     DisseminationBindingInfo[] dissem = null;
     try
     {
       fdor = new FastDOReader(PID);
       dissem = fdor.getDissemination(PID, bDefPID, methodName, versDateTime);
       for (int i=0; i<dissem.length; i++)
       {
           System.out.println("dissemResults["+i+"] = "+i+
           "dissemAddress: "+dissem[i].AddressLocation+
           "dissemOperation: "+dissem[i].OperationLocation+
           "dissemDSLocation: "+dissem[i].DSLocation+
           "dissemProtocol: "+dissem[i].ProtocolType+
           "dissemBindKey: "+dissem[i].DSBindKey);
           if (dissem[i].methodParms != null)
           {
             MethodParmDef[] methodParms = dissem[i].methodParms;
             for (int j=0; j<methodParms.length; j++)
             {
               System.out.println("Dissem: MethodParms:"+
               "parm["+j+"] = "+methodParms[j]);
             }
           } else
           {
             System.out.println("Dissem: Method Has NO PARMS");
           }
       }
       System.out.println("END ----- TEST RESULTS FOR DISSEMINATION\n");
 
       // Test reading method paramters (method has parms)
       System.out.println("\nBEGIN ----- TEST RESULTS FOR READING METHOD"+
                          "PARMS:\n (method that has parms)");
       MethodParmDef[] methodParms = null;
       PID = "1007.lib.dl.test/text_ead/viu00003";
       bDefPID = "web_ead";
       methodName = "get_web_default";
       fdor = new FastDOReader(PID);
       methodParms = fdor.GetBMechMethodParm(bDefPID, methodName,versDateTime);
       for (int i=0; i<methodParms.length; i++)
       {
         System.out.println("methodParmName:"+i+" \n"+methodParms[i].parmName+
         "\n   methodParmDefaultValue["+i+"] = "+methodParms[i].parmDefaultValue+
         "\n   methodParmRequiredFlag["+i+"] = "+methodParms[i].parmRequired+
         "\n   methodParmLabel["+i+"] = "+methodParms[i].parmLabel+"\n");
       }
       // Test reading method parameters (method has no parms)
       System.out.println("(method tha has NO parms)");
       PID = "1007.lib.dl.test/text_ead/viu00003";
       bDefPID = "web_ead";
       methodName = "get_tp";
       fdor = new FastDOReader(PID);
       methodParms = fdor.GetBMechMethodParm(bDefPID, methodName,versDateTime);
       System.out.println("\n\nTest with method that has NO parms\n");
       for (int i=0; i<methodParms.length; i++)
       {
         System.out.println("methodParmName:"+i+" \n"+methodParms[i].parmName+
         "\n   methodParmDefaultValue["+i+"] = "+methodParms[i].parmDefaultValue+
         "\n   methodParmRequiredFlag["+i+"] = "+methodParms[i].parmRequired+
         "\n   methodParmLabel["+i+"] = "+methodParms[i].parmLabel+"\n");
       }
       System.out.println("END ----- TEST RESULTS FOR READING METHOD "+
                         "PARAMETERS\n");
 
       // Test reading behavior methods
       System.out.println("\nBEGIN ----- TEST RESULTS FOR READING ALL METHODS:");
       MethodDef[] methodDefs = null;
       PID = "1007.lib.dl.test/text_ead/viu00003";
       bDefPID = "web_ead";
       fdor = new FastDOReader(PID);
       methodDefs = fdor.GetBMechMethods(bDefPID, versDateTime);
       for (int i=0; i<methodDefs.length; i++)
       {
         System.out.println("methodDefName: "+i+" \n"+methodDefs[i].methodName+
         "\n   methodDefLabel["+i+"] = "+methodDefs[i].methodLabel);
         //"\n   methodDefBindingURL["+i+"] = "+methodDefs[i].httpBindingURL+
         //"\n   methodDefAddress["+i+"] = "+methodDefs[i].httpBindingAddress+
         //"\n   methodDefOperation["+i+"] = "+
         //methodDefs[i].httpBindingOperationLocation+"\n");
         methodParms = methodDefs[i].methodParms;
         if (methodParms != null)
         {
           System.out.println("\nMETHOD HAS PARMs\n");
           for (int j=0; j<methodParms.length; j++)
           {
             System.out.println("methodParm: "+j+" \n"+methodParms[j].parmName+
             "\n   methodParmDefaultValue["+j+"] = "+
             methodParms[j].parmDefaultValue+
             "\n   methodParmRequiredFlag["+j+"] = "+
             methodParms[j].parmRequired+
             "\n   methodParmLabel["+j+"] = "+
             methodParms[j].parmLabel+"\n");
           }
         } else
         {
           System.out.println("\nMETHOD HAS NO PARMS\n");
         }
       }
       System.out.println("END ----- TEST RESULTS FOR READING ALL METHODS\n");
 
       System.out.println("\nBEGIN ----- TEST GET OBJECT METHODS");
       PID = "1007.lib.dl.test/text_ead/viu00003";
       fdor = new FastDOReader(PID);
       ObjectMethodsDef[] omdArray = fdor.getObjectMethods(PID, versDateTime);
       System.out.println("size: "+omdArray.length);
       for (int i=0; i<omdArray.length; i++)
       {
         ObjectMethodsDef omd = omdArray[i];
         System.out.println("omdArray["+i+"] = "+
                            "\n   PID: "+omd.PID+
                            "\n   bDefPID: "+omd.bDefPID+
                            "\n   methodName: "+omd.methodName);
       }
       System.out.println("END ----- TEST GET OBJECT METHODS");
 
       System.out.println("\nBEGIN ----- TEST GET BEAHVIOR DEFS");
       String[] bDefs = null;
       PID = "1007.lib.dl.test/text_ead/viu00003";
       fdor = new FastDOReader(PID);
       bDefs = fdor.GetBehaviorDefs(versDateTime);
       for (int i=0; i<bDefs.length; i++)
       {
         System.out.println("bDef["+i+"] = "+bDefs[i]);
       }
       System.out.println("END ----- TEST GET BEAHVIOR DEFS");
 
       System.out.println("\nBEGIN ----- TEST GET DISSEMINATOR"+
                          "\n (retro style object)");
       PID = "1007.lib.dl.test/text_ead/viu00003";
       fdor = new FastDOReader(PID);
       Disseminator diss = fdor.GetDisseminator("web_ead1", versDateTime);
       System.out.println("dissID: "+diss.dissID+"\nbDefPID: "+diss.bDefID+
                          "\nbMechPID: "+diss.bMechID+"\nbBindMapID: "+
                          diss.dsBindMapID);
       System.out.println("\n(new stle object)");
       PID = "uva-lib:1225";
       fdor = new FastDOReader(PID);
       diss = fdor.GetDisseminator("DISS1", versDateTime);
       System.out.println("dissID: "+diss.dissID+"\nbDefPID: "+diss.bDefID+
                          "\nbMechPID: "+diss.bMechID+"\nbBindMapID: "+
                          diss.dsBindMapID);
       System.out.println("END ----- TEST GET DISSEMINATOR");
 
       System.out.println("\nBEGIN ----- TEST GET DISSEMINATORS"+
                          "\n(new style object)");
       PID = "uva-lib:1225";
       fdor = new FastDOReader(PID);
       Disseminator[] diss1 = fdor.GetDisseminators(versDateTime);
       for (int i=0; i<diss1.length; i++)
       {
         System.out.println("dissID: "+diss1[i].dissID+"\nbDefPID: "+
         diss1[i].bDefID+"\nbMechPID: "+diss1[i].bMechID+"\nbBindMapID: "+
         diss1[i].dsBindMapID);
       }
       System.out.println("\n(retro style object)");
       PID = "1007.lib.dl.test/text_ead/viu00001";
       fdor = new FastDOReader(PID);
       Disseminator[] diss2 = fdor.GetDisseminators(versDateTime);
       System.out.println("size: "+diss1.length);
       for (int i=0; i<diss2.length; i++)
       {
         System.out.println("dissID: "+diss2[i].dissID+"\nbDefPID: "+
         diss2[i].bDefID+"\nbMechPID: "+diss2[i].bMechID+"\nbBindMapID: "+
         diss2[i].dsBindMapID);
       }
       System.out.println("END ----- TEST GET DISSEMINATORS");
 
       System.out.println("\nBEGIN ----- TEST LIST DISSEMINATORIDS"+
                          "\n(retro style object)");
       PID = "1007.lib.dl.test/text_ead/viu00001";
       fdor = new FastDOReader(PID);
       String[] dissIDs = fdor.ListDisseminatorIDs("");
       for (int i=0; i<dissIDs.length; i++)
       {
         System.out.println("ListdissID: "+dissIDs[i]);
       }
       System.out.println("\n(new style object)");
       PID = "uva-lib:1225";
       fdor = new FastDOReader(PID);
       dissIDs = fdor.ListDisseminatorIDs("");
       for (int i=0; i<dissIDs.length; i++)
       {
         System.out.println("ListdissID: "+dissIDs[i]);
       }
       System.out.println("END ----- TEST LIST DISSEMINATORIDS");
 
       System.out.println("\nBEGIN ----- TEST GET DATASTREAM"+
                          "\n(new style object)");
       PID = "uva-lib:1225";
       fdor = new FastDOReader(PID);
       Datastream ds = fdor.GetDatastream("DS1", versDateTime);
       System.out.println("GetDatastreamLabel: "+ds.DSLabel+"\nMIME: "+
                          ds.DSMIME+"\nLocation: "+ds.DSLocation);
       System.out.println("\n(retro style object)");
       PID = "1007.lib.dl.test/text_ead/viu00001";
       fdor = new FastDOReader(PID);
       ds = fdor.GetDatastream("1", versDateTime);
       System.out.println("GetDatastreamLabel: "+ds.DSLabel+"\nMIME: "+
                          ds.DSMIME+"\nLocation: "+ds.DSLocation);
       System.out.println("END ----- TEST GET DATASTREAM");
 
       System.out.println("\nBEGIN ----- TEST GET DATASTREAMS"+
                          "\n(retro style object)");
       PID = "1007.lib.dl.test/text_ead/viu00001";
       fdor = new FastDOReader(PID);
       Datastream[] dsa = fdor.GetDatastreams(versDateTime);
       for (int i=0; i<dsa.length; i++)
       {
         System.out.println("GetDatastreamsLabel: "+dsa[i].DSLabel+"\nMIME: "+
         dsa[i].DSMIME+"\nLocation: "+dsa[i].DSLocation);
       }
       System.out.println("\n(new style object)");
       PID = "uva-lib:1225";
       fdor = new FastDOReader(PID);
       dsa = fdor.GetDatastreams(versDateTime);
       for (int i=0; i<dsa.length; i++)
       {
         System.out.println("GetDatastreamLabel: "+dsa[i].DSLabel+"\nMIME: "+
         dsa[i].DSMIME+"\nLocation: "+dsa[i].DSLocation);
       }
       System.out.println("END ----- TEST GET DATASTREAMS");
 
       System.out.println("\nBEGIN ----- TEST GET OBJECT LABEL");
       PID = "uva-lib:1225";
       fdor = new FastDOReader(PID);
       System.out.println("ObjectLabel: "+fdor.GetObjectLabel());
 
       System.out.println("\nBEGIN ----- TEST GET OBJECT PID");
       PID = "uva-lib:1225";
       fdor = new FastDOReader(PID);
       System.out.println("ObjectLabel: "+fdor.GetObjectPID());
       System.out.println("END ----- TEST GET OBJECT PID");
 
       System.out.println("\nBEGIN ----- TEST GET OBJECT PID only in "+
                          "Definitive Store");
       PID = "uva-lib:1220";
       fdor = new FastDOReader(PID);
       System.out.println("ObjectLabel: "+fdor.GetObjectPID());
       System.out.println("END ----- TEST GET OBJECT PID");
 
     } catch(Exception e)
     {
       System.out.println("ObjNotFound"+e.getMessage());
 
     }
   }
 }
