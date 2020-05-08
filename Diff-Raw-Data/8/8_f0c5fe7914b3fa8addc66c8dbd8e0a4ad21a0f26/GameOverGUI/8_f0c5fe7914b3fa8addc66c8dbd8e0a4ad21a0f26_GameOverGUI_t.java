 package pentagoxl.client;
 
 import javax.swing.JFrame;
 import javax.swing.JTextArea;
 
 public class GameOverGUI extends JFrame {
 
 	public GameOverGUI(String[] winnaars, boolean gewonnen) {
 		//String title = gewonnen ? "Contgrats!" : "Fail!";
 		super(gewonnen ? "Contgrats!" : "Fail!");
 		
 		JTextArea jta = new JTextArea();
 		String text = "Winnaars:";
 		for (String s : winnaars) {
 			text += "\n" + s;
 		}
 		jta.setText(text);
 		this.getContentPane().add(jta);
 		
		this.pack();
		
 		this.setVisible(true);
 	}
 }
