 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.viewsupport;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.internal.ui.UIModelProviderManager;
 import org.eclipse.dltk.ui.ScriptElementImageProvider;
 import org.eclipse.dltk.ui.ScriptElementLabels;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.jface.viewers.IColorProvider;
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.LabelProviderChangedEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 
 public class ScriptUILabelProvider implements ILabelProvider, IColorProvider {
 
 	protected ListenerList fListeners = new ListenerList(1);
 
 	protected ScriptElementImageProvider fImageLabelProvider;
 
 	protected StorageLabelProvider fStorageLabelProvider;
 
 	private int fImageFlags;
 
 	private long fTextFlags;
 
 	private ArrayList fLabelDecorators;
 
 	/**
 	 * Creates a new label provider with default flags.
 	 */
 	public ScriptUILabelProvider() {
 
 		this(ScriptElementLabels.ALL_DEFAULT,
 				ScriptElementImageProvider.OVERLAY_ICONS);
 		fLabelDecorators = null;
 	}
 
 	/**
 	 * @param textFlags
 	 *            Flags defined in <code>ScriptElementLabels</code>.
 	 * @param imageFlags
 	 *            Flags defined in <code>ScriptElementImageProvider</code>.
 	 */
 	public ScriptUILabelProvider(long textFlags, int imageFlags) {
 		fImageLabelProvider = new ScriptElementImageProvider();
 		fStorageLabelProvider = new StorageLabelProvider();
 
 		fImageFlags = imageFlags;
 		fTextFlags = textFlags;
 		fLabelDecorators = null;
 	}
 
 	public final void setTextFlags(long textFlags) {
 		fTextFlags = textFlags;
 	}
 
 	public final void setImageFlags(int imageFlags) {
 		fImageFlags = imageFlags;
 	}
 
 	public final int getImageFlags() {
 		return fImageFlags;
 	}
 
 	public final long getTextFlags() {
 		return fTextFlags;
 	}
 
 	/**
 	 * Evaluates the image flags for a element. Can be overwriten by super
 	 * classes.
 	 * 
 	 * @return Returns a int
 	 */
 	protected int evaluateImageFlags(Object element) {
 		return getImageFlags();
 	}
 
 	/**
 	 * Evaluates the text flags for a element. Can be overwriten by super
 	 * classes.
 	 * 
 	 * @return Returns a int
 	 */
 	protected long evaluateTextFlags(Object element) {
 		return getTextFlags();
 	}
 
 	public Image getImage(Object element) {
 		ILabelProvider[] providers = getProviders(element);
 		Image result = null;
 		if (providers != null) {
 			for (int i = 0; i < providers.length; i++) {
 				Image image = providers[i].getImage(element);
 				if (image != null) {
 					result = image;
 					break;
 				}
 			}
 		}
		if (result == null) {
			result = fImageLabelProvider.getImageLabel(element,
					evaluateImageFlags(element));
		}
 		if (result == null
 				&& (element instanceof IStorage || element instanceof ISourceModule)) {
 			result = fStorageLabelProvider.getImage(element);
 		}
 		return decorateImage(result, element);
 	}
 
 	public String getText(Object element) {
 		ILabelProvider[] providers = getProviders(element);
 		String result = null;
 		if (providers != null) {
 			for (int i = 0; i < providers.length; i++) {
 				String text = providers[i].getText(element);
 				if (text != null) {
 					result = text;
 					break;
 				}
 			}
 		}
 		if (result == null) {
 			result = ScriptElementLabels.getDefault().getTextLabel(element,
 					evaluateTextFlags(element));
 		}
 
 		if (result.length() == 0 && (element instanceof IStorage)) {
 			result = fStorageLabelProvider.getText(element);
 		}
 
 		return decorateText(result, element);
 	}
 
 	private ILabelProvider[] getProviders(Object element) {
 		String idtoolkit = null;
 		if (element instanceof IModelElement) {
 			IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 					.getLanguageToolkit((IModelElement) element);
 			if (toolkit != null) {
 				idtoolkit = toolkit.getNatureId();
 			}
 		}
 		ILabelProvider[] providers = UIModelProviderManager
 				.getLabelProviders(idtoolkit);
 		return providers;
 	}
 
 	public void addListener(ILabelProviderListener listener) {
 		if (fLabelDecorators != null) {
 			for (int i = 0; i < fLabelDecorators.size(); i++) {
 				ILabelDecorator decorator = (ILabelDecorator) fLabelDecorators
 						.get(i);
 				decorator.addListener(listener);
 			}
 		}
 
 		fListeners.add(listener);
 	}
 
 	public void dispose() {
 		if (fLabelDecorators != null) {
 			for (int i = 0; i < fLabelDecorators.size(); i++) {
 				ILabelDecorator decorator = (ILabelDecorator) fLabelDecorators
 						.get(i);
 				decorator.dispose();
 			}
 			fLabelDecorators = null;
 		}
 
 		fStorageLabelProvider.dispose();
 		fImageLabelProvider.dispose();
 	}
 
 	public boolean isLabelProperty(Object element, String property) {
 		return true;
 	}
 
 	public void removeListener(ILabelProviderListener listener) {
 		if (fLabelDecorators != null) {
 			for (int i = 0; i < fLabelDecorators.size(); i++) {
 				ILabelDecorator decorator = (ILabelDecorator) fLabelDecorators
 						.get(i);
 				decorator.removeListener(listener);
 			}
 		}
 		fListeners.remove(listener);
 	}
 
 	public Color getForeground(Object element) {
 		return null;
 	}
 
 	public Color getBackground(Object element) {
 		return null;
 	}
 
 	/**
 	 * Fires a label provider changed event to all registered listeners Only
 	 * listeners registered at the time this method is called are notified.
 	 * 
 	 * @param event
 	 *            a label provider changed event
 	 * 
 	 * @see ILabelProviderListener#labelProviderChanged
 	 */
 	protected void fireLabelProviderChanged(
 			final LabelProviderChangedEvent event) {
 
 		Object[] listeners = fListeners.getListeners();
 		for (int i = 0; i < listeners.length; ++i) {
 			final ILabelProviderListener l = (ILabelProviderListener) listeners[i];
 
 			SafeRunner.run(new SafeRunnable() {
 				public void run() {
 					l.labelProviderChanged(event);
 				}
 			});
 		}
 	}
 
 	public void addLabelDecorator(ILabelDecorator decorator) {
 		if (fLabelDecorators == null) {
 			fLabelDecorators = new ArrayList(2);
 		}
 		fLabelDecorators.add(decorator);
 	}
 
 	protected Image decorateImage(Image image, Object element) {
 		if (fLabelDecorators != null && image != null) {
 			for (int i = 0; i < fLabelDecorators.size(); i++) {
 				ILabelDecorator decorator = (ILabelDecorator) fLabelDecorators
 						.get(i);
 				image = decorator.decorateImage(image, element);
 			}
 		}
 		return image;
 	}
 
 	protected String decorateText(String text, Object element) {
 		if (fLabelDecorators != null && text.length() > 0) {
 			for (int i = 0; i < fLabelDecorators.size(); i++) {
 				ILabelDecorator decorator = (ILabelDecorator) fLabelDecorators
 						.get(i);
 				text = decorator.decorateText(text, element);
 			}
 		}
 		return text;
 	}
 }
