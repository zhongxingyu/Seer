 package com.harcourtprogramming.utils;
 
 import java.io.File;
 
 /**
  * TODO: Document FileUtils
  * File Utility functions
  * @author Benedict Harcourt
  */
 public final class FileUtils
 {
 
 	/**
 	 * TODO: Document relativePath(File, File)
 	 * @param base
 	 * @param target
 	 * @return
 	 */
 	public static String realtivePath(File base, File target)
 	{
 		final String[] fromPath = explodedPath(
 		        base.isDirectory() ? base.getParentFile() : base);
 		final String[] toPath = explodedPath(target);
 
 		final int common;
 		int i;
 
 		for (i = 0; i < Math.min(fromPath.length, toPath.length); i++)
 		{
 			if (!fromPath[i].equals(toPath[i]))
 			{
 				break;
 			}
 		}
 
 		common = i;
 
 		if (i == 0)
 		{
 			return target.getPath();
 		}
 
 		StringBuilder ret = new StringBuilder();
 
		for (i = common; i < fromPath.length; i++)
 		{
 			ret.append("..");
 			ret.append(File.separatorChar);
 		}
 
		for (i = common; i < toPath.length; i++)
 		{
 			ret.append(toPath[i]);
 			ret.append(File.separatorChar);
 		}
 
 		ret.deleteCharAt(ret.length() - 1);
 		return ret.toString();
 
 	}
 
 	/**
 	 * TODO: Document explodedPath(File)
 	 * @param f
 	 * @return
 	 */
 	public static String[] explodedPath(File f)
 	{
 		/*
 		 * Java's String .split method uses Regex at an underlying level.
 		 * This means the \ is a reserved character, and needs to be escaped.
 		 * Damn you, Java, damn you.
 		 */
 		if (File.separatorChar == '\\')
 		{
 			return f.getPath().split("\\\\");
 		}
 		return f.getPath().split(File.separator);
 	}
 
 	/**
 	 * TODO: Document FileUtils()
 	 */
 	private FileUtils()
 	{
 		// Nothing to see here. Move along, citizen.
 	}
 }
