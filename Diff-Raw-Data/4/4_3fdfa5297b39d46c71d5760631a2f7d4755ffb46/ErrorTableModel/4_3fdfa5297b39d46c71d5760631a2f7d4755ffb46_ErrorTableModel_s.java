 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.ErrorListener;
 import com.dmdirc.logger.ErrorManager;
 import com.dmdirc.logger.ErrorReportStatus;
 import com.dmdirc.logger.ProgramError;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 
 /**
  * Table model for displaying program errors.
  */
 public final class ErrorTableModel extends AbstractTableModel implements ErrorListener {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 2;
     /** Data list. */
     private final List<ProgramError> errors;
     /** Are we ready? */
     private boolean ready = false;
 
     /**
      * Creates a new instance of ErrorTableModel.
      */
     public ErrorTableModel() {
         this.errors = Collections.synchronizedList(new ArrayList<ProgramError>());
     }
 
     public void load(final ErrorManager errorManager) {
         errorManager.addErrorListener(this);
         setErrors(errorManager.getErrors());
         ready = true;
     }
 
     /**
      * Sets the list of errors.
      *
      * @param errors List of errors
      */
     public void setErrors(final List<ProgramError> errors) {
         synchronized (this.errors) {
             this.errors.clear();
             this.errors.addAll(errors);
         }
 
         fireTableDataChanged();
     }
 
     @Override
     public int getRowCount() {
        return errors.size();
     }
 
     @Override
     public int getColumnCount() {
         return 5;
     }
 
     @Override
     public String getColumnName(final int columnIndex) {
         switch (columnIndex) {
             case 0:
                 return "ID";
             case 1:
                 return "Count";
             case 2:
                 return "Severity";
             case 3:
                 return "Report Status";
             case 4:
                 return "Message";
             default:
                 throw new IndexOutOfBoundsException(columnIndex + ">= 5");
         }
     }
 
     @Override
     public Class<?> getColumnClass(final int columnIndex) {
         switch (columnIndex) {
             case 0:
                 return Integer.class;
             case 1:
                 return Integer.class;
             case 2:
                 return ErrorLevel.class;
             case 3:
                 return ErrorReportStatus.class;
             case 4:
                 return String.class;
             default:
                 throw new IndexOutOfBoundsException(columnIndex + ">= 5");
         }
     }
 
     @Override
     public boolean isCellEditable(final int rowIndex, final int columnIndex) {
         return false;
     }
 
     @Override
     public Object getValueAt(final int rowIndex, final int columnIndex) {
         synchronized (errors) {
             switch (columnIndex) {
                 case 0:
                     return errors.get(rowIndex).getID();
                 case 1:
                     return errors.get(rowIndex).getCount();
                 case 2:
                     return errors.get(rowIndex).getLevel();
                 case 3:
                     return errors.get(rowIndex).getReportStatus();
                 case 4:
                     return errors.get(rowIndex).getMessage();
                 default:
                     throw new IndexOutOfBoundsException(columnIndex + ">= 5");
             }
         }
     }
 
     @Override
     public void setValueAt(final Object aValue, final int rowIndex,
             final int columnIndex) {
         synchronized (errors) {
             switch (columnIndex) {
                 case 3:
                     if (aValue instanceof ErrorReportStatus) {
                         errors.get(rowIndex).setReportStatus(
                                 (ErrorReportStatus) aValue);
                         break;
                     } else {
                         throw new IllegalArgumentException("Received: " + aValue.getClass()
                                 + ", expecting: " + ErrorReportStatus.class);
                     }
                 default:
                     throw new UnsupportedOperationException("Only editing the "
                             + "status is allowed");
             }
             fireTableCellUpdated(rowIndex, columnIndex);
         }
     }
 
     /**
      * Gets the error at the specified row.
      *
      * @param rowIndex Row to retrieve
      *
      * @return Specified error
      */
     public ProgramError getError(final int rowIndex) {
         synchronized (errors) {
             return errors.get(rowIndex);
         }
     }
 
     /**
      * Returns the index of the specified error or -1 if the error is not found.
      *
      * @param error ProgramError to locate
      *
      * @return Error index or -1 if not found
      */
     public int indexOf(final ProgramError error) {
         synchronized (errors) {
             return errors.indexOf(error);
         }
     }
 
     /**
      * Adds an error to the list.
      *
      * @param error ProgramError to add
      */
     public void addRow(final ProgramError error) {
         synchronized (errors) {
             errors.add(error);
             fireTableRowsInserted(errors.indexOf(error), errors.indexOf(error));
         }
     }
 
     /**
      * Removes a specified row from the list.
      *
      * @param row Row to remove
      */
     public void removeRow(final int row) {
         synchronized (errors) {
             errors.remove(row);
             fireTableRowsDeleted(row, row);
         }
     }
 
     /**
      * Removes a specified error from the list.
      *
      * @param error ProgramError to remove
      */
     public void removeRow(final ProgramError error) {
         synchronized (errors) {
             if (errors.contains(error)) {
                 final int row = errors.indexOf(error);
                 errors.remove(row);
                 fireTableRowsDeleted(row, row);
             }
         }
     }
 
     @Override
     public void errorAdded(final ProgramError error) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 addRow(error);
             }
         });
     }
 
     @Override
     public void errorDeleted(final ProgramError error) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 removeRow(error);
             }
         });
     }
 
     @Override
     public void errorStatusChanged(final ProgramError error) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 synchronized (errors) {
                     final int errorRow = indexOf(error);
                     if (errorRow != -1 && errorRow < getRowCount()) {
                         fireTableRowsUpdated(errorRow, errorRow);
                     }
                 }
             }
         });
     }
 
     @Override
     public boolean isReady() {
         return ready;
     }
 
     /**
      * Disposes of this model, removing any added listeners.
      */
     public void dispose() {
         ErrorManager.getErrorManager().removeErrorListener(this);
         ready = false;
     }
 
 }
