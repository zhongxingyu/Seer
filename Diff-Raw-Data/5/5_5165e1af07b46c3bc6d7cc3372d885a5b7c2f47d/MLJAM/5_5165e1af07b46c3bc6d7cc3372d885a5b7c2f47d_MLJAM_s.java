 /*
  * Mark Logic Interface to Java
  *
  * Copyright 2006 Jason Hunter
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * @author Jason Hunter
  * @version 1.0
  *
  */
 package com.xqdev.jam;
 
 import java.io.*;
 import java.util.*;
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.xml.datatype.*;
 import javax.xml.namespace.QName;
 
 import bsh.*;
 import org.apache.commons.codec.binary.*;
 import org.apache.commons.codec.DecoderException;
 
 /**
  * TODO: Decide if we want to alter the jam:start() behavior so
  *       it connects to the server as a test.  This has the benefit
  *       that any server restart will HUP a client halfway through
  *       something because the context will disappear rather than be
  *       implicitly recreated to an empty state.
  * TODO: Consider adding a feature so jam:start() can provide a
  *       user-specified stale-out duration for the context.
  */
 public class MLJAM extends HttpServlet {
 
   private static HashMap<String, Interpreter> interpreters =
           new HashMap<String, Interpreter>();
 
   // Need to retire interpreters after some period of inactivity.
   // Temporary interpreters (those w/ client assigned names) expire faster
   private static long lastClean = System.currentTimeMillis();
   private static long CLEAN_INTERVAL = 5 * 60 * 1000;        // five minutes
   private static long STALE_TIMEOUT = 60 * 60 * 1000;        // one hour
   private static long TEMP_STALE_TIMEOUT = 60 * 60 * 1000;   // ten minutes
 
   private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ");
 
   /*
   public void init(ServletConfig config) throws ServletException {
     super.init(config);
   }
   */
 
   private static Interpreter getInterpreter(String contextId) throws EvalError {
     // Get the appropriate interpreter
     Interpreter i = null;
     boolean createdInterp = false;
     synchronized (interpreters) {  // serialize two gets of the same name
       i = interpreters.get(contextId);
       if (i == null) {
         i = new Interpreter();
         interpreters.put(contextId, i);
         createdInterp = true;
       }
     }
     if (createdInterp) {
       Log.log("Created context: " + contextId + " (" + i + ")");
 
       // Now configure stdin and stdout to capture 10k of content
       // Store references to the circular buffers within the interpreter itself.
       // This provides a nice place to store them plus theoretically allows
       // advanced use from within the bsh environment.
       // On Windows print() outputs \r\n but in XQuery that's normalized to \n
       // so the 10k of Java buffer may produce less than 10k of content in XQuery!
       OutputStream circularOutput = new CircularByteArrayOutputStream(10240);
       PrintStream printOutput = new PrintStream(circularOutput);
       i.setOut(printOutput);
       i.set("mljamout", circularOutput);
 
       OutputStream circularError = new CircularByteArrayOutputStream(10240);
       PrintStream printError = new PrintStream(circularError);
       i.setErr(printError);
       i.set("mljamerr", circularError);
 
       // Capture the built-in System.out and System.err also.
       // (Commented out since System appears global, can't do per interpreter.)
       //i.set("mljamprintout", printOutput);
       //i.set("mljamprinterr", printError);
       //i.eval("System.setOut(mljamprintout);");
       //i.eval("System.setErr(mljamprinterr);");
 
       // Need to expose hexdecode() and base64decode() built-in functions
       i.eval("hexdecode(String s) { return com.xqdev.jam.MLJAM.hexDecode(s); }");
       i.eval("base64decode(String s) { return com.xqdev.jam.MLJAM.base64Decode(s); }");
 
       // Let's tell the context what its id is
       i.set("mljamid", contextId);
     }
 
     // Update the last accessed time, used for cleaning
     i.set("mljamlast", System.currentTimeMillis());
 
     // If it's been long enough, go snooping for stale contexts
     if (System.currentTimeMillis() > lastClean + CLEAN_INTERVAL) {
       Log.log("Initiated periodic scan for stale context objects");
       lastClean = System.currentTimeMillis();
       Iterator<Interpreter> itr = interpreters.values().iterator();
       while (itr.hasNext()) {
         Interpreter interp = itr.next();
         Long last = (Long) interp.get("mljamlast");
         if (System.currentTimeMillis() > last + STALE_TIMEOUT) {
           itr.remove();
           Log.log("Staled context: " + interp.get("mljamid") + " (" + interp + ")");
         }
         else if ((System.currentTimeMillis() > last + TEMP_STALE_TIMEOUT) &&
                  ("" + interp.get("mljamid")).startsWith("temp:")) {
           itr.remove();
           Log.log("Staled temp context: " + interp.get("mljamid") + " (" + interp + ")");
         }
       }
     }
 
     return i;
   }
 
   private static void endInterpreter(String contextId) throws EvalError {
     Interpreter i = interpreters.get(contextId);
     if (i == null) return;
     i.eval("clear();");  // can't hurt to tell bsh to clean up internally
     interpreters.remove(contextId);  // now wait for GC
     Log.log("Destroyed context: " + contextId + " (" + i + ")");
   }
 
   private static String getBody(HttpServletRequest req) {
     try {
       // Try reading the post body using characters.
       // This might throw an exception if something on the
       // server side already called getInputStream().
       // In that case we'll pull as bytes.
       Reader reader = null;
       try {
         reader = new BufferedReader(req.getReader());
       }
       catch (IOException e) {
         reader = new BufferedReader(new InputStreamReader(req.getInputStream(), "UTF-8"));
       }
 
       StringBuffer sbuf = new StringBuffer();
       char[] cbuf = new char[4096];
       int count = 0;
       while ((count = reader.read(cbuf)) != -1) {
         sbuf.append(cbuf, 0, count);
       }
       return sbuf.toString();
     }
     catch (IOException e2) {
       throw new ServerProblemException("IOException in reading POST body: " + e2.getMessage());
     }
   }
 
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
     doPost(req, res);
   }
 
   protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
     try {
       // A good request looks like /mljam/contextid/verb?name=varname
       // The extra path info includes the context id and verb
       String extra = req.getPathInfo();       // "/contextid/verb"
       if (extra == null || extra.equals("")) {
         throw new ClientProblemException("Request requires a context id and verb in its extra path info");
       }
       String[] parts = extra.split("/");      // { "", "contextid", "verb" }
       if (parts.length < 2) {
         throw new ClientProblemException("Request requires a context id and verb in its extra path info");
       }
       else if (parts.length < 3) {
         throw new ClientProblemException("Request requires a verb in its extra path info");
       }
 
       String contextId = parts[1];
       String verb = parts[2];
       String method = req.getMethod();
 
       if (method.equalsIgnoreCase("get")) {
 
         // We have three GET verbs: get, get-stdout, get-stderr.
         // These are all idempotent, while the POST verbs aren't.  The get
         // verb accept a "name" query string parameter.  The get verb returns
         // either XQuery to evaluate (indicated by x-marklogic/xquery content type)
         // or a raw binary (indicated by an application/binary-encoded content type).
 
         if (verb.equalsIgnoreCase("get")) {
           String name = req.getParameter("name");
           if (name == null || name.equals("")) {
             throw new ClientProblemException("The get verb requires a name parameter");
           }
           Interpreter i = getInterpreter(contextId);
           Object o = i.get(name);
           if (o instanceof byte[]) {
             sendBinaryResponse(res, (byte[]) o);
           }
           else if (o instanceof String) {
             sendStringResponse(res, (String) o);
           }
           else {
             sendXQueryResponse(res, o);
           }
         }
 
         else if (verb.equalsIgnoreCase("get-stdout")) {
           Interpreter i = getInterpreter(contextId);
           i.getOut().flush();
           CircularByteArrayOutputStream circ = (CircularByteArrayOutputStream) i.get("mljamout");
           if (circ != null) {
             sendStringResponse(res, circ.toString());
             circ.reset();
           }
           else {
             throw new ServerProblemException("Could not fetch mljamout from interpreter context");
           }
         }
         else if (verb.equalsIgnoreCase("get-stderr")) {
           Interpreter i = getInterpreter(contextId);
           i.getErr().flush();
           CircularByteArrayOutputStream circ = (CircularByteArrayOutputStream) i.get("mljamerr");
           if (circ != null) {
             sendStringResponse(res, circ.toString());
             circ.reset();
           }
           else {
             throw new ServerProblemException("Could not fetch mljamerr from interpreter context");
           }
         }
 
         else {
           throw new ClientProblemException("Unrecognized GET verb: " + verb);
         }
       }
 
       else if (method.equalsIgnoreCase("post")) {
         // We have six POST verbs: eval, unset, end, source, set-string, and set-binary.
         // These are POST verbs because they aren't idempotent.
         // The set-string, set-binary, unset, and source verbs accept a "name"
         // query string parameter.  The set-string and set-binary verbs accept
         // a value in their post body.  The eval verb accepts code in its post body.
 
         if (verb.equalsIgnoreCase("set-string")) {
           String name = req.getParameter("name");
           if (name == null || name.equals("")) {
             throw new ClientProblemException("The set-string verb requires a name parameter");
           }
           String body = getBody(req);  // a value of "" is legit
           Interpreter i = getInterpreter(contextId);
           i.unset(name);
           i.set(name, body);
           sendNoResponse(res);
         }
 
         else if (verb.equalsIgnoreCase("set-binary")) {
           String name = req.getParameter("name");
           if (name == null || name.equals("")) {
             throw new ClientProblemException("The set-binary verb requires a name parameter");
           }
           String body = getBody(req);  // a value of "" is legit
           byte[] bodyBytes = hexDecode(body);  // later could do this streaming for speed
           Interpreter i = getInterpreter(contextId);
           i.unset(name);
           i.set(name, bodyBytes);
           sendNoResponse(res);
         }
 
         else if (verb.equalsIgnoreCase("eval")) {
           String body = getBody(req);
           if (body == null || body.equals("")) {
             throw new ClientProblemException("The eval verb requires a post body containing code to eval");
           }
           Interpreter i = getInterpreter(contextId);
           i.eval(body);
           sendNoResponse(res);
         }
 
         else if (verb.equalsIgnoreCase("eval-get")) {
           String body = getBody(req);
           if (body == null || body.equals("")) {
             throw new ClientProblemException("The eval-get verb requires a post body containing code to eval");
           }
           Interpreter i = getInterpreter(contextId);
           Object o = i.eval(body);
           if (o instanceof byte[]) {
             sendBinaryResponse(res, (byte[]) o);
           }
           else if (o instanceof String) {
             sendStringResponse(res, (String) o);
           }
           else {
             sendXQueryResponse(res, o);
           }
         }
 
         else if (verb.equalsIgnoreCase("unset")) {
           String name = req.getParameter("name");
           if (name == null || name.equals("")) {
             throw new ClientProblemException("The unset verb requires a name parameter");
           }
           Interpreter i = getInterpreter(contextId);
           i.unset(name);
           sendNoResponse(res);
         }
 
         else if (verb.equalsIgnoreCase("end")) {
           endInterpreter(contextId);
           sendNoResponse(res);
         }
 
         else if (verb.equalsIgnoreCase("source")) {
           String name = req.getParameter("name");
           if (name == null || name.equals("")) {
             throw new ClientProblemException("The source verb requires a name parameter");
           }
           Interpreter i = getInterpreter(contextId);
           i.source(name);
           sendNoResponse(res);
         }
 
         else {
           throw new ClientProblemException("Unrecognized POST verb: " + verb);
         }
       }
     }
     catch (TargetError e) {
       Throwable target = e.getTarget();
       Log.log(e);
       Log.log("Target: " + target);
       sendServerProblemResponse(res,
                                 target.getClass().getName() +
                                 ": " + target.getMessage() +
                                 " when executing Java code: " + e.getErrorText()
       );  // include full trace?
     }
     catch (EvalError e) {
       Log.log(e);
       sendServerProblemResponse(res, e.getClass().getName() + ": " + e.getMessage());  // include full trace?
     }
     catch (ClientProblemException e) {
       Log.log(e);
       sendClientProblemResponse(res, e.getMessage());
     }
     catch (ServerProblemException e) {
       Log.log(e);
       sendServerProblemResponse(res, e.getMessage());
     }
   }
 
   private static String escapeSingleQuotes(String s) {
     return s.replaceAll("'", "''");
   }
 
   public static byte[] hexDecode(String s) {
     try {
       return Hex.decodeHex(s.toCharArray());
     }
     catch (DecoderException e) {
       throw new ClientProblemException("Hex content is not valid hex: " + e.getMessage() + ": " + s);
     }
   }
 
   private static String hexEncode(byte[] bytes) {
     return new String(Hex.encodeHex(bytes));
   }
 
   public static byte[] base64Decode(String s) {
     try {
       return Base64.decodeBase64(s.getBytes("ISO-8859-1"));
     }
     catch (UnsupportedEncodingException e) {
       throw new ServerProblemException("Server does not recognize ISO-8859-1 encoding");
     }
   }
 
   private static void sendClientProblemResponse(HttpServletResponse res, String s) throws IOException {
     // Commenting out the status code because we want the client to eval the error() call
     //res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
     if (s != null && s.length() > 4096) {  // Cap super long errors
       s = s.substring(0, 2048) + " ...[trimmed]... " + s.substring(s.length() - 2048);
     }
     res.setContentType("x-marklogic/xquery; charset=UTF-8");
     Writer writer = res.getWriter();
     writer.write("error('" + escapeSingleQuotes(s) + "')");
     writer.flush();
   }
 
   private static void sendServerProblemResponse(HttpServletResponse res, String s) throws IOException {
     // Commenting out the status code because we want the client to eval the error() call
     //res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     res.setContentType("x-marklogic/xquery; charset=UTF-8");
     if (s != null && s.length() > 4096) {  // Cap super long errors
       s = s.substring(0, 2048) + " ...[trimmed]... " + s.substring(s.length() - 2048);
     }
     Writer writer = res.getWriter();
     writer.write("error('" + escapeSingleQuotes(s) + "')");
     writer.flush();
   }
 
   private static void sendNoResponse(HttpServletResponse res) {
     res.setStatus(HttpServletResponse.SC_NO_CONTENT);
   }
 
   private static void sendBinaryResponse(HttpServletResponse res, byte[] bytes) throws IOException {
     res.setContentType("application/binary-encoded");
     OutputStream out = res.getOutputStream();  // care to handle errors later?
     out.write(bytes);
     out.flush();
   }
 
   private static void sendStringResponse(HttpServletResponse res, String s) throws IOException {
     res.setContentType("text/plain; charset=UTF-8");
     Writer w = res.getWriter();
     w.write(s);
     w.flush();
   }
 
   private static void sendXQueryResponse(HttpServletResponse res, Object o) throws IOException {
     // Make sure to leave the status code alone.  It defaults to 200, but sometimes
     // callers of this method will have set it to a custom code.
     res.setContentType("x-marklogic/xquery; charset=UTF-8");
     //res.setContentType("text/plain");
     Writer writer = res.getWriter();  // care to handle errors later?
 
     if (o == null) {
       writer.write("()");
     }
 
     else if (o instanceof byte[]) {
       writer.write("binary {'");
       writer.write(hexEncode((byte[]) o));
       writer.write("'}");
     }
 
     else if (o instanceof Object[]) {
       Object[] arr = (Object[]) o;
       writer.write("(");
       for (int i = 0; i < arr.length; i++) {
         sendXQueryResponse(res, arr[i]);
         if (i + 1 < arr.length) writer.write(", ");
       }
       writer.write(")");
     }
 
     else if (o instanceof String) {
       writer.write("'");
       writer.write(escapeSingleQuotes(o.toString()));
       writer.write("'");
     }
     else if (o instanceof Integer) {
       writer.write("xs:int(");
       writer.write(o.toString());
       writer.write(")");
     }
     else if (o instanceof Long) {
       writer.write("xs:integer(");
       writer.write(o.toString());
       writer.write(")");
     }
     else if (o instanceof Float) {
       Float flt = (Float) o;
       writer.write("xs:float(");
       if (flt.equals(Float.POSITIVE_INFINITY)) {
         writer.write("'INF'");
       }
       else if (flt.equals(Float.NEGATIVE_INFINITY)) {
         writer.write("'-INF'");
       }
       else if (flt.equals(Float.NaN)) {
         writer.write("fn:number(())");  // poor man's way to write NaN
       }
       else {
         writer.write(o.toString());
       }
       writer.write(")");
     }
     else if (o instanceof Double) {
       Double dbl = (Double) o;
       writer.write("xs:double(");
       if (dbl.equals(Double.POSITIVE_INFINITY)) {
         writer.write("'INF'");
       }
       else if (dbl.equals(Double.NEGATIVE_INFINITY)) {
         writer.write("'-INF'");
       }
       else if (dbl.equals(Double.NaN)) {
         writer.write("fn:number(())");  // poor man's way to write NaN
       }
       else {
         writer.write(o.toString());
       }
       writer.write(")");
     }
     else if (o instanceof Boolean) {
       writer.write("xs:boolean('");
       writer.write(o.toString());
       writer.write("')");
     }
     else if (o instanceof BigDecimal) {
       writer.write("xs:decimal(");
       writer.write(o.toString());
       writer.write(")");
     }
     else if (o instanceof Date) {
       // We want something like: 2006-04-30T01:28:30.499-07:00
       // We format to get:       2006-04-30T01:28:30.499-0700
       // Then we add in the colon
       writer.write("xs:dateTime('");
       String d = dateFormat.format((Date) o);
       writer.write(d.substring(0, d.length() - 2));
       writer.write(":");
       writer.write(d.substring(d.length() - 2));
       writer.write("')");
     }
     else if (o instanceof XMLGregorianCalendar) {
       XMLGregorianCalendar greg = (XMLGregorianCalendar) o;
       QName type = greg.getXMLSchemaType();
       if (type.equals(DatatypeConstants.DATETIME)) {
         writer.write("xs:dateTime('");
       }
       else if (type.equals(DatatypeConstants.DATE)) {
         writer.write("xs:date('");
       }
       else if (type.equals(DatatypeConstants.TIME)) {
         writer.write("xs:time('");
       }
       else if (type.equals(DatatypeConstants.GYEARMONTH)) {
         writer.write("xs:gYearMonth('");
       }
       else if (type.equals(DatatypeConstants.GMONTHDAY)) {
         writer.write("xs:gMonthDay('");
       }
       else if (type.equals(DatatypeConstants.GYEAR)) {
         writer.write("xs:gYear('");
       }
       else if (type.equals(DatatypeConstants.GMONTH)) {
         writer.write("xs:gMonth('");
       }
       else if (type.equals(DatatypeConstants.GDAY)) {
         writer.write("xs:gDay('");
       }
       writer.write(greg.toXMLFormat());
       writer.write("')");
     }
     else if (o instanceof Duration) {
       Duration dur = (Duration) o;
       /*
       // The following fails on Xerces
       QName type = dur.getXMLSchemaType();
       if (type.equals(DatatypeConstants.DURATION)) {
         writer.write("xs:duration('");
       }
       else if (type.equals(DatatypeConstants.DURATION_DAYTIME)) {
         writer.write("xdt:dayTimeDuration('");
       }
       else if (type.equals(DatatypeConstants.DURATION_YEARMONTH)) {
         writer.write("xdt:yearMonthDuration('");
       }
       */
       // If no years or months, must be DURATION_DAYTIME
       if (dur.getYears() == 0 && dur.getMonths() == 0) {
         writer.write("xdt:dayTimeDuration('");
       }
       // If has years or months but nothing else, must be DURATION_YEARMONTH
       else if (dur.getDays() == 0 && dur.getHours() == 0 && dur.getMinutes() == 0 && dur.getSeconds() == 0) {
         writer.write("xdt:yearMonthDuration('");
       }
       else {
         writer.write("xs:duration('");
       }
       writer.write(dur.toString());
       writer.write("')");
     }
 
     else if (o instanceof org.jdom.Element) {
       org.jdom.Element elt = (org.jdom.Element) o;
       writer.write("xdmp:unquote('");
       // Because "&lt;" in XQuery is the same as "<" I need to double escape any ampersands
      writer.write(new org.jdom.output.XMLOutputter().outputString(elt).replaceAll("&", "&amp;"));
       writer.write("')/*");  // make sure to return the root elt
     }
     else if (o instanceof org.jdom.Document) {
       org.jdom.Document doc = (org.jdom.Document) o;
       writer.write("xdmp:unquote('");
      writer.write(new org.jdom.output.XMLOutputter().outputString(doc).replaceAll("&", "&amp;"));
       writer.write("')");
     }
     else if (o instanceof org.jdom.Text) {
       org.jdom.Text text = (org.jdom.Text) o;
       writer.write("text {'");
       writer.write(escapeSingleQuotes(text.getText()));
       writer.write("'}");
     }
     else if (o instanceof org.jdom.Attribute) {
       // <fake xmlns:pref="http://uri.com" pref:attrname="attrvalue"/>/@*:attrname
       // <fake xmlns="http://uri.com" attrname="attrvalue"/>/@*:attrname
       org.jdom.Attribute attr = (org.jdom.Attribute) o;
       writer.write("<fake xmlns");
       if ("".equals(attr.getNamespacePrefix())) {
         writer.write("=\"");
       }
       else {
         writer.write(":" + attr.getNamespacePrefix() + "=\"");
       }
       writer.write(attr.getNamespaceURI());
       writer.write("\" ");
       writer.write(attr.getQualifiedName());
       writer.write("=\"");
       writer.write(escapeSingleQuotes(attr.getValue()));
       writer.write("\"/>/@*:");
       writer.write(attr.getName());
     }
     else if (o instanceof org.jdom.Comment) {
       org.jdom.Comment com = (org.jdom.Comment) o;
       writer.write("comment {'");
       writer.write(escapeSingleQuotes(com.getText()));
       writer.write("'}");
     }
     else if (o instanceof org.jdom.ProcessingInstruction) {
       org.jdom.ProcessingInstruction pi = (org.jdom.ProcessingInstruction) o;
       writer.write("processing-instruction ");
       writer.write(pi.getTarget());
       writer.write(" {'");
       writer.write(escapeSingleQuotes(pi.getData()));
       writer.write("'}");
     }
 
     else if (o instanceof QName) {
       QName q = (QName) o;
       writer.write("fn:expanded-QName('");
       writer.write(escapeSingleQuotes(q.getNamespaceURI()));
       writer.write("','");
       writer.write(q.getLocalPart());
       writer.write("')");
     }
 
     else {
       writer.write("error('XQuery tried to retrieve unsupported type: " + o.getClass().getName() + "')");
     }
 
     writer.flush();
   }
 }
 
 
 
 
 class CircularByteArrayOutputStream extends OutputStream {
   protected byte buf[];
   protected int position = 0;
   protected boolean wrapped = false;
 
   public CircularByteArrayOutputStream(int maxSize) {
     buf = new byte[maxSize];
   }
 
   public synchronized void write(int b) {
     buf[position] = (byte) b;
     position++;
     if (position == buf.length) {
       position = 0;
       wrapped = true;
     }
   }
 
   public byte[] toByteArray() {
     byte[] newBuf = null;
     if (!wrapped) {
       // We haven't wrapped yet so copy up to the current position
       newBuf = new byte[position];
       System.arraycopy(buf, 0, newBuf, 0, position);
     }
     else {
       // We'll have wrapped, so it's a two step process.
       // Copy everything following the position, then everything before
       newBuf = new byte[buf.length];
       // "position" is where we'd write next so it's the oldest byte.
       // So copy starting there for as many as are between there and the end.
       System.arraycopy(buf, position, newBuf, 0, (buf.length - position));
       // Now copy starting at 0, placing into the end of the last copy, up to position.
       System.arraycopy(buf, 0, newBuf, (buf.length - position), position);
     }
     return newBuf;
   }
 
   public String toString() {
     // We don't specify a charset since we assume the
     // console received bytes in the default platform encoding.
     return new String(toByteArray());
   }
 
   public void reset() {
     // Fetching resets the circular buffer
     position = 0;
     wrapped = false;
   }
 }
 
 class ClientProblemException extends RuntimeException {
   ClientProblemException(String s) { super(s); }
 }
 
 class ServerProblemException extends RuntimeException {
   ServerProblemException(String s) { super(s); }
 }
