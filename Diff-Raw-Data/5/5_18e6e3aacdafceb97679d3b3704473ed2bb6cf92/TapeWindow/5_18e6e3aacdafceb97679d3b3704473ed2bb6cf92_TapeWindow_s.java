 package gui;
 
 import java.util.ArrayList;
 
 import javax.swing.*;
 import java.awt.*;
 
import tape.Tape.Type;

 public class TapeWindow extends JFrame {
 
 	JScrollPane scrollpaneRight;
 	ArrayList<tape.GraphicTape> graphicTapes = new ArrayList<tape.GraphicTape>();
 
 	public TapeWindow(ArrayList<tape.Tape> tapes){
 		for(int i = 0; i< tapes.size(); i++){
			if(tapes.get(i).getType() == Type.GRAPHIC){
 				graphicTapes.add((tape.GraphicTape)tapes.get(i)); 
 			}
 		}
 		setTitle("Tapes");
 		setVisible(true);
 		setBounds(200,200,600,700);
 		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		this.setLayout(new FlowLayout());
 	}
 
 	public void init(){
 			for(int i = 0; i< graphicTapes.size(); i++){
 				this.add(this.graphicTapes.get(i).getTapePanel());
 			}
 		
 	}
 
 }
