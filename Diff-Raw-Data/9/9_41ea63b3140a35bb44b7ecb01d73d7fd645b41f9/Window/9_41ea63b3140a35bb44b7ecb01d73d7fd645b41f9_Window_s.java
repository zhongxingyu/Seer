 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 public class Window extends JFrame implements ActionListener, ComponentListener {
 	private static final long serialVersionUID = 1L;
 
 	private JPanel contentPane;
 
 	private float RATIO = 16 / (float) 9;
 
 	private JLabel label1, label2 ;
 	private JTextField text ;
 	private JList list ;
 
 	private JButton button1, button2, button3, button4 ;
 	private JButton button5, button6, button7 ;
 
 	private JOptionPane jop = new JOptionPane();
 	private int previousX = 480 ;
 	private int previousY = 270 ;
 
 	public Window() {
 		// window settings
 		setTitle("UI assignemnt 2") ;
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
 		setLocation (100,100) ;
 		setSize(480, 270);
 		setMinimumSize(new Dimension(480, 270)) ;
 		setResizable(true) ;
 		setVisible(true) ;
 
 		contentPane = new JPanel();
 		contentPane.setLayout(new LayoutMan());
 
 		initComponents() ;
 		contentPane.addComponentListener(this);
 		setContentPane(contentPane);
 		pack();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		Dimension a = new Dimension(270, 480) ;
 		Dimension b = new Dimension(480, 270) ;
 		Dimension c ;
 		
 		if (arg0.getSource() == button1) {
 			jop.showMessageDialog(getComponent(0), "HOLE");
 		}
 		if (arg0.getSource() == button2) {
 			RATIO = 1/RATIO;
 			setSize(getHeight(), getWidth()) ;
 			setMinimumSize(a) ;
 			
 			// switch a and b
 			c = b ;
 			b = a ;
 			a = c ;
 		}
 
 	}
 
 	public void componentResized(ComponentEvent e) {
 		Container window = e.getComponent().getParent().getParent().getParent();
 		Dimension compDim = window.getSize();
 		int x = compDim.width;  
 	    int y = compDim.height;
 	    int x_change = x - this.previousX;
 	    int y_change = y - this.previousY;
 	    
 	    if (x_change == 0 && y_change != 0) {
 	    	compDim.width = (int) (y*RATIO);
 	    	compDim.height = y;
 	    }
 	    else if (x_change != 0 && y_change == 0) {
 	    	compDim.width = x;
 	    	compDim.height = (int) (x/RATIO);
 	    }
 	    else if (x_change != 0 && y_change != 0) {
 	    	compDim.width = x ;
 	    	compDim.height = (int) (x/RATIO);
 	    }
 	    else {
 	    	//No change
 	    }	
 
 		window.setSize(compDim);
 	    
 	    //save the current width and height for the next call of this method
 	    this.previousX = compDim.width ;
 	    this.previousY = compDim.height ;
 	    
 	}
 	
 	@Override
 	public void componentHidden(ComponentEvent arg0) {}
 
 	@Override
 	public void componentMoved(ComponentEvent arg0) {}
 
 	@Override
 	public void componentShown(ComponentEvent arg0) {}
 	
 	public void initComponents() {
 		button1 = new JButton("");
 		button1.addActionListener(this);
 		button1.setBackground(Color.white);
 
 		label1 = new JLabel("#UI");
 		button2 = new JButton();
 		button2.addActionListener(this) ;
 		button2.setBackground(Color.LIGHT_GRAY);
 		text = new JTextField("Search a contact...");
 		text.setForeground(Color.lightGray);
 		button3 = new JButton();
 		String[] listContents = { "Serge Nahas", "Damien Gruel","Cyrielle Recoura", "Marion Grosjean", "Lafayette rouflaquettesssssssssssssssssss" };
 		list = new JList(listContents);
 		button4 = new JButton();
 		button4.setBackground(Color.white);
 		button5 = new JButton();
 		button5.setBackground(Color.white);
 		button6 = new JButton("");
 		button6.setBackground(Color.white);
 		button7 = new JButton("");
 		button7.setBackground(Color.white);
		label2 = new JLabel("LABEL2");
 
 		contentPane.add(label1);
 		contentPane.add(button1);
 		contentPane.add(button2);
 		contentPane.add(text);
 		contentPane.add(button3);
 		 
 		contentPane.add(new JScrollPane(list));
 		contentPane.add(button4);
 		contentPane.add(button5);
 		contentPane.add(button6);
 		contentPane.add(button7);
 		contentPane.add(label2);
 	}
 
 }
