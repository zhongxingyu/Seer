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
 
 package net.sf.okapi.filters.abstractmarkup;
 
 import java.util.EmptyStackException;
 import java.util.Stack;
 
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration.RULE_TYPE;
 
 /**
  * Holds the current parser's rule state. State is maintained on separate
  * stacks.
  */
 public class ExtractionRuleState {
 
 	public final class RuleType {
 
 		public String ruleName;
 		public RULE_TYPE ruleType;
 		public String idValue;
 		
 		public RuleType(String ruleName, RULE_TYPE ruleType, String idValue) {
 			this.ruleName = ruleName;
 			this.ruleType = ruleType;
 			this.idValue = idValue;
 		}
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
 		if (groupRuleStack.peek().ruleType == RULE_TYPE.GROUP_ELEMENT)
 			return true;
 
 		return false;
 	}
 
 	public boolean isTextUnitState() {
 		if (textUnitRuleStack.isEmpty())
 			return false;
 		if (textUnitRuleStack.peek().ruleType == RULE_TYPE.TEXT_UNIT_ELEMENT)
 			return true;
 
 		return false;
 	}
 
 	public boolean isExludedState() {
 		if (excludedIncludedRuleStack.isEmpty())
 			return false;
 		if (excludedIncludedRuleStack.peek().ruleType == RULE_TYPE.EXCLUDED_ELEMENT)
 			return true;
 
 		return false;
 	}
 
 	public boolean isPreserveWhitespaceState() {
 		if (preserveWhiteSpaceRuleStack.isEmpty())
 			return false;
 		if (preserveWhiteSpaceRuleStack.peek().ruleType == RULE_TYPE.PRESERVE_WHITESPACE)
 			return true;
 
 		return false;
 	}
 
 	public void pushPreserverWhitespaceRule(String ruleName) {
 		preserveWhiteSpaceRuleStack.push(new RuleType(ruleName, RULE_TYPE.PRESERVE_WHITESPACE, null));
 	}
 	
 	public void pushPreserverWhitespaceRule(String ruleName, String idValue) {
 		preserveWhiteSpaceRuleStack.push(new RuleType(ruleName, RULE_TYPE.PRESERVE_WHITESPACE, idValue));
 	}
 
 	public RuleType popPreserverWhitespaceRule() {
 		return preserveWhiteSpaceRuleStack.pop();
 	}
 
 	public void pushExcludedRule(String ruleName) {
 		excludedIncludedRuleStack.push(new RuleType(ruleName, RULE_TYPE.EXCLUDED_ELEMENT, null));
 	}
 
 	public void pushExcludedRule(String ruleName, String idValue) {
 		excludedIncludedRuleStack.push(new RuleType(ruleName, RULE_TYPE.EXCLUDED_ELEMENT, idValue));
 	}
 
 	public void pushIncludedRule(String ruleName) {
 		excludedIncludedRuleStack.push(new RuleType(ruleName, RULE_TYPE.INCLUDED_ELEMENT, null));
 	}
 	
 	public void pushIncludedRule(String ruleName, String idValue) {
 		excludedIncludedRuleStack.push(new RuleType(ruleName, RULE_TYPE.INCLUDED_ELEMENT, idValue));
 	}
 
 	public void pushGroupRule(String ruleName) {
 		groupRuleStack.push(new RuleType(ruleName, RULE_TYPE.GROUP_ELEMENT, null));
 	}
 	
 	public void pushGroupRule(String ruleName, String idValue) {
 		groupRuleStack.push(new RuleType(ruleName, RULE_TYPE.GROUP_ELEMENT, idValue));
 	}
 
 	public void pushTextUnitRule(String ruleName) {
 		textUnitRuleStack.push(new RuleType(ruleName, RULE_TYPE.TEXT_UNIT_ELEMENT, null));
 	}
 	
 	public void pushTextUnitRule(String ruleName, String idValue) {
 		textUnitRuleStack.push(new RuleType(ruleName, RULE_TYPE.TEXT_UNIT_ELEMENT, idValue));
 	}
 
 	public RuleType popExcludedIncludedRule() {
 		return excludedIncludedRuleStack.pop();
 	}
 
 	public RuleType popGroupRule() {
 		return groupRuleStack.pop();
 	}
 
 	public RuleType popTextUnitRule() {
 		return textUnitRuleStack.pop();
 	}
	
 	public void clearTextUnitRules() {
 		textUnitRuleStack.clear();
 	}
 	
 	public String getTextUnitElementName() {
 		String n = "";
 		try {
 			n = textUnitRuleStack.peek().ruleName;
 		} catch (EmptyStackException e) {
 			// eat exception
 		}
 		return n;
 	}
 }
