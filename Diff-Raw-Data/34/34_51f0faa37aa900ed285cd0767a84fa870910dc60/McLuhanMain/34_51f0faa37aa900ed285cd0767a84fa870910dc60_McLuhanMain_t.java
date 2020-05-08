 package com.navmenu;
 
 import java.awt.AWTException;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Robot;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Date;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.awt.GLCanvas;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.Timer;
 import javax.swing.border.Border;
 import javax.swing.border.LineBorder;
 
 import org.pirelenito.movieGL.Main;
 
 import com.helper.MP3;
 import com.helper.TextureMap;
 import com.helper.TopSlider;
 import com.jogamp.opengl.util.FPSAnimator;
 import com.physics.PhysicsEngine;
 import com.physics.PhysicsGrabber;
 
 
 public class McLuhanMain extends JFrame{
 	
 	private String[] DIRS = {"City as Classroom","Extensions of Man","Global Village","The Medium is the Message"};
 
 	private JFrame frame;
 	private GLCanvas canvas,vidc,grabc;
 	//TODO convert glnav to class to handle menu grabbing/interaction in gl scene. Might need to be added as an extension to the main gl scene
 	private JPanel menu,glnav;
 	
 	private BackgroundPanel p1,load;
 	
 	private JButton pmen, movietest,aud;
 	
 	private JButton[] btns;
 	
 	private Dimension size = new Dimension(1024,768);
 	
 	private FPSAnimator animator;
 	private PhysicsEngine app;
 	private PhysicsGrabber grabber;
 	
 	private Main video;
 	
 	private Timer quepush;
 	
 	private boolean roll, audio;
 	
 	private ActionListener gogo, repeat, one, two, three, four;
 	
 	private TextureMap themes;
 	
 	private MP3 soundbite;
 	
 	private TopSlider tslide;
 	
 	private static Robot mouseRobot;
 	
 	public int mode;
 	
 	public McLuhanMain() {
 		frame = this;
 		frame.setName("McLuhan Server");
 		frame.setSize(size);
 		//this.setUndecorated(true);
 
 		menu = new JPanel();
 
 		CardLayout system = new CardLayout();
 		menu.setLayout(system);
 
 		//initGrabber();
 		initTSlide();
 		initTData();
 		initSelections();
 		initLoading();
 		initGLNav();
 
 		//menu.add("Grabber",grabc);
 		menu.add("Main One",p1);
 		menu.add("Loader",load);
 
 		frame.getContentPane().add(BorderLayout.NORTH,tslide);
 		frame.getContentPane().add(BorderLayout.CENTER,menu);
 		frame.getContentPane().add(BorderLayout.WEST,glnav);
 		
 		glnav.setVisible(false);
 		tslide.setVisible(false);
 
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
         
         roll = true;
         initFlshSeq();
 
         gogo = new ActionListener(){
         	public void actionPerformed(ActionEvent arg0) {
         		Timer flash = new Timer(3000,one);
         		flash.setRepeats(false);
         		flash.start();
         	}
         };
 
         Timer test = new Timer(1000,gogo);
 		test.setRepeats(false);
 		test.start();
 
 		
 		// Create a new mouse robot
 		try {
 			mouseRobot = new Robot();
 		} catch (AWTException awte) {
 			awte.printStackTrace();
 		}
 		
 		// Init mode
 		mode = 1;
 	}
 
 
 	private void initGrabber() {
 		GLProfile.initSingleton(true);
 		GLProfile glp = GLProfile.getDefault();
         GLCapabilities caps = new GLCapabilities(glp);
         grabc = new GLCanvas(caps);
         
         grabber = new PhysicsGrabber();
         grabc.addGLEventListener(grabber);
         grabc.addKeyListener(grabber);
         grabc.addMouseListener(grabber);
         grabc.addMouseMotionListener(grabber);
         
         grabc.requestFocus();
         
         activateGrabber();
 		
 	}
 
 
 	private void initTSlide() {
 		tslide = new TopSlider();
 	}
 
 
 	private void initTData() {
 		themes = new TextureMap();
 	}
 
 
 	private void initFlshSeq() {
 		one = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				btns[0].getModel().setRollover(true);
 				Timer flash = new Timer(3000,two);
 				flash.setRepeats(false);
 				flash.start();
 			}
 		};
 		
 		two = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				btns[0].getModel().setRollover(false);
 				if(roll){
 					btns[1].getModel().setRollover(true);
 					Timer flash = new Timer(3000,three);
 					flash.setRepeats(false);
 					flash.start();
 				}
 			}
 		};
 		
 		three = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				btns[1].getModel().setRollover(false);
 				if(roll){
 					btns[2].getModel().setRollover(true);
 					Timer flash = new Timer(3000,four);
 					flash.setRepeats(false);
 					flash.start();
 				}
 			}
 		};
 		
 		four = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				btns[2].getModel().setRollover(false);
 				if(roll){
 					btns[3].getModel().setRollover(true);
 					Timer flash = new Timer(3000,repeat);
 					flash.setRepeats(false);
 					flash.start();
 				}
 			}
 		};
 		
 		repeat = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				btns[3].getModel().setRollover(false);
 				if(roll){
 					Timer flash = new Timer(15000,one);
 					flash.setRepeats(false);
 					flash.start();
 				}
 			}
 		};
 
 		
 	}
 
 
 	private void initSelections() {
 		btns = new JButton[4];
 		JButton temp;
 		int os;
 		p1 = new BackgroundPanel("Menus/GlobalVillage.jpg",0,0,new Dimension(1004,738));
 		p1.setLayout(null);
 		p1.setSize(size);
 		
 		for(int i=0;i<4;i++){
 			os = i+1;
 			temp = new JButton(new ImageIcon("Menus/Btn"+os+".jpg"));
 			temp.setRolloverIcon(new ImageIcon("Menus/Btn"+os+"RO.jpg"));
 			//temp.setName(DIRS[i]);
 			temp.setName(i+"");
 			temp.addActionListener(new ActionListener(){
 				public void actionPerformed(final ActionEvent ae) {
 					int opt = Integer.parseInt(((JButton)ae.getSource()).getName());
 					roll = false;
 					//((CardLayout)menu.getLayout()).show(menu, "Loader");
 					menu.validate();
 					//initCanvas("McLuhan/"+((JButton)ae.getSource()).getName(),"McLuhan/Texts/"+((JButton)ae.getSource()).getName());
 					paintSlide(opt);
 					initCanvas(opt);
 					menu.add("ogl",canvas);
 					soundbite = new MP3("McLuhan/"+DIRS[Integer.parseInt(((JButton)ae.getSource()).getName())]+"/mp3.mp3");
 					pmen.setEnabled(true);
 					pmen.setVisible(true);
 					aud.setEnabled(true);
 					aud.setVisible(true);
 					audio = true;
 					aud.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							if(audio){
 								soundbite.close();
 								audio = false;
 								aud.setIcon(new ImageIcon("mute.jpg"));
 							}
 							else{
 								audio=true;
 								soundbite.play();
 								aud.setIcon(new ImageIcon("play.jpg"));
 							}
 							aud.validate();	
 						}
 						
 					});
 					
 					menu.validate();
 					activateAnimation();
 					((CardLayout)menu.getLayout()).show(menu, "Loader");
 					ActionListener time = new ActionListener(){
 						public void actionPerformed(ActionEvent arg0) {
 							glnav.setVisible(true);
 							tslide.setVisible(true);
 							((CardLayout)menu.getLayout()).show(menu, "ogl");
 							    soundbite.play();
 						}
 					};
 					Timer t = new Timer(3000,time);
 					t.setRepeats(false);
 					t.start();
 					   
 					// Change mode
 					mode = 2;
 				}});
 			temp.setBorder(null);
 			btns[i] = temp;
 		}
 		
 		btns[0].setSize(new Dimension(242,154));
 		btns[1].setSize(new Dimension(165,160));
 		btns[2].setSize(new Dimension(143,141));
 		btns[3].setSize(new Dimension(288,122));
 		
 		btns[0].setLocation(284, 165);
 		btns[1].setLocation(820, 288);
 		btns[2].setLocation(82, 11);
 		btns[3].setLocation(401, 585);
 		
 		for(int i=0;i<4;i++){
 			p1.add(btns[i]);
 		}
 		
 		movietest = new JButton("Mcluhan Video-Test");
 		movietest.setPreferredSize(new Dimension(241,206));
 		
 		movietest.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae) {
 				pmen.setEnabled(true);
 				pmen.setVisible(true);
 				validate();
 				initTestVid("McLuhan/Youtube.mpg");
 				menu.add("video",vidc);
 				menu.validate();
 				((CardLayout)menu.getLayout()).show(menu, "video");
 			}});
 		
 	}
 	
 	private void paintSlide(int opt) {
 		tslide.letsFight(themes.getFiles(opt*3), themes.getFiles((opt*3)+1));
 		
 	}
 
 
 	private void activateVideo() {
 		animator = new FPSAnimator(vidc, 30);
         animator.start();
 	}
 	
 	public void activateOGL(){
 		activatecanvas();
 	}
 
 	private void activatecanvas(){
 		pmen.setEnabled(true);
 		pmen.setVisible(true);
 		menu.validate();
 		activateAnimation();
 		((CardLayout)menu.getLayout()).show(menu, "ogl");
 	}
 	
 	private void activateGrabber(){
 		animator = new FPSAnimator(60);
         animator.add(grabc);
         animator.start();
 	}
 	
 
 	private void initTestVid(String string) {
 		canvas = null;
 		GLProfile.initSingleton(true);
 		GLProfile glp = GLProfile.getDefault();
         GLCapabilities caps = new GLCapabilities(glp);
         video = null;
 		try {
 			video = new Main(string);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		vidc = video.getCanvas();
 		
         vidc.addGLEventListener(video);
         
         vidc.requestFocus();
         activateVideo();
         video.runstuff();
 		
 	}
 
 
 	private void initGLNav(){
 		JPanel dem = new JPanel();
 		dem.setLayout(new GridLayout(0,1));
 		dem.setBackground(Color.black);
 
 		aud = new JButton(new ImageIcon("play.jpg"));
 		aud.setPreferredSize(new Dimension(45,45));
 		aud.setOpaque(false);
 		aud.setContentAreaFilled(false);
 		
 		pmen = new JButton(new ImageIcon("home.jpg"));
 		pmen.setPreferredSize(new Dimension(45,45));
 		pmen.setOpaque(false);
 		pmen.setContentAreaFilled(false);
 		pmen.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				pmen.setEnabled(false);
 				pmen.setVisible(false);
 				aud.setIcon(new ImageIcon("play.jpg"));
 				aud.setEnabled(false);
 				aud.setVisible(false);
 				aud.removeActionListener(aud.getActionListeners()[0]);
 				((CardLayout)menu.getLayout()).show(menu, "Main One");
 				if(canvas != null){
 					menu.remove(canvas);
 					canvas = null;
 					animator.stop();
 					menu.validate();
 					quepush.stop();
 					soundbite.close();
 					tslide.removeAll();
 					glnav.setVisible(false);
 					tslide.setVisible(false);
 				}
 				else{
 					menu.remove(vidc);
 					vidc = null;
 					animator.stop();
 					menu.validate();
 					video.end();
 				}
 				roll = true;
 				Timer test = new Timer(1000,gogo);
 				test.setRepeats(false);
 				test.start();
 				
 				// Change mode
 				mode = 1;
 			}
 		});
 		
 		aud.setEnabled(false);
 		aud.setVisible(false);
 	
 		pmen.setEnabled(false);
 		pmen.setVisible(false);
 		
 		glnav = new JPanel();
 		glnav.setBackground(Color.black);
 		glnav.setLayout(new FlowLayout());
 		((FlowLayout)glnav.getLayout()).setVgap(520);
 		dem.add(aud);
 		dem.add(pmen);
 		//glnav.add(pmen);
 		glnav.add(dem);
 	}
 
 
 	private void activateAnimation() {
 		animator = new FPSAnimator(60);
         animator.add(canvas);
         animator.start();
        // glnav.setVisible(true);
 		//tslide.setVisible(true);
         
 	}
 	
 	private void initLoading(){
 		load = new BackgroundPanel("Loading2.gif",245,95,new Dimension(500,500));
 		load.setLayout(null);
 		load.setSize(size);	
 	}
 	/*
 	private void initCanvas(String path, String tpath) {
 		GLProfile.initSingleton(true);
 		GLProfile glp = GLProfile.getDefault();
         GLCapabilities caps = new GLCapabilities(glp);
         canvas = new GLCanvas(caps);
         
         app = new PhysicsEngine(path,tpath);
         canvas.addGLEventListener(app);
         canvas.addKeyListener(app);
         canvas.addMouseListener(app);
         canvas.addMouseMotionListener(app);
         
         canvas.requestFocus();
         
         ActionListener timeout = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				app.callTimer();
 				canvas.display();
 			}
 		};
 		quepush = new Timer(15000,timeout);
 		quepush.setRepeats(true);
 		quepush.start();
 	}
 	*/
 	
 	private void initCanvas(int opt) {
 		GLProfile.initSingleton(true);
 		GLProfile glp = GLProfile.getDefault();
         GLCapabilities caps = new GLCapabilities(glp);
         canvas = new GLCanvas(caps);
         
         app = new PhysicsEngine(themes.getMap(opt*3), themes.getMap((opt*3)+1), themes.getMap((opt*3)+2));
         canvas.addGLEventListener(app);
         canvas.addKeyListener(app);
         canvas.addMouseListener(app);
         canvas.addMouseMotionListener(app);
         tslide.setEngine(app);
         
         canvas.requestFocus();
         
         ActionListener timeout = new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				app.callTimer();
 				canvas.display();
 			}
 		};
 		quepush = new Timer(45000,timeout);
 		quepush.setRepeats(true);
 		quepush.start();
 	}
 	
	public void recvGrabberData(float x, float y, float depth, int select, String action) {
		System.out.println("Mode: " + mode);
		
		double ratioX = size.getWidth() / 640;
		double ratioY = size.getHeight() / 480;
		int newX = (int) (x * ratioX);
		int newY = (int) (y * ratioY);
		
		return;
	}
	
 	public void recvThemeData(float x, float y, float depth, int select, String action) {
 		System.out.println("Mode: " + mode);
 		
 		double ratioX = size.getWidth() / 640;
 		double ratioY = size.getHeight() / 480;
 		int newX = (int) (x * ratioX);
 		int newY = (int) (y * ratioY);
 		
 		mouseRobot.mouseMove(newX, newY);
 		
 		if (action.compareTo("push") == 0) {
 			mouseRobot.mousePress(MouseEvent.BUTTON1_MASK);
 			mouseRobot.mouseRelease(MouseEvent.BUTTON1_MASK);
 		}
		
		return;
 	}
 	
 	public void recvPhysicsData(float x, float y, float depth, int select, String action) {
 		System.out.println("Mode: " + mode);
 		
 		double ratioX = size.getWidth() / 640;
 		double ratioY = size.getHeight() / 480;
 		int newX = (int) (x * ratioX);
 		int newY = (int) (y * ratioY);
 		
 		mouseRobot.mouseMove(newX, newY);
 		
 		if (action.compareTo("push") == 0) {
			if (newY > 100) {
				mouseRobot.mousePress(MouseEvent.BUTTON1_MASK);
			}
			else {
				mouseRobot.mousePress(MouseEvent.BUTTON1_MASK);
				mouseRobot.mouseRelease(MouseEvent.BUTTON1_MASK);
			}
 		}
 		
 		if (action.compareTo("circle") == 0) {
 			mouseRobot.mouseRelease(MouseEvent.BUTTON1_MASK);
 		}
		
		return;
 	}
 	
 }
