 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
 
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
  * This file creation date: 21/05/2004 - 15:33:36
  * net.jforum.view.forum.PostCommon.java
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.view.forum;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.jforum.JForum;
 import net.jforum.SessionFacade;
 import net.jforum.entities.Post;
 import net.jforum.entities.Smilie;
 import net.jforum.repository.BBCodeRepository;
 import net.jforum.repository.SmiliesRepository;
 import net.jforum.util.bbcode.BBCode;
 
 /**
  * @author Rafael Steil
 * @version $Id: PostCommon.java,v 1.2 2004/07/24 15:47:53 jamesyong Exp $
  */
 public class PostCommon
 {
 	public static Post preparePostText(Post p)
 	{
 		if (!p.isHtmlEnabled()) {
 			p.setText(p.getText().replaceAll("<", "&lt;"));
 		}
 		
 		// First of all, convert new lines to <br>'s
 		p.setText(p.getText().replaceAll("\n", "<br>"));
 		
 		// Then, search for bb codes
 		if (p.isBbCodeEnabled()) {
 			if (p.getText().indexOf('[') > -1 && p.getText().indexOf(']') > -1) {
 				int openQuotes = 0;
 				Iterator tmpIter = BBCodeRepository.getBBCollection().getBbList().iterator();
 				
 				while (tmpIter.hasNext()) {
 					BBCode bb = (BBCode)tmpIter.next();
 					
 					// little hacks
 					if (bb.removeQuotes()) {
 						Matcher matcher = Pattern.compile(bb.getRegex()).matcher(p.getText());
 						
 						while (matcher.find()) {
 							String contents = matcher.group(1);
 							contents = contents.replaceAll("'", "");
 							contents = contents.replaceAll("\"", "");
 							
 							String replace = bb.getReplace().replaceAll("\\$1", contents);
 							
 							p.setText(p.getText().replaceFirst(bb.getRegex(), replace));
 						}
 					}
 					else {
 						// Another hack for the quotes
 						if (bb.getTagName().equals("openQuote")) {
 							Matcher matcher = Pattern.compile(bb.getRegex()).matcher(p.getText());								
 							
 							while (matcher.find()) {
 								openQuotes++;
 								
 								p.setText(p.getText().replaceFirst(bb.getRegex(), bb.getReplace()));
 							}
 						}
 						else if (bb.getTagName().equals("closeQuote")) {
 							if (openQuotes > 0) {
 								Matcher matcher = Pattern.compile(bb.getRegex()).matcher(p.getText());
 								
 								while (matcher.find()) {
 									openQuotes--;
 									
 									p.setText(p.getText().replaceFirst(bb.getRegex(), bb.getReplace()));
 								}
 							}
 						}
 						else if (bb.getTagName().equals("code")) {
 							Matcher matcher = Pattern.compile(bb.getRegex()).matcher(p.getText());
 							StringBuffer sb = new StringBuffer(p.getText());
 							
 							while (matcher.find()) {
 								String contents = matcher.group(1);
 								
 								StringBuffer replace = new StringBuffer(bb.getReplace());
 								int index = replace.indexOf("$1");
 								if (index > -1) {
 									replace.replace(index, index + 2, contents);
 								}
 								
 								index = sb.indexOf("[code]");
 								int lastIndex = sb.indexOf("[/code]") + "[/code]".length();
 								
 								sb.replace(index, lastIndex, replace.toString());
 								p.setText(sb.toString());
 							}
 						}
 						else {
 							p.setText(p.getText().replaceAll(bb.getRegex(), bb.getReplace()));
 						}
 					}
 					
 				}
 				
 				if (openQuotes > 0) {
 					BBCode closeQuote = BBCodeRepository.findByName("closeQuote");
 					
 					// I'll not check for nulls ( but I should )
 					for (int i = 0; i < openQuotes; i++) {
 						p.setText(p.getText() + closeQuote.getReplace());
 					}
 				}
 			}
 		}
 		
 		// Smilies...
 		if (p.isSmiliesEnabled()) {
 			p.setText(processSmilies(p.getText(), SmiliesRepository.getSmilies()));
 		}
 		
 		return p;
 	}
 	
 	public static String processSmilies(String text, ArrayList smilies)
 	{
 		Iterator iter = smilies.iterator();
 		while (iter.hasNext()) {
 			Smilie s = (Smilie)iter.next();
 			
 			int index = text.indexOf(s.getCode());
 			if (index > -1) {
 				text = text.replaceAll("\\Q"+ s.getCode() +"\\E", s.getUrl());
 			}
 		}
 		
 		return text;
 	}
 	
 	public static Post fillPostFromRequest()
 	{
 		Post p = new Post();
 		p.setText(JForum.getRequest().getParameter("message"));
 		p.setSubject(JForum.getRequest().getParameter("subject"));
 		p.setBbCodeEnabled(JForum.getRequest().getParameter("disable_bbcode") != null ?  false : true);
 		p.setHtmlEnabled(JForum.getRequest().getParameter("disable_html") != null ?  false : true);
 		p.setSmiliesEnabled(JForum.getRequest().getParameter("disable_smilies") != null ?  false : true);
 		p.setSignatureEnabled(JForum.getRequest().getParameter("attach_sig") != null ? true : false);
 		p.setTime(new GregorianCalendar().getTimeInMillis());
 		p.setUserId(SessionFacade.getUserSession().getUserId());
		p.setUserIp(JForum.getRequest().getRemoteAddr());
 		
 		return p;
 	}
 }
