 package com.physion.ovation.gui.ebuilder;
 
 import java.awt.Frame;
 import java.awt.BorderLayout;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.SwingUtilities;
 import javax.swing.BorderFactory;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 
 import com.physion.ovation.gui.ebuilder.datamodel.RowData;
 
 
 /**
  * This is the class that engineers who want to display the
  * Expression Builder GUI should use.
  *
  * The static method:
  *
  *      ExpressionBuilder.editExpression() 
  *
  * is the only method you will need to use.  For example, the
  * code below will display an "empty" GUI for the user to
  * create an expression.  Once the user clicks Ok or Cancel,
  * the code prints the status and the expression tree the user
  * created.
  *
  *      ExpressionBuilder.ReturnValue returnValue;
  *      returnValue = ExpressionBuilder.editExpression(null);
  *
  * The code below will create an expression tree that has
  * some rows already in it and display that in the GUI.
  *
  *      RowData rootRow = RowData.createTestRowData();
  *      returnValue = ExpressionBuilder.editExpression(rootRow);
  * 
  * The returned returnValue.status value tells the caller whether the
  * user pressed the Ok button or canceled out of the window.
  * If returnValue.status is RETURN_STATUS_CANCEL, then
  * returnValue.rootRow is null.
  *
  * Please see the class's main() method to see example code
  * you can use to create and display the GUI.
  */
 public class ExpressionBuilder
     extends JDialog 
     implements ActionListener {
 
 
     public static final int RETURN_STATUS_OK = 0;
     public static final int RETURN_STATUS_CANCEL = 1;
 
     private RowData rootRow = null;
     private int returnStatus = RETURN_STATUS_CANCEL;
     private EBuilderPanel panel;
     private JButton okButton;
     private JButton cancelButton;
 
 
     /**
      * Create an ExpressionBuilder dialog with its expression
      * tree initialized to a copy of the passed in expression.
      *
      * @param originalRootRow - The rootRow of the expression tree
      * you want to edit.  (Please note, the GUI edits a copy
      * of the passed in rootRow.)
      * Do NOT pass null.
      */
     ExpressionBuilder(RowData originalRootRow) {
 
         super((Frame)null);
         setTitle("Physion ooExpression Builder");
         setModal(true);
 
         /**
          * Make a copy of the passed in expression tree so that
          * way the GUI will not affect it.
          */
         this.rootRow = new RowData(originalRootRow);
 
         panel = new EBuilderPanel(rootRow);
         getContentPane().add(panel);
 
         JPanel buttonPanel = new JPanel(new GridBagLayout());
         getContentPane().add(buttonPanel, BorderLayout.SOUTH);
         buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,7,0));
 
         GridBagConstraints gc;
 
         okButton = new JButton("Ok");
         okButton.addActionListener(this);
         gc = new GridBagConstraints();
         gc.gridx = 0;
         gc.weightx = 1;
         gc.anchor = GridBagConstraints.CENTER;
         buttonPanel.add(okButton, gc);
 
         cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(this);
         gc = new GridBagConstraints();
         gc.gridx = 1;
         gc.weightx = 1;
         gc.anchor = GridBagConstraints.CENTER;
         buttonPanel.add(cancelButton, gc);
 
         pack();
         int width = getSize().width;
         int height = getSize().height;
         if (width < 800)
             width = 800;
         height += 200;
         setSize(width, height);
     }
 
 
     /**
      * This returns the reason the window closed.
      * Engineers using the ExpressionBuilder.editExpression() method
      * should not be calling this method.  They should be using the
      * returned ReturnValue object to get this information.
      *
      * @return Returns ExpressionBuilder.RETURN_STATUS_OK if the user
      * pressed the Ok button.  Returns ExpressionBuilder.RETURN_STATUS_CANCEL
      * if the user pressed the Cancel button or closed the window another
      * way.  It also returns ExpressionBuilder.RETURN_STATUS_CANCEL if
      * a null rootRow parameter was passed to the editExpression() method.
      */
     private int getReturnStatus() {
         return(returnStatus);
     }
 
 
     /**
      * This returns the new, (possibly modified), expression tree.
      * Engineers using the ExpressionBuilder.editExpression() method
      * should not be calling this method.  They should be using the
      * returned ReturnValue object to get this information.
      */
     private RowData getRootRow() {
         return(rootRow);
     }
 
 
     /**
      * This is called when the user clicks the Ok or Cancel button.
      */
     public void actionPerformed(ActionEvent e) {
 
         if (e.getSource() == cancelButton) {
             returnStatus = RETURN_STATUS_CANCEL;
             rootRow = null;
             setVisible(false);
         }
         else if (e.getSource() == okButton) {
             returnStatus = RETURN_STATUS_OK;
             panel.print();
             setVisible(false);
         }
     }
 
 
     /**
      * Engineers should call this method to create a new
      * expression tree from scratch.
      * I.e. you are not editing an already existing expression tree.
      *
      * This is simply a convenience function to call
      * editExpression(null).  See that method for more
      * information about the returned ReturnValue.
      *
      * An example of using this method is in this
      * class's main() method.
      */
     public static ReturnValue editExpression() {
         return(editExpression(null));
     }
 
 
     /**
      * This is the method you should use to display the GUI
      * to edit/create an expression.
      *
      * An example of using this method is in this
      * class's main() method.
      *
      * @param rootRow - This is the "root" RowData of the
      * expression you want the GUI to display and edit.
      * Please note, the GUI edits a COPY of this expression tree, so
      * the rootRow parameter you passed in is not ever modified
      * by the GUI.
      * Pass null if you want to create a new expression from scratch.
      *
      * @return Returns an ExpressionBuilder.ReturnValue object that
      * contains the "status" and "rootRow" values that your code
      * should look at.  Please see ExpressionBuilder.ReturnValue
      * for more information.
      */
     public static ReturnValue editExpression(RowData rootRow) {
 
         /**
          * Create a returnValue that is already initialized
          * to the values used if a user cancels out of the window.
          */
         ReturnValue returnValue = new ReturnValue();
 
         if (rootRow == null) {
             rootRow = RowData.createRootRow();
         }
 
         if (rootRow.getRootRow() == null) {
             System.err.println("ERROR: rootRow.getRootRow() = null");
             return(returnValue);
         }
 
         ExpressionBuilder dialog = new ExpressionBuilder(rootRow);
 
         /**
          * Make the dialog visible.  It is modal, so
          * execution along this path stops at this point.
          * Only when the user hits Ok or Cancel will this
          * call to the setVisible() method return.
          */
         dialog.setVisible(true);
 
         returnValue.status = dialog.getReturnStatus();
         returnValue.rootRow = dialog.getRootRow();
         return(returnValue);
     }
 
 
     /**
      * This class holds the return values of the editExpression() method.
      *
      * status - Is set to ExpressionBuilder.RETURN_STATUS_OK if the user
      * pressed the Ok button.  It is set to
      * ExpressionBuilder.RETURN_STATUS_CANCEL if the user pressed the
      * Cancel button or closed the window another way.
      *
      * rootRow - Is set to the new expression tree that was created
      * and edited by the GUI.
      */
     public static class ReturnValue {
         public int status = RETURN_STATUS_CANCEL;
         public RowData rootRow = null;
     }
 
 
     /**
      * A simple example test program to test this class and show
      * how the editExpression() methods are used.
      */
     public static void main(String[] args) {
 
         ExpressionBuilder.ReturnValue returnValue;
 
 
         /**
          * Create a new expression from scratch.
          */
         returnValue = ExpressionBuilder.editExpression();
         if (returnValue.status == ExpressionBuilder.RETURN_STATUS_OK) {
             System.out.println("User pressed OK.");
             System.out.println("rootRow:\n"+returnValue.rootRow);
         }
         else {
             System.out.println("User pressed Cancel or closed the window.");
         }
 
         /**
          * After the user Oks or Cancels out of the window
          * created above, create another window. 
          *
          * This time though, create an expression tree filled with
          * some values and have the window initialized to that
          * tree's value.
          */
         RowData rootRow = RowData.createTestRowData();
         returnValue = ExpressionBuilder.editExpression(rootRow);
         System.out.println("\nstatus = "+returnValue.status);
         System.out.println("Modified rootRow:\n"+returnValue.rootRow);
         System.out.println("Original rootRow:\n"+rootRow);
         System.exit(returnValue.status);
     }
 }
