 /**
  * Copyright 2009-2010 the original author or authors.
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
 package net.javacrumbs.springws.test.validator;
 
 import java.io.IOException;
 import java.net.URI;
 
 import javax.xml.transform.Source;
 
 import net.javacrumbs.springws.test.RequestProcessor;
 import net.javacrumbs.springws.test.WsTestException;
 import net.javacrumbs.springws.test.lookup.ResourceLookup;
 import net.javacrumbs.springws.test.util.DefaultXmlUtil;
 import net.javacrumbs.springws.test.util.XmlUtil;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.core.io.Resource;
 import org.springframework.util.Assert;
 import org.springframework.ws.WebServiceMessage;
 import org.springframework.ws.WebServiceMessageFactory;
 import org.w3c.dom.Document;
 
 public abstract class AbstractCompareRequestValidator implements InitializingBean, RequestProcessor{
 
 	private ResourceLookup controlResourceLookup;
 	protected final Log logger = LogFactory.getLog(getClass());
 	private XmlUtil xmlUtil = DefaultXmlUtil.getInstance();
 	
 	private boolean failIfControlResourceNotFound;
 
 	public AbstractCompareRequestValidator() {
 		super();
 	}
 
 	public WebServiceMessage processRequest(URI uri, WebServiceMessageFactory messageFactory,
 			WebServiceMessage request) throws IOException {
 		validateRequest(uri, request);
 		return null;
 	}
 	
 	/**
 	 * Loads controleResource and compares it with the message. If controlResource is not SOAP message, only payload is compared.
 	 * @param uri
 	 * @param message
 	 * @throws IOException
 	 */
 	protected void validateRequest(URI uri, WebServiceMessage message) throws IOException {
 	
		Resource controlResource = controlResourceLookup.lookupResource(uri, message);
 		if (controlResource!=null)
 		{
 			Document controlDocument = loadDocument(controlResource);
 			if (isSoap(controlDocument))
 			{
 				Document messageDocument = loadDocument(message);
 				compareDocuments(controlDocument, messageDocument);
 			}
 			else
 			{
 				Document payloadDocument = loadDocument(message.getPayloadSource());
 				compareDocuments(controlDocument, payloadDocument);
 			}
 		}
 		else
 		{
 			onControlResourceNotFound(uri, message);
 		}
 	}
 
 
 	/**
 	 * Called if control resource was not found.
 	 * @param uri
 	 * @param message
 	 */
 	protected void onControlResourceNotFound(URI uri, WebServiceMessage message) {
 		if (failIfControlResourceNotFound)
 		{
 			throw new WsTestException("Control resource not found for "+uri+" and "+xmlUtil.serializeDocument(message));
 		}
 		else
 		{
 			logger.warn("Can not find resource to validate with.");
 		}
 	}
 
 	/**
 	 * Does the actual comparison.
 	 * @param controlDocument
 	 * @param messageDocument
 	 */
 	protected abstract void compareDocuments(Document controlDocument, Document messageDocument);
 	
 	public void afterPropertiesSet(){
 		Assert.notNull(controlResourceLookup, "ControlResourceLookup has to be set");		
 	}
 
 	protected Document loadDocument(WebServiceMessage message) throws IOException {
 		return getXmlUtil().loadDocument(message);
 	}
 
 	protected Document loadDocument(Resource resource) throws IOException {
 		return getXmlUtil().loadDocument(resource);
 	}
 	
 	protected Document loadDocument(Source source) {
 		return getXmlUtil().loadDocument(source);
 	}
 	
 	protected String serializeDocument(Document document) {
 		return getXmlUtil().serializeDocument(document);
 	}
 
 	public ResourceLookup getControlResourceLookup() {
 		return controlResourceLookup;
 	}
 
 	public void setControlResourceLookup(ResourceLookup controlResourceLookup) {
 		this.controlResourceLookup = controlResourceLookup;
 	}
 
 	public XmlUtil getXmlUtil() {
 		return xmlUtil;
 	}
 
 	public void setXmlUtil(XmlUtil xmlUtil) {
 		this.xmlUtil = xmlUtil;
 	}
 
 	public boolean isFailIfControlResourceNotFound() {
 		return failIfControlResourceNotFound;
 	}
 
 	public void setFailIfControlResourceNotFound(boolean failIfControlResourceNotFound) {
 		this.failIfControlResourceNotFound = failIfControlResourceNotFound;
 	}
 
 	//TODO make protected
 	public boolean isSoap(Document document)
 	{
 		return xmlUtil.isSoap(document);
 	}
 }
