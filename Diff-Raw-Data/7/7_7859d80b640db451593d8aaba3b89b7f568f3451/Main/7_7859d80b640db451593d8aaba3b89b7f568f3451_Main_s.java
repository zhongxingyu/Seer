 import java.io.File;
 import java.io.FileWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import net.sourceforge.jwbf.mediawiki.actions.MediaWiki;
 import net.sourceforge.jwbf.mediawiki.actions.queries.AllPageTitles;
 import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
 
 import org.apache.log4j.Logger;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 
 /**
  * Script to extract a RDF dump from a SemanticMediaWiki instance by
  * means of web requests, i.e., without having local access to the server
  * or the database.
  * 
  * Command line syntax: wikiURL [outputfile.rdf]
  * 
  * The wikiURL parameter should be the URL of the wiki, i.e.,
  * http://www.crisiswiki.org/
  * 
  * The second parameter is optional. If specified, the collected
  * RDF triples will be stored in that file. Otherwise, the
  * file "out.rdf" is assumed.
  * 
  * @author Diego Berrueta
  *
  */
 public class Main {
     
     private static final Logger logger = Logger.getLogger(Main.class);
 
     /**
      * @param args
      */
     public static void main(String[] args) throws Exception {
         if (args.length < 1) {
             System.err.println("Please provide the URL to the wiki as the first argument, e.g., http://www.crisiswiki.org/");
         } else {
             String wikiUrl = args[0];
             File outputFile = new File(args.length == 2 ? args[1] : "out.rdf");
             MediaWikiBot b = new MediaWikiBot (wikiUrl);
             int count = 0;
             Model m = ModelFactory.createDefaultModel();
             for (int namespace : MediaWiki.NS_ALL) {
                 logger.info("Getting all pages in namespace " + namespace); // see http://en.wikipedia.org/wiki/Wikipedia:Namespace
                 AllPageTitles apt = new AllPageTitles(b, namespace);
                 for (String articleName : apt) {
                     logger.info("Getting RDF data for article (" + count + "): " + articleName);
                    readArticleIntoModel(m, articleName);
                     count++;
                 }
             }
             removeMalformedURIs(m);
             // save data
             logger.info("Saving " + m.size() + " triples to file " + outputFile + ", " + count + " pages have been retrieved");
             m.write(new FileWriter(outputFile));
         }
     }
 
     /**
      * Fetches the RDF triples about a wiki article and adds them to the model
      * 
      * @param m
      * @param articleName
      */
    private static void readArticleIntoModel(Model m, String articleName) {
        String rdfUrl = "http://www.crisiswiki.org/Special:ExportRDF/" + articleName;
         logger.debug("RDF URL: " + rdfUrl);
         m.read(rdfUrl);
         logger.info("After reading " + rdfUrl + ", the model contains " + m.size() + " triples");
     }
 
     /**
      * Remove buggy resource URIs, i.e., URIs that are not valid
      * 
      * @param m
      */
     private static void removeMalformedURIs(Model m) {
         NodeIterator nodeIterator = m.listObjects();
         while (nodeIterator.hasNext()) {
             RDFNode node = nodeIterator.next();
             if (node.canAs(Resource.class)) {
                 Resource resource = node.asResource();
                 try {
                     new URI(resource.getURI()); // just check if a URI can be constructed from the string
                 } catch (URISyntaxException e) {
                     logger.error("Malformed URI fetched from wiki: " + resource.getURI());
                     logger.info("Removing all triples with object: " + resource.getURI());
                     m.removeAll(null, null, resource);
                 }
             }
         }
     }
 
 }
