 package org.jboss.pressgangccms.utils.common;
 
 import java.io.ByteArrayInputStream;
 
 import javax.xml.XMLConstants;
 
 import org.w3c.dom.DOMConfiguration;
 import org.w3c.dom.DOMError;
 import org.w3c.dom.DOMErrorHandler;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.bootstrap.DOMImplementationRegistry;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSInput;
 import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
 
 /**
  * This class is used to validate XML, optionally also validating the XML against a DDT schema
  */
 public class XMLValidator implements DOMErrorHandler, LSResourceResolver
 {
 	protected boolean errorsDetected;
 	private String errorText;
 	private String dtdFileName;
 	private byte[] dtdData;
 	final private boolean showErrors;
 
 	public XMLValidator(final boolean showErrors)
 	{
 		this.showErrors = showErrors;
 	}
 
 	public XMLValidator()
 	{
 		this.showErrors = false;
 	}
 
 	public Document validateTopicXML(final String xml)
 	{
 		return validateTopicXML(xml, null, null);
 	}
 
 	public Document validateTopicXML(final String xml, final String dtdFileName, final byte[] dtdData)
 	{
 		try
 		{
 			return validateTopicXML(XMLUtilities.convertStringToDocument(xml), dtdFileName, dtdData);
 		}
		catch (final SAXException ex)
		{
			this.errorText = ex.getMessage();
		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 		}
 
 		return null;
 	}
 
 	public Document validateTopicXML(final Document core)
 	{
 		return validateTopicXML(core, null, null);
 	}
 
 	public Document validateTopicXML(final Document core, final String dtdFileName, final byte[] dtdData)
 	{
 		if (core == null)
 			return null;
 
 		try
 		{
 			errorsDetected = false;
 
 			if (dtdFileName != null && dtdData != null)
 			{
 				this.dtdFileName = dtdFileName;
 				this.dtdData = dtdData;
 
 				final DOMConfiguration config = core.getDomConfig();
 				config.setParameter("schema-type", XMLConstants.XML_DTD_NS_URI);
 				config.setParameter("schema-location", dtdFileName);
 				config.setParameter("resource-resolver", this);
 				config.setParameter("error-handler", this);
 				config.setParameter("validate", Boolean.TRUE);
 				config.setParameter("namespaces", Boolean.FALSE);
 				core.normalizeDocument();
 			}
 			else
 			{
 				this.dtdFileName = null;
 				this.dtdData = null;
 			}
 
 			if (this.errorsDetected)
 				return null;
 
 			// all topics have to be sections
 			final Node rootNode = core.getDocumentElement();
 			final String documentNodeName = rootNode.getNodeName();
 			if (!documentNodeName.equals(DocBookUtilities.TOPIC_ROOT_NODE_NAME))
 				return null;
 
 			return core;
 
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 
 			/*
 			 * something happened, which we will assume means the xml is invalid
 			 */
 			return null;
 		}
 	}
 
 	public boolean handleError(final DOMError error)
 	{
 		errorsDetected = true;
 		errorText = error.getMessage();
 		if (showErrors)
 			System.out.println("XMLValidator.handleError() " + errorText);
 		return true;
 	}
 
 	public LSInput resolveResource(final String type, final String namespace, final String publicId, final String systemId, final String baseURI)
 	{
 		try
 		{
 			final DOMImplementationLS impl = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
 			final LSInput dtdsource = impl.createLSInput();
 			if (systemId.equals(this.dtdFileName))
 			{
 				dtdsource.setByteStream(new ByteArrayInputStream(this.dtdData));
 				return dtdsource;
 			}
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 		}
 
 		return null;
 	}
 
 	public String getErrorText()
 	{
 		return errorText;
 	}
 
 	public void setErrorText(String errorText)
 	{
 		this.errorText = errorText;
 	}
 }
