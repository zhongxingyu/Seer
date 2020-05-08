 package jamm.webapp;
 
 import javax.servlet.jsp.tagext.BodyTagSupport;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.JspWriter;
 import java.io.IOException;
 
 /**
  * Implements a table row tag
  *
  * @jsp:tag name="tr"
  *          body-content="JSP"
  */
 public class RowTag extends BodyTagSupport
 {
 
     /**
      * @jsp:attribute   required="true" rtexprvalue="true"
      *                  description="Name of index variable"
      */
     public void setIndex(String index)
     {
         mIndex = index;
     }
 
     /**
      * @jsp:attribute   required="true" rtexprvalue="true"
      *                  description="Even color"
      */
     public void setEvenColor(String evenColor)
     {
         mEvenColor = evenColor;
     }
 
     /**
      * @jsp:attribute   required="true" rtexprvalue="true"
      *                  description="Odd color"
      */
     public void setOddColor(String oddColor)
     {
         mOddColor = oddColor;
     }
 
     public int doStartTag()
         throws JspTagException
     {
         return EVAL_BODY_TAG;
     }
 
     public int doEndTag()
         throws JspException
     {
         StringBuffer buffer = new StringBuffer();
 
         // Create a <tr> element based on the parameters
         buffer.append("<tr");
         renderBgColor(buffer);
         buffer.append(">");
 
         // Add Body Content
         if (bodyContent != null)
         {
             buffer.append(bodyContent.getString().trim());
         }
 
         buffer.append("</tr>");
 
         // Render this element to our writer
         JspWriter writer = pageContext.getOut();
         try
         {
             writer.print(buffer.toString());
         }
         catch (IOException e)
         {
             throw new JspException("Exception in RowTag doEndTag():" +
                                    e.toString());
         }
 
         return EVAL_PAGE;
     }
 
     private void renderBgColor(StringBuffer buffer)
     {
         int index = ((Integer) pageContext.getAttribute(mIndex)).intValue();
         // The index is zero based.  Adding 1 makes it one based, and
         // hence "even" and "odd" make more sense.
         index++;
 
         if ((index % 2) == 0)
         {
             buffer.append(" bgcolor=\"").append(mEvenColor).append("\"");
         }
         else
         {
             buffer.append(" bgcolor=\"").append(mOddColor).append("\"");
         }
     }
 
     private String mIndex;
     private String mEvenColor;
     private String mOddColor;
 }
