 /*******************************************************************************
  * $URL$
  * $Id$
  * **********************************************************************************
  *
  *  Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
  *
  *  Licensed under the Educational Community License, Version 1.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.opensource.org/licenses/ecl1.php
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  ******************************************************************************/
 
 package org.sakaiproject.metaobj.shared.control;
 
 import org.springframework.web.servlet.view.xslt.AbstractXsltView;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.util.NestedServletException;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ObjectError;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.BindingResultUtils;
 import org.jdom.transform.JDOMSource;
 import org.jdom.Element;
 import org.jdom.Document;
 import org.sakaiproject.content.api.ResourceEditingHelper;
 import org.sakaiproject.content.api.ResourceToolAction;
 import org.sakaiproject.metaobj.shared.mgt.StructuredArtifactDefinitionManager;
 import org.sakaiproject.metaobj.shared.model.Artifact;
 import org.sakaiproject.metaobj.shared.model.ElementBean;
 import org.sakaiproject.metaobj.shared.FormHelper;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.tool.api.ToolSession;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.util.ResourceLoader;
 import org.sakaiproject.util.Web;
 
 import javax.xml.transform.*;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 import java.io.InputStream;
 
 /**
  * Created by IntelliJ IDEA.
  * User: johnellis
  * Date: Oct 30, 2006
  * Time: 10:10:42 AM
  * To change this template use File | Settings | File Templates.
  */
 public class XsltArtifactView extends AbstractXsltView {
 
    private ResourceLoader resourceLoader = new ResourceLoader();
    private String bundleLocation;
    private static final String STYLESHEET_PARAMS =
       "org.sakaiproject.metaobj.shared.control.XsltArtifactView.paramsMap";
    private static final String STYLESHEET_LOCATION =
       "org.sakaiproject.metaobj.shared.control.XsltArtifactView.stylesheetLocation";
    private String uriResolverBeanName;
    private URIResolver uriResolver;
    private boolean readOnly;
 
    protected Source createXsltSource(Map map, String string, HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse) throws Exception {
 
       httpServletResponse.setContentType(getContentType());
       WebApplicationContext context = getWebApplicationContext();
       setUriResolver((URIResolver)context.getBean(uriResolverBeanName));
 
       ToolSession toolSession = SessionManager.getCurrentToolSession();
 
       String homeType;
 
       ElementBean bean = (ElementBean) map.get("bean");
 
       Element root;
       Map paramsMap = new Hashtable();
       httpServletRequest.setAttribute(STYLESHEET_PARAMS, paramsMap);
       if (toolSession.getAttribute(FormHelper.PREVIEW_HOME_TAG) != null) {
          paramsMap.put("preview", "true");
       }
 
       if (toolSession.getAttribute(ResourceToolAction.ACTION_PIPE) != null) {
          paramsMap.put("fromResources", "true");
       }
 
       boolean edit = false;
 
       if (bean instanceof Artifact) {
          root = getStructuredArtifactDefinitionManager().createFormViewXml(
             (Artifact) bean, null);
          homeType = getHomeType((Artifact) bean);
          edit = ((Artifact)bean).getId() != null;
       }
       else {
          EditedArtifactStorage sessionBean = (EditedArtifactStorage)httpServletRequest.getSession().getAttribute(
             EditedArtifactStorage.EDITED_ARTIFACT_STORAGE_SESSION_KEY);
 
          root = getStructuredArtifactDefinitionManager().createFormViewXml(
             (Artifact) sessionBean.getRootArtifact(), null);
 
          replaceNodes(root, bean, sessionBean);
          paramsMap.put("subForm", "true");
          homeType = getHomeType(sessionBean.getRootArtifact());
          edit = sessionBean.getRootArtifact().getId() != null;
       }
 
       if (edit) {
          paramsMap.put("edit", "true");
       }
 
       httpServletRequest.setAttribute(STYLESHEET_LOCATION,
          getStructuredArtifactDefinitionManager().getTransformer(homeType, readOnly));
 
       Errors errors = BindingResultUtils.getBindingResult(map, "bean");
       if (errors != null && errors.hasErrors()) {
          Element errorsElement = new Element("errors");
 
          List errorsList = errors.getAllErrors();
 
          for (Iterator i=errorsList.iterator();i.hasNext();) {
             Element errorElement = new Element("error");
             ObjectError error = (ObjectError) i.next();
             if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                errorElement.setAttribute("field", fieldError.getField());
                Element rejectedValue = new Element("rejectedValue");
               if (fieldError.getRejectedValue() != null) {
                  rejectedValue.addContent(fieldError.getRejectedValue().toString());
               }
                errorElement.addContent(rejectedValue);
             }
             Element message = new Element("message");
             message.addContent(context.getMessage(error, getResourceLoader().getLocale()));
             errorElement.addContent(message);
             errorsElement.addContent(errorElement);
          }
 
          root.addContent(errorsElement);
       }
 
       if (httpServletRequest.getParameter("success") != null) {
          Element success = new Element("success");
          success.setAttribute("messageKey", httpServletRequest.getParameter("success"));
          root.addContent(success);
       }
 
       if (toolSession.getAttribute(ResourceEditingHelper.CUSTOM_CSS) != null) {
          Element uri = new Element("uri");
          uri.setText((String) toolSession.getAttribute(ResourceEditingHelper.CUSTOM_CSS));
          root.getChild("css").addContent(uri);
          uri.setAttribute("order", "100");
       }
 
       if (toolSession.getAttribute(FormHelper.FORM_STYLES) != null) {
          List styles = (List) toolSession.getAttribute(FormHelper.FORM_STYLES);
          int index = 101;
          for (Iterator<String> i=styles.iterator();i.hasNext();) {
             Element uri = new Element("uri");
             uri.setText(i.next());
             root.getChild("css").addContent(uri);
             uri.setAttribute("order", "" + index);
             index++;
          }
       }
 
       Document doc = new Document(root);
       return new JDOMSource(doc);
    }
 
    protected String getHomeType(Artifact bean) {
       if (bean.getHome().getType() == null) {
          return "new bean";
       }
       else if (bean.getHome().getType().getId() == null) {
          return "new bean";
       }
       return bean.getHome().getType().getId().getValue();
    }
 
    protected void replaceNodes(Element root, ElementBean bean, EditedArtifactStorage sessionBean) {
       Element structuredData = root.getChild("formData").getChild("artifact").getChild("structuredData");
       structuredData.removeContent();
       structuredData.addContent((Element)bean.getBaseElement().clone());
 
       Element schema = root.getChild("formData").getChild("artifact").getChild("schema");
       Element schemaRoot = schema.getChild("element");
       StringTokenizer st = new StringTokenizer(sessionBean.getCurrentPath(), "/");
       Element newRoot = schemaRoot;
 
       while (st.hasMoreTokens()) {
          String schemaName = st.nextToken();
          List children = newRoot.getChild("children").getChildren("element");
          for (Iterator i=children.iterator();i.hasNext();) {
             Element schemaElement = (Element) i.next();
             if (schemaName.equals(schemaElement.getAttributeValue("name"))) {
                newRoot = schemaElement;
                break;
             }
          }
       }
 
       schema.removeChild("element");
       schema.addContent(newRoot.detach());
    }
 
    protected Map getParameters(HttpServletRequest request) {
       Map params = super.getParameters(request);
 
       if (params == null) {
          params = new Hashtable();
       }
 
       if (ToolManager.getCurrentPlacement() != null) {
          params.put("panelId", Web.escapeJavascript("Main" + ToolManager.getCurrentPlacement().getId()));
       }
 
       params.putAll((Map) request.getAttribute(STYLESHEET_PARAMS));
 
       params.put(STYLESHEET_LOCATION, request.getAttribute(STYLESHEET_LOCATION));
       return params;
    }
 
    /**
     * Perform the actual transformation, writing to the given result.
     * @param source the Source to transform
     * @param parameters a Map of parameters to be applied to the stylesheet
     * @param result the result to write to
     * @throws Exception we let this method throw any exception; the
     * AbstractXlstView superclass will catch exceptions
     */
    protected void doTransform(Source source, Map parameters, Result result, String encoding)
          throws Exception {
 
       InputStream stylesheetLocation = (InputStream) parameters.get(STYLESHEET_LOCATION);
       try {
 
          Transformer trans = getTransformer(stylesheetLocation);
 
          // Explicitly apply URIResolver to every created Transformer.
          if (getUriResolver() != null) {
             trans.setURIResolver(getUriResolver());
          }
 
          // Apply any subclass supplied parameters to the transformer.
          if (parameters != null) {
             for (Iterator it = parameters.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                trans.setParameter(entry.getKey().toString(), entry.getValue());
             }
             if (logger.isDebugEnabled()) {
                logger.debug("Added parameters [" + parameters + "] to transformer object");
             }
          }
 
          // Specify default output properties.
          trans.setOutputProperty(OutputKeys.ENCODING, encoding);
          trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
          // Xalan-specific, but won't do any harm in other XSLT engines.
          trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 
          // Perform the actual XSLT transformation.
          trans.transform(source, result);
          if (logger.isDebugEnabled()) {
             logger.debug("XSLT transformed with stylesheet [" + stylesheetLocation + "]");
          }
       }
       catch (TransformerConfigurationException ex) {
          throw new NestedServletException("Couldn't create XSLT transformer for stylesheet [" +
                stylesheetLocation + "] in XSLT view with name [" + getBeanName() + "]", ex);
       }
       catch (TransformerException ex) {
          throw new NestedServletException("Couldn't perform transform with stylesheet [" +
                stylesheetLocation + "] in XSLT view with name [" + getBeanName() + "]", ex);
       }
    }
 
    protected Transformer getTransformer(InputStream transformer) throws TransformerException {
       return getTransformerFactory().newTransformer(new StreamSource(transformer));
    }
 
    protected StructuredArtifactDefinitionManager getStructuredArtifactDefinitionManager() {
       return (StructuredArtifactDefinitionManager)
          ComponentManager.get("structuredArtifactDefinitionManager");
    }
 
    public String getBundleLocation() {
       return bundleLocation;
    }
 
    public void setBundleLocation(String bundleLocation) {
       this.bundleLocation = bundleLocation;
       setResourceLoader(new ResourceLoader(bundleLocation));
    }
 
    public ResourceLoader getResourceLoader() {
       return resourceLoader;
    }
 
    public void setResourceLoader(ResourceLoader resourceLoader) {
       this.resourceLoader = resourceLoader;
    }
 
    public String getUriResolverBeanName() {
       return uriResolverBeanName;
    }
 
    public void setUriResolverBeanName(String uriResolverBeanName) {
       this.uriResolverBeanName = uriResolverBeanName;
    }
 
    public URIResolver getUriResolver() {
       return uriResolver;
    }
 
    public void setUriResolver(URIResolver uriResolver) {
       this.uriResolver = uriResolver;
       getTransformerFactory().setURIResolver(uriResolver);
    }
 
    public boolean isReadOnly() {
       return readOnly;
    }
 
    public void setReadOnly(boolean readOnly) {
       this.readOnly = readOnly;
    }
 
    protected void dumpDocument(Element node) {
 	   try {
 			Transformer transformer = TransformerFactory.newInstance().newTransformer();
 			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
 			transformer.transform( new JDOMSource(node), new StreamResult(System.out) );
 		}
 		catch ( Exception e )
 		{
 		   e.printStackTrace();
 		}
    }
 
 }
