 package sf.retriever;
 
 import java.io.*;
 import java.util.*;
 
 import sf.retriever.CorefMention.*;
 
 public class CorefIndex implements AutoCloseable {
 	private BufferedReader reader;
 	private long docId, nextDocId;
 	private String[] nextLine;
 	private Collection<CorefEntity> entities;
 	Map<Long, List<CorefMention>> sentencesMap;
 	
 	/**
 	 * Creates a coreference index that reads through the file at the given
 	 * path.
 	 * 
 	 * @param path Path to the document to read.
 	 * @throws FileNotFoundException if the document cannot be found.
 	 */
 	public CorefIndex( String path ) throws FileNotFoundException {
 		this( new InputStreamReader( new FileInputStream( path ) ) );
 	}
 	
 	/**
 	 * Creates a coreference index that reads through the given stream.
 	 * 
 	 * @param in Source of characters to read.
 	 */
 	public CorefIndex( Reader in ) {
 		this.reader = new BufferedReader( in );
 		nextDocId = -1;
 		docId = -1;
 		entities = Collections.emptyList();
 		sentencesMap = new HashMap<Long, List<CorefMention>>();
 		getNextLine();
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
 			nextDocId = Long.parseLong( nextLine[0] );
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
 			
 			// Create a coreference mention object.
 			CorefMention mention = new CorefMention();
 			mention.start = Integer.parseInt( tokens[5] ) - 1;
 			mention.end   = Integer.parseInt( tokens[6] ) - 2; // TODO: check
 			mention.head  = Integer.parseInt( tokens[7] ) - 1;
 			mention.mentionSpan = tokens[9];
 			mention.type = Type.valueOf( tokens[10] );
 			mention.number = Plurality.valueOf( tokens[11] );
 			mention.gender = Gender.valueOf( tokens[12] );
 			mention.animacy = Animacy.valueOf( tokens[13] );
 			
 			// Add to cluster
 			long clusterId = Long.parseLong( tokens[1] );
 			CorefEntity entity = entitiesMap.get( clusterId );
 			if ( entity == null ) {
 				entity = new CorefEntity();
 				entitiesMap.put( clusterId, entity );
 			}
 			entity.mentions.add( mention );
 			mention.entity = entity;
 			
 			// Add to sentences map
 			long sentenceId = Long.parseLong( tokens[3] );
 			List<CorefMention> sentenceMentions =
 					sentencesMap.get( sentenceId );
 			if ( sentenceMentions == null ) {
 				sentenceMentions = new ArrayList<CorefMention>();
 				sentencesMap.put( sentenceId, sentenceMentions );
 			}
 			sentenceMentions.add( mention );
 			
 			getNextLine();
 		}
 		
 		// Create list of clusters
 		entities = entitiesMap.values();
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
