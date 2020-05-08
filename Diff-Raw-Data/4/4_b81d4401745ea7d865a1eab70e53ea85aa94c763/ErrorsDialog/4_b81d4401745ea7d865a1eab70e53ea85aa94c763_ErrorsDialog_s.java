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
 
 package com.dmdirc.addons.ui_swing.dialogs.errors;
 
 import com.dmdirc.addons.ui_swing.components.GenericTableModel;
 import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
 import com.dmdirc.addons.ui_swing.injection.MainWindow;
 import com.dmdirc.interfaces.ui.ErrorsDialogModel;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.ProgramError;
 import com.dmdirc.addons.ui_swing.components.IconManager;
 
 import java.awt.Dimension;
 import java.awt.Window;
 
 import javax.inject.Inject;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Dialog listing errors that have occurred in the client.
  */
 public class ErrorsDialog extends StandardDialog {
 
     private static final long serialVersionUID = 3;
     private final ErrorsDialogModel model;
     private final IconManager iconManager;
     private JTable table;
     private GenericTableModel<ProgramError> tableModel;
     private JSplitPane splitPane;
     private JScrollPane tableScrollPane;
     private JTextField date;
     private JTextField severity;
     private JTextField reportStatus;
     private JTextArea details;
     private JButton deleteAll;
     private JButton delete;
     private JButton send;
 
     /**
      * Creates a new instance of StandardDialog.
      *
      * @param owner The frame that owns this dialog
      */
     @Inject
     public ErrorsDialog(@MainWindow final Window owner,
             final ErrorsDialogModel model, final IconManager iconManager) {
         super(owner, ModalityType.MODELESS);
         this.model = model;
         this.iconManager = iconManager;
         initComponents();
         layoutComponents();
         setTitle("Error list");
         setMinimumSize(new Dimension(600, 550));
     }
 
     @Override
     public void display() {
         new ErrorsDialogController(model)
                 .init(this, tableModel, table, date, severity, reportStatus, details, deleteAll,
                         delete, send, getCancelButton());
         super.display();
     }
 
     @Override
     public void dispose() {
         model.unload();
         super.dispose();
     }
 
     private void initComponents() {
         deleteAll = new JButton("Delete All");
         delete = new JButton("Delete");
         send = new JButton("Send");
         date = new JTextField();
         severity = new JTextField();
         reportStatus = new JTextField();
         details = new JTextArea();
         getCancelButton().setText("Close");
         tableModel = new GenericTableModel<>(ProgramError.class,
                 "getLevel", "getReportStatus", "getMessage");
         tableModel.setHeaderNames("Severity", "Report Status", "Message");
         tableScrollPane = new JScrollPane();
         splitPane = getSplitPane();
        table = new JTable(tableModel);
         table.setAutoCreateRowSorter(true);
         table.getModel().addTableModelListener(table);
         tableScrollPane.setViewportView(table);
         table.setPreferredScrollableViewportSize(new Dimension(600, 150));
         tableScrollPane.setMinimumSize(new Dimension(150, 100));
         table.setDefaultRenderer(ErrorLevel.class, new ErrorLevelIconCellRenderer(iconManager));
     }
 
     private JPanel getTopPanel() {
         final JPanel top = new JPanel(new MigLayout("fill"));
         top.add(tableScrollPane, "span 2, grow, push");
         return top;
     }
 
     private JPanel getBottomPanel() {
         final JPanel bottom = new JPanel(new MigLayout("fill, wrap 2"));
         bottom.add(new JLabel("Date: "), "");
         bottom.add(date, "growx, pushx");
         bottom.add(new JLabel("Severity: "), "");
         bottom.add(severity, "growx, pushx");
         bottom.add(new JLabel("Report Status: "), "");
         bottom.add(reportStatus, "growx, pushx");
         bottom.add(new JLabel("Details: "), "");
         bottom.add(new JScrollPane(details), "grow, push");
         return bottom;
     }
 
     private JSplitPane getSplitPane() {
         final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
         splitPane.setTopComponent(getTopPanel());
         splitPane.setBottomComponent(getBottomPanel());
         return splitPane;
     }
 
     private void layoutComponents() {
         setLayout(new MigLayout("fill"));
         add(splitPane, "grow, push, wrap, spanx");
         add(deleteAll, "split 4, sg button, tag left");
         add(delete, "sg button, tag other");
         add(send, "sg button, tag other");
         add(getCancelButton(), "sg button, tag ok");
     }
 }
