 package de.prob.ui.ltl;
 
 import java.util.List;
 
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IPredicateRule;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.Token;
 
 import de.prob.ui.ltl.util.CharacterScannerReader;
 
 public class ReservedWordRule implements IPredicateRule {
 
 	private List<String> allowedCharacterSequences;
 
 	protected String word;
 	protected IToken token;
 
 	public ReservedWordRule(String word, IToken token, List<String> allowedCharacterSequences) {
 		this.word = word;
 		this.token = token;
 		this.allowedCharacterSequences = allowedCharacterSequences;
 	}
 
 	@Override
 	public IToken evaluate(ICharacterScanner scanner) {
 		CharacterScannerReader reader = new CharacterScannerReader(scanner);
 
 		reader.savePosition();
 		if (word.charAt(0) == reader.peek()) {
 			String readWord = reader.readString(word.length());
 			if (word.equals(readWord)) {
 				if (checkSeqAfter(reader)) {
 					if (checkSeqBefore(reader)) {
						if (allowedCharacterSequences != null) {
							reader.jump(word.length());
						}
 						return token;
 					}
 				}
 			}
 		}
 		reader.resetPosition();
 		return Token.UNDEFINED;
 	}
 
 	@Override
 	public IToken getSuccessToken() {
 		return token;
 	}
 
 	@Override
 	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
 		return evaluate(scanner);
 	}
 
 	boolean checkSeqBefore(CharacterScannerReader reader) {
 		if (allowedCharacterSequences == null) {
 			return true;
 		}
 		reader.resetPosition();
 		if (reader.isBOF()) {
 			return true;
 		}
 		for (String seq : allowedCharacterSequences) {
 			int n = seq.length();
 			reader.resetPosition();
 
 			if (-n == reader.jump(-n)) {
 				String before = reader.readString(n);
 				if (seq.equals(before)) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	boolean checkSeqAfter(CharacterScannerReader reader) {
 		if (allowedCharacterSequences == null) {
 			return true;
 		}
 		if (reader.isEOF()) {
 			return true;
 		}
 		for (String seq : allowedCharacterSequences) {
 			int n = seq.length();
 
 			String after = reader.readString(n);
 			if (after != null) {
 				reader.jump(-n);
 				if (seq.equals(after)) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 }
