 package org.apache.lucene.search;
 
 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.lucene.index.AtomicReaderContext;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.ConjunctionTermScorer.DocsAndFreqs;
 import org.apache.lucene.search.TermQuery.TermWeight;
 import org.apache.lucene.search.similarities.Similarity;
 import org.apache.lucene.util.Bits;
 import org.apache.lucene.util.ToStringUtils;
 
 import eu.europeana.ranking.bm25f.params.BM25FParameters;
 import eu.europeana.ranking.bm25f.similarity.BM25FSimilarity;
 
 /**
  * A Query that matches documents matching boolean combinations of other
  * queries, documents are scored using the BM25F ranking function [1].
  * 
  * [1] The probabilistic relevance framework: BM25 and beyond, Robertson,
  * Stephen, Zaragoza, Hugo
  */
 public class BM25FBooleanQuery extends Query implements Iterable<BooleanClause> {
 
 	private static int maxClauseCount = 1024;
 
 	private BM25FParameters bm25fparams;
 
 	/**
 	 * Thrown when an attempt is made to add more than
 	 * {@link #getMaxClauseCount()} clauses. This typically happens if a
 	 * PrefixQuery, FuzzyQuery, WildcardQuery, or TermRangeQuery is expanded to
 	 * many terms during search.
 	 */
 	public static class TooManyClauses extends RuntimeException {
 		public TooManyClauses() {
 			super("maxClauseCount is set to " + maxClauseCount);
 		}
 	}
 
 	/**
 	 * Return the maximum number of clauses permitted, 1024 by default. Attempts
 	 * to add more than the permitted number of clauses cause
 	 * {@link TooManyClauses} to be thrown.
 	 * 
 	 * @see #setMaxClauseCount(int)
 	 */
 	public static int getMaxClauseCount() {
 		return maxClauseCount;
 	}
 
 	/**
 	 * Set the maximum number of clauses permitted per BooleanQuery. Default
 	 * value is 1024.
 	 */
 	public static void setMaxClauseCount(int maxClauseCount) {
 		if (maxClauseCount < 1)
 			throw new IllegalArgumentException("maxClauseCount must be >= 1");
 		BM25FBooleanQuery.maxClauseCount = maxClauseCount;
 	}
 
 	private ArrayList<BooleanClause> clauses = new ArrayList<BooleanClause>();
 	private final boolean disableCoord;
 
 	/** Constructs an empty boolean query. */
 	public BM25FBooleanQuery() {
 		disableCoord = false;
 	}
 
 	/**
 	 * Constructs an empty boolean query.
 	 * 
 	 * {@link Similarity#coord(int,int)} may be disabled in scoring, as
 	 * appropriate. For example, this score factor does not make sense for most
 	 * automatically generated queries, like {@link WildcardQuery} and
 	 * {@link FuzzyQuery}.
 	 * 
 	 * @param disableCoord
 	 *            disables {@link Similarity#coord(int,int)} in scoring.
 	 */
 	public BM25FBooleanQuery(boolean disableCoord) {
 		this.disableCoord = disableCoord;
 	}
 
 	public BM25FBooleanQuery(BM25FParameters bm25fparams) {
 		this();
 		this.bm25fparams = bm25fparams;
 
 	}
 
 	/**
 	 * Returns true iff {@link Similarity#coord(int,int)} is disabled in scoring
 	 * for this query instance.
 	 * 
 	 * @see #BooleanQuery(boolean)
 	 */
 	public boolean isCoordDisabled() {
 		return disableCoord;
 	}
 
 	/**
 	 * Specifies a minimum number of the optional BooleanClauses which must be
 	 * satisfied.
 	 * 
 	 * <p>
 	 * By default no optional clauses are necessary for a match (unless there
 	 * are no required clauses). If this method is used, then the specified
 	 * number of clauses is required.
 	 * </p>
 	 * <p>
 	 * Use of this method is totally independent of specifying that any specific
 	 * clauses are required (or prohibited). This number will only be compared
 	 * against the number of matching optional clauses.
 	 * </p>
 	 * 
 	 * @param min
 	 *            the number of optional clauses that must match
 	 */
 	public void setMinimumNumberShouldMatch(int min) {
 		this.minNrShouldMatch = min;
 	}
 
 	protected int minNrShouldMatch = 0;
 
 	/**
 	 * Gets the minimum number of the optional BooleanClauses which must be
 	 * satisfied.
 	 */
 	public int getMinimumNumberShouldMatch() {
 		return minNrShouldMatch;
 	}
 
 	/**
 	 * Adds a clause to a boolean query.
 	 * 
 	 * @throws TooManyClauses
 	 *             if the new number of clauses exceeds the maximum clause
 	 *             number
 	 * @see #getMaxClauseCount()
 	 */
 	public void add(Query query, BooleanClause.Occur occur) {
 		add(new BooleanClause(query, occur));
 	}
 
 	/**
 	 * Adds a clause to a boolean query.
 	 * 
 	 * @throws TooManyClauses
 	 *             if the new number of clauses exceeds the maximum clause
 	 *             number
 	 * @see #getMaxClauseCount()
 	 */
 	public void add(BooleanClause clause) {
 		if (clauses.size() >= maxClauseCount)
 			throw new TooManyClauses();
 
 		clauses.add(clause);
 	}
 
 	/** Returns the set of clauses in this query. */
 	public BooleanClause[] getClauses() {
 		return clauses.toArray(new BooleanClause[clauses.size()]);
 	}
 
 	/** Returns the list of clauses in this query. */
 	public List<BooleanClause> clauses() {
 		return clauses;
 	}
 
 	/**
 	 * Returns an iterator on the clauses in this query. It implements the
 	 * {@link Iterable} interface to make it possible to do:
 	 * 
 	 * <pre>
 	 * for (BooleanClause clause : booleanQuery) {
 	 * }
 	 * </pre>
 	 */
 	public final Iterator<BooleanClause> iterator() {
 		return clauses().iterator();
 	}
 
 	/**
 	 * Expert: the Weight for BooleanQuery, used to normalize, score and explain
 	 * these queries.
 	 * 
 	 * <p>
 	 * NOTE: this API and implementation is subject to change suddenly in the
 	 * next release.
 	 * </p>
 	 */
 	public class BM25FBooleanWeight extends Weight {
 		/** The Similarity implementation. */
 		protected Similarity similarity;
 		protected ArrayList<Weight> weights;
 		protected int maxCoord; // num optional + num required
 		private final boolean disableCoord;
 		private final boolean termConjunction;
 
 		public BM25FBooleanWeight(IndexSearcher searcher, boolean disableCoord)
 				throws IOException {
 			this.similarity = searcher.getSimilarity();
 			if (this.similarity instanceof BM25FSimilarity) {
 				((BM25FSimilarity) this.similarity).setBM25FParams(bm25fparams);
 			}
 			this.disableCoord = disableCoord;
 			weights = new ArrayList<Weight>(clauses.size());
 			boolean termConjunction = clauses.isEmpty()
 					|| minNrShouldMatch != 0 ? false : true;
 			Set<Term> terms = new HashSet<Term>();
 			for (int i = 0; i < clauses.size(); i++) {
 				BooleanClause c = clauses.get(i);
 				terms.clear();
 				c.getQuery().extractTerms(terms);
 				assert terms.size() == 1;
 				Weight w = new BM25FBooleanTermQuery(
 						terms.toArray(new Term[1])[0], bm25fparams)
 						.createWeight(searcher);
 				// if (!(c.isRequired() && (w instanceof TermWeight))) {
 				// termConjunction = false;
 				// }
 				weights.add(w);
 				if (!c.isProhibited())
 					maxCoord++;
 			}
 			this.termConjunction = termConjunction;
 		}
 
 		@Override
 		public Query getQuery() {
 			return BM25FBooleanQuery.this;
 		}
 
 		@Override
 		public float getValueForNormalization() throws IOException {
 			float sum = 0.0f;
 			for (int i = 0; i < weights.size(); i++) {
 				// call sumOfSquaredWeights for all clauses in case of side
 				// effects
 				float s = weights.get(i).getValueForNormalization(); // sum sub
 																		// weights
 				if (!clauses.get(i).isProhibited())
 					// only add to sum for non-prohibited clauses
 					sum += s;
 			}
 
 			sum *= getBoost() * getBoost(); // boost each sub-weight
 
 			return sum;
 		}
 
 		public float coord(int overlap, int maxOverlap) {
 			return similarity.coord(overlap, maxOverlap);
 		}
 
 		@Override
 		public void normalize(float norm, float topLevelBoost) {
 			topLevelBoost *= getBoost(); // incorporate boost
 			for (Weight w : weights) {
 				// normalize all clauses, (even if prohibited in case of side
 				// affects)
 				w.normalize(norm, topLevelBoost);
 			}
 		}
 
 		@Override
 		public Explanation explain(AtomicReaderContext context, int doc)
 				throws IOException {
 			final int minShouldMatch = BM25FBooleanQuery.this
 					.getMinimumNumberShouldMatch();
 			Explanation result = new Explanation();
 			result.setDescription("sum of:");
 
 			for (Iterator<Weight> wIter = weights.iterator(); wIter.hasNext();) {
 				Weight w = wIter.next();
 				result.addDetail(w.explain(context, doc));
 
 			}
 			Scorer s = scorer(context, true, false, context.reader()
 					.getLiveDocs());
 			float score = 0;
 			int docId = s.advance(doc);
 			if (docId == doc) {
 				score = s.score();
 			}
 			result.setValue(score);
 
 			return result;
 
 		}
 
 		@Override
 		public Scorer scorer(AtomicReaderContext context,
 				boolean scoreDocsInOrder, boolean topScorer, Bits acceptDocs)
 				throws IOException {
 			// TODO investigate on term conjunction
 			// if (termConjunction) {
 			// // specialized scorer for term conjunctions
 			// return createConjunctionTermScorer(context, acceptDocs);
 			// }
 			List<Scorer> required = new ArrayList<Scorer>();
 			List<Scorer> prohibited = new ArrayList<Scorer>();
 			List<Scorer> optional = new ArrayList<Scorer>();
 			Iterator<BooleanClause> cIter = clauses.iterator();
 			for (Weight w : weights) {
 				BooleanClause c = cIter.next();
 				Scorer subScorer = w.scorer(context, true, false, acceptDocs);
 				if (subScorer == null) {
 					if (c.isRequired()) {
 						return null;
 					}
 				} else if (c.isRequired()) {
 					required.add(subScorer);
 				} else if (c.isProhibited()) {
 					prohibited.add(subScorer);
 				} else {
 					optional.add(subScorer);
 				}
 			}
 			// FIXME if optional is not empty, it never finish
 
 			// Check if we can return a BooleanScorer
 			if (!scoreDocsInOrder && topScorer && required.size() == 0) {
 				return new BM25FBooleanScorer(this, disableCoord,
 						minNrShouldMatch, required, optional, prohibited,
 						maxCoord);
 			}
 
 			if (required.size() == 0 && optional.size() == 0) {
 				// no required and optional clauses.
 				return null;
 			} else if (optional.size() < minNrShouldMatch) {
 				// either >1 req scorer, or there are 0 req scorers and at least
 				// 1
 				// optional scorer. Therefore if there are not enough optional
 				// scorers
 				// no documents will be matched by the query
 				return null;
 			}
 
 			return new BM25FBooleanScorer(this, disableCoord, minNrShouldMatch,
 					required, optional, prohibited, maxCoord);
 			// Return a BooleanScorer2
 			// return new BooleanScorer2(this, disableCoord, minNrShouldMatch,
 			// required, prohibited, optional, maxCoord);
 		}
 
 		private Scorer createConjunctionTermScorer(AtomicReaderContext context,
 				Bits acceptDocs) throws IOException {
 
 			// TODO: fix scorer API to specify "needsScores" up
 			// front, so we can do match-only if caller doesn't
 			// needs scores
 
 			final DocsAndFreqs[] docsAndFreqs = new DocsAndFreqs[weights.size()];
 			for (int i = 0; i < docsAndFreqs.length; i++) {
 				final TermWeight weight = (TermWeight) weights.get(i);
 				final Scorer scorer = weight.scorer(context, true, false,
 						acceptDocs);
 				if (scorer == null) {
 					return null;
 				} else {
 					assert scorer instanceof TermScorer;
 					docsAndFreqs[i] = new DocsAndFreqs((TermScorer) scorer);
 				}
 			}
 			return new ConjunctionTermScorer(this, disableCoord ? 1.0f : coord(
 					docsAndFreqs.length, docsAndFreqs.length), docsAndFreqs);
 		}
 
 		@Override
 		public boolean scoresDocsOutOfOrder() {
 			for (BooleanClause c : clauses) {
 				if (c.isRequired()) {
 					return false; // BS2 (in-order) will be used by scorer()
 				}
 			}
 
 			// scorer() will return an out-of-order scorer if requested.
 			return true;
 		}
 
 	}
 
 	@Override
 	public Weight createWeight(IndexSearcher searcher) throws IOException {
 		return new BM25FBooleanWeight(searcher, disableCoord);
 	}
 
 	@Override
 	public Query rewrite(IndexReader reader) throws IOException {
 		return this;
 	}
 
 	// inherit javadoc
 	@Override
 	public void extractTerms(Set<Term> terms) {
 		for (BooleanClause clause : clauses) {
 			clause.getQuery().extractTerms(terms);
 		}
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public BM25FBooleanQuery clone() {
 		BM25FBooleanQuery clone = (BM25FBooleanQuery) super.clone();
 		clone.clauses = (ArrayList<BooleanClause>) this.clauses.clone();
 		return clone;
 	}
 
 	/** Prints a user-readable version of this query. */
 	@Override
 	public String toString(String field) {
 		StringBuilder buffer = new StringBuilder();
 		boolean needParens = (getBoost() != 1.0)
 				|| (getMinimumNumberShouldMatch() > 0);
 		if (needParens) {
 			buffer.append("(");
 		}
 
 		for (int i = 0; i < clauses.size(); i++) {
 			BooleanClause c = clauses.get(i);
 			if (c.isProhibited())
 				buffer.append("-");
 			else if (c.isRequired())
 				buffer.append("+");
 
 			Query subQuery = c.getQuery();
 			if (subQuery != null) {
 				if (subQuery instanceof BooleanQuery) { // wrap sub-bools in
 														// parens
 					buffer.append("(");
 					buffer.append(subQuery.toString(field));
 					buffer.append(")");
 				} else {
 					buffer.append(subQuery.toString(field));
 				}
 			} else {
 				buffer.append("null");
 			}
 
 			if (i != clauses.size() - 1)
 				buffer.append(" ");
 		}
 
 		if (needParens) {
 			buffer.append(")");
 		}
 
 		if (getMinimumNumberShouldMatch() > 0) {
 			buffer.append('~');
 			buffer.append(getMinimumNumberShouldMatch());
 		}
 
 		if (getBoost() != 1.0f) {
 			buffer.append(ToStringUtils.boost(getBoost()));
 		}
 
 		return buffer.toString();
 	}
 
 	/** Returns true iff <code>o</code> is equal to this. */
 	@Override
 	public boolean equals(Object o) {
 		if (!(o instanceof BooleanQuery))
 			return false;
 		BM25FBooleanQuery other = (BM25FBooleanQuery) o;
 		return (this.getBoost() == other.getBoost())
 				&& this.clauses.equals(other.clauses)
 				&& this.getMinimumNumberShouldMatch() == other
 						.getMinimumNumberShouldMatch()
 				&& this.disableCoord == other.disableCoord;
 	}
 
 }
