 package org.libtoil.core;
 
 import java.util.Iterator;
 
 public class TemplateTokenizer implements Iterable<TemplateTokenizer.Token> {
 	char[] src;
 
 	TemplateTokenizer(char[] src) {
 		this.src = src;
 	}
 	
 	TemplateTokenizer(String srcString) {
 		this.src = srcString.toCharArray();
 	}
 	
 	public Iterator<Token> iterator() {
 		return new TemplateTokenizerIterator();
 	}
 	
 	public class TemplateTokenizerIterator implements Iterator<Token> {
 		int pos;
 		Token nextToken;
 		
 		public TemplateTokenizerIterator() {
 			this.pos = 0;
 			this.nextToken = null;
 		}
 
 		boolean pump() {
 			if (nextToken != null) {
 				return true;
 			}
 			if (!(pos < src.length)) {
 				return false;
 			}
 			if (src[pos] == '@') {
 				int cur = pos+1;
 				while (cur < src.length && src[cur] != '@' && !Character.isWhitespace(src[cur])) {
 					cur++;
 				}
				if (cur == src.length || src[cur] != '@') { // loop terminated before finding @
					nextToken = new Token("LITERALAT", pos, pos+1);
 					pos += 1;
					return true;
 				} else if (cur - pos == 1) { // found "@@"
					nextToken = new Token("LITERALAT", pos, cur+1);
					pos = cur + 1;
					return true;
				} else {
					nextToken = new Token("COMMAND", pos, cur+1);
 					pos = cur + 1;
 					return true;
 				}
 			} else if (Character.isWhitespace(src[pos])) {
 				int cur = pos+1;
 				while (cur < src.length && Character.isWhitespace(src[cur])) {
 					cur++;
 				}
 				nextToken = new Token("WHITESPACE", pos, cur);
 				pos = cur;
 				return true;
 			} else {
 				int cur = pos+1;
 				while (cur < src.length && src[cur] != '@' && !Character.isWhitespace(src[cur])) {
 					cur++;
 				}
 				nextToken = new Token("TEXTLITERAL", pos, cur);
 				pos = cur;
 				return true;
 			}
 		}
 
 		public boolean hasNext() {
 			return nextToken != null || pump();
 		}
 
 		public Token next() {
 			pump();
 			Token temp = nextToken;
 			nextToken = null;
 			return temp;
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 	public class Token {
 		String type;
 		int start, end;
 		
 		public Token(String type, int start, int end) {
 			this.type = type;
 			this.start = start;
 			this.end = end;
 		}
 		
 		public String toString() {
 			return new String(src, start, end-start);
 		}
 	}
 
 
 }
