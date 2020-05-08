 import java.awt.event.KeyEvent;
import java.io.IOException;
 
 public class GotchiScreen implements IScreen {
 	private Gotchi gotchi = null;
 
 	public GotchiScreen(Gotchi gotchi) {
 		this.gotchi = gotchi;
 
 		/*
 		 * char test = ' '; try { test = (char)System.in.read(); } catch
 		 * (IOException e) { } System.out.print(test);
 		 */
 	}
 
 	@Override
 	public void Draw() {
 		// TODO Auto-generated method stub
 
 		String[] gotchiArt = gotchi.getImage();
 
 		// print image
 		for (int i = 0; i < gotchiArt.length; i++) {
 			System.out.println("║" + centerString(gotchiArt[i], 80 - 2) + "║");
 		}
 		// fill up to always have 15 lines for image
 		for (int i = gotchiArt.length; i < 15; i++) {
 			System.out.println("║                                                                              ║");
 		}
 
 		// print the Footer
 		String formatStarvation = padLeft(String.valueOf(gotchi.getStarvation()), 3);
 		System.out.println("╟─────────────┬────────────────┬─────────────────┬────────────┬────────────────╢");
 		System.out.println("║ Hunger: " + formatStarvation + " │ Müdigkeit: 000 │ Langeweile: 000 │ Alter: 000 │     Aktion [E] ║");
 		System.out.println("╚═════════════╧════════════════╧═════════════════╧════════════╧════════════════╝");
 	}
 
 	private String centerString(String content, int len) {
 		content = padLeft(content, (len + content.length()) / 2);
 		content = padRight(content, len);
 		return content;
 	}
 
 	private String padLeft(String content, int len) {
 		while (content.length() < len) {
 			content = " " + content;
 		}
 		return content;
 	}
 
 	private String padRight(String content, int len) {
 		while (content.length() < len) {
 			content = content + " ";
 		}
 		return content;
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		switch (e.getKeyCode()) {
 		case 69: // e
 			System.out.println("e");
 		case 37: // left
 		case 38: // up
 		case 39: // right
 		case 40: // down
 		}
 	}
 }
