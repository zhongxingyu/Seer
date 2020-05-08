 package Standard;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JOptionPane;
 
 public class Tutorial implements KeyListener {
 	private int schritte, bomben, stage;
 	private Spielfeld sp;
 
 	public Tutorial(Spielfeld sp) {
 		this.sp = sp;
 		schritte = 0;
 		bomben = 0;
 		stage = 1;
 	}
 
 	public void run() {
 		int answer = JOptionPane
 				.showConfirmDialog(
 						Main.f.f,
 						"<html><body>&nbsp;&nbsp;&nbsp;&nbsp;Mit den Pfeiltasten kannst du den Bomberman bewegen.<br>&nbsp;&nbsp;&nbsp;&nbsp;Bewege ihn ein wenig.</body></html>",
 						"Hinweis", JOptionPane.DEFAULT_OPTION);
 	}
 
 	public void keyPressed(KeyEvent ke) {
 		switch (ke.getKeyCode()) {
 		case KeyEvent.VK_UP: {
 			if (stage == 1) {
 				schritte++;
 				if (schritte == 10) {
 					stage = 2;
 					int answer = JOptionPane
 							.showConfirmDialog(
 									Main.f.f,
 									"<html><body>&nbsp;&nbsp;&nbsp;&nbsp;Sehr gut. Mit der Leertaste kannst du nun auch Bomben legen.<br>&nbsp;&nbsp;&nbsp;&nbsp;Durch die Explosion von Bomben können zerstörbare Mauern zerstört und ein Bomberman getötet werden.<br>&nbsp;&nbsp;&nbsp;&nbsp;Lege also nun einige Bomben.</body></html>",
 									"Hinweis", JOptionPane.DEFAULT_OPTION);
 				}
 			}
 		}
 			break;
 		case KeyEvent.VK_DOWN: {
 			if (stage == 1) {
 				schritte++;
 				if (schritte == 10) {
 					stage = 2;
 					int answer = JOptionPane
 							.showConfirmDialog(
 									Main.f.f,
 									"<html><body>&nbsp;&nbsp;&nbsp;&nbsp;Sehr gut. Mit der Leertaste kannst du nun auch Bomben legen.<br>&nbsp;&nbsp;&nbsp;&nbsp;Durch die Explosion von Bomben können zerstörbare Mauern zerstört und ein Bomberman getötet werden.<br>&nbsp;&nbsp;&nbsp;&nbsp;Lege also nun einige Bomben.</body></html>",
 									"Hinweis", JOptionPane.DEFAULT_OPTION);
 				}
 			}
 		}
 			break;
 		case KeyEvent.VK_LEFT: {
 			if (stage == 1) {
 				schritte++;
 				if (schritte == 10) {
 					stage = 2;
 					int answer = JOptionPane
 							.showConfirmDialog(
 									Main.f.f,
									"<html><body>&nbsp;&nbsp;&nbsp;&nbsp;Sehr gut. Mit der Leertaste kannst du nun auch Bomben legen.<br>&nbsp;&nbsp;&nbsp;&nbsp;Durch die Explosion von Bomben können zerstörbare Mauern zerstört und ein Bomberman getötet werden.<br>&nbsp;&nbsp;&nbsp;&nbsp;Lege also nun einige Bomben.</body></html>",
 									"Hinweis", JOptionPane.DEFAULT_OPTION);
 				}
 			}
 		}
 			break;
 		case KeyEvent.VK_RIGHT: {
 			if (stage == 1) {
 				schritte++;
 				if (schritte == 10) {
 					stage = 2;
 					int answer = JOptionPane
 							.showConfirmDialog(
 									Main.f.f,
									"<html><body>&nbsp;&nbsp;&nbsp;&nbsp;Sehr gut. Mit der Leertaste kannst du nun auch Bomben legen.<br>&nbsp;&nbsp;&nbsp;&nbsp;Durch die Explosion von Bomben können zerstörbare Mauern zerstört und ein Bomberman getötet werden.<br>&nbsp;&nbsp;&nbsp;&nbsp;Lege also nun einige Bomben.</body></html>",
 									"Hinweis", JOptionPane.DEFAULT_OPTION);
 				}
 			}
 		}
 			break;
 		case KeyEvent.VK_SPACE: {
 			if (stage == 2) {
 				bomben++;
 				if (bomben == 5) {
 					sp.raster[sp.bm.bombs.get(sp.bm.counter - 1).getPosX()][sp.bm.bombs
 							.get(sp.bm.counter - 1).getPosY()] = 0;
 					sp.bm.bombs.get(sp.bm.counter - 1).tExp.cancel();
 					sp.bm.bombs.get(sp.bm.counter - 1).tUnExp.cancel();
 					sp.bm.bombs.remove(sp.bm.counter - 1);
 					int answer = JOptionPane
 							.showConfirmDialog(
 									Main.f.f,
 									"<html><body>&nbsp;&nbsp;&nbsp;&nbsp;Gute Arbeit Soldat!<br>&nbsp;&nbsp;&nbsp;&nbsp;Unter einer der zerstörbaren Mauern versteckt sich der Ausgang aus dieser Welt.<br>&nbsp;&nbsp;&nbsp;&nbsp;Finde und betrete ihn um das Tutorial zu beenden.</body></html>",
 									"Hinweis", JOptionPane.DEFAULT_OPTION);
 					stage = 3;
 				}
 			}
 			break;
 		}
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 }
