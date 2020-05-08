 package fedora.server.access;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 
 import fedora.server.Context;
 import fedora.server.Module;
 import fedora.server.Server;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.ModuleShutdownException;
 import fedora.server.errors.ServerException;
 import fedora.server.storage.DOManager;
 import fedora.server.storage.DisseminatingDOReader;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.types.gen.MethodDef;
 import fedora.server.types.gen.MethodParmDef;
 import fedora.server.types.gen.MIMETypedStream;
 import fedora.server.types.gen.ObjectMethodsDef;
 import fedora.server.types.gen.Property;
 import fedora.server.utilities.DateUtility;
 
 /**
  *
  * <p>Title: DefaultAccess.java</p>
  * <p>Description: The Access Module, providing support for API-A.</p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 public class DefaultAccess extends Module implements Access
 {
   private final static String CONTENT_TYPE_XML = "text/xml";
   private final static String LOCAL_ADDRESS_LOCATION = "LOCAL";
   private DOManager m_manager;
   private static boolean debug = true;
 
   /**
    * <p>Creates and initializes the Access Module. When the server is starting
    * up, this is invoked as part of the initialization process.</p>
    *
    * @param moduleParameters A pre-loaded Map of name-value pairs comprising
    *        the intended configuration of this Module.
    * @param server The <code>Server</code> instance.
    * @param role The role this module fulfills, a java class name.
    * @throws ModuleInitializationException If initilization values are
    *         invalid or initialization fails for some other reason.
    */
   public DefaultAccess(Map moduleParameters, Server server, String role)
           throws ModuleInitializationException
   {
     super(moduleParameters, server, role);
   }
 
   /**
    * <p>Initializes the module.</p>
    *
    * @throws ModuleInitializationException If the module cannot be initialized.
    */
   public void initModule()
           throws ModuleInitializationException {
       m_manager=(DOManager) getServer().getModule(
               "fedora.server.storage.DOManager");
       if (m_manager==null) {
           throw new ModuleInitializationException("Can't get a DOManager "
                   + "from Server.getModule", getRole());
       }
   }
 
   /**
    * <p>Gets the persistent identifiers or PIDs of all Behavior Definition
    * objects associated with the specified digital object.</p>
    *
    * @param PID The persistent identifier of the digitla object.
    * @param asOfDateTime The versioning datetime stamp.
    * @return An Array containing the list of Behavior Definition object PIDs.
    */
   public String[] getBehaviorDefinitions(Context context, String PID,
       Calendar asOfDateTime) throws ServerException
   {
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDateTime);
     String[] behaviorDefs = null;
     try
     {
       String[] behaviorDefinitions = null;
       DisseminatingDOReader reader =
           m_manager.getDisseminatingReader(context, PID);
       behaviorDefs = reader.GetBehaviorDefs(versDateTime);
     } catch (Exception e)
     {
       getServer().logWarning(e.getMessage());
       return behaviorDefs;
     }
     return behaviorDefs;
   }
 
   /**
    * <p>Gets the method definitions associated with the specified Behavior
    * Definition object.</p>
    *
    * @param PID The persistent identifier of the digital object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param asOfDateTime The versioning datetime stamp.
    * @return An Array containing the list of method definitions.
    */
   public MethodDef[] getBehaviorMethods(Context context, String PID,
                                         String bDefPID, Calendar asOfDateTime)
       throws ServerException
   {
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDateTime);
     MethodDef[] methodDefs = null;
     try
     {
       DisseminatingDOReader reader =
           m_manager.getDisseminatingReader(context, PID);
       fedora.server.storage.types.MethodDef[] methodResults =
           reader.GetBMechMethods(bDefPID, versDateTime);
       methodDefs = new MethodDef[methodResults.length];
       for (int i=0; i<methodResults.length; i++)
       {
         MethodDef mdef =  new MethodDef();
         mdef.setMethodLabel(methodResults[i].methodLabel);
         mdef.setMethodName(methodResults[i].methodName);
         fedora.server.storage.types.MethodParmDef[] parmResults =
             methodResults[i].methodParms;
         if (parmResults.length > 0)
         {
           MethodParmDef[] methodParms = new MethodParmDef[parmResults.length];
           for (int j=0; j<parmResults.length; j++)
           {
             MethodParmDef parmdef = new MethodParmDef();
             parmdef.setParmDefaultValue(parmResults[j].parmDefaultValue);
             parmdef.setParmLabel(parmResults[j].parmLabel);
             parmdef.setParmName(parmResults[j].parmName);
             parmdef.setParmRequired(parmResults[j].parmRequired);
             methodParms[j] = parmdef;
           }
           mdef.setMethodParms(methodParms);
         }
       methodDefs[i] = mdef;
     }
     } catch (Exception e)
     {
       getServer().logWarning(e.getMessage());
       return methodDefs;
     }
     return methodDefs;
   }
 
   /**
    * <p>Gets the method definitions associated with the specified Behavior
    * Definition object.</p>
    *
    * @param PID The persistent identifier of the digital object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param asOfDateTime The versioning datetime stamp.
    * @return A MIME-typed stream containing the method definitions in the form
    * of an XML fragment obtained from the WSDL in the associated Behavior
    * Mechanism object.
    */
   public MIMETypedStream getBehaviorMethodsAsWSDL(Context context,
       String PID, String bDefPID, Calendar asOfDateTime) throws ServerException
   {
     ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
     MIMETypedStream methodDefs = null;
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDateTime);
     InputStream methodResults = null;
     try
     {
       DisseminatingDOReader reader =
           m_manager.getDisseminatingReader(context, PID);
       methodResults = reader.GetBMechMethodsWSDL(bDefPID, versDateTime);
       // FIXME!! versioning based on datetime not yet implemented
       int byteStream = 0;
       while ((byteStream = methodResults.read()) >= 0)
       {
         baos.write(byteStream);
       }
       methodResults.close();
     } catch (Exception e)
     {
       getServer().logWarning(e.getMessage());
       return methodDefs;
     }
     if (methodResults != null)
     {
     methodDefs = new MIMETypedStream();
     methodDefs.setMIMEType(CONTENT_TYPE_XML);
     methodDefs.setStream(baos.toByteArray());
     }
     return methodDefs;
   }
 
   /**
    * <p>Disseminates the content produced by executing the specified method
    * of the associated Behavior Mechanism object of the specified digital
    * object.</p>
    *
    * @param PID The persistent identifier of the digital object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param methodName The name of the method to be executed.
    * @param userParms An array of user-supplied method parameters consisting
    * of name/value pairs.
    * @param asOfDateTime The versioning datetime stamp.
    * @return A MIME-typed stream containing the result of the dissemination.
    */
   public MIMETypedStream getDissemination(Context context, String PID,
       String bDefPID, String methodName, Property[] userParms,
       Calendar asOfDateTime) throws ServerException
   {
     String protocolType = null;
     DisseminationBindingInfo[] dissResults = null;
     DisseminationBindingInfo dissResult = null;
     String dissURL = null;
     String operationLocation = null;
     MIMETypedStream dissemination = null;
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDateTime);
     fedora.server.storage.types.Property[] uParms =
         new fedora.server.storage.types.Property[userParms.length];
     for (int i=0; i<userParms.length; i++)
     {
      fedora.server.storage.types.Property uParm =
               new fedora.server.storage.types.Property();
      uParm.name = userParms[i].getName();
      uParm.value = userParms[i].getValue();
      uParms[i] = uParm;
     }
     try
     {
       // Get the dissemination binding info by reading from Fast store
       DisseminatingDOReader reader =
           m_manager.getDisseminatingReader(context, PID);
       dissResults = reader.getDissemination(PID, bDefPID, methodName,
           versDateTime);
       // Assemble the dissemination from the binding info
       DisseminationService dissService = new DisseminationService();
       fedora.server.storage.types.MIMETypedStream diss =
           dissService.assembleDissemination(uParms, dissResults);
       dissemination.setMIMEType(diss.MIMEType);
       dissemination.setStream(diss.stream);
     } catch (Exception e)
     {
       getServer().logWarning(e.getMessage());
       return dissemination;
     }
    return dissemination;
   }
 
   /**
    * <p>Gets a list of all Behavior Definition object PIDs and method names
    * associated with the specified digital object.</p>
    *
    * @param PID The persistent identifier of the digital object
    * @param asOfDateTime The versioning datetime stamp
    * @return An array of all methods associated with the specified
    * digital object.
    */
   public ObjectMethodsDef[] getObjectMethods(Context context, String PID,
       Calendar asOfDateTime) throws ServerException
   {
     ObjectMethodsDef[] methodDefs = null;
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDateTime);
     try
     {
       DisseminatingDOReader reader =
           m_manager.getDisseminatingReader(context, PID);
       fedora.server.storage.types.ObjectMethodsDef[] methodResults =
           reader.getObjectMethods(PID, versDateTime);
       int size = methodResults.length;
       methodDefs = new ObjectMethodsDef[methodResults.length];
       for (int i=0; i<methodResults.length; i++)
       {
         ObjectMethodsDef mdef = new ObjectMethodsDef();
         mdef.setPID(methodResults[i].PID);
         mdef.setBDefPID(methodResults[i].bDefPID);
         mdef.setMethodName(methodResults[i].methodName);
         methodDefs[i] = mdef;
       }
     } catch (Exception e)
     {
       getServer().logWarning(e.getMessage());
       return methodDefs;
     }
     return methodDefs;
   }
 }
