 package simplegui;
 
 import item.Item;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public abstract class SimpleGUI {
 	
 	private JFrame frame;
 	private JPanel panel;
 	
 	public SimpleGUI(String title, final int width, final int height) {
 		frame = new JFrame(title);
 		panel = new JPanel(null) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

 			@Override public void paintComponent(Graphics g) {
 				super.paintComponent(g);
 				SimpleGUI.this.paint((Graphics2D)g);
 			}
 		};
 		panel.setPreferredSize(new Dimension(width,height));
 		panel.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				handleMouseClick(e.getX(), e.getY(), e.getClickCount() == 2);
 			}
 			
 		});
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().add(panel);
 		frame.pack();
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 	
 	public abstract void paint(Graphics2D graphics);
 	
 	public void handleMouseClick(int x, int y, boolean doubleClick) {
 	}
 	
 	public final void repaint() {
 		panel.repaint();
 		
 	}
 	
 	public final Button createButton(int x, int y, int width, int height, Runnable clickHandler) {
 		JButton button = new JButton();
 		button.setLocation(x, y);
 		button.setSize(width, height);
 		panel.add(button);
 		Button b = new Button(button);
 		b.setClickHandler(clickHandler);
 		return b;
 	}
 	
 	public final ComboBox createComboBox(int x, int y, int width, int height, Runnable clickHandler) {
 		JComboBox<Item> comboBox = new JComboBox<Item>();
 		comboBox.setLocation(x, y);
 		comboBox.setSize(width, height);
 		panel.add(comboBox);
 		ComboBox b = new ComboBox(comboBox);
 		b.setClickHandler(clickHandler);
 		return b;
 	}
 	
 	public final Image loadImage(String url, int width, int height) {
 		Image image = panel.getToolkit().createImage(ClassLoader.getSystemResource(url));
 		MediaTracker tracker = new MediaTracker(panel);
 		tracker.addImage(image, 1, width, height);
 		try {
 			tracker.waitForAll();
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 		return image;
 	}
 	
 	public void resize(int width, int height){
 		panel.setPreferredSize(new Dimension(width,height));
 		frame.pack();
 		frame.repaint();
 		repaint();
 	}
 	
 	public void quit(){
 		frame.dispose();
 	}
 	
 }
