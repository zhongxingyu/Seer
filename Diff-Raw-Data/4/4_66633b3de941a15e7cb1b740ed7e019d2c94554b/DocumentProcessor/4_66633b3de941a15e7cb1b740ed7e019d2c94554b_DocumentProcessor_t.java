 package com.cse454.nel;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import com.cse454.nel.disambiguate.AbstractDisambiguator;
 import com.cse454.nel.disambiguate.SimpleDisambiguator;
 import com.cse454.nel.extract.AbstractEntityExtractor;
 import com.cse454.nel.extract.EntityExtractor;
 import com.cse454.nel.search.AbstractSearcher;
 import com.cse454.nel.search.BasicSearcher;
 
 public class DocumentProcessor {
 
 	private final int docID;
 
 	public DocumentProcessor(int docID) throws SQLException {
 		this.docID = docID;
 	}
 
 	public void run() throws SQLException {
		SentenceConnect docs = new SentenceConnect();
		List<Sentence> sentences = docs.getDocument(this.docID);
 
 		AbstractEntityExtractor extractor = new EntityExtractor();
 		List<EntityMention> mentions = extractor.extract(sentences);
 
 		AbstractSearcher searcher = new BasicSearcher();
 		for (EntityMention mention : mentions) {
 			searcher.GetCandidateEntities(mention);
 		}
 
 		AbstractDisambiguator disambiguator = new SimpleDisambiguator();
 		disambiguator.disambiguate(mentions);
 
 		// TODO: output entities to file
 	}
 
 }
