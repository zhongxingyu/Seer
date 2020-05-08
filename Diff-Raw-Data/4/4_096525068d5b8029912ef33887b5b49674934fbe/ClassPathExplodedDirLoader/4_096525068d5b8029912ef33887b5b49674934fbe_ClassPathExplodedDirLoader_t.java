 /**
 *   ORCC rapid content creation for entertainment, education and media production
 *   Copyright (C) 2012 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package org.mcuosmipcuter.orcc.util;
 
 import java.io.File;
 import java.util.Set;
 
 /**
  * Finds class names matching the given target class inside exploded directories on the class path.
  * @author Michael Heinzelmann
  */
 public class ClassPathExplodedDirLoader  {
 
 
 	/**
 	 * Loads all class names that match the given target class into the set given.
 	 * @param resultSet set of strings to store the result
 	 * @param targetClass the class to match (instanceof)
 	 */
 	public static final void loadClassNamesInto(Set<String> resultSet, Class<?> targetClass) {
 		
 		String classPath = System.getProperty("java.class.path");
 		String separator = System.getProperty("path.separator");
 		String fileSeparator = System.getProperty("file.separator");
 		
 		String[] paths = classPath.split(separator);
 
 		for(String path : paths) {
 			File f = new File(path);
 
 			if(f.isDirectory()) {
 				for(String top : f.list()) {
 					String pathB = new String(path +  fileSeparator + top + fileSeparator);
 					String packAge = new String(top + ".");
 					File t = new File(f.getAbsolutePath() + fileSeparator + top);
					if(t.isDirectory()) {
						recurse(t.list(), pathB, fileSeparator, packAge, resultSet, targetClass);
					}
 				}
 			}
 		}
 	}
 	
 	// private recursive calls
 	private static void recurse(String[] paths, String path, String fileSeparator, String packAge, Set<String> result, Class<?> targetClass) {
 		
 		for(String p : paths) {
 			File f = new File(path + p);
 
 			if(f.isDirectory()) {
 				recurse(f.list(), path + p + fileSeparator, fileSeparator, packAge + p + ".", result, targetClass);
 			}
 			else {
 				if(p.endsWith(".class")) {
 					String className = packAge + p.substring(0, p.length() - 6);
 					Class<?> claZZ;
 					try {
 						claZZ = Class.forName(className);
 						if(targetClass.isAssignableFrom( claZZ)) {
 							result.add(className);
 						}
 					} catch (ClassNotFoundException e) {
 						e.printStackTrace();
 					}
 	
 				}
 
 			}
 			
 		}
 	}
 
 }
