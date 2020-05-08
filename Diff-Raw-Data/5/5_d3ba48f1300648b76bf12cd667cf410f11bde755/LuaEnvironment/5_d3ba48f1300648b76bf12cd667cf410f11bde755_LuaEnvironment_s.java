 package no.runsafe.framework.lua;
 
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.api.lua.Library;
 import no.runsafe.framework.minecraft.RunsafeServer;
 import org.apache.commons.io.FileUtils;
 import org.luaj.vm2.Globals;
 import org.luaj.vm2.LuaTable;
 import org.luaj.vm2.LuaValue;
 import org.luaj.vm2.lib.jse.JsePlatform;
 import org.picocontainer.Startable;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.List;
 
 public class LuaEnvironment implements Startable
 {
 	public static void loadFile(String file)
 	{
 		RunsafeServer.Instance.getDebugger().logInformation("Loading script %s", file);
 		global.get("dofile").call(LuaValue.valueOf(file));
 	}
 
 	public static Globals global;
 
 	static
 	{
 		global = JsePlatform.standardGlobals().checkglobals();
 		File source = new File("plugins/runsafe/lua");
 		if (source.exists() && source.isDirectory())
 		{
 			Collection<File> scripts = FileUtils.listFiles(source, new String[]{"lua"}, false);
 			for (File script : scripts)
 				loadFile(script.getAbsolutePath());
 		}
 	}
 
 	public LuaEnvironment(RunsafePlugin plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	@Override
 	public void start()
 	{
 		loadAPI();
 		loadScripts();
 	}
 
 	@Override
 	public void stop()
 	{
 	}
 
 	private void loadAPI()
 	{
 		List<Library> libraries = plugin.getComponents(Library.class);
 		if (!libraries.isEmpty())
 		{
 			global.set(plugin.getName(), new LuaTable());
 			for (Library library : libraries)
 				global.load(library);
 		}
 	}
 
 	private void loadScripts()
 	{
 		Collection<File> files = getScripts();
 		if (files != null)
 			for (File script : files)
 				if (script.isFile())
 					loadFile(script.getAbsolutePath());
 	}
 
 	private Collection<File> getScripts()
 	{
 		File data = plugin.getDataFolder();
 		if (!data.exists() || !data.isDirectory())
 			return null;
 
 		File scripts = new File(data, "lua");
		Collection<File> list = FileUtils.listFiles(scripts, new String[]{"lua"}, false);
 
 		return list != null && !list.isEmpty() ? list : null;
 //
 //		if (!scripts.exists() || !scripts.isDirectory())
 //			return null;
 //
 //		File[] list = scripts.listFiles((FilenameFilter) new WildcardFileFilter("*.lua"));
 //		return list != null && list.length > 0 ? list : null;
 	}
 
 	private RunsafePlugin plugin;
 }
