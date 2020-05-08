 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.awt.GLJPanel;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.JTree;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import com.jogamp.opengl.util.FPSAnimator;
 
 // Main view with layout
 // contends so far: the frame holds a screen filling horizJSplitPane, which has a JTree as its left,
 // and a JScrollPane with a JLabel holding multiple GLJPanels as its right component
 // TODO: why is JScrollPane not scrolling?
 public class MainView extends JFrame
 {
	private static final long serialVersionUID = 1L;

 	// graph panel
 	private JPanel graphPanel;
 	
 	// primary split pane
 	JSplitPane mainSplitPane;
 	
 	// scrollpane holding the visualizations
 	JScrollPane graphListPane;
 	
 	// tree containing car data types
 	private JTree dataBlocksTree;
 	
 	public MainView(String title)
 	{
 		super(title);
 		setSize(1024, 768);
 		Container contentPane = this.getContentPane();
 		setExtendedState(JFrame.MAXIMIZED_BOTH);
 		// quit program when window is closed
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		graphPanel = new JPanel();
 		graphPanel.setLayout( new BoxLayout(graphPanel, BoxLayout.Y_AXIS) );
		graphListPane = new JScrollPane(graphPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		
 		GLProfile glprofile = GLProfile.getDefault();
         GLCapabilities glcapabilities = new GLCapabilities( glprofile );
 		for(int i = 0; i < 4; ++i)
 	    {
 			// TODO: replace GLJPanel with ParameterView once implemented
 			GLJPanel panel = new GLJPanel( glcapabilities );
 	        if(i % 2 == 0) panel.addGLEventListener(new ParameterGraph(2.5, 0, 45));
 	        else panel.addGLEventListener(new ParameterQuad(-1.25, 1.25, 0.0));
 	        FPSAnimator animator = new FPSAnimator(panel, 30);
 	        animator.start();
 	        panel.setMinimumSize(new Dimension(500, 300));
 	        graphPanel.add(panel);
 	        graphPanel.add(Box.createRigidArea( new Dimension(50, 8)));
 	        graphPanel.setMinimumSize(new Dimension(500, i * 400));
 	    }
 
 		// fill tree
 		DefaultMutableTreeNode dataRoot = new DefaultMutableTreeNode("DataBlocks");
 		dataBlocksTree = new JTree(dataRoot);
 		DefaultMutableTreeNode general = new DefaultMutableTreeNode("General");
 		general.add(new DefaultMutableTreeNode("stuff"));
 		DefaultMutableTreeNode engine = new DefaultMutableTreeNode("Engine");
 		engine.add(new DefaultMutableTreeNode("stuff"));
 		DefaultMutableTreeNode tires = new DefaultMutableTreeNode("Tires");
 		tires.add(new DefaultMutableTreeNode("stuff"));
 		DefaultMutableTreeNode environment = new DefaultMutableTreeNode("Environment");
 		environment.add(new DefaultMutableTreeNode("stuff"));
 		dataRoot.add(general);
 		dataRoot.add(engine);
 		dataRoot.add(tires);
 		dataRoot.add(environment);
 
 		// setup split pane
         mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dataBlocksTree, graphListPane);
         mainSplitPane.setDividerLocation(130);
         mainSplitPane.setContinuousLayout(true);
 
         contentPane.add(mainSplitPane, BorderLayout.CENTER);
 	}
 
 }
