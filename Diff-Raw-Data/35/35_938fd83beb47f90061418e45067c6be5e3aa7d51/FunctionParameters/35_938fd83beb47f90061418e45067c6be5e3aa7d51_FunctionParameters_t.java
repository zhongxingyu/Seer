 package no.runsafe.eventengine.engine;
 
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.player.RunsafePlayer;
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
 
 	public RunsafePlayer getPlayer(int index)
 	{
		RunsafePlayer player = RunsafeServer.Instance.getPlayerExact(this.getString(index));
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
 
 	List<LuaValue> parameters = new ArrayList<LuaValue>();
 }
