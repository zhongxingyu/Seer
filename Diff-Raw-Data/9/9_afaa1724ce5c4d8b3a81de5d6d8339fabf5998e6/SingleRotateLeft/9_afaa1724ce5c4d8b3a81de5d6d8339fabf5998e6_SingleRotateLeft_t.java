 package rotationAnimation;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 
 public class SingleRotateLeft extends JApplet {
 	public static void main(String s[]) {
 		JFrame frame = new JFrame();
 		frame.setTitle("Insert");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JApplet applet = new SingleRotateLeft();
 		applet.init();
 		frame.getContentPane().add(applet);
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public void init(){
 		getContentPane().add(new DrawingPanel());
 	}
 
 	private class DrawingPanel extends AbstractDrawingPanel{
 		private Thread thread;
 		private JButton button, stopButton, playButton;
 		private GrahpicalNode node1, node2, node4, node6, node10, node12, node14, node16;
 		private int iteration, maxIteration = 7, y = 0, j = 0;
 		public DrawingPanel(){
 			this.setLayout(new BorderLayout());
 			button = new JButton("Next");
 			button.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					if(iteration <maxIteration) iteration++;
 					else if(iteration >= maxIteration) iteration = 0;
 					start(); 
 					System.out.println(iteration);
 					repaint();
 				}
 			});
 
 			playButton = new JButton("Play");
 			playButton.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					start();
 
 				}
 			});
 
 			stopButton = new JButton("Pause");
 			stopButton.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					stop();
 
 				}
 			});
 			JPanel southButtonPanel = new JPanel();
 			southButtonPanel.setLayout(new FlowLayout());
 			southButtonPanel.add(stopButton);
 			southButtonPanel.add(playButton);
 			southButtonPanel.add(button);
 			this.add(southButtonPanel, BorderLayout.SOUTH);
 			iteration = 0;
 		}
 
 
 
 
 		public void paintComponent(Graphics g){
 			super.paintComponents(g);
 			Graphics2D g2 = (Graphics2D) g;
 			g2.clearRect(0, 0, getWidth(), getHeight());
 			if(iteration == 0){
				node1 = new GrahpicalNode("1", 170, 310);
				paintNode(node1, g2);
 			}
 		}
 
 		public void stop() {
 			if(thread != null) thread = null;
 		}
 
 		public void start() {
 
 			if (thread == null) {
 				thread = new Thread(this);
 				thread.start();
 			}
 		}
 
 		@Override
 		public void run() {
 			while (thread != null) {
 				System.out.println(iteration);
 				try{
 					Thread.sleep(100);
 				}
 				catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				repaint();
 			}
 		}
 	}
 }
