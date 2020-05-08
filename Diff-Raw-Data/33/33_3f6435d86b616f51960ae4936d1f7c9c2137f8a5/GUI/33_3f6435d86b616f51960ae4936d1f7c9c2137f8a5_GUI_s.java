 /***********************************************************************
  * -----------------------
  * BorderLayoutDemo.java
  * -----------------------
  * Team U1
  * 3D Manipulator GUI window
  * 
  * (Work in progress)
  * 
  ***********************************************************************/
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.IOException;
 import java.util.Hashtable;
 
 import javax.imageio.ImageIO;
 import javax.media.opengl.awt.GLCanvas;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextArea;
 import javax.swing.JToolBar;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 
 public class GUI extends JPanel implements MouseListener, MouseMotionListener {
 
 	private static final long serialVersionUID = 1L;
 
 	private static Scene s;
 	private GLCanvas c;
 	private JFrame frame;
 
 	// Menu
 	private JMenuBar menubar;
 	private JMenu file, edit, help;
 	private JMenuItem save, load, exit, blank, about;
 
 	// Panels
 	private JPanel mainPanel, rightToolbar, currentShapes, rotatePane,
			resizePane, aestheticsPane, centerPanel;
 
 	// Shapes Toolbar
 	private JToolBar shapesToolbar;
 	private JButton btn_triPri, btn_sqrPri, btn_hexPri, btn_triPyr, btn_sqrPyr,
 			btn_cylider, btn_sphere, btn_line;
 	private Image img_triPri, img_sqrPri, img_hexPri, img_triPyr, img_sqrPyr,
 			img_cylider, img_sphere, img_line;
 	private JTextArea logText;
 	private JLabel statusBar;
 
 	public GUI() {
 		s = new Scene();
 		c = s.getCanvas();
 		c.addGLEventListener(s);
 		c.addMouseMotionListener(this);
 
 		init();
 	}
 
 	public final void init() {
 
 		frame = new JFrame("3D GUI");
 		frame.setSize(900, 700);
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		menubar = new JMenuBar();
 		frame.setJMenuBar(menubar);
 
 		// File
 		file = new JMenu("File");// Items under File
 		edit = new JMenu("Edit");// Items under Edit
 		help = new JMenu("Help");
 
 		menubar.add(file);
 		menubar.add(edit);
 		menubar.add(Box.createHorizontalGlue()); // adheres Help menu to right
 													// side
 		menubar.add(help);
 
 		save = new JMenuItem("Save");
 		load = new JMenuItem("Load");
 		exit = new JMenuItem("Exit");
 
 		file.add(save);
 		file.add(load);
 		file.add(exit);
 
 		// Edit
 		blank = new JMenuItem("Blank Button");
 		edit.add(blank);
 
 		// Help
 		about = new JMenuItem("About");
 		help.add(about);
 
 		// Adding the function of the action to the button
 		exit.addActionListener(new ExitAction());
 		about.addActionListener(new AboutAction());
 		blank.addActionListener(new BlankAction());
 		save.addActionListener(new SaveAction());
 		load.addActionListener(new LoadAction());
 
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 		frame.add(mainPanel);
 
 		// creates left-hand toolbar
 		shapesToolbar = new JToolBar(JToolBar.VERTICAL);
 		shapesToolbar.setFloatable(false);
 		mainPanel.add(shapesToolbar, BorderLayout.LINE_START);
 
 		shapesToolbar.setLayout(new GridLayout(8, 1, 1, 10));
 		shapesToolbar.setBorder(LineBorder.createGrayLineBorder());
 
 		// creates buttons for each shape
 
 		btn_triPri = new JButton();
 		btn_sqrPri = new JButton();
 		btn_hexPri = new JButton();
 		btn_triPyr = new JButton();
 		btn_sqrPyr = new JButton();
 		btn_cylider = new JButton();
 		btn_sphere = new JButton();
 		btn_line = new JButton();
 
 		// change background color of buttons
 		btn_triPri.setBackground(Color.WHITE);
 		btn_sqrPri.setBackground(Color.WHITE);
 		btn_hexPri.setBackground(Color.WHITE);
 		btn_triPyr.setBackground(Color.WHITE);
 		btn_sqrPyr.setBackground(Color.WHITE);
 		btn_cylider.setBackground(Color.WHITE);
 		btn_sphere.setBackground(Color.WHITE);
 		btn_line.setBackground(Color.WHITE);
 
 		// adds images to the buttons
 		try {
 			img_triPri = ImageIO.read(getClass().getResource(
 					"resources/triangular_prism.png"));
 			btn_triPri.setIcon(new ImageIcon(img_triPri));
 		} catch (IOException ex) {
 			btn_triPri.setText("Tri Pri");
 		}
 		try {
 			img_sqrPri = ImageIO.read(getClass().getResource(
 					"resources/square_prism.png"));
 			btn_sqrPri.setIcon(new ImageIcon(img_sqrPri));
 		} catch (IOException ex) {
 			btn_sqrPri.setText("Sqr Pri");
 		}
 		try {
 			img_hexPri = ImageIO.read(getClass().getResource(
 					"resources/hexagonal_prism.png"));
 			btn_hexPri.setIcon(new ImageIcon(img_hexPri));
 		} catch (IOException ex) {
 			btn_hexPri.setText("Hex Pri");
 		}
 		try {
 			img_triPyr = ImageIO.read(getClass().getResource(
 					"resources/triangular_pyramid.png"));
 			btn_triPyr.setIcon(new ImageIcon(img_triPyr));
 		} catch (IOException ex) {
 			btn_triPyr.setText("Tri Pyr");
 		}
 		try {
 			img_sqrPyr = ImageIO.read(getClass().getResource(
 					"resources/square_pyramid.png"));
 			btn_sqrPyr.setIcon(new ImageIcon(img_sqrPyr));
 		} catch (IOException ex) {
 			btn_sqrPyr.setText("Sqr Pyr");
 		}
 		try {
 			img_cylider = ImageIO.read(getClass().getResource(
 					"resources/cylinder.png"));
 			btn_cylider.setIcon(new ImageIcon(img_cylider));
 		} catch (IOException ex) {
 			btn_cylider.setText("Cylinder");
 		}
 		try {
 			img_sphere = ImageIO.read(getClass().getResource(
 					"resources/sphere.png"));
 			btn_sphere.setIcon(new ImageIcon(img_sphere));
 		} catch (IOException ex) {
 			btn_sphere.setText("Sphere");
 		}
 		try {
 			img_line = ImageIO.read(getClass()
 					.getResource("resources/line.png"));
 			btn_line.setIcon(new ImageIcon(img_line));
 		} catch (IOException ex) {
 			btn_line.setText("Line");
 		}
 
 		// adds buttons to left-hand toolbar
 		shapesToolbar.add(btn_triPri);
 		shapesToolbar.add(btn_sqrPri);
 		shapesToolbar.add(btn_hexPri);
 		shapesToolbar.add(btn_triPyr);
 		shapesToolbar.add(btn_sqrPyr);
 		shapesToolbar.add(btn_cylider);
 		shapesToolbar.add(btn_sphere);
 		shapesToolbar.add(btn_line);
 
 		// creates right-hand toolbar
 		rightToolbar = new JPanel();
 		rightToolbar.setPreferredSize(new Dimension(150, 0));
 		// rightToolbar.setLayout(new GridLayout(4,1,0,15));
 		rightToolbar.setLayout(new BoxLayout(rightToolbar, BoxLayout.Y_AXIS));
 		rightToolbar.setBorder(LineBorder.createGrayLineBorder());
 
 		mainPanel.add(rightToolbar, BorderLayout.LINE_END);
 
 		// current shapes panel
 		JPanel currentShapes = new JPanel();
 		currentShapes.setPreferredSize(new Dimension(150, 100));
 		rightToolbar.add(currentShapes);
 		rightToolbar.add(Box.createVerticalGlue());
 		currentShapes.setBorder(LineBorder.createGrayLineBorder());
 
 		new CurrentShapesPanel(currentShapes);
 
 		// rotation panel
 		JPanel rotatePane = new JPanel();
 		rotatePane.setMaximumSize(new Dimension(150, 190));
 		rotatePane.setPreferredSize(new Dimension(150, 190));
 		rightToolbar.add(rotatePane);
 		rotatePane.setBorder(new EmptyBorder(0, 0, 0, 0));
 		rotatePane.setBorder(LineBorder.createGrayLineBorder());
 		rightToolbar.add(Box.createVerticalGlue());
 
 		new RotatePanel(rotatePane);
 
 		// resize panel
 		JPanel resizePane = new JPanel();
 		resizePane.setPreferredSize(new Dimension(100, 100));
 		rightToolbar.add(resizePane);
 		resizePane.setBorder(LineBorder.createGrayLineBorder());
 
 		new ResizePanel(resizePane);
 
 		// aesthetics panel
 		JPanel aestheticsPane = new JPanel();
 		aestheticsPane.setMaximumSize(new Dimension(150, 110));
 		aestheticsPane.setPreferredSize(new Dimension(150, 110));
 		rightToolbar.add(aestheticsPane);
 		aestheticsPane.setBorder(LineBorder.createGrayLineBorder());
 
 		new AestheticsPanel(aestheticsPane);
 
 		// creates center panel
 		centerPanel = new JPanel();
 		centerPanel.setLayout(new BorderLayout());
 
 		mainPanel.add(centerPanel, BorderLayout.CENTER);
 
 		centerPanel.add(c, BorderLayout.CENTER);
 
		JPanel bottomCenter = new JPanel();
 		bottomCenter.setLayout(new BorderLayout());
 
 		logText = new JTextArea(); // **LOGGER PANEL**
 		logText.setBorder(LineBorder.createGrayLineBorder());
		logText.setPreferredSize(new Dimension(0, 135));
 
		JSlider zoom = new JSlider(JSlider.HORIZONTAL, 5, 200, 100);
 
 		zoom.setSnapToTicks(true);
 		zoom.setMajorTickSpacing(25);
 		zoom.setMinorTickSpacing(5);
 		zoom.setPaintTicks(true);
 		// zoom.setPaintLabels(true);
 
		bottomCenter.add(logText, BorderLayout.PAGE_END);
 		bottomCenter.add(zoom, BorderLayout.LINE_END);
 		centerPanel.add(bottomCenter, BorderLayout.PAGE_END);
 
		logText = new JTextArea(); // **LOGGER PANEL**
		logText.setBorder(LineBorder.createGrayLineBorder());
		logText.setPreferredSize(new Dimension(0, 150));

		centerPanel.add(logText, BorderLayout.PAGE_END);

 		statusBar = new JLabel();
 		statusBar
 				.setText(" Cursor Position:  |  Selected: x  |  Total Shapes: x");
 		statusBar.setPreferredSize(new Dimension(-1, 22));
 		statusBar.setBorder(LineBorder.createGrayLineBorder());
 		mainPanel.add(statusBar, BorderLayout.PAGE_END);
 
 		frame.setVisible(true);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 	}
 
 	public static void main(String[] args) {
 		GUI ex = new GUI();
 		ex.setVisible(true);
 	}
 
 	class SaveAction implements ActionListener {// Action For Save goes here
 		// NOT COMPLETE
 		public void actionPerformed(ActionEvent e) {
 			JFrame saveFrame = new JFrame("Save");
 			saveFrame.setVisible(true);
 			saveFrame.setSize(200, 200);
 			JLabel label = new JLabel("YOU CLICKED SAVE");
 			JPanel panel = new JPanel();
 			saveFrame.add(panel);
 			panel.add(label);
 		}
 	}
 
 	class LoadAction implements ActionListener {// Action For Save goes here
 		// NOT COMPLETE
 		public void actionPerformed(ActionEvent e) {
 
 			JFileChooser chooser = new JFileChooser();
 			CustomFileFilter filter = new CustomFileFilter();
 			filter.addExtension("log"); // Only choose text files
 			filter.setDescription("Log Files");
 			chooser.setFileFilter(filter);
 
 			int returnVal = chooser.showOpenDialog(null);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				System.out.println("You chose to open this file: "
 						+ chooser.getSelectedFile().getName());
 			}
 		}
 	}
 
 	class ExitAction implements ActionListener {// Action For Exit
 		public void actionPerformed(ActionEvent e) {
 			System.exit(0);
 		}
 	}
 
 	class AboutAction implements ActionListener {// Action For About
 		public void actionPerformed(ActionEvent e) {
 			JFrame aboutFrame = new JFrame("About");
 			aboutFrame.setVisible(true);
 			aboutFrame.setSize(400, 250);
 
 			JTextArea aboutText = new JTextArea(
 					"This application was created by:"
 							+ "\n\nJennifer Hill\nRyan Kane\nDorothy Kirlew\n"
 							+ "Donald Shaner\nand Sean Weber");
 			Font JTextFont = new Font("Verdana", Font.BOLD, 12);
 			aboutText.setFont(JTextFont);
 
 			aboutText.setPreferredSize(new Dimension(380, 250));
 			aboutText.setLineWrap(true);
 			aboutText.setWrapStyleWord(true);
 
 			JPanel panel = new JPanel();
 			aboutFrame.add(panel);
 			panel.add(aboutText);
 		}
 	}
 
 	class BlankAction implements ActionListener {// Blank action
 		public void actionPerformed(ActionEvent e) {
 			JFrame blankFrame = new JFrame("Blank");
 			blankFrame.setVisible(true);
 			blankFrame.setSize(200, 200);
 			JLabel label = new JLabel("Blank");
 			JPanel panel = new JPanel();
 			blankFrame.add(panel);
 			panel.add(label);
 		}
 	}
 
 	public JLabel getStatusbar() {
 		return statusBar;
 	}
 
 	public void setStatusbar(JLabel statusbar) {
 		this.statusBar = statusBar;
 	}
 
 	public void mouseDragged(MouseEvent arg0) {
 		statusBar.setText(" Cursor Position: " + s.getCurPos()
 				+ "  |  Selected: x  |  Total Shapes: x");
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		statusBar.setText(" Cursor Position: " + s.getCurPos()
 				+ "  |  Selected: x  |  Total Shapes: x");
 	}
 
 	public void mouseClicked(MouseEvent arg0) {
 	}
 
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	public void mouseExited(MouseEvent arg0) {
 	}
 
 	public void mousePressed(MouseEvent arg0) {
 	}
 
 	public void mouseReleased(MouseEvent e) {
 	}
 
 	public static Scene getScene() {
 		return s;
 	}
 }
