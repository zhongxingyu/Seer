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
		l.nl( "print( x )" );
		
		l.nl( "g2drt = luajava.bindClass(\"dlib.tests.Graphics2DRendererTest\")");
		l.nl( "g2 = luajava.new( g2drt )" );
		l.nl( "g2:main( {} )" );
 		
 		l.run();
 	}
 }
