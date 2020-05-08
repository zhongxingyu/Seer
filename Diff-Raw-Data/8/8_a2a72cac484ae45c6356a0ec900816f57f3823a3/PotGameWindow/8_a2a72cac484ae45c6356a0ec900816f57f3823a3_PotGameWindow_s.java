 package me.queekus.programs.ThePotionGame;
 import javax.swing.*;
 
 import me.queekus.programs.ThePotionGame.Objects.*;
 import me.queekus.programs.ThePotionGame.api.CauldronRecipe;
 
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
 			    String s = ThePotionGame.gui.Input.getText();
 			    String[] args = s.split(" ");
 			    
 			    if(args[0].equals("add")){
 		    		if (args[1].equals("wings")){
 			    		ThePotionGame.gui.write("add wings");
 			    		ThePotionGame.Cauldron.addToCauldron(GameObject.flyWings);
 		    		}else if (args[1].equals("flowers")){
 			    		ThePotionGame.gui.write("add flowers");
 			    		ThePotionGame.Cauldron.addToCauldron(GameObject.flowers);
 		    		}else if (args[1].equals("plague")){
 			    		ThePotionGame.gui.write("add plague");
 			    		ThePotionGame.Cauldron.addToCauldron(GameObject.plagueSample);
 		    		}else if (args[1].equals("heat100")){
 			    		ThePotionGame.gui.write("add heat100");
 			    		ThePotionGame.Cauldron.addToCauldron(GameObject.temperature100);
 		    		}
 			    }else if (args[0].equals("bottle")){
 		    		ThePotionGame.gui.write("bottle");
 			    	Potion result = ThePotionGame.Cauldron.bottlePotion();
 			    	int numofaddstats = 0;
 			    	if (result.stats != null){
 			    		numofaddstats = result.stats.length;
 					}
 			    	if (numofaddstats > 0){
 			    		ThePotionGame.gui.write("Misc Potion,\nPotion Name = " + result.name);
 			    		for (GameObject addedItem: result.stats){
 			    			if (addedItem != null){
 			    				if (addedItem.statType != null){
 					    			ThePotionGame.gui.write("Effect: " + addedItem.statValue + " " + addedItem.statType);
 								}
 							}
 			    		}
 			    	}
 			    	else{
 			    		ThePotionGame.gui.write("Potion Successful,\nPotion Name = " + result.name + "\nEffect = " + result.statValue + " " + result.statType);
 			    	}
 			    	
 					CauldronRecipe[] recipes = new CauldronRecipe[999];
 					recipes = ThePotionGame.Cauldron.recipeList;
 					ThePotionGame.Cauldron = new Cauldron();
 					
 					for (int rep = 0; rep < recipes.length; rep++){
 						ThePotionGame.Cauldron.addRecipe(recipes[rep]);
 					}
 			    	
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
