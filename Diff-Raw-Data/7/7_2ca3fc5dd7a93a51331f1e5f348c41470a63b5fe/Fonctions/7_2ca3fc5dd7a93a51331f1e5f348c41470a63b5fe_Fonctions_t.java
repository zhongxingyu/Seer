 public class Fonctions {
 	static Audio a=new Audio();
 	
 	public static void init(Fenetre f, Mot m, Chrono c){
 		f.setMot(m.getMot());
 	}
 	public static void repaintAndPause(Fenetre f, Chrono c){
 		Plateau.boucleMots=true;
 		Plateau.boucleJeu=true;
 		Fenetre.finJeu=true;
 		f.repaint();
 		c.pause();
 	}
 	public static void repaintAndPauseAndDispose(Fenetre f, Chrono c, Mot m){
 		Plateau.boucleMots=true;
 		Fenetre.finMot=true;
		if(m.getMot().equals(f.getTest())){
 			f.repaint();
			Fenetre.mots++;
		}
 		c.pause();
 		f.dispose();
 	}
 	public static void wait8ms(){
 		try {
 			Thread.sleep(8);
 		} catch (InterruptedException e) {}
 	}
 	public static void update(Fenetre f, Mot m, Plateau p) {
 		if(f.getTest().length()==0)
 			f.setTest("x");
 		else
 			f.setTest(f.getJtf().getText());
 		p.updateAll(m.getDico(), m.getAlphabet(), f.getTest(), m.getMot());
 		f.repaint();
 		System.out.println(p.toString(m.getMot()));
 		MyKey.go=false;
 	}
 	public static void gereChrono(Chrono c, Fenetre f) {
 		c.setChrono();
 		Chrono.minutes=c.updateMin();
 		Chrono.secondes=c.updateSec();
 		f.repaint();
 		if(c.getMinutes()==-1){
 			Plateau.boucleMots=true;
 			Plateau.boucleJeu=true;
 			Fenetre.finJeu=true;
 		}
 	}
 	public static boolean isNumeric(String s) {  
 	    return s.matches("[-+]?\\d*\\.?\\d+");
 	}
 	public static void testFin(Mot m, int cptEssais, Fenetre f, Chrono c) {
 		if(m.getMot().equals(f.getTest()) || cptEssais==7){
 			if(m.getMot().equals(f.getTest()))
 				a.sound();
 			if(Fenetre.mots==10 && m.getMot().equals(f.getTest()))
 				Fonctions.repaintAndPause(f, c);
 			else
 				Fonctions.repaintAndPauseAndDispose(f, c, m);
 		}
 	}
 }
