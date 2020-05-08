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
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.servlet.ServletRequest; // for javadoc
 import javax.servlet.jsp.JspException;
 
 import org.apache.portals.bridges.struts.PortletServlet;
 import org.apache.portals.bridges.struts.config.PortletURLTypes; // javadoc
 import org.apache.struts.taglib.html.LinkTag;
 
 /** Supports the Struts html:link tag to be used within uPortlet context for
  * generating urls for the bedework portlet.
  *
  * @author <a href="mailto:ate@douma.nu">Ate Douma</a>
  * @author <a href="mailto:satish@mun.ca">Satish Sekharan</a>
  * @author Mike Douglass    douglm at rpi.edu
  * @version $Id: RewriteTag.java 2005-10-25 12:31:13Z satish $
  */
 public class CalLinkTag extends LinkTag {
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
   protected String calculateURL() throws JspException {
     String urlStr = super.calculateURL();
 
     if (!PortletServlet.isPortletRequest(pageContext.getRequest())) {
       return urlStr;
     }
 
     try {
       URL url = new URL(urlStr);
 
       String path = url.getPath();
       if (path.endsWith(".rdo")) {
         setRenderURL("true");
       } else if (path.endsWith(".do")) {
         setActionURL("true");
       }
 
       /* We want a context relative url */
       urlStr = url.getFile();
 
       //System.out.println("LLLLLLLLLLLLLLLLLLUrlStr = " + urlStr);
 
       /* Drop the context
        */
       int pos = urlStr.indexOf('/');
       if (pos > 0) {
         urlStr = urlStr.substring(pos);
       }
 
       urlStr = TagsSupport.getURL(pageContext, urlStr, urlType);
 
       /* remove embedded anchor because calendar xsl stylesheet
        * adds extra parameters later during transformation
        */
       pos = urlStr.indexOf('#');
       if (pos > -1) {
         urlStr = urlStr.substring(0, pos);
       }
 
       /* Remove bedework dummy request parameter -
        * it's an encoded form of ?b=de */
       urlStr = urlStr.replaceAll(bedeworkDummyPar, "");
 
       //Generate valid xml markup for transformationthrow new
       urlStr = urlStr.replaceAll("&", "&amp;");
 
       //System.out.println("LLLLLLLLLLLLLLLLLLLLLLLUrlStr = " + urlStr);
     } catch (MalformedURLException mue) {
       throw new JspException(mue);
     }
 
     return urlStr;
   }
 
   public void release() {
     super.release();
     urlType = null;
   }
 }
