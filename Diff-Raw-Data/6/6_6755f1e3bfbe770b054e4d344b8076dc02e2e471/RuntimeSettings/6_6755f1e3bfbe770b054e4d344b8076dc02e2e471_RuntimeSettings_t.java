 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.core.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeProvider;
 import org.osgi.service.metatype.ObjectClassDefinition;
 
 /**
  * A component to allow the user to change the java-runtime properties
  * stored in <code>start.ini</code> via the OSGI configuration-management.
  */
 public class RuntimeSettings implements MetaTypeProvider, ManagedService {
 	public static final String PID = RuntimeSettings.class.getName();
 	private static final String CM_XMX = "jvm.xmx";
 	private static final String CM_XMS = "jvm.xms";
 	private static final String CM_HEAP_DUMP = "jvm.heapdump";
 	private static final String CM_OTHER = "jvm.other";
 	
 	// TODO: remove space when mutliline-attributes are implemented
 	private static final String OPT_OTHER_SPLIT = " " + System.getProperty("line.separator");
 	
 	/**
 	 * Represents an JVM-parameter or - in the special case of {@link RuntimeSettings#CM_OTHER_ENTRY} -
 	 * a whole list of parameters.
 	 * There are three distinct states one of which an instance of this class can represent:
 	 * <ol>
 	 *   <li>A JVM-parameter composed of {@link #split two parts} : A {@link #fixOpt static identifier}-string and a
 	 *       {@link #defVal value} directly concatenated to the former.</li>
 	 *   <li>A non-variant JVM-parameter whose only possible representations are enabled or disabled,
 	 *       in contrast to the {@link #split}-parameter.</li>
 	 *   <li>A special state of which only one should exist: multiple parameters for the JVM, therefore no {@link #fixOpt}
 	 *       exists and is set to <code>null</code>, see {@link RuntimeSettings#CM_OTHER_ENTRY}.</li>
 	 * </ol>
 	 * <p>
 	 * In the CM, {@link OptEntry OptEntries} of the first state {@link #update(List, Object) save} only the variant
 	 * value supplied by the user. The second (non-split) version stores the {@link String}-representation of a
 	 * <code>boolean</code> value defining whether this option is set or not. The third version finally stores it's
 	 * value by first {@link RuntimeSettings#splitOptionLine(List, String) splitting} it into the distinct parameters
 	 * and then concatenating those using {@link RuntimeSettings#OPT_OTHER_SPLIT} to ensure a consistent format.
 	 */
 	private static final class OptEntry {
 		final String pid;
 		final boolean split;
 		final String fixOpt;
 		final Object defVal;
 		final String pattern;
 		
 		private OptEntry(final String pid, final boolean split, final String defKey, final Object defVal, final String pattern) {
 			this.pid = pid;
 			this.split = split;
 			this.fixOpt = defKey;
 			this.defVal = defVal;
 			this.pattern = pattern;
 		}
 		
 		public OptEntry(final String pid, final String opt, final boolean defEnabled) {
 			this(pid, false, opt, Boolean.valueOf(defEnabled), null);
 		}
 		
 		public OptEntry(final String pid, final String key, final String val, final String pattern) {
 			this(pid, true, key, val, pattern);
 		}
 		
 		public String getPid() {
 			return PID + '.' + pid;
 		}
 		
 		public String matches(final String opt) {
 			if (opt == null)
 				return "";
 			if (pattern == null)
 				return null;
 			return opt.matches(pattern) ? "" : "Doesn't match '" + pattern + "'";
 		}
 		
 		public Object getValue(final String opt, final boolean init) {
 			if (opt == null)
 				return defVal;
 			if (split) {
 				return (fixOpt == null) ? opt : opt.substring(fixOpt.length());
 			} else {
 				return (init) ? Boolean.TRUE : defVal;
 			}
 		}
 		
 		public boolean update(final List<String> currentSettings, final Object value) {
 			if (split) {
 				return updateSetting(currentSettings, fixOpt, (String)value);
 			} else {
 				final boolean enabled = (value instanceof String) ? Boolean.parseBoolean((String)value) : ((Boolean)value).booleanValue();
 				if (enabled) {
 					return updateSetting(currentSettings, fixOpt);
 				} else {
 					return currentSettings.remove(fixOpt);
 				}
 			}
 		}
 	}
 	
 	private static final Set<OptEntry> OPTIONS;
 	private static final OptEntry CM_OTHER_ENTRY = new OptEntry(CM_OTHER, null, "", ".*");
 	static {
 		final Set<OptEntry> options = new HashSet<OptEntry>();
 		options.add(new OptEntry(CM_XMX, "-Xmx", "128m", "\\d+[gGmMkK]"));
 		options.add(new OptEntry(CM_XMS, "-Xms", "64m", "\\d+[gGmMkK]"));
 		options.add(new OptEntry(CM_HEAP_DUMP, "-XX:+HeapDumpOnOutOfMemoryError", false));
 		OPTIONS = Collections.unmodifiableSet(options);
 	}
 	
 	/**
 	 * For logging
 	 */
 	private Log logger = LogFactory.getLog(RuntimeSettings.class);
 	
 	/**
 	 * A list of {@link Locale} for which a {@link ResourceBundle} exists
 	 * @see MetaTypeProvider#getLocales()
 	 */
 	private final String[] locales;		
 	
 	/**
 	 * Config file where the runtime-options are stored, e.g.
 	 * <code>start.ini</code>
 	 */
 	private final File iniFile;
 	
 	public RuntimeSettings(String[] locales) {
 		this(locales, new File("start.ini"));
 	}
 	
 	public RuntimeSettings(String[] locales, File iniFile) {
 		if (locales == null) throw new NullPointerException("The locale array is null");
 		if (iniFile == null) throw new NullPointerException("The ini-file is null");
 		
 		this.locales = locales;
 		this.iniFile = iniFile;
 	}	
 	
 	private synchronized List<String> readSettings() {
 		BufferedReader reader = null;
 		ArrayList<String> configOptions = new ArrayList<String>();
 		try {
 			reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.iniFile), "UTF-8"));
 			
 			String line = null;		
 			while ((line = reader.readLine())!=null) {
 				configOptions.add(line.trim());
 			}
 			
 			reader.close();
 		} catch (Exception e) {
 			this.logger.error("Unable to read settings from file: " + this.iniFile.toString() ,e);
 		} finally {
 			if (reader != null) try { reader.close(); } catch (Exception e) {/* ignore this */}
 		}
 		return configOptions;
 	}
 	
 	private synchronized void writeSettings(List<String> runtimeSettings) {
 		PrintWriter writer = null;
 		try {
 			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.iniFile),"UTF-8"));
 			
 			if (runtimeSettings != null) {
 				for (String item : runtimeSettings) {
 					writer.println(item.trim());
 				}
 			}
 			
 			writer.flush();
 			writer.close();
 			writer = null;
 		} catch (Exception e) {
 			this.logger.error("Unable to write settings to file: " + this.iniFile.toString() ,e);
 		} finally {
 			if (writer != null) try { writer.close(); } catch (Exception e) {/*ignore this*/}
 		}		
 	}
 	
 	/**
 	 * @see MetaTypeProvider#getLocales()
 	 */
 	public String[] getLocales() {
 		return this.locales;
 	}
 	
 	/**
 	 * @see MetaTypeProvider#getObjectClassDefinition(String, String)
 	 */
 	public ObjectClassDefinition getObjectClassDefinition(String id, String localeStr) {
 		final Locale locale = (localeStr==null) ? Locale.ENGLISH : new Locale(localeStr);
 		final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/" + RuntimeSettings.class.getSimpleName(), locale);		
 		
 		final class OptAD implements AttributeDefinition {
 			
 			private final OptEntry entry;
 			private final String option;
 			
 			public OptAD(final OptEntry entry, final String option) {
 				this.entry = entry;
 				this.option = option;
 			}
 			
 			public String getID() {
 				return entry.getPid();
 			}
 			
 			public int getCardinality() {
 				return 0;
 			}
 			
 			public String getDescription() {
 				return rb.getString(entry.pid + ".desc");
 			}
 			
 			public String getName() {
 				return rb.getString(entry.pid + ".name");
 			}
 			
 			public String[] getDefaultValue() {
 				return new String[] { entry.getValue(option, false).toString() };
 			}
 			
 			public String[] getOptionLabels() {
 				return null;
 			}
 			
 			public String[] getOptionValues() {
 				return null;
 			}
 			
 			public int getType() {
 				return (entry.split) ? STRING : BOOLEAN;
 			}
 			
 			public String validate(String value) {
 				return entry.matches(value);
 			}
 		}
 		
 		return new ObjectClassDefinition() {
 			public AttributeDefinition[] getAttributeDefinitions(int filter) {
 				final List<AttributeDefinition> attribs = new ArrayList<AttributeDefinition>();
 				
 				// loading all currently avilable jvm options
 				final List<String> runtimeSettings = readSettings();
 				final HashSet<OptEntry> optEntries = new HashSet<OptEntry>(OPTIONS);
				String otherValues = "";
 				if (runtimeSettings != null) {
 					// process all known options and concatenate all unknown ones to one string
 					// known options are those, that conform to an OptEntry saved in the OPTIONS-set
 					final StringBuilder sb = new StringBuilder();
 					outer: for (final String opt : runtimeSettings) {
 						final Iterator<OptEntry> it = optEntries.iterator();
 						while (it.hasNext()) {
 							final OptEntry e = it.next();
 							if (opt.startsWith(e.fixOpt)) {
 								attribs.add(new OptAD(e, opt));
 								it.remove();
 								continue outer;
 							}
 						}
 						if (sb.length() > 0)
 							sb.append(OPT_OTHER_SPLIT);
 						sb.append(opt);
 					}
 					otherValues = sb.toString();
 				}
 				for (final OptEntry e : optEntries)
 					attribs.add(new OptAD(e, null));
 				
 				// put the remaining options into an AD allowing arbitrary strings
				attribs.add(new OptAD(CM_OTHER_ENTRY, otherValues));
 				
 				return attribs.toArray(new AttributeDefinition[attribs.size()]);
 			}
 			
 			public String getDescription() {
 				try {
 					return MessageFormat.format(
 							rb.getString("runtimeSettings.desc"),
 							iniFile.getCanonicalFile().toString()
 					);
 				} catch (IOException e) {
 					logger.error(e);
 					return rb.getString("runtimeSettings.desc");
 				}
 			}
 			
 			public String getID() {
 				return PID;
 			}
 			
 			public InputStream getIcon(int size) throws IOException {
 				return null;
 			}
 			
 			public String getName() {
 				return rb.getString("runtimeSettings.name");
 			}
 		};				
 	}
 	
 	/**
 	 * @see ManagedService#updated(Dictionary)
 	 */	
 	@SuppressWarnings("unchecked")
 	public void updated(Dictionary properties) throws ConfigurationException {
 		if (properties == null) return;
 		
 		// loading current settings
 		List<String> currentSettings = this.readSettings();
 		boolean changesDetected = false;
 		
 		// check all pre-defined options and update them in the currentSettings-list
 		for (final OptEntry entry : OPTIONS) {
 			final Object value = properties.get(entry.getPid());
 			if (value != null)
 				changesDetected |= entry.update(currentSettings, value);
 		}
 		
 		// check all other options and update the currentSettings-list
 		final String valOthers = (String)properties.get(CM_OTHER_ENTRY.getPid());
 		if (valOthers != null) {
 			final Set<String> otherSettings = new HashSet<String>();
 			final String[] valSplit = valOthers.split("[\\r\\n]");
 			for (int i=0; i<valSplit.length; i++) {
 				final String valOther = valSplit[i].trim();
 				if (valOther.length() > 0) try {
 					splitOptionLine(otherSettings, valOther);
 				} catch (ParseException e) {
 					throw new ConfigurationException(
 							CM_OTHER_ENTRY.getPid(),
 							e.getMessage() + " at position " + e.getErrorOffset() + " in line " + i);
 				}
 			}
 			
 			/* check the currentSettings for any parameters that do not
 			 * - match a pre-defined option
 			 * - equal an user-specified option in otherSettings
 			 * and remove it.
 			 * This results in a list which only contains options which are either known or
 			 * explicitely specified by the user */
 			final Iterator<String> it = currentSettings.iterator();
 			outer: while (it.hasNext()) {
 				final String setting = it.next();
 				for (final OptEntry entry : OPTIONS)
 					if (setting.startsWith(entry.fixOpt))
 						continue outer;
 				if (otherSettings.contains(setting))
 					continue;
 				it.remove();
 				changesDetected = true;
 			}
 			
 			// finally add all otherSettings to the currentSettings-list, which is
 			for (final String setting : otherSettings)
 				changesDetected |= updateSetting(currentSettings, setting);
 		}
 		
 		if (changesDetected) {
 			// write changes into file
 			this.writeSettings(currentSettings);
 		}
 	}
 	
 	private static boolean splitOptionLine(final Collection<String> currentSettings, final String valOthers) throws ParseException {
 		// split the value at whitespace, but obey single and double quotes
 		boolean changesDetected = false;
 		int level = 0;
 		char lastLeveled = 0;
 		int last = 0;
 		
 		for (int i=0; i<valOthers.length(); i++) {
 			final char c = valOthers.charAt(i);
 			if (c == '"' || c == '\'') {
 				if (lastLeveled == c) {
 					level--;
 					lastLeveled = (level == 0) ? 0 : (c == '"') ? '\'' : '"';
 				} else {
 					level++;
 					lastLeveled = c;
 				}
 			} else if (level == 0 && (c == ' ' || c == '\t' || c == '\f')) {
 				if (last < i) {
 					final String opt = valOthers.substring(last, i).trim();
 					if (opt.length() > 0)
 						changesDetected |= updateSetting(currentSettings, opt);
 				}
 				last = i + 1;
 			}
 		}
 		
 		if (level != 0)
 			throw new ParseException("unmatched " + ((lastLeveled == '"') ? "double" : "single") + " quote", last);
 		if (last < valOthers.length() - 1) {
 			final String opt = valOthers.substring(last).trim();
 			if (opt.length() > 0)
 				changesDetected |= updateSetting(currentSettings, opt);
 		}
 		
 		return changesDetected;
 	}
 	
 	public Dictionary<?,?> getCurrentIniSettings() {
 		final Dictionary<String,Object> props = new Hashtable<String,Object>();
 		final List<String> iniSettings = readSettings();
 		
 		final StringBuilder sb = new StringBuilder();
 		final HashSet<OptEntry> opts = new HashSet<OptEntry>(OPTIONS);
 		outer: for (final String opt : iniSettings) {
 			final Iterator<OptEntry> it = opts.iterator();
 			while (it.hasNext()) {
 				final OptEntry e = it.next();
 				if (opt.startsWith(e.fixOpt)) {
 					props.put(e.getPid(), e.getValue(opt, true));
 					it.remove();
 					continue outer;
 				}
 			}
 			if (sb.length() > 0)
 				sb.append(OPT_OTHER_SPLIT);
 			sb.append(opt);
 		}
 		for (final OptEntry e : opts)
 			props.put(e.getPid(), e.getValue(null, true));
 		if (sb.length() > 0)
 			props.put(CM_OTHER_ENTRY.getPid(), sb.toString());
 		
 		return props;
 	}
 	
 	private static boolean updateSetting(final Collection<String> currentSetting, final String value) {
 		if (currentSetting.contains(value))
 			return false;
 		currentSetting.add(value);
 		return true;
 	}
 	
 	private static boolean updateSetting(List<String> currentSettings, String prefix, String value) {
 		boolean found = false;
 		boolean updated = false;
 		
 		// loop through the settings to find the current value
 		for (int i=0; i<currentSettings.size(); i++) {
 			String setting = currentSettings.get(i);
 			if (setting.startsWith(prefix)) {
 				found = true;
 				if (!setting.equals(value)) {
 					// replace with new value
 					currentSettings.set(i, prefix + value);
 					updated = true;
 				}
 			}
 		}
 		
 		if (!found) {
 			// just append value
 			currentSettings.add(value);
 			updated = true;
 		}
 		
 		return updated;
 	}
 }
