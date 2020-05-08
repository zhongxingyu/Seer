 package autograph;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import autograph.ui.mainWindow;
 
 public class Autograph extends JFrame {
 
 	public Autograph(String title) {
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String filePath = "NewGraph.xml";
 		String writeTo = "NewGraph.xml";
 		final Graph graph = GraphHelper.mImportGraphFromXML(filePath);
 		GraphHelper.mExportGraphToXML(graph, writeTo);
 		int imageWidth = GraphHelper.mGetPreferredImageWidth(graph);
 		imageWidth = 800;
 		this.setPreferredSize(new Dimension(imageWidth, imageWidth));
 		this.setTitle(title + " - " + graph.mGetTitle());
 		JPanel panel = new JPanel() {
 			@Override
 			public void paint(Graphics g) {
 				super.paint(g);
 				g.setColor(Color.white);
 				g.fillRect(0, 0, getWidth(), getHeight());
 
 				try{
 					GraphHelper.mDrawForceDirectedGraph(graph, g, this);
 				}
 				catch(Exception e) {
 					e.getMessage();
 					e.getCause();
 				}
 			}
 		};
 		//	mainWindow win =  new mainWindow();
 
 
 		this.add(panel);
 		//this.setPreferredSize(new Dimension(800, 600));
 		this.pack();
 		this.setLocationRelativeTo(null);
 
 	}
 
 	public static void main(String[] args) {
 
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				Autograph autograph = new Autograph(
 						"Autograph");
 				autograph.setVisible(true);
 			}
 
 		});
 	}
 }
