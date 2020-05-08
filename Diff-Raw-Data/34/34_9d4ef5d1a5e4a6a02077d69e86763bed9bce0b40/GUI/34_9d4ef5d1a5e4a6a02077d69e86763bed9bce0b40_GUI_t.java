 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.filechooser.FileFilter;
 
 import bl.VideoPreview;
 
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.ToolFactory;
 import com.xuggle.xuggler.demos.VideoImage;
 
 
 /**
  * Main class for all GUI elements
  * @author Johann Houszka 0625523
  *
  */
 public class GUI extends JFrame implements ActionListener {
 	
 	/**
 	 * random serial number
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * Instantiate user interface variables
 	 */
 	JFrame frame = new JFrame("Titeleditor");
 	JPanel panel_video_big, panel_effects, panel_video, panel_settings, setting_timeline, start_panel;
 	JMenuBar menu;
 	JMenu file, options, help;
 	JMenuItem open, save, exit, about;
 	JButton start, stopp, pause, back, forw, volume;
 	JButton cent, right, left, ital, bol, under;
 	
 	//image icons for video preview
 	ImageIcon play = new ImageIcon("img/play_24.png");
 	ImageIcon stop = new ImageIcon("img/stop_24.png");
 	ImageIcon paused = new ImageIcon("img/pause_24.png");
 	ImageIcon backward = new ImageIcon("img/backward_24.png");
 	ImageIcon forward = new ImageIcon("img/forward_24.png");
 	ImageIcon vol = new ImageIcon("img/volume_24.png");
 	
 	//image icons for text effects
 	ImageIcon center = new ImageIcon("img/center_24.png");
 	ImageIcon rightbound = new ImageIcon("img/rightbound_24.png");
 	ImageIcon leftbound = new ImageIcon("img/leftbound_24.png");
 	ImageIcon italic = new ImageIcon("img/italic1_24.png");
 	ImageIcon bold = new ImageIcon("img/bold1_24.png");
 	ImageIcon underline = new ImageIcon("img/underline1_24.png");
 	
 	
 	GridBagConstraints gbc = new GridBagConstraints();
 	
 
 	/**
 	 * Build user interface
 	 */
 	public void initInterface() {
 		frame.setLayout(new BorderLayout());
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		menu = new JMenuBar();
 		file = new JMenu("File");
 		options = new JMenu("Options");
 		help = new JMenu("Help");
 		
 		open = new JMenuItem("Open");
         open.addActionListener(this);
         save = new JMenuItem("Save");
         save.addActionListener(this);
         exit = new JMenuItem("Exit");
         exit.addActionListener(this);
         about = new JMenuItem("About");
         about.addActionListener(this);
         
         menu.add(file);
         menu.add(options);
         menu.add(help);
         
         file.add(open);
         file.add(save);
         file.add(exit);
         help.add(about);
         
         frame.add(menu, BorderLayout.NORTH);
         
 		//////
         //panels
         //////
 		panel_effects = new JPanel(new GridBagLayout());
 		panel_effects.setPreferredSize(new Dimension(600, 100));
 		panel_effects.setBorder(BorderFactory.createTitledBorder("Video Effects"));
 		
 		panel_video = new JPanel(new GridBagLayout());
 		panel_video.setPreferredSize(new Dimension(600, 400));
 		panel_video.setBorder(BorderFactory.createTitledBorder("Video Preview"));
 		
 		panel_settings = new JPanel(new GridBagLayout());
 		panel_settings.setPreferredSize(new Dimension(200, 500));
 		panel_settings.setBorder(BorderFactory.createTitledBorder("Text Styles"));
 		panel_settings.setSize(400, 400);
 		
 		setting_timeline = new JPanel(new GridBagLayout());
 		setting_timeline.setPreferredSize(new Dimension(600, 100));
 		setting_timeline.setBorder(BorderFactory.createTitledBorder("Timeline"));
 		
 		start_panel = new JPanel(new GridBagLayout());
 		start_panel.setPreferredSize(new Dimension(600, 50));
 		start_panel.setBorder(BorderFactory.createTitledBorder("Start/Stop"));
 		
 		panel_video_big = new JPanel();
 		//panel_video_big.setPreferredSize(new Dimension(600, 400));
 		panel_video_big.setLayout(new BoxLayout(panel_video_big, BoxLayout.PAGE_AXIS));
 		panel_video_big.add(panel_effects);
 		panel_video_big.add(panel_video);
 		panel_video_big.add(start_panel);
 		
 		frame.add(panel_video_big,BorderLayout.CENTER);
 		frame.add(panel_settings,BorderLayout.EAST);
 		frame.add(setting_timeline, BorderLayout.PAGE_END);
 		
 		frame.setSize(800, 600);
 		
 		
 		//////
 		//buttons video preview
 		//////
 		back = new JButton(backward);
 		back.setOpaque(false);
 		back.setFocusPainted(false);
 		back.setBorderPainted(false);
 		back.setContentAreaFilled(false);
 		back.setBorder(BorderFactory.createEmptyBorder(2,2,10,2)); // Especially important
 		gbc.gridx = 1;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		start_panel.add(back, gbc);
 		
 		
 		start = new JButton(play);
 		start.setPreferredSize(new Dimension(30,30));
 		start.setOpaque(false);
 		start.setFocusPainted(false);
 		start.setBorderPainted(false);
 		start.setContentAreaFilled(false);
 		start.setBorder(BorderFactory.createEmptyBorder(2,2,10,2)); // Especially important
 		gbc.gridx = 2;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		start_panel.add(start, gbc);
 		
 		stopp = new JButton(stop);
 		stopp.setPreferredSize(new Dimension(30,30));
 		stopp.setOpaque(false);
 		stopp.setFocusPainted(false);
 		stopp.setBorderPainted(false);
 		stopp.setContentAreaFilled(false);
 		stopp.setBorder(BorderFactory.createEmptyBorder(2,2,10,2)); // Especially important
 		gbc.gridx = 3;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		start_panel.add(stopp, gbc);
 		
 		pause = new JButton(paused);
 		pause.setPreferredSize(new Dimension(30,30));
 		pause.setOpaque(false);
 		pause.setFocusPainted(false);
 		pause.setBorderPainted(false);
 		pause.setContentAreaFilled(false);
 		pause.setBorder(BorderFactory.createEmptyBorder(2,2,10,2)); // Especially important
 		gbc.gridx = 4;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		start_panel.add(pause, gbc);
 		
 		forw = new JButton(forward);
 		forw.setPreferredSize(new Dimension(30,30));
 		forw.setOpaque(false);
 		forw.setFocusPainted(false);
 		forw.setBorderPainted(false);
 		forw.setContentAreaFilled(false);
 		forw.setBorder(BorderFactory.createEmptyBorder(2,2,10,2)); // Especially important
 		gbc.gridx = 5;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		start_panel.add(forw, gbc);
 		
 		volume = new JButton(vol);
 		volume.setPreferredSize(new Dimension(30,30));
 		volume.setOpaque(false);
 		volume.setFocusPainted(false);
 		volume.setBorderPainted(false);
 		volume.setContentAreaFilled(false);
 		volume.setBorder(BorderFactory.createEmptyBorder(2,30,10,2)); // Especially important
 		gbc.gridx = 6;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		start_panel.add(volume, gbc);
 		
 		///////
 		//buttons text effects
 		//////
 		left = new JButton(leftbound);
 		//left.setPreferredSize(new Dimension(30,30));
 		left.setOpaque(false);
 		left.setFocusPainted(false);
 		left.setBorderPainted(false);
 		left.setContentAreaFilled(false);
 		left.setBorder(BorderFactory.createEmptyBorder(1,1,1,1)); // Especially important
 		gbc.gridx = 1;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		panel_settings.add(left, gbc);
 		
 		cent = new JButton(center);
 		//left.setPreferredSize(new Dimension(30,30));
 		cent.setOpaque(false);
 		cent.setFocusPainted(false);
 		cent.setBorderPainted(false);
 		cent.setContentAreaFilled(false);
 		cent.setBorder(BorderFactory.createEmptyBorder(1,1,1,1)); // Especially important
 		gbc.gridx = 2;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		panel_settings.add(cent, gbc);
 		
 		right = new JButton(rightbound);
 		//left.setPreferredSize(new Dimension(30,30));
 		right.setOpaque(false);
 		right.setFocusPainted(false);
 		right.setBorderPainted(false);
 		right.setContentAreaFilled(false);
 		right.setBorder(BorderFactory.createEmptyBorder(1,1,1,1)); // Especially important
 		gbc.gridx = 3;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		panel_settings.add(right, gbc);
 		
 		ital = new JButton(italic);
 		//left.setPreferredSize(new Dimension(30,30));
 		ital.setOpaque(false);
 		ital.setFocusPainted(false);
 		ital.setBorderPainted(false);
 		ital.setContentAreaFilled(false);
 		ital.setBorder(BorderFactory.createEmptyBorder(1,5,1,1)); // Especially important
 		gbc.gridx = 4;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		panel_settings.add(ital, gbc);
 		
 		bol = new JButton(bold);
 		//left.setPreferredSize(new Dimension(30,30));
 		bol.setOpaque(false);
 		bol.setFocusPainted(false);
 		bol.setBorderPainted(false);
 		bol.setContentAreaFilled(false);
 		bol.setBorder(BorderFactory.createEmptyBorder(1,1,1,1)); // Especially important
 		gbc.gridx = 5;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		panel_settings.add(bol, gbc);
 		
 		under = new JButton(underline);
 		//left.setPreferredSize(new Dimension(30,30));
 		under.setOpaque(false);
 		under.setFocusPainted(false);
 		under.setBorderPainted(false);
 		under.setContentAreaFilled(false);
 		under.setBorder(BorderFactory.createEmptyBorder(1,1,1,1)); // Especially important
 		gbc.gridx = 6;
 		gbc.gridy = 0;
 		gbc.gridheight = 1;
 		gbc.gridwidth = 1;
 		panel_settings.add(under, gbc);
 		
 		//frame.pack();
 		frame.setVisible(true);
 		frame.setResizable(false);
 		
 	}
 
 
 	
 	@SuppressWarnings("deprecation")
 	public void actionPerformed(ActionEvent object) {
 		//close application
 		if (object.getSource() == exit){
 			System.exit(0);
        }
 		if (object.getSource() == open){
 			JFileChooser chooser = new JFileChooser();
 			chooser.addChoosableFileFilter(new FileFilter() {
 			    public boolean accept(File f) {
 			      if (f.isDirectory()) return true;
 			      return f.getName().toLowerCase().endsWith(".mp4");
 			    }
 			    public String getDescription () { return "Video Files"; }  
 			  });
 			  chooser.setMultiSelectionEnabled(false);
 			  if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
 				System.out.println ("Datei "+chooser.getSelectedFile()+" ausgewhlt.");
 				String filename = chooser.getSelectedFile().toString();
 		     	VideoPreview prev = new VideoPreview();
		     	//prev.showVideo(filename);

		     	    
		     	    // create a new reader

		     	    IMediaReader reader = ToolFactory.makeReader(filename);
		     	    reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		     	    //
		     	    // Create a MediaViewer object and tell it to play video only
		     	    //
		     	    reader.addListener(ToolFactory.makeViewer(true));//(IMediaViewer.Mode.AUDIO_VIDEO));

		     	    // read out the contents of the media file, and sit back and watch

		     	    while (reader.readPacket() == null)
		     	    	do {} while(false);
 				
 				 
 				
 			  } //end if.approved
 
        } //end actionperformend-open
 	
 		
 	}//end action performed
 	/**
 	 * The window we'll draw the video on.
 	 * 
 	 */
 	private static VideoImage mScreen = null;
 
 	public static void updateJavaWindow(BufferedImage javaImage)
 	{
 	  mScreen.setImage(javaImage);
 	}
 
 	/**
 	 * Opens a Swing window on screen.
 	 */
 	public static void openJavaWindow()
 	{
 	  mScreen = new VideoImage();
 	}
 
 	/**
 	 * Forces the swing thread to terminate; I'm sure there is a right
 	 * way to do this in swing, but this works too.
 	 */
 	public static void closeJavaWindow()
 	{
 	  System.exit(0);
 	}
 	
 }//end class
 
 
