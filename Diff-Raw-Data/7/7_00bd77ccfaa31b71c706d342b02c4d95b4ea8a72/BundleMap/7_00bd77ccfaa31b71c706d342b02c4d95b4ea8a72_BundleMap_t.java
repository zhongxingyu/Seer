 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.bundle;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IFileEditorInput;
 import org.jboss.tools.common.el.core.model.ELArgumentInvocation;
 import org.jboss.tools.common.el.core.model.ELExpression;
 import org.jboss.tools.common.el.core.model.ELInstance;
 import org.jboss.tools.common.el.core.model.ELInvocationExpression;
 import org.jboss.tools.common.el.core.model.ELModel;
 import org.jboss.tools.common.el.core.model.ELPropertyInvocation;
 import org.jboss.tools.common.el.core.parser.ELParser;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.event.XModelTreeEvent;
 import org.jboss.tools.common.model.event.XModelTreeListener;
 import org.jboss.tools.common.model.options.PreferenceModelUtilities;
 import org.jboss.tools.common.model.project.IModelNature;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.i18n.MainLocaleProvider;
 import org.jboss.tools.jst.jsp.preferences.IVpePreferencesPage;
 import org.jboss.tools.jst.jsp.util.Constants;
 import org.jboss.tools.jst.web.project.WebProject;
 import org.jboss.tools.jst.web.project.list.WebPromptingProvider;
 
 public class BundleMap {
 
 	public static final String TITLE_ATTRIBUTE_NAME = "title"; //$NON-NLS-1$
 	private static final String[] JSF_PROJECT_NATURES = {
 		WebProject.JSF_NATURE_ID
 	};
 		
 	private BundleMapListener[] bundleMapListeners = new BundleMapListener[0];
 	private String[] javaSources;
     /*
      * Stores the current VPE locale.
      */
 	private Locale locale;
 	private BundleEntry[] bundles = new BundleEntry[0];
 	private Map<String,UsedKey> usedKeys = new HashMap<String,UsedKey>();
     
     boolean showBundleUsageAsEL = JspEditorPlugin.getDefault().getPreferenceStore().getBoolean(
 			IVpePreferencesPage.SHOW_RESOURCE_BUNDLES_USAGE_AS_EL); 
 
 	XModelTreeListener modelListener = new ML();
 	private IProject project;
 	
 	public void init(IEditorInput input){
 		if (input instanceof IFileEditorInput) {
			project=((IFileEditorInput)input).getFile().getProject();
 		}
		init(project);
 	}
 	
 	public void init(IProject project){
 		if (project!=null) {
 			javaSources = getJavaProjectSrcLocations(project);
 		}
 		/*
 		 * Initialize the locale with default value.
 		 */
 		locale = MainLocaleProvider.getInstance().getLocale(project);
 		refreshRegisteredBundles();
 		PreferenceModelUtilities.getPreferenceModel().addModelTreeListener(modelListener);
 	}
 	
 	public void refreshRegisteredBundles() {
 		if (hasJsfProjectNatureType()
 				&& project!=null) {
 			IModelNature modelNature = EclipseResourceUtil
 					.getModelNature(project);
 			if (modelNature != null) {
 				XModel model = modelNature.getModel();
 				List<Object> l = WebPromptingProvider.getInstance().getList(
 						model, WebPromptingProvider.JSF_REGISTERED_BUNDLES,
 						null, null);
 				if (l != null && l.size() > 1 && (l.get(1) instanceof Map)) {
 					Map<?, ?> map = (Map<?, ?>) l.get(1);
 					/*
 					 * Fix for https://jira.jboss.org/jira/browse/JBIDE-5218
 					 * When updating f:view's locale attribute right after
 					 * template creation - map of registered bundles is empty
 					 * and couldn't be updated. To change bundle's locale they
 					 * should be accessed through <code>bundles</code> variable.
 					 */
 					if (map.keySet().size() > 0) {
 						Iterator<?> it = map.keySet().iterator();
 						while (it.hasNext()) {
 							String uri = it.next().toString();
 							String prefix = map.get(uri).toString();
 							int hash = (prefix + ":" + uri).hashCode(); //$NON-NLS-1$
 							removeBundle(hash);
 							addBundle(hash, prefix, uri, true);
 						}
 					} else if (bundles.length > 0) {
 						for (int i = 0; i < bundles.length; i++) {
 							String uri = bundles[i].uri;
 							String prefix = bundles[i].prefix;
 							int hash = (prefix + ":" + uri).hashCode(); //$NON-NLS-1$
 							removeBundle(hash);
 							addBundle(hash, prefix, uri, true);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void clearAll() {
 		bundles = new BundleEntry[0];
 	    usedKeys = new HashMap<String,UsedKey>();
 	}
 	
 	public void dispose() {
 		PreferenceModelUtilities.getPreferenceModel().removeModelTreeListener(modelListener);
 	}	
 
 	public boolean isShowBundleUsageAsEL() {
 		return showBundleUsageAsEL;
 	}
 
 	private boolean hasJsfProjectNatureType() {
 		boolean hasJsfProjectNatureType = false;
 		try {
 			if(project!=null) {
 				if (project.exists() && project.isOpen()) {
 					for (int i = 0; i < JSF_PROJECT_NATURES.length; i++) {
 						if (project.hasNature(JSF_PROJECT_NATURES[i])){ 
 							hasJsfProjectNatureType = true;
 						break;
 						}
 					}
 				}
 			}
 		} catch (CoreException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 		return hasJsfProjectNatureType;
 	}
 	
 	public boolean openBundle(String expression, String locale){
 		List<ELInstance> is = parseJSFExpression(expression);
 		if(is == null || is.size() == 0) return false;
 		String prefix = null;
 		String propertyName = null;
 		for (ELInstance i: is) {
 			ELExpression expr = i.getExpression();
 			if(expr == null) continue;
 			List<ELInvocationExpression> invs = expr.getInvocations();
 			if(invs.size() > 0) {
 				String[] values = getCall(invs.get(0));
 				if(values != null) {
 					prefix = values[0];
 					propertyName = values[1];
 					break;
 				}
 			}
 		}
 		if(prefix == null) return false;
 
 		BundleEntry entry = getBundle(prefix);
 		
 		if(entry == null){
 			if (hasJsfProjectNatureType()) {
 				if (project!=null) {
 					XModel model = EclipseResourceUtil.getModelNature(project).getModel();
 					String prefix2 = prefix;
 					if(propertyName != null && prefix != null) {
 						prefix2 = prefix + "." + propertyName; //$NON-NLS-1$
 					}
 					WebPromptingProvider.getInstance().getList(model, WebPromptingProvider.JSF_BEAN_OPEN, prefix2, null);
 				}
 			}
 			return false;
 		}
 
 		if (hasJsfProjectNatureType()) {
 			Properties p = new Properties();
 			p.put(WebPromptingProvider.BUNDLE, entry.uri);
 			p.put(WebPromptingProvider.KEY, propertyName);
 			if (locale != null) p.put(WebPromptingProvider.LOCALE, locale);
 			p.put(WebPromptingProvider.FILE, project);
 			String error = null;
 			if (project!=null) {
 				XModel model = EclipseResourceUtil.getModelNature(project).getModel();
 				WebPromptingProvider.getInstance().getList(model, WebPromptingProvider.JSF_OPEN_KEY, entry.uri, p);
 				error = p.getProperty(WebPromptingProvider.ERROR); 
 			}
 			return (error == null || error.length() == 0);
 		}
 		return false;
 	}
 
 	/**
 	 * Gets the bundle file based on the locale 
 	 * from the loaded resource bundle.
 	 *
 	 * @param uri the uri
 	 * @return the bundle file
 	 */
 	public IFile getBundleFile(String uri){
 		if(project == null || !project.isOpen()) {
 			return null;
 		}
 		try {
 			if(!project.hasNature(JavaCore.NATURE_ID)) {
 				return null;
 			}
 			IJavaProject javaProject = JavaCore.create(project);		
 			IClasspathEntry[] es = javaProject.getResolvedClasspath(true);
 			for (int i = 0; i < es.length; i++) {
 				if(es[i].getEntryKind() != IClasspathEntry.CPE_SOURCE) {
 					continue;
 				}
 				IFile file = (IFile) project.getWorkspace().getRoot()
 					.getFolder(es[i].getPath()).findMember("/" + getBundleFileName(uri)); //$NON-NLS-1$
 				if(file != null && file.exists()) {
 					return file;
 				}
 			}
 		} catch (CoreException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 			return null;
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the bundle file name.
 	 * <a href="https://jira.jboss.org/browse/JBIDE-6729">
 	 * Related Jira</a>
 	 *
 	 * @param uri the uri
 	 * @return the bundle file name
 	 */
 	private String getBundleFileName(String uri) {
 		String resultUri = uri.replace('.','/');
 		ResourceBundle bundle = getBundleByUrl(uri, locale);
 		String localeString = bundle.getLocale().toString();
 		if ((null != localeString) && (localeString.length() > 0)) {
 			/*
 			 * getLanguage() method in  ResourceBundle could return "en_us" string.
 			 * Bundle's file is case sensitive thus country name 
 			 * should be transformed to UpperCase. 
 			 */
 			String[] parts = localeString.split(Constants.UNDERSCORE);
 			if (parts.length == 2) {
 				parts[1] = parts[1].toUpperCase();
 			}
 			for (String part : parts) {
 				resultUri += Constants.UNDERSCORE + part;
 			}
 		}
 		resultUri += ".properties"; //$NON-NLS-1$
 		return resultUri;
 	}
 	
 	private ResourceBundle getBundleByUrl(String uri, Locale locale) {
 		try {
 			if (javaSources!=null) {
 				File file;
 				URL[] urls = new URL[javaSources.length];
 				for (int i=0;i<javaSources.length;++i) {
 					try {
 						file = new File(javaSources[i]).getCanonicalFile();
 						urls[i] = file.toURL();
 					} catch (IOException ioe) {
 						JspEditorPlugin.getDefault().logError(ioe);
 						return null;
 					}
 				}
 				
 				ClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
 				ResourceBundle bundle = ResourceBundle.getBundle(uri, locale, classLoader);
 				return bundle;
 			}
 		} catch (MissingResourceException ex) {
 		    // Ignore this exception
 		}
 	
 		return null;
 	}
 
 	private static String[] getJavaProjectSrcLocations(IProject project) {
 		return EclipseResourceUtil.getJavaProjectSrcLocations(project);
 	}
 	
 	private void removeBundle(int hashCode, boolean refresh) {
 		if (bundles.length == 0) {
 			return;
 		}
 		int index = -1;
 		for (int i = 0; i < bundles.length; i++) {
 			if (hashCode == bundles[i].hashCode){
 				index = i;
 				break;
 			}
 		}
 		if (index == -1) {
 			return;
 		}
 		if (bundles.length == 1) {
 			bundles = new BundleEntry[0];
 			return;
 		}
 		BundleEntry[] newBundles = new BundleEntry[bundles.length - 1];
 		System.arraycopy(bundles, 0, newBundles, 0, index);
 		System.arraycopy(bundles, index + 1, newBundles, index, bundles.length - index - 1);
 		bundles = newBundles;
 		if (refresh) {
 			refreshUsedKeys();
 		}
 	}
 
 	public void removeBundle(int hashCode) {
 		removeBundle(hashCode, true);
 	}
 	
 	public void addBundle(int hashCode, String prefix, String uri,boolean refresh) {
 		ResourceBundle bundle = getBundleByUrl(uri, locale);
 		BundleEntry entry = new BundleEntry(bundle, uri, prefix, hashCode);
 		if (bundle != null) {
 			BundleEntry[] newBundles = new BundleEntry[bundles.length + 1];
 			System.arraycopy(bundles, 0, newBundles, 0, bundles.length);
 			bundles = newBundles;
 			bundles[bundles.length - 1] = entry;
 		}
 		if (refresh) {
 			refreshUsedKeys();
 		}
 	}
 	
 	public void changeBundle(int hashCode, String prefix, String uri){
 		removeBundle(hashCode, false);
 		addBundle(hashCode, prefix, uri, true);
 	}
 	
 	private void changeBundleWithoutRefresh(int hashCode, String prefix, String uri){
 		removeBundle(hashCode, false);
 		addBundle(hashCode, prefix, uri, false);
 	}
 
 	private BundleEntry getBundle(String prefix) {
 		if (prefix == null) {
 			return null;
 		}
 		BundleEntry lastBundle = null;
 		for (int i = 0; i < bundles.length; i++) {
 			if (prefix.equals(bundles[i].prefix)) {
 				lastBundle = bundles[i];
 			}
 		}
 		return lastBundle;
 	}
 	
 	public void refresh(){
 		refreshRegisteredBundles();
 		if (project!=null) {
 			javaSources = getJavaProjectSrcLocations(project);
 			UsedKey key;
 			UsedKey[] array = new UsedKey[0];
 			array = usedKeys.values().toArray(array);			
 			
 			for(int i=0; i<array.length;i++){
 				key = (UsedKey)array[i];
 				changeBundleWithoutRefresh(key.hashCode, key.prefix, key.uri);
 			}
 			refreshUsedKeys();
 		}
 	}
 	
 	private void refreshUsedKeys(){
 		UsedKey keyValue;
 		
 		
 		/* yradtsevich: Fix of JBIDE-5818. The map usedKey cannot be modified
 		 * in the following foreach loop. Therefore the keys to remove
 		 * are marked and removed after the loop. */
 		List<String> keysToBeRemoved = new ArrayList<String>(0);
 
 		Set<String> usedKeysSet  = this.usedKeys.keySet();
 		for(String key : usedKeysSet){
 			keyValue =this.usedKeys.get(key);
 			BundleEntry entry = getBundle(keyValue.prefix);
 			if(entry != null){
 				String value;
 				try{
 					value = (String)entry.bundle.getObject(keyValue.key);
 				}catch(MissingResourceException ex){
 					value = null;
 					fireBundleKeyChanged(keyValue.prefix, keyValue.key, value);
 					// Fix of JBIDE-5818
 					keysToBeRemoved.add(key);
 					continue;
 				}
 				if((value == null && keyValue.value != null) || (value != null && keyValue.value == null)){
 					keyValue.value = value;
 					fireBundleKeyChanged(keyValue.prefix, keyValue.key, value);
 					continue;
 				}else if(value != null && keyValue.value != null && !value.equals(keyValue.value)){
 					keyValue.value = value;
 					fireBundleKeyChanged(keyValue.prefix, keyValue.key, value);
 					continue;
 				}
 			} else{
 				keyValue.value = null;
 				fireBundleKeyChanged(keyValue.prefix, keyValue.key, null);
 			}
 		}
 
 		// Fix of JBIDE-5818
 		for (String key : keysToBeRemoved) {
 			this.usedKeys.remove(key);
 		}
 	}
 	
 	private List<ELInstance> parseJSFExpression(String expression){
 		ELParser parser = ELParserUtil.getDefaultFactory().createParser();
 		ELModel model = parser.parse(expression);
 		List<ELInstance> is = model.getInstances();
 		return is;
 	}
 	
 	public String getBundleValue(String name) {
 //		System.out.println("\n1 BM -> getBundleValue -> " + name);
 //		System.out.println("2 BM -> showBundleUsageAsEL = "
 //				+ showBundleUsageAsEL);
 		String bundleValue = name;
 		if (!showBundleUsageAsEL) {
 			List<ELInstance> is = parseJSFExpression(name);
 			if (is != null) {
 				StringBuffer sb = new StringBuffer();
 				int index = 0;
 				for (ELInstance i : is) {
 					int start = i.getStartPosition();
 					sb.append(name.substring(index, start));
 					index = start;
 					if (i.getExpression() instanceof ELInvocationExpression) {
 						ELInvocationExpression expr = (ELInvocationExpression) i
 								.getExpression();
 						String[] values = getCall(expr);
 						if (values != null) {
 							String value = getBundleValue(values[0], values[1]);
 							if (value != null) {
 								sb.append(value);
 								index = i.getEndPosition();
 							}
 
 						}
 					}
 					if (index < i.getEndPosition()) {
 						// fix has been added by Maksim Areshkau
 						// https://jira.jboss.org/jira/browse/JBIDE-6064
 						if (name.length() > i.getEndPosition()) {
 							sb.append(name.substring(index, i.getEndPosition()));
 							index = i.getEndPosition();
 						} else {
 							sb.append(name.substring(index, name.length()));
 							index = name.length();
 						}
 					}
 				}
 				bundleValue = sb.append(name.substring(index)).toString();
 			}
 		}
 //		System.out.println("3 BM -> getBundleValue -> " + bundleValue);
 		return bundleValue;
 	}
 	
 	String[] getCall(ELInvocationExpression expr) {
 		if(expr == null) return null;
 		ELInvocationExpression left = expr.getLeft();
 		if(left == null) return null;
 		String name = expr.getMemberName();
 		if(name == null || name.length() == 0) return null;
 		if(expr instanceof ELPropertyInvocation) {
 			return new String[]{left.getText(), name};
 		} else if(expr instanceof ELArgumentInvocation) {
 			if(name.startsWith("\"") || name.startsWith("'")) { //$NON-NLS-1$ //$NON-NLS-2$
 				name = name.substring(1);
 			}
 			if(name.endsWith("\"") || name.endsWith("'")) { //$NON-NLS-1$ //$NON-NLS-2$
 				name = name.substring(0, name.length() - 1);
 			}
 			if(name.length() == 0) return null;
 			return new String[]{left.getText(), name};
 		}
 		return null;
 	}
 	
 	private String getBundleValue(String prefix, String propertyName) {
 		String bundleValue = null;
 		BundleEntry entry = getBundle(prefix);
 		if (entry != null) {
 			String name = prefix + "." + propertyName; //$NON-NLS-1$
 			try {
 				bundleValue = (String) entry.bundle.getObject(propertyName);
 				if (!usedKeys.containsKey(name))
 					usedKeys.put(name, new UsedKey(entry.uri, prefix,
 							propertyName, bundleValue, entry.hashCode));
 			} catch (MissingResourceException ex) {
 				/*
 				 * Null string will be returned.
 				 */
 			}
 		}
 		return bundleValue;
 	}
 	
 	public void addBundleMapListener(BundleMapListener listener) {
 		if (listener != null) {
 			BundleMapListener[] newBundleMapListener = new BundleMapListener[bundleMapListeners.length + 1];
 			System.arraycopy(bundleMapListeners, 0, newBundleMapListener, 0, bundleMapListeners.length);
 			bundleMapListeners = newBundleMapListener;
 			bundleMapListeners[bundleMapListeners.length - 1] = listener;
 		}
 	}
 	
 	public void removeBundleMapListener(BundleMapListener listener) {
 		if (listener == null || bundleMapListeners.length == 0) return;
 		int index = -1;
 		for (int i = 0; i < bundleMapListeners.length; i++) {
 			if (listener == bundleMapListeners[i]){
 				index = i;
 				break;
 			}
 		}
 		if (index == -1) return;
 		if (bundleMapListeners.length == 1) {
 			bundleMapListeners = new BundleMapListener[0];
 			return;
 		}
 		BundleMapListener[] newBundleMapListener = new BundleMapListener[bundleMapListeners.length - 1];
 		System.arraycopy(bundleMapListeners, 0, newBundleMapListener, 0, index);
 		System.arraycopy(bundleMapListeners, index + 1, newBundleMapListener, index, bundleMapListeners.length - index - 1);
 		bundleMapListeners = newBundleMapListener;
 	}
 	
 	private void fireBundleKeyChanged(String prefix, String key, String value) {
 		for (int i = 0; i < bundleMapListeners.length; i++) {
 			bundleMapListeners[i].bundleKeyChanged(prefix, key, value);
 		}
 	}
 	
 	public void updateShowBundleUsageAsEL(boolean showBundlesAsEL) {
 		if(showBundleUsageAsEL != showBundlesAsEL) {
 			showBundleUsageAsEL = showBundlesAsEL;
 			refresh();
 		}	
 	}
 	
 	public void updateShowBundleUsageAsEL() {
 		updateShowBundleUsageAsEL(JspEditorPlugin.getDefault().getPreferenceStore().getBoolean(
 				IVpePreferencesPage.SHOW_RESOURCE_BUNDLES_USAGE_AS_EL));
 	}
 	
 	public Locale getLocale() {
 		return locale;
 	}
 
 	public void setLocale(Locale locale) {
 		this.locale = locale;
 	}
 	
 	static class Expression {
 		public String prefix;
 		public String propertyName;
 	}
 
 	/*
 	 * https://jira.jboss.org/browse/JBIDE-6287
 	 * It was required to get access to BundleEntry class.
 	 */
 	public static class BundleEntry {
 		public ResourceBundle bundle;
 		public String uri;
 		public String prefix;
 		public int hashCode;
 
 		public BundleEntry(ResourceBundle bundle, String uri, String prefix,
 				int hashCode) {
 			this.bundle = bundle;
 			this.uri = uri;
 			this.prefix = prefix;
 			this.hashCode = hashCode;
 		}
 
 		@Override
 		public String toString() {
 			return "BundleEntry [prefix=" + prefix + ", uri=" + uri + ", hashCode=" + hashCode //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					+ ", bundle="+ bundle +"]"; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		
 	}
 
 	static class UsedKey {
 		public int hashCode;
 		public String uri;
 		public String prefix;
 		public String key;
 		public String value;
 
 		public UsedKey(String uri, String prefix, String key, String value,
 				int hashCode) {
 			this.uri = uri;
 			this.prefix = prefix;
 			this.key = key;
 			this.value = value;
 			this.hashCode = hashCode;
 
 		}
 	}
 	
 	class ML implements XModelTreeListener {
 
 		public void nodeChanged(XModelTreeEvent event) {
 			updateShowBundleUsageAsEL();
 		}
 
 		public void structureChanged(XModelTreeEvent event) {
 		}
 		
 	}
 
 	/**
 	 * @param showBundleUsageAsEL the showBundleUsageAsEL to set
 	 */
 	public void setShowBundleUsageAsEL(boolean showBundleUsageAsEL) {
 		this.showBundleUsageAsEL = showBundleUsageAsEL;
 	}
 	
 	public BundleEntry[] getBundles() {
 		return bundles;
 	}
 
 }
