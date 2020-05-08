 package kartenspiel;
 
 public class Stapel {
 	private Karte[] karten;		//Zur Verwaltung der Karten
 	private int pos;
 	
 	public Stapel(){
 		int counter = 0;
 		karten = new Karte[52]; // nicht vergessen das Array zu initialisieren!
 		
 		for (int i = 0; i < 4; i++) { // Farben
 			for (int j = 2; j < 15; j++) { // Werte
 				karten[counter] = new Karte(j, i);
 				counter++;
 			}
 		}
 	}
 	
 	public void mischen(){
 		for (int i = 0; i < 100; i++) {
 			int random1 = (int)(Math.random()*51);
 			int random2 = (int)(Math.random()*51);
 			Karte temp = karten[random1];
 			karten[random1] = karten[random2];
 			karten[random2] = temp;
 		}
 		this.pos = 0;
 	}
 	
 	public Karte getNextCard(){
		if(pos<51){
 			pos++;
 		}
 		else{
 			this.mischen();
 		}
 		return this.karten[pos];
 	}
 }
