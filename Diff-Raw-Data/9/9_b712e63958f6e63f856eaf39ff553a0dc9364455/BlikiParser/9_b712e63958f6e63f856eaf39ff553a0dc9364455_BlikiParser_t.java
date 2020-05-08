 package org.jamwiki.parser.bliki;
 
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.parser.jflex.JFlexParser;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 
 public class BlikiParser extends JFlexParser {
 	private static final WikiLogger logger = WikiLogger.getLogger(BlikiParser.class.getName());
 
 	public BlikiParser(ParserInput parserInput) {
 		super(parserInput);
 	}
 
 	/**
 	 * Perform a bare minimum of parsing as required prior to saving a topic to
 	 * the database. In general this method will simply parse signature tags are
 	 * return.
 	 * 
 	 * @param raw
 	 *          The raw Wiki syntax to be converted into HTML.
 	 * @return The parsed content.
 	 * @throws Exception
 	 *           Thrown if any error occurs during parsing.
 	 */
 	public String parseMinimal(String raw) throws Exception {
 		long start = System.currentTimeMillis();
 		String output = raw;
 		ParserOutput parserOutput = new ParserOutput();
 		JAMWikiModel wikiModel = new JAMWikiModel(parserInput, parserOutput, "");
 		output = wikiModel.parseTemplates(raw, true);
 		output = output == null ? "" : output;
 		String topicName = (!StringUtils.isBlank(this.parserInput.getTopicName())) ? this.parserInput.getTopicName() : null;
 		logger.info("Parse time (parseMinimal) for " + topicName + " (" + ((System.currentTimeMillis() - start) / 1000.000) + " s.)");
 		return output;
 	}
 
 	/**
 	 * Returns a HTML representation of the given wiki raw text for online
 	 * representation.
 	 * 
 	 * @param parserOutput
 	 *          A ParserOutput object containing parser metadata output.
 	 * @param raw
 	 *          The raw Wiki syntax to be converted into HTML.
 	 * @return The parsed content.
 	 * @throws Exception
 	 *           Thrown if any error occurs during parsing.
 	 */
 	public String parseHTML(ParserOutput parserOutput, String raw) throws Exception {
 		long start = System.currentTimeMillis();
 		String output = null;
		if (!StringUtils.isBlank(this.isRedirect(parserInput, raw, JFlexParser.MODE_LAYOUT))) {
 			// redirects are parsed differently
 			output = this.parseRedirect(parserOutput, raw);
 		} else {
 			String context = parserInput.getContext();
 			String baseURL = "";
 			if (context != null) {
 				baseURL += context;
 			}
 			// context never ends with a "/" per servlet specification
 			baseURL += "/";
 			baseURL += Utilities.encodeAndEscapeTopicName(parserInput.getVirtualWiki());
 			baseURL += "/";
 			JAMWikiModel wikiModel = new JAMWikiModel(parserInput, parserOutput, baseURL);
 			output = wikiModel.render(new JAMHTMLConverter(parserInput), raw);
 			output = output == null ? "" : output;
 		}
 		String topicName = (!StringUtils.isBlank(this.parserInput.getTopicName())) ? this.parserInput.getTopicName() : null;
 		logger.info("Parse time (parseHTML) for " + topicName + " (" + ((System.currentTimeMillis() - start) / 1000.000) + " s.)");
 		return output;
 	}
 
 }
