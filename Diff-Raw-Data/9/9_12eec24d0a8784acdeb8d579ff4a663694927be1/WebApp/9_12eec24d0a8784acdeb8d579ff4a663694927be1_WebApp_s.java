 package http;
 
 import graphexpr.ExprResolver;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 import latex.TikzReduction;
 
 import org.simpleframework.http.Request;
 import org.simpleframework.http.Response;
 import org.simpleframework.http.core.Container;
 import org.simpleframework.http.core.ContainerServer;
 import org.simpleframework.transport.Server;
 import org.simpleframework.transport.connect.Connection;
 import org.simpleframework.transport.connect.SocketConnection;
 
 import app.SimpleTokenizer;
 
 import com.hp.hpl.jena.rdf.model.Statement;
 
 import pregroup.Parser;
 import pregroup.SimpleType;
 
 import rdf.GraphCompiler;
 import rdf.GraphString;
 import rdf.TypeException;
 import tagging.StanfordTagger;
 import util.InternalException;
 import util.TokenizerException;
 import util.UnknownTagException;
 import xmllexicon.InputException;
 import xmllexicon.SemanticLexicon;
 
 class UserInputError extends Exception
 {
 	private static final long serialVersionUID = 1L;
 	public String what;
 	public UserInputError(String w) { what = w; }
 }
 
 public class WebApp implements Container 
 {
 	private String homePage;
 	private String resultPage;
 	private String thatsMyFault;
 	private String notSupported;
 	private String tokenError;
 			
 	private final static Integer port = 4040;
 	//private String errorPage;
 	
 	private SemanticLexicon sem;
 	private StanfordTagger tagger;
 	
 	public void handle(Request request, Response response)
 	{
 		try
 		{
 			PrintStream body = response.getPrintStream();
 			long time = System.currentTimeMillis();
 			String pageName = request.getPath().getPath();
 			
 			response.setValue("Content-Type", "text/html");
 			response.setValue("Server", "PregroupSPARQL/1.0 (Simple)");
 			response.setDate("Date", time);
 			response.setDate("Last-Modified", time);
 			
 			System.out.println("Requested \""+pageName+"\"");
 			if(pageName.equals("/index.html") || pageName.equals("/"))
 				body.println(homePage);
 			else if(pageName.equals("/process.html"))
 				body.println(processSentence(request.getQuery().get("sentence")));
 			else if(pageName.startsWith("/tmp/img/"))
 			{
 				//! TODO THIS IS A BACKDOOOOOOOOR
 				response.setValue("Content-Type", "image/png");
 				body.write(readBinary("."+pageName));
 			}
 			else
 				body.println("404 Page Not Found\n");
 			body.close();
 		}
 		catch (Exception e)
 		{
 			System.out.println("Exception caught in handle():");
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private String escapeHtml(String input)
 	{
 		String output = new String(input);
 		output = output.replace("&", "&amp;");
 		output = output.replace(">", "&gt;");
 		return output.replace("<", "&lt;");
 	}
 	
 	private String processSentence(String input)
 	{
 		String resPage = new String(resultPage);
 
 		System.out.println("Input sentence: "+input);
 		
 		String messages = "";
 		
 		try
 		{
 			if(input == null)
 			{
 				resPage = resPage.replace("$SENTENCE", "");
 				throw new UserInputError("No input sentence.");
 			}
 			
 			resPage = resPage.replace("$SENTENCE", "Sentence: \""+input+"\"\n<br/>\n");
 			
 			try
 			{
 				List<String> sentence;
 				try
 				{ sentence = new SimpleTokenizer(input).toList();
 				}
 				catch(Exception e)
 				{
 					throw new UserInputError("Unable to tokenize the input sentence."+tokenError);
 				}
 				
 				SimpleType target =
 						new SimpleType("s", 0);
 				
 				List<String> tags = tagger.tagSentence(sentence);
 				
 				messages += "<h5>Tagging</h5>\n"+
 							"<table>\n"+
 								"<tr>\n";
 				for(String word : sentence)
 					messages += "<td>"+escapeHtml(word)+"</td>";
 				messages += "\n</tr>\n"+
 							"<tr>\n";
 				for(String tag : tags)
 					messages += "<td>"+tag+"</td>";
 				messages += "\n</tr>\n"+
 							"</table>\n\n";
 				
 				
 				GraphString phrase;
 				try
 				{ phrase =new  GraphString(sem, tags, sentence, target);
 				} catch(TypeException e)
 				{
 					throw new UserInputError(e.what+thatsMyFault);
 				}
 				
 				Parser p = new Parser(phrase, sem.getComparator());
 				System.out.println(phrase.toString());
 
 				if(p.run())
 				{
 					genImage(TikzReduction.draw(phrase, sentence, p.getReduction()));
 					messages += "<h5>Type reduction</h5>\n"+
 							"<img alt=\"Type reduction\" src=\"tmp/img/output.png\" />\n\n";
 					
 					ExprResolver resolver = new ExprResolver(phrase, p.getReduction());
 					GraphCompiler compiler = new GraphCompiler(resolver);
 	
 					try {
 						Statement res = compiler.compileStmt(phrase.getPattern(resolver.getEntryPoint()));
 						compiler.assume(res);
 
 						String graph = compiler.dumpTriples();
 						messages += "<h5>Graph</h5>\n"+
 								    "<pre>\n"+
 								    escapeHtml(graph)+
 								    "\n</pre>\n\n";
 					} catch (TypeException e) {
						throw new UserInputError("Type exception in the grammar:<br/>"+e.what);
 					}
 						
 				}
 				else
					throw new UserInputError("Incorrect sentence.");
 			}
 			catch(UnknownTagException e)
 			{
 				throw new UserInputError("Unknown tag: "+e.what+notSupported);
 			}
 		}
 		catch(UserInputError e)
 		{
 			messages += "<h5>Error</h5>\n"+e.what+"\n\n";
 		}
 		catch(Exception e)
 		{
 			System.out.println("Exception caught in processSentence():");
 			e.printStackTrace();
 		}
 
 		resPage = resPage.replace("$OUTPUT", messages);
 		return resPage;
 	}
 	
 	private String genImage(String tikzCode)
 	{
 		try
 		{
 			PrintWriter out = new PrintWriter("tmp/output.tex");
 			out.println(tikzCode);
 			out.close();
 			
 			List<String> pdflatex = new ArrayList<String>();
 			pdflatex.add("pdflatex");
 			pdflatex.add("-halt-on-error");
 			pdflatex.add("standalone.tex");
 			
 			ProcessBuilder builder = new ProcessBuilder(pdflatex);
 			builder.directory(new File("tmp"));
 			Process p = builder.start();
 			p.waitFor();
 			
 			List<String> convert = new ArrayList<String>();
 			convert.add("convert");
 			convert.add("-density");
 			convert.add("150");
 			convert.add("standalone.pdf");
 			convert.add("img/output.png");
 			
 			builder = new ProcessBuilder(convert);
 			builder.directory(new File("tmp"));
 			p = builder.start();
 			p.waitFor();
 			//Runtime.getRuntime().exec("convert -density 300 tmp/standalone.pdf tmp/standalone.png");
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 		catch(InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 		
 		return "";
 	}
 	
 	public WebApp() throws InternalException
 	{
 		super();
 		try
 		{
 			homePage = readFile("www/index.html");
 			thatsMyFault = readFile("www/thatsMyFault.html");
 			notSupported = readFile("www/notSupported.html");
 			tokenError = readFile("www/tokenError.html");
 			resultPage = readFile("www/process.html");
 		}
 		catch(IOException e)
 		{
 			homePage = "Error while loading the web pages.";
 		}
 		
 		org.apache.jena.atlas.logging.Log.setLog4j();
 		
 		sem = new SemanticLexicon();
 
 		sem.load("semantics.xml");
 		
 		tagger = new StanfordTagger();
 		tagger.load("taggers/english-left3words-distsim.tagger");
 	}
 	
 	public static void main(String[] list) throws Exception
 	{
 		try {
 	      Container container = new WebApp();
 	      Server server = new ContainerServer(container);
 	      Connection connection = new SocketConnection(server);
 	      SocketAddress address = new InetSocketAddress(port);
 
 	      connection.connect(address);
 		} catch(InternalException e)
 		{
 			System.err.println(e.what);
 		}
 	}
 
 	private String readFile( String file ) throws IOException {
 	    BufferedReader reader = new BufferedReader( new FileReader (file));
 	    String         line = null;
 	    StringBuilder  stringBuilder = new StringBuilder();
 	    String         ls = System.getProperty("line.separator");
 
 	    while( ( line = reader.readLine() ) != null ) {
 	        stringBuilder.append( line );
 	        stringBuilder.append( ls );
 	    }
 	    reader.close();
 	    return stringBuilder.toString();
 	}
 	
 	private byte[] readBinary(String fileName) throws IOException
 	{
 		File file = new File(fileName);
 		byte[] result = new byte[(int)file.length()];
 		
 	    InputStream input = null;
 	    try
 	    {
 		    int totalBytesRead = 0;
 		    input = new BufferedInputStream(new FileInputStream(file));
 		    while(totalBytesRead < result.length)
 		    {
 		        int bytesRemaining = result.length - totalBytesRead;
 		        int bytesRead = input.read(result, totalBytesRead, bytesRemaining); 
 		        if (bytesRead > 0)
 		        {
 		        	totalBytesRead = totalBytesRead + bytesRead;
 		        }
 		    }
 	    }
 	    finally
 	    {
 	    	input.close();
 	    }
 	    
 	    return result;
 	}
 	
 }
