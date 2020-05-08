 package project.senior.app.pormmo;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 /**
  * @author John Fisher
  */
 public class UserInterfaceFrame extends JFrame
 {
 
     private JPanel controlPanel;
     private OutputPanel outputPanel;
     private File selectedInputFile;
     private GridBagLayout gl;
     private VlcInterface vlcIFace;
 
     public UserInterfaceFrame()
     {
         setPreferredSize(new Dimension(500, 500));
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         GridBagConstraints c = new GridBagConstraints();
             c.fill = GridBagConstraints.HORIZONTAL;
         gl = new GridBagLayout();
         setLayout(gl);
         
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
 
         outputPanel = new OutputPanel();
             outputPanel.setBackground(Color.green);
             outputPanel.setPreferredSize(new Dimension(500,500));
             
         controlPanel = new JPanel();
             controlPanel.setBackground(Color.blue);
             
         JButton playButton = new JButton("Play");
             playButton.addMouseListener(new MediaControlsListener());
             controlPanel.add(playButton);
         JButton stopButton = new JButton("Stop");
             stopButton.addMouseListener(new MediaControlsListener());
             controlPanel.add(stopButton);
         JButton pauseButton = new JButton("Pause");
             pauseButton.addMouseListener(new MediaControlsListener());
             controlPanel.add(pauseButton);
         JButton snapshotButton = new JButton("Snapshot");
             snapshotButton.addMouseListener(new MediaControlsListener());
             controlPanel.add(snapshotButton);
         
         
         c.gridx = 0;
         c.gridy = 0;
         add(outputPanel, c);
 
         c.gridx = 0;
         c.gridy = 1;
         add(controlPanel, c);
         
         pack();
         setVisible(true);
     }
 
     private void showFileSelect()
     {
         JFileChooser jFC = new JFileChooser();
         jFC.setAcceptAllFileFilterUsed(false);
         jFC.setFileFilter(new ExtFileFilter(new String[]{".mp4",".mpg",".avi",".wmv"}));
         jFC.setFileFilter(new ExtFileFilter(new String[]{".png",".jpg"}));       
 
         jFC.showDialog(this, "Open");
         selectedInputFile = jFC.getSelectedFile();  
         OpenSource();
     }
 
     private void OpenSource()
     {
         if(selectedInputFile.canRead())
         {
             try {
              vlcIFace = new VlcInterface(selectedInputFile);
             }
             catch (java.lang.NoClassDefFoundError e)
             {
                 System.out.println("Error: " + e.getMessage());
             }
         }
     }
 
     private class MediaControlsListener implements MouseListener{
 
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
             
             if(vlcIFace==null) JOptionPane.showMessageDialog(null, "Please choose a file via the File menu");
             else {
                 JButton clickedButton = (JButton)e.getSource();
 
                 switch (clickedButton.getText().toLowerCase()){
                     case "play":
                         vlcIFace.Play();
                         break;
                     case "stop":
                         vlcIFace.Stop();
                         break;
                     case "pause":
                         vlcIFace.Pause();
                         break;
                     case "snapshot":
                         BufferedImage snapshot;
                         
                         vlcIFace.Snapshot();
                         snapshot = vlcIFace.LastSnapShot();
                         Dimension imageSize = new Dimension(snapshot.getWidth(), snapshot.getHeight());
 
                         outputPanel.DrawBufferedImage(snapshot);
                         outputPanel.setPreferredSize(imageSize);
 
                        double outputWindowWidth = 100.0;
                        if(imageSize.getWidth()>controlPanel.getWidth()) outputWindowWidth = imageSize.getWidth();
                        else outputWindowWidth = controlPanel.getWidth();
                        setSize(new Dimension((int)outputWindowWidth, (int)imageSize.getHeight()+(controlPanel.getHeight()*3)));
                         
                         validate();
                         repaint();
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
             JMenuItem jMenuItem = (JMenuItem) e.getSource();
             switch (jMenuItem.getText().toLowerCase())
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
 }
