 package no.runsafe.framework.api.lua;
 
 import no.runsafe.framework.minecraft.RunsafeLocation;
 import no.runsafe.framework.minecraft.RunsafeServer;
 import no.runsafe.framework.minecraft.RunsafeWorld;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import org.luaj.vm2.LuaError;
 import org.luaj.vm2.LuaValue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class FunctionParameters
 {
 	public void addParameter(LuaValue value)
 	{
 		this.parameters.add(value);
 	}
 
 	private LuaValue getLuaValue(int index)
 	{
 		if (this.parameters.size() < index -1)
 			throw new LuaError("Function contains an invalid amount of parameters");
 
 		return this.parameters.get(index);
 	}
 
 	public String getString(int index)
 	{
 		return this.getLuaValue(index).toString();
 	}
 
 	public Double getDouble(int index)
 	{
 		return this.getLuaValue(index).todouble();
 	}
 
 	public Integer getInt(int index)
 	{
 		return this.getLuaValue(index).toint();
 	}
 
 	public Boolean getBool(int index)
 	{
 		return this.getLuaValue(index).toboolean();
 	}
 
 	public Float getFloat(int index)
 	{
 		return this.getLuaValue(index).tofloat();
 	}
 
 	public Short getShort(int index)
 	{
 		return this.getLuaValue(index).toshort();
 	}
 
 	public Byte getByte(int index)
 	{
 		return this.getLuaValue(index).tobyte();
 	}
 
 	public RunsafePlayer getPlayer(int index)
 	{
 		RunsafePlayer player = RunsafeServer.Instance.getOfflinePlayerExact(this.getString(index));
 		if (player == null)
			throw new LuaError(String.format("Argument %s is not a valid player.", index));
 
 		return player;
 	}
 
 	public RunsafeWorld getWorld(int index)
 	{
 		RunsafeWorld world = RunsafeServer.Instance.getWorld(this.getString(index));
 		if (world == null)
			throw new LuaError(String.format("Argument %s is not a valid world.", index));
 
 		return world;
 	}
 
 	public RunsafeLocation getLocation(int index)
 	{
 		return new RunsafeLocation(this.getWorld(index), getDouble(index + 1), getDouble(index + 2), getDouble(index + 3));
 	}
 
 	public boolean hasParameter(int index)
 	{
 		return this.parameters.size() >= index + 1;
 	}
 
 	private final List<LuaValue> parameters = new ArrayList<LuaValue>();
 }
