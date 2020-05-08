 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.editor;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 import org.eclipse.mylyn.docs.intent.client.ui.utils.IntentEditorOpener;
 import org.eclipse.mylyn.docs.intent.core.genericunit.IntentSectionReferenceInstruction;
 import org.eclipse.mylyn.docs.intent.serializer.ParsedElementPosition;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
  * This will allow us to plug the CTRL+click "open declaration" into Intent editors.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IntentHyperlinkDetector extends AbstractHyperlinkDetector {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer,
 	 *      org.eclipse.jface.text.IRegion, boolean)
 	 */
 	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region,
 			boolean canShowMultipleHyperlinks) {
 		ITextEditor textEditor = (ITextEditor)getAdapter(ITextEditor.class);
 		if (!textEditor.isDirty()) {
 			// NOTE: the hovered element should not be computed using the position manager as it doesn't
 			// reflects the latest changes made by the user.
 			// WORKAROUND: hyperlinks are desactivated when the editor is dirty
 			if (region != null && textEditor instanceof IntentEditor && textEditor.getEditorInput() != null) {
 				IntentEditorDocument document = (IntentEditorDocument)((IntentDocumentProvider)textEditor
 						.getDocumentProvider()).getDocument(textEditor.getEditorInput());
 				EObject element = document.getElementAtOffset(region.getOffset());
 				EObject target = getTarget(element);
 				if (target != null) {
 					// a link can be set to a target
					ParsedElementPosition actualPosition = document.getIntentPosition(element);
					Region hyperlinkRegion = new Region(actualPosition.getOffset(),
							actualPosition.getDeclarationLength());
 					return new IHyperlink[] {new IntentHyperlink((IntentEditor)textEditor, hyperlinkRegion,
 							target),
 					};
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the target of the given element if supported, null instead.
 	 * 
 	 * @param element
 	 *            the element
 	 * @return the target
 	 */
 	private EObject getTarget(EObject element) {
 		EObject target = null;
 		if (element instanceof IntentSectionReferenceInstruction) {
 			IntentSectionReferenceInstruction ref = (IntentSectionReferenceInstruction)element;
 			// TODO check metamodel consistency
 			target = ref.getReferencedObject().getReferencedObject();
 		}
 		return target;
 	}
 
 	/**
 	 * This implementation of an hyperlink allows for the opening of Intent elements declarations.
 	 * 
 	 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 	 */
 	private class IntentHyperlink implements IHyperlink {
 		/** Region of this hyperlink. */
 		private final IRegion hyperLinkRegion;
 
 		/** EObject that will be opened via this hyperlink. */
 		private final EObject target;
 
 		/** Editor on which this link appears. */
 		private final IntentEditor sourceEditor;
 
 		/**
 		 * Instantiates an hyperlink given the editor it appears on, the text region it spans to, and the
 		 * link's target.
 		 * 
 		 * @param editor
 		 *            Editor on which this hyperlink is shown.
 		 * @param region
 		 *            Region of the editor where this hyperlink appears.
 		 * @param linkTarget
 		 *            Target of the hyperlink.
 		 */
 		public IntentHyperlink(IntentEditor editor, IRegion region, EObject linkTarget) {
 			this.sourceEditor = editor;
 			this.hyperLinkRegion = region;
 			this.target = linkTarget;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
 		 */
 		public IRegion getHyperlinkRegion() {
 			return hyperLinkRegion;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
 		 */
 		public String getHyperlinkText() {
 			return "Open declaration for " + target; //$NON-NLS-1$
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
 		 */
 		public String getTypeLabel() {
 			return null;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
 		 */
 		public void open() {
 			if (target != null) {
 				IntentEditorOpener.openIntentEditor(
 						((IntentDocumentProvider)sourceEditor.getDocumentProvider()).getRepository(), target,
 						false, target, false);
 			}
 		}
 	}
 }
