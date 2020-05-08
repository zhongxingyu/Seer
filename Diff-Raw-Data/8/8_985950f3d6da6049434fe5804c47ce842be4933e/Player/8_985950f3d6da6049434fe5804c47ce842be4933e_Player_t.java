 package com.admteal.dndhp;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 //TODO: MOVE THE MATH TO THIS CLASS RATHER THAN DNDHPActivity.java
 public class Player implements Serializable {
 	private static final long serialVersionUID = -736811401990731225L; //generated
 	
 	private boolean blinded, dazed, deafened, dominated, dying, grabbed, 
 		helpless, immobile, marked, petrified, prone, restrained, 
 		stunned, unconscious, weakened = false;
 		
     // The player's various stats and HPs are tracked in this class.
 
 	private int maxHP;
     private int currentHP, currentHS, currentOngo, currentSurges, currentDeathSaves, bloodied;
     /* 
      * Note: class uses int division because we round down in D&D.
      * 32 should equal 1.
      */
     private ArrayList<Integer> changeHistory = new ArrayList<Integer>();
     private ArrayList<Integer> HPHistory = new ArrayList<Integer>();
     
     //default constructor for if we aren't passing a real max hp
     public Player() {
     	maxHP = 999;
      	bloodied = 499;
     	currentHP = 0;
     	currentHP = 0;
     	currentHS = 0;
      	currentOngo = 0;
      	currentSurges = 0;
      	currentDeathSaves = 3;
     }
     
     //create a new player at full HP
     public Player(int newHP, int newSurges) {
     	currentHP = newHP;
         maxHP = newHP;
         bloodied = newHP / 2;
         currentSurges = newSurges;
     }
     
     //create a new player at less than full HP
     public Player(int newHP, int newMaxHP, int newSurges) {
     	currentHP = newHP;
     	currentHP = newMaxHP;
         bloodied = newMaxHP / 2;
         currentSurges = newSurges;
     }
 
     // TODO: TRACK HP ABOVE MAX HP AS TEMPORARY HP?  SOMETHING NEEDS TO BE DONE TO ACCOUNT FOR THP
     
     public void heal(int healBy) {
     	if (currentHP + healBy > maxHP) { //No going above max.
     		changeHistory.add(maxHP-currentHP);
     		currentHP = maxHP;
     	} else {
         	currentHP += healBy;
         	changeHistory.add(healBy);
     	}
     	dying = false;
     	unconscious = false;
     	HPHistory.add(currentHP);
     }
     
     public void injure(int injureBy) {
     	int negBloodied = bloodied * -1;
     	//injureBy was passed as a negative number.  Treat it using addition for subtraction.
     	if (currentHP - injureBy <= negBloodied) { //No going below negative bloodied (you're dead)
     		changeHistory.add(negBloodied - currentHP);
     		currentHP = negBloodied;
     	} else {
     		currentHP -= injureBy;
     		changeHistory.add(-injureBy); //We store injuries as a negative number in the history.
     	}
     	HPHistory.add(currentHP);
     }
 
     //HP VALUE
     public int getHP() {
         return currentHP;
     }
 
     public void setHP(int newHP) {
         currentHP = newHP;
     }
 
     //MAX HP VALUE
     public int getMaxHP() {
         return maxHP;
     }
 
     public void setMaxHP(int newMaxHP) {
         maxHP = newMaxHP;
         bloodied = maxHP / 2; 
     }
     	
     	public void setMaxHP(int newMaxHP, int newBloodied) {
     		maxHP = newMaxHP;
     		bloodied = newBloodied;
     	}
     	
     //BLOODIED VALUE
     public int getBloodied() {
     	return bloodied;
     }
     
     public void setBloodied(int newBloodied) {
     	bloodied = newBloodied;
     }
 
     //SURGES COUNT
     public int getSurges() {
         return currentSurges;
     }
 
     public void setSurges(int newSurges) {
     	if (newSurges >= 0) {
             currentSurges = newSurges;
     	}
     }
     
     public void addSurge() {
     	currentSurges++;
     }
     
     public void remSurge() {
     	if (currentSurges == 0) {
     		return;
     	}
     	currentSurges--;
     }
     
     //SURGE VALUE
     public void setHS(int newHS) {
     	currentHS = newHS;
     }
     
     public int getHS() {
     	return currentHS;
     }
     
     //ONGOING VALUE
     public void setOngo(int newOngo) {
     	currentOngo = newOngo;
     }
     
     public int getOngo() {
     	return currentOngo;
     }
     	
    //regen is the opposite of ongoing damage
    public int getRegen() { 
        return currentOngo * -1;
    }
 	    
     public void addOngo() {
     	currentOngo++;
     }
     
     public void remOngo() {
     	currentOngo--;
     }
     
     //DEATH SAVES COUNT
     public void setDeathSaves(int newDeathSaves) {
     	currentDeathSaves = newDeathSaves;
     }
     
     public int getDeathSaves() {
     	return currentDeathSaves;
     }
     
     public void addDeathSave() {
     	currentDeathSaves++;
     }
     
     public void remDeathSave() {
     	currentDeathSaves--;
     }
     
     //STATUS EFFECTS    
 	public boolean isBlinded() {return blinded;}
 	public void blind() {
 		blinded = true;
 	}
 	public void unblind() {
 		blinded = false;
 	}
 	
 	public boolean isDazed() {return dazed;}
 	public void daze() {
 		dazed = true;
 	}
 	
 	public void undaze() {
 		dazed = false;
 	}
 	
 	public boolean isDeafened() {return deafened;}
 	public void deafen() {
 		deafened = true;
 	}
 	public void undeafen() {
 		deafened = false;
 	}
 	
 	public boolean isDominated() {return dominated;}
 	public void dominate() {
 		dominated = true;
 	}
 	public void undominate() {
 		dominated = false;
 	}
 	
 	public boolean isDying() {return dying;}
 	public void kill() {
 		dying = true;
 	}
 	public void unkill() {
 		dying = false;
 	}
 	
 	public boolean isGrabbed() {return grabbed;}
 	public void grab() {
 		grabbed = true;
 	}
 	public void ungrab() {
 		grabbed = false;
 	}
 	
 	public boolean isHelpless() {return helpless;}
 	public void incapacitate() {
 		helpless = true;
 	}
 	public void unincapacitate() {
 		helpless = false;
 	}
 	
 	public boolean isImmobile() {return immobile;}
 	public void immobilize() {
 		immobile = true;
 	}
 	public void unimmobilize() {
 		immobile = false;
 	}
 	
 	public boolean isMarked() {return marked;}
 	public void mark() {
 		marked = true;
 	}
 	public void unmark() {
 		marked = false;
 	}
 	
 	public boolean isPetrified() {return petrified;}
 	public void petrify() {
 		petrified = true;
 	}
 	public void unpetrify() {
 		petrified = false;
 	}
 	
 	public boolean isProne() {return prone;}
 	public void knockProne() {
 		prone = true;
 	}
 	public void getUp() {  //NOTE THIS BREAKS THE FORM BECAUSE unknockProne SOUNDS STUPID
 		prone = false;
 	}
 	
 	public boolean isRestrained() {return restrained;}
 	public void restrain() {
 		restrained = true;
 	}
 	public void unrestrain() {
 		restrained = false;
 	}
 	
 	public boolean isStunned() {return stunned;}
 	public void stun() {
 		stunned = true;
 	}
 	public void unstun() {
 		stunned = false;
 	}
 	
 	public boolean isUnconscious() {return unconscious;}
 	public void knockOut() {
 		unconscious = true;
 	}
 	public void wakeUp() {  //NOTE THIS BREAKS THE FORM BECAUSE unknockOut SOUNDS STUPID
 		unconscious = false;
 	}
 	
 	public boolean isWeakened() {return weakened;}
 	public void weaken() {
 		weakened = true;
 	}
 	public void unweaken() {
 		weakened = false;
 	}
     
 	//History trackers
     public ArrayList<Integer> getChangeHistory() {
     	return changeHistory;
     }
     
     public ArrayList<Integer> getHPHistory() {
     	return HPHistory;
     }
 }
