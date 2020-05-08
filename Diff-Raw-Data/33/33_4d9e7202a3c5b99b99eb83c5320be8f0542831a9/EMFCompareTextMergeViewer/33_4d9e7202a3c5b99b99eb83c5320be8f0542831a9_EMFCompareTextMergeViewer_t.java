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
 package org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.text;
 
 import java.util.ResourceBundle;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.CompareHandlerService;
 import org.eclipse.compare.internal.MergeSourceViewer;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.compare.AttributeChange;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareConstants;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.DynamicObject;
 import org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.provider.AttributeChangeNode;
 import org.eclipse.emf.compare.ide.ui.internal.util.EMFCompareEditingDomain;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class EMFCompareTextMergeViewer extends TextMergeViewer {
 
 	private static final String BUNDLE_NAME = EMFCompareTextMergeViewer.class.getName();
 
 	private final EMFCompareEditingDomain fEditingDomain;
 
	private DynamicObject fDynamicObject;
 
 	/**
 	 * @param parent
 	 * @param configuration
 	 */
 	public EMFCompareTextMergeViewer(Composite parent, CompareConfiguration configuration) {
 		super(parent, configuration);
 		fEditingDomain = (EMFCompareEditingDomain)getCompareConfiguration().getProperty(
 				EMFCompareConstants.EDITING_DOMAIN);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.TextMergeViewer#copy(boolean)
 	 */
 	@SuppressWarnings("restriction")
 	@Override
 	protected void copy(boolean leftToRight) {
 		Object input = getInput();
 		if (input instanceof AttributeChangeNode) {
 			AttributeChange attributeChange = ((AttributeChangeNode)input).getTarget();
 			final Command copyCommand;
 			if (leftToRight) {
 				copyCommand = fEditingDomain.createCopyLeftToRightCommand(attributeChange);
 			} else {
 				copyCommand = fEditingDomain.createCopyRightToLeftCommand(attributeChange);
 			}
 			fEditingDomain.getCommandStack().execute(copyCommand);
 
 			Match match = attributeChange.getMatch();
 			final MergeSourceViewer mergeSourceViewer;
 			final EObject eObject;
 			if (leftToRight) {
 				eObject = match.getRight();
 				mergeSourceViewer = getRightSourceViewer();
 				setRightDirty(true);
 			} else {
 				eObject = match.getLeft();
 				mergeSourceViewer = getLeftSourceViewer();
 				setLeftDirty(true);
 			}
 
 			EAttribute attribute = attributeChange.getAttribute();
 			EDataType eAttributeType = attribute.getEAttributeType();
 			String newValue = EcoreUtil.convertToString(eAttributeType, eObject.eGet(attribute));
 
 			// mergeSourceViewer.getSourceViewer().getTextWidget().setText(newValue);
 
 			refresh();
 		}
 	}
 
	/**
	 * @return the fDynamicObject
	 */
	public DynamicObject getDynamicObject() {
		if (fDynamicObject == null) {
			this.fDynamicObject = new DynamicObject(this);
		}
		return fDynamicObject;
	}

 	@SuppressWarnings("restriction")
 	protected final MergeSourceViewer getLeftSourceViewer() {
		return (MergeSourceViewer)getDynamicObject().get("fLeft"); //$NON-NLS-1$
 	}
 
 	@SuppressWarnings("restriction")
 	protected final MergeSourceViewer getRightSourceViewer() {
		return (MergeSourceViewer)getDynamicObject().get("fRight"); //$NON-NLS-1$
	}

	protected final void setHandlerService(@SuppressWarnings("restriction") CompareHandlerService service) {
		getDynamicObject().set("fHandlerService", service); //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.TextMergeViewer#createToolItems(org.eclipse.jface.action.ToolBarManager)
 	 */
	@SuppressWarnings("restriction")
 	@Override
 	protected void createToolItems(ToolBarManager tbm) {
		// forced to do that to avoid NPE in org.eclipse.compare.internal.ViewerDescriptor.createViewer
		setHandlerService(CompareHandlerService.createFor(getCompareConfiguration().getContainer(),
				getLeftSourceViewer().getSourceViewer().getControl().getShell()));
 	}
 
 	@Override
 	protected ResourceBundle getResourceBundle() {
 		return ResourceBundle.getBundle(BUNDLE_NAME);
 	}
 
 }
