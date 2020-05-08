 //
 // Created       : 2006 Jul 28 (Fri) 14:21:09 by Harold Carr.
// Last Modified : 2006 Sep 23 (Sat) 16:20:08 by Harold Carr.
 //
 
 package com.differentity.server;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdql.Query;
 import com.hp.hpl.jena.rdql.QueryEngine;
 import com.hp.hpl.jena.rdql.QueryResults;
 
 import com.differentity.client.Main;
 
 public class Jena
 {
     public final String NOWHERE = "http://nowhere/";
     public final String RDF_XML = "RDF/XML";
     public static String lastQueryString; // for debug
 
     Model model;
 
     public Jena()
     {
 	model = ModelFactory.createDefaultModel();
     }
 
     public void close()
     {
 	model.close();
     }
 
     public void readRDF(String filename)
 	throws IOException
     {
 	readRDF(filename, RDF_XML);
     }
 
     public void readRDF(String filename, String format)
 	throws IOException
     {
 	FileInputStream in = null;
 	try {
 	    in = new FileInputStream(filename);
 	    model.read(in, NOWHERE, format);
 	} finally {
 	    if (in != null) {
 		try { in.close(); } catch (IOException e) {}
 	    }
 	}
     }
 
     public void assertFact(String s, String p, String v)	
     {
 	Resource subj = model.createResource(s);
 	Property prop = model.createProperty(p);
 	Resource val  = model.createResource(v);
 	subj.addProperty(prop, val);
     }
 
     public QueryResults doQuery(String s, String p, String v)
     {
 	String subject  = formatInput(s);
 	String property = formatInput(p);
 	String value    = formatInput(v);
 	String selectVars = "";
 	if (subject.startsWith(Main.questionMarkSymbol)) {
 	    selectVars = selectVars + " " + subject;
 	}
 	if (property.startsWith(Main.questionMarkSymbol)) {
 	    selectVars = selectVars + " " + property;
 	}
 	if (value.startsWith(Main.questionMarkSymbol)) {
 	    selectVars = selectVars + " " + value;
 	}
 
	if (selectVars.equals("")) {
	    selectVars = "?ddduuummmyyy";
	}

 	String queryString =
 	    " SELECT " + selectVars +
 	    " WHERE ( " +
 	    subject  + " " +
 	    property + " " +
 	    value    + " )";
 
 	// Spent a lot of time here trying to figure out why URIs
 	// disappeared in the status output.
 	// It's simple: because they look like "<URI>" - when
 	// that is put in an HTML element it disappears as an unknown tag.
 	// TODO: make it visible - either "ampersand" it our change
 	// status area from HTML to TextBox.
 
 	lastQueryString =
 	    " Jena: " +
 	    "[subject "  + s + " / " + subject  + "]" +
 	    "[property " + p + " / " + property + "]" +
 	    "[value "    + v + " / " + value    + "]" + "<br/>" +
 	    queryString;
 
 	return doQuery(queryString);
     }
 
     public QueryResults doQuery(String queryString)
     {
 	Query query = new Query(queryString);
 	query.setSource(model);
 	QueryEngine qe = new QueryEngine(query);
 	return qe.exec();
     }
 
     private String formatInput(String x)
     {
 	if (x.startsWith("?")) {
 	    return x;
 	}
 	return "<" + x + ">";
     }
 }
 
 // End of file.
