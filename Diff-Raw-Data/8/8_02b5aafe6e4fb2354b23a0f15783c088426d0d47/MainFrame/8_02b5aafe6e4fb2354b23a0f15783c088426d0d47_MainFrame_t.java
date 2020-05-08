 package de.osmui.ui;
 
 import java.awt.BorderLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JSplitPane;
 
 import de.osmui.model.pipelinemodel.JGPipelineModel;
 import de.osmui.ui.models.TaskBoxTableModel;
 import de.osmui.util.CommandlineTranslator;
 import de.osmui.util.ConfigurationManager;
 import de.osmui.util.exceptions.ImportException;
 
 public class MainFrame extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4767348652713972190L;
 
 	private static MainFrame instance;
 
 	protected TaskBoxTableModel taskBoxTableModel;
 	// Holds the right split pane that contains the pipelineBox with the
 	// pipeline's graph representation and the copyBox on the lower side of this
 	// split.
 	protected ContentSplitPane rightContent;
 	// Holds the main split pane that contains the right split pane and
 	// the tabBox component on the left side of this split.
 	protected ContentSplitPane content;
 
 	protected JGPipelineModel pipeModel;
 
 	// Prevents the creation of the object with other methods
 	private MainFrame() {
 		pipeModel = new JGPipelineModel();
 		taskBoxTableModel = new TaskBoxTableModel();
 		rightContent = new ContentSplitPane(JSplitPane.VERTICAL_SPLIT,
 				new PipelineBox(pipeModel.getGraph()), new CopyBox());
 		content = new ContentSplitPane(JSplitPane.HORIZONTAL_SPLIT, new TabBox(
 				taskBoxTableModel), rightContent);
 		Menu menu = new Menu();
 		this.setJMenuBar(menu);
 
 		setLayout(new BorderLayout());
 		add(content, BorderLayout.CENTER);
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				ConfigurationManager.saveConfiguration();
 			}
 
 		});
 		// TESTCODE
 		CommandlineTranslator trans = CommandlineTranslator.getInstance();
 		try {
 			trans.importLine(
 					pipeModel,
 					"--rx full/planet-071128.osm.bz2 "
							+ "--tee as  \\"
 							+ "--bp file=polygons/europe/germany/baden-wuerttemberg.poly  \\"
 							+ "--wx baden-wuerttemberg.osm.bz2  \\"
 							+ "--bp file=polygons/europe/germany/bayern.poly "
 							+ "--wx bayern.osm.bz2 ");
 
 		} catch (ImportException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		pipeModel.layout();
 		try {
 			taskBoxTableModel.showCompatibleTasks("read-xml");
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * @return the ContentDeviderLocation
 	 */
 	public int getContentDeviderLocation() {
 		return content.getDividerLocation();
 	}
 
 	/**
 	 * @param ContentDividerLocation
 	 *            the ContentDividerLocation to set
 	 */
 	public void setContentDevider(int contentDividerLocation) {
 		content.setDividerLocation(contentDividerLocation);
 	}
 
 	/**
 	 * @return the RightContentDeviderLocation
 	 */
 	public int getRightContentDeviderLocation() {
 		return rightContent.getDividerLocation();
 	}
 
 	/**
 	 * @param rightContentDividerLocation
 	 *            the rightContentDividerLocation to set
 	 */
 	public void setRightContentDeviderLocation(int rightContentDividerLocation) {
 		rightContent.setDividerLocation(rightContentDividerLocation);
 	}
 
 	/**
 	 * @return the taskBoxTableModel
 	 */
 	public TaskBoxTableModel getTaskBoxTableModel() {
 		return taskBoxTableModel;
 	}
 
 	// A access method on class level, which creates only once a instance a
 	// concrete object
 	// in a session of OsmUi and returns it.
 	public static MainFrame getInstance() {
 		if (MainFrame.instance == null) {
 			MainFrame.instance = new MainFrame();
 		}
 		return MainFrame.instance;
 	}
 
 }
