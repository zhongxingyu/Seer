 package tests;
 
 import java.io.StringReader;
 
 import sf.retriever.CorefIndex;
 
 /*
 Coreference data format, from the documentation:
1. docID as the second field in 'sentences.articleIDs' (scope: dataset)
 2. mention.corefClusterID: the cluster id for the mention (scope: document)
 3. mention.mentionID: the mention id (scope: document)
4. global sentenceID as the first field in 'sentences.articleIDs' and other
   'sentences.*'
 5. local sentenceID (index from 1, scope: document)
 6. mention.startIndex (token, index from 1, scope: sentence)
 7. mention.endIndex (token, index from 1, scope: sentence)
 8. mention.headIndex (token, index from 1, scope: sentence)
 9. mention.position: redundant information. not useful.
 10. mention.mentionSpan : the actual mention string
 11. mention.mentionType: (one of PRONOMINAL, PROPER and NOMINAL)
 12. mention.number: (one of SINGULAR, PLURAL and UNKNOWN)
 13. mention.gender: (one of FEMALE, UNKNOWN, MALE and NEUTRAL)
 14. mention.animacy: (one of ANIMATE, INANIMATE and UNKNOWN)
 15. a boolean value: true if this mention is the representative mention of
     a cluster; false otherwise.
 */
 /**
  * Produces coreference indices for testing.
  * 
  * @author Jeffrey Booth
  */
 class IndexMaker {
 	private final String SPAN = "A brand new and bigger Air China";
 	private StringBuffer data;
 	
 	public IndexMaker() {
 		data = new StringBuffer();
 	}
 	
 	public IndexMaker row(Object... items) {
 		boolean first = true;
 		for ( Object item : items ) {
 			if ( !first )
 				data.append( "\t" );
 			data.append( item.toString() );
 			first = false;
 		}
 		data.append( "\n" );
 		return this;
 	}
 	
 	public IndexMaker part( long docId, long clusterId, long sentenceId ) {
 		return part( docId, clusterId, sentenceId, 1, 8 );
 	}
 	
 	public IndexMaker part( long docId, long clusterId, long sentenceId,
 			int start, int end ) {
 		return row( docId, clusterId, 8, sentenceId, 2, start, end, 7, 0, SPAN,
 				"PROPER", "SINGULAR", "NEUTRAL", "INANIMATE", true );
 	}
 	
 	public CorefIndex make() {
 		// TODO: provide a ProcessedCorpus...
 		return new CorefIndex( new StringReader( data.toString() ), null );
 	}
 }
