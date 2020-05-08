 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.loader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.vjet.dsf.jsgroup.bootstrap.JsLibBootstrapLoader;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.ts.IJstTypeLoader;
 import org.eclipse.vjet.dsf.jst.ts.util.JstSrcFileCollector;
 import org.eclipse.vjet.dsf.jst.ts.util.JstTypeSerializer;
 import org.eclipse.vjet.dsf.jstojava.parser.VjoParser;
 import org.eclipse.vjet.dsf.ts.event.group.AddGroupEvent;
 
 /**
  * Default Jst type loader will load JSt source from group src dir or jar library
  * ToDo: Should be enhanced to load serilized JstTypes directly * 
  *
  */
 
 public class DefaultJstTypeLoader implements IJstTypeLoader {
 	
 	private FileSuffix[] m_acceptedSuffixes;
 	private static final String[] m_seperators = {"/", File.separator }; 
 
 	public DefaultJstTypeLoader() {
 		this(FileSuffix.js,FileSuffix.vjo, FileSuffix.ser);
 	}
 	
 	public DefaultJstTypeLoader(FileSuffix ... acceptedSuffixes) {
 		m_acceptedSuffixes = acceptedSuffixes;
 	}
 	
 	public enum FileSuffix{
 		js(".js"),
 		vjo(".vjo"),
 		ser(".ser");
 		
 		String m_value;
 		
 		private FileSuffix(String val){
 			m_value = val;
 		}
 		
 		public String getValue(){
 			return m_value;
 		}
 		
 	}
 	
 	public List<SourceType> loadJstTypes(List<AddGroupEvent> groupList) {
 		
 		List<SourceType> typeList = new ArrayList<SourceType>();
 		
 		for (AddGroupEvent group : groupList) {
 			String groupName = group.getGroupName();
 			String groupPath = group.getGroupPath();
 			List<String> srcPathList = group.getSourcePathList();
 			
 			if (srcPathList == null || srcPathList.size() == 0) {
 				File groupFolderOrFile = getGroupSrcFolder(groupPath, null);			
 				typeList.addAll(loadJstTypesFromGroup(groupName, groupFolderOrFile));
 			}
 			else {
 				for (String srcPath : srcPathList) {					
 					File groupFolderOrFile = getGroupSrcFolder(groupPath, srcPath);			
 					typeList.addAll(loadJstTypesFromGroup(groupName, groupFolderOrFile));
 				}
 			}
 		}
 		
 		return typeList;
 		
 	}
 	
 	private List<SourceType> loadJstTypesFromGroup(String groupName, File groupFolderOrFile) {
 		
 		ArrayList<SourceType> srcTypeList = new ArrayList<SourceType>();
 		
 		if (groupFolderOrFile != null) {
 			
 			try {
 				if (groupFolderOrFile.getCanonicalFile().isDirectory()) {
 					return loadJstTypesFromProject(groupName, groupFolderOrFile);
 				}
 				else {
 					return loadJstTypesFromLibrary(groupName, groupFolderOrFile);
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		return srcTypeList; // return empty list
 		
 	}
 	
 	private File getGroupSrcFolder(String groupPath, String srcPath) {
 		System.out.println("groupPath:" + groupPath + ", srcPath:" + srcPath);
 		if (groupPath != null) {
 			File groupFile = new File(groupPath);
			// handle spaces or other utf-8 chars
			groupPath = decodePath(groupPath);
 			if (groupFile.exists()) {
 				if (!groupFile.isDirectory() || srcPath == null) {
 					return groupFile;
 				}
 				else {
 					File srcFolder = new File(groupFile, srcPath);
 					return srcFolder;
 				}
 			}
 		}
 		else if (srcPath != null) {
			srcPath = decodePath(srcPath);
 			File srcFolder = new File(srcPath);
 			
 			if (srcFolder.exists() && srcFolder.isDirectory()) {
 				return srcFolder;
 			}			
 		}
 		
 		return null;
 		
 	}
 	
	private String decodePath(String groupPath) {
		try {
			groupPath = URLDecoder.decode(groupPath, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return groupPath;
	}
	
 	protected List<SourceType> loadJstTypesFromProject(String groupName, File srcFolder) {
 		
 		JstSrcFileCollector fileColl = new JstSrcFileCollector();
 		ArrayList<SourceType> srcTypeList = new ArrayList<SourceType>();
 		
 		if (srcFolder != null) {
 			
 			List<File> list = fileColl.getJsSrcFiles(srcFolder);
 			
 			for (File file : list) {
 								
 				try {			
 					if(isAccepted(file)){
 						srcTypeList.add(createType(groupName, srcFolder.getPath(), file));
 					}
 				}
 				catch (IOException e) {
 					e.printStackTrace();
 				}								
 			}			
 		}
 		
 		return srcTypeList;
 		
 	}
 	
 	protected boolean isAccepted(File file) {
 		return isAcceptedSuffix(file.getPath());
 	}
 
 	protected List<SourceType> loadJstTypesFromLibrary(String groupName, File libFile) {
 		List<SourceType> typeList = new ArrayList<SourceType>();
 		
 		String libFileName = libFile.getName().toLowerCase();
 
 		if (libFileName.endsWith(".zip") || libFileName.endsWith(".jar")) {
 			
 			try {				
 				ZipFile jarFile = new ZipFile(libFile);
 				
 				// load in bootstrap.js first
 				ZipEntry bootstrapEntry = jarFile.getEntry("bootstrap.js");
 				if(bootstrapEntry!=null){
 					InputStream stream = jarFile.getInputStream(bootstrapEntry);
 					JsLibBootstrapLoader.load(VjoParser.load(stream, "bootstrap.js"), groupName);
 				}
 			
 				Enumeration<? extends ZipEntry> enumeration = jarFile.entries();
 				
 				while (enumeration.hasMoreElements()) {
 	
 					ZipEntry elem = enumeration.nextElement();
 	
 					if (acceptZipEntry(elem)) {
 						if (elem.getName().endsWith(".ser")) {
 							typeList.addAll(loadAllTypes(groupName, jarFile, elem));
 						}
 						else if(!elem.getName().contains("bootstrap.js")) {
 							typeList.add(createType(groupName, jarFile, elem));
 						}
 					}
 				}					
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}				
 		}		
 		
 		return typeList;
 	}
 	
 	private boolean acceptZipEntry(ZipEntry elem) {
 		if (!elem.isDirectory()) {
 			String name = elem.getName();
 			
 			if(isAcceptedSuffix(name)){
 				return true; 
 			}
 		}
 	
 		return false;
 	}
 
 	private boolean isAcceptedSuffix(String name) {
 		for(FileSuffix suf : m_acceptedSuffixes){
 			if(name.endsWith(suf.getValue())){
 				return true;
 			}
 				
 		}
 		return false;
 	}
 	
 	private static String replaceSeperators(String fileName) {
 		
 		String typeName = fileName;
 		
 		if (fileName != null && fileName.length() != 0) {	
 			
 			for (String seperator : m_seperators) {
 				typeName = typeName.replace(seperator, ".");
 			}
 		}
 		
 		return typeName;
 		
 	}
 	
 	public static SourceType createType(String groupName, String srcPath, File file) 
 		throws IOException 
 		{
 		String fileName = file.getPath();
 		
 		// replace / and \ with .
 		fileName = replaceSeperators(fileName);
 		
 		String srcFolder = srcPath;
 		srcFolder = replaceSeperators(srcFolder);
 		
 		int start = fileName.lastIndexOf(srcFolder);
 		start += srcFolder.length() + 1; // skip the trailing .
 		
 		int end = fileName.length();
 			
 		//TODO figure out why we need static check here if loader only load specific extensions?
 		if (fileName.endsWith(".js")) {
 			end = fileName.lastIndexOf(".js"); // remove .js from type name
 		}
 		else if (fileName.endsWith(".vjo")) {
 			end = fileName.lastIndexOf(".vjo"); // remove .vjo from type name
 		}
 		
 		String typeName = fileName.substring(start, end);
 		
 		StringBuilder builder = new StringBuilder();		
 	
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 	
 		char[] cbuf = new char[10000];
 		int len = 0;
 	
 		while ((len = reader.read(cbuf)) != -1) {
 			builder.append(cbuf, 0, len);
 		}
 			
 		reader.close();
 		
 		SourceType srcType = new SourceType(groupName, typeName, builder.toString(), file);
 		
 		return srcType;		
 	}
 	
 	public static SourceType createType(String groupName, ZipFile jarFile, ZipEntry elem) throws IOException {
 		
 		String typeName = elem.getName();
 		int end = typeName.length();
 		
 		//TODO figure out why we need static check here if loader only load specific extensions?
 		if (typeName.endsWith(".js")) {
 			end = typeName.lastIndexOf(".js"); // remove .js from type name
 		}
 		else if (typeName.endsWith(".vjo")) {
 			end = typeName.lastIndexOf(".vjo"); // remove .vjo from type name
 		}
 		
 		typeName = typeName.substring(0, end);	
 		
 		typeName = replaceSeperators(typeName);
 		
 		InputStream stream = jarFile.getInputStream(elem);
 		byte[] bs = new byte[stream.available()];
 		stream.read(bs);
 		stream.close();
 		
 		String source = new String(bs);
 		
 		SourceType srcType = new SourceType(groupName, typeName, source);
 		
 		return srcType;		
 	}
 	
 	protected List<SourceType> loadAllTypes(String groupName, ZipFile jarFile, ZipEntry elem) throws IOException {
 		
 		InputStream stream = jarFile.getInputStream(elem);
 		
 		List<IJstType> jstTypes = JstTypeSerializer.getInstance().deserialize(stream);
 		
 		List<SourceType> srcTypes = new ArrayList<SourceType>();
 		
 		for (IJstType type : jstTypes) {
 			srcTypes.add(new SourceType(groupName, type));			
 		}
 		
 		return srcTypes;		
 	}	
 }
