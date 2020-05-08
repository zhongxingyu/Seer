 /*******************************************************************************
  * Copyright (c) Jun 28, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.mapping.validator;
 
 import static org.zend.sdklib.mapping.IMappingModel.APPDIR;
 import static org.zend.sdklib.mapping.IMappingModel.LIBRARY;
 import static org.zend.sdklib.mapping.IMappingModel.SCRIPTSDIR;
 import static org.zend.sdklib.mapping.PropertiesBasedMappingLoader.EXCLUDES;
 import static org.zend.sdklib.mapping.PropertiesBasedMappingLoader.GLOBAL;
 import static org.zend.sdklib.mapping.PropertiesBasedMappingLoader.INCLUDES;
 import static org.zend.sdklib.mapping.PropertiesBasedMappingLoader.SEPARATOR;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.zend.sdklib.internal.mapping.LibraryMapping;
 
 /**
  * Default implementation of {@link IMappingValidator}.
  * 
  * @author Wojciech Galanciak, 2011
  * 
  */
 public class MappingValidator implements IMappingValidator {
 
 	private static final String KEY_VALUE_SEPARATOR = "=";
 
 	private static final String LINE_SEPARATOR = "\\";
 
 	private static final String KEY_SEPARATOR = "\\.";
 
 	private File container;
 	private boolean hasAppdir;
 	private int buffer;
 
 	public MappingValidator(File container) {
 		super();
 		this.container = container;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.zend.sdklib.mapping.validator.IMappingValidator#parse(java.io.InputStream
 	 * )
 	 */
 	@Override
 	public boolean parse(InputStream stream) throws MappingParseException {
 		List<MappingParseStatus> result = new ArrayList<MappingParseStatus>();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
 		String line = null;
 		String previousLastChar = null;
 		int i = 0;
 		String lastKey = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				i++;
 				String[] parts = line.split(KEY_VALUE_SEPARATOR);
 				if (parts.length == 2) {
 					MappingParseStatus keyStatus = checkValidKey(parts[0], i, line);
 					if (keyStatus != null) {
 						result.add(keyStatus);
 					}
 					lastKey = parts[0].trim();
 					List<MappingParseStatus> valueStatus = checkValidValues(
 							parts[1], i, line, lastKey);
 					if (!valueStatus.isEmpty()) {
 						result.addAll(valueStatus);
 					}
 				} else {
 					if (!line.trim().isEmpty()) {
 						if (LINE_SEPARATOR.equals(previousLastChar)) {
 							String entry = line.trim();
 							if (entry.endsWith(LINE_SEPARATOR)) {
 								entry = entry.substring(0, line.trim().length() - 1);
 							}
 							List<MappingParseStatus> valueStatus = checkValidValues(
 									entry, i, line, lastKey);
 							if (!valueStatus.isEmpty()) {
 								result.addAll(valueStatus);
 							}
 						} else {
 							int start = buffer + line.indexOf(line.trim());
 							int end = start + line.trim().length();
 							result.add(new MappingParseStatus(i, start, end,
 									MappingParseMessage.INVALID_LINE));
 						}
 					}
 				}
 				buffer += line.length() + 1;
 				previousLastChar = line.trim().isEmpty() ? null : line.trim().substring(
 						line.trim().length() - 1);
 			}
 			if (i == 0) {
 				result.add(new MappingParseStatus(0, 0, 0, MappingParseMessage.EMPTY_FILE));
 			} else if (!hasAppdir) {
 				result.add(new MappingParseStatus(0, 0, 0, MappingParseMessage.NO_APPDIR));
 			}
 			stream.close();
 		} catch (IOException e) {
 			result.add(new MappingParseStatus(0, 0, 0, MappingParseMessage.CANNOT_READ));
 		}
 		if (result.isEmpty()) {
 			return true;
 		} else {
 			throw new MappingParseException(result);
 		}
 	}
 
 	private List<MappingParseStatus> checkValidValues(String value, int lineNo,
 			String line, String lastKey) {
 		List<MappingParseStatus> result = new ArrayList<MappingParseStatus>();
 		String[] values = value.split(SEPARATOR);
 		if (values.length == 0 || value.trim().length() == 0) {
 			int offset = line.length() - value.length();
 			result.add(new MappingParseStatus(lineNo, offset, offset + 1,
 					MappingParseMessage.EMPTY_MAPPING));
 			return result;
 		}
 		for (String entry : values) {
 			entry = entry.trim();
 			if (entry.isEmpty() || entry.equals(LINE_SEPARATOR)) {
 				continue;
 			}
 			if ((LIBRARY + INCLUDES).equals(lastKey)) {
				if (lastKey != null) {
					String keyName = lastKey.substring(0, lastKey.indexOf('.'));
					LibraryMapping mapping = LibraryMapping.create(keyName,
							entry);
					if (mapping == null) {
 						int offset = line.indexOf(entry);
 						result.add(new MappingParseStatus(lineNo, offset
 								+ buffer, offset + entry.length() + buffer,
								MappingParseMessage.INVALID_LIBRARY));
					} else {
						String lib = mapping.getLibraryPath();
						File libraryFile = new File(lib);
						if (!libraryFile.isAbsolute()) {
							libraryFile = new File(container, lib);
						}
						if (!libraryFile.exists()) {
							int offset = line.indexOf(entry);
							result.add(new MappingParseStatus(lineNo, offset
									+ buffer, offset + entry.length() + buffer,
									MappingParseMessage.NOT_EXIST));
						}
 					}
 				}
 			} else {
 				boolean isGlobal = entry.startsWith(GLOBAL);
 				if (isGlobal) {
 					entry = entry.substring(GLOBAL.length());
 				} else {
 					File file = new File(container, entry);
 					if (!file.exists()) {
 						int offset = line.indexOf(entry);
 						result.add(new MappingParseStatus(lineNo, offset
 								+ buffer, offset + entry.length() + buffer,
 								MappingParseMessage.NOT_EXIST));
 					}
 
 				}
 			}
 		}
 		return result;
 	}
 
 	private MappingParseStatus checkValidKey(String key, int lineNo, String line) {
 		key = key.trim();
 		if (key.equals(APPDIR + INCLUDES)) {
 			hasAppdir = true;
 			return null;
 		} else if (key.equals(APPDIR + EXCLUDES)
 				|| key.equals(SCRIPTSDIR + INCLUDES)
 				|| key.equals(SCRIPTSDIR + EXCLUDES)
 				|| key.equals(LIBRARY + INCLUDES)) {
 			return null;
 		} else {
 			String[] parts = key.split(KEY_SEPARATOR);
 			if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
 				int start = line.indexOf(key) + buffer;
 				int end = start + key.length();
 				return new MappingParseStatus(lineNo, start, end, MappingParseMessage.INVALID_KEY);
 			}
 			String folder = parts[0];
 			if (APPDIR.equals(folder) || SCRIPTSDIR.equals(folder)) {
 				String suffix = parts[1];
 				if (INCLUDES.equals(suffix) || EXCLUDES.equals(suffix)) {
 					return null;
 				} else {
 					int start = line.indexOf(key) + folder.length() + buffer + 1;
 					int end = line.indexOf(key) + key.length() + buffer;
 					return new MappingParseStatus(lineNo, start, end,
 							MappingParseMessage.INVALID_SUFFIX);
 				}
 			} else {
 				int start = line.indexOf(key) + buffer;
 				int end = start + folder.length();
 				return new MappingParseStatus(lineNo, start, end,
 						MappingParseMessage.INVALID_FOLDER);
 			}
 		}
 	}
 
 }
