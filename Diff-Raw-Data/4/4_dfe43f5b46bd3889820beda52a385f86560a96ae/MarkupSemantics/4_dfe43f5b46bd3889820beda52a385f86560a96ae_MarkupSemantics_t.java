 package controllers;
 
 class MarkupSemantics extends mouse.runtime.SemanticsBase
 {
 	final static int CUTLINE = 50;
 
 	private String parsed = "";
 
 	public String getParsed() {
 		return parsed;
 	}
 
 	private static String escape(String input) {
 		String out = "";
 		for(char c : input.toCharArray()) {
 			switch(c) {
 			case '<':
 				out += "&lt;";
 				break;
 			case '>':
 				out += "&gt;";
 				break;
 			case '&':
 				out += "&amp;";
 				break;
 			default:
 				out += c;
 			}
 		}
 		return out;
 	}
 
 	void newline() {
 		lhs().put("<br>");
 	}
 
 	void escape() {
 		String out = "";
 		for(int i = 0; i < rhsSize(); i++)
 			out += rhs(i).text();
 		out = escape(out);
 		lhs().put(out);
 	}
 
 	void paragraph() {
 		String out = "<p>";
 		for(int i = 0; i < rhsSize(); i++)
 			out += (String)rhs(i).get();
 		out += "</p>";
 		lhs().put(out);
 		parsed += out;
 	}
 
 	void multispace() {
 		boolean nonbreak = true;
 		String out = "";
 		for(int i = 0; i < rhsSize(); i++) {
 			out += nonbreak ? "&nbsp;" : " ";
 			nonbreak = nonbreak ? false : true;
 		}
 		lhs().put(out);
 	}
 
 	void longword() {
 		String out = "";
 
 		for(int i = 0; i < rhsSize() && i < CUTLINE; i++) {
 			out += escape(rhs(i).text());
 		}
 
 		for(int i = CUTLINE; i < rhsSize() - CUTLINE; i++) {
 			out += escape(rhs(i).text());
 			out += "&shy;";
 		}
 
 		for(int i = rhsSize() - CUTLINE; i > 0 && i < rhsSize(); i++) {
 			out += escape(rhs(i).text());
 		}
 		lhs().put(out);
 	}
 
 	private static String contract(String url) {
 		if(url.length() <= CUTLINE * 2)
 			return url;
 		String out = url.substring(0, CUTLINE / 2);
 		out += "...";
 		out += url.substring(url.length() - CUTLINE / 4, url.length());
 		return out;
 	}
 
 	void link() {
 		String url = "";
 		for(int i = 0; i < rhsSize(); i++)
 			url += rhs(i).text();
		String out = "<a href=\"" + escape(url) + "\">" + escape(contract(url)) + "</a>";
 		lhs().put(out);
 	}
 
 }
