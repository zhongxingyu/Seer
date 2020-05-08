 /**
  * ChEBISearch.java
  *
  * 2011.10.25
  *
  * This file is part of the CheMet library
  * 
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.io.remote;
 
 import com.google.common.collect.Multimap;
 import org.apache.lucene.analysis.Analyzer;
 import uk.ac.ebi.interfaces.services.RemoteResource;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.prefs.Preferences;
 import javax.xml.stream.XMLStreamException;
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.KeywordAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.SimpleFSDirectory;
 import org.apache.lucene.util.Version;
 import uk.ac.ebi.interfaces.identifiers.Identifier;
 import uk.ac.ebi.io.xml.UniProtAnnoationLoader;
 import uk.ac.ebi.resource.protein.UniProtIdentifier;
 import uk.ac.ebi.interfaces.services.LuceneService;
 import uk.ac.ebi.resource.classification.KEGGOrthology;
 import uk.ac.ebi.resource.organism.Taxonomy;
 
 /**
  *          KEGGOrthology2OrganismProtein - 2011.12.19 <br>
  *          Creates a lucene index for the KEGG KO Orthology families pointing towards UniProt accessions and the organism
  *          of the protein. It is based on the uniprot download. 
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  */
 public class KEGGOrthology2OrganismProtein extends AbstrastRemoteResource implements RemoteResource, LuceneService {
 
     private static final Logger LOGGER = Logger.getLogger(KEGGOrthology2OrganismProtein.class);
     private static final String locationTrEMBL = "ftp://ftp.ebi.ac.uk/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_trembl.xml.gz";
     private static final String locationSwissProt = "ftp://ftp.ebi.ac.uk/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_sprot.xml.gz";
     // we should probably use both TrEMBL and Swissprot.
     private Analyzer analzyer;
 
     public Analyzer getAnalzyer() {
         return analzyer;
     }
 
     public Directory getDirectory() {
         try {
             return new SimpleFSDirectory(getLocal());
         } catch (IOException ex) {
             throw new UnsupportedOperationException("Index can not fail to open! unsupported");
         }
     }
 
     private List<Document> getKEGGOrthologyUniProtDocsForFile(String location) throws XMLStreamException, IOException {
         UniProtAnnoationLoader loader = new UniProtAnnoationLoader();
         setRemote(location);
        loader.update(getRemote().openStream());
         Multimap<UniProtIdentifier, Identifier> map = loader.getMap();
         LinkedList<Document> docs = new LinkedList();
         for (UniProtIdentifier uniProtIdentifier : map.keySet()) {
             Document doc = new Document();
             doc.add(new Field(KEGGOrthologyOrgProtLuceneFields.UniprotAcc.toString(), uniProtIdentifier.getAccession(), Field.Store.YES, Field.Index.NOT_ANALYZED));
             for (Identifier identifier : map.get(uniProtIdentifier)) {
                 if(identifier instanceof Taxonomy) {
                     doc.add(new Field(KEGGOrthologyOrgProtLuceneFields.TaxID.toString(), identifier.getAccession(),Field.Store.YES,Field.Index.NOT_ANALYZED));
                 } else if(identifier instanceof KEGGOrthology) {
                     doc.add(new Field(KEGGOrthologyOrgProtLuceneFields.KEGGOrthologyFamily.toString(),identifier.getAccession(),Field.Store.YES,Field.Index.NOT_ANALYZED));
                 }
             }
             if(doc.getFields().size()<3)
                 continue;
             docs.add(doc);
         }
         return docs;
     }
     
     private void init() {
         this.analzyer = new KeywordAnalyzer();
     }
 
     public enum KEGGOrthologyOrgProtLuceneFields {
 
         UniprotAcc, KEGGOrthologyFamily, TaxID;
     }
 
     public KEGGOrthology2OrganismProtein() {
         super(locationTrEMBL, getFile());
         init();
     }
 
     public void update() throws IOException {
         LinkedList<Document> docs = new LinkedList();
         try {
             docs.addAll(getKEGGOrthologyUniProtDocsForFile(locationTrEMBL));
             docs.addAll(getKEGGOrthologyUniProtDocsForFile(locationSwissProt));
         } catch (XMLStreamException e) {
             LOGGER.error("Problems parsing uniprot XML:", e);
         }
 
         // write the index
         Directory index = new SimpleFSDirectory(getLocal());
         IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_34, new KeywordAnalyzer()));
         writer.addDocuments(docs);
         writer.close();
         index.close();
 
     }
 
     public static File getFile() {
         String defaultFile = System.getProperty("user.home")
                 + File.separator + "databases"
                 + File.separator + "indexes"
                 + File.separator + "keggorthology-uniprot";
         Preferences prefs = Preferences.userNodeForPackage(KEGGOrthology2OrganismProtein.class);
         return new File(prefs.get("keggorthology.uniprot.links.path", defaultFile));
     }
 
     public String getDescription() {
         return "KEGG Orthology to UniProt links";
     }
     
     public static void main(String[] args) throws MalformedURLException, IOException {
         new KEGGOrthology2OrganismProtein().update();
     }
 }
