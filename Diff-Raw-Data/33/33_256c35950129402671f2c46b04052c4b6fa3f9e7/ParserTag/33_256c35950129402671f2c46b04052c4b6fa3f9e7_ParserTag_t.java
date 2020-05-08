 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.parser;
 
 /**
  * A tag parses a specific set of Wiki syntax that has been passed to it.  This
  * interface defines methods that must be implemented by each parser tag.
  */
 public interface ParserTag {
 
 	/**
	 * Interface method used for parsing a token and returning a parsed version of that
	 * token, as well as setting all parser output metadata.
 	 *
 	 * @param parserInput The ParserInput object that contains parser configuration
 	 *  settings.
	 * @param parserDocument The ParserDocument object that holds output metadata and
 	 *  other values.
	 * @param mode The parser mode to use when parsing the token.  The return value
	 *  may be different depending on the parsing mode used.
 	 * @param raw The raw Wiki sytnax that is being parsed.
 	 * @return A parsed version of the raw Wiki syntax.
 	 * @throws Exception Thrown if any parsing error occurs.
 	 */
 	public String parse(ParserInput parserInput, ParserDocument parserDocument, int mode, String raw) throws Exception;
 }
