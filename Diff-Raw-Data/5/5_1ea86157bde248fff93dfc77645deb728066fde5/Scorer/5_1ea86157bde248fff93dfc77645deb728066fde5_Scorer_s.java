 package ceid.netcins.exo.similarity;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import ceid.netcins.exo.catalog.CatalogEntry;
 import ceid.netcins.exo.catalog.ContentCatalogEntry;
 import ceid.netcins.exo.catalog.ScoreBoard;
 import ceid.netcins.exo.catalog.UserCatalogEntry;
 import ceid.netcins.exo.content.ContentProfile;
 import ceid.netcins.exo.messages.QueryPDU;
 import ceid.netcins.exo.messages.ResponsePDU;
 
 /**
  * This object defines the methods of the "Scorer" thread which is waiting until
  * a scoring request has been issued to the scoringRequest queue! The thread
  * itself is created in the CatalogService class!
  * 
  * TODO : REFINEMENT
  * 
  * @author <a href="mailto:loupasak@ceid.upatras.gr">Andreas Loupasakis</a>
  * @author <a href="mailto:ntarmos@cs.uoi.gr">Nikos Ntarmos</a>
  * @author <a href="mailto:peter@ceid.upatras.gr">Peter Triantafillou</a>
  * 
  * "eXO: Decentralized Autonomous Scalable Social Networking"
  * Proc. 5th Biennial Conf. on Innovative Data Systems Research (CIDR),
  * January 9-12, 2011, Asilomar, California, USA.
  * @version 1.0
  */
 public class Scorer {
 
 	// Coefficients to compute the ENHANCED scores
 	public static final float A1 = (float) 0.5;
 	public static final float A2 = (float) 0.5;
 
 	// Variable to avoid missed signals and spurious wakeups
 	private boolean wasSignalled = false;
 
 	// The Vector with the requests for the Scorer thread
 	private Vector<SimilarityRequest> similarityRequests;
 
 	// Controls the running loop of the "Scorer" thread!
 	private boolean main_running;
 
 	public Scorer() {
 		similarityRequests = new Vector<SimilarityRequest>();
 		main_running = true;
 	}
 
 	/**
 	 * notify the Scorer Thread that a request has been received
 	 */
 	public void doNotify() {
 		synchronized (this) {
 			wasSignalled = true;
 			this.notify();
 		}
 	}
 
 	/**
 	 * This method executes the corresponding scoring request!
 	 * 
 	 * @param req The similarity request to be served.
 	 */
 	@SuppressWarnings("unchecked")
 	private void serveRequest(SimilarityRequest req) {
 
 		@SuppressWarnings("rawtypes")
 		Collection profileEntries = req.getProfileEntries();
 		String[] query = req.getQuery();
 
 		// ************** CONTENT SEARCHING PART **************
 		if ((req.getType() == QueryPDU.CONTENTQUERY 
 				|| req.getType() == QueryPDU.CONTENT_ENHANCEDQUERY)
 				&& profileEntries != null
 				&& !profileEntries.isEmpty()) {
 
 			// Compute Global Set of terms and feed in the CosineSimilarity
 			ContentProfile cprof;// The check "instanceof" is important
 
 			// They must be reused
 			TreeSet<String> docTerms = new TreeSet<String>();
 			CosineSimilarity cossim = null, cossimUserProfiles = null;
 
 			// HashMap which contains the CatalogEntries with the corresponding
 			// score
 			HashMap<ContentCatalogEntry, Float> scoreBoard = new HashMap<ContentCatalogEntry, Float>();
 
 			// 1. QUERY WEIGHTS
 			BinaryWeight[] queryWeights = new BinaryWeight[query.length];
 			int z = 0;
 			for (z = 0; z < query.length; z++) {
 				queryWeights[z] = new BinaryWeight(query[z]);
 			}
 			// Wrap the query weights in the reusable CosineSimilarity object
 			cossim = new CosineSimilarity(queryWeights);
 
 			// Computation of source and destination users' global term set!
 			// 2. SOURCE USER PROFILE WEIGHTS (ENHANCED QUERY)
 			if (req.getType() == QueryPDU.CONTENT_ENHANCEDQUERY
 					|| req.getType() == QueryPDU.USER_ENHANCEDQUERY
 					|| req.getType() == QueryPDU.HYBRID_ENHANCEDQUERY) {
 
 				// *** Source User ***
 
 				// get Source User Profile and compute global term set
 				cprof = req.getSourceUserProfile();
 				if (cprof != null) {
 					// Fill the docTerms Set with the terms of profile
 					cprof.getTermSet(docTerms);
 
 					// Create the Weights!
 					BinaryWeight[] profileWeights1 = new BinaryWeight[docTerms
 							.size()];
 					z = 0;
 					for (String term : docTerms) {
 						profileWeights1[z] = new BinaryWeight(term);
 						z++;
 					}
 
 					// This should be reused with every entry's user profile
 					cossimUserProfiles = new CosineSimilarity(profileWeights1);
 				}
 			}
 
 			// For every CatalogEntry of compute 1)global term set and 2) the
 			// CosineSimilarity
			for(ContentCatalogEntry entry : (Vector<ContentCatalogEntry>)profileEntries) {
 
 				// Content Profile
 				cprof = entry.getContentProfile();
 				// Get the set of profile terms
 				cprof.getTermSet(docTerms);
 
 				// Create the Weights!
 				// 3. CONTENT PROFILE WEIGHTS
 				BinaryWeight[] docWeights = new BinaryWeight[docTerms.size()];
 				z = 0;
 				for(String term : docTerms) {
 					docWeights[z] = new BinaryWeight(term);
 					z++;
 				}
 				// Put weights in the reusable CosineSimilarity object!
 				cossim.setDocWeights(docWeights);					
 
 				// *** User Profile of this entry - global term computation ***
 
 				// 4. ENTRY's USER PROFILE WEIGHTS (ENHANCED QUERY)
 				if (cossimUserProfiles != null) {
 					// User Profile
 					cprof = entry.getUserProfile();
 					if (cprof != null) {
 						// Get the set of profile terms
 						cprof.getTermSet(docTerms);
 						
 						// Create the Weights!
 						BinaryWeight[] profileWeights2 = new BinaryWeight[docTerms
 								.size()];
 						z = 0;
 						for(String term : docTerms) {
 							profileWeights2[z] = new BinaryWeight(term);
 							z++;
 						}
 
 						// Reuse the CosineSimilarity object for user profile.
 						cossimUserProfiles.setDocWeights(profileWeights2);
 
 						scoreBoard.put(entry, new Float(0.5 * cossim.getScore()
 								+ 0.5 * cossimUserProfiles.getScore()));
 					} else
 						scoreBoard.put(entry, new Float(
 								0.5 * cossim.getScore() + 0.5 * 0));
 				} else {
 					scoreBoard.put(entry, new Float(cossim.getScore()));
 				}
 
 			} // End of Entry Similarity
 
 			// Sort by float score value
 			LinkedHashMap<ContentCatalogEntry, Float> sortedScoreBoard = this
 					.sortHashMapByValuesD(scoreBoard);
 
 			Vector<CatalogEntry> v1 = new Vector<CatalogEntry>();
 			v1.addAll(sortedScoreBoard.keySet());
 			Vector<Float> v2 = new Vector<Float>();
 			v2.addAll(sortedScoreBoard.values());
 
 			// Dropping the zero scored entries here.
 			Iterator<CatalogEntry> it1 = v1.iterator();
 			Iterator<Float> it2 = v2.iterator();
 			while (it1.hasNext()) {
 				it1.next();
 				Float f = it2.next();
 				if (f == 0) {
 					it1.remove();
 					it2.remove();
 				}
 			}
 			
 			// Resolve ties at the end of results list
 			resolveTies(v1, v2, req.getK());
 
 			ScoreBoard topK = new ScoreBoard(v1,	v2);
 			req.getContinuation().receiveResult(
 					new ResponsePDU(req.getMessagesCounter(), topK));
 
 			// ************** USER SEARCHING PART **************
 		} else if ((req.getType() == QueryPDU.USERQUERY 
 				|| req.getType() == QueryPDU.USER_ENHANCEDQUERY)
 				&& profileEntries != null
 				&& !profileEntries.isEmpty()) {
 
 			// Compute Global Set of terms and feed in the CosineSimilarity
 			ContentProfile cprof;// The check "instanceof" is important
 
 			// They must be reused
 			TreeSet<String> docTerms = new TreeSet<String>();
 			CosineSimilarity cossim = null, cossimUserProfiles = null;
 
 			// HashMap which contains the CatalogEntries with the corresponding
 			// score
 			HashMap<UserCatalogEntry, Float> scoreBoard = new HashMap<UserCatalogEntry, Float>();
 
 			// 1. QUERY WEIGHTS
 			BinaryWeight[] queryWeights = new BinaryWeight[query.length];
 			int z = 0;
 			for (z = 0; z < query.length; z++) {
 				queryWeights[z] = new BinaryWeight(query[z]);
 			}
 			// This object will be reused.
 			cossim = new CosineSimilarity(queryWeights);
 
 			// Computation of source and destination users' global term set!
 			// 2. SOURCE USER PROFILE WEIGHTS (ENHANCED QUERY)
 			if (req.getType() == QueryPDU.CONTENT_ENHANCEDQUERY
 					|| req.getType() == QueryPDU.USER_ENHANCEDQUERY
 					|| req.getType() == QueryPDU.HYBRID_ENHANCEDQUERY) {
 
 				// *** Source User ***
 				// get Source User Profile and compute global term set
 				cprof = req.getSourceUserProfile();
 				if (cprof != null) {
 					// Get the set of profile terms
 					cprof.getTermSet(docTerms);
 
 					// Create the Weights!
 					BinaryWeight[] profileWeights1 = new BinaryWeight[docTerms
 							.size()];
 					z = 0;
 					for(String term : docTerms) {
 						profileWeights1[z] = new BinaryWeight(term);
 						z++;
 					}
 
 					// This should be reused with every entry's user profile
 					cossimUserProfiles = new CosineSimilarity(null,
 							profileWeights1);
 				}
 			}
 
 			// For every CatalogEntry compute 1)global term set and 2) the
 			// CosineSimilarity
			for(UserCatalogEntry entry : (Vector<UserCatalogEntry>)profileEntries) {
 
 				// User Profile
 				cprof = entry.getUserProfile();
 				// Get the set of profile terms
 				cprof.getTermSet(docTerms);
 
 				// Create the Weights!
 				// 3. ENTRY's USER PROFILE WEIGHTS
 				BinaryWeight[] docWeights = new BinaryWeight[docTerms.size()];
 				z = 0;
 				for(String term : docTerms) {
 					docWeights[z] = new BinaryWeight(term);
 					z++;
 				}
 
 				// Reuse the CosineSimilarity object with every profile
 				cossim.setDocWeights(docWeights);
 
 				// *** User Profile of this entry - global term computation ***
 				// Important to clear in order to be reused!
 				// NO NEED TO CLEAR THE docTerms because we want the same
 				// weights!!!
 				if (cossimUserProfiles != null) {
 					// Create the Weights!
 					// 4. ENTRY's USER PROFILE WEIGHTS (ENHANCED QUERY)
 					BinaryWeight[] profileWeights2 = new BinaryWeight[docTerms
 							.size()];
 					z = 0;
 					for(String term : docTerms) {
 						profileWeights2[z] = new BinaryWeight(term);
 						z++;
 					}
 					// Reuse the CosineSimilarity object with every profile
 					cossimUserProfiles.setDocWeights(profileWeights2);
 
 					scoreBoard.put(entry, new Float(0.5 * cossim.getScore()
 							+ 0.5 * cossimUserProfiles.getScore()));
 				} else {
 					scoreBoard.put(entry, new Float(cossim.getScore()));
 				}
 
 			} // End of Entry Similarity
 
 			// Sort by float score value
 			LinkedHashMap<UserCatalogEntry, Float> sortedScoreBoard = this
 					.sortHashMapByValuesU(scoreBoard);
 
 			Vector<CatalogEntry> v1 = new Vector<CatalogEntry>();
 			v1.addAll(sortedScoreBoard.keySet());
 			Vector<Float> v2 = new Vector<Float>();
 			v2.addAll(sortedScoreBoard.values());
 
 			// Drop the zero scored entries, here.
 			Iterator<CatalogEntry> it1 = v1.iterator();
 			Iterator<Float> it2 = v2.iterator();
 			while (it1.hasNext()) {
 				it1.next();
 				Float f = it2.next();
 				if (f == 0) {
 					it1.remove();
 					it2.remove();
 				}
 			}
 
 			// Select the k results that will be returned
 			resolveTies(v1, v2, req.getK());
 
 			ScoreBoard topK = new ScoreBoard(v1, v2);
 			req.getContinuation().receiveResult(
 					new ResponsePDU(req.getMessagesCounter(), topK));
 		} else { // Raw ScoreBoard Without Scores :-)
 			req.getContinuation().receiveResult(
 					new ResponsePDU(req.getMessagesCounter(),
 							new ScoreBoard(null, null)));
 		}
 	}
 
 	/**
 	 * Used to handle ties at the end of the result vectors. E.g. by choosing 
 	 * RANDOMLY the appropriate number of entries and concatenate them to the
 	 * end of the result lists.
 	 * 
 	 * @param v1 Set of entry objects (e.g. of type CatalogEntry)
 	 * @param v2 The corresponding score float numbers. 
 	 * @param k The size of results to be returned.
 	 */
 	private void resolveTies(Vector<CatalogEntry> v1, Vector<Float> v2, int k){
 		int startOfTies = 0, kst=k;
 		if (kst != QueryPDU.RETURN_ALL && v1.size() > kst) {
 			Vector<CatalogEntry> randomSet = new Vector<CatalogEntry>();
 			// As we count from 0 and not from 1
 			kst--;
 			Float kstScore = v2.get(kst);
 			// The previous ties including kst UCE
 			while (kst >= 0 && v2.get(kst).equals(kstScore)) {
 				randomSet.add(v1.get(kst));
 				kst--;
 			}
 			// Here we will start applying our random choice (+1)
 			startOfTies = kst + 1;
 			kst = k;
 			// The next ties
 			while (kst < v1.size() && v2.get(kst).equals(kstScore)) {
 				randomSet.add(v1.get(kst));
 				kst++;
 			}
 			// Clean the ties temporary
 			while (v1.size() != startOfTies || v2.size() != startOfTies) {
 				v1.remove(v1.lastElement());
 				v2.remove(v2.lastElement());
 			}
 			// How many to print from the randomSet?
 			int answer = k - startOfTies;
 			int choice;
 			// Pick randomly an entry and put it in the printed
 			Random random = new Random();
 			for (int l = 0; l < answer; l++) {
 				choice = random.nextInt(randomSet.size());
 				v1.add(randomSet.get(choice));
 				randomSet.remove(choice);
 				// Put the corresponding score values
 				v2.add(kstScore);
 			}
 		}
 	}
 	
 	public void startScorer() {
 		// Run each test
 		while (main_running) {
 			try {
 				while (!similarityRequests.isEmpty()) {
 					serveRequest(similarityRequests.remove(0));
 				}
 				// Wait until the Selector Thread notify!
 				synchronized (this) {
 					while (!wasSignalled) {
 						try {
 							wait();
 						} catch (InterruptedException ex) {
 							System.out.println("Scorer woke up!");
 						}
 					}
 					// clear signal and continue running.
 					wasSignalled = false;
 				}
 
 			} catch (Exception e) {
 				System.out.println("Error : " + e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * This function will return the scored CatalogEntries sorted by the value
 	 * of score!!!
 	 * 
 	 * @param passedMap
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedHashMap<ContentCatalogEntry, Float> sortHashMapByValuesD(
 			HashMap<ContentCatalogEntry, Float> passedMap) {
 		List<ContentCatalogEntry> mapKeys = new ArrayList<ContentCatalogEntry>(
 				passedMap.keySet());
 		List<Float> mapValues = new ArrayList<Float>(passedMap.values());
 		Collections.sort(mapValues);
 		Collections.sort(mapKeys);
 
 		// Descending Order
 		Collections.reverse(mapKeys);
 		Collections.reverse(mapValues);
 
 		LinkedHashMap<ContentCatalogEntry, Float> sortedMap = new LinkedHashMap<ContentCatalogEntry, Float>();
 
 		Iterator<Float> valueIt = mapValues.iterator();
 		while (valueIt.hasNext()) {
 			Object val = valueIt.next();
 			Iterator<ContentCatalogEntry> keyIt = mapKeys.iterator();
 
 			while (keyIt.hasNext()) {
 				Object key = keyIt.next();
 				Float comp1 = passedMap.get(key);
 				Float comp2 = (Float) val;
 
 				if (comp1.equals(comp2)) {
 					passedMap.remove(key);
 					mapKeys.remove(key);
 					sortedMap.put((ContentCatalogEntry) key, (Float) val);
 					break;
 				}
 
 			}
 
 		}
 		return sortedMap;
 	}
 
 	/**
 	 * This function will return the scored UserCatalogEntries sorted by the
 	 * value of score!!!
 	 * 
 	 * @param passedMap
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedHashMap<UserCatalogEntry, Float> sortHashMapByValuesU(
 			HashMap<UserCatalogEntry, Float> passedMap) {
 		List<UserCatalogEntry> mapKeys = new ArrayList<UserCatalogEntry>(
 				passedMap.keySet());
 		List<Float> mapValues = new ArrayList<Float>(passedMap.values());
 		Collections.sort(mapValues);
 		Collections.sort(mapKeys);
 
 		// Descending Order
 		Collections.reverse(mapKeys);
 		Collections.reverse(mapValues);
 
 		LinkedHashMap<UserCatalogEntry, Float> sortedMap = new LinkedHashMap<UserCatalogEntry, Float>();
 
 		Iterator<Float> valueIt = mapValues.iterator();
 		while (valueIt.hasNext()) {
 			Object val = valueIt.next();
 			Iterator<UserCatalogEntry> keyIt = mapKeys.iterator();
 
 			while (keyIt.hasNext()) {
 				Object key = keyIt.next();
 				Float comp1 = passedMap.get(key);
 				Float comp2 = (Float) val;
 
 				if (comp1.equals(comp2)) {
 					passedMap.remove(key);
 					mapKeys.remove(key);
 					sortedMap.put((UserCatalogEntry) key, (Float) val);
 					break;
 				}
 
 			}
 
 		}
 		return sortedMap;
 	}
 
 	public Vector<SimilarityRequest> getSimilarityRequests() {
 		return similarityRequests;
 	}
 
 	public void cleanup() {
 		this.main_running = false;
 	}
 
 	public void addRequest(SimilarityRequest req) {
 		this.similarityRequests.add(req);
 	}
 
 }
