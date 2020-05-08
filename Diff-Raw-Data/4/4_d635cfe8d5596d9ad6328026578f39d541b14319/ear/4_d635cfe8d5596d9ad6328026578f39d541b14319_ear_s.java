 //import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.Vector;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.JFrame;
 
 
 public class ear {
 	
 	//*******************declarations*****************//
 	private static factorysample affichage;
 	private static JFrame win = new JFrame("GuidoEngine Java Factory");	 
 	
 	
 
    	//**********************************************//
 	
 	private static final String[] notes= { "c", "c#", "d", "d#","e","f","f#","g","g#","a", "a#", "b","b#" }; // find the octave number
    	static int midiToOctave(int midi) { return midi/12-1; } // find the note name 
 	static String  midiToNote(int midi) { return notes[midi%12]; } // find the midi number 
 	
 	int noteToMidi(String note, int octave) {    
 		int i;    
 		for (i= 0; i < notes.length; i++)       
 			if (note.equals(notes[i]))          
 				return 12*(octave+1)+i;    
 		return -1; }
 	
 	public static void main(String[] args) {
 
 		//*******************declarations*****************// 
 		Vector tab 		= new Vector() ;
 		Vector reponse 	= new Vector();
 		MainGauche gauche = new MainGauche();	
 		int[] temp2;
 				
 		//*************saisie*******************//
 		System.out.println("Degre ? ");
 		Scanner temp = new Scanner(System.in).useDelimiter("\\s");
 		do{
 		tab.addElement(temp.nextInt());
 		}while(temp.hasNextInt());
 		String mode = temp.next();
 		temp.close();
 		//*************************************//
 		Vector []mesure = new Vector[tab.size()];
 				
 		for( int i = 0; i< tab.size();i++){
 		 mesure[i]= new Vector();
 		 mesure[i] = gauche.genereAccord(mode, Integer.parseInt(tab.elementAt(i).toString()));
 		 for(int j = 0 ; j < mesure[i].size() ; j++){
			 System.out.println("Reponse :"+(int)mesure[i].get(j) );
		 mesure[i].setElementAt(midiToNote((int)mesure[i].get(j)), j);
 		 }
 			 
 		}//for i
 		
 		affichage = new factorysample(mesure, tab.size());
 		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		win.setBounds(10, 30, 500, 400);
 	    win.add( affichage );
 	    win.setVisible(true);
 	}//main
 
 	
 }//classe
