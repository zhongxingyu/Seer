 package units;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import server_battle.Position;
 import units.Unit;
 
 public abstract class Unit {
 	private static final AtomicInteger uniqueIdCounter = new AtomicInteger();
 	
 	public final int uniqueId, buildTime;
 	protected long updateOrder;
 	private final String name;
 	
 	private Position position;
 	
 	public Unit (String name, int buildTime)	{
 		uniqueId = uniqueIdCounter.getAndIncrement();
 		this.buildTime = buildTime;
 		this.name = name;
 	}
 	
 	public Position getPosition () {
 		return position;
 	}
	
<<<<<<< HEAD
	public void setPosition (Position position) {
		position.
=======
 	public Position setPosition (Position newPosition) {
 		return position = newPosition;
>>>>>>> acdc89e6827cfa140e222c430882f37bf4355069
 	}
 	
 	public void update(){isupdaterequired();}
 	public boolean isupdaterequired(){return false;}
 	
 	public String getName() {
 		return name;
 	}
 	public int getbuildTime() {
 		return buildTime;
 	}
 }
