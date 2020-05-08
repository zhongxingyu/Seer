 package at.rovo.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import at.rovo.common.UrlReader;
 
 /**
  * <p>
  * A parser reads a HTML page or its text representation as {@link String} into
  * memory and splits the text into {@link Token}s - namely {@link Tag}s and
  * {@link Word}s.
  * </p>
  * <p>
  * The tokens are stored within a {@link List} without adding any ancestor
  * information. The respective fields of the {@link Tag} instances are
  * initialized with 0 therefore.
  * </p>
  * <p>
  * The parser allows omitting specified HTML tags through invocation of the
  * according clean method. Cleaning a HTML tag removes the tag as well as the
  * content between the opening and closing tag, which might include further HTML
  * tags.
  * </p>
  * <p>
  * Words can be combined to a single word token by invoking
  * {@link #combineWords(boolean)} with a true parameter.
  * </p>
  * <p>
  * By default IFrame-, Script-, NoScript-, Link-, Style-, Form-, Doctype-Tags
  * and Comments are removed.
  * </p>
  * 
  * @author Roman Vottner
  */
 public class Parser
 {
 	/** The logger of this class **/
 	protected static Logger logger = LogManager.getLogger(Parser.class);
 
 	/**
 	 * Specifies if words inside a tag should be combined into a single
 	 * word-segment
 	 **/
 	protected boolean combineWords = false;
 	/** Tags that collect everything between opening and closing tags **/
 	protected List<String> compactTags = new ArrayList<String>();
 
 	/**
 	 * If set to true will result in META tags to be removed from the token list
 	 **/
 	private boolean cleanMeta = false;
 	/**
 	 * If set to true will result in IFRAME tags to be removed from the token
 	 * list
 	 **/
 	private boolean cleanIFrame = true;
 	/**
 	 * If set to true will result in LINK tags to be removed from the token list
 	 **/
 	private boolean cleanLinks = true;
 	/**
 	 * If set to true will result in SCRIPT tags to be removed from the token
 	 * list
 	 **/
 	private boolean cleanScripts = true;
 	/**
 	 * If set to true will result in NOSCRIPT tags to be removed from the token
 	 * list
 	 **/
 	private boolean cleanNoScripts = true;
 	/**
 	 * If set to true will result in STYLE tags to be removed from the token
 	 * list
 	 **/
 	private boolean cleanStyles = true;
 	/**
 	 * If set to true will result in FORM tags to be removed from the token list
 	 **/
 	private boolean cleanFormElements = true;
 	/**
 	 * If set to true will result in IMG tags to be removed from the token list
 	 **/
 	private boolean cleanImages = false;
 	/**
 	 * If set to true will result in A tags to be removed from the token list
 	 **/
 	private boolean cleanAnchors = false;
 	/**
 	 * If set to true will result in Comments to be removed from the token list
 	 **/
 	private boolean cleanComments = true;
 	/**
 	 * If set to true will result in DOCTYPE tags to be removed from the token
 	 * list
 	 **/
 	private boolean cleanDoctypes = true;
 
 	// Fields required during tag processing
 	/** The last HTML tag found **/
 	private Tag tag = null;
 
 	/** The last word added to the tokenList **/
 	protected Word lastWord = null;
 	/**
 	 * The tag's short name which is currently compacted; null if no compaction
 	 * is carried out at the moment
 	 **/
 	private String compact = null;
 
 	/** The ID of the token **/
 	protected int id = 0;
 	/** The number of words found **/
 	protected int numWords = 0;
 	/** The number of tags found **/
 	protected int tagPos = 0;
 	/** Meta-data informations **/
 	protected ParsingMetaData metaData = new ParsingMetaData();
 	/**
 	 * Flag which indicates if a preceding tag was parsed completely or if it
 	 * still open
 	 **/
 	protected boolean tagFinished = true;
 
 	/**
 	 * <p>
 	 * Initializes a new instance and sets default values for certain fields
 	 * like the tags that are combined or tags that ignore parenting.
 	 * </p>
 	 */
 	public Parser()
 	{
 		if (this.cleanIFrame)
 			this.compactTags.add("iframe");
 		if (this.cleanScripts)
 			this.compactTags.add("script");
 		if (this.cleanNoScripts)
 			this.compactTags.add("noscript");
 		if (this.cleanFormElements)
 			this.compactTags.add("form");
 		if (this.cleanComments)
 			this.compactTags.add("<!--");
 		if (this.cleanStyles)
 			this.compactTags.add("style");
 		if (this.cleanAnchors)
 			this.compactTags.add("a");
 	}
 
 	/**
 	 * <p>
 	 * Defines if a META tag should be removed from the parsed token list.
 	 * </p>
 	 * 
 	 * @param clean
 	 *            True specifies to remove META tags; false will keep them in
 	 *            the result
 	 */
 	public void cleanMeta(boolean clean)
 	{
 		this.cleanMeta = clean;
 	}
 
 	/**
 	 * <p>
 	 * Defines if a IFRAME tag should be removed from the parsed token list.
 	 * </p>
 	 * 
 	 * @param clean
 	 *            True specifies to remove IFRAME tags; false will keep them in
 	 *            the result
 	 */
 	public void cleanIFrame(boolean clean)
 	{
 		this.cleanIFrame = clean;
 	}
 
 	/**
 	 * <p>
 	 * Defines if a LINK tag should be removed from the parsed token list.
 	 * </p>
 	 * 
 	 * @param clean
 	 *            True specifies to remove LINK tags; false will keep them in
 	 *            the result
 	 */
 	public void cleanLinks(boolean clean)
 	{
 		this.cleanLinks = clean;
 	}
 
 	/**
 	 * <p>
 	 * Defines if a SCRIPT tag should be removed from the parsed token list.
 	 * </p>
 	 * 
 	 * @param clean
 	 *            True specifies to remove SCRIPT tags; false will keep them in
 	 *            the result
 	 */
 	public void cleanScripts(boolean clean)
 	{
 		this.cleanScripts = clean;
 		if (clean && !this.compactTags.contains("script"))
 			this.compactTags.add("script");
 		else if (!clean)
 			this.compactTags.remove("script");
 	}
 
 	/**
 	 * <p>
 	 * Defines if a NOSCRIPT tag should be removed from the parsed token list.
 	 * </p>
 	 * 
 	 * @param clean
 	 *            True specifies to remove NOSCRIPT tags; false will keep them
 	 *            in the result
 	 */
 	public void cleanNoScripts(boolean clean)
 	{
 		this.cleanNoScripts = clean;
 		if (clean && !this.compactTags.contains("noscript"))
 			this.compactTags.add("noscript");
 		else if (!clean)
 			this.compactTags.remove("noscript");
 	}
 
 	/**
 	 * <p>
 	 * Defines if a STYLE tag should be removed from the parsed token list.
 	 * </p>
 	 * 
 	 * @param clean
 	 *            True specifies to remove STYLE tags; false will keep them in
 	 *            the result
 	 */
 	public void cleanStyles(boolean clean)
 	{
 		this.cleanStyles = clean;
 		if (clean && !this.compactTags.contains("style"))
 			this.compactTags.add("style");
 		else if (!clean)
 			this.compactTags.remove("style");
 	}
 
 	/**
 	 * <p>
 	 * Defines if a FORM tag should be removed from the parsed token list.
 	 * </p>
 	 * 
 	 * @param clean
 	 *            True specifies to remove FORM tags; false will keep them in
 	 *            the result
 	 */
 	public void cleanFormElements(boolean clean)
 	{
 		this.cleanFormElements = clean;
 		if (clean && !this.compactTags.contains("form"))
 			this.compactTags.add("form");
 		else if (!clean)
 			this.compactTags.remove("form");
 	}
 
 	/**
 	 * <p>
 	 * Defines if a IMG tag should be removed from the parsed token list.
 	 * </p>
 	 * 
 	 * @param clean
 	 *            True specifies to remove IMG tags; false will keep them in the
 	 *            result
 	 */
 	public void cleanImages(boolean clean)
 	{
 		this.cleanImages = clean;
 	}
 
 	/**
 	 * <p>
 	 * Defines if a A tag should be removed from the parsed token list.
 	 * </p>
 	 * 
 	 * @param clean
 	 *            True specifies to remove A tags; false will keep them in the
 	 *            result
 	 */
 	public void cleanAnchors(boolean clean)
 	{
 		this.cleanAnchors = clean;
 		if (clean && !this.compactTags.contains("a"))
 			this.compactTags.add("a");
 		else if (!clean)
 			this.compactTags.remove("a");
 	}
 
 	/**
 	 * <p>
 	 * Defines if a comments should be removed from the parsed token list.
 	 * </p>
 	 * 
 	 * @param clean
 	 *            True specifies to remove comments; false will keep them in the
 	 *            result
 	 */
 	public void cleanComments(boolean clean)
 	{
 		this.cleanComments = clean;
 		if (clean && !this.compactTags.contains("<!--"))
 			this.compactTags.add("<!--");
 		else if (!clean)
 			this.compactTags.remove("<!--");
 	}
 
 	/**
 	 * <p>
 	 * Defines if DOCTYPE tags should be removed from the parsed token list.
 	 * </p>
 	 * 
 	 * @param clean
 	 *            True specifies to remove DOCTYPE tags; false will keep them in
 	 *            the result
 	 */
 	public void cleanDoctypes(boolean clean)
 	{
 		this.cleanDoctypes = clean;
 	}
 
 	/**
 	 * <p>
 	 * Returns if META tags are removed from the token list.
 	 * </p>
 	 * 
 	 * @return True if META tags are removed from the token list; false
 	 *         otherwise
 	 */
 	public boolean cleanMeta()
 	{
 		return this.cleanMeta;
 	}
 
 	/**
 	 * <p>
 	 * Returns if IFRAME tags are removed from the token list.
 	 * </p>
 	 * 
 	 * @return True if IFRAME tags are removed from the token list; false
 	 *         otherwise
 	 */
 	public boolean cleanIFrame()
 	{
 		return this.cleanIFrame;
 	}
 
 	/**
 	 * <p>
 	 * Returns if LINK tags are removed from the token list.
 	 * </p>
 	 * 
 	 * @return True if LINK tags are removed from the token list; false
 	 *         otherwise
 	 */
 	public boolean cleanLinks()
 	{
 		return this.cleanLinks;
 	}
 
 	/**
 	 * <p>
 	 * Returns if SCRIPT tags are removed from the token list.
 	 * </p>
 	 * 
 	 * @return True if SCRIPT tags are removed from the token list; false
 	 *         otherwise
 	 */
 	public boolean cleanScripts()
 	{
 		return this.cleanScripts;
 	}
 
 	/**
 	 * <p>
 	 * Returns if NOSCRIPT tags are removed from the token list.
 	 * </p>
 	 * 
 	 * @return True if NOSCRIPT tags are removed from the token list; false
 	 *         otherwise
 	 */
 	public boolean cleanNoScripts()
 	{
 		return this.cleanScripts;
 	}
 
 	/**
 	 * <p>
 	 * Returns if STYLE tags are removed from the token list.
 	 * </p>
 	 * 
 	 * @return True if STYLE tags are removed from the token list; false
 	 *         otherwise
 	 */
 	public boolean cleanStyles()
 	{
 		return this.cleanStyles;
 	}
 
 	/**
 	 * <p>
 	 * Returns if FORM tags are removed from the token list.
 	 * </p>
 	 * 
 	 * @return True if FORM tags are removed from the token list; false
 	 *         otherwise
 	 */
 	public boolean cleanFormElements()
 	{
 		return this.cleanFormElements;
 	}
 
 	/**
 	 * <p>
 	 * Returns if IMG tags are removed from the token list.
 	 * </p>
 	 * 
 	 * @return True if IMG tags are removed from the token list; false otherwise
 	 */
 	public boolean cleanImages()
 	{
 		return this.cleanImages;
 	}
 
 	/**
 	 * <p>
 	 * Returns if A tags are removed from the token list.
 	 * </p>
 	 * 
 	 * @return True if A tags are removed from the token list; false otherwise
 	 */
 	public boolean cleanAnchors()
 	{
 		return this.cleanAnchors;
 	}
 
 	/**
 	 * <p>
 	 * Returns if comments are removed from the token list.
 	 * </p>
 	 * 
 	 * @return True if comments are removed from the token list; false otherwise
 	 */
 	public boolean cleanComments()
 	{
 		return this.cleanComments;
 	}
 
 	/**
 	 * <p>
 	 * Returns if DOCTYPE tags are removed from the token list.
 	 * </p>
 	 * 
 	 * @return True if DOCTYPE tags are removed from the token list; false
 	 *         otherwise
 	 */
 	public boolean cleanDoctypes()
 	{
 		return this.cleanDoctypes;
 	}
 
 	/**
 	 * <p>
 	 * Specifies if words between tags should be combined to one single object
 	 * (true) or if they should be kept separated in the resulting token list
 	 * (false).
 	 * </p>
 	 * <p>
 	 * By default words will not get combined.
 	 * </p>
 	 * 
 	 * @param combineWords
 	 *            Set to true will combine words between tags to a single word
 	 *            object, false will generate a word object for each word
 	 */
 	public void combineWords(boolean combineWords)
 	{
 		this.combineWords = combineWords;
 	}
 
 	/**
 	 * <p>
 	 * Returns if words between tags should be combined to a single word or if
 	 * they are kept separated.
 	 * </p>
 	 * 
 	 * @return True if words between tags are being combined to a single word;
 	 *         false otherwise
 	 */
 	public boolean isWordCombined()
 	{
 		return this.combineWords;
 	}
 
 	/**
 	 * <p>
 	 * Builds a {@link List} of {@link Token}s representing the page referenced
 	 * by the URL provided.
 	 * </p>
 	 * <p>
 	 * This method fetches the content of the URL provided and hands it over to
 	 * {@link #tokenize(String)} method.
 	 * </p>
 	 * 
 	 * @param html
 	 *            A {@link String} representing the URL of the page to split up
 	 *            into tokens
 	 * @param formatText
 	 *            Indicates if the output tokens should be formated
 	 * @return A {@link List} of {@link Token}s representing the HTML page
 	 */
 	public ParseResult tokenizeURL(String url, boolean formatText)
 	{
 		if (url != null && !url.equals(""))
 		{
 			logger.debug("Reading page from URL: {}", url);
 
 			UrlReader reader = new UrlReader();
 			String html = reader.readPage(url);
 
 			return this.tokenize(html, formatText);
 		}
 		else
 			throw new IllegalArgumentException("Invalid URL passed. Got: "+ url);
 	}
 
 	/**
 	 * <p>
 	 * Builds a {@link List} of {@link Token}s representing the provided string.
 	 * </p>
 	 * 
 	 * @param html
 	 *            A {@link String} representing the full HTML code of a web site
 	 * @param formatText
 	 *            Indicates if the tokens should be formated or not
 	 * @return A {@link List} of {@link Token}s representing the HTML page
 	 */
 	public ParseResult tokenize(String html, boolean formatText)
 	{
 		ParseResult result = new ParseResult();
 		if (html == null || html.equals(""))
 			throw new IllegalArgumentException("Invalid html string passed.");
 
 		// split the html into a token-array
 		logger.debug("Splitting page");
 
 		// parse and process the tokens from the HTML file
 		List<Token> tokenList = this.parseToTokens(html, " ", ">", "<",
 				formatText);
 
 		// generate the result
 		result.setTitle(metaData.getTitle());
 		result.setParsedTokens(tokenList);
 		result.setAuthorName(metaData.getAuthorNames());
 		result.setAuthors(metaData.getAuthor());
 		result.setPublishDate(metaData.getDate());
 		result.setByline(metaData.getByline());
 		result.setNumWords(numWords);
 		result.setNumTokens(tokenList.size());
 		result.setNumTags(id);
 
 		return result;
 	}
 
 	/**
 	 * <p>
 	 * Parses the HTML content into tokens of words and tags and calls
 	 * {@link #processTokens(String, List, Stack, boolean)} to further process
 	 * these tokens.
 	 * </p>
 	 * 
 	 * @param text
 	 *            The HTML text to parse
 	 * @param replace
 	 *            Splitting characters that should not get included in the
 	 *            resulting tokens
 	 * @param nonReplace
 	 *            Splitting characters that should get appended to the leading
 	 *            token
 	 * @param splitAndInclude
 	 *            Splitting character that is the fist character of the new
 	 *            token
 	 * @param formatText
 	 *            Indicates if the output tokens should be formated
 	 * @return A {@link List} of {@link Token}s representing the HTML page
 	 */
 	protected List<Token> parseToTokens(String text, String replace,
 			String nonReplace, String splitAndInclude, boolean formatText)
 	{
 		List<Token> tokenList = new ArrayList<Token>();
 		Stack<Tag> stack = new Stack<Tag>();
 		stack.add(new Tag(0, "", 0, 0, 0));
 
 		char[] replaceChars = replace.toCharArray();
 		char[] nonReplaceChars = nonReplace.toCharArray();
 		char[] splitAndIncludeChars = splitAndInclude.toCharArray();
 
 		StringBuilder sb = new StringBuilder();
 		boolean found = false;
 		boolean openQuotes = false;
 		for (int i = 0; i < text.length(); i++)
 		{
			if (this.tag == null || !this.tag.isValid() && this.tag.getHTML().startsWith("<") && !openQuotes || this.tag.isValid())
 			{
 				for (char c : replaceChars)
 				{
 					// we found a separating character - split the token at this
 					// position - This token should not get included in the
 					// final
 					// token so jump continue with the next iteration
 					if (text.charAt(i) == c)
 					{
 						if (!sb.toString().trim().equals(""))
 							this.processTokens(sb.toString(), tokenList, stack,	formatText);
 						sb = new StringBuilder();
 						found = true;
 						break;
 					}
 				}
 
 				for (char c : splitAndIncludeChars)
 				{
 					// we found a separating character - split the token at this
 					// position and include the splitting character to the newly
 					// created token
 					if (text.charAt(i) == c)
 					{
 						if (!sb.toString().trim().equals(""))
 							this.processTokens(sb.toString(), tokenList, stack,	formatText);
 						sb = new StringBuilder();
 					}
 				}
 
 				// we have already found a replaceable char - this should not be
 				// added to the token
 				if (found)
 				{
 					found = false;
 					continue;
 				}
 			}
 
 			// catch quoted sections
 			if (text.charAt(i) == '"')
 				openQuotes = !openQuotes;
 
 			// catch constructs like <div id="companionAd""> but allow sections
 			// like <img src="..." alt="" />
 			if (i > 3 && text.charAt(i - 2) != '=' && text.charAt(i - 1) == '"'	&& text.charAt(i) == '"')
 				// ignore the char
 				openQuotes = false;
 			else
 				sb.append(text.charAt(i));
 
			if (this.tag == null || !this.tag.isValid() && this.tag.getHTML().startsWith("<") && !openQuotes || this.tag.isValid())
 			{
 				for (char c : nonReplaceChars)
 				{
 					// we found a separating character - split the token and add
 					// the
 					// separating character to the old token as its last
 					// character
 					if (text.charAt(i) == c)
 					{
 						if (!sb.toString().trim().equals(""))
 							this.processTokens(sb.toString(), tokenList, stack,	formatText);
 						sb = new StringBuilder();
 						found = true;
 						break;
 					}
 				}
 
 				if (found)
 					found = false;
 			}
 		}
 
 		return tokenList;
 	}
 
 	/**
 	 * <p>
 	 * Processes parsed tokens. Processing involves filtering unneeded tags and
 	 * building a DOM tree.
 	 * </p>
 	 * 
 	 * @param token
 	 *            The current token that needs to be processed
 	 * @param tokenList
 	 *            The list of parsed HTML tags and words contained in the HTML
 	 *            page that is currently parsed
 	 * @param stack
 	 *            This method will ignore the stack
 	 * @param formatText
 	 *            Indicates if the output tokens should be formated
 	 */
 	protected void processTokens(String token, List<Token> tokenList,
 			Stack<Tag> stack, boolean formatText)
 	{
 		// remove whitespace characters
 		token = token.trim();
 
 		// valid tokens start with a < character
 		if (token.startsWith("<") &&
 				// a valid tag needs at least 2 characters
 				token.length() > 1 &&
 				// create a new tag if either there is no tag from the previous
 				// iteration or it was no script tag
 				(this.tag == null || !this.tag.getHTML().startsWith("<script") ||
 				// create a script tag only if it is a complete tag
 				// we might find a token that equals '<playlistArr.length;i++){'
 				// don't treat is as a tag
 				this.tag.getHTML().startsWith("<script")
 						&& token.endsWith("</script>"))
 				// only add a new tag if the previous tag is complete and there
 				// is no compact in progress
 				&& this.tagFinished && this.compact == null)
 		{
 			// we found a new tag either after a word or a preceding tag
 			this.tagFinished = false;
 			this.tag = new Tag(token);
 
 			// check if we should compact tokens between certain tokens
 			// together
 			for (String compactTag : this.compactTags)
 			{
 				if (this.tag.getShortTag().equals(compactTag))
 				{
 					if (compactTag.equals("<!--") && !token.endsWith("-->"))
 						this.compact = "-->";
 					// check if the found tag was provided as a one-line-tag
 					// f.e. <!--empty-->
 					else if (compactTag.equals("<!--") && token.endsWith("-->"))
 						this.compact = null;
 					else
 						this.compact = compactTag;
 
 					break;
 				}
 			}
 			this.lastWord = null;
 
 			this.checkTagValidity(this.tag, tokenList, stack);
 		}
 		else if (this.tag != null && !this.tagFinished && this.compact == null)
 		{
 			// the preceding tag was not yet finished so append the token to
 			// tag
 			this.tag.append(token);
 
 			if (token.endsWith(">"))
 				this.tag.setName(this.getTagName(this.tag.getHTML()));
 
 			this.checkTagValidity(this.tag, tokenList, stack);
 		}
 		// compact specified token sequences like scripts or iframes
 		else if (this.compact != null && this.tag != null)
 		{
 			// as appending content to an already valid tag is not possible,
 			// we have to set the content manually
 			this.tag.setHTML(this.tag.getHTML() + " " + token);
 			// check if the end of the tag sequence was reached
 			if (this.tag.getHTML().endsWith(this.compact + ">")
 					|| this.tag.getHTML().endsWith(this.compact))
 			{
 				this.checkTagValidity(this.tag, tokenList, stack);
 				this.compact = null;
 				this.tagFinished = true;
 				this.tag = null;
 			}
 		}
 		else
 		{
 			// preceding tags are all closed and the token doesn't start with
 			// an opening tag symbol - so we have a word here
 			int numWords = this.addWord(token, this.id, stack, tokenList,
 					formatText);
 			this.metaData.checkToken(this.lastWord, this.combineWords);
 			// keep track of the id's
 			this.id += numWords;
 			this.numWords += numWords;
 
 			if (!this.combineWords)
 				this.lastWord = null;
 		}
 	}
 
 	/**
 	 * <p>
 	 * Returns a simple representation of the HTML tag which omits everything
 	 * but the opening and closing-type and the name of the HTML tag.
 	 * </p>
 	 * 
 	 * @param token
 	 *            The HTML text of the tag
 	 * @return A simple representation of the HTML tag omitting all informations
 	 *         like defined classes or id's
 	 */
 	protected String getTagName(String token)
 	{
 		String rawName = token.replaceAll("[<|/|>]", "");
 		if (rawName.contains(" "))
 			rawName = rawName.substring(0, rawName.indexOf(" "));
 		String tagName = "<" + (token.charAt(1) == '/' ? "/" : "") + rawName
 				+ (token.charAt(token.length() - 2) == '/' ? " /" : "") + ">";
 		return tagName;
 	}
 
 	/**
 	 * <p>
 	 * Creates a new HTML tag.
 	 * </p>
 	 * 
 	 * @param token
 	 *            The HTML text of the tag to create
 	 * @param tokenList
 	 *            The list of already parsed HTML tokens
 	 * @param stack
 	 *            This method will ignore the stack
 	 * @return The newly created HTML tag
 	 * @throws InvalidAncestorException
 	 *             Thrown by overriding methods of child classes to indicate
 	 *             that a closing tag has no corresponding opening tag on the
 	 *             stack
 	 */
 	protected Tag createNewTag(String token, List<Token> tokenList,
 			Stack<Tag> stack) throws InvalidAncestorException
 	{
 		// identify the tag name
 		String tagName = this.getTagName(token);
 
 		// create the new tag
 		Tag tag = new Tag(this.id++, tagName, 0, 0, 0);
 		tag.setHTML(token);
 
 		return tag;
 	}
 
 	/**
 	 * <p>
 	 * Checks if a tag is allowed to be added to the list of parsed tokens.
 	 * </p>
 	 * 
 	 * @param tag
 	 *            The tag to check for its validity to be added to the list of
 	 *            parsed tokens
 	 * @param tokenList
 	 *            The list of already parsed HTML tokens
 	 * @param stack
 	 *            This method will ignore the stack
 	 */
 	protected void checkTagValidity(Tag tag, List<Token> tokenList,
 			Stack<Tag> stack)
 	{
 		// check if the tag is complete
 		if (tag.isValid())
 		{
 			// remove the flag
 			this.tagFinished = true;
 
 			// check if we are allowed to add the tag
 			if (!this.needsRemoval(tag))
 			{
 				// create a new tag object with ancestor and sibling
 				// informations
 				try
 				{
					Tag newTag = this.createNewTag(tag.getHTML(), tokenList, stack);
 					tokenList.add(newTag);
 
 					newTag.setIndex(this.tagPos++);
 
 					logger.debug("\tadded Tag: {}", tag);
 
 					this.metaData.checkTag(newTag);
 				}
 				catch (InvalidAncestorException iaEx)
 				{
 					logger.catching(iaEx);
 				}
 			}
 		}
 	}
 
 	/**
 	 * <p>
 	 * Adds a word to the list of parsed tokens and provides the possibility to
 	 * format a word if <em>formatText</em> is set to true.
 	 * </p>
 	 * 
 	 * @param word
 	 *            The word to add to the list of parsed tokens
 	 * @param id
 	 *            The id of the word to add
 	 * @param stack
 	 *            This method will ignore the stack
 	 * @param tokenList
 	 *            The list of parsed tokens
 	 * @param formatText
 	 *            Indicates if the output tokens should be formated
 	 * @return The number of words added to the list of parsed tokens
 	 */
 	protected int addWord(String word, int id, Stack<Tag> stack,
 			List<Token> tokenList, boolean formatText)
 	{
 		int numWords = 0;
 
 		if (formatText)
 			word = ParserUtil.formatText(word);
 
 //		// split words in case they contain a / or a - but are no URL
 //		if ((word.contains("/") && !word.startsWith("http://")) || word.contains("-"))
 //		{
 //			for (String w : word.split("[/|-]"))
 //			{
 //				numWords += this.addWord(w, id++, stack, tokenList);
 //			}
 //		}
 //		else
 		{
 			// only a single word to add
 			numWords += this.addWord(word, id, stack, tokenList);
 		}
 
 		return numWords;
 	}
 
 	/**
 	 * <p>
 	 * Adds either a new word to the list of parsed tokens or appends the word
 	 * to the last added word if {@link #combineWords} is set to true.
 	 * </p>
 	 * 
 	 * @param word
 	 *            The word to add
 	 * @param id
 	 *            The id of the word to add
 	 * @param stack
 	 *            This method will ignore the stack
 	 * @param tokenList
 	 *            The list of parsed tokens
 	 * @return The number of words added; This is 0 if the word got appended to
 	 *         a preceding word
 	 */
 	protected int addWord(String word, int id, Stack<Tag> stack,
 			List<Token> tokenList)
 	{
 		int ret = 0;
 		// check if this word is the first word after a HTML tag and if we
 		// should
 		// combine words to preceding words
 		if (!this.combineWords || (this.combineWords && 
 				(this.lastWord == null || this.lastWord.getText() == null)))
 		{
 			if (this.lastWord == null)
 			{
 				this.lastWord = new Word(id, word, 0, 0, 0);
 				this.lastWord.setText(word);
 			}
 			else
 			{
 				this.lastWord.setNo(id);
 				this.lastWord.setName(word);
 				this.lastWord.setText(word);
 				this.lastWord.setLevel(0);
 				this.lastWord.setParentNo(0);
 				this.lastWord.setSibNo(0);
 			}
 
 			// add child to the parent
 			tokenList.add(this.lastWord);
 
 			ret = 1;
 		}
 		else
 		{
 			// we should combine words and the preceding token was a word too
 			// so append the content of this word to the preceding word
 			this.lastWord.setText(this.lastWord.getText() + " " + word);
 		}
 
 		// update the name of the word
 		this.lastWord.setName(this.lastWord.getText());
 
 		return ret;
 	}
 
 	/**
 	 * <p>
 	 * Checks if a tag needs to be removed.
 	 * </p>
 	 * 
 	 * @param tag
 	 *            The tag which should be checked for removal
 	 * @return True if the tag needs to be removed; false otherwise
 	 */
 	protected boolean needsRemoval(Tag tag)
 	{
 		// TODO: better extensibility mechanism wanted
 		if (this.cleanComments && tag.isComment() || this.cleanDoctypes
 				&& tag.getShortTag().toLowerCase().equals("!doctype")
 				|| this.cleanMeta
 				&& tag.getShortTag().toLowerCase().equals("meta")
 				|| this.cleanIFrame
 				&& tag.getShortTag().toLowerCase().equals("iframe")
 				|| this.cleanScripts
 				&& tag.getShortTag().toLowerCase().equals("script")
 				|| this.cleanNoScripts
 				&& tag.getShortTag().toLowerCase().equals("noscript")
 				|| this.cleanLinks
 				&& tag.getShortTag().toLowerCase().equals("link")
 				|| this.cleanStyles
 				&& tag.getShortTag().toLowerCase().equals("style")
 				|| this.cleanFormElements && tag.getShortTag().equals("form")
 				|| this.cleanImages
 				&& tag.getShortTag().toLowerCase().equals("img")
 				|| this.cleanAnchors
 				&& tag.getShortTag().toLowerCase().equals("a"))
 			return true;
 		return false;
 	}
 }
