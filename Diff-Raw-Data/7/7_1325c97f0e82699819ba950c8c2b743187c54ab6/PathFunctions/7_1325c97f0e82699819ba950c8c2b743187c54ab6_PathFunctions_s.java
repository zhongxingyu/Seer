 /*
  * Copyright 2012 - Six Dimensions
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package com.sixdimensions.wcm.cq.cqex.functions;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tldgen.annotations.Function;
 
 public class PathFunctions {
 	private static final Logger log = LoggerFactory
 			.getLogger(PathFunctions.class);
 
 	/**
 	 * Converts the specified path into a link based on the following rules:
 	 * <ul>
	 * <li>if the path starts with 'http' use the path as is</li>
 	 * <li>if the path contains '.' use the path as is</li>
 	 * <li>otherwise append '.html'</li>
 	 * </ul>
 	 */
 	@Function(example = "${cqex:toLink(path)}")
 	public static String toLink(final String path) {
 		log.trace("toLink");
 
 		if (path == null) {
 			log.debug("Path is null");
 			return null;
 		}
 
 		log.trace("Converting path: " + path + " to link");
 		String link = path;
 		if (!link.startsWith("http") && !link.startsWith("mailto")
 				&& !link.startsWith("#") && !link.contains(".")) {
 			link += ".html";
 		}
 		return StringEscapeUtils.escapeHtml(link);
 	}
 
 	/**
 	 * Do not invoke the default constructor.
 	 */
 	protected PathFunctions() {
 		// prevents calls from subclass
 		throw new UnsupportedOperationException();
 	}
 }
