 package com.computas.sublima.app.service;
 
 import java.io.IOException;
 import com.computas.sublima.query.RDFObject;
 import com.computas.sublima.query.service.SearchService;
 import org.apache.log4j.Logger;
 
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Arrays;
 import java.util.Random;
 
 import com.hp.hpl.jena.sparql.util.StringUtils;
 
 /**
  * A service class with methods to create a SPARQL DESCRIBE query from a
  * key-value structure. The constructor takes a mandatory array of prefix
  * declarations, on the form
  * 
  * <pre>
  * 				prefix: &lt;URI&gt;
  * </pre>
  * 
  * e.g., it may be called as myService = new Form2SparqlService(new
  * String[]{"dct: <http://purl.org/dc/terms/>", "foaf:
  * <http://xmlns.com/foaf/0.1/>"});
  *
  * This class has become rather complex, as it has to account for a very high number of
  * different situations
  *
  * @author kkj
  * @version 1.0
  * @param: prefixes
  *         string array with prefixes for names used.
  * @param freetextFields
  * 			  A List containing the fields that containing the fields 
  *            that needs to be treated as free-text-indexed fields.
 
  */
 public class Form2SparqlService {
 	
 	private static Logger logger = Logger.getLogger(Form2SparqlService.class);
 	
 	private String language;
 	
 	private String SparulSubjectURI;
 
 	private List<String> prefixes = new ArrayList<String>();
 
 	private List subjectVarList = new LinkedList();
 
 	private int variablecount = 1; // the var below has to be unique across calls
 
     private List freetextFields = new ArrayList<String>();
 
 
     public Form2SparqlService(String[] pr) {
         prefixes = new ArrayList<String>(Arrays.asList(pr));
         freetextFields = null;
     }
 
     public Form2SparqlService(String[] pr, String[] ff) {
 		prefixes = new ArrayList<String>(Arrays.asList(pr));
         if (ff != null) {
             freetextFields = new ArrayList<String>(Arrays.asList(ff));
         } else {
             freetextFields = null;
         }
     }
 
     /**
 	 * Can be used to set the language of all content passed to the service.
 	 * 
 	 * @param lang
 	 *            A ISO 639 string.
 	 */
 	public void setLanguage(String lang) {
 		language = lang;
 	}
 
 	/**
 	 * Returns the language set for all strings.
 	 * 
 	 * @return A ISO 639 string.
 	 */
 	public String getLanguage() {
 		return language;
 	}
 	
 	/**
 	 * Returns the Subject URI created or used by the SPARQL Update query just created.
 	 * 
 	 * @return a String containing a URI.
 	 */
 	public String getURI() {
 		return SparulSubjectURI;
 	}
 
 	/**
 	 * Returns a string of prefixes as used in N3 based on the array set in the
 	 * constructor.
 	 * 
 	 * @return a string of prefixes as used in N3.
 	 */
 	public String getPrefixString() {
 		StringBuffer res = new StringBuffer();
 		for (String prefix : prefixes) {
 			res.append("PREFIX " + prefix + "\n");
 		}
 		return res.toString();
 	}
 
     /**
 	 * Adds a prefix to the list of prefixes 
 	 *
 	 * @param prefix
 	 *        A string with the prefix declaration
 	 */
      public void addPrefix(String prefix) { prefixes.add(prefix); }
 
 
     /**
       * Adds a freetext field to the list of freetext fields
       *
       * @param freetextField
       *        A string with the freetextField declaration
       */
 
 
       public void addFreetextField(String freetextField) {
         if (freetextFields == null) {
             freetextFields = new ArrayList<String>(Arrays.asList(freetextField));
         } else {
             freetextFields.add(freetextField);
         }
       }
 
 
 	/**
 	 * Takes a key with corresponding values and returns an N3 representation
 	 * based on certain assumptions about the nature of the data. <p/> The value
 	 * is always the object of the resulting triple, but often, it doesn't have
 	 * a direct relationship to its subject. For example, a <tt>dc:subject</tt>
 	 * may not be string, but is related through a rdfs:label, like
 	 * 
 	 * <pre>
 	 * 				?resource	dc:subject 	?var .
 	 * 				?var		rdfs:label	&quot;Foo&quot; .
 	 * </pre>
 	 * 
 	 * <p/> The key for this structure is created by taking the property names
 	 * and separate them with a /, e.g.
 	 * 
 	 * <pre>
 	 * 			dc:subject/rdfs:label
 	 * </pre>
 	 * 
 	 * <p/> For this key, you may give "Foo" as the value, which will return the
 	 * above example.
 	 * 
 	 * @return N3 with the result.
 	 * @param key
 	 *            The key as described above.
 	 * @param values
 	 *            A string array containing values for the key.
 	 */
 	public String convertFormField2N3(String key, String[] values) {
 		StringBuffer n3Buffer = new StringBuffer();
 		String[] keys = key.split("/");
 		for (String value : values) { // TODO low-pri: Optimize to comma-separate values.
 			String var = "?resource "; // The first SPARQL variable will always be resource
 			int j = 0;
 			for (String qname : keys) {
 				j++;
                 if ("dct:subject/all-labels".equals(key) && "all-labels".equals(qname)) {
                     logger.debug("Will expand the search to include all labels");
                     RDFObject myRDFObject = new RDFObject(value, language);
                     String thisObjectString = null;
                     if (freetextFields != null && freetextFields.contains("dct:subject/all-labels"))  {
                     	int freetextNo = freetextFields.indexOf(key)+1;
                     	n3Buffer.append("\n?free" + freetextNo + " pf:textMatch '+" + value + "' .");
                     	thisObjectString = "?free" + freetextNo + " .";
 					} else {
 						thisObjectString = myRDFObject.toN3();
 					}
                     n3Buffer.append("\nOPTIONAL {\n?resource dct:subject " + var +".\n"+ var +"skos:prefLabel ");
                     n3Buffer.append(thisObjectString);
                     n3Buffer.append(" }\nOPTIONAL {\n?resource dct:subject " + var +".\n"+ var +"skos:altLabel ");
                     n3Buffer.append(thisObjectString);
                     n3Buffer.append(" }\nOPTIONAL {\n?resource dct:subject " + var +".\n"+ var +"skos:hiddenLabel ");
                     n3Buffer.append(thisObjectString);
                     n3Buffer.append(" }\nFILTER ( bound( "+ var +") )\n");
                } else if (!"dct:subject".equals(qname)) {
                     n3Buffer.append("\n" + var + qname + " ");
                 }
                 if ("".equals(value)) { // Then, it is a block with no value, which will be caught by a catch-all
 					return "\n";
 				}
 				if (!subjectVarList.contains(var)) {
 					subjectVarList.add(var);
 				}
 
                 if (!"all-labels".equals(qname)) {
                     if (keys.length == j && !"".equals(value)) {
                         // Then we are on the actual form input value
                         RDFObject myRDFObject = new RDFObject(value, language);
                         if (freetextFields != null)  {
                             myRDFObject.setFreetext(freetextFields.indexOf(key)+1);
                         }
                         n3Buffer.append(myRDFObject.toN3());
                     } else { // Then we have to connect the object of this
                         // statement to the subject of the next
                         var = "?var" + variablecount + " "; // Might need more work
                         // to ensure uniqueness
                         logger.debug("Using unique N3 variable " + var);
                        if(!"dct:subject".equals(qname)) {
                         	n3Buffer.append(var + ".");
                         }
                         variablecount++;
                     }
                 }
 			}
 		}
 		logger.trace("Returning N3: " + n3Buffer.toString());
 		return n3Buffer.toString();
 	}
 
 	/**
 	 * Returns a full SPARQL DESCRIBE query based on a key-value Map. See above
 	 * for an explanation of the structure of each key-value. In addition to the
 	 * above described key-value-pairs, it may have a key
 	 * <tt>interface-language</tt> that holds the language of any literal.
 	 * 
 	 * @param parameterMap
 	 *            The data structure with the key-value-pairs.
 	 * @return A full SPARQL DESCRIBE query.
 	 */
 	public String convertForm2Sparql(Map<String, String[]> parameterMap) {
 
 		// Using StringBuffer, since regular String can cause performance issues
 		// with large datasets
 		StringBuffer sparqlQueryBuffer = new StringBuffer();
 		ArrayList n3List = new ArrayList();
 		sparqlQueryBuffer.append("DESCRIBE ");
 
 		if (parameterMap.get("interface-language") != null) {
 			setLanguage(parameterMap.get("interface-language")[0]);
 			parameterMap.remove("interface-language");
 		}	
 	
 		// locale is a parameter used by Cocoon's
 		// LocaleAction. It must also be removed from the
 		// parametermap if present.  
 
 		// If the interface-language from the locale is used,
 		// it is set elsewhere.
 		if (parameterMap.get("locale") != null) {
 		    parameterMap.remove("locale");
 		}	
 
         if (parameterMap.get("dct:subject/all-labels") != null) { // Then there are SKOS labels
            addPrefix("skos: <http://www.w3.org/2004/02/skos/core#>");
         }
 
 
 
         if (parameterMap.get("searchstring") != null) { // Then it is a simple freetext search
 		    sparqlQueryBuffer.append("?subject ?publisher ");
 
             //Do deep search in external resources or not
             boolean deepsearch = false;
             if (parameterMap.get("deepsearch") != null && "deepsearch".equalsIgnoreCase(parameterMap.get("deepsearch")[0])) {
                 deepsearch = true;
                 addPrefix("sub: <http://xmlns.computas.com/sublima#>");
                 addPrefix("link: <http://www.w3.org/2007/ont/link#>");
                 sparqlQueryBuffer.append("?request ");
                 parameterMap.remove("deepsearch");
                logger.debug("SUBLIMA: Deep search enabled");
             }
             n3List.add(freeTextQuery(parameterMap.get("searchstring")[0], deepsearch));
 //		    logger.trace("n3List so far in freetext:\n"+n3List.toString());
 		    parameterMap.remove("searchstring");
 		}
 
 
 		if (freetextFields != null) {
             addPrefix("pf: <http://jena.hpl.hp.com/ARQ/property#>");
 		}
 	
 		
 		for (Map.Entry<String, String[]> e : parameterMap.entrySet()) {
 			n3List.add(convertFormField2N3(e.getKey(), e.getValue()));
 		}
 
 		// Add the variables to the query
 		for (Object element : subjectVarList) {
 			sparqlQueryBuffer.append((String) element);
 		}
 			
 		sparqlQueryBuffer.append("?rest WHERE {");
 		sparqlQueryBuffer.append(OptimizeTripleOrder(n3List));
 		sparqlQueryBuffer.append("\n?resource ?p ?rest .");
 		sparqlQueryBuffer.append("\n}");
 		sparqlQueryBuffer.insert(0, getPrefixString());
 		String returnString = sparqlQueryBuffer.toString();
 		System.out.println(returnString);
 		logger.trace("Constructed SPARQL query: \n" + returnString);
 		return returnString;
 	}
 
 	/**
 	 * Returns two SPARQL Update queries where the first deletes all statements 
 	 * with the given subject. It works based on a key-value Map, and the usual 
 	 * usage is to pass the query parameters from a HTTP request.
 	 * 
 	 * The subject can be given either directly by a URI in a key named <tt>the-resource</tt>,
 	 * or by sending a <tt>title-field</tt> key containing the name a key containing 
 	 * a user-given string, which will be stripped of accents and non-alphanumeric characters.
 	 * In the latter case <tt>subjecturi-prefix</tt> must also be given, and it should contain 
 	 * a valid URI which will be prepended to the above title, to give the full subject URI.
 	 * 
 	 * Different languages are supported. It may either be given as a key
 	 * <tt>interface-language</tt> that holds the language of any literal.
 	 * This may be overridden by giving the keys unique names, where one contains the literal, 
 	 * the other contains a Lingvoj language URI. See the test class for examples.
 	 * 
 	 * Only triples can be inserted, it does not support the path-like notation of
 	 * the DESCRIBE methods. The subject resource must be sent as a key named
 	 * <tt>the-resource</tt> 
 	 * 
 	 * @param parameterMap
 	 *            The data structure with the key-value-pairs.
 	 * @return A full SPARQL Update query.
 	 * @throws IOException if no subject can be found or constructed.
 	 */
 
 	public String convertForm2Sparul(Map<String, String[]> parameterMap)
 			throws IOException {
 
 		String language = new String();
 		if (parameterMap.get("interface-language") != null) {
 			language = parameterMap.get("interface-language")[0];
 			parameterMap.remove("interface-language");
 		}
 
 		// locale is a parameter used by Cocoon's
 		// LocaleAction. It must also be removed from the
 		// parametermap if present.  
 
 		// If the interface-language from the locale is used,
 		// it is set elsewhere.
 		if (parameterMap.get("locale") != null) {
 		    parameterMap.remove("locale");
 		}	
 
 
 		if (parameterMap.get("the-resource") != null) {
 			SparulSubjectURI = parameterMap.get("the-resource")[0];
 			parameterMap.remove("the-resource");
 		} 
 		else if (parameterMap.get("title-field") != null && parameterMap.get("subjecturi-prefix") != null) {
 			SearchService check = new SearchService();
 			Random rng = new Random();
 			String theTitle = Integer.toString(rng.nextInt(100000));
 			for (String field : parameterMap.get(parameterMap.get("title-field")[0])) {
 				if (! field.startsWith("http://www.lingvoj.org/lang/")) {
 					theTitle = field;
 				}
 			}
 			SparulSubjectURI = parameterMap.get("subjecturi-prefix")[0] + check.sanitizeStringForURI(theTitle);
 			parameterMap.remove("title-field");
 			parameterMap.remove("subjecturi-prefix");
 		}
 		else {
 			throw new IOException(
 					"The subject is given neither in the form of a 'the-resource' " +
 					"parameter or of a title-field and subjecturi-prefix combination.");
 		}
 
 		StringBuffer sparqlQueryBuffer = new StringBuffer();
 		sparqlQueryBuffer.append(getPrefixString());
 		sparqlQueryBuffer.append("DELETE { <"+ SparulSubjectURI +"> ?p ?o . ");
 		sparqlQueryBuffer.append("}\nWHERE { <"+ SparulSubjectURI +"> ?p ?o . }\n");
 		sparqlQueryBuffer.append("\nINSERT DATA {\n");
 		
 		for (Map.Entry<String, String[]> e : parameterMap.entrySet()) {
 			if (e.getValue() != null) {
 				String property = e.getKey();
 				if (property == property.split("-")[0]) { // Then we have normal triple
 					for (String value : e.getValue()) {	
 						if (!"".equalsIgnoreCase(value) && value != null) {
 							RDFObject myRDFObject = new RDFObject(value, language);
 							sparqlQueryBuffer.append("<" + SparulSubjectURI + "> " + property
 								+ " " + myRDFObject.toN3() + "\n");
 						}
 					}
 				} else { // Then, the language is included as an URI in one of the values
 					property = property.split("-")[0];
 					String object = new String();
 					for (String value : e.getValue()) {	
 						if (!"".equalsIgnoreCase(value) && value != null) {
 							if (value.startsWith("http://www.lingvoj.org/lang/")) {
 								language = value.substring(value.lastIndexOf("/")+1, value.length());
 							} else {
 								object = value;
 							}
 						}
 					}
 					if (!"".equalsIgnoreCase(object)) {	
 						RDFObject myRDFObject = new RDFObject(object, language);
 						sparqlQueryBuffer.append("<" + SparulSubjectURI + "> " + property	
 								+ " " + myRDFObject.toN3() + "\n");	
 					}
 				}	
 			}
 		}	
 		sparqlQueryBuffer.append("}\n");
 
 		String returnString = sparqlQueryBuffer.toString();
 		logger.trace("Constructed SPARUL query: " + returnString);
 		return returnString;
 	}
 	
 	/*
 	 * A method that will take a simple search string and return the triples need to do freetext search
 	 * 
 	 * @param searchstring
 	 *		 			The string to search for
 	 * @return	
 	 * 			A string with triples
 	 * 
 	 */
     public String freeTextQuery (String searchstring, boolean deepsearch) {
         if (!subjectVarList.contains("?resource ")) {
             subjectVarList.add("?resource ");
         }
         String result = StringUtils.join("\n", new String[]{
 					"\n  ?lit pf:textMatch ( '" + searchstring + "' 100) .",
 					"  {",
 					"    ?resource ?p1 ?lit;",
 					"              dct:subject ?subject ;",
 					"              dct:publisher ?publisher "});
         if (deepsearch) {
             result = result + ";\n              link:request ?request ";
         }
         result = result +             ".\n  }" +  StringUtils.join("\n", new String[]{
 					"\n  UNION",
 					"  {",
 					"      ?resource dct:subject ?subject1 .",
 					"      ?subject1 ?p2 ?lit .",
 					"      ?resource dct:subject ?subject ;",
 					"                dct:publisher ?publisher "});
         if (deepsearch) {
             result = result + ";\n                link:request ?request ";
         }
         result = result +             ".\n  }" +  StringUtils.join("\n", new String[]{
                     "\n  UNION",
 					"  {",
 					"      ?resource dct:publisher ?publisher1 .",
 					"      ?publisher1 ?p2 ?lit .",
 					"      ?resource dct:subject ?subject ;",
 					"                dct:publisher ?publisher "});
         if (deepsearch) {
             result = result + StringUtils.join("\n", new String[]{
                     ";\n                link:request ?request .",
                     "  }\n  UNION\n  {",
                     "      ?resource link:request ?request1 .",
                     "      ?request1 sub:stripped ?lit .",
                     "      ?resource dct:subject ?subject ;",
                     "                dct:publisher ?publisher ;",
                     "                link:request ?request "});
         }
         result = result + ".\n  }";
     
 
         return result;
     }
 
     private StringBuffer OptimizeTripleOrder(ArrayList <String>n3List) {
         StringBuffer ordered = new StringBuffer();
         for (String triple : n3List) {
             ordered.insert(0, triple);   
         }
 
         return ordered;
     }
 
 }
