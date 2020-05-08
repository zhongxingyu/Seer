 package fedora.soapclient;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PipedReader;
 import java.io.PipedWriter;
 import java.net.URL;
 import java.net.URLDecoder;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.xml.namespace.QName;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Properties;
 
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 
 import com.icl.saxon.expr.StringValue;
 
 import fedora.server.types.gen.FieldSearchQuery;
 import fedora.server.types.gen.FieldSearchResult;
 import fedora.server.types.gen.MethodDef;
 import fedora.server.types.gen.MethodParmDef;
 import fedora.server.types.gen.MIMETypedStream;
 import fedora.server.types.gen.ObjectMethodsDef;
 import fedora.server.types.gen.ObjectProfile;
 import fedora.server.types.gen.RepositoryInfo;
 import fedora.server.types.gen.Property;
 import fedora.server.utilities.DateUtility;
 
 import org.apache.axis.client.Service;
 import org.apache.axis.client.Call;
 import org.apache.axis.encoding.ser.BeanSerializerFactory;
 import org.apache.axis.encoding.ser.BeanDeserializerFactory;
 
 /**
  * <p><b>Title: </b>FedoraAccessSoapServlet.java</p>
  * <p><b>Description: </b>An example of a web-based client that provides a front end
  * to the Fedora Access SOAP service. This servlet is designed to provide a
  * "browser centric" view of the Fedora Access interface. Return types from
  * the Fedora Access SOAP service are translated into a form suitable for
  * viewing with a web browser; in other words MIME-typed streams. Applications
  * that can readily handle SOAP requests and responses would most likely
  * communicate directly with the Fedora Access SOAP service rather than use a
  * java servlet as an intermediary. This servlet serves as an example of how to
  * construct a client that uses the Fedora Access API via SOAP.</p>
  *
  * <p>Input parameters for the servlet include:</p>
  * <ul>
  * <li>action_ name of Fedora service which must be one of the following:
  * <ol>
  * <li>GetBehaviorDefinitions - Gets list of Behavior Defintions</li>
  * <li>GetBehaviorMethods - Gets list of Behavior Methods</li>
  * <li>GetBehaviorMethodsXML - Gets Behavior Methods as XML</li>
  * <li>GetDissemination - Gets a dissemination result</li>
  * <li>GetObjectmethods - Gets a list of all Behavior Methods of an object.</li>
  * <li>GetObjectProfile - Gets object profile.</li>
  * <li>DescribeRepository - Gets information about the repository server.</li>
  * </ol>
  * <li>PID_ - persistent identifier of the digital object</li>
  * <li>bDefPID_ - persistent identifier of the Behavior Definiton object</li>
  * <li>methodName_ - name of the method</li>
  * <li>asOfDateTime_ - versioning datetime stamp</li>
  * <li>xml_ - boolean switch used in conjunction with GetObjectMethods,
  *                  GetObjectProfile, and DescribeRepository
  *                  that determines whether output is formatted as XML or
  *                  as HTML; value of "true" indicates XML format; value of
  *                  false or omission indicates HTML format.
  * <li>userParms - behavior methods may require or provide optional parameters
  *                 that may be input as arguments to the method; these method
  *                 parameters are entered as name/value pairs like the other
  *                 serlvet parameters.(optional)</li>
  * </ul>
  * <p><i>Note that all servlet parameter names that are implementation
  * specific end with the underscore character ("_"). This is done to avoid
  * possible name clashes with user-supplied method parameter names. As a
  * general rule, user-supplied parameters should never contain names that end
  * with the underscore character to prevent possible name conflicts.</i>
  * </ul>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author rlw@virginia.edu
  * @version $Id$
  */
 public class FedoraAccessSoapServlet extends HttpServlet
 {
 
   /** Content type for html. */
   private static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";
 
   /** Content type for xml. */
   private static final String CONTENT_TYPE_XML  = "text/xml; charset=UTF-8";
 
   /** URI of Fedora API definitions. */
   private static final String FEDORA_API_URI =
       "http://www.fedora.info/definitions/1/0/api/";
 
   /** URI of Fedora Type definitions. */
   private static final String FEDORA_TYPE_URI =
       "http://www.fedora.info/definitions/1/0/types/";
 
   /** GetBehaviorDefinitions service name. */
   private static final String GET_BEHAVIOR_DEFINITIONS =
       "GetBehaviorDefinitions";
 
   /** GetBehaviorMethods service name. */
   private static final String GET_BEHAVIOR_METHODS =
       "GetBehaviorMethods";
 
   /** GetBehaviorMethodsXML service name. */
   private static final String GET_BEHAVIOR_METHODS_XML =
       "GetBehaviorMethodsXML";
 
   /** GetDissemination service name. */
   private static final String GET_DISSEMINATION =
       "GetDissemination";
 
   /** GetObjectMethods service name. */
   private static final String GET_OBJECT_METHODS =
       "GetObjectMethods";
 
   /** GetObjectProfile service name. */
   private static final String GET_OBJECT_PROFILE =
       "GetObjectProfile";
 
   /** GetObjectProfile service name. */
   private static final String DESCRIBE_REPOSITORY =
       "DescribeRepository";
 
   /** Properties file for soap client */
   private static final String soapClientPropertiesFile =
       "WEB-INF/soapclient.properties";
 
   /** URI of Fedora Access SOAP service. */
   private static String FEDORA_ACCESS_ENDPOINT = null;
 
   /** Servlet mapping for this servlet */
   private static String SOAP_CLIENT_SERVLET_PATH = null;
 
   /** Servlet mapping for MethodParmResolverServlet */
   private static String METHOD_PARM_RESOLVER_SERVLET_PATH = null;
 
   /** User-supplied method parameters from servlet URL. */
   private Hashtable h_userParms = null;
 
   /** Host name of the Fedora server **/
   private static String fedoraServerHost = null;
 
   /** Port number on which the Fedora server is running. **/
   private static String fedoraServerPort = null;
 
   /**
    * <p>Process Fedora Access Request. Parse and validate the servlet input
    * parameters and then execute the specified request by calling the
    * appropriate Fedora Access SOAP service.</p>
    *
    * @param request  The servlet request.
    * @param response servlet The servlet response.
    * @throws ServletException If an error occurs that effects the servlet's
    *         basic operation.
    * @throws IOException If an error occurs with an input or output operation.
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
     Calendar asOfDateTime = null;
     Date versDateTime = null;
     String action = null;
     String bDefPID = null;
     String methodName = null;
     String PID = null;
     Property[] userParms = null;
     URLDecoder decoder = new URLDecoder();
     boolean xml = false;
     long servletStartTime = new Date().getTime();
     h_userParms = new Hashtable();
     //ServletOutputStream out = response.getOutputStream();
 
     // Get servlet input parameters.
     Enumeration URLParms = request.getParameterNames();
     while ( URLParms.hasMoreElements())
     {
       String parm = decoder.decode((String) URLParms.nextElement(), "UTF-8");
       if (parm.equals("action_"))
       {
         action = request.getParameter(parm);
       } else if (parm.equals("PID_"))
       {
         PID = decoder.decode(request.getParameter(parm), "UTF-8");
       } else if (parm.equals("bDefPID_"))
       {
         bDefPID = decoder.decode(request.getParameter(parm), "UTF-8");
       } else if (parm.equals("methodName_"))
       {
         methodName = decoder.decode(request.getParameter(parm), "UTF-8");
       } else if (parm.equals("asOfDateTime_"))
       {
         asOfDateTime = DateUtility.
                    convertStringToCalendar(request.getParameter(parm));
       } else if (parm.equals("xml_"))
       {
         xml = new Boolean(request.getParameter(parm)).booleanValue();
       } else
       {
         // Any remaining parameters are assumed to be user-supplied method
         // parameters. Place user-supplied parameters in hashtable for
         // easier access.
         h_userParms.put(parm, decoder.decode(request.getParameter(parm), "UTF-8"));
       }
     }
 
     // API-A interface requires user-supplied parameters to be of type
     // Property[]; create Property[] from hashtable of user parameters.
     int userParmCounter = 0;
     if ( !h_userParms.isEmpty() )
     {
       userParms = new Property[h_userParms.size()];
       for ( Enumeration e = h_userParms.keys(); e.hasMoreElements();)
       {
         Property userParm = new Property();
         userParm.setName((String)e.nextElement());
         userParm.setValue((String)h_userParms.get(userParm.getName()));
         userParms[userParmCounter] = userParm;
         userParmCounter++;
       }
     }
 
     // Validate servlet URL parameters to verify that all parameters required
     // by the servlet are present and to verify that any other user-supplied
     // parameters are valid for the request.
     if (isValidURLParms(action, PID, bDefPID, methodName, versDateTime,
                       h_userParms, response))
     {
       // Have valid request.
       if (action.equals(GET_BEHAVIOR_DEFINITIONS))
       {
         String[] behaviorDefs = null;
         ObjectProfile objProfile = null;
         PipedWriter pw = null;
         PipedReader pr = null;
         OutputStreamWriter out = null;
         try
         {
 
           //out = response.getOutputStream();
           pw = new PipedWriter();
           pr = new PipedReader(pw);
           behaviorDefs = getBehaviorDefinitions(PID, asOfDateTime);
           if (behaviorDefs != null)
           {
             // Object Methods found.
             // Deserialize ObjectmethodsDef datastructure into XML
             new BehaviorDefinitionsSerializerThread(PID, behaviorDefs, versDateTime, pw).start();
             if (xml)
             {
               // Return results as raw XML
               response.setContentType(CONTENT_TYPE_XML);
               out = new OutputStreamWriter(response.getOutputStream(),"UTF-8");
               int bufSize = 4096;
               char[] buf=new char[bufSize];
               int len=0;
               while ( (len = pr.read(buf, 0, bufSize)) != -1) {
                   out.write(buf, 0, len);
               }
               out.flush();
               //int bytestream = 0;
               //while ( (bytestream = pr.read()) >= 0)
               //{
               //  out.write(bytestream);
               //}
               //out.flush();
             } else
             {
               // Transform results into an html table
               response.setContentType(CONTENT_TYPE_HTML);
               out = new OutputStreamWriter(response.getOutputStream(),"UTF-8");
               TransformerFactory factory = TransformerFactory.newInstance();
               Templates template = factory.newTemplates(new StreamSource(this.getServletContext().getRealPath("WEB-INF/xsl/behaviorDefs.xslt")));
               Transformer transformer = template.newTransformer();
               Properties details = template.getOutputProperties();
               transformer.setParameter("title_", new StringValue("Fedora Digital Object"));
               transformer.setParameter("subtitle_", new StringValue("Behavior Definitions View"));
               transformer.setParameter("soapClientServletPath", new StringValue(SOAP_CLIENT_SERVLET_PATH));
               transformer.setParameter("soapClientMethodParmResolverServletPath", new StringValue(METHOD_PARM_RESOLVER_SERVLET_PATH));
               transformer.transform(new StreamSource(pr), new StreamResult(out));
             }
             out.flush();
 
           } else
           {
             // Behavior Definition request returned nothing.
             String message = "[FedoraAccessSoapServlet] No Behavior Definitons "
                 + "returned.";
             System.err.println(message);
             showURLParms(action, PID, bDefPID, methodName, asOfDateTime,
                          userParms, response, message);
           }
 
           // FIXME!! Needs more refined Exception handling.
         } catch (Exception e)
         {
           String message = "[FedoraAccessSoapServlet] Failed to get Behavior "
               + "Definitions <br > Exception: "
               + e.getClass().getName() + " <br> Reason: "
               + e.getMessage();
           System.err.println(message);
           showURLParms(action, PID, bDefPID, methodName, asOfDateTime,
                        userParms, response, message);
         } finally
         {
           try
           {
             if (pr != null) pr.close();
             if (out != null) out.close();
           } catch (Throwable th)
           {
             String message = "[FedoraAccessSoapServlet] An error has occured. "
                 + " The error was a \" " + th.getClass().getName()
                 + " \". Reason: "  + th.getMessage();
             throw new ServletException(message);
           }
         }
         long stopTime = new Date().getTime();
         long interval = stopTime - servletStartTime;
         //System.out.println("[FedoraAccessSoapServlet] Roundtrip "
         //    + "GetBehaviorDefinitions: " + interval + " milliseconds.");
       }
       else if (action.equals(GET_BEHAVIOR_METHODS))
       {
         MethodDef[] methodDefs = null;
         ObjectMethodsDef[] objMethDefArray = null;
         PipedWriter pw = null;
         PipedReader pr = null;
         OutputStreamWriter out = null;
         try
         {
 
           //out = response.getOutputStream();
           pw = new PipedWriter();
           pr = new PipedReader(pw);
           methodDefs = getBehaviorMethods(PID, bDefPID, asOfDateTime);
           if (methodDefs != null)
           {
             // Object Methods found.
             // Deserialize ObjectmethodsDef datastructure into XML
             new BehaviorMethodsSerializerThread(PID, bDefPID, methodDefs, versDateTime, pw).start();
             if (xml)
             {
               // Return results as raw XML
               response.setContentType(CONTENT_TYPE_XML);
               out = new OutputStreamWriter(response.getOutputStream(),"UTF-8");
               int bufSize = 4096;
               char[] buf=new char[bufSize];
               int len=0;
               while ( (len = pr.read(buf, 0, bufSize)) != -1) {
                   out.write(buf, 0, len);
               }
               out.flush();
               //int bytestream = 0;
               //while ( (bytestream = pr.read()) >= 0)
               //{
               //  out.write(bytestream);
               //}
               //out.flush();
             } else
             {
               // Transform results into an html table
               response.setContentType(CONTENT_TYPE_HTML);
               out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
               TransformerFactory factory = TransformerFactory.newInstance();
               Templates template = factory.newTemplates(new StreamSource(this.getServletContext().getRealPath("WEB-INF/xsl/objectMethods.xslt")));
               Transformer transformer = template.newTransformer();
               Properties details = template.getOutputProperties();
               transformer.setParameter("title_", new StringValue("Fedora Digital Object"));
               transformer.setParameter("subtitle_", new StringValue("Behavior Methods View"));
               transformer.setParameter("soapClientServletPath", new StringValue(SOAP_CLIENT_SERVLET_PATH));
               transformer.setParameter("soapClientMethodParmResolverServletPath", new StringValue(METHOD_PARM_RESOLVER_SERVLET_PATH));
               transformer.transform(new StreamSource(pr), new StreamResult(out));
             }
             out.flush();
 
           } else
           {
             // Method Definitions request returned nothing.
             String message = "[FedoraAccessSoapServlet] No Behavior Methods "
                 + "returned.";
             System.err.println(message);
             showURLParms(action, PID, bDefPID, methodName, asOfDateTime,
                          userParms, response, message);
           }
 
           // FIXME!! Needs more refined Exception handling.
         } catch (Exception e)
         {
           String message = "[FedoraAccessSoapServlet] No Behavior Methods "
               + "returned. <br> Exception: " + e.getClass().getName()
               + " <br> Reason: "  + e.getMessage();
           System.err.println(message);
           showURLParms(action, PID, bDefPID, methodName, asOfDateTime,
                        userParms, response, message);
         } finally
         {
           try
           {
             if (pr != null) pr.close();
             if (out != null) out.close();
           } catch (Throwable th)
           {
             String message = "[FedoraAccessSoapServlet] An error has occured. "
                 + " The error was a \" " + th.getClass().getName()
                 + " \". Reason: "  + th.getMessage();
             throw new ServletException(message);
           }
         }
         long stopTime = new Date().getTime();
         long interval = stopTime - servletStartTime;
         //System.out.println("[FedoraAccessSoapServlet] Roundtrip "
         //    + "GetBehaviorDefinitions: " + interval + " milliseconds.");
       }
       else if (action.equalsIgnoreCase(GET_BEHAVIOR_METHODS_XML))
       {
         MIMETypedStream methodDefs = null;
         ByteArrayInputStream methodResults = null;
         ServletOutputStream out = response.getOutputStream();
         try
         {
           // Call Fedora Access SOAP service to request Method Definitions
           // in XML form.
           methodDefs = getBehaviorMethodsXML(PID, bDefPID, asOfDateTime);
           if (methodDefs != null)
           {
             // Method Definitions found; output resutls as XML.
             //
             // Note that what is returned by the Fedora Access SOAP service is
             // a data structure. In a browser-based environment, it makes more
             // sense to return something that is "browser-friendly" so the
             // returned datastructure is transformed into an html table. In a
             // nonbrowser-based environment, one would use the returned data
             // structures directly and most likely forgo this transformation
             // step.
             methodResults = new ByteArrayInputStream(methodDefs.getStream());
             response.setContentType(methodDefs.getMIMEType());
             int byteStream = 0;
             byte[] buffer = new byte[255];
             while ((byteStream = methodResults.read(buffer)) >= 0)
             {
               out.write(buffer, 0, byteStream);
             }
             out.flush();
             buffer = null;
           } else
           {
             // Method Definition request in XML form returned nothing.
             String message = "[FedoraAccessSoapServlet] No Behavior Methods "
                 + "returned as XML.";
             System.err.println(message);
             showURLParms(action, PID, bDefPID, methodName, asOfDateTime,
                          userParms, response, message);
           }
         } catch (Exception e)
         {
           // FIXME!! Needs more refined Exception handling.
           String message = "[FedoraAccessSoapServlet] No Behavior Methods "
               + "returned as XML. <br> Exception: " + e.getClass().getName()
               + " <br> Reason: "  + e.getMessage();
           System.err.println(message);
           showURLParms(action, PID, bDefPID, methodName, asOfDateTime,
                        userParms, response, message);
         } finally
         {
           try
           {
             if (out != null) out.close();
             if (methodResults != null) methodResults.close();
           } catch (Throwable th)
           {
             String message = "[FedoraAccessSoapServlet] An error has occured. "
                 + " The error was a \" " + th.getClass().getName()
                 + " \". Reason: "  + th.getMessage();
             throw new ServletException(message);
           }
         }
         long stopTime = new Date().getTime();
         long interval = stopTime - servletStartTime;
         //System.out.println("[FedoraAccessSoapServlet] Roundtrip "
         //    + "GetBehaviorMethodsAsWSDL: " + interval + " milliseconds.");
       }
       else if (action.equals(GET_DISSEMINATION))
       {
         ServletOutputStream out = response.getOutputStream();
         ByteArrayInputStream dissemResult = null;
         try
         {
           // Call Fedora Access SOAP service to request dissemination.
           MIMETypedStream dissemination = null;
           dissemination = getDissemination(PID, bDefPID, methodName,
               userParms, asOfDateTime);
           if (dissemination != null)
           {
             // Dissemination found. Output the mime-typed stream.
             //
             // Note that what is returned by the Fedora Access SOAP service is
             // a data structure. In a browser-based environment, it makes more
             // sense to return something that is "browser-friendly" so the
             // returned datastructure is written back to the serlvet response.
             // In a nonbrowser-based environment, one would use the returned
             // data structure directly and most likely forgo this
             // transformation step.
             //
             if (dissemination.getMIMEType().
                 equalsIgnoreCase("application/fedora-redirect"))
             {
               // A MIME type of application/fedora-redirect signals that the
               // MIMETypedStream returned from the dissemination is a special
               // Fedora-specific MIME type. In this case, teh Fedora server
               // will not proxy the stream, but instead perform a simple
               // redirect to the URL contained within the body of the
               // MIMETypedStream. This special MIME type is used primarily
               // for streaming media.
               BufferedReader br = new BufferedReader(
                   new InputStreamReader(
                   new ByteArrayInputStream(dissemination.getStream())));
               StringBuffer sb = new StringBuffer();
               String line = null;
               while ((line = br.readLine()) != null)
               {
                 sb.append(line);
               }
               response.sendRedirect(sb.toString());
             } else
             {
               response.setContentType(dissemination.getMIMEType());
               int byteStream = 0;
               dissemResult = new ByteArrayInputStream(dissemination.getStream());
               byte[] buffer = new byte[255];
              while ((byteStream = dissemResult.read(buffer)) != -1)
               {
                 out.write(buffer, 0, byteStream);
               }
               out.flush();
               buffer = null;
             }
           } else
           {
             // Dissemination request returned nothing.
             String message = "[FedoraAccessSoapServlet] No Dissemination "
                 + "result returned. <br> See server logs for additional info";
             System.err.println(message);
             showURLParms(action, PID, bDefPID, methodName, asOfDateTime,
                          userParms, response, message);
           }
         } catch (Exception e)
         {
           // FIXME!! Needs more refined Exception handling.
           e.printStackTrace();
           String message = "[FedoraAccessSoapServlet] No Dissemination "
               + "result returned. <br> Exception: "
               + e.getClass().getName()
               + " <br> Reason: "  + e.getMessage()
               + " <br> See server logs for additional info";
           System.err.println(message);
           showURLParms(action, PID, bDefPID, methodName, asOfDateTime,
                        userParms, response, message);
         } finally
         {
           try
           {
             if (dissemResult != null) dissemResult.close();
           } catch (Throwable th)
           {
             String message = "[FedoraAccessSoapServlet] An error has occured. "
                 + " The error was a \" " + th.getClass().getName()
                 + " \". Reason: "  + th.getMessage();
             throw new ServletException(message);
           }
         }
         long stopTime = new Date().getTime();
         long interval = stopTime - servletStartTime;
         //System.out.println("[FedoraAccessSoapServlet] Roundtrip "
         //    + "GetDissemination: " + interval + " milliseconds.");
       }
       else if (action.equals(GET_OBJECT_METHODS))
       {
         ObjectMethodsDef[] objMethDefArray = null;
         PipedWriter pw = new PipedWriter();
         PipedReader pr = new PipedReader(pw);
         OutputStreamWriter out = null;
 
         try
         {
           pw = new PipedWriter();
           pr = new PipedReader(pw);
           objMethDefArray = getObjectMethods(PID, asOfDateTime);
           if (objMethDefArray != null)
           {
             // Object Methods found.
             // Deserialize ObjectmethodsDef datastructure into XML
             new ObjectMethodsSerializerThread(PID, objMethDefArray, versDateTime, pw).start();
             if (xml)
             {
               // Return results as raw XML
               response.setContentType(CONTENT_TYPE_XML);
               out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
               int bufSize = 4096;
               char[] buf=new char[bufSize];
               int len=0;
               while ( (len = pr.read(buf, 0, bufSize)) != -1) {
                   out.write(buf, 0, len);
               }
               out.flush();
               //int bytestream = 0;
               //while ( (bytestream = pr.read()) >= 0)
               //{
               //  out.write(bytestream);
               //}
               //out.flush();
             } else
             {
               // Transform results into an html table
               response.setContentType(CONTENT_TYPE_HTML);
               out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
               TransformerFactory factory = TransformerFactory.newInstance();
               Templates template = factory.newTemplates(new StreamSource(this.getServletContext().getRealPath("WEB-INF/xsl/objectMethods.xslt")));
               Transformer transformer = template.newTransformer();
               Properties details = template.getOutputProperties();
               transformer.setParameter("title_", new StringValue("Fedora Digital Object"));
               transformer.setParameter("subtitle_", new StringValue("Object Methods View"));
               transformer.setParameter("soapClientServletPath", new StringValue(SOAP_CLIENT_SERVLET_PATH));
               transformer.setParameter("soapClientMethodParmResolverServletPath", new StringValue(METHOD_PARM_RESOLVER_SERVLET_PATH));
               transformer.transform(new StreamSource(pr), new StreamResult(out));
             }
             out.flush();
           } else
           {
             // Object Methods Definition request returned nothing.
             String message = "[FedoraAccessSoapServlet] No Object Method "
                 + "Definitions returned.";
             System.out.println(message);
             showURLParms(action, PID, "", "", asOfDateTime, new Property[0],
                          response, message);
             response.setStatus(HttpServletResponse.SC_NO_CONTENT);
             response.sendError(response.SC_NO_CONTENT, message);
           }
         } catch (Throwable th)
         {
           String message = "[FedoraAccessSoapServlet] An error has occured. "
               + " The error was a \" "
               + th.getClass().getName()
               + " \". Reason: "  + th.getMessage();
           System.out.println(message);
           th.printStackTrace();
           response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
           response.sendError(response.SC_INTERNAL_SERVER_ERROR, message);
         } finally
         {
           try
           {
             if (pr != null) pr.close();
             if (out != null) out.close();
           } catch (Throwable th)
           {
             String message = "[FedoraAccessSoapServlet] An error has occured. "
                 + " The error was a \" " + th.getClass().getName()
                 + " \". Reason: "  + th.getMessage();
             throw new ServletException(message);
           }
         }
         long stopTime = new Date().getTime();
         long interval = stopTime - servletStartTime;
         //System.out.println("[FedoraAccessSoapServlet] Roundtrip "
         //  + "GetObjectMethods: " + interval + " milliseconds.");
       }
       else if (action.equals(GET_OBJECT_PROFILE))
       {
         ObjectProfile objProfile = null;
         PipedWriter pw = new PipedWriter();
         PipedReader pr = new PipedReader(pw);
         OutputStreamWriter out = null;
 
         try
         {
           //out = response.getOutputStream();
           pw = new PipedWriter();
           pr = new PipedReader(pw);
           objProfile = getObjectProfile(PID, asOfDateTime);
           if (objProfile != null)
           {
             // Object Profile found.
             // Deserialize ObjectProfile datastructure into XML
             new ProfileSerializerThread(PID, objProfile, versDateTime, pw).start();
             if (xml)
             {
               // Return results as raw XML
               response.setContentType(CONTENT_TYPE_XML);
               out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
               int bufSize = 4096;
               char[] buf=new char[bufSize];
               int len=0;
               while ( (len = pr.read(buf, 0, bufSize)) != -1) {
                   out.write(buf, 0, len);
               }
               out.flush();
               //int bytestream = 0;
               //while ( (bytestream = pr.read()) >= 0)
               //{
               //  out.write(bytestream);
               //}
               //out.flush();
             } else
             {
               // Transform results into an html table
               response.setContentType(CONTENT_TYPE_HTML);
               out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
               File xslFile = new File(this.getServletContext().getRealPath("WEB-INF/xsl/viewObjectProfile.xslt"));
               TransformerFactory factory = TransformerFactory.newInstance();
               Templates template = factory.newTemplates(new StreamSource(xslFile));
               Transformer transformer = template.newTransformer();
               Properties details = template.getOutputProperties();
               transformer.setParameter("title_", new StringValue("Fedora Digital Object"));
               transformer.setParameter("subtitle_", new StringValue("Object Profile View"));
               transformer.setParameter("soapClientServletPath", new StringValue(SOAP_CLIENT_SERVLET_PATH));
               transformer.setParameter("soapClientMethodParmResolverServletPath", new StringValue(METHOD_PARM_RESOLVER_SERVLET_PATH));
               transformer.transform(new StreamSource(pr), new StreamResult(out));
             }
             out.flush();
 
           } else
           {
             // No Object Profile returned
             String message = "[FedoraAccessSoapServlet] No Object Profile returned.";
             System.out.println(message);
             showURLParms(action, PID, "", "", asOfDateTime, new Property[0],
                          response, message);
           }
         } catch (Throwable th)
         {
           String message = "[FedoraAccessSoapServlet] An error has occured. "
               + " The error was a \" "
               + th.getClass().getName()
               + " \". Reason: "  + th.getMessage();
           System.out.println(message);
           th.printStackTrace();
           response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
           response.sendError(response.SC_INTERNAL_SERVER_ERROR, message);
         } finally
         {
           try
           {
             if (pr != null) pr.close();
             if (out != null) out.close();
           } catch (Throwable th)
           {
             String message = "[FedoraAccessSoapServlet] An error has occured. "
                 + " The error was a \" " + th.getClass().getName()
                 + " \". Reason: "  + th.getMessage();
             throw new ServletException(message);
           }
         }
         long stopTime = new Date().getTime();
         long interval = stopTime - servletStartTime;
         //System.out.println("[FedoraAccessSoapServlet] Roundtrip "
         //  + "GetObjectProfile: " + interval + " milliseconds.");
         // end Object Profile processing
       }
       else if (action.equals(DESCRIBE_REPOSITORY))
       {
         RepositoryInfo repositoryInfo = null;
         PipedWriter pw = new PipedWriter();
         PipedReader pr = new PipedReader(pw);
         OutputStreamWriter out = null;
 
         try
         {
           pw = new PipedWriter();
           pr = new PipedReader(pw);
           repositoryInfo = describeRepository();
           if (repositoryInfo != null)
           {
             // Repository Info found.
             // Deserialize RepositoryInfo datastructure into XML
             new ReposInfoSerializerThread(repositoryInfo, pw).start();
             if (xml)
             {
               // Return results as raw XML
               response.setContentType(CONTENT_TYPE_XML);
               out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
               int bufSize = 4096;
               char[] buf=new char[bufSize];
               int len=0;
               while ( (len = pr.read(buf, 0, bufSize)) != -1) {
                   out.write(buf, 0, len);
               }
               out.flush();
             } else
             {
               // Transform results into an html table
               response.setContentType(CONTENT_TYPE_HTML);
               out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
               File xslFile = new File(this.getServletContext().getRealPath("WEB-INF/xsl/viewRepositoryInfo.xslt"));
               TransformerFactory factory = TransformerFactory.newInstance();
               Templates template = factory.newTemplates(new StreamSource(xslFile));
               Transformer transformer = template.newTransformer();
               Properties details = template.getOutputProperties();
               transformer.setParameter("title_", new StringValue("Fedora"));
               transformer.setParameter("subtitle_", new StringValue("Describe Repository View"));
               transformer.setParameter("soapClientServletPath", new StringValue(SOAP_CLIENT_SERVLET_PATH));
               transformer.setParameter("soapClientMethodParmResolverServletPath", new StringValue(METHOD_PARM_RESOLVER_SERVLET_PATH));
               transformer.transform(new StreamSource(pr), new StreamResult(out));
             }
             out.flush();
 
           } else
           {
             // No Repository Info returned
             String message = "[FedoraAccessSoapServlet] No Repository Info returned.";
             System.out.println(message);
             showURLParms(action, "", "", "", null, new Property[0],
                          response, message);
           }
         } catch (Throwable th)
         {
           String message = "[FedoraAccessSoapServlet] An error has occured. "
               + " The error was a \" "
               + th.getClass().getName()
               + " \". Reason: "  + th.getMessage();
           System.out.println(message);
           th.printStackTrace();
           response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
           response.sendError(response.SC_INTERNAL_SERVER_ERROR, message);
         } finally
         {
           try
           {
             if (pr != null) pr.close();
             if (out != null) out.close();
           } catch (Throwable th)
           {
             String message = "[FedoraAccessSoapServlet] An error has occured. "
                 + " The error was a \" " + th.getClass().getName()
                 + " \". Reason: "  + th.getMessage();
             throw new ServletException(message);
           }
         }
         long stopTime = new Date().getTime();
         long interval = stopTime - servletStartTime;
       }
       else
       {
         // Action not recognized
         String message = "[FedoraAccessSoapServlet] Requested action not recognized.";
         System.out.println(message);
         showURLParms(action, PID, "", "", asOfDateTime, new Property[0],
                      response, message);
       }
     }
   }
 
   /**
    * <p> A Thread to serialize an ObjectMethodsDef object into XML.</p>
    *
    */
   public class ObjectMethodsSerializerThread extends Thread
   {
     private PipedWriter pw = null;
     private String PID = null;
     private ObjectMethodsDef[] objMethDefArray = new ObjectMethodsDef[0];
     private Date versDateTime = null;
 
     /**
      * <p> Constructor for SerializeThread.</p>
      *
      * @param PID The persistent identifier of the specified digital object.
      * @param objMethDefArray An array of object mtehod definitions.
      * @param versDateTime The version datetime stamp of the request.
      * @param pw A PipedWriter to which the serialization info is written.
      */
     public ObjectMethodsSerializerThread(String PID, ObjectMethodsDef[] objMethDefArray,
                         Date versDateTime, PipedWriter pw)
     {
       this.pw = pw;
       this.PID = PID;
       this.objMethDefArray = objMethDefArray;
       this.versDateTime = versDateTime;
     }
 
     /**
      * <p> This method executes the thread.</p>
      */
     public void run()
     {
       if (pw != null)
       {
         try
         {
           pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
           if (versDateTime == null || DateUtility.
               convertDateToString(versDateTime).equalsIgnoreCase(""))
           {
             pw.write("<objectMethods "
                 + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                 + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                 + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
                 + " http://" + fedoraServerHost + ":" + fedoraServerPort
                 + "/objectMethods.xsd\""
                 + " pid=\"" + PID + "\" >");
             //pw.write("<objectMethods "
             //    + " targetNamespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
             //    + " pid=\"" + PID + "\" >");
             //pw.write("<import namespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " location=\"objectMethods.xsd\"/>");
           } else
           {
             pw.write("<objectMethods "
                 + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                 + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                 + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
                 + " http://" + fedoraServerHost + ":" + fedoraServerPort
                 + "/objectMethods.xsd\""
                 + " pid=\"" + PID
                 + " dateTime=\"" + DateUtility.convertDateToString(versDateTime)
                 + "\" >");
             //pw.write("<objectMethods "
             //    + " targetNamespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
             //    + " pid=\"" + PID + "\""
             //    + " dateTime=\"" + DateUtility.convertDateToString(versDateTime)
             //    + "\" >");
             //pw.write("<import namespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " location=\"objectMethods.xsd\"/>");
           }
           String nextBdef = "null";
           String currentBdef = "";
           for (int i=0; i<objMethDefArray.length; i++)
           {
             currentBdef = objMethDefArray[i].getBDefPID();
             if (!currentBdef.equalsIgnoreCase(nextBdef))
             {
               if (i != 0) pw.write("</bdef>");
               pw.write("<bdef pid=\"" + objMethDefArray[i].getBDefPID() + "\" >");
             }
             pw.write("<method name=\"" + objMethDefArray[i].getMethodName() + "\" >");
             MethodParmDef[] methodParms = objMethDefArray[i].getMethodParmDefs();
             for (int j=0; j<methodParms.length; j++)
             {
               pw.write("<parm parmName=\"" + methodParms[j].getParmName()
                   + "\" parmDefaultValue=\"" + methodParms[j].getParmDefaultValue()
                   + "\" parmRequired=\"" + methodParms[j].isParmRequired()
                   + "\" parmType=\"" + methodParms[j].getParmType()
                   + "\" parmLabel=\"" + methodParms[j].getParmLabel() + "\" >");
               if (methodParms[j].getParmDomainValues().length > 0 )
               {
                 pw.write("<parmDomainValues>");
                 for (int k=0; k<methodParms[j].getParmDomainValues().length; k++)
                 {
                   pw.write("<value>" + methodParms[j].getParmDomainValues()[k]
                       + "</value>");
                 }
                 pw.write("</parmDomainValues>");
               }
               pw.write("</parm>");
             }
 
             pw.write("</method>");
             nextBdef = currentBdef;
           }
           pw.write("</bdef>");
           pw.write("</objectMethods>");
           pw.flush();
           pw.close();
         } catch (IOException ioe) {
           System.err.println("WriteThread IOException: " + ioe.getMessage());
         } finally
         {
           try
           {
             if (pw != null) pw.close();
           } catch (IOException ioe)
           {
             System.err.println("WriteThread IOException: " + ioe.getMessage());
           }
         }
       }
     }
   }
 
   /**
    * <p> A Thread to serialize an ObjectProfile object into XML.</p>
    *
    */
   public class ProfileSerializerThread extends Thread
   {
     private PipedWriter pw = null;
     private String PID = null;
     private ObjectProfile objProfile = null;
     private Date versDateTime = null;
 
     /**
      * <p> Constructor for ProfileSerializeThread.</p>
      *
      * @param PID The persistent identifier of the specified digital object.
      * @param objProfile An object profile data structure.
      * @param versDateTime The version datetime stamp of the request.
      * @param pw A PipedWriter to which the serialization info is written.
      */
     public ProfileSerializerThread(String PID, ObjectProfile objProfile,
                         Date versDateTime, PipedWriter pw)
     {
       this.pw = pw;
       this.PID = PID;
       this.objProfile = objProfile;
       this.versDateTime = versDateTime;
     }
 
     /**
      * <p> This method executes the thread.</p>
      */
     public void run()
     {
       if (pw != null)
       {
         try
         {
           pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
           if (versDateTime == null || DateUtility.
               convertDateToString(versDateTime).equalsIgnoreCase(""))
           {
             pw.write("<objectProfile "
                 + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                 + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                 + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
                 + " http://" + fedoraServerHost + ":" + fedoraServerPort
                 + "/objectProfile.xsd\""
                 + " pid=\"" + PID + "\" >");
             //pw.write("<objectProfile "
             //    + " targetNamespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
             //    + " pid=\"" + PID + "\" >");
             //pw.write("<import namespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " location=\"objectProfile.xsd\"/>");
           } else
           {
             pw.write("<objectProfile "
                 + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                 + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                 + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
                 + " http://" + fedoraServerHost + ":" + fedoraServerPort
                 + "/objectProfile.xsd\""
                 + " pid=\"" + PID
                 + " dateTime=\"" + DateUtility.convertDateToString(versDateTime)
                 + "\" >");
             //pw.write("<objectProfile "
             //    + " targetNamespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
             //    + " pid=\"" + PID + "\""
             //    + " dateTime=\"" + DateUtility.convertDateToString(versDateTime)
             //    + "\" >");
             //pw.write("<import namespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " location=\"objectProfile.xsd\"/>");
           }
 
           // PROFILE FIELDS SERIALIZATION
           pw.write("<objLabel>" + objProfile.getObjLabel() + "</objLabel>");
           pw.write("<objContentModel>" + objProfile.getObjContentModel() + "</objContentModel>");
           String cDate = DateUtility.convertCalendarToString(objProfile.getObjCreateDate());
           pw.write("<objCreateDate>" + cDate + "</objCreateDate>");
           String mDate = DateUtility.convertCalendarToString(objProfile.getObjLastModDate());
           pw.write("<objLastModDate>" + mDate + "</objLastModDate>");
           String objType = objProfile.getObjType();
           pw.write("<objType>");
           if (objType.equalsIgnoreCase("O"))
           {
             pw.write("Fedora Data Object");
           }
           else if (objType.equalsIgnoreCase("D"))
           {
             pw.write("Fedora Behavior Definition Object");
           }
           else if (objType.equalsIgnoreCase("M"))
           {
             pw.write("Fedora Behavior Mechanism Object");
           }
           pw.write("</objType>");
           pw.write("<objDissIndexViewURL>" + objProfile.getObjDissIndexViewURL() + "</objDissIndexViewURL>");
           pw.write("<objItemIndexViewURL>" + objProfile.getObjItemIndexViewURL() + "</objItemIndexViewURL>");
           pw.write("</objectProfile>");
           pw.flush();
           pw.close();
         } catch (IOException ioe) {
           System.err.println("WriteThread IOException: " + ioe.getMessage());
         } finally
         {
           try
           {
             if (pw != null) pw.close();
           } catch (IOException ioe)
           {
             System.err.println("WriteThread IOException: " + ioe.getMessage());
           }
         }
       }
     }
   }
 
   /**
    * <p> A Thread to serialize a RepositoryInfo object into XML.</p>
    *
    */
   public class ReposInfoSerializerThread extends Thread
   {
     private PipedWriter pw = null;
     private RepositoryInfo repositoryInfo = null;
 
     /**
      * <p> Constructor for ReposInfoSerializeThread.</p>
      *
      * @param repositoryInfo A repository info data structure.
      * @param pw A PipedWriter to which the serialization info is written.
      */
     public ReposInfoSerializerThread(RepositoryInfo repositoryInfo, PipedWriter pw)
     {
       this.pw = pw;
       this.repositoryInfo = repositoryInfo;
     }
 
     /**
      * <p> This method executes the thread.</p>
      */
     public void run()
     {
       if (pw != null)
       {
         try
         {
           pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
           pw.write("<fedoraRepository "
               + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
               + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
               + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
               + " http://" + fedoraServerHost + ":" + fedoraServerPort
               + "/fedoraRepository.xsd\">");
           //pw.write("<fedoraRepository "
           //    + " targetNamespace=\"http://www.fedora.info/definitions/1/0/access/\""
           //    + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
           //    + ">");
           //pw.write("<import namespace=\"http://www.fedora.info/definitions/1/0/access/\""
           //    + " location=\"fedoraRepository.xsd\"/>");
 
           // REPOSITORY INFO FIELDS SERIALIZATION
           pw.write("<repositoryName>" + repositoryInfo.getRepositoryName() + "</repositoryName>");
           pw.write("<repositoryBaseURL>" + repositoryInfo.getRepositoryBaseURL() + "</repositoryBaseURL>");
           pw.write("<repositoryVersion>" + repositoryInfo.getRepositoryVersion() + "</repositoryVersion>");
           pw.write("<repositoryPID>");
           pw.write("    <PID-namespaceIdentifier>"
             + repositoryInfo.getRepositoryPIDNamespace()
             + "</PID-namespaceIdentifier>");
           pw.write("    <PID-delimiter>" + ":"+ "</PID-delimiter>");
           pw.write("    <PID-sample>" + repositoryInfo.getSamplePID() + "</PID-sample>");
           pw.write("</repositoryPID>");
           pw.write("<repositoryOAI-identifier>");
           pw.write("    <OAI-namespaceIdentifier>"
             + repositoryInfo.getOAINamespace()
             + "</OAI-namespaceIdentifier>");
           pw.write("    <OAI-delimiter>" + ":"+ "</OAI-delimiter>");
           pw.write("    <OAI-sample>" + repositoryInfo.getSampleOAIIdentifier() + "</OAI-sample>");
           pw.write("</repositoryOAI-identifier>");
           pw.write("<sampleSearch-URL>" + repositoryInfo.getSampleSearchURL() + "</sampleSearch-URL>");
           pw.write("<sampleAccess-URL>" + repositoryInfo.getSampleAccessURL() + "</sampleAccess-URL>");
           pw.write("<sampleOAI-URL>" + repositoryInfo.getSampleOAIURL() + "</sampleOAI-URL>");
           String[] emails = repositoryInfo.getAdminEmailList();
           for (int i=0; i<emails.length; i++)
           {
             pw.write("<adminEmail>" + emails[i] + "</adminEmail>");
           }
           pw.write("</fedoraRepository>");
           pw.flush();
           pw.close();
         } catch (IOException ioe) {
           System.err.println("WriteThread IOException: " + ioe.getMessage());
         } finally
         {
           try
           {
             if (pw != null) pw.close();
           } catch (IOException ioe)
           {
             System.err.println("WriteThread IOException: " + ioe.getMessage());
           }
         }
       }
     }
   }
 
   /**
    * <p> A Thread to serialize an ObjectMethodsDef object into XML.</p>
    *
    */
   public class BehaviorMethodsSerializerThread extends Thread
   {
     private PipedWriter pw = null;
     private String PID = null;
     private String bDefPID = null;
     private MethodDef[] methodDefArray = new MethodDef[0];
     private Date versDateTime = null;
 
     /**
      * <p> Constructor for SerializeThread.</p>
      *
      * @param PID The persistent identifier of the specified digital object.
      * @param bDefPID The persistent identifier of the behavior definition object.
      * @param methodDefArray An array of object mtehod definitions.
      * @param versDateTime The version datetime stamp of the request.
      * @param pw A PipedWriter to which the serialization info is written.
      */
     public BehaviorMethodsSerializerThread(String PID, String bDefPID, MethodDef[] methodDefArray,
                         Date versDateTime, PipedWriter pw)
     {
       this.pw = pw;
       this.PID = PID;
       this.bDefPID = bDefPID;
       this.methodDefArray = methodDefArray;
       this.versDateTime = versDateTime;
     }
 
     /**
      * <p> This method executes the thread.</p>
      */
     public void run()
     {
       if (pw != null)
       {
         try
         {
           pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
           if (versDateTime == null || DateUtility.
               convertDateToString(versDateTime).equalsIgnoreCase(""))
           {
             pw.write("<objectMethods "
                 + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                 + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                 + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
                 + " http://" + fedoraServerHost + ":" + fedoraServerPort
                 + "/objectMethods.xsd\""
                 + " pid=\"" + PID + "\" >");
             //pw.write("<objectMethods "
             //    + " targetNamespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
             //    + " pid=\"" + PID + "\" >");
             //pw.write("<import namespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " location=\"objectMethods.xsd\"/>");
           } else
           {
             pw.write("<objectMethods "
                 + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                 + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                 + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
                 + " http://" + fedoraServerHost + ":" + fedoraServerPort
                 + "/objectMethods.xsd\""
                 + " pid=\"" + PID
                 + " dateTime=\"" + DateUtility.convertDateToString(versDateTime)
                 + "\" >");
             //pw.write("<objectMethods "
             //    + " targetNamespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
             //    + " pid=\"" + PID + "\""
             //    + " dateTime=\"" + DateUtility.convertDateToString(versDateTime)
             //    + "\" >");
             //pw.write("<import namespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " location=\"objectMethods.xsd\"/>");
           }
           String nextBdef = "null";
           String currentBdef = "";
           for (int i=0; i<methodDefArray.length; i++)
           {
             currentBdef = bDefPID;
             if (!currentBdef.equalsIgnoreCase(nextBdef))
             {
               if (i != 0) pw.write("</bdef>");
               pw.write("<bdef pid=\"" + bDefPID + "\" >");
             }
             pw.write("<method name=\"" + methodDefArray[i].getMethodName() + "\" >");
             MethodParmDef[] methodParms = methodDefArray[i].getMethodParms();
             for (int j=0; j<methodParms.length; j++)
             {
               pw.write("<parm parmName=\"" + methodParms[j].getParmName()
                   + "\" parmDefaultValue=\"" + methodParms[j].getParmDefaultValue()
                   + "\" parmRequired=\"" + methodParms[j].isParmRequired()
                   + "\" parmType=\"" + methodParms[j].getParmType()
                   + "\" parmLabel=\"" + methodParms[j].getParmLabel() + "\" >");
               if (methodParms[j].getParmDomainValues().length > 0 )
               {
                 pw.write("<parmDomainValues>");
                 for (int k=0; k<methodParms[j].getParmDomainValues().length; k++)
                 {
                   pw.write("<value>" + methodParms[j].getParmDomainValues()[k]
                       + "</value>");
                 }
                 pw.write("</parmDomainValues>");
               }
               pw.write("</parm>");
             }
 
             pw.write("</method>");
             nextBdef = currentBdef;
           }
           pw.write("</bdef>");
           pw.write("</objectMethods>");
           pw.flush();
           pw.close();
         } catch (IOException ioe) {
           System.err.println("WriteThread IOException: " + ioe.getMessage());
         } finally
         {
           try
           {
             if (pw != null) pw.close();
           } catch (IOException ioe)
           {
             System.err.println("WriteThread IOException: " + ioe.getMessage());
           }
         }
       }
     }
   }
 
   /**
    * <p> A Thread to serialize an ObjectMethodsDef object into XML.</p>
    *
    */
   public class BehaviorDefinitionsSerializerThread extends Thread
   {
     private PipedWriter pw = null;
     private String PID = null;
     private String[] behaviorDefArray = new String[0];
     private Date versDateTime = null;
 
     /**
      * <p> Constructor for SerializeThread.</p>
      *
      * @param PID The persistent identifier of the specified digital object.
      * @param behaviorDefArray An array of behavior method definitions.
      * @param versDateTime The version datetime stamp of the request.
      * @param pw A PipedWriter to which the serialization info is written.
      */
     public BehaviorDefinitionsSerializerThread(String PID, String[] behaviorDefArray,
                         Date versDateTime, PipedWriter pw)
     {
       this.pw = pw;
       this.PID = PID;
       this.behaviorDefArray = behaviorDefArray;
       this.versDateTime = versDateTime;
     }
 
     /**
      * <p> This method executes the thread.</p>
      */
     public void run()
     {
       if (pw != null)
       {
         try
         {
           pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
           if (versDateTime == null || DateUtility.
               convertDateToString(versDateTime).equalsIgnoreCase(""))
           {
             pw.write("<behaviorDefs "
                 + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                 + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                 + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
                 + " http://" + fedoraServerHost + ":" + fedoraServerPort
                 + "/behaviorDefs.xsd\""
                 + " pid=\"" + PID + "\" >");
             //pw.write("<behaviorDefs "
             //    + " targetNamespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
             //    + " pid=\"" + PID + "\" >");
             //pw.write("<import namespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " location=\"behaviorDefs.xsd\"/>");
           } else
           {
             pw.write("<behaviorDefs "
                 + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                 + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                 + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
                 + " http://" + fedoraServerHost + ":" + fedoraServerPort
                 + "/behaviorDefs.xsd\""
                 + " pid=\"" + PID
                 + " dateTime=\"" + DateUtility.convertDateToString(versDateTime)
                 + "\" >");
             //pw.write("<behaviorDefs "
             //    + " targetNamespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
             //    + " pid=\"" + PID + "\""
             //    + " dateTime=\"" + DateUtility.convertDateToString(versDateTime)
             //    + "\" >");
             //pw.write("<import namespace=\"http://www.fedora.info/definitions/1/0/access/\""
             //    + " location=\"behaviorDefs.xsd\"/>");
           }
           String nextBdef = "null";
           String currentBdef = "";
           for (int i=0; i<behaviorDefArray.length; i++)
           {
             if (versDateTime == null || DateUtility.
               convertDateToString(versDateTime).equalsIgnoreCase(""))
             {
               pw.write("<bdef pid=\"" + behaviorDefArray[i] + "\" />");
             } else
             {
               pw.write("<bdef pid=\"" + behaviorDefArray[i] + "\" dateTime=\""
                   + versDateTime + "\" />");
             }
           }
           pw.write("</behaviorDefs>");
           pw.flush();
           pw.close();
         } catch (IOException ioe) {
           System.err.println("WriteThread IOException: " + ioe.getMessage());
         } finally
         {
           try
           {
             if (pw != null) pw.close();
           } catch (IOException ioe)
           {
             System.err.println("WriteThread IOException: " + ioe.getMessage());
           }
         }
       }
     }
   }
 
   /**
    * <p>For now, treat a HTTP POST request just like a GET request.</p>
    *
    * @param request The servet request.
    * @param response The servlet response.
    * @throws ServletException If thrown by <code>doGet</code>.
    * @throws IOException If thrown by <code>doGet</code>.
    * @throws ServletException If an error occurs that effects the servlet's
    *         basic operation.
    */
   public void doPost(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
    doGet(request, response);
   }
 
   /**
    * <p>Gets a list of Behavior Definition object PIDs for the specified
    * digital object by invoking the appropriate Fedora Access SOAP service.</p>
    *
    * @param PID The persistent identifier of the digital object.
    * @param asOfDateTime The versioning datetime stamp.
    * @return An array of Behavior Definition PIDs.
    * @throws Exception If an error occurs in communicating with the Fedora
    *         Access SOAP service.
    */
   public String[] getBehaviorDefinitions(String PID, Calendar asOfDateTime)
       throws Exception
   {
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDateTime);
     Service service = new Service();
     Call call = (Call) service.createCall();
     call.setTargetEndpointAddress( new URL(FEDORA_ACCESS_ENDPOINT) );
     call.setOperationName(new QName(FEDORA_API_URI, GET_BEHAVIOR_DEFINITIONS) );
     String[] behaviorDefs = (String[]) call.invoke(new Object[] { PID,
           versDateTime });
     return behaviorDefs;
   }
 
   /**
    * <p>Gets a list of Behavior Methods associated with the specified
    * Behavior Mechanism object by invoking the appropriate Fedora Access
    * SOAP service.</p>
    *
    * @param PID The persistent identifier of Digital Object.
    * @param bDefPID The persistent identifier of Behavior Definition object.
    * @param asOfDateTime The versioning datetime stamp.
    * @return An array of method definitions.
    * @throws Exception If an error occurs in communicating with the Fedora
    *         Access SOAP service.
    */
   public MethodDef[] getBehaviorMethods(String PID,
       String bDefPID, Calendar asOfDateTime) throws Exception
   {
     MethodDef[] methodDefs = null;
     Service service = new Service();
     Call call = (Call) service.createCall();
     call.setOperationName(new QName(FEDORA_API_URI, GET_BEHAVIOR_METHODS) );
     QName qn = new QName(FEDORA_TYPE_URI, "MethodDef");
     QName qn2 = new QName(FEDORA_TYPE_URI, "MethodParmDef");
     call.setTargetEndpointAddress( new URL(FEDORA_ACCESS_ENDPOINT) );
 
     // Any Fedora-defined types required by the SOAP service must be registered
     // prior to invocation so the SOAP service knows the appropriate
     // serializer/deserializer to use for these types.
     call.registerTypeMapping(MethodDef.class, qn,
         new BeanSerializerFactory(MethodDef.class, qn),
         new BeanDeserializerFactory(MethodDef.class, qn));
     call.registerTypeMapping(MethodParmDef.class, qn2,
         new BeanSerializerFactory(MethodParmDef.class, qn2),
         new BeanDeserializerFactory(MethodParmDef.class, qn2));
     methodDefs = (MethodDef[]) call.invoke( new Object[] { PID,
           bDefPID, asOfDateTime} );
     return methodDefs;
   }
 
   /**
    * <p>Gets a bytestream containing the XML that defines the Behavior Methods
    * of the associated Behavior Mechanism object by invoking the appropriate
    * Fedora Access SOAP service.
    *
    * @param PID The persistent identifier of digital object.
    * @param bDefPID The persistent identifier of Behavior Definition object.
    * @param asOfDateTime The versioning datetime stamp.
    * @return MIME-typed stream containing XML-encoded method definitions.
    * @throws Exception If an error occurs in communicating with the Fedora
    *         Access SOAP service.
    */
   public MIMETypedStream getBehaviorMethodsXML(
       String PID, String bDefPID, Calendar asOfDateTime) throws Exception
   {
     MIMETypedStream methodDefs = null;
     Service service = new Service();
     Call call = (Call) service.createCall();
     call.setOperationName(new QName(FEDORA_API_URI,
                                     GET_BEHAVIOR_METHODS_XML) );
     QName qn = new QName(FEDORA_TYPE_URI, "MIMETypedStream");
     call.setTargetEndpointAddress( new URL(FEDORA_ACCESS_ENDPOINT) );
 
     // Any Fedora-defined types required by the SOAP service must be registered
     // prior to invocation so the SOAP service knows the appropriate
     // serializer/deserializer to use for these types.
     call.registerTypeMapping(MIMETypedStream.class, qn,
         new BeanSerializerFactory(MIMETypedStream.class, qn),
         new BeanDeserializerFactory(MIMETypedStream.class, qn));
     methodDefs = (MIMETypedStream)
                  call.invoke( new Object[] { PID, bDefPID, asOfDateTime} );
     return methodDefs;
   }
 
   /**
    * <p>Gets a MIME-typed bytestream containing the result of a dissemination
    * by invoking the appropriate Fedora Access SOAP service.
    *
    * @param PID The persistent identifier of the digital object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param methodName The name of the method.
    * @param asOfDateTime The version datetime stamp of the digital object.
    * @param userParms An array of user-supplied method parameters and values.
    * @return A MIME-typed stream containing the dissemination result.
    * @throws Exception If an error occurs in communicating with the Fedora
    *         Access SOAP service.
    */
   public MIMETypedStream getDissemination(String PID, String bDefPID,
       String methodName, Property[] userParms, Calendar asOfDateTime)
       throws Exception
    {
     // Generate a call to the Fedora SOAP service requesting the
     // GetDissemination method
     MIMETypedStream dissemination = null;
     Service service = new Service();
     Call call = (Call) service.createCall();
     call.setTargetEndpointAddress( new URL(FEDORA_ACCESS_ENDPOINT) );
     call.setOperationName(new QName(FEDORA_API_URI, GET_DISSEMINATION) );
     QName qn =  new QName(FEDORA_TYPE_URI, "MIMETypedStream");
     QName qn2 = new QName(FEDORA_TYPE_URI, "Property");
 
     // Any Fedora-defined types required by the SOAP service must be registered
     // prior to invocation so the SOAP service knows the appropriate
     // serializer/deserializer to use for these types.
     call.registerTypeMapping(MIMETypedStream.class, qn,
         new BeanSerializerFactory(MIMETypedStream.class, qn),
         new BeanDeserializerFactory(MIMETypedStream.class, qn));
     call.registerTypeMapping(fedora.server.types.gen.Property.class, qn2,
         new BeanSerializerFactory(Property.class, qn2),
         new BeanDeserializerFactory(Property.class, qn2));
     dissemination = (MIMETypedStream) call.invoke( new Object[] { PID, bDefPID,
         methodName, userParms, asOfDateTime} );
     return dissemination;
    }
 
    /**
     * <p>Gets a list of all method definitions for the specified object by
     * invoking the appropriate Fedora Access SOAP service.</p>
     *
     * @param PID The persistent identifier for the digital object.
     * @param asOfDateTime The versioning datetime stamp.
     * @return An array of object method definitions.
     * @throws Exception If an error occurs in communicating with the Fedora
     *         Access SOAP service.
     */
   public ObjectMethodsDef[] getObjectMethods(String PID,
       Calendar asOfDateTime) throws Exception
   {
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDateTime);
     ObjectMethodsDef[] objMethDefArray = null;
     Service service = new Service();
     Call call = (Call) service.createCall();
     call.setOperationName(new QName(FEDORA_API_URI, GET_OBJECT_METHODS) );
     QName qn = new QName(FEDORA_TYPE_URI, "ObjectMethodsDef");
     QName qn2 = new QName(FEDORA_TYPE_URI, "MethodParmDef");
     call.setTargetEndpointAddress( new URL(FEDORA_ACCESS_ENDPOINT) );
 
     // Any Fedora-defined types required by the SOAP service must be registered
     // prior to invocation so the SOAP service knows the appropriate
     // serializer/deserializer to use for these types.
     call.registerTypeMapping(ObjectMethodsDef.class, qn,
         new BeanSerializerFactory(ObjectMethodsDef.class, qn),
         new BeanDeserializerFactory(ObjectMethodsDef.class, qn));
     call.registerTypeMapping(MethodParmDef.class, qn2,
         new BeanSerializerFactory(MethodParmDef.class, qn2),
         new BeanDeserializerFactory(MethodParmDef.class, qn2));
     objMethDefArray =
         (ObjectMethodsDef[]) call.invoke( new Object[] { PID, asOfDateTime} );
     return objMethDefArray;
   }
 
    /**
     * <p>Gets a object profile for the specified object by
     * invoking the appropriate Fedora Access SOAP service.</p>
     *
     * @param PID The persistent identifier for the digital object.
     * @param asOfDateTime The versioning datetime stamp.
     * @return An object profile data structure.
     * @throws Exception If an error occurs in communicating with the Fedora
     *         Access SOAP service.
     */
   public ObjectProfile getObjectProfile(String PID,
       Calendar asOfDateTime) throws Exception
   {
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDateTime);
     ObjectProfile objProfile = null;
     Service service = new Service();
     Call call = (Call) service.createCall();
     call.setOperationName(new QName(FEDORA_API_URI, GET_OBJECT_PROFILE) );
     QName qn = new QName(FEDORA_TYPE_URI, "ObjectProfile");
     call.setTargetEndpointAddress( new URL(FEDORA_ACCESS_ENDPOINT) );
 
     // Any Fedora-defined types required by the SOAP service must be registered
     // prior to invocation so the SOAP service knows the appropriate
     // serializer/deserializer to use for these types.
     call.registerTypeMapping(ObjectProfile.class, qn,
         new BeanSerializerFactory(ObjectProfile.class, qn),
         new BeanDeserializerFactory(ObjectProfile.class, qn));
     objProfile =
         (ObjectProfile) call.invoke( new Object[] { PID, asOfDateTime} );
     return objProfile;
   }
 
 /**
   * <p>Gets repository information for the server by
   * invoking the appropriate Fedora Access SOAP service.</p>
   *
   * @return A repository information data structure.
   * @throws Exception If an error occurs in communicating with the Fedora
   *         Access SOAP service.
   */
   public RepositoryInfo describeRepository() throws Exception
   {
     RepositoryInfo repositoryInfo = null;
     Service service = new Service();
     Call call = (Call) service.createCall();
     call.setOperationName(new QName(FEDORA_API_URI, DESCRIBE_REPOSITORY) );
     QName qn = new QName(FEDORA_TYPE_URI, "RepositoryInfo");
     call.setTargetEndpointAddress( new URL(FEDORA_ACCESS_ENDPOINT) );
 
     // Any Fedora-defined types required by the SOAP service must be registered
     // prior to invocation so the SOAP service knows the appropriate
     // serializer/deserializer to use for these types.
     call.registerTypeMapping(RepositoryInfo.class, qn,
         new BeanSerializerFactory(RepositoryInfo.class, qn),
         new BeanDeserializerFactory(RepositoryInfo.class, qn));
     repositoryInfo =
         (RepositoryInfo) call.invoke( new Object[] {} );
     return repositoryInfo;
   }
 
    /**
     * <p>Lists the specified fields of each object matching the given
     * criteria.</p>
     *
     * @param resultFields the names of the fields to return
     * @param maxResults the maximum number of results to return at a time
     * @param query the query
     * @return the specified fields of each object matching the given
     *         criteria.
     * @throws Exception If an error occurs in communicating with the Fedora
     *         Access SOAP service.
     */
     public FieldSearchResult findObjects(String[] resultFields, int maxResults,
         FieldSearchQuery query) throws Exception
     {
       FieldSearchResult fieldSearchResult = null;
       Service service = new Service();
       Call call = (Call) service.createCall();
       call.setOperationName(new QName(FEDORA_API_URI, DESCRIBE_REPOSITORY) );
       QName qn = new QName(FEDORA_TYPE_URI, "FieldSearchResult");
       QName qn2 = new QName(FEDORA_TYPE_URI, "FieldSearchQuery");
       call.setTargetEndpointAddress( new URL(FEDORA_ACCESS_ENDPOINT) );
 
       // Any Fedora-defined types required by the SOAP service must be registered
       // prior to invocation so the SOAP service knows the appropriate
       // serializer/deserializer to use for these types.
       call.registerTypeMapping(FieldSearchResult.class, qn,
           new BeanSerializerFactory(FieldSearchResult.class, qn),
           new BeanDeserializerFactory(FieldSearchResult.class, qn));
       call.registerTypeMapping(FieldSearchQuery.class, qn2,
         new BeanSerializerFactory(FieldSearchQuery.class, qn2),
         new BeanDeserializerFactory(FieldSearchQuery.class, qn2));
       fieldSearchResult =
           (FieldSearchResult) call.invoke( new Object[] {resultFields, new Integer(maxResults), query} );
       return fieldSearchResult;
   }
 
 
   /**
    * <p>Resumes an in-progress listing of object fields.</p>
    *
    * @param sessionToken the token of the session in which the remaining
    *        results can be obtained
    * @return the remaining specified fields of each object matching the given
    *         criteria.
    * @throws Exception If an error occurs in communicating with the Fedora
    *         Access SOAP service.
    */
    public FieldSearchResult resumeFindObjects(String sessionToken) throws Exception
    {
      FieldSearchResult fieldSearchResult = null;
      Service service = new Service();
      Call call = (Call) service.createCall();
      call.setOperationName(new QName(FEDORA_API_URI, DESCRIBE_REPOSITORY) );
      QName qn = new QName(FEDORA_TYPE_URI, "FieldSearchResult");
      call.setTargetEndpointAddress( new URL(FEDORA_ACCESS_ENDPOINT) );
 
      // Any Fedora-defined types required by the SOAP service must be registered
      // prior to invocation so the SOAP service knows the appropriate
      // serializer/deserializer to use for these types.
      call.registerTypeMapping(FieldSearchResult.class, qn,
          new BeanSerializerFactory(FieldSearchResult.class, qn),
          new BeanDeserializerFactory(FieldSearchResult.class, qn));
      fieldSearchResult =
          (FieldSearchResult) call.invoke( new Object[] {sessionToken} );
      return fieldSearchResult;
   }
 
   /**
    * <p>Initialize servlet.</p>
    *
    * @throws ServletException If the servet cannot be initialized.
    */
   public void init() throws ServletException
   {
     try
     {
       System.out.println("Realpath Properties File: "
           + getServletContext().getRealPath(soapClientPropertiesFile));
       FileInputStream fis = new FileInputStream(this.getServletContext().getRealPath(soapClientPropertiesFile));
       Properties p = new Properties();
       p.load(fis);
       FEDORA_ACCESS_ENDPOINT = p.getProperty("fedoraEndpoint");
       SOAP_CLIENT_SERVLET_PATH = p.getProperty("soapClientServletPath");
       METHOD_PARM_RESOLVER_SERVLET_PATH = p.getProperty("soapClientMethodParmResolverServletPath");
       System.out.println("FedoraEndpoint: " + FEDORA_ACCESS_ENDPOINT);
       System.out.println("soapClientServletPath: " + SOAP_CLIENT_SERVLET_PATH);
       System.out.println("soapClientMethodParmResolverServletPath: " + METHOD_PARM_RESOLVER_SERVLET_PATH);
       // Locations of the internal Fedora XML schemas are local to the Fedora server so it is
       // the Fedora server hostname and port number are extracted from the
       // FEDORA_ACCESS_ENDPOINT string for easier access within the servlet.
       int i = FEDORA_ACCESS_ENDPOINT.indexOf(":",8);
       int j = FEDORA_ACCESS_ENDPOINT.indexOf("/",i);
       fedoraServerHost = FEDORA_ACCESS_ENDPOINT.substring(7,i);
       fedoraServerPort = FEDORA_ACCESS_ENDPOINT.substring(i+1,j);
       System.out.println("fedoraServerHost: "+fedoraServerHost);
       System.out.println("fedoraServerPort: "+fedoraServerPort);
 
     } catch (Throwable th)
     {
       String message = "[FedoraSOAPServlet] An error has occurred. "
           + "The error was a \"" + th.getClass().getName() + "\"  . The "
           + "Reason: \"" + th.getMessage() + "\"  .";
       throw new ServletException(message);
     }
   }
 
   /**
    * <p>Cleans up servlet resources.</p>
    */
   public void destroy()
   {}
 
   /**
    * <p>Validates required servlet URL parameters. Different parameters
    * are required based on the requested action.</p>
    *
    * @param action The Fedora service to be executed
    * @param PID The persistent identifier of the Digital Object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param methodName The method name.
    * @param versDateTime The version datetime stamp of the digital object.
    * @param h_userParms A hashtabe of user-supplied method parameters.
    * @param response The servlet response.
    * @return True if required parameters are valid; false otherwise.
    * @throws IOException If an error occurrs with an input or output operation.
    */
   private boolean isValidURLParms(String action, String PID, String bDefPID,
                           String methodName, Date versDateTime,
                           Hashtable h_userParms,
                           HttpServletResponse response)
       throws IOException
   {
     // Check for missing parameters required either by the servlet or the
     // requested Fedora Access SOAP service.
     boolean isValid = true;
     ServletOutputStream out = response.getOutputStream();
     String versDate = DateUtility.convertDateToString(versDateTime);
     StringBuffer html = new StringBuffer();
     if (action != null && action.equals(GET_DISSEMINATION))
     {
       if (PID == null || bDefPID == null || methodName == null)
       {
         // Dissemination requires PID, bDefPID, and methodName;
         // asOfDateTime is optional.
         response.setContentType(CONTENT_TYPE_HTML);
         html.append("<html>");
         html.append("<head>");
         html.append("<title>FedoraAccessSOAPServlet</title>");
         html.append("</head>");
         html.append("<body>");
         html.append("<p><font size='+1' color='red'>"
                     + "Required parameter missing "
                     + "in Dissemination Request:</font></p>");
         html.append("<table cellpadding='5'>");
         html.append("<tr>");
         html.append("<td><font color='red'>action_</font></td>");
         html.append("<td> = </td>");
         html.append("<td>" + action + "</td>");
         html.append("<td><font color='blue'>(REQUIRED)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td><font color='red'>PID_</font></td>");
         html.append("<td> = </td>");
         html.append("<td>" + PID + "</td>");
         html.append("<td><font color='blue'>(REQUIRED)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td><font color='red'>bDefPID_</font></td>");
         html.append("<td> = </td><td>" + bDefPID + "</td>");
         html.append("<td><font color='blue'>(REQUIRED)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td><font color='red'>methodName_</font></td>");
         html.append("<td> = </td>");
         html.append("<td>" + methodName + "</td>");
         html.append("<td><font color='blue'>(REQUIRED)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td><font color='red'>asOfDateTime_</font></td>");
         html.append("<td> = </td>");
         html.append("<td>" + versDate + "</td>");
         html.append("<td><font color='green'>(OPTIONAL)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td colspan='5'><font size='+1' color='blue'>"
                     + "Other Parameters Found:</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("</tr>");
         for (Enumeration e = h_userParms.keys() ; e.hasMoreElements(); )
         {
           String name = (String)e.nextElement();
           html.append("<tr>");
           html.append("<td><font color='red'>"+name+"</font></td>");
           html.append("<td>= </td>");
           html.append("<td>" + h_userParms.get(name) + "</td>");
           html.append("</tr>");
         }
         html.append("</table>");
         html.append("</body>");
         html.append("</html>");
         out.println(html.toString());
         isValid = false;
       }
       //FIXME!! Validation for any user-supplied parameters not implemented.
     } else if (action != null &&
                (action.equals(GET_BEHAVIOR_DEFINITIONS) ||
                 action.equals(GET_OBJECT_METHODS) ||
                 action.equals(GET_OBJECT_PROFILE)))
     {
       if (PID == null)
       {
         // GetBehaviorDefinitions and GetObjectMethods require PID;
         // asOfDateTime is optional.
         response.setContentType(CONTENT_TYPE_HTML);
         html.append("<html>");
         html.append("<head>");
         html.append("<title>FedoraAccessSOAPServlet</title>");
         html.append("</head>");
         html.append("<body>");
         html.append("<p><font size='+1' color='red'>"
                     + "Required parameter missing in Behavior "
                     + "Definition Request:</font></p>");
         html.append("<table cellpadding='5'>");
         html.append("<tr>");
         html.append("<td><font color='red'>action_</td>");
         html.append("<td> = </td>");
         html.append("<td>" + action + "</td>");
         html.append("<td><font color='blue'>(REQUIRED)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td><font color='red'>PID_</td>");
         html.append("<td> = </td>");
         html.append("<td>" + PID + "</td>");
         html.append("<td><font color='blue'>(REQUIRED)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td><font color='red'>asOfDateTime_</td>");
         html.append("<td> = </td>");
         html.append("<td>" + versDate + "</td>");
         html.append("<td><font color='green'>(OPTIONAL)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td colspan='5'><font size='+1' color='blue'>"
                     + "Other Parameters Found:</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("</tr>");
         for (Enumeration e = h_userParms.keys() ; e.hasMoreElements(); )
         {
           String name = (String)e.nextElement();
           html.append("<tr>");
           html.append("<td><font color='red'>"+name+"</font></td>");
           html.append("<td>= </td>");
           html.append("<td>"+h_userParms.get(name)+"</td>");
           html.append("</tr>");
         }
         html.append("</table>");
         html.append("</body>");
         html.append("</html>");
         out.println(html.toString());
         isValid = false;
       }
     } else if (action != null &&
                (action.equalsIgnoreCase(GET_BEHAVIOR_METHODS) ||
                action.equalsIgnoreCase(GET_BEHAVIOR_METHODS_XML)))
     {
       if (PID == null || bDefPID == null)
       {
         // GetBehaviorMethods and GetBehaviorMethodsXML require PID, bDefPID;
         // asOfDateTime is optional.
         response.setContentType(CONTENT_TYPE_HTML);
         html.append("<html>");
         html.append("<head>");
         html.append("<title>FedoraAccessSOAPServlet</title>");
         html.append("</head>");
         html.append("<body>");
         html.append("<p><font size='+1' color='red'>"
                     + "Required parameter missing in Behavior "
                     + "Methods Request:</font></p>");
         html.append("<table cellpadding='5'>");
         html.append("<tr>");
         html.append("<td><font color='red'>action_</td>");
         html.append("<td> = </td>");
         html.append("<td>" + action + "</td>");
         html.append("<td><font color='blue'>(REQUIRED)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td><font color='red'>PID_</td>");
         html.append("<td> = </td>");
         html.append("<td>" + PID + "</td>");
         html.append("<td><font color='blue'>(REQUIRED)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td><font color='red'>bDefPID_</td>");
         html.append("<td> = </td>");
         html.append("<td>" + bDefPID + "</td>");
         html.append("<td><font color='blue'>(REQUIRED)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<tr>");
         html.append("<td><font color='red'>asOfDateTime_</td>");
         html.append("<td> = </td>");
         html.append("<td>" + versDate + "</td>");
         html.append("<td><font color='green'>(OPTIONAL)</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("<td colspan='5'><font size='+1' color='blue'>"
                     + "Other Parameters Found:</font></td>");
         html.append("</tr>");
         html.append("<tr>");
         html.append("</tr>");
         for (Enumeration e = h_userParms.keys() ; e.hasMoreElements(); )
         {
           String name = (String)e.nextElement();
           html.append("<tr>");
           html.append("<td><font color='red'>"+name+"</font></td>");
           html.append("<td>= </td>");
           html.append("<td>" + h_userParms.get(name) + "</td>");
           html.append("</tr>");
         }
         html.append("</table>");
         html.append("</body>");
         html.append("</html>");
         out.println(html.toString());
         isValid = false;
       }
     } else if (action != null && (action.equalsIgnoreCase(DESCRIBE_REPOSITORY)))
     {
       System.out.println("Validated DESCRIBE_REPOSITORY as good request w/no parms");
       isValid = true;
     } else
     {
       System.out.println("Unknown API-A request encountered.");
       // Unknown Fedora service has been requested.
       response.setContentType(CONTENT_TYPE_HTML);
       html.append("<html>");
       html.append("<head>");
       html.append("<title>FedoraAccessSOAPServlet</title>");
       html.append("</head>");
       html.append("<body>");
       html.append("<p><font size='+1' color='red'>Invalid 'action' "
                   + "parameter specified in Servlet Request: action= "
                   + action+"<p>");
       html.append("<br></br><font color='blue'>Reserved parameters "
                   + "in Request:</font>");
       html.append("<table cellpadding='5'>");
       html.append("<tr>");
       html.append("<td><font color='red'>action_</td>");
       html.append("<td> = </td>");
       html.append("<td>" + action + "</td>");
       html.append("</tr>");
       html.append("<tr>");
       html.append("<td><font color='red'>PID_</td>");
       html.append("<td> = </td>");
       html.append("<td>" + PID + "</td>");
       html.append("</tr>");
       html.append("<tr>");
       html.append("<td><font color='red'>bDefPID_</td>");
       html.append("<td> = </td>");
       html.append("<td>" + bDefPID + "</td>");
       html.append("</tr>");
       html.append("<tr>");
       html.append("<td><font color='red'>methodName_</td>");
       html.append("<td> = </td>");
       html.append("<td>" + methodName + "</td>");
       html.append("</tr>");
       html.append("<tr>");
       html.append("<td><font color='red'>asOfDateTime_</td>");
       html.append("<td> = </td>");
       html.append("<td>" + versDate + "</td>");
       html.append("</tr>");
       html.append("<tr>");
       html.append("</tr>");
       html.append("<tr>");
       html.append("<td colspan='5'><font size='+1' color='blue'>"
                   + "Other Parameters Found:</font></td>");
       html.append("</tr>");
       html.append("<tr>");
       html.append("</tr>");
       for (Enumeration e = h_userParms.keys() ; e.hasMoreElements(); )
       {
         String name = (String)e.nextElement();
         html.append("<tr>");
         html.append("<td><font color='red'>"+name+"</font></td>");
         html.append("<td>= </td>");
         html.append("<td>" + h_userParms.get(name) + "</td>");
         html.append("</tr>");
       }
       html.append("</table>");
       html.append("</body>");
       html.append("</html>");
       out.println(html.toString());
       isValid = false;
     }
 
     return isValid;
   }
 
   /**
    * <p>Displays a list of the servlet input parameters. This method is
    * generally called when a service request returns no data. Usually
    * this is a result of an incorrect spelling of either a required
    * URL parameter or in one of the user-supplied parameters. The output
    * from this method can be used to help verify the URL parameters
    * sent to the servlet and hopefully fix the problem.</p>
    *
    * @param action The Fedora service requested.
    * @param PID The persistent identifier of the digital object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param methodName the name of the method.
    * @param asOfDateTime The version datetime stamp of the digital object.
    * @param userParms An array of user-supplied method parameters and values.
    * @param response The servlet response.
    * @param message The message text to include at the top of the output page.
    * @throws IOException If an error occurrs with an input or output operation.
    */
   private void showURLParms(String action, String PID, String bDefPID,
                            String methodName, Calendar asOfDateTime,
                            Property[] userParms,
                            HttpServletResponse response,
                            String message)
       throws IOException
   {
 
     String versDate = DateUtility.convertCalendarToString(asOfDateTime);
     ServletOutputStream out = response.getOutputStream();
     response.setContentType(CONTENT_TYPE_HTML);
 
     // Display servlet input parameters
     StringBuffer html = new StringBuffer();
     html.append("<html>");
     html.append("<head>");
     html.append("<title>FedoraAccessSOAPServlet</title>");
     html.append("</head>");
     html.append("<body>");
     html.append("<br></br><font size='+2'>" + message + "</font>");
     html.append("<br></br><font color='red'>Request Parameters</font>");
     html.append("<br></br>");
     html.append("<table cellpadding='5'>");
     html.append("<tr>");
     html.append("<td><font color='red'>action_</td>");
     html.append("<td> = </td>");
     html.append("<td>" + action + "</td>");
     html.append("</tr>");
     html.append("<tr>");
     html.append("<td><font color='red'>PID_</td>");
     html.append("<td> = <td>" + PID + "</td>");
     html.append("</tr>");
     html.append("<tr>");
     html.append("<td><font color='red'>bDefPID_</td>");
     html.append("<td> = </td>");
     html.append("<td>" + bDefPID + "</td>");
     html.append("</tr>");
     html.append("<tr>");
     html.append("<td><font color='red'>methodName_</td>");
     html.append("<td> = </td>");
     html.append("<td>" + methodName + "</td>");
     html.append("</tr>");
     html.append("<tr>");
     html.append("<td><font color='red'>asOfDateTime_</td>");
     html.append("<td> = </td>");
     html.append("<td>" + versDate + "</td>");
     html.append("</tr>");
     html.append("<tr>");
     html.append("</tr>");
     html.append("<tr>");
     html.append("<td colspan='5'><font size='+1' color='blue'>"+
                 "Other Parameters Found:</font></td>");
     html.append("</tr>");
     html.append("<tr>");
     html.append("</tr>");
 
     // List user-supplied parameters if any
     if (userParms != null)
     {
     for (int i=0; i<userParms.length; i++)
     {
       html.append("<tr>");
       html.append("<td><font color='red'>" + userParms[i].getName()
                   + "</font></td>");
       html.append("<td> = </td>");
       html.append("<td>" + userParms[i].getValue() + "</td>");
         html.append("</tr>");
     }
     }
     html.append("</table></center></font>");
     html.append("</body></html>");
     out.println(html.toString());
 
     System.err.println("REQUEST Returned NO Data");
   }
 }
