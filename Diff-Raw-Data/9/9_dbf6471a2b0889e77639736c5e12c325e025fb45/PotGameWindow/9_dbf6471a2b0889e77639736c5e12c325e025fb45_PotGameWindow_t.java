 package me.queekus.programs.ThePotionGame;
 import javax.swing.*;
 
 import me.queekus.programs.ThePotionGame.Objects.GameObject;
 import me.queekus.programs.ThePotionGame.Objects.Potion;
 
 import java.awt.event.*;
 
 public class PotGameWindow  extends JFrame implements KeyListener{
 	JPanel pnl = new JPanel();
 	
 	JTextArea Output = new JTextArea(10,38);
 	JTextField Input = new JTextField(38);
 	
 	JScrollPane pane = new JScrollPane(Output);
 			
 	public PotGameWindow(String title, int x, int y){
 		super(title);
 		setSize(x,y);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		add(pnl);
 		setVisible(true);
 		
 		Output.setLineWrap(true);
 		Output.setWrapStyleWord(true);
		Output.append("Click on the box below, enter your text and press ENTER to submit the command");
 		
 		Input.addKeyListener(this);
 		
 		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		pnl.add(pane);
 		pnl.add(Input);
 	}
 
 
 	@Override
 	public void keyPressed(KeyEvent event) {
 		if (event.getKeyCode() == KeyEvent.VK_ENTER){
 			try{
 			    //BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
 			    String s = ThePotionGame.gui.Input.getText();//bufferRead.readLine();
 			    
 			    String[] args = s.split(" ");
 			    
 			    switch(args[0]){
 			    	case "add":
 			    		switch(args[1]){
 				    	case "wings":
 				    		ThePotionGame.gui.write("add wings");
 				    		if (ThePotionGame.Cauldron.addToCauldron(GameObject.flyWings)){
 				    		}
 				    		break;
 			    		}
 			    		break;
 			    	case "bottle":
 			    		ThePotionGame.gui.write("bottle");
 				    	Potion result = ThePotionGame.Cauldron.bottlePotion();
 					    ThePotionGame.gui.write("Potion Successful,\nPotion Name = " + result.name + "\nEffect = " + result.statValue + " " + result.statType);
 			    		break;
 			    }
 			    
 			    
 			}
 			finally{
 				ThePotionGame.gui.Input.setText("");
 			}
 		}else{
 			//Output.append("\n" + event.getKeyCode());
 		}
 		
 	}
 
 
 	@Override
 	public void keyReleased(KeyEvent event) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void write(String text){
 		Output.append("\n" + text);
 	}
 	
 }
