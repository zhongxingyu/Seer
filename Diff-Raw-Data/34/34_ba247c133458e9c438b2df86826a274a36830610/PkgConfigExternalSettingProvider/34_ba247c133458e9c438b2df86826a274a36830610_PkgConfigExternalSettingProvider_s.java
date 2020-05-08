 /*******************************************************************************
  * Copyright (c) 2011 Petri Tuononen and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Petri Tuononen - Initial implementation
  *******************************************************************************/
 package org.eclipse.cdt.managedbuilder.pkgconfig.settings;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.eclipse.cdt.core.model.CoreModel;
 import org.eclipse.cdt.core.settings.model.CExternalSetting;
 import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
 import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
 import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
 import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
 import org.eclipse.cdt.core.settings.model.ICFolderDescription;
 import org.eclipse.cdt.core.settings.model.ICIncludePathEntry;
 import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
 import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
 import org.eclipse.cdt.core.settings.model.ICProjectDescription;
 import org.eclipse.cdt.core.settings.model.ICSettingEntry;
 import org.eclipse.cdt.core.settings.model.ICStorageElement;
 import org.eclipse.cdt.core.settings.model.extension.CExternalSettingProvider;
 import org.eclipse.cdt.managedbuilder.pkgconfig.Activator;
 import org.eclipse.cdt.managedbuilder.pkgconfig.util.Parser;
 import org.eclipse.cdt.managedbuilder.pkgconfig.util.PathToToolOption;
 import org.eclipse.cdt.managedbuilder.pkgconfig.util.PkgConfigUtil;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 
 /**
  * TODO: formOtherFlagEntries Bug 349791
  */
 public class PkgConfigExternalSettingProvider extends CExternalSettingProvider {
 
 	public static final String ID = "org.eclipse.cdt.managedbuilder.pkgconfig.extSettings"; //$NON-NLS-1$
 	private final static String PACKAGES = "packages"; //$NON-NLS-1$
 	
 	@Override
 	public CExternalSetting[] getSettings(IProject proj,
 			ICConfigurationDescription cfg) {
         
 		ICSettingEntry[] includes = getEntries(proj, ICSettingEntry.INCLUDE_PATH);
 		ICSettingEntry[] libFiles = getEntries(proj, ICSettingEntry.LIBRARY_FILE); 
 		ICSettingEntry[] libPaths = getEntries(proj, ICSettingEntry.LIBRARY_PATH); 
 		
 		CExternalSetting includeSettings = new CExternalSetting(null, new String[] {
 				"org.eclipse.cdt.core.cSource", "org.eclipse.cdt.core.cxxSource" }, null, includes); //$NON-NLS-1$ //$NON-NLS-2$
 		
 		CExternalSetting libraryFileSettings =
 				new CExternalSetting(null, new String[] {
 				"org.eclipse.cdt.managedbuilder.core.compiledObjectFile" }, null, libFiles); //$NON-NLS-1$
 		
 		CExternalSetting libraryPathSettings =
 				new CExternalSetting(null, new String[] {
 				"org.eclipse.cdt.managedbuilder.core.compiledObjectFile" }, null, libPaths); //$NON-NLS-1$
 		
 		addOtherFlagsToTools(proj);
 		
 		return new CExternalSetting[] { includeSettings, libraryFileSettings, libraryPathSettings };
 	}
 
 	/**
 	 * Get language setting entries for given ICSettingEntry.
 	 * @param proj
 	 * @param settingEntry
 	 * @return
 	 */
 	private static ICLanguageSettingEntry[] getEntries(IProject proj, int settingEntry) {
 		String[] values = null;
 		ICLanguageSettingEntry[] newEntries = null;
 		ICLanguageSetting lang = getGCCLanguageSetting(proj);
 		if (lang!=null) {
 			switch (settingEntry) {
 			case ICSettingEntry.INCLUDE_PATH:
 				values = getIncludePathsFromCheckedPackages(proj);
 				newEntries = formIncludePathEntries(values);
 				lang.setSettingEntries(ICSettingEntry.INCLUDE_PATH, newEntries);
 				break;
 			case ICSettingEntry.LIBRARY_FILE:
 				values = getLibraryFilesFromCheckedPackages(proj);
 				newEntries = formLibraryFileEntries(values);
 				lang.setSettingEntries(ICSettingEntry.LIBRARY_FILE, newEntries);
 				break;
 			case ICSettingEntry.LIBRARY_PATH:
 				values = getLibraryPathsFromCheckedPackages(proj);
 				newEntries = formLibraryPathEntries(values);
 				lang.setSettingEntries(ICSettingEntry.LIBRARY_PATH, newEntries);
 				break;
 			default:
 				break;
 			}
 		}
 		return newEntries;
 	}
 	
 	/**
 	 * Get language settings for given project and language id.
 	 * @param proj
 	 * @param languageId
 	 * @return
 	 */
 	private static ICLanguageSetting getLanguageSetting(IProject proj, String languageId) {
 		ICLanguageSetting[] langSettings = getLanguageSettings(proj);
 		ICLanguageSetting lang = null;
 		for (ICLanguageSetting langSetting : langSettings) {
			if (langSetting.getLanguageId().equalsIgnoreCase(languageId)) {
				lang = langSetting;
				return lang;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Get language settings for the given project.
 	 * @param proj
 	 * @return
 	 */
 	private static ICLanguageSetting[] getLanguageSettings(IProject proj) {
 		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(proj);
 		ICConfigurationDescription activeConf = projectDescription.getActiveConfiguration();
 		ICFolderDescription folderDesc = activeConf.getRootFolderDescription(); 
 		ICLanguageSetting[] langSettings = folderDesc.getLanguageSettings();
 		return langSettings;
 	}
 	
 	/**
 	 * Get language settings for C projects.
 	 * @param proj
 	 * @return
 	 */
 	private static ICLanguageSetting getGCCLanguageSetting(IProject proj) {
 		return getLanguageSetting(proj, "org.eclipse.cdt.core.gcc"); //$NON-NLS-1$
 	}
 	
 	/**
 	 * Form ICIncludePathEntry array from include path String array.
 	 * @param includes
 	 * @return
 	 */
 	private static ICLanguageSettingEntry[] formIncludePathEntries(String[] includes) {
 		ArrayList<ICLanguageSettingEntry> incPathEntries = new ArrayList<ICLanguageSettingEntry>();
 		for(String inc : includes) {
 			ICIncludePathEntry incPathEntry = new CIncludePathEntry(new Path(inc),
 					ICSettingEntry.INCLUDE_PATH);
 			incPathEntries.add(incPathEntry);
 		}
 		return incPathEntries.toArray(new ICLanguageSettingEntry[incPathEntries.size()]);
 	}
 	
 	/**
 	 * Form CLibraryFileEntry array from library file String array.
 	 * @param libs
 	 * @return
 	 */
 	private static ICLanguageSettingEntry[] formLibraryFileEntries(String[] libs) {
 		ArrayList<ICLanguageSettingEntry> libEntries = new ArrayList<ICLanguageSettingEntry>();
 		for(String lib : libs) {
 			CLibraryFileEntry libFileEntry = new CLibraryFileEntry(lib,
 					ICSettingEntry.LIBRARY_FILE);
 			libEntries.add(libFileEntry);
 		}
 		return libEntries.toArray(new ICLanguageSettingEntry[libEntries.size()]);
 	}
 	
 	/**
 	 * Form CLibraryPathEntry array from library path String array.
 	 * @param libPaths
 	 * @return
 	 */
 	private static ICLanguageSettingEntry[] formLibraryPathEntries(String[] libPaths) {
 		ArrayList<ICLanguageSettingEntry> libPathEntries = new ArrayList<ICLanguageSettingEntry>();
 		for(String libPath : libPaths) {
 			CLibraryPathEntry libPathEntry = new CLibraryPathEntry(new Path(libPath),
 					ICSettingEntry.LIBRARY_PATH);
 			libPathEntries.add(libPathEntry);
 		}
 		return libPathEntries.toArray(new ICLanguageSettingEntry[libPathEntries.size()]);
 	}
 	
 	/**
 	 * Get include paths from the checked packages.
 	 * @param proj
 	 * @return
 	 */
 	private static String[] getIncludePathsFromCheckedPackages(IProject proj) {
 		ArrayList<String> includeList = new ArrayList<String>();
 		String[] pkgs = getCheckedPackageNames(proj);
 		String cflags = null;
 		String[] includeArray = null;
 		for (String pkg : pkgs) {
 			cflags = PkgConfigUtil.getCflags(pkg);
 			includeArray = Parser.parseIncPaths(cflags);
 			if (includeArray!=null) {
 				Collections.addAll(includeList, includeArray);
 			}
 		}
 		return includeList.toArray(new String[includeList.size()]);
 	}
 	
 	/**
 	 * Get library files from the checked packages.
 	 * @param proj
 	 * @return
 	 */
 	private static String[] getLibraryFilesFromCheckedPackages(IProject proj) {
 		ArrayList<String> libList = new ArrayList<String>();
 		String[] pkgs = getCheckedPackageNames(proj);
 		String libs = null;
 		String[] libArray = null;
 		for (String pkg : pkgs) {
 			libs = PkgConfigUtil.getLibFilesOnly(pkg);
 			libArray = Parser.parseLibs2(libs);
 			if (libArray!=null) {
 				Collections.addAll(libList, libArray);
 			}
 		}
 		return libList.toArray(new String[libList.size()]);
 	}
 	
 	/**
 	 * Get library paths from the checked packages.
 	 * @param proj
 	 * @return
 	 */
 	private static String[] getLibraryPathsFromCheckedPackages(IProject proj) {
 		ArrayList<String> libPathList = new ArrayList<String>();
 		String[] pkgs = getCheckedPackageNames(proj);
 		String libPaths = null;
 		String[] libPathArray = null;
 		for (String pkg : pkgs) {
 			libPaths = PkgConfigUtil.getLibPathsOnly(pkg);
 			libPathArray = Parser.parseLibPaths2(libPaths);
 			if (libPathArray!=null) {
 				Collections.addAll(libPathList, libPathArray);
 			}
 		}
 		return libPathList.toArray(new String[libPathList.size()]);
 	}
 	
 	/**
 	 * Get other flags from the checked packages.
 	 * @param proj
 	 * @return
 	 */
 	static String[] getOtherFlagsFromCheckedPackages(IProject proj) {
 		ArrayList<String> otherFlagList = new ArrayList<String>();
 		String[] pkgs = getCheckedPackageNames(proj);
 		String cflags = null;
 		String[] otherFlagArray = null;
 		for (String pkg : pkgs) {
 			cflags = PkgConfigUtil.getCflags(pkg);
 			otherFlagArray = Parser.parseCflagOptions(cflags);
 			if (otherFlagArray!=null) {
 				Collections.addAll(otherFlagList, otherFlagArray);
 			}
 		}
 		return otherFlagList.toArray(new String[otherFlagList.size()]);
 	}
 	
 	/**
 	 * Add other flags to Tool's Option.
 	 * @param proj
 	 */
 	private static void addOtherFlagsToTools(final IProject proj) {
 		Job j = new Job("Add other flags") { //$NON-NLS-1$
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				String[] flags = getOtherFlagsFromCheckedPackages(proj);
 				for (String flag : flags) {
 					PathToToolOption.addOtherFlag(flag, proj);
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		j.setPriority(Job.INTERACTIVE);
 		j.schedule();
 	}
 
 	/**
 	 * Get a storage element which stores the checked packages.
 	 * @param proj
 	 * @return
 	 */
 	private static ICStorageElement getPackageStorage(IProject proj) {
 		try {
 			ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(proj);
 			ICConfigurationDescription activeConf = projectDescription.getActiveConfiguration();
 			ICConfigurationDescription desc = activeConf.getConfiguration();
 			ICStorageElement strgElem = null;
 			try {
 				strgElem = desc.getStorage(PACKAGES, true);
 				return strgElem;
 			} catch (CoreException e) {
 				Activator.getDefault().log(e, "Getting packages from the storage failed."); //$NON-NLS-1$
 			}
 		} catch (NullPointerException e) {
 			Activator.getDefault().log(e, "Getting project description failed."); //$NON-NLS-1$
 		}
 		return null;
 	}
 	
 	/**
 	 * Get names of the checked packages.
 	 * @param proj
 	 * @return
 	 */
 	private static String[] getCheckedPackageNames(IProject proj) {
 		ICStorageElement pkgStorage = getPackageStorage(proj);
 		String[] pkgNames = pkgStorage.getAttributeNames();
 		ArrayList<String> pkgs = new ArrayList<String>();
 		String value = null;
 		for(String pkgName : pkgNames) {
 			value = pkgStorage.getAttribute(pkgName);
 			if(value!=null) {
 				if(value.equals("true")) { //$NON-NLS-1$
 					/*
 					 * replace + symbols, because + symbols in package names
 					 * had to be replaced when storing them to
 					 * ICStorageElement to prevent error
 					 */
 					if (pkgName.contains("plus")) { //$NON-NLS-1$
 						pkgName = pkgName.replace("plus", "+"); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 					pkgs.add(pkgName);
 				}
 			}
 		}
 		return pkgs.toArray(new String[pkgs.size()]);
 	}
 	
 }
