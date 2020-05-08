 /*
  * mongobrowser - a webstart gui application for viewing, 
  *                editing and administering a Mongo Database
  * Copyright 2009-2011 MeBigFatGuy.com
  * Copyright 2009-2011 Dave Brosius
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0 
  *    
  * Unless required by applicable law or agreed to in writing, 
  * software distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and limitations 
  * under the License. 
  */
 package com.mebigfatguy.mongobrowser.model;
 
import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
public class IndexFieldList implements Iterable<IndexField>, Serializable {
 
	private static final long serialVersionUID = -2452588491740324146L;
 	private final List<IndexField> indexFields = new ArrayList<IndexField>();
 
 	public IndexFieldList() {
 	}
 
 	public IndexFieldList(Collection<IndexField> fields) {
 		indexFields.addAll(fields);
 	}
 
 	public void add(String fieldName, boolean ascending) {
 		IndexField indexField = new IndexField(fieldName, ascending);
 		int index = indexFields.indexOf(indexField);
 		if (index < 0) {
 			indexFields.add(indexField);
 		} else {
 			indexFields.set(index, indexField);
 		}
 	}
 
 	@Override
 	public Iterator<IndexField> iterator() {
 		return indexFields.iterator();
 	}
 
 	public int size() {
 		return indexFields.size();
 	}
 
 	public IndexField get(int index) {
 		return indexFields.get(index);
 	}
 }
