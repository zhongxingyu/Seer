 package project.senior.app.pormmo;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.ScrollPane;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 
 /**
  * @author John Fisher
  */
 public class UserInterfaceFrame extends JFrame
 {
 
   private JPanel controlPanel;
   private OutputPanel outputPanel;
   private File selectedInputFile;
   private GridBagLayout controlLayout;
   private VlcInterface vlcIFace;
   private GridBagConstraints controlGBC;
   private ScrollPane sPane;
   private JFrame me;
   private BufferedImage snapshot;
   private String fileDirectory;
 
   public UserInterfaceFrame()
   {
     initFrame();
     initLayouts();
     initMenu();
     initControlPanel();
     initOutputPanel();
     initDisplay();
     showFrame();
   }
 
   private void initFrame()
   {
     setPreferredSize(new Dimension(500, 500));
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     setLayout(new BorderLayout());
     this.addComponentListener(new FrameListener());
     me = this;
   }
 
   private void showFrame()
   {
     pack();
     setVisible(true);
   }
 
   private void initLayouts()
   {
     controlLayout = new GridBagLayout();
     controlGBC = new GridBagConstraints();
     controlGBC.fill = GridBagConstraints.HORIZONTAL;
 
   }
 
   private void initDisplay()
   {
     sPane = new ScrollPane();
     sPane.add(outputPanel);
     sPane.setPreferredSize(this.getPreferredSize());
 
     add(controlPanel, BorderLayout.NORTH);
     add(sPane, BorderLayout.CENTER);
     fileDirectory = "";
   }
 
   private void initMenu()
   {
     JMenuBar frameMenuBar = new JMenuBar();
     JMenu fileMenu = new JMenu("File");
     JMenuItem openMenuItem = new JMenuItem("Open");
     openMenuItem.addMouseListener(new UserInterfaceFrameMenuListener());
     fileMenu.add(openMenuItem);
     JMenuItem exitMenuItem = new JMenuItem("Exit");
     exitMenuItem.addMouseListener(new UserInterfaceFrameMenuListener());
     fileMenu.add(exitMenuItem);
 
     frameMenuBar.add(fileMenu);
 
     setJMenuBar(frameMenuBar);
   }
 
   private void initOutputPanel()
   {
     outputPanel = new OutputPanel();
     outputPanel.setPreferredSize(new Dimension(500, 500));
   }
 
   private void initControlPanel()
   {
 
     controlPanel = new JPanel();
     controlPanel.setLayout(controlLayout);
 
     JButton playButton = new JButton("Play");
     playButton.addMouseListener(new MediaControlsListener());
     controlGBC.gridx = 0;
     controlGBC.gridy = 0;
     controlPanel.add(playButton, controlGBC);
 
     JButton stopButton = new JButton("Stop");
     stopButton.addMouseListener(new MediaControlsListener());
     controlGBC.gridx = 1;
     controlGBC.gridy = 0;
     controlPanel.add(stopButton, controlGBC);
 
     JButton fwdButton = new JButton("Forward");
     fwdButton.addMouseListener(new MediaControlsListener());
     controlGBC.gridx = 2;
     controlGBC.gridy = 0;
     controlPanel.add(fwdButton, controlGBC);
 
     JButton rwdButton = new JButton("Rewind");
     rwdButton.addMouseListener(new MediaControlsListener());
     controlGBC.gridx = 3;
     controlGBC.gridy = 0;
     controlPanel.add(rwdButton, controlGBC);
 
     JButton pauseButton = new JButton("Pause");
     pauseButton.addMouseListener(new MediaControlsListener());
     controlGBC.gridx = 4;
     controlGBC.gridy = 0;
     controlPanel.add(pauseButton, controlGBC);
 
     JButton snapshotButton = new JButton("Snapshot");
     snapshotButton.addMouseListener(new MediaControlsListener());
     controlGBC.gridx = 5;
     controlGBC.gridy = 0;
     controlPanel.add(snapshotButton, controlGBC);
 
     JSlider posSlider = new JSlider();
     controlGBC.gridx = 0;
     controlGBC.gridy = 1;
     controlGBC.gridwidth = 6;
     controlPanel.add(posSlider, controlGBC);
   }
 
   /**
    * Check and do settings. 
    * 0 = Check for the Settings Directory.
    * 
    * 1 = Check for FileSelect directories and last location.
    * ##~If there is no last one, it will create the file.
    * 
    * 2 = Save latest location for FileSelect
    * 
    * @param setting The setting you wish to check/do. 
    */
   private void doSettings(int setting) 
   {
     switch(setting)
     {
       case 0:
        File SettingsDirectory = new File("/Settings/");
         if (!SettingsDirectory.exists())
           SettingsDirectory.mkdir();
         break;
       //------------------------------------------------------------------------
       case 1:
        File SavedDirectory = new File("/Settings/SD.pmo");
         if(SavedDirectory.exists())
         {
           try
           {
             Scanner scan = new Scanner(SavedDirectory);
             fileDirectory = scan.nextLine();
             scan.close();
           }
           catch(Exception e)
           {
             //#DEBUG
             System.out.println("Insufficient Access to file! setting = 1");
           }
         }//::End if(SavedDirectory)
         break;
       //------------------------------------------------------------------------
       case 2:
         try
         {
           BufferedWriter bw =
                          new BufferedWriter(new FileWriter("/Settings/SD.pmo"));
           bw.write(fileDirectory);
           bw.close();
         }
         catch (Exception e)
         {
           //#DEBUG
           System.out.println("Can't write to file! setting = 2");
         }
         break;
     }   
   }
   
   private void showFileSelect()
   {
     doSettings(0);
     doSettings(1);
     
     JFileChooser jFC = new JFileChooser();
     jFC.setSelectedFile(new File(fileDirectory));
     jFC.setAcceptAllFileFilterUsed(false);
     jFC.setFileFilter(new ExtFileFilter(new String[]
             {
               ".png", ".jpg"
             }));
     jFC.setFileFilter(new ExtFileFilter(new String[]
             {
               ".mp4", ".mpg", ".avi", ".wmv"
             }));
 
     jFC.showDialog(this, "Open");
     selectedInputFile = jFC.getSelectedFile();   
     fileDirectory = jFC.getSelectedFile().getPath();
     doSettings(2);
     OpenSource();
   }
 
   private void OpenSource()
   {
     if (selectedInputFile.canRead())
     {
       try
       {
         vlcIFace = new VlcInterface(selectedInputFile);
 
       } catch (java.lang.NoClassDefFoundError e)
       {
         System.out.println("Error: " + e.getMessage());
       }
     }
   }
 
   private class MediaControlsListener implements MouseListener
   {
 
     @Override
     public void mouseClicked(MouseEvent e)
     {
     }
 
     @Override
     public void mousePressed(MouseEvent e)
     {
     }
 
     @Override
     public void mouseReleased(MouseEvent e)
     {
 
       if (vlcIFace == null)
       {
         JOptionPane.showMessageDialog(null, "Please choose a file via the File menu");
       } else
       {
         JButton clickedButton = (JButton) e.getSource();
 
         switch (clickedButton.getText().toLowerCase())
         {
           case "play":
             vlcIFace.Play();
             break;
           case "forward":
             vlcIFace.Forward();
             break;
           case "rewind":
             vlcIFace.Rewind();
             break;
           case "stop":
             vlcIFace.Stop();
             break;
           case "pause":
             vlcIFace.Pause();
             break;
           case "snapshot":
             vlcIFace.Snapshot();
             snapshot = vlcIFace.LastSnapShot();
 
             outputPanel.DrawBufferedImage(snapshot);
             outputPanel.setPreferredSize(new Dimension(me.getWidth(), me.getHeight() - controlPanel.getHeight()));
             break;
         }
       }
     }
 
     @Override
     public void mouseEntered(MouseEvent e)
     {
     }
 
     @Override
     public void mouseExited(MouseEvent e)
     {
     }
   }
 
   private class UserInterfaceFrameMenuListener extends MouseAdapter
   {
     @Override
     public void mouseReleased(MouseEvent e)
     {
 
       switch (((JMenuItem)e.getSource()).getText().toLowerCase())
       {
         case "open":
           showFileSelect();
           break;
         case "exit":
           System.exit(0);
           break;
       }
     }
   }
 
   private class FrameListener implements ComponentListener
   {
 
     @Override
     public void componentResized(ComponentEvent e)
     {
       controlPanel.setPreferredSize(new Dimension(me.getWidth(), controlPanel.getHeight()));
       outputPanel.setPreferredSize(new Dimension(me.getWidth(), outputPanel.getHeight() - controlPanel.getHeight()));
       me.validate();
       me.repaint();
     }
 
     @Override
     public void componentMoved(ComponentEvent e)
     {
       System.out.println("Comp Mvd");
     }
 
     @Override
     public void componentShown(ComponentEvent e)
     {
       System.out.println("Comp Shwn");
     }
 
     @Override
     public void componentHidden(ComponentEvent e)
     {
       System.out.println("Comp Hid");
     }
   }
 }
