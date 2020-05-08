 /*
  * Thibaut Colar Apr 6, 2010
  */
 package net.colar.netbeans.fan.test;
 
 import java.io.File;
 import java.io.FileInputStream;
 import net.colar.netbeans.fan.FanLanguage;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.structure.FanSemanticAnalyzer;
 import net.jot.testing.JOTTester;
 import org.netbeans.modules.parsing.api.Snapshot;
import org.parboiled.common.StringUtils;
 
 /**
  * Try to give some random partial file through the parser and see that it doesn't explode
  * @author thibautc
  */
 public class FantomPartialFileParserTest extends FantomCSLTest
 {
 
 	int NB_FILES_TO_USE = 10;
 	int ITERATIONS_PER_FILE = 20;
 
 	@Override
 	public void cslTest() throws Throwable
 	{
 		File folder = new File(prefs.getString("fantom.home") + File.separator + "src" + File.separator + "sys" + File.separator + "fan");
 		File[] files = folder.listFiles();
 		for (int i = 0; i != NB_FILES_TO_USE; i++)
 		{
 			int rand = (int) Math.round((Math.random() * (files.length - 1)));
 			File f = files[rand];
 			FileInputStream fis = new FileInputStream(f);
 			byte[] buffer = new byte[fis.available()];
 			fis.read(buffer);
 			fis.close();
 			for (int j = 0; j != ITERATIONS_PER_FILE; j++)
 			{
 				String text = new String(buffer);
 				int var = i % 3;
 				int rand2 = (int) Math.random() * (text.length() - 2);
 				if (var == 0)
 				{
 					// remove single char
 					text = text.substring(0, rand2) + text.substring(rand2 + 1);
 				} else if (var == 1)
 				{
 					// remove random size hunk
 					text = mangleText(text);
 				} else
 				{
 					// add single char
 					int rand3 = (int) Math.random() * (text.length() - 1);
 					text = text.substring(0, rand2) + text.charAt(rand3) + text.substring(rand2);
 				}
 				testDoc(NBTestUtilities.textToSnapshot(text, FanLanguage.FAN_MIME_TYPE));
 			}
 		}
 	}
 
 	private void testDoc(Snapshot snap) throws Exception
 	{
 		FanParserTask task = null;
 		boolean err =false;
 		try
 		{
 			task = new FanParserTask(snap);
 			task.parse();
 			task.parseGlobalScope();
			System.err.println(StringUtils.join(task.getDiagnostics(), "---\n"));
			JOTTester.checkIf("Errors during partial file analysis", task.getDiagnostics().isEmpty());
 			task.parseLocalScopes();
 			// parse eroors are ok, we just don't want exceptions
 			if (task.getDiagnostics().isEmpty())
 			{
 				// IDE only does semantic analysis if there where no parsing errors
 				FanSemanticAnalyzer analyzer = new FanSemanticAnalyzer();
 				analyzer.run(task, null);
 			}
 			// we don't care if analyzer finds errors, we just don't want it to crash
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 			err = true;
 		}
 		JOTTester.checkIf("Exception during partial file analysis", ! err);
 	}
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			JOTTester.singleTest(new FantomPartialFileParserTest(), false);
 		} catch (Throwable t)
 		{
 			t.printStackTrace();
 		}
 	}
 
 	/**
 	 * Just make a "hole" in the text ... so we get incomplete code as it would be as a user is typing.
 	 * @param text
 	 * @return
 	 */
 	private String mangleText(String text)
 	{
 		int start = (int) Math.random() * (text.length() - 1);
 		int end = (int) Math.random() * (text.length() - start - 1);
 		text = text.substring(0, start).concat(text.substring(end));
 		return text;
 	}
 }
