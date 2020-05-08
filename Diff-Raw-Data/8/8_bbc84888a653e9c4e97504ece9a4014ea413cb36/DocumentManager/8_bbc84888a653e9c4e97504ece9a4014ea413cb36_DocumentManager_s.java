 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wombat;
 
 import gui.MainFrame;
 import gui.SchemeDocument;
 import gui.SchemeTextArea;
 
 import java.awt.Component;
 import java.awt.FileDialog;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import javax.swing.JOptionPane;
 import javax.swing.text.BadLocationException;
 
 import util.errors.ErrorManager;
 import util.files.RecentDocumentManager;
 
 import net.infonode.docking.*;
 import net.infonode.docking.util.*;
 
 /**
  * Manage open documents.
  */
 public final class DocumentManager implements FocusListener {
 	static DocumentManager me;
 	
 	// Next file to create.
 	int lastIndex;
 	
 	// GUI references.
 	MainFrame Main;
 	RootWindow Root;
     TabWindow Documents;
     StringViewMap Views = new StringViewMap();
     
     // Active documents.
     List<SchemeTextArea> allDocuments;
     SchemeTextArea activeDocument;
     
     // Hide constructor.
     private DocumentManager() {}
     
     /**
      * Manage documents.
      * 
      * @param main The main frame.
      * @param root The root window (for splitting when strange things happen).
      * @param views View map (holds all of the documents).
      * @param documents Document tab manager (holds open documents at first).
      */
     public static DocumentManager init(MainFrame main, RootWindow root, StringViewMap views, TabWindow documents) {
     	if (me == null) {
 	    	me = new DocumentManager();
 	    	
 	        me.lastIndex = 0;
 	        
 	        me.Main = main;
 	        me.Root = root;
 	        me.Views = views;
 	        me.Documents = documents;
 	        
 	        me.allDocuments = new ArrayList<SchemeTextArea>();
     	}
         
         return me;
     }
     
     /**
      * Create a new document.
      */
     public static boolean New() {
     	if (me == null) throw new RuntimeException("Document manager not initialized.");
     	
         me.lastIndex++;
         
         String id = "document-" + me.lastIndex;
 
         SchemeTextArea ss = new SchemeTextArea();
         me.allDocuments.add(ss);
         ss.code.addFocusListener(me);
         
         me.Views.addView(id, new View("<new document>", null, ss));
         ss.myView = me.Views.getView(id);
         
         me.Documents.addTab(me.Views.getView(id));
         
         if (me.Root != null && !me.Documents.isShowing())
         	me.Root.setWindow(new SplitWindow(false, 0.6f, me.Documents, me.Root.getWindow()));
          
         ss.code.requestFocusInWindow();
         
         return true;
     }
     
     /**
      * Load a file from a dialog.
      * @return If the load worked.
      */
     public static boolean Open() {
     	if (me == null) throw new RuntimeException("Document manager not initialized.");
     	
     	FileDialog fc = new FileDialog(me.Main, "Open...", FileDialog.LOAD);
         fc.setVisible(true);
         
         if (fc.getFile() == null)
             return false;
         
         File file = new File(fc.getDirectory(), fc.getFile());
         if (!file.exists())
         {
         	ErrorManager.logError("Unable to load file (does not exist): " + fc.getFile());
             return false;
         }
         
         return Open(file);
     }
 
     /**
      * Load a specific file.
      * @param file The file to load.
      * @return If the load worked.
      */
     public static boolean Open(File file) {
     	if (me == null) throw new RuntimeException("Document manager not initialized.");
     	
         me.lastIndex++;
         
         String id = "document-" + me.lastIndex;
         String filename = file.getName();
         
         try {
             if (!file.exists())
                 file.createNewFile();
 
             Scanner scanner = new Scanner(file);
             StringBuilder content = new StringBuilder();
            String NL = System.getProperty("line.separator");
 
             while (scanner.hasNextLine()) {
                 content.append(scanner.nextLine());
                 content.append(NL);
             }
 
             SchemeTextArea ss = new SchemeTextArea(content.toString());
             me.allDocuments.add(ss);
             ss.myFile = file;
             ss.code.addFocusListener(me);
 
             me.Views.addView(id, new View(filename, null, ss));
             ss.myView = me.Views.getView(id);
 
             me.Documents.addTab(me.Views.getView(id));
             
             if (me.Root != null && !me.Documents.isShowing())
             	me.Root.setWindow(new SplitWindow(false, 0.6f, me.Documents, me.Root.getWindow()));
             
             ss.code.requestFocusInWindow();
 
             RecentDocumentManager.addFile(file);
             
             return true;
         }
         catch(IOException ex) {
         	ErrorManager.logError("Unable to load file (" + file.getName() + "): " + ex.getMessage());
             return false;
         }
     }
     
     /**
      * Save the current file.
      * @return If the save worked.
      */
     public static boolean Save() {
     	if (me == null) throw new RuntimeException("Document manager not initialized.");
     	
         if (me.activeDocument == null)
             return false;
         if (me.activeDocument.myFile == null)
             return SaveAs();
         
         try {
             Writer out = new OutputStreamWriter(new FileOutputStream(me.activeDocument.myFile));
             out.write(me.activeDocument.getText());
             out.flush();
             out.close();
             
             RecentDocumentManager.addFile(me.activeDocument.myFile);
             
             return true;
         } catch(FileNotFoundException ex) {
             return false;
         } catch(IOException ex) {
             return false;
         }
     }
 
     /**
      * Save the active file with a new name.
      * @return If it worked.
      */
     public static boolean SaveAs() {
     	if (me == null) throw new RuntimeException("Document manager not initialized.");
     	
         if (me.activeDocument == null)
             return false;
 
         FileDialog fc = new FileDialog(me.Main, "Save as...", FileDialog.SAVE);
         fc.setVisible(true);
         if (fc.getFile() == null)
             return false;
 
         File file = new File(fc.getDirectory(), fc.getFile());
         me.activeDocument.myFile = file;
         me.activeDocument.myView.getViewProperties().setTitle(file.getName());
 
         return Save();
     }
 
     /**
      * Close the active document.
      * @return If it worked.
      */
     public static boolean Close() {
     	if (me == null) throw new RuntimeException("Document manager not initialized.");
     	
         if (me.activeDocument == null)
             return false;
 
         if (!me.activeDocument.isEmpty())
         {
             String name = me.activeDocument.myView.getViewProperties().getTitle();
             if (!Options.ConfirmOnClose || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                     me.activeDocument,
                     "Save " + name + " before closing?",
                     "Close...",
                     JOptionPane.YES_NO_OPTION
                 ))
             {
                 Save();
             }
         }
         
         me.allDocuments.remove(me.activeDocument);
         me.activeDocument.myView.close();
         	        
         return true;
     }
     
     /**
      * Close all documents.
      * @return If it worked.
      */
     public static boolean CloseAll() {
     	if (me == null) throw new RuntimeException("Document manager not initialized.");
     	
     	boolean closedAll = true;
     	while (!me.allDocuments.isEmpty())
     	{
     		me.activeDocument = me.allDocuments.get(0);
     		closedAll &= Close();
     	}
     	return closedAll;
     }
     
     /**
      * Reload all documents (to update formatting).
      * @return If it worked.
      */
     public static boolean ReloadAll() {
     	if (me == null) throw new RuntimeException("Document manager not initialized.");
     	
     	for (SchemeTextArea ss : me.allDocuments)
 			try {
 				((SchemeDocument) ss.code.getDocument()).processChangedLines(0, ss.getText().length());
 			} catch (BadLocationException e) {
 				ErrorManager.logError("Unable to format " + ss.getFile() + ": " + e.getMessage());
 			}
     	
     	return me.Main.updateDisplay();
     }
 
     /**
      * Run the active document.
      * @return If it worked.
      */
     public static boolean Run() {
     	if (me == null) throw new RuntimeException("Document manager not initialized.");
     	
         if (me.activeDocument == null)
             return false;
 
         if (!me.activeDocument.isEmpty())
         {
             String name = me.activeDocument.myView.getViewProperties().getTitle();
             if (!Options.ConfirmOnRun || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                     me.activeDocument,
                     "Save " + name + " before running?",
                     "Close...",
                     JOptionPane.YES_NO_OPTION
                 ))
             {
                 if (!Save())
                 	return false;
             }
         }
 
         me.Main.doCommand("(load \"" + me.activeDocument.myFile.getAbsolutePath().replace("\\", "/")  + "\")");
         me.Main.focusREPL();
         
         return true;
     }
 
     /**
      * Format the active document.
      * @return If it worked.
      */
     public static boolean Format() {
     	if (me == null) throw new RuntimeException("Document manager not initialized.");
     	
         if (me.activeDocument == null)
             return false;
         
         me.activeDocument.format();
         
         return true;
     }
     
     /**
      * Insert a tab / return.
      * 
      * @param insertReturn Insert a newline before tabbing.
      * @return If it worked.
      */
 	public static boolean Tab(boolean insertReturn) {
 		if (me == null) throw new RuntimeException("Document manager not initialized.");
 		
 		SchemeTextArea doc = me.activeDocument;
 		if (doc == null)
 			return false;
 		
 		if (insertReturn) {
 			try {
 				doc.code.getDocument().insertString(doc.code.getCaretPosition(), "\n", null);
 	        } catch (BadLocationException ble) {
 	        	ErrorManager.logError("Unable to add a new line on ENTER.");
 	        }
 		}
 		
 		doc.tab();
 		return true;
 	}
 
     /**
      * Keep track of which text area last had focus.
      * @param e The event.
      */
     @Override
     public void focusGained(FocusEvent e) {
         if (!(e.getSource() instanceof Component))
             return;
 
         Component c = (Component) e.getSource();
         while (c != null)
         {
             if (c instanceof SchemeTextArea)
             {
                 activeDocument = (SchemeTextArea) c;
                 return;
             }
 
             c = c.getParent();
         }
     }
 
     /**
      * Ignore this.
      * @param e
      */
     @Override
     public void focusLost(FocusEvent e) {
     }
 }
