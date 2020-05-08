 /*******************************************************************************
  * Copyright (c) 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the annotation model for the R4E annotations.
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.annotation.content;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.AnnotationModelEvent;
 import org.eclipse.jface.text.source.IAnnotationModelListener;
 import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
 import org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotation;
 import org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotationModel;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EID;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIContent;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIContentsContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIDelta;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIPostponedFile;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUISelection;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class R4EAnnotationModel implements IReviewAnnotationModel {
 
 	// ------------------------------------------------------------------------
 	// Members
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fAnnotationModelListeners.
 	 */
 	private final Set<IAnnotationModelListener> fAnnotationModelListeners = new HashSet<IAnnotationModelListener>(2);
 
 	/**
 	 * Field fAnnotationsMap.
 	 */
 	private final Map<R4EID, IReviewAnnotation> fAnnotationsMap = new HashMap<R4EID, IReviewAnnotation>();
 
 	/**
 	 * Field fSortedAnnotationsList.
 	 */
 	private final Map<String, List<IReviewAnnotation>> fSortedAnnotationsListsMap = new HashMap<String, List<IReviewAnnotation>>();
 
 	/**
 	 * Field fSortedAnnotationsIndexMap.
 	 */
 	private final Map<String, Integer> fSortedAnnotationsIndexMap = new HashMap<String, Integer>();
 
 	/**
 	 * Field fFileContext.
 	 */
 	private R4EUIFileContext fFileContext = null;
 
 	/**
 	 * Field fDocument.
 	 */
 	private IDocument fDocument;
 
 	/**
 	 * Field fDocumentListener.
 	 */
 	private final IDocumentListener fDocumentListener = new IDocumentListener() {
 		public void documentAboutToBeChanged(DocumentEvent aEvent) {
 			//not used for now
 		}
 
 		public void documentChanged(DocumentEvent aEvent) {
 			//not used for now
 		}
 	};
 
 	/**
 	 * Field ANNOTATION_COMPARATOR.
 	 */
 	private static final Comparator<IReviewAnnotation> ANNOTATION_COMPARATOR = new Comparator<IReviewAnnotation>() {
 		// This is where the sorting happens.
 		public int compare(IReviewAnnotation aObject1, IReviewAnnotation aObject2) {
 			final int sortOrder = aObject1.getPosition().getOffset() - aObject2.getPosition().getOffset();
 			if (sortOrder == 0) {
 				return aObject1.getPosition().getLength() - aObject2.getPosition().getLength();
 			}
 			return sortOrder;
 		}
 	};
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method getAnnotationIterator.
 	 * 
 	 * @return Iterator<IReviewAnnotation>
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#getAnnotationIterator()
 	 */
 	public Iterator<IReviewAnnotation> getAnnotationIterator() {
 		return fAnnotationsMap.values().iterator();
 	}
 
 	/**
 	 * Method setFile.
 	 * 
 	 * @param aFileContext
 	 *            Object
 	 */
 	public void setFile(Object aFileContext) {
 		fFileContext = (R4EUIFileContext) aFileContext;
 		fFileContext.registerAnnotationModel(this);
 	}
 
 	/**
 	 * Method getPosition.
 	 * 
 	 * @param aAnnotation
 	 *            Annotation
 	 * @return Position
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#getPosition(Annotation)
 	 */
 	public Position getPosition(Annotation aAnnotation) {
 		if (aAnnotation instanceof IReviewAnnotation) {
 			return ((IReviewAnnotation) aAnnotation).getPosition();
 		} else {
 			// we dont understand any other annotations
 			return null;
 		}
 	}
 
 	/**
 	 * Method addAnnotationModelListener.
 	 * 
 	 * @param aListener
 	 *            IAnnotationModelListener
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#addAnnotationModelListener(IAnnotationModelListener)
 	 */
 	public void addAnnotationModelListener(IAnnotationModelListener aListener) {
 		fAnnotationModelListeners.add(aListener);
 	}
 
 	/**
 	 * Method removeAnnotationModelListener.
 	 * 
 	 * @param aListener
 	 *            IAnnotationModelListener
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#removeAnnotationModelListener(IAnnotationModelListener)
 	 */
 	public void removeAnnotationModelListener(IAnnotationModelListener aListener) {
 		fAnnotationModelListeners.remove(aListener);
 	}
 
 	/**
 	 * Method connect.
 	 * 
 	 * @param aDocument
 	 *            IDocument
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#connect(IDocument)
 	 */
 	public void connect(IDocument aDocument) {
 		fDocument = aDocument;
 		if (fDocument.getLength() > 0) {
 			for (IReviewAnnotation annotation : fAnnotationsMap.values()) {
 				try {
 					fDocument.addPosition(annotation.getPosition());
 				} catch (BadLocationException e) {
 					R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e); //$NON-NLS-1$
 				}
 			}
 			fDocument.addDocumentListener(fDocumentListener);
 			refreshAnnotations();
 		}
 	}
 
 	/**
 	 * Method disconnect.
 	 * 
 	 * @param aDocument
 	 *            IDocument
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#disconnect(IDocument)
 	 */
 	public void disconnect(IDocument aDocument) {
 		for (IReviewAnnotation annotation : fAnnotationsMap.values()) {
 			aDocument.removePosition(annotation.getPosition());
 		}
 		aDocument.removeDocumentListener(fDocumentListener);
 		fDocument = null;
 	}
 
 	/**
 	 * Method clearAnnotations.
 	 */
 	public void clearAnnotations() {
 		final AnnotationModelEvent event = new AnnotationModelEvent(this);
 		clear(event);
 		fireModelChanged(event);
 	}
 
 	/**
 	 * Method clear.
 	 * 
 	 * @param aEvent
 	 *            AnnotationModelEvent
 	 */
 	private void clear(AnnotationModelEvent aEvent) {
 		for (IReviewAnnotation annotation : fAnnotationsMap.values()) {
 			aEvent.annotationRemoved((R4EAnnotation) annotation, annotation.getPosition());
 		}
 		fAnnotationsMap.clear();
 		for (List<IReviewAnnotation> annotationList : fSortedAnnotationsListsMap.values()) {
 			annotationList.clear();
 		}
 		fSortedAnnotationsIndexMap.clear();
 	}
 
 	/**
 	 * Method refreshAnnotations.
 	 */
 	public void refreshAnnotations() {
 		final AnnotationModelEvent event = new AnnotationModelEvent(this);
 		clear(event);
 		if ((fDocument != null) && (fFileContext != null)) {
 			R4EUIAnomalyContainer anomalies = fFileContext.getAnomalyContainerElement();
 			if (null != anomalies) {
 				for (IR4EUIModelElement anomaly : anomalies.getChildren()) {
 					addAnnotation(fDocument, event, anomaly);
 				}
 			}
 			R4EUIContentsContainer contents = fFileContext.getContentsContainerElement();
 			if (null != contents) {
 				for (IR4EUIModelElement content : fFileContext.getContentsContainerElement().getChildren()) {
 					addAnnotation(fDocument, event, content);
 				}
 			}
 			if (fFileContext instanceof R4EUIPostponedFile) {
 				IR4EUIModelElement[] postponedAnomalies = ((R4EUIPostponedFile) fFileContext).getChildren();
 				for (IR4EUIModelElement anomaly : postponedAnomalies) {
 					addAnnotation(fDocument, event, anomaly);
 				}
 			}
 		}
 		fireModelChanged(event);
 	}
 
 	/**
 	 * Method updateAnnotation.
 	 * 
 	 * @param aAnnotationContent
 	 *            Object
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotationModel#updateAnnotation(Object)
 	 */
 	public void updateAnnotation(Object aAnnotationContent) {
 		removeAnnotation(aAnnotationContent);
 		addAnnotation(aAnnotationContent);
 	}
 
 	/**
 	 * Method addAnnotation.
 	 * 
 	 * @param aAnnotationContent
 	 *            Object
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotationModel#addAnnotation(Object)
 	 */
 	public void addAnnotation(Object aAnnotationContent) {
 		final AnnotationModelEvent event = new AnnotationModelEvent(this);
 		if ((fDocument != null) && (fFileContext != null)) {
 			addAnnotation(fDocument, event, aAnnotationContent);
 		}
 		fireModelChanged(event);
 	}
 
 	/**
 	 * Method addAnnotation.
 	 * 
 	 * @param aDocument
 	 *            IDocument
 	 * @param aEvent
 	 *            AnnotationModelEvent
 	 * @param aAnnotationContent
 	 *            Object
 	 */
 	private void addAnnotation(IDocument aDocument, AnnotationModelEvent aEvent, Object aAnnotationContent) {
 		R4EAnnotation newAnnotation = null;
 		if (aAnnotationContent instanceof R4EUIAnomalyBasic) {
 			newAnnotation = new R4EAnomalyAnnotation((R4EUIAnomalyBasic) aAnnotationContent);
 		} else if (aAnnotationContent instanceof R4EUIDelta) {
 			newAnnotation = new R4EDeltaAnnotation((R4EUIDelta) aAnnotationContent);
 		} else if (aAnnotationContent instanceof R4EUISelection) {
 			newAnnotation = new R4ESelectionAnnotation((R4EUISelection) aAnnotationContent);
 		}
 
 		if (null != newAnnotation) {
 			addAnnotation(newAnnotation, null);
 			aEvent.annotationAdded(newAnnotation);
 		}
 	}
 
 	/**
 	 * Method addAnnotation.
 	 * 
 	 * @param aAnnotation
 	 *            Annotation
 	 * @param aPosition
 	 *            Position
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#addAnnotation(Annotation, Position)
 	 */
 	public void addAnnotation(Annotation aAnnotation, Position aPosition) {
 		if (aAnnotation instanceof R4EAnnotation) {
 			final R4EAnnotation newAnnotation = (R4EAnnotation) aAnnotation;
 			fAnnotationsMap.put((R4EID) newAnnotation.getId(), newAnnotation);
 			List<IReviewAnnotation> annotationList = fSortedAnnotationsListsMap.get(newAnnotation.getType());
 			if (null == annotationList) {
 				annotationList = new ArrayList<IReviewAnnotation>();
 				fSortedAnnotationsListsMap.put(newAnnotation.getType(), annotationList);
 			}
 			annotationList.add(newAnnotation);
 			Collections.sort(annotationList, ANNOTATION_COMPARATOR);
 			fSortedAnnotationsIndexMap.put(newAnnotation.getType(), annotationList.indexOf(newAnnotation));
		} else {
			//should never happen
			String msg = "Cannot add invalid Annonation of type " + aAnnotation.getClass().toString();
			R4EUIPlugin.Ftracer.traceWarning(msg);
			R4EUIPlugin.getDefault().logWarning(msg, null);
 		}
 	}
 
 	/**
 	 * Method removeAnnotation.
 	 * 
 	 * @param aAnnotationContent
 	 *            Object
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotationModel#removeAnnotation(Object)
 	 */
 	public void removeAnnotation(Object aAnnotationContent) {
 		R4EID id = null;
 		if (aAnnotationContent instanceof R4EUIAnomalyBasic) {
 			id = ((R4EUIAnomalyBasic) aAnnotationContent).getAnomaly().getId();
 		} else if (aAnnotationContent instanceof R4EUIContent) {
 			id = ((R4EUIContent) aAnnotationContent).getContent().getId();
 		}
 		if (null != id) {
 			final IReviewAnnotation removedAnnotation = fAnnotationsMap.remove(id);
 			removeAnnotation((R4EAnnotation) removedAnnotation);
 			final AnnotationModelEvent event = new AnnotationModelEvent(this);
 			event.annotationRemoved((R4EAnnotation) removedAnnotation);
 			fireModelChanged(event);
 		}
 	}
 
 	/**
 	 * Method removeAnnotation.
 	 * 
 	 * @param aAnnotation
 	 *            Annotation
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#removeAnnotation(Annotation)
 	 */
 	public void removeAnnotation(Annotation aAnnotation) {
 		if (aAnnotation instanceof R4EAnnotation) {
 			final R4EAnnotation remAnnotation = (R4EAnnotation) aAnnotation;
 			fAnnotationsMap.remove(remAnnotation.getId());
 			final List<IReviewAnnotation> annotationList = fSortedAnnotationsListsMap.get(remAnnotation.getType());
 			int remAnnotationIndex = annotationList.indexOf(remAnnotation);
 			annotationList.remove(remAnnotation);
 			if (remAnnotationIndex < 0) {
 				fSortedAnnotationsIndexMap.remove(remAnnotation.getType()); //type list is now empty
 			} else {
 				fSortedAnnotationsIndexMap.put(remAnnotation.getType(), --remAnnotationIndex);
 			}
 		} else {
 			//should never happen
 			String msg = "Cannot remove invalid Annonation of type " + aAnnotation.getClass().toString();
 			R4EUIPlugin.Ftracer.traceWarning(msg);
 			R4EUIPlugin.getDefault().logWarning(msg, null);
 		}
 	}
 
 	/**
 	 * Method fireModelChanged.
 	 * 
 	 * @param aEvent
 	 *            AnnotationModelEvent
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotationModel#fireModelChanged(AnnotationModelEvent)
 	 */
 	public void fireModelChanged(AnnotationModelEvent aEvent) {
 		aEvent.markSealed();
 		if (!aEvent.isEmpty()) {
 			for (IAnnotationModelListener listener : fAnnotationModelListeners) {
 				if (listener instanceof IAnnotationModelListenerExtension) {
 					((IAnnotationModelListenerExtension) listener).modelChanged(aEvent);
 				} else {
 					listener.modelChanged(this);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Method isAnnotationsAvailable.
 	 * 
 	 * @param aType
 	 *            String
 	 * @return boolean
 	 */
 	public boolean isAnnotationsAvailable(String aType) {
 		return true; //Always return available even if there are no annotations
 	}
 
 	/**
 	 * Method getNextAnnotation.
 	 * 
 	 * @param aType
 	 *            String
 	 * @return IReviewAnnotation
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotationModel#getNextAnnotation(String)
 	 */
 	public IReviewAnnotation getNextAnnotation(String aType) {
 		final List<IReviewAnnotation> annotationList = fSortedAnnotationsListsMap.get(aType);
 		if (null == annotationList || annotationList.size() == 0) {
 			return null; //empty list
 		} else {
 			int annotationIndex = fSortedAnnotationsIndexMap.get(aType);
 			++annotationIndex;
 			if (annotationIndex == annotationList.size()) {
 				annotationIndex = 0; //wrap around
 			}
 			fSortedAnnotationsIndexMap.put(aType, annotationIndex);
 			return annotationList.get(annotationIndex);
 		}
 	}
 
 	/**
 	 * Method getPreviousAnnotation.
 	 * 
 	 * @param aType
 	 *            String
 	 * @return IReviewAnnotation
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotationModel#getPreviousAnnotation(String)
 	 */
 	public IReviewAnnotation getPreviousAnnotation(String aType) {
 		final List<IReviewAnnotation> annotationList = fSortedAnnotationsListsMap.get(aType);
 
 		if (null == annotationList || annotationList.size() == 0) {
 			return null; //empty list
 		} else {
 			int annotationIndex = fSortedAnnotationsIndexMap.get(aType);
 			--annotationIndex;
 			if (annotationIndex < 0) {
 				annotationIndex = annotationList.size() - 1; //wrap around
 			}
 			fSortedAnnotationsIndexMap.put(aType, annotationIndex);
 			return annotationList.get(annotationIndex);
 		}
 	}
 
 	//Test Methods
 
 	/**
 	 * Method findAnnotation.
 	 * 
 	 * @param aType
 	 *            String
 	 * @param aSourceElement
 	 *            Object
 	 * @return IReviewAnnotation
 	 * @see org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotationModel#findAnnotation(String, Object)
 	 */
 	public IReviewAnnotation findAnnotation(String aType, Object aSourceElement) {
 		final List<IReviewAnnotation> annotationList = fSortedAnnotationsListsMap.get(aType);
 		for (IReviewAnnotation annotation : annotationList) {
 			if (((R4EAnnotation) annotation).getSourceElement().equals(aSourceElement)) {
 				return annotation;
 			}
 		}
 		return null;
 	}
 }
