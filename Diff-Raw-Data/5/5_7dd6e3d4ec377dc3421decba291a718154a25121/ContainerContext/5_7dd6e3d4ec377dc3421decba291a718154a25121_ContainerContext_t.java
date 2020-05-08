 package kkckkc.syntaxpane.parse.grammar;
 
 import kkckkc.syntaxpane.model.Scope;
 import kkckkc.syntaxpane.parse.grammar.SubPatternContext.Where;
 import kkckkc.syntaxpane.regex.Matcher;
 import kkckkc.syntaxpane.regex.Pattern;
 import kkckkc.syntaxpane.regex.PatternFactory;
 import kkckkc.utils.StringUtils;
 
 import java.util.Map;
 
 public class ContainerContext extends MatchableContext {
 	protected Pattern beginPattern;
 	protected Pattern endPattern;
 	
 	protected boolean styleInside;
 	protected boolean endAtLineEnd;
 
 	protected String previousEndPattern = "";
 	private PatternFactory factory;
     private boolean disabled;
 
     private boolean contentNameContext;
     private String endPatternExpression;
 
     public ContainerContext(PatternFactory factory) {
 		this.factory = factory;
 		this.beginPattern = factory.create("$^");
 	}
 
 	public ContainerContext(PatternFactory factory, String id) {
 		this.id = id;
 		this.factory = factory;
 		this.beginPattern = factory.create("$^");
 	}
 
 	public void setBegin(Pattern begin) {
 		this.beginPattern = begin;
 	}
 	
 	public void setEnd(Pattern end) {
 		this.endPattern = end;
         this.endPatternExpression = end.pattern();
 	}
 
 	public boolean isStyleInside() {
 		return styleInside;
 	}
 
 	public void setStyleInside(boolean styleInside) {
 		this.styleInside = styleInside;
 	}
 	
 	public boolean isEndAtLineEnd() {
 		return endAtLineEnd;
 	}
 
 	public void setEndAtLineEnd(boolean endAtLineEnd) {
 		this.endAtLineEnd = endAtLineEnd;
 	}
 
 	
 	public Scope createScope(Scope parent, Matcher matcher) {
 		Scope s;
 		if (isStyleInside()) {
 			s = new Scope(matcher.end(), matcher.end(), this, parent);
 		} else {
 			s = new Scope(matcher.start(), matcher.end(), this, parent);
 		}
 
 		if (endPattern != null && !contentNameContext && endPatternExpression.indexOf("@start") >= 0) {
 			for (int i = 0; i < matcher.groupCount(); i++) {
 				s.addAttribute(i + "@start", matcher.group(i));
 			}
 		}
 		
 		buildSubPatternScopes(s, matcher, Where.START);
 		return s;
 	}
 	
 	public int close(Scope scope, Matcher matcher) {
 		if (isStyleInside()) {
 			scope.close(matcher.start());
 			buildSubPatternScopes(scope, matcher, Where.END);
 			return matcher.start();
 		} else {
 			scope.close(matcher.end());
 			buildSubPatternScopes(scope, matcher, Where.END);
 			return matcher.end();
 		}
 	}
 
 	public Matcher getEndMatcher(CharSequence segment, Scope scope) {
		if (endPattern != null && endPatternExpression.indexOf("@start") >= 0) {
 			String p = endPatternExpression;
             if (contentNameContext) {
                 for (Map.Entry<String, String> entry : scope.getParent().getAttributes().entrySet()) {
                     p = StringUtils.replace(p, "\\%{" + entry.getKey() + "}", entry.getValue());
                 }
            } else if (scope.getAttributes() != null) {
                 for (Map.Entry<String, String> entry : scope.getAttributes().entrySet()) {
                     p = StringUtils.replace(p, "\\%{" + entry.getKey() + "}", entry.getValue());
                 }
             }
 			
 			if (! previousEndPattern.equals(p)) {
 				previousEndPattern = p;
 				endPattern = factory.create(p);
 			}
 		}		
 		
 		return endPattern.matcher(segment);
 	}
 
 	public Matcher getMatcher(CharSequence s) {
         if (disabled) {
             return getDisabledMatcher(s);
         } else {
 		    return beginPattern.matcher(s);
         }
 	}
 
     private Matcher getDisabledMatcher(CharSequence s) {
         return factory.create("a$b").matcher(s);
     }
 
     public void setDisabled(boolean b) {
         this.disabled = b;
     }
 
     public void setContentNameContext(boolean b) {
         this.contentNameContext = b;
     }
 }
