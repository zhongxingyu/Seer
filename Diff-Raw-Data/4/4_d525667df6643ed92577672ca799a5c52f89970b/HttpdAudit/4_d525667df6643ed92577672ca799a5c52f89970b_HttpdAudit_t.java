 package org.akquinet.audit.bsi.httpd;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.akquinet.audit.InteractiveAsker;
 import org.akquinet.audit.YesNoQuestion;
 import org.akquinet.httpd.ConfigFile;
 
 public class HttpdAudit
 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		//TODO all
 		//for now this is just for debugging
 		try
 		{
 			ConfigFile conf = null;
 			File apacheExecutable = null;
 			boolean highSec = true;	//TODO initialize me in some way
 			switch(args.length)
 			{
 			case 2:
				conf = new ConfigFile(new File(args[0]));
				apacheExecutable = new File(args[1]);
 				break;
 			default:
 				System.err.println("parameters: apacheConfigFile apacheExecutable");
 				return;
 			}
 			
 			List<YesNoQuestion> tmpList = new LinkedList<YesNoQuestion>();
 
 			tmpList.add(new Quest1(highSec));
 			tmpList.add(new Quest3(conf, apacheExecutable));
 			tmpList.add(new Quest5(conf));
 			tmpList.add(new Quest6(apacheExecutable));
 			tmpList.add(new Quest11b(conf));
 			
 			InteractiveAsker asker = new InteractiveAsker(tmpList);
 			
 			asker.askQuestions();
 		}
 		catch (IOException e) { e.printStackTrace(); }
 	}
 
 }
