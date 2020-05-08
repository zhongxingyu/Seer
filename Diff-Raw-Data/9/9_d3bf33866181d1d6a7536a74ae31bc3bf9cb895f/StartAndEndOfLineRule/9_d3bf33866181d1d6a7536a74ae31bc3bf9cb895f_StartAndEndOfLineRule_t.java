 package patcheditor.editors;
 
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.PatternRule;
 import org.eclipse.jface.text.rules.Token;
 
 public class StartAndEndOfLineRule extends PatternRule {
 
 	public StartAndEndOfLineRule(String startSequence, IToken token) {
 		this(startSequence, null, token);
 	}
 	
 	public StartAndEndOfLineRule(String startSequence, String endSequence, IToken token) {
 		super(startSequence, endSequence, token, (char) 0, true, false) ;
 	}
 
 	protected IToken doEvaluate(ICharacterScanner scanner, boolean resume) {
 
 		if (resume) {
 
 			if (endSequenceDetected(scanner))
 				return fToken;
 
 		} else {
 
 			scanner.unread();
 			int lastChar = scanner.read();
 			int c= scanner.read();
 			
			if(fStartSequence.length == 1){
				if(lastChar == -1 && c == fStartSequence[0]){
					if (sequenceDetected(scanner, fStartSequence, false)) {
						if (endSequenceDetected(scanner))
							return fToken;
					}
				} 
			}
			
 			if (c == fStartSequence[0] && (lastChar == '\r' || lastChar == '\n')) {
 				if (sequenceDetected(scanner, fStartSequence, false)) {
 					if (endSequenceDetected(scanner))
 						return fToken;
 				}
 			}
 		}
 
 		scanner.unread();
 		return Token.UNDEFINED;
 	}
 
 }
