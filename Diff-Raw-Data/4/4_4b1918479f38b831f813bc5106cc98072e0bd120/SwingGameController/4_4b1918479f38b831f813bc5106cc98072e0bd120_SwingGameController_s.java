 /**
 *GUMA a simple math game for elementary school students
 *	Copyright (C) 2012-1013  Dimitrios Desyllas (pc_magas)
 *
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Contact with me by at this address: pc_magas@yahoo.gr
 */
 
 package guma.gui;
 
 import javax.swing.JOptionPane;
 import guma.gui.SettingFrame;
 import guma.core.Game;
 import guma.ui.main.GameController;
 import guma.gui.SimulatorGui;
 import guma.arithmetic.Praxis;
 
 /**
 *A class that allows you to Control the Game via Swing
 */
 public class SwingGameController extends GameController
 {
 
 	/**
 	*Make the Controller
 	*/
 	public SwingGameController()
 	{
 		super();
 	}
 
 	/**
 	*Make the Cotroller it can Initilize a Game
 	*@param makeGame: Tells if it will be created a game or not
 	*/
 	public SwingGameController(boolean makeGame)
 	{
 		super(makeGame);
 	}
 
 	/**
 	*In some arithmetic operations especially into Division
 	*So it provides you a way to get the extra result. 
 	*/
 	protected int getExtraResult()
 	{
 			String praxisResults=(String)JOptionPane.showInputDialog(null,"Η διαίρεση αυτή έχει Υπόλοιπο"+
 																								"\nΠαρακαλώ εισάγεται το υπόλοιπο",
 																								"Απαιτείται υπόλοιπό",
 																								JOptionPane.PLAIN_MESSAGE);
 		return Integer.parseInt(praxisResults);
 	}
 
 	/**
 	*Method that shows to the user an error message
 	*/
 	public void displayError(String title, String message)
 	{
 		JOptionPane.showMessageDialog(null,message,title,JOptionPane.ERROR_MESSAGE);
 	}
 
 	/**
 	*Displays a Message
 	*/
 	public void displayMessage(String title, String message)
 	{
 		JOptionPane.showMessageDialog(null,message,title,JOptionPane.PLAIN_MESSAGE);
 	}
 	
 	/**
 	*Displays a Message
 	*/
 	public void triesEndMessage(String title, String message)
 	{
 		String[] options={"Προσομοίωση πράξης","OK"};
 		int returnVal= JOptionPane.showOptionDialog(null,message,title,JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,
 									options,options[1]);
 		if(returnVal==JOptionPane.OK_OPTION)
 		{
 			simulate();
 		}
 		
 	}
 	
 	/**
 	*@override
 	*/
 	public void simulate()
 	{
 		try
 		{
 			Praxis p=paixnidi.getPraxis(true);
 			SimulatorGui g= new SimulatorGui(p.getTelestis1(),p.getTelestis2(),p.getPraxisType());
 			g.showSimulator(null);
 		}
 		catch(NullPointerException n)
 		{
 			n.printStackTrace();
 		}
 	}
 
 	/**
 	*Initialize a new game
 	*/
 	public	Game makeNewGame()
 	{
 		return new SettingFrame().getGame(null);
 	}
 	
 }
