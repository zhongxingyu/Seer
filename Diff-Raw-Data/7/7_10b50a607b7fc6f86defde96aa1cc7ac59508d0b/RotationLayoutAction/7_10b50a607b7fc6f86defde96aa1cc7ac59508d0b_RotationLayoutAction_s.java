 package cytoscape.actions;
 
 import cytoscape.Cytoscape;
 import cytoscape.foo.GraphConverter;
 import cytoscape.graph.layout.algorithm.MutableGraphLayout;
 import cytoscape.graph.layout.impl.RotationLayouter;
 import cytoscape.util.CytoscapeAction;
 import java.awt.BorderLayout;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.WindowConstants;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class RotationLayoutAction extends CytoscapeAction
 {
 
   public RotationLayoutAction()
   {
     super("Rotate Graph");
     setPreferredMenu("Layout");
   }
 
   public void actionPerformed(ActionEvent e)
   {
    final MutableGraphLayout nativeGraph = GraphConverter.getGraphCopy(1.0d);
     Frame cyFrame = Cytoscape.getDesktop();
     JDialog dialog = new JDialog(cyFrame, "Rotate", true);
     dialog.setResizable(false);
     dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
     JPanel panel = new JPanel(new BorderLayout());
     panel.setBorder(new EmptyBorder(20, 20, 20, 20));
     panel.add(new JLabel("Degrees of Rotation:"), BorderLayout.CENTER);
     final JSlider slider = new JSlider(0, 360, 0);
     slider.setMajorTickSpacing(90);
     slider.setMinorTickSpacing(15);
     slider.setPaintTicks(true);
     slider.setPaintLabels(true);
     slider.addChangeListener(new ChangeListener() {
         private int prevValue = slider.getValue();
         public void stateChanged(ChangeEvent e) {
           double radians = ((double) (slider.getValue() - prevValue)) *
             2.0d * Math.PI / 360.0d;
           RotationLayouter.rotateGraph(nativeGraph, radians);
           prevValue = slider.getValue(); } });
     panel.add(slider, BorderLayout.SOUTH);
     dialog.getContentPane().add(panel, BorderLayout.CENTER);
     dialog.pack();
     dialog.move((cyFrame.size().width - dialog.size().width) / 2 +
                 cyFrame.location().x,
                 (cyFrame.size().height - dialog.size().height) / 5 +
                 cyFrame.location().y);
     dialog.show(); // This blocks until dialog is disposed of.

    GraphConverter.updateCytoscapeLayout(nativeGraph);
   }
 
 }
