 package net.sf.okapi.filters.doxygen;
 
 import java.util.IdentityHashMap;
 import java.util.regex.Pattern;
 
 public class DoxygenPatterns {
 	
 	public static final IdentityHashMap<Pattern, Pattern> tokenizerDelimiters;
 	public static final IdentityHashMap<Pattern, Pattern> chunkDelimiters;
 	
 	/**
 	 * Matches blank lines that separate paragraphs.
 	 */
 	public static final String BLANK_LINES = "(?:^|(?<=\\S))(?:\\s*(?:\\r?\\n)\\s*){2,}(?=\\S)";
 	public static final Pattern BLANK_LINES_PATTERN = Pattern.compile(BLANK_LINES);
 	
 	/**
 	 * Single-quoted string: 'string'
 	 * These could contain "false-positive" comment delimiters, and so must
 	 * be tokenized separately.
 	 * Hint taken from: http://stackoverflow.com/a/5455705/448068
 	 */
 	public static final String SINGLE_QUOTE_STRING = "(?<!\\\\)(?:\\\\{2})*(?<!')'(?!')";
 	public static final Pattern SINGLE_QUOTE_STRING_PREFIX_PATTERN = Pattern.compile(SINGLE_QUOTE_STRING);
 	public static final Pattern SINGLE_QUOTE_STRING_SUFFIX_PATTERN = Pattern.compile(SINGLE_QUOTE_STRING);
 
 	/**
 	 * Double-quoted string: "string"
 	 */
 	public static final String DOUBLE_QUOTE_STRING = "(?<!\\\\)(?:\\\\{2})*(?<!\")\"(?!\")";
 	public static final Pattern DOUBLE_QUOTE_STRING_PREFIX_PATTERN = Pattern.compile(DOUBLE_QUOTE_STRING);
 	public static final Pattern DOUBLE_QUOTE_STRING_SUFFIX_PATTERN = Pattern.compile(DOUBLE_QUOTE_STRING);
 
 	/**
 	 * Single-line comment start: /// or //!
 	 */
 	public static final String CPP_COMMENT_PREFIX = "(?<!\\/)\\/\\/[\\/!](?!\\/)";
 	public static final Pattern CPP_COMMENT_PREFIX_PATTERN = Pattern.compile(CPP_COMMENT_PREFIX);
 		
 	/**
 	 * Single-line comment end: linebreak
 	 */
 	public static final String CPP_COMMENT_SUFFIX = "\\r?\\n";
 	public static final Pattern CPP_COMMENT_SUFFIX_PATTERN = Pattern.compile(CPP_COMMENT_SUFFIX);
 	
 	/**
 	 * Non-Doxygen single-line comment start: //
 	 */
 	public static final String CPP_EXCLUDE_COMMENT_PREFIX = "(?<!\\/)\\/\\/(?![\\/!])";
 	public static final Pattern CPP_EXCLUDE_COMMENT_PREFIX_PATTERN = Pattern.compile(CPP_EXCLUDE_COMMENT_PREFIX);
 
 	/**
 	 * Non-Doxygen single-line comment end: linebreak
 	 */
 	public static final Pattern CPP_EXCLUDE_COMMENT_SUFFIX_PATTERN = Pattern.compile(CPP_COMMENT_SUFFIX);
 	
 	/**
 	 * Multiline comment start: /** or /*!
 	 */
 	public static final String JAVADOC_COMMENT_PREFIX = "\\/\\*[\\*!](?![\\*/])[^\\S\\n]*\\n?";
 	public static final Pattern JAVADOC_COMMENT_PREFIX_PATTERN = Pattern.compile(JAVADOC_COMMENT_PREFIX);
 	
 	/**
 	 * Multiline comment end: *\/
 	 */
 	public static final String JAVADOC_COMMENT_SUFFIX = "[^\\S\\n\\r]*\\*\\/";
 	public static final Pattern JAVADOC_COMMENT_SUFFIX_PATTERN = Pattern.compile(JAVADOC_COMMENT_SUFFIX);
 	
 	/**
 	 * Non-Doxygen multiline comment start: /*
 	 */
 	public static final String JAVADOC_EXCLUDE_COMMENT_PREFIX = "\\/\\*(?![\\*!])[^\\S\\n]*\\n?";
 	public static final Pattern JAVADOC_EXCLUDE_COMMENT_PREFIX_PATTERN = Pattern.compile(JAVADOC_EXCLUDE_COMMENT_PREFIX);
 
 	/**
 	 * Non-Doxygen multiline comment end: *\/
 	 */
 	public static final Pattern JAVADOC_EXCLUDE_COMMENT_SUFFIX_PATTERN = Pattern.compile(JAVADOC_COMMENT_SUFFIX);
 	
 	/**
 	 * Python multiline comment: '''
 	 * (same delimiter for both prefix and suffix)
 	 */
 	public static final String PYTHON_SINGLE_COMMENT_DELIMITER = "(?<!')'''(?!')";
 	public static final Pattern PYTHON_SINGLE_COMMENT_PREFIX_PATTERN = Pattern.compile(PYTHON_SINGLE_COMMENT_DELIMITER);
 	public static final Pattern PYTHON_SINGLE_COMMENT_SUFFIX_PATTERN = Pattern.compile(PYTHON_SINGLE_COMMENT_DELIMITER);
 	
 	
 	/**
 	 * Python multiline comment: """
 	 * (same delimiter for both prefix and suffix)
 	 */
 	public static final String PYTHON_DOUBLE_COMMENT_DELIMITER = "(?<!\")\"\"\"(?!\")";
 	public static final Pattern PYTHON_DOUBLE_COMMENT_PREFIX_PATTERN = Pattern.compile(PYTHON_DOUBLE_COMMENT_DELIMITER);
 	public static final Pattern PYTHON_DOUBLE_COMMENT_SUFFIX_PATTERN = Pattern.compile(PYTHON_DOUBLE_COMMENT_DELIMITER);
 
 			
 	/**
 	 * Matches the decoration preceding additional lines of multiline comments.
 	 * Ex:
 	 * <pre>
 	 *     /**
 	 *  [    * ]Foo
 	 *  [    * ]bar
 	 *       *\/
 	 * </pre>
 	 */
 	public static final String MULTILINE_DECORATION = "^[^\\S\\r\\n]*\\*?[^\\S\\r\\n]*?(?=$|\\S)";
 	public static final Pattern MULTILINE_DECORATION_PATTERN = Pattern.compile(MULTILINE_DECORATION, Pattern.MULTILINE);
 	    
 	/**
 	 * Matches Doxygen special commands: \cmd, \@cmd, \cmd{arg}, <cmd\>, <cmd arg="val">, etc.
 	 * @see http://www.stack.nl/~dimitri/doxygen/commands.html
 	 */
 	public static final String DOXYGEN_COMMAND = "[\\\\@]\\w+(?:[\\[\\(\\{].*?[\\]\\)\\}])?|</?[a-z]+(?: [^>]*)?>|@[{}]";
 	public static final Pattern DOXYGEN_COMMAND_PATTERN = Pattern.compile(DOXYGEN_COMMAND);
 	
 	/**
 	 * Matches member comments.
 	 * Ex:
 	 * <pre>
 	 * 		bool mybool; ///< Paragraph 4
 	 * </pre>
 	 */
 	public static final String MEMBER_COMMENT_PREFIX = "^<[^\\S\\r\\n]+";
 	public static final Pattern MEMBER_COMMENT_PREFIX_PATTERN = Pattern.compile(MEMBER_COMMENT_PREFIX, Pattern.MULTILINE);
 	public static final Pattern MEMBER_COMMENT_SUFFIX_PATTERN = Pattern.compile(CPP_COMMENT_SUFFIX);
 	
 	/**
 	 * Matches list delimiters.
 	 * Ex:
 	 * <pre>
 	 * 		/// Paragraph 1
 	 * 		/// - Paragraph 2
 	 * 		///  -# Paragraph 3
 	 * 		///  2. Paragraph 4
 	 * </pre>
 	 */
	public static final String LIST_ITEM_PREFIX = "^(?:\\s*(?:[-+*]#?|\\d+\\.)\\s+|\\s*\\.\\s*$)";
 	public static final Pattern LIST_ITEM_PREFIX_PATTERN = Pattern.compile(LIST_ITEM_PREFIX, Pattern.MULTILINE);
	public static final Pattern LIST_ITEM_SUFFIX_PATTERN = Pattern.compile(LIST_ITEM_PREFIX, Pattern.MULTILINE);
 	
 	static
 	{
 		// Build map of prefix-suffix patterns
 		tokenizerDelimiters = new IdentityHashMap<Pattern, Pattern>();
 		tokenizerDelimiters.put(JAVADOC_COMMENT_PREFIX_PATTERN, JAVADOC_COMMENT_SUFFIX_PATTERN);
 		tokenizerDelimiters.put(CPP_COMMENT_PREFIX_PATTERN, CPP_COMMENT_SUFFIX_PATTERN);
 		tokenizerDelimiters.put(PYTHON_SINGLE_COMMENT_PREFIX_PATTERN, PYTHON_SINGLE_COMMENT_SUFFIX_PATTERN);
 		tokenizerDelimiters.put(PYTHON_DOUBLE_COMMENT_PREFIX_PATTERN, PYTHON_DOUBLE_COMMENT_SUFFIX_PATTERN);
 		tokenizerDelimiters.put(SINGLE_QUOTE_STRING_PREFIX_PATTERN, SINGLE_QUOTE_STRING_SUFFIX_PATTERN);
 		tokenizerDelimiters.put(DOUBLE_QUOTE_STRING_PREFIX_PATTERN, DOUBLE_QUOTE_STRING_SUFFIX_PATTERN);
 		tokenizerDelimiters.put(CPP_EXCLUDE_COMMENT_PREFIX_PATTERN, CPP_EXCLUDE_COMMENT_SUFFIX_PATTERN);
 		tokenizerDelimiters.put(JAVADOC_EXCLUDE_COMMENT_PREFIX_PATTERN, JAVADOC_EXCLUDE_COMMENT_SUFFIX_PATTERN);
 	
 		chunkDelimiters = new IdentityHashMap<Pattern, Pattern>();
 		chunkDelimiters.put(MEMBER_COMMENT_PREFIX_PATTERN, MEMBER_COMMENT_SUFFIX_PATTERN);
 		chunkDelimiters.put(LIST_ITEM_PREFIX_PATTERN,LIST_ITEM_SUFFIX_PATTERN);
 	}
 }
