 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.internal.ui.text.hover;
 
 import java.util.Iterator;
 
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.filebuffers.ITextFileBufferManager;
 import org.eclipse.core.filebuffers.LocationKind;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.internal.ui.editor.ScriptAnnotationIterator;
 import org.eclipse.dltk.internal.ui.text.HTMLPrinter;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.utils.TextUtils;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IStorageEditorInput;
 import org.eclipse.ui.editors.text.EditorsUI;
 import org.eclipse.ui.texteditor.AnnotationPreference;
 import org.eclipse.ui.texteditor.ChainedPreferenceStore;
 import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
 
 /**
  * Abstract super class for annotation hovers.
  * 
  */
 public abstract class AbstractAnnotationHover extends
 		AbstractScriptEditorTextHover {
 
 	protected DefaultMarkerAnnotationAccess fAnnotationAccess = new DefaultMarkerAnnotationAccess();
 	protected boolean fAllAnnotations;
 
 	public AbstractAnnotationHover(boolean allAnnotations) {
 		fAllAnnotations = allAnnotations;
 	}
 
 	protected String postUpdateMessage(String message) {
 		return message;
 	}
 
 	/*
 	 * Formats a message as HTML text.
 	 */
 	protected String formatMessage(String message) {
 		StringBuffer buffer = new StringBuffer();
 		HTMLPrinter.insertPageProlog(buffer, 0, getStyleSheet());
 		buffer.append(postUpdateMessage(TextUtils.escapeHTML(message)));
 		HTMLPrinter.addPageEpilog(buffer);
 		return buffer.toString();
 	}
 
 	/*
 	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
 	 */
 	@Override
 	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
 		IPath path;
 		IAnnotationModel model;
 
 		if (textViewer instanceof ISourceViewer) {
 			path = null;
 			model = ((ISourceViewer) textViewer).getAnnotationModel();
 		} else {
 			// Get annotation model from file buffer manager
 			path = getEditorInputPath();
 			model = getAnnotationModel(path);
 		}
 
 		if (model == null) {
 			return null;
 		}
 
 		try {
 			final IPreferenceStore store = getCombinedPreferenceStore();
 			Iterator<Annotation> e = new ScriptAnnotationIterator(model, true,
 					fAllAnnotations);
 			int layer = -1;
 			String message = null;
 			boolean multi = false;
 			while (e.hasNext()) {
 				Annotation a = e.next();
 
 				AnnotationPreference preference = getAnnotationPreference(a);
 				if (preference == null) {
 					continue;
 				}
 				if (!isActive(store, preference.getTextPreferenceKey())
 						&& !isActive(store,
 								preference.getHighlightPreferenceKey())) {
 					continue;
 				}
 
 				Position p = model.getPosition(a);
 
 				int l = fAnnotationAccess.getLayer(a);
 
 				if (l >= layer
 						&& p != null
 						&& p.overlapsWith(hoverRegion.getOffset(),
 								hoverRegion.getLength())) {
 					String msg = getMessageFromAnnotation(a);
 					if (msg != null && msg.trim().length() > 0) {
 						if (message != null) {
 							message = message + "\n-" + msg;
 							multi = true;
 						} else {
 							message = msg;
 						}
 						layer = l;
 					}
 				}
 			}
			if (layer > -1)
			{
				if (multi)
				{
					message = "Multiply Markers:\n-" + message;
 				}
 				return formatMessage(message);
 			}
 
 		} finally {
 			try {
 				if (path != null) {
 					ITextFileBufferManager manager = FileBuffers
 							.getTextFileBufferManager();
 					manager.disconnect(path, LocationKind.NORMALIZE, null);
 				}
 			} catch (CoreException ex) {
 				DLTKUIPlugin.log(ex.getStatus());
 			}
 		}
 
 		return null;
 	}
 
 	protected String getMessageFromAnnotation(Annotation a) {
 		return a.getText();
 	}
 
 	protected static boolean isActive(IPreferenceStore store, String preference) {
 		return preference != null && store.getBoolean(preference);
 	}
 
 	private IPreferenceStore combinedStore = null;
 
 	protected synchronized IPreferenceStore getCombinedPreferenceStore() {
 		if (combinedStore == null) {
 			combinedStore = new ChainedPreferenceStore(new IPreferenceStore[] {
 					getPreferenceStore(), EditorsUI.getPreferenceStore() });
 		}
 		return combinedStore;
 	}
 
 	protected IPath getEditorInputPath() {
 		if (getEditor() == null)
 			return null;
 
 		IEditorInput input = getEditor().getEditorInput();
 		if (input instanceof IStorageEditorInput) {
 			try {
 				return ((IStorageEditorInput) input).getStorage().getFullPath();
 			} catch (CoreException ex) {
 				DLTKUIPlugin.log(ex.getStatus());
 			}
 		}
 		return null;
 	}
 
 	protected IAnnotationModel getAnnotationModel(IPath path) {
 		if (path == null)
 			return null;
 
 		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
 		try {
 			manager.connect(path, LocationKind.NORMALIZE, null);
 		} catch (CoreException ex) {
 			DLTKUIPlugin.log(ex.getStatus());
 			return null;
 		}
 
 		IAnnotationModel model = null;
 		try {
 			model = manager.getTextFileBuffer(path, LocationKind.NORMALIZE)
 					.getAnnotationModel();
 			return model;
 		} finally {
 			if (model == null) {
 				try {
 					manager.disconnect(path, LocationKind.NORMALIZE, null);
 				} catch (CoreException ex) {
 					DLTKUIPlugin.log(ex.getStatus());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns the annotation preference for the given annotation.
 	 * 
 	 * @param annotation
 	 *            the annotation
 	 * @return the annotation preference or <code>null</code> if none
 	 */
 	protected AnnotationPreference getAnnotationPreference(Annotation annotation) {
 		if (annotation.isMarkedDeleted()) {
 			return null;
 		}
 
 		return EditorsUI.getAnnotationPreferenceLookup()
 				.getAnnotationPreference(annotation);
 	}
 }
