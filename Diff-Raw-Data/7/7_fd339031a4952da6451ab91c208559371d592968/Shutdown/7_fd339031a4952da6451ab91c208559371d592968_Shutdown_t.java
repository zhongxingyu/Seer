 package org.integratedmodelling.thinklab.client.commands;
 
 import java.util.ArrayList;
 
 import org.integratedmodelling.thinklab.client.CommandHandler;
 import org.integratedmodelling.thinklab.client.Result;
 import org.integratedmodelling.thinklab.client.Session;
 import org.integratedmodelling.thinklab.client.annotations.Command;
 import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
 import org.integratedmodelling.thinklab.client.shell.CommandLine;
 
 import uk.co.flamingpenguin.jewel.cli.Option;
 
 @Command(id="shutdown")
 public class Shutdown extends CommandHandler {
 
 	interface RArgs extends Arguments {
 
 		@Option(description="restart server after shutdown")
 		boolean isRestart();
 		
 		@Option(description="update server to given branch and restart")
 		String getUpdate();
 		boolean isUpdate();
 		
 		@Option(description="time to shutdown in seconds")
 		int getTime();
 		boolean isTime();
 	}
 	
 	@Override
 	public Class<? extends Arguments> getArgumentsClass() {
 		return RArgs.class;
 	}
 
 	@Override
 	public Result execute(Arguments arguments, Session session, CommandLine cl)
 			throws ThinklabClientException {
 		
 		if (!session.isConnected())
 			throw new ThinklabClientException("remote command: not connected to a server");
 
 		RArgs args = (RArgs) arguments;		
 		ArrayList<String> arg = new ArrayList<String>();
 		
 		String s = null;
 		if (args.isUpdate())
 			s = cl.ask("please confirm attempt to update remote " 
 					+ session.getName() + " to branch " + 
 					args.getUpdate() + " [yes|no] ");
 		else if (args.isRestart())	
 			s = cl.ask("please confirm restart of remote server " 
 					+ session.getName() + " [yes|no] ");
 		else 
 			s = cl.ask("please confirm shutdown of remote server " 
 					+ session.getName() + " [yes|no] ");
 
 		if (s.equals("yes")) {
 			if (args.isUpdate()) {
 				arg.add("hook");
 				arg.add("update");
 				arg.add("hookarg1");
 				arg.add(args.getUpdate());
 			} else if (args.isRestart()) {
 				arg.add("hook");
 				arg.add("restart");
 			}
 		} else {
 			cl.say("command aborted");
 			return null;
 		}
 
 		if (args.isTime()) {
 			arg.add("time");
 			arg.add(args.getTime()+"");
 		}
 		
 		/*
 		 * send shutdown command with appropriate hook and parameter if required.
 		 */
 		Result ret = session.send("shutdown", false, arg.toArray(new String[arg.size()]));
 		
 		/*
 		 * TODO cleanup session
 		 */
		if (ret.getStatus() == Result.OK)
			session.disconnect();
 		
		return ret.setSession(session);
 	}
 
 }
