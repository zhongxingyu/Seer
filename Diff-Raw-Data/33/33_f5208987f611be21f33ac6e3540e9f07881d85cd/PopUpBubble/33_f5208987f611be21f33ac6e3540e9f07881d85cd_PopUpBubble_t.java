 package civ;
 
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Font;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
 
 public class PopUpBubble extends JPanel{
 	
 	private Image i; //Flytta till globalt scope
 
 //Anropas vid avslutad strid, ourloss ar den egna soldatens forluster, theirloss ar motstandarens forluster
 //skall ritas ut med vanster horn pa X / Y
 	PopUpBubble(int x, int y, int ourloss, int theirloss){
 		if (ourloss>theirloss){
 			try{
 				i = ImageIO.read(new File("data/img/bubbla3.png"));
 				}
 				catch(IOException e){
 					return;
 				}
 	
 		}
 		else if(ourloss<theirloss){
 			try{
 				i = ImageIO.read(new File("data/img/bubbla4.png"));
 				}
 				catch(IOException e){
 					return;
 				}
 
 		}
 		else{
 			try{
 				i = ImageIO.read(new File("data/img/bubbla5.png"));
 			}
 			catch(IOException e){
 				return;
 			}
 		}
 		setLayout(null);
 		setBounds(x, y-(i.getHeight(null)), i.getWidth(null)+1, i.getHeight(null)+1);//bor beraknas efter bilden		
 		setOpaque(false);		
 			
 		JLabel losslabel = new JLabel(""+ourloss);
 		Font curFont = losslabel.getFont();
 		losslabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 24));
 		losslabel.setBounds(170, 62, 300, 50);
 		add(losslabel);	
 
 		JLabel winlabel = new JLabel(""+theirloss);
 		curFont = winlabel.getFont();
 		winlabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 24));			
 		winlabel.setBounds(170, 88, 300, 50);
 		add(winlabel);	
 		
 
 	}
 
 //Anropas nar kommando ges till enhet/stad, skall svara med att rita ut simpel feedback med vanster horn(typ) pa X / Y
 	//bilden ar 300 x 226 y stor
 	PopUpBubble(int x, int y){
 		try{
 			i = ImageIO.read(new File("data/img/bubbla2.png"));
 			}
 			catch(IOException e){
 				return;
 			}
 		
 		setLayout(null);
 		//setBounds(x, y-226, 301, 227);//bor beraknas efter bilden
 		setBounds(x, y-(i.getHeight(null)), i.getWidth(null), i.getHeight(null));
 		setOpaque(false);		
 		
 		//JLabel feedbacklabel = new JLabel("w/e");
 		
 		//feedbacklabel.setBounds(100, 100, 300, 50);
 		//add(feedbacklabel);
 		
 	}
 	
 	protected void paintComponent(Graphics g){
 		super.paintComponent(g);
 		g.drawImage(/*pratbubbla har*/ i, 0, 0, this);
 	}
 }
