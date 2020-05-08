 package com.benzrf.sblock.sburbmachines;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.benzrf.sblock.sburbmachines.machines.Cruxtruder;
 import com.benzrf.sblock.sburbmachines.machines.Machine;
 import com.benzrf.sblock.sburbmachines.machines.Transmaterializer;
 import com.google.gson.Gson;
 import com.griefcraft.lwc.LWC;
 import com.griefcraft.lwc.LWCPlugin;
 
 public class SburbMachines extends JavaPlugin
 {
 	@Override
 	public void onEnable()
 	{
 		SburbMachines.instance = this;
		this.lwc = ((LWCPlugin) this.getServer().getPluginManager().getPlugin("LWC")).getLWC();
 		try
 		{
 			this.smachines.addAll(Arrays.asList(this.gson.fromJson(this.readFile("plugins/SburbMachines/cruxtruders.smd"), Cruxtruder[].class)));
 			this.smachines.addAll(Arrays.asList(this.gson.fromJson(this.readFile("plugins/SburbMachines/transmaterializers.smd"), Transmaterializer[].class)));
 			for (Machine m : this.smachines)
 			{
 				m.makeUsable();
 				this.addMachine(m);
 			}
 		}
 		catch (IOException e)
 		{
 			Logger.getLogger("Minecraft").warning("[SburbMachines] Error reading machine files:");
 			e.printStackTrace();
 		}
 		this.listener = new SburbMachinesListener();
 		this.getServer().getPluginManager().registerEvents(this.listener, this);
 	}
 	
 	private String readFile(String path) throws IOException
 	{
 		String file;
 		FileInputStream stream = new FileInputStream(new File(path));
 		try
 		{
 			FileChannel fc = stream.getChannel();
 			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 			file = Charset.defaultCharset().decode(bb).toString();
 		}
 		finally
 		{
 			stream.close();
 		}
 		return file;
 	}
 	
 	@Override
 	public void onDisable()
 	{
 		try
 		{
 			Set<Cruxtruder> cs = new HashSet<Cruxtruder>();
 			Set<Transmaterializer> ts = new HashSet<Transmaterializer>();
 			for (Machine m : this.smachines)
 			{
 				m.makeSerializable();
 				if (m instanceof Cruxtruder)
 				{
 					cs.add((Cruxtruder) m);
 				}
 				else if (m instanceof Transmaterializer)
 				{
 					ts.add((Transmaterializer) m);
 				}
 			}
 			BufferedWriter w;
 			w = new BufferedWriter(new FileWriter("plugins/SburbMachines/cruxtruders.smd"));
 			w.write(this.gson.toJson(cs));w.flush();w.close();
 			w = new BufferedWriter(new FileWriter("plugins/SburbMachines/transmaterializers.smd"));
 			w.write(this.gson.toJson(ts));w.flush();w.close();
 		}
 		catch (Exception e)
 		{
 			Logger.getLogger("Minecraft").warning("[SburbMachines] Error writing machine files:");
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		if (!sender.isOp()) return true;
 		if (commandLabel.equalsIgnoreCase("crux"))
 		{
 			Cruxtruder c = new Cruxtruder(((Player) sender).getLocation(), false);
 			this.addMachine(c);
 		}
 		else if (commandLabel.equalsIgnoreCase("delmachine"))
 		{
 			try
 			{
 				this.deleteMachine(this.machines.get(((Player) sender).getTargetBlock(null, 100).getLocation()));
 			}
 			catch (NullPointerException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return true;
 	}
 	
 	public void addMachine(Machine m)
 	{
 		this.smachines.add(m);
 		for (Location block : m.getBlocks())
 		{
 			this.machines.put(block, m);
 		}
 	}
 	
 	public void removeMachine(Machine m)
 	{
 		this.smachines.remove(m);
 		for (Location block : m.getBlocks())
 		{
 			this.machines.remove(block);
 		}
 	}
 	
 	public void deleteMachine(Machine m)
 	{
 		this.removeMachine(m);
 		for (Location block : m.getBlocks())
 		{
 			block.getBlock().setTypeIdAndData(0, (byte) 0, false);
 		}
 	}
 	
 	public void setBlock(Block b, int id, byte data, boolean phys)
 	{
 		if (this.machines.containsKey(b.getLocation()))
 		{
 			this.deleteMachine(this.machines.get(b.getLocation()));
 		}
 		b.setTypeIdAndData(id, data, phys);
 	}
 	
 	protected Map<Location, Machine> machines = new HashMap<Location, Machine>();
 	protected Set<Machine> smachines = new HashSet<Machine>();
 	private SburbMachinesListener listener;
 	private Gson gson = new Gson();
	public LWC lwc;
 	public static SburbMachines instance;
 }
