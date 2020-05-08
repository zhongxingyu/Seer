 package fi.helsinki.biblex.ui;
 
 import fi.helsinki.biblex.App;
 import fi.helsinki.biblex.domain.BibTexEntry;
 import fi.helsinki.biblex.domain.BibTexStyle;
 import fi.helsinki.biblex.validation.AbstractValidator;
 import fi.helsinki.biblex.validation.ValidationException;
 import java.awt.*;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.util.*;
 import javax.swing.*;
 
 /**
  * This class handles the application GUI.
  */
 public class GUI {
     static final String APP_NAME = "Biblex";
 
     private EntryPane p_entryPane;
     private Window p_window;
     private BibTexEntry p_entry;
 
     private ExportFileChooser p_exportFileChooser;
     private ImportFileChooser p_importFileChooser;
 
 
     public GUI() {
         p_entryPane = new EntryPane();
         p_window = new Window(p_entryPane);
 
         p_exportFileChooser = new ExportFileChooser();
         p_exportFileChooser.setName("exportFileChooser");
 
         p_importFileChooser = new ImportFileChooser();
         p_importFileChooser.setName("importFileChooser");
     }
 
     public void init() {
         // Populate Window
         populateEntryStyles();
         createActions();
 
         populateEntryList("");
         newEntry();
 
         p_window.setVisible(true);
     }
 
 
     public JFrame getWindow() {
         return p_window.getWindow();
     }
 
     public JTable getRefTable() {
         return p_entryPane.getEntryTable();
     }
 
     /**
      * Add all known entry styles to the style selection combo-box
      */
     private void populateEntryList(String filter) {
         p_entryPane.clearEntryList();
         p_entryPane.addEntry(App.getStorage());
     }
 
     /**
      * Add all known entry styles to the style selection combo-box
      */
     private void populateEntryStyles() {
         p_entryPane.clearEntryList();
         for (BibTexStyle style : BibTexStyle.values()) {
             p_window.addEntryStyle(style);
         }
     }
 
     private void newEntry() {
         p_entry = null;
         p_window.setEntry("", "");
     }
 
     /**
      * Set the name and style of the current entry
      *
      * Also removes all fields from the current entry, and adds all required
      * fields for the selected entry style.
      */
     private void setEntry(BibTexStyle style, String name) {
         if (style == null || name.trim().isEmpty()) {
             p_window.displayError("Reference name or type is invalid", "Reference creation failed");
             return;
         }
 
         p_entry = new BibTexEntry(name, style);
         p_window.setEntry(style.name(), p_entry.getName());
 
         AbstractValidator validator = App.getValidationService().getValidator(style);
         for (String field : validator.getSetOfRequiredFields()) {
             p_window.addField(field, "");
         }
     }
 
     /**
      * Open an existing entry
      *
      * @param entry Entry to open
      */
     private void openEntry(BibTexEntry entry) {
         if (entry == null)
             throw new RuntimeException("Internal error, tried to open null reference");
 
         p_entry = entry;
         p_window.setEntry(entry.getStyle().name(), p_entry.getName());
 
         for (Map.Entry<String, String> e : entry) {
             p_window.addField(e.getKey(), e.getValue());
         }
     }
 
     private boolean submitEntry() {
         if (p_entry == null) {
             return false;
         }
 
         try {
             for (Map.Entry<String, String> field : p_window) {
                 p_entry.put(field.getKey(), field.getValue());
             }
 
             App.getValidationService().checkEntry(p_entry);
         } catch (ValidationException e) {
             p_window.displayError(e.getMessage(), "Validation failed");
             return false;
         }
 
         try {
             if(App.getStorage().get(p_entry.getName()) != null) {
                 App.getStorage().update(p_entry.getId(), p_entry);
                return false;
             }
             else {
                 App.getStorage().add(p_entry);
             }
         } catch (Exception ex) {
             p_window.displayError(ex.getMessage(), "Saving failed");
             return false;
         }
         return true;
     }
 
     /**
      * Set up the actions to be used in the UI
      */
     private void createActions() {
         // Window
         p_window.registerAction(
                 Window.UIAction.SUBMIT,
                 new AbstractAction("Save Reference", UIManager.getIcon("FileView.hardDriveIcon")) {
                     public void actionPerformed(ActionEvent e) {
                         if(submitEntry()) {
                             p_entryPane.addEntry(p_entry);
                         }
                        else {
                            p_entryPane.getRefTableModel().fireTableDataChanged();
                        }
                     }
                 }
         );
 
         p_window.registerAction(
                 Window.UIAction.ADD_FIELD,
                 new AbstractAction("Add Field") {
                     public void actionPerformed(ActionEvent e) {
                         String name = p_window.getFieldNameEntry();
                         if (name.trim().isEmpty()) {
                             if (GUI.this.p_entry != null) {
                                 p_window.showAddableFieldPopup();
                             }
 
                             return;
                         }
 
                         p_window.addField(name, "");
                         p_entry.put(name, "");
                         p_window.clearFieldNameEntry();
                     }
                 }
         );
 
         p_window.registerAction(
                 Window.UIAction.DELETE_FIELD,
                 new AbstractAction("", UIManager.getIcon("InternalFrame.paletteCloseIcon")) {
                     public void actionPerformed(ActionEvent e) {
                         p_window.deleteField(e.getActionCommand());
                         p_entry.remove(e.getActionCommand());
                     }
                 }
         );
 
         p_window.registerAction(
                 Window.UIAction.SET_ENTRY,
                 new AbstractAction("Create") {
                     public void actionPerformed(ActionEvent e) {
                         setEntry(p_window.getEntryStyleInput(), p_window.getEntryNameInput());
                         p_window.clearEntryNameInput();
                     }
                 }
         );
 
         p_window.registerAction(
                 Window.UIAction.APPLY_FILTER,
                 new AbstractAction("Filter") {
                     public void actionPerformed(ActionEvent e) {
                         populateEntryList(p_window.getFilterEntry());
                     }
                 }
         );
 
         p_window.registerAction(
                 Window.UIAction.MENU_EXPORT,
                 new AbstractAction("Export") {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         try {
                             String filename = p_exportFileChooser.getFileName();
                             if (filename != null)
                                 App.getExporter().write(filename);
                         } catch (IOException ex) {
                             p_window.displayError(ex.getMessage(), "Export failed");
                         }
                     }
                 }
         );
 
         p_window.registerAction(
                 Window.UIAction.MENU_IMPORT,
                 new AbstractAction("Import") {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 String filename = p_importFileChooser.getFileName();
                 if (filename != null) {
                       App.getImporter().importFromFile(filename);
                       populateEntryList("");
                 }
             }
         });
 
         p_window.registerAction(
                 Window.UIAction.MENU_QUIT,
                 new AbstractAction("Quit") {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         System.exit(0);
                     }
                 }
         );
 
         p_window.registerAction(
                 Window.UIAction.MENU_NEW_ENTRY,
                 new AbstractAction("New Reference") {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         newEntry();
                     }
                 }
         );
 
         // EntryPane
         p_entryPane.addMListener(new MouseListener() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 if(e.getClickCount() > 1) {
                     e.consume();
                     openEntry(App.getStorage().get(p_entryPane.getSelectedEntry()));
                 }
             }
 
             public void mousePressed(MouseEvent e) {}
             public void mouseReleased(MouseEvent e) {}
             public void mouseEntered(MouseEvent e) {}
             public void mouseExited(MouseEvent e) {}
         });
 
         p_entryPane.registerDeleteAction(new AbstractAction("Delete") {
             public void actionPerformed(ActionEvent e) {
                 try {
                     BibTexEntry selected = App.getStorage().get(p_entryPane.getSelectedEntry());
                     App.getStorage().delete(selected.getId());
                 } catch (Exception ex) {
                     p_window.displayError(ex.toString(), "Failed to delete");
                 }
                 p_entryPane.getRefTableModel().deleteData(p_entryPane.getSelectedIndex());
                 p_entryPane.getRefTableModel().fireTableStructureChanged();
             }
         });
 
         p_entryPane.registerCopyToClipboardAction(new AbstractAction("Copy as BibTeX") {
             public void actionPerformed(ActionEvent e) {
                 try {
                     BibTexEntry selected = App.getStorage().get(p_entryPane.getSelectedEntry());
                     StringSelection selection = new StringSelection(selected.toString());
                     Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                 } catch (NullPointerException ex) {
                     p_window.displayError("No entry selected", "Failed to copy entry");
                 }
             }
         });
     }
 }
