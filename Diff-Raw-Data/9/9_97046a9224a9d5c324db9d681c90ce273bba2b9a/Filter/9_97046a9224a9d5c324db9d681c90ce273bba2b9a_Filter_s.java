 package newsrack.filter;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import newsrack.NewsRack;
 import newsrack.database.DB_Interface;
 import newsrack.database.NewsItem;
 import newsrack.util.StringUtils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class Filter implements java.io.Serializable
 {
 // ############### STATIC FIELDS AND METHODS ############
 			  static       int        GLOBAL_MIN_CONCEPT_HIT_SCORE = 2;	// Minimum # of hits for a concept-rule to be triggered
 	        static       int        GLOBAL_MIN_MATCH_SCORE       = 2;	// Minimum score for a filter to be triggered
 	private static       int        _defaultMinConceptHitScore;	      // Minimum # of hits for a concept-rule to be triggered
 	private static       int        _defaultMinMatchScore;	         // Minimum score for a filter to be triggered
 	private static       String     _indent               = "";
 	private static final FilterOp[] _termTypes;
 	private static final HashMap<FilterOp, Integer> _typeMap = new HashMap<FilterOp, Integer>();
 
 	public static enum FilterOp { NOP, LEAF_CONCEPT, AND_TERM, OR_TERM, NOT_TERM, CONTEXT_TERM, LEAF_CAT, LEAF_FILTER, PROXIMITY_TERM, SOURCE_FILTER };
 
 	public static int CONTEXT_TERM_OPERAND   = -1;
 	public static int PROXIMITY_TERM_OPERAND = -2;
 
 	public static FilterOp getTermType(int n) { return _termTypes[n]; }
 	public static int      getValue(FilterOp op) { return _typeMap.get(op); }
 
    private static Log _log = LogFactory.getLog(Filter.class);
 
 	static { 
 		_termTypes = FilterOp.values(); 
 		for (int i = 0; i < _termTypes.length; i++)
 			_typeMap.put(_termTypes[i], i);
 	}
 
 	public static void init(DB_Interface db)
 	{
 		try {
 		   String p = NewsRack.getProperty("concept.match.minhits");
 			if (p != null) 
 				GLOBAL_MIN_CONCEPT_HIT_SCORE = Integer.parseInt(p);
 
 		   p = NewsRack.getProperty("filter.match.minscore");
 			if (p != null) 
 				GLOBAL_MIN_MATCH_SCORE = Integer.parseInt(p);
 		}
 		catch (final Exception e) {
 			_log.error("Exception initializing filter defaults ... reverting to hardwired defaults!", e);
 			GLOBAL_MIN_CONCEPT_HIT_SCORE = 1;
 			GLOBAL_MIN_MATCH_SCORE = 2;
 		}
 	}
 
 		// Set global minimum hit score for all filters
 	public final static void setMinMatchScore(int n)      { _defaultMinMatchScore = (n < 1) ? 1 : n; }
 	public final static void setMinConceptHitScore(int n) { _defaultMinConceptHitScore = (n < 1) ? 1 : n; }
 	public final static void resetMinScores() 
 	{
 		setMinMatchScore(GLOBAL_MIN_MATCH_SCORE);
 		setMinConceptHitScore(GLOBAL_MIN_CONCEPT_HIT_SCORE);
 	}
 
 // ############### NON-STATIC FIELDS AND METHODS ############
 					 Long     _key;				// unique db key
 	public final String   _name;				// Filter name
 	public final String 	 _ruleString;		// Rule - as a string
 	public final RuleTerm _rule;				// Rule - as an expression tree
 	public final int      _minMatchScore;	// Minimum score for this filter to pass-through a news article
 
 	public Filter(String name, String ruleString, RuleTerm r, int h) { _name = name; _rule = r; _ruleString = ruleString; _minMatchScore = (h < 1) ? 1 : h; }
 	public Filter(String name, RuleTerm r)        { _name = name; _rule = r; _ruleString = r.toString(); _minMatchScore = _defaultMinMatchScore; }
 	public Filter(String name, RuleTerm r, int h) { _name = name; _rule = r; _ruleString = r.toString(); _minMatchScore = (h < 1) ? 1 : h; }
 	public void setKey(Long k)    { _key = k; }
 	public Long getKey()          { return _key; }
 	public String getName()       { return _name; }
 	public RuleTerm getRule()     { return _rule; }
 	public String getRuleString() { return _ruleString; }
 	public int getMinMatchScore() { return _minMatchScore; }
 
 	public int getMatchScore(NewsItem article, int numTokens, Hashtable matchScores)
 	{
 		try {
 			return _rule.getMatchScore(this, article, numTokens, matchScores); 
 		}
 		catch (Exception e) {
			_log.error("Caught exception in match score for filter: " + _key + ": " + _ruleString, e);
 			return 0;
 		}
 	}
 
 	public void  collectUsedConcepts(Set<Concept> concepts) { _rule.collectUsedConcepts(concepts); } 
 
 // ############### NESTED CLASSES ############
 
 	/* class RuleTerm is a tree-node in the parse tree
 	 * representing a category matching rule 
 	 * The term could be 
 	 *   - a leaf-concept term
 	 *   - a leaf-filter term
 	 *   - a leaf-category term
 	 *   - a leaf-negation term
 	 *   - a context-sensitive concept term
 	 *   - a non-leaf AND term
 	 *   - a non-leaf OR term
 	 */
 	public static abstract class RuleTerm implements java.io.Serializable
 	{
 		abstract public FilterOp getType();
 		abstract public Object   getOperand1();
 		abstract public Object   getOperand2();
 		abstract public String   toString();
 		abstract public void     print(PrintWriter pw);
 		abstract public void     collectUsedConcepts(Set<Concept> usedConcepts);
 		abstract public int      getMatchScore(Filter f, NewsItem article, int numTokens, Hashtable matchScores);
 	}
 
 	/* class LeafConcept encodes a leaf concept */
 	public static class LeafConcept extends RuleTerm
 	{
 		private Concept _concept;
 		private int     _minConceptHitScore;
 
 		public LeafConcept(Concept c) { _concept = c; _minConceptHitScore = Filter._defaultMinConceptHitScore; }
 		public LeafConcept(Concept c, Integer n) { _concept = c; _minConceptHitScore = (n == null) ? Filter._defaultMinConceptHitScore : ((n < 1) ? 1 : n); }
 		public FilterOp getType()     { return FilterOp.LEAF_CONCEPT; }
 		public Object getOperand1()   { return _concept; }
 		public Object getOperand2()   { return null; }
 		public int getMinOccurences() { return _minConceptHitScore; }
 		public String toString()      { return _concept.getName() + (_minConceptHitScore == 1 ? "" : ":" + _minConceptHitScore); }
 		public void print(PrintWriter pw) { pw.println(_indent + _concept.getLexerToken().getToken() + (_minConceptHitScore == 1 ? "" : ":" + _minConceptHitScore)); }
 		public void collectUsedConcepts(final Set<Concept> usedConcepts) { usedConcepts.add(_concept); }
 
 		public int getMatchScore(Filter f, NewsItem article, int numTokens, Hashtable matchScores)
 		{
 			Score mc    = (Score)matchScores.get(_concept.getLexerToken().getToken());
 			int   score = ((mc == null) ? 0 : mc.value());
 			return (f._minMatchScore * score) / _minConceptHitScore;
 		}
 	}
 
 	/* class LeafFilter encodes a leaf filter */
 	public static class LeafFilter extends RuleTerm
 	{
 		private Filter _filt;
 
 		public LeafFilter(final Filter f) { _filt = f; }
 		public FilterOp getType()   { return FilterOp.LEAF_FILTER; }
 		public Object getOperand1() { return _filt; }
 		public Object getOperand2() { return null; }
 		public String toString()    { return "[" + _filt.getName() + "]"; }
 		public void print(PrintWriter pw) { pw.println(_indent + _filt.getName()); }
 		public void collectUsedConcepts(final Set<Concept> usedConcepts) { _filt.collectUsedConcepts(usedConcepts); }
 		public int getMatchScore(Filter f, NewsItem article, int numTokens, Hashtable matchScores) { return _filt.getMatchScore(article, numTokens, matchScores); }
 	}
 
 	/* class SourceFilter encodes a source filter */
 	public static class SourceFilter extends RuleTerm
 	{
 		private NR_SourceCollection _srcColl;
 
 		public SourceFilter(final NR_SourceCollection coll) { _srcColl = coll; }
 		public FilterOp getType()   { return FilterOp.SOURCE_FILTER; }
 		public Object getOperand1() { return _srcColl; }
 		public Object getOperand2() { return null; }
 		public String toString()    { return "[" + _srcColl.getName() + "]"; }
 		public void print(PrintWriter pw) { pw.println(_indent + _srcColl.getName()); }
 		public void collectUsedConcepts(final Set<Concept> usedConcepts) { }
 		public int getMatchScore(Filter f, NewsItem article, int numTokens, Hashtable matchScores) {
 			return _srcColl.containsFeed(article.getFeed()) ? GLOBAL_MIN_MATCH_SCORE : 0;
 		}
 	}
 
 	/* class LeafCategory encodes a leaf category */
 	public static class LeafCategory extends RuleTerm
 	{
 		private Category _cat;
 
 		public LeafCategory(final Category c) { _cat = c; }
 		public FilterOp getType()   { return FilterOp.LEAF_CAT; }
 		public Object getOperand1() { return _cat; }
 		public Object getOperand2() { return null; }
 		public String toString()    { return "[" + _cat.getName() + "]"; }
 		public void print(PrintWriter pw) { pw.println(_indent + _cat.getName()); }
 		public void collectUsedConcepts(Set<Concept> usedConcepts) { }
 
 		public int getMatchScore(Filter f, NewsItem article, int numTokens, Hashtable matchScores)
 		{
 				// FIXME: Use hashcode instead!
 			Score mc = (Score)matchScores.get("[" + _cat.getName() + "]");
 			if (mc == null) {
 				if (_log.isDebugEnabled()) _log.debug("CAT " + _cat.getName() + " in issue " + _cat.getIssue().getName() + " being processed recursively!");
 				mc = _cat.getMatchScore(article, numTokens, matchScores);
 			}
 			return mc.value();
 		}
 	}
 
 	/* class ContextTerm encodes a context-sensitive
 	 * concept term. */
 	public static class ContextTerm extends RuleTerm
 	{
 		private List     _context;
 		private RuleTerm _r;
 
 		public ContextTerm(final RuleTerm r, final List context) { _r = r; _context = new ArrayList(); _context.addAll(context); }
 		public FilterOp getType()   { return FilterOp.CONTEXT_TERM; }
 		public Object getOperand1() { return _r; }
 		public Object getOperand2() { return _context; }
 
 		public String toString()
 		{
 			final StringBuffer b = new StringBuffer();
 			final Iterator it = _context.iterator();
 			b.append("|" + ((Concept)it.next()).getName());
 			while (it.hasNext())
 				b.append(", " + ((Concept)it.next()).getName());
 			b.append("|." + _r.toString());
 			return b.toString();
 		}
 
 		public void print(PrintWriter pw)
 		{
 			pw.println(_indent);
 			final Iterator it = _context.iterator();
 			pw.print(it.next());
 			while (it.hasNext())
 				pw.print(", " + it.next());
 			pw.print(".");
 			_r.print(pw);
 		}
 
 		public void collectUsedConcepts(final Set<Concept> usedConcepts)
 		{
 			final Iterator it = _context.iterator();
 			while (it.hasNext()) {
 				final Concept c = (Concept)it.next();
 				usedConcepts.add(c);
 			}
 			_r.collectUsedConcepts(usedConcepts);
 		}
 
 		public int getMatchScore(Filter f, NewsItem article, int numTokens, Hashtable matchScores)
 		{
 				// First, check if the context matches
 			boolean contextMatched = false;
 			final Iterator it = _context.iterator();
 			while (it.hasNext()) {
 				final Concept c = (Concept)it.next();
 				if (matchScores.get(c.getLexerToken().getToken()) != null) {
 					contextMatched = true;
 					break;
 				}
 			}
 
 			if (!contextMatched)
 				return 0;
 
 			return _r.getMatchScore(f, article, numTokens, matchScores);
 		}
 	}
 
 	/* class NegConcept encodes a negation of a concept */
 	public static class NegTerm extends RuleTerm
 	{
 		private RuleTerm _t;
 
 		public NegTerm(final RuleTerm t) { _t = t; }
 		public FilterOp getType()   { return FilterOp.NOT_TERM; }
 		public Object getOperand1() { return _t; }
 		public Object getOperand2() { return null; }
 		public String toString()    { return "-" + _t.toString(); }
 		public void print(PrintWriter pw) { pw.println(_indent + "-" + _t); }
 		public void collectUsedConcepts(final Set<Concept> usedConcepts) { _t.collectUsedConcepts(usedConcepts); }
 
 		public int getMatchScore(Filter f, NewsItem article, int numTokens, Hashtable matchScores)
 		{
 				// FIXME: This is very strict!
 			final int score = (1 - _t.getMatchScore(f, article, numTokens, matchScores));
 			return (score > 0) ? f._minMatchScore : 0;
 		}
 	}
 
 	/* class AndOrTerm encodes an AND/OR expression */
 	public static class AndOrTerm extends RuleTerm
 	{
 		private FilterOp 	_op;		// AND / OR?
 		private RuleTerm  _lTerm;	// Left term
 		private RuleTerm  _rTerm;	// Right term
 
 		public AndOrTerm(final FilterOp op, final RuleTerm lt, final RuleTerm rt) { _op  = op; _lTerm = lt; _rTerm = rt; }
 		public FilterOp getType()   { return _op; }
 		public Object getOperand1() { return _lTerm; }
 		public Object getOperand2() { return _rTerm; }
 		public String toString()    { return "(" + _lTerm.toString() + ((_op == FilterOp.AND_TERM) ? " AND " : " OR ") + _rTerm.toString() + ")"; }
 
 		public void print(PrintWriter pw)
 		{
 			final String old = _indent;
 			_indent = old + StringUtils.TAB;
 
 			if (_op == FilterOp.AND_TERM)
 				pw.println(old + " AND ");
 			else
 				pw.println(old + " OR ");
 			_lTerm.print(pw);
 			_rTerm.print(pw);
 
 			_indent = old;
 		}
 
 		public void collectUsedConcepts(final Set<Concept> usedConcepts)
 		{
 			_lTerm.collectUsedConcepts(usedConcepts);
 			_rTerm.collectUsedConcepts(usedConcepts);
 		}
 
 		public int getMatchScore(Filter f, NewsItem article, int numTokens, Hashtable matchScores)
 		{
 			final int lscore = _lTerm.getMatchScore(f, article, numTokens, matchScores);
 			final int rscore = _rTerm.getMatchScore(f, article, numTokens, matchScores);
 			if (_op == FilterOp.AND_TERM) {
 				if ((lscore >= f._minMatchScore) && (rscore >= f._minMatchScore))
 					return (lscore + rscore)/2;
 				else
 					return Math.min(lscore, rscore);
 			} 
 			else {
 				return lscore + rscore;
 			}
 		}
 	}
 
 	public static class ProximityTerm extends RuleTerm
 	{
 		private Concept _c1;
 		private Concept _c2;
 		private int     _proximityVal;
 
 		public ProximityTerm(final Concept c1, final Concept c2, Integer pval) { _c1 = c1; _c2 = c2; _proximityVal = pval.intValue(); }
 		public FilterOp getType() { return FilterOp.PROXIMITY_TERM; }
 		public Object getOperand1() { return _c1; }
 		public Object getOperand2() { return _c2; }
 		public int    getProximityVal() { return _proximityVal; }
 		public String toString() { return _c1.getName() + " ~" + _proximityVal + " " + _c2.getName(); }
 		public void collectUsedConcepts(final Set<Concept> usedConcepts) { usedConcepts.add(_c1); usedConcepts.add(_c2); }
 
 		public void print(PrintWriter pw) 
 		{ 
 			pw.println(_indent + _c1.getLexerToken().getToken() + " ~" + _proximityVal + " " + _c2.getLexerToken().getToken()); 
 		}
 
 		public int getMatchScore(Filter f, NewsItem article, int numTokens, Hashtable matchScores) 
 		{ 
 			Score s1 = (Score)matchScores.get(_c1.getLexerToken().getToken());
 			Score s2 = (Score)matchScores.get(_c2.getLexerToken().getToken());
 			if ((s1 == null) || (s2 == null))
 				return 0;
 
 			Iterator<Integer> mp1 = s1.getMatchPosns().iterator();
 			Iterator<Integer> mp2 = s2.getMatchPosns().iterator();
 
 				// Now process the match positions mp1 and mp2 
 			int     score = 0;
 			Integer p1 = mp1.hasNext() ? mp1.next() : null;
 			Integer p2 = mp2.hasNext() ? mp2.next() : null;
 			while ((p1 != null) && (p2 != null)) {
 					// -1 because we are looking for count of intervening concepts
 					// So, in the string "tribal leader", tribal and leader have 0 intervening concepts
 					// even though the difference in position is 1
 				int diff = p1 < p2 ? p2 - p1 - 1: p1 - p2 - 1;	
 				if (diff <= _proximityVal) {
 						// Match -- move on to next pair
 					score++;
 					p1 = mp1.hasNext() ? mp1.next() : null;
 					p2 = mp2.hasNext() ? mp2.next() : null;
 				}
 				else if (p1 < p2) {
 						// No match .. too far .. move to next c1 position, leave c2 as is.
 					p1 = mp1.hasNext() ? mp1.next() : null;
 				}
 				else {
 						// No match .. too far .. move to next c2 position, leave c1 as is.
 					p2 = mp2.hasNext() ? mp2.next() : null;
 				}
 			}
 
 			return score;
 		}
 	}
 }
