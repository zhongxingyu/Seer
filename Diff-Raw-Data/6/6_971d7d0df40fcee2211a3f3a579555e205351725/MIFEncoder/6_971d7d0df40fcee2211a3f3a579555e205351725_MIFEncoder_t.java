 /*===========================================================================
   Copyright (C) 2008-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.mif;
 
 import java.nio.charset.CharsetEncoder;
 
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.encoder.IEncoder;
 import net.sf.okapi.common.resource.Property;
 
 /**
  * Implements {@link IEncoder} for Adobe FrameMaker MIF format.
  */
 public class MIFEncoder implements IEncoder {
 
 	@Override
 	public String encode (String text,
 		int context)
 	{
 		StringBuilder escaped = new StringBuilder();
 		char ch;
 		for ( int i=0; i<text.length(); i++ ) {
 			ch = text.charAt(i);
 			switch ( text.charAt(i) ) {
 			case '\t':
 				escaped.append("\\t");
 				break;
 			case '>':
 				escaped.append("\\>");
 				break;
 			case '\'':
 				escaped.append("\\q");
 				break;
 			case '`':
 				escaped.append("\\Q");
 				break;
 			case '\\':
 				escaped.append("\\\\");
 				break;
 			default:
 				if ( ch > 127 ) {
 					String res = tryCharStatment(ch);
 					if ( res == null ) escaped.append(ch);
 					else escaped.append(res);
 				}
 				else {
 					escaped.append(ch);
 				}
 				break;
 			}
 		}
 		return escaped.toString();
 	}
 
 	@Override
 	public String encode (char value,
 		int context)
 	{
 		switch ( value ) {
 		case '\t':
 			return "\\t";
 		case '>':
 			return "\\>";
 		case '\'':
 			return "\\q";
 		case '`':
 			return "\\Q";
 		case '\\':
 			return "\\\\";
 		default:
 			if ( value > 127 ) {
 				String res = tryCharStatment(value);
 				if ( res == null ) return String.valueOf(value); //return String.format("\\u%04X", (int)value);
 				else return res;
 			}
 			else {
 				return String.valueOf(value);
 			}
 		}
 	}
 
 	@Override
 	public String encode (int value,
 		int context)
 	{
 		switch ( value ) {
 		case '\t':
 			return "\\t";
 		case '>':
 			return "\\>";
 		case '\'':
 			return "\\q";
 		case '`':
 			return "\\Q";
 		case '\\':
 			return "\\\\";
 		default:
 			//TODO: supplemental chars
 			if ( value > 127 ) {
 				String res = tryCharStatment(value);
 				if ( res == null ) return String.valueOf((char)value); //return String.format("\\u%04X", value);
 				else return res;
 			}
 			else {
 				return String.valueOf((char)value);
 			}
 		}
 	}
 
 	@Override
 	public void setOptions (IParameters params,
 		String encoding,
 		String lineBreak)
 	{
 		// Nothing to do
 	}
 
 	@Override
 	public String toNative (String propertyName,
 		String value)
 	{
 		if ( Property.ENCODING.equals(propertyName) ) {
 			if ( "shift-jis".equals(value) ) return "\u65E5\u672C\u8A9E";
 			//TODO: CJK etc...
 		}
 
 		// PROP_LANGUGE: Not applicable
 
 		// No changes for the other values
 		return value;
 	}
 
 	@Override
 	public String getLineBreak () {
 		return "\n";
 	}
 
 	@Override
 	public CharsetEncoder getCharsetEncoder () {
 		return null;
 	}
 
 	private String tryCharStatment (int value) {
 		String token = "";
 		switch ( value ) {
 		case '\t': token = "Tab"; break;
 		case '\u00a0': token = "HardSpace"; break;
		case '\u2010': token = "SoftHyphen"; break;
		case '\u2011': token = "HardHyphen"; break;
		case '\u00ad': token = "DiscHyphen"; break;
 		case '\u200d': token = "NoHypen"; break;
 		case '\u00a2': token = "Cent"; break;
 		case '\u00a3': token = "Pound"; break;
 		case '\u00a5': token = "Yen"; break;
 		case '\u2013': token = "EnDash"; break;
 		case '\u2014': token = "EmDash"; break;
 		case '\u2020': token = "Dagger"; break;
 		case '\u2021': token = "DoubleDagger"; break;
 		case '\u2022': token = "Bullet"; break;
 		case '\n': token = "HardReturn"; break;
 		case '\u2007': token = "NumberSpace"; break;
 		case '\u2009': token = "ThinSpace"; break;
 		case '\u2002': token = "EnSpace"; break;
 		case '\u2003': token = "EmSpace"; break;
 		default:
 			return null;
 		}
 		return "'><Char " + token + "><String `";
 	}
 }
