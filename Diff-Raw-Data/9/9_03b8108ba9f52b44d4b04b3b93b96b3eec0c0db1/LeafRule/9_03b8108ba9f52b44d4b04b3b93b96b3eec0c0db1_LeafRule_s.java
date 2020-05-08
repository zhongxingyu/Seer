 package dfh.grammar;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 /**
  * {@link Rule} defined over sequence of terminal objects rather than other
  * <code>Rules</code>. In particular, this is such a {@link Rule} when the
  * pattern of terminal characters is defined by a {@link Pattern}.
  * <p>
  * <b>Creation date:</b> Feb 19, 2011
  * 
  * @author David Houghton
  */
 @Reversible
 public class LeafRule extends Rule {
 	/**
 	 * For normalizing the unique ids of regexes that allow comments.
 	 */
 	private static final Pattern NORMALIZATION_PATTERN = Pattern.compile(
 			"#.*$", Pattern.MULTILINE);
 
 	private class LeafMatcher extends Matcher {
 		private Map<Integer, CachedMatch> cache;
 		private boolean fresh = true;
 
 		public LeafMatcher(CharSequence s, Integer offset,
 				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
 			super(s, offset, master);
 			this.cache = cache.get(label);
 		}
 
 		@Override
 		public Match match() {
 			if (options.debug)
 				LeafRule.this.matchTrace(this);
 			if (fresh) {
 				fresh = false;
 				CachedMatch cm = cache.get(offset);
 				if (cm == null) {
 					if (options.study) {
 						if (options.debug)
 							LeafRule.this.matchTrace(this, null);
 						return null;
 					}
 				} else {
 					if (options.debug)
 						LeafRule.this.matchTrace(this, cm.m);
 					return register(cm.m);
 				}
 				java.util.regex.Matcher m = p.matcher(s);
 				m.region(offset, options.end);
 				m.useTransparentBounds(true);
 				m.useAnchoringBounds(false);
 				if (m.lookingAt()) {
 					Match n = new Match(LeafRule.this, offset, m.end());
 					cm = new CachedMatch(n);
 				} else
 					cm = CachedMatch.MISMATCH;
 				cache.put(offset, cm);
 				if (options.debug)
 					LeafRule.this.matchTrace(this, cm.m);
 				return register(cm.m);
 			}
 			if (options.debug)
 				LeafRule.this.matchTrace(this, null);
 			return null;
 		}
 
 		@Override
 		public boolean mightHaveNext() {
 			return fresh;
 		}
 
 		@Override
 		public String toString() {
 			return "M:" + LeafRule.this;
 		}
 
 		@Override
 		protected Rule rule() {
 			return LeafRule.this;
 		}
 	}
 
 	private static final long serialVersionUID = 1L;
 	protected final Pattern p;
 	protected final boolean reversible;
 
 	/**
 	 * Generates {@link LeafRule} with given label and {@link Pattern}.
 	 * 
 	 * @param label
 	 * @param p
 	 */
 	public LeafRule(Label label, Pattern p, boolean reversible) {
 		super(label);
 		this.p = p;
 		this.reversible = reversible;
 	}
 
 	@Override
 	public Matcher matcher(CharSequence s, final Integer offset,
 			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
 		return new LeafMatcher(s, offset, cache, master);
 	}
 
 	@Override
 	protected String uniqueId() {
 		String s = descriptionWOCondition().toString();
 		if ((p.flags() & Pattern.COMMENTS) == Pattern.COMMENTS) {
 			// must normalize away spaces and comments
 			int index = s.lastIndexOf('/');
 			String suffix = s.substring(index);
 			s = s.substring(1, index);
 			StringBuilder b = new StringBuilder();
 			java.util.regex.Matcher m = NORMALIZATION_PATTERN.matcher(s);
 			int start = 0;
 			while (m.find()) {
 				b.append(s.substring(start, m.start()));
 				start = m.end();
 			}
 			if (start < s.length())
 				b.append(s.substring(start));
 			s = b.toString();
 			s = s.replaceAll("\\s++", "");
 			return '/' + s + suffix;
 		}
 		if (condition != null)
 			s += '(' + condition + ')';
 		return s;
 	}
 
 	@Override
 	public String description() {
 		StringBuilder b = descriptionWOCondition();
 		if (condition != null)
 			b.append(" (").append(condition).append(')');
 		return b.toString();
 	}
 
 	private StringBuilder descriptionWOCondition() {
 		StringBuilder b = new StringBuilder();
 		b.append('/');
 		b.append(p.toString());
 		b.append('/');
 		if (reversible)
 			b.append('r');
 		if ((p.flags() & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE)
 			b.append('i');
 		if ((p.flags() & Pattern.DOTALL) == Pattern.DOTALL)
 			b.append('s');
 		if ((p.flags() & Pattern.MULTILINE) == Pattern.MULTILINE)
 			b.append('m');
 		if ((p.flags() & Pattern.UNIX_LINES) == Pattern.UNIX_LINES)
 			b.append('d');
 		if ((p.flags() & Pattern.UNICODE_CASE) == Pattern.UNICODE_CASE)
 			b.append('u');
 		if ((p.flags() & Pattern.COMMENTS) == Pattern.COMMENTS)
 			b.append('x');
 		return b;
 	}
 
 	@Override
 	public Set<Integer> study(CharSequence s,
 			Map<Label, Map<Integer, CachedMatch>> cache,
 			Set<Rule> studiedRules, GlobalState options) {
 		Map<Integer, CachedMatch> subCache = cache.get(label);
 		Set<Integer> startOffsets = new HashSet<Integer>();
 		if (subCache.keySet().isEmpty()) {
 			if (studiedRules.contains(this))
 				return startOffsets;
 			else
 				studiedRules.add(this);
 			java.util.regex.Matcher m = p.matcher(s);
 			m.useAnchoringBounds(false);
 			m.useTransparentBounds(true);
 			m.region(options.start, options.end);
 			while (m.find()) {
 				Integer i = m.start();
 				startOffsets.add(i);
 				Match n = new Match(this, m.start(), m.end());
 				subCache.put(i, new CachedMatch(n));
 				int newStart = m.start() + 1;
 				if (newStart >= options.end)
 					break;
 				m.region(newStart, options.end);
 			}
 		} else {
 			startOffsets.addAll(subCache.keySet());
 		}
 		return startOffsets;
 	}
 
 	@Override
 	public boolean zeroWidth() {
 		return false;
 	}
 
 	@Override
 	public Rule shallowClone() {
 		LeafRule lr = new LeafRule((Label) label.clone(), p, reversible);
 		return lr;
 	}
 
 	@Override
 	public Rule conditionalize(Condition c, String id) {
 		return new ConditionalLeafRule(this, c, id);
 	}
 }
