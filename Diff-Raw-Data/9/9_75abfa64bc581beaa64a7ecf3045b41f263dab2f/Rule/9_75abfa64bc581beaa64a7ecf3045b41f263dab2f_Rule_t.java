 package vlt.text.textimprover;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Rule {
     public static final int PRIORITY_HIGH = 3;
     public static final int PRIORITY_MEDIUM = 2;
     public static final int PRIORITY_LOW = 1;
     /**
      * Regexp to search for words that this rule can be applied to.
      */
     private String regexp;
     /**
      * First letter after Improved suffix. Like
      * " => " letter "".
      */
     private String outcomeLetter;
     private String throwAwayRegex;
     private int allowedLength;
     private int priority;
     private Pattern pattern;
     private String name;
 
     public Rule() {
	setOutcomeLetter("");
	setThrowAwayRegex("");
     }
 
     public Rule(String regexp, String outcomeLetter, int allowedLength,
 	    String name) {
 	setRegexp(regexp);
 	this.outcomeLetter = outcomeLetter;
 	this.allowedLength = allowedLength;
     }
 
     public Rule(String regexp, String outcomeLetter, int allowedLength,
 	    String name, int priority) {
 	this(regexp, outcomeLetter, allowedLength, name);
 	setPriority(priority);
     }
 
     public boolean testRuleBelonging(String word) {
 	Matcher matcher = pattern.matcher(word);
 	return matcher.matches();
     }
 
     @Override
     public int hashCode() {
 	final int prime = 31;
 	int result = 1;
 	result = prime * result + allowedLength;
 	result = prime * result
 		+ ((outcomeLetter == null) ? 0 : outcomeLetter.hashCode());
 	result = prime * result + ((regexp == null) ? 0 : regexp.hashCode());
 	return result;
     }
 
     @Override
     public boolean equals(Object obj) {
 	if (this == obj)
 	    return true;
 	if (obj == null)
 	    return false;
 	if (getClass() != obj.getClass())
 	    return false;
 	Rule other = (Rule) obj;
 	if (allowedLength != other.allowedLength)
 	    return false;
 	if (outcomeLetter == null) {
 	    if (other.outcomeLetter != null)
 		return false;
 	} else if (!outcomeLetter.equals(other.outcomeLetter))
 	    return false;
 	if (regexp == null) {
 	    if (other.regexp != null)
 		return false;
 	} else if (!regexp.equals(other.regexp))
 	    return false;
 	return true;
     }
 
     public String getOutcomeLetter() {
 	return outcomeLetter;
     }
 
     public void setOutcomeLetter(String outcomeLetter) {
 	this.outcomeLetter = outcomeLetter;
     }
 
     public int getAllowedLength() {
 	return allowedLength;
     }
 
     public void setAllowedLength(int allowedLength) {
 	this.allowedLength = allowedLength;
     }
 
     public String getRegexp() {
 	return regexp;
     }
 
     public void setRegexp(String regexp) {
 	this.regexp = regexp;
 	pattern = Pattern.compile(regexp);
     }
 
     public int getPriority() {
 	return priority;
     }
 
     public void setPriority(int priority) {
 	this.priority = priority;
     }
 
     public String getName() {
 	return name;
     }
 
     public void setName(String name) {
 	this.name = name;
     }
 
     public String getThrowAwayRegex() {
 	return throwAwayRegex;
     }
 
     public void setThrowAwayRegex(String throwAwayRegex) {
 	this.throwAwayRegex = throwAwayRegex;
     }
 
 }
