 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.plugins.compression;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.eclipse.swordfish.core.SwordfishException;
 import org.eclipse.swordfish.internal.core.util.xml.XmlUtil;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 public class CompressorImpl implements Compressor {
 
 	private static final Log LOG = LogFactory.getLog(CompressorImpl.class);
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.swordfish.plugins.compression.Compressor#isSourceEmpty(javax.xml.transform.Source)
 	 */
 	public boolean isSourceEmpty(Source src) {
 		if (src == null) {
 			return true;
 		} else {
 			if (src instanceof DOMSource) {
 				Node node = ((DOMSource) src).getNode();
 				if (node == null) {
 					return true;
 				}
 				if (node instanceof Document && !node.hasChildNodes()) {
 					return true;
 				}
 			} else if (src instanceof StreamSource) {
 				InputStream is = ((StreamSource) src).getInputStream();
 				if (is == null) {
 					return true;
 				// should probably also check if the stream has data available
 				}
 			}
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.swordfish.plugins.compression.Compressor#asCompressedSource(javax.xml.transform.Source)
 	 */
 	public Source asCompressedSource(Source src, int sizeThreshold) {
 		try {
 			
			if(!isExceedSizeThreshold(src, sizeThreshold)){
 				return null;
 			}
 			
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			GZIPOutputStream zipped = new GZIPOutputStream(baos);
 			Result result = new StreamResult(zipped);
 			TransformerFactory tf = TransformerFactory.newInstance();
 			Transformer transformer = tf.newTransformer();
 			transformer.transform(src, result);
 			zipped.finish();
 			byte[] compressedBytes = baos.toByteArray();
 			String encoded = new String(new Base64().encode(compressedBytes));
 			Document doc = DocumentBuilderFactory.newInstance()
 				.newDocumentBuilder().newDocument();
 			Element wrapper = doc.createElementNS(CompressionConstants.COMPRESSION_NS,
 				CompressionConstants.COMPRESSION_ELEMENT_PREFIX + ":"
 				+ CompressionConstants.COMPRESSED_ELEMENT);
 			wrapper.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"
 				+ CompressionConstants.COMPRESSION_ELEMENT_PREFIX,
 				CompressionConstants.COMPRESSION_NS);
 			wrapper.appendChild(doc.createTextNode(encoded));
 			doc.appendChild(wrapper);
 			return new DOMSource(doc);
 		} catch (Exception e) {
 			throw new SwordfishException(e);
 		}
 	}
 	
 	private boolean isExceedSizeThreshold(Source src, int sizeThreshold) throws IOException {
 		if(sizeThreshold == 0) {
			return true;
 		}
 		int actualSize = 0;
 		if (src instanceof DOMSource) {
 			Node root = ((DOMSource)src).getNode();
 			final String s = XmlUtil.toStringFromDOM(root);
 			actualSize = s.length();
 		}
 		if (src instanceof StreamSource) {
 			InputStream is = ((StreamSource)src).getInputStream();
 			actualSize = is.available();
 			is.close();
 		}
 		
 		return actualSize > sizeThreshold;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.swordfish.plugins.compression.Compressor#asUncompressedSource(javax.xml.transform.Source)
 	 */
 	public Source asUncompressedSource(Source src) {
 		try {
 			String encoded = null;
 			if (src instanceof DOMSource) {
 				Node root = ((DOMSource) src).getNode();
 				if (root instanceof Document) {
 					root = ((Document) root).getDocumentElement();
 				}
 				Element rootElement = (Element) root;
 				String qName = rootElement.getNodeName();
 				String localName = qName.substring(qName.indexOf(":") + 1,
 						qName.length());
 				if (localName.equalsIgnoreCase(CompressionConstants.COMPRESSED_ELEMENT)) {
 					Node node = rootElement.getFirstChild();
 					if (node != null && node.getNodeType() == Node.TEXT_NODE) {
 						encoded = node.getNodeValue();
 					}
 				}
 				if (null != encoded && encoded.length() > 0) {
 					byte[] decoded = new Base64().decode(encoded.getBytes());
 					InputStream is = new GZIPInputStream(
 							new ByteArrayInputStream(decoded));
 					DocumentBuilder builder = DocumentBuilderFactory
 							.newInstance().newDocumentBuilder();
 					Document doc = builder.parse(is);
 					return new DOMSource(doc);
 				} else {
 					return src;
 				}
 			} else if (src instanceof StreamSource) {
 				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
 				CompressedContentHandler ch = new CompressedContentHandler();
 				xmlReader.setContentHandler(ch);
 				xmlReader.parse(new InputSource(((StreamSource) src)
 						.getInputStream()));
 				encoded = ch.getContent();
 				if (null != encoded && encoded.length() > 0) {
 					byte[] decoded = new Base64().decode(encoded.getBytes());
 					InputStream is = new GZIPInputStream(
 							new ByteArrayInputStream(decoded));
 					return new StreamSource(is);
 				} else {
 					throw new SwordfishException(
 							"Payload is empty, cannot uncompress.");
 				}
 			} else {
 				return asUncompressedSource(new SourceTransformer().toDOMSource(src));
 			}
 		} catch (Exception e) {
 			LOG.error(e.getMessage(), e);
 			throw new SwordfishException(e);
 		}
 	}
 
 	private class CompressedContentHandler implements ContentHandler {
 
 		private int state = 0;
 		private final StringBuilder buf = new StringBuilder();
 		private final Map<String, String> prefixes = new HashMap<String, String>();
 
 		public void startDocument() throws SAXException {
 			state++;
 		}
 
 		public void startElement(String uri, String localName, String qName,
 				Attributes atts) throws SAXException {
 			if (state == 1
 					&& localName.equalsIgnoreCase(CompressionConstants.COMPRESSED_ELEMENT)
 					&& uri.equalsIgnoreCase(CompressionConstants.COMPRESSION_NS)) {
 				state++;
 			}
 		}
 
 		public void characters(char[] ch, int start, int length)
 				throws SAXException {
 			if (state == 2) {
 				buf.append(new String(ch, start, length));
 			}
 		}
 
 		public void endElement(String uri, String localName, String qName)
 				throws SAXException {
 			if (state == 2
 					&& localName.equalsIgnoreCase(CompressionConstants.COMPRESSED_ELEMENT)
 					&& uri.equalsIgnoreCase(CompressionConstants.COMPRESSION_NS)) {
 				state++;
 			}
 		}
 
 		public void endDocument() throws SAXException {
 			if (state == 3) {
 				state++;
 			}
 		}
 
 		public String getContent() {
 			if (state == 4) {
 				return buf.toString();
 			} else {
 				return null;
 			}
 		}
 
 		public void startPrefixMapping(String prefix, String uri)
 				throws SAXException {
 			prefixes.put(prefix, uri);
 		}
 
 		public void endPrefixMapping(String prefix) throws SAXException {
 			prefixes.remove(prefix);
 		}
 
 		/*
 		 * unused methods from ContentHandler
 		 */
 
 		public void ignorableWhitespace(char[] ch, int start, int length)
 				throws SAXException {
 		}
 
 		public void processingInstruction(String target, String data)
 				throws SAXException {
 		}
 
 		public void setDocumentLocator(Locator locator) {
 		}
 
 		public void skippedEntity(String name) throws SAXException {
 		}
 
 	}
 }
