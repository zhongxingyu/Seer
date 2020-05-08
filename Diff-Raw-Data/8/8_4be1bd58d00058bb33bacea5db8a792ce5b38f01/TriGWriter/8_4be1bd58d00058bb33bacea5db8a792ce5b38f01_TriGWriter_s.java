 /*
 * $Id: TriGWriter.java,v 1.9 2009/04/13 17:13:28 hartig Exp $
  */
 package de.fuberlin.wiwiss.ng4j.trig;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.mem.GraphMem;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.impl.ModelCom;
 import com.hp.hpl.jena.shared.JenaException;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraph;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSetWriter;
 
 /**
  * <p>Serializes a {@link NamedGraphSet} as a TriG file (see
  * <a href="http://www.wiwiss.fu-berlin.de/suhl/bizer/TriG/">TriG
  * specification</a>).</p>
  * 
  * <p>This class is typically not used directly, but through the
  * NamedGraphSet.write methods.</p>
  * 
  * <p>When used directly, custom namespace prefixes can be defined:</p>
  * 
  * <pre>TriGWriter writer = new TriGWriter();
  * writer.addNamespace("ex", "http://example.org/");
  * writer.write(myNamedGraphSet, System.out, "http://example.org/baseURI");</pre>
  *
  * @author Richard Cyganiak (richard@cyganiak.de)
  */
 public class TriGWriter implements NamedGraphSetWriter {
 	private Writer writer;
 	private NamedGraph currentGraph;
 	private PrettyNamespacePrefixMaker prefixMaker;
 	private Map<String,String> customPrefixes = new HashMap<String,String>();
 	
 	/**
 	 * Writes a NamedGraphSet to a Writer. The base URI is optional.
 	 */
 	public void write(NamedGraphSet set, Writer out, String baseURI) {
 		this.writer = new BufferedWriter(out);
 		Iterator<NamedGraph> graphIt = set.listGraphs();
		Graph allTriples = graphIt.hasNext() ?
				set.asJenaGraph(( graphIt.next()).getGraphName()) :
				new GraphMem();
 		while ( graphIt.hasNext() ) {
 			Iterator tripleIt = graphIt.next().find( Node.ANY, Node.ANY, Node.ANY );
 			while ( tripleIt.hasNext() ) {
 				allTriples.add( (Triple) tripleIt.next() );
 			}
 		}
 		this.prefixMaker = new PrettyNamespacePrefixMaker(allTriples);
 		this.prefixMaker.setBaseURI(baseURI);
 		this.prefixMaker.addDefaultNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
 		this.prefixMaker.addDefaultNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
 		this.prefixMaker.addDefaultNamespace("owl", "http://www.w3.org/2002/07/owl#");
 		this.prefixMaker.addDefaultNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
 		this.prefixMaker.addDefaultNamespace("dc", "http://purl.org/dc/elements/1.1/");
 		this.prefixMaker.addDefaultNamespace("dcterms", "http://purl.org/dc/terms/");
 		this.prefixMaker.addDefaultNamespace("rss", "http://purl.org/rss/1.0/");
 		this.prefixMaker.addDefaultNamespace("foaf", "http://xmlns.com/foaf/0.1/");
 		this.prefixMaker.addDefaultNamespace("contact", "http://www.w3.org/2000/10/swap/pim/contact#");
 		this.prefixMaker.addDefaultNamespace("doap", "http://usefulinc.com/ns/doap#");
 		this.prefixMaker.addDefaultNamespace("cc", "http://web.resource.org/cc/");
 		this.prefixMaker.addDefaultNamespace("swp1", "http://www.w3.org/2004/03/trix/swp-1/");
 		this.prefixMaker.addDefaultNamespace("swp", "http://www.w3.org/2004/03/trix/swp-2/");
 		this.prefixMaker.addDefaultNamespace("rdfg", "http://www.w3.org/2004/03/trix/rdfg-1/");
 		Iterator<String> it = this.customPrefixes.keySet().iterator();
 		while (it.hasNext()) {
 			String prefix = (String) it.next();
 			String uri = (String) this.customPrefixes.get(prefix);
 			this.prefixMaker.addNamespace(prefix, uri);
 		}
 		Model namespaceModel = ModelFactory.createDefaultModel();
 		namespaceModel.setNsPrefixes(this.prefixMaker.getPrefixMap());
 		new N3JenaWriterOnlyNamespaces().write(namespaceModel, out, baseURI);
 		it = getSortedGraphNames(set).iterator();
 		while (it.hasNext()) {
 			String graphName = (String) it.next();
 			this.currentGraph = set.getGraph(graphName);
 			Model aModel = new ModelCom(this.currentGraph);
 			Set tmp = new HashSet( aModel.getNsPrefixMap().keySet() );
 			for ( Object p : tmp ) {               // Remove all prefixes because
 				aModel.removeNsPrefix( (String) p); // some of them could be un-
 			}                                      // known to prefixMaker.  Olaf
 			aModel.setNsPrefixes(this.prefixMaker.getPrefixMap());
 			new N3JenaWriterOnlyStatements().write(
 					aModel, out, baseURI);
 		}
 		try {
 			this.writer.flush();
 		} catch (IOException ioex) {
 			throw new JenaException(ioex);
 		}
 	}
 
 	/**
 	 * Writes a NamedGraphSet to an OutputStream. The base URI is optional.
 	 */
 	public void write(NamedGraphSet set, OutputStream out, String baseURI) {
 		try {
 			write(set, new OutputStreamWriter(out, "utf-8"), baseURI);
 		} catch (UnsupportedEncodingException ueex) {
 			// UTF-8 is always supported
 		}
 	}
 
 	/**
 	 * Adds a custom namespace prefix.
 	 * @param prefix The namespace prefix
 	 * @param namespaceURI The full namespace URI
 	 */
 	public void addNamespace(String prefix, String namespaceURI) {
 		this.customPrefixes.put(prefix, namespaceURI);
 	}
 
 	private String getCurrentGraphURI() {
 		return this.currentGraph.getGraphName().getURI();
 	}
 
 	private boolean currentGraphIsEmpty() {
 		return this.currentGraph.isEmpty();
 	}
 
 	private List<String> getSortedGraphNames(NamedGraphSet set) {
 		Iterator<NamedGraph> unsortedIt = set.listGraphs();
 		List<String> sorting = new ArrayList<String>();
 		while (unsortedIt.hasNext()) {
 			sorting.add((unsortedIt.next()).getGraphName().getURI());
 		}
 		Collections.sort(sorting);
 		return sorting;
 	}
 
 	private class N3JenaWriterOnlyNamespaces extends N3JenaWriterCommon {
 		protected void writeHeader(Model model) {
 			// don't write out the base URI
 		}
 		protected void writeModel(Model model) {
 			// don't write body
 		}
 	}
 
 	private class N3JenaWriterOnlyStatements extends N3JenaWriterPP {
 
 		// we override this only to remove that one println()
 	    protected void processModel(Model baseModel)
 	    {
 	        prefixMap = baseModel.getNsPrefixMap() ;
 	        Model model = ModelFactory.withHiddenStatements( baseModel );
 	        bNodesMap = new HashMap<Resource,String>() ;
 
 	        // If no base defined for the model, but one given to writer,
 	        // then use this.
 	        String base2 = (String)prefixMap.get("") ;
 	        
 	        if ( base2 == null && baseURIrefHash != null )
 	            prefixMap.put("", baseURIrefHash) ;
 
 	        for ( Iterator<String> iter = prefixMap.keySet().iterator() ; iter.hasNext() ; )
 	        {
 	            String prefix = iter.next() ;
 	            if ( prefix.indexOf('.') != -1 )
 	                iter.remove() ;
 	        }
 	        
 	        startWriting() ;
 	        prepare(model) ;
 
 	        writeHeader(model) ;
 	        writePrefixes(model) ;
 
 // No! -- RC
 //	        if (prefixMap.size() != 0)
 //	            out.println();
 
 	        // Do the output.
 	        writeModel(model) ;
 
 	        // Release intermediate memory - allows reuse of a writer
 	        finishWriting() ;
 	        bNodesMap = null ;
 	    }
 
 		protected void startWriting() {
 			if (TriGWriter.this.currentGraphIsEmpty()) {
 				this.out.println(formatURI(getCurrentGraphURI()) + " { }");
 				this.out.println();
 				super.startWriting();
 				return;
 			}
 			this.out.print(formatURI(getCurrentGraphURI()) + " {");
 
 			// indent all statements of the graph
 			this.out.setIndent(4);
 
 			super.startWriting();
 		}
 
 		protected void finishWriting() {
 			super.finishWriting();
 
 			if (TriGWriter.this.currentGraphIsEmpty()) {
 				return;
 			}
 			// return to normal indentation level and close graph
 			this.out.killDeferredIndent();
 			this.out.setIndent(0);
 
 			this.out.println("}");
 			this.out.println();
 		}
 
 		protected void writeHeader(Model model) {
 			// don't write header
 		}
 
 		protected void writePrefixes(Model model) {
 			// don't write prefixes
 		}
 
 		private String getCurrentGraphURI() {
 			// Hackish ... smuggling in the current graph name
 			return TriGWriter.this.getCurrentGraphURI();
 		}
 	}
 }
 
 /*
  *  (c) Copyright 2004 - 2009 Christian Bizer (chris@bizer.de)
  *   All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. The name of the author may not be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
