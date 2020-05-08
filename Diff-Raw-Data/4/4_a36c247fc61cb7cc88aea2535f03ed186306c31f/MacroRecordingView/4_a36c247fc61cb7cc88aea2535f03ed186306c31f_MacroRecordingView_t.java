 package kkckkc.jsourcepad.ui.statusbar;
 
 import kkckkc.jsourcepad.model.MacroManager;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.util.messagebus.DispatchStrategy;
 
 import javax.swing.*;
 import java.awt.*;
 
 public class MacroRecordingView extends JPanel implements MacroManager.Listener {
     private static final int SIZE = 10;
     private boolean recording;
 
     public MacroRecordingView(Window window) {
         window.topic(MacroManager.Listener.class).subscribe(DispatchStrategy.ASYNC_EVENT, this);
        setOpaque(false);
     }
 
     public Dimension getPreferredSize() {
         return new Dimension(SIZE, SIZE);
     }
 
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
         if (recording) {
             g2.setColor(Color.red);
         } else {
             g2.setColor(null);
         }
 
        g2.fillOval(1, 1, SIZE - 2, SIZE - 2);
     }
 
     @Override
     public void startRecording() {
         recording = true;
         repaint();
     }
 
     @Override
     public void stopRecording() {
         recording = false;
         repaint();
     }
 }
