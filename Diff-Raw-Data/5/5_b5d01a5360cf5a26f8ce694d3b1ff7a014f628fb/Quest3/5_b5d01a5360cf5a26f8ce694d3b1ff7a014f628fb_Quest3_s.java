 package org.akquinet.audit.bsi.httpd.software;
 
 import java.io.File;
 import java.util.List;
 
 import org.akquinet.audit.FormattedConsole;
 import org.akquinet.audit.ModuleHelper;
 import org.akquinet.audit.YesNoQuestion;
 import org.akquinet.httpd.ConfigFile;
 import org.akquinet.httpd.syntax.Directive;
 
 public class Quest3 extends ModuleHelper implements YesNoQuestion
 {
 	private static final String _id = "Quest3";
 	private static final FormattedConsole _console = FormattedConsole.getDefault();
 	private static final FormattedConsole.OutputLevel _level = FormattedConsole.OutputLevel.Q1;
 	
 	public Quest3(ConfigFile conf, File apacheExecutable)
 	{
 		super(conf, apacheExecutable);
 	}
 
 	@Override
 	public boolean answer()
 	{
 		_console.println(FormattedConsole.OutputLevel.HEADING, "----" + _id + "----");
		//TODO: test
 		List<Directive> loadList = getLoadModuleList();
 		for (Directive directive : loadList)
 		{
 			String[] arguments = directive.getValue().trim().split("[ \t]+");
 			if(arguments == null || arguments.length < 2)
 			{
 				continue;
 			}
 			
 			if(arguments[0].equals("security2_module"))
 			{
 				Directive modSec = directive;
 				_console.printAnswer(_level, true, "ModSecurity is being loaded:");
 				_console.println(_level, modSec.getLinenumber() + ": " + modSec.getName() + " " + modSec.getValue());
 				return true;
 			}
 			
 		}
 		
 		//maybe ModSecurity is compiled into the apache binary, check for that:
 		String[] modList = getCompiledIntoModulesList();
 		for (String str : modList)
 		{
			if(str.matches("( |\t)*mod_security.c"))
 			{
 				_console.printAnswer(_level, true, "ModSecurity is compiled into the httpd binary.");
 				return true;
 			}
 		}
 		
 		_console.printAnswer(_level, false, "ModSecurity seems not to be loaded.");
 		return false;
 	}
 
 	@Override
 	public boolean isCritical()
 	{
 		return false;
 	}
 
 	@Override
 	public String getID()
 	{
 		return _id;
 	}
 }
