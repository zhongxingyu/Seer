 package gui.mainapp;
 
 import engine.expressions.parser.ParsingException;
 import engine.expressions.parser.SyntaxError;
 import gui.mainapp.editor.RedLineHighlightPainter;
 
 import javax.swing.*;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Highlighter;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * User: Oleksiy Pylypenko
  * Date: 3/25/13
  * Time: 4:12 PM
  */
 public class SyntaxErrorDisplay {
     private final JLabel messageLabel;
     private final JTextArea equationPad;
     private final Highlighter highlighter;
 
     public SyntaxErrorDisplay(JLabel messageLabel, JTextArea equationPad) {
         this.messageLabel = messageLabel;
         this.equationPad = equationPad;
         this.highlighter = equationPad.getHighlighter();
     }
 
     public void hide() {
         highlighter.removeAllHighlights();
         messageLabel.setVisible(false);
     }
 
     public void show(ParsingException e) {
         if (equationPad.getText().isEmpty()) {
             hide();
         } else {
             highlighter.removeAllHighlights();
             showErrorMassage(e);
             highlightErrors(e);
             equationPad.repaint();
         }
     }
 
     private void showErrorMassage(ParsingException e) {
         String message = composeMessage(e);
         messageLabel.setText(message);
         messageLabel.setVisible(true);
     }
 
     private String composeMessage(ParsingException e) {
         StringBuilder builder = new StringBuilder();
        builder.append("<html>Please correct the expression ");
         Set<String> messages = new TreeSet<String>();
         for (SyntaxError error : e.getErrors()) {
             messages.add(error.getMessage());
         }
         builder.append("<ul> ");
         for (String str : messages) {
             builder.append("<li>" + str + "</li> ");
         }
         builder.append("</ul></body></html>");
         return builder.toString();
     }
 
     private void highlightErrors(ParsingException e) {
         int txtLen = equationPad.getText().length();
         List<SyntaxError> errors = e.getErrors();
         for (SyntaxError err : errors) {
             try {
                 Highlighter highlighter = equationPad.getHighlighter();
                 int idx = err.getStartIndex();
                 if (idx >= txtLen) {
                     idx = txtLen - 1;
                 }
                 highlighter.addHighlight(
                         idx,
                         err.getEndIndex(),
                         new RedLineHighlightPainter());
             } catch (BadLocationException e1) {
                 //skip
             }
         }
     }
 
 }
