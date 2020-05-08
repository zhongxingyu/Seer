 package com.computas.sublima.app.service;
 
 import com.computas.sublima.query.RDFObject;
 import com.computas.sublima.query.service.MappingService;
 import com.computas.sublima.query.service.SearchService;
 import com.computas.sublima.query.service.SettingsService;
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * A service class with methods to create a SPARQL DESCRIBE query from a
  * key-value structure. The constructor takes a mandatory array of prefix
  * declarations, on the form
  * <p/>
  * <pre>
  * 				prefix: &lt;URI&gt;
  * </pre>
  * <p/>
  * e.g., it may be called as myService = new Form2SparqlService(new
  * String[]{"dct: <http://purl.org/dc/terms/>", "foaf:
  * <http://xmlns.com/foaf/0.1/>"});
  * <p/>
  * This class has become rather complex, as it has to account for a very high number of
  * different situations
  * <p/>
  * It is important to note that the list of WHERE clause statements is built only once.
  * This means that if the query changes, one will have to create a new instance of this class.
  *
  * @author kkj
  * @version 1.0
  * @param: prefixes * string array with prefixes for names used.
  * @param: freetextFields
  * A List containing the fields that containing the fields
  * that needs to be treated as free-text-indexed fields.
  */
 public class Form2SparqlService {
 
   private static Logger logger = Logger.getLogger(Form2SparqlService.class);
 
   private String language;
 
   private String resourceSubject = "?resource ";
 
   private String SparulSubjectURI;
 
   private List<String> prefixes = new ArrayList<String>();
 
   private List<String> subjectVarList = new LinkedList<String>();
 
   private int variablecount = 1; // the var below has to be unique across calls
 
   private List<String> freetextFields = new ArrayList<String>();
 
   private ArrayList<String> n3List = null;
 
 
   private boolean truncate = true;
 
   boolean deepsearch = false;
 
   private String archiveuri;
 
   private MappingService mapping = new MappingService();
 
 
   public Form2SparqlService(String[] pr) {
     prefixes = new ArrayList<String>(Arrays.asList(pr));
     freetextFields = null;
     this.archiveuri = "<" + SettingsService.getProperty("sublima.basegraph") + ">";
   }
 
   public Form2SparqlService(String[] pr, String[] ff) {
     prefixes = new ArrayList<String>(Arrays.asList(pr));
     this.archiveuri = "<" + SettingsService.getProperty("sublima.basegraph") + ">";
     if (ff != null) {
       freetextFields = new ArrayList<String>(Arrays.asList(ff));
     } else {
       freetextFields = null;
     }
   }
 
   /**
    * Can be used to set the language of all content passed to the service.
    *
    * @param lang A ISO 639 string.
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
    * Can be used to set the resource string, i.e. the "top subject" of the query. If not
    * set, it will default to "?resource"
    *
    * @param subject The subject string
    */
   public void setResourceSubject(String subject) {
     resourceSubject = subject + " ";
   }
 
   /**
    * Returns the resourceSubject
    *
    * @return the subject string
    */
   public String getResourceSubject() {
     return resourceSubject.trim();
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
     StringBuilder res = new StringBuilder();
     for (String prefix : prefixes) {
       res.append("PREFIX " + prefix + "\n");
     }
     return res.toString();
   }
 
   /**
    * Adds a prefix to the list of prefixes
    *
    * @param prefix A string with the prefix declaration
    */
   public void addPrefix(String prefix) {
     prefixes.add(prefix);
   }
 
 
   /**
    * Adds a freetext field to the list of freetext fields
    *
    * @param freetextField A string with the freetextField declaration
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
    * <p/>
    * <pre>
    * 				?resource	dc:subject 	?var .
    * 				?var		rdfs:label	&quot;Foo&quot; .
    * </pre>
    * <p/>
    * <p/> The key for this structure is created by taking the property names
    * and separate them with a /, e.g.
    * <p/>
    * <pre>
    * 			dc:subject/rdfs:label
    * </pre>
    * <p/>
    * <p/> For this key, you may give "Foo" as the value, which will return the
    * above example.
    *
    * @param key    The key as described above.
    * @param values A string array containing values for the key.
    * @return N3 with the result.
    */
   public String convertFormField2N3(String key, String[] values) {
     StringBuilder n3Buffer = new StringBuilder();
     String[] keys = key.split("/");
     String var = resourceSubject; // The first SPARQL variable will always be resource
     int j = 0;
     for (String qname : keys) {
       j++;
         n3Buffer.append("\n" + var + qname + " ");
       if ("".equals(values[0])) { // Then, it is a block with no value, which will be caught by a catch-all
         return "\n";
       }
       if (!subjectVarList.contains(var)) {
         subjectVarList.add(var);
       }
 
         if (keys.length == j && !"".equals(values[0])) {
           // Then we are on the actual form input value
           int i = 0;
           for (String value : values) {
             i++;
             if (value == null) {
               if (i == 1) {
                 n3Buffer.append("?object" + values.length);
                 n3Buffer.append(" .");
               }
             } else {
               RDFObject myRDFObject = new RDFObject(value, language);
               if (freetextFields != null) {
                 myRDFObject.setFreetext(freetextFields.indexOf(key) + 1);
               }
               n3Buffer.append(myRDFObject.toN3());
               if (i == values.length) {
                 n3Buffer.append(" .");
               } else {
                 n3Buffer.append(", ");
               }
             }
           }
         } else { // Then we have to connect the object of this
           // statement to the subject of the next
           var = "?var" + variablecount + " "; // Might need more work
           // to ensure uniqueness
           logger.debug("Using unique N3 variable " + var);
           n3Buffer.append(var + ".");
           variablecount++;
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
    * @param parameterMap The data structure with the key-value-pairs.
    * @return A full SPARQL DESCRIBE query.
    */
   public String convertForm2Sparql(Map<String, String[]> parameterMap) {
 
     // Using StringBuilder, since regular String can cause performance issues
     // with large datasets
     StringBuilder sparqlQueryBuffer = new StringBuilder();
     sparqlQueryBuffer.append("DESCRIBE ");
 
     ArrayList<String> n3List = getN3List(parameterMap);
     // Add the variables to the query
     for (String element : subjectVarList) {
       sparqlQueryBuffer.append(element);
     }
     sparqlQueryBuffer.append("?rest WHERE {");
     sparqlQueryBuffer.append(OptimizeTripleOrder(n3List));
     sparqlQueryBuffer.append("\n" + resourceSubject + "?p ?rest .");
     sparqlQueryBuffer.append("\n}");
     sparqlQueryBuffer.insert(0, getPrefixString());
     String returnString = sparqlQueryBuffer.toString();
     //logger.trace("Constructed SPARQL query: \n" + returnString);
     return returnString;
   }
 
 
   /**
    * Returns a SPARQL query which can be used to determine if there is a certain number of hits to a query.
    * More concretely, the query will return the URI of the cutoff-ths resource.
    * This is based on a key-value Map. See above for an explanation of the structure of each
    * key-value. In addition to the above described key-value-pairs, it may have a key
    * <tt>interface-language</tt> that holds the language of any literal.
    * <p/>
    * When running this query, one will get the number of resources that would be
    * returned using this WHERE clause.
    *
    * @param parameterMap The data structure with the key-value-pairs.
    * @param cutoff       int the number of hits
    * @return A SPARQL count query.
    */
   public String convertForm2SparqlCount(Map<String, String[]> parameterMap, int cutoff) {
 
     // Using StringBuilder, since regular String can cause performance issues
     // with large datasets
     StringBuilder sparqlQueryBuffer = new StringBuilder();
     sparqlQueryBuffer.append("SELECT DISTINCT ?resource ");
     ArrayList<String> n3Listtmp = getN3List(parameterMap);
     if (n3Listtmp.size() < 1) {
       logger.info("convertForm2SparqlCount had no triples in the WHERE clause, avoiding getting the whole database.");
       return null;
     }
     ArrayList<String> n3List = new ArrayList<String>();
     sparqlQueryBuffer.append("WHERE {");
     for (String triple : n3Listtmp) {
         n3List.add(triple);
     }
 
     sparqlQueryBuffer.append(OptimizeTripleOrder(n3List));
     sparqlQueryBuffer.append("\n}\nOFFSET " + (cutoff - 1) + " LIMIT 1");
     sparqlQueryBuffer.insert(0, getPrefixString());
     String returnString = sparqlQueryBuffer.toString();
     //logger.trace("Constructed SPARQL query: \n" + returnString);
     return returnString;
   }
 
   public String convertForm2SparqlMoreThanZeroHits(Map<String, String[]> parameterMap) {
 
     // Using StringBuilder, since regular String can cause performance issues
     // with large datasets
     StringBuilder sparqlQueryBuffer = new StringBuilder();
     sparqlQueryBuffer.append("SELECT DISTINCT ?resource ");
     ArrayList<String> n3Listtmp = getN3List(parameterMap);
     if (n3Listtmp.size() < 1) {
       logger.info("convertForm2SparqlCount had no triples in the WHERE clause, avoiding getting the whole database.");
       return null;
     }
     ArrayList<String> n3List = new ArrayList<String>();
     sparqlQueryBuffer.append("WHERE {");
     sparqlQueryBuffer.append("?resource a sub:Resource .\n");
     for (String triple : n3Listtmp) {
       // This is a hack to optimize the count a bit by leaving out the check for approved resources.
       if (!triple.contains("status/godkjent_av_administrator")) {
         n3List.add(triple);
       }
     }
     sparqlQueryBuffer.append(OptimizeTripleOrder(n3List));
     sparqlQueryBuffer.append("\n}\nLIMIT 1");
     sparqlQueryBuffer.insert(0, getPrefixString());
     String returnString = sparqlQueryBuffer.toString();
     //logger.trace("Constructed SPARQL query: \n" + returnString);
     return returnString;
   }
 
   /**
    * Appends most of the core SPARQL WHERE clause and a bit more to the querybuffer
    *
    * @param parameterMap
    */
   private ArrayList<String> getN3List(Map<String, String[]> parameterMap) {
 
     if (n3List == null) {
       n3List = new ArrayList<String>();
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
 
       if (parameterMap.get("dct:subject/sub:literals") != null) { // Then there are SKOS labels
         addPrefix("skos: <http://www.w3.org/2004/02/skos/core#>");
       }
 
 
       if (parameterMap.get("searchstring") != null) { // Then it is a simple freetext search
         //Do deep search in external resources or not
 
         addPrefix("sub: <http://xmlns.computas.com/sublima#>");
         if ((parameterMap.get("deepsearch") != null && "deepsearch".equalsIgnoreCase(parameterMap.get("deepsearch")[0])) || deepsearch) {
           deepsearch = true;
           parameterMap.remove("deepsearch");
           logger.debug("SUBLIMA: Deep search enabled");
         }
         n3List.add(freeTextQuery(parameterMap.get("searchstring")[0], deepsearch));
 //		    logger.trace("n3List so far in freetext:\n"+n3List.toString());
         parameterMap.remove("searchstring");
       }
 
       for (Map.Entry<String, String[]> e : parameterMap.entrySet()) {
         n3List.add(convertFormField2N3(e.getKey(), e.getValue()));
       }
 
       return n3List;
 
     } else {
       return n3List;
     }
   }
 
   /**
    * Returns two SPARQL Update queries where the first deletes all statements
    * with the given subject. It works based on a key-value Map, and the usual
    * usage is to pass the query parameters from a HTTP request.
    * <p/>
    * The subject can be given either directly by a URI in a key named <tt>the-resource</tt>,
    * or by sending a <tt>title-field</tt> key containing the name a key containing
    * a user-given string, which will be stripped of accents and non-alphanumeric characters.
    * In the latter case <tt>subjecturi-prefix</tt> must also be given, and it should contain
    * a valid URI which will be prepended to the above title, to give the full subject URI.
    * <p/>
    * Different languages are supported. It may either be given as a key
    * <tt>interface-language</tt> that holds the language of any literal.
    * This may be overridden by giving the keys unique names, where one contains the literal,
    * the other contains a Lingvoj language URI. See the test class for examples.
    * <p/>
    * Only triples can be inserted, it does not support the path-like notation of
    * the DESCRIBE methods.
    *
    * @param parameterMap The data structure with the key-value-pairs.
    * @return A full SPARQL Update query.
    * @throws IOException if no subject can be found or constructed.
    */
 
   public String convertForm2Sparul(Map<String, String[]> parameterMap) throws IOException {
 
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
     } else if (parameterMap.get("title-field") != null && parameterMap.get("subjecturi-prefix") != null) {
       SearchService check = new SearchService();
       Random rng = new Random();
       String theTitle = Integer.toString(rng.nextInt(100000));
       for (String field : parameterMap.get(parameterMap.get("title-field")[0])) {
         if (!field.startsWith("http://www.lingvoj.org/lang/")) {
           theTitle = field;
         }
       }
       SparulSubjectURI = parameterMap.get("subjecturi-prefix")[0] + check.sanitizeStringForURI(theTitle);
       parameterMap.remove("title-field");
       parameterMap.remove("subjecturi-prefix");
     } else {
       throw new IOException(
               "The subject is given neither in the form of a 'the-resource' " +
                       "parameter or of a title-field and subjecturi-prefix combination.");
     }
 
     if (archiveuri.isEmpty()) throw new IOException("No archive is given. Cannot insert or delete without a given archive uri.");
 
     StringBuilder sparqlQueryBuffer = new StringBuilder();
     sparqlQueryBuffer.append(getPrefixString());
     sparqlQueryBuffer.append("DELETE FROM GRAPH " + archiveuri + " { <" + SparulSubjectURI + "> ?p ?o . }\n WHERE { <" + SparulSubjectURI + "> ?p ?o . }\n");
     sparqlQueryBuffer.append("\nINSERT INTO GRAPH " + archiveuri + " {\n");
 
     for (Map.Entry<String, String[]> e : parameterMap.entrySet()) {
       if (e.getValue() != null) {
         String property = e.getKey();
         if (property.equals(property.split("-")[0]) || (property.startsWith("<") && property.endsWith(">"))) { // Then we have normal triple
           for (String value : e.getValue()) {
             value = value.trim();
             if (!"".equalsIgnoreCase(value) && value != null) {
               RDFObject myRDFObject = new RDFObject(value, language);
               sparqlQueryBuffer.append("<" + SparulSubjectURI + "> " + property
                       + " " + myRDFObject.toN3() + " .\n");
             }
           }
         } else { // Then, the language is included as an URI in one of the values
           property = property.split("-")[0];
           String object = new String();
          String lang = null;
           for (String value : e.getValue()) {
             if (!"".equalsIgnoreCase(value) && value != null) {
               if (value.startsWith("http://www.lingvoj.org/lang/")) {
                lang = value.substring(value.lastIndexOf("/") + 1, value.length());
               } else {
                 object = value.trim();
               }
             }
           }
           if (!"".equalsIgnoreCase(object)) {
            RDFObject myRDFObject = new RDFObject(object, (lang != null ? lang : language));
             sparqlQueryBuffer.append("<" + SparulSubjectURI + "> " + property
                     + " " + myRDFObject.toN3() + " .\n");
           }
         }
       }
     }
     sparqlQueryBuffer.append("}\n");
 
     String returnString = sparqlQueryBuffer.toString();
     //logger.trace("Constructed SPARUL query: " + returnString);
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
   public String freeTextQuery(String searchstring, boolean deepsearch) {
     if (!subjectVarList.contains(resourceSubject)) {
       subjectVarList.add(resourceSubject);
     }
     //searchstring = mapping.charactermapping(searchstring);
 
     String result = "";
 
     if (deepsearch) {
       result = result + "\n?resource sub:externalliterals ?lit .";
     } else {
       result = result + "\n?resource sub:literals ?lit .";
       //result = result + "\n?resource dct:identifier ?lit ."; // for test
     }
 
     result = result + "\n?lit <bif:contains> \"\"\"" + searchstring + "\"\"\" .";
     return result;
   }
 
   private StringBuilder OptimizeTripleOrder(ArrayList<String> n3List) {
     StringBuilder ordered = new StringBuilder(); // Stuff that isn't ordered any special way goes here
     StringBuilder start = new StringBuilder(); // Stuff that should be in the start of the query goes here
     StringBuilder end = new StringBuilder(); // Stuff that should end the query goes here.
 
     for (String triple : n3List) {
       if (triple.contains("bif:contains") && (!triple.contains("skos:prefLabel"))) {
         if (triple.contains("?lit ")) {
           start.insert(0, triple);
         } else {
           // In this case, it isn't actually a triple, and the freetext thing needs to go first
           List<String> tmplist = Arrays.asList(triple.split("\n"));
           Collections.reverse(tmplist);
           start.insert(0, triple);
           //    start.insert(0, "\n");
         }
       } else if (triple.contains("dct:language")) {
         end.append(triple);
       } else if (triple.contains("describedBy")) {
         end.append(triple);
       } else {
         ordered.insert(0, triple);
       }
     }
     ordered.insert(0, start);
     ordered.append(end);
     return ordered;
   }
 
   public void setTruncate(boolean truncate) {
     this.truncate = truncate;
   }
 
 
   public void setArchiveuri(String archiveuri) {
     if (!archiveuri.startsWith("<") && !archiveuri.endsWith(">")) {
       this.archiveuri = "<" + archiveuri + ">";
     } else {
       this.archiveuri = archiveuri;
     }
 
   }
 
   public void setDeepsearch(boolean deepsearch) {
       this.deepsearch = deepsearch;
   }
 
   public void nullN3List() {
       this.n3List = null;
   }
 
 }
