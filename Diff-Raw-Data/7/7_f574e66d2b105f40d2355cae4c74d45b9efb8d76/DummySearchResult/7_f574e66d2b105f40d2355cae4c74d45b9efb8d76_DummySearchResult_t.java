 // © Maastro Clinic, 2013
 package nl.maastro.eureca.aida.search.zylabpatisclient;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification;
 
 /**
  * Search result uses as expected results.
  *
  * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
  */
 public class DummySearchResult implements SearchResult {
 	/**
 	 * Use {@link #valueOf(nl.maastro.eureca.aida.search.zylabpatisclient.classification.EligibilityClassification) };
 	 * {@link #create(nl.maastro.eureca.aida.search.zylabpatisclient.PatisNumber) } to create a {@code DummySearchResult} for
 	 * {@code classification} as expected classification for {@code patient}.
 	 */
 	public enum Creators {
		NOT_ELIGIBLE(EligibilityClassification.NOT_ELIGIBLE, 1),
 		UNCERTAIN(EligibilityClassification.UNCERTAIN, 1),
		ELIGIBLE(EligibilityClassification.ELIGIBLE, 0),
		UNKNOWN(EligibilityClassification.UNKNOWN, 1);
 
 		private static final Map<EligibilityClassification, Creators> classificationToCreator;
 		static {
 			EnumMap<EligibilityClassification, Creators> result = new EnumMap<>(EligibilityClassification.class);
 			result.put(EligibilityClassification.NOT_ELIGIBLE, NOT_ELIGIBLE);
 			result.put(EligibilityClassification.UNCERTAIN, UNCERTAIN);
 			result.put(EligibilityClassification.ELIGIBLE, ELIGIBLE);
 			result.put(EligibilityClassification.UNKNOWN, UNKNOWN);
 			classificationToCreator = Collections.unmodifiableMap(result);
 		}
 		private final EligibilityClassification classification;
 		private final int hitCount;
 
 		private Creators(EligibilityClassification classification_, int hitCount_) {
 			this.classification = classification_;
 			this.hitCount = hitCount_;
 		}
 
 		public static Creators valueOf(EligibilityClassification classification) {
 			return classificationToCreator.get(classification);
 		}
 
 		public DummySearchResult create(PatisNumber patient) {
 			return new DummySearchResult(patient, classification, hitCount);
 		}
 	}
 
 	private final PatisNumber patient;
 	private final int totalHits;
 	private final Set<EligibilityClassification> classification;
 
 	public DummySearchResult(PatisNumber patient_, EligibilityClassification classification_, int hitCount) {
 		this.patient = patient_;
 		this.totalHits = hitCount;
 		this.classification = Collections.singleton(classification_);
 	}
 
 	@Override
 	public Set<EligibilityClassification> getClassification() {
 		return Collections.unmodifiableSet(classification);
 	}
 	
 
 	@Override
 	public PatisNumber getPatient() {
 		return patient;
 	}
 
 	@Override
 	public int getTotalHits() {
 		return totalHits;
 	}
 
 	@Override
 	public Set<DocumentId> getMatchingDocumentIds() {
 		return Collections.emptySet();
 	}
 
 	@Override
 	public ResultDocument getDoc(DocumentId id) {
 		throw new NoSuchElementException("DummySearchResult has no document");
 	}
 
 	@Override
 	public Collection<ResultDocument> getMatchingDocuments() {
 		return Collections.emptySet();
 	}
 
 	@Override
 	public void add(ResultDocument doc) {
 		throw new UnsupportedOperationException("Not supported; Cannot modify DummySearchResult.");
 	}
 
 	@Override
 	public void remove(ResultDocument doc) {
 		throw new UnsupportedOperationException("Not supported; Cannot modify DummySearchResult.");
 	}
 
 	@Override
 	public Map<DocumentId, Set<Snippet>> assignUnknownTo(SemanticModifier modifier) {
 		return Collections.emptyMap();
 	}
 }
