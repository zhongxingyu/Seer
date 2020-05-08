 package concubattles;
 
public class Soldier extends Thread {
	
 	private Castle team;
 	private int level;
 	private int experience;
 	private int experienceToNextLevel;
 	private Place my_place;
 	private Place previous_place;
 	private boolean live;
 
 	public Castle getTeam() {
 		return team;
 	}
 
 	public void setTeam(Castle team) {
 		this.team = team;
 	}
 
 	public Place getMy_place() {
 		return my_place;
 	}
 
 	public void setMy_place(Place my_place) {
 		this.my_place = my_place;
 	}
 
 	public Place getPrevious_place() {
 		return previous_place;
 	}
 
 	public void setPrevious_place(Place previous_place) {
 		this.previous_place = previous_place;
 	}
 
 	public void setLevel(int level) {
 		this.level = level;
 	}
 
 	public void setLive(boolean live) {
 		this.live = live;
 	}
   
 	public boolean getLive(){
 		return this.live;
 	}
 	
 	public int getLevel(){
 		return this.level;
 	}
 
 	public Soldier(Place my_place, Castle team) {
 		this.my_place = my_place;
 		this.team = team;
 		this.level = 1;
 		this.experience = 0;
 		this.experienceToNextLevel = 1;
 	}
 
 	public int fibonacci(int e) {
 
 		int f1 = 1;
 		int f2 = 0;
 		int f = 0;
 
 		for (int i = 1; i < e; i++) {
 
 			f = f1 + f2;
 			f2 = f1;
 			f1 = f;
 
 		}
 		return f;
 
 	}
 
 	public void levelUp() {
 		this.level = this.level + 1;
 	}
 
 	public Place getMyPlace() {
 		return this.my_place;
 	}
 
 	public boolean isLive() {
 		return this.live;
 	}
     public void checkForLevel(){                       // SI EL LEVEL DEL SOLDADO ES MAYOR A 1
     	if (this.getLevel() > 1){                     // CUANDO ESTE MUERE SE TIENE QUE CREAR OTRO
     		this.getTeam().createSoldier();
     	}
     }
 	public void run() {
 		while (this.live) {
 			Place place = this.getMyPlace();
 			Place next_place = place.getNextPlace(this.previous_place);
 			if (!(place instanceof Way)) {
 				place.getPermission();
 				next_place.getPermission();
 			} else {
 				next_place.getPermission();
 				place.getPermission();
 			}
 			if (this.isLive()) {
 				next_place.receive(this);
 				place.remove(this);
 				next_place.returnPermission();
 				place.returnPermission();				
 			} else {
 				next_place.returnPermission();
 				place.returnPermission();
 				break;
 			}
 
 		}
 	}
 
 	/**
 	 * funcion Fibonacci para la experiencia cada soldado gana 1 de experiencia
 	 * por matar a un soldado enemigo..
 	 */
 	public void experienceUp() {
 
 		this.experience = this.experience + 1;
 		if (this.experience == this.experienceToNextLevel) {
 			this.levelUp();
 			this.experienceToNextLevel = this.fibonacci(this.level);
 			this.experience = 0;
 		}
 
 	}
 
 }
