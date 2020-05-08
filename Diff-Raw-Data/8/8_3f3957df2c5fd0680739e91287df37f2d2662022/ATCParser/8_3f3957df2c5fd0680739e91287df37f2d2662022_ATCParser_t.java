 package no.ntnu.tdt4215.group7.parser;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import no.ntnu.tdt4215.group7.entity.ATC;
 
 import org.apache.jena.riot.RDFDataMgr;
 import org.apache.lucene.analysis.no.NorwegianAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.FieldType;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopScoreDocCollector;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.ontology.OntProperty;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 public class ATCParser {
     private Model model;
     private List<ATC> atcs;
 
     public ATCParser() {
         this.atcs = new ArrayList<ATC>();
     }
 
     public Model getModel() {
         return model;
     }
 
     public void setModel(Model model) {
         this.model = model;
     }
 
     public void parse(String pathFile) {
         model.read(pathFile);
     }
 
     /**
      * Parses with Jena the atc file and create atc object
      * */
     public List<ATC> parseATC(String pathFile) {
         // model.read(pathFile);
         this.model = RDFDataMgr.loadModel(pathFile);
         // each ATC code has the rdfs:label property, so we are sure to retrieve
         // all code
         Set<Resource> resources = this.model.listResourcesWithProperty(RDFS.label).toSet();
         // we have all the resources, each is defined by the code
         for (Resource resource : resources) {
             ATC atc = new ATC();
             // the code is given by the local name of the resource
             String atcCode = resource.getLocalName();
             // rdfs:label value
             String label = "";
             // the atc codes of rdfs:subClassOf
             List<String> subClassOf = new ArrayList<String>();
             // we take all the statement triples related to the resource
             List<Statement> resourceStatements = resource.listProperties().toList();
             Set<String> labelTerms = new HashSet<String>(); // we use the set
                                                             // for not having
                                                             // duplicated terms
             for (Statement statement : resourceStatements) {
                 // the resource is always the object of these triples
                 Node predicate = statement.asTriple().getPredicate();
                 Node object = statement.asTriple().getObject();
                 String predicateString = predicate.getLocalName();
                 if (predicateString != null && predicateString.equals("label")) {
                     // some resources have more than one label
                     labelTerms.add(parseTheLabel(object.toString()));
                 }
                 if (predicateString != null && predicateString.equals("subClassOf")) {
                     subClassOf.add(object.getLocalName());
                 }
             }
             for (String term : labelTerms) {
                 label += term + " ";
             }
             atc.setCode(atcCode);
             atc.setLabel(label);
             atc.setSubClassOf(subClassOf);
             this.atcs.add(atc);
         }
         return this.atcs;
     }
 
     /**
      * Extract
      * */
     private String parseTheLabel(String label) {
         String result = "";
         result = label.replaceAll("\\^\\^http://.*|(\\@no)", "");// to remove
                                                                  // http and @no
                                                                  // annotations
         result = result.substring(1, result.length() - 1);// to remove the first
                                                           // and last "
         result = result.toLowerCase();
         return result;
     }
 
     public Directory createIndex() throws IOException {
         NorwegianAnalyzer analyzer = new NorwegianAnalyzer(Version.LUCENE_40);
         Directory index = new RAMDirectory();
         IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
         IndexWriter w = new IndexWriter(index, config);
         for (ATC atc : this.atcs) {
             this.addATCDoc(w, atc);
         }
         w.close();
         return index;
     }
 
     public List<ATC> getAtcs() {
         return atcs;
     }
 
     public void setAtcs(List<ATC> atcs) {
         this.atcs = atcs;
     }
 
     private void addATCDoc(IndexWriter w, ATC atc) throws IOException {
         
         String code = atc.getCode();
         String label = atc.getLabel();
 
         Document doc = new Document();
         FieldType type = new FieldType();
         type.setIndexed(true);
         type.setStored(true);
         type.setStoreTermVectors(true);
         type.setTokenized(true);
         Field fieldLabel = new Field("label", label, type);
 
         doc.add(fieldLabel);
 
         // use a string field for because we don't want it tokenized
         doc.add(new StringField("code", code, Field.Store.YES));
         w.addDocument(doc);
     }
     
     public void query(String queryString, String fieldToQuery, Directory index) throws IOException {
         Query q = null;
         try {
             q = new QueryParser(Version.LUCENE_40, fieldToQuery, new NorwegianAnalyzer(Version.LUCENE_40))
                             .parse(queryString);
         } catch (org.apache.lucene.queryparser.classic.ParseException e) {
             e.printStackTrace();
         }
 
         // 3. search
         int hitsPerPage = 100;
         IndexReader reader = DirectoryReader.open(index);
         IndexSearcher searcher = new IndexSearcher(reader);
         TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
 
         searcher.search(q, collector);
         ScoreDoc[] hits = collector.topDocs().scoreDocs;
 
         // 4. display results
         System.out.println("Found " + hits.length + " hits.");
         for (int i = 0; i < hits.length; i++) {
             int docId = hits[i].doc;
             float score = hits[i].score;
             Document d = searcher.doc(docId);
 //            System.out.println((score) + ". " + "CODE COMPACTED: " + d.get("code_compacted") + "\t" + "LABEL: "
 //                            + d.get("label") + d.get("extra"));
             System.out.println((score) + "."+ "Code: " + d.get("code")+"\t Label: "+d.get("label"));
         }
     }
 }
