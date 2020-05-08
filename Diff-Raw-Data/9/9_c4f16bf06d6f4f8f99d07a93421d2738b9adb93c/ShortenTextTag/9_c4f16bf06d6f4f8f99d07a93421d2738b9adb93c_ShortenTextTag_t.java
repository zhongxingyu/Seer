 /**
 * $Id: ShortenTextTag.java,v 1.4 2005-12-29 19:42:45 georgeda Exp $
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.3  2005/12/02 14:17:30  georgeda
 * Defect #241, handle truncated HTML tags
 *
  * Revision 1.2  2005/11/28 17:56:39  georgeda
  * Defect #193. 30 chars too big as default
  *
  * Revision 1.1  2005/11/28 13:52:03  georgeda
  * Defect #193.  Initial revision
  *
  * 
  */
 package gov.nih.nci.camod.webapp.taglib;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.jsp.*;
 import javax.servlet.jsp.tagext.*;
 
 /**
  * Custom tag for handling text in the left menu bar
  */
 public class ShortenTextTag implements BodyTag, Serializable {
 
     private static final long serialVersionUID = 5618297483211863400L;
 
     private BodyContent myBodyContent;
     private Tag myParent = null;
    private int myLength = 28;
 
     private final int NOT_STARTED = 99999999;
 
     public void setPageContext(PageContext inPageContext) {
     }
 
     public void setParent(Tag inParent) {
         myParent = inParent;
     }
 
     public Tag getParent() {
         return myParent;
     }
 
     /**
      * Sets the length attribute. This is included in the tld file.
      * 
      * @jsp.attribute description="The length attribute is used to determine how
      *                much of the text to display"
      * 
      * required="false"
      * 
      * rtexprvalue="false"
      */
     public void setLength(String inLength) {
         try {
             myLength = Integer.parseInt(inLength);
         } catch (Exception e) {
             // ignore
         }
     }
 
     public void setBodyContent(BodyContent inBodyContent) {
         myBodyContent = inBodyContent;
     }
 
     public int doStartTag() throws JspException {
         return EVAL_BODY_BUFFERED;
     }
 
     public int doAfterBody() throws JspTagException {
 
         try {
             // Make sure we put anything in the output stream in the
             // body to the output stream of the JSP
             JspWriter out = myBodyContent.getEnclosingWriter();
             String theBody = myBodyContent.getString();
             if (theBody.length() > myLength) {
 
                 try {
                     String theSubBody = theBody.substring(0, myLength);
 
                     // Remove any dangling tags
                     int theLastStartTag = theSubBody.lastIndexOf("<");
                     int theLastEndTag = theSubBody.lastIndexOf(">");
 
                     // Unterminated tag
                     if (theLastStartTag > theLastEndTag) {
                         theSubBody = theSubBody.substring(0, theLastStartTag);
                     }
 
                     // Started HTML tags
                     if (theSubBody.indexOf("<") != -1 && theSubBody.indexOf(">") != -1) {
 
                         // Keep a list of the started tags
                         List theStartedTags = new ArrayList();
 
                         int theTagStart = NOT_STARTED;
 
                         // Find the start tags
                         for (int i = 0; i < theSubBody.length() - 1; i++) {
 
                             // Start of an html tag
                             if (theSubBody.charAt(i) == '<' && theSubBody.charAt(i + 1) != '/') {
                                 theTagStart = i + 1;
                             }
 
                             // End of an html tag
                             else if (theSubBody.charAt(i) == '>') {
 
                                 // We only care about the > if we've started a
                                 // tag
                                 if (i > theTagStart) {
 
                                     // Add the tag text
                                     theStartedTags.add(theSubBody.substring(theTagStart, i));
                                     theTagStart = NOT_STARTED;
                                 }
                             }
                         }
 
                         // Find the end tags
                         theTagStart = NOT_STARTED;
                         for (int i = 0; i < theSubBody.length() - 1; i++) {
                             if (theSubBody.charAt(i) == '<') {
                                 if (theSubBody.charAt(i + 1) == '/') {
                                     theTagStart = i + 2;
                                 }
                             } else if (theSubBody.charAt(i) == '>') {
 
                                 if (i > theTagStart) {
                                     String theTag = theSubBody.substring(theTagStart, i);
                                     if (theStartedTags.contains(theTag)) {
                                         theStartedTags.remove(theTag);
                                     }
 
                                     theTagStart = NOT_STARTED;
                                 }
                             }
                         }
 
                         for (int i = 0; i < theStartedTags.size(); i++) {
                             theSubBody += "</" + theStartedTags.get(i) + ">";
                         }
 
                     }
                     String theAnchor = "<span onMouseOver=\"stm(['', '"
                             + theBody
                             + "'],['white','black','#475b82','#E3E3EB','','','','','','','center','','','',200,'',2,2,10,10,'','','','',''])\" onMouseOut=\"htm();\">";
                     theBody = theAnchor + theSubBody + "..." + "</span>";
 
                 }
                 // Do our best, but don't cause the page to crash if something
                 // doesn't work.
                 catch (Exception e) {
                     out.println(theBody);
                 }
             }
             out.println(theBody);
         } catch (IOException ioe) {
             throw new JspTagException("Error in ShortenTextTag tag doAfterBody " + ioe);
         }
 
         return SKIP_BODY;
     }
 
     public int doEndTag() throws JspException {
         return EVAL_PAGE;
     }
 
     public void doInitBody() {
     }
 
     public void release() {
         myParent = null;
     }
 }
