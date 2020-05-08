 package eu.stratosphere.sopremo.io;
 
 import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
 import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
 import it.unimi.dsi.fastutil.chars.CharArrayList;
 import it.unimi.dsi.fastutil.chars.CharList;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import eu.stratosphere.core.fs.FSDataInputStream;
 import eu.stratosphere.sopremo.type.ArrayNode;
 import eu.stratosphere.sopremo.type.BigIntegerNode;
 import eu.stratosphere.sopremo.type.BooleanNode;
 import eu.stratosphere.sopremo.type.DecimalNode;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.sopremo.type.IntNode;
 import eu.stratosphere.sopremo.type.LongNode;
 import eu.stratosphere.sopremo.type.MissingNode;
 import eu.stratosphere.sopremo.type.NullNode;
 import eu.stratosphere.sopremo.type.ObjectNode;
 import eu.stratosphere.sopremo.type.TextNode;
 
 public class JsonParser {
 
 	private final BufferedReader reader;
 
 	private boolean reachedEnd;
 
 	private int currentCounter;
 
 	private boolean skipWrappingArray;
 
 	private static char ELEMENT_SEPARATOR = ',';
 
 	private static char ARRAY_START = '[';
 
 	private static int STEP_SIZE = 1;
 
 	private static String JSON_URL = "www.json.org";
 
 	private static String ERROR_BASE = "Couldn't parse the given input: \n";
 
 	private static String ERROR_INVALID_JSON = ERROR_BASE +
 		"Invalid json format at position %s (visit " + JSON_URL
 		+ " for a detailed specification).\nCurrent Token: %s.\nExpected \"%s\", but was \"%s\".";
 
 	private static String ERROR_IO = ERROR_BASE + "Couldn't access input or mark the input for reset at position %s";
 
 	public JsonParser(final FSDataInputStream stream) {
 		this(new InputStreamReader(stream, Charset.forName("utf-8")));
 	}
 
 	public JsonParser(final InputStream stream) {
 		this(new InputStreamReader(stream, Charset.forName("utf-8")));
 	}
 
 	/*
 	 * Constructors
 	 */
 	public JsonParser(final Reader inputStreamReader) {
 		this.reader = new BufferedReader(inputStreamReader);
 		this.initialize();
 	}
 
 	public JsonParser(final String value) {
 		this(new BufferedReader(new StringReader(value)));
 	}
 
 	public JsonParser(final URL url) throws IOException {
 		this(new BufferedReader(new InputStreamReader(url.openStream())));
 	}
 
 	/**
 	 * Checks if the whole input is already parsed.
 	 * 
 	 * @return either the whole input is already parsed or not
 	 */
 	public boolean checkEnd() {
 		return this.reachedEnd;
 	}
 
 	/**
 	 * Closes the stream to the input of this parser.
 	 * 
 	 * @throws IOException
 	 */
 	public void close() throws IOException {
 		this.reader.close();
 	}
 
 	/**
 	 * Returns the number of characters already parsed.
 	 * 
 	 * @return the number of characters
 	 */
 	public int getNumberOfParsedChars() {
 		return this.currentCounter - 1;
 	}
 
 	/*
 	 * Provided functionality
 	 */
 	/**
 	 * Parses the next possible element contained in the input and creates a corresponding {@link IJsonNode}. To specify
 	 * multiple elements in one input they must be separated with ','. After reaching the end of the input, each
 	 * subsequent call to this method will return a {@link MissingNode}.
 	 * 
 	 * @return the parsed element as an {@link IJsonNode}
 	 * @throws JsonParseException
 	 *         Should something went wrong during the parsing process, this exception will be thrown. To find out the
 	 *         reason of failure consult {@link JsonParseException#getMessage()}.
 	 */
 	public IJsonNode readValueAsTree() throws JsonParseException {
 		final boolean firstCall = this.currentCounter == 0;
 		if (this.checkEnd() && !this.skipWrappingArray)
 			return MissingNode.getInstance();
 		int currentChar = this.readIgnoreWhitespace();
 		if (this.checkForEOF(currentChar))
 			return this.getRoot().createJsonNode((char) currentChar, this);
 		if (firstCall && this.skipWrappingArray && (char) currentChar == ARRAY_START)
 			currentChar = this.readIgnoreWhitespace();
 		final IJsonNode result = this.parseElement(currentChar);
 		this.finishCurrentParsingStep();
 		return result;
 	}
 
 	public void setWrappingArraySkipping(final boolean skipWrappingArray) {
 		this.skipWrappingArray = skipWrappingArray;
 	}
 
 	private boolean checkForEOF(final int currentChar) {
 		if (currentChar == -1) {
 			this.reachedEnd = true;
 			return true;
 		}
 		return false;
 	}
 
 	private void finishCurrentParsingStep() throws JsonParseException {
 		int currentChar;
 		currentChar = this.readIgnoreWhitespace();
 		if (currentChar == -1)
 			this.reachedEnd = true;
 		else if ((char) currentChar != JsonParser.ELEMENT_SEPARATOR)
 			if (this.skipWrappingArray)
 				this.reachedEnd = true;
 			else
 				throw this.getParseException(this.getRoot().getName(), String.valueOf(ELEMENT_SEPARATOR) + " or eof",
 					String.valueOf((char) currentChar));
 	}
 
 	private JsonParseException getIOException() {
 		return new JsonParseException(String.format(JsonParser.ERROR_IO, this.getNumberOfParsedChars()));
 	}
 
 	private JsonParseException getParseException(final String currentToken, final String expectedValue,
 			final String currentValue) {
 		return new JsonParseException(String.format(JsonParser.ERROR_INVALID_JSON, this.getNumberOfParsedChars() + 1,
 			currentToken, expectedValue, currentValue));
 	}
 
 	private STATE getRoot() {
 		return STATE.ROOT;
 	}
 
 	private void initialize() {
 		STATE.initializeTransitions();
 		this.currentCounter = 0;
 		this.reachedEnd = false;
 	}
 
 	/*
 	 * private
 	 */
 	private void markReader() throws JsonParseException {
 		try {
 			this.reader.mark(JsonParser.STEP_SIZE);
 		} catch (final IOException e) {
 			throw this.getIOException();
 		}
 	}
 
 	private IJsonNode parseElement(final int currentChar) throws JsonParseException {
 		final STATE nextState = this.getRoot().nextState(
 			(char) currentChar);
 		if (nextState == null)
 			throw this.getParseException(this.getRoot().getName(), "one of ['{', '[', 't', 'f', 'n', 0-9, '\"', '-']",
 				String.valueOf((char) currentChar));
 		return nextState.createJsonNode((char) currentChar, this);
 	}
 
 	private int read() throws JsonParseException {
 		int character;
 		try {
 			character = this.reader.read();
 		} catch (final IOException e) {
 			throw this.getIOException();
 		}
 		this.currentCounter++;
 		return character;
 	}
 
 	private int readIgnoreWhitespace() throws JsonParseException {
 		int nextChar = this.read();
 		while (Character.isWhitespace((char) nextChar) && nextChar != -1)
 			nextChar = this.read();
 		return nextChar;
 	}
 
 	private void resetReader() throws JsonParseException {
 		try {
 			this.reader.reset();
 		} catch (final IOException e) {
 			throw this.getIOException();
 		}
 		this.currentCounter = this.currentCounter - JsonParser.STEP_SIZE;
 	}
 
 	private enum STATE {
 		OBJECT {
 			private final char charToFinishObject = '}';
 
 			private final char keyValueSeparator = ':';
 
 			private final char elementSeparator = ',';
 
 			@Override
 			public IJsonNode createJsonNode(final char startChar, final JsonParser parser) throws JsonParseException {
 
 				boolean currentInputIsAKey = true;
 				final ObjectNode result = new ObjectNode();
 				final StringBuffer currentKey = new StringBuffer();
 				int nextChar;
 
 				while ((nextChar = parser.readIgnoreWhitespace()) != this.charToFinishObject) {
 					if (this.handleSpecialChar(nextChar, currentInputIsAKey, parser))
 						continue;
 					currentInputIsAKey = this.handleElement(nextChar, currentKey, parser, currentInputIsAKey,
 						result);
 				}
 				if (!currentInputIsAKey)
 					throw parser.getParseException(this.getName(), "json value expected",
 						String.valueOf(this.charToFinishObject));
 				return result;
 			}
 
 			@Override
 			protected String getName() {
 				return "json object";
 			}
 
 			@Override
 			protected void initialize() {
 				this.transitions.put('{', STATE.OBJECT);
 				this.transitions.put('[', STATE.ARRAY);
 				this.transitions.put('t', STATE.BOOLEAN);
 				this.transitions.put('f', STATE.BOOLEAN);
 				this.transitions.put('n', STATE.NULL);
 				this.transitions.put('-', STATE.NUMBER);
 				this.transitions.put('0', STATE.NUMBER);
 				this.transitions.put('1', STATE.NUMBER);
 				this.transitions.put('2', STATE.NUMBER);
 				this.transitions.put('3', STATE.NUMBER);
 				this.transitions.put('4', STATE.NUMBER);
 				this.transitions.put('5', STATE.NUMBER);
 				this.transitions.put('6', STATE.NUMBER);
 				this.transitions.put('7', STATE.NUMBER);
 				this.transitions.put('8', STATE.NUMBER);
 				this.transitions.put('9', STATE.NUMBER);
 				this.transitions.put('"', STATE.STRING);
 			}
 
 			private boolean handleElement(final int nextChar, final StringBuffer currentKey, final JsonParser parser,
 					final boolean currentInputIsAKey,
 					final ObjectNode result) throws JsonParseException {
 				final STATE state = this.nextState((char) nextChar);
 				if (state == null)
 					throw parser.getParseException(this.getName(),
 						"one of ['{', '[', 't', 'f', 'n', '-' , 0-9, '\"']", String.valueOf((char) nextChar));
 				final IJsonNode element = state.createJsonNode((char) nextChar,
 					parser);
 				if (currentInputIsAKey) {
 					if (!(element instanceof TextNode))
 						throw parser.getParseException(this.getName(), "key must be a string", element
 							.getType().toString());
 					currentKey.append((TextNode) element);
 					return false;
 				}
 				result.put(currentKey.toString(), element);
 				currentKey.delete(0, currentKey.length());
 				return true;
 			}
 
 			private boolean handleSpecialChar(final int character, final boolean keyNext, final JsonParser parser)
 					throws JsonParseException {
 				if (character == -1)
 					throw parser.getParseException(this.getName(),
 						String.valueOf(this.charToFinishObject),
 						"eof");
 				if ((char) character == this.elementSeparator)
 					return true;
 				if ((char) character == this.keyValueSeparator) {
 					if (keyNext)
 						throw parser.getParseException(this.getName(), "key expected",
 							String.valueOf(this.keyValueSeparator));
 					return true;
 				}
 				return false;
 			}
 		},
 		ARRAY {
 			private final char charToFinishArray = ']';
 
 			private final char arrayElementSeparator = ',';
 
 			@Override
 			public IJsonNode createJsonNode(final char startChar, final JsonParser parser) throws JsonParseException {
 
 				final List<IJsonNode> result = new ArrayList<IJsonNode>();
 				boolean valueExpected = false;
 				int nextChar;
 
 				while ((nextChar = parser.readIgnoreWhitespace()) != this.charToFinishArray) {
 					if (this.handleSpecialChar(nextChar, parser, valueExpected, result.size())) {
 						valueExpected = true;
 						continue;
 					}
 					this.handleElement(nextChar, result, parser);
 					valueExpected = false;
 				}
 				if (valueExpected)
 					throw parser.getParseException(this.getName(), "json value",
 						String.valueOf(this.charToFinishArray));
 				return ArrayNode.valueOf(result.iterator());
 			}
 
 			@Override
 			protected String getName() {
 				return "json array";
 			}
 
 			@Override
 			protected void initialize() {
 				this.transitions.put('{', STATE.OBJECT);
 				this.transitions.put('[', STATE.ARRAY);
 				this.transitions.put('t', STATE.BOOLEAN);
 				this.transitions.put('f', STATE.BOOLEAN);
 				this.transitions.put('n', STATE.NULL);
 				this.transitions.put('-', STATE.NUMBER);
 				this.transitions.put('0', STATE.NUMBER);
 				this.transitions.put('1', STATE.NUMBER);
 				this.transitions.put('2', STATE.NUMBER);
 				this.transitions.put('3', STATE.NUMBER);
 				this.transitions.put('4', STATE.NUMBER);
 				this.transitions.put('5', STATE.NUMBER);
 				this.transitions.put('6', STATE.NUMBER);
 				this.transitions.put('7', STATE.NUMBER);
 				this.transitions.put('8', STATE.NUMBER);
 				this.transitions.put('9', STATE.NUMBER);
 				this.transitions.put('"', STATE.STRING);
 			}
 
 			private void handleElement(final int nextChar, final List<IJsonNode> result, final JsonParser parser)
 					throws JsonParseException {
 				final STATE state = this.nextState((char) nextChar);
 				if (state == null)
 					throw parser.getParseException(this.getName(),
 						"one of ['{', '[', 't', 'f', 'n', '-' , 0-9, '\"']", String.valueOf((char) nextChar));
 				final IJsonNode element = state.createJsonNode((char) nextChar,
 					parser);
 				result.add(element);
 			}
 
 			private boolean handleSpecialChar(final int nextChar, final JsonParser parser, final boolean valueExpected,
 					final int currentResultSize) throws JsonParseException {
 				if (nextChar == -1)
 					throw parser.getParseException(this.getName(),
 						String.valueOf(this.charToFinishArray),
 						"eof");
 				if ((char) nextChar == this.arrayElementSeparator) {
 					if (currentResultSize == 0 || valueExpected)
 						throw parser.getParseException(this.getName(), "json value",
 							String.valueOf(this.arrayElementSeparator));
 					return true;
 				}
 				return false;
 			}
 		},
 		STRING {
 			private final char charToFinishString = '"';
 
 			@Override
 			public IJsonNode createJsonNode(final char startChar, final JsonParser parser) throws JsonParseException {
 				final StringBuffer buffer = new StringBuffer();
 
 				for (char nextChar = (char) parser.read(); nextChar != this.charToFinishString; nextChar =
 					(char) parser.read())
 					if (nextChar == '\\')
 						buffer.append(this.unescape(parser));
 					else
 						buffer.append(nextChar);
 				return TextNode.valueOf(buffer.toString());
 			}
 
 			@Override
 			protected String getName() {
 				return "string value";
 			}
 
 			private char unescape(final JsonParser parser) throws JsonParseException {
 				final char escapeChar = (char) parser.read();
 				switch (escapeChar) {
 				case '"':
 				case '\\':
 				case '/':
 					return escapeChar;
 
 				case 'b':
 					return '\b';
 				case 'f':
 					return '\f';
 				case 'n':
 					return '\n';
 				case 'r':
 					return '\r';
 				case 't':
 					return '\t';
 
 				case 'u':
 					final char[] hexCode =
 					{ (char) parser.read(), (char) parser.read(), (char) parser.read(), (char) parser.read(), };
					return (char) Integer.parseInt(String.valueOf(hexCode), 16);
 
 				default:
 					throw parser.getParseException(this.getName(),
 						"a valid escape sequence \\(\" | \\ | / | b | f | n | r | t | uXXXX)",
 						String.valueOf("\\" + escapeChar));
 				}
 			}
 		},
 		BOOLEAN {
 			private final char expectedCharTrue = 't';
 
 			private final char[] trueChars = new char[] { 'r', 'u', 'e' };
 
 			private final char expectedCharFalse = 'f';
 
 			private final char[] falseChars = new char[] { 'a', 'l', 's', 'e' };
 
 			@Override
 			public IJsonNode createJsonNode(final char startChar, final JsonParser parser) throws JsonParseException {
 				final char[] expectedContentWithoutFirstChar;
 				IJsonNode result;
 				if (startChar == this.expectedCharTrue) {
 					expectedContentWithoutFirstChar = this.trueChars;
 					result = BooleanNode.TRUE;
 				} else if (startChar == this.expectedCharFalse) {
 					expectedContentWithoutFirstChar = this.falseChars;
 					result = BooleanNode.FALSE;
 				} else
 					throw parser.getParseException(this.getName(), "one of [" + this.expectedCharTrue + ", "
 						+ this.expectedCharFalse + "]", String.valueOf(startChar));
 
 				STATE.compareWithReaderContent(expectedContentWithoutFirstChar, parser, this);
 				return result;
 
 			}
 
 			@Override
 			protected String getName() {
 				return "boolean value";
 			}
 		},
 		NUMBER {
 			private final CharList allowedCharactersInNumbers = new CharArrayList(Arrays.asList('-', '0', '1', '2',
 				'3', '4', '5', '6', '7', '8', '9', '.', 'e', 'E', '+'));
 
 			private final String numberRegex = "^[-]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$";
 
 			@Override
 			public IJsonNode createJsonNode(final char startChar, final JsonParser parser) throws JsonParseException {
 				final StringBuffer buffer = new StringBuffer().append(startChar);
 				char nextChar;
 				parser.markReader();
 
 				while (this.allowedCharactersInNumbers.contains(nextChar = (char) parser.read())) {
 					parser.markReader();
 					buffer.append(nextChar);
 				}
 				parser.resetReader();
 
 				final IJsonNode number = this.parseNumber(buffer.toString());
 				if (number == null)
 					throw parser.getParseException(this.getName(), "a numerical value", buffer.toString());
 				return number;
 			}
 
 			@Override
 			protected String getName() {
 				return "numerical value";
 			}
 
 			private IJsonNode parseNumber(final String value) {
 				if (value.matches(this.numberRegex)) {
 					final BigDecimal bigDec = new BigDecimal(value);
 					if (bigDec.scale() == 0) {
 						final BigInteger bigInt = bigDec.unscaledValue();
 						if (bigInt.bitLength() <= 31)
 							return IntNode.valueOf(bigInt.intValue());
 						if (bigInt.bitLength() <= 63)
 							return LongNode.valueOf(bigInt.longValue());
 						return BigIntegerNode.valueOf(bigInt);
 					}
 					return DecimalNode.valueOf(bigDec);
 				}
 				return null;
 			}
 		},
 		NULL {
 			private final char expectedStartChar = 'n';
 
 			private final char[] nullWithoutFirstChar = { 'u', 'l', 'l' };
 
 			@Override
 			public IJsonNode createJsonNode(final char startChar, final JsonParser parser) throws JsonParseException {
 				if (startChar != this.expectedStartChar)
 					throw parser.getParseException(this.getName(), String.valueOf(this.expectedStartChar),
 						String.valueOf(startChar));
 				STATE.compareWithReaderContent(this.nullWithoutFirstChar, parser, this);
 				return NullNode.getInstance();
 			}
 
 			@Override
 			protected String getName() {
 				return "null value";
 			}
 		},
 		ROOT {
 			@Override
 			public IJsonNode createJsonNode(final char startChar,
 					final JsonParser parser)
 					throws JsonParseException {
 				return MissingNode.getInstance();
 			}
 
 			@Override
 			protected String getName() {
 				return "root element";
 			}
 
 			@Override
 			protected void initialize() {
 				this.transitions.put('{', STATE.OBJECT);
 				this.transitions.put('[', STATE.ARRAY);
 				this.transitions.put('t', STATE.BOOLEAN);
 				this.transitions.put('f', STATE.BOOLEAN);
 				this.transitions.put('n', STATE.NULL);
 				this.transitions.put('-', STATE.NUMBER);
 				this.transitions.put('0', STATE.NUMBER);
 				this.transitions.put('1', STATE.NUMBER);
 				this.transitions.put('2', STATE.NUMBER);
 				this.transitions.put('3', STATE.NUMBER);
 				this.transitions.put('4', STATE.NUMBER);
 				this.transitions.put('5', STATE.NUMBER);
 				this.transitions.put('6', STATE.NUMBER);
 				this.transitions.put('7', STATE.NUMBER);
 				this.transitions.put('8', STATE.NUMBER);
 				this.transitions.put('9', STATE.NUMBER);
 				this.transitions.put('"', STATE.STRING);
 			}
 		};
 
 		protected Char2ObjectMap<STATE> transitions;
 
 		private STATE() {
 			this.transitions = new Char2ObjectOpenHashMap<STATE>();
 		}
 
 		/**
 		 * Creates an {@link IJsonNode} that corresponds with this state.
 		 * 
 		 * @param startChar
 		 *        the character that is responsible for entering this state.
 		 * @param parser
 		 *        the {@link JsonParser} that holds the input data
 		 * @return the created {@link IJsonNode}
 		 * @throws JsonParseException
 		 *         Should something went wrong during the parsing process, this exception will be thrown. To find out
 		 *         the reason of failure consult {@link JsonParseException#getMessage()}.
 		 */
 		public abstract IJsonNode createJsonNode(char startChar,
 				JsonParser parser) throws JsonParseException;
 
 		/**
 		 * Determines the transition that is associated with the given character and returns the resulting state or
 		 * 'null' if no such transition exists.
 		 * 
 		 * @param character
 		 *        the character that should be used to determine the transition
 		 * @return the resulting state
 		 */
 		public STATE nextState(final char character) {
 			if (this.transitions.containsKey(character))
 				return this.transitions.get(character);
 			return null;
 		}
 
 		protected abstract String getName();
 
 		protected void initialize() {
 		}
 
 		/**
 		 * Initializes the transitions between the different states.
 		 */
 		public static void initializeTransitions() {
 			for (final STATE state : STATE.values())
 				state.initialize();
 		}
 
 		private static boolean compareWithReaderContent(final char[] expected,
 				final JsonParser parser, final STATE currentState) throws JsonParseException {
 			for (final char c : expected) {
 				final char currentChar = (char) parser.read();
 				if (c != currentChar)
 					throw parser.getParseException(currentState.getName(), String.valueOf(c),
 						String.valueOf(currentChar));
 			}
 			return true;
 		}
 	}
 }
