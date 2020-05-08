 package org.programmingteam;
 
 import java.io.File;
 
 ///
 /// \brief Set of helper methods, most of them is used to manipulate paths and filenames
 ///
 public class Helpers
 {
 	///
 	/// \brief checks whether file should be placed in CiCompile region
 	/// \param String file to check
 	/// \param String compile file extensions to search
 	/// \return true if file is CiCompile
 	///
 	public static boolean isCompile(File file, String compileExt)
 	{
		String ext = getFileExt(file.getAbsolutePath());
 		if( compileExt.matches("(^|.*,)"+ext+"(,.*|$)"))
 			return true;
 		else
 			return false;
 	}
 	
 	///
 	/// \brief checks whether file should be placed in CiInclude region
 	/// \param String file to check
 	/// \param String include file extensions to search in
 	/// \return true if file is CiInclude
 	///
 	public static boolean isInclude(File file, String incExt)
 	{
		String ext = getFileExt(file.getAbsolutePath());
 		if(incExt.matches("(^|.*,)"+ext+"(,.*|$)"))
 			return true;
 		else
 			return false;
 	}
 	
 	///
 	/// \brief checks whether path is absolute or not (work only with windows)
 	/// \param String path to check
 	/// \return true if path is absolute
 	///
 	public static boolean isAbsolute(String path)
 	{
 		if(path.length()>1 && path.charAt(1)==':')
 			return true;
 		else
 			return false;
 	}
 	
 	///
 	/// \brief returns file extension
 	/// \param String file
 	/// \reutrn String extension of file
 	///
 	public static String getFileExt(String file)
 	{
 		int index = file.lastIndexOf('.');
 		if(index!=-1 && index<file.length())
 		{
 			return file.substring(index+1, file.length());
 		}
 		else
 		{
 			return "";
 		}
 	}
 	
 	///
 	/// \brief returns path part of file
 	/// 
 	public static String getPath(String file)
 	{
 		int i;
 		if( (i=file.lastIndexOf("\\")) >1 )
 			return file.substring(0,  i+1);
 		else return null;
 	}
 	
 	///
 	/// \biref resolves path
 	/// There are three steps:
 	/// 	1. Resolve env vars (if certain var is not found in system, app exits)
 	///		2. Fix slashes (remove duplicates, convert to backslashes)
 	/// 	3. Check if file is absolute, if not, glue it with basedir
 	/// 
 	/// \param String basedif  	path to files basedir
 	/// \param String path 		path to be resolved
 	/// \return String resolved path (always absolute)
 	///
 	public static String resolvePath(String basedir, String path)
 	{
 		String resolved = fixSlashes(resolveEnvVars(path));
 		if(isAbsolute(resolved))
 			return resolved;
 		else
 			return (basedir + File.separatorChar + resolved);
 	}
 	
 	///
 	/// \brief removes duplicate slashes, convert to backslashes
 	/// \param String path to fix
 	/// \return String fixed path
 	///
 	public static String fixSlashes(String file)
 	{
 		String output = file.replace('/','\\');
 		output = output.replace("\\\\","\\");
 		output = output.replace("\\\\","\\");
 		//Log.d(output);
 		
 		if(output.charAt(0)=='\\') 
 			output=output.substring(1, output.length());
 		if(output.charAt(output.length()-1) == '\\')
 			output=output.substring(0, output.length()-1);
 		return output;
 	}
 	
 	///
 	/// \brief removes slashes at begining and at the end of string
 	/// \param String to parse
 	/// \return stripped string
 	///
 	public static String stripSlashes(String file)
 	{
 		String output = file;
 		while(output.charAt(0)=='\\')
 			output = output.substring(1, output.length());
 		
 		while(output.charAt(output.length()-1)=='\\')
 			output = output.substring(0, output.length()-1);
 		
 		return output;
 	}
 	
 	///
 	/// \brief resolves env variables
 	/// If variable is not found, exits with code -1
 	/// \param String path to resolve
 	/// \return String resolved path
 	///
 	public static String resolveEnvVars(String file)
 	{
 		StringBuffer varBuff = null;
 		StringBuffer outBuff = new StringBuffer();
 		String output = null;
 		boolean flgStarted = false;
 		for(int i=0; i<file.length(); ++i)
 		{
 			if(file.charAt(i)=='$' && file.charAt(i+1)=='{')
 			{
 				varBuff = new StringBuffer();
 				flgStarted = true;
 				++i;
 				continue;
 			}
 			
 			if(file.charAt(i)=='}')
 			{
 				String varToResolve = varBuff.toString();
 				String resolved = System.getenv(varToResolve);
 				if(resolved == null)
 				{
 					Log.e("Could not resolve environment variable: ${" + varToResolve + "}");
 					System.exit(-1);
 				}
 				
 				outBuff.append(resolved);
 				flgStarted = false;
 				continue;
 			}
 			
 			if(flgStarted)
 			{
 				varBuff.append(file.charAt(i));
 				continue;
 			}
 			
 			if(!flgStarted)
 			{
 				outBuff.append(file.charAt(i));
 			}
 		}
 		
 		output = outBuff.toString();
 		return output;
 	}
 
 	///
 	/// \brief count occurances of char in string
 	/// \param String 	to search in
 	/// \param char 	to search for
 	/// \return int number of occurances
 	///
 	public static int countOccurances(String str, char what)
 	{
 		int occurances = 0;
 		for(int i=0; i<str.length(); ++i)
 		{
 			if(str.charAt(i)==what) ++occurances;
 		}
 		return occurances;
 	}
 	
 	public static boolean compFiles(String file1, String file2)
 	{
 		File f1 = new File(file1);
 		File f2 = new File(file2);
 		if(f1.getName()==null || f2.getName()==null) 
 			return false;
 		else
 			return f1.getName().equals(f2.getName());
 	}
 }
