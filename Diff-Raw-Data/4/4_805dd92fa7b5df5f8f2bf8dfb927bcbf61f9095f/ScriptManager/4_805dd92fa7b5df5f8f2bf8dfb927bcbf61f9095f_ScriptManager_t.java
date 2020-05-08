 package no.runsafe.eventengine.engine;
 
 import no.runsafe.eventengine.Plugin;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.internal.lua.Environment;
 import org.apache.commons.io.FileUtils;
 import org.luaj.vm2.LuaError;
 
 import java.io.File;
 import java.util.Collection;
 
 public class ScriptManager implements IPluginEnabled
 {
 	public ScriptManager(Plugin eventEngine, IConsole output, IScheduler scheduler)
 	{
 		this.scheduler = scheduler;
 		scriptPath = new File(eventEngine.getDataFolder(), "scripts");
 		if (!scriptPath.exists())
 			if (scriptPath.mkdirs())
 				output.logWarning("Failed to create scripts directory at: " + scriptPath.getPath());
 
 		this.output = output;
 	}
 
 	@Override
 	public void OnPluginEnabled()
 	{
 		scheduler.startSyncTask(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				loadScripts();
 			}
 		}, 1);
 	}
 
 	private void loadScripts()
 	{
 		int succeeded = 0;
 		int failed = 0;
 
 		Collection<File> files = FileUtils.listFiles(scriptPath, new String[]{"lua"}, true);
 		if (files != null)
 		{
 			for (File file : files)
 			{
 				String output = this.runScript(file);
 				if (output != null)
 				{
 					this.output.logError(output);
 					failed++;
 				}
 				else
 				{
 					succeeded++;
 				}
 			}
 		}
 
 		this.output.logInformation("%d lua script(s) loaded.", succeeded);
 		if (failed > 0)
 			this.output.logError("%d lua script(s) failed to load.", failed);
 	}
 
 	private String runScript(File script)
 	{
 		if (!script.isFile())
 			return null;
 
 		try
 		{
 			Environment.loadFile(script.getAbsolutePath());
 		}
 		catch (LuaError error)
 		{
 			return "Lua Error: " + error.getMessage();
 		}
 		return null;
 	}
 
 	private final File scriptPath;
 	private final IConsole output;
 	private final IScheduler scheduler;
 }
