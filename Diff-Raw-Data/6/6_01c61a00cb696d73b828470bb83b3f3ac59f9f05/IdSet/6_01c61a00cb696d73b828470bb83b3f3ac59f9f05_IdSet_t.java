 /* begin license *
  *
  * "Meresco Solr" is a set of components and tools
  *  to integrate Solr into "Meresco."
  *
  * Copyright (C) 2013 Seecr (Seek You Too B.V.) http://seecr.nl
  * Copyright (C) 2013 Stichting Bibliotheek.nl (BNL) http://www.bibliotheek.nl
  *
  * This file is part of "Meresco Solr"
  *
  * "Meresco Solr" is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * "Meresco Solr" is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with "Meresco Solr"; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  *
  * end license */
 
 package org.meresco.solr;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.lucene.search.FieldCache;
 import org.apache.solr.common.SolrException;
 import org.apache.solr.search.DocIterator;
 import org.apache.solr.search.DocSet;
 import org.apache.solr.search.HashDocSet;
 import org.apache.solr.search.SolrIndexSearcher;
 
 
 public class IdSet {
 	private long[] idFieldValues;
 	private Set<Long> ids = new HashSet<Long>();
 	private Map<Long, List<Integer>> id2docIds = new HashMap<Long, List<Integer>>();
 	private int numberOfDocIds = 0;
 	
 	public long fetchValuesTime = 0;
 	public long dataStructureTime = 0;
 	public long intersectionTime = 0;
 	public long makeDocSetTime = 0;
 	public long docSetNextTime = 0;
 	
 	public IdSet(DocSet docSet, SolrIndexSearcher searcher, final String idFieldName) {
 		try {
 			long t0 = System.currentTimeMillis();
 			idFieldValues = FieldCache.DEFAULT.getLongs(searcher.getAtomicReader(), idFieldName, true);
 			fetchValuesTime += System.currentTimeMillis() - t0;
 		} catch (IOException e) {
 			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
 		} catch (NumberFormatException e) {
 			throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "'from' field " + idFieldName + " is unknown or not a long field.");
 		}		
 		
 		if (docSet != null) {
 			long t0 = System.currentTimeMillis();			
 			for (DocIterator iterator = docSet.iterator(); iterator.hasNext();) {
 				int docId = (int) iterator.nextDoc();
 				long value = idFieldValues[docId];
				if (value == 0) {
					throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "'from' field " + idFieldName + " is not a numeric field.");
				}
 				addDocIdForValue(docId, value);
 			}
 			docSetNextTime += System.currentTimeMillis() - t0;			
 		}
 		else {
 			for (int docId=0; docId < idFieldValues.length; docId++) {
 				long value = idFieldValues[docId];
 				if (value == 0) {
 					continue;
 				}
 				addDocIdForValue(docId, value);				
 			}
 		}
 		long t0 = System.currentTimeMillis();		
 		ids = id2docIds.keySet();
 		dataStructureTime += System.currentTimeMillis() - t0;		
 	}
 
 	final private void addDocIdForValue(int docId, long value) {
 		List<Integer> l = id2docIds.get(value);
 		if (l == null) {
 			l = new ArrayList<Integer>();
 			id2docIds.put(value, l);
 		}
 		l.add(docId);
 		numberOfDocIds++;
 	}
 	
 	public IdSet(SolrIndexSearcher searcher, final String idFieldName) {
 		this(null, searcher, idFieldName);
 	}
 
 	public List<Integer> getDocIds(long id) {
 		return id2docIds.get(id);
 	}
 
 	public Set<Long> ids() {
 		return ids;
 	}
 		
 	public void retainAll(IdSet idSet) {
 		long t0 = System.currentTimeMillis();
 		ids.retainAll(idSet.ids());
 		intersectionTime += System.currentTimeMillis() - t0;
 	}
 
 	public DocSet makeDocSet(IdSet mapping) {
 		long t0 = System.currentTimeMillis();
     	int[] docIds = new int[mapping.numberOfDocIds];
     	int docs = 0;
     	Iterator<Long> idsIter = ids.iterator();
     	while (idsIter.hasNext()) {
     		for (Iterator<Integer> docIdsIter = mapping.getDocIds(idsIter.next()).iterator(); docIdsIter.hasNext();) {
     			docIds[docs++] = docIdsIter.next();
 			}
     	}
 		HashDocSet docSet = new HashDocSet(docIds, 0, docs);
 		makeDocSetTime += System.currentTimeMillis() - t0;
 		return docSet;
 	}
 
 	public DocSet makeDocSet() {
 		return makeDocSet(this);
 	}
 }
