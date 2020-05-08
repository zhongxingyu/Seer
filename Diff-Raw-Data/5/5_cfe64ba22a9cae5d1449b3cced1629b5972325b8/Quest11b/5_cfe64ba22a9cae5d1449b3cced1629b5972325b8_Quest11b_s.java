 package org.akquinet.audit.bsi.httpd;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.akquinet.audit.FormattedConsole;
 import org.akquinet.audit.YesNoQuestion;
 import org.akquinet.httpd.ConfigFile;
 import org.akquinet.httpd.syntax.Context;
 import org.akquinet.httpd.syntax.Directive;
 import org.akquinet.httpd.syntax.Statement;
 
 public class Quest11b implements YesNoQuestion
 {
 	private static final String _id = "Quest11b";
 	private ConfigFile _conf;
 	private static final FormattedConsole _console = FormattedConsole.getDefault();
 	private static final FormattedConsole.OutputLevel _level = FormattedConsole.OutputLevel.Q2;
 
 	public Quest11b(ConfigFile conf)
 	{
 		_conf = conf;
 	}
 
 	/**
 	 * checks whether there are any Include-directives in the config file
 	 */
 	@Override
 	public boolean answer()
 	{
		//TODO check whether all cases are handled (correctly)
 		List<Statement> statList = _conf.getHeadExpression().getStatements()._statements;
 		List<Context> dirList = createDirectoryRoot_List(statList);
 
 		boolean ret = false;
 		for (Context dir : dirList)
 		{
 			if (!dir.getDirectiveIgnoreCase("allow").isEmpty())
 			{
 				_console.printAnswer(_level, false, "Nobody should be able to access \"/\". Remove the following \"Allow\" directives:");
 				List<Directive> allowList = dir.getDirectiveIgnoreCase("allow");
 				for (Directive directive : allowList)
 				{
 					_console.println(_level, directive.getLinenumber() + ": " + directive.getName() + " " + directive.getValue());
 				}
 				return false;
 			}
 
 			if (dir.getSurroundingContexts().get(0) != null)
 			{
 				continue;
 			}
 
 			List<Directive> orderList = dir.getDirectiveIgnoreCase("order");
 			List<Directive> denyList = dir.getDirectiveIgnoreCase("deny");
 			if (orderList.size() == 1 && denyList.size() == 1 && orderList.get(0).getLinenumber() > denyList.get(0).getLinenumber())
 			{
 				Directive order = orderList.get(0);
 				Directive deny = denyList.get(0);
 				// fast evaluation ensures, that only one of these methods can
 				// output an answer-message
 				if (orderIsNotOK(order) || denyIsNotOK(deny))
 				{
 					return false;
 				}
 				else
 				{
 					_console.printAnswer(_level, true, "Access to \"/\" correctly blocked via mod_access.");
 					ret = true;
 				}
 			}
 			else
 			{
 				_console.printAnswer(_level, false, "I found multiple and/or incorrectly sorted \"Order\" and \"Deny\" directives betwenn lines "
						+ "-" + dir.getEndLineNumber() + ". Please make them unique, sort them and run me again.");
 				return false;
 			}
 		}
 
 		return ret;
 	}
 
 	private static boolean denyIsNotOK(Directive deny)
 	{
 		if (deny.getValue().matches("( |\t)*from all( |\t)*"))
 		{
 			return true;
 		}
 		else
 		{
 			_console.printAnswer(_level, false, "Nobody should be able to access \"/\". Correct the following directive to \"Deny from all\".");
 			_console.println(_level, deny.getLinenumber() + ": " + deny.getName() + " " + deny.getValue());
 			return false;
 		}
 	}
 
 	private static boolean orderIsNotOK(Directive order)
 	{
 		if (order.getValue().matches("( |\t)*from all( |\t)*"))
 		{
 			return true;
 		}
 		else
 		{
 			_console.printAnswer(_level, false, "Nobody should be able to access \"/\". Correct the following directive to \"Order Deny,Allow\".");
 			_console.println(_level, order.getLinenumber() + ": " + order.getName() + " " + order.getValue());
 			return false;
 		}
 	}
 
 	protected static List<Context> createDirectoryRoot_List(List<Statement> statList)
 	{
 		List<Context> conList = new LinkedList<Context>();
 		for (Statement stat : statList)
 		{
 			if (stat instanceof Context)
 			{
 				Context con = (Context) stat;
 				if (con.getName().equalsIgnoreCase("directory"))
 				{
 					if (con.getSurroundingContexts().size() == 1)
 					{
 						if (con.getValue().matches("( |\t)*/( |\t)*"))
 						{
 							conList.add(con);
 						}
 					}
 				}
 			}
 		}
 		return conList;
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
