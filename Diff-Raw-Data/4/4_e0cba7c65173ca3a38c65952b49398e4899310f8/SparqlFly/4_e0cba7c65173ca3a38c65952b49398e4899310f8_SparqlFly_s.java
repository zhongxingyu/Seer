 package de.ifgi.lodum.sparqlfly;
 
 /* Copyright 2011-2013 
 Johannes Trame, johannes.trame@uni-muenster.de
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
       http:www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 This basically means: do with the code whatever your want. */
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLDecoder;
 import java.nio.charset.Charset;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import net.rootdev.javardfa.ParserFactory;
 import net.rootdev.javardfa.StatementSink;
 import net.rootdev.javardfa.ParserFactory.Format;
 import net.rootdev.javardfa.jena.JenaStatementSink;
 
 import org.antlr.stringtemplate.StringTemplate;
 import org.antlr.stringtemplate.StringTemplateGroup;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 import org.geospatialweb.arqext.Geo;
 import org.geospatialweb.arqext.Indexer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.XMLReader;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFormatter;
 import com.hp.hpl.jena.rdf.model.InfModel;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RSIterator;
 import com.hp.hpl.jena.reasoner.Reasoner;
 import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
 import com.hp.hpl.jena.reasoner.rulesys.Rule;
 import com.hp.hpl.jena.shared.ReificationStyle;
 import com.hp.hpl.jena.sparql.core.DataFormat;
 import com.hp.hpl.jena.sparql.resultset.TextOutput;
 
 
 import de.ifgi.lodum.sparqlfly.util.SparqlAcceptHeader;
 import de.ifgi.lodum.sparqlfly.util.StringInputStream;
 import de.ifgi.lodum.sparqlfly.util.StringOutputStream;
 
 import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml3;
 
 
 public class SparqlFly extends AbstractHandler {
 
 	Logger logger;
 	
 	private SparqlAcceptHeader accept;
 	private StringTemplateGroup templates =new StringTemplateGroup("templates","templates/");
 	private StringTemplate header = templates.getInstanceOf("header");
 	private StringTemplate defaultQuery = templates.getInstanceOf("defaultQuery");
 	private StringTemplate form = templates.getInstanceOf("form");
 	private StringTemplate error = templates.getInstanceOf("error");
 	private StringTemplate footer = templates.getInstanceOf("footer");
 
 
 	/**
 	 * Entry point for the server, handles all incoming requests. 
 	 */
 	public void handle(String target, Request baseRequest,
 			HttpServletRequest request, HttpServletResponse response)
 	throws IOException, ServletException {
 		this.logger = LoggerFactory.getLogger(SparqlFly.class);
 
 
 		//handle all static file requests
 		if(request.getRequestURI().startsWith("/files"))
 			return;
 
 		//prepare html templates
 		header.setAttribute("baseURL",getRequestBaseURL(request));
 
 		PrintWriter writer = response.getWriter();
 		accept = new SparqlAcceptHeader(request);
 
 		// change post requests to get requests
 		if (request.getMethod().equals("POST")) {
 			try {
 				response.sendRedirect(getRequestBaseURL(request)+ request.getParameter("query") + request.getParameter("accept"));
 			} catch (Exception e) {
 				logger.error(e.toString());
 				error.setAttribute("error", escapeHtml3(e.toString()));
 				writer.write(header.toString()+error.toString()+footer.toString());
 				response.setStatus(HttpServletResponse.SC_OK);
 				baseRequest.setHandled(true);
 			}
 		}else{
 			if(request.getParameter("query")!=null){
 				try{
 					executeQuery(request.getParameter("query"), baseRequest, request, writer, response);
 				}catch (Exception e) {
 					response.setContentType("text/html");
 					logger.error(e.toString());
 					error.setAttribute("error", escapeHtml3(e.toString()));
 					writer.write(header.toString()+error.toString()+footer.toString());
 				}
 			}else if(request.getParameter("queryString")!=null){
 				response.setContentType("text/html");
 				form.setAttribute("query",URLDecoder.decode(request.getParameter("queryString").replace("+", "%2B"),"UTF-8").replace("%2B", "+"));
 				writer.write(header.toString()+form.toString()+footer.toString());
 				form.reset();
 			}
 			else if(request.getParameter("queryFrom")!=null){
 				response.setContentType("text/html");
 				Model m=null;
 				try {
 					m = getModelFromURL(request.getParameter("queryFrom"),getFormat(request.getParameter("queryFrom")));
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				if(!m.isEmpty()){
 					try{
 						String q="";
 						for(Entry<String, String> pre:m.getNsPrefixMap().entrySet()){
 							if(!pre.getKey().contains("j."))
 								q+="PREFIX "+pre.getKey()+": <"+pre.getValue()+">\n";
 						}
 						q+="\nSELECT * \nFROM <"+request.getParameter("queryFrom")+">\n"+"WHERE {\n?a ?b ?c\n} LIMIT 10";
 						form.setAttribute("query",q);
 						writer.write(header.toString()+form.toString()+footer.toString());
 						form.reset();
 					}catch (Exception e) {
 						logger.error(e.toString());
 						error.setAttribute("error", e.toString());
 						writer.write(header.toString()+error.toString()+footer.toString());
 					}
 				}
 			}
 			else{
 				response.setContentType("text/html");
 				form.setAttribute("query", defaultQuery.toString());
 				writer.write(header.toString()+form.toString()+footer.toString());
 				form.reset();
 				header.reset();
 			}
 			response.setStatus(HttpServletResponse.SC_OK);
 			baseRequest.setHandled(true);
 		}
 	}
 
 	/**
 	 * returns the data format given a path or url
 	 * @param url
 	 * @return String
 	 */
 	private String getFormat(String url){
 		if(url.toLowerCase().endsWith(".rdf")){
 			return DataFormat.langXML.getSymbol();
 		}else if(url.toLowerCase().endsWith(".ttl")){
 			return DataFormat.langTurtle.getSymbol();
 		}else if(url.toLowerCase().endsWith(".n3")){
 			return DataFormat.langN3.getSymbol();
 		}else if(url.toLowerCase().endsWith(".html")){
 			return "HTML";
 		}
 		return "HTML";
 	}
 
 
 	/**
 	 * executes an sparql query and writes the query solution to the servlet response
 	 * @param queryString
 	 * @param baseRequest
 	 * @param request
 	 * @param writer
 	 * @param response
 	 */
 	private void executeQuery(String queryString, Request baseRequest, HttpServletRequest request, PrintWriter writer,HttpServletResponse response ) throws Exception {
 		response.setHeader("Connection", "Keep-Alive");
 		response.setHeader("Cache-Control", "no-cache"); 
 		response.setHeader("Access-Control-Allow-Origin", "*");
 		response.setHeader("Access-Control-Allow-Methods", "GET");
 		response.setHeader("Access-Control-Allow-Headers", "Content-Type");
 
 		// add RDF++ rules
 		Reasoner reasoner = new GenericRuleReasoner(Rule.rulesFromURL("files/rdfsplus.rule"));
 
 		// Create inferred model using the reasoner and write it out.
 		InfModel inf = ModelFactory.createInfModel(reasoner, ModelFactory.createDefaultModel(ReificationStyle.Standard));
 
 		Query query =null;
 
 		query = QueryFactory.create(queryString);
 
 		Iterator<String> remoteFiles = query.getGraphURIs().iterator();
 		while(remoteFiles.hasNext()){
 			String temp = remoteFiles.next();
 			
 			inf.add(getModelFromURL(temp,getFormat(temp)));
 
 		}
 
 
 
 
 
 		QueryExecution qexec=null;
 		//check if geo methods have been used in the query. if not no spatial index is needed
 		if(query.getPrefixMapping().getNsPrefixMap().containsValue("java:org.geospatialweb.arqext.")){
 			Indexer i = Indexer.createDefaultIndexer();
 			i.createIndex(inf);
 			qexec = QueryExecutionFactory.create(query, inf);
 			Geo.setContext(qexec, i);
 
 		}else{
 			qexec = QueryExecutionFactory.create(query, inf);
 		}
 
 		if(query.isSelectType()){
 			ResultSet results = qexec.execSelect();
 			String outputString=null;
 			StringOutputStream outstream = new StringOutputStream();
 			//write the query solution to different formats depending on the requested format
 			if (accept.getPrefMIME().equalsIgnoreCase("application/sparql-results+json") || accept.getPrefMIME().equalsIgnoreCase("application/json")  ){
 				response.setContentType("application/sparql-results+json");
 				ResultSetFormatter.outputAsJSON(outstream, results);
 				writer.print(outstream);
 			}else if (accept.getPrefMIME().equalsIgnoreCase("application/sparql-results+xml")){
 				response.setContentType("application/sparql-results+xml");
 
 				ResultSetFormatter.outputAsXML(outstream,results);
 				writer.print(outstream.toString());
 			}else if (accept.getPrefMIME().equalsIgnoreCase("application/rdf+xml")){
 				response.setContentType("application/rdf+xml");
 
 				//	Model model = ModelFactory.createDefaultModel();
 				ResultSetFormatter.asRDF(inf, results);
 				inf.write(writer, "RDF/XML");
 			}else if (accept.getPrefMIME().equalsIgnoreCase("application/x-turtle")){
 				response.setContentType("application/x-turtle");
 
 				//	Model model = ModelFactory.createDefaultModel();
 				ResultSetFormatter.asRDF(inf, results);
 				inf.write(writer, "TURTLE");
 			}else if(accept.getPrefMIME().equalsIgnoreCase("text/html")){
 				response.setContentType("text/html");
 
 				ResultSetFormatter.outputAsXML(outstream,results);
 				//transform xml sparql result to html table with xslt transformation
 				Source xmlSource = new StreamSource(new StringInputStream(outstream.toString()));
 				Source xsltSource = null;
 
 				xsltSource = new StreamSource(new FileInputStream(new File("files/result-to-html.xsl")));
 
 				StringOutputStream resultStream = new StringOutputStream();
 				Result result = new StreamResult(resultStream);
 				TransformerFactory transFact=TransformerFactory.newInstance(  );
 				Transformer trans = null;
 				try {
 					trans = transFact.newTransformer(xsltSource);
 				} catch (TransformerConfigurationException e1) {
 					e1.printStackTrace();
 				}
 				try {
 					trans.transform(xmlSource, result);
 				} catch (TransformerException e) {
 					e.printStackTrace();
 				}
 				String html =resultStream.toString();
 				String prefixes="";
 				if(request.getParameter("prefix")!=null && request.getParameter("prefix").equals("true")){
 					prefixes+="<b>Prefixes</b><br>";
 					for(Entry<String, String> pre:inf.getNsPrefixMap().entrySet()){
 						if(pre.getKey() !=null&&pre.getValue()!=null&&html.contains(pre.getValue())){
 							prefixes+=pre.getKey()+" :" + pre.getValue() +"<br>";
 							html=html.replace(pre.getValue(), pre.getKey()+" : ");				
 						}
 					}
 					prefixes+="<br/><br/>";
 				}
 				html =html.replace("$PREFIXES$", prefixes);
 				writer.write(header.toString()+html+"<br><br><br><input type=button value=\"<< back\" onClick=\"history.back()\">"+footer.toString());
 			}else if(accept.getPrefMIME().equalsIgnoreCase("application/n3")){
 				response.setContentType("application/n3");
 				ResultSetFormatter.asRDF(inf, results);
 				inf.write(writer, "N3");
 			}else if(accept.getPrefMIME().equalsIgnoreCase("text/comma-separated-values")){
 				response.setContentType("text/comma-separated-values");
 				ResultSetFormatter.outputAsCSV(outstream, results);
 				writer.print(outstream.toString());
 			}
 			else{
 				response.setContentType("text/plain");
 				outputString = new TextOutput(inf).asString(results); 
 				writer.print(outputString);
 			}
 			//if the query is a construct query
 		}else if(query.isConstructType()){
 			Model resultModel = qexec.execConstruct();
 			if (accept.getPrefMIME().equalsIgnoreCase("application/x-turtle")){
 				response.setContentType("application/x-turtle");
 				resultModel .write(writer, "TURTLE");
 			}else if(accept.getPrefMIME().equalsIgnoreCase("application/n3")){
 				response.setContentType("application/n3");
 				resultModel.write(writer, "N3");
 			}else{
 				response.setContentType("application/rdf+xml");
 				resultModel.write(writer, "RDF/XML");
 			}
 		}
 		response.setStatus(HttpServletResponse.SC_OK);
 		baseRequest.setHandled(true);
 		form.reset();
 		header.reset();
 
 	}
 	/**
 	 * Returns jena model from URL
 	 * @throws ParseException 
 	 */
 	private Model getModelFromURL(String urlString, String format) throws Exception{
 		Model m = ModelFactory.createDefaultModel(ReificationStyle.Standard);
 		
		if(format.equals(Format.HTML)){
 			 StatementSink sink = new JenaStatementSink(m);
 		        XMLReader parser = ParserFactory.createReaderForFormat(sink, Format.HTML);
 		}else{
 		URL url = null;
 		try {
 			url = new URL(urlString);
 		} catch (MalformedURLException e1) {
 			e1.printStackTrace();
 		}
 
 		URLConnection c=null;
 		try {
 			c = url.openConnection();
 			c.setConnectTimeout(10000);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new InputStreamReader(c.getInputStream(),Charset.forName("UTF-8")));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		m.read(in,"",format);
 		try {
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		RSIterator it = m.listReifiedStatements();
 		while(it.hasNext()){
 			m.add(it.nextRS().getStatement());
 		}
 		}
 
 		return m;
 	}
 
 	/**
 	 * Returns the base URL of the server 
 	 * 
 	 * @param request
 	 * @return
 	 */
 	private String getRequestBaseURL(HttpServletRequest request) {
 		return ("http://" + request.getServerName() + ":" + request
 				.getServerPort());
 	}
 
 
 }
