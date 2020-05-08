 package it.wolfed.swing;
 
 import it.wolfed.model.PetriNetGraph;
 import it.wolfed.util.SpringUtilities;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 
 
 public class OperationDialog extends JDialog
 {
     private int requiredGraphs;
     private List<PetriNetGraph> avaiableGraphs;
     private List<PetriNetGraph> selectedGraphs;
 
     /**
      * @param avaiableGraphs 
      * @param requiredGraphs 
      */
     public OperationDialog(List<PetriNetGraph> avaiableGraphs, int requiredGraphs)
     {
         this.requiredGraphs = requiredGraphs;
         this.avaiableGraphs = avaiableGraphs;
         this.selectedGraphs = new ArrayList<>();
 
         setModal(true);// Stop thread
         setTitle("Select " + requiredGraphs + " Workflow/Petri Net.");
         setSize(300, 200);
         setLocationRelativeTo(null);
         setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 
         // Selection window
         JComponent newContentPane = createSelectionWindow();
         newContentPane.setOpaque(true);
         setContentPane(newContentPane);
         setVisible(true);
     }
     
     public List<PetriNetGraph> getSelectedGraphs()
     {
         return selectedGraphs;
     }
 
     private JPanel createSelectionWindow()
     {
         SpringLayout layout = new SpringLayout();
         JPanel window = new JPanel(layout);
        final List<JComboBox> boxList = new ArrayList<>();
 
         for (int i = 0; i < requiredGraphs; i++)
         {
            JComboBox box = new JComboBox();
             
             for (PetriNetGraph graph : avaiableGraphs)
             {
                 box.addItem(graph);
             }
             
             JLabel label = new JLabel(":: Available WF/PN ::");
             label.setLabelFor(box);
 
             boxList.add(box);
             window.add(label);
             window.add(box);
         }
 
         //Layout the panel.
         SpringUtilities.makeCompactGrid(window,
                 requiredGraphs, 2,  //rows, cols
                 6, 6,               //initX, initY
                 6, 6);              //xPad, yPad
 
         JButton selectButton = new JButton("Select");
         selectButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 selectedGraphs.clear();
 
                 for(JComboBox box : boxList)
                 {
                     selectedGraphs.add((PetriNetGraph) box.getSelectedItem()); 
                 }
                 
                 if(selectedGraphs.size() == requiredGraphs)
                 {
                     setVisible(false);
                 }
             }
         });
         
         JPanel mainWindow = new JPanel(new BorderLayout());
         mainWindow.add(window, BorderLayout.PAGE_START);
         mainWindow.add(selectButton, BorderLayout.PAGE_END);
 
         return mainWindow;
     }
 } 
