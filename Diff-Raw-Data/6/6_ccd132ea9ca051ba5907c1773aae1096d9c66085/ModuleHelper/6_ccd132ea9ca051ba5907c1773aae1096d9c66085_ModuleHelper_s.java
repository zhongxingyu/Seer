 package org.akquinet.audit;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import org.akquinet.httpd.ConfigFile;
 import org.akquinet.httpd.syntax.Directive;
 
 public class ModuleHelper
 {
 	private ConfigFile _conf;
 	private ProcessBuilder _httpd;
 	
 	public ModuleHelper(ConfigFile conf)
 	{
 		_conf = conf;
 		_httpd = null;
 	}
 	
 	public ModuleHelper(ConfigFile conf, File apacheExecutable)
 	{
 		_conf = conf;
 		
 		try
 		{
 			_httpd = new ProcessBuilder(apacheExecutable.getCanonicalPath(), "-l");
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	protected List<Directive> getLoadModuleList()
 	{
 		return _conf.getDirectiveIgnoreCase("LoadModule");
 	}
 	
 	protected List<Directive> getLoadFileList()
 	{
 		return _conf.getDirectiveIgnoreCase("LoadFile");
 	}
 
 	protected String[] getCompiledIntoModulesList()
 	{
 		try
 		{
 			Process p = _httpd.start();
 			InputStream stdErr = p.getErrorStream();
 			
 			StringBuffer buf = new StringBuffer();
 //			int b = stdErr.read();
 			int b;
 			while(stdErr.available() >= 1)
 			{
 				b = stdErr.read();
 				buf.append((char)b);
 			}
			
 			p.waitFor();
 			
 			String output = buf.toString();
			String[] modList = output.split("(\n|\r\n)");
 			return modList;
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
