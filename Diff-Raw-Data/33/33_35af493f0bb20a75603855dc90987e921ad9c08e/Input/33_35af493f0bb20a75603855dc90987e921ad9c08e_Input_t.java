 package ModuleGUIs;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.io.IOException;
 
 import Engine.Module;
 import GUI.ContainerWindow;
 import GUI.ModuleGUI;
 import GUI.PortGUI;
 
 public class Input extends ModuleGUI
 {
 	public int width;
 	public int height;
 	protected int text_width;
 	protected int text_height;
 	
 	public Input(ContainerWindow main_window, Module module) throws IOException
 	{
 		super(main_window, module);
 		
 		width = 50;
 		height = 50;
 		
 		name = "Frequency Input";
 		
 		// FIXME: Find out how to guess the length of a string
 		text_width = name.length()*6+2;
 		text_height = 10+2;
 		
 		// FIXME: Make all these magic numbers constants or lookups
 		width = Math.max(26, text_width);
 		height = 2*10 + 10;
 		
 		Dimension d = new Dimension(width, height+text_height);
 		setMinimumSize(d);
 		setPreferredSize(d);
 		setSize(d);
 		
 		// Creating the ports
 		int tmpx, tmpy;
 		input_ports = new PortGUI[0];
 		output_ports = new PortGUI[1];
 
 		double a = (width - 10.0) / (2.0);
 		tmpx = (int) Math.round(a);
 		// Should be 2.5; FIXME: Replace with height
 		tmpy = height/2 - 5;
 		
 		output_ports[0] = new PortGUI(main_window, module, Engine.Constants.OUTPUT_PORT, 0);
 		output_ports[0].setLocation(tmpx, tmpy + text_height);
 		add(output_ports[0]);
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		
 		Graphics2D g2d = (Graphics2D) g;
 		
 		g2d.setColor(Color.black);
 		
 		// Yay copying from zetcode, section on Painting (see sources file)
 		float[] dash3 = { 4f, 0f, 2f };
         BasicStroke bs3 = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash3, 2f );	
 		
         Font font = new Font("Arial", Font.BOLD, text_height-2);
 		g2d.setFont(font);
         g2d.drawString(name, 1, text_height-1);
         
 		g2d.setColor(Color.black);
 		g2d.drawRect(0, 0, width-1, text_height+height-1);// Implicit 2-point distance to text
 		g2d.drawRect(0, text_height, width, height);  
 		// [/Copying]
 		
 		int tmpx, tmpy;
 
 		double a = (width - 10.0) / (2.0);
 		tmpx = (int) Math.round(a);
 		// Should be 2.5; FIXME: Replace with height
 		tmpy = height/2 - 5;
 		output_ports[0].setLocation(tmpx, tmpy + text_height);
 	}
 }
