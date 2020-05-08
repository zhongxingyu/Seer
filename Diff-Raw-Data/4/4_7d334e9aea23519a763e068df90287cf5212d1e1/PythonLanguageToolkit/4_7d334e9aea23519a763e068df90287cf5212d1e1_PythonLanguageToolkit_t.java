 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.python.core;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.AbstractLanguageToolkit;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 
 public class PythonLanguageToolkit extends AbstractLanguageToolkit {
 	private static final String[] langaugeExtensions = new String[] { "py" };
 	private static PythonLanguageToolkit sInstance = new PythonLanguageToolkit();
 
 	public PythonLanguageToolkit() {
 	}
 
 	public boolean languageSupportZIPBuildpath() {
 		return true;
 	}
 
 	public boolean validateSourcePackage(IPath path) {
 		File file = new File(path.toOSString());
 		if (file != null) {
 			String members[] = file.list(new FilenameFilter() {
 
 				public boolean accept(File dir, String name) {
 					if (name.toLowerCase().equals("__init__.py")) {
 						return true;
 					}
 					return false;
 				}
 			});
 			if (members.length > 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String getNatureId() {
 		return PythonNature.NATURE_ID;
 	}
 
 	public static IDLTKLanguageToolkit getDefault() {
 		return sInstance;
 	}
 
 	public String getDelimeterReplacerString() {
 		return ".";
 	}
 
 	public String[] getLanguageFileExtensions() {
 		return langaugeExtensions;
 	}
 
 	public String getLanguageName() {
 		return "Python";
 	}
 
 	public String getLanguageContentType() {
 		return "org.eclipse.dltk.pythonContentType";
 	}
 }
