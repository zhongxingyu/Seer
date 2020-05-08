 package org.rubypeople.rdt.internal.ui.text.ruby;
 
 import org.eclipse.jface.text.rules.IToken;
 
 /**
  * A rule which will return the given Token for Ruby symbols.
  * 
  * @author cawilliams
  * 
  */
 public class SymbolRule extends SingleCharacterPrefixRule {
 
	private static final char PREFIX = ':';
 
	public SymbolRule(IToken token) {
		super(PREFIX, token, 2, Integer.MAX_VALUE);
	}

	protected boolean isValidCharacter(int c, int index) {
		if (!super.isValidCharacter(c, index))
			return false;
		char theChar = (char) c;
		switch (theChar) {
		case ':': // other than first character (prefix) symbols can't contain
					// another colon
		case '[': // FIXME This isn't always the case. There's special method names containing brackets that is valid (only the particular sequence)
		case ']':
		case '{':
		case '}':
		case '(':
		case ')':
			return false;

		default:
			return true;
		}
	}
 
 }
