 // Copyright (c) 2004 ScenPro, Inc.
 
// $Header: /share/content/gforge/sentinel/sentinel/src/gov/nih/nci/cadsr/sentinel/tags/FootTag.java,v 1.1 2008-11-07 14:11:10 hebell Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.sentinel.tags;
 
 import gov.nih.nci.cadsr.sentinel.tool.Constants;
 
 import java.io.IOException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.TagSupport;
 import org.apache.struts.util.MessageResources;
 import org.apache.struts.Globals;
 import org.jboss.Version;
 
 /**
  * This is used to place a standard footer on every JSP in the Sentinel Tool
  * interface.
  * 
  * @author Larry Hebel
  */
 
 public class FootTag extends TagSupport
 {
     /**
      * Constructor.
      */
     public FootTag()
     {
     }
 
     /**
      * Output the standard footer
      * 
      * @return EVAL_PAGE to continue processing the JSP.
      */
     public int doEndTag()
     {
         try
         {
             String jboss = Version.getInstance().getMajor() + "." + Version.getInstance().getMinor() + "." + Version.getInstance().getRevision();
             MessageResources msgs = (MessageResources) pageContext
                 .findAttribute(Globals.MESSAGES_KEY);
             JspWriter out = pageContext.getOut();
             out
                 .print("<table class=\"table3\"><colgroup></colgroup><tbody class=\"secttbody\" />\n"
                     + "<tr><td class=\"ncifmenu\"><span style=\"color: #dddddd\">"
                     + msgs.getMessage(Constants._APLVERS).replace(" ", "&nbsp;")
                     + "&nbsp;(" + jboss + "/" + System.getProperty("java.version") + ")"
                     + "</span></td></tr>\n"
                     + "<tr>\n<td class=\"nciftrtable\">\n"
                    + "<a href=\"mailto:ncicb@pop.nci.nih.gov?subject=caDSR%20Sentinel%20Tool\"><span class=\"wdemail\" title=\"Email NCICB Help Desk\">&#42;</span></a>\n"
                     + "<a target=\"_blank\" href=\"http://www.cancer.gov/\"><img border=\"0\" src=\"/cadsrsentinel/images/footer_nci.gif\" alt=\"National Cancer Institute Logo\" title=\"National Cancer Institute\"></a>\n"
                     + "<a target=\"_blank\" href=\"http://www.dhhs.gov/\"><img border=\"0\" src=\"/cadsrsentinel/images/footer_hhs.gif\" alt=\"Department of Health and Human Services Logo\" title=\"Department of Health and Human Services\"></a>\n"
                     + "<a target=\"_blank\" href=\"http://www.nih.gov/\"><img border=\"0\" src=\"/cadsrsentinel/images/footer_nih.gif\" alt=\"National Institutes of Health Logo\" title=\"National Institutes of Health\"></a>\n"
                     + "<a target=\"_blank\" href=\"http://www.usa.gov/\"><img border=\"0\" src=\"/cadsrsentinel/images/footer_usagov.gif\" alt=\"USA.gov\" title=\"USA.gov\"></a>\n"
                     + "</td>\n</tr>\n</table>\n");
         }
         catch (IOException ex)
         {
         }
         return EVAL_PAGE;
     }
 
     private static final long serialVersionUID = -4073456777204188509L;
 }
