 package client.views.swing.menu;
 
 import client.views.swing.common.ImageIconTools;
 import client.views.swing.gameboard.Game;
 import client.views.swing.gameboard.Rack;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 /**
  *
  * @author Bernard <bernard.debecker@gmail.com>
  */
 public class BlankDialog extends JDialog {
 
    private static final String PATH_TILE = "../media/";
    private static final String PATH_LETTER = "../media/letters/";
    private static final String PATH_NUMBER = "../media/numbers/";
     private static final String[] LETTERS = {"a", "b", "c", "d", "e", "f", "g", "h", "i",
         "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
     private ArrayList<String> lettersList;
     private JButton leftArrowButton, rightArrowButton, tileButton;
     private JPanel leftArrowPanel, rightArrowPanel, tilePanel;
     private int index = 0;
     private String choice;
 
     public BlankDialog(JFrame frame) {
         super(frame, "Blank", true);
         this.setSize(250, 100);
         this.setLocationRelativeTo(null);
         this.setResizable(false);
         this.setBackground(Color.WHITE);
         this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         this.setLayout(new BorderLayout());
         initComponents();
         this.add(rightArrowPanel, BorderLayout.EAST);
         this.add(tilePanel, BorderLayout.CENTER);
         this.add(leftArrowPanel, BorderLayout.WEST);
     }
 
     private void initComponents() {
         lettersList = new ArrayList<>();
         lettersList.addAll(Arrays.asList(LETTERS));
         initPanels();
         initTileButton();
         initArrows();
         addButtonsToPanels();
     }
 
     private void initPanels() {
         leftArrowPanel = new JPanel();
         rightArrowPanel = new JPanel();
         tilePanel = new JPanel();
     }
 
     private void initTileButton() {
         tileButton = new JButton();
         tileButton.setIcon(new ImageIcon(makeTileIcon(lettersList.get(index))));
         tileButton.setSize(60, 60);
         tileButton.setBackground(Color.WHITE);
         tileButton.setOpaque(false);
         tileButton.setBorder(null);
         tileButton.addActionListener(new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Game.tileBlank = lettersList.get(index);
                 dispose();
             }
         });
 
     }
 
     private void initArrows() {
         leftArrowButton = new JButton(ImageIconTools.createImageIcon(PATH_TILE + "left_arrow_icon.png", null));
         leftArrowButton.setSize(60, 60);
         leftArrowButton.setBackground(Color.WHITE);
         leftArrowButton.setOpaque(false);
         leftArrowButton.setBorder(null);
         leftArrowButton.addActionListener(new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (index > 0) {
                     index--;
                 } else {
                     index = 25;
                 }
                 tileButton.setIcon(new ImageIcon(makeTileIcon(lettersList.get(index))));
             }
         });
         rightArrowButton = new JButton(ImageIconTools.createImageIcon(PATH_TILE + "right_arrow_icon.png", null));
         rightArrowButton.setSize(60, 60);
         rightArrowButton.setBackground(Color.WHITE);
         rightArrowButton.setOpaque(false);
         rightArrowButton.setBorder(null);
         rightArrowButton.addActionListener(new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (index < 25) {
                     index++;
                 } else {
                     index = 0;
                 }
                 tileButton.setIcon(new ImageIcon(makeTileIcon(lettersList.get(index))));
             }
         });
     }
 
     private void addButtonsToPanels() {
         leftArrowPanel.add(leftArrowButton);
         tilePanel.add(tileButton);
         rightArrowPanel.add(rightArrowButton);
     }
 
     private Image makeTileIcon(String letter) {
         BufferedImage tile = null;
         BufferedImage letterB = null;
         try {
             tile = ImageIO.read(Rack.class.getResource(PATH_TILE + "vintage_tile.png"));
             if (!letter.equals("?")) {
                 letterB = ImageIO.read(Rack.class.getResource(PATH_LETTER + letter.toLowerCase() + ".png"));
             }
         } catch (IOException ex) {
             Logger.getLogger(ImageIconTools.class.getName()).log(Level.SEVERE, null, ex);
         }
         BufferedImage finalTile = new BufferedImage(437, 481, BufferedImage.TYPE_INT_ARGB);
         Graphics g = finalTile.getGraphics();
         g.drawImage(tile, 0, 0, null);
         g.drawImage(letterB, 0, 0, null);
         Image result = finalTile.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
         return result;
     }
 
     public String showBlank() {
         this.setVisible(true);
         return lettersList.get(index);
     }
 }
