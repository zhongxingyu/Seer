 package us.percept.pile.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import us.percept.pile.repo.ArxivSource;
 import us.percept.pile.repo.PaperSource;
 import us.percept.pile.store.PaperFetcher;
 import us.percept.pile.store.PaperIndex;
 import us.percept.pile.store.PaperStorage;
 import us.percept.pile.view.ModeView;
 import us.percept.pile.view.ModeViewListener;
 import us.percept.pile.view.PileView;
 
 import javax.swing.*;
 import java.awt.*;
import java.io.File;
 import java.io.IOException;
 
 /**
  * Author: spartango
  * Date: 8/6/13
  * Time: 10:05 PM.
  */
 public class ApplicationController implements Controller, ModeViewListener {
     private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);
 
     // Controllers
     private QueueController   queueController;
     private SearchController  exploreController;
     private ArchiveController archiveController;
 
     private Controller activeController;
 
     // Views
     private PileView pileView;
     private ModeView modeView;
     private JFrame   frame;
 
     // Model facilities
     private PaperSource  paperSource;
     private PaperFetcher paperFetcher;
     private PaperStorage paperStorage;
     private PaperIndex   paperIndex;
 
     public void onLoad() {
         // Setup the model facilities
         paperSource = new ArxivSource();
        paperFetcher = new PaperFetcher(System.getProperty("user.dir") + File.separator + "papers");
         paperStorage = new PaperStorage();
 
         try {
             paperIndex = new PaperIndex(paperStorage);
         } catch (IOException e) {
             logger.error("Failed to initialize the index", e);
             return;
         }
 
         // Setup the views
         pileView = new PileView();
         modeView = new ModeView();
 
         // Setup the controllers
         queueController = new QueueController(pileView, paperStorage, paperSource, paperFetcher, paperIndex);
         exploreController = new SearchController(pileView, paperStorage, paperSource, paperFetcher);
         archiveController = new ArchiveController(pileView, paperStorage, paperIndex);
 
         // Listen to mode transitions
         modeView.addListener(this);
 
         // Setup the frame
         frame = new JFrame("Pile");
         pileView.add(modeView, new com.intellij.uiDesigner.core.GridConstraints(2,
                                                                                 0,
                                                                                 1,
                                                                                 2,
                                                                                 com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                                 com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                                 com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                                 com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
                                                                                 null,
                                                                                 new Dimension(150, -1),
                                                                                 null,
                                                                                 0,
                                                                                 false));
 
         frame.add(pileView);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(800, 700);
         frame.setVisible(true);
 
         // Default controller
         activeController = exploreController;
         activeController.onLoad();
     }
 
     @Override public void onUnload() {
         //TODO implement ApplicationController.onUnload
     }
 
     @Override public void onExploreMode() {
         frame.setTitle("Explore");
         switchActiveController(exploreController);
     }
 
     @Override public void onQueueMode() {
         frame.setTitle("Inbox");
         switchActiveController(queueController);
     }
 
     @Override public void onArchiveMode() {
         frame.setTitle("Archives");
         switchActiveController(archiveController);
     }
 
     public void switchActiveController(Controller target) {
         // Unload the current Controller
         activeController.onUnload();
 
         // Load the controller
         activeController = target;
         activeController.onLoad();
     }
 
 
 }
