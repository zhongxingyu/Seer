 /*
  * Copyright 2012 Anthony Cassidy
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.github.a2g.core.objectmodel;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 public class SceneObjectCollection {
 	private List<String> theOtids;
 	private List<Short> theOCodes;
 	private ArrayList<SceneObject> list;
 
 	public SceneObjectCollection() {
 		theOtids = new LinkedList<String>();
 		theOCodes = new LinkedList<Short>();
 		list = new ArrayList<SceneObject>();
 	}
 
 	public void add(SceneObject sceneObject) {
 		list.add(sceneObject);
 		Collections.sort(list, new Comparator<SceneObject>() {
 			@Override
 			public int compare(SceneObject o1, SceneObject o2) {
 				return o1.getNumberPrefix() - o2.getNumberPrefix();
 			}
 		});
 
 		theOtids.clear();
 		theOCodes.clear();
 		for (int i = 0; i < list.size(); i++) {
 			theOtids.add(list.get(i).getOtid());
 			theOCodes.add(list.get(i).getOCode());
 		}
 
 	}
 
 	public SceneObject getByIndex(int index) throws NoSuchElementException {
 		if (index == -1)
 			return null;
 		if (index >= list.size())
 			throw new NoSuchElementException();
 		return list.get(index);
 	}
 
 	public SceneObject getByOtid(String otid) {
 		int i = this.theOtids.indexOf(otid);
 		return this.getByIndex(i);
 	}
 
 	public SceneObject getByOCode(Short ocode) {
 		int i = this.theOCodes.indexOf(ocode);
 		return this.getByIndex(i);
 	}
 
 	public int count() {
 		return list.size();
 	}
 
 }
