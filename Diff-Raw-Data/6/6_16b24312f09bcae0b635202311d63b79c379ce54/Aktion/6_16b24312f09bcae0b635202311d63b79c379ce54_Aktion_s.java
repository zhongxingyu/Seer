 
 
 /**
  * Klassenkommentar:
  * Hauptspiellogik bzw. was tun wenn was passiert
  * 
  */
 public class Aktion{
 
 private static int figurX;
 private static int figurY;
 private static int aktuellesLevel = 0;
 private static int aktuellerRaum =0;
 private static int newFigurX;
 private static int newFigurY;
 
 private static final int RECHTS = 0;
 private static final int UNTEN = 1;
 private static final int LINKS = 2;
 private static final int OBEN= 3;
 
 static boolean reachedCheckpoint;
 static int checkpointMerkeLevel;
 static int checkpointMerkeRaum;
 
 /**
  * Methode bewegt Figur anhand von Koordinaten
  * 
  */
 	
 	public void figurBewegen(int richtung){
 		
 		
 		if (richtung == RECHTS){ 
 			newFigurX=figurX+1;
 			newFigurY=figurY;
 		}	   
 		else if (richtung == UNTEN){ 
 			newFigurX=figurX;
 			newFigurY=figurY-1;
 		}
 		else if (richtung == LINKS){ 
 			newFigurX=figurX-1;
 			newFigurY=figurY;
 		}
 		else if (richtung == OBEN){ 
 			newFigurX=figurX;
 			newFigurY=figurY+1;
 		}
 		if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.BODEN)
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.FIGUR)
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==13) //13 und 14 sind Hilfselemente um an bestimmte Punkte zurueck zu kehren
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==14)){ //13: X steht in level.txt fuer die Stelle neben dem Ziel
 													                                                //14: Y steht in level.txt fuer die Stelle neben dem Checkpoint
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,newFigurX,newFigurY);
 			figurX=newFigurX;
 			figurY=newFigurY;
 		}
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.START)
 				&((aktuellerRaum!=0)|(aktuellesLevel!=0))){
 		
 			if (aktuellerRaum>0){
 				aktuellerRaum--;
 			}
 			else if(aktuellesLevel>0){
 				aktuellesLevel--;
 				aktuellerRaum=2;
 			}
 			Menu.levelDarstellen(aktuellesLevel,aktuellerRaum);
 			Menu.figurZumZiel(aktuellesLevel,aktuellerRaum);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.ZIEL){
 			if (aktuellerRaum<2){
 				aktuellerRaum++;
 			}
 			else {
 				aktuellesLevel++;
 				aktuellerRaum=0;
 			}
 			Menu.levelDarstellen(aktuellesLevel,aktuellerRaum);
 			Menu.figurReset(aktuellesLevel,aktuellerRaum, figurX, figurY);
 		}
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.FALLE)
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.MOB)){
 			
 			Figur.schadenBekommen(1);
			
 		}	
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.SIEG){
 			aktuellesLevel=0;
 			aktuellerRaum=0;
 			Menu.sieg();
 		}
 		
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.CHECKPOINT){
 			reachedCheckpoint=true;
 			checkpointMerkeLevel=aktuellesLevel;
 			checkpointMerkeRaum=aktuellerRaum;
 			Menu.displayPlayerStats();
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,newFigurX,newFigurY);
 			figurX=newFigurX;
 			figurY=newFigurY;
 			
 			
 			
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.STORYTELLER){
 		    Spielfeld.storyteller();
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.MUENZEN){
 			Figur.muenzen(1);
 			Spielfeld.wertSetzenBeiXY(aktuellesLevel, aktuellerRaum, newFigurX, newFigurY, Main.BODEN);
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,newFigurX,newFigurY);
 			
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.FARBEGELB){
 			Figur.setFarbe(Main.GELB);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.FARBEROT){
 			Figur.setFarbe(Main.ROT);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.FARBEBLAU){
 			Figur.setFarbe(Main.BLAU);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.HPTRANK){
 			Figur.heilen(1);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.MANATRANK){
 			Figur.manaReg(1);
 		}
 
 
 	}
 
 
 
 	
 	
 	
 
 /**
  * Methode setzt Figur
  * 
  */
 public static void setFigurXY(int x, int y){
 	figurX=x;
 	figurY=y;		
 }
 /**
  * 
  * Methode setzt das aktuelle Level
  * 
  */
 public static void setLevel(int level){
 	aktuellesLevel = level;
 }
 public static void setRaum(int raum){
 	aktuellerRaum = raum;
 }
 public static int getFigurX(){
 	return figurX;
 }
 public static int getFigurY(){
 	return figurY;
 }
 public static int getLevel(){
 	return aktuellesLevel;
 }
 public static int getRaum(){
 	return aktuellerRaum;
 }
 public static void zumCheckpoint(){
 	if (reachedCheckpoint == true){
 		Figur.resetHP();
 		aktuellesLevel=checkpointMerkeLevel;
 		aktuellerRaum=checkpointMerkeRaum;
 		Menu.levelDarstellen(aktuellesLevel,aktuellerRaum);
 		Menu.figurZumCheckpoint(aktuellesLevel,aktuellerRaum);
 		reachedCheckpoint=false;
 		Menu.displayPlayerStats();
 	}
 	else{
 		Figur.resetHP();
		Menu.levelDarstellen(aktuellesLevel, 0);
 	}
 }
 
 }
 
