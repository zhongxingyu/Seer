 package org.wings.resource;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.ReloadManager;
 import org.wings.SFrame;
 import org.wings.plaf.Update;
 import org.wings.plaf.css.Utils;
 import org.wings.io.Device;
 import org.wings.script.ScriptListener;
 import org.wings.session.ScriptManager;
 import org.wings.session.SessionManager;
 
 /**
  * This resource is responsible for incremental page updates using AJAX. While requests sent to the
  * {@link ReloadResource} always entail a full rewrite of the frame's complete component hierarchy,
  * requests received by the {@link UpdateResource} will result in preferably small data chunks being
  * sent back to the client. These so called "incremental updates" encapsulate all informations the
  * client needs in order to synchronize its page with the server's state. Because this resource is
  * typically requested from an XMLHttpRequest object, it returns a well-formed XML document with MIME
  * type "text/xml". The updates in this document will be processed by according JavaScript functions.
  *
  * The XML structure looks as follows:
  *
  * <pre>
  * <?xml version="1.0" encoding="[encoding]" standalone="yes"?>
  * <updates>
  *   <update><![CDATA[ my1stClientSideHandler(...) ]]></update>
  *   <update><![CDATA[ my2ndClientSideHandler(...) ]]></update>
  *   <update><![CDATA[ my3rdClientSideHandler(...) ]]></update>
  *   ...
  * </updates>
  * </pre>
  *
  * @author Stephan Schuster
  */
 public class UpdateResource extends DynamicResource {
 
     private static final transient Log log = LogFactory.getLog(UpdateResource.class);
 
     public UpdateResource(final SFrame f) {
         super(f);
         this.extension = "xml";
         this.mimeType = "text/xml; charset=" + SessionManager.getSession().getCharacterEncoding();
     }
 
     public void write(Device out) throws IOException {
         try {
             final SFrame frame = getFrame();
             final ReloadManager reloadManager = frame.getSession().getReloadManager();
             final ScriptManager scriptManager = frame.getSession().getScriptManager();
 
             writeHeader(out);
             if (reloadManager.isReloadRequired(frame)) {
                 writeUpdate(out, "wingS.request.reloadFrame()");
             } else {
                 // update components
                 for (Iterator i = reloadManager.getUpdates().iterator(); i.hasNext();) {
                    Update update = (Update) i.next();
                    if (update == null) {
                        continue;
                    }
                    writeUpdate(out, update);
                 }
 
                 // update scripts
                 ScriptListener[] scriptListeners = scriptManager.getScriptListeners();
                 for (int i = 0; i < scriptListeners.length; ++i) {
                     if (scriptListeners[i].getScript() != null) {
                         writeUpdate(out, scriptListeners[i].getScript());
                     }
                 }
                 scriptManager.clearScriptListeners();
 			}
             writeFooter(out);
 
         } catch (IOException e) {
             throw e;
         } catch (Exception e) {
             log.fatal("resource: " + getId(), e);
             throw new IOException(e.getMessage());
         }
     }
 
     public static void writeHeader(Device out) throws IOException {
         String encoding = SessionManager.getSession().getCharacterEncoding();
         out.print("<?xml version=\"1.0\" encoding=\"" + encoding + "\" standalone=\"yes\"?>");
         out.print("\n<updates>");
 
         if (log.isDebugEnabled()) {
             SFrame frame = SessionManager.getSession().getRootFrame();
             log.debug("Request: " + (frame != null ? frame.getEventEpoch() : "x"));
         }
     }
 
     public static void writeFooter(Device out) throws IOException {
         out.print("\n</updates>");
     }
 
     public static void writeUpdate(Device out, Update update) throws IOException {
         Update.Handler handler = update.getHandler();
 
         writePrefix(out);
     	out.print(handler.getName()).print('(');
         Iterator parameters = handler.getParameters();
         boolean isFirst = true;
         while (parameters.hasNext()) {
             if (!isFirst) out.print(',');
             Utils.encodeJS(out, parameters.next());
             isFirst = false;
         }
         out.print(')');
         writePostfix(out);
 
         if (log.isDebugEnabled()) {
             log.debug(update.getClass().getName() + ":");
             StringBuilder builder = new StringBuilder();
             builder.append(handler.getName()).append("(");
             parameters = handler.getParameters();
             if (parameters.hasNext())
                 builder.append(parameters.next());
             while (parameters.hasNext())
                 builder.append(",").append(parameters.next());
             builder.append(")");
             log.debug(builder.toString());
         }
     }
 
     public static void writeUpdate(Device out, String update) throws IOException {
         writePrefix(out);
         out.print(update);
         writePostfix(out);
     }
 
     private static void writePrefix(Device out) throws IOException {
         out.print("\n  <update><![CDATA[");
     }
 
     private static void writePostfix(Device out) throws IOException {
         out.print("]]></update>");
     }
 }
