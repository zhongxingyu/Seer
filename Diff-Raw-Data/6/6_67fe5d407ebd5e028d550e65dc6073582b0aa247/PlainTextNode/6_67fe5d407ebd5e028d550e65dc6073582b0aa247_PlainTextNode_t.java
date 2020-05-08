 /**
  *
  * nutz - Markdown processor for JVM
  * Copyright (c) 2012, Sandeep Gupta
  * 
  * http://www.sangupta/projects/nutz
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.sangupta.nutz.ast;
 
 import java.util.Map;
 
 /**
  * 
  * @author sangupta
  *
  */
 public class PlainTextNode extends TextNode {
 	
 	private String text;
 	
 	public PlainTextNode(Node parent, String text) {
 		super(parent);
 		this.text = text;
 	}
 	
 	public void appendText(String text) {
 		this.text = this.text + text;
 	}
 	
 	@Override
 	public void write(StringBuilder builder, boolean atRootNode, Map<String, AnchorNode> referenceLinks) {
 		builder.append(this.text);
 	}
 	
 	@Override
 	public String getPlainText() {
 		return this.text;
 	}
 
 	/**
 	 * @return the text
 	 */
 	public String getText() {
 		return text;
 	}
 	
 	@Override
 	public String toString() {
 		return this.text;
 	}
 
 }
