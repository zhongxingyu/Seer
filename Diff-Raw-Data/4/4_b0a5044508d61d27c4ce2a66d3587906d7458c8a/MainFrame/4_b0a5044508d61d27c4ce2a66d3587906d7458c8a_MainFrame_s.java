 /**
 *    Copyright (c) 2013 Abhishek Banerjee.
 *    This file is part of Elite Comix Reader.
 *    
 *    Elite Comix Reader is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *    
 *    Elite Comix Reader is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *    
 *    You should have received a copy of the GNU General Public License
 *    along with Elite Comix Reader.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
 
 package com;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.nio.file.Files;
 import static java.nio.file.StandardCopyOption.*;
 import javax.swing.*;
 import net.iharder.dnd.FileDrop;
 /**
  *
  * @author Abhishek Banerjee
  */
 
 public class MainFrame extends JFrame {
     
     private static ImagePanel panel;
     private boolean fullscreen = false, alwaysOnTop = false;
     private static ToolBar t;
     private boolean toolBarStatus;
     private PopupMenu popupMenu;
     
     //Constructor
     public MainFrame (Dimension d, ArchiveManager ext) {
         
         initComponents(d, ext);
     }
     
     public MainFrame(ArchiveManager ext) {
         
         this(new Dimension(400, 400), ext);
     }
     
     public MainFrame(BufferedImage image, ArchiveManager ext) {
         
         this(new Dimension(400, 400), ext);     
     }
     
     
     private void initComponents(Dimension d, final ArchiveManager ext)
     {
         
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLocation(200, 200);
         setVisible(true);
         setMinimumSize(new Dimension(200, 200));
         setSize(d);
         panel = new ImagePanel();
         addKeyListener(new KeyAdapter() {
                 @Override
                     public void keyPressed(KeyEvent e) {
                     keyPressedAction(e,ext);
                 }
                 });
         t = new ToolBar(panel, this, ext);
         
         getContentPane().add(t, BorderLayout.NORTH);
         getContentPane().add(panel);
         
         try {
 
                 UIManager.setLookAndFeel(
                 "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                 SwingUtilities.updateComponentTreeUI(this);
             } catch (ClassNotFoundException | InstantiationException 
                     | IllegalAccessException | UnsupportedLookAndFeelException e) {
                 JOptionPane.showMessageDialog(this, e.getMessage());
             }
         
         
         popupMenu = new PopupMenu(panel, this, ext);
         FileDrop fileDrop = new  FileDrop( panel, new FileDrop.Listener()                                      
         {                                          
             @Override 
             public void  filesDropped( java.io.File[] files )                                     
             {                                         
                 File f = files[0];
                 doWork(ext, f);                    
             }
         });
         fileDrop = null;
         addMouseListener( new MouseAdapter(){
             @Override
             public void mouseClicked(MouseEvent e) {
                 if(e.getClickCount() == 2)
                 {
                     fullscreen();
                 }
                 if(e.getButton() == MouseEvent.BUTTON3) {
                     popupMenu.showPopup(e);
                 }
             }
             });
         scale(1.0, panel);
          
     }
     
     private void keyPressedAction(KeyEvent e, ArchiveManager ext) {
         
         if(e.getKeyCode() == KeyEvent.VK_O)
                     {
                         open(ext);   
                     }
                     else if(!panel.isImageEmpty(panel.getIndex())) {
                     if(e.getKeyCode() == KeyEvent.VK_DOWN){
                         moveDown();
                     }
                     else if(e.getKeyCode() == KeyEvent.VK_UP){
                         if(panel.getImageHeight() > panel.getFrameHeight())
                         {   
                             if( Math.abs(panel.getYPos() + 5) < 
                                     panel.getImageHeight() - panel.getFrameHeight() 
                                 && panel.getYPos() + 5 <= 0)
                             {
                                 panel.setY(panel.getYPos() + 5);
                                 panel.repaint();
                             }
                         }
                     }
                     else if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                     {
                         fullscreen();
                     }
                     else if(e.getKeyCode() == KeyEvent.VK_W)
                     {
                         ToolBar.setFitToggle();
                         panel.toggleMode(ToolBar.isFitWidthSelected());
                         fitImage(ToolBar.isFitWidthSelected());          
                     }
                     else if(e.getKeyCode() == KeyEvent.VK_H)
                     {
                         toolBarStatus = !toolBarStatus;
                         t.setVisible(toolBarStatus);                               
                     }
                     else if(e.getKeyCode() == KeyEvent.VK_T)
                     {
                         alwaysOnTop();
                         ToolBar.setAlwaysOnTopToggle();
                     }
                     else if(e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET)
                     {
                       panel.zoomOut();
                     }
                     else if(e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET)
                     {
                       panel.zoomIn();
                     }
                     else if(e.getKeyCode() == KeyEvent.VK_0)
                     {
                         panel.setTransform(90);
                         panel.repaint();
                     }
                     else if(e.getKeyCode() == KeyEvent.VK_RIGHT)
                     {
                         
                         panel.nextPage(ext);
                     }
                     else if(e.getKeyCode() == KeyEvent.VK_LEFT)
                     {
                         panel.prevPage(ext);
                         
                     }
                     else if(e.getKeyCode() == KeyEvent.VK_S)
                     {
                         save();   
                     }
                     }
                     
     }
     
     static void open(ArchiveManager e) {
         
         JFileChooser chooser = new JFileChooser();
         chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
         //chooser.setCurrentDirectory(new File("."));
         chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
             
             @Override
             public boolean accept(File f) {
              
                 return     f.isDirectory() 
                         || f.getName().toLowerCase().endsWith(".cbr")
                         || f.getName().toLowerCase().endsWith(".cbz")
                         || f.getName().toLowerCase().endsWith(".rar")
                         || f.getName().toLowerCase().endsWith(".zip");
              }
              
             @Override
             public String getDescription() { return "Comic Book files"; }});
         
         chooser.showOpenDialog(panel);
         
         File f = chooser.getSelectedFile();
         doWork(e, f);
         chooser = null;
     }
     
     void save() {
         if(ArchiveManager.getSize() != 0) {
            JFileChooser ch = new JFileChooser("file:\\E:\\misc\\");
             
             ch.setSelectedFile(ArchiveManager.getFile(panel.getIndex()));
             ch.setCurrentDirectory(null);
             int approve = ch.showSaveDialog(panel);
             if(approve == JFileChooser.APPROVE_OPTION) {
                 File f = ch.getSelectedFile();
                 try {
                     Files.copy(ArchiveManager.getFile(panel.getIndex()).toPath(),
                             f.toPath(), COPY_ATTRIBUTES);
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(this, ex.getMessage());
                 }
 
             }
         }
     }
     
     private void moveDown(){
         
         if(panel.getOrientation() == 0) 
         {
             if(panel.getImageHeight() > panel.getFrameHeight())        
             {
                 if(Math.abs(panel.getYPos() - 3) < panel.getImageHeight() 
                         - panel.getFrameHeight() && panel.getYPos() - 3 <= 0)
                 {
                     panel.setY(panel.getYPos() - 3);
                     panel.repaint();
                 }
             }
         }
         else if(panel.getOrientation() == 1)
         {
             
             if(panel.getImageHeight() > panel.getFrameHeight())        
             {
                 
                 if(Math.abs(panel.getXPos() - 3) < panel.getImageHeight() 
                         - panel.getFrameHeight() 
                             && panel.getXPos() - 3 <= 0)
                 {
                    //System.out.print("jhgfjh");
                     panel.setX(panel.getXPos() - 3);
                     panel.repaint();
                 }
             }
         }
     }
     
     static void scale(Double scale, ImagePanel imagePanel) {
         imagePanel.setScale(scale);
     }
     
     void fitImage(boolean b) {
         
         panel.toggleMode(b);
         if(ArchiveManager.getSize() != 0)
         {   
             panel.fit(panel.getMode());
             panel.repaint();
         }
         
     }
     
     void fullscreen() {
         
         if(isFullscreen()) {
             t.setVisible(isFullscreen());
             fullscreen = false;
             dispose();
             setUndecorated(false);
             GraphicsEnvironment ge = 
                     GraphicsEnvironment.getLocalGraphicsEnvironment();
             GraphicsDevice gs = ge.getDefaultScreenDevice();
             gs.setFullScreenWindow(null);
             //setSize(400, 400);
             validate();
             setVisible(true);
         }
         else {
             t.setVisible(isFullscreen());
             fullscreen = true;
             dispose();
             setUndecorated(true);
             GraphicsEnvironment ge = 
                     GraphicsEnvironment.getLocalGraphicsEnvironment();
             GraphicsDevice gs = ge.getDefaultScreenDevice();
             gs.setFullScreenWindow(this);
             validate();
             //f.setVisible(true);
         }
     }
     
     private boolean isFullscreen()
     {
         return fullscreen;
     }
     
     private boolean isOnTop()
     {
         return alwaysOnTop;
     }
     
     void alwaysOnTop() {
         
         setAlwaysOnTop(!isOnTop());
         alwaysOnTop = !isOnTop();
         
     }
     
     public static void main(String args[]) {
         ArchiveManager e = new ArchiveManager();
         MainFrame mainFrame = new MainFrame(e);
         mainFrame = null;
     }
     
     static void doWork(ArchiveManager ext, File f) {
         try {
                                        
                                     BufferedImage a = null;
                                         try {
                                             int success = ext.extract(f);
                                             if(success == 0) {
                                                 panel.setIndex(0);
 
                                                 if(panel.getIndex() <= ArchiveManager.getSize() 
                                                         && ArchiveManager.getSize() != 0) {
                                                     a = ext.getImage(panel.getIndex());
                                                     //System.out.println("sjdkb");
                                                 }
                                                 else if(ArchiveManager.getSize() == 0) {
                                                     panel.setEmptyImage();
                                                     JOptionPane.showMessageDialog(panel, "No images in the File!!");
                                                 }
                                             }
                                             else {
                                                 JOptionPane.showMessageDialog(panel, ArchiveManager.getError());
                                             }
                                         } catch (IOException ex) {
                                             //
                                         }
                                     if(a != null)
                                     {
                                         panel.loadImage(a);
                                         panel.repaint();
                                     }
                                     }
                                     catch(NullPointerException r) {
                                     }
     }
 }
