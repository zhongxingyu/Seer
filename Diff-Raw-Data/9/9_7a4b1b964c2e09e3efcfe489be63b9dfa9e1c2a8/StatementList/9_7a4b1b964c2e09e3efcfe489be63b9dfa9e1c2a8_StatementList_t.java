 package org.akquinet.httpd.syntax;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
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
 					_statements.add(new Directive(getParent()));
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
 }
