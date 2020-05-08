 /*
  * Copyright (c) 2003, 2004 Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: 27/09/2004 23:59:10
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.util;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 
 import org.apache.log4j.Logger;
 import org.htmlparser.Attribute;
 import org.htmlparser.Node;
 import org.htmlparser.Tag;
 import org.htmlparser.lexer.Lexer;
 import org.htmlparser.nodes.TextNode;
 
 /**
  * Process text with html and remove possible
  * malicious tags and attributes.
  * 
  * @author Rafael Steil
 * @version $Id: SafeHtml.java,v 1.4 2004/10/11 14:24:31 rafaelsteil Exp $
  */
 public class SafeHtml 
 {
 	private static final Logger logger = Logger.getLogger(SafeHtml.class);
 	private Set welcomeTags;
 	
 	public SafeHtml()
 	{
 		this.welcomeTags = new HashSet();
 		String[] tags = SystemGlobals.getValue(ConfigKeys.HTML_TAGS_WELCOME).toUpperCase().split(",");
 
 		for (int i = 0; i < tags.length; i++) {
 			this.welcomeTags.add(tags[i].trim());
 		}
 	}
 	
 	private String processAllNodes(String contents) throws Exception
 	{
 		StringBuffer sb = new StringBuffer(512);
 		Lexer lexer = new Lexer(contents);
 		Node node;
 		while ((node = lexer.nextNode()) != null) {
 			if (this.isTagWelcome(node)) {
 				sb.append(node.toHtml());
 			}
 			else {
 				sb.append(node.toHtml().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
 			}
 		}
 		
 		return sb.toString();
 	}
 	
 	private boolean isTagWelcome(Node node)
 	{
 		if (node instanceof TextNode) {
 			return true;
 		}
 		
 		Tag tag = (Tag)node;
 		if (!this.welcomeTags.contains(tag.getTagName())) {
 			return false;
 		}
 		
 		this.checkAndValidateAttributes(tag);
 		
 		return true;
 	}
 	
 	private void checkAndValidateAttributes(Tag tag)
 	{
 		Vector newAttributes = new Vector();
 		for (Iterator iter = tag.getAttributesEx().iterator(); iter.hasNext(); ) {
 			Attribute a = (Attribute)iter.next();
 
 			String name = a.getName();
 			if (name != null) {
 				name = name.toLowerCase();
				if (("href".equals(name) || "src".equals(name)) && a.getValue() != null 
						&& a.getValue().toLowerCase().indexOf("javascript:") > -1) {
 					a.setValue("#");
 					newAttributes.add(a);
 				}
 				else if (!name.startsWith("on") && !name.startsWith("style")) {
 					newAttributes.add(a);
 				}
 			}
 			else {
 				newAttributes.add(a);
 			}
 		}
 		
 		tag.setAttributesEx(newAttributes);
 	}
 
 	public static String makeSafe(String contents)
 	{
 		if (contents == null || contents.trim().length() == 0) {
 			return contents;
 		}
 		
 		try {
 			contents = new SafeHtml().processAllNodes(contents);
 		}
 		catch (Exception e) {
 			logger.warn("Problems while parsing the html: " + e);
 			; // we don't care
 		}
 		
 		return contents;
 	}
 }
