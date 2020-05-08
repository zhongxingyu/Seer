 package raisa.vis;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * See http://arduino.cc/playground/Interfacing/Java for RXTX library setup
  */
 public class Visualizer extends JPanel implements Observer {
     private float mx;
     private float my;
     public List<Spot> spots;
     
     public Visualizer() {
         setBackground(Color.gray);
         addMouseMotionListener(new MouseAdapter() {
             @Override
             public void mouseMoved(MouseEvent mouseEvent) {
                 mx = mouseEvent.getX();
                 my = mouseEvent.getY();
                 repaint();
             }
         });
     }
     
     public void update(Observable o, Object arg) {
     	this.spots = (List<Spot>)arg;
     	repaint();
     }
     
     public void paintComponent(Graphics g) {
         g.clearRect(0, 0, getBounds().width, getBounds().height);
         float cx = getBounds().width * 0.5f;
         float cy = getBounds().height * 0.5f;
         g.setColor(Color.black);
         for (Spot spot : spots) {
             float w = 3.0f;
             float h = 3.0f;
             float x = cx + spot.x - 0.5f * w;
             float y = cy + spot.y - 0.5f * h;
             g.fillRect((int) x, (int) y, (int) w, (int) h);
         }
         g.setColor(Color.blue);
         g.drawLine((int)cx, (int)cy, (int)mx, (int)my);
         float dx = cx - mx;
         float dy = cy - my;
         float l = (float)Math.sqrt(dx * dx + dy * dy);
         g.drawString(l + " cm",  0, 20);
     }
     
     private static List<Sample> getExampleSamples() {
         ExampleWorld1 world = new ExampleWorld1();
         float x = 0.0f;
         float y = 0.0f;
         List<String> sampleStrings = new ArrayList<String>();
         for (int i = 0; i < 100; ++i) {
             sampleStrings.add(world.sample(x, y, i / 50.0f * (float)Math.PI));
         }
         List<Sample> samples = new ArrayList<Sample>();
         for (String str : sampleStrings) {
             samples.add(new Sample(x, y, str));
         }   
         return samples;
     }
     
     private static List<Sample> getFileSamples(String filename) throws Exception {
 		BufferedReader fr = new BufferedReader(new FileReader(filename));
 		List<Sample> samples = new ArrayList<Sample>();		
 		String line = fr.readLine();
 		while (line != null) {
 			System.out.println(line);
 			if (!line.matches("J\\d+,\\d+")) {
 				System.out.println("Invalid sample!");
 			} else {
 				samples.add(new Sample(0,0,line));
 			}
 			line = fr.readLine();
 		}
 		return samples;
     }
     
      
     public static void main(String[] args) throws Exception {
     	if (args.length==0) {
     		System.out.println("Usage: java ...");
     		return;
     	}
     	
     	List<Spot> spots = new ArrayList<Spot>();
     	String inputMode = args[0];
     	if ("serial".equals(inputMode)) {
     		SerialWorld serialWorld = new SerialWorld();
     		serialWorld.initialize();
     	} else if ("file".equals(inputMode)) {
     		if (args.length!=2) {
     			System.out.println("Missing filename");
     		}
     		String filename = args[1];
     		List<Sample> samples = getFileSamples(filename);
             spots = new ArrayList<Spot>();
             for (Sample sample : samples) {
                 spots.add(sample.getSpot());
             }            		
     	} else {
     		List<Sample> samples = getExampleSamples();
             spots = new ArrayList<Spot>();
             for (Sample sample : samples) {
                 spots.add(sample.getSpot());
             }            		
     	}
     	    	
         JFrame frame = new JFrame();
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         frame.setSize(600, 400);
         Visualizer visualizer = new Visualizer();
         visualizer.spots = spots;
         frame.add(visualizer);
         frame.setVisible(true);
     }
 }
