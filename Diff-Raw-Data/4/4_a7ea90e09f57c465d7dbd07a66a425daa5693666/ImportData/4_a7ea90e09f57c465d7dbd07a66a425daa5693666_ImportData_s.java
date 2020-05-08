 package com.computas.sublima.app.adhoc;
 
 import com.computas.sublima.app.index.EndpointSaver;
 import com.computas.sublima.query.impl.DefaultSparulDispatcher;
 import com.computas.sublima.query.service.CachingService;
 import com.hp.hpl.jena.rdf.model.InfModel;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.reasoner.Reasoner;
 import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
 import com.hp.hpl.jena.reasoner.rulesys.Rule;
 import com.hp.hpl.jena.util.PrintUtil;
 import net.spy.memcached.MemcachedClient;
 import org.apache.log4j.Logger;
 
 import java.io.ByteArrayOutputStream;
 
 public class ImportData {
 
   private static Logger logger = Logger.getLogger(ImportData.class);
   private DefaultSparulDispatcher sparul = new DefaultSparulDispatcher();
 
   public ImportData() {
   }
 
   /**
    * Ad hoc method to import data to the Pg Data Store.
    *
    * @param url  The URL of the RDF to import
    * @param lang The serialisation language of the file
    */
   public void load(java.lang.String url, java.lang.String lang) {
 
     try {
       Model model = ModelFactory.createDefaultModel();
       model.read(url, lang);
 
       // Add the rules here
       String skosNs = "http://www.w3.org/2004/02/skos/core#";
       String dctNs = "http://purl.org/dc/terms/";
       String subNs = "http://xmlns.computas.com/sublima#";
       String rdfNs = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
       String wdrNs = "http://www.w3.org/2007/05/powder#";
 
       PrintUtil.registerPrefix("skos", skosNs);
       PrintUtil.registerPrefix("dct", dctNs);
       PrintUtil.registerPrefix("sub", subNs);
       PrintUtil.registerPrefix("rdf", rdfNs);
       PrintUtil.registerPrefix("wdr", wdrNs);
 
       //String rules = SettingsService.getProperty("sublima.import.rules");
       String rules = "[inverseRule1: (?X skos:broader ?Y) -> (?Y skos:narrower ?X)]" +
               "[inverseRule2: (?X skos:narrower ?Y) -> (?Y skos:broader ?X)]" +
               "[statusRule1: (?X rdf:type sub:Resource) noValue(?X wdr:describedBy) -> (?X wdr:describedBy <http://sublima.computas.com/status/godkjent_av_administrator>)]" +
               "[statusRule2: (?X rdf:type skos:Concept) noValue(?X wdr:describedBy) -> (?X wdr:describedBy <http://sublima.computas.com/status/godkjent_av_administrator>)]";
 
 
       logger.info("Applying rules " + rules);
       Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
       InfModel inf = ModelFactory.createInfModel(reasoner, model);
 
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       inf.write(out, "N-TRIPLE");
 
       String[] results = out.toString().split("\n");
      EndpointSaver save = new EndpointSaver("http://detektor.sublima.computas.com:8180/test/", 250);//SettingsService.getProperty("sublima.basegraph"), 250);
 
       for (String result : results) {
         save.Add(result);
       }
 
       save.Flush();
 
       out.close();
       model.close();
       inf.close();
 
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   private void modelChanged() {
     logger.debug("ImportData invalidates the cache.");
     CachingService cache = new CachingService();
     MemcachedClient memcached = cache.connect();
     cache.modelChanged(memcached);
     cache.close(memcached);
   }
 
   public static void main(String[] args) {
     ImportData id = new ImportData();
     id.load(args[0], args[1]);
 
     //load("file:\\Prosjekter\\SUBLIMA\\Kode\\Sublima\\blocks\\sublima-app\\src\\main\\resources\\rdf-data\\information-model.n3", "N3");
     //System.out.println("Done loading information-model.n3");
     //load("file:\\Prosjekter\\SUBLIMA\\Kode\\Sublima\\blocks\\sublima-app\\src\\main\\resources\\rdf-data\\sublima-ns.ttl", "Turtle");
     //System.out.println("Done loading sublima-ns.ttl");
     //load("file:\\Prosjekter\\SUBLIMA\\Kode\\Sublima\\blocks\\sublima-app\\src\\main\\resources\\rdf-data\\detektor-test-data.n3", "N3");
     //System.out.println("Done loading detektor-test-data.n3");
     //load("http://www.lingvoj.org/lingvoj.rdf", "RDF/XML");
   }
 }
