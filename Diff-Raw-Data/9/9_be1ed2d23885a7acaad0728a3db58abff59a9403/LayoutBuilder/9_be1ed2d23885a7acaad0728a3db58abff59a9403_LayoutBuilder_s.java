 package org.menu_builder;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.TextField;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 
 public class LayoutBuilder {
 	
 	public JFrame frame;
 	private Container container;
 	private Dimension screenSize;
 	public int _mheight;		
 	public int _width;
 	public int xlocation;
 	public int ylocation;
 	public int scrnheight;
 	public int scrnwidth;
 	
 	
 	
 	public LayoutBuilder( int height, int width, String title){
 		frame = new JFrame();
 		container = frame.getContentPane();	
 		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		frame.setTitle( "Snakes and Ladder" );
 		
 		_mheight = height;
 		_width = width;
 		
 		scrnheight = screenSize.height;		
   		scrnwidth = screenSize.width;
   		
   		int hsize = height;
   		int wsize = _width;
   		
   		xlocation = (scrnwidth/2)-(wsize/2);
  	    ylocation = ( scrnheight/2 ) - ( (hsize/2)+20);
 		container.setBackground(new Color(0xFFFFFF));	
 		container.setLayout(null);	
 		
 		frame.setSize(wsize, hsize);
  	    frame.setLocation( xlocation, ylocation);
 		frame.setVisible(true);
 		frame.setResizable(false);
 		frame.setTitle( title );
 		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );	
 		
 		
 		
 	}
 	
 	public JLabel buildpic(String source_file, int height, int width, int picxlocation, int picylocation){
 		
 		BufferedImage myPicture = null;
 		
 		try {
			myPicture = ImageIO.read(new File("source/bg.jpg"));
 		} catch (IOException e) {
 			System.out.println(e);
 		}
 		JLabel picLabel = new JLabel(new ImageIcon( myPicture ));
 		picLabel.setSize( width, height);
 		picLabel.setLocation(picxlocation,picylocation);
 		return picLabel;
 	}
 	
 	public JLabel buildJLabel(String title, Font font, int Jlbl_height, int Jlbl_width, Color color, int xlocation, int ylocation){
 		JLabel JLbl;
 		JLbl = new JLabel(title);
 		JLbl.setFont(font);
 		JLbl.setForeground(color);
 		JLbl.setSize( Jlbl_height , Jlbl_width );
 		JLbl.setLocation( Jlbl_height - Jlbl_height + xlocation , ylocation );
 		return JLbl;
 		
 	}
 	
 	public JTable buildJtable(Object[][] data, String[] data1, int height, int width, int xlocation, int ylocation){
 		
 		JTable gridtable = new JTable(data, data1 );
 		gridtable.setSize( height ,width);
 		gridtable.setLocation( xlocation , ylocation);
 		return gridtable;
 	}
 	
 	public JButton buildJButton( String title, int height, int width , int xlocation, int ylocation){
 		
 		JButton jb = new JButton( title );
 		jb.setSize( height ,width);
 		jb.setLocation( xlocation , ylocation);
 		return jb;
 	}
 	
 	public TextField buildJTextField( String title, int height, int width , int xlocation, int ylocation ){
 		TextField textfld = new TextField( title );	
 		textfld.setSize( 20 ,20);
 		textfld.setLocation( 20 , 20);
 	    return textfld;
 	    
 	}
 	
 	public void addbuilder(Component comp){
 		
 		 container.add(comp);
 		 container.repaint();
 	}
 
 
 	public void close_panel() {
 		frame.dispose();
 	}
 	
 	public JFrame getFrame() {
 		return (JFrame)this.frame;
 
 	}
 }
