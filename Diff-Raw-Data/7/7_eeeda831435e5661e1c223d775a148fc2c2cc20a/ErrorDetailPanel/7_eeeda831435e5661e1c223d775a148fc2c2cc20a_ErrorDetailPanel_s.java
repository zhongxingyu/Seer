 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs.error;
 
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.logger.ErrorListener;
 import com.dmdirc.logger.ErrorManager;
 import com.dmdirc.logger.ProgramError;
 
 import java.util.List;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.text.BadLocationException;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Shows information about an error.
  */
 public final class ErrorDetailPanel extends JPanel implements ErrorListener {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 3;
     /** The error manager to retrieve errors from. */
     private final ErrorManager errorManager;
     /** Error to show. */
     private ProgramError error;
     /** Date field. */
     private JTextField date;
     /** Severity field. */
     private JTextField level;
     /** Report Status field. */
     private JTextField reportStatus;
     /** Details field. */
     private JTextArea details;
     /** Details scrollpane. */
     private JScrollPane scrollPane;
 
     /** Creates a new instance of ErrorDetailPanel. */
     public ErrorDetailPanel(final ErrorManager errorManager) {
         this(errorManager, null);
     }
 
     /**
      * Creates a new instance of ErrorDetailPanel.
      *
      * @param errorManager The error manager to retrieve errors from
      * @param error        Error to be displayed
      */
     public ErrorDetailPanel(final ErrorManager errorManager, final ProgramError error) {
         this.errorManager = errorManager;
         this.error = error;
 
         initComponents();
 
         updateDetails();
 
         layoutComponents();
     }
 
     /**
      * Sets the error used for this panel.
      *
      * @param newError New ProgramError
      */
     public void setError(final ProgramError newError) {
         error = newError;
         updateDetails();
     }
 
     /** Initialises the components. */
     private void initComponents() {
         date = new JTextField();
         level = new JTextField();
         reportStatus = new JTextField();
         details = new JTextArea();
         scrollPane = new JScrollPane(details);
 
         date.setEditable(false);
         level.setEditable(false);
         reportStatus.setEditable(false);
         details.setEditable(false);
         details.setRows(5);
         details.setWrapStyleWord(true);
 
         errorManager.addErrorListener(this);
     }
 
     /** Updates the panels details. */
     private void updateDetails() {
         SwingUtilities.invokeLater(() -> {
             details.setText("");
             if (error == null) {
                 date.setText("");
                 level.setText("");
                 reportStatus.setText("");
 
                 return;
             }
 
            date.setText(error.occurrencesString());
             level.setText(error.getLevel().toString());
             reportStatus.setText(error.getReportStatus().toString());
 
             details.append(error.getMessage() + '\n');
             final List<String> trace = error.getTrace();
             if (!trace.isEmpty()) {
                 details.append("\n");
             }
             for (String traceLine : trace) {
                 details.append(traceLine + '\n');
             }
             try {
                 details.getDocument().remove(details.getDocument().getLength() - 1, 1);
             } catch (BadLocationException ex) {
                 //Ignore
             }
 
             UIUtilities.resetScrollPane(scrollPane);
         });
     }
 
     /** Lays out the components. */
     private void layoutComponents() {
         setLayout(new MigLayout("fill, wrap 2", "[right]rel[grow,fill]", ""));
 
         add(new JLabel("Date: "));
         add(date);
 
         add(new JLabel("Severity: "));
         add(level);
 
         add(new JLabel("Report status: "));
         add(reportStatus);
 
         add(new JLabel("Details: "));
         add(scrollPane, "grow, push");
     }
 
     @Override
     public void errorAdded(final ProgramError error) {
         //Ignore
     }
 
     @Override
     public void errorDeleted(final ProgramError error) {
         //Ignore
     }
 
     @Override
     public void errorStatusChanged(final ProgramError error) {
         if (this.error != null && this.error.equals(error)) {
             reportStatus.setText(error.getReportStatus().toString());
            date.setText(this.error.occurrencesString());
         }
     }
 
     @Override
     public boolean isReady() {
         return isVisible();
     }
 
 }
