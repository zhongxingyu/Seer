 /*
  * Copyright (c) 2006 Oliver Stewart.  All Rights Reserved.
  *
  * This file is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2, or (at your option)
  * any later version.
  *
  * This file is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 package com.trailmagic.image.ui;
 
import org.apache.taglibs.standard.tag.common.core.Util;

 import java.io.IOException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.TagSupport;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 /*
 import javax.servlet.jsp.el.ExpressionEvaluator;
 import javax.servlet.jsp.el.VariableResolver;
 */
 import com.trailmagic.user.*;
 import com.trailmagic.image.*;
 
 public class ImageTag extends TagSupport {
     private Image m_image;
     private String m_sizeLabel;
     private String m_alt;
     private String m_cssClass;
 
     private static final String USER_ATTR = "user";
     private static final String DEFAULT_LABEL_ATTR = "defaultLabel";
     private static final String SIZE_ATTR = "size";
     private static final String LABEL_ATTR = "label";
 
     public int doStartTag() throws JspException {
         StringBuffer html = new StringBuffer();
         ImageManifestation mf;
 
         try {
             HttpServletRequest req =
                 (HttpServletRequest)pageContext.getRequest();
             // XXX: maybe this should be a parameter of the tag instead?
 
             // XXX: this is sort of a kludge for setting a default label
             String defaultLabel = req.getParameter(DEFAULT_LABEL_ATTR);
             HttpSession session = req.getSession();
             if ( defaultLabel != null ) {
                 session.setAttribute(DEFAULT_LABEL_ATTR, defaultLabel);
             }
 
             // XXX: end kludge
 
             String size = req.getParameter(SIZE_ATTR);
             if (size != null) {
                 mf = WebSupport.getMFBySize(m_image, Integer.parseInt(size));
             } else {
                 // get label by precedence: req param, tag spec, sess attr
                 String label = req.getParameter(LABEL_ATTR);
                 if ( label == null ) {
                     label = m_sizeLabel;
                 }
                 if ( label == null ) {
                     label = (String)session.getAttribute(DEFAULT_LABEL_ATTR);
                 }
                 if (label != null) {
                     mf = WebSupport.getMFByLabel(m_image, label);
                 } else {
                     mf = WebSupport.getDefaultMF((User)pageContext
                                                  .findAttribute(USER_ATTR),
                                                  m_image);
                 }
             }
 
             if ( mf != null ) {
                 // XXX: resume kludge
                 pageContext.setAttribute("currentLabel", getLabel(mf));
             // XXX: end kludge
                 // XXX: end kludge
                 html.append("<img src=\"");
 
                 //XXX: yeek?
                 /*
                 LinkHelper helper =
                     new LinkHelper((HttpServletRequest)pageContext.getRequest());
                 */
                 WebApplicationContext ctx =
                     WebApplicationContextUtils
                     .getRequiredWebApplicationContext(pageContext
                                                       .getServletContext());
                 LinkHelper helper =
                     (LinkHelper)ctx.getBean("linkHelper");
                 // XXX: check for null bean
                 // XXX: evil cast?
                 helper.setRequest((HttpServletRequest)
                                   pageContext.getRequest());
 
                 html.append(helper.getImageMFUrl(mf));
                 html.append("\" height=\"");
                 html.append(mf.getHeight());
                 html.append("\" width=\"");
                 html.append(mf.getWidth());
                 html.append("\" alt=\"");
                 if (m_alt != null) {
                     html.append(m_alt);
                 } else if (m_image.getCaption() != null) {
                    html.append(Util.escapeXml(m_image.getCaption()));
                 } else {
                    html.append(Util.escapeXml(m_image.getDisplayName()));
                 }
 
                 html.append("\" class=\"");
                 if (mf.getWidth() > mf.getHeight()) {
                     html.append("landscape");
                 } else {
                     html.append("portrait");
                 }
                 if (m_cssClass != null) {
                     html.append(" ");
                     html.append(m_cssClass);
                 }
 
                 html.append("\"/>");
             } else {
                 html.append("No manifestations found for the specified " +
                             "image.");
             }
             pageContext.getOut().write(html.toString());
             return SKIP_BODY;
         } catch (IOException e) {
             throw new JspException(e);
         }
     }
     public void setImage(Image image) {
         m_image = image;
     }
 
     public Image getImage() {
         return m_image;
     }
 
     public void setSizeLabel(String label) {
         m_sizeLabel = label;
     }
 
     public String getSizeLabel() {
         return m_sizeLabel;
     }
 
     public void setAlt(String alt) {
         m_alt = alt;
     }
 
     public String getAlt() {
         return m_alt;
     }
 
     public void setCssClass(String cssClass) {
         m_cssClass = cssClass;
     }
 
     public String getCssClass() {
         return m_cssClass;
     }
 
     private String getLabel(ImageManifestation mf) {
         int area = mf.getArea();
         int distance = Integer.MAX_VALUE;
         int newDistance;
         String label = "small";
 
         newDistance = Math.abs(area - 192*128);
         if ( newDistance < distance ) {
             label = "thumbnail";
             distance = newDistance;
         }
         newDistance = Math.abs(area - 384*256);
         if ( newDistance  < distance ) {
             label = "small";
             distance = newDistance;
         }
         newDistance = Math.abs(area - 768*512);
         if ( newDistance < distance ) {
             label = "medium";
             distance = newDistance;
         }
         newDistance = Math.abs(area - 1536*1024);
         if (newDistance < distance ) {
             label = "large";
             distance = newDistance;
         }
         newDistance = Math.abs(area - 3072*2048);
         if ( newDistance < distance ) {
             label = "huge";
         }
         return label;
     }
 }
