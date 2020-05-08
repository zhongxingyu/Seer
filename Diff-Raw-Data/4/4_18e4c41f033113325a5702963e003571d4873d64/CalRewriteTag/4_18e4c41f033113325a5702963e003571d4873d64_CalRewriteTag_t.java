 /*
  * Copyright 2000-2004 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ca.mun.portal.strutsbridge.taglib;
 
 import javax.servlet.ServletRequest; // for javadoc
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.BodyContent;
 
 import org.apache.portals.bridges.struts.PortletServlet;
 import org.apache.portals.bridges.struts.config.PortletURLTypes; // javadoc
 import org.apache.struts.taglib.html.RewriteTag;
 import org.apache.struts.taglib.TagUtils;
 
 /** Supports the Struts html:rewrite tag to be used within uPortlet context for
  * generating urls for the bedework portlet.
  *
  * @author <a href="mailto:ate@douma.nu">Ate Douma</a>
  * @author <a href="mailto:satish@mun.ca">Satish Sekharan</a>
  * @version $Id: RewriteTag.java 2005-10-25 12:31:13Z satish $
  */
 public class CalRewriteTag extends RewriteTag {
   /** Indicates which type of a url must be generated: action, render or resource.
    * <p>If not specified, the type will be determined by
    * {@link PortletURLTypes#getType(String)}</p>.
    */
   protected PortletURLTypes.URLType urlType = null;
 
   /**
    * @return String
    */
   public String getActionURL() {
     if (urlType != null &&
         urlType.equals(PortletURLTypes.URLType.ACTION)) {
       return "true";
     }
 
     return "false";
   }
 
   /** Render an ActionURL when set to "true"
    *
    * @param value "true" renders an ActionURL
    */
   public void setActionURL(String value) {
     if (value != null &&
         value.equalsIgnoreCase("true")) {
       urlType = PortletURLTypes.URLType.ACTION;
     } else {
       urlType = null;
     }
   }
 
   /**
    * @return String
    */
   public String getRenderURL() {
     if (urlType != null &&
         urlType.equals(PortletURLTypes.URLType.RENDER)) {
       return "true";
     }
 
     return "false";
   }
 
   /** Render a RenderURL when set to "true"
    *
    * @param value "true" renders a RenderURL
    */
   public void setRenderURL(String value) {
     if (value != null &&
         value.equalsIgnoreCase("true")) {
       urlType = PortletURLTypes.URLType.RENDER;
     } else {
       urlType = null;
     }
   }
 
   /**
    * @return String
    */
   public String getResourceURL() {
     if (urlType != null &&
         urlType.equals(PortletURLTypes.URLType.RESOURCE)) {
       return "true";
     }
 
     return "false";
   }
 
   /**
    * Render a ResourceURL when set to "true"
    * @param value "true" renders a ResourceURL
    */
   public void setResourceURL(String value) {
     if (value != null &&
         value.equalsIgnoreCase("true")) {
       urlType = PortletURLTypes.URLType.RESOURCE;
     } else {
       urlType = null;
     }
   }
 
  /* bedework dummy request parameter - it's an encoded form of ?b=de */
  private static final String bedeworkDummyPar = "%3Fb%3Dde";
 
   /** Generates a PortletURL or a ResourceURL for the link when in the context of a
    * {@link PortletServlet#isPortletRequest(ServletRequest) PortletRequest}, otherwise
    * the default behaviour is maintained.
    *
    * @return the link url
    * @exception JspException if a JSP exception has occurred
    */
   public int doStartTag() throws JspException {
     if ( PortletServlet.isPortletRequest(pageContext.getRequest())) {
       String url = null;
       BodyContent bodyContent = pageContext.pushBody();
 
       try {
         super.doStartTag();
         url = bodyContent.getString();
 
         /* replace with a relative URL
          * (quick and dirty fix for now - change this later)
          */
         url = url.replaceFirst("http://(.*)/","/");
         url = url.replaceFirst("https://(.*)/","/");
 
         url = TagsSupport.getURL(pageContext, url, urlType);
 
         /* remove embedded anchor because calendar xsl stylesheet
          * adds extra parameters later during transformation
          */
         int hash = url.indexOf('#');
         if ( hash > -1 ) {
           url = url.substring(0,hash);
         }
 
         /* Remove bedework dummy request parameter -
          * it's an encoded form of ?be=d */
         url = url.replaceAll(bedeworkDummyPar, "");
 
         //Generate valid xml markup for transformation
         url = url.replaceAll("&","&amp;");
 
       } finally {
         pageContext.popBody();
       }
 
       TagUtils.getInstance().write(pageContext, url);
       return (SKIP_BODY);
     } else {
       return super.doStartTag();
     }
   }
 
   public void release() {
     super.release();
     urlType = null;
   }
 }
