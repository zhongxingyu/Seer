 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.parser.tika.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
 import org.apache.tika.config.TikaConfig;
 import org.apache.tika.exception.TikaException;
 import org.apache.tika.metadata.Metadata;
 import org.apache.tika.parser.Parser;
 import org.apache.tika.sax.BodyContentHandler;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.component.ComponentContext;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.IParserDocument.Status;
 import org.paxle.parser.ASubParser;
 import org.paxle.parser.IParserContext;
 import org.paxle.parser.ISubParser;
 import org.paxle.parser.ParserContext;
 import org.paxle.parser.ParserException;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 
@Component(immediate=true)
 public class TikaParserManager {
 	/**
 	 * For logging
 	 */
 	protected final Log logger = LogFactory.getLog(this.getClass());	
 	
 	/**
 	 * All registered parsers
 	 */
 	protected List<ServiceRegistration> services = new ArrayList<ServiceRegistration>();
 	
 	protected void activate(ComponentContext context) throws TikaException {
 		BundleContext bc = context.getBundleContext();
 		
 		// determining all available tika parsers
 		TikaConfig config = TikaConfig.getDefaultConfig();
 		
 		// loop through all parsers and register them as paxle-parsers
 		Map<String, Parser> parserMap = config.getParsers();
 		if (parserMap != null) {
 			for (Entry<String, Parser> parser : parserMap.entrySet()) {
 				final String mimeType = parser.getKey();
 				final Parser tikaParser = parser.getValue();
 				
 				final ISubParser paxleParser = new ParserWrapper(mimeType, tikaParser);
 				final Hashtable<String, Object> paxleParserProps = new Hashtable<String, Object>();
				paxleParserProps.put(ISubParser.PROP_MIMETYPES, new String[]{mimeType});
 				paxleParserProps.put(Constants.SERVICE_PID, tikaParser.getClass().getName()+"_"+mimeType.hashCode());
 				final ServiceRegistration reg = bc.registerService(ISubParser.class.getName(), paxleParser, paxleParserProps);
 				this.services.add(reg);
 			}
 		}
 		
 		this.logger.info("Apache Tika framework started");		
 	}
 	
 	protected void deactivate(ComponentContext context) {
 		for (ServiceRegistration reg : this.services) {
 			reg.unregister();		
 		}
 		this.services.clear();
 		
 		this.logger.info("Apache Tika framework stopped");
 	}
 	
 	private static class ParserWrapper extends ASubParser {
 		private final String mimeType;
 		private final Parser tikaParser;
 		
 		public ParserWrapper(String mimeType, Parser tikaParser) {
 			this.mimeType = mimeType;
 			this.tikaParser = tikaParser;
 		}
 		
 		@Override
 		public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException {
 			try {
 				IParserContext context = ParserContext.getCurrentContext();
 				
 				// creating an empty document
 				IParserDocument pDoc = context.createDocument();
 				pDoc.setMimeType(this.mimeType);
 				
 				// parsing the content
 				ContentHandler textHandler = new BodyContentHandler() {
 					@Override
 					public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
 						super.startElement(uri, localName, name, atts);
 					}
 				};
 				Metadata metadata = new Metadata();
 				this.tikaParser.parse(is, textHandler, metadata);
 
 				// TODO: accessing the values
 		
 				
 				// parsing finished
 				pDoc.setStatus(Status.OK);
 				return pDoc;
 			} catch (Throwable e) {
 				throw new ParserException(e.getMessage());
 			} finally {
 				is.close();
 			}
 		}
 	}
 }
