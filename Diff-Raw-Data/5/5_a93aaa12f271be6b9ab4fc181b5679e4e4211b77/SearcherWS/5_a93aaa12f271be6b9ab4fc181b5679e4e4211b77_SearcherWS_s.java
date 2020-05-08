 package org.vle.aid.lucene;
 
 /**
  * This is the implementation bean class for the SearchLucene web service.
  *
  * TODO: URI-style file locater, SRB
  * TODO: Get analyzer type and more from index configfile
  *
  *
  * @author emeij
  */
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.logging.Logger;
 import java.math.BigDecimal;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 import java.util.regex.Pattern;
 import org.vle.aid.lucene.tools.Snippet;
 import org.apache.lucene.analysis.util.WordlistLoader;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.analysis.util.CharArraySet;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexableField;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
 import org.apache.lucene.queryparser.classic.ParseException;
 import org.apache.lucene.search.MultiTermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.WildcardQuery;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 import org.apache.xmlbeans.XmlOptions;
 import org.vle.aid.FieldType;
 import org.vle.aid.ResultDocument;
 import org.vle.aid.ResultType;
 import org.vle.aid.lucene.tools.Thumbnails;
 
 
 /**
  * Main class which ...
  *
  * @author  Edgar Meij
  * @version $Revision$, $Date$
  */
 public class SearcherWS {
 
   private Analyzer analyzer;
   private IndexSearcher searcher;
   
   private File indexLocation;
   private String index;
   private CharArraySet stopwords = new CharArraySet(Version.LUCENE_41, 100, true);
   private XmlOptions xmlOpts   = new XmlOptions();
 
   /** logger for Commons logging. */
   private transient Logger log =
     Logger.getLogger("SearcherWS.class.getName()");
 
   /**
    * The max number of hits this class will ever return
    */
   public final int numMaxHits = 1000;
   
   /**
    * The default max number if none is specified
    */
   public final int defaultMaxHits = 10;
   
   private int checkInt (String integerString, int defaultMaxHits) {
     int intMaxHits;
         // Check value of maxHits
         try { intMaxHits  = Integer.parseInt(integerString); }
             catch (NumberFormatException e) { intMaxHits = -1; }
 
         if (intMaxHits < 0 || intMaxHits > numMaxHits)
             intMaxHits = defaultMaxHits;
 
     return intMaxHits;
   }
 
   /**
    * Returns the set INDEXDIR
    * 
    * @return
    */
   public String getIndexLocation() {
     return indexLocation.getPath();
   }
 
   @Deprecated
   public void setIndexLocation(String indexLocation) {
     this.indexLocation = new File(indexLocation);
     log.info("indexLocation: " +indexLocation);
   }
 
   public void setIndexLocation(File indexLocation_) {
 	  this.indexLocation = indexLocation_;
     log.info(String.format("indexLocation: %s", indexLocation.getPath()));
   }
   
   public void setIndexLocationRelative(String dir) {
 
     File INDEXDIR = new File(System.getenv("INDEXDIR"));
 
     if (INDEXDIR == null) {
         log.severe("***INDEXDIR not found!!!***");
         INDEXDIR = new File("");
     } else {
         log.info("Found INDEXDIR: " + INDEXDIR);
     }
     
     this.index = dir;
     setIndexLocation(new File(INDEXDIR,dir));
   }
 
   /**
    * @return array of stopwords that the {@link StandardAnalyzer} uses.
    * @deprecated	prefer {@link #getStopwords(boolean) }, which returns an
    * 	{@link CharArraySet}.
    */
   @Deprecated
   public String[] getStopwords() {
 	  String result[] = new String[this.stopwords.size()];
 	  return this.stopwords.toArray(result);
   }
 
   /**
    * Replace the stopwords that {@link #analyzer} uses with {@code stopwords}.
    * And replace {@link #analyzer} with one that uses these stopwords.
    * 
    * @param stopwords	array of stopwords to use
    * 
    * @deprecated	prefer {@link #setStopwords(org.apache.lucene.analysis.util.CharArraySet) },
    * 	which uses an {@link CharArraySet}.
    */
   @Deprecated
   public void setStopwords(String[] stopwords) {
 	  Collection<String> c = Arrays.asList(stopwords);
 	  this.setStopwords(new CharArraySet(Version.LUCENE_41, c, true));
   }
 
   /**
    * Retrieve the stopwords used (as in {@link StandardAnalyzer#StandardAnalyzer(org.apache.lucene.util.Version, org.apache.lucene.analysis.util.CharArraySet) }
    * 
    * @param notused
    * 
    * @return the {@link CharArraySet} containing the stopwords.
    */
   public CharArraySet getStopwords(boolean notused) {
 	  return this.stopwords;
   }
 
   /**
    * Replace stopwords that {@link #analyzer} uses with {@code newStopwords}.
    * And replace {@link #analyzer} with one that uses these stopwords.
    * 
    * @param newStopwords 	<ul><li>the stop words to replace the previous set of 
    * 			stopwords with; or</li>
    * 		<li>{@code null} replace the analyser with a {@link StandardAnalyzer}
    * 		without any stopwords.</li></ul>
    */
   public void setStopwords(CharArraySet newStopwords) {
   	  if(newStopwords != null) {
 		  if(this.stopwords != newStopwords) {
 			  this.stopwords.retainAll(newStopwords);
 			  this.stopwords.addAll(newStopwords);
 			  this.analyzer = new StandardAnalyzer(Version.LUCENE_41, this.stopwords);
 		  }
 	  } else {
 		  this.analyzer = new StandardAnalyzer(Version.LUCENE_41);
 	  }
   }
 
   /**
    * Read stopwords from file ({@code stopwords.txt}) in {@link #indexLocation}.
    * 
    * @throws IOException 
    */
   public void setStopwords() throws IOException {
 
     File stopwordsFile = new File(indexLocation, "stopwords.txt");
     
     if (stopwordsFile == null || !stopwordsFile.exists() || !stopwordsFile.canRead()) {
       // throw new IllegalArgumentException("can't read stopword file " + stopwords);
       log.info("Can't read default stopwords file: " + stopwordsFile.getAbsolutePath());
 	  setStopwords((CharArraySet)null);
     } else {
       // No 1.5 on Mac os x...
       // HashSet <String> stopSet = new HashSet <String> ();
       CharArraySet stopSet = new CharArraySet(Version.LUCENE_41, 100, true);
       stopSet = WordlistLoader.getWordSet(new FileReader(stopwordsFile), Version.LUCENE_41);
 	  setStopwords(stopSet);
     }
   }
   
 /**
  * Call lucene and search for {@code query} in {@code indexDir}.
  * 
  * Side effect set {@link #searcher}
  * 
  * @see SearcherWSTest
  * 
  * @param indexDir		the Lucene index {@link Directory} to search in.
  * @param query			the query to search for.
  * @param numMaxHits	the number of maximum hits to limit 
  * 		{@link IndexSearcher#search(Query, int)}.
  * 
  * @return	the {@link TopDocs} that {@link IndexSearcher#search(Query, int)} returns.
  * 
  * @throws IOException	 When {@code indexDir} does not contain an index.
  */
 TopDocs _search (Query query) throws IOException {
 	if(indexLocation == null || !indexLocation.exists()) {
 		throw new Error(
 				new IllegalStateException("Set indexLocation before calling _search"));
 	}
 	
     Directory indexDir = FSDirectory.open(indexLocation); 
 	if (DirectoryReader.indexExists(indexDir)) {
 		searcher = new IndexSearcher(DirectoryReader.open(indexDir));
 		TopDocs result = searcher.search(query, numMaxHits);
 
 	return result;
 	} else {
 	throw new IOException("No index found in " + indexDir.toString());
 	}
   }
   
   ResultType makeXML(TopDocs hits, int intMaxHits) throws IOException {
 	if(searcher == null) {
 		throw new Error(
 				new IllegalStateException("Set searcher before calling makeXML"));
 	}  
 	
     xmlOpts.setSavePrettyPrint();
     xmlOpts.setSavePrettyPrintIndent(2);
     xmlOpts.setSaveUseOpenFrag();
     xmlOpts.setSaveNamespacesFirst();
     xmlOpts.setCharacterEncoding("UTF-8");
 
     // Create a new result document.
     ResultDocument resultDoc = ResultDocument.Factory.newInstance();
     xmlOpts.setSaveSyntheticDocumentElement(resultDoc.type.getElementProperties()[0].getName());
 
     // Create a new instance of ResultType and set the values of its child elements.
     ResultType result = resultDoc.addNewResult();
     log.info("hits: " + hits.totalHits);
 
     for (int i = 0; i < Math.min(intMaxHits, hits.totalHits); i++) {
       
       org.vle.aid.Document doc = result.addNewDoc();
      List<IndexableField> fields = searcher.doc(i).getFields();
       
       for (Iterator<IndexableField> it = fields.iterator(); it.hasNext();) {
         IndexableField f = it.next();
         FieldType fieldType = doc.addNewField();
 
         fieldType.setName(f.name());
         fieldType.setValue(escape(f.stringValue()));
         
         if (f.name().equalsIgnoreCase("PMID") || f.name().equalsIgnoreCase("ID")) 
           doc.setUrl("http://localhost:80/search/item/"+f.stringValue()+"?index="+this.index);
       }
 
       // Add Lucene Document ID
       FieldType fieldType = doc.addNewField();
       fieldType.setName("LuceneDocID");
       fieldType.setValue(String.valueOf(hits.scoreDocs[i].doc));
 
       doc.setRank(i + 1);
       doc.setScore(new BigDecimal(hits.scoreDocs[i].score));
       log.info("doc: " + doc.getUrl());
     }
 
     result.setTotal(hits.totalHits);
 
     return result;
   }
 
   /**
    * Searches a given Lucene index for a given Multifielded query
    * @param index the name of the index to search
    * @param queryString a String containing the query terms.
    * @param maxHits the number of hits the searcher will return (with a max of 1000)
    * @param defaultField the fields of the index Lucene searches in
    * @param operator DefaultOperator for Multifielded query
    * @return a String with XML'ified results
   */
   public String searchMFquery(String index, String queryString, String maxHits, String[] defaultField, String operator) {
 
     Date start = new Date();
 
     // Do some String[] preprocessing to play nice with Kepler
     String[] tempFields = defaultField;
     for (int z=0; z<defaultField.length; z++) {
       defaultField[z] = tempFields[z].replaceAll("\"","").replaceAll("&quot;","");
       log.info("Field" + z + ": [" + defaultField[z] + "]");
     }
 
     int intMaxHits = checkInt(maxHits, defaultMaxHits);
     setIndexLocationRelative(index);
     
 
     try {
 
       setStopwords();          
 
       MultiFieldQueryParser parser =
               new MultiFieldQueryParser(Version.LUCENE_41, defaultField, analyzer);
 
       if (operator.equalsIgnoreCase("or"))
           parser.setDefaultOperator(MultiFieldQueryParser.OR_OPERATOR);
       else
           parser.setDefaultOperator(MultiFieldQueryParser.AND_OPERATOR);
 
       Query queryTerms = parser.parse(queryString);
       TopDocs hits = this._search(queryTerms);
 
       ResultType result = makeXML(hits, intMaxHits);
       result.setQuery(queryTerms.toString());
 
       Date end = new Date();
       double duration = (end.getTime() - start.getTime())/1000;
       result.setTime(new BigDecimal(duration));
 
       log.info("Query: " + queryString + ", index: " + index + ", " +
           hits.totalHits + " documents, time taken: " + duration + " seconds");
 
       ByteArrayOutputStream destStream = new ByteArrayOutputStream();
       result.save(destStream, xmlOpts);
       destStream.close();
       return destStream.toString("UTF-8");
 
     } catch (ParseException e) {
       log.severe("ParseException: " + e.toString());
       return e.toString();
     } catch(IOException e) {
       StringWriter sw = new StringWriter();
       PrintWriter pw = new PrintWriter(sw);
       e.printStackTrace(pw);
       log.severe(sw.toString());
       return e.toString();
     }
   }
 
     /**
      * Searches a given Lucene index for a given Multifielded query
      * @param index the name of the index to search
      * @param queryString a String containing the query terms.
      * @param startString starts with
      * @param defaultField the fields of the index Lucene searches in
      * @param countString how many
      * @return a String with JASON-style results
     */
     public String searchJason(String index, String queryString, String startString, String defaultField, String countString) {
 
       int start = checkInt(startString, defaultMaxHits);
       int count = checkInt(countString, defaultMaxHits);
         
       setIndexLocationRelative(index);
       
       final String CACHE_PREFIX = getIndexLocation() + File.separator + "cache" + File.separator;
       Vector<File> thumbnails = new Vector<File>();
       
       String json = "{\n";
       
       try {
         setStopwords();
         
         String newQueryString;
         Query queryTerms;
         Snippet snip;
 
         // TODO: debug several escaped characters
         queryString = queryString.replaceAll("(%3F)", "?");
         // queryString = QueryParser.escape(queryString);
 
         QueryParser parser = new QueryParser(Version.LUCENE_41, defaultField, analyzer);
         parser.setDefaultOperator(QueryParser.OR_OPERATOR);
         queryTerms = parser.parse(queryString);
         newQueryString = queryTerms.toString();
         
 		Directory indexDir = FSDirectory.open(indexLocation);
         if (DirectoryReader.indexExists(indexDir))
           searcher = new IndexSearcher(DirectoryReader.open(indexDir));
         else
           throw new IOException("No index found in " + indexLocation);
     
         TopDocs hits = searcher.search(queryTerms, numMaxHits);
         queryTerms = searcher.rewrite(queryTerms);        
 
         snip = new Snippet(queryTerms, searcher, analyzer, "content");
 
         log.info("Received JSON request: \""+ queryString +"\", hits: " + hits.totalHits);
         json += "\t\"types\": {\"Result\" : {\"pluralLabel\": \"Results\"}},\n";
         json += "\t\"hits\" : \""+hits.totalHits+"\",\n";
         json += "\t\"query\" : \"" + newQueryString + "\",\n";
         json += "\n\t\"items\" : [";
 
 
         for (int i = (start); i < Math.min((start + count), hits.totalHits); i++) {
 
           HashMap values_map = new HashMap();
           json += "\n\t{\n\t\t\"type\" : \"Result\", ";
 
           // avoid mutliple required fields
           boolean _hasid      = false;
           boolean _hascontent = false;
           // not required, but convenient
           boolean _hastitle = false;
           boolean _haspath = false;
           String id = "";
 
           json += "\n\t\t\"score\" : \"" + hits.scoreDocs[i].score + "\",";
 
           // first iteration looks for duplicate fields
		  List<IndexableField> fields = searcher.doc(i).getFields();
           for (Iterator<IndexableField> it = fields.iterator(); it.hasNext();) {
             IndexableField f = it.next();
 
             if (values_map.containsKey(f.name())) {
               Vector v = (Vector) values_map.get(f.name());
               v.add(f.stringValue());
               values_map.put(f.name(), v);
             } else {
               Vector v = new Vector();
               v.add(f.stringValue());
               values_map.put(f.name(), v);
             }
           }
 
           Set set = values_map.keySet();
           Iterator iter = set.iterator();
           while (iter.hasNext()){
 
             String key = (String) iter.next();
 
             if (key.equalsIgnoreCase("fulltext"))
               continue;
 
             Vector v = (Vector) values_map.get(key);
 
             if ((key.equalsIgnoreCase("PMID") || key.equalsIgnoreCase("ID")) && ! _hasid ) {
 
               id = escapeJason((String) v.firstElement()) ;
               
               json += "\n\t\t\"id\" : \""+
                 //index+ "/" +
                 id + "\",";
               // Exhibit-specific
               json += "\n\t\t\"label\" : \"" + id + "\",";
               //json += "\n\t\t\"path\" : \"" + id  + "\",";
               json += "\n\t\t\"uri\" : \"http://localhost:80/search/item/" + id + "?index="+index+"\",";
               
               // Thumbnail todo list
               if (id.toLowerCase().endsWith("pdf")) {
                 thumbnails.add(new File(CACHE_PREFIX + id));
               }
               
               _hasid = true;
               
               
 
             } else if (key.equalsIgnoreCase("TI") || key.equalsIgnoreCase("title")) {
               json += "\n\t\t\"title\" : \"" + escapeJason((String) v.firstElement()) + "\",";
               _hastitle = true;
 
             } else if (key.equalsIgnoreCase("content") && ! _hascontent ) {
               //log.info("\tcontent: \"" + escapeJason((String) v.firstElement()));
               snip.setFIELD_NAME("content");
               json += "\n\t\t\"snippet\" : \"" + escapeJason(snip.getSnippet((String) v.firstElement())) + "\",";
               String txt = (String) v.firstElement();
               json += "\n\t\t\"description\" : \"" + escapeJason(
                 txt.length() > 2000 ? txt.substring(0,2000) + "..." : txt
               ) + "\",";
               _hascontent = true;
 
             } else if (key.equalsIgnoreCase("AB") && ! _hascontent ) {
               snip.setFIELD_NAME("AB");
               json += "\n\t\t\"snippet\" : \"" + escapeJason(snip.getSnippet((String) v.firstElement())) + "\",";
               String txt = (String) v.firstElement();
               json += "\n\t\t\"description\" : \"" + escapeJason(
                 txt.length() > 2000 ? txt.substring(0,2000) + "..." : txt
               ) + "\",";
               _hascontent = true;
 
             } else if (key.equalsIgnoreCase("BODY") && ! _hascontent) {
               snip.setFIELD_NAME("BODY");
               json += "\n\t\t\"snippet\" : \"" + escapeJason(snip.getSnippet((String) v.firstElement())) + "\",";
               String txt = (String) v.firstElement();
               json += "\n\t\t\"description\" : \"" + escapeJason(
                 txt.length() > 2000 ? txt.substring(0,2000) + "..." : txt
               ) + "\",";
               _hascontent = true;
 
             } else {
               if (v.size() > 1) {
                 json += "\n\t\t" + key + " : [ ";
                 for (Iterator n = v.iterator(); n.hasNext(); ) {
                   json += "\n\t\t\t\"" + escapeJason((String) n.next()) + "\",";
                 }
                 json = json.substring(0, json.length()-1) + "],";
               } else {
                 json += "\n\t\t" + key + " : \"" + escapeJason((String) v.firstElement()) + "\",";
               }
               
               if (key.equalsIgnoreCase("path"))
                 _haspath = true;
             }
           }
 
           if (! _hascontent) {
             json += "\n\t\t\"snippet\" : \"---\",";
             json += "\n\t\t\"description\" : \"---\",";
           }
 
           if (! _hasid) {
             json += "\n\t\t\"id\" : \"---\",";
             // Exhibit-specific
             json += "\n\t\t\"label\" : \"---\",";
           }
 
           if (!_hastitle)
             json += "\n\t\t\"title\" : \""+id      +"\",";
           
           if (!_haspath)
             json += "\n\t\t\"path\" : \""+id      +"\",";
 
           json = json.substring(0,json.length()-1) + "\n\t},"; //cut off trailing \",\"
         }
 
         if (hits.totalHits == 0)
           json = "{ \"hits\" : \"0\",        \n \"items\": []}";
         else
           json = json.substring(0,json.length()-1) + "\n]}";//cut off trailing \",\"
 
         // Start a thumbnail creation thread
         try {
           ThumbnailWorkerThread t = new ThumbnailWorkerThread(thumbnails);
           t.start();
         } catch (Exception e) {
           StringWriter sw = new StringWriter();
           PrintWriter pw = new PrintWriter(sw);
           e.printStackTrace(pw);
           log.severe(sw.toString());
         }
         
       } catch (ParseException e) {
         log.severe("ParseException: " + e.toString());
         json = "{\"success\":false, \"errors\":{\"reason\":\"org.apache.lucene.queryParser.ParseException\"}}";
       } catch(IOException e) {
           log.severe("IOException: " + e);
           json = "{\"success\":false, \"errors\":{\"reason\":\""+e.toString().replaceAll("\\n", "<br/>")+"\"}}";
       }
       
       
       return json.replaceAll("'","\"");
     }
 
     /**
      * Searches a given Lucene index for a given query and field
      * @param index the name of the index to search
      * @param queryString a String containing the query. Lucene syntax is allowed
      * @param maxHits the number of hits the searcher will return (with a max of 1000)
      * @param defaultField the defaultfield of the index Lucene searches in
      * @return a String with XML'ified results
     */
     public String search(String index, String queryString, String maxHits, String defaultField) {
 
         Date start = new Date();
         
         // Do some String preprocessing to play nice with Kepler
         defaultField = defaultField.replaceAll("\"","").replaceAll("&quot;","");
         
         int intMaxHits = checkInt(maxHits, defaultMaxHits);
         setIndexLocationRelative(index);
 
         String newQueryString;
         MultiTermQuery MqueryTerms;
         Query queryTerms;
         TopDocs hits;
 
         // TODO: debug several escaped characters
         queryString = queryString.replaceAll("(%3F)", "?");
         // queryString = QueryParser.escape(queryString);
 
         try {
           setStopwords();
     
           // Check for wildcard operators (? and *) in query:
           if (Pattern.matches("(.*)\\*(.*)", queryString) || Pattern.matches("(.*)\\?(.*)", queryString)) {
               MqueryTerms = new WildcardQuery(new Term(defaultField, queryString));
               newQueryString = MqueryTerms.toString();
               hits = this._search(MqueryTerms);
           } else {
               QueryParser parser = new QueryParser(Version.LUCENE_41, defaultField, analyzer);
               queryTerms = parser.parse(queryString);
               hits = this._search(queryTerms);
               newQueryString = queryTerms.toString();
           }
 
           ResultType result = makeXML(hits, intMaxHits);
           result.setQuery(newQueryString);
 
           Date end = new Date();
           double duration = (end.getTime() - start.getTime())/1000;
           result.setTime(new BigDecimal(duration));
 
           log.info("Query: " + newQueryString + ", index: " + index + ", " +
               hits.totalHits + " documents, time taken: " + duration + " seconds");
 
           ByteArrayOutputStream destStream = new ByteArrayOutputStream();
           result.save(destStream, xmlOpts);
           destStream.close();
           return destStream.toString("UTF-8");
             
         } catch (ParseException e) {
           log.severe("ParseException: " + e.toString());
           return e.toString();
         } catch(IOException e) {
           StringWriter sw = new StringWriter();
           PrintWriter pw = new PrintWriter(sw);
           e.printStackTrace(pw);
           log.severe(sw.toString());
           return e.toString();
       }
     }
 
 
     /**
      * Searches a given Lucene index for a given query and default field content
      * @param index the name of the index to search
      * @param queryString a String containing the query. Lucene syntax is allowed
      * @param maxHits the number of hits the searcher will return (with a max of 1000)
      * @param defaultField the defaultfield of the index Lucene searches in
      * @return a String with XML'ified results
     */
     public String searchContent(String index, String queryString, String maxHits) {
         return search(index, queryString, maxHits, "content");
     }
 
     /**
      * Substitutes some special characters and returns nicer HTML characters
      * Currently:
      * "&"
      * "<"
      * ">"
      * @param text String of text to be escaped
      * @return a String with replaced characters
     */
     private static String escape(String text) {
         text = text.replaceAll("&", "&amp;");
         text = text.replaceAll("<", "&lt;");
         text = text.replaceAll(">", "&gt;");
         //text = text.replaceAll(">", "&#39;");
         text = text.replaceAll("\\'", "&quot;");
         text = text.replaceAll("\"", "&quot;");
         text = text.replaceAll("`", "&quot;");
         text = text.replaceAll("%", "&quot;");
         text = text.replaceAll("\\f", "");
         text = text.replaceAll("\\r", "");
         text = text.replaceAll("\\n", " ");
         text = text.replaceAll("\\{", " ");
         text = text.replaceAll("\\}", " ");
         text = text.replaceAll("\\*", "");
         return text;
     }
 
     /**
      * Substitutes some special characters and returns nicer HTML characters
      * Currently:
      * "&"
      * "<"
      * ">"
      * @param text String of text to be escaped
      * @return a String with replaced characters
     */
     private static String escapeJason(String text) {
         text = text.replaceAll("&", "&amp;");
         text = text.replaceAll("\\'", "&quot;");
         text = text.replaceAll("\"", "&quot;");
         text = text.replaceAll("`", "&quot;");
         text = text.replaceAll("%", "&quot;");
         text = text.replaceAll("\\f", "");
         text = text.replaceAll("\\r", "");
         text = text.replaceAll("\\n", " ");
         text = text.replaceAll("\\{", " ");
         text = text.replaceAll("\\}", " ");
         text = text.replaceAll("\\]", ")");
         text = text.replaceAll("\\[", "(");
         text = text.replaceAll("\\*", "");
         //text = text.replaceAll("\\<", "&lt;");
         //text = text.replaceAll("\\>", "&gt;");
         return text;
     }
 
     /*
      * Convenience method
      *
      * @param args command-line arguments
      *
     */
     public static void main(String[] args) {
 
         IndexSearcher cmdLineSearcher = null;
         Analyzer cmdLineAnalyzer = null;
         HashMap properties = new HashMap();
         String configFile = null;
         String queryFile = null;
         int intMaxHits = 1000;
 
         for (int i = 0; i < args.length; ++i) {
             if (args[i].equals("-h")) {
                 usage();
             } else if (args[i].equals("-c")) {
                 i++;
                 if (i >= args.length)
                         throw new IllegalArgumentException("specify config file after -c");
                 configFile = args[i];
             } else if (args[i].equals("-f")) {
                 i++;
                 if (i >= args.length)
                     throw new IllegalArgumentException("specify query file after -f");
                 queryFile = args[i];
             } else {
                 System.err.println("Unsupported argument " + args[i]);
             }
         }
 
         if (configFile == null || queryFile == null) {
             usage();
         }
 
         properties = readConfigFile(configFile);
 
         try {
 			Directory indexDir = FSDirectory.open(new File((String)properties.get("indexDir")));
             if (DirectoryReader.indexExists(indexDir)) {
                 cmdLineSearcher = new IndexSearcher(DirectoryReader.open(indexDir));
 			} else
                 throw new IllegalArgumentException("can't read index: " + (String)properties.get("indexDir"));
 
             File stopwords = new File((String)properties.get("stopwords"));
             if (stopwords == null || !stopwords.exists() || !stopwords.canRead()) {
                 System.err.println("can't read stopword file " + stopwords);
                 cmdLineAnalyzer = new StandardAnalyzer(Version.LUCENE_41);
             } else {
                 cmdLineAnalyzer = new StandardAnalyzer(Version.LUCENE_41, new FileReader(stopwords));
             }
 
             BufferedReader input =
                     new BufferedReader(new FileReader(queryFile));
 
             String line = null;
             String queryID = "-1";
             String queryString = "";
 
             while ((line = input.readLine()) != null) {
                 if (line.length() > 0) {
 
                     String[] lineParts = new String[2];
 
                     if (line.startsWith("<")) {
                         lineParts = line.substring(1).split(">");
                     } else {
                         lineParts = line.split("\t+");
                     }
 
                     System.err.println(lineParts[0] + ": " + lineParts[1]);
                     //results = retriever.getHits(lineParts[1] + " .maxResults=1000", lineParts[0], true);
 
                     queryID = lineParts[0];
                     queryString = lineParts[1];
                     Query queryTerms;
                     TopDocs hits = null;
                     float score = 0f;
                     String docID = null;
                     String results = "";
 
                     try {
                         QueryParser parser = new QueryParser(Version.LUCENE_41, "content", cmdLineAnalyzer);
                         queryTerms = parser.parse(queryString);
                         System.err.println("Searching for: " + queryTerms.toString("content"));
                         hits = cmdLineSearcher.search(queryTerms, intMaxHits);
                     } catch (ParseException e) {
                         e.printStackTrace();
                     }
 
                     for (int i = 0; i < hits.totalHits; i++) {
                         Document doc = null;
 
                         score = hits.scoreDocs[i].score;
                         doc = cmdLineSearcher.doc(hits.scoreDocs[i].doc);
                         docID = doc.get("PMID");
 
                         if (docID == null)
                             docID = doc.get("id");
 
                         if (docID == null) {
                             System.err.println("Document with no ID in collection");
                         } else {
                             System.out.println(
                                 results + queryID
                                         + " 0 "
                                         + docID
                                         + " 0 "
                                         + score
                                         + " 0"
                             );
                             /*
                             results += queryID
                                     + "\t"
                                     + collDocID
                                     + "\t"
                                     + (i+1)
                                     + "\t"
                                     + score
                                     + "\t"
                                     + doc.get("OFFSET")
                                     + "\t"
                                     + doc.get("LENGTH")
                                     + "\t"
                                     + " tag";
                              */
                         }
                     } // end Hits (for) loop
                 }
             } // end queryfile
         } catch(IOException e) {
             e.printStackTrace();
         }
     }
 
     public static HashMap readConfigFile(String fileName) {
         HashMap properties = new HashMap();
         BufferedReader input = null;
         String line = "";
         try {
             input = new BufferedReader(new FileReader(fileName));
             while ((line = input.readLine()) != null) {
                 if (!line.startsWith("#")) {
                     String[] lineParts = line.split("[\t ]+");
                     if (lineParts.length > 1) {
                         String property = lineParts[1];
                         for (int i = 2; i < lineParts.length; i++) {
                             property += " " + lineParts[i];
                         }
                         properties.put(lineParts[0], property);
                     }
                 }
             }
         } catch (FileNotFoundException ex) {
                 ex.printStackTrace();
         } catch (IOException ex) {
                 ex.printStackTrace();
         } finally {
             try {
                 if (input != null) {
                     input.close();
                 }
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
 
         return properties;
     }
 
     private static void usage() {
         String usage =
                 "java "
                         + SearcherWS.class.getName()
                         + "\n"
                         + "-c <config_file>\n"
                         + "-f <query-file>\n";
         System.err.println("Usage: " + usage);
         System.exit(-1);
     }
     
   /**
    * A simple threaded class for creating thumbnails
    * 
    * @author emeij
    *
    */
   class ThumbnailWorkerThread extends Thread {
 
     private Vector<File> todolist;
       /** logger for Commons logging. */
     private transient Logger log =
       Logger.getLogger("ThumbnailWorkerThread.class.getName()");
 
     ThumbnailWorkerThread(Vector<File> todolist) {
       this.todolist = todolist;
       if (todolist.size() > 0)
         log.info("Creating thumbnails for " + todolist.size() + " files");
     }
 
     public void run() {
       for (File f : todolist.toArray(new File[0])) {
         Thumbnails.createthumbnail(f);
         synchronized (todolist) {
           todolist.remove(f);
         }
       }
     }
   }
 
 }
