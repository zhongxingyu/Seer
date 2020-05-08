 /*******************************************************************************
  * Copyright (c) 2010, 2011 SAP AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Kaloyan Raev (SAP AG) - initial API and implementation
  *******************************************************************************/
 package org.eclipse.libra.facet;
 
 import static org.eclipse.libra.facet.OSGiBundleFacetUtils.getManifestFile;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.libra.facet.internal.LibraFacetPlugin;
 import org.eclipse.osgi.util.ManifestElement;
 import org.eclipse.wst.common.project.facet.core.ActionConfig;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
 import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectEvent;
 import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectListener;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Version;
 
 
 public class OSGiBundleFacetInstallConfig extends ActionConfig implements IFacetedProjectListener {
 	
 	private static final String QUALIFIER = "qualifier"; //$NON-NLS-1$
 
 	private IObservableValue symbolicNameValue;
 	private IObservableValue versionValue;
 	private IObservableValue nameValue;
 	private IObservableValue vendorValue;
 	
 	private Map<String, String> headers;
 	
 	public OSGiBundleFacetInstallConfig() {
 		Realm realm = OSGiBundleFacetRealm.getRealm();
 		
 		headers = new HashMap<String, String>();
 		
 		symbolicNameValue = new WritableValue(realm, getDefaultSymbolicName(), String.class);
 		versionValue = new WritableValue(realm, getDefaultVersion(), String.class);
 		nameValue = new WritableValue(realm, getDefaultSymbolicName(), String.class);
 		vendorValue = new WritableValue(realm, getDefaultVendor(), String.class);
 	}
 	
 	public IObservableValue getSymbolicNameValue() {
 		return symbolicNameValue;
 	}
 
 	public IObservableValue getVersionValue() {
 		return versionValue;
 	}
 
 	public IObservableValue getNameValue() {
 		return nameValue;
 	}
 
 	public IObservableValue getVendorValue() {
 		return vendorValue;
 	}
 
 	public String getSymbolicName() {
 		return (String) getSymbolicNameValue().getValue();
 	}
 
 	public Version getVersion() {
 		return Version.parseVersion((String) getVersionValue().getValue());
 	}
 
 	public String getName() {
 		return (String) getNameValue().getValue();
 	}
 
 	public String getVendor() {
 		return (String) getVendorValue().getValue();
 	}
 
 	public Map<String, String> getHeaders() {
 		return headers;
 	}
 
 	@Override
 	public void setFacetedProjectWorkingCopy(IFacetedProjectWorkingCopy fpjwc) {
 		super.setFacetedProjectWorkingCopy(fpjwc);
 		fpjwc.addListener(this, IFacetedProjectEvent.Type.PROJECT_NAME_CHANGED);
 		
 		// first read any existing manifest headers
 		updateHeaders();
 		
 		// update the default values for the configurable fields 
 		updateDefaultValues();
 	}
 
 	public void handleEvent(IFacetedProjectEvent event) {
 		if (event.getType() == IFacetedProjectEvent.Type.PROJECT_NAME_CHANGED) {
 			updateDefaultNameValues();
 		}
 	}
 	
 	private void updateHeaders() {
 		try {
 			IFile manifest = null;
 			IProject project = getProject();
 			if (project != null) {
 				manifest = getManifestFile(project);
 			}
 			
 			if (manifest != null && manifest.exists()) {
 					Map<String, String> manifestHeaders = ManifestElement.parseBundleManifest(manifest.getContents(), null);
					for (String key : manifestHeaders.keySet()) {
						headers.put(key, manifestHeaders.get(key));
 					}
 			}
 		} catch (Exception e) {
 			LibraFacetPlugin.logError(e);
 		}
 	}
 	
 	private void updateDefaultNameValues() {
 		symbolicNameValue.setValue(getDefaultSymbolicName());
 		nameValue.setValue(getDefaultName());
 	}
 	
 	private void updateDefaultValues() {
 		symbolicNameValue.setValue(getDefaultSymbolicName());
 		versionValue.setValue(getDefaultVersion());
 		nameValue.setValue(getDefaultName());
 		vendorValue.setValue(getDefaultVendor());
 	}
 
 	private Object getDefaultSymbolicName() {
 		String symbolicName = null;
 		
 		// check if there any existing manifest headers
 		if (headers.containsKey(Constants.BUNDLE_SYMBOLICNAME)) {
 			// there is existing symbolic name - use it as default value
 			symbolicName = headers.get(Constants.BUNDLE_SYMBOLICNAME);
 		} else {
 			// no existing symbolic name header - use the project name as default value
 			IFacetedProjectWorkingCopy fpjwc = getFacetedProjectWorkingCopy();
 			if (fpjwc != null) {
 				symbolicName = fpjwc.getProjectName();
 			}
 		}
 		
 		return symbolicName;
 	}
 	
 	private String getDefaultVersion() {
 		String version = null;
 		
 		// check if there any existing manifest headers
 		if (headers.containsKey(Constants.BUNDLE_VERSION)) {
 			// there is existing version - use it as default value
 			version = headers.get(Constants.BUNDLE_VERSION);
 		} else {
 			// no existing version header - use "1.0.0.qualifier" as default name
 			version = new Version(1, 0, 0, QUALIFIER).toString();
 		}
 		
 		return version;
 	}
 
 	private Object getDefaultName() {
 		String bundleName = null;
 		
 		// check if there any existing manifest headers
 		if (headers.containsKey(Constants.BUNDLE_NAME)) {
 			bundleName = headers.get(Constants.BUNDLE_NAME);
 		} else {
 			// no existing bundle name header - use the capitalized project name as default value
 			IFacetedProjectWorkingCopy fpjwc = getFacetedProjectWorkingCopy();
 			if (fpjwc != null) {
 				bundleName = fpjwc.getProjectName();
 				// capitalize the first letter
 				if (bundleName != null && bundleName.length() > 0 && !Character.isTitleCase(bundleName.charAt(0))) {
 					StringBuilder builder = new StringBuilder(bundleName);
 					builder.replace(0, 1, String.valueOf(Character.toTitleCase(bundleName.charAt(0))));
 					bundleName = builder.toString();
 				}
 			}
 		}
 		
 		return bundleName;
 	}
 
 	private Object getDefaultVendor() {
 		String vendor = null;
 		
 		// check if there any existing manifest headers
 		if (headers.containsKey(Constants.BUNDLE_VENDOR)) {
 			// there is existing vendor - use it as default value
 			vendor = headers.get(Constants.BUNDLE_VENDOR);
 		}
 		
 		return vendor;
 	}
 	
 	private IProject getProject() {
 		IFacetedProjectWorkingCopy fpjwc = getFacetedProjectWorkingCopy();
 		if (fpjwc == null) 
 			return null;
 		
 		IFacetedProject fproj = fpjwc.getFacetedProject();
 		if (fproj == null)
 			return null;
 		
 		return fproj.getProject();
 	}
 
 	public static class SymbolicNameValidator implements IValidator {
 
 		public IStatus validate(Object value) {
 			String symbolicName = (String) value;
 			
 			if (symbolicName == null || symbolicName.trim().length() == 0) {
 				return ValidationStatus.error(Messages.OSGiBundleFacetInstallConfig_EmptySymbolicName);
 			}
 			
 			return ValidationStatus.ok();
 		}
 		
 	}
 	
 	public static class VersionValidator implements IValidator {
 
 		public IStatus validate(Object value) {
 			String version = (String) value;
 			
 			if (version == null || version.trim().length() == 0) {
 				return ValidationStatus.error(Messages.OSGiBundleFacetInstallConfig_EmptyVersion);
 			}
 			
 			return ValidationStatus.ok();
 		}
 
 	}
 	
 }
