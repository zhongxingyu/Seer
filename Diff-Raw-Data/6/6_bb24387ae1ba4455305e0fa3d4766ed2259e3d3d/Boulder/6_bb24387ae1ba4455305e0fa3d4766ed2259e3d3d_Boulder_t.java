 package Items;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 
 import Core.GUI;
 import Core.MainGame;
 import Core.Player;
 
 
 
 public class Boulder extends Item implements PickUpItem {
 
 	public Boulder(BufferedImage image) {
 		super(image);
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
 	public void playerConsume(Player p) {
 		int r = JOptionPane.showConfirmDialog(null, new JLabel("Game Over!\n Play Again?"),
 				"Game Over!", JOptionPane.YES_NO_OPTION,
 				JOptionPane.WARNING_MESSAGE);
 		if (r == 0){
 			//New Game
 			GUI.canvas.removeAll();
			GUI.canvas.getGUI().dispose();
 			Player.sobriety = Player.MAX_SOBRIETY;
 			Player.score = 0;
 			GUI gui = new GUI();
		} else{
			System.exit(1);
		}
 		MainGame.running = false;
 		try {
 			Thread.sleep(5000);
 			System.exit(1);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 
 }
 	
 }
