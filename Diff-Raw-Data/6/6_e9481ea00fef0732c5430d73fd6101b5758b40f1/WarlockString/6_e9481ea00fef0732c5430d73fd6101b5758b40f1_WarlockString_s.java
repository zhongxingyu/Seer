 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.client;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class WarlockString {
 
 	private StringBuffer text = new StringBuffer();
 	private LinkedList<WarlockStringMarker> styles = new LinkedList<WarlockStringMarker>();
 	
 	public WarlockString() {
 	}
 	
 	public WarlockString(CharSequence text) {
 		this.text.append(text);
 	}
 	
 	public WarlockString(String text) {
 		this.text.append(text);
 	}
 	
 	public WarlockString(String text, IWarlockStyle style) {
 		this.text.append(text);
 		addStyle(style);
 	}
 	
 	public String toString() {
 		return text.toString();
 	}
 	
 	public int lastIndexOf(String str) {
 		return this.text.lastIndexOf(str);
 	}
 	
 	public void append(String text) {
 		this.text.append(text);
 	}
 	
 	public void append(WarlockString string) {
 		int charCount = text.length();
 		text.append(string.toString());
 		for(WarlockStringMarker marker : string.getStyles()) {
 			styles.add(marker.copy(charCount));
 		}
 	}
 	
 	public void addStyle(IWarlockStyle style) {
 		WarlockStringMarker newMarker = new WarlockStringMarker(style, 0, text.length());
 		
 		for(WarlockStringMarker curMarker : styles) {
 			newMarker.addMarker(curMarker);
 		}
 		
 		styles.clear();
 		
 		styles.add(newMarker);
 	}
 	
 	public List<WarlockStringMarker> getStyles() {
 		return styles;
 	}
 	
 	public boolean hasStyleNamed(String styleName) {
 		for(WarlockStringMarker marker : styles) {
 			if(marker.hasStyleNamed(styleName))
 				return true;
 		}
 		return false;
 	}
 	
 	public int length() {
 		return text.length();
 	}
 	
 	public void clear() {
 		text.setLength(0);
 		styles.clear();
 	}
 	
 	public WarlockString substring(int start) {
 		return substring(start, text.length());
 	}
 	
 	public void addMarker(WarlockStringMarker marker) {
 		styles.add(marker);
 	}
 	
 	public WarlockString substring(int start, int end) {
 		WarlockString substring = new WarlockString(text.substring(start, end));
 		
 		for(WarlockStringMarker marker : styles) {
			if(marker.getEnd() >= start)
				continue;
			
			substring.addMarker(marker.copy(-start, end - start));
 		}
 		
 		return substring;
 	}
 	
 	public WarlockString[] split(String regex) {
 		return split(regex, 0);
 	}
 	
 	public WarlockString[] split(String regex, int limit) {
 		Pattern p = Pattern.compile(regex);
 		Matcher m = p.matcher(toString());
 		ArrayList<WarlockString> parts = new ArrayList<WarlockString>();
 		int i = 0;
 		int start = 0;
 
 		// if limit is non-positive, there is no limit
 		// always stop with one substring less than the limit,
 		// so we don't go over the limit by adding the remainder
 		while (m.find(start) && (i + 1 < limit || limit <= 0)) {
 			int end = m.start();
 			// make sure that we actually have a substring to add
 			if(end != start) {
 				parts.add(this.substring(start, end));
 				++i;
 			}
 			// set the start of the next substring
 			start = m.end();
 		}
 		
 		// add the remainder of the string if we didn't get to the end
 		if (this.length() - start > 0) {
 			parts.add(this.substring(start));
 		}
 		
 		return parts.toArray(new WarlockString[parts.size()]);
 	}
 }
