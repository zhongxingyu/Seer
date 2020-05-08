 package org.paxle.parser.impl;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.paxle.core.charset.ICharsetDetector;
 import org.paxle.core.doc.ICrawlerDocument;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.ParserDocument;
 import org.paxle.core.io.temp.ITempFileManager;
 import org.paxle.core.mimetype.IMimeTypeDetector;
 import org.paxle.core.norm.IReferenceNormalizer;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.threading.AWorker;
 import org.paxle.parser.ISubParser;
 import org.paxle.parser.ISubParserManager;
 import org.paxle.parser.ParserContext;
 import org.paxle.parser.ParserException;
 
 public class ParserWorker extends AWorker<ICommand> {
 
 	/**
 	 * A class to manage {@link ISubParser sub-parsers}
 	 */
 	private ISubParserManager subParserManager = null;
 	
 	/**
 	 * A class to detect mimetypes
 	 */
 	IMimeTypeDetector mimeTypeDetector = null;
 	
 	/**
 	 * A class to detect charsets
 	 */
 	ICharsetDetector charsetDetector = null;
 	
 	ITempFileManager tempFileManager = null;
 	
 	IReferenceNormalizer referenceNormalizer = null;
 	
 	/**
 	 * A logger class 
 	 */
 	private final Log logger = LogFactory.getLog(ParserWorker.class);
 	
 	public ParserWorker(ISubParserManager subParserManager) {
 		this.subParserManager = subParserManager;
 	}
 	
 	/**
 	 * Init the parser context
 	 */
 	protected void initParserContext() {
 		// init the parser context object
 		ParserContext parserContext = new ParserContext(
 				this.subParserManager,
 				this.mimeTypeDetector,
 				this.charsetDetector,
 				this.tempFileManager,
 				this.referenceNormalizer);
 		ParserContext.setCurrentContext(parserContext);		
 	}
 	
 	@Override
 	protected void execute(ICommand command) {
 		final long start = System.currentTimeMillis();
 		IParserDocument parserDoc = null;
 		try {
 			
 			/* ================================================================
 			 * Input Parameter Check
 			 * ================================================================ */
 			String errorMsg = null;
 			if (command.getResult() != ICommand.Result.Passed) {
 				errorMsg = String.format(
 						"Won't parse resource '%s'. Command status is: '%s' (%s)",
 						command.getLocation(),
 						command.getResult(),
 						command.getResultText()
 				);
 			} else if (command.getCrawlerDocument() == null) {
 				errorMsg = String.format(
 						"Won't parse resource '%s'. Crawler-document is null",
 						command.getLocation()
 				);
 			} else if (command.getCrawlerDocument().getStatus() != ICrawlerDocument.Status.OK) {
 				errorMsg = String.format(
 						"Won't parse resource '%s'. Crawler-document status is: '%s' (%s)",
 						command.getLocation(),
 						command.getCrawlerDocument().getStatus(),
 						command.getCrawlerDocument().getStatusText()
 				);			
 			}
 
 			if (errorMsg != null) {
 				this.logger.warn(errorMsg);
 				return;
 			}
 
 			/* ================================================================
 			 * Parse Resource
 			 * 
 			 * a) determine content mime-type
 			 * b) fetch appropriate parser
 			 * c) parse resource
 			 * d) process parser response
 			 * ================================================================ */			
 			
 			// init the parser context
 			this.initParserContext();
 
 			// determine resource mimetype
 			String mimeType = command.getCrawlerDocument().getMimeType();
 			if (mimeType == null) {
 				// document not parsable
 				this.logger.error(String.format("Unable to parse resource '%s'. No mime-type was specified.",command.getLocation()));
 				command.setResult(ICommand.Result.Failure, "No mime-type was specified");
 				return;			
 			}
 
 			// get appropriate parser
 			this.logger.debug(String.format("Getting parser for mime-type '%s' ...", mimeType));
 			ISubParser parser = this.subParserManager.getSubParser(mimeType);
 			
 			if (parser == null) {
 				// document not parsable
 				this.logger.error(String.format("No parser for resource '%s' and mime-type '%s' found.",command.getLocation(),mimeType));
 				command.setResult(
 						ICommand.Result.Failure, 
 						String.format("No parser for mime-type '%s' found.", mimeType)
 				);
 				return;
 			}
 			this.logger.debug(String.format("Parser '%s' found for mime-type '%s'.", parser.getClass().getName(), mimeType));
 
 			// parse resource
 			try {
 				this.logger.info(String.format("Parsing resource '%s' with mime-type '%s' ...", command.getLocation(), mimeType));
 				parserDoc = parser.parse(
 						command.getLocation(), 
 						command.getCrawlerDocument().getCharset(), 
 						command.getCrawlerDocument().getContent()
 				);
 			} catch (ParserException e) {
 				parserDoc = new ParserDocument();
 				parserDoc.setStatus(IParserDocument.Status.FAILURE, e.getMessage());
 			}
 			
 			/* ================================================================
 			 * Process parser response
 			 * ================================================================ */			
 			
 			if (parserDoc == null) {
 				command.setResult(
 						ICommand.Result.Failure, 
 						String.format("Parser '%s' returned no parser-document.",parserDoc.getClass().getName())
 				);
 				return;
 			} else if (parserDoc.getStatus() == null || parserDoc.getStatus() != IParserDocument.Status.OK) {
 				command.setResult(
 						ICommand.Result.Failure, 
 						String.format("Parser-document status is '%s'.",parserDoc.getStatus())
 				);
 				return;
 			}
 			
 			// setting of default properties
 			if (parserDoc.getMimeType() == null) {
 				parserDoc.setMimeType(mimeType);
 			}
 			
 			// setting command status to passed
 			command.setResult(ICommand.Result.Passed,null);
 			
 		} catch (Exception e) {
 			// setting command status
 			command.setResult(
 					ICommand.Result.Failure, 
 					String.format("Unexpected '%s' while parsing resource. %s",e.getClass().getName(),e.getMessage())
 			);
 			
 			// log error
 			this.logger.warn(String.format("Unexpected '%s' while parsing resource '%s'.",
 					e.getClass().getName(),
 					command.getLocation()
 			),e);			
 		} finally {
 			/* 
 			 * Append parser-doc to command object.
 			 * 
 			 * This must be done even in error situations to 
 			 * - allow filters to correct the error (if possible)
 			 * - to report the error back properly (e.g. to store it into db
 			 *   or send it back to a remote peer). 
 			 */
 			if (parserDoc != null) {
 				command.setParserDocument(parserDoc);
 			}
 			
 			ICrawlerDocument crawlerDoc = command.getCrawlerDocument();
 			
 			
 			if (logger.isDebugEnabled()) {
 				this.logger.info(String.format(
 						"Finished parsing of resource '%s' with mime-type '%s' in %d ms.\r\n" +
 						"\tCrawler-Status: '%s' %s\r\n" +
 						"\tParser-Status:  '%s' %s",
 						command.getLocation(),
 						(command.getCrawlerDocument() == null) ? "unknown" : command.getCrawlerDocument().getMimeType(),
 						Long.valueOf(System.currentTimeMillis() - start),
 						(crawlerDoc == null) ? "unknown" : crawlerDoc.getStatus().toString(),
 						(crawlerDoc == null) ? "" : (crawlerDoc.getStatusText()==null)?"":crawlerDoc.getStatusText(),								
 						(parserDoc == null)  ? "unknown" : parserDoc.getStatus().toString(),
 						(parserDoc == null)  ? "" : (parserDoc.getStatusText()==null)?"":parserDoc.getStatusText()
 				));
 			} else if (logger.isInfoEnabled()) {
 				logger.info(String.format("Finished parsing of resource '%s' with mime-type '%s' in %d ms.\r\n" +
 						"\tParser-Status:  '%s' %s",
 						command.getLocation(),
						(command.getCrawlerDocument() == null) ? "unknown" : command.getCrawlerDocument().getMimeType(),
 						Long.valueOf(System.currentTimeMillis() - start),
						(parserDoc == null)  ? "unknown" : parserDoc.getStatus().toString(),
 						(parserDoc == null)  ? "" : (parserDoc.getStatusText()==null)?"":parserDoc.getStatusText()));
 			}
 		}
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
