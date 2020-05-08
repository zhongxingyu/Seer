 /*
  * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
  *
  * This file is part of Iglu.
  *
  * Iglu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Iglu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ijsberg.iglu.util.io;
 
 import org.ijsberg.iglu.util.collection.ArraySupport;
 import org.ijsberg.iglu.util.formatting.PatternMatchingSupport;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 /**
  * Contains up to 4 rules that a file may match.
  *
  * Masks are wildcard expressions as defined in Iglu's pattern matching support.
  *
  * Note: Unix-style file separators are assumed in expressions.
  *
  * @see PatternMatchingSupport#valueMatchesWildcardExpression(String, String)
  */
 public class FileFilterRuleSet implements Cloneable, Serializable {
 
 	private String baseDir = null;
     private String includeFilesWithNameMask;
 	private String excludeFilesWithNameMask = "";
 	private String[] includeFilesContainingText = new String[0];
 	private String[] excludeFilesContainingText = new String[0];
 
 
 	public FileFilterRuleSet(String baseDir) {
 		super();
 		setBaseDir(baseDir);
 	}
 
 	private String getComparableFileName(File file) {
 		return getComparableFileName(file.getPath());
 
 	}
 
 	private String getComparableFileName(String fileName) {
 		String retval = FileSupport.convertToUnixStylePath(fileName);
 
 		if(baseDir != null) {
 			if(retval.startsWith(baseDir)) {
 				retval = retval.substring(baseDir.length());
 			}
 		}
 
 
 		return retval;
 	}
 
 	public FileFilterRuleSet() {
 		super();
 	}
 
 
 	private FileFilterRuleSet(String includeFilesWithNameMask, String excludeFilesWithNameMask,
 			String[] includeFilesContainingLineMask, String[] excludeFilesContainingLineMask, String baseDir) {
 		super();
 		this.includeFilesWithNameMask = includeFilesWithNameMask;
 		this.excludeFilesWithNameMask = excludeFilesWithNameMask;
 		this.includeFilesContainingText = includeFilesContainingLineMask;
 		this.excludeFilesContainingText = excludeFilesContainingLineMask;
 		this.baseDir = baseDir;
 	}
 
 
 /*    public FileFilterRuleSet setBaseDir(File baseDir) {
         this.baseDir = baseDir;
     }*/
 
 	/**
 	 * Checks if file matches rules.
 	 * @param file
 	 * @return true if a file exists, matches include and exclude rules and is successfully parsed in case of inspection of contents
 	 * @throws IOException
 	 */
 	public boolean fileMatchesRules(File file) {
 
         try {
             if(file.exists()) {
                 if(includeFilesContainingText.length == 0 && excludeFilesContainingText.length == 0) {
                     return fileMatchesRules(
 							getComparableFileName(file));
                 } else {
                     return fileMatchesRules(
 							getComparableFileName(file),
                             FileSupport.getTextFileFromFS(file));
                 }
             }
         } catch (IOException ioe) {
             //at the moment file does not match rules
         }
 		return false;
 	}
 
     public boolean fileMatchesRules(ZipEntry entry, ZipFile zipFile) {
 
         try {
                 if(includeFilesContainingText.length == 0 && excludeFilesContainingText.length == 0) {
                     return fileMatchesRules(
 							getComparableFileName(entry.getName()));
                 } else {
                     return fileMatchesRules(
 							getComparableFileName(entry.getName()),
                             FileSupport.getTextFileFromZip(entry.getName(), zipFile));
                 }
         } catch (IOException ioe) {
             //at the moment file does not match rules
         }
         return false;
     }
 
     public boolean fileMatchesRules(String fileName) {
 
             return
                     includeBecauseOfName(getComparableFileName(fileName)) &&
                             !excludeBecauseOfName(fileName);
     }
 
     public boolean fileMatchesRules(String fileName, String fileContents) {
 
         try {
             return
                     fileMatchesRules(getComparableFileName(fileName)) &&
                         includeBecauseOfContainedTextLine(fileContents) &&
                         !excludeBecauseOfContainedTextLine(fileContents);
 
         } catch (IOException ioe) {
             //at the moment file does not match rules
         }
         return false;
     }
 
 	private boolean includeBecauseOfName(String fileName) {
 
 		//System.out.println(baseDir);
 		//System.out.println(includeFilesWithNameMask + " <-- " + fileName);
 
 
 		boolean retval = includeFilesWithNameMask == null || "*".equals(includeFilesWithNameMask) ||
 				PatternMatchingSupport.valueMatchesWildcardExpression(fileName, includeFilesWithNameMask)
 		//		|| PatternMatchingSupport.valueMatchesWildcardExpression(fileName, "*/" + includeFilesWithNameMask)
 		;
 
 		return retval;
 	}
 
 	private boolean excludeBecauseOfName(String fileName) {
 		boolean retval = excludeFilesWithNameMask != null && !"".equals(excludeFilesWithNameMask) &&
                 (PatternMatchingSupport.valueMatchesWildcardExpression(fileName, excludeFilesWithNameMask)
 		//		|| PatternMatchingSupport.valueMatchesWildcardExpression(fileName, "*/" + excludeFilesWithNameMask)
 				);
 
 		return retval;
 	}
 
 	private boolean includeBecauseOfContainedTextLine(String fileContents) throws IOException {
 		boolean retval = includeFilesContainingText == null ||
                 includeFilesContainingText.length == 0 ||
                 occurenceFound(fileContents, includeFilesContainingText);
 		return retval;
 	}
 	
 	private boolean excludeBecauseOfContainedTextLine(String fileContents) throws IOException {
 		boolean retval = excludeFilesContainingText != null &&
                 excludeFilesContainingText.length > 0 &&
                 occurenceFound(fileContents, excludeFilesContainingText);
 		return retval;
 	}
 
 
     private static boolean occurenceFound(String fileContents, String[] expressions) throws IOException {
 
         for(String expression : expressions) {
             if(fileContents.contains(expression)) {
                 return true;
             }
         }
         return false;
     }
 
 	public FileFilterRuleSet setIncludeFilesWithNameMask(String includeFilesWithNameMask) {
 
 		if(includeFilesWithNameMask != null && includeFilesWithNameMask.startsWith("/")) {
 			this.includeFilesWithNameMask = includeFilesWithNameMask.substring(1);
 		} else {
 			this.includeFilesWithNameMask = includeFilesWithNameMask;
 		}
 		return this;
 	}
 
 
 	public FileFilterRuleSet setExcludeFilesWithNameMask(String excludeFilesWithNameMask) {
 
 		if(excludeFilesWithNameMask != null && excludeFilesWithNameMask.startsWith("/")) {
 			this.excludeFilesWithNameMask = excludeFilesWithNameMask.substring(1);
 		} else {
 			this.excludeFilesWithNameMask = excludeFilesWithNameMask;
 		}
 		return this;
 	}
 
 
 	public FileFilterRuleSet setIncludeFilesContainingText(
             String ... includeFilesContainingText) {
 		this.includeFilesContainingText = includeFilesContainingText;
 		return this;
 	}
 
 
 	public FileFilterRuleSet setExcludeFilesContainingText(
             String ... excludeFilesContainingText) {
 		this.excludeFilesContainingText = excludeFilesContainingText;
 		return this;
 	}
 
 	public FileFilterRuleSet setBaseDir(String baseDir) {
 		if(baseDir != null && !"".equals(baseDir)) {
 			this.baseDir = FileSupport.convertToUnixStylePath(baseDir);
 			if(!this.baseDir.endsWith("/")) {
 				this.baseDir += "/";
 			}
 		}
 		return this;
 	}
 
	public String getBaseDir() {
		return baseDir;
	}

 	@Override
 	public FileFilterRuleSet clone() {
 		return new FileFilterRuleSet(includeFilesWithNameMask, excludeFilesWithNameMask, includeFilesContainingText, excludeFilesContainingText, baseDir);
 	}
 	
 
     public String toString() {
         return "file filter:\n" +
 				"base directory: " + baseDir + "\n" +
                 "include names: " + includeFilesWithNameMask + "\n" +
                 "include lines containing: " + ArraySupport.format("\"", "\"", includeFilesContainingText, ",") + "\n" +
                 "exclude names: " + ArraySupport.format("\"", "\"", excludeFilesContainingText, ", ") + "\n" +
                 "exclude lines containing: " + ArraySupport.format("\"", "\"", excludeFilesContainingText, ", ") + "\n";
     }
 	
 
 }
