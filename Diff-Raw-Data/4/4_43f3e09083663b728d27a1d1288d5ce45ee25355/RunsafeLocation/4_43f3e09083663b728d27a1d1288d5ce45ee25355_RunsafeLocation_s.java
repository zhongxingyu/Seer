 package no.runsafe.framework.server;
 
 import no.runsafe.framework.server.block.RunsafeBlock;
 import no.runsafe.framework.server.chunk.RunsafeChunk;
 import org.bukkit.Location;
 import org.bukkit.util.Vector;
 
 public class RunsafeLocation
 {
 	public RunsafeLocation(Location toWrap)
 	{
 		location = toWrap;
 	}
 
 	public RunsafeLocation(RunsafeWorld world, double x, double y, double z)
 	{
 		location = new Location(world.getRaw(), x, y, z);
 	}
 
 	public RunsafeLocation(RunsafeWorld world, double x, double y, double z, float yaw, float pitch)
 	{
 		location = new Location(world.getRaw(), x, y, z, yaw, pitch);
 	}
 
 	public RunsafeLocation top()
 	{
 		Location target = location.getWorld().getHighestBlockAt(location).getLocation();
 		target.setPitch(location.getPitch());
 		target.setYaw(location.getYaw());
 		target.setX(location.getX());
 		target.setZ(location.getZ());
 		return ObjectWrapper.convert(target);
 	}
 
 	public double getX()
 	{
 		return location.getX();
 	}
 
 	public double getY()
 	{
 		return location.getY();
 	}
 
 	public double getZ()
 	{
 		return location.getZ();
 	}
 
 	public void setYaw(float yaw)
 	{
 		location.setYaw(yaw);
 	}
 
 	public void setPitch(float pitch)
 	{
 		location.setPitch(pitch);
 	}
 
 	public float getYaw()
 	{
 		return location.getYaw();
 	}
 
 	public float getPitch()
 	{
 		return location.getPitch();
 	}
 
 	public void setX(double x)
 	{
 		this.location.setX(x);
 	}
 
 	public void setY(double y)
 	{
 		this.location.setY(y);
 	}
 
 	public void setZ(double z)
 	{
 		this.location.setZ(z);
 	}
 
 	public RunsafeWorld getWorld()
 	{
 		return new RunsafeWorld(location.getWorld());
 	}
 
 	public int getBlockX()
 	{
 		return location.getBlockX();
 	}
 
 	public int getBlockY()
 	{
 		return location.getBlockY();
 	}
 
 	public int getBlockZ()
 	{
 		return location.getBlockZ();
 	}
 
 	public Location getRaw()
 	{
 		return location;
 	}
 
 	public RunsafeChunk getChunk()
 	{
 		return ObjectWrapper.convert(this.location.getChunk());
 	}
 
 	public RunsafeBlock getBlock()
 	{
 		return ObjectWrapper.convert(this.location.getBlock());
 	}
 
 	public Vector getDirection()
 	{
 		return this.location.getDirection();
 	}
 
 	public RunsafeLocation add(RunsafeLocation vec)
 	{
 		return ObjectWrapper.convert(this.location.add(vec.getRaw()));
 	}
 
 	public RunsafeLocation add(double x, double y, double z)
 	{
 		return ObjectWrapper.convert(this.location.add(x, y, z));
 	}
 
 	public RunsafeLocation subtract(RunsafeLocation vec)
 	{
 		return ObjectWrapper.convert(this.location.subtract(vec.getRaw()));
 	}
 
 	public RunsafeLocation subtract(Vector vec)
 	{
 		return ObjectWrapper.convert(this.location.subtract(vec));
 	}
 
 	public RunsafeLocation subtract(double x, double y, double z)
 	{
 		return ObjectWrapper.convert(this.location.subtract(x, y, z));
 	}
 
 	public double length()
 	{
 		return this.location.length();
 	}
 
 	public double lengthSquared()
 	{
 		return this.location.lengthSquared();
 	}
 
 	public double distance(RunsafeLocation location)
 	{
 		return this.location.distance(location.getRaw());
 	}
 
 	public double distanceSquared(RunsafeLocation location)
 	{
 		return this.location.distanceSquared(location.getRaw());
 	}
 
 	public RunsafeLocation multiply(double m)
 	{
 		return ObjectWrapper.convert(this.location.multiply(m));
 	}
 
 	public RunsafeLocation zero()
 	{
 		return ObjectWrapper.convert(this.location.zero());
 	}
 
 	public void incrementX(double x)
 	{
 		this.setX(this.getX() + x);
 	}
 
 	public void incrementY(double y)
 	{
 		this.setY(this.getY() + y);
 	}
 
 	public void incrementZ(double z)
 	{
 		this.setZ(this.getZ() + z);
 	}
 
 	public void decrementX(double x)
 	{
 		this.setX(this.getX() - x);
 	}
 
 	public void decrementY(double y)
 	{
 		this.setY(this.getY() - y);
 	}
 
 	public void decrementZ(double z)
 	{
 		this.setZ(this.getZ() - z);
 	}
 
 	public void offset(double x, double y, double z)
 	{
 		if (x > 0) this.incrementX(x); else this.decrementX(x);
		if (y > 0) this.incrementX(y); else this.decrementX(y);
		if (z > 0) this.incrementX(z); else this.decrementX(z);
 	}
 
 	private final Location location;
 }
