 package dlib.tests;
 
 import javax.script.ScriptException;
 
 import dlib.lua.DLua;
 
 public class DLuaTest 
 {
 	public static void main( String[] args ) throws ScriptException
 	{
 		DLua l = new DLua();
 		l.nl( "function returnten()" );
 		l.nl( "return 10" );
 		l.nl( "end" );
 		l.nl( "x = returnten()^2 + math.sqrt( 25 )" );
 		
 		l.run();
		System.out.println( l.get( "x" ) );
 	}
 }
