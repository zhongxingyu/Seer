 package org.data2semantics.recognize;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.queryParser.ParseException;
 import org.data2semantics.indexer.D2S_Indexer;
 import org.data2semantics.indexer.D2S_PDFHandler;
 import org.data2semantics.vocabulary.D2S_CTCAEVocabularyHandler;
 import org.data2semantics.vocabulary.D2S_Concept;
 import org.data2semantics.vocabulary.D2S_VocabularyHandler;
 
 public class D2S_DictionaryRecognizer {
 	private Log log = LogFactory.getLog(D2S_DictionaryRecognizer.class);
 
 	// Here is where we get all the vocabularies
 	D2S_VocabularyHandler vocabularyHandler;
 	
 	// List of PDF handlers where we can get the contents
 	List<D2S_PDFHandler> pdfHandlers;
 	
 	
 	//List of annotation results
 	List<D2S_Annotation> results;
 
 	// Think of a better way to provide this directory as a configuration
 	static final String PUB_DIR="/home/lrd900/tmpDocs/literature";
 	static final String GUIDE_DIR="/home/lrd900/tmpDocs/guidelines";
 
 	// Coba bikin dari publication dulu
 	D2S_Indexer currentIndex=null;
 	
 	public D2S_DictionaryRecognizer() {
 		log.info("Preparing vocabulary");
 		
 		vocabularyHandler = new D2S_CTCAEVocabularyHandler();
 		log.info("Done preparing vocabulary");
 		
 		try {
 			log.info("Preparing indexes");
 			currentIndex = new D2S_Indexer();
 			currentIndex.addPDFDirectoryToIndex(PUB_DIR);
 			currentIndex.addPDFDirectoryToIndex(GUIDE_DIR);
 			//log.info("Done preparing indexes");
 			
 		} catch (IOException e) {
 			log.error("Failed to add files to index");
 		}
 		
 	}
 	
 
 
 	//Let's just do it and see how far we go.
 	public void doIt() throws CorruptIndexException, IOException, ParseException{
 		printAnnotatorHeader();
 		printDocumentHeaders();
 		
 		results = new Vector<D2S_Annotation>();
 		int count =0;
 		for(D2S_Concept currentConcept : vocabularyHandler.getAvailableConcepts()){
 				Set<String> synonyms = currentConcept.getSynonyms();
 				String mainTerm = currentConcept.getMainTerm();
 				Document[] foundDocuments = currentIndex.simpleStringSearch(mainTerm, "contents");
 				if(foundDocuments != null && foundDocuments.length != 0) {
 					//System.out.println("Found " + found.length + "  results for "+mainTerm);
 					generateAnnotationOntologyString(foundDocuments, mainTerm, currentConcept);
 					count ++;
 
 				}
 				for(String term : synonyms){
 					foundDocuments = currentIndex.simpleStringSearch(term, "contents");
 					if(foundDocuments == null || foundDocuments.length == 0) continue;
 					//System.out.println("Found " + found.length + "  results for "+term);
 					generateAnnotationOntologyString(foundDocuments, term, currentConcept);
 					count ++;
 				}
 					
 		}
 		log.info("Check count number of hits: "+count);
 		log.info("Best score : "+currentIndex.bestScore);
 
 		log.info("Best term : "+currentIndex.bestTerm);
 		log.info("Best doc: "+currentIndex.bestDoc);
 		
 	}
 	
 	private void printAnnotatorHeader(){
 		System.out.println(
 			"\n@prefix ao: <http://purl.org/ao/core#> . "+
 			"\n@prefix aot: <http://purl.org/ao/types#> ."+
 			"\n@prefix aos: <http://purl.org/ao/selectors#> ."+
 			"\n@prefix aof: <http://purl.org/ao/foaf#> ."+
			"\n@prefix aoa: <http://purl.org/ao/annotea#> ."+
 			"\n@prefix pav: <http://purl.org/pav#> ."+
 			"\n@prefix ann: <http://www.w3.org/2000/10/annotation-ns#> ."+
 			"\n@prefix pro: <http://purl.obolibrary.org/obo#> ."+
 			"\n@prefix foaf: <http://xmlns.com/foaf/0.1#> ." +
 			"\n@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
 		);
 		
 		System.out.println(
 				"\n<http://www.data2semantics.org/example/D2SAnnotator/> rdf:type <http://xmlns.com/foaf/0.1/Software/>;"+
 				"\n		foaf:name \"D2S Annotator\" . " 
 				);
 	}
 	
 	private void printDocumentHeaders(){
 			List<File> files = currentIndex.getIndexedFiles();
 			String template = 		
 					"\n<http://www.data2semantics.org/example/sourceDocs/NAMEOFFILE> rdf:type pav:SourceDocument ;"+        
 					"\n		pav:retrievedFrom <http://www.data2semantics.org/example/Docs/NAMEOFFILE/>;      "+  
 					"\n		pav:sourceAccessedOn \"2011-11-11\" .\n"+
 					"\n<http://www.data2semantics.org/example/Docs/NAMEOFFILE/> rdf:type foaf:Document .";
 			
 			for(File currentFile : files){
 				String NAMEOFFILE = currentFile.getName().replaceAll(" ","_");
 				System.out.println(template.replaceAll("NAMEOFFILE", NAMEOFFILE));
 			}
 	}
 	
 	
 	private void generateAnnotationOntologyString(Document[] found, String mainTerm, D2S_Concept currentConcept){
 	
 		for (Document d : found) {
 			String content = d.get("contents");
 			String lowerCaseContent 	= content.toLowerCase();
 			String NAMEOFFILE = d.get("filename").replaceAll(" ","_");
 			String page_nr 	= d.get("page_nr");
 			String chunk_nr = d.get("chunk_nr");
 			String position = d.get("position");
 			int termLocation =lowerCaseContent.indexOf(mainTerm.toLowerCase());
 			
 			if (termLocation  >= 0) {
 				String prefix = content.substring(0,termLocation);
 				String suffix = content.substring(termLocation+mainTerm.length()+1);
 				String uriID = currentConcept.getStringID();
 				String textSelectorID = NAMEOFFILE+"_"+page_nr+"_"+chunk_nr+"_"+termLocation;
 				System.out.println(fillInValues(textSelectorID, mainTerm, prefix, suffix, NAMEOFFILE, uriID,position));
 			} 
 		}
 	}
 	
 	private String fillInValues(String selectorID, String MAIN_TERM, String PREFIX, String POSTFIX, String NAMEOFFILE, String ANNOTATION, String position){
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		 
 		String CREATED_ON = sdf.format(new Date());
 				
 		String result = 
 				"\n<http://www.data2semantics.org/example/prefixpostfixtextselector/"+selectorID +"/> rdf:type ao:Selector; "+
 				"\n 		rdf:type aos:TextSelector;"+
 				"\n 		rdf:type aos:PrefixPostfixSelector;"+
 				"\n 		aos:exact \""+MAIN_TERM+"\";"+
 				"\n 		aos:prefix \""+PREFIX+"\";"+
 				"\n 		aos:postfix \""+POSTFIX+"\";"+
 				"\n 		aof:onDocument <http://www.data2semantics.org/example/Docs/"+NAMEOFFILE+">;"+
 				"\n 		ao:onSourceDocument <http://www.data2semantics.org/example/sourceDocs/"+NAMEOFFILE+"> ."+
 				
 				"\n<http://www.data2semantics.org/example/imageselector/"+selectorID +"/> rdf:type ao:Selector; "+
 				"\n 		rdf:type aos:ImageSelector;"+
 				"\n 		rdf:type aos:InitEndCornerSelector;"+
 				"\n 		aos:init \""+ position.split("-")[0]+"\";"+
 				"\n 		aos:end \""+ position.split("-")[1]+"\";"+
 				"\n 		aof:onDocument <http://www.data2semantics.org/example/Docs/"+NAMEOFFILE+">;"+
 				"\n 		ao:onSourceDocument <http://www.data2semantics.org/example/sourceDocs/"+NAMEOFFILE+"> ."+
 
 				
 				"\n<http://www.data2semantics.org/example/qualifier/"+selectorID+"/> rdf:type aot:Qualifier ;"+
 				"\n 		rdf:type ao:Annotation ;"+
 				"\n 		rdf:type ann:Annotation;"+
 				"\n 		aof:annotatesDocument <http://www.data2semantics.org/example/Docs/"+NAMEOFFILE+">;"+
 				"\n 		ao:hasTopic <"+ANNOTATION+">;"+
 				"\n 		pav:createdOn \""+CREATED_ON+"\";"+
 				"\n 		pav:createdBy <http://www.data2semantics.org/example/D2SAnnotator/>;"+
 				"\n 		ao:context <http://www.data2semantics.org/example/prefixpostfixtextselector/"+selectorID+"/> .";
 				return result;
 	}
 	
 	public int getNumberOfFiles(){
 		return currentIndex.getNumberOfFiles();
 	}
 	
 	
 }
