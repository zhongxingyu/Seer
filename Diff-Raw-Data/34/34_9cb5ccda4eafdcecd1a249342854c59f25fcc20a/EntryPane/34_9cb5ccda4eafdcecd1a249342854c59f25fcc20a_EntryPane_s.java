 package fi.helsinki.biblex.ui;
 
 import fi.helsinki.biblex.domain.BibTexEntry;
import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseListener;
 
 
 /**
  * This class handles the visual appearance of the main UI window
  */
 public class EntryPane extends JPanel {
     private JScrollPane p_scrollPane;
     private JTable p_entryTable;
     private ReferenceTableModel refTableModel;
     
     private JMenuItem p_menuDeleteEntry;
     private JMenuItem p_menuCopyEntryToClipboard;
 
 
     public EntryPane() {
         refTableModel = new ReferenceTableModel();
         p_entryTable = new JTable(refTableModel);
 
         populate();
     }
 
     public JTable getEntryTable() {
         return p_entryTable;
     }
     
     public ReferenceTableModel getRefTableModel() {
         return refTableModel;
     }
 
     /**
      * Add a new entry to the entry list
      *
      * @param name Name/ID of the entry to add
      */
     public void addEntry(BibTexEntry entry) {
         refTableModel.addData(entry);
         refTableModel.fireTableStructureChanged();
        p_entryTable.revalidate();
     }
 
 
     public void clearEntryList() {
         refTableModel.clear();
         refTableModel.fireTableStructureChanged();
        p_entryTable.revalidate();
     }
     
 
     public String getSelectedEntry() {
        //return (String) refTableModel.getValueAt(refTableModel.getRowByName(), 0);
         return (String) refTableModel.getValueAt(p_entryTable.convertRowIndexToModel(p_entryTable.getSelectedRow()), 0);
     }
     
     public int getSelectedIndex() {
         return (int) p_entryTable.convertRowIndexToModel(p_entryTable.getSelectedRow());
     }
 
 
     public void addMListener(MouseListener ml) {
         p_entryTable.addMouseListener(ml);
     }
 
 
     public void registerDeleteAction(Action ac) {
         p_menuDeleteEntry.setAction(ac);
     }
 
 
     public void registerCopyToClipboardAction(Action ac) {
         p_menuCopyEntryToClipboard.setAction(ac);
     }
 
 
     /**
      * Add UI elements to the window, and setup action handling
      */
     private void populate() {
         this.setLayout(new BorderLayout());
 
         JPopupMenu popMenu = new JPopupMenu();
         p_menuDeleteEntry = new JMenuItem();
         p_menuCopyEntryToClipboard = new JMenuItem();
         p_menuCopyEntryToClipboard.setName("menuCopyEntryToClipboard");
 
         popMenu.add(p_menuDeleteEntry);
         popMenu.add(p_menuCopyEntryToClipboard);
         p_entryTable.setComponentPopupMenu(popMenu);
         p_entryTable.setAutoCreateRowSorter(true);
         
         
         p_scrollPane = new JScrollPane(p_entryTable);
         p_scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         p_scrollPane.setBorder(BorderFactory.createEmptyBorder());
 
         p_scrollPane.setMinimumSize(new Dimension(175, 390));
         p_scrollPane.setPreferredSize(new Dimension(175, 390));
 
         this.add(p_scrollPane, BorderLayout.CENTER);
     }
 }
