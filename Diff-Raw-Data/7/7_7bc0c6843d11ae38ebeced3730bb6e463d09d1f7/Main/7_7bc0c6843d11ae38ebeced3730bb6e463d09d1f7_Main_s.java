 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 
 import model.GuessMyWordModel;
 import model.Memory;
 import view.GuessPanel;
 import view.UserInterface;
 
 public class Main {
 	
 	public static void main(String[] args) {
 		Main main = new Main();
 		main.init();
 	}
 	
 	private void init() {
 		Memory model = new Memory();
 		UserInterface panel = new UserInterface(model);
 
 		JFrame frame = new JFrame("Guess My Word"); // s�tt n�got annat h�r
 		frame.add(panel);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(new Dimension(500, 600));
 		frame.setVisible(true);
<<<<<<< HEAD
 		////TESTGITT
 		///Removed by Kev
=======

>>>>>>> cf68e6b82c94db4063ffd51822bcd703a982c3dd
 	}
 }
