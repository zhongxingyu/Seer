 /*
    Copyright 2011 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxi.status;
 
 import java.util.LinkedList;
 
 public class SortedStringList extends LinkedList<String> {
 	private static final long serialVersionUID = 1L;
 
 	public void addString(String str) {
 		int i, l, r;
 		
 		i = -1;
 		l = 0;
 		r = size() - 1;
 		while (r >= l) {
 			String item;
 			int cond;
 
 			i = l + (r - l) / 2;
 			item = get(i);
 			cond = item.compareTo(str);
 			if (cond == 0) {
 				add(i + 1, str);
 				return;
 			} else if (cond > 0) {
 				r = i - 1;
 			} else /* if (cond < 0) */ {
 				l = i + 1;
 			}
 		}
 		
 		add(r + 1, str);
 		return;
 	}
 	
 	public void mergeList(SortedStringList list) {
 		int i1, i2;
 		String s1, s2;
 		
		if (list == null)
			return;
 		i1 = 0;
 		for (i2 = 0; i2 < list.size(); i2++) {
 			s2 = list.get(i2);
 			for (/* nop */; i1 < size(); i1++) {
 				s1 = get(i1);
 				if (s1.compareTo(s2) > 0) {
 					add(i1, s2);
 					i1++;
 					break;
 				}
 			}
 			if (i1 >= size()) {
 				add(s2);
 			}
 		}
 	}
 	
 	public String diffList(SortedStringList list) {
 		int i1, i2;
 		String s1, s2;
 		SortedStringList result; /* This list may not be sorted. */
 		
		if (list == null)
			list = new SortedStringList();
 		result = new SortedStringList();
 		for (i1 = i2 = 0; i1 < size() || i2 < list.size(); /* nop */) {
 			s1 = s2 = null;
 			if (i1 >= size()) {
 				result.add('-' + list.get(i2));
 				i2++;
 				continue;
 			}
 			if (i2 >= list.size()) {
 				result.add('+' + get(i1));
 				i1++;
 				continue;
 			}
 			
 			s1 = get(i1);
 			s2 = list.get(i2);
 			
 			int cond = s1.compareTo(s2);
 			if (cond == 0) {
 				result.add(s1);
 				i1++;
 				i2++;
 			} else if (cond > 0) {
 				result.add('-' + s2);
 				i2++;
 			} else {
 				result.add('+' + s1);
 				i1++;
 			}
 		}
 		return result.toString();
 	}
 
 	public String toString() {
 		StringBuilder sb;
 
 		sb = new StringBuilder();
 		for (int i = 0; i < size(); i++) {
 			sb.append(get(i));
 			sb.append('\n');
 		}
 		return sb.toString();
 	}
 }
