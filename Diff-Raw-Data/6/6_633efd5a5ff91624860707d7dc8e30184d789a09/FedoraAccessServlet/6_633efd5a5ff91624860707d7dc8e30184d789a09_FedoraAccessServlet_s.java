 package fedora.server.access;
 
 /**
  * <p>Title: FedoraAccessServlet.java</p>
  * <p>Description: Implements Fedora Access interface</p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 
 // Fedora imports
 import fedora.server.access.localservices.HttpService;
 import fedora.server.errors.HttpServiceNotFoundException;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.errors.MethodParmNotFoundException;
 import fedora.server.storage.DefinitiveBMechReader;
 import fedora.server.storage.DefinitiveDOReader;
 import fedora.server.storage.FastDOReader;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.utilities.DateUtility;
 
 // Java imports
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.ServletException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Vector;
 
 public class FedoraAccessServlet extends HttpServlet implements FedoraAccess
 {
 
   private Hashtable disseminationCache = new Hashtable();
   private HttpSession session = null;
   private static final String CONTENT_TYPE_HTML = "text/html";
   private static final String CONTENT_TYPE_XML = "text/xml";
   private static final String GET_BEHAVIOR_DEFINITIONS =
       "GetBehaviorDefinitions";
   private static final String GET_BEHAVIOR_METHODS = "GetBehaviorMethods";
   private static final String GET_DISSEMINATION = "GetDissemination";
   private static final String VIEW_OBJECT = "ViewObject";
   private static final String LOCAL_ADDRESS_LOCATION = "LOCAL";
   private static final String YES = "yes";
   private static final int DISS_CACHE_SIZE = 100;
   private Hashtable v_userParms = new Hashtable();
 
   // For Testing
   private static final boolean debug = true;
 
 
   /**
    * Initialize servlet
    *
    * @throws ServletException
    */
   public void init() throws ServletException
   {}
 
   /**
    * Process Post request.
    *
    * @param request
    * @param response
    * @throws ServletException
    * @throws IOException
    */
   public void doPost(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
    // treat Post same as a Get
    doGet(request, response);
   }
 
   /**
    * Process Fedora Access Request.
    *
    * @param request - Servlet request
    * @param response - Servlet response
    * @throws ServletException
    * @throws IOException
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
     String action = "";
     String PID = null;
     String bDefPID = null;
     String methodName = null;
     Calendar asOfDate = null;
     String versDate = null;
     String clearCache = "";
     String requestURI = null;
     Vector userParms = new Vector();
     this.getServletContext().log("message", new Exception());
 
     Enumeration params = request.getParameterNames();
     requestURI = request.getServerName()+":"+request.getServerPort()+
                  request.getRequestURI()+"?"+request.getQueryString();
     session = request.getSession(true);
     PrintWriter out = response.getWriter();
     if (debug) System.out.println("RequestURI: "+requestURI+
                                   "Session: "+session);
 
     while ( params.hasMoreElements())
     {
       String param = (String) params.nextElement();
       if (param.equals("action_"))
       {
         action = request.getParameter(param);
       } else if (param.equals("PID_"))
       {
         PID = request.getParameter(param);
       } else if (param.equals("bDefPID_"))
       {
         bDefPID = request.getParameter(param);
       } else if (param.equals("methodName_"))
       {
         methodName = request.getParameter(param);
       } else if (param.equals("asOfDate_"))
       {
         versDate = request.getParameter(param);
         asOfDate = DateUtility.convertStringToCalendar(versDate);
       } else if (param.equals("clear-cache_"))
       {
         clearCache = request.getParameter(param);
       } else
       {
         // get any user-supplied parameters
         String parmvalue = request.getParameter(param);
         userParms.addElement(param);
         userParms.addElement(parmvalue);
         v_userParms.put(param, parmvalue);
       }
     }
 
     // Check for required URL parameters
     // Perform requested acton if required parameters are present
     if (checkURLParams(action, PID, bDefPID, methodName, versDate,
                       userParms, clearCache, response))
     {
       // FIXME!! Session management code needs to go here
       if (action.equals(GET_DISSEMINATION))
       {
         // See if dissemination request is in local cache
         MIMETypedStream dissemination = null;
         dissemination = checkCache(requestURI, action, PID, bDefPID, methodName,
                                    userParms, asOfDate, clearCache, response);
         if (dissemination != null)
         {
           response.setContentType(dissemination.MIMEType);
           int byteStream = 0;
           ByteArrayInputStream dissemResult =
               new ByteArrayInputStream(dissemination.stream);
           while ((byteStream = dissemResult.read()) >= 0)
           {
             out.write(byteStream);
           }
         }
       } else if (action.equals(GET_BEHAVIOR_DEFINITIONS))
       {
         String servletURL = "http://"+request.getServerName()+":"+
                                     request.getServerPort()+
                                     request.getRequestURI()+"?";
         if(debug) System.out.println("servletURL: "+servletURL);
         response.setContentType(CONTENT_TYPE_HTML);
         // FIXME!! versioning based on datetime stamp not yet implemented
         String[] bDefs = GetBehaviorDefinitions(PID, asOfDate);
         out.println("<br><table border='1' cellspacing='2' cellpadding='5'>");
         out.println("<tr>");
         out.println("<td><b><font size='+2'><b>PID</font></td></b>");
         out.println("<td><b><font size='+2'>Version Date</font></b></td>");
         out.println("<td><b><font size='+2'>Behavior Definitions</font>"+
                     "</b></td");
         out.println("</tr>");
 
         for (int i=0; i<bDefs.length; i++)
         {
           out.println("<tr>");
           out.println("<td><font color='blue'><a href='"+servletURL+
                       "action_=ViewObject&PID_="+PID+"'>"+PID+
                       "</a></font></td>");
           out.flush();
           out.println("<td><font color='blue'>"+versDate+"</font></td>");
           out.println("<td><font color='red'>"+bDefs[i]+"</font></td></tr>");
         }
         out.println("</table><br>");
         out.println("</body></html>");
       } else if (action.equals(GET_BEHAVIOR_METHODS))
       {
         try
         {
           // FIXME!! versioning based on datetime stamp not yet implemented
           MIMETypedStream bDefMethods = GetBehaviorMethods(PID, bDefPID, asOfDate);
           if (bDefMethods == null)
           {
             emptyResult(action, PID, bDefPID, methodName, asOfDate, userParms,
                         clearCache, response);
           } else
           {
 
             ByteArrayInputStream methodResults =
                 new ByteArrayInputStream(bDefMethods.stream);
             response.setContentType(bDefMethods.MIMEType);
             if (debug) System.out.println("MIMEType: "+bDefMethods.MIMEType);
             // WSDL is actually just an XML fragment so add appropriate
             // XML namespace and XML declaration to make a valid XML
             // output stream
             // FIXME!! Should these be added automatically in
             // the class DefinitiveBMechReader??
             out.println("<?xml version=\"1.0\"?>");
             out.println("<definitions xmlns:xsd=\"http://www.w3.org/2000/10/"+
                         "XMLSchema-instance\" xmlns:wsdl=\"http://schemas."+
                         "xmlsoap.org/wsdl/\">");
             int byteStream = 0;
             while ((byteStream = methodResults.read()) >= 0)
             {
               out.write(byteStream);
             }
             out.println("</definitions>");
           }
         } catch (IOException ioe)
         {
           System.out.println(ioe);
           this.getServletContext().log(ioe.getMessage(), ioe.getCause());
         }
       } else if (action.equals(VIEW_OBJECT))
       {
         response.setContentType(CONTENT_TYPE_HTML);
         //out.println("<html><body>");
         viewObject(PID, bDefPID, methodName, asOfDate, request, out);
       }
     }
   }
 
   /**
    * Method that implements GetBehaviorDefinitions in the FedoraAccess
    * interface. The method returns a string array containing a list of the
    * Behavior Definitions of the associated digital object.
    *
    * @param PID - persistent identifier of the digital object
    * @param asOfDate - version datetime stamp of the digital object
    * @return - String[] containing Behavior Definitions
    */
   public String[] GetBehaviorDefinitions(String PID, Calendar asOfDate)
   {
     DefinitiveDOReader doReader = new DefinitiveDOReader(PID);
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDate);
     // FIXME!! versioning based on datetime not yet implemented
     return doReader.GetBehaviorDefs(versDateTime);
   }
 
   /**
    * Method that implements GetBehaviorMethods in the FedoraAccess interface.
    * The method returns a bytestream containing the WSDL that defines
    * the Behavior Methods of the associated Behavior Definition object.
    *
    * @param PID - persistent identifier of Digital Object
    * @param bDefPID - persistent identifier of Behavior Definition object
    * @param asOfDate - version datetime stamp of the digital object
    * @return - MIMETypedStream containing WSDL definitions for methods
    */
   public MIMETypedStream GetBehaviorMethods(String PID, String bDefPID,
       Calendar asOfDate)
   {
     ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
     MIMETypedStream bDefMethods = null;
     try
     {
       DefinitiveBMechReader doReader = new DefinitiveBMechReader(bDefPID);
       // FIXME!! versioning based on datetime not yet implemented
       InputStream methodResults = doReader.GetBehaviorMethodsWSDL(null);
       int byteStream = 0;
       while ((byteStream = methodResults.read()) >= 0)
       {
         baos.write(byteStream);
       }
     } catch (IOException ioe)
     {
       System.out.println(ioe);
       this.getServletContext().log(ioe.getMessage(), ioe.getCause());
     }
     bDefMethods = new MIMETypedStream(CONTENT_TYPE_XML,baos.toByteArray());
 
     return(bDefMethods);
 
   }
 
   /**
    * Method that implements Getdissemination in the FedoraAccess interface.
    * the method returns a MIME-typed bytestream containing the result
    * of a dissemination request.
    *
    * @param PID - persistent identifier of the Digital Object
    * @param bDefPID - persistent identifier of the Behavior Definition object
    * @param methodName - name of the method
    * @param asOfDate - version datetime stamp of the digital object
    * @param userParms - array of user-supplied method parameters and values
    * @return - MIMETypedStream containing the dissemination result
    */
   public MIMETypedStream GetDissemination(String PID, String bDefPID,
        String methodName, Vector userParms, Calendar asOfDate)
    {
      String protocolType = null;
      Vector dissResult = null;
      String dissURL = null;
      String operationLocation = null;
      MIMETypedStream dissemination = null;
      Date versDateTime = null;
      if (!(asOfDate == null))
      {
        versDateTime = DateUtility.convertCalendarToDate(asOfDate);
      }
      FastDOReader fastReader = null;
      try
      {
        fastReader = new FastDOReader(PID, bDefPID, methodName,
            versDateTime);
        dissResult = fastReader.getDissemination(PID, bDefPID, methodName,
            versDateTime);
        String replaceString = null;
        DissResultSet results = new DissResultSet();
        // Build a hashtable of the dissemination result sets to be used
        // as a way of indexing the different result sets.
        Enumeration e = dissResult.elements();
        Hashtable h = new Hashtable();
        int index = 1;
        Integer key = new Integer(index);
        while (e.hasMoreElements())
        {
          results = new DissResultSet((String[])e.nextElement());
          if (debug) System.out.println("KEY: "+key+" VALUE: "+results.dsBindingKey);
          h.put(key,results);
          index++;
          key = new Integer(index);
        }
        int counter = 1;
        int numElements = dissResult.size();
        e = dissResult.elements();
        // Get row(s) of WSDL results and perform string substitution
        // on DSBindingKey and method parameter values in WSDL
        // Note: In case where more than one datastream matches the
        // DSBindingKey or there are multiple DSBindingKeys for the
        // method, multiple rows will be returned; otherwise
        // a single row is returned.
        while (e.hasMoreElements())
        {
          results = new DissResultSet((String[])e.nextElement());
          // If AddressLocation is LOCAL, this is a flag to indicate
          // the associated OperationLocation requires no AddressLocation.
          // i.e., the OperationLocation contains all information necessary
          // to perform the dissemination request.
          if (results.addressLocation.equals(LOCAL_ADDRESS_LOCATION))
          {
            results.addressLocation = "";
          }
          // Match DSBindingKey pattern in WSDL
          String bindingKeyPattern = "\\("+results.dsBindingKey+"\\)";
          if (counter == 1)
          {
            operationLocation = results.operationLocation;
            dissURL = results.addressLocation+operationLocation;
            protocolType = results.protocolType;
          }
          if (debug) System.out.println("counter: "+counter+" numelem: "+numElements);
          String currentKey = results.dsBindingKey;
          Integer hashKey = null;
          String nextKey = "";
          if (counter != numElements)
          {
            // Except for last row, get the value of the next binding key
            // out of the hashtable to compare with the value of the current
            // binding key.
           key = new Integer(counter+1);
            DissResultSet result2 = (DissResultSet)h.get(hashKey);
            nextKey = result2.dsBindingKey;
           if (debug) System.out.println("key: '"+key+"' currentKey: '"+currentKey+"' nextKey: '"+nextKey+"'");
          }
          // In most cases, there is only a single datastream that matches a given
          // DSBindingKey so the substitution process is to just replace the
          // occurence of (BINDING_KEY) with the value of the datastream location.
          // However, when multiple datastreams match the same DSBindingKey, the
          // occurrence of (BINDING_KEY) is replaced with the value of the
          // datastream location and the value +(BINDING_KEY) is appended so that
          // subsequent datastreams matching the binding key will be substituted.
          // The end result is that the binding key will be replaced by a string
          // datastream locations separated by a plus(+) sign. e.g.,
          //
          // file=(PHOTO) becomes
          // file=dslocation1+dslocation2+dslocation3
          //
          // It is the responsibility of the Behavior Mechanism to know how to
          // handle an input parameter with multiple datastreams.
          //
          // In the case of a method containing multiple binding keys,
          // substitutions are performed on each binding key. e.g.,
          //
          // image=(PHOTO)&watermark=(WATERMARK) becomes
          // image=dslocation1&watermark=dslocation2
          //
          // In the case with mutliple binding keys and multiple datastreams,
          // the substitution might appear like the following:
          //
          // image=(PHOTO)&watermark=(WATERMARK) becomes
          // image=dslocation1+dslocation2&watermark=dslocation3
          if (nextKey.equalsIgnoreCase(currentKey) & counter != numElements)
          {
            replaceString = results.dsLocation+"+("+results.dsBindingKey+")";
          } else
          {
            replaceString = results.dsLocation;
          }
          if (debug) System.out.println("replaceString: "+replaceString);
          dissURL = substituteString(dissURL, bindingKeyPattern, replaceString);
          counter++;
          if (debug) System.out.println("replaced dissURL = "+
                                       dissURL.toString()+
                                       " counter = "+counter);
        }
        // FIXME!! need to implement handling of user-supplied method parameters
        // Would need to validate user-supplied parameters and then substitute
        // values in operationLocation.
 
        // FIXME!! need to implement Access Policy control
 
        // Resolve content referenced by dissemination result
        if (debug) System.out.println("ProtocolType = "+protocolType);
        if (protocolType.equalsIgnoreCase("http"))
        {
          HttpService httpService = new HttpService(dissURL);
          try
          {
            dissemination = httpService.getHttpContent(dissURL);
          } catch (HttpServiceNotFoundException onfe)
          {
            System.out.println(onfe.getMessage());
          }
        } else if (protocolType.equalsIgnoreCase("soap"))
        {
          // FIXME!! future handling by soap interface
          System.out.println("Protocol type specified: "+protocolType);
          dissemination = null;
        } else
        {
          System.out.println("Unknown protocol type: "+protocolType);
          dissemination = null;
        }
      } catch (ObjectNotFoundException onfe)
      {
        // Object was not found in SQL database or in XML storage area
        System.out.println("getdissem: ObjectNotFound");
        this.getServletContext().log(onfe.getMessage(), onfe.getCause());
      }
      return dissemination;
    }
 
   public void viewObject(String PID, String bDefPID, String methodName,
                          Calendar asOfDate, HttpServletRequest request, PrintWriter out) throws IOException
   {
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDate);
     FastDOReader fastReader = null;
     Vector queryResults = null;
 
     try
     {
       fastReader = new FastDOReader(PID, bDefPID, methodName, versDateTime);
       queryResults = fastReader.getObject(PID, versDateTime);
       String servletURL = request.getServerName()+":"+request.getServerPort()+
                    request.getRequestURI();
       if (debug) System.out.println("servletURL: "+servletURL);
       out.println("<br><br><center><b><font color=\"purple\">" +
                   "DIGITAL OBJECT:</font><font color=\"blue\"> " +
                   PID + " </font></b></center>\n");
       out.println("<br><center><table border>\n");
       Enumeration e = queryResults.elements();
       out.println("<tr>");
       for (int i=0; i<5; i++)
       {
         switch(i)
         {
           case 0: out.println("<th><font color=\"blue\"> Object PID "+
                               " </font></th>");
           break;
           case 1: out.println("<th><font color=\"green\"> Disseminator ID"+
                               " </font></th>");
           break;
           case 2: out.println("<th><font color=\"green\"> BDEF PID"+
                               " </font></th>");
           break;
           case 3: out.println("<th><font color=\"red\"> BMECH PID"+
                               " </font></th>");
           break;
           case 4: out.println("<th><font color=\"red\"> Method Name"+
                               " </font></th>");
           break;
           default: out.println("<td><font color=\"black\"></font></td>");
         }
       }
       out.println("</tr>");
       while (e.hasMoreElements())
       {
         String[] results = (String[])e.nextElement();
         out.println("<tr>");
         for (int i=0; i<results.length; i++)
         {
           switch(i)
           {
             case 0: out.println("<td><font color=\"blue\"> " + results[0]+
                                 " </font></td>");
             break;
             case 1: out.println("<td><font color=\"green\"> " + results[1] +
                                 " </font></td>");
             break;
             case 2: out.println("<td><font color=\"green\"> " + results[2] +
                                 " </font></td>");
             break;
             case 3: out.println("<td><font color=\"green\"> " + results[3] +
                                 " </font></td>");
             break;
             case 4: out.println("<td><font color=\"red\"> " +
                                 "<a href=\"http://"+servletURL+
                                 "?action_=GetDissemination&PID_=" +
                                 results[0] + "&bDefPID_=" + results[2] +
                                 "&methodName_=" + results[4] + "\"> " +
                                 results[4] + " </a> </td>");
             break;
             default: out.println("<td><font color=\"black\"> " + results[i-1] +
                                  " </font></td>");
           }
         }
         out.println("</tr>");
       }
     } catch (ObjectNotFoundException onfe)
     {
       System.out.println(onfe.getMessage());
       this.getServletContext().log(onfe.getMessage(), onfe.getCause());
     }
   }
 
   /**
    * Method to perform simple string replacement using regular expressions.
    * All mathcing occurrences of the pattern string will be replaced in the
    * input string by the replacement string.
    *
    * @param inputString - source string
    * @param patternString - regular expression pattern
    * @param replaceString - replacement string
    * @return - source string with substitutions
    */
   private String substituteString(String inputString, String patternString,
                                  String replaceString)
   {
     Pattern pattern = Pattern.compile(patternString);
     Matcher m = pattern.matcher(inputString);
     return(m.replaceAll(replaceString));
   }
 
   /**
    * Method to validate user-supplied method parameters against values
    * in the corresponding Behavior Definition object.
    *
    * @param PID - persistent identifier of the Digital Object
    * @param bDefPID - persistent identifier of the Behavior Definition object
    * @param methodName - name of the method
    * @param userParms - array of user-supplied method parameters and values
    * @return - true if method parameters are valid; false otherwise
    *
    */
   private boolean validateUserParms(String PID, String bDefPID, String methodName,
                                    Hashtable v_userParms, Date versDateTime)
   {
     boolean valid = true;
     FastDOReader fdor = null;
     Vector methodParms = new Vector();
     try
     {
       fdor = new FastDOReader(PID, bDefPID, methodName, versDateTime);
       methodParms = fdor.getMethodParms(bDefPID, methodName, versDateTime);
     } catch(MethodParmNotFoundException mpnfe)
     {
       System.out.println(mpnfe.getMessage());
       this.getServletContext().log(mpnfe.getMessage(), mpnfe.getCause());
     } catch (ObjectNotFoundException onfe)
     {
       System.out.println(onfe.getMessage());
       this.getServletContext().log(onfe.getMessage(), onfe.getCause());
     }
     // Put valid method parameters and their attributes into hashtable
     Enumeration e = methodParms.elements();
     Hashtable v_validParms = new Hashtable();
     while (e.hasMoreElements())
     {
       ParmResultSet prs = new ParmResultSet((String[])e.nextElement());
       v_validParms.put(prs.name,prs);
     }
     // check if no user supplied parameters
     if (!v_userParms.isEmpty())
     {
       // Iterate over each user supplied parameter name
       Enumeration parmNames = v_userParms.keys();
       while (parmNames.hasMoreElements())
       {
         ParmResultSet prs = null;
         String name = (String)parmNames.nextElement();
         prs = (ParmResultSet)v_validParms.get(name);
         if (prs != null && prs.name != null)
         {
           // Method has at least one parameter and name matches userParm
           if (prs.requiredFlag.equalsIgnoreCase("Y"))
           {
             // Method parm is required
             if (v_userParms.get(prs.name) == null)
             {
               // Error: required method parameter not in user-supplied list
               System.out.println("REQUIRED PARAMETER:" + prs.name + " NOT FOUND");
               valid = false;
             } else
             {
               // Required parameter found
               if (debug) System.out.println("Required parameter FOUND: "+prs.name);
             }
           }
           // Method parameter is not required
           // Check for default value if user-supplied value is null(empty string)
           String value = (String)v_userParms.get(prs.name);
           if (value != null && value.equalsIgnoreCase(""))
           {
             if(prs.defaultValue != null)
             {
               v_userParms.put(prs.name,prs.defaultValue);
               if (debug) System.out.println("SET DEFAULT VALUE: "+prs.defaultValue);
             }
           } else
           {
             if (debug) System.out.println("NO DEFAULT VALUES");
           }
           String s = (String)v_userParms.get(prs.name);
           if (!v_userParms.isEmpty() && v_userParms.get(prs.name) == null)
           {
             System.out.println("USER SUPPLIED PARAMETER NOT VALID FOR THIS METHOD: "+prs.name);
             valid = false;
           }
         } else
         {
           if (debug) System.out.println("NAME NOT FOUND: "+name);
         }
     }
     } else
     {
       // There were no user supplied parameters.
       // Check if this method has any required parameters.
       e = methodParms.elements();
       while (e.hasMoreElements())
       {
         ParmResultSet prs = new ParmResultSet((String[])e.nextElement());
         if (prs.requiredFlag.equalsIgnoreCase("Y"))
         {
           if (debug) System.out.println("emptyREQUIRED PARAM NAME NOT FOUND: "+prs.name);
         } else
         {
           if (debug) System.out.println("emptyNON-REQUIRED PARAM FOUND: "+prs.name);
         }
       }
     }
     return(valid);
   }
 
   private void emptyResult(String action, String PID, String bDefPID,
                            String methodName, Calendar asOfDate,
                            Vector userParms, String clearCache, HttpServletResponse response)
       throws IOException
   {
 
     String versDate = DateUtility.convertCalendarToString(asOfDate);
     //String versDate = "";
     //if (asOfDate != null)
     //{
       //Date date = asOfDate.getTime();
       //versDate = formatter.format(date);
     if (debug) System.out.println("versdate: "+versDate);
     //}
     PrintWriter out = response.getWriter();
     response.setContentType(CONTENT_TYPE_HTML);
     out.println("<html>");
     out.println("<head><title>FedoraServlet</title></head>");
     out.println("<body>");
     out.println("<br></br><h3>REQUEST Returned NO Data</h3>");
     out.println("<br></br><font color='red'>Request Parameters</font>"+
                 "<br></br>");
     out.println("<table>"+
                 "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                 action+"</td></tr>"+
                 "<tr><td><font color='red'>PID_</td><td> = <td>"+PID+
                 "</td></tr></font><tr><td><font color='red'>bDefPID_</td>"+
                 "<td> = </td><td>"+bDefPID+"</td></tr></font>"+
                 "<tr><td><font color='red'>methodName_</td><td> = </td>"+
                 "<td>"+methodName+"</td></tr></font><tr><td><font color='red'>"+
                 "AsOfDate_</td><td> = </td><td>"+versDate+"</td></tr></font>"+
                 "<tr><td><font color='red'>clear-cache_</td><td> = </td>");
     out.println("<tr></tr><tr><td colspan='5'><font color='blue'>"+
                 "Other-Parameters:</font></td></tr><tr></tr>");
     for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
     {
       out.println("<tr><td><font color='red'>"+e.nextElement()+" </td>"+
       "<td>= </td><td>"+e.nextElement()+"</td></tr></font>");
     }
     out.println("</table></font>");
     out.println("</body></html>");
 
     if (debug)
     {
       System.out.println("PID: "+PID+"bDEF: "+bDefPID+"methodName: "+methodName);
       for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
       {
         System.out.println("<p>userParm: "+e.nextElement()+
         " userValue: "+e.nextElement());
       }
     }
     System.out.println("REQUEST Returned NO Data");
   }
 
   /**
    * Method to validate required servlet URL parameters. Different parameters
    * are required based on the requested action.
    *
    * @param action - servlet action to be executed
    * @param PID - persistent identifier of the Digital Object
    * @param bDefPID - persistent identifier of the Behavior Definition object
    * @param methodName - method name
    * @param versDate - version datetime stamp of the digital object
    * @param userParms - user-supplied method parameters
    * @param clearCache - boolean to clear dissemination cache
    * @param response - Servlet http response
    * @return - true if required parameters are valid; false otherwise
    * @throws IOException
    */
   private boolean checkURLParams(String action, String PID, String bDefPID,
                           String methodName, String versDate, Vector userParms,
                           String clearCache, HttpServletResponse response)
       throws IOException
   {
     // check for missing required parameters
     boolean checkOK = true;
     if (action.equals(GET_DISSEMINATION))
     {
       if (PID == null || bDefPID == null || methodName == null)
       {
         PrintWriter out = response.getWriter();
         response.setContentType(CONTENT_TYPE_HTML);
         out.println("<html>");
         out.println("<head><title>FedoraServlet</title></head>");
         out.println("<body>");
         out.println("<br></br><font color='red'>Required parameter missing "+
                     "in Dissemination Request:</font>");
         out.println("<table>"+
                     "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                     action+"</td><td><font color='blue'>(REQUIRED)</font></td>"+
                     "</tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = </td><td>"+PID+
                     "</td><td><font color='blue'>(REQUIRED)</font></td></tr>"+
                     "</font><tr><td><font color='red'>bDefPID_</td>"+
                     "<td> = </td><td>"+bDefPID+"</td><td><font color='blue'>"+
                     "(REQUIRED)</font></td></tr></font><tr><td>"+
                     "<font color='red'>methodName_</td><td> = </td><td>"+methodName+
                     "</td><td><font color='blue'>(REQUIRED)</font></td></tr>"+
                     "</font><tr><td><font color='red'>AsOfDate_</td>"+
                     "<td> = </td><td>"+versDate+"</td><td>"+
                     "<font color='green'>(OPTIONAL)</font></td></tr></font>"+
                     "<tr><td><font color='red'>clear-cache_</td><td> = </td>"+
                     "<td>"+clearCache+"</td><td><font color='green'>"+
                     "(OPTIONAL)</font></td></tr></font>");
         out.println("<tr></tr><tr><td colspan='5'>Other-Parameters:</td>"+
                     "</tr><tr></tr>");
         for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
         {
           out.println("<tr><td><font color='red'>"+e.nextElement()+
           " </td><td>= </td><td>"+e.nextElement()+"</td></tr>");
         }
         out.println("</table>></font>");
         out.println("</body></html>");
         checkOK = false;
       }
     } else if (action.equals(GET_BEHAVIOR_DEFINITIONS))
     {
       if (PID == null)
       {
         PrintWriter out = response.getWriter();
         response.setContentType(CONTENT_TYPE_HTML);
         out.println("<html>");
         out.println("<head><title>FedoraServlet</title></head>");
         out.println("<body>");
         out.println("<tr><td colspan='5'><font color='red'>Required "+
                     "parameter missing in Behavior Definition Request:"+
                     "</td></tr></font>");
         out.println("<table>"+
                     "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                     action+"</td><td><font color='blue'>(REQUIRED)</font></td>"+
                     "</tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = <td>"+PID+
                     "</td><td><font color='blue'>(REQUIRED)"+
                     "</font></td></tr></font><tr><td><font color='red'>"+
                     "bDefPID_</td><td> = </td><td>"+bDefPID+"</td>"+
                     "<td><font color='green'>(OPTIONAL)</font></td>"+
                     "</tr></font><tr><td><font color='red'>methodName_</td>"+
                     "<td> = </td><td>"+methodName+"</td><td><font color='green'>"+
                     "(OPTIONAL)</font></td></tr></font><tr><td>"+
                     "<font color='red'>AsOfDate_</td><td> = </td><td>"+
                     versDate+"</td><td><font color='green'>(OPTIONAL)"+
                     "</font></td></tr></font><tr><td><font color='red'>"+
                     "clear-cache_</td><td> = </td><td>"+clearCache+
                     "</td><td><font color='green'>(OPTIONAL)"+
                     "</font></td></tr></font>");
         out.println("<tr></tr><tr><td colspan='5'>Other-Parameters:</td>"+
                     "</tr><tr></tr>");
         for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
         {
           out.println("<tr><td><font color='red'>"+e.nextElement()+
           " </td><td>= </td><td>"+e.nextElement()+"</td></tr></font>");
         }
         out.println("</table>></font>");
         out.println("</body></html>");
         checkOK = false;
       }
     } else if (action.equals(GET_BEHAVIOR_METHODS))
     {
       if (PID == null || bDefPID == null)
       {
         PrintWriter out = response.getWriter();
         response.setContentType(CONTENT_TYPE_HTML);
         out.println("<html>");
         out.println("<head><title>FedoraServlet</title></head>");
         out.println("<body>");
         out.println("<br></br><font color='red'>Required parameter "+
                     "missing in Behavior Methods Request:</font>");
         out.println("<table>"+
                     "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                     action+"</td><td><font color='blue'>(REQUIRED)</font></td>"+
                     "</tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = </td><td>"+PID+
                     "</td><td><font color='blue'>(REQUIRED)</font></td></tr>"+
                     "</font><tr><td><font color='red'>bDefPID_</td>"+
                     "<td> = </td><td>"+bDefPID+"</td><td><font color='blue'>"+
                     "(REQUIRED)</font></td></tr></font><tr><td>"+
                     "<font color='red'>methodName_</td><td> = </td><td>"+methodName+
                     "</td><td><font color='green'>(OPTIONAL)</font></td>"+
                     "</tr></font><tr><td><font color='red'>AsOfDate_</td>"+
                     "<td> = </td><td>"+versDate+"</td><td>"+
                     "<font color='green'>(OPTIONAL)</font></td></tr></font>"+
                     "<tr><td><font color='red'>clear-cache_</td><td> = </td>"+
                     "<td>"+clearCache+"</td><td><font color='green'>"+
                     "(OPTIONAL)</font></td></tr></font>");
         out.println("<tr></tr><tr><td colspan='5'>Other-Parameters:</td></tr>"+
                     "<tr></tr>");
         for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
         {
           out.println("<tr><td><font color='red'>"+e.nextElement()+
           " <td>= <td>"+e.nextElement()+"<td><font color='green'>"+
           "(OPTIONAL)</font></tr></font>");
         }
         out.println("</table>></font>");
         out.println("</body></html>");
         checkOK = false;
       }
     } else if (action.equals(VIEW_OBJECT))
     {
       if (PID == null)
       {
         PrintWriter out = response.getWriter();
         response.setContentType(CONTENT_TYPE_HTML);
         out.println("<html>");
         out.println("<head><title>FedoraServlet</title></head>");
         out.println("<body>");
         out.println("<tr><td colspan='5'><font color='red'>Required "+
                     "parameter missing in Behavior Definition Request:"+
                     "</td></tr></font>");
         out.println("<table>"+
                     "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                     action+"</td><td><font color='blue'>(REQUIRED)</font></td>"+
                     "</tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = <td>"+PID+
                     "</td><td><font color='blue'>(REQUIRED)"+
                     "</font></td></tr></font><tr><td><font color='red'>"+
                     "bDefPID_</td><td> = </td><td>"+bDefPID+"</td>"+
                     "<td><font color='green'>(OPTIONAL)</font></td>"+
                     "</tr></font><tr><td><font color='red'>methodName_</td>"+
                     "<td> = </td><td>"+methodName+"</td><td><font color='green'>"+
                     "(OPTIONAL)</font></td></tr></font><tr><td>"+
                     "<font color='red'>AsOfDate_</td><td> = </td><td>"+
                     versDate+"</td><td><font color='green'>(OPTIONAL)"+
                     "</font></td></tr></font><tr><td><font color='red'>"+
                     "clear-cache_</td><td> = </td><td>"+clearCache+
                     "</td><td><font color='green'>(OPTIONAL)"+
                     "</font></td></tr></font>");
         out.println("<tr></tr><tr><td colspan='5'>Other-Parameters:</td>"+
                     "</tr><tr></tr>");
         for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
         {
           out.println("<tr><td><font color='red'>"+e.nextElement()+
           " </td><td>= </td><td>"+e.nextElement()+"</td></tr></font>");
         }
         out.println("</table>></font>");
         out.println("</body></html>");
         checkOK = false;
       }
     } else
     {
       PrintWriter out = response.getWriter();
       response.setContentType(CONTENT_TYPE_HTML);
       out.println("<html>");
       out.println("<head><title>FedoraServlet</title></head>");
       out.println("<body>");
       out.println("<br><font color='red'>Invalid action parameter"+
                  " specified in Servlet Request:action= "+action+"<br></br>");
       out.println("<br></br><font color='blue'>Reserved parameters "+
           "in Request:</td></tr></font>");
       out.println("<table>"+
                   "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                    action+"</td></tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = <td>"+PID+
                     "</td></tr></font><tr><td><font color='red'>bDefPID_</td>"+
                     "<td> = </td><td>"+bDefPID+"</td></tr></font>"+
                     "<tr><td><font color='red'>methodName_</td><td> = </td>"+
                     "<td>"+methodName+"</td></tr></font><tr><td><font color='red'>"+
                     "AsOfDate_</td><td> = </td><td>"+versDate+"</td></tr>"+
                     "</font><tr><td><font color='red'>clear-cache_</td>"+
                     "<td> = </td><td>"+clearCache+"</td></tr></font>");
       out.println("<tr></tr><tr><td colspan='5'><font color='blue'>"+
                   "Other-Parameters:</font></td></tr><tr></tr>");
       for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
       {
         out.println("<tr><td><font color='red'>"+e.nextElement()+" </td>"+
         "<td>= </td><td>"+e.nextElement()+"</td></tr></font>");
       }
       out.println("</table></font>");
       out.println("</body></html>");
       checkOK = false;
     }
 
     if (debug)
     {
       System.out.println("PID: "+PID+"bDEF: "+bDefPID+"methodName: "+methodName+
                          "action: "+action);
       for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
       {
         System.out.println("<p>userParm: "+e.nextElement()+
         " userValue: "+e.nextElement());
       }
     }
 
     return(checkOK);
   }
 
   /**
    * Method to check dissemination cache. The method retrieves the dissemination
    * result from the cache if possible; otherwise, it initiates a new call
    * to GetDissemination.
    *
    * @param dissRequestID - originating URI request used as hash key
    * @param PID - persistent identifier of the Digital Object
    * @param bDefPID - persistent identifier of the Behavior Definition object
    * @param methodName - method name
    * @param userParms - user-supplied method parameters
    * @param asOfDate - version datetime stamp of the digital object
    * @return - MIMETypedStream containing dissemination result
    */
   private synchronized MIMETypedStream checkCache(String dissRequestID,
       String action, String PID, String bDefPID, String methodName,
       Vector userParms, Calendar asOfDate, String clearCache,
       HttpServletResponse response) throws IOException
   {
     // Clear cache if size gets larger than DISS_CACHE_SIZE
     if (disseminationCache.size() > DISS_CACHE_SIZE ||
         clearCache.equalsIgnoreCase(YES))
     {
       clearDisseminationCache();
     }
     MIMETypedStream disseminationResult = null;
     // See if dissemination request is in local cache
     disseminationResult =
         (MIMETypedStream)disseminationCache.get(dissRequestID);
     if (disseminationResult == null)
     {
       // Dissemination request NOT in local cache.
       // Try reading from database
       disseminationResult = GetDissemination(PID, bDefPID, methodName,
           userParms, asOfDate);
       if (disseminationResult != null)
       {
         // Dissemination request succeeded, so add to local cache
         disseminationCache.put(dissRequestID, disseminationResult);
          if (debug) System.out.println("ADDED to CACHE: "+dissRequestID);
       } else
       {
         // Dissemination request failed
         emptyResult(action, PID, bDefPID, methodName, asOfDate, userParms,
                     clearCache, response);
         System.out.println("Dissemination Result: NULL");
         this.getServletContext().log("Dissemination Result: NULL");
       }
       if (debug) System.out.println("CACHE SIZE: "+disseminationCache.size());
     }
 
     return disseminationResult;
 
   }
 
   /**
    * Instantiates a new dissemination cache.
    */
   private synchronized void clearDisseminationCache() {
     disseminationCache = new Hashtable();
   }
 
   /**
    * Cleans up servlet resources
    */
   public void destroy()
   {
 
   }
 }
