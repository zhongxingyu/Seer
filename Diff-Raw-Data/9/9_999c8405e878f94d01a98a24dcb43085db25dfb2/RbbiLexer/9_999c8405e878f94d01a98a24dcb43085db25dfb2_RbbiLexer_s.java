 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.steps.tokenization.engine;
 
 import java.util.TreeMap;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.steps.tokenization.common.AbstractLexer;
 import net.sf.okapi.steps.tokenization.common.Lexem;
 import net.sf.okapi.steps.tokenization.common.Lexems;
 import net.sf.okapi.steps.tokenization.common.LexerRule;
 import net.sf.okapi.steps.tokenization.tokens.Tokens;
 
 import com.ibm.icu.text.BreakIterator;
 import com.ibm.icu.text.RuleBasedBreakIterator;
 import com.ibm.icu.util.ULocale;
 
 public class RbbiLexer extends AbstractLexer {
 
 	// Cache for iterators reuse
 	private TreeMap <LocaleId, RuleBasedBreakIterator> iterators = new TreeMap <LocaleId, RuleBasedBreakIterator>();
 
 	private RuleBasedBreakIterator iterator = null;
 	private int start;
 	private int end;
 	private String text;
 	
 	@Override
 	public void lexer_init() {
 		
 	}
 	
 	@Override
 	public boolean lexer_hasNext() {
 		
 		return end != BreakIterator.DONE;
 	}
 
 	@Override
 	public Lexem lexer_next() {
 		
 		end = iterator.next();
 		if (end == BreakIterator.DONE) return null;
 		
 		if (start >= end) return null;
 		
 		int lexemId = iterator.getRuleStatus();
 		Lexem lexem = new Lexem(lexemId, text.substring(start,end), start, end);
 		
 		//System.out.println(lexem.toString());
 		start = end; // Prepare for the next iteration
 		
 		return lexem;
 	}
 
 	public static String formatRule(String buffer, String name, String description, String rule, int lexemId) {
 		
 		buffer = Util.normalizeNewlines(buffer);
 		rule = rule.replace("\\", "\\\\");
 		
 		String part1 = String.format("\\$%s = %s;", name, rule);
 		String part2 = String.format("\\$%s {%d};", name, lexemId);
 		
 		buffer = buffer.replaceFirst("(!!forward;)", String.format("%s$0", part1));
 		buffer = buffer.replaceFirst("(!!reverse;)", String.format("%s$0", part2));
 		
 		return buffer;
 	}
 	
 	@Override
 	public void lexer_open(String text, LocaleId language, Tokens tokens) {
 		
 		if (Util.isEmpty(text)) {
 			cancel();
 			return;
 		}
 		this.text = text;
 		
 		if ( iterators.containsKey(language) ) {
 			iterator = iterators.get(language);
 		}
 		else {
 			iterator = (RuleBasedBreakIterator)BreakIterator.getWordInstance(
 				ULocale.createCanonical(language.toString()));
 			String defaultRules = iterator.toString();
 			
 			// Collect rules for the language, combine with defaultRules
 			String newRules = defaultRules;			
 			
 			for (LexerRule rule : getRules()) {
 				
 				boolean isInternal = Util.isEmpty(rule.getPattern());
 				
 				if (checkRule(rule, language) && !isInternal) {
 					
 					newRules = formatRule(newRules, rule.getName(), rule.getDescription(), rule.getPattern(), rule.getLexemId());
 				}					
 			}				
 			
 			// Recreate iterator for the language(with new rules), store for future reuse
 			iterator = new RuleBasedBreakIterator(newRules); 
 			iterators.put(language, iterator);
			RuleBasedBreakIterator.registerInstance(iterator, language.toJavaLocale(),
					BreakIterator.KIND_WORD);
 		}
 
 		if ( iterator == null ) return;		
 		iterator.setText(text);
 		
 		// Sets the current iteration position to the beginning of the text
 		start = iterator.first();
 		end = start;
 	}
 
 	public Lexems process(String text, LocaleId language, Tokens tokens) {
 		
 		return null;
 	}
 
 }
