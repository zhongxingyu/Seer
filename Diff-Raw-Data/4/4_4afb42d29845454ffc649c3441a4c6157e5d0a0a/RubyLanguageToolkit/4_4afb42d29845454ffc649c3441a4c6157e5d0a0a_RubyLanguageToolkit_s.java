 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.core;
 
 import org.eclipse.dltk.core.AbstractLanguageToolkit;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 
 public class RubyLanguageToolkit extends AbstractLanguageToolkit {
 	private static final String RB_EXTENSION = "rb";
 	private static final String[] RUBY_EXTENSION_ARRAY = new String[] { RB_EXTENSION };
 	protected static RubyLanguageToolkit sToolkit = new RubyLanguageToolkit();
 	public RubyLanguageToolkit() {
 
 	}
 
 	public String getRubyExtension() {
 		return RB_EXTENSION;
 	}
 
 	public boolean languageSupportZIPBuildpath() {
 		return false;
 	}
 
 	public String getNatureId() {
 		return RubyNature.NATURE_ID;
 	}
 
 	public String getDelimeterReplacerString() {
 		return ".";
 	}
 
 	public static IDLTKLanguageToolkit getDefault() {
 		return sToolkit;
 	}
 
 	public String[] getLanguageFileExtensions() {
 		return RUBY_EXTENSION_ARRAY;
 	}
 
 	public String getLanguageName() {
 		return "Ruby";
 	}
 
	protected String getCorePluginID() {
		return RubyPlugin.PLUGIN_ID;
	}

 	public String getLanguageContentType() {
 		return "org.eclipse.dltk.rubyContentType";
 	}
 }
