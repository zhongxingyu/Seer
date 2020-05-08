 package v1.Impl;
 
 import v1.Interface.Command;
 import v1.Interface.Horloge;
 
 /**
  * @(#) Moteur.java
  */
 
 public class Moteur {
 	
 	private Controller ctl;
 	private Horloge timer;
 	private Command tocTemps, tocMesure;
 	private boolean enMarche;
 	private int mesure, t;
 	private float tempo;
 
 	
 	public Moteur() {
 		timer = new HorlogeImpl();
 		
 		enMarche = false;
 		mesure = 4;
 		t = 0;
 	}
 	
 	public void setController(Controller ctl) {
 		this.ctl = ctl;
 	}
 	
 	public void setTocTemps(Command tocTemps) {
 		this.tocTemps = tocTemps;
 	}
 	
 	public void setTocMesure(Command tocMesure) {
 		this.tocMesure = tocMesure;
 	}
 	
 	public void toc() {
 		t++;
 		calcMesure();
 	}
 	
 	private void calcMesure() {
 		tocTemps.execute();
 		if (t >= mesure) {
 			tocMesure.execute();
 			t = 0;
 		}
 	}
 	
 	public boolean getEtatMarche() {
 		return enMarche;
 	}
 
 	public void setEnMarche(boolean actif) {
 		enMarche = actif;
 		
 		if (enMarche) {
 			float delay = 1000 / (tempo / 60);
 			timer.activerPeriodiquement(new Toc(this), delay);
 		} else {
 			timer.desactiver();
 			t = 0;
 		}
 	}
 
 	public void inc() {
 		if (mesure < Metronome.MESURE_MAX)
 			mesure++;
 	}
 
 	public void dec() {
 		if (mesure > Metronome.MESURE_MIN)
 			mesure--;
 	}
 
 	public void setTempo(float tempo) {
 		this.tempo = tempo;
		if (enMarche) {
			timer.desactiver();
			float delay = 1000 / (tempo / 60);
			timer.activerPeriodiquement(new Toc(this), delay);
		}
 	}
 
 	public float getTempo() {
 		return tempo;
 	}
 	
 	public void setMesure(int mesure) {
 		if (mesure >= 2 && mesure <= 7)
 			this.mesure = mesure;
 	}
 	
 	public int getMesure() {
 		return mesure;
 	}
 }
