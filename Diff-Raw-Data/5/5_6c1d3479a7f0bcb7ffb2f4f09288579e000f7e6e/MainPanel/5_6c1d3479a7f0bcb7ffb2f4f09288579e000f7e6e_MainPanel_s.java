 package gr.uoi.cs.dmod.hecate.gui.swing;
 
 import gr.uoi.cs.dmod.hecate.graph.tree.HecateRowModel;
 import gr.uoi.cs.dmod.hecate.graph.tree.HecateTreeModel;
 import gr.uoi.cs.dmod.hecate.graph.tree.HecateTreeRenderer;
 import gr.uoi.cs.dmod.hecate.sql.Schema;
 
 import java.awt.GridLayout;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import org.netbeans.swing.outline.DefaultOutlineModel;
 import org.netbeans.swing.outline.Outline;
 import org.netbeans.swing.outline.OutlineModel;
 
 /**
  * This Panel contains two panes with the original and the modified
  * version of the generated trees of the schemas.
  * @author giskou
  *
  */
 @SuppressWarnings("serial")
 public class MainPanel extends JPanel {
 	
 	private JScrollPane leftScrollPane;
 	private JScrollPane rightScrollPane;
 	private Outline leftOutline;
 	private Outline rightOutline;
 	private HecateTreeModel leftTreeModel;
 	private HecateTreeModel rightTreeModel;
 	private OutlineModel leftOutlineModel;
 	private OutlineModel rightOutlineModel;
 	
 	/**
 	 * Default Constructor.
 	 */
 	public MainPanel() {
 		setLayout(new GridLayout(1,2));
 		
 		leftScrollPane = new JScrollPane();
 		add(leftScrollPane);
 
 		
 		rightScrollPane = new JScrollPane();
 		add(rightScrollPane);
 
 	}
 	
 	/**
 	 * Creates and draws a tree of the schema <code>s</code> at side
 	 * <code>side</code>.
 	 * @param s the schema to be drawn.
 	 * @param side the side of the Panel the schema will be drawn.
 	 * Accepted values are <code>"old"</code> for the left side and
 	 * <code>"new"</code> for the right side.
 	 */
 	protected void drawSchema(Schema s, String side) {
		if (side == "old") {
 			leftTreeModel = new HecateTreeModel(s);
 			leftOutlineModel = DefaultOutlineModel.createOutlineModel(leftTreeModel,
 			                     new HecateRowModel(), true, "Name");
 			leftOutline = new Outline();
 			leftOutline.setModel(leftOutlineModel);
 			leftOutline.setRenderDataProvider(new HecateTreeRenderer());
 			leftScrollPane.add(leftOutline);
 			leftScrollPane.setViewportView(leftOutline);
 		}
		else if (side == "new") {
 			rightTreeModel = new HecateTreeModel(s);
 			rightOutlineModel = DefaultOutlineModel.createOutlineModel(rightTreeModel,
 			                      new HecateRowModel(), true, "Name");
 			rightOutline = new Outline();
 			rightOutline.setModel(rightOutlineModel);
 			rightOutline.setRenderDataProvider(new HecateTreeRenderer());
 			rightScrollPane.add(rightOutline);
 			rightScrollPane.setViewportView(rightOutline);
 		}
 	}
 }
