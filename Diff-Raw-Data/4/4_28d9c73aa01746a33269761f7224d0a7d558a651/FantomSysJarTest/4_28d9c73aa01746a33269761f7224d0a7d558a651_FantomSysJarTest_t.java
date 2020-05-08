 /*
  * Thibaut Colar Feb 2, 2010
  */
 
 package net.colar.netbeans.fan.test;
 
import fan.sys.Env;
 import net.colar.netbeans.fan.indexer.FanIndexer;
 import net.colar.netbeans.fan.platform.FanPlatform;
 import net.colar.netbeans.fan.platform.FanPlatformSettings;
 import net.jot.testing.JOTTestable;
 import net.jot.testing.JOTTester;
 
 /**
  * Testing sys.jar use, since
  * it can break between releases when Fantom sys.jar is changed.
  * @author thibautc
  */
 public class FantomSysJarTest implements JOTTestable
 {
 
 	public void jotTest() throws Throwable
 	{
 		FanPlatformSettings.getInstance().put(FanPlatformSettings.PREF_FAN_HOME, "/home/thibautc/fantom/");
 		FanPlatform.getInstance(false).readSettings();
 
		Env.cur().homeDir();

 		String input="hello *there* `http://www.google.com`";
 		String out = FanIndexer.fanDocToHtml(input);
 		boolean test = out.indexOf("<em>there</em>")!=-1 &&
 						out.indexOf("<a href='http://www.google.com'>")!=-1;
 		JOTTester.checkIf("fanDocToHtml", test, "Got: '"+out+"'");
 	}
 
 	public static void main(String[] args)
 	{
 		try{new FantomSysJarTest().jotTest();}catch(Throwable e){e.printStackTrace();}
 	}
 }
