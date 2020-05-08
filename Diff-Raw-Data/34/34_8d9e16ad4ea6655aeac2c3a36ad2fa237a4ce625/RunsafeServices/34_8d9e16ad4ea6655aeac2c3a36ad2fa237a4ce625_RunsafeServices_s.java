 package no.runsafe;
 
 import no.runsafe.command.DebugLevelCommand;
 import no.runsafe.command.ReloadConfigCommand;
 import no.runsafe.command.RunsafePluginVersions;
 import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IDebug;
 import no.runsafe.framework.api.command.Command;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
 
import java.util.logging.Level;

public class RunsafeServices extends RunsafeConfigurablePlugin implements IPluginEnabled
 {
 	@Override
 	protected void PluginSetup()
 	{
 		Command command = new Command("runsafe", "Runsafe core services", null);
 
 		Command config = new Command("config", "Commands to control plugin configurations", null);
 		config.addSubCommand(getInstance(ReloadConfigCommand.class));
 		command.addSubCommand(config);
 
 		command.addSubCommand(getInstance(DebugLevelCommand.class));
 		command.addSubCommand(getInstance(RunsafePluginVersions.class));
 
 		addComponent(command);
 	}

	@Override
	public void OnPluginEnabled()
	{
		String level = getComponent(IConfiguration.class).getConfigValueAsString("debug");
		if (level != null && !level.equals("false"))
		{
			Level debugLevel = Level.parse(level);
			for (IDebug debugger : RunsafePlugin.getPluginAPI(IDebug.class))
				debugger.setDebugLevel(debugLevel);
		}
	}
 }
