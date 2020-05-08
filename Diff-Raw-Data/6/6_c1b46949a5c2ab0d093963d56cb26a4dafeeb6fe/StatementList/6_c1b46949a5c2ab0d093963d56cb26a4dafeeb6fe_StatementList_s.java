 package org.akquinet.httpd.syntax;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.akquinet.audit.ui.UserCommunicator;
 import org.akquinet.httpd.ConfigFile;
 import org.akquinet.httpd.MultipleMarkerInputStream;
 
 public class StatementList extends SyntaxElement
 {
 	public List<Statement> _statements;
 
 	public StatementList(SyntaxElement parent) throws IOException
 	{
 		super(parent);
 		_statements = new LinkedList<Statement>();
 		parse();
 	}
 
 	public StatementList(SyntaxElement parent, MultipleMarkerInputStream text) throws IOException
 	{
 		super(parent, text);
 		_statements = new LinkedList<Statement>();
 		parse();
 	}
 	
 	public Context getContext()
 	{
 		//an Expression is always child of a Context or child of null
 		if(getParent() == null)
 		{
 			return null;
 		}
 		else
 		{
 			return (Context)getParent();
 		}
 	}
 
 	protected void parse() throws IOException
 	{
 		try
 		{
 			while (true)
 			{
 				eatBlanks();
 				switch (getActualChar())
 				{
 				case '#':
 					_statements.add(new Comment(getParent()));
 					break;
 
 				case '<':
 					char n = ' '; // dummy
 					try
 					{
 						n = lookForward();
 					}
 					catch (FileEndException e)
 					{
 						throw new RuntimeException(UNEXPECTED_EOF_ERROR_STRING);
 					}
 
 					if (n == '/')
 					{
 						return; // the ending sequence of a Context object is
 								// handled by itself
 					}
 					else
 					{
 						_statements.add(new Context(getParent()));
 					}
 					break;
 
 				case '\r':
 					if (lookForward() == '\n') // I don't catch FileEndException
 												// exceptions right here because
 												// I treat a '\r' like not being
 												// the
 					{
 						readNextChar();
 						readNextChar();
 					}
 					break;
 
 				case '\n':
 					readNextChar();
 					break;
 
 				default:
 					Directive dir = new Directive(getParent());
 					_statements.add(dir);
 					handleSpecialDirectives(dir);
 					break;
 				} // switch
 			} // while
 		} // try
 		catch (FileEndException e)
 		{
 			// this is ok and expected
 			return;
 		}
 	}
 
 	private void handleSpecialDirectives(Directive dir)
 	{
 		if(dir.getName().equalsIgnoreCase("Include"))
 		{
 			try
 			{
 				List<Statement> stats = include(getParent(), dir.getValue().trim());
 				_statements.addAll(stats);
 			}
 			catch (ServerRootNotSetException e)
 			{
				UserCommunicator.getDefault().reportError("There are problems handling an Include directive: You haven't specified the ServerRoot directory but use relative paths.");
 			}
 		}
 		else if(dir.getName().equalsIgnoreCase("ServerRoot"))
 		{
 			setServerRoot(dir.getValue().trim());
 		}
 	}
 
 	private List<Statement> include(SyntaxElement parent, String fnmatchPath) throws ServerRootNotSetException
 	{
 		String serverRoot = getServerRoot();
 		List<File> filesToDo = filesToInclude(serverRoot , fnmatchPath);
 		List<Statement> ret = new LinkedList<Statement>();
 		
 		for (File file : filesToDo)
 		{
 			ConfigFile conf;
 			try
 			{
 				conf = new ConfigFile(file, serverRoot);
 			}
 			catch (IOException e)
 			{
 				throw new RuntimeException(e);
 			}
 			ret.addAll(conf.getHeadExpression().getStatements()._statements);
 		}
 		
 		return ret;
 	}
 
 	private static List<File> filesToInclude(String serverRoot, String fnmatchPath) throws ServerRootNotSetException
 	{
 		if(!fnmatchPath.startsWith("/") && serverRoot == null)
 		{
 			throw new ServerRootNotSetException();
 		}
 		String[] pathParts = fnmatchPath.split("/");
 		
 		return recursiveFilesToInclude(fnmatchPath.startsWith("/") ? new File("/") : new File(serverRoot), pathParts);
 	}
 
 	private static List<File> recursiveFilesToInclude(File prepend, String[] pathParts)
 	{
 		try
 		{
 			if(pathParts.length == 0)
 			{
 				if(prepend.isFile())
 				{
 					List<File> ret = new LinkedList<File>();
 					ret.add(prepend);
 					return ret;
 				}
 				
 				return recursiveIncludeDirectory(prepend);
 			}
 			else
 			{
 				if(pathParts[0].equals(""))	//jump over empty path parts (i.e. 2 / directly following each other
 				{
 					String[] tmp = new String[pathParts.length-1];
 					for(int i = 1; i < pathParts.length; ++i)
 					{
 						tmp[i-1] = pathParts[i];
 					}
 					return recursiveFilesToInclude(prepend, tmp);
 				}
 				else if(pathParts[0].contains("*"))
 				{
 					StringBuffer regexBuf = new StringBuffer(pathParts[0]);
 					for(int i = 0; i < regexBuf.length(); ++i)
 					{
 						if(regexBuf.charAt(i) == '*')
 						{
 							regexBuf.insert(i++, '.');
 						}
 						else if(regexBuf.charAt(i) == '.')
 						{
 							regexBuf.insert(i++, '\\');
 						}
 					}
 					
 					final String regex = regexBuf.toString();
 					File[] list = prepend.listFiles(new FileFilter()
 					{
 						@Override
 						public boolean accept(File arg0)
 						{
 							return arg0.getName().matches(regex);
 						}
 					});
 					
 					List<File> ret = new LinkedList<File>();
 					
 					for (File file : list)
 					{
 						String[] tmp = new String[pathParts.length-1];
 						for(int i = 1; i < pathParts.length; ++i)
 						{
 							tmp[i-1] = pathParts[i];
 						}
 						ret.addAll(recursiveFilesToInclude(file, tmp));
 					}
 					
 					return ret;
 				}
 				else
 				{
 					String[] tmp = new String[pathParts.length-1];
 					for(int i = 1; i < pathParts.length; ++i)
 					{
 						tmp[i-1] = pathParts[i];
 					}
 					return recursiveFilesToInclude(new File(prepend.getCanonicalPath() + File.separator + pathParts[0]), tmp);
 				}
 			}
 		}
 		catch (IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 	private static List<File> recursiveIncludeDirectory(File prepend)
 	{
 		List<File> ret = new LinkedList<File>();
 		File[] content = prepend.listFiles();
 		
 		for (File file : content)
 		{
 			if(file.isFile())
 			{
 				ret.add(file);
 			}
 			else if(file.isDirectory())
 			{
 				ret.addAll(recursiveIncludeDirectory(file));
 			}
 		}
 		
 		return ret;
 	}
 
 	public List<Directive> getDirective(String name)
 	{
 		List<Directive> ret = new LinkedList<Directive>();
 		for (Statement statement : _statements)
 		{
 			if(! (statement instanceof Context) )
 			{
 				ret.addAll( statement.getDirective(name) );
 			}
 		}
 		return ret;
 	}
 
 	public List<Directive> getDirectiveIgnoreCase(String name)
 	{
 		List<Directive> ret = new LinkedList<Directive>();
 		for (Statement statement : _statements)
 		{
 			if(! (statement instanceof Context) )
 			{
 				ret.addAll( statement.getDirectiveIgnoreCase(name) );
 			}
 		}
 		return ret;
 	}
 
 	public List<Directive> getAllDirectives(String name)
 	{
 		List<Directive> ret = new LinkedList<Directive>();
 		for (Statement statement : _statements)
 		{
 			ret.addAll( statement.getAllDirectives(name) );
 		}
 		return ret;
 	}
 	
 	public List<Directive> getAllDirectivesIgnoreCase(String name)
 	{
 		List<Directive> ret = new LinkedList<Directive>();
 		for (Statement statement : _statements)
 		{
 			ret.addAll( statement.getAllDirectivesIgnoreCase(name) );
 		}
 		return ret;
 	}
 	
 	private static class ServerRootNotSetException extends Exception
 	{
 		private static final long serialVersionUID = -8095933665345794676L;
 	}
 }
