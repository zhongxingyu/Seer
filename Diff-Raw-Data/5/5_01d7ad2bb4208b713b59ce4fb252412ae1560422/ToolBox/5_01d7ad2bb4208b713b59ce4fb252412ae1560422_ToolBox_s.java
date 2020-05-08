 package be.libis.digitool;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import org.apache.axis.client.Call;
 import org.apache.axis.client.Service;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 /**
  *
  * @author kris
  */
 public class ToolBox {
 
     private static final String soapURL = "http://aleph08.libis.kuleuven.be:1801/de_repository_web/services/";
     private static final String generalFile = "/xml/general.xml";
     private static final String findPid1Xml = "/xml/findPid1.xml";
     private static final String findPid2Xml = "/xml/findPid2.xml";
     public static final String digitalEntityCallXml = "/xml/digitalEntityCall.xml";
     public static final String pidDigitalEntityXml = "/xml/pidDigitalEntity.xml";
     public static final String newDigitalEntityXml = "/xml/newDigitalEntity.xml";
     public static final String copyDigitalEntityXsl = "/xsl/copyDigitalEntity.xsl";
     public static final String deleteManifestationsXsl = "/xsl/deleteManifestations.xsl";
     public static final String updateInfoXsl = "/xsl/updateInfo.xsl";
     public int max_result = 100;
     private String active_library = "LIA01";
     private String active_user = "lia01";
     private String user_name = "super";
     private String password = "super";
 
     public void setActiveLibrary(String active_library) {
         this.active_library = active_library;
     }
 
     public void setActiveUser(String active_user) {
         this.active_user = active_user;
     }
 
     public void setUserName(String user_name) {
         this.user_name = user_name;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
     private Logger logger = Logger.getLogger(ToolBox.class.getName());
 
     public void setLogger(Logger logger) {
         this.logger = logger;
     }
 
     public Logger getLogger() {
         return this.logger;
     }
 
     public String getUpdateInfoStylesheet() {
         try {
             return readFile(updateInfoXsl);
         } catch (IOException ex) {
             printExceptionInfo(ex);
         }
         return "";
     }
 
     public List<String> getManifestationPids(String pid, String usage_type) {
         List<String> pids = new ArrayList<String>();
 
         String reply = sendGetRequest(
                 "http://aleph08.libis.kuleuven.be:8881/dtl-cgi/get-pid.pl",
                 "pid=" + pid + "&usagetype=" + usage_type.toUpperCase());
 
         Document resultDoc = parseString(reply);
 
         NodeList resultNodes = resultDoc.getElementsByTagName("target_pid");
 
         for (int i = 0; i < resultNodes.getLength(); ++i) {
             Node pidNode = resultNodes.item(i);
             String pid_found = pidNode.getTextContent();
             if (pid_found == null || pid_found.equals("")) {
                 continue;
             }
             logger.finest("Found pid " + pid_found + " as manifestation "
                     + usage_type + " of pid " + pid);
             pids.add(pid_found);
         }
 
         return pids;
     }
 
     public String[] getPid(String label) {
 
         logger.fine("Searching for PIDs with label='" + label + "'");
 
         boolean success = false;
 
         String[] pid = new String[]{};
 
         try {
 
             String searchString = readFile(findPid1Xml);
             String query = searchString.replaceAll("%max_result%", new Integer(max_result).toString()).replaceAll("%label%", xmlify(label));
 
             String reply = DE_Search(query);
 
             if (!checkReplyForError(reply)) {
                 pid = parseReply(reply, "pid");
                 success = true;
             }
 
         } catch (Exception e) {
             printExceptionInfo(e);
         }
 
         if (success && pid.length > 0) {
             String message = "Found PIDs:";
             for (int i = 0; i < pid.length; i++) {
                 message += " '" + pid[i] + "'";
             }
             logger.fine(message);
         } else {
             logger.severe("No PIDs found with label='" + label + "'");
             return null;
         }
 
         return pid;
     }
 
     public String[] getPid(String label, String usagetype) {
 
         logger.fine("Searching for PIDs with label='" + label
                 + "' and usage_type='" + usagetype + "'");
 
         boolean success = false;
 
         String[] pid = new String[]{};
 
         try {
 
             String searchString = readFile(findPid2Xml);
             String usageQuery = usagetype.equalsIgnoreCase("VIEW")
                     ? usagetype + " not VIEW_MAIN"
                     : usagetype;
 
             String query = searchString.replaceAll("%max_result%", new Integer(max_result).toString()).replaceAll("%label%", xmlify(label)).replaceAll("%usagetype%", xmlify(usageQuery));
 
             String reply = DE_Search(query);
 
             if (!checkReplyForError(reply)) {
                 pid = parseReply(reply, "pid");
                 success = true;
             }
 
         } catch (Exception e) {
             printExceptionInfo(e);
         }
 
         if (success && pid.length > 0) {
             String message = "Found PIDs:";
             for (int i = 0; i < pid.length; i++) {
                 message += " '" + pid[i] + "'";
             }
             logger.fine(message);
         } else {
             logger.severe("No PIDs found with label='" + label
                     + "' and usage_type='" + usagetype + "'");
             return null;
         }
 
         return pid;
     }
 
     public String retrieveObject(String pid) {
 
         logger.fine("Retrieving object info for PID: '" + pid + "'");
 
         boolean success = false;
 
         String result = null;
 
         try {
 
             String reply = DE_Call(
                     readFile(pidDigitalEntityXml).replaceAll("%pid%", xmlify(pid)),
                     "retrieve");
 
             if (!checkReplyForError(reply)) {
                 result = parseReply(reply, "xb:digital_entity")[0];
                 result = addNamespace(result);
 
                 success = true;
 
             }
 
         } catch (Exception e) {
             printExceptionInfo(e);
         }
 
         if (success) {
             logger.fine("Retrieved object info");
         } else {
             logger.severe("Failed to retrieve object info for PID: '" + pid + "'");
         }
 
         return result;
     }
 
     static public class DENewParameters {
 
         public String label;
         public final String usage_type;
         public final String file_path;
         public String file_location = "nfs";
         public String file_operation = "copy";
         public String relation_type = "manifestation";
         public final String relation_pid;
         public String extra_control = "";
         public String metadata = "";
 
         public DENewParameters(String file_path, String usage_type, String relation_pid) {
             this.file_path = file_path;
             this.usage_type = usage_type;
             this.relation_pid = relation_pid;
             this.label = fileToLabel(file_path);
         }
     }
 
     public String addDigitalEntity(DENewParameters params) {
 
         logger.fine("Creating new object");
 
         boolean success = false;
 
         String new_pid = null;
 
         try {
 
             String newDigitalEntity = readFile(newDigitalEntityXml).replaceAll("%label%", params.label).replaceAll("%usage_type%", params.usage_type).replaceAll("%file_path%", params.file_path).replaceAll("%file_location%", params.file_location).replaceAll("%file_operation%", params.file_operation).replaceAll("%relation_type%", params.relation_type).replaceAll("%relation_with%", params.relation_pid).replaceAll("%extra_control%", params.extra_control).replaceAll("%metadata%", params.metadata);
 
             String reply = DE_Call(newDigitalEntity, "create");
 
             if (!checkReplyForError(reply)) {
                 String[] new_pids = parseReply(reply, "pid");
                 if (new_pids.length > 0) {
                     new_pid = new_pids[0];
                     success = true;
                 }
             }
 
         } catch (Exception e) {
             printExceptionInfo(e);
         }
 
         if (success) {
             logger.fine("New object created: " + new_pid);
         } else {
             logger.severe("Could not create new object for stream '"
                     + params.file_path + "'");
         }
 
         return new_pid;
     }
 
     public boolean deleteDigitalEntity(String pid) {
 
         logger.fine("Deleting object with PID: '" + pid + "'");
 
         boolean success = false;
 
         do {
 
             try {
 
                 // digital entities that still have manifestation relations cannot be deleted
 
                 String digitalEntity = retrieveObject(pid);
 
                 if (digitalEntity == null) {
                     break;
                 }
 
                 TransformationParameter[] trans_params = new TransformationParameter[]{};
                 digitalEntity = transformXml(digitalEntity, deleteManifestationsXsl, trans_params);
 
                 if (digitalEntity == null) {
                     break;
                 }
 
                 String reply = DE_Call(digitalEntity, "update");
 
                 if (checkReplyForError(reply)) {
                     break;
                 }
 
                 // now we can delete the object
 
                 digitalEntity = readFile(pidDigitalEntityXml).replaceAll("%pid%", pid);
 
                 reply = DE_Call(digitalEntity, "delete");
 
                 if (checkReplyForError(reply)) {
                     break;
                 }
 
                 success = true;
 
             } catch (Exception e) {
                 printExceptionInfo(e);
             }
 
         } while (false);
 
         if (success) {
             logger.fine("Object '" + pid + "' deleted.");
         } else {
             logger.severe("Could not delete object with PID '" + pid + "'");
         }
 
         return success;
     }
 
     static public class DECopyParameters {
 
         public final String from_pid;
         public final String to_pid;
         public String usage_type = null;
         public Boolean copyControl = false;
         public Boolean copyMetadata = false;
         public Boolean copyRelations = false;
 
         public DECopyParameters(String from_pid, String to_pid) {
             this.from_pid = from_pid;
             this.to_pid = to_pid;
         }
     }
 
     public boolean copyDigitalEntityInfo(DECopyParameters params) {
 
         logger.fine("Copying object info (control, description, "
                 + "access rights, and relations from old object");
 
         boolean success = false;
 
         do {
 
             try {
 
 
                 String oldDigitalEntity = retrieveObject(params.from_pid);
 
                 if (oldDigitalEntity == null) {
                     break;
                 }
 
                 ArrayList<TransformationParameter> tparams =
                         new ArrayList<TransformationParameter>(5);
 
                 tparams.add(new TransformationParameter("pid", params.to_pid));
                 if (params.usage_type != null) {
                     tparams.add(new TransformationParameter("usage", params.usage_type));
                 }
                 tparams.add(new TransformationParameter(
                         "copyControl", new Boolean(params.copyControl)));
                 tparams.add(new TransformationParameter(
                         "copyMetadata", new Boolean(params.copyMetadata)));
                 tparams.add(new TransformationParameter(
                         "copyRelations", new Boolean(params.copyRelations)));
 
                 String newDigitalEntity = transformXml(
                         oldDigitalEntity,
                         copyDigitalEntityXsl,
                         tparams.toArray(new TransformationParameter[]{}));
 
                 if (newDigitalEntity == null) {
                     break;
                 }
 
                 newDigitalEntity = removeNamespace(newDigitalEntity);
 
                 String reply = DE_Call(newDigitalEntity, "update");
 
                 if (checkReplyForError(reply)) {
                     break;
                 }
 
                 success = true;
 
             } catch (Exception e) {
                 printExceptionInfo(e);
             }
         } while (false);
 
         if (success) {
             logger.fine("Object info copied");
         } else {
             logger.severe("could not copy info from '"
                     + params.from_pid + "' to '" + params.to_pid + "'");
         }
 
         return success;
     }
 
     public static class TransformationParameter {
 
         public final String name;
         public final Object value;
 
         public TransformationParameter(String name, Object value) {
             this.name = name;
             this.value = value;
         }
     }
 
     public String transformXml(String xml, String stylesheet, TransformationParameter[] parameters) {
 
         String result = null;
 
         if (xml == null) {
             return result;
         }
 
         try {
 
             Transformer transformer = TransformerFactory.newInstance().newTransformer(
                     new StreamSource(
                     new ByteArrayInputStream(
                     readFile(stylesheet).getBytes("UTF-8"))));
 
             for (int i = 0; i < parameters.length; i++) {
                 transformer.setParameter(parameters[i].name, parameters[i].value);
             }
 
             ByteArrayOutputStream out = new ByteArrayOutputStream(xml.length());
 
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 
             transformer.transform(
                     new StreamSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))),
                     new StreamResult(out));
 
             result = new String(out.toByteArray(), "UTF-8");
 
         } catch (Exception e) {
             printExceptionInfo(e);
         }
 
         return result;
     }
 
     public boolean updateDigitalEntity(String digital_entity) {
 
         boolean success = false;
 
         try {
 
             String reply = DE_Call(digital_entity, "update");
 
             if (checkReplyForError(reply)) {
                 return success;
             }
 
             success = true;
 
         } catch (Exception ex) {
             printExceptionInfo(ex);
         }
 
         return success;
     }
 
     private String DE_Search(String query) {
 
         String reply = null;
 
         try {
 
             String general = getGeneralFile();
 
             Service service = new Service();
             Call call = (Call) service.createCall();
 
             call.setOperationName("digitalEntitySearch");
 
             call.addParameter("general", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
             call.addParameter("query", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
 
             call.setReturnType(org.apache.axis.Constants.XSD_STRING);
 
 //            logger.finest("DESearch: " + query);
 
             reply = doSoapCall("DigitalEntityExplorer", call, new Object[]{general, query});
 
 //            logger.finest("DEReply: " + reply);
 
         } catch (Exception e) {
             printExceptionInfo(e);
         }
 
         return reply;
 
     }
 
     private String DE_Call(String digital_entity, String command) throws Exception {
 
         String digitalEntity = removeNamespace(digital_entity);
 
         String digitalEntityCall = readFile(digitalEntityCallXml).replaceAll("%digital_entity%", digitalEntity).replaceAll("%command%", command);
 
         logger.finest("DECall: " + digitalEntityCall);
 
         String reply = DE_Call(digitalEntityCall);
 
         logger.finest("DEReply: " + reply);
 
         return reply;
 
     }
 
     private String DE_Call(String digital_entity_call) {
 
         String reply = null;
 
         try {
 
             String general = getGeneralFile();
 
             Service service = new Service();
             Call call = (Call) service.createCall();
 
             call.setOperationName("digitalEntityCall");
 
             call.addParameter("general", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
             call.addParameter("digital_entity_call", org.apache.axis.Constants.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
 
             call.setReturnType(org.apache.axis.Constants.XSD_STRING);
 
             reply = doSoapCall("DigitalEntityManager", call, new Object[]{general, digital_entity_call});
 
         } catch (Exception e) {
             printExceptionInfo(e);
         }
 
         return reply;
     }
 
     private String xmlify(String input) {
         return new String("<![CDATA[" + input.replaceAll("]]>", "]]>]]><![CDATA[") + "]]>");
     }
 
     public Document parseString(String xml) {
 
         Document result = null;
 
         try {
 
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
 
             result = db.parse(new InputSource(new StringReader(xml)));
 
         } catch (Exception e) {
             printExceptionInfo(e);
         }
 
         return result;
     }
 
     private String nodeToString(Node node) {
 
         StringWriter sw = new StringWriter();
 
         try {
 
             Transformer t = TransformerFactory.newInstance().newTransformer();
 
             t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 
             t.transform(new DOMSource(node), new StreamResult(sw));
 
         } catch (TransformerException te) {
             printExceptionInfo(te);
         }
 
         return sw.toString();
     }
 
    private String addNamespace(String digital_entity) {
         return digital_entity.replaceFirst("<xb:digital_entity>", "<xb:digital_entity xmlns:xb=\"http://com/exlibris/digitool/repository/api/xmlbeans\">");
     }
 
    private String removeNamespace(String digital_entity) {
         String result = digital_entity.replaceAll("<[?]xml[^>]*>","");
         result = result.replaceFirst("<xb:digital_entity[^>]*>", "<xb:digital_entity>");
         return result;
     }
 
     private boolean checkReplyForError(String reply) {
         boolean result = false;
 
         try {
 
             if (reply == null) {
                 return true;
             }
 
             Document doc = parseString(reply);
             NodeList errors = doc.getElementsByTagName("error");
 
             if (errors.getLength() > 0) {
                 result = true;
                 String message = "Error in SOAP reply: ";
                 NodeList errorInfo = doc.getElementsByTagName("error_description");
                 for (int i = 0; i < errorInfo.getLength(); i++) {
                     Node error_description = errorInfo.item(i);
                     logger.severe(message + error_description.getTextContent());
                 }
             }
 
         } catch (Exception e) {
             printExceptionInfo(e);
         }
 
         return result;
     }
 
     private String[] parseReply(String reply, String tag) {
 
         ArrayList<String> result = new ArrayList<String>(0);
 
         try {
 
             Document doc = parseString(reply);
             NodeList tags = doc.getElementsByTagName(tag);
 
             for (int i = 0; i < tags.getLength(); i++) {
                 if (tags.item(i).getFirstChild().getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                     result.add(tags.item(i).getTextContent());
                 } else {
                     result.add(nodeToString(tags.item(i)));
                 }
             }
 
         } catch (Exception e) {
             printExceptionInfo(e);
             return null;
         }
 
         return result.toArray(new String[]{});
 
     }
 
     private String getGeneralFile() throws IOException {
         String general = readFile(generalFile);
         general = general.replaceAll("%active_library%", active_library).replaceAll("%active_user%", active_user).replaceAll("%user_name%", user_name).replaceAll("%password%", password);
         return general;
 
     }
 
     private String readFile(String fileName) throws IOException {
         InputStream in = getClass().getResourceAsStream(fileName);
         ByteArrayOutputStream out = new ByteArrayOutputStream(100);
         byte[] buffer = new byte[100];
         int length = 0;
         while ((length = in.read(buffer)) > 0) {
             out.write(buffer, 0, length);
         }
         return new String(out.toByteArray(), "UTF-8");
     }
 
     private String doSoapCall(String service, Call call, Object[] parameters) {
 
         String result = null;
 
         try {
             String endpoint = soapURL + service;
 
             call.setTargetEndpointAddress(new java.net.URL(endpoint));
 
             result = (String) call.invoke(parameters);
 
         } catch (Exception e) {
             printExceptionInfo(e);
         }
 
         return result;
 
     }
 
     public static void printExceptionInfo(Exception e, Logger logger) {
         ToolBox tb = new ToolBox();
         tb.setLogger(logger);
         tb.printExceptionInfo(e);
     }
 
     private void printExceptionInfo(Exception e) {
         logger.severe(e.getClass().getName() + " : " + e.getMessage());
         for (StackTraceElement el : e.getStackTrace()) {
             logger.finer(" - " + el.toString());
         }
     }
 
     static public String fileToLabel(String file_name) {
         File f = new File(file_name);
         String label = f.getName();
         int dotPosition = label.lastIndexOf('.');
         if (dotPosition > 0) {
             label = label.substring(0, dotPosition);
         }
         return label;
     }
 
     private String sendGetRequest(String endpoint, String requestParameters) {
         String result = null;
 
         try {
             StringBuffer data = new StringBuffer();
             String urlStr = endpoint;
 
             if (requestParameters != null && requestParameters.length() > 0) {
                 urlStr += "?" + requestParameters;
             }
 
             logger.finest("Sending URL: " + urlStr);
             URL url = new URL(urlStr);
             URLConnection conn = url.openConnection();
 
             // Get the response
             BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             StringBuffer sb = new StringBuffer();
             String line;
             while ((line = rd.readLine()) != null) {
                 sb.append(line);
             }
             rd.close();
             result = sb.toString();
 
             logger.finest("Received reply: " + result);
 
         } catch (Exception e) {
             ToolBox.printExceptionInfo(e, logger);
         }
 
         return result;
     }
 }
