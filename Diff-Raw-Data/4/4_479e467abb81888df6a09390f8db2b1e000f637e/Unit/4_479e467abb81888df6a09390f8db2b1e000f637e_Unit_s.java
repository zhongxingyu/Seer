 package units;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import server_battle.Position;
 import units.Unit;
 
 public abstract class Unit {
 	private static final AtomicInteger uniqueIdCounter = new AtomicInteger();
 	
 	// unique id
 	public final int uniqueId, buildTime;
 	//protected final ToDoQueue toDoQueue;
 	protected long updateOrder;
 	private final String name;
 	
 	private Position position;
 	
 	public Unit (/*ToDoQueue toDoQueue,*/ String name, int buildTime)	{
 		uniqueId = uniqueIdCounter.getAndIncrement();
 //		this.toDoQueue = toDoQueue;
 		this.buildTime = buildTime;
 		this.name = name;
 	}
 	
 	public Position getPosition () {
 		return position;
 	}
 	
	public void setPosition () {
		
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
