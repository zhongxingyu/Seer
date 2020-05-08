 package dfh.grammar;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import dfh.grammar.Label.Type;
 
 /**
  * Creates {@link Rule} objects from stringified specifications. This is a bunch
  * of stateless functional code that assists a {@link Compiler} object.
  * <p>
  * <b>Creation date:</b> Feb 19, 2011
  * 
  * @author David Houghton
  */
 public class RuleParser {
 	/**
 	 * Basic pattern of a labeled rule.
 	 */
 	public static final Pattern labelPattern = Pattern.compile("<(\\w++)>");
 	/**
 	 * Pattern matching the rule identifier to the left of the equals sign in a
 	 * rule definition.
 	 */
 	public static final Pattern leftValuePattern = Pattern
 			.compile("(?:<(\\w++)>|(\\w++))");
 	/**
 	 * Pattern that defines a rule as "<"<name>">" "=" <remainder>
 	 */
 	public static final Pattern basePattern = Pattern.compile("\\s*+"
 			+ leftValuePattern + "\\s*+=\\s*+(.*?)\\s*+");
 	/**
 	 * Pattern of repetition symbols such as <code>*</code>.
 	 */
 	public static final Pattern repetitionPattern = Pattern
 			.compile("(?:([?*+]|\\{\\d*+(?:,\\d*+)?\\})([+?]?+))?+");
 
 	/**
 	 * Pattern for comments and blank lines.
 	 */
 	public static final Pattern ignorePattern = Pattern
 			.compile("^\\s*+(?:#.*)?$");
 
 	/**
 	 * Parses a line of the string representation of a grammar. Does
 	 * tokenization and parsing but does not check completeness of rule set.
 	 * 
 	 * @param line
 	 *            string representation of a grammar rule
 	 * @return line parsed into properly nested tokens
 	 * @throws GrammarException
 	 */
 	public static LinkedList<RuleFragment> parse(String line)
 			throws GrammarException {
 		if (line == null)
 			throw new GrammarException("cannot parse nulls");
 		if (ignorePattern.matcher(line).matches())
 			return null;
 		Matcher m = basePattern.matcher(line);
 		if (m.matches()) {
 			String id = m.group(1) == null ? m.group(2) : m.group(1);
 			String remainder = m.group(3);
 			if (remainder.length() == 0)
 				throw new GrammarException("no rule body provided in " + line);
 			LinkedList<RuleFragment> parse = new LinkedList<RuleFragment>();
 			Type t;
 			if (id.equals(Label.ROOT)) {
 				t = Type.root;
 			} else
 				t = Type.indeterminate;
 			// we've parsed out the rule label
 			parse.add(new Label(t, id));
 			int[] offset = { 0 };
 			LinkedList<RuleFragment> body = parseBody(remainder, offset,
 					(char) 0);
 			checkBarriers(body.peekLast() instanceof ConditionFragment ? body
 					.subList(0, body.size() - 1) : body);
 			parse.addAll(body);
 			return parse;
 		} else
 			throw new GrammarException("ill-formed rule: " + line);
 	}
 
 	/**
 	 * Makes sure we don't have any '::' barriers unaccompanied by other rule
 	 * fragments.
 	 * 
 	 * @param body
 	 */
 	private static void checkBarriers(List<RuleFragment> body) {
 		boolean oneElementList = body.size() == 1;
 		for (RuleFragment r : body)
 			checkBarriers(r, oneElementList);
 	}
 
 	private static void checkBarriers(RuleFragment r, boolean oneElementList) {
 		if (oneElementList && r instanceof BarrierFragment)
 			throw new GrammarException(
 					"all backtracking barriers must occur as members of a sequence");
 		if (r instanceof GroupFragment) {
 			GroupFragment gf = (GroupFragment) r;
 			for (List<RuleFragment> alternate : gf.alternates)
 				checkBarriers(alternate);
 		}
 	}
 
 	/**
 	 * Parses portion of rule to right of "=".
 	 * 
 	 * @param body
 	 *            body of rule
 	 * @param offset
 	 *            single element int array allowing pass-by-reference for int,
 	 *            defines start of substring being parsed
 	 * @param bracket
 	 *            end bracket character being sought, 0 when none is sought
 	 * @return
 	 * @throws GrammarException
 	 */
 	private static LinkedList<RuleFragment> parseBody(String body,
 			int[] offset, char bracket) throws GrammarException {
 		LinkedList<RuleFragment> parse = new LinkedList<RuleFragment>();
 		GroupFragment gf = null;
 		OUTER: while (offset[0] < body.length()) {
 			trimWhitespace(body, offset);
 			if (offset[0] == body.length())
 				break;
 			char c = body.charAt(offset[0]);
 			switch (c) {
 			case '[':
 				offset[0]++;
 				Set<String> alternateTags = alternateTags(body, offset);
 				GroupFragment r = new GroupFragment(
 						parseBody(body, offset, ']'), alternateTags);
 				Repetition rep = getRepetition(body, offset);
 				if (rep.redundant() && r.alternates.size() == 1
 						&& r.alternates.get(0).size() == 1
 						&& alternateTags.isEmpty()) {
 					// redundant brackets
 					if (gf == null)
 						parse.addAll(r.alternates.get(0));
 					else {
 						for (RuleFragment rf : r.alternates.get(0))
 							gf.add(rf);
 					}
 				} else {
 					r.setRepetition(rep);
 					add(parse, gf, r);
 				}
 				break;
 			case '|':
 				if (gf == null) {
 					gf = new GroupFragment(parse, new TreeSet<String>());
 					parse.clear();
 					parse.add(gf);
 				} else
 					gf.newSequence();
 				offset[0]++;
 				break;
 			case '/':
 				Regex rx = getRegex(body, offset);
 				rep = getRepetition(body, offset);
 				rx.setRepetition(rep);
 				add(parse, gf, rx);
 				break;
 			case '"':
 			case '\'':
 				String literal = getLiteral(body, offset, c);
 				RepeatableRuleFragment rrf = new LiteralFragment(literal);
 				rep = getRepetition(body, offset);
 				rrf.setRepetition(rep);
 				add(parse, gf, rrf);
 				break;
 			case '#':
 				break OUTER;
 			case '(':
 				if (parse.isEmpty())
 					throw new GrammarException("condition without rule in "
 							+ body);
 				ConditionFragment cond = getCondition(body, offset);
 				parse.add(cond);
 				break OUTER;
 			case ':':
 				BarrierFragment bf = getBarrier(body, offset);
 				if (bf.id.equals(":")) {
 					if (gf != null) {
 						if (gf.currentSequence.isEmpty())
 							throw new GrammarException(
 									"':' is redundant as the first element of a sequence: "
 											+ body);
 					} else if (parse.isEmpty())
 						throw new GrammarException(
 								"':' is redundant as the first element of a sequence: "
 										+ body);
 				}
 				add(parse, gf, bf);
 				break;
 			case '~':
 			case '!':
 				AssertionFragment as = getAssertion(body, offset);
 				add(parse, gf, as);
 				break;
 			default:
 				if (c == bracket) {
 					offset[0]++;
 					if (gf != null)
 						gf.done();
 					bracket = 0;
 					break OUTER;
 				} else if (Character.isDigit(c)) {
 					int reference = getBackReference(body, offset);
 					if (reference == 0)
 						throw new GrammarException(
 								"back references must be greater than 0");
 					boolean isUpLevel = false;
 					if (offset[0] < body.length()
 							&& body.charAt(offset[0]) == '^') {
 						isUpLevel = true;
 						offset[0]++;
 					}
 					rep = getRepetition(body, offset);
 					if (!(rep == Repetition.NONE || isUpLevel)) {
 						throw new GrammarException(
 								"simple back reference cannot be modified with repetition suffix; use uplevel backreference; e.g., "
										+ reference + "^" + rep);
 					}
 					if (!isUpLevel) {
 						if (gf == null) {
 							if (reference > parse.size())
 								throw new GrammarException("back reference "
 										+ reference + " is too big");
 						} else {
 							if (reference > gf.currentSequence.size())
 								throw new GrammarException("back reference "
 										+ reference + " is too big");
 						}
 					}
 					if (isUpLevel) {
 						UplevelBackReferenceFragment ubrf = new UplevelBackReferenceFragment(
 								reference);
 						ubrf.setRepetition(rep);
 						add(parse, gf, ubrf);
 					} else {
 						BackReferenceFragment brf = new BackReferenceFragment(
 								reference);
 						add(parse, gf, brf);
 					}
 				} else {
 					RuleFragment ru = nextRule(body, offset, bracket);
 					if (ru instanceof RepeatableRuleFragment) {
 						rep = getRepetition(body, offset);
 						((RepeatableRuleFragment) ru).setRepetition(rep);
 					}
 					add(parse, gf, ru);
 				}
 			}
 		} // OUTER
 		if (bracket > 0)
 			throw new GrammarException("could not find closing '" + bracket
 					+ "' in " + body);
 		if (parse.isEmpty())
 			throw new GrammarException("empty rule body: " + body);
 		if (gf != null)
 			gf.done();
 		completeAssertions(parse, body);
 		return parse;
 	}
 
 	private static Regex getRegex(String body, int[] offset) {
 		boolean escaped = false, terminated = false;
 		int start = offset[0];
 		OUTER: while (++offset[0] < body.length()) {
 			char c = body.charAt(offset[0]);
 			if (terminated) {
 				switch (c) {
 				case 'r':
 				case 'i':
 				case 'm':
 				case 's':
 				case 'd':
 				case 'u':
 				case 'x':
 					break;
 				default:
 					break OUTER;
 				}
 			} else {
 				if (escaped) {
 					escaped = false;
 				} else {
 					switch (c) {
 					case '\\':
 						escaped = true;
 						break;
 					case '/':
 						terminated = true;
 					}
 				}
 			}
 		}
 		if (!terminated)
 			throw new GrammarException("unterminated regular expression in "
 					+ body);
 		return new Regex(body.substring(start, offset[0]));
 	}
 
 	/**
 	 * Parses out tags after square brackets. E.g.,
 	 * 
 	 * <pre>
 	 *   &lt;a&gt; = [{foo} 'a'++ 'b' ] | [{quux} 'c'++ 'd']
 	 * </pre>
 	 * 
 	 * @param body
 	 * @param offset
 	 * @return set of tags found
 	 */
 	private static Set<String> alternateTags(String body, int[] offset) {
 		trimWhitespace(body, offset);
 		Set<String> alternateTags = new TreeSet<String>();
 		if (body.charAt(offset[0]) == '{') {
 			offset[0]++;
 			boolean escaped = false, foundClose = false;
 			int start = offset[0];
 			while (!foundClose && offset[0] < body.length()) {
 				if (escaped) {
 					escaped = false;
 				} else {
 					char c = body.charAt(offset[0]);
 					switch (c) {
 					case '\\':
 						escaped = true;
 						break;
 					case '}':
 						foundClose = true;
 					case ',':
 						String tag = body.substring(start, offset[0]);
 						if (tag.length() == 0)
 							throw new GrammarException("zero length tag in "
 									+ body);
 						alternateTags.add(tag);
 						start = offset[0] + 1;
 						break;
 					}
 				}
 				offset[0]++;
 			}
 		}
 		return alternateTags;
 	}
 
 	/**
 	 * Joins assertions to their constituent rules.
 	 * 
 	 * @param parse
 	 */
 	private static void completeAssertions(LinkedList<RuleFragment> parse,
 			String body) {
 		if (parse.peekLast() instanceof AssertionFragment)
 			throw new GrammarException("no rule after assertion marker in "
 					+ body);
 		RuleFragment previous = null;
 		for (Iterator<RuleFragment> i = parse.iterator(); i.hasNext();) {
 			RuleFragment rf = i.next();
 			if (rf instanceof GroupFragment) {
 				GroupFragment gf = (GroupFragment) rf;
 				for (LinkedList<RuleFragment> list : gf.alternates)
 					completeAssertions(list, body);
 			}
 			if (previous != null && previous instanceof AssertionFragment) {
 				if (rf instanceof AssertionFragment)
 					throw new GrammarException(
 							"two consecutive assertion markers in " + body);
 				if (rf instanceof BarrierFragment)
 					throw new GrammarException(
 							"assertion marker immediately before backtracking barrier in "
 									+ body);
 				if (rf instanceof ConditionFragment)
 					throw new GrammarException(
 							"assertion marker immediately before condition in "
 									+ body);
 				AssertionFragment af = (AssertionFragment) previous;
 				af.rf = rf;
 				i.remove();
 			}
 			previous = rf;
 		}
 	}
 
 	private static AssertionFragment getAssertion(String body, int[] offset) {
 		// TODO implement modifier for backwards assertions
 		boolean positive = body.charAt(offset[0]) == '~';
 		boolean forward = true;
 		offset[0]++;
 		// TODO check to see whether this can ever be true
 		if (offset[0] == body.length())
 			throw new GrammarException("no rule after assertion marker: "
 					+ body);
 		char c = body.charAt(offset[0]);
 		if (c == '+' || c == '-') {
 			forward = c == '+';
 			offset[0]++;
 			// TODO see previous
 			if (offset[0] == body.length())
 				throw new GrammarException("no rule after assertion marker: "
 						+ body);
 		}
 		return new AssertionFragment(positive, forward);
 	}
 
 	private static int getBackReference(String body, int[] offset) {
 		int start = offset[0];
 		while (offset[0] < body.length()
 				&& Character.isDigit(body.charAt(offset[0]))) {
 			offset[0]++;
 		}
 		return Integer.parseInt(body.substring(start, offset[0]));
 	}
 
 	private static void add(List<RuleFragment> parse, GroupFragment gf,
 			RuleFragment r) {
 		if (gf == null)
 			parse.add(r);
 		else
 			gf.add(r);
 	}
 
 	/**
 	 * @param body
 	 * @param offset
 	 * @return quote delimited String literal
 	 */
 	private static String getLiteral(String body, int[] offset, char delimiter) {
 		boolean escaped = false, everEscaped = false;
 		int start = offset[0] + 1;
 		boolean found = false;
 		while (offset[0] + 1 < body.length()) {
 			offset[0]++;
 			char c = body.charAt(offset[0]);
 			if (escaped)
 				escaped = false;
 			else if (c == '\\')
 				everEscaped = escaped = true;
 			else if (c == delimiter) {
 				found = true;
 				break;
 			}
 		}
 		if (!found)
 			throw new GrammarException("could not find closing \" in " + body);
 		String s = body.substring(start, offset[0]);
 		offset[0]++;
 		return everEscaped ? cleanEscapes(s) : s;
 	}
 
 	private static String cleanEscapes(String s) {
 		StringBuilder b = new StringBuilder();
 		boolean escaped = false;
 		for (int i = 0; i < s.length(); i++) {
 			char c = s.charAt(i);
 			switch (c) {
 			case 'n':
 				b.append(escaped ? '\n' : c);
 				escaped = false;
 				break;
 			case 'r':
 				b.append(escaped ? '\r' : c);
 				escaped = false;
 				break;
 			case 'f':
 				b.append(escaped ? '\f' : c);
 				escaped = false;
 				break;
 			case 't':
 				b.append(escaped ? '\t' : c);
 				escaped = false;
 				break;
 			case 'b':
 				b.append(escaped ? '\b' : c);
 				escaped = false;
 				break;
 			case '\\':
 				if (escaped) {
 					b.append(c);
 					escaped = false;
 				} else
 					escaped = true;
 				break;
 			default:
 				escaped = false;
 				b.append(c);
 			}
 		}
 		return b.toString();
 	}
 
 	private static Repetition getRepetition(String body, int[] offset)
 			throws GrammarException {
 		Matcher m = repetitionPattern.matcher(body.substring(offset[0]));
 		// necessarily matches because it will match the null string
 		m.lookingAt();
 		Repetition r = null;
 		if (m.group().length() == 0)
 			return Repetition.NONE;
 		else {
 			String base = m.group(1);
 			String modifier = m.group(2);
 			switch (base.charAt(0)) {
 			case '*':
 				if (modifier.equals(""))
 					r = Repetition.ASTERISK;
 				else if (modifier.equals("+"))
 					r = Repetition.ASTERISK_P;
 				else
 					r = Repetition.ASTERISK_Q;
 				break;
 			case '+':
 				if (modifier.equals(""))
 					r = Repetition.PLUS;
 				else if (modifier.equals("+"))
 					r = Repetition.PLUS_P;
 				else
 					r = Repetition.PLUS_Q;
 				break;
 			case '?':
 				if (modifier.equals(""))
 					r = Repetition.QMARK;
 				else if (modifier.equals("+"))
 					r = Repetition.QMARK_P;
 				else
 					r = Repetition.QMARK_Q;
 				break;
 			case '{':
 				// trim off curly brackets
 				base = base.substring(1, base.length() - 1);
 				if (base.length() == 0 || base.equals(","))
 					throw new GrammarException("bad repetition modifier: {"
 							+ base + '}');
 				int index = base.indexOf(','),
 				top,
 				bottom;
 				if (index == -1) {
 					top = bottom = Integer.parseInt(base);
 				} else if (index == 0) {
 					bottom = 0;
 					top = Integer.parseInt(base.substring(1));
 				} else if (index == base.length() - 1) {
 					bottom = Integer.parseInt(base.substring(0, index));
 					top = Integer.MAX_VALUE;
 				} else {
 					bottom = Integer.parseInt(base.substring(0, index));
 					top = Integer.parseInt(base.substring(index + 1));
 				}
 				Repetition.Type t;
 				if (modifier.equals(""))
 					t = Repetition.Type.greedy;
 				else if (modifier.equals("+"))
 					t = Repetition.Type.possessive;
 				else
 					t = Repetition.Type.stingy;
 				r = new Repetition(t, bottom, top);
 				break;
 			default:
 				throw new GrammarException("impossible repetition: "
 						+ m.group());
 			}
 			offset[0] += m.group().length();
 		}
 		return r;
 	}
 
 	private static RuleFragment nextRule(String body, int[] offset, char bracket)
 			throws GrammarException {
 		Matcher m = labelPattern.matcher(body.substring(offset[0]));
 		if (m.lookingAt()) {
 			offset[0] += m.end();
 			String id = m.group(1);
 			if (id.equals(Label.ROOT))
 				return new Label(Label.Type.root, id);
 			else
 				return new Label(Label.Type.indeterminate, id);
 		} else
 			throw new GrammarException("ill-formed rule: " + body);
 	}
 
 	/**
 	 * Adjust offset to end of string or next non-whitespace character.
 	 * 
 	 * @param body
 	 * @param offset
 	 */
 	private static void trimWhitespace(String body, int[] offset) {
 		while (offset[0] < body.length()
 				&& Character.isWhitespace(body.charAt(offset[0])))
 			offset[0]++;
 	}
 
 	private static ConditionFragment getCondition(String body, int[] offset)
 			throws GrammarException {
 		offset[0]++;
 		int start = offset[0], bracketCount = 1;
 		while (offset[0] < body.length() && bracketCount > 0) {
 			switch (body.charAt(offset[0]++)) {
 			case '(':
 				bracketCount++;
 				break;
 			case ')':
 				bracketCount--;
 				break;
 			}
 		}
 		if (bracketCount > 0)
 			throw new GrammarException("ill formed condition in " + body);
 		int end = offset[0] - 1;
 		if (start - end == 1)
 			throw new GrammarException("zero-width condition identifier in "
 					+ body);
 		@SuppressWarnings("unused")
 		String s = body.substring(start, end);
 		trimWhitespace(body, offset);
 		if (offset[0] < body.length() && body.charAt(offset[0]) != '#')
 			throw new GrammarException(
 					"no content other than a comment permitted after a condition: "
 							+ body);
 		return new ConditionFragment(body.substring(start, end));
 	}
 
 	private static BarrierFragment getBarrier(String body, int[] offset)
 			throws GrammarException {
 		int count = 0;
 		while (offset[0] < body.length() && body.charAt(offset[0]) == ':') {
 			offset[0]++;
 			count++;
 		}
 		if (count > 2)
 			throw new GrammarException("two many colons in '" + body
 					+ "' barriers must appear singly");
 		return new BarrierFragment(count == 1);
 	}
 }
