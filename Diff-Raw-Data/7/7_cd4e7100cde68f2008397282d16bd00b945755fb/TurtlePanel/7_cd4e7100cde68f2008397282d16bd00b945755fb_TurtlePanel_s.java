 package tdanford.turtle;
 
 import java.util.*;
 import java.awt.*;
 
 public class TurtlePanel extends JPanel { 
 	
 	public static void main(String[] args) { 
 		TurtleSettings settings = new TurtleSettings();
 		Turtle t = new Turtle(settings);
 		Program p = new Program("FF-FF+F+F-");
 		TurtlePanel panel = new TurtlePanel(t, p);
 		TurtleFrame frame = new TurtleFrame(panel);
 	}
 	
 	private Turtle turtle;
 	private Program program;
 
 	public TurtlePanel(Turtle t, Program p) { 
 		turtle = t;	
 		program = p;
 	}
 
 	public void setState(Turtle t, Program p) { 
 		turtle = t;
 		program = p;
 	}
 
 	protected void paintComponent(Graphics g) { 
 		super.paintComponent(g);
 		int w = getWidth(), h = getHeight();
 		int cx = w/2, cy = h/2;
 		Graphics2D g2 = (Graphics2D)g;
 
 		g2.setColor(Color.white);
 		g2.fillRect(0, 0, w, h);
 		g2.setColor(Color.black);
 
 		g2.translate(cx, cy);
 
 		turtle.execute(g2, program);
 
 		g2.translate(-cx, -cy);
 	}
 }
 
 class TurtleFrame extends JFrame { 
 	
 	private TurtlePanel panel;
 
 	public TurtleFrame(TurtlePanel tp) { 
 		super("Turtle");
 		panel = tp;
 		panel.setOpaque(true);
		this.setContainer(panel);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.setDefaultSize(new Dimension(500, 500));
 
 		setVisible(true);
 		pack();
 	}
 }
 
