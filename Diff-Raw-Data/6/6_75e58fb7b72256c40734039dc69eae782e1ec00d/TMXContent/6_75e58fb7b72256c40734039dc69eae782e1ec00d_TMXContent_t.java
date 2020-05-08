 /*===========================================================================
   Copyright (C) 2008 by the Okapi Framework contributors
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
 ============================================================================*/
 
 package net.sf.okapi.applications.rainbow.lib;
 
 import java.util.List;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.TextFragment;
 
 /**
  * Handles the conversion between a abstract content (IContainer)
  * and TMX notation.
  */
 public class TMXContent {
 
 	private String codedText;
 	private List<Code> codes;
 	private boolean withTradosWorkarounds = false;
 	
 	public TMXContent () {
 		codedText = "";
 	}
 	
 	public TMXContent (TextFragment content) {
 		setContent(content);
 	}
 	
 	public void setTradosWorkarounds (boolean value) {
 		withTradosWorkarounds = value;
 	}
 	
 	public TMXContent setContent (TextFragment content) {
 		codedText = content.getCodedText();
 		codes = content.getCodes();
 		return this;
 	}
 	
 	@Override
 	public String toString () {
 		return toString(1, true);
 	}
 
 	/**
 	 * Generates a TMX string from the content.
 	 * @param quoteMode 0=no quote escaped, 1=apos and quot, 2=#39 and quot,
 	 * and 3=quot only.
 	 * @param escapeGT True to always escape '>' to gt.
 	 * @return
 	 */
 	public String toString (int quoteMode,
 		boolean escapeGT)
 	{
 		StringBuilder tmp = new StringBuilder();
 		int index;
 		int id;
 		Code code;
 		for ( int i=0; i<codedText.length(); i++ ) {
 			//TODO: output attribute 'type' whenever possible
 			switch ( codedText.codePointAt(i) ) {
 			case TextFragment.MARKER_OPENING:
 				index = TextFragment.toIndex(codedText.charAt(++i));
 				code = codes.get(index);
 				if ( code.hasData() ) {
 					tmp.append(String.format("<bpt i=\"%d\">", code.getId()));
 					tmp.append(Util.escapeToXML(codes.get(index).toString(), quoteMode, escapeGT, null));
 					tmp.append("</bpt>");
 				}
 				if ( code.hasAnnotation("protected") ) {
 					tmp.append("<hi type=\"protected\">");
 				}
 				break;
 			case TextFragment.MARKER_CLOSING:
 				index = TextFragment.toIndex(codedText.charAt(++i));
 				code = codes.get(index);
 				if ( code.hasData() ) {
 					tmp.append(String.format("<ept i=\"%d\">", code.getId()));
 					tmp.append(Util.escapeToXML(codes.get(index).toString(), quoteMode, escapeGT, null));
 					tmp.append("</ept>");
 				}
 				if ( code.hasAnnotation("protected") ) {
 					tmp.append("</hi>");
 				}
 				break;
 			case TextFragment.MARKER_ISOLATED:
 			case TextFragment.MARKER_SEGMENT:
 				index = TextFragment.toIndex(codedText.charAt(++i));
 				code = codes.get(index);
 				id = code.getId();
 				// Use <ph> or <it> depending on underlying tagType
 				switch ( code.getTagType() ) {
 				case PLACEHOLDER:
					if ( withTradosWorkarounds
						&& ((code.getData().indexOf('{') != -1 )
							|| (code.getData().indexOf('}') != -1 )
							|| (code.getData().indexOf('\\') != -1 )))
					{
 						tmp.append("<ut>{\\cs6\\f1\\cf6\\lang1024 </ut>");
 						tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
 						tmp.append("<ut>}</ut>");
 					}
 					else {
 						tmp.append(String.format("<ph x=\"%d\">", id));
 						tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
 						tmp.append("</ph>");
 					}
 					break;
 				case OPENING:
 					tmp.append(String.format("<it x=\"%d\" pos=\"begin\">", id));
 					tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
 					tmp.append("</it>");
 					break;
 				case CLOSING:
 					tmp.append(String.format("<it x=\"%d\" pos=\"end\">", id));
 					tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
 					tmp.append("</it>");
 					break;
 				case SEGMENTHOLDER: // Should not really be used
 					tmp.append(String.format("<ph x=\"%d\">", id));
 					tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
 					tmp.append("</ph>");
 					break;
 				}
 				
 				break;
 			case '>':
 				if ( escapeGT ) tmp.append("&gt;");
 				else {
 					if (( i > 0 ) && ( codedText.charAt(i-1) == ']' )) 
 						tmp.append("&gt;");
 					else
 						tmp.append('>');
 				}
 				break;
 			case '<':
 				tmp.append("&lt;");
 				break;
 			case '&':
 				tmp.append("&amp;");
 				break;
 			case '"':
 				if ( quoteMode > 0 ) tmp.append("&quot;");
 				else tmp.append('"');
 				break;
 			case '\'':
 				switch ( quoteMode ) {
 				case 1:
 					tmp.append("&apos;");
 					break;
 				case 2:
 					tmp.append("&#39;");
 					break;
 				default:
 					tmp.append(codedText.charAt(i));
 					break;
 				}
 				break;
 			default:
 				tmp.append(codedText.charAt(i));
 				break;
 			}
 		}
 		return tmp.toString();
 	}
 }
