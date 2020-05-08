 package edu.ncu.csie.oolab;
 
 import java.lang.reflect.Type;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 import org.python.util.PythonInterpreter;
 import org.sikuli.script.Screen;
 import org.sikuli.script.Settings;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 public class CommandParser {
 
 	public CommandParser() {
 		self = this;
 		this.HASH_TYPE = ( new TypeToken< HashMap< String, Object > >() {
 		} ).getType();
 		this.CHARSET = Charset.forName( "UTF-8" );
 		this.json_ = new Gson();
 		this.commands_ = new HashMap< String, AbstractCommand >();
 		this.python_ = new PythonInterpreter();
 		this.python_.setOut( System.err );
 		this.screen_ = new Screen();
 
 		this.commands_.put( "exit", new ExitCommand() );
 		this.commands_.put( "capture", new CaptureCommand( this.screen_ ) );
 		this.commands_.put( "execute", new ExecuteCommand( this.python_ ) );
 		this.commands_.put( "bundle", new BundleCommand( this.python_ ) );
 
 		this.python_.exec( "from sikuli.Sikuli import *" );
 		this.python_.exec( "def spyInput(id_, delay, object_, method, *args):\n    from edu.ncu.csie.oolab import CommandParser\n    CommandParser.input(id_, delay, object_, method, args)\n\n" );
		this.python_.exec( "def spyCheck(id_, file_, line, cpid, *args):\n    from edu.ncu.csie.oolab import CommandParser\n    CommandParser.check(id_, file_, line, cpid, *args)\n\n" );
		this.python_.exec( "def spyAsyncCheck(id_, file_, line, acpid, pre, *args):\n    from edu.ncu.csie.oolab import CommandParser\n    CommandParser.asyncCheck(id_, file_, line, acpid, pre, *args)\n\n" );
 	}
 
 	/**
 	 * Executes commands.
 	 *
 	 * b64Data must has command field.
 	 *
 	 * @param b64Data
 	 *            Base64 encoded JSON string
 	 * @return null if command is invalid; Base64 encoded JSON string otherwise.
 	 */
 	public String execute( String b64Data ) {
 		HashMap< String, Object > args = this.decodeCommand( b64Data );
 
 		AbstractCommand cmd = this.commands_.get( args.get( "command" ) );
 		if( cmd == null ) {
 			return null;
 		}
 
 		b64Data = this.encodeResult( cmd.execute( args ) );
 
 		return b64Data;
 	}
 
 	static public void setVerbose( Boolean verbose ) {
 		Settings.ActionLogs = verbose;
 		Settings.DebugLogs = verbose;
 		Settings.InfoLogs = verbose;
 		Settings.ProfileLogs = verbose;
 	}
 
 	private HashMap< String, Object > decodeCommand( String b64Data ) {
 		String json = new String( Base64.decodeBase64( b64Data.getBytes( this.CHARSET ) ), this.CHARSET );
 		return this.json_.fromJson( json, this.HASH_TYPE );
 	}
 
 	private String encodeResult( HashMap< String, Object > result ) {
 		String json = this.json_.toJson( result );
 		String b64Data = new String( Base64.encodeBase64( json.getBytes( this.CHARSET ) ), this.CHARSET );
 		return b64Data;
 	}
 
 	public static void input( Integer id, Integer delay, String object, String method, List<Object> args ) {
 		try {
 			Thread.sleep( delay );
 		} catch( InterruptedException e ) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		HashMap< String, Object > data = new HashMap< String, Object >();
 		data.put( "result", "input" );
 		data.put( "id", id );
 		data.put( "object", object );
 		data.put( "method", method );
 		data.put( "args", args );
 
 		String b64data = self.encodeResult( data );
 		System.out.println( b64data );
 	}
 
 	public static void check( Integer id, String file, Integer line, String cpid, List<Object> args ) {
 		HashMap< String, Object > data = new HashMap< String, Object >();
 		data.put( "result", "check" );
 		data.put( "id", id );
 		HashMap< String, Object > cp = new HashMap< String, Object >();
 		cp.put( "file", file );
 		cp.put( "line", line );
 		cp.put( "id", cpid );
 		cp.put( "args", args );
 		data.put( "cp", cp );
 
 		String b64data = self.encodeResult( data );
 		System.out.println( b64data );
 	}
 
 	public static void asyncCheck( Integer id, String file, Integer line, String acpid, String pre, List<Object> args ) {
 		HashMap< String, Object > data = new HashMap< String, Object >();
 		data.put( "result", "async_check" );
 		data.put( "id", id );
 		HashMap< String, Object > acp = new HashMap< String, Object >();
 		acp.put( "file", file );
 		acp.put( "line", line );
 		acp.put( "id", acpid );
 		acp.put( "pre", pre );
 		acp.put( "args", args );
 		data.put( "acp", acp );
 
 		String b64data = self.encodeResult( data );
 		System.out.println( b64data );
 	}
 
 	private static CommandParser self = null;
 	private final Type HASH_TYPE;
 	private final Charset CHARSET;
 	private Gson json_;
 	private HashMap< String, AbstractCommand > commands_;
 	private PythonInterpreter python_;
 	private Screen screen_;
 
 }
