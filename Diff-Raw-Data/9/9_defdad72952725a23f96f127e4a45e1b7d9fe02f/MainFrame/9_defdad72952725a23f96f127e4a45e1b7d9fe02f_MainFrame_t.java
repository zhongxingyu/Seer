 import java.awt.Button;
 import java.awt.TextField;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 
 public class MainFrame extends JFrame{
 	private java.awt.Color userColor;
 	private ColorsPanel panel1;
 	private NamesPanel panel2;
 	
 	public MainFrame() {
 		super("Wow, someone forgot to set the window title!");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(500, 600);
 		addElements();
 	}
 	public MainFrame(String title) {
 		super(title);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(500, 600);
 		addElements();
 	}
 	public void addElements(){
 		JMenuBar menus=new JMenuBar();
 		JMenu fileMenu = new JMenu();
 		JMenuItem Exit = new JMenuItem("");
 		menus.add(fileMenu);
 		this.add(new TextField());
 		this.getComponent(0).setEnabled(false);
 	}
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		MainFrame main = new MainFrame();
		main.setVisible(true);
 	}
 
 }
 //BorderLayout is north, south
 //FlowLayout
 //GridLayout
