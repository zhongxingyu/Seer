 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.markupfilter;
 
 import java.util.Stack;
 
 import net.sf.okapi.filters.markupfilter.ExtractionRule.EXTRACTION_RULE_TYPE;
 
 /**
  * Holds the current HTML parser's rule state.
  */
 public class ExtractionRuleState {
 
	private static final class RuleType {
 
 		public RuleType(String ruleName, EXTRACTION_RULE_TYPE ruleType) {
 			this.ruleName = ruleName;
 			this.ruleType = ruleType;
 		}
 
 		public String ruleName;
 		public EXTRACTION_RULE_TYPE ruleType;
 	}
 
 	private Stack<RuleType> preserveWhiteSpaceRuleStack;
 	private Stack<RuleType> excludedIncludedRuleStack;
 	private Stack<RuleType> groupRuleStack;
 	private Stack<RuleType> textUnitRuleStack;	
 
 	/**
 	 * 
 	 */
 	public ExtractionRuleState() {
 		preserveWhiteSpaceRuleStack = new Stack<RuleType>();
 		excludedIncludedRuleStack = new Stack<RuleType>();		
 		groupRuleStack = new Stack<RuleType>();
 		textUnitRuleStack = new Stack<RuleType>();
 	}
 	
 	public void reset() {				
 	}
 	
 	public boolean isGroupState() {
 		if (groupRuleStack.isEmpty())
 			return false;
 		if (groupRuleStack.peek().ruleType == EXTRACTION_RULE_TYPE.GROUP_ELEMENT)
 			return true;
 
 		return false;
 	}
 
 	public boolean isTextUnitState() {
 		if (textUnitRuleStack.isEmpty())
 			return false;
 		if (textUnitRuleStack.peek().ruleType == EXTRACTION_RULE_TYPE.TEXT_UNIT_ELEMENT)
 			return true;
 
 		return false;
 	}
 
 	public boolean isExludedState() {
 		if (excludedIncludedRuleStack.isEmpty())
 			return false;
 		if (excludedIncludedRuleStack.peek().ruleType == EXTRACTION_RULE_TYPE.EXCLUDED_ELEMENT)
 			return true;
 
 		return false;
 	}
 
 	public boolean isPreserveWhitespaceState() {
 		if (preserveWhiteSpaceRuleStack.isEmpty())
 			return false;
 		if (preserveWhiteSpaceRuleStack.peek().ruleType == EXTRACTION_RULE_TYPE.PRESERVE_WHITESPACE)
 			return true;
 
 		return false;		
 	}
 
 	public void pushPreserverWhitespaceRule(String ruleName) {
 		preserveWhiteSpaceRuleStack.push(new RuleType(ruleName, EXTRACTION_RULE_TYPE.PRESERVE_WHITESPACE));
 	}
 
 	public void popPreserverWhitespaceRule() {
 		preserveWhiteSpaceRuleStack.pop();
 	}
 
 	public void pushExcludedRule(String ruleName) {
 		excludedIncludedRuleStack.push(new RuleType(ruleName, EXTRACTION_RULE_TYPE.EXCLUDED_ELEMENT));
 	}
 
 	public void pushIncludedRule(String ruleName) {
 		excludedIncludedRuleStack.push(new RuleType(ruleName, EXTRACTION_RULE_TYPE.INCLUDED_ELEMENT));
 	}
 	
 	public void pushGroupRule(String ruleName) {
 		groupRuleStack.push(new RuleType(ruleName, EXTRACTION_RULE_TYPE.GROUP_ELEMENT));
 	}
 
 	public void pushTextUnitRule(String ruleName) {
 		textUnitRuleStack.push(new RuleType(ruleName, EXTRACTION_RULE_TYPE.TEXT_UNIT_ELEMENT));
 	}
 	
 	public void popExcludedIncludedRule() {
 		excludedIncludedRuleStack.pop();
 	}
 	
 	public void popGroupRule() {
 		groupRuleStack.pop();
 	}
 
 	public void popTextUnitRule() {
 		textUnitRuleStack.pop();
 	}		
 }
