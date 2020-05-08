 package at.ac.tuwien.sepm.ui;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo
  * Date: 27.06.13
  * Time: 00:57
  * To change this template use File | Settings | File Templates.
  */
 @UI
 public class BiggerInfoPanel extends StandardInsidePanel {
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
     private JTextArea infoText;
 
     public BiggerInfoPanel() {
         this.setLayout(null);
         this.setBounds(334,42,530,180);
         loadFonts();
         loadText();
         try {
             image = ImageIO.read(ClassLoader.getSystemResource("img/biggerInfo.jpg"));
         } catch (IOException e) {
             log.error("Error: " + e.getMessage());
         }
         repaint();
         revalidate();
     }
 
     private void loadText() {
         infoText = new JTextArea();
         infoText.setOpaque(false);
         infoText.setLineWrap(true);
         infoText.setWrapStyleWord(true);
         infoText.setEditable(false);
         infoText.setFont(standardTextFont);
         infoText.setBounds(0,0, 531, 181);
         infoText.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
 
         JScrollPane scroll = new JScrollPane(infoText);
         scroll.setOpaque(false);
         scroll.getViewport().setOpaque(false);
         scroll.setBounds(0,0, 531, 181);
 
         this.add(scroll);
     }
 
     public void setInfoText(String s) {
        s.replaceAll("(\r\n|\n)", "(\r\n|\n) *");
        infoText.setText(s);
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         if (image != null) {
             g.drawImage(image, 0,0, null);
         }
     }
 }
