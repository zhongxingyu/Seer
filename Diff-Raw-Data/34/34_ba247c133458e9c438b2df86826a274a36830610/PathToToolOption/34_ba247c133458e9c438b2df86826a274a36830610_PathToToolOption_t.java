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
 package org.eclipse.cdt.managedbuilder.pkgconfig.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.cdt.managedbuilder.core.IConfiguration;
 import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
 import org.eclipse.cdt.managedbuilder.core.IOption;
 import org.eclipse.cdt.managedbuilder.core.ITool;
 import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
 import org.eclipse.cdt.managedbuilder.pkgconfig.Activator;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 
 /**
  * Add other flags to compiler's miscellaneous option.
 
  */
 public class PathToToolOption {
 
 	//tool input extensions
 	private static final String[] inputTypes = {"cpp", "c"};  //$NON-NLS-1$ //$NON-NLS-2$
 	
 	private final static String OtherFlagsOptionName = "Other flags"; //$NON-NLS-1$
 
 	/**
 	 * Adds new other flag to Compiler's Other flags option.
 	 * 
 	 * @param otherFlag Include path to be added to Compiler's Include Option 
 	 */
 	public static void addOtherFlag(String otherFlag, IProject proj) {
 		if (proj != null && (otherFlag.length()>0)) {
 			IConfiguration cf = getActiveBuildConf(proj);
 			if (cf != null) {
 				//Add path to compiler's Other flags Option
 				addOtherFlagToToolOption(cf, otherFlag);
 			}
 		}
 	}
 
 	/**
 	 * Adds an other flag to compiler's Other flags Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param otherFlag Other flag
 	 * @return boolean Returns true if Other flags Option was added successfully to the compiler.
 	 */
 	private static boolean addOtherFlagToToolOption(IConfiguration cf, String otherFlag) {
 		ITool compiler = getCompiler(cf);
 		//If the compiler is found from the given build configuration
 		if (compiler != null) {
 			//get compiler's Other flags option
 			IOption otherFlagOption = getCompilerOtherFlagsOption(cf);
 			if (otherFlagOption != null) {
 				//add other flag to compiler's Other flags Option type
 				boolean val = addOtherFlagToToolOption(cf, compiler, otherFlagOption, otherFlag);
 				return val;
 			}
 			return false;
 		} 
 		//adding the other flag failed
 		return false;
 	}
 
 	/**
 	 * Adds new other flag to the Compiler's Other flags Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param cfTool ITool Tool
 	 * @param option Tool Option type
 	 * @param newOtherFlag
 	 * @since 8.0
 	 */
 	private static boolean addOtherFlagToToolOption(IConfiguration cf, ITool cfTool, IOption option, String newOtherFlag) {
 		String flags = option.getValue().toString();
 		if (flags == null) {
 			flags = ""; //$NON-NLS-1$
 		}
 
 		if (!flags.contains(newOtherFlag)) {
 			//append the new flag to existing flags
 			flags = flags+" "+newOtherFlag; //$NON-NLS-1$
 
 			//add a new other flag to compiler's other flags option.
 				ManagedBuildManager.setOption(cf, cfTool, option, flags);
 		} else {
 			return false;
 		}
 		return false;
 	}
 
 	/**
 	 * Return compiler according to the input type.
 	 * @param cf IConfiguration Build configuration
 	 * @return ITool Compiler
 	 */
 	private static ITool getCompiler(IConfiguration cf) {
 		//get compiler according to the input type
 		for(int i=0; i<inputTypes.length; i++) {
 			ITool tool = getIToolByInputType(cf, inputTypes[i]);
 			if (tool != null) {
 				return tool;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns ITool associated based on the input extension.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param ext input extension associated with ITool
 	 * @return ITool Tool that matches input extension
 	 */
 	private static ITool getIToolByInputType(IConfiguration cf, String ext) {
 		//get ITool associated with the input extension
 		return cf.getToolFromInputExtension(ext);
 	}
 
 	/**
 	 * Returns compiler's Other flags Option type.
 	 * 
 	 * @param cf IConfiguration Project build configuration
 	 * @return IOption Tool option type
 	 */
 	private static IOption getCompilerOtherFlagsOption(IConfiguration cf) {
 		//get ITool associated with the input extension
 		ITool cfTool = getCompiler(cf);
 		//get option id for other flags
 		String otherFlagsOptionId = getOptionIdByName(cfTool, OtherFlagsOptionName);
 		return getToolOptionType(cfTool, otherFlagsOptionId);
 	}
 
 	/**
 	 * Returns Tool's option id.
 	 * 
 	 * @param cfTool ITool Tool
 	 * @param name Option's name
 	 * @return optionId Tool's option id.
 	 */
 	private static String getOptionIdByName(ITool cfTool, String name) {
 		String optionId = null;
 		//get all Tool options.
 		IOption[] options = cfTool.getOptions();
 		for (IOption opt : options) {
			if(opt != null) {
				//try to match option name
				if(opt.getName().equalsIgnoreCase(name)) {
					//get option id
					optionId = opt.getId();
					break;
				}
 			}
 		}	
 		return optionId;
 	}
 	
 	/**
 	 * Returns Tool's Option type by Id.
 	 * 
 	 * @param cfTool ITool Tool
 	 * @param optionId String Tool option type id
 	 * @return IOption Tool option type
 	 */
 	private static IOption getToolOptionType(ITool cfTool, String optionId) {
 		//get option type with specific id for the ITool
 		return cfTool.getOptionById(optionId);
 	}
 
 	/**
 	 * Adds one or more paths to the list of paths.
 	 * 
 	 * @param existingPaths Existing list of paths to add to
 	 * @param newPath New path to add. May include multiple directories with a path delimiter.
 	 * @return String[] List that includes existing paths as well as new paths.
 	 */
 	public static String[] addNewPathToExistingPathList(String[] existingPaths, String newPath) {
 		List<String> newPathList = new ArrayList<String>();
 		String path;
 		//adds existing paths to new paths list
 		for (int i = 0; i < existingPaths.length; i++) {
 			path = existingPaths[i];
 			newPathList.add(path);
 		}
 		//separates new path if it has multiple paths separated by a path separator
 		String[] newPathArray = newPath.split(Separators.getPathSeparator());
 		for (int i = 0; i < newPathArray.length; i++) {
 			path = newPathArray[i];
 			newPathList.add(path);
 		}
 		//creates a new list that includes all existing paths as well as new paths
 		String[] newArray = newPathList.toArray(new String[0]);
 		return newArray;
 	}
 
 	/**
 	 * Get the active build configuration.
 	 * 
 	 * @param proj IProject
 	 * @return IConfiguration
 	 */
 	public static IConfiguration getActiveBuildConf(IProject proj) {
 		IConfiguration conf = null;
 		IManagedBuildInfo info = null;
 		//try to get Managed build info
 		try {
 			info = ManagedBuildManager.getBuildInfo(proj); //null if doesn't exists
 		} catch (Exception e) { //if not a managed build project
 			Activator.getDefault().log(IStatus.INFO, e, "Not a managed build project."); //$NON-NLS-1$
 			return conf;
 		}
 		//info can be null for projects without build info. For example, when creating a project
 		//from Import -> C/C++ Executable
 		if(info == null) {
 			return conf;
 		}
 		conf = info.getDefaultConfiguration();
 		return conf;
 	}
 	
 }
