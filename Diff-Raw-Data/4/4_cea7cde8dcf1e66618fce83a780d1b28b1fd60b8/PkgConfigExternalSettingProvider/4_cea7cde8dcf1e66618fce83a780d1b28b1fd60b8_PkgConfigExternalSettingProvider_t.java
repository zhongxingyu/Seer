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
 import org.eclipse.cdt.managedbuilder.pkgconfig.util.PkgConfigUtil;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 
 /**
  * 
  * TODO: Get settings for other flags.
  * TODO: Cache values
  */
 public class PkgConfigExternalSettingProvider extends CExternalSettingProvider {
 
 	public static final String ID = "org.eclipse.cdt.managedbuilder.pkgconfig.extSettings"; //$NON-NLS-1$
 	private final static String PACKAGES = "packages";
 	
 	@Override
 	public CExternalSetting[] getSettings(IProject proj,
 			ICConfigurationDescription cfg) {
         
 		ICLanguageSettingEntry[] includes = getEntries(proj, ICSettingEntry.INCLUDE_PATH);
 		ICLanguageSettingEntry[] libFiles = getEntries(proj, ICSettingEntry.LIBRARY_FILE); 
 		ICLanguageSettingEntry[] libPaths = getEntries(proj, ICSettingEntry.LIBRARY_PATH); 
 		
 		ArrayList<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
 		Collections.addAll(settings, includes);
 		Collections.addAll(settings, libFiles);
 		Collections.addAll(settings, libPaths);
 
 		CExternalSetting setting =
 				new CExternalSetting(new String[] { "org.eclipse.cdt.core.gcc", "org.eclipse.cdt.core.g++" }, new String[] {
 				"org.eclipse.cdt.core.cSource" }, null,
 				settings.toArray(new ICLanguageSettingEntry[settings.size()]));
 		
 		return new CExternalSetting[] { setting };
 	}
 
 	private static ICLanguageSettingEntry[] getEntries(IProject proj, int settingEntry) {
 		String[] values = null;
 		ICLanguageSettingEntry[] newEntries = null;
 		switch (settingEntry) {
 		case ICSettingEntry.INCLUDE_PATH:
 			values = getIncludePathsFromCheckedPackages(proj);
 			newEntries = formIncludePathEntries(values);
 			break;
 		case ICSettingEntry.LIBRARY_FILE:
 			values = getLibraryFilesFromCheckedPackages(proj);
 			newEntries = formLibraryFileEntries(values);
 			break;
 		case ICSettingEntry.LIBRARY_PATH:
 			values = getLibraryPathsFromCheckedPackages(proj);
 			newEntries = formLibraryPathEntries(values);
 			break;
 		default:
 			break;
 		}
 		ArrayList<ICLanguageSettingEntry> newEntryList = new ArrayList<ICLanguageSettingEntry>();
 		ICLanguageSetting lang = getGCCLanguageSetting(proj);
 		ICLanguageSettingEntry[] entries = null;
 		if (lang!=null) {
 			ICLanguageSettingEntry[] currentEntries = null;
 			switch (settingEntry) {
 			case ICSettingEntry.INCLUDE_PATH:
 				currentEntries = lang.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
 				break;
 			case ICSettingEntry.LIBRARY_FILE:
 				currentEntries = lang.getSettingEntries(ICSettingEntry.LIBRARY_FILE);
 				break;
 			case ICSettingEntry.LIBRARY_PATH:
 				currentEntries = lang.getSettingEntries(ICSettingEntry.LIBRARY_PATH);
 				break;
 			default:
 				break;
 			}
 			Collections.addAll(newEntryList, currentEntries);
 			for (ICLanguageSettingEntry entry : newEntries) {
 				if(!newEntryList.contains(entry) && !entry.getName().equalsIgnoreCase("")) {
 					newEntryList.add(entry);
 				}
 			}
 			entries = (ICLanguageSettingEntry[]) newEntryList.toArray(new ICLanguageSettingEntry[newEntryList.size()]);
 			switch (settingEntry) {
 			case ICSettingEntry.INCLUDE_PATH:
 				lang.setSettingEntries(ICSettingEntry.INCLUDE_PATH, entries);
 				break;
 			case ICSettingEntry.LIBRARY_FILE:
 				lang.setSettingEntries(ICSettingEntry.LIBRARY_FILE, entries);
 				break;
 			case ICSettingEntry.LIBRARY_PATH:
 				lang.setSettingEntries(ICSettingEntry.LIBRARY_PATH, entries);
 				break;
 			default:
 				break;
 			}
 		}
 		return entries;
 	}
 	
 	private static ICLanguageSetting[] getLanguageSettings(IProject proj) {
 		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(proj);
 		ICConfigurationDescription activeConf = projectDescription.getActiveConfiguration();
 		ICFolderDescription folderDesc = activeConf.getRootFolderDescription(); 
 		ICLanguageSetting[] langSettings = folderDesc.getLanguageSettings();
 		return langSettings;
 	}
 	
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
 	
 	private static ICLanguageSetting getGCCLanguageSetting(IProject proj) {
 		return getLanguageSetting(proj, "org.eclipse.cdt.core.gcc");
 	}
 	
 	private static ICLanguageSetting getGPPLanguageSetting(IProject proj) {
 		return getLanguageSetting(proj, "org.eclipse.cdt.core.g++");
 	}
 	
 	private static ICLanguageSettingEntry[] formIncludePathEntries(String[] includes) {
 		ArrayList<ICLanguageSettingEntry> incPathEntries = new ArrayList<ICLanguageSettingEntry>();
 		for(String inc : includes) {
 			ICIncludePathEntry incPathEntry = new CIncludePathEntry(new Path(inc),
 					ICSettingEntry.INCLUDE_PATH);
 			incPathEntries.add(incPathEntry);
 		}
 		return incPathEntries.toArray(new ICLanguageSettingEntry[incPathEntries.size()]);
 	}
 	
 	private static ICLanguageSettingEntry[] formLibraryFileEntries(String[] libs) {
 		ArrayList<ICLanguageSettingEntry> libEntries = new ArrayList<ICLanguageSettingEntry>();
 		for(String lib : libs) {
 			CLibraryFileEntry libFileEntry = new CLibraryFileEntry(new Path(lib),
 					ICSettingEntry.LIBRARY_FILE);
 			libEntries.add(libFileEntry);
 		}
 		return libEntries.toArray(new ICLanguageSettingEntry[libEntries.size()]);
 	}
 	
 	private static ICLanguageSettingEntry[] formLibraryPathEntries(String[] libPaths) {
 		ArrayList<ICLanguageSettingEntry> libPathEntries = new ArrayList<ICLanguageSettingEntry>();
 		for(String libPath : libPaths) {
			CLibraryPathEntry libPathEntry = new CLibraryPathEntry(new Path(libPath),
 					ICSettingEntry.LIBRARY_PATH);
 			libPathEntries.add(libPathEntry);
 		}
 		return libPathEntries.toArray(new ICLanguageSettingEntry[libPathEntries.size()]);
 	}
 	
 	private static String[] getOtherFlagsFromCheckedPackages(IProject proj) {
 		ArrayList<String> otherFlagList = new ArrayList<String>();
 		String[] pkgs = getCheckedPackageNames(proj);
 		String cflags = null;
 		String[] otherFlagArray = null;
 		for (String pkg : pkgs) {
 			cflags = PkgConfigUtil.getCflags(pkg);
 			otherFlagArray = Parser.parseCflagOptions(cflags);
 			Collections.addAll(otherFlagList, otherFlagArray);
 		}
 		return otherFlagList.toArray(new String[otherFlagList.size()]);
 	}
 	
 	private static String[] getIncludePathsFromCheckedPackages(IProject proj) {
 		ArrayList<String> includeList = new ArrayList<String>();
 		String[] pkgs = getCheckedPackageNames(proj);
 		String cflags = null;
 		String[] includeArray = null;
 		for (String pkg : pkgs) {
 			cflags = PkgConfigUtil.getCflags(pkg);
 			includeArray = Parser.parseIncPaths(cflags);
 			Collections.addAll(includeList, includeArray);
 		}
 		return includeList.toArray(new String[includeList.size()]);
 	}
 	
 	private static String[] getLibraryFilesFromCheckedPackages(IProject proj) {
 		ArrayList<String> libList = new ArrayList<String>();
 		String[] pkgs = getCheckedPackageNames(proj);
 		String libs = null;
 		String[] libArray = null;
 		for (String pkg : pkgs) {
 			libs = PkgConfigUtil.getLibFilesOnly(pkg);
 			libArray = Parser.parseLibs2(libs);
 			Collections.addAll(libList, libArray);
 		}
 		return libList.toArray(new String[libList.size()]);
 	}
 	
 	private static String[] getLibraryPathsFromCheckedPackages(IProject proj) {
 		ArrayList<String> libPathList = new ArrayList<String>();
 		String[] pkgs = getCheckedPackageNames(proj);
 		String libPaths = null;
 		String[] libPathArray = null;
 		for (String pkg : pkgs) {
 			libPaths = PkgConfigUtil.getLibPathsOnly(pkg);
 			libPathArray = Parser.parseLibPaths2(libPaths);
 			Collections.addAll(libPathList, libPathArray);
 		}
 		return libPathList.toArray(new String[libPathList.size()]);
 	}
 	
 	private static ICStorageElement getPackageStorage(IProject proj) {
 		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(proj);
 		ICConfigurationDescription activeConf = projectDescription.getActiveConfiguration();
 		ICConfigurationDescription desc = activeConf.getConfiguration();
 		ICStorageElement strgElem = null;
 		try {
 			strgElem = desc.getStorage(PACKAGES, true);
 		} catch (CoreException e) {
 			Activator.getDefault().log(e, "Getting packages from the storage failed.");
 		}
 		return strgElem;
 	}
 	
 	private static String[] getCheckedPackageNames(IProject proj) {
 		ICStorageElement pkgStorage = getPackageStorage(proj);
 		String[] pkgNames = pkgStorage.getAttributeNames();
 		ArrayList<String> pkgs = new ArrayList<String>();
 		String value = null;
 		for(String pkgName : pkgNames) {
 			value = pkgStorage.getAttribute(pkgName);
 			if(value!=null) {
 				if(value.equals("true")) {
 					pkgs.add(pkgName);
 				}
 			}
 		}
 		return (String[]) pkgs.toArray(new String[pkgs.size()]);
 	}
 	
 }
