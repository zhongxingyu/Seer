 /*-
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawnsci.intro.content;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IContributor;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.spi.RegistryContributor;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.intro.config.IIntroContentProviderSite;
 import org.eclipse.ui.intro.config.IIntroXHTMLContentProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Content provider for the perspective intro page
  * 
  * @author Baha El Kassaby
  * 
  */
 public class PerspectiveContentProvider implements IIntroXHTMLContentProvider {
 
 	private Logger logger = LoggerFactory
 			.getLogger(PerspectiveContentProvider.class);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.intro.config.IIntroContentProvider#init(org.eclipse.ui
 	 * .intro.config.IIntroContentProviderSite)
 	 */
 	@Override
 	public void init(IIntroContentProviderSite site) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.intro.config.IIntroContentProvider#createContent(java.
 	 * lang.String, java.io.PrintWriter)
 	 */
 	@Override
 	public void createContent(String id, PrintWriter out) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.intro.config.IIntroContentProvider#createContent(java.
 	 * lang.String, org.eclipse.swt.widgets.Composite,
 	 * org.eclipse.ui.forms.widgets.FormToolkit)
 	 */
 	@Override
 	public void createContent(String id, Composite parent, FormToolkit toolkit) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.intro.config.IIntroContentProvider#dispose()
 	 */
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private static final String ATT_CLASS = "class"; //$NON-NLS-1$
 	private static final String ATT_NAME = "name"; //$NON-NLS-1$
 	private static final String ATT_DESCRIPTION = "description"; //$NON-NLS-1$
 	private static final String ATT_ICON = "icon"; //$NON-NLS-1$
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.intro.config.IIntroXHTMLContentProvider#createContent(
 	 * java.lang.String, org.w3c.dom.Element)
 	 */
 	@Override
 	public void createContent(String id, Element parent) {
 		buildOpenPerspectiveGroup();
 //		System.out.println("ID:" + id);
 //		System.out.println("parent:" + parent.getLocalName());
 
 		Document dom = parent.getOwnerDocument();
 	//	Element div = dom.createElement("div");
 		//parent.setAttribute("class", "grid_" + introActions.size());
 		// parent.appendChild(div);
 		parent.setAttribute("class","left");
 
 		Iterator<IConfigurationElement> iter = introActions.iterator();
 		while (iter.hasNext()) {
 			try {
 				IConfigurationElement config = iter.next();
 				String perspectiveClass = config.getAttribute(ATT_CLASS);
 				String description = config.getAttribute(ATT_DESCRIPTION);
 				String name = config.getAttribute(ATT_NAME);
 				IContributor contrib = config.getContributor();
 				String contribName = contrib instanceof RegistryContributor ? ((RegistryContributor) contrib)
 						.getActualName() : contrib.getName();
 				URL bundleURL = FileLocator.find(
 						Platform.getBundle(contribName),
 						new Path(config.getAttribute(ATT_ICON)), null);
 				URL imgURL = null;
 
 				imgURL = FileLocator.toFileURL(bundleURL);
 				Element div = dom.createElement("div");
 				Element a = dom.createElement("a");
 				Element span = dom.createElement("span");
 				Element img = dom.createElement("img");
 				Element p = dom.createElement("p");
 				Element b = dom.createElement("b");
 				div.setAttribute("class", "left");
				div.setAttribute("style", "text-align: center;");
 				a.setAttribute(
 						"href",
 						"http://org.eclipse.ui.intro/runAction?pluginId=org.dawnsci.intro&class=org.dawnsci.intro.actions.OpenPerspectiveAction&name="
 								+ perspectiveClass);
 				// a.setAttribute("onClick", "sessionStorage.id=");
 				a.setAttribute("class", "tooltip portfolio_item float");
 				//a.setAttribute("class", "tooltip");
 				String imgPath = imgURL.getPath();
 				if(isWindows())
 					imgPath = "file:/"+imgPath;//.substring(1);
 				img.setAttribute("src", imgPath);
 				img.setAttribute("class", "tooltip portfolio_item float");
 				//img.setAttribute("alt", "");
 				span.setAttribute("class", "classic info");
 				
 				span.appendChild(dom.createTextNode(description));
				b.appendChild(dom.createTextNode(name));
 				p.appendChild(b);
 				a.appendChild(img);
 				a.appendChild(span);
				div.appendChild(a);
 				div.appendChild(p);
 				parent.appendChild(div);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				logger.error("TODO put description of error here", e);
 			}
 		}
 
 		//parent.appendChild(div);
 	}
 
 	public static boolean isWindows() {
 		String os = System.getProperty("os.name").toLowerCase();
 		// windows
 		return (os.indexOf("win") >= 0); 
 	}
 
 	private static final String INTROREGISTER_EXTENSION_ID = "uk.ac.diamond.scisoft.introRegister"; //$NON-NLS-1$	
 	private LinkedList<IConfigurationElement> introActions;
 
 	private void buildOpenPerspectiveGroup() {
 		introActions = new LinkedList<IConfigurationElement>();
 
 		final IExtension[] extensions = getExtensions(INTROREGISTER_EXTENSION_ID);
 		for (int i = 0; i < extensions.length; i++) {
 
 			IExtension extension = extensions[i];
 			IConfigurationElement[] configElements = extension
 					.getConfigurationElements();
 
 			for (int j = 0; j < configElements.length; j++) {
 				IConfigurationElement config = configElements[j];
 				introActions.add(config);
 			}
 		}
 		// ordering of the items
 		introActions = orderElements(introActions);
 
 	}
 
 	/**
 	 * Discover extensions for the given extensionPointId
 	 * 
 	 * @param extensionPointId
 	 *            the extension point id
 	 * @return an array of discovered extensions
 	 */
 	private IExtension[] getExtensions(String extensionPointId) {
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint point = registry.getExtensionPoint(extensionPointId);
 		IExtension[] extensions = point.getExtensions();
 		return extensions;
 	}
 
 	/**
 	 * Sorts the LinkedList of IConfigurationElements according to their
 	 * ATT_NAME
 	 * 
 	 * @param elems
 	 * @return orderedElems
 	 */
 	private LinkedList<IConfigurationElement> orderElements(
 			LinkedList<IConfigurationElement> elems) {
 		LinkedList<IConfigurationElement> orderedElems = new LinkedList<IConfigurationElement>();
 
 		List<String> items = new ArrayList<String>();
 		List<String> prerelease = new ArrayList<String>();
 		for (int i = 0; i < elems.size(); i++) {
 			if (!elems.get(i).getAttribute(ATT_NAME).startsWith("(Pre-release")) {
 				items.add(elems.get(i).getAttribute(ATT_NAME));
 			} else {
 				prerelease.add(elems.get(i).getAttribute(ATT_NAME));
 			}
 		}
 
 		Object[] strItems = items.toArray();
 		Object[] strPrerelease = prerelease.toArray();
 
 		Arrays.sort(strItems);
 		Arrays.sort(strPrerelease);
 		Object[] allItems = concat(strItems, strPrerelease);
 
 		for (int j = 0; j < allItems.length; j++) {
 			for (int i = 0; i < elems.size(); i++) {
 				if (elems.get(i).getAttribute(ATT_NAME)
 						.equals(allItems[j].toString())) {
 					orderedElems.add(elems.get(i));
 					break;
 				}
 			}
 		}
 
 		return orderedElems;
 	}
 
 	private Object[] concat(Object[] strItems, Object[] strPrerelease) {
 		Object[] C = new String[strItems.length + strPrerelease.length];
 		System.arraycopy(strItems, 0, C, 0, strItems.length);
 		System.arraycopy(strPrerelease, 0, C, strItems.length,
 				strPrerelease.length);
 		return C;
 	}
 }
