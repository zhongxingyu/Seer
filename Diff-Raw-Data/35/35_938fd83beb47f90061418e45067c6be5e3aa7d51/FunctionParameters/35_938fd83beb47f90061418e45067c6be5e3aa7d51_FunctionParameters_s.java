 package no.runsafe.eventengine.engine;
 
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.player.RunsafePlayer;
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
		return RunsafeServer.Instance.getPlayerExact(this.getString(index));
	}
 
	public RunsafeWorld getWorld(int index)
	{
		return RunsafeServer.Instance.getWorld(this.getString(index));
 	}
 
	public RunsafeLocation getLocation(int index)
 	{
		RunsafeWorld world = this.getWorld(index);
		if (world != null)
			return new RunsafeLocation(world, getDouble(index + 1), getDouble(index + 2), getDouble(index +3));
 
		return null;
 	}
 
	public boolean isPlayer(int index)
 	{
		return this.getPlayer(index) != null;
 	}
 
 	List<LuaValue> parameters = new ArrayList<LuaValue>();
 }
