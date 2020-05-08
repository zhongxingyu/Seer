 package com.ftwinston.Killer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public abstract class WorldOption
 {
 	static List<WorldOptionPlugin> worldOptions = new ArrayList<WorldOptionPlugin>();
 	static WorldOptionPlugin get(int num) { return worldOptions.get(num); }
 	Killer plugin; Game game;
 	
 	final void initialize(Game game, WorldOptionPlugin optionPlugin)
 	{
 		this.game = game;
 		plugin = game.plugin;
 		name = optionPlugin.getName();
 		options = setupOptions();
 	}
 	
 	private String name;
 	public final String getName()
 	{
 		return name;
 	}
 
 	protected final JavaPlugin getPlugin() { return plugin; }
 	
 	protected final World createWorld(WorldConfig worldConfig, Runnable runWhenDone)
 	{
 		Game game = worldConfig.getGame();
		game.getGameMode().beforeWorldGeneration(game.getNumber(), worldConfig);
 		
 		World world = plugin.worldManager.createWorld(worldConfig, runWhenDone);
 		game.getWorlds().add(world);
 		return world;
 	}
 	
 	final void createWorlds(Game game, Runnable runWhenDone)
 	{
 		final Environment[] environments = game.getGameMode().getWorldsToGenerate();
 				
 		for ( int i=environments.length-1; i>=0; i-- )
 			runWhenDone = new WorldSetupRunner(game, environments[i], i, runWhenDone);
 		
 		runWhenDone.run();
 	}
 	
 	protected abstract void setupWorld(WorldConfig world, Runnable runWhenDone);
 	
 	private class WorldSetupRunner implements Runnable
 	{
 		public WorldSetupRunner(Game game, Environment environment, int num, Runnable runNext)
 		{
 			this.game = game;
 			this.environment = environment;
 			this.num = num;
 			this.runNext = runNext;
 		}
 	
 		private Game game;
 		private Environment environment;
 		private int num;
 		private Runnable runNext;
 		
 		public void run()
 		{
			String worldName = Settings.killerWorldName + "_" + (num+1);
			
 			final WorldConfig helper = new WorldConfig(game, worldName, environment);
			System.out.println("WorldSetupRunner.run");
			
 			setupWorld(helper, runNext);
 		}
 	}
 	
 	private Option[] options;
 	protected abstract Option[] setupOptions();
 	public final Option[] getOptions() { return options; }
 	public final Option getOption(int num) { return options[num]; }
 	public final int getNumOptions() { return options.length; }
 	
 	public void toggleOption(int num)
 	{
 		Option option = options[num];
 		option.setEnabled(!option.isEnabled());
 	}
 }
