 /*******************************************************************************
  * Copyright (c) 2012, 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.editor;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.domain.ICompareEditingDomain;
 import org.eclipse.emf.compare.ide.ui.internal.configuration.EMFCompareConfiguration;
import org.eclipse.emf.edit.tree.TreeFactory;
import org.eclipse.emf.edit.tree.TreeNode;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class ComparisonEditorInput extends AbstractEMFCompareEditorInput {
 
 	private final Comparison comparison;
 
 	/**
 	 * @param configuration
 	 */
 	public ComparisonEditorInput(EMFCompareConfiguration configuration, Comparison comparison,
 			ICompareEditingDomain editingDomain, AdapterFactory adapterFactory) {
 		super(configuration, editingDomain, adapterFactory);
 		this.comparison = comparison;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.editor.AbstractEMFCompareEditorInput#doPrepareInput(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	protected Object doPrepareInput(IProgressMonitor monitor) throws InvocationTargetException,
 			InterruptedException {
		final TreeNode treeNode = TreeFactory.eINSTANCE.createTreeNode();
		treeNode.setData(comparison);
		return getAdapterFactory().adapt(treeNode, ICompareInput.class);
 	}
 }
