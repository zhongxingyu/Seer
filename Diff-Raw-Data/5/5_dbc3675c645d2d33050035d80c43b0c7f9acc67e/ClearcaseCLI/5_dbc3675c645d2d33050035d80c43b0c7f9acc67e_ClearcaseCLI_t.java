 package net.sourceforge.eclipseccase;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 import net.sourceforge.eclipseccase.IClearcase.Status;
 
 public class ClearcaseCLI implements IClearcase
 {
 	private String newLine = System.getProperty("line.separator");
 	private String prompt = "Usage: pwd" + newLine;
 	private Process cleartool;
 	private BufferedReader stdout;
 	private BufferedReader stderr;
 	private Writer stdin;
 	private char[] buf = new char[4096];
 
 	public ClearcaseCLI()
 	{
 	}
 
 	public void destroy()
 	{
 		if(isRunning())
 			cleartool.destroy();
 	}
 	
 	private boolean isRunning()
 	{
 		boolean running = false;
 		if (cleartool != null)
 		{
 			try
 			{
 				cleartool.exitValue();
 			}
 			catch (IllegalThreadStateException ex)
 			{
 				running = true;
 			}
 		}
 		return running;
 	}
 
 	private void validateProcess() throws Exception
 	{
 		if (! isRunning())
 		{
 			// Only want to add shutdown hook once per process, and
 			// cleartool is only ever null first time through
 			if (cleartool == null)
 			{
 				Runtime.getRuntime().addShutdownHook(new Thread()
 				{
 					public void run()
 					{
 						try
 						{
 							if (cleartool != null)
 								cleartool.destroy();
 						}
 						catch (Exception ex) {}
 					}
 				});
 			}
 			cleartool = Runtime.getRuntime().exec("cleartool");
 			stdout = new BufferedReader(new InputStreamReader(new BufferedInputStream(cleartool.getInputStream())));
 			stderr = new BufferedReader(new InputStreamReader(new BufferedInputStream(cleartool.getErrorStream())));
 			stdin = new OutputStreamWriter(cleartool.getOutputStream());
 		}
 	}
 
 	private synchronized Status execute(final String cmd)
 	{
 		Status result = null;
 		try
 		{
 			validateProcess();
 			stdin.write(cmd);
 			stdin.write(newLine);
 			stdin.write("pwd -h");
 			stdin.write(newLine);
 			stdin.flush();
 			
 			return readOutput();
 		}
 		catch (Exception ex)
 		{
 			return new Status(false, "Could not execute command due to unexpected exception: " + ex);
 		}
 	}
 
 	private Status readOutput()
 	{
 		int count = 0;
 		StringBuffer out = new StringBuffer();
 		StringBuffer err = new StringBuffer();
 		StringBuffer msg = new StringBuffer();
 		boolean status = true;
 		
 		try
 		{
 			while(true)
 			{
 				// block on read of stdout as we always expect at least the prompt
 				count = stdout.read(buf);
 				out.append(buf, 0, count);
 
 				if (stderr.ready())
 				{
 					count = stderr.read(buf);
 					err.append(buf, 0, count);
 				}
 
 				// Exit when we get the prompt as its always the last thing done,
 				// so we know cmd has finished
 				if (out.toString().endsWith(prompt))
 				{
 					out.delete(out.length() - prompt.length(), out.length());
 					status = true;
 					break;
 				}
 			}
 		}
 		catch (IOException ex)
 		{
 			throw new RuntimeException("IOException while trying to parse cleartool output: " + ex);
 		}
 
 
 		if (out.length() > 0)
 		{
			msg.append(out.toString());
 		}
 		if (err.length() > 0)
 		{
			msg.append(err.toString());
 			status = false;
 		}
 			
 		return new Status(status, msg.toString());
 	}
 
 	private String readStderr()
 	{
 		char[] buf = new char[4096];
 		StringBuffer result = new StringBuffer();
 		try
 		{
 			while (stdout.ready())
 			{
 				int count = stdout.read(buf);
 				result.append(buf, 0, count);
 			}
 		}
 		catch (IOException e)
 		{
 		}
 		return result.toString();
 	}
 
 	private String quote(String file)
 	{
 		return "\"" + file + "\"";
 	}
 	
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#add(String, String, boolean)
 	 */
 	public Status add(String file, String comment, boolean isdirectory)
 	{
 		if (isdirectory)
 			return execute("mkdir -c " + quote(comment) + " " + quote(file));
 		else
 			return execute("mkelem -c " + quote(comment) + " " + quote(file));
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#checkin(String, String, boolean)
 	 */
 	public Status checkin(String file, String comment, boolean ptime)
 	{
 		String ptimeFlag = ptime ? "-ptime " : "";
 		return execute("checkin -c " + quote(comment) + " " + ptimeFlag + quote(file));
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#checkout(String, String, boolean, boolean)
 	 */
 	public Status checkout(
 		String file,
 		String comment,
 		boolean reserved,
 		boolean ptime)
 	{
 		String ptimeFlag = ptime ? "-ptime " : "";
 		String resFlag = reserved ? "-reserved " : "-unreserved ";
 		return execute("checkout -c " + quote(comment) + " " + ptimeFlag + resFlag + quote(file));
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#cleartool(String)
 	 */
 	public Status cleartool(String cmd)
 	{
 		return execute(cmd);
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#delete(String, String)
 	 */
 	public Status delete(String file, String comment)
 	{
 		return execute("rmname -c " + quote(comment) + " " + quote(file));
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#getViewName(String)
 	 */
 	public Status getViewName(String file)
 	{
 		File dir = new File(file);
 		if (! dir.isDirectory())
 			dir = dir.getParentFile();
 		synchronized(this)
 		{
 			Status result = execute("cd " + quote(dir.getPath()));
 			if (result.status)
 				result = execute ("pwv -s");
 			return result;
 		}
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#isCheckedOut(String)
 	 */
 	public boolean isCheckedOut(String file)
 	{
 		Status ret = execute("describe -fmt " + quote("%f") + " " + quote(file));
 		if (ret.status && ret.message.trim().length() > 0)
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#isDifferent(String)
 	 */
 	public boolean isDifferent(String file)
 	{
 		boolean result = false;
 
 		if (isCheckedOut(file))
 		{
 			Status diffResult = execute("diff -pred " + quote(file));
 			if (! diffResult.message.startsWith("Files are identical"))
 				result = true;
 		}
 		return result;
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#isElement(String)
 	 */
 	public boolean isElement(String file)
 	{
 		Status ret = execute("describe -fmt " + quote("%Vn") + " " + quote(file));
 		if (ret.status && ret.message.trim().length() > 0)
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#isHijacked(String)
 	 */
 	public boolean isHijacked(String file)
 	{
 		boolean result = false;
 		Status lsResult = execute("ls " + quote(file));
 		if (lsResult.status && lsResult.message.indexOf("[hijacked]") != -1)
 			result = true;
 		return result;
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#isSnapShot(String)
 	 */
 	public boolean isSnapShot(String file)
 	{
 		File dir = new File(file);
 		if (! dir.isDirectory())
 			dir = dir.getParentFile();
 		synchronized(this)
 		{
 			Status result = execute("cd " + dir.getPath());
 			if (result.status)
 				result = execute ("lsview -cview -properties -full");
 			if (result.status && result.message.indexOf("Properties: snapshot") != -1)
 				return true;
 			else
 				return false;
 		}
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#move(String, String, String)
 	 */
 	public Status move(String file, String newfile, String comment)
 	{
 		return execute("move -c " + quote(comment) + " " + quote(file) + " " + quote(newfile));
 	}
 
 	/**
 	 * @see net.sourceforge.eclipseccase.IClearcase#uncheckout(String, boolean)
 	 */
 	public Status uncheckout(String file, boolean keep)
 	{
 		String flag = keep ? "-keep " : "-rm ";
 		return execute("uncheckout " + flag + quote(file));
 	}
 	
 	/** For testing puposes only */
 	public static void main(String[] args)
 	{
 		if (args.length == 0)
 		{
 			System.out.println("Usage: Clearcase existing_ccase_elt nonexisting_ccase_elt");
 			System.exit(1);
 		}
 		String file = args[0];
 		IClearcase ccase = new ClearcaseCLI();
 		
 		System.out.println("getViewName: " + ccase.getViewName(file).message);
 		System.out.println("isSnapShot: " + ccase.isSnapShot(file));
 		System.out.println("isElement: " + ccase.isElement(file));
 		System.out.println("isCheckedOut: " + ccase.isCheckedOut(file));
 		System.out.println("checkout: " + ccase.checkout(file, "", false, true).message);
 		System.out.println("isCheckedOut: " + ccase.isCheckedOut(file));
 		System.out.println("uncheckout: " + ccase.uncheckout(file, false).message);
 		System.out.println("isCheckedOut: " + ccase.isCheckedOut(file));
 
 		if (args.length > 1)
 		{
 			String newfile = args[1];
 			System.out.println("isElement: " + ccase.isElement(newfile));
 			System.out.println("add: " + ccase.add(newfile, "", false).message);
 			System.out.println("isElement: " + ccase.isElement(newfile));
 			System.out.println("checkin: " + ccase.checkin(newfile, "", true).message);
 			System.out.println("delete: " + ccase.delete(newfile, "").message);
 			System.out.println("isElement: " + ccase.isElement(newfile));
 		}
 	}
 
 }
