 package org.sankozi.rogueland.gui;
 
 import java.awt.Font;
 import javax.swing.JTextPane;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.html.HTMLDocument;
 import org.sankozi.rogueland.resources.ResourceProvider;
 
 /**
  *
  * @author sankozi
  */
 public class DescriptionTextArea extends JTextPane {
    Font font = ResourceProvider.getFont(Constants.STANDARD_FONT_NAME, 14f);
    
     {
         setEditable(false);
         setContentType("text/html");
         setDocument(new CustomFontStyledDocument());
     }
 
    private class CustomFontStyledDocument extends HTMLDocument{
         @Override
         public Font getFont(AttributeSet attr) {
             return ResourceProvider.getFont(Constants.STANDARD_FONT_NAME, StyleConstants.getFontSize(attr));
         }
     }
 }
