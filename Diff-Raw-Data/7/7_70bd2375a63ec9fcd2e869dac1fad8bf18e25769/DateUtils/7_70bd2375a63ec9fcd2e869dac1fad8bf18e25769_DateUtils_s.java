 /*******************************************************************************
  *  Copyright (c) 2011 University Of Moratuwa
  *                                                                      
  * All rights reserved. This program and the accompanying materials     
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at             
  * http://www.eclipse.org/legal/epl-v10.html                            
  *                                                                      
  * Contributors:                                                        
  *    Isuru Udana - UI Integration in the Workbench
  *******************************************************************************/
 package org.eclipse.ecf.salvo.ui.tools;
 
 import org.eclipse.ecf.protocol.nntp.model.IArticle;
 import java.text.DateFormat;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * This class provides utilities to format article dates 
  *
  */
 public class DateUtils {
 	private static DateUtils INSTANCE;
 	
 	/**
 	 * @return an instance of DateUtil
 	 */
 	public static DateUtils instance(){
 		if (INSTANCE==null){
 			INSTANCE=new DateUtils();
 		}
 		return INSTANCE;
 	}
 	
 	/**
 	 * Makes a pleasant readable date like "today 12:15" or "12:15"
 	 * 
 	 * @param article 
 	 * @param date RFC822 Date
 	 * @return a pleasant readable date
 	 */
 	public String getNiceDate(IArticle article, Date date) {
 
 		if (date != null) {
 
 			Date now = new Date();
 
 			Format formatter = new SimpleDateFormat("dd/MM/yy");
 			String today = formatter.format(now);
 			String articleDate = formatter.format(date);
 			
 			String formattedDate = DateFormat.getInstance().format(date); 
 			
 			if (today.equals(articleDate)) {
				return "Today " + formattedDate.split(" ")[1] +" "+ formattedDate.split(" ")[2];
 			}
 			return formattedDate;
 
 		}
 		return article.getDate();
 	}
 }
