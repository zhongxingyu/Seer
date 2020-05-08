 
 package org.paxle.parser.html.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.nio.charset.Charset;
 import java.nio.charset.IllegalCharsetNameException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.pool.ObjectPool;
 import org.apache.commons.pool.PoolableObjectFactory;
 import org.apache.commons.pool.impl.GenericObjectPool;
 
 import org.htmlparser.Parser;
 import org.htmlparser.lexer.InputStreamSource;
 import org.htmlparser.lexer.Lexer;
 
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.parser.CachedParserDocument;
 import org.paxle.parser.ParserContext;
 import org.paxle.parser.ParserException;
 import org.paxle.parser.html.IHtmlParser;
 
 /**
  * Parses (X)HTML-pages using the html parser from
  * <a href="http://htmlparser.sf.net">http://htmlparser.sf.net</a>.
  * <p>
  *  It uses a kind of iterator with callback to walk through the node-tree of
  *  the HTML page, extracting information whereever supported and putting it
  *  into the {@link CachedParserDocument}.
  * </p>
  * @see org.htmlparser.Parser#visitAllNodesWith(org.htmlparser.visitors.NodeVisitor) for the iterator
  * @see org.paxle.parser.html.impl.NodeCollector for the callback
  */
 public class HtmlParser implements IHtmlParser, PoolableObjectFactory {
 	
 	private static final List<String> MIME_TYPES = Arrays.asList(
 			"text/html",
 			"application/xhtml+xml",
 			"application/xml",
 			"text/xml",
 			"text/sgml");
 	
 	private final Log logger = LogFactory.getLog(HtmlParser.class);
 	
 	/*
 	public static void main(String[] args) {
 		final File dir = new File(args[0]);
 		final String[] files = dir.list();
 		for (int i=0; i<files.length; i++) try {
 			ParserDocument doc = new ParserDocument();
 			Page page = new PPage(new InputStreamSource(new FileInputStream(new File(dir, files[i]))));
 			page.setUrl("http://www.example.com/");
 			Parser parser = new Parser(new Lexer(page));
 			System.out.println("PARSING: " + parser.getURL());
 			parser.setNodeFactory(NodeCollector.NODE_FACTORY);
 			NodeCollector nc = new NodeCollector(doc);
 			System.out.println(files[i]);
 			parser.visitAllNodesWith(nc);
 			page.close();
 			System.out.println("-------------------------------------------------------------------------------------------");
 			System.out.println();
 			System.out.println(doc.toString());
 		} catch (final Exception e) { e.printStackTrace(); }
 	}*/
 	
 	private final ObjectPool pool = new GenericObjectPool(this);
 	
 	public HtmlParser() {
 	}
 	
 	public void activateObject(Object arg0) throws Exception {
 		((HtmlParserRequisites)arg0).reset();
 	}
 	
 	public void destroyObject(Object arg0) throws Exception {
 		// nothing to do
 	}
 	
 	public Object makeObject() throws Exception {
 		return new HtmlParserRequisites();
 	}
 	
 	public void passivateObject(Object arg0) throws Exception {
 		// nothing to do
 	}
 	
 	public boolean validateObject(Object arg0) {
 		// don't know how
 		return false;
 	}
 	
 	public List<String> getMimeTypes() {
 		return MIME_TYPES;
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		pool.close();
 		super.finalize();
 	}
 	
 	public IParserDocument parse(URI location, String charset, InputStream is)
 			throws ParserException, UnsupportedEncodingException, IOException {
 		try {
 			// testing if we support the charset. if not we try to use UTF-8
 			boolean unsupportedCharset = false;
 			try {
 				if (charset != null && !Charset.isSupported(charset)) {
 					unsupportedCharset = true;
 				}
 			} catch (IllegalCharsetNameException e) {
 				unsupportedCharset = true;
 			}
 			
 			if (unsupportedCharset) {
 				this.logger.warn(String.format(
 						"The resource '%s' has an unsupported charset '%s'. We try to use UTF-8 instead.", 
 						location,
 						charset
 				));
 				charset = "UTF-8";
 			}
 			
 			final InputStreamSource iss = new InputStreamSource(is, charset);
 			final ParserContext context = ParserContext.getCurrentContext();
 			final IParserDocument doc = new CachedParserDocument(context.getTempFileManager());
 			final HtmlParserRequisites req = (HtmlParserRequisites)pool.borrowObject();
 			req.logger.setLocation(location);
 			req.page.init(iss);
			req.page.setUrl(location.toASCIIString());
 			req.nc.init(doc, context.getReferenceNormalizer());
 			req.parser.visitAllNodesWith(req.nc);
 			
 			/*
 			final FixedPage page = new FixedPage(iss);
 			final ParserLogger pl = new ParserLogger(logger, location);
 			final Parser parser = new Parser(new Lexer(page), pl);
 			
 			final NodeCollector nc = new NodeCollector(doc, pl, page, context.getReferenceNormalizer());
 			parser.visitAllNodesWith(nc);
 			page.close();
 			*/
 			
 			iss.destroy();
 			pool.returnObject(req);
 			
 			if (charset != null && doc.getCharset() == null)
 				doc.setCharset(Charset.forName(charset));
 			doc.setStatus(IParserDocument.Status.OK);
 			return doc;
 		} catch (org.htmlparser.util.ParserException e) {
 			throw new ParserException("error parsing HTML nodes-tree", e);
 		} catch (Exception e) {
 			throw new ParserException("internal error: " + e.getMessage(), e);
 		}
 	}
 	
 	private final class HtmlParserRequisites {
 		final ParserLogger logger = new ParserLogger(HtmlParser.this.logger);
 		final FixedPage page = new FixedPage();
 		final Lexer lexer = new Lexer(page);
 		final Parser parser = new Parser(lexer, logger);
 		final NodeCollector nc = new NodeCollector(logger, page);
 		
 		public HtmlParserRequisites() {
 	        lexer.setNodeFactory(NodeCollector.NODE_FACTORY);
 		}
 		
 		public void reset() {
 			// no need to reset the logger
 			lexer.getCursor().setPosition(0);
 			/* the lexer does not have to be reset, it would only reset
 			 *  - the page, which is done prior to each parsing process by the parse()-method
 			 *  - the cursor, which is done above
 			 */
 			// no need to reset the parser, it would only reset the lexer - see above
 			// no need to reset the nc, this is done prior to each parsing process by the parse()-method
 		}
 	}
 	
 	/**
 	 * TODO: the html parser does not seem to extract the keywords
 	 */
 	public IParserDocument parse(URI location, String charset, File content) throws ParserException,
 			UnsupportedEncodingException, IOException {
 		final FileInputStream fis = new FileInputStream(content);
 		try {
 			return parse(location, charset, fis);
 		} finally {
 			fis.close();
 		}
 	}
 }
