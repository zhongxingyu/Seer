 package graindcafe.tribu.Configuration;
 
public enum FocusType {
 	None,
 	NearestPlayer,
 	RandomPlayer,
 	InitialSpawn,
 	DeathSpawn;
 	
 	public String toString(){
 		return name().toString();
 	}
	
 	public static FocusType fromString(String name)
 	{
 		if(name==null)
 			return FocusType.None;
 		else if(name.toLowerCase().startsWith("near"))
 			return FocusType.NearestPlayer;
 		else if(name.toLowerCase().startsWith("rand"))
 			return FocusType.RandomPlayer;
 		else if(name.toLowerCase().startsWith("init"))
 			return FocusType.InitialSpawn;
 		else if(name.toLowerCase().startsWith("death"))
 			return FocusType.DeathSpawn;
 		else
 			return FocusType.None;
 	}
 }
 
