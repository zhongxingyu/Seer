 /*******************************************************************************
  * Copyright (c) 2012 eBay Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     eBay Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.parser.comments;
 
 public class VjCommentUtil {
 
 	public static boolean isVjetComment(String comment) {
 		
 		if(!isHTMLTag(comment)){
 			return true;
 		}
 		return false;
 	}
 
 	private static boolean isHTMLTag(String comment) {
 		int startLessThan = comment.indexOf('<');
 		int endGreaterThan = comment.indexOf('>');
 		int semiColon = comment.indexOf(';');
 		if(semiColon==-1){
 			semiColon = comment.length();
 		}
 		if(semiColon >-1 && semiColon<endGreaterThan){
 			return false;
 		}
		int secondLessThanAndSlash = comment.indexOf("</", startLessThan+1);
 		int secondLessThan = comment.indexOf("<", startLessThan+1);
		if(secondLessThanAndSlash != secondLessThan && secondLessThan!=-1 && secondLessThan<semiColon){
 			return false;
 		}
 		if(startLessThan==-1 && endGreaterThan ==-1){
 			return false;
 		}
 		
 		if(startLessThan>-1 && endGreaterThan>-1 && startLessThan<endGreaterThan){
 			if((endGreaterThan - startLessThan) < 100){ // delta has to be less than 10 chars for html tag
 				return true;
 			}
 			
 		}
 		return false;
 	}
 	
 }
