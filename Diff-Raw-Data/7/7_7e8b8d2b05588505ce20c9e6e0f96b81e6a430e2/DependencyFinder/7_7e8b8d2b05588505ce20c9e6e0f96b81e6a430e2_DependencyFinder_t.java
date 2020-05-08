 package com.shivanshusingh.pluginanalyser.comparison;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.shivanshusingh.pluginanalyser.utils.Util;
 import com.shivanshusingh.pluginanalyser.utils.logging.Log;
 import com.shivanshusingh.pluginanalyser.utils.parsing.Constants;
 import com.shivanshusingh.pluginanalyser.utils.parsing.ParsingUtil;
 
 /**
  * @author Shivanshu Singh
  * 
  */
 public class DependencyFinder {
 
 	static Map<String, ImpExp> functions = new HashMap<String, ImpExp>();
 	static Set<String> pluginExtractsIgnored = new HashSet<String>();
 	static Map<String, ImpExp> types = new HashMap<String, ImpExp>();
 
 	// sets of plugins
 	static Map<String, PluginObject> plugins = new HashMap<String, PluginObject>();
 
 	/**
 	 * 
 	 * this function build the superset of dependencies from the extract files
 	 * to finally figure out what dependencies were unstated or missing.
 	 * 
 	 * @param pathToBasePluginExtractsDir
 	 * @param pathToPluginDependencyAnalysisOutputLocation
 	 * @param considerBundleExportersOnly
 	 *            true if only those exports to consider that are being
 	 *            explicitly being specified as exported by plugins in their
 	 *            plugin manifests. CAUTION: Exercise caution In case the bundle
 	 *            manifest information listing exported packages is missing,
 	 *            setting this to true in this case will essentially mean that
 	 *            the plugin has no exports whatsoever.
 	 * @param ignoreBundlesMarkedToBeIgnored
 	 *            true if you want the plugins marked as to be ignored in the
 	 *            Plugin Extracts, to be ignored when building the dependency,
 	 *            false if you would want them to be not ignored. extracts.
 	 * @param alsoConsiderInvokationSatisfactionProxies
 	 *            true if the invokation proxies should be considered as well
 	 *            when building the dependency set and checking if any of those
 	 *            can in turn satisfy some original invokation.
 	 * @param eraseOld
 	 * @throws IOException
 	 */
 	public static void buildPluginDependencySuperSet(String pathToBasePluginExtractsDir,
 			String pathToPluginDependencyAnalysisOutputLocation, boolean considerBundleExportersOnly,
 			boolean ignoreBundlesMarkedToBeIgnored, boolean alsoConsiderInvokationSatisfactionProxies, boolean eraseOld)
 			throws IOException {
 
 		Log.outln("==== Now Building the  Plugin Dependency  Set from source: " + pathToBasePluginExtractsDir
 				+ " , considerBundleExportsOnly is " + considerBundleExportersOnly + " ====");
 		Log.errln("==== Now Building the  Plugin Dependency  Set from source: " + pathToBasePluginExtractsDir
 				+ " , considerBundleExportsOnly is " + considerBundleExportersOnly + " ====");
 
 		long time1 = System.currentTimeMillis();
 
 		File pluginExtractDirectory = new File(pathToBasePluginExtractsDir);
 
 		if (!Util.checkDirectory(pluginExtractDirectory, true, true, true, false)) {
 			// the plugin extracts source base directory is not accessible.
 			Log.errln("xxxx in buildPluginDependencySuperSet \\\n the plugin dir: "
 					+ pluginExtractDirectory.getAbsolutePath()
 					+ "\n is not a directory or not readable or   does not exist. \nxxxx");
 			return;
 		}
 		if (!Util.checkAndCreateDirectory(pathToPluginDependencyAnalysisOutputLocation)) {
 			Log.errln("xxxx in buildPluginDependencySuperSet \\\n the output location dir: "
 					+ pathToPluginDependencyAnalysisOutputLocation + "\n is not accessible. \nxxxx");
 			return;
 		}
 
 		if (eraseOld) {
 			Util.clearFolder(new File(pathToPluginDependencyAnalysisOutputLocation));
 		}
 		File[] pluginExtractEntries = pluginExtractDirectory.listFiles();
 		int entriesLength = pluginExtractEntries.length;
 		if (entriesLength < 2) {
 
 			Log.errln("XXXX  \n "
 					+ pluginExtractDirectory.getAbsolutePath()
 					+ "\n has has than 2 extracts, cannot compare.\n The location must have at least 2 extract files.  \nXXXX  ");
 		}
 
 		long pluginExtractsDone = 0;
 
 		for (File pluginExtract : pluginExtractEntries) {
 			pluginExtractsDone++;
 			if (Util.checkFile(pluginExtract, true, true, true, false)) {
 				/**
 				 * this is the name of the PluginExtract file, without the
 				 * extension. this is for obtaining the fully qualified plugin
 				 * name with version and qualifier, even if that information is
 				 * not in the pluginExtract file, cause because of an originally
 				 * missing manifest.mf file for thus plugin.
 				 */
 				String thisPluginExtractName = pluginExtract.getName().trim();
				
				// ignoring the ones that are not plugin or other extracts
				if(!thisPluginExtractName.endsWith(Constants.EXTRACT_FILE_EXTENSION_PLUGIN))
					continue;
				
 				int startingIndex = thisPluginExtractName.indexOf(Constants.EXTRACT_FILE_PREFIX_PLUGIN)
 						+ Constants.EXTRACT_FILE_PREFIX_PLUGIN.length();
				
 				thisPluginExtractName = thisPluginExtractName.substring(startingIndex);
 				if (thisPluginExtractName.toLowerCase().endsWith(Constants.EXTRACT_FILE_EXTENSION_PLUGIN))
 					thisPluginExtractName = thisPluginExtractName.substring(0, thisPluginExtractName.length()
 							- Constants.EXTRACT_FILE_EXTENSION_PLUGIN.length());
 				// Log.outln("==== Adding to DependencySet,  plugin " +
 				// pluginExtractsDone + " of " + entriesLength + " : "
 				// + thisPluginExtractName + "====");
 
 				// restoring the functions information from the file.
 
 				boolean ignorePluginExtract = false;
 				Set<String> ignoreBundleProperty = ParsingUtil.restorePropertyFromExtract(pluginExtract,
 						Constants.BUNDLE_IGNORE);
 				if (ignoreBundlesMarkedToBeIgnored) {
 					if (null != ignoreBundleProperty && 1 == ignoreBundleProperty.size())
 						ignorePluginExtract = ignoreBundleProperty.toString().toLowerCase().trim().contains("true");
 				}
 				// System.out.println("==== ignoreBundleProperty= "+ignoreBundleProperty.toString());
 				if (ignorePluginExtract) {
 					Log.outln("==== " + thisPluginExtractName + " must be IGNORED");
 					Log.errln("==== " + thisPluginExtractName + " must be IGNORED");
 					pluginExtractsIgnored.add(thisPluginExtractName);
 					continue;
 
 				}
 
 				Set<String> bundleExports = new HashSet<String>();
 				if (considerBundleExportersOnly) {
 					bundleExports = ParsingUtil.restorePropertyFromExtract(pluginExtract, Constants.BUNDLE_EXPORTS);
 				}
 
 				Set<String> myMethodExports = ParsingUtil.restorePropertyFromExtract(pluginExtract,
 						Constants.PLUGIN_ALL_MY_METHODS_PUBLIC);
 				Set<String> myMethodImports = ParsingUtil.restorePropertyFromExtract(pluginExtract,
 						Constants.PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL_AND_NON_JAVA);
 				Set<String> myTypeExports = ParsingUtil.restorePropertyFromExtract(pluginExtract,
 						Constants.PLUGIN_ALL_MY_TYPES_PUBLIC);
 				Set<String> myTypeImports = ParsingUtil.restorePropertyFromExtract(pluginExtract,
 						Constants.PLUGIN_ALL_TYPES_DETECTED_EXTERNAL_AND_NON_JAVA);
 				Set<String> superClassesAndInterfacesSuperSet = ParsingUtil.restorePropertyFromExtract(pluginExtract,
 						Constants.PLUGIN_ALL_INHERITANCE_AND_INTERFACE_PAIRS);
 				Set<String> myInvokationProxyPairs = ParsingUtil.restorePropertyFromExtract(pluginExtract,
 						Constants.PLUGIN_ALL_INVOKATION_PROXY_PAIRS);
 
 				// //////////////////////////////////////////
 				// building DependencyFinder.plugins object
 				// /////////////////////////////////////////
 
 				PluginObject po = new PluginObject();
 				if (plugins.containsKey(thisPluginExtractName))
 					po = plugins.get(thisPluginExtractName);
 
 				po.name = thisPluginExtractName;
 
 				// building the superclassAndInterface SuperSet
 				for (String pair : superClassesAndInterfacesSuperSet) {
 					String[] pairElements = pair.split(Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE);
 					String baseType = pairElements[0].trim();
 					String superType = pairElements[1].trim();
 
 					Set<String> depTypes = new HashSet<String>();
 					if (po.superClassesAndInterfaces.containsKey(baseType))
 						depTypes = po.superClassesAndInterfaces.get(baseType);
 					depTypes.add(superType);
 					po.superClassesAndInterfaces.put(baseType, depTypes);
 
 				}
 
 				// building the interfaceProxies Set
 				for (String pair : myInvokationProxyPairs) {
 					String[] pairElements = pair.split(Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE);
 					String invokationBase = pairElements[0].trim();
 					String invokationProxy = pairElements[1].trim();
 
 					Set<String> invokationProxies = new HashSet<String>();
 					if (po.invokationProxies.containsKey(invokationBase))
 						invokationProxies = po.invokationProxies.get(invokationBase);
 					invokationProxies.add(invokationProxy);
 					po.invokationProxies.put(invokationBase, invokationProxies);
 
 				}
 
 				// building the plugins object for imports. (combining type and
 				// function information together)
 
 				po.imports.addAll(myMethodImports);
 				po.imports.addAll(myTypeImports);
 
 				// merging functions
 				for (String myMethodExport : myMethodExports) {
 					myMethodExport = myMethodExport.trim();
 					if (1 <= myMethodExport.length()) {
 						ImpExp impexp = new ImpExp();
 
 						if (functions.containsKey(myMethodExport))
 							impexp = functions.get(myMethodExport);
 
 						boolean flag_isExported = true;
 
 						if (considerBundleExportersOnly) {
 							flag_isExported = false;
 
 							// String funcClassName = s.split(" ")[1].trim();
 							String[] funcNameElements = ParsingUtil.separateFuncNameElements(myMethodExport);
 
 							String funcClassName = funcNameElements[1];
 
 							for (String bundleExport : bundleExports) {
 								bundleExport = bundleExport.trim();
 								if (1 <= bundleExport.length()) {
 
 									bundleExport = ParsingUtil.getBundlePropertyNameFromBundleEntry(bundleExport);
 									if (1 < bundleExport.length()) {
 										// Log.outln("******** Checking if "+funcWithoutReturnType+" starts with "+a.trim()
 										// +
 										// ".\n  |  original invokation:"+s+"    original bundleExportEntry:"+b);
 										if (funcClassName.startsWith(bundleExport.trim() + ".")) {
 											// Log.outln("************* YES DOES.");
 											flag_isExported = true;
 											break;
 										}
 									}
 								}
 							}
 						}
 						if (flag_isExported) {
 							// Log.outln("*********** adding to exports:"+thisPluginExtractName+
 							// "    for invokation: "+ myMethodExport);
 							impexp.addToExp(thisPluginExtractName);
 							functions.put(myMethodExport, impexp);
 
 							// building the plugins object for exports.
 							po.exports.add(myMethodExport.trim());
 
 						}
 					}
 				}
 
 				for (String s : myMethodImports) {
 					s = s.trim();
 					if (1 <= s.length()) {
 
 						ImpExp impexp = new ImpExp();
 						if (functions.containsKey(s))
 							impexp = functions.get(s);
 
 						impexp.addToImp(thisPluginExtractName);
 						functions.put(s, impexp);
 
 					}
 				}
 
 				// merging types (classes)
 				for (String myTypeExport : myTypeExports) {
 					myTypeExport = myTypeExport.trim();
 					if (1 <= myTypeExport.length()) {
 						ImpExp impexp = new ImpExp();
 						if (types.containsKey(myTypeExport))
 							impexp = types.get(myTypeExport);
 						boolean flag_isExported = true;
 
 						if (considerBundleExportersOnly) {
 							flag_isExported = false;
 
 							for (String bundleExport : bundleExports) {
 								bundleExport = bundleExport.trim();
 								if (1 <= bundleExport.length()) {
 
 									bundleExport = ParsingUtil.getBundlePropertyNameFromBundleEntry(bundleExport);
 									if (1 < bundleExport.length()) {
 
 										if (myTypeExport.startsWith(bundleExport.trim() + ".")) {
 											// Log.outln("************* YES DOES.");
 											flag_isExported = true;
 											break;
 										}
 									}
 								}
 							}
 						}
 						if (flag_isExported) {
 							// Log.outln("*********** adding to exports:"+thisPluginExtractName+
 							// "    for type: "+myTypeExport);
 							impexp.addToExp(thisPluginExtractName);
 							types.put(myTypeExport, impexp);
 
 							// building the plugins object for exports.
 							po.exports.add(myTypeExport.trim());
 						}
 					}
 				}
 
 				for (String s : myTypeImports) {
 					s = s.trim();
 					if (1 <= s.length()) {
 						ImpExp impexp = new ImpExp();
 						if (types.containsKey(s))
 							impexp = types.get(s);
 						impexp.addToImp(thisPluginExtractName);
 						types.put(s, impexp);
 					}
 				}
 				// System.out.println(types.keySet().toString());
 
 				if (pluginExtractsDone % 250 == 0 || pluginExtractsDone == entriesLength) {
 					Log.outln("#### PluginExtractsMerged = " + pluginExtractsDone + " of " + entriesLength + " ("
 							+ (pluginExtractsDone * 100 / entriesLength) + "%)");
 
 					//
 					// int functionsMemSize = functions.toString().length() /
 					// (1024 * 1024); int typesMemSize =
 					// types.toString().length() / (1024 * 1024); int
 					// pluginsMemSize = (plugins.toString().length() +
 					// po.toString().length()) / (1024 * 1024);
 					//
 					// Log.outln("## functions \tobjectSize\t= " +
 					// functionsMemSize + "MB");
 					// Log.outln("## types \t\tobjectSize\t= " + typesMemSize +
 					// "MB"); Log.outln("## plugins \tobjectSize\t= " +
 					// pluginsMemSize + "MB");
 					// Log.outln("## TOTAL 3 \tobjectSize\t= " +
 					// (functionsMemSize + typesMemSize + pluginsMemSize) +
 					// "MB");
 					//
 					Log.outln("## time (merging) so far \t= " + Util.getFormattedTime(System.currentTimeMillis() - time1));
 				}
 
 				// adding the constructed object to the DependencyFinder.plugins
 				// object.
 				plugins.put(thisPluginExtractName, po);
 
 				// ///////////////// done with adding the current plugin
 				// information to the DependencyFinder.plugins object /////////
 			}
 		}
 		Log.outln(" ///////// now doing the circular inter plugin dependency analysis    /////////    ");
 
 		Set<Entry<String, PluginObject>> pluginEntrySet = plugins.entrySet();
 
 		long totalnumberofplugins = pluginEntrySet.size(), counter = 0, indirectDepAnalysisTime1 = System
 				.currentTimeMillis();
 
 		for (Entry<String, PluginObject> entry : pluginEntrySet) {
 
 			counter++;
 			PluginObject pluginObj = entry.getValue();
 			Log.outln("== plugin  " + counter + " : " + pluginObj.name);
 			for (String imp : pluginObj.imports) {
 				Set<Set<String>> exporterPluginSets = new LinkedHashSet<Set<String>>();
 				// if the alsoConsiderInvokationSatisfactionProxies flag
 				// parameter is true, then build a set of all invokations to be
 				// considered: all its proxy invokations if they exists else
 				// check the original invokation.
 				if (alsoConsiderInvokationSatisfactionProxies) {
 					Set<String> invokationProxies = pluginObj.invokationProxies.get(imp);
 					if (null != invokationProxies && 1 <= invokationProxies.size()) {
 						// check only the proxies, as we know that if it has not
 						// been satisfied till now at the plugin level ( i.e. a
 						// proxy exists) then if it has to be satisfied , it
 						// will be satisfied through the proxy only.
 						exporterPluginSets = findExporters(invokationProxies, pluginObj.name);
 					} else {
 						exporterPluginSets = findExporters(imp);
 					}
 				} else {
 
 					exporterPluginSets = findExporters(imp);
 				}
 
 				// now if there were any indirect / transitive exporters, check
 				// for that and mark the import as satisfied.
 
 				// recording the data in the functions or types objects.
 				if (functions.containsKey(imp) && 1 <= exporterPluginSets.size()) {
 					ImpExp impexp = functions.get(imp);
 					impexp.satisfyingPluginsSets.addAll(exporterPluginSets);
 					impexp.addToExp("==SatisfyingPluginSet: " + exporterPluginSets.toString());
 					functions.put(imp, impexp);
 				} else if (types.containsKey(imp) && 1 <= exporterPluginSets.size()) {
 					ImpExp impexp = types.get(imp);
 					impexp.satisfyingPluginsSets.addAll(exporterPluginSets);
 					impexp.addToExp(exporterPluginSets.toString());
 					types.put(imp, impexp);
 				}
 			}
 			if (counter % 250 == 0 || counter == totalnumberofplugins) {
 
 				Log.outln("#### indirect dep analysis done for " + (counter) + " of " + totalnumberofplugins + " ("
 						+ (counter * 100 / totalnumberofplugins) + "%)");
 				Log.outln("## time (indirect dep analysis) so far \t= "
 						+ Util.getFormattedTime(System.currentTimeMillis() - indirectDepAnalysisTime1));
 			}
 		}
 
 		// write out the merged file
 		writeData(pathToPluginDependencyAnalysisOutputLocation);
 
 		long time2 = System.currentTimeMillis();
 		Log.outln("Dependency Set Creation for " + entriesLength + " plugin extracts, at plugin extract src  :  "
 				+ pathToBasePluginExtractsDir + "  time: " + Util.getFormattedTime(time2 - time1));
 		Log.errln("Dependency Set Creation for " + entriesLength + " plugin extracts, at plugin extract src  :  "
 				+ pathToBasePluginExtractsDir + "  time: " + Util.getFormattedTime(time2 - time1));
 
 	}
 
 	/**
 	 * helper to store already traversed imports, helping DependencyFinder.
 	 * findExporters
 	 */
 	private static Map<String, Set<Set<String>>> exporterSetsCache = new HashMap<String, Set<Set<String>>>();
 
 	private static Set<Set<String>> findExporters(Set<String> importEntryProxies, String ownerPluginName) {
 
 		Set<Set<String>> result = new LinkedHashSet<Set<String>>();
 
 		// otherwise, try the proxies but then this means that we would need to
 		// include the plugin that owns the import entry in the result as the
 		// proxy is generated to go outside of the owner plugin from inside of
 		// the owner plugin and the dependency cannot be satisfied without the
 		// owner plugin in the mix.
 
 		for (String importEntryProxy : importEntryProxies) {
 			Set<Set<String>> interimResult = new LinkedHashSet<Set<String>>();
 
 			interimResult = findExporters(importEntryProxy);
 			if (interimResult.size() > 0)
 				for (Set<String> set : interimResult) {
 					if (set.size() > 0)
 						set.add(ownerPluginName);
 				}
 			result.addAll(interimResult);
 
 		}
 		return result;
 	}
 
 	/**
 	 * recursively finds the exporter sets of the given imports.
 	 * 
 	 * @param imp
 	 * @return
 	 */
 	private static Set<Set<String>> findExporters(String imp) {
 		// if(exporterSetsCache.containsKey(imp))
 		// return exporterSetsCache.get(imp);
 
 		Set<Set<String>> result = new LinkedHashSet<Set<String>>();
 		imp = imp.trim();
 		int impType = ParsingUtil.getEntryType(imp);
 		if (0 != impType) {
 			String classname = imp;
 			if (1 == impType) {
 				// it is a function, so get the class name.
 				String[] funcElements = ParsingUtil.separateFuncNameElements(imp);
 				classname = funcElements[1];// class name.
 
 			}
 			Set<PluginObject> targetPlugins = new HashSet<PluginObject>();
 			targetPlugins = getTargetPlugins(classname);
 			for (PluginObject targetPlugin : targetPlugins) {
 				if (null != targetPlugin) {
 					if (null != targetPlugin.exports && targetPlugin.exports.contains(imp)) {
 
 						Set<String> newSet = new LinkedHashSet<String>();
 						newSet.add(targetPlugin.name);
 						result.add(newSet);
 						// System.out.println("result: "+result);
 
 					}
 
 					else {
 						if (null != targetPlugin.superClassesAndInterfaces) {
 							Set<String> superclasses = targetPlugin.superClassesAndInterfaces.get(classname);
 							for (String superclass : superclasses) {
 
 								String newImp = imp.replace(classname, superclass);
 								// if imp is a functioninvokation, then replace
 								// just the class name with superclass name
 								boolean impTypeIsFunction = (1 == ParsingUtil.getEntryType(imp)) ? true : false;
 
 								if (impTypeIsFunction) {
 									// it is a function, so get the class name.
 									String[] funcElements = ParsingUtil.separateFuncNameElements(imp);
 									// replace the class name.
 									funcElements[1] = superclass;
 									// recnstruct
 									newImp = ParsingUtil.reconstructFuncSignature(funcElements);
 
 								}
 
 								Set<Set<String>> setOfSets = findExporters(newImp);
 								if (setOfSets.size() > 0)
 									for (Set<String> set : setOfSets) {
 										if (set.size() > 0)
 											set.add(targetPlugin.name);
 
 									}
 								result.addAll(setOfSets);
 
 								// System.out.println("result_branch: "+result);
 							}
 						}
 					}
 				}
 			}
 		}
 		return result;
 
 	}
 
 	/**
 	 * gets a set of PluginsObject for the plugins that export the said
 	 * classname.
 	 * 
 	 * @param classname
 	 * @return
 	 */
 	private static Set<PluginObject> getTargetPlugins(String classname) {
 		Set<PluginObject> result = new HashSet<PluginObject>();
 
 		// getting the list of plugin names that export this type.
 		if (types.containsKey(classname)) {
 			Set allPluginExporters = types.get(classname).getExp();
 			for (Object pluginName : allPluginExporters) {
 				// getting the PluginObjects for all the plugin names obtained.
 				PluginObject pobj = plugins.get((String) pluginName);
 				result.add(pobj);
 			}
 
 		}
 		return result;
 	}
 
 	private static void writeData(String pluginDependencySetOutputLocationPath) throws IOException {
 
 		Set<String> unmatchedFunctionImports = new LinkedHashSet<String>();
 		Set<String> unmatchedTypeImports = new LinkedHashSet<String>();
 
 		if (!Util.checkAndCreateDirectory(pluginDependencySetOutputLocationPath)) {
 			Log.errln("xxxx\n in writeData() in   DependencyFinder, the output location: "
 					+ pluginDependencySetOutputLocationPath + "  \n is not accessible, cannot record data.  \nxxxx");
 			return;
 		}
 
 		File functionFile = new File(pluginDependencySetOutputLocationPath + "/"
 				+ Constants.DEPENDENCY_SET_FILE_PREFIX_PLUGIN + "functions"
 				+ Constants.DEPENDENCY_SET_FILE_EXTENSION_PLUGIN);
 		File typeFile = new File(pluginDependencySetOutputLocationPath + "/" + Constants.DEPENDENCY_SET_FILE_PREFIX_PLUGIN
 				+ "types" + Constants.DEPENDENCY_SET_FILE_EXTENSION_PLUGIN);
 
 		// writing functions set.
 
 		FileWriter filewriter = new FileWriter(functionFile);
 		BufferedWriter writer = new BufferedWriter(filewriter);
 
 		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_FUNCTIONS + "\n");
 		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_FUNCTIONS + "\n");
 
 		long counter = 0;
 		long functionsLength = functions.keySet().size();
 		for (String key : functions.keySet()) {
 
 			writer.write(Constants.DELIM_PLUGIN_DEPENDENCY_ELEMENT_SET + "\n");
 
 			// writing the function signature.
 			writer.write(key + "\n");
 
 			ImpExp impexp = functions.get(key);
 
 			Set imp = impexp.getImp();
 			Set exp = impexp.getExp();
 
 			// all importers
 			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS + "\n");
 			for (Object s : imp) {
 				writer.write((String) s + "\n");
 			}
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 			// all exporters
 			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS + "\n");
 			for (Object s : exp) {
 				writer.write((String) s + "\n");
 			}
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 			// all importers not satisfied
 			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS_UNSATISFIED + "\n");
 			writer.write((0 == exp.size() ? true + ", " + imp.size() + " importer(s) hungry " : false + ", " + exp.size()
 					+ (1 < exp.size() ? " exporters!! PLURAL??" : " exporter") + " available ")
 					+ "\n");
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 			// add to the unmatchedFunctionImports Set.
 			if (0 == exp.size())
 				unmatchedFunctionImports.add(key);
 
 			// all exporters whose exports were not needed by anyone
 			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS_UNSATISFIED + "\n");
 			writer.write((0 == imp.size() ? true + ", " + exp.size()
 					+ (1 < exp.size() ? " exporters!! PLURAL??" : " exporter") + " eagerly available " : false + ", "
 					+ imp.size() + " importer(s) hungry ")
 					+ "\n");
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 			writer.write(Constants.PLUGIN_DEPENDENCY_SATISFYING_PLUGINS_SETS + "\n");
 			for (Set<String> s : impexp.satisfyingPluginsSets) {
 				writer.write(s + "\n");
 			}
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 			// terminating the key ( function name )
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 			counter++;
 			if (counter % 200000 == 0 || counter == functionsLength)
 				Log.outln("## functions written: " + counter + " of " + functionsLength + " ("
 						+ (counter * 100 / functionsLength) + "%)");
 		}
 		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_FUNCTION_IMPORTS + "\n");
 		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_FUNCTION_IMPORTS + "\n");
 		for (String s : unmatchedFunctionImports)
 			writer.write(s + "\n");
 		writer.write("COUNT=" + unmatchedFunctionImports.size() + "\n");
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS + "\n");
 		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS + "\n");
 
 		for (String s : pluginExtractsIgnored)
 			writer.write(s + "\n");
 		writer.write("COUNT=" + pluginExtractsIgnored.size() + "\n");
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		// terminating the functions set
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		writer.close();
 		filewriter.close();
 
 		// writing types set.
 
 		filewriter = new FileWriter(typeFile);
 		writer = new BufferedWriter(filewriter);
 
 		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_TYPES + "\n");
 
 		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_TYPES + "\n");
 
 		counter = 0;
 		long typesLength = types.keySet().size();
 
 		for (String key : types.keySet()) {
 
 			writer.write(Constants.DELIM_PLUGIN_DEPENDENCY_ELEMENT_SET + "\n");
 
 			// writing the type signature.
 			writer.write(key + "\n");
 
 			ImpExp impexp = types.get(key);
 
 			Set imp = impexp.getImp();
 			Set exp = impexp.getExp();
 
 			// all importers
 			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS + "\n");
 			for (Object s : imp) {
 				writer.write((String) s + "\n");
 			}
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 			// all exporters
 			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS + "\n");
 			for (Object s : exp) {
 				writer.write((String) s + "\n");
 			}
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 			// all importers not satisfied
 			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS_UNSATISFIED + "\n");
 			writer.write((0 == exp.size() ? true + ", " + imp.size() + " importer(s) hungry " : false + ", " + exp.size()
 					+ (1 < exp.size() ? " exporters!! PLURAL??" : " exporter") + " available ")
 					+ "\n");
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 			// add to the unmatchedTypeImports Set.
 			if (0 == exp.size())
 				unmatchedTypeImports.add(key);
 
 			// all exporters whose exports were not needed by anyone.
 			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS_UNSATISFIED + "\n");
 			writer.write((0 == imp.size() ? true + ", " + exp.size()
 					+ (1 < exp.size() ? " exporters!! PLURAL??" : " exporter") + " eagerly available " : false + ", "
 					+ imp.size() + " importer(s) hungry ")
 					+ "\n");
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 			writer.write(Constants.PLUGIN_DEPENDENCY_SATISFYING_PLUGINS_SETS + "\n");
 			for (Set<String> s : impexp.satisfyingPluginsSets) {
 				writer.write(s + "\n");
 			}
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 			// terminating the key ( type /class name )
 			writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 			counter++;
 			if (counter % 75000 == 0 || counter == typesLength)
 				Log.outln("## types written: " + counter + " of " + typesLength + " (" + (counter * 100 / typesLength)
 						+ "%)");
 
 		}
 
 		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_TYPE_IMPORTS + "\n");
 		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_TYPE_IMPORTS + "\n");
 
 		for (String s : unmatchedTypeImports)
 			writer.write(s + "\n");
 		writer.write("COUNT=" + unmatchedTypeImports.size() + "\n");
 
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS + "\n");
 		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS + "\n");
 
 		for (String s : pluginExtractsIgnored)
 			writer.write(s + "\n");
 		writer.write("COUNT=" + pluginExtractsIgnored.size() + "\n");
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		// terminating the types set
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		writer.close();
 		filewriter.close();
 
 	}
 
 }
