 package com.jpii.navalbattle.gui;
 
 import java.awt.event.*;
 import javax.swing.*;
 
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.gui.listeners.KeyboardListener;
 
 @SuppressWarnings("serial")
 public class Window extends JFrame {
 	
 	/**
 	 * Constructor for Window. Superclass for all GUI windows that
 	 * handles size, icon, etc. To redefine elements, use custom constructor.
 	 * 
 	 * Will log opening automatically, but closing (disposing) should be
 	 * handled within each subclass.
 	 */
 	protected int width;
 	protected int height;
 	
 	public Window() {
 		width = 491;
 		height = 339;
 		startup();
 		setDefaults();
 	}
 	public Window(int x, int y) {
 		width = x;
 		height = y;
 		startup();
 	}
 	
 	private void startup(){
 		try {
 			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
			
		}
 	}
 	
 	protected void setDefaults(){
 		this.setIconImage(Helper.GUI_WINDOW_ICON);
 		setTitle("NavalBattle");
 		setSize(width, height);
 		setLocation(1280/2-getWidth()/2,800/2-getHeight()/2);
 		setResizable(false);
 		setFocusable(true);
 		addKeyListener(new KeyboardListener(this));
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent we) {
 				NavalBattle.close();
 			}
 		});
 		setVisible(true);
 	}
 	
 	/**
 	 * Get method for Window
 	 * 
 	 * @return Window
 	 */
 	public JFrame getFrame() {
 		return this;
 	}
 }
