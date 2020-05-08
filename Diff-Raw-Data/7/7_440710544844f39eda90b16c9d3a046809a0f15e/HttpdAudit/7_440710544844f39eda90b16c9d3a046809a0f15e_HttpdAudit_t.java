 package org.akquinet.audit.bsi.httpd;
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import org.akquinet.audit.Asker;
 import org.akquinet.audit.Heading2Printer;
 import org.akquinet.audit.YesNoQuestion;
 import org.akquinet.audit.bsi.httpd.os.Quest1;
 import org.akquinet.audit.bsi.httpd.software.Quest2;
 import org.akquinet.audit.bsi.httpd.software.Quest3;
 import org.akquinet.audit.bsi.httpd.software.Quest4;
 import org.akquinet.audit.bsi.httpd.software.Quest5;
 import org.akquinet.audit.bsi.httpd.software.Quest6;
 import org.akquinet.audit.bsi.httpd.software.Quest7;
 import org.akquinet.audit.bsi.httpd.usersNrights.Quest10;
 import org.akquinet.audit.bsi.httpd.usersNrights.Quest11;
 import org.akquinet.audit.bsi.httpd.usersNrights.Quest12;
 import org.akquinet.audit.bsi.httpd.usersNrights.Quest8;
 import org.akquinet.audit.bsi.httpd.usersNrights.Quest9;
 import org.akquinet.audit.ui.UserCommunicator;
 
 public class HttpdAudit
 {
 	enum OperatingSystem
 	{
 		Ubuntu,
 		Debian,
 		SUSE,
 		RedHat,
 		UNKNOWN
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		Locale locale = getLocale(args);
 		ResourceBundle labels = ResourceBundle.getBundle("global", locale);
 		ensureRootPermissions(labels);
 		String apacheExec;
 		String apacheConf;
 		
 		switch(getOperatingSystem())
 		{
 		case Debian:
 		case Ubuntu:
 			apacheExec = "/usr/sbin/apache2";
 			apacheConf = "/etc/apache2/apache2.conf";
 			break;
 		case SUSE:
 			apacheExec = "/usr/sbin/httpd2";
 			apacheConf = "/etc/apache2/httpd.conf";
 			break;
 		case RedHat:
 			apacheExec = "/usr/sbin/httpd";
 			apacheConf = "/etc/httpd/httpd.conf";
 			break;
 		case UNKNOWN:
 		default:
 			apacheExec = "/usr/sbin/httpd";
 			apacheConf = "/etc/httpd/httpd.conf";
 			break;
 		}
 		
 		setHtmlReportFile(labels);
 		
 		UserCommunicator uc = UserCommunicator.getDefault();
 		uc.setLocale(locale);
 		
 		setFastMode(args, uc);
 		
 		try
 		{
 			PrologueData pD = new PrologueData(apacheExec, apacheConf, null, null, null, true, true);
 			Asker a = new Asker();
 			
 			a.askPrologue(new PrologueQuestion(pD));
 			
 			YesNoQuestion[] quests = { new Heading2Printer( labels.getString("H3") , 1),
 					new Quest1(pD._highSec),
 					new Heading2Printer( labels.getString("H4") , 2),
 					new Quest2(pD._apacheExecutable),
 					new Quest3(pD._conf, pD._apacheExecutable),
 					new Quest4(pD._conf, pD._apacheExecutable),
 					new Quest5(pD._conf),
 					new Quest6(pD._apacheExecutable),
 					new Quest7(pD._conf),
 					new Heading2Printer( labels.getString("H5") , 3),
 					new Quest8(pD._configFile, pD._conf, pD._highSec),
 					new Quest9(pD._conf, pD._apacheExecutable.getName(), pD._highSec),
 					new Quest10(pD._conf),
 					new Quest11(pD._conf),
 					new Quest12(pD._conf, pD._apacheExecutable.getName())
 			};
 			
 			a.addQuestions(quests);
 			
 			printResumee(labels, uc, a.askQuestions());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			uc.finishCommunication();
 		}
 	}
 
 	private static void printResumee(ResourceBundle labels,
 			UserCommunicator uc, boolean overallAnswer)
 	{
 		if(overallAnswer)
 		{
 			uc.printHeading1( labels.getString("H6") );
 			uc.printParagraph( labels.getString("L3_ok") );
 		}
 		else
 		{
 			uc.printHeading1( labels.getString("H6") );
 			uc.printParagraph( labels.getString("L3_bad") );
 		}
 	}
 
 	private static void setFastMode(String[] args, UserCommunicator uc)
 	{
 		for (String string : args)
 		{
 			if(string.equals("--fast"))
 			{
 				uc.setIgnore_WaitForUserToContinue(true);
 			}
 		}
 	}
 
 	private static void setHtmlReportFile(ResourceBundle labels)
 	{
 		try
 		{
 			File htmlReport = new File("./htmlReport.htm");
 			System.out.println( MessageFormat.format(labels.getString("L1_willSaveHTML"), htmlReport.getCanonicalPath()) );
 			UserCommunicator.setDefault(new UserCommunicator(htmlReport));
 		}
		catch (Exception e)
 		{
			throw new RuntimeException(e);
 		}
 	}
 
 	private static void ensureRootPermissions(ResourceBundle labels)
 	{
 		if(!"root".equals(System.getenv("USER")))
 		{
 			//System.out.println("This application has to be run as root!");
 			System.out.println( labels.getString("E1_notRunAsRoot") );
 			System.exit(1);
 		}
 	}
 
 	private static Locale getLocale(String[] args)
 	{
 		Locale locale = Locale.getDefault();
 		for (String arg : args)
 		{
 			if(arg.equals("en"))
 			{
 				locale = Locale.ENGLISH;
 				break;
 			}
 			else if(arg.equals("de"))
 			{
 				locale = Locale.GERMAN;
 				break;
 			}
 		}
 		return locale;
 	}
 
 	private static OperatingSystem getOperatingSystem()
 	{
 		try
 		{
 			Process p = (new ProcessBuilder("./distro.sh")).start();
 			int exitVal = p.waitFor();
 			
 			switch(exitVal)
 			{
 			case 10:
 				return OperatingSystem.Ubuntu;
 			case 20:
 				return OperatingSystem.SUSE;
 			case 30:
 				return OperatingSystem.RedHat;
 			case 40:
 				return OperatingSystem.Debian;
 			case 0:
 			default:
 				return OperatingSystem.UNKNOWN;
 			}
 		}
 		catch (Exception e)
 		{
 			//TODO better handling here
 			e.printStackTrace();
 			return OperatingSystem.UNKNOWN;
 		}
 	}
 }
