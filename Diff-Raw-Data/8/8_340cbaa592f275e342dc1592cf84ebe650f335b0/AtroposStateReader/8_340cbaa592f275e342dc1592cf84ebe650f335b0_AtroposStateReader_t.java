 package edu.bu;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Reads in an {@link AtroposState} from an {@link InputStream}.
  * 
  * @author dml
  * 
  */
 public class AtroposStateReader {
 	private final Reader input;
 
 	/**
 	 * @param input
 	 *            - the {@link Reader} this {@link AtroposStateReader} will
 	 *            read from.
 	 */
 	public AtroposStateReader(Reader input) {
 		this.input = input;
 	}
 	
 	public AtroposState read() throws IOException {
 		StringBuilder result = new StringBuilder();
 		while (true) {
 			int read = input.read();
 			if (read < 0) {
 				break;
 			}
 			result.append((char) read);
 		}
 		Pair<List<List<Integer>>, List<Integer>> state = seq(rows, lastPlay).parse(result.toString()).token;
 		int size = state.first.size();
 		AtroposCircle[][] board = new AtroposCircle[size][size];
 		for (int i = 0; i < size - 1; ++i) {
 			List<Integer> row = state.first.get(i);
 			for (int j = 0; j < size; ++j) {
 				int height = size - i - 1;
 				board[height][j] = new AtroposCircle(j < row.size() ? row.get(j) : Colors.Uncolored.getValue(), size - i - 1, j, size - height - j);
 			}
 		}
 		// unroll last iteration
 		List<Integer> row = state.first.get(size - 1);
		board[0][0] = new AtroposCircle(Colors.Uncolored.getValue(), 0, 0, size);
		for (int j = 0; j < row.size(); ++j) {
			board[0][j + 1] = new AtroposCircle(j < row.size() ? row.get(j)
					: Colors.Uncolored.getValue(), 0, j + 1, size - 1 - j);
 		}
 		
 		return new AtroposState(board, new AtroposCircle(state.second.get(0),
 				state.second.get(1), state.second.get(2), state.second.get(3)));
 	}
 	
 	private static class ParseResult<T> {
 		public final T token;
 		public final String rest;
 		public ParseResult(T token, String rest) {
 			this.token = token;
 			this.rest = rest;
 		}
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return "<<" + token + ", \"" + rest + "\">>";
 		}
 	}
 	
 	public static class ParseException extends RuntimeException {
 		private static final long serialVersionUID = -5822709037165835325L;
 		public ParseException() {
 			super();
 		}
 		public ParseException(String message, Throwable cause) {
 			super(message, cause);
 		}
 		public ParseException(String message) {
 			super(message);
 		}
 		public ParseException(Throwable cause) {
 			super(cause);
 		}
 	}
 	
 	public static interface Parser<T> {
 		ParseResult<T> parse(String toParse);
 		boolean isValid(String toParse);
 	}
 	public static final class Unit {
 		public static Unit unit = new Unit();
 		private Unit() {};
 	}
 	public static class Pair<A, B> {
 		public final A first;
 		public final B second;
 		public Pair(A first, B second) {
 			this.first = first;
 			this.second = second;
 		}
 	}
 	public static Parser<String> token(String token) {
 		return new Token(token);
 	}
 	public static class Token implements Parser<String> {
 		private final String token;
 
 		/**
 		 * @param token
 		 *            - the constant token to consume
 		 */
 		public Token(String token) {
 			this.token = token;
 		}
 		@Override
 		public ParseResult<String> parse(String toParse) {
 			if (!isValid(toParse)) {
 				throw new ParseException("Expected token \"" + token
 						+ "\" when parsing \"" + toParse + "\"");
 			}
 			return new ParseResult<String>(token, toParse.substring(token.length()));
 		}
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return "\"" + token + "\"";
 		}
 		@Override
 		public boolean isValid(String toParse) {
 			return toParse.startsWith(token);
 		}
 	}
 	public static class OneOfParser<T> implements Parser<T> {
 		private final List<Parser<T>> options;
 		/**
 		 * @param options
 		 */
 		public OneOfParser(List<Parser<T>> options) {
 			this.options = options;
 		}
 		@Override
 		public ParseResult<T> parse(String toParse) {
 			ParseResult<T> result = null;
 			for (Parser<T> option : options) {
 				if (option.isValid(toParse)) {
 					result = option.parse(toParse);
 					break;
 				}
 			}
 			if(result == null) {
 				throw new ParseException("Couldn't find " + options.toString() + " in \"" + toParse + "\"");
 			}
 			return result;
 		}
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return options.toString();
 		}
 
 		@Override
 		public boolean isValid(String toParse) {
 			for (Parser<T> option : options) {
 				if (option.isValid(toParse)) {
 					return true;
 				}
 			}
 			return false;
 		}
 	}
 	public static final Parser<String> zero = new Token("0");
 	public static final Parser<String> one = new Token("1");
 	public static final Parser<String> two = new Token("2");
 	public static final Parser<String> three = new Token("3");
 	public static final Parser<String> four = new Token("4");
 	public static final Parser<String> five = new Token("5");
 	public static final Parser<String> six = new Token("6");
 	public static final Parser<String> seven = new Token("7");
 	public static final Parser<String> eight = new Token("8");
 	public static final Parser<String> nine = new Token("9");
 	private static final List<Parser<String>> digitList = new ArrayList<Parser<String>>();
 	static {
 		digitList.add(zero);
 		digitList.add(one);
 		digitList.add(two);
 		digitList.add(three);
 		digitList.add(four);
 		digitList.add(five);
 		digitList.add(six);
 		digitList.add(seven);
 		digitList.add(eight);
 		digitList.add(nine);
 	}
 	public static final Parser<String> digit = new OneOfParser<String>(digitList);
 	public static <T> Parser<List<T>> many(Parser<T> component) {
 		return new Many<T>(component);
 	}
 	public static class Many<T> implements Parser<List<T>> {
 		private final Parser<T> parser;
 		public Many(Parser<T> parser) {
 			this.parser = parser;
 		}
 		@Override
 		public ParseResult<List<T>> parse(String toParse) {
 			List<T> results = new ArrayList<T>();
 			String remaining = toParse;
 			while(parser.isValid(remaining)) {
 				ParseResult<T> parsed = parser.parse(remaining);
 				remaining = parsed.rest;
 				results.add(parsed.token);
 			}
 			return new ParseResult<List<T>>(results, remaining);
 		}
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return "(" + parser + ")*";
 		}
 		@Override
 		public boolean isValid(String toParse) {
 			return true;
 		}
 	}
 	public static <A, B> Sequence<A, B> seq(Parser<A> first, Parser<B> second) {
 		return new Sequence<A, B>(first, second);
 	}
 	public static final class Sequence<A, B> implements Parser<Pair<A, B>> {
 		private final Parser<A> first;
 		private final Parser<B> second;
 		private Sequence(Parser<A> first, Parser<B> second) {
 			this.first = first;
 			this.second = second;
 		}
 		@Override
 		public ParseResult<Pair<A, B>> parse(String toParse) {
 			ParseResult<A> r0 = first.parse(toParse);
 			ParseResult<B> r1 = second.parse(r0.rest);
 			return new ParseResult<Pair<A, B>>(new Pair<A, B>(r0.token,
 					r1.token), r1.rest);
 		}
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return first + " " + second;
 		}
 		@Override
 		public boolean isValid(String toParse) {
 			return first.isValid(toParse)
 					&& second.isValid(first.parse(toParse).rest);
 		}
 	}
 	public static <A, B> Parser<A> asFirst(Sequence<A, B> sequence) {
 		return new AsFirst<A, B>(sequence);
 	}
 	public static final class AsFirst<A, B> implements Parser<A> {
 		public final Sequence<A, B> sequence;
 		public AsFirst(Sequence<A, B> sequence) {
 			this.sequence = sequence;
 		}
 		@Override
 		public ParseResult<A> parse(String toParse) {
 			ParseResult<Pair<A, B>> parsed = sequence.parse(toParse);
 			return new ParseResult<A>(parsed.token.first, parsed.rest);
 		}
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return "(" + sequence + ")[0]";
 		}
 		@Override
 		public boolean isValid(String toParse) {
 			return sequence.isValid(toParse);
 		}
 	}
 	public static <A, B> Parser<B> asSecond(Sequence<A, B> sequence) {
 		return new AsSecond<A, B>(sequence);
 	}
 	public static final class AsSecond<A, B> implements Parser<B> {
 		public final Sequence<A, B> sequence;
 		public AsSecond(Sequence<A, B> sequence) {
 			this.sequence = sequence;
 		}
 		@Override
 		public ParseResult<B> parse(String toParse) {
 			ParseResult<Pair<A, B>> parsed = sequence.parse(toParse);
 			return new ParseResult<B>(parsed.token.second, parsed.rest);
 		}
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return "(" + sequence + ")[1]";
 		}
 
 		@Override
 		public boolean isValid(String toParse) {
 			return sequence.isValid(toParse);
 		}
 	}
 	public static <T> OneOrMore<T> oneOrMore(Parser<T> component) {
 		return new OneOrMore<T>(component);
 	}
 	public static class OneOrMore<T> implements Parser<List<T>> {
 		private final Parser<T> parser;
 		public OneOrMore(Parser<T> parser) {
 			this.parser = parser;
 		}
 		@Override
 		public ParseResult<List<T>> parse(String toParse) {
 			ParseResult<Pair<T, List<T>>> parsed = seq(parser, new Many<T>(parser)).parse(toParse);
 			List<T> results = parsed.token.second;
 			results.add(0, parsed.token.first);
 			return new ParseResult<List<T>>(results, parsed.rest);
 		}
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return parser + "+";
 		}
 		@Override
 		public boolean isValid(String toParse) {
 			return parser.isValid(toParse);
 		}
 	}
 	public static <T> Parser<List<T>> separatedBy(Parser<T> element, String separator) {
 		return new SeparatedBy<T>(element, new Token(separator));
 	}
 	public static class SeparatedBy<T> implements Parser<List<T>> {
 		private final Parser<T> component;
 		private final Parser<String> separator;
 		public SeparatedBy(Parser<T> component, Parser<String> separator) {
 			this.component = component;
 			this.separator = separator;
 		}
 		@Override
 		public ParseResult<List<T>> parse(String toParse) {
 			ParseResult<Pair<List<T>, T>> parsed = seq(
 					many(asFirst(seq(component, separator))), component).parse(
 					toParse);
 			List<T> results = parsed.token.first;
 			results.add(parsed.token.second);
 			return new ParseResult<List<T>>(results, parsed.rest);
 		}
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return "(" + component +  separator + ")*";
 		}
 
 		@Override
 		public boolean isValid(String toParse) {
 			return component.isValid(toParse);
 		}
 
 	}
 	public static class Joined implements Parser<String> {
 		private final Parser<List<String>> chars;
 		public Joined(Parser<List<String>> chars) {
 			this.chars = chars;
 		}
 		@Override
 		public ParseResult<String> parse(String toParse) {
 			ParseResult<List<String>> parsed = chars.parse(toParse);
 			StringBuilder joined = new StringBuilder();
 			for (String digit : parsed.token) {
 				joined.append(digit);
 			}
 			return new ParseResult<String>(joined.toString(), parsed.rest);
 		}
 		@Override
 		public boolean isValid(String toParse) {
 			return chars.isValid(toParse);
 		}
 	}
 	public static final Parser<String> digits = new Joined(new OneOrMore<String>(digit));
 
 	public static Parser<Integer> integer = new IntegerParser();
 	private static class IntegerParser implements Parser<Integer> {
 		@Override
 		public ParseResult<Integer> parse(String toParse) {
 			ParseResult<String> parsed = digits.parse(toParse);
 			int result;
 			try {
 				result = Integer.parseInt(parsed.token);
 			} catch (NumberFormatException nfe) {
 				throw new ParseException("Unable to parse integer from \""
 						+ toParse + "\"", nfe);
 			}
 			return new ParseResult<Integer>(result, parsed.rest);
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return "<integer>";
 		}
 
 		@Override
 		public boolean isValid(String toParse) {
 			return digit.isValid(toParse);
 		}
 	}
 	public static Parser<Integer> baseTen = new Parser<Integer>() {
 		@Override
 		public boolean isValid(String toParse) {
 			return digit.isValid(toParse);
 		}
 
 		@Override
 		public ParseResult<Integer> parse(String toParse) {
 			ParseResult<String> parsed = digit.parse(toParse);
 			int result;
 			try {
 				result = Integer.parseInt(parsed.token);
 			} catch (NumberFormatException nfe) {
 				throw new ParseException("Failed parsing base 10 digit from \""
 						+ parsed.token + "\".  Remaining: " + parsed.rest, nfe);
 			}
 			return new ParseResult<Integer>(result, parsed.rest);
 		}};
 	
 	public static Parser<List<Integer>> row = asFirst(seq(asSecond(seq(
 			token("["), oneOrMore(baseTen))), token("]")));
 	public static Parser<List<List<Integer>>> rows = many(row);
 	public static Parser<List<Integer>> lastPlay = asFirst(seq(asSecond(seq(
 			token("LastPlay:("), separatedBy(integer, ","))), token(")")));
 
 }
