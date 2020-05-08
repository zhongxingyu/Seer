 package concubattles;
 
 import java.io.Serializable;
 
 public class Soldier implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	int team;
 	int level;
 	int experience;
 	int experienceToNextLevel;
 	Battle myPlace;
 	Battle next_place;
 	Battle previous_place;
 	boolean live = false;
 
 	public Soldier(int team) {
 		super();
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
	
 	public void run(){
 		Battle place = this.myPlace;
 		place.getControlChannel();
 		SoldierState state = place.getMyState(this);
 		switch (state) {
 		case dead:
 			this.live = False;
 			break;
 		case live:
 			Battle next_place = this.place.get_nect_place(this.previous_place);
 			next_place.ConctrolChannel().reveicve();
 			next_place.add(this);
 			this.place.remove(this);
 			next_place.controlChannel().send()
 			place.controlChannel().send()
 			break;		
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
 	// public static void main(String[] args) {
 	// Soldier x = new Soldier();
 	// System.out.println("soldado nuevo");
 	// System.out.println(x.experience);
 	// System.out.println(x.experienceToNextLevel);
 	// System.out.println("mato 1 solado");
 	// x.experienceUp();
 	// System.out.println(x.level);
 	// System.out.println(x.experienceToNextLevel);
 	// System.out.println("mato al segundo soldado");
 	// x.experienceUp();
 	// System.out.println(x.level);
 	// System.out.println(x.experience);
 	// System.out.println(x.experienceToNextLevel);
 	// System.out.println("mato al tercer soldado");
 	// x.experienceUp();
 	// System.out.println(x.level);
 	// System.out.println(x.experience);
 	// System.out.println(x.experienceToNextLevel);
 	// }
 }
