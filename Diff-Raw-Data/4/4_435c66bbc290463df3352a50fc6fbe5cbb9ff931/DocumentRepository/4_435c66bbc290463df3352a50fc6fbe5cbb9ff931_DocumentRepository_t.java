 /**
  * Copyright (C) 2000, 2001 Maynard Demmon, maynard@organic.com
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
 
 package com.organic.maynard.outliner.dom;
 
 import java.util.*;
 import com.organic.maynard.outliner.*;
 import com.organic.maynard.outliner.util.find.*;
 import com.organic.maynard.outliner.event.*;
 
 /**
  * @author  $Author$
  * @version $Revision$, $Date$
  */
 
 public class DocumentRepository {
 	
 	// Instance Fields
 	private ArrayList openDocuments = new ArrayList();
 	private Document mostRecentDocumentTouched = null;
 	
 	private ArrayList documentRepositoryListeners = new ArrayList();
 	private ArrayList documentListeners = new ArrayList();
 	private ArrayList outlinerDocumentListeners = new ArrayList();
 	private ArrayList treeSelectionListeners = new ArrayList();
 	private ArrayList undoQueueListeners = new ArrayList();
 	
 	
 	// Constructors
 	public DocumentRepository() {}
 	
 	
 	// TreeSelectionEvent Handling
 	public void addTreeSelectionListener(TreeSelectionListener l) {
 		treeSelectionListeners.add(l);
 	}
 	
 	public void removeTreeSelectionListener(TreeSelectionListener l) {
 		treeSelectionListeners.remove(l);
 	}
 	
 	public TreeSelectionListener[] getTreeSelectionListeners() {
 		return (TreeSelectionListener[]) treeSelectionListeners.toArray();
 	}
 	
 	private TreeSelectionEvent selectionChangedEvent = new TreeSelectionEvent(null, TreeSelectionEvent.UNKNOWN_SELECTION);
 	
 	public void fireSelectionChangedEvent(JoeTree tree, int type) {
 		selectionChangedEvent.setTree(tree);
 		selectionChangedEvent.setType(type);
 		for (int i = 0, limit = treeSelectionListeners.size(); i < limit; i++) {
 			((TreeSelectionListener) treeSelectionListeners.get(i)).selectionChanged(selectionChangedEvent);
 		}
 		
 		// Cleanup
 		selectionChangedEvent.setTree(null);
 	}
 	
 	
 	// UndoQueueEvent Handling
 	public void addUndoQueueListener(UndoQueueListener l) {
 		undoQueueListeners.add(l);
 	}
 	
 	public void removeUndoQueueListener(UndoQueueListener l) {
 		undoQueueListeners.remove(l);
 	}
 	
 	public UndoQueueListener[] getUndoQueueListeners() {
 		return (UndoQueueListener[]) undoQueueListeners.toArray();
 	}
 	
 	private UndoQueueEvent undoEvent = new UndoQueueEvent(null, UndoQueueEvent.UNKNOWN);
 	
 	public void fireUndoQueueEvent(Document doc, int type) {
 		undoEvent.setDocument(doc);
 		undoEvent.setType(type);
 		for (int i = 0, limit = undoQueueListeners.size(); i < limit; i++) {
 			((UndoQueueListener) undoQueueListeners.get(i)).undo(undoEvent);
 		}
 		
 		// Cleanup
 		undoEvent.setDocument(null);
 	}
 	
 	
 	// DocumentEvent Handling
 	public void addDocumentListener(DocumentListener l) {
 		documentListeners.add(l);
 	}
 	
 	public void removeDocumentListener(DocumentListener l) {
 		documentListeners.remove(l);
 	}
 	
 	public DocumentListener[] getDocumentListeners() {
 		return (DocumentListener[]) documentListeners.toArray();
 	}
 	
 	private DocumentEvent modifiedStateChangedEvent = new DocumentEvent(null);
 	
 	public void fireModifiedStateChangedEvent(Document doc) {
 		modifiedStateChangedEvent.setDocument(doc);
 		for (int i = 0, limit = documentListeners.size(); i < limit; i++) {
 			((DocumentListener) documentListeners.get(i)).modifiedStateChanged(modifiedStateChangedEvent);
 		}
 		for (int i = 0, limit = outlinerDocumentListeners.size(); i < limit; i++) {
 			((DocumentListener) outlinerDocumentListeners.get(i)).modifiedStateChanged(modifiedStateChangedEvent);
 		}
 		
 		// Cleanup
 		modifiedStateChangedEvent.setDocument(null);
 	}
 	
 	
 	// OutlinerDocumentEvent Handling
 	public void addOutlinerDocumentListener(DocumentListener l) {
 		outlinerDocumentListeners.add(l);
 	}
 	
 	public void removeOutlinerDocumentListener(DocumentListener l) {
 		outlinerDocumentListeners.remove(l);
 	}
 	
 	public OutlinerDocumentListener[] getOutlinerDocumentListeners() {
 		return (OutlinerDocumentListener[]) outlinerDocumentListeners.toArray();
 	}
 	
 	private OutlinerDocumentEvent attributesVisibilityChangedEvent = new OutlinerDocumentEvent(null);
 	private OutlinerDocumentEvent hoistDepthChangedEvent = new OutlinerDocumentEvent(null);
 	
 	public void fireAttributesVisibilityChangedEvent(OutlinerDocument doc) {
 		attributesVisibilityChangedEvent.setOutlinerDocument(doc);
 		for (int i = 0, limit = outlinerDocumentListeners.size(); i < limit; i++) {
 			((OutlinerDocumentListener) outlinerDocumentListeners.get(i)).attributesVisibilityChanged(attributesVisibilityChangedEvent);
 		}
 		
 		// Cleanup
 		attributesVisibilityChangedEvent.setDocument(null);
 	}
 	
 	public void fireHoistDepthChangedEvent(OutlinerDocument doc) {
 		hoistDepthChangedEvent.setOutlinerDocument(doc);
 		for (int i = 0, limit = outlinerDocumentListeners.size(); i < limit; i++) {
 			((OutlinerDocumentListener) outlinerDocumentListeners.get(i)).hoistDepthChanged(hoistDepthChangedEvent);
 		}
 		
 		// Cleanup
 		hoistDepthChangedEvent.setDocument(null);
 	}
 	
 	
 	// DocumentRepositoryEvent Handling
 	public void addDocumentRepositoryListener(DocumentRepositoryListener l) {
 		documentRepositoryListeners.add(l);
 	}
 	
 	public void removeDocumentRepositoryListener(DocumentRepositoryListener l) {
 		documentRepositoryListeners.remove(l);
 	}
 	
 	public DocumentRepositoryListener[] getDocumentRepositoryListeners() {
 		return (DocumentRepositoryListener[]) documentRepositoryListeners.toArray();
 	}
 	
 	private DocumentRepositoryEvent addedEvent = new DocumentRepositoryEvent(null);
 	private DocumentRepositoryEvent removedEvent = new DocumentRepositoryEvent(null);
 	private DocumentRepositoryEvent changedMostRecentDocumentTouchedEvent = new DocumentRepositoryEvent(null);
 	
 	protected void fireDocumentAddedEvent(Document doc) {
 		addedEvent.setDocument(doc);
 		for (int i = 0, limit = documentRepositoryListeners.size(); i < limit; i++) {
 			((DocumentRepositoryListener) documentRepositoryListeners.get(i)).documentAdded(addedEvent);
 		}
 		
 		// Cleanup
 		addedEvent.setDocument(null);
 	}
 	
 	protected void fireDocumentRemovedEvent(Document doc) {
 		removedEvent.setDocument(doc);
 		for (int i = 0, limit = documentRepositoryListeners.size(); i < limit; i++) {
 			((DocumentRepositoryListener) documentRepositoryListeners.get(i)).documentRemoved(removedEvent);
 		}
 		
 		// Cleanup
 		removedEvent.setDocument(null);
 	}
 	
 	protected void fireChangedMostRecentDocumentTouchedEvent(Document doc) {
 		changedMostRecentDocumentTouchedEvent.setDocument(doc);
 		for (int i = 0, limit = documentRepositoryListeners.size(); i < limit; i++) {
 			((DocumentRepositoryListener) documentRepositoryListeners.get(i)).changedMostRecentDocumentTouched(changedMostRecentDocumentTouchedEvent);
 		}
 		
 		// Cleanup
 		changedMostRecentDocumentTouchedEvent.setDocument(null);
 	}
 	
 	
 	// Accessors
 	public Document getMostRecentDocumentTouched() {
 		return this.mostRecentDocumentTouched;
 	}
 	
 	public void setMostRecentDocumentTouched(Document doc) {
 		this.mostRecentDocumentTouched = doc;
 		
 		// Fire Event
 		fireChangedMostRecentDocumentTouchedEvent(doc);
 	}
 	
 	public void addDocument(Document doc) {
 		openDocuments.add(doc);
 		
 		// Register the Document
 		doc.setDocumentRepository(this);
 		
 		// Fire Event
 		fireDocumentAddedEvent(doc);
 	}
 	
 	
 	public int indexOfOpenDocument(Document doc) {
 		return openDocuments.indexOf(doc);
 	}
 	
 	public Document getDocument(int i) {
 		return (Document) openDocuments.get(i);
 	}
 	
 	
 	public Document getDocument(String name) {
 		for (int i = 0, limit = openDocuments.size(); i < limit; i++) {
 			Document doc = getDocument(i);
 			if (name.equals(doc.getFileName())) {
 				return doc;
 			}
 		}
 		return null;
 	}
 	
 	public void removeDocument(Document doc) {
 		openDocuments.remove(doc);
 		
 		// Fire Event
 		fireDocumentRemovedEvent(doc);
 		
 		// Unregister the Document
 		doc.setDocumentRepository(null);
 		
 		// Select the last non-iconified document in the window menu and 
 		// change to it. Otherwise change to an iconified doc. Otherwise, change to null.
 		if (mostRecentDocumentTouched == doc) {
 			setMostRecentDocumentTouched(null);
 			if (openDocumentCount() > 0) {
 				for (int i = openDocumentCount() - 1; i >= 0; i--) {
 					Document newDoc = getDocument(i);
 					if (!newDoc.isIcon() || i == 0) {
 						Outliner.menuBar.windowMenu.changeToWindow((OutlinerDocument) newDoc);
 						return;
 					}
 				}
 			}
			
			// We've closed the last document. We need to set focus 
			// somewhere, otherwise the ctrl-q, ctrl-n keys, etc. won't work correctly.
			Outliner.desktop.requestFocus();
 		}
 	}
 	
 	public int openDocumentCount() {
 		return openDocuments.size();
 	}
 	
 	public boolean isFileNameUnique(String filename) {
 		for (int i = 0, limit = openDocuments.size(); i < limit; i++) {
 			if (PlatformCompatibility.areFilenamesEquivalent(filename, getDocument(i).getFileName())) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	
 	// Iterator Methods
 	public Iterator getOpenDocumentIterator(int index) {
 		ArrayList temp = new ArrayList();
 		temp.addAll(openDocuments.subList(index, openDocuments.size()));
 		temp.addAll(openDocuments.subList(0, index));
 		return temp.iterator();
 	}
 	
 	public Iterator getLoopedOpenDocumentIterator() {
 		return getOpenDocumentIterator(indexOfOpenDocument(mostRecentDocumentTouched));
 	}
 	
 	public Iterator getDefaultOpenDocumentIterator() {
 		return getOpenDocumentIterator(0);
 	}
 	
 	
 	// Misc Methods
 	public void redrawAllOpenDocuments() {
 		for (int i = 0, limit = openDocuments.size(); i < limit; i++) {
 			OutlinerDocument doc = (OutlinerDocument) getDocument(i);
 			
 			// Only request focus for the current document.
 			if (doc == getMostRecentDocumentTouched()) {
 				doc.panel.layout.redraw();
 			} else {
 				doc.panel.layout.draw();
 			}
 		}
 	}
 }
