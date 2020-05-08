 package fedora.server.access.dissemination;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.httpclient.Header;
 
 import org.apache.log4j.Logger;
 
 import fedora.common.Constants;
 import fedora.common.http.WebClient;
 import fedora.common.http.HttpInputStream;
 
 import fedora.server.Context;
 import fedora.server.Server;
 import fedora.server.errors.DisseminationException;
 import fedora.server.errors.DisseminationBindingInfoNotFoundException;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.HttpServiceNotFoundException;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.ServerInitializationException;
 import fedora.server.security.Authorization;
 import fedora.server.security.BackendPolicies;
 import fedora.server.security.BackendSecurity;
 import fedora.server.security.BackendSecuritySpec;
 import fedora.server.storage.DOManager;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DatastreamMediation;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.types.MethodParmDef;
 import fedora.server.storage.types.Property;
 import fedora.server.utilities.DateUtility;
 import fedora.server.utilities.ServerUtility;
 
 /**
  * <p><b>Title: </b>DisseminationService.java</p>
  * <p><b>Description: </b>A service for executing a dissemination given its
  * binding information.</p>
  *
  * @author rlw@virginia.edu
  * @version $Id$
  */
 public class DisseminationService
 {
 
   /** Logger for this class. */
   private static final Logger LOG = Logger.getLogger(
         DisseminationService.class.getName());
 
   /** The Fedora Server instance */
   private static Server s_server;
 
   /** An instance of DO manager */
   private static DOManager m_manager;
 
   /** Signifies the special type of address location known as LOCAL.
    *  An address location of LOCAL implies that no remote host name is
    *  required for the address location and that the contents of the
    *  operation location are sufficient to execute the associated mechanism.
    */
   private static final String LOCAL_ADDRESS_LOCATION = "LOCAL";
 
   /** The expiration limit in minutes for removing entries from the database. */
   private static int datastreamExpirationLimit = 0;
 
   /** An incremental counter used to insure uniqueness of tempIDs used for
    * datastream mediation.
    */
   private static int counter = 0;
 
   /** Datastream Mediation control flag. */
   private static boolean doDatastreamMediation;
   
   /** Configured Fedora server host */
   private static String fedoraServerHost = null;
   
   /** Configured Fedora server port */
   private static String fedoraServerPort = null;
   
   /** Configured Fedora redirect port */
   private static String fedoraServerRedirectPort = null;
   
   private static String fedoraHome = null;
   
   private static BackendSecuritySpec m_beSS = null;
   
   private static BackendSecurity m_beSecurity;
 
   private static WebClient s_http;
 
   /** Make sure we have a server instance for error logging purposes. */
   static
   {
     try
     {
       fedoraHome = Constants.FEDORA_HOME;
       if (fedoraHome == null)
       {
           throw new ServerInitializationException(
               "[DisseminationService] Server failed to initialize: "
               + "FEDORA_HOME is undefined");
       } else
       {
         s_server = Server.getInstance(new File(fedoraHome), false);
         fedoraServerHost = s_server.getParameter("fedoraServerHost");
         fedoraServerPort = s_server.getParameter("fedoraServerPort");
         fedoraServerRedirectPort = s_server.getParameter("fedoraRedirectPort");
         m_manager = (DOManager) s_server.getModule("fedora.server.storage.DOManager");
         m_beSecurity = (BackendSecurity) s_server.getModule("fedora.server.security.BackendSecurity");
         m_beSS = m_beSecurity.getBackendSecuritySpec();
         String expireLimit = s_server.getParameter("datastreamExpirationLimit");
         if (expireLimit == null || expireLimit.equalsIgnoreCase(""))
         {
           LOG.info("datastreamExpirationLimit unspecified; defaulting to "
                 + "300 seconds");
           datastreamExpirationLimit = 300;
         } else
         {
           datastreamExpirationLimit = new Integer(expireLimit).intValue();
           LOG.info("datastreamExpirationLimit=" + datastreamExpirationLimit);
         }
         String dsMediation =
             s_server.getModule("fedora.server.access.Access").getParameter("doMediateDatastreams");
         if (dsMediation == null || dsMediation.equalsIgnoreCase(""))
         {
           LOG.info("doMediateDatastreams unspecified; defaulting to false");
         } else
         {
           doDatastreamMediation = new Boolean(dsMediation).booleanValue();
         }
 
         s_http = new WebClient();
         s_http.USER_AGENT = "Fedora";
       }
 
     } catch (InitializationException ie)
     {
         LOG.error("Initialization error", ie);
     }
   }
 
   /** The hashtable containing information required for datastream mediation. */
   protected static Hashtable dsRegistry = new Hashtable(1000);
   protected static Hashtable beSecurityHash = new Hashtable();
 
   /**
    * <p>Constructs an instance of DisseminationService. Initializes two class
    * variables that contain the IP address and port number of the Fedora server.
    * The port number is obtained from the Fedora server config file and the IP
    * address of the server is obtained dynamically. These variables are needed
    * to perform the datastream proxy service for datastream requests.</p>
    */
   public DisseminationService()
   {  }
 /*
   public void checkState(Context context, String state, String dsID, String PID)
       throws ServerException
   {
     // Check Object State
     if ( state.equalsIgnoreCase("D")  &&
          ( context.get("canUseDeletedObject")==null
            || (!context.get("canUseDeletedObject").equals("true")) )
       )
     {
       throw new GeneralException("The requested dissemination for data object \""+PID+"\" is no "
           + "longer available. One of its datastreams (dsID=\""+dsID+"\") has been flagged for DELETION "
           + "by the repository administrator. ");
 
     } else if ( state.equalsIgnoreCase("I")  &&
                 ( context.get("canUseInactiveObject")==null
                   || (!context.get("canUseInactiveObject").equals("true")) )
               )
     {
       throw new GeneralException("The requested dissemination for data object \""+PID+"\" is no "
           + "longer available. One of its datastreams (dsID=\""+dsID+"\") has been flagged as INACTIVE "
           + "by the repository administrator. ");
     }
   }
   */
 
   /**
    * <p>Assembles a dissemination given an instance of <code>
    * DisseminationBindingInfo</code> which has the dissemination-related
    * information from the digital object and its associated Behavior
    * Mechanism object.</p>
    *
    * @param context The current context. 
    * @param PID The persistent identifier of the digital object.
    * @param h_userParms A hashtable of user-supplied method parameters.
    * @param dissBindInfoArray The associated dissemination binding information.
    * @return A MIME-typed stream containing the result of the dissemination.
    * @throws ServerException If unable to assemble the dissemination for any
    *         reason.
    */
   public MIMETypedStream assembleDissemination(Context context, String PID,
       Hashtable h_userParms, DisseminationBindingInfo[] dissBindInfoArray, 
       String bMechPid, String methodName)
       throws ServerException
   {
 
     LOG.debug("Started assembling dissemination");
 
     String dissURL = null;
     String protocolType = null;
     DisseminationBindingInfo dissBindInfo = null;
     MIMETypedStream dissemination = null;
     long initStartTime = new Date().getTime();
     boolean isRedirect = false;
     
     if (LOG.isDebugEnabled()) {
         printBindingInfo(dissBindInfoArray);
     }    
 
     if (dissBindInfoArray != null && dissBindInfoArray.length > 0)
     {
       String replaceString = null;
       int numElements = dissBindInfoArray.length;
 
       // Get row(s) of binding info and perform string substitution
       // on DSBindingKey and method parameter values in WSDL
       // Note: In case where more than one datastream matches the
       // DSBindingKey or there are multiple DSBindingKeys for the
       // method, multiple rows will be present; otherwise there is only
       // a single row.
       for (int i=0; i<dissBindInfoArray.length; i++)
       {
         ((Authorization)s_server.getModule("fedora.server.security.Authorization")).enforce_Internal_DSState(
         		context, dissBindInfoArray[i].dsID, dissBindInfoArray[i].dsState);
         dissBindInfo = dissBindInfoArray[i];
 
         // Before doing anything, check whether we can replace any
         // placeholders in the datastream url with parameter values from
         // the request.  This supports the special case where a
         // datastream's URL is dependent on user parameters, such
         // as when the datastream is actually a dissemination that
         // takes parameters.
         if (dissBindInfo.dsLocation!=null && 
                 ( dissBindInfo.dsLocation.startsWith("http://") 
                   || dissBindInfo.dsLocation.startsWith("https://") )  ) {
             String[] parts=dissBindInfo.dsLocation.split("=\\("); // regex for =(
             if (parts.length>1) {
                 StringBuffer replaced=new StringBuffer();
                 replaced.append(parts[0]);
                 for (int x=1; x<parts.length; x++) {
                     replaced.append('=');
                     int rightParenPos=parts[x].indexOf(")");
                     if (rightParenPos!=-1 && rightParenPos>0) {
                         String key=parts[x].substring(0, rightParenPos);
                         String val=(String) h_userParms.get(key);
                         if (val!=null) {
                             // We have a match... so insert the urlencoded value.
                             try {
                                 replaced.append(URLEncoder.encode(val, "UTF-8"));
                             } catch (UnsupportedEncodingException uee) {
                                 // won't happen: java always supports UTF-8
                             }
                             if (rightParenPos<parts[x].length()) {
                                 replaced.append(parts[x].substring(rightParenPos+1));
                             }
                         } else {
                             replaced.append('(');
                             replaced.append(parts[x]);
                         }
                     } else {
                         replaced.append('(');
                         replaced.append(parts[x]);
                     }
                 }
                 dissBindInfo.dsLocation=replaced.toString();
             }
         }       
         
         // Match DSBindingKey pattern in WSDL which is a string of the form:
         // (DSBindingKey). Rows in DisseminationBindingInfo are sorted
         // alphabetically on binding key.
         String bindingKeyPattern = "\\(" + dissBindInfo.DSBindKey + "\\)";
         if (i == 0)
         {
           // If addressLocation has a value of "LOCAL", this indicates
           // the associated operationLocation requires no addressLocation.
           // i.e., the operationLocation contains all information necessary
           // to perform the dissemination request. This is a special case
           // used when the web services are generally mechanisms like cgi-scripts,
           // java servlets, and simple HTTP GETs. Using the value of LOCAL
           // in the address location also enables one to have different methods
           // serviced by different hosts. In true web services like SOAP, the
           // addressLocation specifies the host name of the service and all
           // methods are served from that single host location.
           if (dissBindInfo.AddressLocation.equalsIgnoreCase(LOCAL_ADDRESS_LOCATION))
           {
             dissURL = dissBindInfo.OperationLocation;
           } else
           {
             dissURL = dissBindInfo.AddressLocation+dissBindInfo.OperationLocation;
           }
           protocolType = dissBindInfo.ProtocolType;
         }
         
         // Assess beSecurity for backend service and for datastreams that may be parameters for the
         // backend service.
         //
         // dsMediatedCallbackHost - when dsMediation is in effect, all M, X, and E type datastreams
         //                          are encoded as callbacks to the Fedora server to obtain the
         //                          datastream's contents. dsMediatedCallbackHost contains protocol, 
         //                          host, and port used for this type of backendservice-to-fedora callback.
         //                          The specifics of protocol, host, and port are obtained from the
         //                          beSecurity configuration file.
         // dsMediatedServletPath - when dsMediation is in effect, all M, X, and E type datastreams
         //                         are encoded as callbacks to the Fedora server to obtain the
         //                         datastream's contents. dsMediatedServletPath contains the servlet
         //                         path info for this type of backendservice-to-fedora callback.
         //                         The specifics of servlet path are obtained from the beSecurity configuration
         //                         file and determines whether the backedservice-to-fedora callback
         //                         will use authentication or not.
         // callbackRole - contains the role of the backend service (the bMechPid of the service).
 
         String callbackRole = bMechPid;
         Hashtable beHash = m_beSS.getSecuritySpec(callbackRole, methodName);
         boolean callbackBasicAuth = new Boolean((String) beHash.get("callbackBasicAuth")).booleanValue();
         boolean callbackSSL = new Boolean((String) beHash.get("callbackSSL")).booleanValue();
         String dsMediatedServletPath = null;
         if (callbackBasicAuth) {
             dsMediatedServletPath = "/fedora/getDSAuthenticated?id=";
         } else {
             dsMediatedServletPath = "/fedora/getDS?id=";
         }
         String dsMediatedCallbackHost = null;
         if (callbackSSL) {
             dsMediatedCallbackHost = "https://"+fedoraServerHost+":"+fedoraServerRedirectPort;
         } else {
             dsMediatedCallbackHost = "http://"+fedoraServerHost+":"+fedoraServerPort;
         }
         String datastreamResolverServletURL = dsMediatedCallbackHost + dsMediatedServletPath;        
         if (LOG.isDebugEnabled()) {
             LOG.debug("******************Checking backend service dsLocation: "+dissBindInfo.dsLocation);
             LOG.debug("******************Checking backend service dsControlGroupType: "+dissBindInfo.dsControlGroupType);
             LOG.debug("******************Checking backend service callbackBasicAuth: "+callbackBasicAuth);
             LOG.debug("******************Checking backend service callbackSSL: "+callbackSSL);
             LOG.debug("******************Checking backend service callbackRole: "+callbackRole);
             LOG.debug("******************DatastreamResolverServletURL: "+datastreamResolverServletURL);            
         }           		                    
         
         String currentKey = dissBindInfo.DSBindKey;
         String nextKey = "";
         if (i != numElements-1)
         {
           // Except for last row, get the value of the next binding key
           // to compare with the value of the current binding key.
           nextKey = dissBindInfoArray[i+1].DSBindKey;
         }
         LOG.debug("currentKey: '" + currentKey + "', nextKey: '" + nextKey + "'");
         // In most cases, there is only a single datastream that matches a
         // given DSBindingKey so the substitution process is to just replace
         // the occurence of (BINDING_KEY) with the value of the datastream
         // location. However, when multiple datastreams match the same
         // DSBindingKey, the occurrence of (BINDING_KEY) is replaced with the
         // value of the datastream location and the value +(BINDING_KEY) is
         // appended so that subsequent datastreams matching the binding key
         // will be substituted. The end result is that the binding key will
         // be replaced by a series of datastream locations separated by a
         // plus(+) sign. For example, in the case where 3 datastreams match
         // the binding key for PHOTO:
         //
         // file=(PHOTO) becomes
         // file=dslocation1+dslocation2+dslocation3
         //
         // It is the responsibility of the Behavior Mechanism to know how to
         // handle an input parameter with multiple datastream locations.
         //
         // In the case of a method containing multiple binding keys,
         // substitutions are performed on each binding key. For example, in
         // the case where there are 2 binding keys named PHOTO and WATERMARK
         // where each matches a single datastream:
         //
         // image=(PHOTO)&watermark=(WATERMARK) becomes
         // image=dslocation1&watermark=dslocation2
         //
         // In the case with mutliple binding keys and multiple datastreams,
         // the substitution might appear like the following:
         //
         // image=(PHOTO)&watermark=(WATERMARK) becomes
         // image=dslocation1+dslocation2&watermark=dslocation3
         if (nextKey.equalsIgnoreCase(currentKey) & i != numElements)
         {
           // Case where binding keys are equal which means that multiple
           // datastreams matched the same binding key.
           if (doDatastreamMediation  &&
               !dissBindInfo.dsControlGroupType.equalsIgnoreCase("R"))
           {
             // Use Datastream Mediation (except for redirected datastreams).
            
             replaceString = datastreamResolverServletURL
                 + registerDatastreamLocation(dissBindInfo.dsLocation,
                                            dissBindInfo.dsControlGroupType,
 										   callbackRole, methodName)
                 + "+(" + dissBindInfo.DSBindKey + ")";
           } else
           {
             // Bypass Datastream Mediation.
             if ( dissBindInfo.dsControlGroupType.equalsIgnoreCase("M") ||
                  dissBindInfo.dsControlGroupType.equalsIgnoreCase("X"))
             {
                 // Use the Default Disseminator syntax to resolve the internal
                 // datastream location for Managed and XML datastreams.
                 replaceString =
                     resolveInternalDSLocation(context, dissBindInfo.dsLocation, PID, dsMediatedCallbackHost)
                         + "+(" + dissBindInfo.DSBindKey + ")";;
             } else {
                 replaceString =
                         dissBindInfo.dsLocation + "+(" + dissBindInfo.DSBindKey + ")";
             }
             if (dissBindInfo.dsControlGroupType.equalsIgnoreCase("R") &&
                 dissBindInfo.AddressLocation.equals(LOCAL_ADDRESS_LOCATION))
                 isRedirect = true;
           }
         } else
         {
           // Case where there are one or more binding keys.
           if (doDatastreamMediation &&
               !dissBindInfo.dsControlGroupType.equalsIgnoreCase("R"))
           {
             // Use Datastream Mediation (except for Redirected datastreams)              
             replaceString = datastreamResolverServletURL
                 + registerDatastreamLocation(dissBindInfo.dsLocation,
                   dissBindInfo.dsControlGroupType,
             		  callbackRole, methodName); //this is generic, should be made specific per service                        
           } else
           {
             // Bypass Datastream Mediation.
             if ( dissBindInfo.dsControlGroupType.equalsIgnoreCase("M") ||
                  dissBindInfo.dsControlGroupType.equalsIgnoreCase("X"))
             {
                 // Use the Default Disseminator syntax to resolve the internal
                 // datastream location for Managed and XML datastreams.
                 replaceString =
                     resolveInternalDSLocation(context, dissBindInfo.dsLocation, PID, dsMediatedCallbackHost);
             } else
             {
                 replaceString = dissBindInfo.dsLocation;
             }
             if (dissBindInfo.dsControlGroupType.equalsIgnoreCase("R") &&
                 dissBindInfo.AddressLocation.equals(LOCAL_ADDRESS_LOCATION))
                     isRedirect = true;
           }
         }
         try
         {
           // If the operationLocation contains datastreamInputParms
           // URLEncode each parameter before substitution. Otherwise, the
           // operationLocation has no parameters (i.e., it is a simple URL )
           // so bypass URLencoding.
           if (dissURL.indexOf("=(") != -1 )
           {
             dissURL = substituteString(dissURL, bindingKeyPattern, URLEncoder.encode(replaceString, "UTF-8"));
           } else
           {
             dissURL = substituteString(dissURL, bindingKeyPattern, replaceString);
           }
         } catch (UnsupportedEncodingException uee)
         {
           String message = "[DisseminationService] An error occured. The error "
               + "was \"" + uee.getClass().getName() + "\"  . The Reason was \""
               + uee.getMessage() + "\"  . String value: " + replaceString + "  . ";
           LOG.error(message);
           throw new GeneralException(message);
         }
         LOG.debug("Replaced dissURL: " + dissURL.toString() 
                 + " DissBindingInfo index: " + i);
       }
 
       // Substitute method parameter values in dissemination URL
       Enumeration e = h_userParms.keys();
       while (e.hasMoreElements())
       {
         String name = null;
         String value = null;
         try
         {
           name = URLEncoder.encode((String)e.nextElement(), "UTF-8");
           value = URLEncoder.encode((String)h_userParms.get(name), "UTF-8");
         } catch (UnsupportedEncodingException uee)
         {
           String message = "[DisseminationService] An error occured. The error "
               + "was \"" + uee.getClass().getName() + "\"  . The Reason was \""
               + uee.getMessage() + "\"  . Parameter name: " + name + "  . "
               + "Parameter value: " + value + "  .";
           LOG.error(message);
           throw new GeneralException(message);
         }
         String pattern = "\\(" + name + "\\)";
         dissURL = substituteString(dissURL, pattern, value);
         LOG.debug("User parm substituted in URL: " + dissURL);
       }
 
       // FIXME Need a more elegant means of handling optional userInputParm
       // method parameters that are not supplied by the invoking client;
       // for now, any optional parms that were not supplied are removed from
       // the outgoing URL. This works because parms are validated in
       // DefaultAccess to insure all required parms are present and all parm
       // names match parm names defined for the specific method. The only
       // unsubstituted parms left in the operationLocation string at this point
       // are those for optional parameters that the client omitted in the
       // initial request so they can safely be removed from the outgoing
       // dissemination URL. This step is only needed when optional parameters
       // are not supplied by the client.
       if (dissURL.indexOf("(") != -1)
       {
           dissURL = stripParms(dissURL);
           LOG.debug("Non-supplied optional userInputParm values removed "
                   + "from URL: " + dissURL);
       }
 
       // Resolve content referenced by dissemination result.
       LOG.debug("ProtocolType: " + protocolType);
       if (protocolType.equalsIgnoreCase("http"))
       {
 
         if (isRedirect)
         {
           // The dsControlGroupType of Redirect("R") is a special control type
           // used primarily for streaming media. Datastreams of this type are
           // not mediated (proxied by Fedora) and their physical dsLocation is
           // simply redirected back to the client. Therefore, the contents
           // of the MIMETypedStream returned for dissemination requests will
           // contain the raw URL of the dsLocation and will be assigned a
           // special fedora-specific MIME type to identify the stream as
           // a MIMETypedStream whose contents contain a URL to which the client
           // should be redirected.
 
           InputStream is = null;
           try
           {
             is = new ByteArrayInputStream(dissURL.getBytes("UTF-8"));
           } catch (UnsupportedEncodingException uee)
           {
             String message = "[DisseminationService] An error has occurred. "
                 + "The error was a \"" + uee.getClass().getName() + "\"  . The "
                 + "Reason was \"" + uee.getMessage() + "\"  . String value: "
                 + dissURL + "  . ";
             LOG.error(message);
             throw new GeneralException(message);
           }
           LOG.debug("Finished assembling dissemination");
           dissemination = new MIMETypedStream("application/fedora-redirect",is, null);
         } else
         {
           // For all non-redirected disseminations, Fedora captures and returns
           // the MIMETypedStream resulting from the dissemination request.
           //ExternalContentManager externalContentManager = (ExternalContentManager)
           //    s_server.getModule("fedora.server.storage.ExternalContentManager");
           LOG.debug("Finished assembling dissemination");
           LOG.debug("URL: "+dissURL);
 	        
           // See if backend service reference is to fedora server itself or an external location.
           // We must examine URL to see if this is referencing a remote backend service or is
           // simply a callback to the fedora server. If the reference is remote, then use
           // the role of backend service bMechPid. If the referenc is to the fedora server, 
           // use the special role of "fedoraInternalCall-1" to denote that the callback will come from the 
           // fedora server itself.          
 	        String beServiceRole = null;
 	        if ( ServerUtility.isURLFedoraServer(dissURL) ) {
 	            beServiceRole =  BackendPolicies.FEDORA_INTERNAL_CALL;
 	        } else {
 	            beServiceRole = bMechPid;
 	        }
 	        
 	        // Get basicAuth and SSL info about the backend service and use this info to configure the
 	        // "call" to the backend service.
 	        Hashtable beHash = m_beSS.getSecuritySpec(beServiceRole, methodName);
 	        boolean beServiceCallSSL = new Boolean((String) beHash.get("callSSL")).booleanValue();
 	        String beServiceCallUsername = "";
 	        String beServiceCallPassword = "";	
 	        boolean beServiceCallBasicAuth = new Boolean((String) beHash.get("callBasicAuth")).booleanValue();
 	        if (beServiceCallBasicAuth) {
 	        	beServiceCallUsername = (String) beHash.get("callUsername");
 	        	beServiceCallPassword = (String) beHash.get("callPassword");	
 	        }
 	        
 	        /*
 	        //fixup:
 	        if (BackendPolicies.FEDORA_INTERNAL_CALL.equals(beServiceRole)) {
 	        	if (beServiceCallSSL) {
 			    	if (dissURL.startsWith("http:")) {
 			    		dissURL = dissURL.replaceFirst("http:", "https:");
 			    	}
 			    	if (dissURL.indexOf(":"+fedoraServerPort+"/") >= 0) {
 				    	dissURL = dissURL.replaceFirst(":"+fedoraServerPort+"/", ":"+fedoraServerRedirectPort+"/");			    		
 			    	} 
 	        	} else {
 	        		if (dissURL.startsWith("https:")) {
 	        			dissURL = dissURL.replaceFirst("https:", "http:");			    		
 	        		}
 	        		if (dissURL.indexOf(":"+fedoraServerRedirectPort+"/") >= 0) {
 				    	dissURL = dissURL.replaceFirst(":"+fedoraServerRedirectPort+"/", ":"+fedoraServerPort+"/");
 			    	}
 	        	}
 	        	if (beServiceCallBasicAuth) {
 			    	if (dissURL.indexOf("getDS?") >= 0) {
 				    	dissURL = dissURL.replaceFirst("getDS\\?", "getDSAuthenticated\\?");			    		
 			    	}	        		
 	        	} else {
 			    	if (dissURL.indexOf("getDSAuthenticated?") >= 0) {
 				    	dissURL = dissURL.replaceFirst("getDSAuthenticated\\?", "getDS\\?");			    		
 			    	}	        			        		
 	        	}
 	        	
 		    } 
 		    */
         
 	        if (LOG.isDebugEnabled()) {
 	            LOG.debug("******************getDisseminationContent beServiceRole: "+beServiceRole);
 	            LOG.debug("******************getDisseminationContent beServiceCallBasicAuth: "+beServiceCallBasicAuth);
 	            LOG.debug("******************getDisseminationContent beServiceCallSSL: "+beServiceCallSSL);
 	            LOG.debug("******************getDisseminationContent beServiceCallUsername: "+beServiceCallUsername);
 	            LOG.debug("******************getDisseminationContent beServiceCallPassword: "+beServiceCallPassword);
 	            LOG.debug("******************getDisseminationContent dissURL: "+dissURL);	            
 	        }    	        
 	        
 	        // Dispatch backend service URL request authenticating as necessary based on beSecurity configuration
           dissemination = getDisseminationContent(dissURL, context, beServiceCallUsername, beServiceCallPassword);
         }
 
       } else if (protocolType.equalsIgnoreCase("soap"))
       {
         // FIXME!! future handling of soap bindings.
         String message = "[DisseminationService] Protocol type: "
             + protocolType + "NOT yet implemented";
         LOG.error(message);
         throw new DisseminationException(message);
 
       } else
       {
         String message = "[DisseminationService] Protocol type: "
             + protocolType + "NOT supported.";
         LOG.error(message);
         throw new DisseminationException(message);
       }
 
     } else
     {
       // DisseminationBindingInfo was empty so there was no information
       // provided to construct a dissemination.
       String message = "[DisseminationService] Dissemination Binding "+
                          "Info contained no data";
       LOG.error(message);
       throw new DisseminationBindingInfoNotFoundException(message);
     }
     return dissemination;
   }
 
 
   /**
    * <p>Datastream locations are considered privileged information by the
    * Fedora repository. To prevent disclosing physical datastream locations
    * to external mechanism services, a proxy is used to disguise the datastream
    * locations. This method generates a temporary ID that maps to the
    * physical datastream location and registers this information in a
    * memory resident hashtable for subsequent resolution of the physical
    * datastream location. The servlet <code>DatastreamResolverServlet</code>
    * provides the proxy resolution service for datastreams.</p>
    * <p></p>
    * <p>The format of the tempID is derived from <code>java.sql.Timestamp</code>
    * with an arbitrary counter appended to the end to insure uniqueness. The
    * syntax is of the form:
    * <ul>
    * <p>YYYY-MM-DD HH:mm:ss.mmm:dddddd where</p>
    * <ul>
    * <li>YYYY - year (1900-8099)</li>
    * <li>MM - month (01-12)</li>
    * <li>DD - day (01-31)</li>
    * <li>hh - hours (0-23)</li>
    * <li>mm - minutes (0-59)</li>
    * <li>ss - seconds (0-59)</li>
    * <li>mmm - milliseconds (0-999)</li>
    * <li>dddddd - incremental counter (0-999999)</li>
    * </ul>
    * </ul>
    *
    * @param dsLocation The physical location of the datastream.
    * @param dsControlGroupType The type of the datastream.
    * @return A temporary ID used to reference the physical location of the
    *         specified datastream
    * @throws ServerException If an error occurs in registering a datastream
    *         location.
    */
   public String registerDatastreamLocation(String dsLocation,
       String dsControlGroupType, String beServiceCallbackRole, String methodName) throws ServerException
   {
 
     String tempID = null;
     Timestamp timeStamp = null;
     if (counter > 999999) counter = 0;
     long currentTime = new Timestamp(new Date().getTime()).getTime();
     long expireLimit = currentTime -
                        (long)datastreamExpirationLimit*1000;
     String dsMediatedServletPath = null;
     String dsMediatedCallbackHost = null;
     
     try
     {
 
       // Remove any datastream registrations that have expired.
       // The expiration limit can be adjusted using the Fedora config parameter
       // named "datastreamExpirationLimit" which is in seconds.
       for ( Enumeration e = dsRegistry.keys(); e.hasMoreElements(); )
       {
         String key = (String)e.nextElement();
         timeStamp = Timestamp.valueOf(extractTimestamp(key));
         if (expireLimit > timeStamp.getTime())
         {
           dsRegistry.remove(key);
           LOG.debug("DatastreamMediationKey removed from Hash: " + key);
         }
       }
       
       // Register datastream.
       if (tempID == null)
       {
         timeStamp = new Timestamp(new Date().getTime());
         tempID = timeStamp.toString()+":"+counter++;
         DatastreamMediation dm = new DatastreamMediation();
         dm.mediatedDatastreamID = tempID;
         dm.dsLocation = dsLocation;
         dm.dsControlGroupType = dsControlGroupType; 
         dm.methodName = methodName;
         
         // See if datastream reference is to fedora server itself or an external location.
         // M and X type datastreams always reference fedora server. With E type datastreams
         // we must examine URL to see if this is referencing a remote datastream or is
         // simply a callback to the fedora server. If the reference is remote, then use
         // the role of the backend service that will make a callback for this datastream.
         // If the referenc s to the fedora server, use the special role of "fedoraInternalCall-1" to
         // denote that the callback will come from the fedora server itself.
         String beServiceRole = null;
         if ( ServerUtility.isURLFedoraServer(dsLocation)  || 
              dsControlGroupType.equals("M") ||
              dsControlGroupType.equals("X") ) {
             beServiceRole =  BackendPolicies.FEDORA_INTERNAL_CALL;
         } else {
             beServiceRole = beServiceCallbackRole;
         }        
         
         // Store beSecurity info in hash 
         Hashtable beHash = m_beSS.getSecuritySpec(beServiceRole, methodName);
         boolean beServiceCallbackBasicAuth = new Boolean((String) beHash.get("callbackBasicAuth")).booleanValue();
         boolean beServiceCallBasicAuth = new Boolean((String) beHash.get("callBasicAuth")).booleanValue();
         boolean beServiceCallbackSSL = new Boolean((String) beHash.get("callbackSSL")).booleanValue();
         boolean beServiceCallSSL = new Boolean((String) beHash.get("callSSL")).booleanValue();
         String beServiceCallUsername = (String) beHash.get("callUsername");
         String beServiceCallPassword = (String) beHash.get("callPassword");
         if (LOG.isDebugEnabled()) {
             LOG.debug("******************Registering datastream dsLocation: "+dsLocation);
             LOG.debug("******************Registering datastream dsControlGroupType: "+dsControlGroupType);
             LOG.debug("******************Registering datastream beServiceRole: "+beServiceRole);
             LOG.debug("******************Registering datastream beServiceCallbackBasicAuth: "+beServiceCallbackBasicAuth);
             LOG.debug("******************Registering datastream beServiceCallBasicAuth: "+beServiceCallBasicAuth);
             LOG.debug("******************Registering datastream beServiceCallbackSSL: "+beServiceCallbackSSL);
             LOG.debug("******************Registering datastream beServiceCallSSL: "+beServiceCallSSL);
             LOG.debug("******************Registering datastream beServiceCallUsername: "+beServiceCallUsername);
             LOG.debug("******************Registering datastream beServiceCallPassword: "+beServiceCallPassword);
         }           		                
         dm.callbackRole = beServiceRole;
         dm.callUsername = beServiceCallUsername;
         dm.callPassword = beServiceCallPassword;
         dm.callbackBasicAuth = beServiceCallbackBasicAuth;
         dm.callBasicAuth = beServiceCallBasicAuth;
         dm.callbackSSL = beServiceCallbackSSL;
         dm.callSSL = beServiceCallSSL;
         dsRegistry.put(tempID, dm);
         LOG.debug("DatastreammediationKey added to Hash: " + tempID);
       }
 
     } catch(Throwable th)
     {
       throw new DisseminationException("[DisseminationService] register"
           + "DatastreamLocation: "
           + "returned an error. The underlying error was a "
           + th.getClass().getName() + " The message "
           + "was \"" + th.getMessage() + "\" .");
     }
 
     // Replace the blank between date and time with the character "T".
     return tempID.replaceAll(" ","T");
   }
 
   /**
    * <p>The tempID that is used for datastream mediation consists of a <code>
    * Timestamp</code> plus a counter appended to the end to insure uniqueness.
    * This method is a utility method used to extract the Timestamp portion
    * from the tempID by stripping off the arbitrary counter at the end of
    * the string.</p>
    *
    * @param tempID The tempID to be extracted.
    * @return The extracted Timestamp value as a string.
    */
   public String extractTimestamp(String tempID)
   {
     StringBuffer sb = new StringBuffer();
     sb.append(tempID);
     sb.replace(tempID.lastIndexOf(":"),tempID.length(),"");
     return sb.toString();
   }
 
   /**
    * <p>Performs simple string replacement using regular expressions.
    * All matching occurrences of the pattern string will be replaced in the
    * input string by the replacement string.
    *
    * @param inputString The source string.
    * @param patternString The regular expression pattern.
    * @param replaceString The replacement string.
    * @return The source string with substitutions.
    */
   private String substituteString(String inputString, String patternString,
                                  String replaceString)
   {
     Pattern pattern = Pattern.compile(patternString);
     Matcher m = pattern.matcher(inputString);
     return m.replaceAll(replaceString);
   }
 
   /**
    * <p> Removes any optional userInputParms which remain in the dissemination
    * URL. This occurs when a method has optional parameters and the user does
    * not supply a value for one or more of the optional parameters. The result
    * is a syntax similar to "parm=(PARM_BIND_KEY)". This method removes these
    * non-supplied optional parameters from the string.</p>
    *
    * @param dissURL String to be processed.
    * @return An edited string with parameters removed where no value was
    *         specified for any optional parameters.
    */
   private String stripParms(String dissURL)
   {
     String requestURI = dissURL.substring(0,dissURL.indexOf("?")+1);
     String parmString = dissURL.substring(dissURL.indexOf("?")+1,dissURL.length());
     String[] parms = parmString.split("&");
     StringBuffer sb = new StringBuffer();
     for (int i=0; i<parms.length; i++)
     {
       int len = parms[i].length() - 1;
       if (parms[i].lastIndexOf(")") != len)
       {
         sb.append(parms[i]+"&");
       }
     }
     int index = sb.lastIndexOf("&");
     if ( index != -1 && index+1 == sb.length())
       sb.replace(index,sb.length(),"");
     return requestURI+sb.toString();
   }
 
   /**
    * <p>Converts the internal dsLocation used by managed and XML type datastreams
    * to the corresponding Default Dissemination request that will return the
    * datastream contents.</p>
    *
    * @param internalDSLocation - dsLocation of the Managed or XML type datastream.
    * @param PID - the persistent identifier of the digital object.
    * @return - A URL corresponding to the Default Dissemination request for the
    *           specified datastream.
    * @throws ServerException - If anything goes wrong during the conversion attempt.
    */
   private String resolveInternalDSLocation(Context context, String internalDSLocation,
       String PID, String callbackHost) throws ServerException
   {
       
       if (callbackHost == null || callbackHost.equals(""))
       {
         throw new DisseminationException("[DisseminationService] was unable to "
             + "resolve the base URL of the Fedora Server. The URL specified was: \""
             + callbackHost + "\". This information is required by the Dissemination Service.");
       }  
       
       String[] s = internalDSLocation.split("\\+");
       String dsLocation = null;
       if (s.length == 3)
       {
           DOReader doReader =  m_manager.getReader(Server.GLOBAL_CHOICE, context, PID);
           Datastream d = (Datastream) doReader.getDatastream(s[1], s[2]);
           dsLocation = 
               callbackHost
               +"/fedora/get/"+s[0]+"/"+s[1]+"/"
               +DateUtility.convertDateToString(d.DSCreateDT);
           
       } else
       {
         String message = "[DisseminationService] An error has occurred. "
             + "The internal dsLocation: \"" + internalDSLocation + "\" is "
             + "not in the required format of: "
             + "\"doPID+DSID+DSVERSIONID\" .";
         LOG.error(message);
         throw new GeneralException(message);
       }
       LOG.debug("********** Resolving Internal Datastream dsLocation: "+dsLocation);
       return dsLocation;
   }
 
   public static void printBindingInfo(DisseminationBindingInfo[] info) {
     for (int i = 0; i < info.length; i++) {
       LOG.debug("DisseminationBindingInfo[" + i + "]:");
       LOG.debug("  DSBindKey          : " + info[i].DSBindKey);
       LOG.debug("  dsLocation         : " + info[i].dsLocation);
       LOG.debug("  dsControlGroupType : " + info[i].dsControlGroupType);
       LOG.debug("  dsID               : " + info[i].dsID);
       LOG.debug("  dsVersionID        : " + info[i].dsVersionID);
       LOG.debug("  AddressLocation    : " + info[i].AddressLocation);
       LOG.debug("  OperationLocation  : " + info[i].OperationLocation);
       LOG.debug("  ProtocolType       : " + info[i].ProtocolType);
       LOG.debug("  dsState            : " + info[i].dsState);
       for (int j = 0; j < info[i].methodParms.length; j++) {
         MethodParmDef def = info[i].methodParms[j];
         LOG.debug("  MethodParamDef[" + j + "]:");
         LOG.debug("    parmName         : " + def.parmName);
         LOG.debug("    parmDefaultValue : " + def.parmDefaultValue);
         LOG.debug("    parmRequired     : " + def.parmRequired);
         LOG.debug("    parmLabel        : " + def.parmLabel);
         LOG.debug("    parmPassBy       : " + def.parmPassBy);
         for (int k = 0; k < def.parmDomainValues.length; k++) {
           LOG.debug("    parmDomainValue  : " + def.parmDomainValues[k]);
         }
       }
     }
   }
 
     /**
      * Get a MIMETypedStream for the given URL.
      *
      * If user or password are <code>null</code>, basic authentication will 
      * not be attempted.
      */
     private static MIMETypedStream get(String url,
                                        String user,
                                        String pass) throws GeneralException {
         LOG.debug("DisseminationService.get(" + url + ")");
         try {
            HttpInputStream response = s_http.get(url, true, user, pass);
             String mimeType = response.getResponseHeaderValue("Content-Type",
                                                               "text/plain");
             Property[] headerArray = toPropertyArray(
                                          response.getResponseHeaders());
   		    return new MIMETypedStream(mimeType, response, headerArray);
         } catch (Exception e) {
             throw new GeneralException("Error getting " + url, e);
         }
     }
 
     /**
      * Convert the given HTTP <code>Headers</code> to an array of 
      * <code>Property</code> objects.
      */
     private static Property[] toPropertyArray(Header[] headers) {
 
         Property[] props = new Property[headers.length];
         for (int i = 0; i < headers.length; i++) {
             props[i] = new Property();
             props[i].name = headers[i].getName();
             props[i].value = headers[i].getValue();
         }
         return props;
     }
   
   /**
    * A method that reads the contents of the specified URL and returns the
    * result as a MIMETypedStream
    *
    * @param url The URL of the external content.
    * @return A MIME-typed stream.
    * @throws HttpServiceNotFoundException If the URL connection could not
    *         be established.
    */
   public MIMETypedStream getDisseminationContent(String url, Context context, String user, String pass)
       throws GeneralException, HttpServiceNotFoundException {
     return get(url, user, pass);
   }  
   
 }
