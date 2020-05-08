 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.uriplay.query.content.fuzzy;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.IndexWriter.MaxFieldLength;
 import org.apache.lucene.search.Collector;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.Scorer;
 import org.apache.lucene.search.Searcher;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 import org.uriplay.media.entity.Brand;
 import org.uriplay.media.entity.Item;
 import org.uriplay.persistence.content.ContentListener;
 import org.uriplay.util.stats.Score;
 
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.query.Selection;
 
 public class InMemoryFuzzySearcher implements ContentListener, FuzzySearcher {
 
 	private static final Log log = LogFactory.getLog(InMemoryFuzzySearcher.class);
 	
 	static final String FIELD_TITLE_FLATTENED = "title-flattened";
 	static final String FIELD_CONTENT_TITLE = "title";
 	private static final String FIELD_CONTENT_URI = "contentUri";
 	
 	private static final TitleQueryBuilder titleQueryBuilder = new TitleQueryBuilder();
 
 	protected static final int MAX_RESULTS = 5000;
 	
 	private final Directory brandsDir = new RAMDirectory();
 	private final Directory itemsDir = new RAMDirectory();
 
 	public InMemoryFuzzySearcher() {
 		try {
 			formatDirectory(brandsDir);
 			formatDirectory(itemsDir);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private static void closeWriter(IndexWriter writer) {
 		try {
 			//writer.commit();
 			writer.optimize();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		} finally {
 			try {
 				writer.close();
 			} catch (Exception e) {
 				// not much that can be done here
 				throw new RuntimeException(e);
 			}
 		}
 	}
 	
 	private static void formatDirectory(Directory dir) throws CorruptIndexException, IOException {
 		IndexWriter writer = writerFor(dir);
 		writer.close();
 	}
 
 	private static IndexWriter writerFor(Directory dir) throws CorruptIndexException, LockObtainFailedException, IOException {
 		return new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_30), MaxFieldLength.UNLIMITED);
 	}
 
 	private Document brandToDoc(Brand brand) {
 		return asDocument(brand.getCanonicalUri(), brand.getTitle());
 	}
 	
 	private Document itemToDoc(Item item) {
 		return asDocument(item.getCanonicalUri(), item.getTitle());
 	}
 
 	private Document asDocument(String uri, String title) {
 		if (StringUtils.isBlank(uri) || StringUtils.isBlank(title)) {
 			return null;
 		}
 		
 		Document doc = new Document();
         doc.add(new Field(FIELD_CONTENT_TITLE, title, Field.Store.NO, Field.Index.ANALYZED));
         doc.add(new Field(FIELD_TITLE_FLATTENED, title.replace(" ", ""), Field.Store.NO, Field.Index.ANALYZED));
         doc.add(new Field(FIELD_CONTENT_URI, uri, Field.Store.YES,  Field.Index.NOT_ANALYZED));
         return doc;
 	}
 
 	public List<String> brandTitleSearch(String queryString) {
		return search(searcherFor(brandsDir), titleQueryBuilder.build(queryString), new Selection());
 	}
 
 	public List<String> itemTitleSearch(String queryString) {
		return search(searcherFor(itemsDir), titleQueryBuilder.build(queryString), new Selection());
 	}
 	
 	private static Searcher searcherFor(Directory dir)  {
 		try {
 			return new IndexSearcher(dir);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private List<String> search(Searcher searcher, Query query, Selection selection)  {
 		try {
 			if (selection == null) {
				selection = new Selection();
 			}
 			int startIndex = selection.startIndexOrDefaultValue(0);
 			int endIndex = selection.hasLimit() ? startIndex + selection.getLimit() : Integer.MAX_VALUE;
 			
 			final List<Score<Integer>> hits = Lists.newArrayList();
 			
 			searcher.search(query, new Collector() {
 				
 				private Scorer scorer;
 
 				@Override
 				public void setScorer(Scorer scorer) throws IOException {
 					this.scorer = scorer;
 				}
 				
 				@Override
 				public void setNextReader(IndexReader arg0, int docBase) throws IOException {
 				}
 				
 				@Override
 				public void collect(int docId) throws IOException {
 					if (hits.size() < MAX_RESULTS) {
 						hits.add(new Score<Integer>(docId, scorer.score()));
 					}
 				}
 				
 				@Override
 				public boolean acceptsDocsOutOfOrder() {
 					return false;
 				}
 			});
 			
 			Collections.sort(hits, Collections.reverseOrder());
 			
 			List<String> found = Lists.newArrayListWithCapacity(hits.size());
 
 			for (int i = startIndex; i < Math.min(hits.size(), endIndex); i++) {
 				Document doc = searcher.doc(hits.get(i).getTarget());
 				found.add(doc.getField(FIELD_CONTENT_URI).stringValue());
 			}
 			return found;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		} finally {
 			try {
 				searcher.close();
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	@Override
 	public void brandChanged(Collection<Brand> brands, changeType changeType) {
 		IndexWriter writer = null;
 		try {
 			writer = writerFor(brandsDir);
 			writer.setWriteLockTimeout(5000);
 			for (Brand brand : brands) {
 				Document doc = brandToDoc(brand);
 				if (doc != null) {
 					if (changeType == ContentListener.changeType.BOOTSTRAP) {
 						writer.addDocument(doc);
 					}
 					else {
 						writer.updateDocument(new Term(FIELD_CONTENT_URI, brand.getCanonicalUri()), doc);	
 					}
 				}
 				else {
 					log.info("Brand with title " + brand.getTitle() + " and uri " + brand.getCanonicalUri() + " not added due to null elements");
 				}
 			}
 		} catch(Exception e) {
 			throw new RuntimeException(e);
 		}		
 		finally {
 			if (writer != null) {
 				closeWriter(writer);
 			}
 		}
 	}
 
 	@Override
 	public void itemChanged(Collection<Item> items, changeType changeType) {
 		IndexWriter writer = null;
 		try {
 			writer = writerFor(itemsDir);
 			writer.setWriteLockTimeout(5000);
 			for (Item item : items) {
 				
 				Document doc = itemToDoc(item);
 				if (doc != null) {
 					if (changeType == ContentListener.changeType.BOOTSTRAP) {
 						writer.addDocument(doc);
 					}
 					else {
 						writer.updateDocument(new Term(FIELD_CONTENT_URI, item.getCanonicalUri()), doc);	
 					}
 				}
 				else {
 					log.info("Item with title " + item.getTitle() + " and uri " + item.getCanonicalUri() + " not added due to null elements");
 				}
 			}
 		} catch(Exception e) {
 			throw new RuntimeException(e);
 		}		
 		finally {
 			if (writer != null) {
 				closeWriter(writer);
 			}
 		}
 	}
 }
