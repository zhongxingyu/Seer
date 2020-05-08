 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi.config;
 
 import java.io.InputStream;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 /**
  * Parses the given configuration file
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class ConfigurationParser {
 
 	public enum TokenType {
 		INTERCEPTORS("interceptors"), DEPENDENCIES("dependencies"), PROPERTIES("properties"), CONTROLLERS("controllers"), BOOTLOADERS("bootloaders"), URL(
				"^((/[a-zA-Z_0-9]+)*(/\\{[a-zA-Z_0-9]+\\})*(/[a-zA-Z_0-9]+)*)+/?$"), PROPERTY("(([A-Z]+[0-9]*)+_?([A-Z]+[0-9]*)+)*"), CLASS("(\\w+\\.)+\\w+"), STRING(
 				"\\S+");
 
 		private Pattern pattern;
 
 		TokenType(String strPattern) {
 			this.pattern = Pattern.compile(strPattern);
 		}
 
 		public static TokenType getTokenType(String input) {
 			for (TokenType t : TokenType.values()) {
 				if (t.pattern.matcher(input).matches()) {
 					return t;
 				}
 			}
 			throw new IllegalArgumentException("Input doesn't matches any token type");
 		}
 	}
 
 	private Scanner lineScanner;
 	private Scanner inScanner;
 
 	public ConfigurationParser(InputStream in) {
 		this.inScanner = new Scanner(in);
 	}
 
 	public Token nextToken() {
 		return new Token(this.getFromLine());
 	}
 
 	/**
 	 * @return
 	 */
 	private String getFromLine() {
 		if (this.lineScanner == null || !this.lineScanner.hasNext()) {
 			String line;
 			do {
 				line = this.inScanner.nextLine().trim();
 			} while (line.startsWith("#") || line.isEmpty());
 			this.lineScanner = new Scanner(line);
 		}
 		return this.lineScanner.next();
 	}
 
 	/**
 	 * A configuration token
 	 */
 	class Token {
 		private String content;
 		private TokenType tokenType;
 
 		public Token(String content) {
 			this.content = content;
 			this.tokenType = TokenType.getTokenType(content);
 		}
 
 		/**
 		 * @return the content
 		 */
 		public String getContent() {
 			return this.content;
 		}
 
 		/**
 		 * @return the tokenType
 		 */
 		public TokenType getTokenType() {
 			return this.tokenType;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return String.format("%s - %s", this.tokenType.name(), this.content);
 		}
 	}
 }
