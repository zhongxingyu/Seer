 package com.ntnu.tdt4215.searchEngine;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.lucene.queryparser.classic.ParseException;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.SimpleFSDirectory;
 
 import com.ntnu.tdt4215.document.NLHChapter;
 import com.ntnu.tdt4215.document.NLHIcd10s;
 import com.ntnu.tdt4215.document.ScoredDocument;
 import com.ntnu.tdt4215.index.SentenceQueryPolicy;
 import com.ntnu.tdt4215.index.manager.IndexManager;
 import com.ntnu.tdt4215.index.manager.MergingManager;
 import com.ntnu.tdt4215.index.manager.SimpleManager;
 import com.ntnu.tdt4215.parser.AtcFSM;
 import com.ntnu.tdt4215.parser.Icd10FSM;
 import com.ntnu.tdt4215.parser.IndexingFSM;
 import com.ntnu.tdt4215.parser.NLHWebsiteCrawlerFSM;
 import com.ntnu.tdt4215.query.NorwegianQueryFactory;
 import com.ntnu.tdt4215.query.QueryFactory;
 import com.ntnu.tdt4215.query.WhiteSpaceQueryFactory;
 
 
 public class NLHIcd10 extends SearchEngine {
 	private QueryFactory norwegianQPF;
 	private QueryFactory whiteSpaceQPF;
 	private static final File INDEXNLH = new File("indexes/NLH");
 	private static final File INDEXICD10 = new File("indexes/icd10");
 	private static final File INDEXNLHICD10 = new File("indexes/NLHicd10");
 	private static final File INDEXATC = new File("indexes/atc");
 	private static final int NBHITS_ICD = 1;
 
 	public NLHIcd10() throws IOException {
 		super();
 		createQPFs();
 		createManagers();
 	}
 
 	private void createQPFs() {
 		norwegianQPF = new NorwegianQueryFactory();
 		whiteSpaceQPF = new WhiteSpaceQueryFactory();
 	}
 	
 	private void createManagers() throws IOException {
 		Directory dirIcd10 = new SimpleFSDirectory(INDEXICD10);
		IndexManager idxIcd10 = new MergingManager(dirIcd10, norwegianQPF, new SentenceQueryPolicy(2f));
 		addIndex("icd10", idxIcd10);
 		Directory dirATC = new SimpleFSDirectory(INDEXATC);
		IndexManager idxATC = new MergingManager(dirATC, norwegianQPF, new SentenceQueryPolicy(2f));
 		addIndex("atc", idxATC);
 		
 		Directory dirNLH = new SimpleFSDirectory(INDEXNLH);
 		IndexManager idxNLH = new SimpleManager(dirNLH, norwegianQPF);
 		addIndex("NLH", idxNLH);
 		Directory dirNLHIcd10 = new SimpleFSDirectory(INDEXNLHICD10);
 		IndexManager idxNLHIcd10 = new SimpleManager(dirNLHIcd10, whiteSpaceQPF);
 		addIndex("NLHicd10", idxNLHIcd10);
 	}
 
 	public Collection<ScoredDocument> getResults(int nbHits, String querystr)
 			throws IOException, ParseException {
 		Collection<ScoredDocument> docs = getIndex("icd10").getResults(NBHITS_ICD, querystr);
 		Collection<ScoredDocument> icdChapters = null;
 		if (docs.size() > 0) {
 			String queryIcd = "";
 			for (ScoredDocument d : docs) {
 				queryIcd += d.getField("id") + " ";
 			}
 			icdChapters = getIndex("NLHicd10").getResults(nbHits, queryIcd);
 		}
		Collection<ScoredDocument> chapters = getIndex("NLH").getResults(nbHits, querystr);
 		ArrayList<ScoredDocument> res = new ArrayList<ScoredDocument>(nbHits);
 		for (ScoredDocument sd: chapters) {
 			if (icdChapters != null && icdChapters.contains(sd)) {
				sd.setScore(sd.getScore() * 1.2f);
 			}
 			res.add(sd);
 		}
		if (icdChapters != null) {
			for (ScoredDocument sd: icdChapters) {
				if (sd.getScore() > 0.5) {
					if (!chapters.contains(sd)) {
						res.add(sd);
					}
				}
			}
		}

		Collections.sort(res);
 		return res;
 	}
 
 	@Override
 	public void clean() throws IOException {
 		deleteDirectory(INDEXNLH);
 		deleteDirectory(INDEXICD10);
 		deleteDirectory(INDEXNLHICD10);
 		deleteDirectory(INDEXATC);
 	}
 
 	@Override
 	public void indexAll() throws IOException {
 		IndexingFSM icd10fsm = new Icd10FSM("documents/icd10no.owl");
 		addAll("icd10", icd10fsm);
 		
 		/*IndexingFSM atcfsm = new AtcFSM("documents/atc_no_ext.ttl");
 		addAll("atc", atcfsm);*/
 		
 		IndexingFSM NLHfsm = new NLHWebsiteCrawlerFSM("documents/NLH/T/");
 		NLHfsm.initialize();
 		while (NLHfsm.hasNext()) {
 			NLHChapter chap = (NLHChapter) NLHfsm.next();
 			try {
 				// We look for entries in icd10 that match the chapter
 				Collection<ScoredDocument> res = indexes.get("icd10").getResults(NBHITS_ICD, chap.getContent());
 				// We add these entries inside an index
 				if (res.size() > 0) {
 					getIndex("NLHicd10").addDoc(new NLHIcd10s(chap.getTitle(), res).getDocument());
 				}
 				// We add the chapter to the index
 				getIndex("NLH").addDoc(chap.getDocument());
 			} catch (ParseException e) {
 				System.err.println("Couldn't parse properly" + chap.getTitle() +
 									". This chapter won't be indexed");
 			}
 		}
 		NLHfsm.finish();
 		IndexingFSM NLHfsm2 = new NLHWebsiteCrawlerFSM("documents/NLH/L/");
 		addAll("NLH", NLHfsm2);
 		IndexingFSM NLHfsm3 = new NLHWebsiteCrawlerFSM("documents/NLH/G/");
 		addAll("NLH", NLHfsm3);
 		this.closeWriter();
 	}
 	
 	/**
 	 * delete the index present at file
 	 * @param file
 	 * @throws IOException
 	 */
 	private void deleteDirectory(File file) throws IOException {
 		if (file.exists() && file.isDirectory()) {
 			if (file.canWrite()) {
 				FileUtils.deleteDirectory(file);
 			} else {
 				throw new IOException("Can't delete the directory:" + file.getAbsolutePath());
 			}
 		} else if(file.exists() && !file.isDirectory()) {
 			throw new IOException("Can't delete:" + file.getAbsolutePath() + " it is not a directory");
 		}
 	}
 
 }
