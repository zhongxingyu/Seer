 /**
  * FileMenu class
  *
  *	Does the work for the File menu commands New, Open, Import Save, Export, Revert, Close
  *
  *	Members
  *	constants
  *		class
  *			private
  *				MODE_SAVE
  *				MODE_EXPORT
  *				MODE_OPEN
  *				MODE_IMPORT
  *	methods
  *		instance
  *			public
  *				constructors
  *					public FileMenu()
  *				void startSetup(AttributeList)
  *				void endSetup(AttributeList)
  *		class
  *			public
  *				void exportFile(String, OutlinerDocument, FileProtocol)
  *				void saveFile(String, OutlinerDocument, boolean)
  *				void saveFile(String, OutlinerDocument, FileProtocol, boolean)
  *				void saveFile(String, OutlinerDocument, FileProtocol, boolean, int)
  *				void updateSaveAllMenuItem()
  *				void updateFileMenuItems()
  *
  *
  *
  *
  *			protected
  *				void importFile(DocumentInfo, FileProtocol)
  *				void openFile(DocumentInfo, FileProtocol)
  *				void openFile(DocumentInfo, FileProtocol, int)
  *				void importFile(DocumentInfo, FileProtocol)
  *				void revertFile(OutlinerDocument document)
  *			private
  *				int openOrImportFileAndGetTree(JoeTree, DocumentInfo, FileProtocol, int)
  *				void setupAndDraw(DocumentInfo, OutlinerDocument, int)
  *				int promptUser(String)
  *
  *
  *
  *
 .*
  * Portions copyright (C) 2000-2001 Maynard Demmon, maynard@organic.com
  * Portions copyright (C) 20012002 Stan Krute <Stan@StanKrute.com>
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
  * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 /**
  * @author  $Author$
  * @version $Revision$, $Date$
  */
 
 // we're part of this
 package com.organic.maynard.outliner;
 
 // we use these
 import java.io.*;
 import java.util.*;
 import java.text.SimpleDateFormat;
 import java.awt.*;
 import javax.swing.*;
 import org.xml.sax.*;
 import com.organic.maynard.util.string.Replace;
 import com.organic.maynard.util.string.StanStringTools ;
 
 // this class implements the meat of several File Menu commands: New, Open, Import, Save, Revert, Close
 public class FileMenu extends AbstractOutlinerMenu implements GUITreeComponent, JoeReturnCodes {
 
 	// public class constants
 	private static final int MODE_SAVE = 0;
 	private static final int MODE_EXPORT = 1;
 	private static final int MODE_OPEN = 0;
 	private static final int MODE_IMPORT = 1;
 
 	private static final String TRUNC_STRING = GUITreeLoader.reg.getText("trunc_string");
 
 	// document title name forms
 	private static final int FULL_PATHNAME = 0 ;
 	private static final int TRUNC_PATHNAME = 1 ;
 	private static final int JUST_FILENAME = 2 ;
 
 	
 	// The Constructors
 	public FileMenu() {
 		super();
 	} // end FileMenu
 
 	// GUITreeComponent interface
 	public void startSetup(AttributeList atts) {
 		super.startSetup(atts);
 		Outliner.menuBar.fileMenu = this;
 	} // end startSetup
 
 	public void endSetup(AttributeList atts) {
 		// Disable menus at startup
 		((OutlinerSubMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SAVE_AS_MENU_ITEM)).setEnabled(false);
 		((OutlinerSubMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.EXPORT_MENU_ITEM)).setEnabled(false);
 		((OutlinerSubMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.EXPORT_SELECTION_MENU_ITEM)).setEnabled(false);
 
 		super.endSetup(atts);
 	} // end endSetup
 
 	// Utility Methods
 
 	// export a file
 	// we call the master-blaster-flavor save routine
 	public static void exportFile(String filename, OutlinerDocument document, FileProtocol protocol) {
 		saveFile(filename, document, protocol, true, MODE_EXPORT);
 	} // end exportFile
 
 	// save a file
 	// this flavor uses the doc's existing file protocol
 	public static void saveFile(String filename, OutlinerDocument document, boolean saveAs) {
 		DocumentInfo docInfo = document.getDocumentInfo();
 		FileProtocol protocol = Outliner.fileProtocolManager.getProtocol(docInfo.getProtocolName());
 		saveFile(filename, document, protocol, saveAs, MODE_SAVE);
 	} // end saveFile
 
 	// save a file
 	// this flavor lets you spec a file protocol
 	public static void saveFile(String filename, OutlinerDocument document, FileProtocol protocol, boolean saveAs) {
 		saveFile(filename, document, protocol, saveAs, MODE_SAVE);
 	} // end save file
 
 	// save or export a file
 	// this is the master-blaster routine that the other save/export routines call
 	public static void saveFile(String filename, OutlinerDocument document, FileProtocol protocol, boolean saveAs, int mode) {
 
 		// set up local vars
 		String msg = null;
 		DocumentInfo docInfo = document.getDocumentInfo();
 		String fileFormatName = docInfo.getFileFormat();
 		SaveFileFormat saveOrExportFileFormat = null;
 		boolean commentExists = false;
 		boolean editableExists = false;
 		boolean moveableExists = false;
 		boolean attributesExist = false;
 		boolean documentAttributesExist = false;
 		boolean wereImported = false ;
 		String savedDocsPrevName = null ;
 		String title ;
 		
 		// set up the protocol
 		docInfo.setProtocolName(protocol.getName()) ;
 
 		// Get the proper file format object for the specified mode
 		// Initialize DocumentInfo with current document state, prefs and document settings.
 		// Filter out bad modes
 		switch (mode) {
 
 			case MODE_SAVE:
 				saveOrExportFileFormat = Outliner.fileFormatManager.getSaveFormat(fileFormatName);
 				savedDocsPrevName = document.getFileName () ;
 				document.setFileName(filename);
 				docInfo.updateDocumentInfoForDocument(document, saveAs); // Might not be neccessary anymore.
 				break ;
 
 			case MODE_EXPORT:
 				saveOrExportFileFormat = Outliner.fileFormatManager.getExportFormat(fileFormatName);
 				docInfo.setPath(filename);
 				break ;
 
 			default:
 				// Ack, this shouldn't happen.
 				// illegal/unknown mode specification
 				System.out.println("FileMenu:SaveFile: bad mode parameter");
 				return ;
 			} // end switch
 
 		// if we couldn't get a saveOrExportFileFormat
 		if (saveOrExportFileFormat == null) {
 
 				msg = GUITreeLoader.reg.getText("error_could_not_save_no_file_format");
 				msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, docInfo.getPath());
 				msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_2, fileFormatName);
 				JOptionPane.showMessageDialog(document, msg);
 				return;
 			} // end if
 
 
 		// we're going to use the document settings
 		document.settings.useDocumentSettings = true;
 
 		// Check to see what special features the outline has
 		// That's because some save/export formats don't support all special features
 
 		// do we have document attributes ?
 		if (document.tree.getAttributeCount() > 0) {
 			documentAttributesExist = true;
 		} // end if
 
 		// walk the document tree, looking for various node features:
 		//     comments, not-editable, locked-in-place, attributes
 		//  [srk] hmmm, I sniff it'd be a useful optimization to maintain this info in DocInfo
 		// as someone works on an outline .... rather than having to
 		// scan the whole tree .... I think this scan is a big factor in
 		//  the slowness of saving very large outlines  ... can test that by
 		// temporarily commenting it out and doing some vlo saves
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
 		} // end while
 
 		// if we found comment nodes, but the save/export format doesn't support that concept  ...
 		if (commentExists && !saveOrExportFileFormat.supportsComments()) {
 			msg = GUITreeLoader.reg.getText("error_file_format_does_not_support_comments");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, fileFormatName);
 			if (USER_ABORTED == promptUser(msg)) {
 				return;
 			} // end if
 		} // end if
 
 		// if found non-editable nodes, but the save/export format doesn't support that concept ...
 		if (editableExists && !saveOrExportFileFormat.supportsEditability()) {
 			msg = GUITreeLoader.reg.getText("error_file_format_does_not_support_editability");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, fileFormatName);
 			if (USER_ABORTED == promptUser(msg)) {
 				return;
 			} // end if
 		} // end if
 
 		// if we found immoveable nodes, but the save/export format doesn't support that concept ...
 		if (moveableExists && !saveOrExportFileFormat.supportsMoveability()) {
 			msg = GUITreeLoader.reg.getText("error_file_format_does_not_support_moveability");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, fileFormatName);
 			if (USER_ABORTED == promptUser(msg)) {
 				return;
 			} // end if
 		} // end if
 
 		// if we found nodes with attributes, but the save/export format doesn't support that concept ...
 		if (attributesExist && !saveOrExportFileFormat.supportsAttributes()) {
 			msg = GUITreeLoader.reg.getText("error_file_format_does_not_support_attributes");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, fileFormatName);
 			if (USER_ABORTED == promptUser(msg)) {
 				return;
 			} // end if
 		} // end if
 
 		// if we've got document attributes, but the save/export format doesn't support that concept ....
 		if (documentAttributesExist && !saveOrExportFileFormat.supportsDocumentAttributes()) {
 			msg = GUITreeLoader.reg.getText("error_file_format_does_not_support_document_attributes");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, fileFormatName);
 			if (USER_ABORTED == promptUser(msg)) {
 				return;
 			} // end if
 		} // end if
 
 		// if parts of the doc are hoisted, temporarily dehoist
 		// so that a hoisted doc will be completely saved.
 		if (document.hoistStack.isHoisted()) {
 			document.hoistStack.temporaryDehoistAll();
 		} //
 
 		// ask the save/export format to send us an array of bytes to save/export. 
 		// This also gives the format a chance to display a dialog to the user.
 		byte[] bytes = saveOrExportFileFormat.save(document.tree, docInfo);
 
 		int saveOrExportResult = 0; // -1 error, 0 user aborted, 1 success.
 		
 		// Make sure bytes isn't null. If it is then we need to abort since there was an error in the format.
 		if (bytes != null) {
 			// point the doc info at that array of save/export bytes
 			docInfo.setOutputBytes(bytes);
 
 			// if we're an imported file, the savee/exportee won't be
 			if (wereImported = docInfo.isImported()){
 				docInfo.setImported(false) ;
 			} // end if we're imported
 
 			// ask the protocol to save/export the file
 			boolean result = protocol.saveFile(docInfo);
 			if (result) {saveOrExportResult = 1;} else {saveOrExportResult = -1;}
 		}
 
 		// if we had to unhoist stuff
 		if (document.hoistStack.isHoisted()) {
 			// rehoist it
 			document.hoistStack.temporaryHoistAll();
 		} // end if
 
 		// if we succeeded
 		if (saveOrExportResult == 1) {
 			// do special stuff based on mode
 			switch (mode) {
 
 			case MODE_SAVE:
 				// Stop collecting text edits into the current undoable.
 				UndoableEdit.freezeUndoEdit(document.tree.getEditingNode());
 
 				// Update the Recent File List
 				
 				// TBD [srk] collapse this if/else/if now that 
 				//	addFileNameToList is smarter 
 				// if we were imported ...
 				if (wereImported) {
 					RecentFilesList.addFileNameToList(docInfo);
 					
 				// else if we're doing a Save As and the name has changed ...
 				} else if ( saveAs && (! savedDocsPrevName.equals(filename))) {
 					
 					RecentFilesList.addFileNameToList(docInfo);
 					
 				// else we weren't imported and haven't changed our name on a Save As op
 				} else {
 					
 					RecentFilesList.addFileNameToList(docInfo);
 				} // end else
 
 				//document.setFileName(filename);
 				document.setFileModified(false);
 				// document.setTitle(StanStringTools.getTruncatedPathName(filename, TRUNC_STRING)) ;
 				
 				// case out on the form to build the title
 				switch (OutlinerDocument.getTitleNameForm()) {
 				
 				case FULL_PATHNAME:
 				default: 
 					title = filename ;
 					break ;
 					
 				case TRUNC_PATHNAME: 
 					title = StanStringTools.getTruncatedPathName(filename, TRUNC_STRING) ;
 					break ;
 					
 				case JUST_FILENAME: 
 					title = StanStringTools.getFileNameFromPathName(filename) ;
 					break ;
 					
 				} // end switch
 				
 				// set the title
 				document.setTitle(title) ;
 
 				// Update the Window Menu
 				WindowMenu.updateWindow(document);
 				break ;
 
 			case MODE_EXPORT:
 				// if we were imported, we stay that way
 				if (wereImported) {
 					docInfo.setImported(true) ;
 				} // end if
 				break ;
 
 			default:
 				break ;
 
 			} // end switch
 
 		} else if (saveOrExportResult == -1) {
 			// we failed
 
 				// if were were imported, we stay that way
 				if (wereImported) {
 					docInfo.setImported(true) ;
 				} // end if
 
 			msg = GUITreeLoader.reg.getText("error_could_not_save_file");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, docInfo.getPath());
 			JOptionPane.showMessageDialog(document, msg);
 		} else {
 				// if were were imported, we stay that way
 				if (wereImported) {
 					docInfo.setImported(true) ;
 				} // end if		
 		}
 
 		// Get rid of the bytes now that were done so they can be GC'd.
 		docInfo.setOutputBytes(null);
 
 	} // end method saveFile
 
 
 	// open/import a file and store its outline into a tree
 	private static int openOrImportFileAndGetTree(JoeTree tree, DocumentInfo docInfo, FileProtocol protocol, int mode) {
 
 		// local vars
 		String msg = null;
 		int openOrImportResult = FAILURE;
 		OpenFileFormat openOrImportFileFormat = null ;
 
 		// try to open the file
 		if (!protocol.openFile(docInfo)) {
 			return FAILURE;
 		} // end if
 
 		// Get the proper file format object for the specified mode
 		switch (mode) {
 
 			case MODE_OPEN:
 				openOrImportFileFormat = Outliner.fileFormatManager.getOpenFormat(docInfo.getFileFormat());
 				break ;
 
 			case MODE_IMPORT:
 				openOrImportFileFormat = Outliner.fileFormatManager.getImportFormat(docInfo.getFileFormat());
 				break ;
 
 			default:
 				// Ack, this shouldn't happen.
 				// illegal/unknown mode specification
 				System.out.println("FileMenu:OpenFile: bad mode parameter");
 				return FAILURE;
 			} // end switch
 
 		// if we couldn't get a file format object reference ...
 		if (openOrImportFileFormat == null) {
 			msg = GUITreeLoader.reg.getText("error_could_not_open_no_file_format");
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, docInfo.getPath());
 			msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_2, docInfo.getFileFormat());
 			JOptionPane.showMessageDialog(Outliner.outliner, msg);
 		} else {
 			// we got one
 			// try to open the file
 			openOrImportResult = openOrImportFileFormat.open(tree, docInfo, docInfo.getInputStream());
 
 			// if we couldn't ....
 			if (openOrImportResult == FAILURE) {
 				msg = GUITreeLoader.reg.getText("error_could_not_open_file");
 				msg = Replace.replace(msg,GUITreeComponentRegistry.PLACEHOLDER_1, docInfo.getPath());
 
 				JOptionPane.showMessageDialog(Outliner.outliner, msg);
 				RecentFilesList.removeFileNameFromList(docInfo);
 			} else if (openOrImportResult != FAILURE_USER_ABORTED) {
 				// Deal with a childless RootNode or an Empty or Null Tree
 				if ((tree.getRootNode() == null) || (tree.getRootNode().numOfChildren() <= 0)) {
 					tree.reset();
 				} // end if
 			} // end else if
 		} // end else
 
 		// no matter what happened,
 		// reset the input stream in the docInfo
 		docInfo.setInputStream(null);
 
 		// return the results of our efforts
 		return openOrImportResult;
 
 	} // end method openOrImportFileAndGetTree
 
 
 	// import a file
 	// similar to opening a file
 	// key diff is that we assume we can't save it
 	// calls the master opening method, mode set to IMPORT
 	protected static void importFile(DocumentInfo docInfo, FileProtocol protocol) {
 
 		openFile (docInfo, protocol, MODE_IMPORT) ;
 
 	} // end method importFile
 
 	// open a file
 	// calls the master opening method, mode set to OPEN
 	protected static void openFile(DocumentInfo docInfo, FileProtocol protocol) {
 
 		openFile (docInfo, protocol, MODE_OPEN) ;
 
 	} // end method openFile
 
 
 	// open or import a file
 	protected static void openFile(DocumentInfo docInfo, FileProtocol protocol, int mode) {
 
 		// create a fresh new tree
 		JoeTree tree = Outliner.newTree(null);
 		
 		// try to open the file and pour its data into that tree
 		int openOrImportResult = openOrImportFileAndGetTree(tree, docInfo, protocol, mode);
 
 		// if things didn't go well, abort the mission
 		if ((openOrImportResult != SUCCESS) && (openOrImportResult != SUCCESS_MODIFIED)) { // Might be good to have codes we can do % on.
 			return;
 		} // end if
 
 		// if mode is invalid, abort
 		switch (mode) {
 			case MODE_OPEN:
 			case MODE_IMPORT:
 				break ;
 			default:
 				// ack, as they say
 				// we should never get here
 				System.out.println("FileMenu:OpenFile: invalid mode parameter");
 				return ;
 			} // end switch
 
 		// Create a new document
 		OutlinerDocument newDoc = new OutlinerDocument(docInfo.getPath(), docInfo);
 
 		// give it the docInfo we've got
 		newDoc.setDocumentInfo(docInfo);
 
 		// hook the outline tree to the doc, and the doc to the outline tree
 		tree.setDocument(newDoc);
 		newDoc.tree = tree;
 
 		// [srk] bug:we can get to this
 		// point with no line ending set
 		//  fix: set it to current pref
 		// if lineEnding is not yet set ...
 		if (docInfo.getLineEnding().length() == 0) {
 			docInfo.setLineEnding (Preferences.getPreferenceLineEnding(Preferences.SAVE_LINE_END).cur);
 		} // end if
 
 		// [srk] bug:we can get to this
 		// point with no owner name
 		//  fix: set it to current pref
 		// if ownerName is not yet set ...
 		if (docInfo.getOwnerName().length() == 0) {
 			docInfo.setOwnerName(Preferences.getPreferenceString(Preferences.OWNER_NAME).cur);
 		} // end if
 
 		// [srk] bug:we can get to this
 		// point with no owner email
 		//  fix: set it to current pref
 		// if ownerEmail is not yet set ...
 		if (docInfo.getOwnerEmail().length() == 0) {
 			docInfo.setOwnerEmail(Preferences.getPreferenceString(Preferences.OWNER_EMAIL).cur);
 		} // end if
 
 		// Update DocumentSettings
 		//newDoc.settings.syncPrefs();
 		newDoc.settings.lineEnd.def = docInfo.getLineEnding();
 		newDoc.settings.lineEnd.restoreCurrentToDefault();
 		newDoc.settings.lineEnd.restoreTemporaryToDefault();
 
 		newDoc.settings.saveEncoding.def = docInfo.getEncodingType();
 		newDoc.settings.saveEncoding.restoreCurrentToDefault();
 		newDoc.settings.saveEncoding.restoreTemporaryToDefault();
 
 		// if we were imported ....
 		if (mode == MODE_IMPORT) {
 			// the doc's default save format is the app's default save format
 			newDoc.settings.saveFormat.def = Preferences.getPreferenceString(Preferences.SAVE_FORMAT).cur;
 		// else we weren't imported
 		} else {
 			// the doc's default save format is its existing format
 			newDoc.settings.saveFormat.def = docInfo.getFileFormat();
 		} // end else
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
 
 		// make any final modal adjustments
 		switch (mode) {
 			case MODE_IMPORT:
 				// we were imported
 				docInfo.setImported(true) ;
 				break ;
 
 			case MODE_OPEN:
 				break ;
 
 			default:
 				break ;
 		} // end switch
 
 		// make sure we're in the Recent Files list
 		RecentFilesList.addFileNameToList(docInfo);
 
 		// perform final setup and draw the suckah
 		setupAndDraw(docInfo, newDoc, openOrImportResult);
 
 	} // end method openFile
 
 	// revert a file to it's previous state
 	// TBD get this to work with IMPORTs
 	protected static void revertFile(OutlinerDocument document) {
 		// get the document info and file protocol
 		DocumentInfo docInfo = document.getDocumentInfo();
 		FileProtocol protocol = Outliner.fileProtocolManager.getProtocol(docInfo.getProtocolName());
 
 		// set mode based on whether we were OPENed or IMPORTed
 		int mode = docInfo.isImported()?MODE_IMPORT:MODE_OPEN;
 
 		// create a fresh new tree
 		JoeTree tree = Outliner.newTree(null);
 		
 		// try to open the file and pour its data into that tree
 		int openOrImportResult = openOrImportFileAndGetTree(tree, docInfo, protocol, mode);
 
 		// if we failed somehow, bag it
 		if ((openOrImportResult != SUCCESS) && (openOrImportResult != SUCCESS_MODIFIED)) { // Might be good to have codes we can do % on.
 			return;
 		} // end if we failed somehow
 
 		// we succeeded
 
 		// swap in the new tree
 		tree.setDocument(document) ;
 		document.tree = tree;
 
 		// Clear the UndoQueue
 		document.undoQueue.clear();
 
 		// Clear the HoistStack
 		document.hoistStack.clear();
 
 		// make any necessary adjustments based on mode
 		// TBD as warranted
 		switch (mode) {
 			case MODE_OPEN:
 				break ;
 			case MODE_IMPORT:
 				break ;
 			default:
 				break ;
 			} // end switch
 
 
 		// okay, let's finish up and draw the suckah
 		setupAndDraw(docInfo, document, openOrImportResult);
 
 		} // end method Revert
 
 
 	private static void setupAndDraw(DocumentInfo docInfo, OutlinerDocument doc, int openOrImportResult) {
 		String title ;
 		
 		// grab a ref to the tree
 		JoeTree tree = doc.tree;
 		
 		// grab the path
 		String filename = docInfo.getPath();
 
 		// Clear current selection
 		tree.clearSelection();
 
 		// Clear the VisibleNodeCache
 		tree.getVisibleNodes().clear();
 
 		// Insert nodes into the VisibleNodes Cache
 		for (int i = 0; i < tree.getRootNode().numOfChildren(); i++) {
 			tree.addNode(tree.getRootNode().getChild(i));
 		}
 
 		// Update the menuBar
 		doc.setFileName(filename);
 		doc.setFileModified(false);
 		// doc.setTitle(filename) ;
 		
 		// case out on the form to build the title
 		switch (doc.getTitleNameForm()) {
 		
 		case FULL_PATHNAME:
 		default: 
 			title = filename ;
 			break ;
 			
 		case TRUNC_PATHNAME: 
 			title = StanStringTools.getTruncatedPathName(filename, TRUNC_STRING) ;
 			break ;
 			
 		case JUST_FILENAME: 
 			title = StanStringTools.getFileNameFromPathName(filename) ;
 			break ;
 			
 		} // end switch
 		
 		// set the title
 		doc.setTitle(title) ;
 
 		// update window menu entry
 		WindowMenu.updateWindow(doc) ;
 		
 		// Expand Nodes
 		ArrayList expandedNodes = docInfo.getExpandedNodes();
 		for (int i = 0; i < expandedNodes.size(); i++) {
 			int nodeNum = ((Integer) expandedNodes.get(i)).intValue();
 			try {
 				Node node = doc.tree.getVisibleNodes().get(nodeNum);
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
 			firstVisibleNode = tree.getVisibleNodes().get(index);
 		} catch (IndexOutOfBoundsException e) {
 			index = 0;
 			firstVisibleNode = tree.getVisibleNodes().get(0);
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
 		if (openOrImportResult == SUCCESS_MODIFIED) {
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
 	public static void updateFileMenuItems() {
 		// grab the menu items
 		JMenuItem saveItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SAVE_MENU_ITEM);
 		JMenuItem saveAsItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SAVE_AS_MENU_ITEM);
 		JMenuItem revertItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.REVERT_MENU_ITEM);
 		JMenuItem closeItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.CLOSE_MENU_ITEM);
 		JMenuItem closeAllItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.CLOSE_ALL_MENU_ITEM);
 		JMenuItem exportItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.EXPORT_MENU_ITEM);
 		JMenuItem exportSelectionItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.EXPORT_SELECTION_MENU_ITEM);
 
 		// try to grab the topmost doc
 		OutlinerDocument topmostDoc = Outliner.getMostRecentDocumentTouched();
 
 		// if there was none ...
 		if (topmostDoc == null) {
 			saveItem.setEnabled(false);
 			saveAsItem.setEnabled(false);
 			revertItem.setEnabled(false);
 			exportItem.setEnabled(false);
 			exportSelectionItem.setEnabled(false);
 			closeItem.setEnabled(false);
 			closeAllItem.setEnabled(false);
 		// else if it has no name (e.g., it's a new doc, not yet saved) ...
 		} else if (topmostDoc.getFileName().equals("")) {
 			saveItem.setEnabled(true);
 			saveAsItem.setEnabled(true);
 			revertItem.setEnabled(false);
 			exportItem.setEnabled(true);
 			closeItem.setEnabled(true);
 			closeAllItem.setEnabled(true);
 		// else if it has a name, thus it's not a new doc, and it's been modified
 		} else if (topmostDoc.isFileModified()) {
 			saveItem.setEnabled(! topmostDoc.getDocumentInfo().isImported());
 			saveAsItem.setEnabled(true);
 			revertItem.setEnabled(true);
 			exportItem.setEnabled(true);
 			closeItem.setEnabled(true);
 			closeAllItem.setEnabled(true);
 		// else it has a name, but has not been modified
 		} else {
 			saveItem.setEnabled(false);
 			saveAsItem.setEnabled(true);
 			revertItem.setEnabled(false);
 			exportItem.setEnabled(true);
 			closeItem.setEnabled(true);
 			closeAllItem.setEnabled(true);
 		}
 	}
 
 	public static void updateSaveAllMenuItem() {
 		// start out disabled
 		boolean enabledState = false;
 
 		// for each open document ...
 		for (int i = 0; i < Outliner.openDocumentCount(); i++) {
 
 			// get its doc reference
 			OutlinerDocument doc = Outliner.getDocument(i);
 
 			// if it wasn't imported ....
 			if (! doc.getDocumentInfo().isImported()) {
 
 				// if it's been modified or it's a new and unsaved doc
 				if (doc.isFileModified() || doc.getFileName().equals("")) {
 
 					// enable Save All
 					enabledState = true;
 
 					// break outta the loop, we're done
 					break;
 				} // end if modified or not yet saved
 
 			}
 		}
 
 		JMenuItem saveAllItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SAVE_ALL_MENU_ITEM);
 		saveAllItem.setEnabled(enabledState);
 	}
 }
