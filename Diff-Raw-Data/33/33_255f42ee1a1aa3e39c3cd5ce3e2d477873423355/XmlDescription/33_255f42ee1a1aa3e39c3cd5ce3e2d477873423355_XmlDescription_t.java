 package org.itea.neotiq.xmpp;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.itea.neotiq.devicemodel.Device;
 import org.itea.neotiq.devicemodel.DeviceManager;
 import org.itea.neotiq.devicemodel.VDEditText;
 import org.xmlpull.v1.XmlSerializer;
 
 import android.util.Log;
 import android.util.Xml;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 
 public class XmlDescription {
     
     private static final String WS_URL = "http://213.215.48.35:9903";
     private static final String WS_ADD_QUERY = "diyse/global-repo";
     
 
     public static String generateXML() {
         XmlSerializer serializer = Xml.newSerializer();
         String id = "vdf";
         StringWriter writer = new StringWriter();
         try {
             serializer.setOutput(writer);
             serializer.startDocument("UTF-8", true);
                 serializer.startTag("", "ns2:remote-device");
                 serializer.attribute("", "xmlns", "http://api.reasoning.diyse.org");
                serializer.attribute("", "xmlns:ns2", "http://generated.device.diyse.org");
                 //serializer.attribute("", "xmlns:ns3", "http://model.device.gateway.diyse.org");
                 serializer.attribute("", "xmlns:ns4", "http://wadl.dev.java.net/2009/02");
                 serializer.attribute("", "id", id);
                 serializer.attribute("", "type", "NOT IMPLEMENTED");
             
                     serializer.startTag("", "ns2:deviceDescription");
                     serializer.attribute("", "id", id);
                     serializer.attribute("", "type", "NOT IMPLEMENTED");
             
                         serializer.startTag("", "operation");
                         serializer.attribute("", "cost", "5");
                         serializer.attribute("", "knowledgeLevel", "Data");
                         serializer.attribute("", "name", "vdfList");
             
                             serializer.startTag("", "output");
                             serializer.attribute("", "annotation", "Description");
                             serializer.attribute("", "event", "false");
                             serializer.attribute("", "type", "text/xml");
                             serializer.endTag("", "output");
                         
                             serializer.startTag("", "input");
                             serializer.attribute("", "annotation", "void");
                             serializer.attribute("", "event", "false");
                             serializer.attribute("", "type", "void");
                             serializer.endTag("", "input");
 
                         serializer.endTag("", "operation");
                         
                         serializer.startTag("", "operation");
                         serializer.attribute("", "cost", "5");
                         serializer.attribute("", "knowledgeLevel", "Data");
                         serializer.attribute("", "name", "device");
             
                             serializer.startTag("", "output");
                             serializer.attribute("", "annotation", "Result");
                             serializer.attribute("", "event", "false");
                             serializer.attribute("", "type", "text/plain");
                             serializer.endTag("", "output");
                         
                             serializer.startTag("", "input");
                             serializer.attribute("", "annotation", "Device");
                             serializer.attribute("", "event", "false");
                             serializer.attribute("", "type", "text/xml");
                             serializer.endTag("", "input");
 
                         serializer.endTag("", "operation");
                         
                         for (Device d : DeviceManager.getDevices()) {
                             serializer.startTag("", "operation");
                             serializer.attribute("", "cost", "5");
                             serializer.attribute("", "knowledgeLevel", "Data");
                             serializer.attribute("", "name", d.getName().replace(" ", "")+"currentValue");
                 
                                 serializer.startTag("", "output");
                                 serializer.attribute("", "annotation", d.getSemanticAnnotation());
                                 serializer.attribute("", "event", "false");
                                 serializer.attribute("", "type", (d instanceof VDEditText && ((VDEditText)d).isInt()) ? "int" : "string");
                                 serializer.endTag("", "output");
 
                                 serializer.startTag("", "input");
                                 serializer.attribute("", "annotation", "void");
                                 serializer.attribute("", "event", "false");
                                 serializer.attribute("", "type", "void");
                                 serializer.endTag("", "input");
                             
                             serializer.endTag("", "operation");
                             
                             serializer.startTag("", "operation");
                             serializer.attribute("", "cost", "5");
                             serializer.attribute("", "knowledgeLevel", "Data");
                             serializer.attribute("", "name", d.getName().replace(" ", "")+"event");
                 
                                 serializer.startTag("", "output");
                                 serializer.attribute("", "annotation", d.getClass().getName());
                                 serializer.attribute("", "event", "true");
                                 serializer.attribute("", "type", (d instanceof VDEditText && ((VDEditText)d).isInt()) ? "int" : "string");
                                 serializer.endTag("", "output");
                             
                             serializer.endTag("", "operation");
                         }
                     serializer.endTag("", "ns2:deviceDescription");
                     
                     serializer.startTag("", "ns2:grounding");
                     
                         serializer.startTag("", "ns2:publishes");
                         for (Device d : DeviceManager.getDevices()) {
                             serializer.startTag("", "ns2:publish");
                             serializer.attribute("", "xmpp-topic", "pubsub."+XMPPComm.XMPP_HOST+"/"+((XMPPComm)d.getDelegate()).XMPP_NODE);
                             serializer.attribute("", "operation", d.getName().replace(" ", "")+"event");
                             serializer.endTag("", "ns2:publish");
                         }
                         serializer.endTag("", "ns2:publishes");
 
                         serializer.startTag("", "ns4:resources");
                         serializer.attribute("", "base", "http://"+XMPPComm.XMPP_HOST+":"+XMPPComm.XMPP_PORT);
                         
                             serializer.startTag("", "ns4:resource");
                             serializer.attribute("", "path", "/vdfList");
                             serializer.attribute("", "ns2:jid", XMPPComm.getInstance().XMPP_JID);
                                 serializer.startTag("", "ns4:method");
                                 serializer.attribute("", "name", "GET");
                                 serializer.attribute("", "ns2:operation", "vdfList");
                                     serializer.startTag("", "ns4:request");
                                     serializer.endTag("", "ns4:request");
                                     serializer.startTag("", "ns4:response");
                                         serializer.startTag("", "ns4:representation");
                                         serializer.attribute("", "mediaType", "text/xml");
                                             serializer.startTag("", "ns4:param");
                                             serializer.attribute("", "name", "result");
                                             serializer.attribute("", "style", "plain");
                                             serializer.endTag("", "ns4:param");
                                         serializer.endTag("", "ns4:representation");
                                     serializer.endTag("", "ns4:response");
                                 serializer.endTag("", "ns4:method");
                             serializer.endTag("", "ns4:resource");
                                     
                             serializer.startTag("", "ns4:resource");
                             serializer.attribute("", "path", "/device");
                             serializer.attribute("", "ns2:jid", XMPPComm.getInstance().XMPP_JID);
                                 serializer.startTag("", "ns4:method");
                                 serializer.attribute("", "name", "POST");
                                 serializer.attribute("", "ns2:operation", "device");
                                     serializer.startTag("", "ns4:request");
                                         serializer.startTag("", "ns4:representation");
                                         serializer.attribute("", "mediaType", "text/xml");
                                             serializer.startTag("", "ns4:param");
                                             serializer.attribute("", "name", "device");
                                             serializer.attribute("", "style", "plain");
                                             serializer.endTag("", "ns4:param");
                                         serializer.endTag("", "ns4:representation");
                                     serializer.endTag("", "ns4:request");
                                     serializer.startTag("", "ns4:response");
                                         serializer.startTag("", "ns4:representation");
                                         serializer.attribute("", "mediaType", "text/plain");
                                             serializer.startTag("", "ns4:param");
                                             serializer.attribute("", "name", "result");
                                             serializer.attribute("", "style", "plain");
                                             serializer.endTag("", "ns4:param");
                                         serializer.endTag("", "ns4:representation");
                                     serializer.endTag("", "ns4:response");
                                 serializer.endTag("", "ns4:method");
                             serializer.endTag("", "ns4:resource");
 
                             for (Device d : DeviceManager.getDevices()) {
                                 serializer.startTag("", "ns4:resource");
                                 serializer.attribute("", "path", d.getName().replace(" ", "")+"/currentValue");
                                 serializer.attribute("", "ns2:jid", ((XMPPComm)d.getDelegate()).XMPP_JID);
                                     serializer.startTag("", "ns4:method");
                                     serializer.attribute("", "name", "GET");
                                     serializer.attribute("", "ns2:operation", d.getName().replace(" ", "")+"currentValue");
                                         serializer.startTag("", "ns4:request");
                                         serializer.endTag("", "ns4:request");
                                         serializer.startTag("", "ns4:response");
                                             serializer.startTag("", "ns4:representation");
                                             serializer.attribute("", "mediaType", "text/plain");
                                                 serializer.startTag("", "ns4:param");
                                                 serializer.attribute("", "name", "result");
                                                 serializer.attribute("", "style", "plain");
                                                 serializer.endTag("", "ns4:param");
                                             serializer.endTag("", "ns4:representation");
                                         serializer.endTag("", "ns4:response");
                                     serializer.endTag("", "ns4:method");
                                 serializer.endTag("", "ns4:resource");
                             }
                             
                         serializer.endTag("", "ns4:resources");
 
                     serializer.endTag("", "ns2:grounding");
                 serializer.endTag("", "ns2:remote-device");
             serializer.endDocument();
             
         } catch (IllegalArgumentException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IllegalStateException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         Log.d("XmlDescription", writer.toString());
         return writer.toString();
         
     }
 
     public static void sendToGlobalRepo() {
         DefaultHttpClient httpclient = new DefaultHttpClient();  
         HttpPost request = new HttpPost(WS_URL + "/" + WS_ADD_QUERY + "/" + XMPPComm.getInstance().XMPP_JID);
         try {
             request.setEntity(new StringEntity(generateXML()));
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         request.setHeader("Content-type", "text/xml");
         HttpResponse result = null;
         try {
             result = httpclient.execute(request);
         } catch (ClientProtocolException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }  
         try {
             Log.d("XmlDesc", result.toString());
             if (result.getStatusLine().getStatusCode() == 204) {
                 Log.d("XmlDesc", "Success");
             } else {
                 Log.d("XmlDesc", "Failure : "+EntityUtils.toString(result.getEntity()));
             }
         } catch (ParseException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } 
         httpclient.getConnectionManager().shutdown();  
     }
     
 }
