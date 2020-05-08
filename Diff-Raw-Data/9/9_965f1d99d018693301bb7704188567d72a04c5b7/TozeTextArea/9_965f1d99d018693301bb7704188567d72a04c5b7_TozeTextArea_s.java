 package edu.uwlax.toze.editor;
 
 import edu.uwlax.toze.objectz.TozeToken;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.FocusEvent;
 import java.awt.event.KeyEvent;
 import java.awt.font.TextAttribute;
 import java.io.BufferedReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.text.AttributedString;
 import java.util.ArrayList;
 import java.util.List;
 
 public class TozeTextArea extends JTextArea
 {
     static private final Color ERROR_COLOR = new Color(255, 0, 0);
 
     private boolean ignoresEnter = true;
     private final List<TozeToken> errors = new ArrayList<TozeToken>();
     static private boolean m_anyChanged = false;
     private String m_orig = null;
 
     /**
      * @param s
      */
     public TozeTextArea(String s)
     {
         super(s);
         setFocusable(true);
     }
 
     /**
      * @param ignoresEnter
      */
     public void setIgnoresEnter(boolean ignoresEnter)
     {
         this.ignoresEnter = ignoresEnter;
     }
 
     /**
      * @return
      */
     @Override
     public Dimension getPreferredSize()
     {
         Dimension d = super.getPreferredSize();
 
         /*
          * Make sure that the control has some size even
          * if there is not data otherwise the control
          * would be invisible on the screen when there was
          * no data in the control.
          */
        d.width += 20;
         d.height += 5;
 
         return d;
     }
 
     /**
      * @param e
      */
     @Override
     protected void processKeyEvent(KeyEvent e)
     {
         int c;
         boolean handled = false;
 
         c = e.getKeyChar();
 
         if (m_orig == null)
             {
             m_orig = getText();
             }
 
         if (e.getID() == KeyEvent.KEY_PRESSED)
             {
 
             if (ignoresEnter && (c == 10))
                 {
                 handled = true;
                 }
             else if (c == '\t')
                 {
 
                 if (e.isShiftDown() && !e.isControlDown())
                     {
                     transferFocusBackward();
                     handled = true;
                     }
                 else if (!e.isShiftDown() && !e.isControlDown())
                     {
                     transferFocus();
                     handled = true;
                     }
                 }
             }
 
         if (!handled)
             {
             super.processKeyEvent(e);
             }
 
         if ((e.getID() == KeyEvent.KEY_TYPED))
             {
 
             String t1 = m_orig;
             String t2 = getText();
 
             boolean changed = !m_orig.equals(getText());
 
             if (changed)
                 {
                 m_orig = getText();
                 }
 
             if (!m_anyChanged)
                 {
                 m_anyChanged = changed;
                 }
 
             int pos = this.getCaretPosition();
             TozeChars.TozeMap tozeMap = TozeChars.mapLast(getText(), pos);
 
             if (tozeMap != null)
                 {
                 setText(tozeMap.tozeChar);
                 setCaretPosition(tozeMap.pos);
                 revalidate();
                 }
             }
     }
 
     /**
      * @param e
      */
     @Override
     public void processFocusEvent(FocusEvent e)
     {
         super.processFocusEvent(e);
         int p = getCaretPosition();
         setCaretPosition(p);
     }
 
     /**
      * @param g
      */
     @Override
     public void paint(Graphics g)
     {
         Component parent = this.getParent();
 
         if (this.getParent() == null)
             {
             setBackground(Color.WHITE);
             }
         else
             {
             setBackground(parent.getBackground());
             }
 
         g.setFont(TozeFontMap.getFont());
         setFont(TozeFontMap.getFont());
 
         super.paint(g);
 
         TozeReader r = getReader();
 
         /*
          * Draw the text after the error in red.
          */
 
         if (!errors.isEmpty())
             {
             FontMetrics fm = g.getFontMetrics();
             for (TozeToken error : errors)
                 {
                 String tmp = r.nextLine();
                 while (error.m_lineNum != r.getLineNumber() && tmp != null)
                     {
                     tmp = r.nextLine();
                     }
                 if (tmp == null)
                     {
                     break;
                     }
                 int pos = error.m_pos;
                 if (pos >= tmp.length())
                     {
                     pos = tmp.length() - 1;
                     }
                 if (pos < 0)
                     {
                     pos = 0;
                     }
 
                 if (tmp.substring(pos).length() != 0)
                     {
                     // don't bother if there is nothing to draw
                     AttributedString aString = new AttributedString(tmp.substring(pos));
                     aString.addAttribute(TextAttribute.FONT, fm.getFont());
                     aString.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
                     aString.addAttribute(TextAttribute.FOREGROUND, ERROR_COLOR);
                     aString.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL, 0, tmp.length() - pos);
                     g.drawString(aString.getIterator(),
                                  fm.stringWidth(tmp.substring(0, pos)),
                                  ((fm.getHeight() * (r.getLineNumber() + 1)) - fm.getDescent()));
                     }
                 }
             }
     }
 
     /**
      * @return
      */
     private TozeReader getReader()
     {
         StringReader reader = new StringReader(getText());
         return new TozeReader(reader);
     }
 
     /**
      *
      */
     public void clearErrors()
     {
         errors.clear();
         setToolTipText(null);
     }
 
     /**
      *
      * @param tokens
      */
     public void addErrors(List<TozeToken> tokens)
     {
         errors.addAll(tokens);
         setToolTipText("Syntax error");
     }
 
     /**
      * @return
      */
     public String toString()
     {
         return "TozeTextArea[" + this.getText() + "]";
     }
 
     /**
      *
      */
     public class TozeReader extends BufferedReader
     {
         public int m_num = -1;
         public String m_ret = null;
 
         /**
          * @param r
          */
         public TozeReader(Reader r)
         {
             super(r);
             try
                 {
                 m_ret = readLine();
                 }
             catch (Exception e)
                 {
                 }
         }
 
         /**
          * @return
          */
         public boolean hasMoreLines()
         {
             return m_ret != null;
         }
 
         /**
          * @return
          */
         public String nextLine()
         {
             String str = m_ret;
             m_num++;
 
             try
                 {
                 m_ret = readLine();
                 }
             catch (Exception e)
                 {
                 m_ret = null;
                 }
 
             return str;
         }
 
         public int getLineNumber()
         {
             return m_num;
         }
     }
 }
