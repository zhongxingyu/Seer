 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
 
 
 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.markup.resource;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.mylyn.docs.intent.markup.builder.ModelDocumentBuilder;
 import org.eclipse.mylyn.docs.intent.markup.markup.Document;
 import org.eclipse.mylyn.docs.intent.markup.markup.Image;
 import org.eclipse.mylyn.docs.intent.markup.markup.Link;
 import org.eclipse.mylyn.docs.intent.markup.markup.MarkupFactory;
 import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
 import org.eclipse.mylyn.wikitext.core.util.IgnoreDtdEntityResolver;
 import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
import com.google.common.collect.Iterators;

 /**
  * A resource implementation for web-based pages on wikimedia.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 public class WikimediaResource extends ResourceImpl {
 
 	private static final int BUFFER_SIZE = 0x10000;
 
 	public WikimediaResource(URI eUri) {
 		super(eUri);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecore.resource.impl.ResourceImpl#load(java.util.Map)
 	 */
 	@Override
 	public void load(Map<?, ?> options) throws IOException {
 		// let's build the http URI...
 		URI uri = getURI();
 
 		WikimediaURI wURI = new WikimediaURI(uri);
 
 		String pageName = wURI.pageName();
 		String apiURI = wURI.baseServer() + "/api.php?format=xml&action=query&prop=revisions&titles="
 				+ pageName + "&rvprop=content";
 		URI eApiURI = URI.createURI(apiURI);
 
 		Map<?, ?> response = null;
 		if (options != null) {
 			response = (Map<?, ?>)options.get(URIConverter.OPTION_RESPONSE);
		}
		if (response == null) {
 			response = new HashMap<Object, Object>();
 		}
 
 		InputStream inputStream = getInputStream(eApiURI);
 
 		URI eImgURI = URI.createURI(wURI.baseServer() + "/api.php?action=query&titles=" + pageName
 				+ "&generator=images&prop=imageinfo&iiprop=url&format=xml");
 		InputStream inputImage = getInputStream(eImgURI);
 
 		try {
 			wikimediaLoad(inputStream, options);
 			/*
 			 * Now let's get more information about the images
 			 */
 
 			handleImagesData(eImgURI, wURI.baseServer(), inputImage);
 
 		} catch (SAXException e) {
 			// TODO proper logging
 			throw new RuntimeException(e);
 		} catch (ParserConfigurationException e) {
 			throw new RuntimeException(e);
 		} finally {
 			inputStream.close();
 			inputImage.close();
 			Long timeStamp = (Long)response.get(URIConverter.RESPONSE_TIME_STAMP_PROPERTY);
 			if (timeStamp != null) {
 				setTimeStamp(timeStamp);
 			}
 		}
 
 		prepareProxyFromLinks();
 
 	}
 
 	private void handleImagesData(URI eImgURI, String baseServer, InputStream input)
 			throws ParserConfigurationException, SAXException, IOException {
 
 		final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
 		parserFactory.setNamespaceAware(true);
 		parserFactory.setValidating(false);
 		SAXParser saxParser = parserFactory.newSAXParser();
 		XMLReader xmlReader = saxParser.getXMLReader();
 		xmlReader.setEntityResolver(IgnoreDtdEntityResolver.getInstance());
 
 		ImageFetchingContentHandler contentHandler = new ImageFetchingContentHandler();
 		xmlReader.setContentHandler(contentHandler);
 
 		try {
 			xmlReader.parse(new InputSource(input));
 		} catch (IOException e) {
 			throw new RuntimeException(
 					String.format("Unexpected exception retrieving data from %s", eImgURI), e); //$NON-NLS-1$
 		}
 
 		if (contentHandler.imageTitleToUrl.size() > 0) {
 			Iterator<Image> it = Iterators.filter(getAllContents(), Image.class);
 			while (it.hasNext()) {
 				Image cur = it.next();
 				String completeURL = contentHandler.imageTitleToUrl.get("Image:" + cur.getUrl());
 				if (completeURL != null) {
 					cur.setUrl(baseServer + "/" + completeURL);
 				}
 			}
 		}
 
 	}
 
 	private InputStream getInputStream(URI eApiURI) throws IOException {
 		// If an input stream can't be created, ensure that the resource is
 		// still considered loaded after the failure,
 		// and do all the same processing we'd do if we actually were able to
 		// create a valid input stream.
 		//
 		InputStream inputStream = null;
 		try {
 			inputStream = getURIConverter().createInputStream(eApiURI);
 		} catch (IOException exception) {
 			Notification notification = setLoaded(true);
 			isLoading = true;
 			if (errors != null) {
 				errors.clear();
 			}
 			if (warnings != null) {
 				warnings.clear();
 			}
 			isLoading = false;
 			if (notification != null) {
 				eNotify(notification);
 			}
 			setModified(false);
 
 			throw exception;
 		}
 		return inputStream;
 	}
 
 	private void prepareProxyFromLinks() {
 
 		Iterator<Link> it = Iterators.filter(getAllContents(), Link.class);
 		while (it.hasNext()) {
 			Link lnk = it.next();
 			String href = lnk.getHrefOrHashName();
 			if (lnk.getTarget() == null && href.startsWith("/wiki/")) {
 				String targetPageName = href.substring(href.indexOf("/wiki/") + 6);
 				URI uri = getURI();
 				URI targetUri = uri.trimSegments(uri.segmentCount());
 				targetUri = URI.createURI(targetUri.toString() + targetPageName + "#/0");
 				Document proxifiedDoc = MarkupFactory.eINSTANCE.createDocument();
 				((InternalEObject)proxifiedDoc).eSetProxyURI(targetUri);
 				lnk.setTarget(proxifiedDoc);
 			}
 		}
 
 	}
 
 	private void wikimediaLoad(InputStream is, Map<?, ?> options) throws SAXException, IOException {
 
 		final char[] buffer = new char[BUFFER_SIZE];
 		StringBuilder out = new StringBuilder();
 		Reader in = new InputStreamReader(is, "UTF-8");
 		int read;
 		do {
 			read = in.read(buffer, 0, buffer.length);
 			if (read > 0) {
 				out.append(buffer, 0, read);
 			}
 		} while (read >= 0);
 
 		String outString = out.toString();
 		int begin = outString.indexOf("<rev>") + 4;
 		int end = outString.indexOf("</rev>");
 
 		String revisionContent = outString.substring(begin + 1, end - begin);
 
 		MarkupParser parser = new MarkupParser(new MediaWikiLanguage());
 		ModelDocumentBuilder builder = new ModelDocumentBuilder();
 		parser.setBuilder(builder);
 		parser.parse(revisionContent, true);
 
 		Collection<EObject> roots = builder.getRoots();
 
 		getContents().addAll(roots);
 
 	}
 
 }
 
 /**
  * URI for for web-based pages on wikimedia.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 class WikimediaURI {
 
 	private URI baseURI;
 
 	public WikimediaURI(URI baseURI) {
 		this.baseURI = baseURI;
 	}
 
 	public String pageName() {
 
 		int startingSegment = 0;
 		for (int i = 0; i < baseURI.segmentCount(); i++) {
 			String seg = baseURI.segment(i);
 			if ("index.php".equals(seg)) {
 				startingSegment = i + 1;
 			}
 		}
 
 		String pageName = "";
 		for (int i = startingSegment; i < baseURI.segmentCount(); i++) {
 			if (i > startingSegment) {
 				pageName += "/";
 			}
 			pageName += baseURI.segment(i);
 		}
 		return pageName;
 	}
 
 	public String baseServer() {
 		return baseURI.scheme() + "://" + baseURI.host();
 	}
 
 }
 
 /**
  * A specific content handler.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 class ImageFetchingContentHandler implements ContentHandler {
 
 	final Map<String, String> imageTitleToUrl = new HashMap<String, String>();
 
 	private String currentPage;
 
 	private boolean inImageInfo;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
 	 *      org.xml.sax.Attributes)
 	 */
 	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
 		if ("page".equals(localName)) { //$NON-NLS-1$
 			currentPage = atts.getValue("title"); //$NON-NLS-1$
 		} else if ("imageinfo".equals(localName)) { //$NON-NLS-1$
 			inImageInfo = true;
 		} else if (inImageInfo && "ii".equals(localName)) { //$NON-NLS-1$
 			imageTitleToUrl.put(currentPage, atts.getValue("url")); //$NON-NLS-1$
 			imageTitleToUrl.put(currentPage.replace(' ', '_'), atts.getValue("url"));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 		if ("page".equals(localName)) { //$NON-NLS-1$
 			currentPage = null;
 		} else if ("imageinfo".equals(localName)) { //$NON-NLS-1$
 			inImageInfo = false;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
 	 */
 	public void characters(char[] ch, int start, int length) throws SAXException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.ContentHandler#endDocument()
 	 */
 	public void endDocument() throws SAXException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
 	 */
 	public void endPrefixMapping(String prefix) throws SAXException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
 	 */
 	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
 	 */
 	public void processingInstruction(String target, String data) throws SAXException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
 	 */
 	public void setDocumentLocator(Locator locator) {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
 	 */
 	public void skippedEntity(String name) throws SAXException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.ContentHandler#startDocument()
 	 */
 	public void startDocument() throws SAXException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
 	 */
 	public void startPrefixMapping(String prefix, String uri) throws SAXException {
 	}
 
 }
