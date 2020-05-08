 /*
  * Copyright (c) 2009, James Leigh All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution. 
  * - Neither the name of the openrdf.org nor the names of its contributors may
  *   be used to endorse or promote products derived from this software without
  *   specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * 
  */
 package org.openrdf.repository.object.compiler;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.ContextStatementImpl;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.RDFParser;
 import org.openrdf.rio.RDFParserRegistry;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Reads ontologies and schemas into memory from remote sources.
  * 
  * @author James Leigh
  *
  */
 public class OntologyLoader {
 
 	private Logger logger = LoggerFactory.getLogger(OntologyLoader.class);
 	private Model model;
 	/** context -&gt; prefix -&gt; namespace */
 	private Map<URI, Map<String, String>> namespaces = new HashMap<URI, Map<String,String>>();
 	private List<URL> imported = new ArrayList<URL>();
 	private ValueFactory vf = ValueFactoryImpl.getInstance();
 
 	public OntologyLoader(Model model) {
 		this.model = model;
 	}
 
 	public List<URL> getImported() {
 		return imported;
 	}
 
 	public Model getModel() {
 		return model;
 	}
 
 	/** context -&gt; prefix -&gt; namespace */
 	public Map<URI, Map<String, String>> getNamespaces() {
 		return namespaces;
 	}
 
 	public void loadOntologies(List<URL> urls) throws RDFParseException,
 			IOException {
 		for (URL url : urls) {
 			loadOntology(url, null, vf.createURI(url.toExternalForm()));
 		}
 	}
 
 	public void followImports() throws RDFParseException, IOException {
 		List<URL> urls = new ArrayList<URL>();
 		for (Value obj : model.filter(null, OWL.IMPORTS, null).objects()) {
 			if (obj instanceof URI) {
 				URI uri = (URI) obj;
				if (!model.contains(null, null, null, uri)) {
 					URL url = new URL(uri.stringValue());
 					if (!imported.contains(url)) {
 						urls.add(url);
 					}
 				}
 			}
 		}
 		if (!urls.isEmpty()) {
 			imported.addAll(urls);
 			for (URL url : urls) {
 				String uri = url.toExternalForm();
 				loadOntology(url, null, vf.createURI(uri));
 			}
 			followImports();
 		}
 	}
 
 	private void loadOntology(URL url, RDFFormat override, final URI uri)
 			throws IOException, RDFParseException {
 		try {
 			URLConnection conn = url.openConnection();
 			if (override == null) {
 				conn.setRequestProperty("Accept", getAcceptHeader());
 			} else {
 				conn
 						.setRequestProperty("Accept", override
 								.getDefaultMIMEType());
 			}
 			RDFFormat format = override;
 			if (override == null) {
 				format = RDFFormat.RDFXML;
 				format = RDFFormat.forFileName(url.toString(), format);
 				format = RDFFormat.forMIMEType(conn.getContentType(), format);
 			}
 			RDFParserRegistry registry = RDFParserRegistry.getInstance();
 			RDFParser parser = registry.get(format).getParser();
 			parser.setRDFHandler(new StatementCollector(model, model
 					.getNamespaces()) {
 				@Override
 				public void handleStatement(Statement st) {
 					Resource s = st.getSubject();
 					URI p = st.getPredicate();
 					Value o = st.getObject();
 					super
 							.handleStatement(new ContextStatementImpl(s, p, o,
 									uri));
 				}
 
 				@Override
 				public void handleNamespace(String prefix, String ns)
 						throws RDFHandlerException {
 					Map<String, String> map = namespaces.get(uri);
 					if (map == null) {
 						namespaces
 								.put(uri, map = new HashMap<String, String>());
 					}
 					map.put(prefix, ns);
 					super.handleNamespace(prefix, ns);
 				}
 			});
 			InputStream in = conn.getInputStream();
 			try {
 				parser.parse(in, url.toExternalForm());
 			} catch (RDFHandlerException e) {
 				throw new AssertionError(e);
 			} catch (RDFParseException e) {
 				if (override == null && format.equals(RDFFormat.NTRIPLES)) {
 					// sometimes text/plain is used for rdf+xml
 					loadOntology(url, RDFFormat.RDFXML, uri);
 				} else {
 					throw e;
 				}
 			} finally {
 				in.close();
 			}
 		} catch (RDFParseException e) {
 			logger.warn("Could not load {} {}", url, e.getMessage());
 			String msg = e.getMessage() + " in " + url;
 			throw new RDFParseException(msg, e.getLineNumber(), e.getColumnNumber());
 		} catch (IOException e) {
 			logger.warn("Could not load {} {}", url, e.getMessage());
 		} catch (SecurityException e) {
 			logger.warn("Could not load {} {}", url, e.getMessage());
 		}
 	}
 
 	private String getAcceptHeader() {
 		StringBuilder sb = new StringBuilder();
 		String preferred = RDFFormat.RDFXML.getDefaultMIMEType();
 		sb.append(preferred).append(";q=0.2");
 		Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
 		for (RDFFormat format : rdfFormats) {
 			for (String type : format.getMIMETypes()) {
 				if (!preferred.equals(type)) {
 					sb.append(", ").append(type);
 				}
 			}
 		}
 		return sb.toString();
 	}
 }
