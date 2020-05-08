 package org.moldeas.pscs.mappers;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexDeletionPolicy;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyLikeThisQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.FuzzyTermEnum;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.Searcher;
 import org.apache.lucene.search.TopScoreDocCollector;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.store.RAMDirectory;
 import org.moldeas.common.exceptions.MoldeasModelException;
 import org.moldeas.common.loader.JenaRDFModelWrapper;
 import org.moldeas.common.loader.resources.ResourceLoader;
 import org.moldeas.common.utils.MappingConstants;
 import org.moldeas.common.utils.PSCConstants;
 import org.moldeas.pscs.analyzers.PSCAnalyzer;
 import org.moldeas.pscs.to.PSCMappingTO;
 import org.moldeas.pscs.to.PSCTO;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.SimpleSelector;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.vocabulary.DC;
 
 public class CPV2008Mapper implements PSCMapper {
 	protected static Logger logger = Logger.getLogger(CPV2008Mapper.class);
 	public static final String MAPPER_FIELD_PREF_LABEL = "prefLabel";
 
 	RAMDirectory idx;
 	Analyzer standardAnalyzer;
 	private Map<String,PSCTO> cpv2008;
 	protected final static String SOURCE = "cpv/cpv-2008.ttl";
 
 	public CPV2008Mapper(ResourceLoader loader){
 		try{
 			this.cpv2008 = createPSCTOs(loader);
 			this.idx = new RAMDirectory();
 			this.standardAnalyzer = new PSCAnalyzer();
 			indexPSC(this.cpv2008.values());
 		}catch(Exception e){
 			throw new MoldeasModelException(e);
 		}
 	}
 	private void indexPSC(Collection<PSCTO> pscTOs) throws CorruptIndexException, LockObtainFailedException, IOException {
 		boolean create = true;
 		IndexDeletionPolicy deletionPolicy = 
 				new KeepOnlyLastCommitDeletionPolicy(); 
 		IndexWriter indexWriter = 
 				new IndexWriter(idx,standardAnalyzer,create,
 						deletionPolicy,IndexWriter.MaxFieldLength.UNLIMITED);
 		for(PSCTO pscTO:pscTOs){
 			Field uriField = new Field(MappingConstants.MAPPER_FIELD_URI,pscTO.getUri(),Field.Store.YES,Field.Index.NOT_ANALYZED);
 			Field subjectField = new Field(MAPPER_FIELD_PREF_LABEL,pscTO.getPrefLabel(),Field.Store.YES,Field.Index.ANALYZED);
 			Document doc = new Document();
 			doc.add(uriField);
 			doc.add(subjectField);
 			indexWriter.addDocument(doc);
 		}
 		indexWriter.optimize();
 		indexWriter.close();
 	}
 	private Map<String,PSCTO> createPSCTOs(ResourceLoader loader) {
 		Map<String,PSCTO> cpvPSCTOs = new HashMap<String,PSCTO>();
 		JenaRDFModelWrapper rdfModel = new JenaRDFModelWrapper(loader,"TURTLE");
 		Model model = (Model) rdfModel.getModel();		
 		ResIterator it = model.listResourcesWithProperty(model.getProperty(PSCConstants.SKOS_prefLabel));
 		
 		while (it.hasNext()){
 			PSCTO current = new PSCTO();
 			Resource r = it.next();
 			StmtIterator iter1 = model.listStatements(
 					new SimpleSelector(r, DC.identifier, (RDFNode) null) {
 						public boolean selects(Statement s){	
 							return !(s.getLiteral().getString().matches("^[A-Z]+.*$"));
 						}
 						});	
 			while(iter1.hasNext()){	
 				StmtIterator iter = model.listStatements(
 						new SimpleSelector(r, model.getProperty(PSCConstants.SKOS_prefLabel), (RDFNode) null) {
 							public boolean selects(Statement s)
 							{return s.getLiteral().getLanguage().equalsIgnoreCase("en");}
 						});	
 				while (iter.hasNext()){
 					current.setUri(r.getURI());
 					current.setPrefLabel(iter.next().getString());
 				}
 				logger.info("Loaded "+current);
 				cpvPSCTOs.put(current.getUri(),current);
 				iter1.next();
 			}
 		}		
 		return cpvPSCTOs;
 	}
 	public List<PSCMappingTO> createMappings(PSCTO pscTO){
 		List<PSCMappingTO> mappings = new LinkedList<PSCMappingTO>();		
 		try {
 			IndexSearcher indexSearcher = new IndexSearcher(this.idx);		
 			logger.debug("Searching "+pscTO);
 			List<ScoreDoc> matchPSCTOs = new LinkedList<ScoreDoc>();
 			String prefLabel = pscTO.getPrefLabel();
 			ScoreDoc[] scoreDocs = fetchSearchResults(
 					createQueryFromString(CPV2008Mapper.cleanPrefLabel(prefLabel)), indexSearcher, 3);
 			for(int i = 0; i<scoreDocs.length;i++){
 				Document doc = indexSearcher.doc(scoreDocs[i].doc);
 				String uriTO = doc.getField(MappingConstants.MAPPER_FIELD_URI).stringValue();
 				PSCMappingTO mapping = new PSCMappingTO();
 				mapping.setFrom(pscTO);
 				mapping.setTo(this.cpv2008.get(uriTO));
 				mapping.setConfidence(scoreDocs[i].score);
 				mappings.add(mapping);
 				logger.debug("Added mapping "+mapping);
 			}
 		} catch (Exception e) {
 			logger.error("Processing "+pscTO);
 			logger.error(e);
 		}finally{
 
 		}
 		return mappings;
 	}
 
 
 	public List<PSCMappingTO> createMappings(List<PSCTO> pscTOs){
 		List<PSCMappingTO> mappings = new LinkedList<PSCMappingTO>();		
 		//FIXME: to optimize indexsearcher no delegate call
 		for(PSCTO pscTO:pscTOs){
 			try {
 				IndexSearcher indexSearcher = new IndexSearcher(this.idx);		
 
 				logger.debug("Searching "+pscTO);
 				List<ScoreDoc> matchPSCTOs = new LinkedList<ScoreDoc>();
 				String prefLabel = pscTO.getPrefLabel();
 				ScoreDoc[] scoreDocs = fetchSearchResults(
 						createQueryFromString(CPV2008Mapper.cleanPrefLabel(prefLabel)), indexSearcher, 3);
				//If no result then fuzzy query
				if (scoreDocs.length == 0){
					scoreDocs = fetchSearchResults(
							createFuzzyQueryFromString(CPV2008Mapper.cleanPrefLabel(prefLabel)), indexSearcher, 3);
				}
 				for(int i = 0; i<scoreDocs.length;i++){
 					Document doc = indexSearcher.doc(scoreDocs[i].doc);
 					String uriTO = doc.getField(MappingConstants.MAPPER_FIELD_URI).stringValue();
 					PSCMappingTO mapping = new PSCMappingTO();
 					mapping.setFrom(pscTO);
 					mapping.setTo(this.cpv2008.get(uriTO));
 					mapping.setConfidence(scoreDocs[i].score);
 					mappings.add(mapping);
 					logger.debug("Added mapping "+mapping);
 				}
 			} catch (Exception e) {
 				logger.error(pscTO.getUri());
 				logger.error(e);
 			}finally{
 
 			}
 		}
 		return mappings;
 	}
 
 	public static String getSource() {
 		return SOURCE;
 	}
 
 
 	public static Query createQueryFromString(String q) throws ParseException {		
 		QueryParser parser = new QueryParser(MAPPER_FIELD_PREF_LABEL,
 				new PSCAnalyzer());
 		parser.setDefaultOperator(QueryParser.Operator.OR);
		Query query = parser.parse(QueryParser.escape(q));
		return query;
		
	}
 
	public static Query createFuzzyQueryFromString(String q) throws ParseException {		
		FuzzyLikeThisQuery flt=new FuzzyLikeThisQuery(50,new PSCAnalyzer()); 
		flt.addTerms(q, MAPPER_FIELD_PREF_LABEL, 0.75f,	FuzzyQuery.defaultPrefixLength); 
		return flt;
 	}
	
	
 //
 //	//FIXME: Does not work
 //	public static Query createTermQueryFromString(String key, String q) throws ParseException {		
 //		Term keyTerm = new Term("key", key);
 //		TermQuery keyTermQuery = new TermQuery(keyTerm);
 //		
 //		Term rawTerm = new Term(MAPPER_FIELD_PREF_LABEL, q);
 //
 //		SpanFirstQuery spanFirstQuery = new SpanFirstQuery(new SpanTermQuery(rawTerm), 1);
 //
 ////		BooleanQuery booleanQuery = new BooleanQuery();
 ////		booleanQuery.add(new BooleanClause(keyTermQuery, BooleanClause.Occur.SHOULD));
 ////		booleanQuery.add(new BooleanClause(spanFirstQuery, BooleanClause.Occur.SHOULD));
 //		System.out.println("From "+q+" created "+spanFirstQuery);
 //		return spanFirstQuery;
 //
 //	}
 //	
 //	//FIXME
 //	public static Query createKeyTermQueryFromString(String key) throws ParseException {		
 //		QueryParser parser = new QueryParser("key",
 //				new PSCAnalyzer());
 //		parser.setDefaultOperator(QueryParser.Operator.OR);
 //		return parser.parse(QueryParser.escape(key));
 //
 //	}
 	
 	
 
 //	public static ScoreDoc test(String cleanPrefLabel, Searcher indexSearcher) throws CorruptIndexException, IOException, ParseException{
 //		String key = StringUtils.capitalize(cleanPrefLabel.split(" ")[0]);
 //		ScoreDoc scoreDoc = null;
 //		//First try normal fetch
 //		try{
 //			ScoreDoc[] scoreDocs = fetchSearchResults(
 //					createQueryFromString(cleanPrefLabel), 
 //					indexSearcher, 1);
 //			 scoreDoc = filterCheckResults(
 //					key,
 //					scoreDocs, 
 //					indexSearcher);
 //			if(scoreDoc == null){
 //				scoreDoc = filterCheckResults(key,fetchSearchResults(
 //								createKeyTermQueryFromString(key), 
 //								indexSearcher, 1), 
 //								indexSearcher);
 //			}
 //		}catch(ParseException e){
 //			scoreDoc = filterCheckResults(key,fetchSearchResults(
 //					createKeyTermQueryFromString(key), 
 //					indexSearcher, 1), 
 //					indexSearcher);
 //		}
 //		
 //		return scoreDoc;
 //	}
 
 //	private static ScoreDoc filterCheckResults (String key,ScoreDoc[] scoreDocs, Searcher indexSearcher ) throws CorruptIndexException, IOException{
 //		ScoreDoc result = null;
 //		//System.out.println("FILTERING "+scoreDocs.length);
 //		for(int i = 0; i<scoreDocs.length;i++){
 //			Document doc = indexSearcher.doc(scoreDocs[i].doc);
 //			String keyTO = doc.getField("key").stringValue();
 //			if(keyTO.equalsIgnoreCase(key)){
 //				result =  scoreDocs[i];
 //			}
 //		}
 //		return result;
 //	}
 
 	public static ScoreDoc[] fetchSearchResults(Query query, Searcher indexSearcher, int n ){
 		try{
 			TopScoreDocCollector collector = TopScoreDocCollector.create(n, true);
 			indexSearcher.search(query, collector);
 			ScoreDoc[] scoreDocs = collector.topDocs().scoreDocs;
 			return scoreDocs;
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return new ScoreDoc[0];
 	}
 
 	public static String cleanPrefLabel(String q){
 		String value = q.replaceAll("-", "");
 		value = value.replaceAll("á", "a");
 		value = value.replaceAll("é", "e");
 		value = value.replaceAll("í", "i");
 		value = value.replaceAll("ó", "o");
 		value = value.replaceAll("ú", "u");
 		value = value.replaceAll("/", "");
 		value = value.replaceAll("'", "");
 		value = value.replaceAll("&", " ");
 		value = value.replaceAll("\r", " ");
 		value = value.replaceAll("\n", " ");
 		value = value.replaceAll("\n\r", " ");
 		value = value.replaceAll("\u0085", " ");
 		value = value.replaceAll("\u2028", " ");
 		value = value.replaceAll("\u2029", " ");
 		value = value.replaceAll("\\W", " ").replaceAll("\\d", "");
 		return value;
 	}
 
 }
