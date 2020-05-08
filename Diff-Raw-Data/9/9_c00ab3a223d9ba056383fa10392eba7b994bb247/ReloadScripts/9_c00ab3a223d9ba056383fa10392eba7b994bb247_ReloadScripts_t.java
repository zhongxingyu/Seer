 package no.runsafe.eventengine.commands;
 
 import no.runsafe.eventengine.engine.ScriptManager;
 import no.runsafe.eventengine.engine.hooks.HookHandler;
 import no.runsafe.framework.api.command.ExecutableCommand;
 import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.player.IPlayer;
 
 import java.util.Map;
 
 public class ReloadScripts extends ExecutableCommand
 {
	public ReloadScripts(IConsole output, ScriptManager scriptManager)
 	{
 		super("reloadscripts", "Reloads all lua scripts.", "runsafe.eventengine.reload");
 		this.output = output;
 		this.scriptManager = scriptManager;
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
 	{
 		this.output.logWarning("Reloading lua engine.");
 		HookHandler.clearHooks(); // Clear any hooks scripts have made.
 		this.scriptManager.OnPluginEnabled(); // Clear environment and reload all scripts.
 		return (executor instanceof IPlayer ? "&eReloaded!" : null);
 	}
 
	private final IConsole output;
 	private final ScriptManager scriptManager;
 }
