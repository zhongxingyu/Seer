 package retrieWin.SSF;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import retrieWin.SSF.Constants.EdgeDirection;
 import retrieWin.SSF.Constants.PatternType;
 import retrieWin.Utils.FileUtils;
 
 public class SlotPattern  implements Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private double confidenceScore;
 	private List<Rule> rules;
 	private String pattern;
 	private PatternType patternType = PatternType.WordInBetween;
 	
 	public static class Rule implements Serializable{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		public EdgeDirection direction;
 		public String edgeType;
 		
 		public Rule() {
 			
 		}
 		
 		public Rule(String str) {
 			String val;
 			String[] split;
 			split = str.split(",", 2);
 			val = split[0].split("=")[1].trim().replaceAll("\\[|\\]|\\{|\\}", "");
 			edgeType = val;
 			val = split[1].split("=")[1].trim().replaceAll("\\[|\\]|\\{|\\}", "");
 			direction = val.equals("Out") ? Constants.EdgeDirection.Out : Constants.EdgeDirection.In;
 		}
 		
 		@Override
 		public String toString() {
 			String ret = "[";
 			ret += "edgeType = " + edgeType + ", ";
 			ret += "direction = " + direction + "]";
 			return ret;
 		}
 		
 		@Override
 		public boolean equals(Object obj) {
 		       if (this == obj)
 		           return true;
 		       if (obj == null)
 		           return false;
 		       if (getClass() != obj.getClass())
 		           return false;
 		       final Rule other = (Rule) obj;
 		       if (this.direction == other.direction && this.edgeType.equals(other.edgeType))
 		           return true;
 		       return false;
 		   }
 		
 		@Override
 		public int hashCode() {
 			return edgeType.hashCode() + direction.toString().hashCode();
 		}
 	}
 	public SlotPattern() {
 		
 	}
 	
 	public SlotPattern(String str) {
 		String var, val;
 		String[] split;
 		boolean rulesAdded = false;
 		split = str.split(",", 2);
 		while(split.length > 0 && !rulesAdded) {
 			var = split[0].split("=", 2)[0].trim().replaceAll("\\[|\\]", "");
 			val = split[0].split("=", 2)[1].trim();
 			
 			if(var.equals("pattern"))
 				pattern = val;
 			else if(var.equals("confidenceScore"))
 				confidenceScore = Double.valueOf(val);
 			else if(var.equals("patternType")) {
 				if(!val.equals("null"))
 					setPatternType(Constants.PatternType.valueOf(val));
 			}
 			else if(var.equals("rules")) {
 				rulesAdded = true;
 				for(String r: val.split("\\],")) {
 					addRule(new Rule(r));
 				}
 			}
 			
 			if(split.length == 1)
 				break;
 			else if(split[1].trim().startsWith("rules"))
 				split = new String[]{split[1]};
 			else
 				split = split[1].split(",", 2);
 		}
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 	       if (this == obj)
 	           return true;
 	       if (obj == null)
 	           return false;
 	       if (getClass() != obj.getClass())
 	           return false;
 	       final SlotPattern other = (SlotPattern) obj;
 	       if (this.rules.size() != other.rules.size() || !other.pattern.toLowerCase().equals(this.pattern.toLowerCase()))
 	           return false;
 	       if((this.rules.get(0).equals(other.rules.get(0)) && this.rules.get(1).equals(other.rules.get(1))) ||
 	    		   (this.rules.get(1).equals(other.rules.get(0)) && this.rules.get(0).equals(other.rules.get(1))))
 	    		   return true;
 	       return false;
 	   }
 	
 	@Override
 	public String toString() {
 		String ret = "[";
 		ret += "pattern = " + pattern + ", ";
 		ret += "confidenceScore = " + confidenceScore + ", ";
 		ret += "patternType = " + getPatternType() + ", ";
 		ret += "rules = {" + rules.toString();
 		ret += "}]";
 		return ret;
 	}	
 	
 	@Override
 	public int hashCode() {
		int hashCode = this.pattern.toLowerCase().hashCode();
 		for(Rule rule:this.rules) {
 			hashCode += rule.hashCode();
 		}
 		return hashCode;
 	}
 
 	public List<Rule> getRules() {
 		return rules;
 	}
 	
 	public Rule getRules(int ruleNumber) {
 		if(ruleNumber < rules.size())
 			return rules.get(ruleNumber);
 		return null;
 	}
 
 	public void setRules(List<Rule> rules) {
 		this.rules = rules;
 	}
 	
 	public void addRule(Rule rule) {
 		if(rules == null)
 			rules = new ArrayList<SlotPattern.Rule>();
 		rules.add(rule);
 	}
 
 	public String getPattern() {
 		return pattern;
 	}
 
 	public void setPattern(String pattern) {
 		this.pattern = pattern;
 	}
 
 	public double getConfidenceScore() {
 		return confidenceScore;
 	}
 
 	public void setConfidenceScore(double confidenceScore) {
 		this.confidenceScore = confidenceScore;
 	}
 
 	public PatternType getPatternType() {
 		return patternType;
 	}
 
 	public void setPatternType(PatternType patternType) {
 		this.patternType = patternType;
 	}
 }
