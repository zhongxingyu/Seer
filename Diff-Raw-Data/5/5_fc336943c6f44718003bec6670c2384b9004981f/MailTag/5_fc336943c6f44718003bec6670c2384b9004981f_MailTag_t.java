 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
 package org.infoglue.deliver.taglib.common;
 
 import javax.servlet.jsp.JspException;
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.util.CmsContextListener;
 import org.infoglue.cms.util.mail.MailServiceFactory;
 import org.infoglue.deliver.taglib.TemplateControllerTag;
 
 /**
  * A new simple MailTag to replace in part the apache commons mail-taglib which don't report errors.		
  */
 
 public class MailTag extends TemplateControllerTag 
 {
     private final static Logger logger = Logger.getLogger(MailTag.class.getName());
 
 	private static final long serialVersionUID = 4050206323348354355L;
 	
 	private String from;
 	private String to;
 	private String recipients;
 	private String subject;
 	private String type;
 	private String charset;
 	private String message;
 	
     public MailTag()
     {
         super();
     }
 
 	public int doEndTag() throws JspException
     {
 		try
         {
 			if(type == null)
 				type = "text/html";
 			if(charset == null)
 				charset = "utf-8";
 						
 			MailServiceFactory.getService().sendEmail(type, from, to, recipients, subject, message, charset);
 			setResultAttribute(true);
         } 
 		catch (Exception e)
         {
 			logger.error("Problem sending mail:" + e.getMessage());
			logger.error("	from:" + from);
			logger.error("	to:" + to);
			logger.error("	recipients:" + recipients);
			logger.error("	Subject:" + subject);
			logger.error("	message:" + message);
 			setResultAttribute(false);
 			pageContext.setAttribute("commonMailTagException", e);
         }
 		
 		type = null;
 		charset = null;
 		
         return EVAL_PAGE;
     }
 
 	public void setFrom(String from) throws JspException
 	{
 		this.from = evaluateString("MailTag", "from", from);
 	}
 
 	public void setTo(String to) throws JspException
 	{
 		this.to = evaluateString("MailTag", "valtoue", to);
 	}
 
 	public void setRecipients(String recipients) throws JspException
 	{
 		this.recipients = evaluateString("MailTag", "recipients", recipients);
 	}
 
 	public void setSubject(String subject) throws JspException
 	{
 		this.subject = evaluateString("MailTag", "subject", subject);
 	}
 
 	public void setType(String type) throws JspException
 	{
 		this.type = evaluateString("MailTag", "type", type);
 	}
 
 	public void setCharset(String charset) throws JspException
 	{
 		this.charset = evaluateString("MailTag", "charset", charset);
 	}
 
 	public void setMessage(String message) throws JspException
 	{
 		this.message = evaluateString("MailTag", "message", message);
 	}
 }
