 package jcube.core.filter.controller;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.URLConnection;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 
 import jcube.core.configuration.ConfigReaper;
 import jcube.core.exception.LogicException;
 import jcube.core.server.chain.ChainFilter;
 import jcube.core.server.chain.Filter;
 import jcube.core.server.environ.ControllerDispatcher;
 import jcube.core.server.environ.Environ;
 
 public class ServeStaticContent implements ChainFilter
 {
 	protected Boolean stopChaining = false;
 	
 	Environ environ;
 	
 	@Override
	public void filter(Filter filterOptions) throws Exception
 	{
 		
 		ControllerDispatcher controllerDispatcher = environ.getControllerDispatcher();
 		Map<String, ConfigReaper> patterns = new HashMap<String, ConfigReaper>();
 		String url = controllerDispatcher.getUrl();
 		
 		Map<String, ConfigReaper> configReaper = filterOptions.getConfigReaper();
 		if(configReaper.containsKey("applyPatterns"))
 			patterns = configReaper.get("applyPatterns").getMap();
 		
 		if(!patterns.isEmpty())
 		{
 			String[] splitted = url.split("/");
 			String u = "";
 			for(String item : splitted)
 			{
 				if(item.isEmpty())
 					continue;
 				u += "/" + item;
 				if(patterns.containsKey(u))
 				{
 					this.serveStatic(url.replaceAll(u, patterns.get(u).getString()));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * find file in filesystem
 	 * throw exceptions when file is directory or file not found
 	 * @param path
 	 * @throws Exception
 	 */
	private void serveStatic(String path) throws Exception
 	{
 		try
 		{
 			File file = new File(path);
 			if(file.isDirectory())
 				throw new Exception("File directory is not a file.");
 			if(!file.exists())
 				throw new Exception("File not found. You should have set it before this chain");
 			
 			this.responseStatic(path, file);
 		}
 		catch(Exception e)
 		{
 			// TODO: handle exception
			new LogicException(404, e.getMessage());
 		}
 	}
 	
 	/**
 	 * set environ correct response header
 	 * @param path
 	 * @param file
 	 * @throws Exception
 	 */
 	private void responseStatic(String path, File file) throws Exception
 	{
 		String mime_type = URLConnection.guessContentTypeFromName(file.getName());
 
 		Date date = new Date(file.lastModified());
 		
 		Locale.setDefault(Locale.ENGLISH);
 		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
 		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
 		
 		if (file.getName().endsWith(".js")) 
 			mime_type = "application/javascript";
 		else if (file.getName().endsWith(".css")) 
 			mime_type = "text/css";
 		else if (file.getName().endsWith(".htm")) 
 			mime_type = "text/html";
 		else if (file.getName().endsWith(".html")) 
 			mime_type = "text/html";
 		
 		environ.getResponse().addHeader("Last-Modified",dateFormatGmt.format(date));
 		environ.getResponse().addHeader("Content-Type", mime_type);
 		
 		this.getFileContent(file);
 	}
 	
 	/**
 	 * get file content end set it to environ response
 	 * @param file
 	 * @throws Exception
 	 */
 	private void getFileContent(File file) throws Exception
 	{
 		byte[] byteBuffer = new byte[1024];
 		DataInputStream dis = new DataInputStream(new FileInputStream(file));
 		int length;
 		while ((length = dis.read(byteBuffer)) != -1) {
 			environ.getResponse().getPrintStream().write(byteBuffer, 0, length);
 		}
 		dis.close(); 
 		this.stopChaining = true;
 	}
 	@Override
 	public boolean stopChaining()
 	{
 		
 		return this.stopChaining;
 	}
 	
 	@Override
 	public List<String> skipChainsByName()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public String skipExcept()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 }
