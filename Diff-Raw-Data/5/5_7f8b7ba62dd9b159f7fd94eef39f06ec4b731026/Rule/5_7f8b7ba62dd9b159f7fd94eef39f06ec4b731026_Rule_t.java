 /**
  *   Rule
  *   Copyright(c) 2011 Sergio Gabriel Teves
  * 
  *   This file is part of UrlResolver.
  *
  *   UrlResolver is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   UrlResolver is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with UrlResolver. If not, see <http://www.gnu.org/licenses/>.
  */
 package ar.sgt.resolver.rule;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.code.regexp.NamedMatcher;
 import com.google.code.regexp.NamedPattern;
 
 /**
  * @author gabriel
  *
  */
 public final class Rule {
 
 	private String processor;
 	private String pattern;
 	private String redirect;
 	private String name;
 	
 	private Map<String, String> args;
 	
 	private NamedMatcher matcher;
 	
 	/**
 	 * @param processor
 	 * @param path
 	 * @param type
 	 */
 	public Rule(String name, String processor, String pattern, String redirect) {
 		this.name = name;
 		this.processor = processor;
 		this.pattern = pattern;
 		this.redirect = redirect;
 		this.args = new HashMap<String, String>();
 	}
 
 	public void addArgument(String name, String value) {
 		args.put(name, value);
 	}
 
 	public boolean match(String path) {
 		NamedPattern p = NamedPattern.compile(this.pattern);
 		this.matcher = p.matcher(path);
 		return this.matcher.matches();
 	}
 
 	public Map<String, String> parseParams() {
 		Map<String, String> map = new HashMap<String, String>();
		map.putAll(this.args);
 		if (this.matcher == null) return map;
		map.putAll(this.matcher.namedGroups());
		return map;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder("<rule>\n");
 		builder.append("<processor>").append(this.processor).append("</processor>\n");
 		builder.append("<pattern>").append(this.pattern).append("</pattern>\n");
 		if (this.redirect != null) builder.append("<redirect>").append(this.redirect).append("</redirect>\n");
 		builder.append("</rule>\n");
 		return builder.toString();
 	}
 
 	public String getProcessor() {
 		return processor;
 	}
 
 	public String getPattern() {
 		return pattern;
 	}
 
 	public String getRedirect() {
 		return redirect;
 	}
 
 	public String getName() {
 		return name;
 	}	
 	
 	public Map<String, String> getArguments() {
 		return this.args;
 	}
 }
