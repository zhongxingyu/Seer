 package ch.zhaw.labyrinth.controller;
 
 import ch.zhaw.labyrinth.model.MazeModel;
 import ch.zhaw.labyrinth.model.builder.Builder;
 import ch.zhaw.labyrinth.model.builder.DepthFirstSearch;
 import ch.zhaw.labyrinth.view.MazePanel;
 import ch.zhaw.labyrinth.view.MazeView;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.image.BufferedImage;
 
 /**
  * Controller
  */
 public class MazeController {
     private MazeModel model;
     private MazeView view;
     private MazePanel mazePanel;
     private Builder mazeBuilder;
 
     /**
      * TODO: JavaDoc
      * @param model
      * @param view
      */
     public MazeController(MazeModel model, MazeView view) {
         this.model = model;
         this.view = view;
 
         // Create Gui
         view.createAndShowGUI();
 
         // Add Listeners
         view.addImportListener(new ImportItemListener());
         view.addChangeSpeedListener(new SpeedChangeAction());
         view.addCreateListener(new CreateActionListener());
         //view.addSolveListener(new SolveActionListener());
 
     }
 
 
     /**
      * ActionListener for Create Algorithm Button
      */
     private class CreateActionListener implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
 
             new Thread(new Runnable() {
                 public void run() {
                     startCreate();
                 };
             }).start();
 
         }
     }
 
     private void startCreate() {
         // create maze panel
         mazePanel = new MazePanel(view.getDimension(),view.getZoom());
 
         // Configure the MazePanel object
         mazePanel.setMode("create");
         mazePanel.setSpeed(view.getSpeed());
 
         // Build panel
         mazePanel.buildPanel();
 
         // Build new Frame
         JFrame mazeFrame = new JFrame("Maze");
         mazeFrame.add(mazePanel);
         mazeFrame.pack();
         mazeFrame.setLocationRelativeTo(null);
         mazeFrame.setResizable(false);
        mazeFrame.setVisible(true);
 
         // Get Build Type
         String createAlgorithm = view.getCreateAlgorithm();
 
         // Build selected maze
         if (createAlgorithm.equals("Depth-First")) {
             mazeBuilder = new DepthFirstSearch(model, view.getDimension());
         } else if (createAlgorithm.equals("Import")) {
             view.showFileChooser();
         }
 
         // Register observer
         mazeBuilder.registerObserver(mazePanel);
 
         // Build Maze
         model = mazeBuilder.build();
 
         // print data structure as array if debugging is enabled
         if(view.getDebug()){
             model.printAsArray();
         }
     }
 
 //    /**
 //     * ActionListener for Solve Button
 //     */
 //    private class SolveActionListener implements ActionListener {
 //        @Override
 //        public void actionPerformed(ActionEvent ae) {
 //            // Get Build Type
 //            String type = view.getSolveAlgorithm();
 //
 //            // Build selected MazePanel
 //            if (type.equals("Right-Hand")) {
 //                setLbsolver(new RightHand(lbuilder));
 //            } else if (type.equals("A* Search")) {
 //                setLbsolver(new AStar(lbuilder));
 //            } else {
 //                setLbsolver(null);
 //            }
 //            setMode("solve");
 //            startSolver();
 //
 //
 //        }
 //    }
 
     private class SpeedChangeAction implements ChangeListener {
         @Override
         public void stateChanged(ChangeEvent e) {
             view.setSpeed(view.getSlider().getValue());
            mazePanel.setSpeed(view.getSpeed());
             String str = Integer.toString(view.getSpeed());
             view.setSliderLabel(str);
         }
     }
 
     /**
      * This ItemListeners does fire when the item of the createList is getting changed.
      * if the selected item is Import it does show JFileChooser otherwise it does nothing.
      * @author d.schlegel
      */
     private class ImportItemListener implements ItemListener {
 
         @Override
         public void itemStateChanged(ItemEvent e) {
             if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() == "Import") {
 
                 view.showFileChooser();
             }
         }
     }
 
 }
