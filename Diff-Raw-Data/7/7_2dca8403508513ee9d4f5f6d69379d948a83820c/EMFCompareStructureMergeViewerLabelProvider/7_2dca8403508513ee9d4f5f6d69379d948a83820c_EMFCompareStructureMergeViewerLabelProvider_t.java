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
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.ide.ui.internal.util.StyledStringConverter;
 import org.eclipse.emf.compare.provider.IItemStyledLabelProvider;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
 import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * A specialized {@link AdapterFactoryLabelProvider.FontAndColorProvider} for the structure merge viewer.
  * 
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 class EMFCompareStructureMergeViewerLabelProvider extends AdapterFactoryLabelProvider.FontAndColorProvider implements IStyledLabelProvider {
 
 	protected StyledStringConverter styledStringConverter;
 
 	/**
 	 * Constructor calling super {@link #FontAndColorProvider(AdapterFactory, Viewer)}.
 	 * 
 	 * @param adapterFactory
 	 *            The adapter factory.
 	 * @param viewer
 	 *            The viewer.
 	 */
 	public EMFCompareStructureMergeViewerLabelProvider(AdapterFactory adapterFactory, Viewer viewer) {
 		super(adapterFactory, viewer);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider#getText(java.lang.Object)
 	 */
 	@Override
 	public String getText(Object element) {
 		return getStyledText(element).getString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider#getFont(java.lang.Object)
 	 */
 	@Override
 	public Font getFont(Object object) {
 		if (object instanceof Adapter) {
 			return super.getFont(((Adapter)object).getTarget());
 		}
 		return super.getFont(object);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider#getForeground(java.lang.Object)
 	 */
 	@Override
 	public Color getForeground(Object object) {
 		if (object instanceof Adapter) {
 			return super.getForeground(((Adapter)object).getTarget());
 		}
 		return super.getForeground(object);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider#getBackground(java.lang.Object)
 	 */
 	@Override
 	public Color getBackground(Object object) {
 		if (object instanceof Adapter) {
 			return super.getBackground(((Adapter)object).getTarget());
 		}
 		return super.getBackground(object);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider#getImage(java.lang.Object)
 	 */
 	@Override
 	public Image getImage(Object element) {
 		final Image ret;
 		if (element instanceof ItemProviderAdapter) {
 			ret = super.getImage(element);
 		} else if (element instanceof Adapter) {
 			ret = super.getImage(((Adapter)element).getTarget());
 		} else {
 			ret = super.getImage(element);
 		}
 
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see 
 	 *      org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider.getStyledText(java
 	 *      .lang.Object)
 	 */
 	public StyledString getStyledText(Object element) {
 		final StyledString ret;
 		if (element instanceof ItemProviderAdapter) {
 			ret = getStyledTextFromObject(element);
 		} else if (element instanceof Adapter) {
 			ret = getStyledTextFromObject(((Adapter)element).getTarget());
 		} else {
 			ret = getStyledTextFromObject(element);
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the styled text string of the given <code>object</code> by adapting it to
 	 * {@link IItemStyledLabelProvider} and asking for its
 	 * {@link IItemStyledLabelProvider#getStyledText(Object) text}. Returns null if <code>object</code> is
 	 * null.
 	 * 
 	 * @param object
 	 *            the object from which we want a text
 	 * @return the text, or null if object is null.
 	 * @throws NullPointerException
 	 *             if <code>adapterFactory</code> is null.
 	 */
 	private StyledString getStyledTextFromObject(final Object object) {
 		if (object == null) {
 			return null;
 		}
 		StyledString ret = null;
 		Object itemStyledLabelProvider = getAdapterFactory().adapt(object, IItemStyledLabelProvider.class);
 		if (itemStyledLabelProvider instanceof IItemStyledLabelProvider) {
 			ret = getStyledStringConverter().toJFaceStyledString(
 					((IItemStyledLabelProvider)itemStyledLabelProvider).getStyledText(object));
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the StyledStringConverter used to convert emf compare styled strings to jface styled strings.
 	 * 
 	 * @return the styledStringConverter.
 	 */
 	protected StyledStringConverter getStyledStringConverter() {
 		if (styledStringConverter == null) {
 			styledStringConverter = new StyledStringConverter(getDefaultFont(), getDefaultForeground(),
 					getDefaultBackground());
 		}
 		return styledStringConverter;
 	}
 }
