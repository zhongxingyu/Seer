 package com.yiij.base;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.zip.CRC32;
 
 import javax.servlet.ServletException;
 
 import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.yiij.base.ErrorHandler.ExceptionEvent;
 import com.yiij.base.Event.Listener;
 import com.yiij.base.Event.ListenersList;
 import com.yiij.base.interfaces.IContext;
 import com.yiij.web.HttpRequest;
 
 public abstract class Application extends Module
 {
 	private String _id;
 	private String _name = "My Application";
 	private String _basePath;
 	private String _runtimePath;
 	private String _language;
 	private String _sourceLanguage = "en_us";
 	private String _charset = "UTF-8";
 	private boolean _debugMode = false;
 	private Map<String, String> _aliases = new Hashtable<String, String>();
 	
 	private ErrorHandler _errorHandler;
 	
 	private ListenersList _eventList = new ListenersList();
 	
 	private final Logger logger = LoggerFactory.getLogger(Application.class);	
 	
 	public Application(IContext context)
 	{
 		super(context, "", null);
 		
 		context.setApplication(this);
 		logger.trace("Starting YiiJ WebApplication...");
 		
 		registerCoreComponents();
 	}
 
 	/**
 	 * Processes the request.
 	 * This is the place where the actual request processing work is done.
 	 * Derived classes should override this method.
 	 */
 	abstract public void processRequest() throws java.lang.Exception;
 	
 	/**
 	 * Runs the application.
 	 * This method loads static application components. Derived classes usually overrides this
 	 * method to do more application-specific tasks.
 	 * Remember to call the parent implementation so that static application components are loaded.
 	 */
 	public void run() throws IOException, ServletException
 	{
 		_eventList.fire(BeginRequestEventListener.class, new Event(this));
 		try
 		{
 			processRequest();
 		} catch (ServletException e) {
 			// let exceptions meant for the servlet to go through
 			throw e;
 		} catch (EndApplicationException e) {
 			// normal application termination
 			throw e;
 		} catch (java.lang.Exception e) {
 			handleException(e);
 		}
 		_eventList.fire(EndRequestEventListener.class, new Event(this));
 	}
 
 	public void end(int status)
 	{
 		end(status, true);
 	}
 	
 	/**
 	 * Terminates the application.
 	 * This method replaces PHP's exit() function by calling
 	 * {@link onEndRequest} before exiting.
 	 * @param status exit status (value 0 means normal exit while other values mean abnormal exit).
 	 * @param exit whether to exit the current request. This parameter has been available since version 1.1.5.
 	 * It defaults to true, meaning the PHP's exit() function will be called at the end of this method.
 	 */
 	public void end(int status, boolean exit)
 	{
 		_eventList.fire(EndRequestEventListener.class, new Event(this));
 		//if (exit)
 			//throw new EndApplicationException();
 		//if($exit)
 			//exit($status);
 	}
 	
 	/**
 	 * Returns the unique identifier for the application.
 	 * @return string the unique identifier for the application.
 	 */
 	public String getId()
 	{
 		if(_id!=null)
 			return _id;
 		else
 		{
 			CRC32 cs = new CRC32();
 			cs.update((getBasePath()+_name).getBytes());
 			return _id=String.format("%x", cs.getValue());
 		}
 	}
 
 	/**
 	 * Sets the unique identifier for the application.
 	 * @param string $id the unique identifier for the application.
 	 */
 	public void setId(String id)
 	{
 		_id=id;
 	}
 	
 	/**
 	 * Returns the root path of the application.
 	 * @return the root directory of the application. Defaults to 'protected'.
 	 */
 	public String getBasePath()
 	{
 		return _basePath;
 	}
 
 	/**
 	 * Sets the root directory of the application.
 	 * This method can only be invoked at the begin of the constructor.
 	 * @param path the root directory of the application.
 	 * @throws Exception if the directory does not exist.
 	 */
 	public void setBasePath(String path)
 	{
 		_basePath = path;
 		
 		/*
 		if(($this->_basePath=realpath($path))===false || !is_dir($this->_basePath))
 			throw new CException(Yii::t('yii','Application base path "{path}" is not a valid directory.',
 				array('{path}'=>$path)));
         $this->_autoBasePathUrl = null;
         */
 	}
 	
 	/**
 	 * Returns the directory that stores runtime files.
 	 * @return the directory that stores runtime files. Defaults to 'protected/runtime'.
 	 */
 	public String getRuntimePath()
 	{
 		return _runtimePath;
 		/*
 		if(_runtimePath!=null)
 			return _runtimePath;
 		else
 		{
 			setRuntimePath(getBasePath()+"/runtime");
 			return _runtimePath;
 		}
 		*/
 	}
 	
 	/**
 	 * Sets the directory that stores runtime files.
 	 * @param string $path the directory that stores runtime files.
 	 * @throws CException if the directory does not exist or is not writable
 	 */
 	public void setRuntimePath(String path)
 	{
 		_runtimePath = path;
 		
 		/*
 		if(($runtimePath=realpath($path))===false || !is_dir($runtimePath) || !is_writable($runtimePath))
 			throw new CException(Yii::t('yii','Application runtime path "{path}" is not valid. Please make sure it is a directory writable by the Web server process.',
 				array('{path}'=>$path)));
 		$this->_runtimePath=$runtimePath;
 		*/
 	}
 	
 	/**
 	 * Returns the language that the user is using and the application should be targeted to.
 	 * @return string the language that the user is using and the application should be targeted to.
 	 * Defaults to the {@link #getSourceLanguage() source language}.
 	 */
 	public String getLanguage()
 	{
 		return _language==null ? _sourceLanguage : _language;
 	}
 
 	/**
 	 * Specifies which language the application is targeted to.
 	 *
 	 * This is the language that the application displays to end users.
 	 * If set null, it uses the {@link #getSourceLanguage() source language}.
 	 *
 	 * Unless your application needs to support multiple languages, you should always
 	 * set this language to null to maximize the application's performance.
 	 * @param language the user language (e.g. 'en_US', 'zh_CN').
 	 * If it is null, the {@link #getSourceLanguage()} will be used.
 	 */
 	public void setLanguage(String language)
 	{
 		_language=language;
 	}
 
 	public String findLocalizedFile(String srcFile)
 	{
 		return findLocalizedFile(srcFile, null, null);
 	}
 	
 	public String findLocalizedFile(String srcFile, String srcLanguage)
 	{
 		return findLocalizedFile(srcFile, srcLanguage, null);
 	}
 	
 	/**
 	 * Returns the localized version of a specified file.
 	 *
 	 * The searching is based on the specified language code. In particular,
 	 * a file with the same name will be looked for under the subdirectory
 	 * named as the locale ID. For example, given the file "path/to/view.php"
 	 * and locale ID "zh_cn", the localized file will be looked for as
 	 * "path/to/zh_cn/view.php". If the file is not found, the original file
 	 * will be returned.
 	 *
 	 * For consistency, it is recommended that the locale ID is given
 	 * in lower case and in the format of LanguageID_RegionID (e.g. "en_us").
 	 *
 	 * @param srcFile the original file
 	 * @param srcLanguage the language that the original file is in. If null, the application {@link sourceLanguage source language} is used.
 	 * @param language the desired language that the file should be localized to. If null, the {@link getLanguage application language} will be used.
 	 * @return the matching localized file. The original file is returned if no localized version is found
 	 * or if source language is the same as the desired language.
 	 */
 	public String findLocalizedFile(String srcFile, String srcLanguage, String language)
 	{
 		if(srcLanguage==null)
 			srcLanguage=_sourceLanguage;
 		if(language==null)
 			language=getLanguage();
 		if(language.equals(srcLanguage))
 			return srcFile;
 		
 		File sFile = new File(srcFile);
 		File desiredFile = new File(sFile.getPath()+"/"+language+"/"+sFile.getName());
 		return Object.class.getResource(desiredFile.getAbsolutePath()) != null ? desiredFile.getAbsolutePath() : srcFile;
 	}
 	
 	
 	/**
 	 * @return the application name. Defaults to 'My Application'.
 	 */
 	public String getName()
 	{
 		return _name;
 	}
 
 	/**
 	 * @param the application name. Defaults to 'My Application'.
 	 */
 	public void setName(String name)
 	{
 		_name=name;
 	}
 	
 	
 	/**
 	 * @return the charset currently used for the application. Defaults to 'UTF-8'.
 	 */
 	public String getCharset()
 	{
 		return  _charset;
 	}
 
 	/**
 	 * @param charset the charset to be used for the application. Defaults to 'UTF-8'.
 	 */
 	public void setCharset(String charset)
 	{
 		_charset=charset;
 	}
 	
 	/**
 	 * the language that the application is written in. This mainly refers to
 	 * the language that the messages and view files are in. Defaults to 'en_us' (US English).
 	 * @return
 	 */
 	public String getSourceLanguage()
 	{
 		return  _sourceLanguage;
 	}
 
 	/**
 	 * the language that the application is written in. This mainly refers to
 	 * the language that the messages and view files are in. Defaults to 'en_us' (US English).
 	 * @param language
 	 */
 	public void setSourceLanguage(String language)
 	{
 		_sourceLanguage=language;
 	}
 	
	public boolean isDebugMode()
 	{
 		return _debugMode;
 	}
 	
 	public void setDebugMode(boolean value)
 	{
 		_debugMode = value;
 	}
 	
 	abstract public PrintWriter out() throws IOException;
 	
 	//public function findLocalizedFile($srcFile,$srcLanguage=null,$language=null)
 	
 	/**
 	 * Handles uncaught Java exceptions.
 	 *
 	 * This method will first raise an {@link #onException} event.
 	 * If the exception is not handled by any event handler, it will call
 	 * {@link #getErrorHandler()} to process the exception.
 	 *
 	 * The application will be terminated by this method.
 	 *
 	 * @param exception that is not caught
 	 */
 	public void handleException(java.lang.Exception exception) 
 		throws ServletException
 	{
		String message = ExceptionUtils.getRootCauseMessage(exception);
 		
 		logger.error(message);
 		try
 		{
 			ExceptionEvent event=new ExceptionEvent(this,exception);
 			//$this->onException($event);
 			if(!event.getHandled())
 			{
 				// try an error handler
 				ErrorHandler handler;
 				if((handler=getErrorHandler())!=null)
 					handler.handle(event);
 				else
 					displayException(exception);
 			}
 		}
 		catch(java.lang.Exception e)
 		{
 			displayException(e);
 		}
 
 		try
 		{
 			//end(1);
 		}
 		catch(java.lang.Exception e)
 		{
 			// use the most primitive way to log error
 			/*
 			$msg = get_class($e).': '.$e->getMessage().' ('.$e->getFile().':'.$e->getLine().")\n";
 			$msg .= $e->getTraceAsString()."\n";
 			$msg .= "Previous exception:\n";
 			$msg .= get_class($exception).': '.$exception->getMessage().' ('.$exception->getFile().':'.$exception->getLine().")\n";
 			$msg .= $exception->getTraceAsString()."\n";
 			$msg .= '$_SERVER='.var_export($_SERVER,true);
 			error_log($msg);
 			exit(1);
 			*/
 			System.out.println("Could not catch all exceptions, fall back on exception: "+e.getMessage());
 		}
 		
 	}
 	
 	/**
 	 * Displays the uncaught PHP exception.
 	 * This method displays the exception in HTML when there is
 	 * no active error handler.
 	 * @param exception the uncaught exception
 	 */
 	public void displayException(java.lang.Exception exception)
 	{
 		try
 		{
 			if(_debugMode)
 			{
 				StackTraceElement trace = exception.getStackTrace()[0];
 				
 				out().println("<h1>"+exception.getClass().getCanonicalName()+"</h1>");
 				out().print("<p>"+exception.getMessage()+" ("+trace.getFileName()+":"+trace.getLineNumber()+")</p>");
 				exception.printStackTrace(out());
 			}
 			else
 			{
 				out().println("<h1>"+exception.getClass().getCanonicalName()+"</h1>");
 				out().print("<p>"+exception.getMessage()+"</p>");
 			}
 		} catch(IOException e) {
 			logger.warn("Error on displayException: "+e.getMessage());
 		}
 	}
 	
 	/**
 	 * Registers the core application components.
 	 * @see #setComponents()
 	 */
 	protected void registerCoreComponents()
 	{
 		ComponentConfig config = new ComponentConfig();
 
 		config.put("errorHandler", new ComponentConfig("com.yiij.base.ErrorHandler"));
 		
 		setComponents(config);
 	}
 	
 	/**
 	 * Returns the error handler component.
 	 * 
 	 * @return the error handler component
 	 */
 	public ErrorHandler getErrorHandler()
 	{
 		if (_errorHandler == null)
 			try
 			{
 				_errorHandler = (ErrorHandler) getComponent("errorHandler");
 			} catch (java.lang.Exception e)
 			{
 				logger.error("errorHandler: Should never happen");
 				_errorHandler = null; // should never happen
 			}
 		return _errorHandler;
 	}
 	
 	
 	public String getPathOfAlias(String alias)
 	{
 		int pos;
 		if (_aliases.containsKey(alias))
 			return _aliases.get(alias);
 		else if ((pos=alias.indexOf("."))!=-1)
 		{
 			String rootAlias = alias.substring(0, pos);
 			if (_aliases.containsKey(rootAlias))
 			{
 				String apath = StringUtils.stripEnd(_aliases.get(rootAlias)+"/"+alias.substring(pos+1).replaceAll("\\.", "/"), "*/");
 				_aliases.put(alias, apath);
 				return apath;
 			}
 		}
 		return null;
 	}
 
 	public void setPathOfAlias(String alias, String value)
 	{
 		if (value == null)
 			_aliases.remove(alias);
 		else
 			_aliases.put(alias, StringUtils.stripEnd(value, "\\/"));
 	}
 
 	
 	//
 	// EVENT HANDLING
 	//
 	
 	// BEGIN REQUEST
 	public void addBeginRequestEventListener(BeginRequestEventListener listener)
 	{
 		_eventList.add(BeginRequestEventListener.class, listener);
 	}
 
 	public void removeBeginRequestEventListener(BeginRequestEventListener listener)
 	{
 		_eventList.remove(BeginRequestEventListener.class, listener);
 	}
 	
 	public static abstract class BeginRequestEventListener implements Listener
 	{
 		public BeginRequestEventListener()
 		{
 			super();
 		}
 	}
 	
 	// END REQUEST
 	public void addEndRequestEventListener(EndRequestEventListener listener)
 	{
 		_eventList.add(EndRequestEventListener.class, listener);
 	}
 
 	public void removeEndRequestEventListener(EndRequestEventListener listener)
 	{
 		_eventList.remove(EndRequestEventListener.class, listener);
 	}
 	
 	public static abstract class EndRequestEventListener implements Listener
 	{
 		public EndRequestEventListener()
 		{
 			super();
 		}
 	}
 	
 }
