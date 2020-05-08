 /*
  * FIT VUT - 2013 - GJA project 1 - Photo viewer
  * 
  * Ondrej Fibich <xfibic01@stud.fit.vutbr.cz>
  */
 package cz.vutbr.fit.gja.project.gui;
 
 import cz.vutbr.fit.gja.project.model.ImageModel;
 import cz.vutbr.fit.gja.project.util.Icons;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Enumeration;
 import javax.imageio.ImageIO;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.plaf.FontUIResource;
 
 /**
  * Main window that contains a pane for viewing of images, tool bar, menu bar
  * and status bar.
  *
  * @author Ondrej Fibich
  */
 public final class MainJFrame extends javax.swing.JFrame {
 
     /** Info state type */
     public static final int S_INFO = 0;
     /** Warning state type */
     public static final int S_WARNING = 1;
     /** Error state type */
     public static final int S_ERROR = 2;
     /** Loading state type */
     public static final int S_LOADING = 3;
     
     /** Image */
     private ImageModel image = new ImageModel();
 
     /**
      * Creates new form MainJFrame
      */
     public MainJFrame() {
         this(null);
     }
     
     /**
      * Creates new form MainJFrame
      */
     public MainJFrame(File image) {
         setLookAndFeel();
         initComponents();
         centerFrame();
         openImage(image);
     }
     
     /**
      * Returns the image
      * 
      * @return image model
      */
     public ImageModel getImage() {
         return image;
     }
     
     /**
      * Opens an image in a new thread
      * 
      * @param img image to open
      */
     private void openImage(final File img) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 try {
                     image.open(img);
                     ((ImageJLabel) imageJLabel).display(image.getData());
                     jScrollPane.repaint();
                     displayStats();
                 } catch (IOException ex) {
                     status("Cannot read file: " + ex.getMessage(), S_ERROR);
                 } catch (IllegalArgumentException ex) {
                     status(ex.getMessage(), S_ERROR);
                 } catch (NullPointerException ex) {
                     status("Load an image please.");
                 } finally {
                     updateEnabled();
                 }
             }
         });
     }
     
     /**
      * Saves modified image in a new thread.
      */
     public void saveImage() {
         try {
             image.save();
             status("Image " + image.getFile().getName()
                     + " has been succesfully saved.");
         } catch (IOException ex) {
             status("Cannot save the file: " + ex.getMessage(), S_ERROR);
         }
     }
 
     /**
      * Asks for saving if image is modified.
      */
     public void askForSave() {
         // modofied?
         if (image.isModified()) {
             // confirm
             int r = JOptionPane.showConfirmDialog(
                     this, "Image " + image.getFile().getName() +
                     " was modified, would you like to save him?",
                     "Confirm", JOptionPane.YES_NO_OPTION
             );
             // ok lets save
             if (r == JOptionPane.YES_OPTION) {
                saveImage(); 
             }
         }
     }
     
     /**
      * Reload the current image in a new thread
      */
     private void reloadImage() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 try {
                     image.getData().getImage().flush(); // flush cache
                     ((ImageJLabel) imageJLabel).display(image.getData());
                     jScrollPane.repaint();
                     displayStats();
                 } catch (NullPointerException ex) {
                     status("Load an image please.");
                 } finally {
                     updateEnabled();
                 }
             }
         });
     }
     
     /**
      * Displays statistics of the opened file
      */
     private void displayStats() {
         if (image.isOpened()) {
             double zoom = ((ImageJLabel) imageJLabel).getCurrentScale();
             status("Image " + image.getFile().getName());
             imageInfoJLabel.setText(
                     image.getData().getIconWidth() +
                     "x" + image.getData().getIconHeight()
             );
             imageCountJLabel.setText(
                     (image.indexOfImage() + 1) + "/" +
                     image.countOfImages()
             );
             zoomJLabel.setText(((int) Math.round(zoom * 100)) + "%");
         } else {
             clearStatus();
             imageInfoJLabel.setText("");
             imageCountJLabel.setText("");
             zoomJLabel.setText("");
         }
     }
     
     /**
      * Sets sensitivity of buttons and menus by the current application state.
      */
     private void updateEnabled()
     {
         ImageJLabel ip = (ImageJLabel) imageJLabel;
         rotateLeftJMenuItem.setEnabled(image.isOpened());
         rotateRightJMenuItem.setEnabled(image.isOpened());
         imageSizeJMenuItem.setEnabled(image.isOpened());
         zoomInJButton.setEnabled(image.isOpened() && ip.hasZoomIn());
         zoomInJMenuItem.setEnabled(image.isOpened() && ip.hasZoomIn());
         zoomOutJButton.setEnabled(image.isOpened() && ip.hasZoomOut());
         zoomOutJMenuItem.setEnabled(image.isOpened() && ip.hasZoomOut());
         nextImageJMenuItem.setEnabled(image.hasNext());
         nextJButton.setEnabled(image.hasNext());
         prevImageJMenuItem.setEnabled(image.hasPrev());
         prevJButton.setEnabled(image.hasPrev());
         firstJMenuItem.setEnabled(image.isOpened());
         lastJMenuItem.setEnabled(image.isOpened());
         convertImageJMenuItem.setEnabled(image.isOpened());
         originalSizeJMenuItem.setEnabled(image.isOpened());
         saveJMenuItem.setEnabled(image.isOpened() && image.isModified());
         relaodJMenuItem.setEnabled(image.isOpened());
     }
     
     /**
      * Center frame
      */
     private void centerFrame() {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension size = getSize();
         int x, y;
 
         y = screenSize.height / 2 - size.height / 2;
         x = screenSize.width / 2 - size.width / 2;
         setLocation(x, y);
     }
 
     /**
      * Clear status message at the bottom of the application
      */
     public void clearStatus() {
         statusTextJLabel.setText("");
         statusTextJLabel.setIcon(null);
     }
 
     /**
      * Sets info message to bottom of the application to inform user
      * 
      * @param message Message to display
      */
     public void status(String message) {
         status(message, S_INFO);
     }
 
     /**
      * Sets status message to bottom of the application to inform user
      * 
      * @param message Message to display
      * @param type Type of message (one of <code>S_</code> constants)
      */
     public void status(String message, int type) {
         // set message
         statusTextJLabel.setText(message);
         // set icon and color
         switch (type) {
             case S_INFO:
                 statusTextJLabel.setForeground(Color.BLACK);
                 statusTextJLabel.setIcon(Icons.get("info", 12));
                 break;
             case S_WARNING:
                 statusTextJLabel.setForeground(Color.RED);
                 statusTextJLabel.setIcon(Icons.get("warning", 12));
                 break;
             case S_ERROR:
                 statusTextJLabel.setForeground(Color.RED);
                 statusTextJLabel.setIcon(Icons.get("error", 12));
                 break;
             case S_LOADING:
                 statusTextJLabel.setForeground(Color.BLACK);
                 statusTextJLabel.setIcon(Icons.get("loading", 12));
                 break;
         }
     }
 
     /**
      * Change look and font of application
      */
     private void setLookAndFeel() {
         // change font
        FontUIResource font = new FontUIResource(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
         Enumeration keys = UIManager.getDefaults().keys();
         while (keys.hasMoreElements()) {
             Object key = keys.nextElement();
             Object value = UIManager.get(key);
             if (value instanceof FontUIResource) {
                 UIManager.put(key, font);
             }
         }
         // change style
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception ignore) {
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         jToolBar = new javax.swing.JToolBar();
         prevJButton = new javax.swing.JButton();
         nextJButton = new javax.swing.JButton();
         jSeparator5 = new javax.swing.JToolBar.Separator();
         zoomInJButton = new javax.swing.JButton();
         zoomOutJButton = new javax.swing.JButton();
         jScrollPane = new javax.swing.JScrollPane();
         imageJLabel = new cz.vutbr.fit.gja.project.gui.ImageJLabel(this);
         footerJPanel = new javax.swing.JPanel();
         statusTextJLabel = new javax.swing.JLabel();
         imageInfoJLabel = new javax.swing.JLabel();
         zoomJLabel = new javax.swing.JLabel();
         imageCountJLabel = new javax.swing.JLabel();
         jMenuBar = new javax.swing.JMenuBar();
         fileJMenu = new javax.swing.JMenu();
         openJMenuItem = new javax.swing.JMenuItem();
         saveJMenuItem = new javax.swing.JMenuItem();
         relaodJMenuItem = new javax.swing.JMenuItem();
         jSeparator6 = new javax.swing.JPopupMenu.Separator();
         convertImageJMenuItem = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JPopupMenu.Separator();
         quitJMenuItem = new javax.swing.JMenuItem();
         editJMenu = new javax.swing.JMenu();
         rotateLeftJMenuItem = new javax.swing.JMenuItem();
         rotateRightJMenuItem = new javax.swing.JMenuItem();
         jSeparator3 = new javax.swing.JPopupMenu.Separator();
         imageSizeJMenuItem = new javax.swing.JMenuItem();
         viewJMenu = new javax.swing.JMenu();
         zoomInJMenuItem = new javax.swing.JMenuItem();
         zoomOutJMenuItem = new javax.swing.JMenuItem();
         jSeparator4 = new javax.swing.JPopupMenu.Separator();
         originalSizeJMenuItem = new javax.swing.JMenuItem();
         goJMenu = new javax.swing.JMenu();
         prevImageJMenuItem = new javax.swing.JMenuItem();
         nextImageJMenuItem = new javax.swing.JMenuItem();
         jSeparator2 = new javax.swing.JPopupMenu.Separator();
         firstJMenuItem = new javax.swing.JMenuItem();
         lastJMenuItem = new javax.swing.JMenuItem();
         aboutJMenu = new javax.swing.JMenu();
         aboutJMenuItem = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Proto viewer");
         setIconImage(Icons.get("icon").getImage());
 
         jToolBar.setRollover(true);
         jToolBar.setName("jToolBar"); // NOI18N
 
         prevJButton.setIcon(Icons.get("prev"));
         prevJButton.setText("Previous");
         prevJButton.setFocusable(false);
         prevJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         prevJButton.setName("prevJButton"); // NOI18N
         prevJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         prevJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 prevJButtonActionPerformed(evt);
             }
         });
         jToolBar.add(prevJButton);
 
         nextJButton.setIcon(Icons.get("next"));
         nextJButton.setText("Next");
         nextJButton.setFocusable(false);
         nextJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         nextJButton.setName("nextJButton"); // NOI18N
         nextJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         nextJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 nextJButtonActionPerformed(evt);
             }
         });
         jToolBar.add(nextJButton);
 
         jSeparator5.setName("jSeparator5"); // NOI18N
         jToolBar.add(jSeparator5);
 
         zoomInJButton.setIcon(Icons.get("zoom-in"));
         zoomInJButton.setText("Zoom in");
         zoomInJButton.setFocusable(false);
         zoomInJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         zoomInJButton.setName("zoomInJButton"); // NOI18N
         zoomInJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         zoomInJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 zoomInJMenuItemActionPerformed(evt);
             }
         });
         jToolBar.add(zoomInJButton);
 
         zoomOutJButton.setIcon(Icons.get("zoom-out"));
         zoomOutJButton.setText("Zoom out");
         zoomOutJButton.setFocusable(false);
         zoomOutJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         zoomOutJButton.setName("zoomOutJButton"); // NOI18N
         zoomOutJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         zoomOutJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 zoomOutJMenuItemActionPerformed(evt);
             }
         });
         jToolBar.add(zoomOutJButton);
 
         getContentPane().add(jToolBar, java.awt.BorderLayout.PAGE_START);
 
         jScrollPane.setBackground(new java.awt.Color(254, 254, 254));
         jScrollPane.setMinimumSize(new java.awt.Dimension(400, 300));
         jScrollPane.setName("jScrollPane"); // NOI18N
         jScrollPane.setPreferredSize(new java.awt.Dimension(400, 300));
 
         imageJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         imageJLabel.setName("imageJLabel"); // NOI18N
         jScrollPane.setViewportView(imageJLabel);
 
         getContentPane().add(jScrollPane, java.awt.BorderLayout.CENTER);
 
         footerJPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(189, 189, 189)));
         footerJPanel.setName("footerJPanel"); // NOI18N
         footerJPanel.setPreferredSize(new java.awt.Dimension(400, 25));
         footerJPanel.setLayout(new java.awt.GridBagLayout());
 
         statusTextJLabel.setName("statusTextJLabel"); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
         footerJPanel.add(statusTextJLabel, gridBagConstraints);
 
         imageInfoJLabel.setName("imageInfoJLabel"); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
         footerJPanel.add(imageInfoJLabel, gridBagConstraints);
 
         zoomJLabel.setName("zoomJLabel"); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
         footerJPanel.add(zoomJLabel, gridBagConstraints);
 
         imageCountJLabel.setName("imageCountJLabel"); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
         footerJPanel.add(imageCountJLabel, gridBagConstraints);
 
         getContentPane().add(footerJPanel, java.awt.BorderLayout.PAGE_END);
 
         jMenuBar.setName("jMenuBar"); // NOI18N
 
         fileJMenu.setMnemonic(KeyEvent.VK_F);
         fileJMenu.setText("File");
         fileJMenu.setName("fileJMenu"); // NOI18N
 
         openJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
         openJMenuItem.setIcon(Icons.get("open"));
         openJMenuItem.setMnemonic(KeyEvent.VK_O);
         openJMenuItem.setText("Open image...");
         openJMenuItem.setName("openJMenuItem"); // NOI18N
         openJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 openJMenuItemActionPerformed(evt);
             }
         });
         fileJMenu.add(openJMenuItem);
 
         saveJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
         saveJMenuItem.setIcon(Icons.get("save"));
         saveJMenuItem.setMnemonic(KeyEvent.VK_S);
         saveJMenuItem.setText("Save");
         saveJMenuItem.setName("saveJMenuItem"); // NOI18N
         saveJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 saveJMenuItemActionPerformed(evt);
             }
         });
         fileJMenu.add(saveJMenuItem);
 
         relaodJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, java.awt.event.InputEvent.CTRL_MASK));
         relaodJMenuItem.setIcon(Icons.get("refresh"));
         relaodJMenuItem.setMnemonic(KeyEvent.VK_D);
         relaodJMenuItem.setText("Reload");
         relaodJMenuItem.setName("relaodJMenuItem"); // NOI18N
         relaodJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 relaodJMenuItemActionPerformed(evt);
             }
         });
         fileJMenu.add(relaodJMenuItem);
 
         jSeparator6.setName("jSeparator6"); // NOI18N
         fileJMenu.add(jSeparator6);
 
         convertImageJMenuItem.setMnemonic(KeyEvent.VK_C);
         convertImageJMenuItem.setText("Convert image...");
         convertImageJMenuItem.setName("convertImageJMenuItem"); // NOI18N
         convertImageJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 convertImageJMenuItemActionPerformed(evt);
             }
         });
         fileJMenu.add(convertImageJMenuItem);
 
         jSeparator1.setName("jSeparator1"); // NOI18N
         fileJMenu.add(jSeparator1);
 
         quitJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
         quitJMenuItem.setIcon(Icons.get("quit"));
         quitJMenuItem.setMnemonic(KeyEvent.VK_Q);
         quitJMenuItem.setText("Quit");
         quitJMenuItem.setName("quitJMenuItem"); // NOI18N
         quitJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 quitJMenuItemActionPerformed(evt);
             }
         });
         fileJMenu.add(quitJMenuItem);
 
         jMenuBar.add(fileJMenu);
 
         editJMenu.setMnemonic(KeyEvent.VK_E);
         editJMenu.setText("Edit");
         editJMenu.setName("editJMenu"); // NOI18N
 
         rotateLeftJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
         rotateLeftJMenuItem.setIcon(Icons.get("rotate_left"));
         rotateLeftJMenuItem.setMnemonic(KeyEvent.VK_L);
         rotateLeftJMenuItem.setText("Rotate left");
         rotateLeftJMenuItem.setName("rotateLeftJMenuItem"); // NOI18N
         rotateLeftJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 rotateLeftJMenuItemActionPerformed(evt);
             }
         });
         editJMenu.add(rotateLeftJMenuItem);
 
         rotateRightJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
         rotateRightJMenuItem.setIcon(Icons.get("rotate_right"));
         rotateRightJMenuItem.setMnemonic(KeyEvent.VK_R);
         rotateRightJMenuItem.setText("Rotate right");
         rotateRightJMenuItem.setName("rotateRightJMenuItem"); // NOI18N
         rotateRightJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 rotateRightJMenuItemActionPerformed(evt);
             }
         });
         editJMenu.add(rotateRightJMenuItem);
 
         jSeparator3.setName("jSeparator3"); // NOI18N
         editJMenu.add(jSeparator3);
 
         imageSizeJMenuItem.setMnemonic(KeyEvent.VK_C);
         imageSizeJMenuItem.setText("Scale image...");
         imageSizeJMenuItem.setName("imageSizeJMenuItem"); // NOI18N
         imageSizeJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 imageSizeJMenuItemActionPerformed(evt);
             }
         });
         editJMenu.add(imageSizeJMenuItem);
 
         jMenuBar.add(editJMenu);
 
         viewJMenu.setMnemonic(KeyEvent.VK_V);
         viewJMenu.setText("View");
         viewJMenu.setName("viewJMenu"); // NOI18N
 
         zoomInJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PLUS, java.awt.event.InputEvent.CTRL_MASK));
         zoomInJMenuItem.setIcon(Icons.get("zoom-in"));
         zoomInJMenuItem.setMnemonic(KeyEvent.VK_I);
         zoomInJMenuItem.setText("Zoom in");
         zoomInJMenuItem.setName("zoomInJMenuItem"); // NOI18N
         zoomInJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 zoomInJMenuItemActionPerformed(evt);
             }
         });
         viewJMenu.add(zoomInJMenuItem);
 
         zoomOutJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, java.awt.event.InputEvent.CTRL_MASK));
         zoomOutJMenuItem.setIcon(Icons.get("zoom-out"));
         zoomOutJMenuItem.setMnemonic(KeyEvent.VK_U);
         zoomOutJMenuItem.setText("Zoom out");
         zoomOutJMenuItem.setName("zoomOutJMenuItem"); // NOI18N
         zoomOutJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 zoomOutJMenuItemActionPerformed(evt);
             }
         });
         viewJMenu.add(zoomOutJMenuItem);
 
         jSeparator4.setName("jSeparator4"); // NOI18N
         viewJMenu.add(jSeparator4);
 
         originalSizeJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
         originalSizeJMenuItem.setMnemonic(KeyEvent.VK_Z);
         originalSizeJMenuItem.setText("Original size");
         originalSizeJMenuItem.setName("originalSizeJMenuItem"); // NOI18N
         originalSizeJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 originalSizeJMenuItemActionPerformed(evt);
             }
         });
         viewJMenu.add(originalSizeJMenuItem);
 
         jMenuBar.add(viewJMenu);
 
         goJMenu.setMnemonic(KeyEvent.VK_G);
         goJMenu.setText("Go");
         goJMenu.setName("goJMenu"); // NOI18N
 
         prevImageJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0));
         prevImageJMenuItem.setIcon(Icons.get("prev"));
         prevImageJMenuItem.setMnemonic(KeyEvent.VK_P);
         prevImageJMenuItem.setText("Previous image");
         prevImageJMenuItem.setName("prevImageJMenuItem"); // NOI18N
         prevImageJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 prevJButtonActionPerformed(evt);
             }
         });
         goJMenu.add(prevImageJMenuItem);
 
         nextImageJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0));
         nextImageJMenuItem.setIcon(Icons.get("next"));
         nextImageJMenuItem.setMnemonic(KeyEvent.VK_N);
         nextImageJMenuItem.setText("Next image");
         nextImageJMenuItem.setName("nextImageJMenuItem"); // NOI18N
         nextImageJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 nextJButtonActionPerformed(evt);
             }
         });
         goJMenu.add(nextImageJMenuItem);
 
         jSeparator2.setName("jSeparator2"); // NOI18N
         goJMenu.add(jSeparator2);
 
         firstJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_HOME, java.awt.event.InputEvent.CTRL_MASK));
         firstJMenuItem.setMnemonic(KeyEvent.VK_F);
         firstJMenuItem.setText("First image");
         firstJMenuItem.setName("firstJMenuItem"); // NOI18N
         firstJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 firstJMenuItemActionPerformed(evt);
             }
         });
         goJMenu.add(firstJMenuItem);
 
         lastJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_END, java.awt.event.InputEvent.CTRL_MASK));
         lastJMenuItem.setMnemonic(KeyEvent.VK_L);
         lastJMenuItem.setText("Last image");
         lastJMenuItem.setName("lastJMenuItem"); // NOI18N
         lastJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 lastJMenuItemActionPerformed(evt);
             }
         });
         goJMenu.add(lastJMenuItem);
 
         jMenuBar.add(goJMenu);
 
         aboutJMenu.setMnemonic(KeyEvent.VK_H);
         aboutJMenu.setText("Help");
         aboutJMenu.setName("aboutJMenu"); // NOI18N
 
         aboutJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
         aboutJMenuItem.setIcon(Icons.get("about"));
         aboutJMenuItem.setMnemonic(KeyEvent.VK_A);
         aboutJMenuItem.setText("About");
         aboutJMenuItem.setName("aboutJMenuItem"); // NOI18N
         aboutJMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 aboutJMenuItemActionPerformed(evt);
             }
         });
         aboutJMenu.add(aboutJMenuItem);
 
         jMenuBar.add(aboutJMenu);
 
         setJMenuBar(jMenuBar);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Display about application dialog
      * 
      * @param evt 
      */
     private void aboutJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutJMenuItemActionPerformed
         String about = 
                 "<html>FIT VUTBR &lt;http://www.fit.vutbr.cz&gt;<br>" +
                 "GJA 2013 - Project 1 - Image viewer<br>" +
                 "Ondřej Fibich</b> &lt;xfibic01@stud.fit.vutbr.cz&gt;</html>";
         
         JOptionPane.showMessageDialog(
                 this, about, "About application",
                 JOptionPane.CLOSED_OPTION
         );
     }//GEN-LAST:event_aboutJMenuItemActionPerformed
 
     /**
      * Opens a dialog for opening a file
      * 
      * @param evt 
      */
     private void openJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openJMenuItemActionPerformed
         JFileChooser fc = new JFileChooser();
         fc.setDialogType(JFileChooser.OPEN_DIALOG);
         fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fc.setMultiSelectionEnabled(false);
         fc.setFileFilter(new FileNameExtensionFilter(
                 "Image files", "jpg", "jpeg", "gif", "png"
         ));
         
 		// selected?
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 			openImage(fc.getSelectedFile());
 		}
     }//GEN-LAST:event_openJMenuItemActionPerformed
 
     /**
      * Quits the application
      * 
      * @param evt 
      */
     private void quitJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitJMenuItemActionPerformed
         setVisible(false);
         dispose();
         System.exit(0);
     }//GEN-LAST:event_quitJMenuItemActionPerformed
 
     /**
      * Go to a next image.
      * 
      * @param evt 
      */
     private void prevJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevJButtonActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 askForSave();
                 try {
                     image.prev();
                 } catch (IOException ex) {
                     status("Cannot read file: " + ex.getMessage(), S_ERROR);
                 } finally {
                     reloadImage();
                 }
             }
         });
     }//GEN-LAST:event_prevJButtonActionPerformed
 
     /**
      * Go to previous image.
      * 
      * @param evt 
      */
     private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextJButtonActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 askForSave();
                 try {
                     image.next();
                 } catch (IOException ex) {
                     status("Cannot read file: " + ex.getMessage(), S_ERROR);
                 } finally {
                     reloadImage();
                 }
             }
         });
     }//GEN-LAST:event_nextJButtonActionPerformed
 
     /**
      * Go to the first image
      * 
      * @param evt 
      */
     private void firstJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstJMenuItemActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 askForSave();
                 try {
                     image.first();
                 } catch (IOException ex) {
                     status("Cannot read file: " + ex.getMessage(), S_ERROR);
                 } finally {
                     reloadImage();
                 }
             }
         });
     }//GEN-LAST:event_firstJMenuItemActionPerformed
 
     /**
      * Go to the last image
      * 
      * @param evt 
      */
     private void lastJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastJMenuItemActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 askForSave();
                 try {
                     image.last();
                 } catch (IOException ex) {
                     status("Cannot read file: " + ex.getMessage(), S_ERROR);
                 } finally {
                     reloadImage();
                 }
             }
         });
     }//GEN-LAST:event_lastJMenuItemActionPerformed
 
     /**
      * Handles conversion of the image.
      * 
      * @param evt 
      */
     private void convertImageJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertImageJMenuItemActionPerformed
         // select format
         Object[] options = image.getPossibleExtensionForConvert();
         final String format = (String) JOptionPane.showInputDialog(
                 this, "Choose the target format", "Convert image",
                 JOptionPane.PLAIN_MESSAGE, null, options, null
         );
         // selected?
         if (format == null) return;
         // do the convertation
         status("Converting image to " + format + " format.", S_LOADING);
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 // select new name
                 final String path = image.getFile().getParent();
                 int end = image.getFile().getName().lastIndexOf('.');
                 File f;
                 String filePath;
                 String fname = image.getFile().getName().substring(0, end);
                 int count = 0;
                 do {
                     filePath = path + File.separator + fname;
                     filePath += (count > 0) ? String.valueOf(count) : "";
                     filePath += '.' + format;
                     f = new File(filePath);
                     count++;
                 } while (f.exists());
                 // convert&save
                 try {
                     image.saveAs(f, format);
                     displayStats();
                     status("Image converted and stored to: " + f.getName());
                 } catch (IOException ex) {
                     f.delete();
                     status("Cannot convert image: " + ex.getMessage(), S_ERROR);
                 }
             }
         });
     }//GEN-LAST:event_convertImageJMenuItemActionPerformed
 
     /**
      * Rotates an image to left with 90° angle
      * 
      * @param evt 
      */
     private void rotateLeftJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateLeftJMenuItemActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 image.rotate(Math.toRadians(-90));
                 reloadImage();
             }
         });
     }//GEN-LAST:event_rotateLeftJMenuItemActionPerformed
 
     /**
      * Rotates an image to right with 90° angle
      * 
      * @param evt 
      */
     private void rotateRightJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotateRightJMenuItemActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 image.rotate(Math.toRadians(90));
                 reloadImage();
             }
         });
     }//GEN-LAST:event_rotateRightJMenuItemActionPerformed
 
     /**
      * Scales an image
      * 
      * @param evt 
      */
     private void imageSizeJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageSizeJMenuItemActionPerformed
         // get scale
         ScaleJDialog d = new ScaleJDialog(this);
         d.setVisible(true);
         
         if (d.getReturnStatus() == ScaleJDialog.RET_OK) {
             final Dimension scale = d.getScale();
             status("Scaling image to: " + scale.getWidth()
                     + 'x' + scale.getHeight(), S_LOADING);
             // do the scaling
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     image.scale(scale.width, scale.height);
                     reloadImage();
                 }
             });
         }
     }//GEN-LAST:event_imageSizeJMenuItemActionPerformed
 
     /**
      * Zoom image in
      * 
      * @param evt 
      */
     private void zoomInJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInJMenuItemActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 ((ImageJLabel) imageJLabel).zoomIn();
                 jScrollPane.repaint();
                 displayStats();
                 updateEnabled();
             }
         });
     }//GEN-LAST:event_zoomInJMenuItemActionPerformed
 
     /**
      * Zoom image out
      * 
      * @param evt 
      */
     private void zoomOutJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutJMenuItemActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 ((ImageJLabel) imageJLabel).zoomOut();
                 jScrollPane.repaint();
                 displayStats();
                 updateEnabled();
             }
         });
     }//GEN-LAST:event_zoomOutJMenuItemActionPerformed
 
     /**
      * Zoom zoom original size
      * 
      * @param evt 
      */
     private void originalSizeJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_originalSizeJMenuItemActionPerformed
         reloadImage();
     }//GEN-LAST:event_originalSizeJMenuItemActionPerformed
 
     /**
      * Saves modified image
      * 
      * @param evt 
      */
     private void saveJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveJMenuItemActionPerformed
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 saveImage();
             }
         });
     }//GEN-LAST:event_saveJMenuItemActionPerformed
 
     /**
      * Reloads image
      * 
      * @param evt 
      */
     private void relaodJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relaodJMenuItemActionPerformed
         reloadImage();
     }//GEN-LAST:event_relaodJMenuItemActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenu aboutJMenu;
     private javax.swing.JMenuItem aboutJMenuItem;
     private javax.swing.JMenuItem convertImageJMenuItem;
     private javax.swing.JMenu editJMenu;
     private javax.swing.JMenu fileJMenu;
     private javax.swing.JMenuItem firstJMenuItem;
     private javax.swing.JPanel footerJPanel;
     private javax.swing.JMenu goJMenu;
     private javax.swing.JLabel imageCountJLabel;
     private javax.swing.JLabel imageInfoJLabel;
     private javax.swing.JLabel imageJLabel;
     private javax.swing.JMenuItem imageSizeJMenuItem;
     private javax.swing.JMenuBar jMenuBar;
     private javax.swing.JScrollPane jScrollPane;
     private javax.swing.JPopupMenu.Separator jSeparator1;
     private javax.swing.JPopupMenu.Separator jSeparator2;
     private javax.swing.JPopupMenu.Separator jSeparator3;
     private javax.swing.JPopupMenu.Separator jSeparator4;
     private javax.swing.JToolBar.Separator jSeparator5;
     private javax.swing.JPopupMenu.Separator jSeparator6;
     private javax.swing.JToolBar jToolBar;
     private javax.swing.JMenuItem lastJMenuItem;
     private javax.swing.JMenuItem nextImageJMenuItem;
     private javax.swing.JButton nextJButton;
     private javax.swing.JMenuItem openJMenuItem;
     private javax.swing.JMenuItem originalSizeJMenuItem;
     private javax.swing.JMenuItem prevImageJMenuItem;
     private javax.swing.JButton prevJButton;
     private javax.swing.JMenuItem quitJMenuItem;
     private javax.swing.JMenuItem relaodJMenuItem;
     private javax.swing.JMenuItem rotateLeftJMenuItem;
     private javax.swing.JMenuItem rotateRightJMenuItem;
     private javax.swing.JMenuItem saveJMenuItem;
     private javax.swing.JLabel statusTextJLabel;
     private javax.swing.JMenu viewJMenu;
     private javax.swing.JButton zoomInJButton;
     private javax.swing.JMenuItem zoomInJMenuItem;
     private javax.swing.JLabel zoomJLabel;
     private javax.swing.JButton zoomOutJButton;
     private javax.swing.JMenuItem zoomOutJMenuItem;
     // End of variables declaration//GEN-END:variables
 }
