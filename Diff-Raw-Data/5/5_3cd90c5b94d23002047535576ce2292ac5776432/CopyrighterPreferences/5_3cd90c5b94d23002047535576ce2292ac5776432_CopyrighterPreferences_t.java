 /***************************************************************************
  * Copyright (c) 2013 Codestorming.org.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Codestorming - initial API and implementation
  ****************************************************************************/
 package org.codestorming.copyrighter.preferences;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.codestorming.copyrighter.CopyrighterActivator;
 import org.codestorming.copyrighter.ICopyrighterConstants;
 import org.codestorming.copyrighter.license.License;
 import org.codestorming.eclipse.util.io.FileHelper;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.osgi.framework.Bundle;
 
 /**
  * Provides the Java Copyrighter plug-in global preferences.
  * 
  * @author Thaedrik <thaedrik@gmail.com>
  */
 public class CopyrighterPreferences {
 
 	private static final String SEPARATOR = "%";
 
 	/**
 	 * Returns the Copyrighter plug-in preference store.
 	 * 
 	 * @return the Copyrighter plug-in preference store.
 	 */
 	protected IPreferenceStore getStore() {
 		return CopyrighterActivator.getDefault().getPreferenceStore();
 	}
 
 	/**
 	 * Returns the registered {@link License licenses}.
 	 * 
 	 * @return the registered {@link License licenses}.
 	 */
 	public Set<License> getLicences() {
 		Set<String> licensesNames = getRegisteredLicenses();
 		Set<License> licenses = new LinkedHashSet<License>(licensesNames.size());
 		for (String licenseName : licensesNames) {
 			try {
				final License license = loadLicense(licenseName);
				if (license != null) {
					licenses.add(license);
				}
 			} catch (Exception e) {
 				// Ignore
 			}
 		}
 		return licenses;
 	}
 
 	protected Set<String> getRegisteredLicenses() {
 		String licensesStr = getStore().getString(ICopyrighterConstants.PREF_LICENSES);
 		Set<String> licenses = new LinkedHashSet<String>();
 		if (licensesStr.length() > 0) {
 			for (String license : licensesStr.split(SEPARATOR)) {
 				licenses.add(license);
 			}
 		}
 		return licenses;
 	}
 
 	/**
 	 * Returns the lastly used copyright header.
 	 * 
 	 * @return the lastly used copyright header.
 	 */
 	public String getLastCopyrightHeader() {
 		return getStore().getString(ICopyrighterConstants.PREF_LAST_COPYRIGHT_HEADER);
 	}
 
 	/**
 	 * Returns the lastly used license preset in the preferences.
 	 * 
 	 * @return the lastly used license preset in the preferences.
 	 */
 	public String getLastLicensePreset() {
 		return getStore().getString(ICopyrighterConstants.PREF_LAST_LICENSE);
 	}
 
 	/**
 	 * Returns the registered contributors.
 	 * 
 	 * @return the registered contributors.
 	 */
 	public Set<String> getContributors() {
 		String contributors = getStore().getString(ICopyrighterConstants.PREF_CONTRIBUTORS);
 		Set<String> contributorsSet = new LinkedHashSet<String>();
 		if (contributors.length() > 0) {
 			for (String contributor : contributors.split(SEPARATOR)) {
 				contributorsSet.add(contributor);
 			}
 		}
 		return contributorsSet;
 	}
 
 	/**
 	 * Defines the lastly used copyright header.
 	 * 
 	 * @param header the lastly used copyright header.
 	 */
 	public void setLastCopyrightHeader(String header) {
 		getStore().setValue(ICopyrighterConstants.PREF_LAST_COPYRIGHT_HEADER, header);
 	}
 
 	/**
 	 * Defines the lastly used license preset in the preferences.
 	 * 
 	 * @param licenseName The name of the lastly used license.
 	 */
 	public void setLastLicensePreset(String licenseName) {
 		getStore().setValue(ICopyrighterConstants.PREF_LAST_LICENSE, licenseName);
 	}
 
 	/**
 	 * Defines the contributors used in copyrights.
 	 * 
 	 * @param contributors The contributors to set.
 	 */
 	public void setContributors(Set<String> contributors) {
 		StringBuilder contributorStr = new StringBuilder();
 		if (contributors.size() > 0) {
 			for (String contributor : contributors) {
 				contributorStr.append(contributor);
 				contributorStr.append(SEPARATOR);
 			}
 			contributorStr.deleteCharAt(contributorStr.length() - 1);
 		}
 		getStore().setValue(ICopyrighterConstants.PREF_CONTRIBUTORS, contributorStr.toString());
 	}
 
 	public void setRegisteredLicenses(Set<String> licenses) {
 		StringBuilder licenseStr = new StringBuilder();
 		if (licenses.size() > 0) {
 			for (String license : licenses) {
 				licenseStr.append(license);
 				licenseStr.append(SEPARATOR);
 			}
 			licenseStr.deleteCharAt(licenseStr.length() - 1);
 		}
 		getStore().setValue(ICopyrighterConstants.PREF_LICENSES, licenseStr.toString());
 	}
 
 	/**
 	 * Registers the given {@link License license}.
 	 * <p>
 	 * Does nothing if the license already exists.
 	 * 
 	 * @param license The license to register
 	 * @throws IOException If an error occurs when saving the license.
 	 * @throws SecurityException If the current user have not enough rights to use the
 	 *         file system at the bundle data location.
 	 */
 	public void addLicense(License license) throws SecurityException, IOException {
 		File licenseDataFile = getLicenseFile(license.getName());
 		if (licenseDataFile != null && !licenseDataFile.exists()) {
 			saveLicense(license);
 			addLicense(license.getName());
 		}
 	}
 
 	protected void addLicense(String licenseName) {
 		Set<String> licenses = getRegisteredLicenses();
 		licenses.add(licenseName);
 		setRegisteredLicenses(licenses);
 	}
 
 	/**
 	 * Removes the given {@link License license}.
 	 * 
 	 * @param license The license to remove.
 	 * @throws SecurityException If the current user have not enough rights to use the
 	 *         file system at the bundle data location.
 	 */
 	public void removeLicense(License license) throws SecurityException {
 		File licenseDataFile = getLicenseFile(license == null ? null : license.getName());
 		if (licenseDataFile != null && licenseDataFile.exists()) {
 			licenseDataFile.delete();
 			removeLicense(license.getName());
 		}
 	}
 	
 	protected void removeLicense(String licenseName) {
 		Set<String> licenses = getRegisteredLicenses();
 		licenses.remove(licenseName);
 		setRegisteredLicenses(licenses);
 	}
 
 	/**
 	 * Returns the {@link File} located in the bundle data and corresponding to the given
 	 * {@code licenseName} or {@code null} if the licenseName is {@code null}.
 	 * <p>
 	 * Note the returned file may not exist.
 	 * 
 	 * @param licenseName The name of the file to get.
 	 * @return the {@link File} located in the bundle data and corresponding to the given
 	 *         {@code licenseName}.
 	 */
 	protected File getLicenseFile(String licenseName) {
 		File licenseDataFile = null;
 		if (licenseName != null) {
 			Bundle bundle = CopyrighterActivator.getDefault().getBundle();
 			licenseDataFile = bundle.getDataFile(licenseName);
 		}
 		return licenseDataFile;
 	}
 
 	/**
 	 * Saves the given {@link License license}.
 	 * <p>
 	 * The license will be saved only if its name is not {@code null}.
 	 * 
 	 * @param license The license to save.
 	 * @throws IOException If an error occurs when saving.
 	 * @throws SecurityException If the current user have not enough rights to use the
 	 *         file system at the bundle data location.
 	 */
 	protected void saveLicense(License license) throws IOException, SecurityException {
 		File licenseDataFile = getLicenseFile(license.getName());
 		if (licenseDataFile != null) {
 			if (!licenseDataFile.exists()) {
 				licenseDataFile.createNewFile();
 			}
 			FileOutputStream fos = null;
 			ObjectOutputStream oos = null;
 			try {
 				fos = new FileOutputStream(licenseDataFile);
 				oos = new ObjectOutputStream(fos);
 				oos.writeObject(license);
 			} finally {
 				FileHelper.close(oos);
 			}
 		}
 	}
 
 	/**
 	 * Loads the {@link License} corresponding to the given {@code licenseName}.
 	 * 
 	 * @param licenseName The name of the license to load.
 	 * @return the {@link License} corresponding to the given {@code licenseName}, or
 	 *         {@code null}.
 	 * @throws IOException If an error occurs when saving.
 	 * @throws SecurityException If the current user have not enough rights to use the
 	 *         file system at the bundle data location.
 	 */
 	protected License loadLicense(String licenseName) throws IOException, ClassNotFoundException {
 		File licenseDataFile = getLicenseFile(licenseName);
 		if (licenseDataFile != null && licenseDataFile.exists()) {
 			FileInputStream fis = null;
 			ObjectInputStream ois = null;
 			try {
 				fis = new FileInputStream(licenseDataFile);
 				ois = new ObjectInputStream(fis);
 				Object license = ois.readObject();
 				if (license instanceof License) {
 					return (License) license;
 				}
 			} finally {
 				FileHelper.close(ois);
 			}
 		}
 		return null;
 	}
 }
