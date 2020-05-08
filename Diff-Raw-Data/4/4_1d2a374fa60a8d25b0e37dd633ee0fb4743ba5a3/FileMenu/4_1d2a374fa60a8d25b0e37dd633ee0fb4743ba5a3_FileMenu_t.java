 /**
  * Portions copyright (C) 2000, 2001 Maynard Demmon, maynard@organic.com
  * Portions copyright (C) 2001 Stan Krute <Stan@StanKrute.com>
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the 
  * following conditions are met:
  * 
  *  - Redistributions of source code must retain the above copyright 
  *    notice, this list of conditions and the following disclaimer. 
  * 
  *  - Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution. 
  * 
  *  - Neither the names "Java Outline Editor", "JOE" nor the names of its 
  *    contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
  
 package com.organic.maynard.outliner;
 
 import java.io.*;
 import java.util.*;
 import java.text.SimpleDateFormat;
 import java.awt.*;
 import javax.swing.*;
 import org.xml.sax.*;
 import com.organic.maynard.util.string.Replace;
 
 /**
  * @author  $Author$
  * @version $Revision$, $Date$
  */
 
 public class FileMenu extends AbstractOutlinerMenu implements GUITreeComponent, JoeReturnCodes {
 	
 	// The Constructors
 	public FileMenu() {
 		super();
 	}
 
 
 	// GUITreeComponent interface
 	public void startSetup(AttributeList atts) {
 		super.startSetup(atts);
 		Outliner.menuBar.fileMenu = this;
 	}
 	
 	public void endSetup(AttributeList atts) {
 		// Disable menus at startup
 		((OutlinerSubMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SAVE_AS_MENU_ITEM)).setEnabled(false);
 		((OutlinerSubMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.EXPORT_MENU_ITEM)).setEnabled(false);
 		((OutlinerSubMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.EXPORT_SELECTION_MENU_ITEM)).setEnabled(false);
 		
 		super.endSetup(atts);
 	}
 
 	
 	// Utility Methods
 	private static final int MODE_SAVE = 0;
 	private static final int MODE_EXPORT = 1;
 	
 	public static void exportFile(String filename, OutlinerDocument document, FileProtocol protocol) {
 		saveFile(filename, document, protocol, true, MODE_EXPORT);
 	}
 
 	// part of bug # 495598 fix    this lets a script call saveFile without speccing a protocol    SRK   12/25/01 3:21PM
 	public static void saveFile(String filename, OutlinerDocument document, boolean saveAs) {
 		DocumentInfo docInfo = document.getDocumentInfo();
 		FileProtocol protocol = Outliner.fileProtocolManager.getProtocol(docInfo.getProtocolName());
 		saveFile(filename, document, protocol, saveAs, MODE_SAVE);
 	}
 	// end part of bug # 495598 fix
 
 	public static void saveFile(String filename, OutlinerDocument document, FileProtocol protocol, boolean saveAs) {
 		saveFile(filename, document, protocol, saveAs, MODE_SAVE);
 	}
 	
 	public static void saveFile(String filename, OutlinerDocument document, FileProtocol protocol, boolean saveAs, int mode) {
 		DocumentInfo docInfo = document.getDocumentInfo();
 
 		// part of bug # 495598 fix   this field wasn't getting set    SRK   12/25/01 3:26PM
 		docInfo.setProtocolName(protocol.getName()) ;
 		// end part of bug # 495598 fix
 		
 		// Get the file format object
 		String fileFormatName = docInfo.getFileFormat();
 		SaveFileFormat saveFileFormat = null;
 		if (mode == MODE_SAVE) {
 			saveFileFormat = Outliner.fileFormatManager.getSaveFormat(fileFormatName);
 		} else if (mode == MODE_EXPORT) {
 			saveFileFormat = Outliner.fileFormatManager.getExportFormat(fileFormatName);
 		} else {
 			// Ack, this shouldn't happen.
 		}
 
 		String msg = null;
 		if (saveFileFormat == null) {
 			msg = GUITreeLoader.reg.getText("error_could_not_save_no_file_format");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, docInfo.getPath());
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_2, fileFormatName);
 
 			JOptionPane.showMessageDialog(document, msg);
 			return;
 		}
 
 		// Initialize DocumentInfo with current document state, prefs and document settings.
 		if (mode == MODE_SAVE) {
 			document.setFileName(filename);
 			docInfo.updateDocumentInfoForDocument(document, saveAs); // Might not be neccessary anymore.
 		} else {
 			// Set it here since this would normally be pulled from the document's filename during
 			// updateDocumentInfoForDocument method.
 			docInfo.setPath(filename);
 		}
 				
 		document.settings.useDocumentSettings = true;
 
 		// Check File Format Support
 		boolean commentExists = false;
 		boolean editableExists = false;
 		boolean moveableExists = false;
 		boolean attributesExist = false;
 		boolean documentAttributesExist = false;
 		
 		if (document.tree.getAttributeCount() > 0) {
 			documentAttributesExist = true;
 		}
 		
 		Node node = document.tree.getRootNode();
 		int lineCount = -1;
 		while (true) {
 			node = node.nextNode();
 			lineCount++;
 			
 			if (node.isRoot()) {
 				break;
 			}
 			
 			if (node.isComment()) {
 				commentExists = true;
 			}
 
 			if (!node.isEditable()) {
 				editableExists = true;
 			}
 
 			if (!node.isMoveable()) {
 				moveableExists = true;
 			}
 			
 			if (!attributesExist && node.getAttributeCount() > 0) {
 				attributesExist = true;
 			}
 		}
 
 		
 		if (commentExists && !saveFileFormat.supportsComments()) {
 			msg = GUITreeLoader.reg.getText("error_file_format_does_not_support_comments");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, fileFormatName);
 			if (USER_ABORTED == promptUser(msg)) {
 				return;
 			}
 		}
 
 		if (editableExists && !saveFileFormat.supportsEditability()) {
 			msg = GUITreeLoader.reg.getText("error_file_format_does_not_support_editability");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, fileFormatName);
 			if (USER_ABORTED == promptUser(msg)) {
 				return;
 			}
 		}
 
 		if (moveableExists && !saveFileFormat.supportsMoveability()) {
 			msg = GUITreeLoader.reg.getText("error_file_format_does_not_support_moveability");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, fileFormatName);
 			if (USER_ABORTED == promptUser(msg)) {
 				return;
 			}
 		}
 
 		if (attributesExist && !saveFileFormat.supportsAttributes()) {
 			msg = GUITreeLoader.reg.getText("error_file_format_does_not_support_attributes");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, fileFormatName);
 			if (USER_ABORTED == promptUser(msg)) {
 				return;
 			}
 		}
 
 		if (documentAttributesExist && !saveFileFormat.supportsDocumentAttributes()) {
 			msg = GUITreeLoader.reg.getText("error_file_format_does_not_support_document_attributes");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, fileFormatName);
 			if (USER_ABORTED == promptUser(msg)) {
 				return;
 			}
 		}
 
 		// Save the File
 		if (document.hoistStack.isHoisted()) {
 			document.hoistStack.temporaryDehoistAll(); // So that a hoisted doc will be completely saved.
 		}
 		
 		// WebFile
 		byte[] bytes = saveFileFormat.save(document.tree, docInfo);
 
 		// Write the bytes	
 		docInfo.setOutputBytes(bytes);					
 		boolean success = protocol.saveFile(docInfo);
 
 		if (document.hoistStack.isHoisted()) {
 			document.hoistStack.temporaryHoistAll(); // Now that the whole doc was saved, let's put things back the way they were.
 		}
 
 		if (success) {
 			if (mode == MODE_SAVE) {
 				// Stop collecting text edits into the current undoable.
 				UndoableEdit.freezeUndoEdit(document.tree.getEditingNode());
 				
 				// Update the Recent File List
 				if (saveAs && !document.getFileName().equals(filename)) {
 					RecentFilesList.addFileNameToList(docInfo);
 				} else {
 					RecentFilesList.updateFileNameInList(filename, docInfo);
 				}
 
 				//document.setFileName(filename);
 				document.setTitle(filename);
 				document.setFileModified(false);
 
 				// Update the Window Menu
 				WindowMenu.updateWindow(document);
 			}
 		} else {
 			msg = GUITreeLoader.reg.getText("error_could_not_save_file");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, docInfo.getPath());
 
 			JOptionPane.showMessageDialog(document, msg);
 		}
 		
 		// Get rid of the bytes now that were done so they can be GC'd.
 		docInfo.setOutputBytes(null);
 	}
 	
 	private static int openFileAndGetTree(TreeContext tree, DocumentInfo docInfo, FileProtocol protocol) {
 		// Open the file
 		if (!protocol.openFile(docInfo)) {
 			return FAILURE;
 		}
 
 		// Get the file format object
 		OpenFileFormat openFileFormat = Outliner.fileFormatManager.getOpenFormat(docInfo.getFileFormat());
 
 		String msg = null;
 		int success = FAILURE;
 		
 		if (openFileFormat == null) {
 			msg = GUITreeLoader.reg.getText("error_could_not_open_no_file_format");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, docInfo.getPath());
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_2, docInfo.getFileFormat());
 			JOptionPane.showMessageDialog(Outliner.outliner, msg);
 		} else {
 			// Load the file
 			success = openFileFormat.open(tree, docInfo, docInfo.getInputStream());
 			
 			if (success == FAILURE) {
 				msg = GUITreeLoader.reg.getText("error_could_not_open_file");
 				msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, docInfo.getPath());
 
 				JOptionPane.showMessageDialog(Outliner.outliner, msg);
 				RecentFilesList.removeFileNameFromList(docInfo.getPath());
 			} else if (success != FAILURE_USER_ABORTED) {
 				// Deal with a childless RootNode or an Empty or Null Tree
				if ((tree.getRootNode() == null) || (tree.getRootNode().numOfChildren() <= 0)) {
					tree.reset();
 				}
 			}
 		}
 		
 		// Reset the input stream in the docInfo
 		docInfo.setInputStream(null);
 		
 		return success;
 	}
 	
 	// import a file
 	// similar to opening a file
 	// key diff is that we don't assume we can save it
 	protected static void importFile(DocumentInfo docInfo, FileProtocol protocol) {
 		
 		if (Outliner.DEBUG) { 
 			System.out.println("Stan_Debug:\tFileMenu:importFile: at the start" ); 
 		} // end if
 		
 		
 	} // end method importFile
 	
 
 	protected static void openFile(DocumentInfo docInfo, FileProtocol protocol) {
 		// Get the TreeContext
 		TreeContext tree = new TreeContext();
 		int success = openFileAndGetTree(tree, docInfo, protocol);
 		
 		if ((success != SUCCESS) && (success != SUCCESS_MODIFIED)) { // Might be good to have codes we can do % on.
 			return;
 		}
 		
 		// Create a new document
 		OutlinerDocument newDoc = new OutlinerDocument(docInfo.getPath(), docInfo);
 		newDoc.setDocumentInfo(docInfo);
 		
 		tree.doc = newDoc;
 		newDoc.tree = tree;
 		
 		// Update DocumentSettings
 		//newDoc.settings.syncPrefs();
 		newDoc.settings.lineEnd.def = docInfo.getLineEnding();
 		newDoc.settings.lineEnd.restoreCurrentToDefault();
 		newDoc.settings.lineEnd.restoreTemporaryToDefault();
 
 		newDoc.settings.saveEncoding.def = docInfo.getEncodingType();
 		newDoc.settings.saveEncoding.restoreCurrentToDefault();
 		newDoc.settings.saveEncoding.restoreTemporaryToDefault();
 		
 		newDoc.settings.saveFormat.def = docInfo.getFileFormat();
 		newDoc.settings.saveFormat.restoreCurrentToDefault();
 		newDoc.settings.saveFormat.restoreTemporaryToDefault();
 		
 		newDoc.settings.applyFontStyleForComments.def = docInfo.getApplyFontStyleForComments();
 		newDoc.settings.applyFontStyleForComments.restoreCurrentToDefault();
 		newDoc.settings.applyFontStyleForComments.restoreTemporaryToDefault();
 
 		newDoc.settings.applyFontStyleForEditability.def = docInfo.getApplyFontStyleForEditability();		
 		newDoc.settings.applyFontStyleForEditability.restoreCurrentToDefault();
 		newDoc.settings.applyFontStyleForEditability.restoreTemporaryToDefault();
 
 		newDoc.settings.applyFontStyleForMoveability.def = docInfo.getApplyFontStyleForMoveability();
 		newDoc.settings.applyFontStyleForMoveability.restoreCurrentToDefault();
 		newDoc.settings.applyFontStyleForMoveability.restoreTemporaryToDefault();
 		
 		newDoc.settings.dateCreated = docInfo.getDateCreated();
 		newDoc.settings.dateModified = docInfo.getDateModified();
 
 		newDoc.settings.useDocumentSettings = true;
 
 		// Move it to the bottom of the recent files list
 		RecentFilesList.updateFileNameInList(docInfo.getPath(), docInfo);
 		
 		setupAndDraw(docInfo, newDoc, success);
 	}
 
 	protected static void revertFile(OutlinerDocument document) {
 		DocumentInfo docInfo = document.getDocumentInfo();
 		FileProtocol protocol = Outliner.fileProtocolManager.getProtocol(docInfo.getProtocolName());
 
 		// Get the TreeContext
 		TreeContext tree = new TreeContext();
 		int success = openFileAndGetTree(tree, docInfo, protocol);
 		
 		if ((success != SUCCESS) && (success != SUCCESS_MODIFIED)) { // Might be good to have codes we can do % on.
 			return;
 		}
 		
 		// Swap in the new tree
 		tree.doc = document;
 		document.tree = tree;
 		
 		// Clear the UndoQueue
 		document.undoQueue.clear();
 		
 		// Clear the HoistStack
 		document.hoistStack.clear();
 
 		setupAndDraw(docInfo, document, success);
 	}
 	
 	private static void setupAndDraw(DocumentInfo docInfo, OutlinerDocument doc, int success) {
 		TreeContext tree = doc.tree;
 		String filename = docInfo.getPath();
 		
 		// Clear current selection
 		tree.clearSelection();
 		
 		// Clear the VisibleNodeCache
 		tree.visibleNodes.clear();
 		
 		// Insert nodes into the VisibleNodes Cache
 		for (int i = 0; i < tree.rootNode.numOfChildren(); i++) {
 			tree.addNode(tree.rootNode.getChild(i));
 		}
 		
 		// Update the menuBar
 		doc.setFileName(filename);
 		doc.setFileModified(false);
 		doc.setTitle(filename);
 
 		// Expand Nodes
 		ArrayList expandedNodes = docInfo.getExpandedNodes();
 		for (int i = 0; i < expandedNodes.size(); i++) {
 			int nodeNum = ((Integer) expandedNodes.get(i)).intValue();
 			try {
 				Node node = doc.tree.visibleNodes.get(nodeNum);
 				node.setExpanded(true);
 			} catch (Exception e) {
 				break;
 			}
 		}
 		
 		// Record the current location
 		Node firstVisibleNode;
 		int index = -1;
 		try {
 			index = docInfo.getVerticalScrollState() - 1;
 			firstVisibleNode = tree.visibleNodes.get(index);
 		} catch (IndexOutOfBoundsException e) {
 			index = 0;
 			firstVisibleNode = tree.visibleNodes.get(0);
 		}
 		
 		// Record Document Settings
 		doc.settings.ownerName.cur = docInfo.getOwnerName();
 		doc.settings.ownerEmail.cur = docInfo.getOwnerEmail();
 		
 		tree.setEditingNode(firstVisibleNode);
 		tree.setCursorPosition(0);
 		tree.setComponentFocus(OutlineLayoutManager.TEXT);
 		
 		// Redraw
 		OutlineLayoutManager layout = doc.panel.layout;
 		layout.setNodeToDrawFrom(firstVisibleNode,index);
 		layout.draw();
 		layout.setFocus(firstVisibleNode, OutlineLayoutManager.TEXT);
 
 		// Set document as modified if something happened on open
 		if (success == SUCCESS_MODIFIED) {
 			doc.setFileModified(true);
 		}
 	}
 
 
 	// Utility Methods
 	private static int promptUser(String msg) {
 		String yes = GUITreeLoader.reg.getText("yes");
 		String no = GUITreeLoader.reg.getText("no");
 		String confirm_save = GUITreeLoader.reg.getText("confirm_save");
 
 
 		Object[] options = {yes, no};
 		int result = JOptionPane.showOptionDialog(Outliner.outliner,
 			msg,
 			confirm_save,
 			JOptionPane.YES_NO_OPTION,
 			JOptionPane.QUESTION_MESSAGE,
 			null,
 			options,
 			options[0]
 		);
 		
 		if (result == JOptionPane.NO_OPTION) {
 			return USER_ABORTED;
 		} else {
 			return SUCCESS;
 		}
 	}
 	
 	
 	// Menu Updates
 	public static void updateSaveMenuItem() {
 		JMenuItem saveItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SAVE_MENU_ITEM);
 		JMenuItem saveAsItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SAVE_AS_MENU_ITEM);
 		JMenuItem revertItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.REVERT_MENU_ITEM);
 		JMenuItem closeItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.CLOSE_MENU_ITEM);
 		JMenuItem closeAllItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.CLOSE_ALL_MENU_ITEM);
 		JMenuItem exportItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.EXPORT_MENU_ITEM);
 		JMenuItem exportSelectionItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.EXPORT_SELECTION_MENU_ITEM);
 	
 		if (Outliner.getMostRecentDocumentTouched() == null) {
 			saveItem.setEnabled(false);
 			saveAsItem.setEnabled(false);
 			revertItem.setEnabled(false);
 			exportItem.setEnabled(false);
 			exportSelectionItem.setEnabled(false);
 			closeItem.setEnabled(false);
 			closeAllItem.setEnabled(false);
 		} else if (Outliner.getMostRecentDocumentTouched().getFileName().equals("")) {
 			saveItem.setEnabled(true);
 			saveAsItem.setEnabled(true);
 			revertItem.setEnabled(false);
 			exportItem.setEnabled(true);
 			exportSelectionItem.setEnabled(true);
 			closeItem.setEnabled(true);
 			closeAllItem.setEnabled(true);
 		} else if (Outliner.getMostRecentDocumentTouched().isFileModified()) {
 			saveItem.setEnabled(true);
 			saveAsItem.setEnabled(true);
 			revertItem.setEnabled(true);
 			exportItem.setEnabled(true);
 			exportSelectionItem.setEnabled(true);
 			closeItem.setEnabled(true);
 			closeAllItem.setEnabled(true);
 		} else {
 			saveItem.setEnabled(false);
 			saveAsItem.setEnabled(true);
 			revertItem.setEnabled(false);
 			exportItem.setEnabled(true);
 			exportSelectionItem.setEnabled(true);
 			closeItem.setEnabled(true);
 			closeAllItem.setEnabled(true);
 		}
 	}
 	
 	public static void updateSaveAllMenuItem() {
 		boolean enabledState = false;
 		for (int i = 0; i < Outliner.openDocumentCount(); i++) {
 			OutlinerDocument doc = Outliner.getDocument(i);
 			if (doc.isFileModified() || doc.getFileName().equals("")) {
 				enabledState = true;
 				break;
 			}
 		}
 
 		JMenuItem saveAllItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SAVE_ALL_MENU_ITEM);
 		saveAllItem.setEnabled(enabledState);
 	}
 }
