 /*******************************************************************************
  * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet)
  * and others. All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.ecommons.ltk.internal.ui;
 
 import java.net.URL;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.osgi.framework.Bundle;
 
 import de.walware.ecommons.ui.SharedUIResources;
 
 import de.walware.ecommons.ltk.ui.sourceediting.ContentAssistComputerRegistry;
 
 
 public class AdvancedExtensionsInternal {
 	
 	public static final String CONTENTASSIST_EXTENSIONPOINT_ID = "de.walware.ecommons.ltk.advancedContentAssist"; //$NON-NLS-1$
 	
 	public static final String INFOHOVER_EXTENSIONPOINT_ID = "de.walware.ecommons.ltk.advancedInfoHover"; //$NON-NLS-1$
 	
 	
 	/** The extension schema name of the contribution id attribute. */
 	public static final String CONFIG_ID_ATTRIBUTE_NAME = "id"; //$NON-NLS-1$
 	/** The extension schema name of the contribution name attribute. */
 	public static final String CONFIG_NAME_ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
 	/** The extension schema name of the content type id attribute referencing to a content type id. */
 	public static final String CONFIG_CONTENT_TYPE_ID_ATTRIBUTE_NAME = "contentTypeId"; //$NON-NLS-1$
 	/** The extension schema name of the category id attribute referencing to a category id. */
 	public static final String CONFIG_CATEGORY_ID_ATTRIBUTE_NAME = "categoryId"; //$NON-NLS-1$
 	/** The extension schema name of the partition type element. */
 	public static final String CONFIG_PARTITION_ELEMENT_NAME = "partition"; //$NON-NLS-1$
 	/** The extension schema name of the computer type element. */
 	public static final String CONFIG_COMPUTER_ELEMENT_NAME = "computer"; //$NON-NLS-1$
 	/** The extension schema name of the category type element. */
 	public static final String CONFIG_CATEGORY_ELEMENT_NAME = "category"; //$NON-NLS-1$
 	/** The extension schema name of the content type id attribute of the partition. */
 	public static final String CONFIG_CONTENTTYPE_ID_ELEMENT_NAME = "partitionType"; //$NON-NLS-1$
 	/** The extension schema name of the class attribute. */
 	public static final String CONFIG_CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$
 	/** The extension schema name of the icon resource attribute. */
 	public static final String CONFIG_ICON_ATTRIBUTE_NAME = "icon"; //$NON-NLS-1$
 	
 	private static final Pattern MODIFIERS_PATTERN = Pattern.compile("\\s*\\+\\s*"); //$NON-NLS-1$
 	
 	
 	public static final String getCheckedString(final IConfigurationElement element, final String attrName) throws CoreException {
 		final String s = element.getAttribute(attrName);
 		if (s == null || s.length() == 0) {
 			throw new CoreException(new Status(IStatus.ERROR, SharedUIResources.PLUGIN_ID, -1,
 					NLS.bind("missing value for attribute ''{0}''", attrName), null));
 		}
 		return s;
 	}
 	
 	public static final ImageDescriptor getImageDescriptor(final IConfigurationElement element, final String attrName) throws CoreException {
 		final String imagePath = element.getAttribute(attrName);
 		if (imagePath != null) {
 			final Bundle bundle = ContentAssistComputerRegistry.getBundle(element);
 			final URL url = FileLocator.find(bundle, new Path(imagePath), null);
 			if (url != null) {
 				return ImageDescriptor.createFromURL(url);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Computes the state mask for the given modifier string.
 	 *
 	 * @param modifiers	the string with the modifiers, separated by '+'
 	 * @return the state mask or -1 if the input is invalid
 	 */
 	public static int computeStateMask(final String modifiers) {
 		if (modifiers == null) {
 			return -1;
 		}
 		if (modifiers.length() == 0) {
 			return SWT.NONE;
 		}
 		final String[] mods = MODIFIERS_PATTERN.split(modifiers);
 		int stateMask = 0;
 		for (final String modifier : mods) {
 			final String key = modifier.toUpperCase();
 			if (key.equals("CTRL")) { //$NON-NLS-1$
 				stateMask |= SWT.CTRL;
 			}
 			else if (key.equals("ALT")) { //$NON-NLS-1$
 				stateMask |= SWT.ALT;
 			}
 			else if (key.equals("SHIFT")) { //$NON-NLS-1$
 				stateMask |= SWT.SHIFT;
 			}
 			else if (key.equals("COMMAND")) { //$NON-NLS-1$
 				stateMask |= SWT.COMMAND;
 			}
 			else if (key.equals("M1")) { //$NON-NLS-1$
 				stateMask |= SWT.MOD1;
 			}
 			else if (key.equals("M2")) { //$NON-NLS-1$
				stateMask |= SWT.MOD1;
 			}
 			else if (key.equals("M3")) { //$NON-NLS-1$
				stateMask |= SWT.MOD1;
 			}
 			else if (key.equals("M4")) { //$NON-NLS-1$
				stateMask |= SWT.MOD1;
 			}
 			else {
 				return -1;
 			}
 		}
 		return stateMask;
 	}
 	
 	public static String createUnifiedStateMaskString(final int stateMask) {
 		if (stateMask != 0) {
 			final StringBuilder sb = new StringBuilder();
 			if ((stateMask & SWT.MOD1) == SWT.MOD1) {
 				sb.append("M1+"); //$NON-NLS-1$
 			}
 			if ((stateMask & SWT.MOD2) == SWT.MOD2) {
 				sb.append("M2+"); //$NON-NLS-1$
 			}
 			if ((stateMask & SWT.MOD3) == SWT.MOD3) {
 				sb.append("M3+"); //$NON-NLS-1$
 			}
 			if (SWT.MOD4 != 0 && (stateMask & SWT.MOD4) == SWT.MOD4) {
 				sb.append("M4+"); //$NON-NLS-1$
 			}
 			if (sb.length() > 0) {
 				return sb.substring(0, sb.length()-1);
 			}
 		}
 		return ""; //$NON-NLS-1$
 	}
 	
 }
