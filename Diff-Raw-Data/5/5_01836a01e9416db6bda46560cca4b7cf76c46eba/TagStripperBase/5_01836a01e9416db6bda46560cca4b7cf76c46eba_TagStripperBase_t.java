 /**
  * Copyright 2011-2012 Alexandre Dutra
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 /**
  * 
  */
 package fr.dutra.confluence2wordpress.core.converter.visitors;
 
 import java.util.List;
 
 import org.htmlcleaner.HtmlNode;
 import org.htmlcleaner.TagNode;
 import org.htmlcleaner.TagNodeVisitor;
 
 
 /**
  * @author Alexandre Dutra
  *
  */
 public abstract class TagStripperBase implements TagNodeVisitor {
 
 	protected void stripTag(TagNode parentNode, TagNode tag) {
 		if(tag.hasChildren()) {
 			@SuppressWarnings("unchecked")
 			List<HtmlNode> children = tag.getChildren();
			HtmlNode afterThis = tag;
 			for (HtmlNode child : children) {
			    parentNode.insertChildAfter(afterThis, child);
			    afterThis = child;
 			}
 		}
 		parentNode.removeChild(tag);
 	}
 
 }
