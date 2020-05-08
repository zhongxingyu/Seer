 /*******************************************************************************
  * Copyright (c) 2011 Ericsson and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ericsson - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.common_prefs.core;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 
 import org.eclipse.common_prefs.PreferenceInitializer;
 import org.eclipse.common_prefs.StartupPlugin;
 import org.eclipse.core.internal.preferences.PreferencesService;
 import org.eclipse.core.net.proxy.IProxyData;
 import org.eclipse.core.net.proxy.IProxyService;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.preferences.ConfigurationScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IExportedPreferences;
 import org.eclipse.core.runtime.preferences.IPreferenceFilter;
 import org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor;
 import org.eclipse.core.runtime.preferences.IPreferencesService;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.osgi.service.prefs.BackingStoreException;
 import org.osgi.service.prefs.Preferences;
 
 
 /**
  * Helper class with common methods for preference read etc.
  * Added code for fixing network issue in Eclipse 3.5
  * 
  * @author Domenic Alessi
  *
  */
 public class CommonPrefsHelper {
 	
 	protected static final String VERSION_KEY = "eclipse.preferences.version"; //$NON-NLS-1$
 	
 	  protected static final String VERSION_VALUE = "1"; //$NON-NLS-1$
 	  
 	  
 
 	  	/**
 	 * Read the preference files into the workspace. Report any errors as a
 	 * multi status object, allowing one file to fail but others to succeed.
 	 * 
 	 * @param prefFiles
 	 * @return MultiStatus
 	 */
 	public static MultiStatus loadPreferences() {
 		
 		// The ErrorLog will show the MultiStatus entry as one top level object
 		// with children for each sub-entry. The top level error state will be 
 		// as the most sever of the child states. 
 		MultiStatus status = new MultiStatus(
 				StartupPlugin.PLUGIN_ID, 0,
 				"Log entries recorded when reading preference files", null);
 		
 		IPreferenceStore store = StartupPlugin.getDefault().getPreferenceStore();
 		boolean wsIsInitialized = false;
 		if (store.contains(PreferenceInitializer.PREF_WS_INITIALIZED))
 			wsIsInitialized = store.getBoolean(PreferenceInitializer.PREF_WS_INITIALIZED);	
 
 		IPreferenceFilter[] transfers = getPreferenceFilters();
 		
 		CommonPrefEPFResources prefFiles = StartupPlugin.getDefault().getCommonPrefFiles(status);
 		for (Iterator<CommonPrefEPFResource> iter = prefFiles.iterator(); iter.hasNext();) {
 			CommonPrefEPFResource prefFile = (CommonPrefEPFResource) iter.next();
 			
 			// If ws is initialized, reinit only if pref file is changed
 			long timeStampCurr = prefFile.getLastModified();
 			if (!prefFile.isForce()) {
 				if (wsIsInitialized) {
 					long timeStampOld = store.getLong(prefFile.getResourceName()); // 0 or value
 					if (timeStampCurr <= timeStampOld)
 						continue;
 					else {
 						status.add(new Status(
 								Status.INFO, StartupPlugin.PLUGIN_ID,
 								"Preference resource " + prefFile.getResourceName() + 
 								" is either new or has been updated. Will be read."));								
 					}
 				}
 			}
 			
 			/* NOTE: When using the importPreferences method below, there was an issue when
 			 * loading a preference file; the search window in the preference dialog was gone.
 			 * This didn't occur when using the import wizard. Hence changed to use same method
 			 * as this, see the org.eclipse.ui.internal.wizards.preferences package and
 			 * class::method WizardPreferencesImportPage1::transfer method for code ...
 			 */
 			// IPath path = new Path(pathStr);
 			// Preferences.importPreferences(path);
 			
 			status.add(loadPreferenceFile(prefFile, transfers));
 			
 			// If init file, set timestamp
 			if (!prefFile.isForce())
 				store.setValue(prefFile.getResourceName(), timeStampCurr);
 		}
 		
 		// Mark workspace as initialized
 		store.setValue(PreferenceInitializer.PREF_WS_INITIALIZED, true);
 		
 		return status;
 	}
 	
 	/**
 	 * Read the preferences from selected file and apply. Code inspired by implementation in
 	 * the org.eclipse.ui.internal.wizards.preferences.WizardPreferencesImportPage1#transfer method.
 	 * 
 	 * @param importFile
 	 * @param filters
 	 * @return IStatus
 	 */
 	public static IStatus loadPreferenceFile(CommonPrefEPFResource prefFile, IPreferenceFilter[] filters) {
 		if (!prefFile.exists)
 			return new Status(
         			IStatus.WARNING, StartupPlugin.PLUGIN_ID,
         			"Resource not found " + prefFile.getResourceName());
 		
 		IPreferenceFilter[] filtersLoc = filters;
 		if (filtersLoc == null)
 			filtersLoc = getPreferenceFilters();
 			
 		InputStream fis = null;
 		
 		// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
 		File file = null;
 		InputStream inputStream = null;
 		OutputStream outputStream = null;
 		// END
 		
 		try {
             try {
             	// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
             	file = prefFile.getFile();
             	if (file == null) {
             		inputStream = prefFile.getInputStream();
             	}
             	// END
             	
             	// Comment this code for testing with eclipse 3.5
                 //fis = prefFile.getInputStream();
                 // End
                 
             } catch (IOException e) {
             	return new Status(
             			IStatus.ERROR, StartupPlugin.PLUGIN_ID,
             			"Resource not found " + prefFile.getResourceName(), e);
             }
                                                                                                                                                                                   
             IPreferencesService service = Platform.getPreferencesService();
             try {
             	
             	// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
             	//System.out.println("RESOURCE NAME: " + prefFile.getResourceName() + "\n");
             	/*IExportedPreferences node = service.readPreferences(fis);
             	//File file = new File("C:\\Ericsson\\E4E_SVN\\workspace_commonpreftest\\org.eclipse.common_prefs\\test\\common_pref_out.epf");
             	File file = prefFile.getOutputFile();
             	if (!file.exists()) {
             		outputStream = new FileOutputStream(file);
             		service.exportPreferences(node, filters, outputStream);
             		if (outputStream != null)
             			outputStream.close();
             	}*/
             	if (file != null)
             		inputStream = new FileInputStream(file);
             	
             	IExportedPreferences prefs = service.readPreferences(inputStream);
         		if (inputStream != null)
         			inputStream.close();
         		service.applyPreferences(prefs, filtersLoc);
         		// END
         		
             	//IExportedPreferences prefs = service.readPreferences(fis);
         		
             	// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
             	//IEclipsePreferences[] eclipsePrefs = retrievePreferences(prefs);
             	// END
             	
                 //service.applyPreferences(prefs, filtersLoc);
             	
                 // ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
                 //retrieveNode(eclipsePrefs);
             	//updateCompareTree(eclipsePrefs);
                 // END
                                 
                 // persistConfigurationNode();
             } catch (CoreException e) {
             	return new Status(
             			IStatus.ERROR, StartupPlugin.PLUGIN_ID,
             			"Failed Loading preferences from resource " + prefFile.getResourceName(), e);
             }  catch (IOException e) {
             	
             }
             
 	    } finally {
 	        if (fis != null) {
 				try {
 	                fis.close();
 	            } catch (IOException e) {
 	            }
 			}
 	    }
 	    
 	    return new Status(IStatus.INFO, StartupPlugin.PLUGIN_ID,
 				"Preferences from resource " + prefFile.getResourceName() + " read successfully.");
 	}
 		
 	
 	
 	/**
 	 * ADD THIS METHOD FOR FIXING NETWORK ISSUE IN ECLIPSE 3.5
 	 * 
 	 * @return
 	 */
 	public String[] getProxyType() {
 		IProxyService proxy = StartupPlugin.getDefault().getProxyService();
 		IProxyData[] proxyData = proxy.getProxyData();
 		String[] proxyType = new String[proxyData.length];
 		
 		for (int i = 0; i < proxyData.length; i++)
 			proxyType[i] = proxyData[i].getType();
 		
 		return proxyType;
 	}
 	// END
 	
 	
 	
 	/**
 	 * ADD THIS METHOD FOR FIXING NETWORK ISSUE IN ECLIPSE 3.5
 	 * 
 	 * @return
 	 */
 	public String[] getProxyHost() {
 		IProxyService proxy = StartupPlugin.getDefault().getProxyService();
 		IProxyData[] proxyData = proxy.getProxyData();
 		String[] proxyHost = new String[proxyData.length];
 		
 		for (int i = 0; i < proxyData.length; i++)
 			proxyHost[i] = proxyData[i].getHost();
 		
 		return proxyHost;
 	}
 	// END
 	
 	
 	
 	/**
 	 * ADD THIS METHOD FOR FIXING NETWORK ISSUE IN ECLIPSE 3.5
 	 * 
 	 * @return
 	 */
 	public int[] getProxyPort() {
 		IProxyService proxy = StartupPlugin.getDefault().getProxyService();
 		IProxyData[] proxyData = proxy.getProxyData();
 		int[] proxyPort = new int[proxyData.length];
 		
 		for (int i = 0; i < proxyData.length; i++)
 			proxyPort[i] = proxyData[i].getPort();
 		
 		return proxyPort;
 	}
 	// END
 	
 	
 	
 	/**
 	 * ADD THIS METHOD FOR FIXING NETWORK ISSUE IN ECLIPSE 3.5
 	 * 
 	 * @return
 	 */
 	public boolean[] getAuthenticate() {
 		IProxyService proxy = StartupPlugin.getDefault().getProxyService();
 		IProxyData[] proxyData = proxy.getProxyData();
 		boolean[] proxyAuthentication = new boolean[proxyData.length];
 		
 		for (int i = 0; i < proxyData.length; i++)
 			proxyAuthentication[i] = proxyData[i].isRequiresAuthentication();
 		
 		return proxyAuthentication;
 	}
 	// END
 	
 	
 	
 	/**
 	 * ADD THIS METHOD FOR FIXING NETWORK ISSUE IN ECLIPSE 3.5
 	 * 
 	 * @return
 	 */
 	public String getNonProxiesHost() {
 		IProxyService proxy = StartupPlugin.getDefault().getProxyService();
 		String[] nonProxiesArray = proxy.getNonProxiedHosts();
 		StringBuilder buffer = new StringBuilder();
 				
 		for (int i = 0; i < nonProxiesArray.length; i++) {
 			if (i == nonProxiesArray.length-1)
 				buffer.append(nonProxiesArray[i]);
 			else
 				buffer.append(nonProxiesArray[i]).append("|");
 		}
 		
 		return buffer.toString();
 	}
 	// END
 	
 	
 	/**
 	 * ADD THIS METHOD FOR FIXING NETWORK ISSUE IN ECLIPSE 3.5
 	 * 
 	 * 
 	 */
 	 public boolean getProxiesEnable() {
 		
 		 IProxyService proxy = StartupPlugin.getDefault().getProxyService();
 		 
 		 return proxy.isProxiesEnabled();
 	 }
 	 // END
 	
 	
 		
 	/* Note: When reading the workspace start behavior from preferences, settings
 	 * are not stored since this is handled before we get here. One option might be
 	 * after reading the node, save it's data so it's read on next start. This will
 	 * then overwrite any change a user did when selecting workspace etc on startup
 	 * so NOT elegant. Commenting out code for now - but keeping. Solution for now
 	 * is instead to filter the node from appearing in Common Preferences Export wizard
 	 * so user will not be mislead to try to use it.
 	 * 
 	 * For details, see org.eclipse.ui.internal.ide.ChooseWorkspaceData class where
 	 * logic for persisting/reading and selecting workspace is handled.
 	 */
 /*	
 	private static void persistConfigurationNode() {
         org.osgi.service.prefs.Preferences node =
         	new ConfigurationScope().getNode("org.eclipse.ui.ide");
         if (node != null) {
 			try {
 				node.flush();
 			} catch (BackingStoreException e) {
 				e.printStackTrace();
 			}
         }		
 	}
 */
 	
 	private static IPreferenceFilter[] getPreferenceFilters() {
 		
 		IPreferenceFilter[] transfers = new IPreferenceFilter[1];
 		transfers[0] = new IPreferenceFilter() {
 				public String[] getScopes() {
 					return new String[] {InstanceScope.SCOPE,
 							ConfigurationScope.SCOPE };
 				}
 				@SuppressWarnings("unchecked")
 				public Map getMapping(String scope) {
 					return null;
 				}
 		};	
 		return transfers;
 	}
 	
 	/**
 	 * Save all preferences to a location in the workspace. This is used to
 	 * get a "snapshot" of the default state before making any changes.
 	 */
 	public static void saveDefaultPreferences() {
 		
 		IPath path = StartupPlugin.getDefaultPrefFile();
 				
 		// ADD THIS CODE FOR TESTING
 		IPreferencesService service = PreferencesService.getDefault();
 		File file = path.toFile();
 		OutputStream output = null;
 		FileOutputStream fos = null;
 		try {
 			fos = new FileOutputStream(file);
 			//output = new BufferedOutputStream(fos);
 			IEclipsePreferences node = (IEclipsePreferences) service.getRootNode().node(InstanceScope.SCOPE);
 			service.exportPreferences(node, fos, (String[]) null);
 			fos.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (CoreException e) {
 			e.printStackTrace();
 		} finally {
 			if (output != null)
 				try {
 					fos.close();
 				} catch (IOException e) {
 					// ignore
 				}
 		}
 		
 		// END
 		
 		/*try {
 			Preferences.exportPreferences(path);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}	*/	
 	}	
 	
 	/**
 	 * Read the ini file with the entries for the user defined preferences into a
 	 * Preferences instance. See {@link CommonPrefEPFResources} for details regarding the
 	 * format of the file. Note: OK that this file doesn't exist, then return null.
 	 * 
 	 * @return Preferences
 	 */
 	//public static Preferences readUserPreferencesEntries(MultiStatus status) {
 	public static Properties readUserPreferencesEntries(MultiStatus status) {
 		
 		File f = new File(StartupPlugin.getDefaultUserPrefIniFile());
 		if (!f.exists())
 			return null;
 		
 		// Added this code for testing
 		/*System.out.println("CommonPrefsHelper invoked readUserPreferencesEntries - File exists.\n");
 		try {
 			FileReader fileReader = new FileReader(f);
 			BufferedReader bufferReader = new BufferedReader(fileReader);
 			String line = null;
 			while ((line = bufferReader.readLine()) != null) {
 				System.err.println(line);
 			}
 			bufferReader.close();
 			fileReader.close();
 		}
 		catch(FileNotFoundException e) {
 			
 		}
 		catch(IOException e) { 
 			
 		}*/
 		//End
 		
 		//Preferences userPrefsEntries = new Preferences();
 		
 		// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
 		Properties properties = new Properties();
 		// END
 		
 		CommonPrefResource prefFile;
 		try {
 			prefFile = new CommonPrefResource(
 					StartupPlugin.getDefaultUserPrefIniFile(), true, false);
 		} catch (Exception e) {
 			status.add(new Status(Status.WARNING, StartupPlugin.PLUGIN_ID,
 					"Failed creating reference to " + StartupPlugin.getDefaultUserPrefIniFile(), e));		
 			return null;
 		}
 		//mergePreferences(prefFile, userPrefsEntries, status);
 		
 		// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
 		mergePreferences(prefFile, properties, status);
 		
 		//return userPrefsEntries;
 		
 		// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
 		return properties;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Save the ini file with the entries for the user defined preferences. See 
 	 * {@link CommonPrefEPFResources} for details regarding the format of the file.
 	 * 
 	 * @param userPrefs
 	 * @return boolean
 	 */
 	/*public static boolean saveUserPreferencesEntries(Preferences userPrefs) {*/
 	public static boolean saveUserPreferencesEntries(Properties userPrefsProperties) {
 		File f = new File(StartupPlugin.getDefaultUserPrefIniFile());
 		
 		// NOTE: The FileOutputStream will create an empty file on win32, but 
 		// not on linux/solaris. Hence need explicit creation of file
 		if (!f.exists()) {
 			
 			boolean created = true;
 			File dir = new File(f.getParent());
 			if (!dir.exists()) {
 				created = dir.mkdir();
 			}
 			if (created) {
 				try {
 					created = f.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 					created = false;
 				}
 			}
 			// TODO: Report failed if created == false ...
 		}
 			
 		OutputStream out = null;
 		try {
 			out = new FileOutputStream(f);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 		try {
 			// Temporary comment this code for testing with eclipse 3.5
 			/*userPrefs.store(out, "User defined Common Preferences entries");*/
 			// End
 			
 			// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
 			userPrefsProperties.store(out, "User defined Common Preferences entries");
 			// END
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Read the preferences for the reference. If error, return null.
 	 * 
 	 * @param prefFile
 	 * @return
 	 */
 	public static IExportedPreferences readPreferences(CommonPrefEPFResource prefFile) {
 		if (!prefFile.exists)
 			return null;
 		
 		IPreferenceFilter[] preferenceFilter = getPreferenceFilters();
 		if (preferenceFilter == null)
 			preferenceFilter = getPreferenceFilters();
 		
 		InputStream fis = null;
 		IExportedPreferences prefs = null;
 		
 		// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
 		File file = null;
 		InputStream inputStream = null;
 		OutputStream outputStream = null;
 		// END
 		
 		try {
 			// Comment this code for testing with eclipse 3.5
 			/*fis = prefFile.getInputStream();
 			 IPreferencesService service = Platform.getPreferencesService();
 			 prefs = service.readPreferences(fis);*/
 			// End
 			
 			// ADD IExportedPreferences CODE HERE
 			IPreferencesService myService = Platform.getPreferencesService();
 			//IExportedPreferences node = myService.readPreferences(fis);
 			file = prefFile.getFile();
 			if (file == null) {
 				inputStream = prefFile.getInputStream();
 			}
 			if (file != null) {
 				inputStream = new FileInputStream(file);
 			}
 			/*File file = new File("C:\\Ericsson\\E4E_SVN\\workspace_commonpreftest\\org.eclipse.common_prefs\\test\\common_pref_out.epf");
 			OutputStream outputStream = new FileOutputStream(file);
     		service.exportPreferences(node, preferenceFilter, outputStream);
     		if (outputStream != null)
     			outputStream.close();
     		InputStream inputStream = new FileInputStream(file);*/
     		prefs = myService.readPreferences(inputStream);
     		if (inputStream != null)
     			inputStream.close();
     		//service.applyPreferences(prefs, preferenceFilter);
     		// END			
     		
     		// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
     		/*prefs = service.readPreferences(inputStream);
     		if (inputStream != null)
     			inputStream.close();
     		service.applyPreferences(prefs, preferenceFilter);*/
     		// END
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (CoreException e) {
 			e.printStackTrace();
 		} finally {
 	        if (fis != null) {
 				try {
 	                fis.close();
 	            } catch (IOException e) {
 	            }
 			}
 	    }
 				
 	    return prefs;
 	}
 
 	/**
 	 * Read the .ref files with the entries for the user defined preferences into a
 	 * Preferences instance. See {@link CommonPrefEPFResources} for details regarding the
 	 * format of the file. NOTE: Numbering needs to be unique, else entries will be
 	 * overridden.
 	 * 
 	 * @param status - OK to pass in null
 	 * @return Preferences - If none found, return null
 	 */
 	//public static Preferences readLocalPreferencesEntries(MultiStatus status) {
 	public static Properties readLocalPreferencesEntries(MultiStatus status) {
 		
 		File[] refFiles = StartupPlugin.getLocalReferenceFiles();
 		if (refFiles != null && refFiles.length > 0) {
 			
 			//Preferences localPreferenceEntries = new Preferences();
 			
 			// ADD THIS CODE FOR TESTING
 			Properties properties = new Properties();
 			
 			// END
 			
 			
 			for (File refFile : refFiles) {
 				
 				// Add this code for testing
 				/*try {
 					FileReader fileReader = new FileReader(refFile);
 					BufferedReader bufferReader = new BufferedReader(fileReader);
 					String line = null;
 					while ((line = bufferReader.readLine()) != null) {
 						System.out.println(line);
 					}
 					bufferReader.close();
 					fileReader.close();
 				}
 				catch(FileNotFoundException e) {
 					
 				}
 				catch(IOException e) { 
 					
 				}*/
 				//End
 				
 				CommonPrefResource prefFile = null;
 				try {
 					prefFile = new CommonPrefResource(refFile.getPath(), true, true);
 				} catch (Exception e) {
 					if (status != null) {
 						status.add(new Status(Status.WARNING, StartupPlugin.PLUGIN_ID,
 								"Failed creating reference to " + refFile.getPath(), e));
 					}
 					continue;
 				}				
 				//mergePreferences(prefFile, localPreferenceEntries, status);	
 				mergePreferences(prefFile, properties, status);
 			}
 			//return localPreferenceEntries;
 			return properties;
 		} else {
 			
 			return null;
 		}
 	}	
 	
 	/**
 	 * The method reads the common preferences files into a Preferences object,
 	 * but does not load them into the current workbench. To be used for compare
 	 * with settings in workspace.
 	 * 
 	 * @param readCommon
 	 * @param readDefault
 	 * @return Preferences
 	 */
 	/*public static Preferences readPreferences(boolean readCommon, boolean readDefault) {*/
 	public static Properties readPreferences(boolean readCommon, boolean readDefault) {
 		// Temporary comment this code for testing with eclipse 3.5
 		/*Preferences commonPrefs = new Preferences();*/
 		// End
 		
 		//	ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
 		Properties commonProperties = new Properties();
 		// END
 
 		// NOTE: Should read the default first, since they may be overridden by
 		// values defined in the common property files.
 		if (readDefault) {
 			
 			IPath path = StartupPlugin.getDefaultPrefFile();
 			
 			// Added this code for testing
 			/*System.out.println("PATH TO READ DEFAULT PREF FILE: " + path.toFile().getAbsolutePath() + "\n");
 			try {
 				FileReader fileReader = new FileReader(path.toFile());
 				BufferedReader bufferReader = new BufferedReader(fileReader);
 				String line = null;
 				while ((line = bufferReader.readLine()) != null) {
 					System.err.println(line);
 				}
 				bufferReader.close();
 				fileReader.close();
 			}
 			catch(FileNotFoundException e) {
 				
 			}
 			catch(IOException e) { 
 				
 			}*/
 			// End
 			
 			CommonPrefResource prefFile = null;
 			try {
 				prefFile = new CommonPrefResource(path.toOSString(), true, true);
 				
 				// Temporary comment this code for testing with eclipse 3.5
 				/*mergePreferences(prefFile, commonPrefs, null);*/
 				// End
 				
 				// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
 				mergePreferences(prefFile, commonProperties, null);
 				// END
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 			}				
 		}
 		
 		if (readCommon) {
 			
 			CommonPrefEPFResources prefFiles = StartupPlugin.getDefault().getCommonPrefFiles(null);
 			for (Iterator<CommonPrefEPFResource> iter = prefFiles.iterator(); iter.hasNext();) {
 				CommonPrefEPFResource prefFile = (CommonPrefEPFResource) iter.next();
 				// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
 				mergePreferences(prefFile, commonProperties, null);
 				// End
 				
 				// Temporary comment this code for testing with eclipse 3.5 
 				/*mergePreferences(prefFile, commonPrefs, null);*/
 				// End
 				
 			}
 		}
 
 		return commonProperties;
 		
 		// Temporary comment this code for testing with eclipse 3.5 
 		/*return commonPrefs;*/
 		// End
 	}		
 	
 	/**
 	 * Return the preferences ordered in key order. This to allow the pref tree
 	 * to be sorted. Crude, but no need for sorting after GUI is opened. Skip the 
 	 * "default" and "project" scopes.
 	 * 
 	 * @param prefs
 	 * @param fileNo
 	 * @param noOfFiles
 	 * @param nodeMap
 	 * @return
 	 */
 	public static boolean sortPreferences(
 			IEclipsePreferences prefs,
 			final int fileNo,
 			final int noOfFiles,
 			final TreeMap<String, IEclipsePreferences[]> nodeMap) {
 		
 		try {
 			prefs.accept(new IPreferenceNodeVisitor() {
 				    public boolean visit(IEclipsePreferences node) {
 				    	boolean includeNodeAndTree = includeNodeAndTree(node);
 				    	if (includeNode(node) && includeNodeAndTree) {			    	
 				    		String nodeName = node.name();
 			    			IEclipsePreferences[] values = nodeMap.get(nodeName);
 			    			if (values == null) {
 			    				values = new IEclipsePreferences[noOfFiles];
 			    				nodeMap.put(nodeName, values);								
 			    			}
 			    			values[fileNo] = node;
 				    	}
 				    	return includeNodeAndTree;
 				    }
 				 });
 		} catch (BackingStoreException e) {
 			e.printStackTrace();
 			return false;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;			
 		}
 		return true;
 	}
 	
 	/**
 	 * Return the preferences ordered in key order. This to allow the pref tree
 	 * to be sorted. Crude, but no need for sorting after GUI is opened. Skip the 
 	 * "default" and "project" scopes.
 	 * 
 	 * @return
 	 */
 	public static boolean sortPreferences(final TreeMap<String, IEclipsePreferences> nodeMap) {
 		
 		IPreferencesService prefsService = Platform.getPreferencesService();
 		IEclipsePreferences prefs = prefsService.getRootNode();
 
 		try {
 			prefs.accept(new IPreferenceNodeVisitor() {
 				    public boolean visit(IEclipsePreferences node) {
 				    	boolean includeNodeAndTree = includeNodeAndTree(node);
 				    	if (includeNode(node) && includeNodeAndTree)			    	
 				    		nodeMap.put(node.absolutePath(), node);
 				    	return includeNodeAndTree;
 				    }
 				 });
 		} catch (BackingStoreException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}		
 	
 	/**
 	 * Check if the node should be included.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	private static boolean includeNode(IEclipsePreferences node) {
 		if (node == null)
 			return false;
 		
 		String nodeName = node.absolutePath();
     	String[] nodeParts = nodeName.split("/", 5);
     	if (nodeParts.length < 2) 
     		return false;
     	if (nodeParts.length == 2)
     		if (nodeParts[1].compareTo("instance") == 0  || nodeParts[1].compareTo("") == 0)
     			return false;
     	
     	return true;
 	}
 
 	/**
 	 * Check if the node and it's subtree should be included.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	private static boolean includeNodeAndTree(IEclipsePreferences node) {
 		if (node == null)
 			return false;
 		
 		String nodeName = node.name();    	
     	
     	// Skip the "default" and "project" entries and sub entries
     	if (nodeName.compareTo("project") == 0)
     		return false;
     	if (nodeName.compareTo("default") == 0)
     		return false;
     	
     	// NOTE: Filter out the configuration node for setting workspace
     	// startup behavior. This since they are read before the Common Prefs
     	// code is activated so are not handled correctly. For more info,
     	// see "On workspace startup preferences" chapter in the "Sharing
     	// of Preferences.doc" in the doc folder.   	
     	if (nodeName.compareTo("org.eclipse.ui.ide") == 0) {
     		if (node.parent() != null &&
     				node.parent().name().compareTo("configuration") == 0)
     			return false;
     	}
    	
     	return true;
 	}
 	
 	/**
 	 * The preferences found in the file will be merged into the prefs
 	 * supplied to the method. Note that any new values to existing keys 
 	 * will be overwritten - expected and desired.
 	 * 
 	 * @param prefFile
 	 * @param prefs
 	 * @param status - OK to pass null
 	 */
 	private static void mergePreferences(
 			CommonPrefResource prefFile, Properties properties, MultiStatus status) {
 		if (prefFile == null && properties == null)
 			return;
 
 		if (!prefFile.exists)
 			return;
 		
 		InputStream input = null;
 		
 		// Added this code for testing
 		/*try {
 			input = prefFile.getInputStream();
 			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(input));
 	        String line = null;
 	        System.out.println("CommonPrefsHelper MERGE PREFERENCES: \n");
 	        while((line = bufferReader.readLine()) != null) {
 	        	System.err.println(line);
 	        }
 	        bufferReader.close();
 			
 		}
 		catch(IOException e) {
 			
 		}*/
 		// End
 		
 		try {
 			input = prefFile.getInputStream();
 			//prefs.load(input);
 			
 			// Add this code for testing
 			CommonPrefProjectPreference.read(input, properties);
 			// End
 			
 		} catch (IOException e) {
 			if (status != null) {
 				status.add(new Status(Status.WARNING, StartupPlugin.PLUGIN_ID,
 						"Failed loading preference file " + prefFile.getResourceName(), e));				
 			}
 		} catch (BackingStoreException e) {
 			
 		}
 		finally {
 			if (input != null) {
 				try {
 					input.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * ADD THIS METHOD FOR TESTING WITH ECLIPSE 3.5
 	 * 
 	 */
 	public static void read(InputStream inputStream, Properties properties) throws  BackingStoreException, IOException {
 		
 		//Properties fromDisk = loadProperties(inputStream);
 		loadProperties(inputStream, properties);
 		//return fromDisk;
 		        
 	}
 	
 	
 	/**
 	 * ADD THIS METHOD FOR TESTING WITH ECLIPSE 3.5
 	 * 
 	 */
 	private static void loadProperties(InputStream inputStream, Properties result) throws BackingStoreException, IOException {
         
  		//Properties result = new Properties();
          InputStream input = null;
          try {
         	 input = new BufferedInputStream(inputStream);
              result.load(input);
          } finally {
              //FileUtil.safeClose(input);
          }
          //return result;
      }
 
 	
 	
 	
 	/**
 	 * Add the node to the tree. If parent isn't present, add parent nodes
 	 * first recursively. Note: For the instance scope the top nodes (plugin)
 	 * are added directly as root nodes, for the configuration scope the nodes
 	 * are added under a "Configuration" node
 	 * 
 	 * @param prefNode The preference node to be added
 	 * @param imNodeMap A map with full node name as keys and tree nodes as values
 	 * @param imPrefTree The tree to add a mode to
 	 * @return The tree items that was added
 	 */
 	public static TreeItem addNodeInternal(
 			IEclipsePreferences prefNode,
 			Map<String, TreeItem> imNodeMap,
 			Tree imPrefTree) {
 		
 		String nodeName = prefNode.absolutePath();
 		TreeItem imNode = imNodeMap.get(nodeName);
 		if (imNode != null)
 			return imNode; // Already added
 		
 		ImageRegistry reg = StartupPlugin.getDefault().getImageRegistry();
 		
 		if (CommonPrefsHelper.isConfigurationNode(prefNode)) {
 			// Scope is "configuration" - add top root node to collect children
 			imNode = new TreeItem(imPrefTree, SWT.NONE);
 			imNode.setImage(reg.get(StartupPlugin.EXP_TREE_CONFIG_IMG));
 		} else {
 			IEclipsePreferences pPrefNode = (IEclipsePreferences) prefNode.parent();
 			if (CommonPrefsHelper.isInstanceNode(pPrefNode)) {
 				// Scope is "instance" - add top node
 				imNode = new TreeItem(imPrefTree, SWT.NONE);
 				imNode.setImage(reg.get(StartupPlugin.EXP_TREE_PLUGIN_IMG));			
 			} else {
 				// Scope is either subnode or "configuration" - get parent node
 				String pNodeName = pPrefNode.absolutePath();
 				TreeItem pNode = imNodeMap.get(pNodeName);
 				if (pNode == null)
 					pNode = addNodeInternal(pPrefNode, imNodeMap, imPrefTree);
 				imNode = new TreeItem(pNode, SWT.NONE);
 				
 				// If plug-in node in the "configuration" tree - add image
 				if (CommonPrefsHelper.isConfigurationNode(pPrefNode))
 					imNode.setImage(reg.get(StartupPlugin.EXP_TREE_PLUGIN_IMG));
 			}
 		}
 		
 		String [] nodeParts = nodeName.split("/");
 		imNode.setGrayed(nodeParts.length > 3); // Set grayed if not plug-in node
 		
 		// ADD THIS CODE FOR TESTING
 		if (nodeParts.length >= 2) {
 			Preferences preferences = Platform.getPreferencesService().getRootNode();
 			Preferences node = preferences.node(InstanceScope.SCOPE).node(nodeName);
 			addNetworkNode(nodeParts, node);
 		}
 		// END
 		
 		imNode.setText(prefNode.name());
 		imNode.setData(prefNode);
 		
 		imNodeMap.put(nodeName, imNode);	
 		
 		return imNode;
 	}	
 	
 	
 	// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
 	private static void addNetworkNode(String[] partsNode, Preferences nodePref) {
 		
 		String nodeName = null;
 		if (partsNode[partsNode.length - 1].equals(NetworkPrefResources.ORG_ECLIPSE_CORE_NET)) {
 			
 			NetworkPrefResources networkPrefResources = StartupPlugin.getDefault().getNetworkPrefResources();
 			Map networkNonProxiesPrefMap = networkPrefResources.getNetworkNonProxiesPreferencesMap();
 			NetworkNonProxiesResource nonProxiesResource = (NetworkNonProxiesResource) networkNonProxiesPrefMap.get(NetworkPrefResources.ORG_ECLIPSE_CORE_NET);
 			nodePref.put(NetworkPrefResources.NON_PROXIED_HOST, nonProxiesResource.getNonProxiesHost());
 			nodePref.put(NetworkPrefResources.PROXIES_ENABLE, String.valueOf(nonProxiesResource.getProxiesEnable()));
 		}
 		if (partsNode[partsNode.length - 1].equals(NetworkPrefResources.HTTP)) {
 			
 			NetworkPrefResources networkPrefResources = StartupPlugin.getDefault().getNetworkPrefResources();
 			Map networkPrefMap = networkPrefResources.getNetworkPreferencesMap();
 			NetworkPrefResource networkResource = (NetworkPrefResource) networkPrefMap.get(NetworkPrefResources.HTTP);
 			String host = networkResource.getHost();
			if (host != null) {
 				nodePref.put(NetworkPrefResources.HOST, networkResource.getHost());
 				nodePref.put(NetworkPrefResources.PORT, String.valueOf(networkResource.getPort()));
 				nodePref.put(NetworkPrefResources.HAS_AUTHORIZED, String.valueOf(networkResource.getAuthenticate()));
 			}
 		}
 		if (partsNode[partsNode.length - 1].equals(NetworkPrefResources.HTTPS)) {
 			
 			NetworkPrefResources networkPrefResources = StartupPlugin.getDefault().getNetworkPrefResources();
 			Map networkPrefMap = networkPrefResources.getNetworkPreferencesMap();
 			NetworkPrefResource networkResource = (NetworkPrefResource) networkPrefMap.get(NetworkPrefResources.HTTPS);
 			String host = networkResource.getHost();
			if (host != null) {
 				nodePref.put(NetworkPrefResources.HOST, networkResource.getHost());
 				nodePref.put(NetworkPrefResources.PORT, String.valueOf(networkResource.getPort()));
 				nodePref.put(NetworkPrefResources.HAS_AUTHORIZED, String.valueOf(networkResource.getAuthenticate()));
 			}
 		}
 		
 		try {
 			// Forces the application to save the preferences
 			nodePref.flush();
 		} catch (BackingStoreException e) {
 			e.printStackTrace();
 		}
 
 	}
 	// END
 	
 	
 	// ADD THIS CODE FOR TESTING WITH ECLIPSE 3.5
 	/*private static String buildNodeName(String[] nodeNameParts) {
 		
 		StringBuilder buffer = new StringBuilder("/");
 		for (int i = 0; i < nodeNameParts.length; i++) {
 			if (i == (nodeNameParts.length-1))
 				buffer.append(nodeNameParts[i]);
 			
 			buffer.append(nodeNameParts[i]).append("/");
 		}
 			
 		return null;
 	}*/
 	// END
 	
 	
 	/**
 	 *  ADD THIS METHOD FOR TESTING
 	 * @param node
 	 * @return
 	 */
 	/*public static TreeItem testAddNodeInternal(IEclipsePreferences prefNode, Map<String, TreeItem> imNodeMap,
 											Tree imPrefTree) {
 		
 		String nodeName = prefNode.absolutePath();
 		TreeItem imNode = imNodeMap.get(nodeName);
 		if (imNode != null)
 			return imNode; // Already added
 		
 		ImageRegistry reg = StartupPlugin.getDefault().getImageRegistry();
 		
 		if (CommonPrefsHelper.isConfigurationNode(prefNode)) {
 			// Scope is "configuration" - add top root node to collect children
 			imNode = new TreeItem(imPrefTree, SWT.NONE);
 			imNode.setImage(reg.get(StartupPlugin.EXP_TREE_CONFIG_IMG));
 			// ADD THIS CODE FOR TESTING
 			ConfigurationScope configScope = new ConfigurationScope();
 			IEclipsePreferences eclipsePrefNode = configScope.getNode(nodeName);
 			// END
 		} else {
 			IEclipsePreferences pPrefNode = (IEclipsePreferences) prefNode.parent();
 			// ADD THIS CODE FOR TESTING
 			
 			// END
 			if (CommonPrefsHelper.isInstanceNode(pPrefNode)) {
 				// Scope is "instance" - add top node
 				imNode = new TreeItem(imPrefTree, SWT.NONE);
 				imNode.setImage(reg.get(StartupPlugin.EXP_TREE_PLUGIN_IMG));
 				
 				// ADD THIS CODE FOR TESTING
 				
 				// END
 			} else {
 				// Scope is either subnode or "configuration" - get parent node
 				String pNodeName = pPrefNode.absolutePath();
 				TreeItem pNode = imNodeMap.get(pNodeName);
 				if (pNode == null)
 					pNode = addNodeInternal(pPrefNode, imNodeMap, imPrefTree);
 				imNode = new TreeItem(pNode, SWT.NONE);
 				
 				// If plug-in node in the "configuration" tree - add image
 				if (CommonPrefsHelper.isConfigurationNode(pPrefNode))
 					imNode.setImage(reg.get(StartupPlugin.EXP_TREE_PLUGIN_IMG));
 			}
 		}
 		
 		String [] nodeParts = nodeName.split("/");
 		imNode.setGrayed(nodeParts.length > 3); // Set grayed if not plug-in node
 		
 		imNode.setText(prefNode.name());
 		imNode.setData(prefNode);
 		
 		imNodeMap.put(nodeName, imNode);	
 		
 		return imNode;
 	}	*/
 	
 	
 	/**
 	 * ADD THIS METHOD FOR TESTING
 	 * @param node
 	 * @return
 	 */
 	/*public ScopedPreferenceStore getPreferenceStore(IScopeContext scopeContext, String qualifier, 
 								ScopedPreferenceStore preferenceStore) {
 		
         // Create the preference store lazily.
         if (preferenceStore == null) {
             preferenceStore = new ScopedPreferenceStore(scopeContext,qualifier);
 
         }
         return preferenceStore;
     }*/
 
 	
 	
 	
 	public static boolean isConfigurationNode(IEclipsePreferences node) {
 		return node != null && node.name().compareTo("configuration") == 0;
 	}
 	
 	public static boolean isConfigurationNode(TreeItem node) {
 		return node != null && node.getText().compareTo("configuration") == 0;
 	}
 	
 	public static boolean isInstanceNode(IEclipsePreferences node) {
 		if (node == null)
 			return false;
 		
 		return node != null &&
 			node.name().compareTo("instance") == 0 ||
 			node.name().compareTo("") == 0;
 	}
 	
 	private static boolean isPluginNode(TreeItem item) {
 		if (item.getParentItem() == null)
 			return true;
 		return CommonPrefsHelper.isConfigurationNode(item.getParentItem());
 	}
 	
 	public static TreeItem getPluginNode(TreeItem item) {
 		if (isPluginNode(item)) {
 			return item;
 		} else 
 			return getPluginNode(item.getParentItem());
 	}
 	
 	/**
 	 * Return a list of all plugin items in the tree 
 	 * 
 	 * @param tree
 	 * @return
 	 */
 	public static List<TreeItem> getPluginNodes(Tree tree){
 		ArrayList<TreeItem> items = new ArrayList<TreeItem>();
 		TreeItem[] treeItems = tree.getItems();
 		for (TreeItem item : treeItems) {		
 			if (isConfigurationNode(item)) {
 				TreeItem[] confItems = item.getItems();
 				for (TreeItem confItem : confItems) 
 					items.add(confItem);
 			} else
 				items.add(item);
 		}
 		return items;
 	}
 	
 	
 }
