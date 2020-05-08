 /**
  * jpp — Bash based preprocessor for Java
  * 
  * Copyright © 2012  Mattias Andrée (maandree+jpp@kth.se)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package jpp;
 
 import java.util.*;
 import java.io.*;
 
 
 /**
  * This is the mane class
  * 
  * @author  Mattias Andrée, <a href="mailto:maandree+jpp@kth.se">maandree+jpp@kth.se</a>
  */
 public class Program
 {
     /**
      * This is the mane entry point of the program
      * 
      * @param  args  Command line arguments
      */
     public static void main(final String... args)
     {
 	String linger = null;
 	boolean dashed = false;
 	
 	final ArrayList<String>   vars = new ArrayList<String>();
 	final ArrayList<String>  files = new ArrayList<String>();
 	final HashSet<String>  fileSet = new   HashSet<String>();
 	String oFlag = null;
 	
 	for (final String arg : args)
 	    if (linger == null)
 		if (dashed || ((arg.startsWith("-") || arg.startsWith("+")) == false))
 		{   if (fileSet.contains(arg))
 		    {   err(1, "fatal", arg, "0", "File does not exist");
 			System.exit(-1); return;
 		    }
 		    files.add(arg);
 		    fileSet.add(arg);
 		}
 		else if (arg.equals("--"))
 		    dashed = true;
 		else if (arg.startsWith("-D") && ! arg.equals("-D"))
 		    vars.add(arg.substring(2));
 		else
 		    linger = arg;
 	    else
 	    {   if (linger.equals("-o") || linger.equals("--out") || linger.equals("--output"))
 		    if (oFlag == null)
 			oFlag = arg;
 		    else
 		    {   err(1, "fatal", null, null, "Duplicate option: -o");
 			System.exit(-1); return;
 		    }
 		else if (linger.equals("-D") || linger.equals("--export"))
 		    vars.add(arg);
 		else
 		    err(3, "warning", null, null, "Unrecognised option: " + linger);
 		
 		linger = null;
 	    }
 	
 	if ((linger != null) || (oFlag  == null))
 	{
 	    if (linger != null)  err(1, "fatal", null, null, "Lingering flag argument: " + linger);
 	    if (oFlag  == null)  err(1, "fatal", null, null, "Unspecified option: -o");
 	    System.exit(-1); return;
 	}
 	
 	try
 	{   if ((new File(oFlag)).exists() == false)
 		(new File(oFlag)).mkdirs();
 	    else if ((new File(oFlag)).isDirectory() == false)
 	    {   err(1, "fatal", null, null, oFlag + " exists but is not a directory");
 		System.exit(-1); return;
 	}   }
 	catch (final Throwable err)
 	{   err(1, "error", null, null, err.toString());
 	    System.exit(-2); return;
 	}
 	
 	try
         {   for (final String file : files)
 	    {
 		String _file = file;
 		if (_file.toLowerCase().endsWith(".jpp"))
 		    _file = _file.substring(0, _file.length() - 4) + ".java";
 		_file = (new File(oFlag, _file)).getAbsolutePath();
 		(new File(_file)).getParentFile().mkdirs();
 		process(file, _file, vars); /* Do *NOT* use canonical path */
 		postprocess(_file, _file);
 	    }
 	}
 	catch (final Throwable err)
 	{   err(1, "error", null, null, err.toString());
 	    err.printStackTrace(System.err);
 	    System.exit(-2); return;
 	}
     }
     
     
     /**
      * Prints and error message to stderr
      * 
      * @param  colour       ANSI colour index of error type
      * @param  type         Error type
      * @param  file         The file where the error occoured, may be {@code null}
      * @param  location     The location in the file where the error occoured, may be {@code null}
      * @param  description  The error description
      */
     static void err(final int colour, final String type, final String file, final String location, final String description)
     {
 	final String _type        = type        == null ? null : type       .replace("\033", "\033[2m\\033\033[22m");
 	final String _file        = file        == null ? null : file       .replace("\033", "\033[2m\\033\033[22m");
 	final String _location    = location    == null ? null : location   .replace("\033", "\033[2m\\033\033[22m");
 	final String _description = description == null ? null : description.replace("\033", "\033[2m\\033\033[22m");
 	final String ucs = "\033[0;1;3" + colour + "mjpp\033[36m:\033[3" + colour + "m" + _type + "\033[36m:\033[21;39m"
 	                 + (_file == null ? "" : "\033[35m" + _file + "\033[36m:\033[39m")
 	                 + (_location == null ? "" : "\033[32m" + _location.replace(":", "\033[36m:\033[32m") + "\033[36m:\033[39m")
 	                 + _description.replace(":", "\033[36m:\033[34m") + "\033[0m";
 	final StringBuilder ascii = new StringBuilder();
 	try
 	{   for (final byte b : ucs.getBytes("UTF-8"))
 		if ((((b & 128) == 0) && (' ' <= b)) || (b == '\033'))
 		    ascii.append((char)b);
 		else
 		{   byte o = b;
 		    ascii.append("\033[2m\\0");
 		    while (o != 0)
 		    {   ascii.append((char)('0' | (o & 7)));
 			o >>>= 3;
 		    }
 		    ascii.append("\033[22m");
 	}       }
 	catch (final UnsupportedEncodingException err)
 	{   throw new Error(err); //IOError is missing in classpath
 	}
 	
 	System.err.println(ascii.toString());
     }
     
     
     /**
      * Processes a <tt>jpp</tt> file
      * 
      * @param  input   Input <tt>jpp</tt> file
      * @param  output  Output <tt>java</tt> file
      * @param  vars    Variables
      * 
      * @throws  Throwable  In case something is wrong
      */
     public static void process(final String input, final String output, final List<String> vars) throws Throwable
     {
 	Scanner in = null;
         OutputStream out = null;
 	try
 	{
 	    in = new Scanner(new BufferedInputStream(new FileInputStream(input)), "UTF-8");
 	    out = new BufferedOutputStream(new FileOutputStream(output + ".sh"));
 	    
 	    out.write("#!/usr/bin/env bash\n".getBytes("UTF-8"));
 	    out.write("function _() {\n".getBytes("UTF-8"));
 	    for (final String var : vars)
 		out.write((var + "\n").getBytes("UTF-8"));
 	    int lineIndex = 0;
 	    while (in.hasNextLine())
 		try
 		{
 		    lineIndex++;
 		    String line = in.nextLine();
 		    String trim = "";
 		    while (line.startsWith(" ") || line.startsWith("\t"))
 		    {   trim += line.charAt(0);
 			line = line.substring(1);
 		    }
 		    if (line.startsWith("#") && ! line.startsWith("##"))
 			out.write((trim + line.substring(line.startsWith("#!") ? 0 : 1)).getBytes("UTF-8"));
 		    else
 		    {
 			String data = trim + (line.startsWith("##") ? line.substring(1) : line);
			data = data.replace("<\"\">", "//").replace("'", "'\\''").replace("<\"$", "'\"${").replace("$\">", "}\"'");
 			data = "echo '" + lineIndex + " " + data + '\'';
 			out.write(data.getBytes("UTF-8"));
 		    }
 		    out.write('\n');
 		}
 		catch (final Throwable err)
 		{   err(1, "error", null, null, err.toString());
 		    System.exit(-2); return;
 		}
 	    out.write(("}\n_ > '" + output.replace("'", "'\\''") + "'\n").getBytes("UTF-8"));
 	    out.flush();
 	}
 	finally
 	{
 	    if (in != null)
 		try
 		{   in.close();
 		}
 		catch (final Throwable ignore)
 		    { /* ignore */ }
 	    
 	    if (out != null)
 		try
 		{   out.close();
 		}
 		catch (final Throwable ignore)
 		    { /* ignore */ }
 	}
         
 	(new File(output)).getParentFile().mkdirs();
 	
 	String exec = output + ".sh";
 	if (exec.contains("/") == false)
 	    exec = "./" + exec;
         final ProcessBuilder procBuilder = new ProcessBuilder(("bash\0" + exec).split("\0"));
         //procBuilder.inheritIO();
         final Process process = procBuilder.start();
         process.waitFor();
         if (process.exitValue() != 0)
 	{   err(1, "error", input, null, "bash exited with error code: " + process.exitValue());
 	    System.exit(-3); return;
 	}
     }
     
     
     /**
      * Postprocesses a <tt>java</tt> file
      * 
      * @param  input   Input <tt>java</tt> file
      * @param  output  Output <tt>java</tt> file
      * 
      * @throws  Throwable  In case something is wrong
      */
     public static void postprocess(final String input, final String output) throws Throwable
     {
 	final String EMPTY = "";
 	final ArrayList<String> lines = new ArrayList<String>();
 	
 	Scanner in = null;
 	try
 	{
 	    in = new Scanner(new BufferedInputStream(new FileInputStream(input)), "UTF-8");
 	    while (in.hasNextLine())
 	    {
 		final String line = in.nextLine();
 		int sep = line.indexOf(" ");
 		int num = Integer.parseInt(line.substring(0, sep)) - 1;
 		while (lines.size() < num)
 		    lines.add(EMPTY);
 		lines.add(line.substring(sep + 1));
 	    }
 	}
 	finally
 	{
 	    if (in != null)
 		try
 		{   in.close();
 		}
 		catch (final Throwable ignore)
 		    { /* ignore */ }
 	}
 	
         OutputStream out = null;
 	try
 	{
 	    out = new BufferedOutputStream(new FileOutputStream(output));
 	    for (final String line : lines)
 	    {   out.write(line.getBytes("UTF-8"));
 		out.write('\n');
 	    }
 	    out.flush();
 	}
 	finally
 	{
 	    if (out != null)
 		try
 		{   out.close();
 		}
 		catch (final Throwable ignore)
 		    { /* ignore */ }
 	}
     }
     
 }
 
