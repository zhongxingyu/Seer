 package info.freelibrary.edtf;
 
 import java.util.Iterator;
 
 import info.freelibrary.edtf.internal.EDTFLexer;
 import info.freelibrary.edtf.internal.EDTFParser;
 import info.freelibrary.edtf.internal.EDTFParseListener;
 import info.freelibrary.edtf.internal.ParserErrorListener;
 
 import org.antlr.v4.runtime.ANTLRInputStream;
 import org.antlr.v4.runtime.CommonTokenStream;
 import org.antlr.v4.runtime.Token;
 import org.antlr.v4.runtime.tree.ParseTree;
 import org.antlr.v4.runtime.tree.ParseTreeWalker;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DateTimeParser {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(DateTimeParser.class);
 
 	public DateTimeParser() {}
 
 	public DateTime parseDateTime(String aEDTFDateTime) throws SyntaxException {
 		return (DateTime) parse(aEDTFDateTime);
 	}
 
 	public LocalDate parseLocalDate(String aEDTFDate) throws SyntaxException {
 		return (LocalDate) parse(aEDTFDate);
 	}
 
 	public LocalTime parseLocalTime(String aEDTFTime) throws SyntaxException {
 		return (LocalTime) parse(aEDTFTime);
 	}
 
 	public LocalDateTime parseLocalDateTime(String aEDTFDateTime)
 			throws SyntaxException {
 		return (LocalDateTime) parse(aEDTFDateTime);
 	}
 
 	public EDTF parse(String aEDTFString) throws SyntaxException {
 		EDTFLexer lexer = new EDTFLexer(new ANTLRInputStream(aEDTFString));
 		EDTFParser parser = new EDTFParser(new CommonTokenStream(lexer));
 		ParseTreeWalker walker = new ParseTreeWalker();
 
 		lexer.removeErrorListeners(); // remove generic System.err listener
 
 		if (LOGGER.isDebugEnabled()) {
 			StringBuilder strBuffer = new StringBuilder(aEDTFString + " => ");
 			Iterator<? extends Token> iter = lexer.getAllTokens().iterator();
 
 			while (iter.hasNext()) {
				strBuffer.append(EDTFLexer.tokenNames[iter.next().getType()]);
 				strBuffer.append(' ');
 			}
 
 			LOGGER.debug(strBuffer.insert(0, "Lexer tokens: ").toString());
 			lexer.reset();
 		}
 
 		// Wait to add our ErrorListener until we're ready to parse for real
 		lexer.addErrorListener(new ParserErrorListener(parser));
 		parser.removeErrorListeners(); // remove generic System.err listener
 
 		ParseTree tree = parser.edtf();
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Parse tree: " + tree.toStringTree(parser));
 		}
 
 		walker.walk(new EDTFParseListener(parser), tree);
 
 		if (parser.getNumberOfSyntaxErrors() > 0) {
 			String message = "Parse of '{}' failed";
 			
 			if (LOGGER.isWarnEnabled()) {
 				LOGGER.warn(message, aEDTFString);
 			}
 
 			throw new SyntaxException(message);
 		}
 		else {
 			return (EDTF) new LocalDateTime();
 		}
 	}
 }
