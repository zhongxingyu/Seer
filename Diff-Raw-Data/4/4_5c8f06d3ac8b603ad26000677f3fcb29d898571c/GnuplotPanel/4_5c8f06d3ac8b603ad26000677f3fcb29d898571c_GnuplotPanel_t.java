 package evaluation.simulator.gui.results;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
 
 import org.apache.batik.swing.JSVGCanvas;
 
 import evaluation.simulator.gui.customElements.ConfigChooserPanel;
 import evaluation.simulator.gui.layout.frames.GraphFrame;
 
 @SuppressWarnings("serial")
 public class GnuplotPanel extends JPanel {
 
 	public JSVGCanvas svgCanvas;
 	public static String outputFolder = "inputOutput/simulator/output/";
 
 	public GnuplotPanel(final String gnuplotResultFileName) {
 		// BufferedImage resultsDiagram = null;
 		try {
 			File f = new File(outputFolder + gnuplotResultFileName);
 			while (!f.exists()) {
 				Thread.sleep(1);
 			}
 
 			GridBagLayout gridBagLayout = new GridBagLayout();
 			GridBagConstraints gridBagConstraints = new GridBagConstraints();
 			gridBagConstraints.fill = GridBagConstraints.BOTH;
 			gridBagConstraints.anchor = GridBagConstraints.NORTH;
 			gridBagConstraints.weightx = 1.0;
 			gridBagConstraints.weighty = 1.0;
 			gridBagLayout.setConstraints(this, gridBagConstraints);
 			this.setLayout(gridBagLayout);
 
 			gridBagConstraints.gridx = 0;
 			gridBagConstraints.gridy = 0;
 			svgCanvas = new JSVGCanvas();
 			this.svgCanvas.addMouseListener(new MouseAdapter() {
 
 				public void mousePressed(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
 						JPopupMenu menu = new JPopupMenu();
 						JMenuItem item = new JMenuItem("Show in seperate window");
 						item.addActionListener(new ActionListener() {
 							public void actionPerformed(ActionEvent e) {
 								JFrame externalGraphView = GraphFrame.getInstance(svgCanvas.getURI(),
 										gnuplotResultFileName);
 							}
 						});
 						menu.add(item);
 
 						menu.show(svgCanvas, e.getX(), e.getY());
 					}
 				}
 			});
 			this.add(svgCanvas, gridBagConstraints);
 			svgCanvas.setURI(f.toURL().toString());
 			ConfigChooserPanel.getInstance().exportPictureButton.setEnabled(true);
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		;
 	}
 
 }
