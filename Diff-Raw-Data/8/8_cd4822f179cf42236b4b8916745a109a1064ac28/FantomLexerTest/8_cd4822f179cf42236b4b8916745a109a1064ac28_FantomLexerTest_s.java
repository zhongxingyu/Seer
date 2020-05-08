 /*
  * Thibaut Colar Apr 1, 2010
  */
 package net.colar.netbeans.fan.test;
 
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.util.Date;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FantomLexer;
 import net.jot.testing.JOTTestable;
 import net.jot.testing.JOTTester;
 import org.parboiled.BasicParseRunner;
 import org.parboiled.Parboiled;
 import org.parboiled.common.StringUtils;
 import org.parboiled.support.ParsingResult;
 
 /**
  *
  * @author thibautc
  */
 public class FantomLexerTest implements JOTTestable
 {
 
 	public void jotTest() throws Throwable
 	{
 		FantomLexer lexer = Parboiled.createParser(FantomLexer.class);
 		testFile(lexer, FantomParserTest.FAN_HOME+"/src/testSys/fan/ActorTest.fan");
 	}
 
 	public static void testFile(FantomLexer lexer, String filePath) throws Exception
 	{
 		try
 		{
 			// read the file data into a string
 			DataInputStream dis = new DataInputStream(new FileInputStream(filePath));
 			byte[] buffer = new byte[dis.available()];
 			dis.readFully(buffer);
 			dis.close();//TODO: finally
 			String testInput = new String(buffer);
 
 			// run the lexer
 			long start = new Date().getTime();
 			ParsingResult<AstNode> result = BasicParseRunner.run(lexer.lexer(), testInput);
 			long time = new Date().getTime() - start;
 			
 			if (result.hasErrors())
 			{
 				System.err.println(StringUtils.join(result.parseErrors, "---\n"));
 			}
 			System.out.println("Lexed "+ filePath + " in " + time + "ms");
 
 			JOTTester.checkIf("Lexing " + filePath, !result.hasErrors());
 			JOTTester.checkIf("Lexer time " + filePath, time < 2000, "Took: " + time);
 		} catch (Exception e)
 		{
 			JOTTester.checkIf("Exception while lexing " + filePath, false);
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			JOTTester.singleTest(new FantomLexerTest(), false);
 		} catch (Throwable t)
 		{
 			t.printStackTrace();
 		}
 	}	
 }
