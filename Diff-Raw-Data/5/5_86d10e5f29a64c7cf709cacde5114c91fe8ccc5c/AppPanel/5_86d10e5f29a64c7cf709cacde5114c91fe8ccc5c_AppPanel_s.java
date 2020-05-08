 package com.id.ui.app;
 
 import java.awt.Component;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JLayeredPane;
 
 import com.id.app.Controller;
 import com.id.events.KeyStroke;
 import com.id.events.KeyStrokeHandler;
 import com.id.fuzzy.FuzzyFinder;
 import com.id.ui.editor.TextPanel;
 
 @SuppressWarnings("serial")
 public class AppPanel extends JLayeredPane implements KeyListener, FuzzyFinder.Listener, Controller.Listener {
   private final SpotlightView spotlightView;
   private final FileListView fileListView;
   private final Component stackView;
   private final KeyStrokeHandler handler;
   private final FuzzyFinderPanel fuzzyFinderPanel;
   private final AppLayout appLayout = new AppLayout();
 
   public AppPanel(FileListView fileListView, SpotlightView spotlightView,
       Component stackView, KeyStrokeHandler handler, FuzzyFinderPanel fuzzyFinderPanel,
       TextPanel minibufferView) {
     this.spotlightView = spotlightView;
     this.fileListView = fileListView;
     this.stackView = stackView;
     this.handler = handler;
     this.fuzzyFinderPanel = fuzzyFinderPanel;
     setLayout(appLayout);
     setFocusTraversalKeysEnabled(false);
     add(fileListView, "filelist");
     add(spotlightView, "spotlight");
     add(stackView, "stack");
     add(minibufferView, "minibuffer");
     fuzzyFinderPanel.setListener(this);
   }
 
   @Override
   public void keyTyped(KeyEvent e) {
     // Do nothing.
   }
 
   @Override
   public void keyPressed(KeyEvent e) {
     // The editor doesn't care about shift being pressed.
     if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
       return;
     }
     KeyStroke keyStroke = KeyStroke.fromKeyEvent(e);
     logEventTranslationInfo(e, keyStroke);
     // NOTE KeyEvent.getKeyCode() is only defined in keyPressed when control
     // is down.
     if (keyStroke.getKeyChar() == 'q' && keyStroke.isControlDown()) {
       System.exit(0);
     }
 
     this.handler.handleKeyStroke(keyStroke);
     this.spotlightView.repaint();
     this.fileListView.repaint();
     this.stackView.repaint();
     this.fuzzyFinderPanel.repaint();
   }
 
   private void logEventTranslationInfo(KeyEvent event, KeyStroke keyStroke) {
 //    System.out.println("event: " + event);
 //    System.out.println("keystroke: " + keyStroke);
   }
 
   @Override
   public void keyReleased(KeyEvent e) {
     // Do nothing.
   }
 
   @Override
   public void onQueryChanged() {
     repaint();
   }
 
   @Override
   public void onSetVisible(boolean visible) {
     if (visible) {
       add(fuzzyFinderPanel, "fuzzyfinder");
       setLayer(fuzzyFinderPanel, 100);
     } else {
       remove(fuzzyFinderPanel);
     }
     repaint();
   }
 
   @Override
   public void onStackVisibilityChanged(boolean isStackVisible) {
     appLayout.setStackVisible(isStackVisible);
     invalidate();
   }
 }
