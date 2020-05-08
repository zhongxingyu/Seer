 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.css;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.plaf.Update;
 import org.wings.*;
 import org.wings.event.SRequestListener;
 import org.wings.event.SRequestEvent;
 import org.wings.externalizer.AbstractExternalizeManager;
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.dnd.DragAndDropManager;
 import org.wings.header.*;
 import org.wings.io.Device;
 import org.wings.io.StringBuilderDevice;
 import org.wings.plaf.CGManager;
 import org.wings.plaf.Update.Handler;
 import org.wings.plaf.css.InternalFrameCG.AddWindowUpdate;
 import org.wings.plaf.css.InternalFrameCG.RemoveWindowUpdate;
 import org.wings.plaf.css.script.OnPageRenderedScript;
 import org.wings.resource.ClassPathResource;
 import org.wings.resource.ReloadResource;
 import org.wings.resource.ResourceManager;
 import org.wings.resource.UpdateResource;
 import org.wings.resource.ResourceNotFoundException;
 import org.wings.script.*;
 import org.wings.session.*;
 
 import javax.swing.*;
 
 import java.io.IOException;
 import java.util.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.InputEvent;
 
 /**
  * <code>CmsFrameCG<code>.
  * <p/>
  * User: rrd
  * Date: 10.08.2007
  * Time: 16:43:09
  *
  * @author rrd
  * @version $Id
  */
 public class CmsFrameCG implements org.wings.plaf.FrameCG {
 
     private static final long serialVersionUID = 1L;
 
     private final static Log log = LogFactory.getLog(FrameCG.class);
 
     /**
      * The default DOCTYPE enforcing standard (non-quirks mode) in all current browsers. Please be aware, that
      * changing the DOCTYPE may change the way how browser renders the generate document i.e. esp. the CSS
      * attribute inheritance does not work correctly on <code>table</code> elements.
      * See i.e. http://www.ericmeyeroncss.com/bonus/render-mode.html
      */
     public final static String STRICT_DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" " +
             "\"http://www.w3.org/TR/REC-html40/strict.dtd\">";
 
     /**
      * The HTML DOCTYPE setting all browsers to Quirks mode. We need this to force IE to use the correct box
      * rendering model. It's the only browser you cannot reconfigure via a CSS tag.
      */
     public final static String QUIRKS_DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
 
     /**
      * Lookup for a property Stylesheet.BROWSERNAME to know fitting stylesheets
      */
     private static final String PROPERTY_STYLESHEET = "Stylesheet.";
     private static final String BROWSER_DEFAULT = "default";
 
     private String documentType = STRICT_DOCTYPE;
 
     protected final List<Header> headers = new ArrayList<Header>();
 
     /**
      * Should the returned HTML page start with the &lt;?xml version="1.0" encoding="..."&gt;.
      * This has effects which rendering mode the browsers will choose (quirks/strict)
      */
     private Boolean renderXmlDeclaration = Boolean.FALSE;
 
     private final List<Script> compressedHeaders = new ArrayList<Script>();
     private final Map<Script, Script[]> debugReplacementJsHeaders = new HashMap<Script, Script[]>();
     private final List<Script> debugAddonJsHeaders = new ArrayList<Script>();
     private final String[] firebugResources = new String[] {
             Utils.HTML_DEBUG_FIREBUGLITE,
             Utils.CSS_DEBUG_FIREBUGLITE,
             Utils.IMG_DEBUG_FIREBUGLITE_ERROR,
             Utils.IMG_DEBUG_FIREBUGLITE_WARN,
             Utils.IMG_DEBUG_FIREBUGLITE_INFO
     };
 
     private boolean debugJs = false;
 
     // JS_YUI_UTILITIES = aggregate: yahoo, dom, event, connection, animation, dragdrop, element
     final Script yuiUtilities = Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_UTILITIES);
     final Script yuiContainer = Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_CONTAINER);
     final Script wingsAll = Utils.createExternalizedJSHeaderFromProperty(Utils.JS_WINGS_ALL);
 
     {
         compressedHeaders.add(yuiUtilities);
         compressedHeaders.add(yuiContainer);
         compressedHeaders.add(wingsAll);
         debugReplacementJsHeaders.put(
                 yuiUtilities, new Script[] {
                     Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_YAHOO_DEBUG),
                     Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_DOM_DEBUG),
                     Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_EVENT_DEBUG),
                     Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_CONNECTION_DEBUG),
                     Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_ANIMATION_DEBUG),
                     Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_DRAGDROP_DEBUG),
                     Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_ELEMENT_DEBUG)
                 }
         );
         debugReplacementJsHeaders.put(
                 yuiContainer, new Script[] {
                     Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_CONTAINER_DEBUG)
                 }
         );
         debugReplacementJsHeaders.put(
                 wingsAll, new Script[] {
                     Utils.createExternalizedJSHeaderFromProperty(Utils.JS_WINGS_ALL_DEBUG)
                 }
         );
 
         debugAddonJsHeaders.add(Utils.createExternalizedJSHeaderFromProperty(Utils.JS_DEBUG_FIREBUGLITE));
         // HTML, CSS and Images for firebuglite
         ExternalizeManager extMgr = SessionManager.getSession().getExternalizeManager();
         for (int i = 0; i < firebugResources.length; i++) {
             String classPath = (String) ResourceManager.getObject(firebugResources[i], String.class);
             ClassPathResource res = new ClassPathResource(classPath);
             String string = extMgr.externalize(res, AbstractExternalizeManager.GLOBAL);
         }
     }
 
     /**
      * Initialize properties from config
      */
     public CmsFrameCG() {
         final CGManager manager = SessionManager.getSession().getCGManager();
         final String userDocType = (String) manager.getObject("FrameCG.userDocType", String.class);
         final Boolean userRenderXmlDecl = (Boolean) manager.getObject("FrameCG.renderXmlDeclaration", Boolean.class);
 
         if (userDocType != null) {
             setDocumentType(userDocType);
         }
 
         if (userRenderXmlDecl != null) {
             setRenderXmlDeclaration(userRenderXmlDecl);
         }
 
         // Add CSS headers of YUI components which should be included in every frames by default
         // (DO use files under "yui/assets" and DO NOT use those under "yui/<component>/assets")
         headers.add(Utils.createExternalizedCSSHeaderFromProperty(Utils.CSS_YUI_ASSETS_CONTAINER));
         // Common hack to externalize YUI's 'sprite.png' which contains most (if not all) images of the SAM skin
         new SResourceIcon((String) ResourceManager.getObject(Utils.IMG_YUI_ASSETS_SPRITE, String.class)).getId();
 
         // Add DWR headers
         headers.add(new JavaScriptHeader("../dwr/engine.js"));
         headers.add(new JavaScriptHeader("../dwr/util.js"));
 
         // Common hack to externalize the ugly .htc-file for dealing with form buttons in IE
         new ClassPathResource("org/wings/plaf/css/formbutton.htc", "text/x-component").getId();
     }
 
     public void installCG(final SComponent comp) {
         final SFrame component = (SFrame) comp;
 
         // Add dynamic resources to the frame
         ReloadResource reloadResource = new ReloadResource(component);
         component.addDynamicResource(reloadResource);
         UpdateResource updateResource = new UpdateResource(component);
         component.addDynamicResource(updateResource);
 
         // Externalize update resource
         component.getDynamicResource(UpdateResource.class).getId();
 
         final JavaScriptDOMListener handleClicks = new JavaScriptDOMListener(
                 JavaScriptEvent.ON_CLICK,
                 "wingS.util.handleBodyClick", comp);
         final JavaScriptDOMListener storeFocusFF = new JavaScriptDOMListener(
                 JavaScriptEvent.ON_FOCUS,
                 "wingS.util.storeFocus", comp);
         final JavaScriptDOMListener storeFocusIE = new JavaScriptDOMListener(
                 JavaScriptEvent.ON_ACTIVATE,
                 "wingS.util.storeFocus", comp);
 
         // Add script listeners to the frame
         component.addScriptListener(handleClicks);
         component.addScriptListener(Utils.isMSIE(component) ? storeFocusIE : storeFocusFF);
 
         SessionHeaders.getInstance().registerHeaders(0, headers);
         SessionHeaders.getInstance().registerHeaders(0, compressedHeaders);
         SessionHeaders.getInstance().registerHeaders(getBrowserStylesheets());
 
         new InputMapRequestListener(component);
     }
 
     class InputMapRequestListener {
         SFrame frame;
 
         public InputMapRequestListener(SFrame frame) {
             this.frame = frame;
             frame.putClientProperty("InputMapRequestListener", this);
 
             frame.getSession().addRequestListener(new SRequestListener() {
                 public void processRequest(SRequestEvent e) {
                     if (e.getType() == SRequestEvent.DELIVER_START && InputMapRequestListener.this.frame.getDynamicResources().contains(e.getRequestedResource().getObject())) {
                         boolean changeDetected = false;
 
                         Set<SComponent> components = InputMapRequestListener.this.frame.getGlobalInputMapComponents();
 
                         for (SComponent component : components) {
                             boolean visible = component.isRecursivelyVisible();
                             if (!Boolean.valueOf(visible).equals(component.getClientProperty("visible"))) {
                                 component.putClientProperty("visible", visible);
                                 changeDetected |= true;
                             }
                         }
                         for (SComponent component : components) {
                             if (checkForChange(component, SComponent.WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT)) {
                                 changeDetected |= true;
                             }
                             if (checkForChange(component, SComponent.WHEN_IN_FOCUSED_FRAME)) {
                                 changeDetected |= true;
                             }
                         }
                         if (changeDetected) {
                             String script = strokes(components);
                             InputMapRequestListener.this.frame.getSession().getScriptManager().addScriptListener(new JavaScriptListener(null, null, script));
                         }
                     }
                 }
             });
         }
 
         private boolean checkForChange(SComponent component, int condition) {
             InputMap inputMap = component.getInputMap(condition);
             if (inputMap != null && inputMap.size() > 0) {
                 if (!(inputMap instanceof VersionedInputMap)) {
                     inputMap = new VersionedInputMap(inputMap);
                     component.setInputMap(condition, inputMap);
                     component.putClientProperty("inputMapVersion" + condition, -1);
                 }
 
                 final VersionedInputMap versionedInputMap = (VersionedInputMap)inputMap;
                 final Integer inputMapVersion = (Integer) component.getClientProperty("inputMapVersion" + condition);
                 if (inputMapVersion == null || versionedInputMap.getVersion() != inputMapVersion) {
                     component.putClientProperty("inputMapVersion" + condition, versionedInputMap.getVersion());
                     return true;
                 }
             }
             return false;
         }
     }
 
     protected String strokes(Set<SComponent> components) {
         if (components == null)
             return null;
 
         StringBuilder builder = new StringBuilder();
         builder.append("var wk = wingS.keyboard;");
         builder.append("var kss = wk.keyStrokes = [];");
         builder.append("var ks = wingS.keyboard.KeyStroke;\n");
         for (SComponent component : components) {
             if (component.isRecursivelyVisible()) {
                 appendStrokes(builder, component, SComponent.WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT, component.getInputMap(SComponent.WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT));
                 appendStrokes(builder, component, SComponent.WHEN_IN_FOCUSED_FRAME, component.getInputMap(SComponent.WHEN_IN_FOCUSED_FRAME));
             }
         }
         return builder.toString();
     }
 
     private void appendStrokes(StringBuilder builder, SComponent component, int condition, InputMap inputMap) {
         KeyStroke[] keyStrokes = inputMap.keys();
         if (keyStrokes != null) {
             for (int i = 0; i < keyStrokes.length; i++) {
                 KeyStroke keyStroke = keyStrokes[i];
                 Object binding = inputMap.get(keyStroke);
 
                 switch (keyStroke.getKeyEventType()) {
                     case KeyEvent.KEY_PRESSED:
                         builder.append("kss.push(new ks('");
                         builder.append(component.getName());
                         builder.append("',");
                         builder.append(condition == SComponent.WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT ? "!0" : "!1");
                         builder.append(",'");
                         builder.append(binding);
                         builder.append("',");
                         builder.append(keyStroke.getKeyCode());
                         builder.append(',');
                         builder.append((keyStroke.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0 ? "!0" : "!1");
                         builder.append(',');
                         builder.append((keyStroke.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0 ? "!0" : "!1");
                         builder.append(',');
                         builder.append((keyStroke.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0 ? "!0" : "!1");
                         builder.append("));\n");
                         break;
                     case KeyEvent.KEY_TYPED:
                         break;
                     case KeyEvent.KEY_RELEASED:
                         break;
                 }
             }
         }
     }
 
     /**
      * Externalizes the style sheet(s) for this session. Look up according style sheet file name in
      * org.wings.plaf.css.properties file under Stylesheet.BROWSERNAME. The style sheet is loaded from
      * the class path.
      *
      * @return a list of externalized browser specific stylesheet headers
      */
     private List<Header> getBrowserStylesheets() {
         Session session = SessionManager.getSession();
         final CGManager cgManager = session.getCGManager();
         final String browserName = session.getUserAgent().getBrowserType().getShortName();
 
         String cssClassPaths = (String) cgManager.getObject(PROPERTY_STYLESHEET + browserName, String.class);
         if (cssClassPaths == null) {
             cssClassPaths = (String) cgManager.getObject(PROPERTY_STYLESHEET + BROWSER_DEFAULT, String.class);
         }
 
         List<Header> browserStylesheets = new ArrayList<Header>();
         StringTokenizer tokenizer = new StringTokenizer(cssClassPaths, ",");
         while (tokenizer.hasMoreTokens()) {
             browserStylesheets.add(Utils.createExternalizedCSSHeader(tokenizer.nextToken()));
         }
 
         return browserStylesheets;
     }
 
     /**
      * Uninstall renderer (i.e. other to apply other renderer).
      */
     public void uninstallCG(final SComponent comp) {
         final SFrame component = (SFrame) comp;
 
         component.removeDynamicResource(ReloadResource.class);
         component.removeDynamicResource(UpdateResource.class);
 
         SessionHeaders.getInstance().deregisterHeaders(headers);
     }
 
     public void componentChanged(SComponent c) {
     }
 
     public void write(final Device device, final SComponent component) throws IOException {
         final SFrame frame = (SFrame) component;
 
         String strokes = strokes(frame.getGlobalInputMapComponents());
         if (strokes != null)
             component.getSession().getScriptManager().addScriptListener(new JavaScriptListener(null, null, strokes));
 
         if (!frame.isVisible())
             return;
         else
             frame.fireRenderEvent(SComponent.START_RENDERING);
 
         Session session = SessionManager.getSession();
         final String language = session.getLocale().getLanguage();
         final String title = frame.getTitle();
         final String encoding = session.getCharacterEncoding();
 
         // <?xml version="1.0" encoding="...">
         if (renderXmlDeclaration == null || renderXmlDeclaration) {
             device.print("<?xml version=\"1.0\" encoding=\"");
             Utils.write(device, encoding);
             device.print("\"?>\n");
         }
 
         // <!DOCTYPE HTML PUBLIC ... >
         Utils.writeRaw(device, documentType);
         device.print("\n");
 
         // <html> tag
         device.print("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"");
         Utils.write(device, language);
         device.print("\" lang=\"");
         Utils.write(device, language);
         device.print("\">\n");
 
         // <head> tag
         device.print("<head>");
         if (title != null) {
             device.print("<title>");
             Utils.write(device, title);
             device.print("</title>\n");
         }
 
         // Character set encoding, the default is typically utf-8.
         device.print("<meta http-equiv=\"Content-type\" content=\"text/html; charset=");
         Utils.write(device, encoding);
         device.print("\"/>\n");
 
         /* Insert version and compile time. Since the Version Class is generated on compile time,
          * build errors in SDK's are quite normal. Just run the Version.java ant task.
          */
         device.print("<meta http-equiv=\"Generator\" content=\"wingS v");
         device.print(Version.getVersion());
         device.print(" (http://wingsframework.org) - built on: ");
         device.print(Version.getCompileTime());
         device.print("\" />\n");
 
         // Render all headers
         boolean isDebug = frame.isDebugJs();
         if (isDebug) {
             for (Script debugAddon : debugAddonJsHeaders) {
                 ((Renderable) debugAddon).write(device);
                 device.print("\n");
             }
         }
         for (Object next : frame.getHeaders()) {
             if (next instanceof Renderable) {
                 try {
                     if (isDebug && getDebugHeaders(next) != null) {
                         // Render uncompressed headers
                         Script[] debugHeaders = getDebugHeaders(next);
                         for (Script debugHeader : debugHeaders) {
                             if (debugHeader instanceof Renderable) {
                                 ((Renderable) debugHeader).write(device);
                             } else {
                                 Utils.write(device, debugHeader.toString());
                             }
                         }
                     } else {
                         // Render compressed headers
                         ((Renderable) next).write(device);
                     }
                 } catch (ResourceNotFoundException e) {
                     log.error("Unable to deliver inlined renderable", e);
                 }
             } else {
                 Utils.write(device, next.toString());
             }
             device.print("\n");
         }
 
         // Focus management. Put focus in selected object.
         if (frame.getFocus() != null) {
             String script = "wingS.util.requestFocus('" + frame.getFocus().getName() + "');";
             ScriptManager.getInstance().addScriptListener(new OnPageRenderedScript(script));
         }
 
         device.print("</head>\n");
         device.print("<body");
         Utils.writeEvents(device, frame, null);
         Utils.writeAllAttributes(device, frame);
         device.print(">\n");
 
         // Write contents of the frame
         if (frame.isVisible()) {
             Utils.createExternalizedJSHeaderFromProperty(Utils.JS_ETC_WZ_TOOLTIP).write(device);
             device.print("\n");
 
             // Write components
             frame.getLayout().write(device);
 
             // Write menus
             device.print("\n\n<div id=\"wings_menues\">\n");
             Set menues = frame.getSession().getMenuManager().getMenues(frame);
             for (Iterator i = menues.iterator(); i.hasNext();) {
                 SComponent menuItem = (SComponent) i.next();
                 menuItem.putClientProperty("popup", Boolean.TRUE);
                 menuItem.write(device);
                 menuItem.putClientProperty("popup", null);
             }
             device.print("\n</div>\n\n");
 
             DragAndDropManager dndManager = frame.getSession().getDragAndDropManager();
             dndManager.getCG().write(device, dndManager);
 
             handleScripts(device, frame);
         }
 
         device.print("</body>\n</html>\n");
 
         component.fireRenderEvent(SComponent.DONE_RENDERING);
     }
 
     /**
      * @param next
      * @return
      */
     private Script[] getDebugHeaders(Object next) {
         if (debugReplacementJsHeaders.containsKey(next)) {
             return debugReplacementJsHeaders.get(next);
         } else {
             return null;
         }
     }
 
     protected void handleScripts(Device device, SComponent component) throws IOException {
         final SFrame frame = (SFrame) component;
         final ScriptManager scriptManager = frame.getSession().getScriptManager();
         final SToolTipManager tooltipManager = SToolTipManager.sharedInstance();
 
         // hand script listeners of frame to script manager
         scriptManager.addScriptListeners(frame.getScriptListeners());
 
         device.print("<script type=\"text/javascript\">\n");
 
         // print all scripts
         writeGlobalInitScript(device, frame);
         device.print("\n");
         writeTooltipInitScript(device, tooltipManager);
         device.print("\n");
         ScriptListener[] scriptListeners = scriptManager.getScriptListeners();
         for (int i = 0; i < scriptListeners.length; ++i) {
             if (scriptListeners[i].getScript() != null) {
                 device.print(scriptListeners[i].getScript()).print("\n");
             }
         }
         scriptManager.clearScriptListeners();
 
         device.print("</script>\n");
     }
 
     private void writeGlobalInitScript(Device out, SFrame frame) throws IOException {
         Map<String, Object> initConfig = new HashMap<String, Object>();
         initConfig.put("eventEpoch", frame.getEventEpoch());
         initConfig.put("reloadResource", frame.getDynamicResource(ReloadResource.class).getURL().toString());
         initConfig.put("updateResource", frame.getDynamicResource(UpdateResource.class).getURL().toString());
         initConfig.put("updateEnabled", frame.isUpdateEnabled());
         initConfig.put("updateCursor", Utils.mapToJsObject(frame.getUpdateCursor()));
         initConfig.put("autoAdjustLayout", Utils.mapToJsObject(frame.getAutoAdjustLayout()));
         final String logLevel = frame.getLogLevel();
         if (logLevel != null && !"".equals(logLevel)) {
             initConfig.put("loglevel", logLevel);
         }
 
         out.print("wingS.global.init(");
         Utils.mapToJsObject(initConfig).write(out);
         out.print(");");
     }
 
     private void writeTooltipInitScript(Device out, SToolTipManager tooltipManager) throws IOException {
         out.print("wingS.tooltip.init(");
         out.print(tooltipManager.getInitialDelay()).print(",");
         out.print(tooltipManager.getDismissDelay()).print(",");
         out.print(tooltipManager.isFollowMouse()).print(");");
     }
 
     public String getDocumentType() {
         return documentType;
     }
 
     public void setDocumentType(String documentType) {
         this.documentType = documentType;
     }
 
     /**
      * @return The current rendered DOCTYPE of this document. {@link #STRICT_DOCTYPE}
      */
     public Boolean getRenderXmlDeclaration() {
         return renderXmlDeclaration;
     }
 
     /**
      * Sets should the returned HTML page start with the &lt;?xml version="1.0" encoding="..."&gt;.
      * This has effects which rendering mode the browsers will choose (quirks/strict)
      *
      * @param renderXmlDeclaration should the returned HTML page start with the &lt;?xml version="1.0" encoding="..."&gt;.
      */
     public void setRenderXmlDeclaration(Boolean renderXmlDeclaration) {
         this.renderXmlDeclaration = renderXmlDeclaration;
     }
 
 	public Update getComponentUpdate(SComponent component) {
         return null;
 	}
 
     public Update getAddHeaderUpdate(SFrame frame, int index, Object header) {
         if (header instanceof Script)
             return new HeaderScriptUpdate(frame, true, (Script) header, index);
         else if (header instanceof Link)
             return new HeaderLinkUpdate(frame, true, (Link) header, index);
         else
             return null;
     }
 
     public Update getAddHeaderUpdate(SFrame frame, Object header) {
         if (header instanceof Script)
             return new HeaderScriptUpdate(frame, true, (Script) header);
         else if (header instanceof Link)
             return new HeaderLinkUpdate(frame, true, (Link) header);
         else
             return null;
     }
 
     public Update getRemoveHeaderUpdate(SFrame frame, Object header) {
         if (header instanceof Script)
             // Removing script headers asynchronously would indeed
             // detach the according header, however, the functions
             // contained in the according files are not unloaded.
             // If unloading functions is desired, it might be a good
             // idea to RETURN 'NULL' here. This would create a
             // component update of the frame which in turn would
             // force a complete page reload and function unloading.
             return new HeaderScriptUpdate(frame, false, (Script) header);
         else if (header instanceof Link)
             return new HeaderLinkUpdate(frame, false, (Link) header);
         else
             return null;
     }
 
     public Update getEpochUpdate(SFrame frame, String epoch) {
         return new EpochUpdate(frame, epoch);
     }
 
     public Update getFocusUpdate(SFrame frame, SComponent focus) {
         return new FocusUpdate(frame, focus);
     }
 
     public Update getUpdateEnabledUpdate(SFrame frame, boolean enabled) {
         return new UpdateEnabledUpdate(frame, enabled);
     }
 
     protected class HeaderScriptUpdate extends AbstractUpdate {
 
         private Boolean add;
         private Script script;
         private Integer index;
 
         public HeaderScriptUpdate(SComponent component, boolean add, Script script) {
             super(component);
             this.add = add;
             this.script = script;
         }
 
         public HeaderScriptUpdate(SComponent component, boolean add, Script script, int index) {
             this(component, add, script);
             this.index = index;
         }
 
         public int getPriority() {
             return 5;
         }
 
         public Handler getHandler() {
             UpdateHandler handler = new UpdateHandler("headerScript");
             handler.addParameter(add);
             handler.addParameter(script.getURL().toString());
             handler.addParameter(script.getType());
             if (index != null)
                 handler.addParameter(index);
             return handler;
         }
 
         public boolean equals(Object object) {
             if (this == object)
                 return true;
             if (!super.equals(object))
                 return false;
             if (!script.equals(((HeaderScriptUpdate) object).script))
                 return false;
 
             return true;
         }
 
     }
 
     protected class HeaderLinkUpdate extends AbstractUpdate {
 
         private Boolean add;
         private Link link;
         private Integer index;
 
         public HeaderLinkUpdate(SComponent component, boolean add, Link link) {
             super(component);
             this.add = add;
             this.link = link;
         }
 
         public HeaderLinkUpdate(SComponent component, boolean add, Link link, int index) {
             this(component, add, link);
             this.index = index;
         }
 
         public int getPriority() {
             return 5;
         }
 
         public Handler getHandler() {
             UpdateHandler handler = new UpdateHandler("headerLink");
             handler.addParameter(add);
             handler.addParameter(link.getURL().toString());
             handler.addParameter(link.getType());
             if (link.getRel() != null || link.getRev() != null || link.getTarget() != null || index != null)
                 handler.addParameter(link.getRel());
             if (link.getRev() != null || link.getTarget() != null || index != null)
                 handler.addParameter(link.getRev());
             if (link.getTarget() != null || index != null)
                 handler.addParameter(link.getTarget());
             if (index != null)
                 handler.addParameter(index);
 
             return handler;
         }
 
         public boolean equals(Object object) {
             if (this == object)
                 return true;
             if (!super.equals(object))
                 return false;
             if (!link.equals(((HeaderLinkUpdate) object).link))
                 return false;
 
             return true;
         }
 
     }
 
     protected class EpochUpdate extends AbstractUpdate {
 
         private String epoch;
 
         public EpochUpdate(SComponent component, String epoch) {
             super(component);
             this.epoch = epoch;
         }
 
         public int getPriority() {
             return 0;
         }
 
         public Handler getHandler() {
             UpdateHandler handler = new UpdateHandler("epoch");
             handler.addParameter(epoch);
             return handler;
         }
 
     }
 
     protected class FocusUpdate extends AbstractUpdate {
 
         private SComponent focus;
 
         public FocusUpdate(SComponent component, SComponent focus) {
             super(component);
             this.focus = focus;
         }
 
         public int getPriority() {
             return 0;
         }
 
         public Handler getHandler() {
             UpdateHandler handler = new UpdateHandler("focus");
             handler.addParameter(focus.getName());
             return handler;
         }
 
     }
 
     protected class UpdateEnabledUpdate extends AbstractUpdate {
 
         private Boolean enabled;
 
         public UpdateEnabledUpdate(SComponent component, boolean enabled) {
             super(component);
             this.enabled = Boolean.valueOf(enabled);
         }
 
         public Handler getHandler() {
             UpdateHandler handler = new UpdateHandler("updateEnabled");
             handler.addParameter(enabled);
             return handler;
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     public Update getAddWindowUpdate(SContainer container, SWindow window) {
         return new AddWindowUpdate(container, window);
     }
     
     protected class AddWindowUpdate extends AbstractUpdate {
 
     	private SWindow window;
     	
     	public AddWindowUpdate(SContainer container, SWindow window) {
             super(container);
             this.window = window;
         }
     	
     	@Override
 		public int getPriority() {
 			return Integer.MAX_VALUE;
 		}
 
 		public Handler getHandler() {
             UpdateHandler handler = new UpdateHandler("addWindow");
             handler.addParameter(component.getName());
             handler.addParameter("<div id=\"" + window.getName() + "\"/>");
 			return handler;
         }
     }
 
     public Update getRemoveWindowUpdate(final SContainer container, final SWindow window) {
         return new RemoveWindowUpdate(container, window);
     }
 
     protected class RemoveWindowUpdate extends AbstractUpdate {
 
     	private SWindow window;
     	
         public RemoveWindowUpdate(final SContainer container, final SWindow window) {
             super(container);
             this.window = window;
         }
 
         public Handler getHandler() {
             UpdateHandler handler = new UpdateHandler("removeWindow");
             handler.addParameter(window.getName());
 			return handler;
         }
     }
 }
