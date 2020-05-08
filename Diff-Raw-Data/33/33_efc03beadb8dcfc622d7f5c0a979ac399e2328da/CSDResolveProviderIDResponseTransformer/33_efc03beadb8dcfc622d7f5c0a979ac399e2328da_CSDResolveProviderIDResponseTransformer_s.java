 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
 package org.jembi.rhea.transformers;
 
 import java.io.IOException;
 import java.io.StringReader;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jembi.rhea.orchestration.exceptions.ProviderValidationException;
 import org.mule.api.MuleMessage;
 import org.mule.api.transformer.TransformerException;
 import org.mule.api.transport.PropertyScope;
 import org.mule.transformer.AbstractMessageTransformer;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class CSDResolveProviderIDResponseTransformer extends AbstractMessageTransformer {
 	
 	Log log = LogFactory.getLog(getClass());
 
 
 	@Override
 	public Object transformMessage(MuleMessage msg, String enc)
 			throws TransformerException {
 
 		String success = "false";
 
 		try {
 			String response = (String) msg.getPayload();
 			String targetIdTypeOID = msg.getProperty(CSDResolveProviderIDOffRampTransformer.SESSIONVAR_PROVIDER_TARGETIDTYPE, PropertyScope.SESSION);
 			
 			String targetId = processResponse(targetIdTypeOID, response);
 			success = "true";
 			return targetId;
 		} catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException ex) {
 			throw new TransformerException(this, ex);
 		} catch (ProviderValidationException ex) {
 			log.info("No valid provider id in response", ex);
 			return null;
 		} finally {
 			msg.setProperty("success", success, PropertyScope.OUTBOUND);
 		}
 	}
 	
 	
 	private String processResponse(String targetIdOID, String response) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, ProviderValidationException {
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(response)));
         XPath xpath = XPathFactory.newInstance().newXPath();
         
         NodeList nodes = (NodeList) xpath.evaluate("/CSD/providerDirectory/provider", doc, XPathConstants.NODESET);
         if (nodes==null || nodes.getLength()==0)
         	throw new ProviderValidationException("No providers found in registry for requested identifier");
 
         String id = findIdInNodeList(targetIdOID, nodes);
 
         if (id==null || id.isEmpty())
         	throw new ProviderValidationException("No identifiers found for identifier domain '" + targetIdOID + "'");
 
 		return id;
 	}
 	
 	private String findIdInNodeList(String targetIdOID, NodeList nodes) {
 		String id = null;
 
 		//Find the first matching provider
 		for (int i=0; i<nodes.getLength(); i++) {
 			Node node = nodes.item(i);
 
			if (targetIdOID==null && node.hasAttributes()) {
 				id = ((Element)node).getAttribute("oid");
 				break;
 			} else if (node.hasChildNodes()) {
 				id = getOtherIDCode(targetIdOID, node);
 				if (id!=null)
 					break;
 			}
 		}
 
 		return id;
 	}
 	
 	private String getOtherIDCode(String targetIdOID, Node provider) {
 		String id = null;
 		NodeList children = provider.getChildNodes();
 
 		for (int i=0; i<children.getLength(); i++) {
 			Node child = children.item(i);
 			if ("otherID".equals(child.getNodeName()) && child.hasAttributes()) {
 				Node aaName = child.getAttributes().getNamedItem("assigningAuthorityName");
 				if (aaName!=null && targetIdOID.equals(aaName.getNodeValue())) {
 					Node code = child.getAttributes().getNamedItem("code");
 					id = code.getNodeValue();
 				}
 			}
 		}
 
 		return id;
 	}
 }
