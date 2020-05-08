 package sf.retriever;
 
 import java.io.*;
 import java.util.*;
 
 import sf.SFConstants;
 import sf.retriever.CorefMention.*;
 import util.FileUtil;
 
 public class CorefIndex implements AutoCloseable {
 	public static final String CORPUS_FILE = "documents.coref";
 	
 	private BufferedReader reader;
 	private long docId, nextDocId;
 	private String[] nextLine;
 	private Collection<CorefEntity> entities;
 	Map<Long, List<CorefMention>> sentencesMap;
 	private ProcessedCorpus corpus;
 	private Map<String, String> nextAnnotation;
 	
 	/**
 	 * Creates a coreference index that reads through the file at the given
 	 * path.
 	 * 
 	 * @param path Path of the directory containing the corpus, ending with
 	 *             a path separator.
 	 * @throws FileNotFoundException if the document cannot be found.
 	 */
 	public CorefIndex( String path ) throws Exception {
 		this( new InputStreamReader(
 				new FileInputStream(FileUtil.joinPaths( path, CORPUS_FILE ) )),
 				new ProcessedCorpus( path,
 				new String[] { SFConstants.WIKI, SFConstants.ARTICLE_IDS,
 							   SFConstants.STANFORDNER } ) );
 	}
 	
 	/**
 	 * Creates a coreference index that reads through the given stream.
 	 * 
 	 * @param in Source of characters to read.
 	 * @param corpus The processed corpus.
 	 */
 	public CorefIndex( Reader in, ProcessedCorpus corpus ) {
 		// Set up reader and corpus.
 		this.corpus = corpus;
 		this.reader = new BufferedReader( in );
 		
 		// Set up state such that all sentence queries return empty stuff.
 		nextDocId = -1;
 		docId = -1;
 		entities = Collections.emptyList();
 		sentencesMap = new HashMap<Long, List<CorefMention>>();
 		
 		// Initialize nextDocId and nextLine.
 		getNextLine();
 		
 		// Get the first annotations from the corpus.
 		if ( corpus.hasNext() )
 			nextAnnotation = corpus.next();
 	}
 	
 	private void getNextLine() {
 		nextLine = null;
 		String line = null;
 		try {
 			line = reader.readLine();
 		} catch ( IOException e ) {}
 		if ( line == null ) {
 			nextDocId = -1;
 		} else {
 			nextLine = line.split("\t");
 			if ( nextLine.length > 0 ) {
 				nextDocId = Long.parseLong( nextLine[0] );
 			} else {
 				nextDocId = -1;
 			}
 		}
 	}
 	
 	public boolean hasNextDoc() {
 		return nextDocId > -1;
 	}
 	
 	/**
 	 * Advances to the next document in the coreference file.
 	 * 
 	 * @param The ID of the document to advance to.
 	 */
 	public void nextDoc( long desiredDocId ) {
 		// If we are already on the requested document, we don't need to do
 		// anything.
 		if ( docId == desiredDocId )
 			return;
 		
 		// Reset state for current document.
 		entities = Collections.emptyList();
 		sentencesMap.clear();
 		docId = desiredDocId;
 		
 		// If the current document ID is larger than the desired one, then
 		// there was no coreference data for the desired document, so simply
 		// return no data.
 		if ( docId > desiredDocId )
 			return;
 		
 		// Advance our buffered reader to the desired document.
 		while( nextDocId < desiredDocId ) {
 			getNextLine();
 			// If the buffered reader ran out of lines, then we just don't have
 			// the desired document. Instead, all queries will return blank
 			// info.
 			if ( nextDocId == -1 ) return;
 		}
 		
 		// This map will be used to create the entities.
 		Map<Long, CorefEntity> entitiesMap = new HashMap<Long, CorefEntity>();
 		
 		while ( nextDocId == docId ) {
 			// TODO: factor this out:
 			String[] tokens = nextLine;
 
 			// Make sure the line contains all the entries we'll want.
 			if ( tokens.length > 14 ) {
 				// Create a coreference mention object.
 				CorefMention mention = new CorefMention();
 				mention.id           = Long.parseLong( tokens[2] );
 				mention.start        = Integer.parseInt( tokens[5] ) - 1;
 				mention.end          = Integer.parseInt( tokens[6] ) - 2; // TODO: check
 				mention.head         = Integer.parseInt( tokens[7] ) - 1;
 				mention.mentionSpan  = tokens[9];
 				mention.type         = Type.valueOf( tokens[10] );
 				mention.number       = Plurality.valueOf( tokens[11] );
 				mention.gender       = Gender.valueOf( tokens[12] );
 				mention.animacy      = Animacy.valueOf( tokens[13] );
 				
 				// Add to cluster
 				long clusterId = Long.parseLong( tokens[1] );
 				CorefEntity entity = entitiesMap.get( clusterId );
 				if ( entity == null ) {
 					entity = new CorefEntity();
 					entity.id = clusterId;
 					entitiesMap.put( clusterId, entity );
 				}
 				entity.mentions.add( mention );
 				mention.entity = entity;
 				if ( tokens[14].equals("true") ) {
 					entity.repMention = mention;
 				}
 				
 				// Add to sentences map
 				long sentenceId = Long.parseLong( tokens[3] );
 				List<CorefMention> sentenceMentions =
 						sentencesMap.get( sentenceId );
 				if ( sentenceMentions == null ) {
 					sentenceMentions = new ArrayList<CorefMention>();
 					sentencesMap.put( sentenceId, sentenceMentions );
 				}
 				sentenceMentions.add( mention );
 			}
 			
 			getNextLine();
 		}
 		
 		// Create list of clusters
 		entities = entitiesMap.values();
 		
 		// Keep track of the chosen wiki article titles and NER types for each
 		// entity.
 		Map<CorefEntity, Map<String, Double>> chosenWiki =
 				new HashMap<CorefEntity, Map<String, Double>>();
 		Map<CorefEntity, Map<NerType, Double>> chosenNer =
 				new HashMap<CorefEntity, Map<NerType, Double>>();
 		for ( CorefEntity entity : entities ) {
 			chosenWiki.put( entity, new HashMap<String, Double>() );
 			chosenNer.put( entity, new HashMap<NerType, Double>() );
 		}
 		
 		// Now, attempt to extract wiki and NER data for the entities by
 		// reading the data in the corpus.
 		boolean first = true;
		while ( nextAnnotation != null ) {
 			// Get the next annotation if needed.
 			if ( !first ) nextAnnotation = corpus.hasNext() ? corpus.next() : null;
 			first = false;
 
 			// Skip irrelevant entries
 			String[] sentenceArticle = nextAnnotation.get(
 					SFConstants.ARTICLE_IDS ).split("\t");
 			if ( sentenceArticle.length < 2 )
 				continue;
 			long sentenceDocId = Long.parseLong( sentenceArticle[1] );
 			long sentenceId = Long.parseLong( sentenceArticle[0] );
 			if ( sentenceDocId < docId )
 				continue;
 			if ( sentenceDocId > docId )
 				// This should never happen!
 				break;
 			
 			// Get coref mentions for this sentence
 			Collection<CorefMention> mentions =
 					getSentenceProvider( sentenceId ).all();
 			
 			// Get wiki data
 			String wikiLine = nextAnnotation.get( SFConstants.WIKI );
 			WikiEntry[] wikiEntries = Util.parseWiki( wikiLine );
 			
 			// Get NER data
 			String nerLine = nextAnnotation.get( SFConstants.STANFORDNER );
 			NerType[] nerEntries = Util.parseNer( nerLine );
 			
 			// For each mention, get more information about its wiki article
 			// name and NER classification.
 			for ( CorefMention mention : mentions ) {
 				CorefEntity entity = mention.entity;
 				
 				// See if wiki entries match this entity
 				Map<String, Double> possibleArticles =
 						chosenWiki.get( entity );
 				for ( WikiEntry entry : wikiEntries ) {
 					if ( !mention.overlaps( entry.start, entry.end ) )
 						continue;
 					Double voteObj = possibleArticles.get( entry.articleName );
 					double vote = voteObj == null ? 0 : voteObj;
 					possibleArticles.put( entry.articleName,
 							vote + entry.confidence );
 				}
 				
 				// Use the NER type of the "head" token in the sentence to
 				// determine the entity type.
 				if ( mention.head >= 0 && mention.head < nerEntries.length ) {
 					NerType headNer = nerEntries[ mention.head ];
 					Map<NerType, Double> possibleNers =
 							chosenNer.get( entity );
 					Double voteObj = possibleNers.get( headNer );
 					double vote = voteObj == null ? 0 : voteObj;
 					possibleNers.put( headNer, vote + 1 );
 				}
 			}
 		}
 		
 		// Finish filling in the info for each entity.
 		for ( CorefEntity entity : entities ) {
 			// Update wiki info.
 			double bestScore = 0;
 			Map<String, Double> possibleArticles =
 					chosenWiki.get( entity );
 			for ( Map.Entry<String, Double> entry :
 				possibleArticles.entrySet() ) {
 				double score = entry.getValue();
 				if ( score > bestScore ) {
 					bestScore = score;
 					entity.wikiId = entry.getKey();
 					entity.fullName = entity.wikiId.replace('_', ' ');
 				}
 			}
 			
 			// Update NER info.
 			bestScore = 0;
 			Map<NerType, Double> possibleNers = chosenNer.get( entity );
 			for ( Map.Entry<NerType, Double> entry : possibleNers.entrySet() ) {
 				double score = entry.getValue(); 
 				if ( score > bestScore ) {
 					bestScore = score;
 					entity.nerType = entry.getKey();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Returns the list of entities.
 	 */
 	public Collection<CorefEntity> getEntities() { return entities; }
 	
 	/**
 	 * Returns the ID of the current document.
 	 */
 	public long getDocId() { return docId; }
 	
 	/**
 	 * Provide coreference information for a single sentence.
 	 * 
 	 * @author Jeffrey Booth
 	 */
 	private class SentenceProvider implements CorefProvider {
 		Collection<CorefMention> mentions;
 		
 		public SentenceProvider( Collection<CorefMention> mentions ) {
 			if ( mentions == null )
 				mentions = Collections.emptyList();
 			this.mentions = mentions;
 		}
 
 		@Override
 		public Collection<CorefMention> inRange(int rangeStart, int rangeEnd) {
 			List<CorefMention> results = new ArrayList<CorefMention>();
 			for ( CorefMention mention : mentions ) {
 				if ( mention.end >= rangeStart && mention.start <= rangeEnd )
 					results.add( mention );
 			}
 			return results;
 		}
 
 		@Override
 		public Collection<CorefMention> all() {
 			return mentions;
 		}		
 	}
 	
 	/**
 	 * Returns an interface for a single sentence.
 	 */
 	public CorefProvider getSentenceProvider( long sentenceId ) {
 		return new SentenceProvider( sentencesMap.get( sentenceId ) );
 	}
 	
 	@Override
 	public void close() throws IOException {
 		reader.close();
 	}
 }
