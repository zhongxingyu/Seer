 package org.paxle.parser.impl;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.paxle.core.charset.ICharsetDetector;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.mimetype.IMimeTypeDetector;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.threading.AWorker;
 import org.paxle.parser.ISubParser;
 import org.paxle.parser.ParserContext;
 
 public class ParserWorker extends AWorker<ICommand> {
 
 	/**
 	 * A class to manage {@link ISubParser sub-parsers}
 	 */
 	private SubParserManager subParserManager = null;
 	
 	/**
 	 * A class to detect mimetypes
 	 */
 	IMimeTypeDetector mimeTypeDetector = null;
 	
 	/**
 	 * A class to detect charsets
 	 */
 	ICharsetDetector charsetDetector = null;
 	
 	/**
 	 * A logger class 
 	 */
 	private final Log logger = LogFactory.getLog(ParserWorker.class);
 	
 	public ParserWorker(SubParserManager subParserManager) {
 		this.subParserManager = subParserManager;
 	}
 	
 	/**
 	 * Init the parser context
 	 */
 	protected void initParserContext() {
 		// init the parser context object
 		ParserContext parserContext = new ParserContext(this.subParserManager, this.mimeTypeDetector, this.charsetDetector);
 		ParserContext.setCurrentContext(parserContext);		
 	}
 	
 	@Override
 	protected void execute(ICommand cmd) {
 		// init the parser context
 		this.initParserContext();
 		
 		// get a proper parser
 		String mimeType = cmd.getCrawlerDocument().getMimeType();
 		ISubParser parser = this.subParserManager.getSubParser(mimeType);
 		if (parser == null) {
 			// document not parsable
 			this.logger.error("Unable to parse " + cmd.getLocation() + ", no parser found for it's MIME type '" + mimeType + "'");
 			cmd.setResult(ICommand.Result.Failure, "No parser for MIME type '" + mimeType + "' found");
 			return;
 		}
 		
 		this.logger.info("Parsing of URL '" + cmd.getLocation() + "' (" + mimeType + ")");
 		final long time = System.currentTimeMillis();
 		try {
 			IParserDocument parserdoc = parser.parse(
 					cmd.getLocation(), 
 					cmd.getCrawlerDocument().getCharset(), 
 					cmd.getCrawlerDocument().getContent());
 			
 			parserdoc.setStatus(IParserDocument.Status.OK);			
 			cmd.setParserDocument(parserdoc);
 		} catch (Exception e) {
 			this.logger.error("Error parsing " + cmd.getLocation(), e);			
 			cmd.setResult(ICommand.Result.Failure, e.getMessage());
 			return;
 		}
 		
 		this.logger.info("Finished parsing of " + cmd.getLocation() + " in " + (System.currentTimeMillis() - time) + " ms");
 		cmd.setResult(ICommand.Result.Passed);
 	}
 	
 	@Override
 	protected void reset() {
 		// do some cleanup
 		ParserContext parserContext = ParserContext.getCurrentContext();
 		if (parserContext != null) parserContext.reset();
 		
 		// reset all from parent
 		super.reset();
 	}
 }
