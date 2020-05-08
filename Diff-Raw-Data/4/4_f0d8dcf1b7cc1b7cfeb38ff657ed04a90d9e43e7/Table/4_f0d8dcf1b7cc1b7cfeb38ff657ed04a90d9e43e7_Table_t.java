 // Copyright (C) 2000, 2001, 2002, 2003, 2004 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.console.swingui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.UIManager;
 
 
 /**
  * A read-only JTable that works in conjunction with an extended
  * TableModel specifies some cell rendering.
  *
 # * @author Philip Aston
  * @version $Revision$
  */
 final class Table extends JTable {
 
   /**
    * Interface for our extended TableModel.
    */
   public interface TableModel extends javax.swing.table.TableModel {
     boolean isBold(int row, int column);
     boolean isRed(int row, int column);
   }
 
   private final MyCellRenderer m_cellRenderer = new MyCellRenderer();
   private final TableCellRenderer m_headerRenderer = new MyHeaderRenderer();
   private final Color m_defaultForeground;
   private final Font m_boldFont;
   private final Font m_defaultFont;
 
   public Table(TableModel tableModel) {
     super(tableModel);
 
     setRowSelectionAllowed(false);
 
     m_defaultForeground = m_cellRenderer.getForeground();
     m_defaultFont = m_cellRenderer.getFont();
     m_boldFont = m_defaultFont.deriveFont(Font.BOLD);
 
     createDefaultColumnsFromModel();
   }
 
   /**
    * Set the header renderer for every column to work around Swing's
    * dumb optimization where it assumes the default header renderer is
    * always the same height. This nearly gives reasonably resize
    * behaviour with J2SE 1.3 (but only on the second resize event),
    * and doesn't work at all for J2SE 1.4; I'm still investigating.
    */
   public void addColumn(TableColumn column) {
     column.setHeaderRenderer(m_headerRenderer);
     super.addColumn(column);
   }
 
   public TableCellRenderer getCellRenderer(int row, int column) {
     final TableModel model = (TableModel)getModel();
 
     final boolean red = model.isRed(row, column);
     final boolean bold = model.isBold(row, column);
 
     if (red | bold) {
       m_cellRenderer.setForeground(red ? Colours.RED : m_defaultForeground);
       m_cellRenderer.setTheFont(bold ? m_boldFont : m_defaultFont);
 
       return m_cellRenderer;
     }
     else {
       return super.getCellRenderer(row, column);
     }
   }
 
   private static final class MyCellRenderer extends DefaultTableCellRenderer {
     private Font m_font;
 
     public Component getTableCellRendererComponent(JTable table,
                                                    Object value,
                                                    boolean isSelected,
                                                    boolean hasFocus,
                                                    int row,
                                                    int column) {
       final DefaultTableCellRenderer defaultRenderer =
         (DefaultTableCellRenderer)
         super.getTableCellRendererComponent(table, value, isSelected,
                                             hasFocus, row, column);
 
       // DefaultTableCellRenderer strangely only supports a
       // single font per Table. We override to set font on a per
       // cell basis.
       defaultRenderer.setFont(m_font);
 
       return defaultRenderer;
     }
 
     public void setTheFont(Font f) {
       m_font = f;
     }
   }
 
   private final class MyHeaderRenderer implements TableCellRenderer {
 
     private JTextArea m_textArea = new JTextArea();
 
     private MyHeaderRenderer() {
       m_textArea.setLineWrap(true);
       m_textArea.setWrapStyleWord(true);
       m_textArea.setOpaque(false);
       m_textArea.setEditable(false);
     }
 
     /**
      * If working out what to do from the Swing source, be aware that
      * JTableHeader.createDefaultRenderer() uses an anonymous inner
      * class that overrides
      * DefaultTableCellRenderer.getTableCellRendererComponent().
      */
     public Component getTableCellRendererComponent(JTable table, Object value,
                                                    boolean isSelected,
                                                    boolean hasFocus,
                                                    int row, int column) {
 
       if (table != null) {
         final JTableHeader header = table.getTableHeader();
 
         if (header != null) {
           m_textArea.setForeground(header.getForeground());
           m_textArea.setBackground(header.getBackground());
           m_textArea.setFont(header.getFont());
         }
       }
 
      // See Java Bug 4760433.
      m_textArea.setSize(table.getColumnModel().getColumn(column).getWidth(),
                         Integer.MAX_VALUE);

       m_textArea.setText((value == null) ? "" : value.toString());
       m_textArea.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
 
       return m_textArea;
     }
   }
 }
