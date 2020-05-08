 import java.io.FileReader;
 import java.io.IOException;
 
 
 public class Wall {
 
 	public static void checkp() throws IOException, InterruptedException{
 		
 
 	    // Wie bei GetRoom nur diesmal zum berprfen ob wir an einer Wand sind
 	    FileReader fr = new FileReader("src/Raum" + Globals.level + "-" + Globals.room + ".txt");
 	    int ch;
 	    int j=0;
 	    int k=0;
 	    double x=Globals.xneu;
 	    double y=Globals.yneu;
 	    double s;
 	    double t;
 	   
 	    while( j<21 ){
 	    	k=0;
 	    	while ((ch=fr.read()) != -1 &&k<22){
 	    		
 	    	
 	    	
 	    	if (ch==120 && Globals.wall){
 	    		s=k*0.05;
 	    		t=1-j*0.05;
 
 	    		if(s-0.05< x && x< s+0.05 && t-0.05<y && t+0.05>y) {	// Wenn wir eine Wand erreichen drfen wir in dieswe Richtung nicht weiter laufen.
 	    			j=21;												// Weil sonst nur die 2 Schleife beendet wird
 	    			
 	    			if (s-0.05< Globals.x && Globals.x< s+0.05 && t-0.05<Globals.y && t+0.05>Globals.y)
 	    			{
 	    				//In Mauer eingefroren, als Magician benutzt ->Tot
 	    				//Damages.setDamages();
 	    				Globals.x=Globals.startx;
 	            		Globals.y=Globals.starty;
 	            		
 	    			}
 	    			
 	    			break;	
 	    		}
 	    		
 	    	}
 	    	if (!Globals.wall && (x<=0.05 || y<=0.05 || y>=.95 || x>=.95)) //Verbiete wenigstens Spielfeldrand
 	    	{
 	    		//Verbiete Bewegung
 	    		j=21;
 	    		break;
 	    		
 	    		//Hinweis: Solange Magician aktiv, in ein Raumwechsel NICHT mglich
 	    	}
 	    	
 	    	k++;
 	    	}
 	    	
 	    	j++;
 	    		if (j==21){		//sollte keine Wand im Weg stehen speicher die neue Position, die spter gezeichnet wird.
 	    			
 	    			
 	    			//Teste, ob im Shop:
 	    			if (Globals.shop.draw && Math.abs(Globals.shop.x-Globals.xneu)<=0.05 && Math.abs(Globals.yneu-Globals.shop.y)<=0.05)
 	    			{
 	    				Shop.use();
 	    				
 	    			}
 	    			//Teste, ob Paket aufgnemommen
 	    			else if(Globals.packet.draw && Math.abs(Globals.xneu-Globals.packet.x)<=0.05 && Math.abs(Globals.yneu-Globals.packet.y)<=0.05){
 	    				Globals.packet.draw = false;
 	    				Globals.player.x= Globals.xneu;
 	            		Globals.player.y=Globals.yneu;
 	            		Protection.on();
 	            		
 	            			
 	    			}
 	    			else 
 	    			{
 	    				//Position aktualisieren
 	    				Globals.x=Globals.xneu;
 	    				Globals.y=Globals.yneu;
 	    			}
 	    		}
 	    }
 	    fr.close();
 		}
 
 	public static void checkf(int i) throws IOException{
 		
 
 		// Wie bei GetRoom nur diesmal zum berprfen ob wir an einer Wand sind
 	    FileReader fr = new FileReader("src/Raum" + Globals.level + "-" + Globals.room + ".txt");
 	    int ch;
 	    int j=0;
 	    int k=0;
 	    double x=Globals.falleneu[0];
 	    double y=Globals.falleneu[1];
 	   
 	    double s;
 	    double t;
 	   
 	    while( j<21 ){
 	    	k=0;
 	    	while ((ch=fr.read()) != -1 &&k<22){
 	    		
 	    	
 	    	
 	    	if (ch==120){
 	    		s=k*0.05;
 	    		t=1-j*0.05;
 	    		int r = (int) (Math.random()*8+1);
 	    		if(s-0.05< x && x< s+0.05 && t-0.05<y && t+0.05>y || x>0.99 || x<0.01 || y<0.01 || y>0.99 ){	// Wenn wir eine Wand erreichen dann soll die Falle ihre Richtung ndern
 	    			Globals.richtung[i]=r;
 	    			j=21;
 	    			break;	
 	    		}
 	    		
 	    			    }
 		
 	    			k++;}
 	    	
 	    		j++;
 	    		if (j==21){
 	    			
 	    			Globals.fallen[0+2*i]=Globals.falleneu[0];		// wenn die Falle in keiner Wand landet ndert sich die Richtung nicht und die neue Position wird gespeichert und spter gezeichnet.
 	    			Globals.fallen[1+2*i]=Globals.falleneu[1];
 	    			
 	    			
 	    		}
 	    		}
 	fr.close();}
 	
 	public static void checkb() throws IOException{ //boss
 		
 		FileReader fr = new FileReader("src/Raum" + Globals.level + "-" + Globals.room + ".txt");
 	    int ch;
 	    int j=0;
 	    int k=0;
 	    double x=Globals.boss_neu[0];
 	    double y=Globals.boss_neu[1];
 	   
 	    double s;
 	    double t;
 	   
 	    while( j<21 ){
 	    	k=0;
 	    	while ((ch=fr.read()) != -1 &&k<22){
 	    		
 	    	
 	    	
 	    	if (ch==120){
 	    		s=k*0.05;
 	    		t=1-j*0.05;
 	    		int r = (int) (Math.random()*8+1);
 	    		if(s-0.05< x && x< s+0.05 && t-0.05<y && t+0.05>y || x>0.99 || x<0.01 || y<0.01 || y>0.99 ){	// Wenn wir eine Wand erreichen dann soll die Falle ihre Richtung ndern
	    			Globals.richtung[1]=r;
 	    			j=21;
 	    			break;	
 	    		}
 	    		
 	    			    }
 		
 	    			k++;}
 	    	
 	    		j++;
 	    		if (j==21){
 	    			
 	    			Globals.boss[0]=Globals.boss_neu[0];		// wenn die Falle in keiner Wand landet ndert sich die Richtung nicht und die neue Position wird gespeichert und spter gezeichnet.
 	    			Globals.boss[1]=Globals.boss_neu[1];
 	    			
 	    			
 	    		}
 	    		}
 	fr.close();}
 
 }
 
