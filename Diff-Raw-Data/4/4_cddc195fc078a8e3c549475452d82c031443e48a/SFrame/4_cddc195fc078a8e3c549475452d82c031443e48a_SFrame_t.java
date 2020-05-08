 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings;
 
 import java.awt.Color;
 import java.beans.*;
 import java.io.IOException;
 import java.net.URL;
 import java.util.*;
 import java.util.logging.*;
 
 import org.wings.*;
 import org.wings.header.*;
 import org.wings.io.Device;
 import org.wings.plaf.*;
 import org.wings.style.StyleSheet;
 import org.wings.session.*;
 import org.wings.util.*;
 
 /**
  * The frame is the root component in every component hierarchie.
  * A SessionServlet requires an instance of SFrame to render the page.
  * SFrame consists of some header informaton (meta, link, script)
  * and a stack of components. The bottommost component of the stack is always
  * the contentPane. When dialogs are to be shown, they are stacked on top of
  * it.
  *
  * @author <a href="mailto:hengels@mercatis.de">Holger Engels</a>,
  *         <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SFrame
     extends SRootContainer
     implements PropertyChangeListener
 {
     /**
      * @see #getCGClassID
      */
     private static final String cgClassID = "FrameCG";
 
     /**
      *  The Title of the Frame.
      */
     protected String title;
 
     protected String baseTarget = null;
 
     /**
      * A Set containing additional tags for the html header.
      */
     protected List headers;
 
 
     // do not initialize with null
     private Color textColor;
     private Color linkColor;
     private Color vLinkColor;
     private Color aLinkColor;
 
     /**
      * TODO: documentation
      */
     protected boolean resizable = true;
 
     /** the style sheet used in certain look and feels. */
     protected StyleSheet styleSheet;  // IMPORTANT: initialization with null causes errors; what errors ?
     // These: all properties, that are installed by the plaf, are installed during the initialization of
     // SComponent. The null initializations happen afterwards and overwrite the plaf installed values.
     // However: null is the default initialization value, so this is not a problem!
     // The same applies to all descendants of SComponent!
 
     /**
      * TODO: documentation
      */
     protected String statusLine;
 
     private RequestURL requestURL = null;
     private String targetResource;
 
     private HashMap dynamicResources;
 
     /**
      * TODO: documentation
      *
      */
     public SFrame() {
         getSession().addPropertyChangeListener("lookAndFeel", this);
         getSession().addPropertyChangeListener("request.url", this);
     }
 
     /**
      * TODO: documentation
      *
      * @param title
      */
     public SFrame(String title) {
         this();
         setTitle(title);
     }
 
     /**
      * TODO: documentation
      *
      */
     public void addDynamicResource(DynamicResource d) {
         if (dynamicResources == null) {
             dynamicResources = new HashMap();
         }
         dynamicResources.put(d.getClass(), d);
     }
 
     /**
      * TODO: documentation
      *
      */
     public DynamicResource getDynamicResource(Class c) {
         if (dynamicResources == null) {
             dynamicResources = new HashMap();
         }
         return (DynamicResource) dynamicResources.get(c);
     }
 
     /**
      * TODO: documentation
      *
      * @param b
      */
     public void setResizable(boolean b) {
         resizable = b;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public boolean isResizable() {
         return resizable;
     }
 
     /**
      * Return <code>this</code>.
      *
      * @return this.
      */
     public SFrame getParentFrame() {
         return this;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public String getEventEpoch() {
         return getDynamicResource(DynamicCodeResource.class).getEpoch();
     }
 
     /**
      * Set server address.
      */
     public final void setRequestURL(RequestURL requestURL) {
         this.requestURL = requestURL;
     }
 
     /**
      * Returns the base URL for a request to the WingsServlet. This URL
      * is used to assemble an URL that trigger events. In order to be used
      * for this purpose, you've to add your parameters here.
      */
     public final RequestURL getRequestURL() {
         RequestURL result = null;
         // first time we are called, and we didn't get any change yet
         if (requestURL == null) {
             requestURL =(RequestURL)SessionManager.getSession().getProperty("request.url");
         }
         if (requestURL != null) {
             result = (RequestURL)requestURL.clone();
             result.setResource(getTargetResource());
         }
         return result;
     }
 
     /**
      * Set the target resource
      */
     public void setTargetResource(String targetResource) {
         this.targetResource = targetResource;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public String getTargetResource() {
         if (targetResource == null) {
             targetResource = getDynamicResource(DynamicCodeResource.class).getId();
         }
         return targetResource;
     }
 
     /**
      * Set the base target. This is the target of any link pressed.
      */
     public void setBaseTarget(String baseTarget) {
         this.baseTarget = baseTarget;
     }
     
     /**
      * set the base target frame. This frame will receive all klicks
      * in this frame. Usually you want to use this for the ReloadManager
      * frame.
      */
     public void setBaseTarget(SFrame otherFrame) {
         /*
          * this knows, that the frames are usually named "frame" + ID
          */
         setBaseTarget("frame" + otherFrame.getComponentId());
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public String getBaseTarget() {
         return baseTarget;
     }
 
     public void addHeader(Object m) {
        if (!headers().contains(m))
            headers.add(m);
     }
 
     public void clearHeaders() {
 	headers().clear();
     }
     
     public List headers() {
         if (headers == null)
             headers = new ArrayList(2);
 	return headers;
     }
 
     /**
      * TODO: documentation
      *
      * @param t
      */
     public void setTitle(String title) {
         this.title = title;
     }
     /**
      * TODO: documentation
      *
      * @return
      */
     public String getTitle() { return title; }
 
     /**
      * TODO: documentation
      *
      * @param c
      */
     public void setTextColor(Color c) {
         textColor = c;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public Color getTextColor() {
         return textColor;
     }
 
     /**
      * TODO: documentation
      *
      * @param c
      */
     public void setLinkColor(Color c) {
         linkColor = c;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public Color getLinkColor() {
         return linkColor;
     }
 
     /**
      * TODO: documentation
      *
      * @param c
      */
     public void setVLinkColor(Color c) {
         vLinkColor = c;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public Color getVLinkColor() {
         return vLinkColor;
     }
 
     /**
      * TODO: documentation
      *
      * @param c
      */
     public void setALinkColor(Color c) {
         aLinkColor = c;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public Color getALinkColor() {
         return aLinkColor;
     }
 
     /**
      * TODO: documentation
      *
      * @param s
      */
     public void setStyleSheet(StyleSheet s) {
         styleSheet = s;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public StyleSheet getStyleSheet() {
         return styleSheet;
     }
 
     /**
      * TODO: documentation
      *
      * @param s
      */
     public void setStatusLine(String s) {
         statusLine = s;
     }
 
     /**
      * TODO: documentation
      *
      * @deprecated don't use
      * @return
      */
     public String getStatusLine() {
         return statusLine;
     }
 
     public void show() {
         setVisible(true);
     }
 
     public void hide() {
         setVisible(false);
     }
 
     public void setVisible(boolean b) {
         if ( b ) {
             getSession().addFrame(this);
         } else {
             getSession().removeFrame(this);
         }
         super.setVisible(b);
     }
 
     public void propertyChange(PropertyChangeEvent pe) {
         if ("lookAndFeel".equals(pe.getPropertyName())) {
             updateComponentTreeCG(getContentPane());
         }
         if ("request.url".equals(pe.getPropertyName())) {
             setRequestURL((RequestURL)pe.getNewValue());
         }
     }
 
     private void updateComponentTreeCG(SComponent c) {
         if (c instanceof SComponent) {
             ((SComponent)c).updateCG();
         }
         if (c instanceof SContainer) {
             SComponent[] children = ((SContainer)c).getComponents();
             for(int i = 0; i < children.length; i++) {
                 updateComponentTreeCG(children[i]);
             }
         }
         updateCG();
     }
 
     public String getCGClassID() {
         return cgClassID;
     }
 
     private class SStackLayout extends SAbstractLayoutManager
     {
         private SContainer container = null;
 
         public SStackLayout() {}
 
         public void updateCG() {}
         public void addComponent(SComponent c, Object constraint, int index) {}
         public void removeComponent(SComponent c) {}
 
         public SComponent getComponentAt(int i) {
             return (SComponent)SFrame.this.getComponentAt(i);
         }
 
         public void setContainer(SContainer c) {
             container = c;
         }
 
         /**
          * Allways write code for the topmost component.
          *
          * @param s
          * @throws IOException
          */
         public void write(Device s)
             throws IOException
         {
             int topmost = container.getComponentCount() - 1;
             SComponent comp = (SComponent)SFrame.this.getComponentAt(topmost);
             comp.write(s);
         }
     }
 
     public void setCG(FrameCG cg) {
         super.setCG(cg);
     }
 
     public void invite(ComponentVisitor visitor)
         throws Exception
     {
         visitor.visit(this);
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
