 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.codegen;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.vjet.dsf.jstojava.loader.DefaultJstTypeLoader;
 import org.eclipse.vjet.dsf.ts.event.group.AddGroupEvent;
 
 public class CodeGenTestJstTypeLoader extends DefaultJstTypeLoader {
 	private FileSuffix[] m_acceptedSuffixes;
 	private String m_grpPath;
 	@Override
 	public List<SourceType> loadJstTypes(List<AddGroupEvent> groupList) {
 		List<SourceType> typeList = new ArrayList<SourceType>();
 
 		for (AddGroupEvent group : groupList) {
 			String groupName = group.getGroupName();
 			String groupPath = group.getGroupPath();
 			List<String> srcPathList = group.getSourcePathList();
 
 			if (srcPathList == null || srcPathList.size() == 0) {
 				File groupFolderOrFile = getGroupSrcFolder(groupPath, null);
 				typeList.addAll(loadJstTypesFromGroup(groupName,
 						groupFolderOrFile, null));
 			} else {
 				for (String srcPath : srcPathList) {
 					File groupFolderOrFile = getGroupSrcFolder(groupPath,
 							srcPath);
 					typeList.addAll(loadJstTypesFromGroup(groupName,
 							groupFolderOrFile, srcPath));
 				}
 			}
 		}
 
 		return typeList;
 	}
 
 	private List<SourceType> loadJstTypesFromGroup(String groupName,
 			File groupFolderOrFile, String srcPath) {
 		ArrayList<SourceType> srcTypeList = new ArrayList<SourceType>();
 		if (groupFolderOrFile != null) {
 			if (groupFolderOrFile.isDirectory()) {
 				return loadJstTypesFromProject(groupName, groupFolderOrFile);
 			} else {
 				return loadJstTypesFromLibrary(groupName, groupFolderOrFile, srcPath);
 			}
 		}
 		return srcTypeList; // return empty list
 	}
 
 	protected List<SourceType> loadJstTypesFromLibrary(String groupName,
 			File libFile, String srcPath) {
 		List<SourceType> typeList = new ArrayList<SourceType>();
 		String libFileName = libFile.getName().toLowerCase();
 		if (libFileName.endsWith(".zip") || libFileName.endsWith(".jar")) {
 			try {
 				ZipFile jarFile = new ZipFile(libFile);
 				Enumeration<? extends ZipEntry> enumeration = jarFile.entries();
 				while (enumeration.hasMoreElements()) {
 					ZipEntry elem = enumeration.nextElement();
 					if (acceptZipEntry(elem) && elem.getName().startsWith(srcPath)) {
 						if (elem.getName().endsWith(".ser")) {
 							typeList.addAll(loadAllTypes(groupName, jarFile,
 									elem));
 						} else {
 							SourceType st = createType(groupName, jarFile, elem);
 							typeList.add(st);
 						}
 					}
 				}
 			} catch (IOException e) {
 			}
 		}
 		return typeList;
 	}
 
 	private boolean acceptZipEntry(ZipEntry elem) {
 		if (!elem.isDirectory()) {
 			String name = elem.getName();
 			if (isAcceptedSuffix(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isAcceptedSuffix(String name) {
 		for (FileSuffix suf : m_acceptedSuffixes) {
 			if (name.endsWith(suf.getValue())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private File getGroupSrcFolder(String groupPath, String srcPath) {
 		if (groupPath != null) {
 			m_grpPath = groupPath;
 			URL url = this.getClass().getResource(groupPath+srcPath);
 			if(groupPath.endsWith("!/")){
 				groupPath = groupPath.substring(0, groupPath.indexOf("!/"));
 			}
 			File groupFile = new File(groupPath);
 			
 			if(!groupFile.exists()){
 				// try with forward slash in front of path
 				groupFile = new File("/" +groupPath);
 			}
 			
 			if (groupFile.exists()) {
 				if (!groupFile.isDirectory() || srcPath == null) {
 					return groupFile;
 				} else {
 					File srcFolder = new File(groupFile, srcPath);
 					return srcFolder;
 				}
 			}
 		} else if (srcPath != null) {
 			File srcFolder = new File(srcPath);
 
 			if (srcFolder.exists() && srcFolder.isDirectory()) {
 				return srcFolder;
 			}
 		}
 		return null;
 	}
 
 	public FileSuffix[] getM_acceptedSuffixes() {
 		return m_acceptedSuffixes;
 	}
 
 	public void setM_acceptedSuffixes(FileSuffix[] suffixes) {
 		m_acceptedSuffixes = suffixes;
 	}
 }
