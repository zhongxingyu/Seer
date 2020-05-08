 package no.ntnu.tdt4215.group7;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import no.ntnu.tdt4215.group7.entity.CodeType;
 import no.ntnu.tdt4215.group7.entity.MedDocument;
 import no.ntnu.tdt4215.group7.parser.ATCParser;
 import no.ntnu.tdt4215.group7.utils.Paths;
 
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 
 import com.hp.hpl.jena.ontology.OntProperty;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 public class MainApplication {
 
     public static final String ICD10_FILE = Paths.ICD10_FILE;
     public static final String ATC_FILE = Paths.ATC_FILE;
     public static final String LMHB_DIR = Paths.LMHB_DIR;
 
     public static void main(String[] args) throws IOException {
 
         // String pathFile = ICD10_FILE;
         // OwlParser owlParser = new OwlParser();
         // Map<String, OntProperty> map = owlParser.mapOntProperties(pathFile);
         // List<Resource> resources =
         // owlParser.listResourcesWithProperty(map.get("code_compacted"));
         // owlParser.createICDObjects(resources);
         // Directory index = null;
         // try {
         // index = owlParser.indexIcdObjects();
         // } catch (IOException e) {
         // e.printStackTrace();
         // }
         // String queryString =
         // "Hun har en bror som også har diabetes og som har brukt insulin i flere år";
         // try {
         // owlParser.query(queryString, "extra", index);
         // } catch (IOException e) {
         // e.printStackTrace();
         // }
 
        String queryString = "Eva Andersen er en skoleelev som har hatt insulin diabetes mellitus i 3 år.";
 
         ATCParser atcParser = new ATCParser();
         atcParser.parseATC(ATC_FILE);
         Directory index = null;
         index = atcParser.createIndex();
         atcParser.query(queryString, "label", index);
     }
 }
