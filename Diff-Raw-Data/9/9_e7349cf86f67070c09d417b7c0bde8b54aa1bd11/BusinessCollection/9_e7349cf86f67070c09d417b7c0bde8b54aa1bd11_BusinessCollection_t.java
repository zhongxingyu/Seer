 package org.fnppl.opensdx.common;
 
 /*
  * Copyright (C) 2010-2011 
  * 							fine people e.V. <opensdx@fnppl.org> 
  * 							Henning Thieß <ht@fnppl.org>
  * 
  * 							http://fnppl.org
 */
 
 /*
  * Software license
  *
  * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
  *  
  * This file is part of openSDX
  * openSDX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * openSDX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * and GNU General Public License along with openSDX.
  * If not, see <http://www.gnu.org/licenses/>.
  *      
  */
 
 /*
  * Documentation license
  * 
  * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
  * 
  * This file is part of openSDX.
  * Permission is granted to copy, distribute and/or modify this document 
  * under the terms of the GNU Free Documentation License, Version 1.3 
  * or any later version published by the Free Software Foundation; 
  * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
  * A copy of the license is included in the section entitled "GNU 
  * Free Documentation License" resp. in the file called "FDL.txt".
  * 
  */
 
 import java.util.Vector;
 
 import org.fnppl.opensdx.xml.ChildElementIterator;
 import org.fnppl.opensdx.xml.Element;
 import org.fnppl.opensdx.xml.XMLElementable;
 
 /**
  * 
  * @author Bertram Boedeker <bboedeker@gmx.de>
  * 
  */
 public abstract class BusinessCollection<E> implements XMLElementable {
 	
 	public abstract String getKeyname();
 	private Vector<E> list = new Vector<E>(); 
 	
 	public Element toElement() {
 		Element resultElement = new Element(getKeyname());
 		for (E b : list) {
 			if (b instanceof XMLElementable) {
 				Element e = ((XMLElementable)b).toElement();
 				if (e!=null) {
 					resultElement.addContent(e);
 				}
 			}
 		}
 		return resultElement;
 	}
 
 	public void add(E object) {
 		list.add(object);
 	}
 	
 	public void set(E item) {
 		if (!(item instanceof BusinessItem)) throw new RuntimeException("wrong usage of BusinessCollection.set Method!");
 		String name = ((BusinessItem)item).getKeyname();
 		boolean add = true;
 		for (int i=0;i<list.size() && add;i++) {
			if (list.get(i) instanceof BusinessItem) {
 				if (((BusinessItem)list.get(i)).getKeyname().equals(name)) {
 					list.set(i, item);
 					add = false;
 					break;
 				}
 			}
 		}
 		if (add) {
 			list.add(item);
 		}
 	}
 	
 	public void remove(int index) {
 		if (index<0 || index >= list.size()) return;
 		list.remove(index);
 	}
 	
 	public int indexOf(E object) {
 		return list.indexOf(object);
 	}
 	
 	public void removeAll() {
 		list.removeAllElements();
 	}
 	
 	public E get(int i) {
 		return list.get(i);
 	}
 	
 	public int size() {
 		return list.size();
 	}
 }
