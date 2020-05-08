 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.css;
 
 
 import org.wings.*;
 import org.wings.header.Link;
 import org.wings.header.Script;
 import org.wings.io.Device;
 import org.wings.plaf.CGManager;
 import org.wings.resource.DefaultURLResource;
 import org.wings.resource.DynamicCodeResource;
 import org.wings.script.DynamicScriptResource;
 import org.wings.script.JavaScriptListener;
 import org.wings.script.ScriptListener;
 import org.wings.session.Browser;
 import org.wings.session.BrowserType;
 import org.wings.session.SessionManager;
 import org.wings.style.CSSSelector;
 import org.wings.style.DynamicStyleSheetResource;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 public class FrameCG implements SConstants, org.wings.plaf.FrameCG {
 
     /**
      * The default DOCTYPE enforcing standard (non-quirks mode) in all current browsers.
      * Please be aware, that changing the DOCTYPE may change the way how browser renders the generate
      * document i.e. esp. the CSS attribute inheritance does not work correctly on <code>table</code> elements.
      * See i.e. http://www.ericmeyeroncss.com/bonus/render-mode.html
      */
     public final static String STRICT_DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" " +
             "\"http://www.w3.org/TR/REC-html40/strict.dtd\">";
 
     /**
      * The HTML DOCTYPE setting all browsers to Quirks mode.
      * We need this to force IE to use the correct box rendering model. It's the only browser
      * you cannot reconfigure via an CSS tag.
      */
     public final static String QUIRKS_DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
 
     // Browser type for which we provided a custom css. Others switch to default
     private final static Set providedBrowserCss;
     static {
         providedBrowserCss = new HashSet();
         providedBrowserCss.add(BrowserType.IE);
         providedBrowserCss.add(BrowserType.GECKO);
     }
 
     private String documentType = STRICT_DOCTYPE;
 
     private Boolean renderXmlDeclaration = Boolean.FALSE;
 
     /**
      * Initialize properties from config
      */
     public FrameCG() {
         final CGManager manager = SessionManager.getSession().getCGManager();
         final String userDocType = (String) manager.getObject("FrameCG.userDocType", String.class);
         final Boolean userRenderXmlDecl = (Boolean) manager.getObject("FrameCG.renderXmlDeclaration", Boolean.class);
 
         if (userDocType != null)
             setDocumentType(userDocType);
 
         if (userRenderXmlDecl != null)
             setRenderXmlDeclaration(userRenderXmlDecl);
     }
 
 
     public void installCG(final SComponent comp) {
         final SFrame component = (SFrame) comp;
 
         DynamicCodeResource dynamicCodeRessource;
         DynamicStyleSheetResource styleSheetResource;
         DynamicScriptResource scriptResource;
         Link stylesheetLink;
 
         // dynamic code resource.
         dynamicCodeRessource = new DynamicCodeResource(component);
         component.addDynamicResource(dynamicCodeRessource);
 
         // dynamic stylesheet resource.
         styleSheetResource = new DynamicStyleSheetResource(component);
         stylesheetLink = new Link("stylesheet", null, "text/css", null, styleSheetResource);
         component.addDynamicResource(styleSheetResource);
         component.addHeader(stylesheetLink);
 
         // dynamic java script resource.
         scriptResource = new DynamicScriptResource(component);
         component.addDynamicResource(scriptResource);
         component.addHeader(new Script("JavaScript", "text/javascript", scriptResource));
 
         component.addHeader(new Script("JavaScript", "text/javascript", new DefaultURLResource("../domLib.js")));
         component.addHeader(new Script("JavaScript", "text/javascript", new DefaultURLResource("../domTT.js")));
 
         // determine the stylesheet to use
         BrowserType cssBrowserType = component.getSession().getUserAgent().getBrowserType();
         if (providedBrowserCss.contains(cssBrowserType) == false)
             cssBrowserType = BrowserType.GECKO; // fallback to gecko CSS
         String cssURL = "../" + cssBrowserType.getShortName() + ".css";
         component.headers().add(0, new Link("stylesheet", null, "text/css", null, new DefaultURLResource(cssURL)));
 
         component.addScriptListener(FORM_SCRIPT);
         component.addScriptListener(FOCUS_SCRIPT);
         component.addScriptListener(SCROLL_POSITION_SCRIPT);
         CaptureDefaultBindingsScriptListener.install(component);
     }
 
     public void uninstallCG(final SComponent comp) {
         final SFrame component = (SFrame) comp;
 
         component.removeDynamicResource(DynamicCodeResource.class);
         component.removeDynamicResource(DynamicStyleSheetResource.class);
         component.removeDynamicResource(DynamicScriptResource.class);
         component.removeScriptListener(FORM_SCRIPT);
         component.clearHeaders();
     }
 
     //--- code from common area in template.
     /*
     public static final JavaScriptListener DATE_CHOOSER_SCRIPT_LOADER =
     new JavaScriptListener("", "", loadScript("org/wings/plaf/css/DateChooser.js"));
     */
     public static final JavaScriptListener FORM_SCRIPT =
             new JavaScriptListener("", "", loadScript("org/wings/plaf/css/Form.js"));
 
     public static final JavaScriptListener FOCUS_SCRIPT =
             new JavaScriptListener("onfocus", "storeFocus(event)");
 
     public static final JavaScriptListener SCROLL_POSITION_SCRIPT =
             new JavaScriptListener("onscroll", "storeScrollPosition(event)");
 
     public static String loadScript(String resource) {
         InputStream in = null;
         BufferedReader reader = null;
         try {
             in = FrameCG.class.getClassLoader().getResourceAsStream(resource);
             reader = new BufferedReader(new InputStreamReader(in));
             StringBuffer buffer = new StringBuffer();
             String line;
             while ((line = reader.readLine()) != null)
                 buffer.append(line).append("\n");
            buffer.append("\n");
 
             return buffer.toString();
         } catch (Exception e) {
             e.printStackTrace();
             return "";
         } finally {
             try { in.close(); } catch (Exception ign) {}
             try { reader.close(); } catch (Exception ign) {}
         }
     }
 
 
    public void write(final Device device, final SComponent _c)
            throws IOException {
         if (!_c.isVisible()) return;
         _c.fireRenderEvent(SComponent.START_RENDERING);
         final SFrame component = (SFrame) _c;
 
         Browser browser = SessionManager.getSession().getUserAgent();
         SFrame frame = (SFrame) component;
         String language = SessionManager.getSession().getLocale().getLanguage();
         String title = frame.getTitle();
         List headers = frame.headers();
         String encoding = SessionManager.getSession().getCharacterEncoding();
 
         /**
          *  we need to put ie 6 into quirks mode
          *  for box model compatibility. (border-box)
          */
         if (BrowserType.IE.equals(browser.getBrowserType())) {
             if (browser.getMajorVersion() == 6) {
                 device.print("<!-- IE6 quirks mode switch -->\n");
             }
         }
 
         if (renderXmlDeclaration == null || renderXmlDeclaration.booleanValue()) {
             device.print("<?xml version=\"1.0\" encoding=\"");
             Utils.write(device, encoding);
             device.print("\"?>\n");
         }
 
         Utils.writeRaw(device, documentType);
         device.print("\n");
         device.print("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"");
         Utils.write(device, language);
         device.print("\" lang=\"");
         Utils.write(device, language);
         device.print("\">\n");
 
         /* Insert version and compile time.
          * Since the Version Class is generated on compile time, build errors
          * in SDK's are quite normal. Just run the Version.java ant task.
          */
         device.print("<!-- This is wingS (http://www.j-wings.org) version ");
         device.print(Version.getVersion());
         device.print(" (Build date: ");
         device.print(Version.getCompileTime());
         device.print(") -->\n");
         
         device.print("<head>");
         if (title != null) {
             device.print("<title>");
             Utils.write(device, title);
             device.print("</title>\n");
         }
 
         device.print("<meta http-equiv=\"Content-type\" content=\"text/html; charset=");
         Utils.write(device, encoding);
         device.print("\"/>\n");
 
         for (Iterator iterator = headers.iterator(); iterator.hasNext();) {
             Object next = iterator.next();
             if (next instanceof Renderable) {
                 ((Renderable) next).write(device);
             } else {
                 Utils.write(device, next.toString());
             }
             device.print("\n");
         }
 
         SComponent focus = frame.getFocus();
         Object lastFocus = frame.getClientProperty("focus");
         if (focus != lastFocus) {
             if (lastFocus != null) {
                 ScriptListener[] scriptListeners = frame.getScriptListeners();
 
                 for (int i = 0; i < scriptListeners.length; i++) {
                     ScriptListener scriptListener = scriptListeners[i];
                     if (scriptListener instanceof FocusScriptListener)
                         component.removeScriptListener(scriptListener);
                 }
             }
             if (focus != null) {
                 FocusScriptListener listener = new FocusScriptListener("onload", "requestFocus('" + focus.getName() + "')");
                 frame.addScriptListener(listener);
             }
             frame.putClientProperty("focus", focus);
         }
         
         // let ie understand hover css styles on elements other than anchors
         if (BrowserType.IE.equals(browser.getBrowserType())) {
             device.print("<style type=\"text/css\" media=\"screen\">\n");
             device.print("body{behavior:url(../csshover.htc);}\n");
             device.print("</style>\n");
         }
         
 
 
         // TODO: move this to a dynamic script resource
         SToolTipManager toolTipManager = component.getSession().getToolTipManager();
         device
                 .print("<script language=\"JavaScript\" type=\"text/javascript\">\n")
                 .print("domTT_addPredefined('default', 'caption', false");
         if (toolTipManager.isFollowMouse())
             device.print(", 'trail', true");
         device.print(", 'delay', ").print(toolTipManager.getInitialDelay());
         device.print(", 'lifetime', ").print(toolTipManager.getDismissDelay());
         device
                 .print(");\n")
                 .print("</script>\n");
         
         device.print("</head>\n");
         device.print("<body");
         Utils.optAttribute(device, "id", frame.getName());
         Utils.optAttribute(device, "class", frame.getStyle());
         Utils.writeEvents(device, frame);
         device.print(">\n");
         if (frame.isVisible()) {
             frame.getLayout().write(device);
         }
 
         device.print("</body></html>\n");
         _c.fireRenderEvent(SComponent.DONE_RENDERING);
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
 
     public void setRenderXmlDeclaration(Boolean renderXmlDeclaration) {
         this.renderXmlDeclaration = renderXmlDeclaration;
     }
 
     public CSSSelector mapSelector(CSSSelector selector) {
         return selector;
     }
 }
