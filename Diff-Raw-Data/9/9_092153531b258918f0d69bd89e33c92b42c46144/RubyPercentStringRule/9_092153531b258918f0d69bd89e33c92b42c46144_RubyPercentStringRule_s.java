 package org.eclipse.dltk.ruby.internal.ui.text;
 
 import java.util.Arrays;
 import java.util.Comparator;
 
 import org.eclipse.dltk.ruby.core.utils.RubySyntaxUtils;
 import org.eclipse.jface.text.Assert;
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IPredicateRule;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.Token;
 
 public class RubyPercentStringRule implements IPredicateRule {
 
 	private static final char ESCAPE = '\\';
 
 	/**
 	 * Comparator that orders <code>char[]</code> in decreasing array lengths.
 	 *
 	 * @since 3.1
 	 */
 	private static class DecreasingCharArrayLengthComparator implements Comparator {
 		public int compare(Object o1, Object o2) {
 			return ((char[]) o2).length - ((char[]) o1).length;
 		}
 	}
 
 	/** Internal setting for the un-initialized column constraint */
 	protected static final int UNDEFINED= -1;
 
 	/** The token to be returned on success */
 	protected IToken fToken;
 	/** The pattern's column constrain */
 	protected int fColumn= UNDEFINED;
 	/**
 	 * Indicates whether the escape character continues a line
 	 * @since 3.0
 	 */
 	protected boolean fEscapeContinuesLine;
 	/** Indicates whether end of line terminates the pattern */
 	protected boolean fBreaksOnEOL;
 	/** Indicates whether end of file terminates the pattern */
 	protected boolean fBreaksOnEOF;
 
 	/**
 	 * Line delimiter comparator which orders according to decreasing delimiter length.
 	 * @since 3.1
 	 */
 	private Comparator fLineDelimiterComparator= new DecreasingCharArrayLengthComparator();
 	/**
 	 * Cached line delimiters.
 	 * @since 3.1
 	 */
 	private char[][] fLineDelimiters;
 	/**
 	 * Cached sorted {@linkplain #fLineDelimiters}.
 	 * @since 3.1
 	 */
 	private char[][] fSortedLineDelimiters;
 
 	/**
 	 * Creates a rule for the given starting and ending sequence.
 	 * When these sequences are detected the rule will return the specified token.
 	 * Alternatively, the sequence can also be ended by the end of the line.
 	 * Any character which follows the given escapeCharacter will be ignored.
 	 *
 	 * @param token the token which will be returned on success
 	 * @param breaksOnEOL indicates whether the end of the line also terminates the pattern
 	 */
 	public RubyPercentStringRule(IToken token, boolean breaksOnEOL) {
 		Assert.isNotNull(token);
 
 		fToken= token;
 		fBreaksOnEOL= breaksOnEOL;
 		fBreaksOnEOF= false;
 	}
 
 	/**
 	 * Sets a column constraint for this rule. If set, the rule's token
 	 * will only be returned if the pattern is detected starting at the
 	 * specified column. If the column is smaller then 0, the column
 	 * constraint is considered removed.
 	 *
 	 * @param column the column in which the pattern starts
 	 */
 	public void setColumnConstraint(int column) {
 		if (column < 0)
 			column= UNDEFINED;
 		fColumn= column;
 	}
 
 
 	/**
 	 * Evaluates this rules without considering any column constraints.
 	 *
 	 * @param scanner the character scanner to be used
 	 * @return the token resulting from this evaluation
 	 */
 	protected IToken doEvaluate(ICharacterScanner scanner) {
 		return doEvaluate(scanner, false);
 	}
 
 	/**
 	 * Evaluates this rules without considering any column constraints. Resumes
 	 * detection, i.e. look sonly for the end sequence required by this rule if the
 	 * <code>resume</code> flag is set.
 	 *
 	 * @param scanner the character scanner to be used
 	 * @param resume <code>true</code> if detection should be resumed, <code>false</code> otherwise
 	 * @return the token resulting from this evaluation
 	 * @since 2.0
 	 */
 	protected IToken doEvaluate(ICharacterScanner scanner, boolean resume) {
 
 		if (resume) {
 
 //			if (endSequenceDetected(scanner, (char), ']'))
 //				return fToken;
 
 		} else {
 
 			int c= scanner.read();
 			if (c == '%') {
 				char leader = startSequenceDetected(scanner);
 				char term = RubySyntaxUtils.getPercentStringTerminator((char) leader);
 				if (term != (char) 0) {
 					if (endSequenceDetected(scanner, leader, term))
 						return fToken;
 				}
 			}
 		}
 
 		scanner.unread();
 		return Token.UNDEFINED;
 	}
 
 	/*
 	 * @see IRule#evaluate(ICharacterScanner)
 	 */
 	public IToken evaluate(ICharacterScanner scanner) {
 		return evaluate(scanner, false);
 	}
 
 	protected boolean endSequenceDetected(ICharacterScanner scanner, char lead, char term) {
 
 		char[][] originalDelimiters= scanner.getLegalLineDelimiters();
 		int count= originalDelimiters.length;
 		if (fLineDelimiters == null || originalDelimiters.length != count) {
 			fSortedLineDelimiters= new char[count][];
 		} else {
 			while (count > 0 && fLineDelimiters[count-1] == originalDelimiters[count-1])
 				count--;
 		}
 		if (count != 0) {
 			fLineDelimiters= originalDelimiters;
 			System.arraycopy(fLineDelimiters, 0, fSortedLineDelimiters, 0, fLineDelimiters.length);
 			Arrays.sort(fSortedLineDelimiters, fLineDelimiterComparator);
 		}
 
 		int c;
 		int nestCount = 1;
 		while ((c= scanner.read()) != ICharacterScanner.EOF) {
 			if (c == ESCAPE) {
				System.out.println("ESCAPE " + (char) c);
 				// Skip escaped character(s)
 				if (fEscapeContinuesLine) {
 					c= scanner.read();
 					for (int i= 0; i < fSortedLineDelimiters.length; i++) {
 						if (c == fSortedLineDelimiters[i][0] && sequenceDetected(scanner, fSortedLineDelimiters[i], true))
 							break;
 					}
 				} else
 					scanner.read();
 
 			} else if (lead != term && c == lead) {
 				nestCount++;
 			} else if (c == (int) term) {
 				if (--nestCount <= 0)
 					return true;
 			} else if (fBreaksOnEOL) {
 				System.out.println((char) c);
 				// Check for end of line since it can be used to terminate the pattern.
 				for (int i= 0; i < fSortedLineDelimiters.length; i++) {
 					if (c == fSortedLineDelimiters[i][0] && sequenceDetected(scanner, fSortedLineDelimiters[i], true))
 						return true;
 				}
 			}
 		}
 		if (fBreaksOnEOF) return true;
 		scanner.unread();
 		return false;
 	}
 
 	protected char startSequenceDetected(ICharacterScanner scanner) {
 		int c = scanner.read();
 		if (c == ICharacterScanner.EOF) {
 			scanner.unread();
 			return (char) 0;
 		}
 		boolean letterFound = false;
 		if (RubySyntaxUtils.isValidPercentStringStarter((char) c)) {
 			letterFound = true;
 			c = scanner.read();
 			if (c == ICharacterScanner.EOF) {
 				scanner.unread();
 				scanner.unread();
 				return (char) 0;
 			}
 		}
 		char term = RubySyntaxUtils.getPercentStringTerminator((char) c);
 		if (term == (char) 0) {
 			scanner.unread();
 			if (letterFound)
 				scanner.unread();
 			return (char) 0;
 		}
 		return (char) c;
 	}
 	
 	protected boolean sequenceDetected(ICharacterScanner scanner, char[] sequence, boolean eofAllowed) {
 		for (int i= 1; i < sequence.length; i++) {
 			int c= scanner.read();
 			if (c == ICharacterScanner.EOF && eofAllowed) {
 				return true;
 			} else if (c != sequence[i]) {
 				// Non-matching character detected, rewind the scanner back to the start.
 				// Do not unread the first character.
 				scanner.unread();
 				for (int j= i-1; j > 0; j--)
 					scanner.unread();
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
 	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
 		int c= scanner.read();
 		scanner.unread();
 		if (c == '%') {
 //			RubyPartitionScanner s = (RubyPartitionScanner) scanner;
 //			if (s.getCurrentContext() == RubyContext.AFTER_EXPRESSION)
 //				return Token.UNDEFINED;
 			return doEvaluate(scanner, resume);
 		}
 		return Token.UNDEFINED;
 	}
 
 	public IToken getSuccessToken() {
 		return fToken;
 	}
 }
